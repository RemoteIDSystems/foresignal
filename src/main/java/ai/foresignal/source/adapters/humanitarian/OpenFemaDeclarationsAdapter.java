package ai.foresignal.source.adapters.humanitarian;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code openfema-declarations} (prompt 090): FEMA disaster declarations, one record per designated area.
 *
 * <p>Geometry is a 040 county polygon (a {@code 000} county code is the state polygon; a tribal or otherwise
 * unknown area has none). One record's worth of text (the declaration title) can reach an LLM context, so the
 * descriptor carries OpenFEMA's eligibility {@code llmNotice}, which the MCP output shaper appends.
 */
@Component
public final class OpenFemaDeclarationsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "openfema-declarations";

    /** The record kind. */
    public static final String KIND = "disaster-declaration";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://www.fema.gov";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.openfema-declarations.base-url";

    /** The OpenFEMA eligibility notice appended to every LLM context that includes these records. */
    public static final String LLM_NOTICE = "OpenFEMA data must not be used to determine eligibility for "
            + "assistance; direct people to DisasterAssistance.gov.";

    static final String CURSOR_KEY = "fema:lastRefresh";
    static final Duration RETENTION = Duration.ofDays(1825);
    static final int PAGE = 1_000;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final URI baseUrl;
    private final Supplier<CountyGeometries> counties;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    @Autowired
    public OpenFemaDeclarationsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<CountyGeometries> counties) {
        this(baseUrlOverride, lazy(counties));
    }

    /** Creates the adapter with plain counties (tests). */
    public OpenFemaDeclarationsAdapter(String baseUrlOverride, CountyGeometries counties) {
        this(baseUrlOverride, () -> counties == null ? CountyGeometries.none() : counties);
    }

    private OpenFemaDeclarationsAdapter(String baseUrlOverride, Supplier<CountyGeometries> counties) {
        this.baseUrl = URI.create(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.strip());
        this.counties = counties;
        this.descriptor = buildDescriptor();
    }

    private static Supplier<CountyGeometries> lazy(ObjectProvider<CountyGeometries> provider) {
        return provider == null ? CountyGeometries::none : () -> {
            CountyGeometries available = provider.getIfAvailable();
            return available == null ? CountyGeometries.none() : available;
        };
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        int backfillDays = ctx.params().integer("backfillDays", 365);
        String watermark = ctx.state().get(CURSOR_KEY).orElse(null);
        String filter = watermark != null
                ? "lastRefresh gt '" + watermark + "'"
                : "declarationDate ge '" + ctx.now().minus(Duration.ofDays(backfillDays)) + "'";
        List<NormalizedRecord> records = new ArrayList<>();
        String newest = watermark;
        int skip = 0;
        while (true) {
            UpstreamResponse response = ctx.http().get(URI.create(baseUrl
                    + "/api/open/v2/DisasterDeclarationsSummaries?$filter=" + encode(filter)
                    + "&$orderby=lastRefresh&$top=" + PAGE + "&$skip=" + skip));
            if (response.status() != 200) {
                throw new UpstreamFormatException("OpenFEMA answered HTTP " + response.status(), "");
            }
            JsonNode array = JSON.readTree(response.body()).path("DisasterDeclarationsSummaries");
            if (array == null || !array.isArray() || array.isEmpty()) {
                break;
            }
            for (JsonNode declaration : array) {
                NormalizedRecord record = record(declaration);
                if (record != null) {
                    records.add(record);
                }
                String refresh = text(declaration, "lastRefresh");
                if (refresh != null && (newest == null || refresh.compareTo(newest) > 0)) {
                    newest = refresh;
                }
            }
            if (array.size() < PAGE) {
                break;
            }
            skip += array.size();
        }
        if (newest != null) {
            ctx.state().put(CURSOR_KEY, newest);
        }
        return FetchResult.of(records);
    }

    /** The records of a DisasterDeclarationsSummaries body, using this adapter's county geometries. */
    public List<NormalizedRecord> parse(byte[] body) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (JsonNode declaration : JSON.readTree(body).path("DisasterDeclarationsSummaries")) {
            NormalizedRecord record = record(declaration);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private NormalizedRecord record(JsonNode declaration) {
        String id = text(declaration, "id");
        if (id == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .observedAt(time(text(declaration, "lastRefresh")))
                .eventTime(time(text(declaration, "incidentBeginDate")));
        Geometry geometry = geometry(declaration);
        if (geometry != null) {
            b.geometry(geometry);
        }
        b.put("femaDeclarationString", text(declaration, "femaDeclarationString"));
        b.put("disasterNumber", integer(declaration, "disasterNumber"));
        b.put("declarationType", text(declaration, "declarationType"));
        b.put("declarationDate", text(declaration, "declarationDate"));
        b.put("incidentType", text(declaration, "incidentType"));
        b.put("declarationTitle", text(declaration, "declarationTitle"));
        b.put("state", text(declaration, "state"));
        b.put("designatedArea", text(declaration, "designatedArea"));
        b.put("ihProgramDeclared", bool(declaration, "ihProgramDeclared"));
        b.put("iaProgramDeclared", bool(declaration, "iaProgramDeclared"));
        b.put("paProgramDeclared", bool(declaration, "paProgramDeclared"));
        b.put("hmProgramDeclared", bool(declaration, "hmProgramDeclared"));
        b.put("incidentEndDate", text(declaration, "incidentEndDate"));
        b.put("closeoutDate", text(declaration, "closeoutDate"));
        return b.build();
    }

    private Geometry geometry(JsonNode declaration) {
        CountyGeometries available = counties.get();
        if (available == null || !available.available()) {
            return null;
        }
        String stateFips = text(declaration, "fipsStateCode");
        String countyFips = text(declaration, "fipsCountyCode");
        if (stateFips == null || stateFips.length() != 2 || !stateFips.chars().allMatch(Character::isDigit)) {
            return null;
        }
        if (countyFips != null && countyFips.equals("000")) {
            return available.stateUnion(stateFips).orElse(null);
        }
        if (countyFips == null || !countyFips.chars().allMatch(Character::isDigit)) {
            return null;
        }
        return available.union(List.of(stateFips + countyFips)).orElse(null);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static Integer integer(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || !value.isNumber() ? null : value.asInt();
    }

    private static Boolean bool(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    static Instant time(String value) {
        return UsTravelAdvisoryAdapter.time(value);
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("FEMA disaster declarations")
                .description("FEMA disaster declaration summaries, one record per designated area, with the county "
                        + "or state polygon from the gazetteer. OpenFEMA terms forbid using the data to determine "
                        + "individual eligibility.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://www.fema.gov/about/openfema/data-sets")
                .attribution("OpenFEMA / Federal Emergency Management Agency")
                .licence("US Government work (public domain); OpenFEMA terms apply")
                .termsUrl("https://www.fema.gov/about/openfema/terms-conditions")
                .llmNotice(LLM_NOTICE)
                .param(ParamSpec.optional("backfillDays", ParamType.INTEGER,
                        "On the first run, declarations since now minus this many days", 365))
                .interval(Duration.ofHours(6), Duration.ofHours(6))
                .defaultBudget(10, 200)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .searchFields("declarationTitle", "designatedArea", "state")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{declarationTitle}", "shelter", "#1971C2", null))
                .build();
    }
}
