package ai.foresignal.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.source.spi.ObservedAtMode;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@link MappingCompiler}: every validation rule, each error with its path (prompt 059). */
class MappingCompilerTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final MappingContext ONE_KIND = new MappingContext(Set.of("sensor"), ObservedAtMode.UPSTREAM, true);

    final MappingCompiler compiler = new MappingCompiler();

    static JsonNode json(String text) {
        return JSON.readTree(text.replace('\'', '"'));
    }

    /** A valid mapping with {@code extra} members spliced in. */
    static JsonNode mapping(String extra) {
        return json("{'version':1,'externalId':{'path':'$.id'},'observedAt':{'path':'$.ts','format':'iso'}"
                + (extra.isEmpty() ? "" : "," + extra) + "}");
    }

    List<String> errorFields(JsonNode mapping, MappingContext context) {
        try {
            compiler.compile(mapping, context);
        } catch (MappingInvalidException e) {
            assertThat(e.getCode()).isEqualTo(ErrorCodes.MAPPING_INVALID);
            return e.getErrors().stream().map(FieldError::field).toList();
        }
        return List.of();
    }

    @Test
    @DisplayName("a minimal mapping compiles, the single kind is the default and referenced keys are recorded")
    void minimal() {
        CompiledMapping m = compiler.compile(mapping("'fields':[{'name':'t','path':'$.temp','type':'number'}]"), ONE_KIND);
        assertThat(m.defaultKind()).isEqualTo("sensor");
        assertThat(m.referencedKeys()).contains("id", "ts", "temp");
        assertThat(m.fieldNames()).containsExactly("t");
        assertThat(m.warnings()).isEmpty();
    }

    @Test
    @DisplayName("not an object, wrong version and unknown members")
    void shape() {
        assertThat(errorFields(json("[1]"), ONE_KIND)).containsExactly("mapping");
        assertThat(errorFields(json("{'version':2,'externalId':{'path':'$.id'},'observedAt':{'received':true}}"),
                new MappingContext(Set.of("sensor"), ObservedAtMode.RECEIVED, true))).containsExactly("mapping.version");
        assertThat(errorFields(mapping("'script':'x'"), ONE_KIND)).containsExactly("mapping.script");
    }

    @Test
    @DisplayName("externalId and observedAt are required; kind is required without a single source kind")
    void required() {
        List<String> fields = errorFields(json("{'version':1}"), MappingContext.none());
        assertThat(fields).contains("mapping.externalId", "mapping.observedAt", "mapping.kind");
        assertThat(errorFields(mapping(""), new MappingContext(Set.of("a", "b"), ObservedAtMode.UPSTREAM, true)))
                .containsExactly("mapping.kind");
        assertThat(errorFields(mapping("'kind':{'const':'other'}"), ONE_KIND)).containsExactly("mapping.kind.const");
    }

    @Test
    @DisplayName("a value spec is exactly one of path, template or const")
    void valueSpecs() {
        assertThat(errorFields(json("{'version':1,'externalId':{'path':'$.id','const':'x'},"
                + "'observedAt':{'path':'$.ts','format':'iso'}}"), ONE_KIND)).contains("mapping.externalId");
        assertThat(errorFields(json("{'version':1,'externalId':'$.id','observedAt':{'path':'$.ts','format':'iso'}}"),
                ONE_KIND)).containsExactly("mapping.externalId");
        assertThat(errorFields(json("{'version':1,'externalId':{'expr':'1+1'},'observedAt':{'path':'$.ts','format':'iso'}}"),
                ONE_KIND)).contains("mapping.externalId.expr");
        assertThat(errorFields(json("{'version':1,'externalId':{'template':'fixed'},'observedAt':{'path':'$.ts','format':'iso'}}"),
                ONE_KIND)).containsExactly("mapping.externalId.template");
        assertThat(errorFields(json("{'version':1,'externalId':{'const':{'a':1}},'observedAt':{'path':'$.ts','format':'iso'}}"),
                ONE_KIND)).containsExactly("mapping.externalId.const");
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"$..id", "$.a..b", "$.items.length()", "$.items[?(@.name.length() > 2)]",
        "$.items[?(@.name =~ /a.*/)]", "id", "$.[", ""})
    @DisplayName("deep scan, function calls, regex filters and malformed paths are rejected")
    void rejectedPaths(String path) {
        JsonNode m = json("{'version':1,'externalId':{'path':'" + path + "'},'observedAt':{'path':'$.ts','format':'iso'}}");
        assertThat(errorFields(m, ONE_KIND)).containsExactly("mapping.externalId.path");
    }

    @Test
    @DisplayName("a quoted '..' or a quoted parenthesis is not a deep scan or a function call; plain filters are fine")
    void quotedPathsAllowed() {
        assertThat(errorFields(JSON.readTree("{\"version\":1,\"externalId\":{\"path\":\"$['a..b']\"},"
                + "\"observedAt\":{\"path\":\"$.ts\",\"format\":\"iso\"}}"), ONE_KIND)).isEmpty();
        assertThat(errorFields(JSON.readTree("{\"version\":1,\"externalId\":{\"path\":\"$.items[?(@.k == 'f(x)')].id\"},"
                + "\"observedAt\":{\"path\":\"$.ts\",\"format\":\"iso\"}}"), ONE_KIND)).isEmpty();
    }

    @Test
    @DisplayName("time specs: format, pattern, zone and received")
    void times() {
        assertThat(errorFields(json("{'version':1,'externalId':{'path':'$.id'},'observedAt':{'path':'$.ts'}}"), ONE_KIND))
                .containsExactly("mapping.observedAt.format");
        assertThat(errorFields(json("{'version':1,'externalId':{'path':'$.id'},'observedAt':{'path':'$.ts','format':'rfc'}}"),
                ONE_KIND)).containsExactly("mapping.observedAt.format");
        assertThat(errorFields(json("{'version':1,'externalId':{'path':'$.id'},"
                + "'observedAt':{'path':'$.ts','format':'pattern:yyyy-MM-dd {'}}"), ONE_KIND))
                .containsExactly("mapping.observedAt.format");
        assertThat(errorFields(json("{'version':1,'externalId':{'path':'$.id'},"
                + "'observedAt':{'path':'$.ts','format':'iso','zone':'Mars/Olympus'}}"), ONE_KIND))
                .containsExactly("mapping.observedAt.zone");
        assertThat(errorFields(mapping("'eventTime':{'received':false}"), ONE_KIND)).containsExactly("mapping.eventTime.received");
        assertThat(errorFields(mapping("'validUntil':{'path':'$.exp','format':'epochS','extra':1}"), ONE_KIND))
                .containsExactly("mapping.validUntil.extra");
    }

    @Test
    @DisplayName("the time base must agree with observedAtMode: an error when strict, a warning otherwise")
    void timeBase() {
        JsonNode received = json("{'version':1,'externalId':{'path':'$.id'},'observedAt':{'received':true}}");
        assertThat(errorFields(received, ONE_KIND)).containsExactly("mapping.observedAt");
        assertThat(errorFields(mapping(""), new MappingContext(Set.of("sensor"), ObservedAtMode.RECEIVED, true)))
                .containsExactly("mapping.observedAt");
        CompiledMapping lenient = compiler.compile(received,
                new MappingContext(Set.of("sensor"), ObservedAtMode.UPSTREAM, false));
        assertThat(lenient.warnings()).extracting(MappingWarning::code).containsExactly(MappingWarning.TIME_BASE_MISMATCH);
        assertThat(compiler.compile(received, new MappingContext(Set.of("sensor"), ObservedAtMode.RECEIVED, true))
                .warnings()).isEmpty();
    }

    @Test
    @DisplayName("field rules: name pattern, type, units, scale, offset, default, sentinels, drop, the 64-field cap")
    void fields() {
        String bad = "'drop':['secret'],'fields':["
                + "{'name':'1st','path':'$.a','type':'number'},"
                + "{'name':'ok','path':'$.a','type':'decimal'},"
                + "{'name':'t','path':'$.a','type':'number','unit':{'from':'K','to':'Cel'}},"
                + "{'name':'u','path':'$.a','type':'number','unit':{'from':'Cel','to':'[degF]'}},"
                + "{'name':'s','path':'$.a','type':'string','scale':2},"
                + "{'name':'v','path':'$.a','type':'number','default':{'x':1}},"
                + "{'name':'w','path':'$.a','type':'number','sentinels':5},"
                + "{'name':'secret','path':'$.a','type':'string'},"
                + "{'name':'x','path':'$.a','type':'string','unit':{'from':'m','to':'m'}},"
                + "{'name':'y','path':'$.a','type':'number','offset':'1'}]";
        assertThat(errorFields(mapping(bad), ONE_KIND)).containsExactly(
                "mapping.fields[0].name",
                "mapping.fields[1].type",
                "mapping.fields[2].unit.from",
                "mapping.fields[3].unit",
                "mapping.fields[4].scale",
                "mapping.fields[5].default",
                "mapping.fields[6].sentinels",
                "mapping.fields[7].name",
                "mapping.fields[8].unit",
                "mapping.fields[9].offset");
        StringBuilder many = new StringBuilder("'fields':[");
        for (int i = 0; i < 65; i++) {
            many.append(i == 0 ? "" : ",").append("{'name':'f").append(i).append("','path':'$.a','type':'number'}");
        }
        assertThat(errorFields(mapping(many + "]"), ONE_KIND)).containsExactly("mapping.fields");
        assertThat(errorFields(mapping("'fields':[{'name':'a_b9','path':'$.a','type':'integer',"
                + "'unit':{'from':'[ft_i]','to':'m'},'scale':2,'offset':1,'default':0,'sentinels':[-9999]}]"), ONE_KIND)).isEmpty();
    }

    @Test
    @DisplayName("geometry variants and their errors")
    void geometry() {
        assertThat(errorFields(mapping("'geometry':{'lon':{'path':'$.lon'},'lat':{'path':'$.lat'},'fallback':[1,2]}"),
                ONE_KIND)).isEmpty();
        assertThat(errorFields(mapping("'geometry':{'geojson':'$.g'}"), ONE_KIND)).isEmpty();
        assertThat(errorFields(mapping("'geometry':{'const':[-97.7,30.3]}"), ONE_KIND)).isEmpty();
        assertThat(errorFields(mapping("'geometry':null"), ONE_KIND)).isEmpty();
        assertThat(errorFields(mapping("'geometry':{'lon':{'path':'$.lon'}}"), ONE_KIND)).containsExactly("mapping.geometry");
        assertThat(errorFields(mapping("'geometry':{'const':[200,0]}"), ONE_KIND)).containsExactly("mapping.geometry.const");
        assertThat(errorFields(mapping("'geometry':{'const':[1,2],'lon':{'path':'$.x'}}"), ONE_KIND))
                .containsExactly("mapping.geometry");
        assertThat(errorFields(mapping("'geometry':{'geojson':'$.g','lat':{'path':'$.y'}}"), ONE_KIND))
                .containsExactly("mapping.geometry.lat");
        assertThat(errorFields(mapping("'geometry':{'lon':{'path':'$.x'},'lat':{'path':'$.y'},'fallback':[1]}"), ONE_KIND))
                .containsExactly("mapping.geometry.fallback");
        assertThat(errorFields(mapping("'geometry':'here'"), ONE_KIND)).containsExactly("mapping.geometry");
    }

    @Test
    @DisplayName("tombstone, quality, pairs, spread, drop and filter errors")
    void others() {
        assertThat(errorFields(mapping("'tombstone':{'path':'$.s'}"), ONE_KIND)).containsExactly("mapping.tombstone");
        assertThat(errorFields(mapping("'tombstone':{'path':'$.s','in':'gone'}"), ONE_KIND))
                .containsExactly("mapping.tombstone.in");
        assertThat(errorFields(mapping("'quality':{'extra':{'RSSI':{'path':'$.r'}}}"), ONE_KIND))
                .containsExactly("mapping.quality.extra.RSSI");
        assertThat(errorFields(mapping("'quality':{'confidence':{'const':1}}"), ONE_KIND))
                .containsExactly("mapping.quality.confidence");
        assertThat(errorFields(mapping("'pairs':{'path':'$.p','key':'a b','value':'v'}"), ONE_KIND))
                .containsExactly("mapping.pairs.key");
        assertThat(errorFields(mapping("'fields':[{'name':'pm','path':'$pairs.SDS_P1','type':'number'}]"), ONE_KIND))
                .containsExactly("mapping.pairs");
        assertThat(errorFields(mapping("'spread':{'path':'$.o','max':33}"), ONE_KIND)).containsExactly("mapping.spread.max");
        assertThat(errorFields(mapping("'spread':{'path':'$.o','type':'date'}"), ONE_KIND)).containsExactly("mapping.spread.type");
        assertThat(errorFields(mapping("'drop':['ok', 3]"), ONE_KIND)).containsExactly("mapping.drop[1]");
        assertThat(errorFields(mapping("'filter':[{'path':'$.a','op':'>'}]"), ONE_KIND)).containsExactly("mapping.filter[0].op");
        assertThat(errorFields(mapping("'filter':[{'path':'$.a','op':'in','value':1}]"), ONE_KIND))
                .containsExactly("mapping.filter[0].value");
        assertThat(errorFields(mapping("'filter':[{'path':'$.a','op':'==','value':[1]}]"), ONE_KIND))
                .containsExactly("mapping.filter[0].value");
        assertThat(errorFields(mapping("'filter':[{'path':'$.a','op':'exists','value':'yes'}]"), ONE_KIND))
                .containsExactly("mapping.filter[0].value");
        assertThat(errorFields(mapping("'filter':{'path':'$.a'}"), ONE_KIND)).containsExactly("mapping.filter");
    }

    @Test
    @DisplayName("every problem is reported at once")
    void collectsAll() {
        assertThatThrownBy(() -> compiler.compile(json("{'version':0,'externalId':{'path':'$..x'},'observedAt':{'path':'$.t',"
                + "'format':'x'},'fields':[{'name':'_','path':'$.a','type':'number'}]}"), ONE_KIND))
                .isInstanceOf(MappingInvalidException.class)
                .satisfies(e -> assertThat(((MappingInvalidException) e).getErrors()).extracting(FieldError::field)
                        .contains("mapping.version", "mapping.externalId.path", "mapping.observedAt.format",
                                "mapping.fields[0].name"));
    }

    @Test
    @DisplayName("first keys are taken from dotted and bracketed paths")
    void firstKey() {
        assertThat(MappingCompiler.firstKeyOf("$.a.b")).isEqualTo("a");
        assertThat(MappingCompiler.firstKeyOf("$['frm-payload'].x")).isEqualTo("frm-payload");
        assertThat(MappingCompiler.firstKeyOf("$.msgs[*]")).isEqualTo("msgs");
        assertThat(MappingCompiler.firstKeyOf("$[0]")).isNull();
        assertThat(MappingCompiler.firstKeyOf("$.*")).isNull();
    }
}
