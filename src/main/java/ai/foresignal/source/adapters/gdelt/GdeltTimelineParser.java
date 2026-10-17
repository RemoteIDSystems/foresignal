package ai.foresignal.source.adapters.gdelt;

import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the GDELT DOC 2.0 API's timeline JSON (prompt 117): {@code mode=TimelineVolRaw} (matching article count per
 * step, with {@code norm}, all articles GDELT monitored in that step) and {@code mode=TimelineTone} (average tone per
 * step). Pure: no network, no state.
 *
 * <p>The step ("bucket") is the API's timeline resolution: 15 minutes for spans under 72 hours, hourly up to a week,
 * daily beyond (DOC 2.0 documentation). The parser takes {@code query_details.date_resolution} when the body states
 * it and otherwise infers the step from the spacing of the dates; it is recorded on every point as
 * {@code extra.resolution}. A body that is not JSON (GDELT answers a malformed query with HTTP 200 and a plain-text
 * message) is an {@link UpstreamFormatException} carrying the message.
 */
public final class GdeltTimelineParser {

    /** GDELT's compact timestamp: {@code 20261016T001500Z}. */
    static final DateTimeFormatter COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private GdeltTimelineParser() {}

    /**
     * One step of a timeline.
     *
     * @param start the step's start
     * @param value the count or the tone
     * @param norm all articles monitored in the step, or null (tone timelines carry none)
     */
    public record Bucket(Instant start, double value, Double norm) {}

    /**
     * A parsed timeline.
     *
     * @param buckets the steps, in date order
     * @param resolution the step length
     */
    public record Timeline(List<Bucket> buckets, Duration resolution) {

        /** Copies the buckets. */
        public Timeline {
            buckets = List.copyOf(buckets);
        }

        /** The Foresignal frequency code of the step: {@code PT15M}, {@code PT1H} or {@code P1D}. */
        public String frequency() {
            if (resolution.compareTo(Duration.ofMinutes(15)) <= 0) {
                return "PT15M";
            }
            if (resolution.compareTo(Duration.ofHours(1)) <= 0) {
                return "PT1H";
            }
            return "P1D";
        }

        /** The step as GDELT names it: {@code 15m}, {@code 1h} or {@code 1d}. */
        public String resolutionLabel() {
            return switch (frequency()) {
                case "PT15M" -> "15m";
                case "PT1H" -> "1h";
                default -> "1d";
            };
        }
    }

    /**
     * The timeline of a response body.
     *
     * @throws UpstreamFormatException when the body is not timeline JSON
     */
    public static Timeline parse(byte[] body) {
        if (body == null || body.length == 0) {
            return new Timeline(List.of(), Duration.ofMinutes(15));
        }
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            String text = new String(body, StandardCharsets.UTF_8).strip();
            throw new UpstreamFormatException("gdelt-timelines: GDELT did not answer with JSON: "
                    + (text.length() > 200 ? text.substring(0, 200) : text), null, e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("gdelt-timelines: the response is not a JSON object", null);
        }
        List<Bucket> buckets = new ArrayList<>();
        JsonNode timeline = root.get("timeline");
        if (timeline != null && timeline.isArray() && !timeline.isEmpty()) {
            JsonNode data = timeline.get(0).get("data");
            if (data != null && data.isArray()) {
                for (JsonNode entry : data) {
                    Instant start = date(JsonProps.text(entry, "date"));
                    Double value = JsonProps.number(entry, "value");
                    if (start == null || value == null) {
                        continue;
                    }
                    buckets.add(new Bucket(start, value, JsonProps.number(entry, "norm")));
                }
            }
        }
        buckets.sort(java.util.Comparator.comparing(Bucket::start));
        Duration stated = resolution(root.get("query_details") == null ? null
                : JsonProps.text(root.get("query_details"), "date_resolution"));
        return new Timeline(buckets, stated != null ? stated : inferred(buckets));
    }

