package ai.foresignal.source.adapters.meteoalarm;

import ai.foresignal.source.adapters.cap.CapAlertRecords;
import ai.foresignal.source.formats.cap.CapAlert;
import ai.foresignal.source.formats.cap.CapThreadResolver;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * One MeteoAlarm country (prompt 089): a conditional GET of the JSON API feed, converted to prompt 067's CAP
 * model and threaded with the shared update chain.
 *
 * <p>The JSON API was verified live on 2026-10-14: {@code {"warnings":[{"alert":{...CAP as JSON...},"uuid":…}]}}.
 * No ETag is sent ({@code Cache-Control: must-revalidate}), so the conditional GET re-reads the body each run
 * and the thread resolver decides what changed.
 */
public final class MeteoAlarmAdapter implements PollingAdapter {

    /** The default API base. */
    public static final String DEFAULT_BASE_URL = "https://feeds.meteoalarm.org";

    /** Test-only override of the API base. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.meteoalarm.base-url";

    static final Duration RETENTION = Duration.ofDays(1825);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final MeteoAlarmCountries.Country country;
    private final URI feedUrl;
    private final SourceDescriptor descriptor;

    /** Creates the adapter for one country under the given API base. */
    public MeteoAlarmAdapter(MeteoAlarmCountries.Country country, String baseUrl) {
        this.country = country;
        String base = baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl.strip();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        this.feedUrl = URI.create(base + "/api/v1/warnings/feeds-" + country.slug());
        this.descriptor = build(country);
    }

    /** The reviewed country this adapter serves. */
    public MeteoAlarmCountries.Country country() {
        return country;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(feedUrl, "meteoalarm:index");
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200) {
            throw new UpstreamFormatException("The MeteoAlarm feed of " + country.slug() + " answered HTTP "
                    + response.status(), "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(response.body());
        } catch (RuntimeException e) {
            throw new UpstreamFormatException("The MeteoAlarm feed of " + country.slug() + " is not JSON", "", e);
        }
        JsonNode warnings = root.get("warnings");
        if (warnings == null || !warnings.isArray()) {
            throw new UpstreamFormatException("The MeteoAlarm feed of " + country.slug()
                    + " has no warnings array", "");
        }
        CapThreadResolver resolver = CapThreadResolver.load(ctx.state(), ctx.now());
        List<NormalizedRecord> records = new ArrayList<>();
        int skipped = 0;
        for (JsonNode warning : warnings) {
            JsonNode alert = warning.get("alert");
            if (alert == null) {
                continue;
            }
            CapAlert parsed;
            try {
                parsed = MeteoAlarmJsonAdapter.convert(alert);
            } catch (UpstreamFormatException e) {
                // One malformed warning (no sent time, not an object) is skipped; the rest of the country is kept.
                skipped++;
                continue;
            }
            String thread = resolver.resolve(parsed, null, ctx.now());
            NormalizedRecord record = CapAlertRecords.map(parsed, thread,
                    List.of("en-GB", "en"), false);
            if (record != null) {
                records.add(record);
            }
        }
        resolver.save(ctx.state());
        if (skipped > 0) {
            ctx.log().warn("{}: skipped {} malformed warning(s)", ctx.sourceId(), skipped);
        }
        return FetchResult.of(records);
    }

    private static SourceDescriptor build(MeteoAlarmCountries.Country country) {
        return SourceDescriptor.builder(country.id())
                .name("MeteoAlarm " + country.name())
                .description("MeteoAlarm (EUMETNET) CAP warnings for " + country.name() + " (" + country.iso2()
                        + ") from the JSON API feeds-" + country.slug() + ". Converted to the shared CAP model; "
                        + "Update and Cancel messages follow the shared update chain. Geocode-only areas carry no "
                        + "geometry (no EMMA_ID boundary is bundled).")
                .kind(CapAlertRecords.KIND)
                .mode(Mode.POLLING)
                .homepage("https://www.meteoalarm.org")
                .attribution("MeteoAlarm / EUMETNET")
                .licence("MeteoAlarm / EUMETNET free re-use with attribution")
                .termsUrl("https://www.meteoalarm.org/en/live/page/terms-of-use")
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(15))
                .defaultBudget(2, 200)
                .maxResponseBytes(32 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .entityKey("cap-identifier", "identifier")
                .searchFields("event", "headline", "areaDesc")
                .embedFields("headline", "description")
                .zeroResultsNormal(true)
                .kindDisplay(CapAlertRecords.KIND, new KindDisplay("{event}", "warning", "#E8590C", null))
                .build();
    }
}
