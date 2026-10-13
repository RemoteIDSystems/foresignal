package ai.foresignal.source.adapters.env;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.MappedRecord;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.locationtech.jts.geom.Envelope;

/**
 * What the prompt 078 environmental sources share: the test base-URL property, the {@code maxAge} parameter and
 * the newest-per-entity {@link Collector}, the skip reasons, unit codes and small numeric helpers. Not an adapter;
 * the adapters under {@code env.*} use it and never import each other.
 */
public final class EnvSources {

    /**
     * The test-only property moving every prompt 078 polling URL (not the LAN monitors) under one base as
     * {@code {base}/{host}{path}}; blank in production.
     */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.env.base-url";

    /** The counter of skipped measurements, tagged {@code source} and {@code reason}. */
    public static final String SKIPPED_METRIC = "foresignal.env.skipped";

    /** The description of {@link #SKIPPED_METRIC}. */
    public static final String SKIPPED_DESCRIPTION = "Environmental measurements the parsers skipped";

    /** The parameter that drops measurements older than the given age. */
    public static final String MAX_AGE_PARAM = "maxAge";

    /** The skip reason of a measurement older than {@code maxAge}. */
    public static final String TOO_OLD = "too-old";

    /** The skip reason of a measurement without a usable location. */
    public static final String MISSING_LOCATION = "missing-location";

    /** The skip reason of an entry that carries no value this source stores. */
    public static final String NO_VALUES = "no-values";

    /** The skip reason of an entry outside the operator's area filter. */
    public static final String OUTSIDE_AREA = "outside-area";

    /** The prefix of a gzipped, base64-encoded adapter state value. */
    public static final String GZIP_PREFIX = "gz:";

    /** State values longer than this are stored gzipped. */
    static final int GZIP_ABOVE = 32 * 1024;

    private EnvSources() {}

    /**
     * {@code url} moved under {@code baseOverride} as {@code {base}/{host}{path}}, or unchanged when the override
     * is blank.
     */
    public static URI uri(String url, String baseOverride) {
        return URI.create(KitUrls.rebased(url, baseOverride));
    }

    /**
     * The {@code maxAge} parameter.
     *
     * @param defaultText the default as a duration text, for example {@code 6h}
     */
    public static ParamSpec maxAgeParam(String defaultText) {
        return ParamSpec.optional(MAX_AGE_PARAM, ParamType.DURATION, "Measurements older than this (by their"
                + " measurement time) are skipped and counted; default " + defaultText, defaultText);
    }