    /** A {@code 15m}, {@code 1h}, {@code 1d} resolution label as a duration, or null. */
    static Duration resolution(String label) {
        if (label == null) {
            return null;
        }
        String l = label.strip().toLowerCase(java.util.Locale.ROOT);
        try {
            if (l.endsWith("min")) {
                return Duration.ofMinutes(Long.parseLong(l.substring(0, l.length() - 3)));
            }
            if (l.endsWith("m")) {
                return Duration.ofMinutes(Long.parseLong(l.substring(0, l.length() - 1)));
            }
            if (l.endsWith("h")) {
                return Duration.ofHours(Long.parseLong(l.substring(0, l.length() - 1)));
            }
            if (l.endsWith("d")) {
                return Duration.ofDays(Long.parseLong(l.substring(0, l.length() - 1)));
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    /** The smallest spacing between consecutive buckets; 15 minutes when fewer than two. */
    static Duration inferred(List<Bucket> buckets) {
        Duration smallest = null;
        for (int i = 1; i < buckets.size(); i++) {
            Duration gap = Duration.between(buckets.get(i - 1).start(), buckets.get(i).start());
            if (!gap.isZero() && !gap.isNegative() && (smallest == null || gap.compareTo(smallest) < 0)) {
                smallest = gap;
            }
        }
        return smallest == null ? Duration.ofMinutes(15) : smallest;
    }

    /** A GDELT compact or ISO timestamp, or null. */
    static Instant date(String text) {
        if (text == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, COMPACT).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            try {
                return Instant.parse(text);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    /**
     * The volume points of a topic: {@code gdelt.{topicId}.vol}, value = matching articles, {@code extra.total} =
     * all articles in the step, {@code extra.share} = count ÷ total.
     */
    public static List<NormalizedRecord> volume(String topicId, Timeline timeline, Instant observedAt) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (Bucket bucket : timeline.buckets()) {
            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("resolution", timeline.resolutionLabel());
            if (bucket.norm() != null) {
                extra.put("total", bucket.norm());
                if (bucket.norm() > 0) {
                    extra.put("share", round(bucket.value() / bucket.norm()));
                }
            }
            out.add(point(volumeSeries(topicId), "GDELT article volume: " + topicId, timeline, bucket,
                    "{articles}", extra, observedAt));
        }
        return out;
    }

    /**
     * The tone points of a topic: {@code gdelt.{topicId}.tone}, value = average tone. A step whose article count
     * is known to be zero has no tone and is left out.
     *
     * @param counts the article count per step start from the volume timeline, possibly empty
     */
    public static List<NormalizedRecord> tone(String topicId, Timeline timeline, Map<Instant, Double> counts,
            Instant observedAt) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (Bucket bucket : timeline.buckets()) {
            Double count = counts.get(bucket.start());
            if (count != null && count <= 0) {
                continue;
            }
            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("resolution", timeline.resolutionLabel());
            out.add(point(toneSeries(topicId), "GDELT average tone: " + topicId, timeline, bucket, "1", extra,
                    observedAt));
        }
        return out;
    }

    /** The article count per step start. */
    public static Map<Instant, Double> counts(Timeline volume) {
        Map<Instant, Double> out = new LinkedHashMap<>();
        for (Bucket bucket : volume.buckets()) {
            out.put(bucket.start(), bucket.value());
        }
        return out;
    }

    /** {@code gdelt.{topicId}.vol}. */
    public static String volumeSeries(String topicId) {
        return "gdelt." + topicId + ".vol";
    }

    /** {@code gdelt.{topicId}.tone}. */
    public static String toneSeries(String topicId) {
        return "gdelt." + topicId + ".tone";
    }

    /** The metadata of a GDELT series: attention or tone, no release lag. */
    public static SeriesMeta meta(NormalizedRecord point, String topicId) {
        boolean tone = String.valueOf(point.payload().get("series")).endsWith(".tone");
        return tone ? new SeriesMeta(SeriesMeta.TONE, SeriesMeta.LEVEL, 0, null, null, topicId)
                : new SeriesMeta(SeriesMeta.ATTENTION, SeriesMeta.LOG_LEVEL, 0, null, null, topicId);
    }

    private static NormalizedRecord point(String seriesId, String name, Timeline timeline, Bucket bucket,
            String unit, Map<String, Object> extra, Instant observedAt) {
        Instant end = bucket.start().plus(timeline.resolution());
        return SeriesPoint.point(seriesId, name, "gdelt-doc", timeline.frequency(), bucket.start().toString(),
                bucket.start(), end, bucket.value(), unit, null, extra, "final", observedAt);
    }

    private static double round(double value) {
        return Math.round(value * 1e8) / 1e8;
    }
}
