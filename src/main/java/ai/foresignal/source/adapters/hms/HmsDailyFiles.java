package ai.foresignal.source.adapters.hms;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.SourceHttp;
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
import java.util.Locale;
import java.util.function.BiFunction;

/**
 * The daily HMS files shared by {@code hms-smoke} and {@code hms-fire} (prompt 048).
 *
 * <ul>
 *   <li>Smoke: {@code {host}/pub/FIRE/web/HMS/Smoke_Polygons/KML/{YYYY}/{MM}/hms_smoke{YYYYMMDD}.kml}
 *   <li>Fire: {@code {host}/pub/FIRE/web/HMS/Fire_Points/Text/{YYYY}/{MM}/hms_fire{YYYYMMDD}.txt}
 * </ul>
 *
 * <p>Each run fetches today's file (UTC) and, during the first {@value #YESTERDAY_HOURS} hours after
 * midnight, yesterday's too, because analysts finish it late. Every fetch is a conditional GET (both
 * files carry ETag and Last-Modified, verified 2026-10-13). A 404 is normal for today before the first
 * analysis and is skipped. A 304 changes nothing. Any other failure, or a truncated or unparseable body,
 * fails the run <em>before</em> anything is tombstoned, and forgets the validators of every file this run
 * received, so the next run fetches them again.
 */
final class HmsDailyFiles {

    /** The production host. */
    static final String DEFAULT_BASE_URL = "https://satepsanone.nesdis.noaa.gov";

    /** Hours after UTC midnight during which yesterday's file is fetched again. */
    static final int YESTERDAY_HOURS = 6;

    static final DateTimeFormatter COMPACT = DateTimeFormatter.BASIC_ISO_DATE;

    private HmsDailyFiles() {}

    /**
     * One received and parsed file.
     *
     * @param day the file's day
     * @param stateKey its validator key
     * @param value what the parser made of it
     * @param <T> the parsed type
     */
    record Parsed<T>(LocalDate day, String stateKey, T value) {}

    /**
     * The outcome of one run.
     *
     * @param parsed the files received with a 200 and parsed, oldest day first
     * @param notModified files that answered 304
     * @param missing files that answered 404
     * @param <T> the parsed type
     */
    record Outcome<T>(List<Parsed<T>> parsed, int notModified, int missing) {}

    /** The days to fetch at {@code now}: yesterday (during the first hours after midnight) and today. */
    static List<LocalDate> days(Instant now) {
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
        Instant midnight = today.atStartOfDay().toInstant(ZoneOffset.UTC);
        if (now.isBefore(midnight.plus(Duration.ofHours(YESTERDAY_HOURS)))) {
            return List.of(today.minusDays(1), today);
        }
        return List.of(today);
    }

    /** The smoke file URL of {@code day} under {@code base}. */
    static URI smokeUri(String base, LocalDate day) {
        return URI.create(String.format(Locale.ROOT, "%s/pub/FIRE/web/HMS/Smoke_Polygons/KML/%04d/%02d/hms_smoke%s.kml",
                base, day.getYear(), day.getMonthValue(), COMPACT.format(day)));
    }

    /** The fire file URL of {@code day} under {@code base}. */
    static URI fireUri(String base, LocalDate day) {
        return URI.create(String.format(Locale.ROOT, "%s/pub/FIRE/web/HMS/Fire_Points/Text/%04d/%02d/hms_fire%s.txt",
                base, day.getYear(), day.getMonthValue(), COMPACT.format(day)));
    }

    /** {@code base} without a trailing slash, or the production host when blank. */
    static String base(String override) {
        String chosen = override == null || override.isBlank() ? DEFAULT_BASE_URL : override.trim();
        return chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    /**
     * Fetches and parses the files of {@code days}.
     *
     * @param uri the URL of a day's file
     * @param statePrefix the validator key prefix ({@code smoke}, {@code fire}); the key is
     *     {@code {prefix}:{yyyymmdd}}
     * @param parser parses a day's body; throws {@link UpstreamFormatException} when it cannot
     * @throws AdapterException when a fetch fails other than with a 404
     * @throws UpstreamFormatException when a body is truncated or cannot be parsed
     */
    static <T> Outcome<T> fetch(FetchContext ctx, List<LocalDate> days, java.util.function.Function<LocalDate, URI> uri,
            String statePrefix, BiFunction<LocalDate, byte[], T> parser) throws AdapterException {
        List<Parsed<T>> parsed = new ArrayList<>();
        int notModified = 0;
        int missing = 0;
        for (LocalDate day : days) {
            String stateKey = statePrefix + ":" + COMPACT.format(day);
            UpstreamResponse response;
            try {
                response = ctx.http().getConditional(uri.apply(day), stateKey);
            } catch (AdapterException e) {
                if (e.httpStatus().orElse(0) == 404) {
                    ctx.log().debug("HMS {} for {} is not published yet (404)", statePrefix, day);
                    missing++;
                    continue;
                }
                forget(ctx, parsed);
                throw e;
            }
            if (response.notModified()) {
                notModified++;
                continue;
            }
            try {
                if (response.truncated()) {
                    throw new UpstreamFormatException("HMS " + statePrefix + " file of " + day
                            + " exceeded maxResponseBytes and was truncated; nothing is tombstoned", null);
                }
                parsed.add(new Parsed<>(day, stateKey, parser.apply(day, response.body())));
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), stateKey);
                forget(ctx, parsed);
                throw e;
            }
        }
        return new Outcome<>(parsed, notModified, missing);
    }

    private static void forget(FetchContext ctx, List<? extends Parsed<?>> parsed) {
        for (Parsed<?> p : parsed) {
            SourceHttp.forgetValidators(ctx.state(), p.stateKey());
        }
    }
}
