package ai.foresignal.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.ItemRejection;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@link MappingEngine} and {@link MappingInputs} (prompt 059). */
class MappingEngineTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant RECEIVED = Instant.parse("2026-10-13T15:05:00Z");
    static final MappingContext SENSOR = new MappingContext(Set.of("sensor"), ObservedAtMode.UPSTREAM, true);

    final MappingCompiler compiler = new MappingCompiler();
    final MappingEngine engine = new MappingEngine(JSON);

    static JsonNode json(String text) {
        return JSON.readTree(text.replace('\'', '"'));
    }

    MappingResult apply(String mapping, String doc) {
        return apply(mapping, json(doc), SENSOR);
    }

    MappingResult apply(String mapping, JsonNode doc, MappingContext context) {
        return engine.apply(doc, compiler.compile(json(mapping), context), RECEIVED);
    }

    static String base(String extra) {
        return "{'version':1,'externalId':{'path':'$.id'},'observedAt':{'path':'$.ts','format':'iso'}"
                + (extra.isEmpty() ? "" : "," + extra) + "}";
    }

    @Test
    @DisplayName("select produces one record per array item, with item-relative paths and the item index")
    void selectArray() {
        MappingResult r = apply("{'version':1,'select':'$.msgs[*]','externalId':{'path':'$.mmsi'},"
                + "'observedAt':{'path':'$.t','format':'epochS'},'fields':[{'name':'sog','path':'$.speed','type':'number'}]}",
                "{'msgs':[{'mmsi':1,'t':1791903600,'speed':3},{'nommsi':true},{'mmsi':'3','t':1791903601.5,'speed':'4.5'}]}");
        assertThat(r.items()).isEqualTo(3);
        assertThat(r.records()).extracting(NormalizedRecord::externalId).containsExactly("1", "3");
        assertThat(r.indexes()).containsExactly(0, 2);
        assertThat(r.rejections()).extracting(ItemRejection::reason).containsExactly("EXTERNAL_ID_INVALID");
        assertThat(r.records().get(1).observedAt()).isEqualTo(Instant.parse("2026-10-13T15:00:01.500Z"));
        assertThat(r.records().get(1).payload()).containsEntry("sog", 4.5);
        assertThat(r.toParsedIngest().items()).isEqualTo(3);
    }

    @Test
    @DisplayName("select over a missing array yields no items; a single selected object is one item")
    void selectEdges() {
        String mapping = "{'version':1,'select':'$.items','externalId':{'path':'$.id'},'observedAt':{'received':true}}";
        MappingContext received = new MappingContext(Set.of("sensor"), ObservedAtMode.RECEIVED, true);
        assertThat(apply(mapping, json("{'other':1}"), received).items()).isZero();
        assertThat(apply(mapping, json("{'items':{'id':'x'}}"), received).records()).hasSize(1);
    }

    @Test
    @DisplayName("templates substitute values and are absent when a placeholder is missing")
    void templates() {
        String mapping = "{'version':1,'externalId':{'template':'gmc-{{$.GID}}-{{$.n}}'},'observedAt':{'received':true}}";
        MappingContext received = new MappingContext(Set.of("sensor"), ObservedAtMode.RECEIVED, true);
        MappingResult ok = apply(mapping, json("{'GID':'0987','n':7}"), received);
        assertThat(ok.records().getFirst().externalId()).isEqualTo("gmc-0987-7");
        assertThat(ok.records().getFirst().observedAt()).isEqualTo(RECEIVED);
        assertThat(apply(mapping, json("{'GID':'0987'}"), received).rejections())
                .extracting(ItemRejection::reason).containsExactly("EXTERNAL_ID_INVALID");
    }

    @Test
    @DisplayName("pairs flatten name/value arrays; fields and quality read $pairs.<key>; a repeated name keeps its first value")
    void pairs() {
        MappingResult r = apply("{'version':1,'externalId':{'template':'airrohr-{{$.esp8266id}}'},'observedAt':{'received':true},"
                + "'pairs':{'path':'$.sensordatavalues','key':'value_type','value':'value'},"
                + "'fields':[{'name':'pm25','path':'$pairs.SDS_P2','type':'number'},{'name':'pm25','path':'$pairs.PMS_P2','type':'number'},"
                + "{'name':'pressureHpa','path':'$pairs.BME280_pressure','type':'number','unit':{'from':'Pa','to':'hPa'}}],"
                + "'quality':{'extra':{'wifi.rssi':{'path':'$pairs.signal'}}}}",
                json("{'esp8266id':'9','sensordatavalues':[{'value_type':'PMS_P2','value':'7.5'},{'value_type':'BME280_pressure','value':'100000'},"
                        + "{'value_type':'signal','value':'-70'}]}"),
                new MappingContext(Set.of("sensor"), ObservedAtMode.RECEIVED, true));
        NormalizedRecord record = r.records().getFirst();
        assertThat(record.externalId()).isEqualTo("airrohr-9");
        assertThat(record.payload()).containsEntry("pm25", 7.5).containsEntry("pressureHpa", 1000.0);
        assertThat(record.quality().extra()).containsEntry("wifi.rssi", -70.0);
    }

    @Test
    @DisplayName("units, scale and offset apply in that order; integer rounds; known table values")
    void units() {
        String fields = String.join(",",
                field("tempC", "tempf", "number", "[degF]", "Cel"),
                field("wind", "mph", "number", "[mi_i]/h", "m/s"),
                field("kmh", "kmh", "number", "km/h", "m/s"),
                field("kn", "kn", "number", "[kn_i]", "m/s"),
                field("rain", "in", "number", "[in_i]", "mm"),
                field("rate", "inh", "number", "[in_i]/h", "mm/h"),
                field("p", "inhg", "number", "[in_i'Hg]", "hPa"),
                field("pa", "pa", "number", "Pa", "hPa"),
                field("mi", "mi", "number", "[mi_i]", "km"),
                field("ft", "ft", "integer", "[ft_i]", "m"),
                "{\"name\":\"cpm\",\"path\":\"$.cpm\",\"type\":\"number\",\"unit\":{\"from\":\"/min\",\"to\":\"/min\"},"
                        + "\"scale\":2,\"offset\":1}");
        JsonNode mapping = JSON.readTree("{\"version\":1,\"externalId\":{\"path\":\"$.id\"},"
                + "\"observedAt\":{\"path\":\"$.ts\",\"format\":\"iso\"},\"fields\":[" + fields + "]}");
        MappingResult r = engine.apply(json("{'id':'a','ts':'2026-10-13T15:00:00Z','tempf':50,'mph':10,'kmh':18,'kn':10,'in':1,"
                + "'inh':2,'inhg':30,'pa':98000,'mi':2,'ft':100,'cpm':10}"), compiler.compile(mapping, SENSOR), RECEIVED);
        Map<String, Object> p = r.records().getFirst().payload();
        assertThat((Double) p.get("tempC")).isCloseTo(10.0, within(1e-9));
        assertThat((Double) p.get("wind")).isCloseTo(4.4704, within(1e-9));
        assertThat((Double) p.get("kmh")).isCloseTo(5.0, within(1e-9));
        assertThat((Double) p.get("kn")).isCloseTo(5.144444444, within(1e-6));
        assertThat((Double) p.get("rain")).isCloseTo(25.4, within(1e-9));
        assertThat((Double) p.get("rate")).isCloseTo(50.8, within(1e-9));
        assertThat((Double) p.get("p")).isCloseTo(1015.9167, within(1e-3));
        assertThat((Double) p.get("pa")).isCloseTo(980.0, within(1e-9));
        assertThat((Double) p.get("mi")).isCloseTo(3.218688, within(1e-9));
        assertThat(p.get("ft")).isEqualTo(30L);
        assertThat((Double) p.get("cpm")).isCloseTo(21.0, within(1e-9));
    }

    /** One field with a unit, as JSON text. */
    static String field(String name, String key, String type, String from, String to) {
        return "{\"name\":\"" + name + "\",\"path\":\"$." + key + "\",\"type\":\"" + type + "\",\"unit\":{\"from\":\""
                + from + "\",\"to\":\"" + to + "\"}}";
    }

    @Test
    @DisplayName("sentinels and absent values take the default, or leave the field out")
    void sentinelsAndDefaults() {
        MappingResult r = apply(base("'fields':[{'name':'heading','path':'$.h','type':'number','sentinels':[511]},"
                + "{'name':'lvl','path':'$.lvl','type':'number','sentinels':['-9999'],'default':0},"
                + "{'name':'mode','path':'$.mode','type':'string','default':'auto'},"
                + "{'name':'name','path':'$.name','type':'string','sentinels':['']}]"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z','h':511,'lvl':-9999,'name':''}");
        assertThat(r.records().getFirst().payload()).containsOnly(Map.entry("lvl", 0.0), Map.entry("mode", "auto"));
    }

    @Test
    @DisplayName("type coercion: numeric strings, booleans, integers; NaN, infinity and junk reject only that field")
    void coercion() {
        MappingResult r = apply(base("'fields':[{'name':'n','path':'$.n','type':'number'},"
                + "{'name':'nan','path':'$.nan','type':'number'},{'name':'inf','path':'$.inf','type':'number'},"
                + "{'name':'junk','path':'$.junk','type':'number'},{'name':'i','path':'$.i','type':'integer'},"
                + "{'name':'frac','path':'$.frac','type':'integer'},{'name':'b','path':'$.b','type':'boolean'},"
                + "{'name':'b2','path':'$.b2','type':'boolean'},{'name':'s','path':'$.s','type':'string'},"
                + "{'name':'obj','path':'$.obj','type':'string'},{'name':'big','path':'$.big','type':'number','scale':1e308}]"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z','n':' 12.5 ','nan':'NaN','inf':'Infinity','junk':'abc','i':'7',"
                        + "'frac':7.5,'b':'on','b2':0,'s':42,'obj':{'x':1},'big':10}");
        Map<String, Object> p = r.records().getFirst().payload();
        assertThat(p).containsEntry("n", 12.5).containsEntry("i", 7L).containsEntry("b", true).containsEntry("b2", false)
                .containsEntry("s", "42").doesNotContainKeys("nan", "inf", "junk", "frac", "obj", "big");
        assertThat(r.warnings()).extracting(MappingWarning::code).contains(MappingWarning.FIELD_REJECTED);
        assertThat(r.warnings()).extracting(MappingWarning::path)
                .contains("mapping.fields[1]", "mapping.fields[2]", "mapping.fields[3]", "mapping.fields[5]",
                        "mapping.fields[9]", "mapping.fields[10]");
    }

    @Test
    @DisplayName("filter conditions skip and count items: ==, !=, exists, in, with numeric strings")
    void filters() {
        String mapping = "{'version':1,'select':'$.e[*]','externalId':{'path':'$.id'},'observedAt':{'path':'$.ts','format':'iso'},"
                + "'filter':[{'path':'$.type','op':'in','value':[1,2,3]},{'path':'$.bad','op':'exists','value':false},"
                + "{'path':'$.state','op':'!=','value':'test'},{'path':'$.v','op':'==','value':5}]}";
        MappingResult r = apply(mapping, "{'e':[{'id':'a','ts':'2026-10-13T15:00:00Z','type':'1','state':'ok','v':'5'},"
                + "{'id':'b','ts':'2026-10-13T15:00:00Z','type':9,'v':5},{'id':'c','ts':'2026-10-13T15:00:00Z','type':2,'bad':1,'v':5},"
                + "{'id':'d','ts':'2026-10-13T15:00:00Z','type':3,'state':'test','v':5},{'id':'e','ts':'2026-10-13T15:00:00Z','type':3,'v':6}]}");
        assertThat(r.records()).extracting(NormalizedRecord::externalId).containsExactly("a");
        assertThat(r.skipped()).isEqualTo(4);
        assertThat(r.rejections()).isEmpty();
    }

    @Test
    @DisplayName("drop keys never reach the payload, even through spread")
    void drop() {
        MappingResult r = apply(base("'drop':['secret','longitude'],'spread':{'path':'$.obj','type':'number','max':3}"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z','obj':{'secret':1,'longitude':2,'temp':3,'hum':'4','label':'x',"
                        + "'nested':{'a':1},'bad-name':5,'more':6,'evenmore':7}}");
        assertThat(r.records().getFirst().payload()).containsOnly(Map.entry("temp", 3.0), Map.entry("hum", 4.0),
                Map.entry("more", 6.0));
    }

    @Test
    @DisplayName("tombstone by equals and by in")
    void tombstone() {
        MappingResult equals = apply(base("'tombstone':{'path':'$.status','equals':'deleted'}"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z','status':'deleted','x':1}");
        assertThat(equals.records().getFirst().tombstone()).isTrue();
        assertThat(equals.records().getFirst().payload()).isEmpty();
        MappingResult in = apply(base("'tombstone':{'path':'$.code','in':[410,404]}"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z','code':'404'}");
        assertThat(in.records().getFirst().tombstone()).isTrue();
        MappingResult alive = apply(base("'tombstone':{'path':'$.code','in':[410]}"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z','code':200}");
        assertThat(alive.records().getFirst().tombstone()).isFalse();
    }

    @Test
    @DisplayName("geometry variants: lon/lat, numeric strings, out of range with fallback, GeoJSON, constant, invalid GeoJSON")
    void geometry() {
        Point p = (Point) apply(base("'geometry':{'lon':{'path':'$.lon'},'lat':{'path':'$.lat'}}"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z','lon':'-97.5','lat':30.25}").records().getFirst().geometry();
        assertThat(p.getX()).isEqualTo(-97.5);
        assertThat(p.getY()).isEqualTo(30.25);

        MappingResult sentinel = apply(base("'geometry':{'lon':{'path':'$.lon'},'lat':{'path':'$.lat'},'fallback':[1,2]}"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z','lon':181,'lat':91}");
        assertThat(((Point) sentinel.records().getFirst().geometry()).getX()).isEqualTo(1.0);
        assertThat(sentinel.warnings()).extracting(MappingWarning::code).contains(MappingWarning.GEOMETRY_OUT_OF_RANGE);

        MappingResult noFallback = apply(base("'geometry':{'lon':{'path':'$.lon'},'lat':{'path':'$.lat'}}"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z','lon':181,'lat':91}");
        assertThat(noFallback.records().getFirst().geometry()).isNull();

        MappingResult missing = apply(base("'geometry':{'lon':{'path':'$.lon'},'lat':{'path':'$.lat'},'fallback':[3,4]}"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z'}");
        assertThat(((Point) missing.records().getFirst().geometry()).getY()).isEqualTo(4.0);

        MappingResult geojson = apply(base("'geometry':{'geojson':'$.g'}"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z','g':{'type':'LineString','coordinates':[[0,1],[2,3]]}}");
        assertThat(geojson.records().getFirst().geometry().getGeometryType()).isEqualTo("LineString");

        MappingResult constant = apply(base("'geometry':{'const':[-97.74,30.27]}"), "{'id':'a','ts':'2026-10-13T15:00:00Z'}");
        assertThat(((Point) constant.records().getFirst().geometry()).getX()).isEqualTo(-97.74);

        MappingResult invalid = apply(base("'geometry':{'geojson':'$.g'}"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z','g':{'type':'Blob'}}");
        assertThat(invalid.rejections()).extracting(ItemRejection::reason).containsExactly("INVALID_ITEM");
    }

    @Test
    @DisplayName("time formats: ISO with offset, zone-less ISO in a zone, epoch ms, a pattern; a bad value rejects the item")
    void times() {
        String mapping = "{'version':1,'select':'$.r[*]','externalId':{'path':'$.id'},"
                + "'observedAt':{'path':'$.ts','format':'pattern:yyyy-MM-dd HH:mm:ss','zone':'America/Chicago'},"
                + "'eventTime':{'path':'$.ev','format':'epochMs'},'validUntil':{'path':'$.exp','format':'iso','zone':'UTC'}}";
        MappingResult r = apply(mapping, "{'r':[{'id':'a','ts':'2026-10-13 10:00:00','ev':1791903600000,'exp':'2026-10-13T18:00:00'},"
                + "{'id':'b','ts':'yesterday'}]}");
        NormalizedRecord a = r.records().getFirst();
        assertThat(a.observedAt()).isEqualTo(Instant.parse("2026-10-13T15:00:00Z"));
        assertThat(a.eventTime()).isEqualTo(Instant.parse("2026-10-13T15:00:00Z"));
        assertThat(a.validUntil()).isEqualTo(Instant.parse("2026-10-13T18:00:00Z"));
        assertThat(r.rejections()).extracting(ItemRejection::reason).containsExactly("INVALID_ITEM");
        MappingResult offset = apply(base(""), "{'id':'a','ts':'2026-10-13T10:00:00-05:00'}");
        assertThat(offset.records().getFirst().observedAt()).isEqualTo(Instant.parse("2026-10-13T15:00:00Z"));
        MappingResult absent = apply(base(""), "{'id':'a'}");
        assertThat(absent.records().getFirst().observedAt()).as("left to the normalizer").isNull();
    }

    @Test
    @DisplayName("an unknown kind, a missing kind with several source kinds, and a numeric external id")
    void kindsAndIds() {
        MappingContext two = new MappingContext(Set.of("a", "b"), ObservedAtMode.UPSTREAM, true);
        MappingResult r = apply("{'version':1,'select':'$.x[*]','externalId':{'path':'$.id'},'kind':{'path':'$.k'},"
                + "'observedAt':{'path':'$.ts','format':'iso'}}",
                json("{'x':[{'id':12345678901234567890,'k':'a','ts':'2026-10-13T15:00:00Z'},{'id':'q','k':'z','ts':'2026-10-13T15:00:00Z'},"
                        + "{'id':'r','ts':'2026-10-13T15:00:00Z'}]}"), two);
        assertThat(r.records().getFirst().externalId()).isEqualTo("12345678901234567890");
        assertThat(r.rejections()).extracting(ItemRejection::reason).containsExactly("UNKNOWN_KIND", "UNKNOWN_KIND");
    }

    @Test
    @DisplayName("quality: accuracy and namespaced extras; a negative accuracy is dropped")
    void quality() {
        MappingResult r = apply(base("'quality':{'accuracyMeters':{'path':'$.acc'},'extra':{'lora.rssi':{'path':'$.rssi'},"
                + "'lora.ok':{'path':'$.ok'},'lora.gw':{'path':'$.gw'}}}"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z','acc':'12','rssi':-90,'ok':true,'gw':'gw-1'}");
        var q = r.records().getFirst().quality();
        assertThat(q.accuracyMeters()).isEqualTo(12.0);
        assertThat(q.extra()).containsEntry("lora.rssi", -90.0).containsEntry("lora.ok", true).containsEntry("lora.gw", "gw-1");
        MappingResult negative = apply(base("'quality':{'accuracyMeters':{'path':'$.acc'}}"),
                "{'id':'a','ts':'2026-10-13T15:00:00Z','acc':-1}");
        assertThat(negative.records().getFirst().quality().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("form and query inputs become flat objects of strings; JSON stays JSON")
    void inputs() {
        IngestRequest form = new IngestRequest("tempf=68.4&dateutc=2026-10-13+15%3A04%3A05&tempf=99".getBytes(StandardCharsets.UTF_8),
                MediaType.APPLICATION_FORM_URLENCODED, new HttpHeaders(), Map.of("extra", List.of("1"), "tempf", List.of("0")),
                RECEIVED);
        JsonNode doc = MappingInputs.fromRequest(form, JSON);
        assertThat(doc.get("tempf").asString()).isEqualTo("68.4");
        assertThat(doc.get("dateutc").asString()).isEqualTo("2026-10-13 15:04:05");
        assertThat(doc.get("extra").asString()).isEqualTo("1");

        IngestRequest get = new IngestRequest(new byte[0], null, new HttpHeaders(), Map.of("CPM", List.of("18")), RECEIVED);
        assertThat(MappingInputs.fromRequest(get, JSON).get("CPM").asString()).isEqualTo("18");

        IngestRequest body = IngestRequest.of("{\"a\":[1]}".getBytes(StandardCharsets.UTF_8), MediaType.APPLICATION_JSON, RECEIVED);
        assertThat(MappingInputs.fromRequest(body, JSON).get("a").isArray()).isTrue();

        JsonNode sample = MappingInputs.fromSample(json("{'contentType':'application/x-www-form-urlencoded','body':'a=1&b=x%20y',"
                + "'query':{'c':'2'}}"), JSON);
        assertThat(sample.get("b").asString()).isEqualTo("x y");
        assertThat(sample.get("c").asString()).isEqualTo("2");
        assertThat(MappingInputs.fromSample(json("{'contentType':'text/plain','query':'ID=x&tempf=1'}"), JSON)
                .get("tempf").asString()).isEqualTo("1");
        assertThat(MappingInputs.fromSample(json("{'contentType':'application/json','body':{'k':1}}"), JSON)
                .get("k").asInt()).isEqualTo(1);
    }

    @Test
    @DisplayName("mapping equality compares numbers numerically and strings by text")
    void equality() {
        assertThat(MappingEngine.equal(json("{'a':'5.0'}").get("a"), json("{'a':5}").get("a"))).isTrue();
        assertThat(MappingEngine.equal(json("{'a':'x'}").get("a"), json("{'a':'x'}").get("a"))).isTrue();
        assertThat(MappingEngine.equal(json("{'a':'5'}").get("a"), json("{'a':'5.0'}").get("a"))).isFalse();
        assertThat(MappingEngine.equal(null, json("{'a':null}").get("a"))).isTrue();
    }
}