    /** The {@code maxAge} parameter's value, or {@code fallback} when it is unset or not positive. */
    public static Duration maxAge(ValidatedParams params, Duration fallback) {
        Duration value = params == null || !params.has(MAX_AGE_PARAM) ? null : params.duration(MAX_AGE_PARAM);
        return value == null || value.isZero() || value.isNegative() ? fallback : value;
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

    /** {@code value} rounded half-up to {@code places} decimals, without {@code -0.0}; null and non-finite stay null. */
    public static Double round(Double value, int places) {
        if (value == null || !Double.isFinite(value)) {
            return null;
        }
        double rounded = BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
        return rounded == 0.0 ? 0.0 : rounded;
    }

    /** True when {@code lat}/{@code lon} are finite, in range and not exactly (0, 0) (Core-Data-Model §3). */
    public static boolean validPoint(Double lat, Double lon) {
        return lat != null && lon != null && Double.isFinite(lat) && Double.isFinite(lon) && Math.abs(lat) <= 90
                && Math.abs(lon) <= 180 && !(lat == 0.0 && lon == 0.0);
    }

    /** True when the point lies in {@code box} (inclusive); a null box contains everything. */
    public static boolean inBox(Envelope box, double lon, double lat) {
        return box == null || box.isNull() || box.covers(lon, lat);
    }

    /**
     * An upstream unit as a UCUM code: {@code µg/m³} and {@code UG/M3} become {@code ug/m3}, {@code ppm} and
     * {@code ppb} become {@code [ppm]} and {@code [ppb]}, {@code mg/m³} becomes {@code mg/m3}; anything else is
     * returned trimmed, unchanged.
     *
     * @return the code, or null for a blank unit
     */
    public static String ucum(String unit) {
        if (unit == null || unit.isBlank()) {
            return null;
        }
        String u = unit.strip();
        String key = u.toLowerCase(Locale.ROOT).replace("µ", "u").replace("μ", "u").replace("³", "3")
                .replace(" ", "");
        return switch (key) {
            case "ug/m3" -> "ug/m3";
            case "mg/m3" -> "mg/m3";
            case "ppm" -> "[ppm]";
            case "ppb" -> "[ppb]";
            case "ppt" -> "[ppt]";
            default -> u;
        };
    }

    /**
     * A state value, gzipped and base64-encoded behind {@value #GZIP_PREFIX} when longer than 32 KB.
     */
    public static String pack(String text) {
        if (text == null || text.length() <= GZIP_ABOVE) {
            return text;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
            gz.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return GZIP_PREFIX + Base64.getEncoder().encodeToString(out.toByteArray());
    }

    /** The text of a state value written by {@link #pack}; null stays null. */
    public static String unpack(String value) {
        if (value == null || !value.startsWith(GZIP_PREFIX)) {
            return value;
        }
        byte[] packed = Base64.getDecoder().decode(value.substring(GZIP_PREFIX.length()));
        try (GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(packed))) {
            return new String(gz.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * One parsed response: the records, newest per entity, and the skip reasons to count.
     *
     * @param records one record per entity, sorted by external id
     * @param skipped skip reason to count
     */
    public record Parsed(List<NormalizedRecord> records, Map<String, Integer> skipped) {

        /** Copies both. */
        public Parsed {
            records = records == null ? List.of() : List.copyOf(records);
            skipped = skipped == null ? Map.of() : Map.copyOf(new TreeMap<>(skipped));
        }
    }

    /**
     * Collects mapped entries into one record per entity: the entry with the newest measurement time wins
     * ({@code observedAt}, else {@code eventTime}); a repeated entity with the same or an older time is counted as
     * {@value MappedRecord#DUPLICATE_ID}. With a {@code now} and a {@code maxAge}, an accepted entry measured more
     * than {@code maxAge} before {@code now} is counted as {@value #TOO_OLD} instead.
     */
    public static final class Collector {

        private final Instant now;
        private final Duration maxAge;
        private final Map<String, NormalizedRecord> byId = new LinkedHashMap<>();
        private final Map<String, Integer> skipped = new TreeMap<>();

        /**
         * Creates a collector.
         *
         * @param now the fetch time, or null to keep every age
         * @param maxAge the oldest measurement kept, or null to keep every age
         */
        public Collector(Instant now, Duration maxAge) {
            this.now = now;
            this.maxAge = maxAge;
        }

        /** Counts one skipped entry. */
        public void skip(String reason) {
            skipped.merge(reason, 1, Integer::sum);
        }

        /** Adds one mapped entry (see the class note). */
        public void add(MappedRecord mapped) {
            if (!mapped.accepted()) {
                skip(mapped.reason());
                return;
            }
            NormalizedRecord record = mapped.record();
            Instant at = time(record);
            if (now != null && maxAge != null && at != null && at.isBefore(now.minus(maxAge))) {
                skip(TOO_OLD);
                return;
            }
            NormalizedRecord previous = byId.get(record.externalId());
            if (previous == null) {
                byId.put(record.externalId(), record);
                return;
            }
            Instant before = time(previous);
            if (at != null && (before == null || at.isAfter(before))) {
                byId.put(record.externalId(), record);
            }
            skip(MappedRecord.DUPLICATE_ID);
        }

        /** The records, sorted by external id, and the skip counts. */
        public Parsed result() {
            List<NormalizedRecord> records = new ArrayList<>(byId.values());
            records.sort(Comparator.comparing(NormalizedRecord::externalId));
            return new Parsed(records, skipped);
        }

        private static Instant time(NormalizedRecord record) {
            return record.observedAt() != null ? record.observedAt() : record.eventTime();
        }
    }
}
