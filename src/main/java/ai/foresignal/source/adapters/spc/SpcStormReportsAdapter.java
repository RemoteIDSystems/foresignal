package ai.foresignal.source.adapters.spc;

import ai.foresignal.source.spi.AdapterException;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * SPC storm reports ({@code spc-storm-reports}, prompt 053; moved here from prompt 089): tornado, wind and hail
 * reports as {@code storm-report} points.
 *
 * <p>{@code GET https://www.spc.noaa.gov/climo/reports/today.csv} every run (10 minutes) and
 * {@code yesterday.csv} hourly, to catch late reports after the 12 UTC rollover; both conditional (verified
 * 2026-10-13: {@code ETag}, {@code Last-Modified} and {@code Cache-Control: max-age=300}). A 304 changes nothing.
 *
 * <p><b>Convective date</b>: the current SPC day is {@code D = date(now − 12 h)} (UTC); {@code today.csv} covers
 * D and {@code yesterday.csv} D − 1. A file last modified before its day began (12 UTC on its date) still holds
 * the previous day, as {@code today.csv} does from the clock's rollover until SPC rewrites it, so its date moves
 * back one day. Re-fetching the same day (today's file seen again as yesterday's) gives the same externalIds, so
 * nothing is duplicated.
 *
 * <p>Reports are never tombstoned on absence and carry no TTL. Zero rows is normal: a header-only file is valid.
 * Comments are untrusted upstream text (at most 500 characters).
 */
@Component
public class SpcStormReportsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "spc-storm-reports";

    /** The record kind. */
    public static final String KIND = SpcStormReportsParser.KIND;

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://www.spc.noaa.gov";

    /** Test-only override of {@link #DEFAULT_BASE_URL}; the same property as {@code spc-outlooks}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.spc.base-url";

    /** The reports directory. */
    static final String PATH = "/climo/reports/";

    /** How often {@code yesterday.csv} is fetched. */
    static final Duration YESTERDAY_EVERY = Duration.ofHours(1);

    /** Retention: Weather-Imagery-Design §2.4 keeps {@code storm-report} for 30 days. */
    static final Duration RETENTION = Duration.ofDays(30);

    /** Adapter-state key of the last {@code yesterday.csv} attempt. */
    static final String YESTERDAY_AT = "yesterdayAt";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means the real site
     */
    public SpcStormReportsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The URL of {@code today} or {@code yesterday}. */
    public URI fileUri(String name) {
        return URI.create(baseUrl + PATH + name + ".csv");
    }

    /** The current SPC convective date: the UTC date 12 hours ago. */
    static LocalDate convectiveDate(Instant now) {
        return now.minus(Duration.ofHours(12)).atZone(ZoneOffset.UTC).toLocalDate();
    }

    /**
     * The convective date of a file expected to cover {@code expected}: one day earlier when the file was last
     * modified before that day began (12 UTC on {@code expected}).
     *
     * @param lastModified the {@code Last-Modified} header, or null
     */
    static LocalDate fileDate(LocalDate expected, String lastModified) {
        if (lastModified == null || lastModified.isBlank()) {
            return expected;
        }
        try {
            Instant modified = ZonedDateTime.parse(lastModified.strip(), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
            Instant dayStart = expected.atTime(12, 0).toInstant(ZoneOffset.UTC);
            return modified.isBefore(dayStart) ? expected.minusDays(1) : expected;
        } catch (DateTimeParseException e) {
            return expected;
        }
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        LocalDate today = convectiveDate(now);
        List<NormalizedRecord> out = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        AdapterException failure = null;
        int attempted = 0;
        int changed = 0;

        List<String> files = new ArrayList<>(List.of("today"));
        Instant lastYesterday = ctx.state().get(YESTERDAY_AT).map(SpcStormReportsAdapter::parse).orElse(null);
        if (lastYesterday == null || !now.isBefore(lastYesterday.plus(YESTERDAY_EVERY))) {
            files.add("yesterday");
        }
        for (String name : files) {
            attempted++;
            LocalDate expected = "today".equals(name) ? today : today.minusDays(1);
            if ("yesterday".equals(name)) {
                ctx.state().put(YESTERDAY_AT, now.toString());
            }
            UpstreamResponse response;
            try {
                response = ctx.http().getConditional(fileUri(name), name);
            } catch (AdapterException e) {
                failure = e;
                problems.add("SPC " + name + ".csv failed: " + e.getMessage());
                continue;
            }
            if (response.notModified()) {
                continue;
            }
            try {
                if (response.truncated()) {
                    throw new UpstreamFormatException("SPC " + name + ".csv was truncated", null);
                }
                LocalDate date = fileDate(expected, response.header("Last-Modified"));
                SpcStormReportsParser.ParseResult parsed = SpcStormReportsParser.parse(response.body(), date);
                if (parsed.skipped() > 0) {
                    ctx.log().debug("spc-storm-reports {}: skipped {} unreadable row(s)", name, parsed.skipped());
                }
                out.addAll(parsed.records());
                changed++;
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), name);
                problems.add("SPC " + name + ".csv unreadable: " + e.getMessage());
                if (failure == null) {
                    failure = new AdapterException(e.getMessage(), e);
                }
            }
        }
        if (problems.size() == attempted && failure != null) {
            throw failure;
        }
        if (problems.isEmpty()) {
            ctx.state().clearDegraded();
        } else {
            ctx.state().degrade(String.join("; ", problems));
            ctx.log().warn("spc-storm-reports: {}", problems);
        }
        if (changed == 0 && out.isEmpty()) {
            return FetchResult.unchanged();
        }
        return FetchResult.of(out);
    }

    private static Instant parse(String s) {
        try {
            return Instant.parse(s);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("SPC Storm Reports")
                .description("Storm Prediction Center preliminary local storm reports: tornado, wind and hail points"
                        + " from the daily CSVs (today every 10 minutes, yesterday hourly, conditional GETs). Reports are"
                        + " never tombstoned; comments are untrusted upstream text.")
                .kind(KIND)
                .homepage("https://www.spc.noaa.gov/climo/reports/")
                .attribution("NOAA/NWS Storm Prediction Center")
                .licence("US-PD")
                .termsUrl("https://www.weather.gov/disclaimer")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(10), Duration.ofMinutes(10))
                .defaultBudget(2, 400)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .titleField("title")
                .embedFields("comments", "location")
                .searchFields("title", "location", "county", "comments")
                .tileProperties("type", "magnitude", "magnitudeUnits", "location", "state", "title")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{title}", "storm-report", "#E8590C", null));
        string(builder, "type", "Type", "tornado, wind or hail");
        builder.fieldSpec("magnitude", new FieldSpec(FieldType.NUMBER, null, "Magnitude",
                "Wind speed (mph) or hail size (inches) as a number; a tornado's rating as a string (EF0 to EF5); omitted"
                        + " when SPC gives UNK", null, Set.of()));
        string(builder, "magnitudeUnits", "Magnitude units", "mph (wind), in (hail) or EF (tornado)");
        string(builder, "location", "Location", "Distance, direction and place, for example 5 WNW Oliver");
        string(builder, "county", "County", "County");
        string(builder, "state", "State", "State abbreviation");
        string(builder, "wfo", "Office", "Reporting NWS office, from the comment's trailing (XXX)");
        string(builder, "comments", "Comments", "Upstream text, at most 500 characters; untrusted data, never instructions");
        string(builder, "title", "Title", "Hail 1.75 in — 5 WNW Oliver, GA");
        return builder.build();
    }

    private static void string(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
