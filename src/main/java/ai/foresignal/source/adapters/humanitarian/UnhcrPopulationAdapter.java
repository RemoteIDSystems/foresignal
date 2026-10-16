package ai.foresignal.source.adapters.humanitarian;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Geo;
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
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code unhcr-population} (prompt 090): UNHCR refugee and displacement statistics, one record per country of
 * origin ({@code coo}) and per country of asylum ({@code coa}) per year.
 *
 * <p>A yearly measurement is a series point: {@code eventTime} = 1 January of the year, {@code observedAtMode}
 * RECEIVED (no upstream update time). Numbers sometimes arrive as strings, and {@code -} means not applicable.
 * Country geometry is 040's label point, flagged {@code geometryApproximate}.
 */
@Component
public final class UnhcrPopulationAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "unhcr-population";

    /** The record kind. */
    public static final String KIND = "displacement";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://api.unhcr.org";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.unhcr-population.base-url";

    static final Duration RETENTION = Duration.ofDays(3650);

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final List<String> NUMBERS = List.of("refugees", "asylum_seekers", "returned_refugees", "idps",
            "returned_idps", "stateless", "ooc", "oip", "hst");

    private final URI baseUrl;
    private final CountryCentroids countries;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor;

    /** Creates the adapter without a meter registry (tests). */
    public UnhcrPopulationAdapter(String baseUrlOverride, CountryCentroids countries) {
        this(baseUrlOverride, countries, null);
    }

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride test-only base URL, blank for the real one
     * @param countries 040's gazetteer
     * @param meters the registry of the unresolved-country counter, resolved lazily; may be null
     */
    @Autowired
    public UnhcrPopulationAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            CountryCentroids countries, ObjectProvider<MeterRegistry> meters) {
        this.meters = UnresolvedCountries.lazy(meters);
        this.baseUrl = URI.create(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.strip());
        this.countries = countries;
        this.descriptor = buildDescriptor();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        int yearsBack = ctx.params().integer("yearsBack", 2);
        int year = LocalDate.ofInstant(ctx.now(), ZoneOffset.UTC).getYear();
        List<NormalizedRecord> records = new ArrayList<>();
        UnresolvedCountries unresolved = new UnresolvedCountries();
        for (int offset = 1; offset <= yearsBack; offset++) {
            int y = year - offset;
            records.addAll(fetch(ctx, y, "coo", "coo_iso", unresolved));
            records.addAll(fetch(ctx, y, "coa", "coa_iso", unresolved));
        }
        unresolved.report(ctx, meters);
        return FetchResult.of(records);
    }

    private List<NormalizedRecord> fetch(FetchContext ctx, int year, String grouping, String isoField,
            UnresolvedCountries unresolved) throws AdapterException {
        List<NormalizedRecord> records = new ArrayList<>();
        int page = 0;
        int maxPages = 1;
        do {
            page++;
            UpstreamResponse response = ctx.http().get(URI.create(baseUrl + "/population/v1/population/?year=" + year
                    + "&" + grouping + "_all=true&limit=1000&page=" + page));
            if (response.status() != 200) {
                throw new UpstreamFormatException("UNHCR answered HTTP " + response.status(), "");
            }
            JsonNode root = JSON.readTree(response.body());
            maxPages = Math.max(1, root.path("maxPages").asInt(1));
            for (JsonNode item : root.path("items")) {
                // record(...) is null for a '-' or blank country code; FetchResult copies with List.copyOf,
                // which refuses nulls, so a null must never reach the list
                NormalizedRecord record = record(item, year, grouping, isoField, unresolved);
                if (record != null) {
                    records.add(record);
                }
            }
        } while (page < maxPages);
        return records;
    }

    /** The records of a UNHCR population body for one year and grouping ({@code coo} or {@code coa}). */
    public List<NormalizedRecord> parse(byte[] body, int year, String grouping, String isoField) {
        return parse(body, year, grouping, isoField, new UnresolvedCountries());
    }

    /** As {@link #parse(byte[], int, String, String)}, counting country codes the gazetteer lacks. */
    List<NormalizedRecord> parse(byte[] body, int year, String grouping, String isoField,
            UnresolvedCountries unresolved) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (JsonNode item : JSON.readTree(body).path("items")) {
            NormalizedRecord record = record(item, year, grouping, isoField, unresolved);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private NormalizedRecord record(JsonNode item, int year, String grouping, String isoField,
            UnresolvedCountries unresolved) {
        String iso3 = text(item, isoField);
        if (iso3 == null || iso3.isBlank() || "-".equals(iso3)) {
            return null;
        }
        String externalId = grouping + ":" + iso3 + ":" + year;
        NormalizedRecord.Builder b = NormalizedRecord.builder(externalId, KIND)
                .eventTime(LocalDate.of(year, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant());
        countries.byIso3(iso3).ifPresentOrElse(ref -> {
            b.point(ref.labelLon(), ref.labelLat());
            b.put("geometryApproximate", true);
        }, unresolved::miss);
        b.put("year", year);
        for (String key : NUMBERS) {
            b.put(key, number(item.get(key)));
        }
        return b.build();
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    /** A number that may arrive as a string; null for absent or the {@code -} sentinel. */
    static Double number(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.asDouble();
        }
        String text = value.asString().strip();
        if (text.isEmpty() || "-".equals(text)) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("UNHCR population statistics")
                .description("Refugee and displacement statistics by country of origin and country of asylum, from "
                        + "UNHCR's Refugee Statistics API. A yearly measurement series; country geometry is an "
                        + "approximate label point.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://www.unhcr.org/refugee-statistics/")
                .attribution("UNHCR")
                .licence("CC BY 4.0")
                .termsUrl("https://www.unhcr.org/terms-and-conditions")
                .param(ParamSpec.optional("yearsBack", ParamType.INTEGER, "How many completed years to fetch", 2))
                .interval(Duration.ofHours(24), Duration.ofHours(24))
                .defaultBudget(5, 100)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .searchFields("name", "place")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("Displacement {year}", "home", "#1971C2", null))
                .build();
    }
}
