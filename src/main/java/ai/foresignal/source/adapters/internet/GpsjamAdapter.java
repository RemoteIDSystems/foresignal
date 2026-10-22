package ai.foresignal.source.adapters.internet;

import ai.foresignal.geo.H3Cells;
import ai.foresignal.source.adapters.sanctions.DelistingGuard;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Egress;
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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code gpsjam-daily} (prompt 092): daily GPS-jamming cells from gpsjam.org, H3 resolution 4.
 *
 * <p>A cell's bad fraction is the site's metric {@code pctBad = 100·(bad − 1)/(good + bad)}; cells below the
 * threshold or with fewer than three aircraft are dropped. The manifest date gates the fetch, so an unchanged
 * day fetches nothing. Cells present yesterday and absent today are tombstoned. The site publishes no licence
 * beyond "free to use", so the source is local-only. It needs no key, so it starts enabled (prompt 145).
 */
@Component
public final class GpsjamAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "gpsjam-daily";

    /** The record kind. */
    public static final String KIND = "jamming-cell";

    /** The default base URL. */
    public static final String BASE_URL = "https://gpsjam.org";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.gpsjam-daily.base-url";

    static final String DATE_KEY = "gpsjam:lastDate";
    static final String IDS_KEY = "gpsjam:ids";
    static final Duration RETENTION = Duration.ofDays(1825);
    static final double BAND = 0.5;

    /** The most requests one run sends: the manifest and one day file. */
    static final int REQUESTS_PER_RUN = 2;

    private final URI baseUrl;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    @Autowired
    public GpsjamAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = URI.create(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? BASE_URL : baseUrlOverride.strip());
        this.descriptor = buildDescriptor();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        boolean includeSuspect = ctx.params().bool("includeSuspect", false);
        double minPct = ctx.params().number("minPct") == null ? 2.0 : ctx.params().number("minPct");
        String last = ctx.state().get(DATE_KEY).orElse(null);
        Manifest manifest = manifest(ctx, includeSuspect, last);
        if (manifest == null) {
            return FetchResult.unchanged();
        }
        UpstreamResponse response = ctx.http().get(URI.create(baseUrl + "/data/" + manifest.date() + "-h3_4.csv"));
        if (response.status() != 200) {
            throw new UpstreamFormatException("gpsjam answered HTTP " + response.status(), "");
        }
        List<NormalizedRecord> records = parse(response.body(), manifest, minPct);
        Set<String> previous = new LinkedHashSet<>();
        ctx.state().get(IDS_KEY).orElse("").lines().filter(s -> !s.isBlank()).forEach(previous::add);
        Set<String> current = new LinkedHashSet<>();
        records.forEach(r -> current.add(r.externalId()));
        List<NormalizedRecord> out = new ArrayList<>(records);
        Instant observedAt = LocalDate.parse(manifest.date()).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        for (String id : DelistingGuard.gone(previous, current)) {
            out.add(NormalizedRecord.tombstone(id, KIND, observedAt));
        }
        ctx.state().put(IDS_KEY, String.join("\n", DelistingGuard.sorted(current)));
        ctx.state().put(DATE_KEY, manifest.date());
        return FetchResult.complete(out);
    }

    private Manifest manifest(FetchContext ctx, boolean includeSuspect, String last) throws AdapterException {
        UpstreamResponse response = ctx.http().get(URI.create(baseUrl + "/data/manifest.csv"));
        if (response.status() != 200) {
            throw new UpstreamFormatException("gpsjam manifest answered HTTP " + response.status(), "");
        }
        Manifest newest = null;
        for (String line : new String(response.body(), StandardCharsets.UTF_8).split("\r?\n")) {
            if (line.isBlank() || line.startsWith("date")) {
                continue;
            }
            String[] parts = line.split(",", -1);
            if (parts.length < 2) {
                continue;
            }
            boolean suspect = "true".equalsIgnoreCase(parts[1].strip());
            if (suspect && !includeSuspect) {
                continue;
            }
            if (last != null && parts[0].compareTo(last) <= 0) {
                continue;
            }
            if (newest == null || parts[0].compareTo(newest.date()) > 0) {
                newest = new Manifest(parts[0].strip(), parts.length > 3 ? parts[3].strip() : null);
            }
        }
        return newest;
    }

    /** The records of one {@code {date}-h3_4.csv} body. */
    public List<NormalizedRecord> parse(byte[] body, Manifest manifest, double minPct) {
        List<NormalizedRecord> out = new ArrayList<>();
        Instant eventTime = LocalDate.parse(manifest.date()).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant observedAt = eventTime.plus(Duration.ofDays(1));
        for (String line : new String(body, StandardCharsets.UTF_8).split("\r?\n")) {
            if (line.isBlank() || line.startsWith("hex")) {
                continue;
            }
            String[] parts = line.split(",", -1);
            if (parts.length < 3) {
                continue;
            }
            String hex = parts[0].strip();
            int good = intValue(parts[1]);
            int bad = intValue(parts[2]);
            if (good + bad < 3 || !H3Cells.isValid(hex)) {
                continue;
            }
            double pctBad = 100.0 * (bad - 1) / (good + bad);
            if (pctBad < minPct) {
                continue;
            }
            NormalizedRecord.Builder b = NormalizedRecord.builder("h3:" + hex, KIND)
                    .eventTime(eventTime)
                    .observedAt(observedAt)
                    .geometry(H3Cells.cellToPolygon(hex));
            b.put("date", manifest.date());
            b.put("pctBad", Math.round(pctBad / BAND) * BAND);
            b.put("level", level(pctBad));
            b.put("goodAircraft", good);
            b.put("badAircraft", bad);
            b.put("upstreamSource", manifest.source());
            out.add(b.build());
        }
        return out;
    }

    private static String level(double pctBad) {
        if (pctBad < 2) {
            return "low";
        }
        return pctBad > 10 ? "high" : "medium";
    }

    private static int intValue(String value) {
        try {
            return Integer.parseInt(value.strip());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** One manifest row: the date and its data source. */
    public record Manifest(String date, String source) {}

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("gpsjam daily jamming cells")
                .description("Daily aircraft-derived GPS jamming cells (H3 resolution 4) from gpsjam.org. The bad "
                        + "fraction uses the site's metric; the manifest date gates the fetch. The site publishes no "
                        + "license, so the source is local-only.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://gpsjam.org")
                .attribution("gpsjam.org (John Wiseman)")
                .licence("unspecified — personal use, do not redistribute (gpsjam.org)")
                .termsUrl("https://gpsjam.org/faq")
                .param(ParamSpec.optional("minPct", ParamType.NUMBER, "Minimum bad fraction (%) to keep a cell", 2.0))
                .param(ParamSpec.optional("includeSuspect", ParamType.BOOLEAN,
                        "Include days the site flags as suspect", false))
                .egress(Egress.LOCAL_ONLY)
                .interval(Duration.ofHours(6), Duration.ofHours(6))
                // A run with a new day sends two requests (the manifest, then the day file) back to back, and
                // the per-minute bucket holds perMinute tokens, so one per minute could never fetch a day.
                .defaultBudget(REQUESTS_PER_RUN, 8)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("Jamming {pctBad}%", "radiation", "#E8590C", null))
                .build();
    }
}
