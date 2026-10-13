package ai.foresignal.source.adapters.hydro.usdm;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.hydro.HydroSources;
import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code usdm} (prompt 079): the weekly U.S. Drought Monitor map, one simplified polygon per drought category
 * ({@link UsdmMapper}).
 *
 * <ul>
 *   <li><strong>Each run:</strong> a conditional GET of {@value #CURRENT_URL} (state key {@value #STATE_KEY}); the
 *       map is released on Thursdays, so six days of seven are a 304 ({@link FetchResult#unchanged()});
 *   <li><strong>Map date:</strong> the latest Tuesday on or before the file's {@code Last-Modified}, confirmed with a
 *       HEAD of {@code usdm_{yyyyMMdd}.json}; on a 404 the date steps back one week, once, and is logged; any other
 *       failure of the HEAD keeps the computed date;
 *   <li><strong>Records:</strong> one per category present, and an immediate tombstone for every category the map
 *       lacks. A body that does not parse forgets the validators so the next run reads it again.
 * </ul>
 */
@Component
public class UsdmAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "usdm";

    /** The current map. */
    public static final String CURRENT_URL = "https://droughtmonitor.unl.edu/data/json/usdm_current.json";

    /** The dated map, with {@code {date}} as {@code yyyyMMdd}. */
    public static final String DATED_URL = "https://droughtmonitor.unl.edu/data/json/usdm_{date}.json";

    /** The conditional GET state key. */
    public static final String STATE_KEY = "usdm-current";

    /**
     * The required attribution, as the USDM permission page words it (read 2026-10-13: it now names NASA as a fourth
     * partner).
     */
    public static final String ATTRIBUTION = "The U.S. Drought Monitor is jointly produced by the National Drought"
            + " Mitigation Center at the University of Nebraska-Lincoln, the United States Department of"
            + " Agriculture, the National Oceanic and Atmospheric Administration and the National Aeronautics and"
            + " Space Administration. Map courtesy of NDMC.";

    /** The response cap: the 2026-10-06 map was 23.8 MB, uncompressed (the server sends no gzip). */
    public static final int MAX_RESPONSE_BYTES = 48 * 1024 * 1024;

    private static final DateTimeFormatter COMPACT = DateTimeFormatter.BASIC_ISO_DATE;

    private final URI current;
    private final String dated;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value HydroSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public UsdmAdapter(@Value("${" + HydroSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.current = HydroSources.uri(CURRENT_URL, baseUrlOverride);
        this.dated = HydroSources.uri(DATED_URL.replace("{date}", "DATE"), baseUrlOverride).toString();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The dated map URL of {@code date}. */
    public URI datedUri(LocalDate date) {
        return URI.create(dated.replace("DATE", date.format(COMPACT)));
    }

    /**
     * Reads the current map conditionally and maps it.
     *
     * @throws AdapterException when the map request fails
     * @throws UpstreamFormatException when the map does not parse (its validators are forgotten)
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(current, STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        byte[] body = EnvSources.body(ctx, response, STATE_KEY, current);
        Instant lastModified = UsdmMapper.httpDate(response.header("last-modified"));
        LocalDate date = confirm(ctx, UsdmMapper.mapDate(lastModified == null ? ctx.now() : lastModified));
        List<NormalizedRecord> records;
        try {
            records = UsdmMapper.map(HydroSources.json(body, "USDM map"), date);
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        List<NormalizedRecord> out = new ArrayList<>(records);
        out.addAll(UsdmMapper.tombstones(UsdmMapper.CATEGORIES,
                records.stream().map(NormalizedRecord::externalId).toList(),
                date.atStartOfDay(ZoneOffset.UTC).toInstant()));
        ctx.log().debug("{}: map of {}, categories {}", ID, date,
                records.stream().map(NormalizedRecord::externalId).toList());
        return FetchResult.complete(out);
    }

    /**
     * {@code computed}, confirmed by a HEAD of its dated file; one week earlier on a 404.
     */
    LocalDate confirm(FetchContext ctx, LocalDate computed) {
        URI uri = datedUri(computed);
        try {
            UpstreamResponse head = ctx.http().head(uri);
            if (head.status() == 200) {
                return computed;
            }
            ctx.log().warn("{}: HEAD {} answered {}; keeping map date {}", ID, uri.getPath(), head.status(), computed);
            return computed;
        } catch (AdapterException e) {
            if (e.httpStatus().orElse(0) == 404) {
                LocalDate earlier = computed.minusWeeks(1);
                ctx.log().warn("{}: no dated map for {} (404); using {}", ID, computed, earlier);
                return earlier;
            }
            ctx.log().warn("{}: the map date {} could not be confirmed ({}); keeping it", ID, computed,
                    e.getMessage());
            return computed;
        } catch (UnsupportedOperationException e) {
            return computed;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = UsdmMapper.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("U.S. Drought Monitor")
                .description("The weekly U.S. Drought Monitor map: one area per drought category, D0 (abnormally dry)"
                        + " to D4 (exceptional drought), valid for the Tuesday before the Thursday release. The"
                        + " geometry is simplified to fit the 10,000-vertex limit (geometrySimplified,"
                        + " simplifyToleranceDeg); a category missing from a new map is retired at once.")
                .kind(kind)
                .homepage("https://droughtmonitor.unl.edu/")
                .attribution(ATTRIBUTION)
                .licence("Reproduction permitted with the required attribution (USDM permission page)")
                .termsUrl("https://droughtmonitor.unl.edu/About/Permission.aspx")
                .mode(Mode.POLLING)
                .interval(Duration.ofHours(1), Duration.ofHours(6))
                .defaultBudget(Budget.of(2, 10, 100))
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .fetchTimeout(Duration.ofMinutes(3))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .defaultRetention(HydroSources.EVENT_RETENTION)
                .kindRetention(kind, HydroSources.EVENT_RETENTION)
                .tileProperties("category", "label")
                .searchFields("label", "category")
                .titleField("label");
        HydroSources.text(b, "category", "Category", "D0 to D4");
        HydroSources.text(b, "label", "Label", "Abnormally Dry, Moderate Drought, Severe Drought, Extreme Drought or"
                + " Exceptional Drought");
        b.fieldSpec("mapDate", new FieldSpec(FieldType.STRING, null, "Map date", "The map's valid date (a Tuesday),"
                + " yyyy-MM-dd", null, Set.of()));
        HydroSources.bool(b, FeatureGeometry.SIMPLIFIED_KEY, "Geometry simplified", "True when the polygon was"
                + " simplified to fit the 10,000-vertex limit");
        HydroSources.number(b, FeatureGeometry.TOLERANCE_KEY, "deg", "Simplification tolerance", "The tolerance the"
                + " polygon was simplified with");
        b.kindDisplay(kind, new KindDisplay("{label}", null, "#E8590C", null));
        return b.build();
    }
}
