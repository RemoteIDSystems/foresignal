package ai.foresignal.source.adapters.space;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Locale;

/**
 * What the prompt 077 space and ground-station sources share: the test base-URL property, URL rebasing, the
 * conditional GET of one file, and the upstream time formats. Not an adapter; the adapters under
 * {@code space.*} use it and never import each other.
 */
public final class SpaceSources {

    /**
     * The test-only property moving every prompt 077 polling URL under one base as {@code {base}/{host}{path}};
     * blank in production.
     */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.space.base-url";

    /** {@code 2026-10-13 14:31:37.343} or {@code 2026-10-13T14:47:00}, with an optional fraction; UTC. */
    private static final DateTimeFormatter LOCAL = new DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd")
            .optionalStart().appendLiteral('T').optionalEnd()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .appendPattern("HH:mm")
            .optionalStart().appendPattern(":ss").optionalEnd()
            .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd()
            .toFormatter(Locale.ROOT);

    private SpaceSources() {}

    /**
     * {@code url} moved under {@code baseOverride} as {@code {base}/{host}{path}}, or unchanged when the override
     * is blank.
     */
    public static URI uri(String url, String baseOverride) {
        return URI.create(KitUrls.rebased(url, baseOverride));
    }

    /**
     * One conditional GET of a file whose body must be read (prompt 077: every SWPC file sends {@code ETag} and
     * {@code Last-Modified}).
     *
     * @param ctx the fetch context
     * @param uri the file
     * @param stateKey the validator state key
     * @return the body, or null when the upstream answered 304
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException on a status other than 200 or a truncated body; the validators are forgotten
     *     first, so the next run asks again unconditionally
     */
    public static byte[] conditional(FetchContext ctx, URI uri, String stateKey) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(uri, stateKey);
        if (response.notModified()) {
            return null;
        }
        return body(ctx, response, stateKey, uri);
    }

    /**
     * The body of a 200 response that was not cut at the response cap.
     *
     * @param stateKey the validator state key to forget on failure, or null for an unconditional request
     * @throws UpstreamFormatException otherwise
     */
    public static byte[] body(FetchContext ctx, UpstreamResponse response, String stateKey, URI uri) {
        if (response.status() != 200 || response.truncated()) {
            if (stateKey != null) {
                SourceHttp.forgetValidators(ctx.state(), stateKey);
            }
            throw new UpstreamFormatException(ctx.sourceId() + ": " + uri.getPath() + (response.status() != 200
                    ? " answered HTTP " + response.status() : " exceeds the response cap"), "");
        }
        return response.body();
    }

    /**
     * An upstream time: ISO-8601 with an offset ({@code 2026-10-13T20:37:00Z}), a minute-precision ISO time with
     * a zone ({@code 2026-10-06T05:38Z}, DONKI), or a local date-time in UTC ({@code 2026-10-13T14:47:00},
     * {@code 2026-10-13 14:31:37.343}, {@code 2026-10-13 14:31}).
     *
     * @return the instant, or null when {@code text} is null or unreadable
     */
    public static Instant utc(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String s = text.strip();
        try {
            return OffsetDateTime.parse(s).toInstant();
        } catch (DateTimeParseException e) {
            // fall through to the local forms
        }
        if (s.endsWith("Z")) {
            s = s.substring(0, s.length() - 1);
        }
        try {
            return LocalDateTime.parse(s, LOCAL).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(s).atStartOfDay(ZoneOffset.UTC).toInstant();
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    /** {@code value} rounded half-up to the nearest multiple of {@code step}, without {@code -0.0}. */
    public static double roundTo(double value, double step) {
        BigDecimal steps = BigDecimal.valueOf(value).divide(BigDecimal.valueOf(step), 0, RoundingMode.HALF_UP);
        double rounded = steps.multiply(BigDecimal.valueOf(step)).doubleValue();
        return rounded == 0.0 ? 0.0 : rounded;
    }

    /** {@code value} rounded half-up to {@code places} decimals, without {@code -0.0}; null stays null. */
    public static Double round(Double value, int places) {
        if (value == null || !Double.isFinite(value)) {
            return null;
        }
        double rounded = BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
        return rounded == 0.0 ? 0.0 : rounded;
    }

    /** {@code text} cut to at most {@code max} characters and trimmed, or null when blank. */
    public static String cut(String text, int max) {
        if (text == null) {
            return null;
        }
        String s = text.strip();
        if (s.length() > max) {
            s = s.substring(0, max).strip();
        }
        return s.isEmpty() ? null : s;
    }
}
