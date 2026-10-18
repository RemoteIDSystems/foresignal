package ai.foresignal.czml;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Builds a CZML document (prompt 141; the CZML guide's packet structure) from already-filtered entities.
 *
 * <p>The first packet is the document packet ({@code id: "document"}, {@code version: "1.0"}): its {@code clock}
 * spans the requested range and starts at its beginning, and its {@code description} carries the attribution of the
 * data. Every entity then gets one packet:
 *
 * <ul>
 *   <li>{@code position}: an {@code epoch} plus sampled {@code cartographicDegrees}
 *       {@code [t, lon, lat, h, t, lon, lat, h, …]}, {@code t} in seconds after the epoch; an entity with one sample
 *       gets a constant {@code [lon, lat, h]} instead, because a sampled property with one sample is only defined at
 *       that instant;
 *   <li>{@code availability}: first sample to last sample;
 *   <li>{@code label}: the title;
 *   <li>{@code point}: the kind's {@code kindDisplay} color (an unknown or malformed color falls back to
 *       {@value #DEFAULT_COLOR}).
 * </ul>
 *
 * <p>The output is a list of ordered maps, so a {@code JsonMapper} writes it in a stable member order (the golden
 * test relies on it). Coordinates are rounded to 7 decimals, heights to 2 and sample offsets to milliseconds.
 */
public final class CzmlWriter {

    /** The most entity packets in one document. */
    public static final int MAX_ENTITIES = 5000;

    /** The CZML media type. */
    public static final String MEDIA_TYPE = "application/czml+json";

    /** The point color of a kind without a usable {@code kindDisplay} color. */
    public static final String DEFAULT_COLOR = "#3388FF";

    private static final Pattern HEX = Pattern.compile("^#?[0-9a-fA-F]{6}$");

    private CzmlWriter() {}

    /**
     * One position sample.
     *
     * @param time the sample time
     * @param lon longitude in degrees
     * @param lat latitude in degrees
     * @param height height above the ellipsoid in meters (0 when unknown)
     */
    public record Sample(Instant time, double lon, double lat, double height) {

        /** Requires a time. */
        public Sample {
            Objects.requireNonNull(time, "time");
        }
    }

    /**
     * One entity.
     *
     * @param id the packet id, {@code {sourceId}:{externalId}}
     * @param title the label text (the record title; the id when there is none)
     * @param kind the record kind, kept in the packet's {@code properties}
     * @param color the {@code kindDisplay} color {@code #RRGGBB}, or null
     * @param samples the samples in time order; an entity without samples is skipped
     */
    public record Entity(String id, String title, String kind, String color, List<Sample> samples) {

        /** Copies the samples. */
        public Entity {
            Objects.requireNonNull(id, "id");
            samples = samples == null ? List.of() : List.copyOf(samples);
        }
    }

    /**
     * The document packet's content.
     *
     * @param name the document name
     * @param start the clock start (the requested range's start)
     * @param end the clock end (the requested range's end)
     * @param attribution the attribution text, or null
     * @param truncated true when entities or rows were left out by a cap; a note is appended to the description
     */
    public record Document(String name, Instant start, Instant end, String attribution, boolean truncated) {

        /** Requires the range. */
        public Document {
            Objects.requireNonNull(start, "start");
            Objects.requireNonNull(end, "end");
            if (end.isBefore(start)) {
                throw new IllegalArgumentException("The document end is before its start");
            }
        }
    }

    /**
     * The packets of a document: the document packet, then one packet per entity with at least one sample, at most
     * {@value #MAX_ENTITIES} of them.
     *
     * @param document the document packet's content
     * @param entities the entities in output order
     * @return the CZML packets
     */
    public static List<Map<String, Object>> packets(Document document, List<Entity> entities) {
        List<Map<String, Object>> out = new ArrayList<>();
        out.add(documentPacket(document));
        int written = 0;
        for (Entity entity : entities) {
            if (entity.samples().isEmpty()) {
                continue;
            }
            if (written == MAX_ENTITIES) {
                break;
            }
            out.add(entityPacket(entity));
            written++;
        }
        return out;
    }

    /** The document packet. */
    static Map<String, Object> documentPacket(Document document) {
        Map<String, Object> packet = new LinkedHashMap<>();
        packet.put("id", "document");
        packet.put("name", document.name() == null ? "Foresignal" : document.name());
        packet.put("version", "1.0");
        Map<String, Object> clock = new LinkedHashMap<>();
        clock.put("interval", interval(document.start(), document.end()));
        clock.put("currentTime", document.start().toString());
        clock.put("multiplier", multiplier(document.start(), document.end()));
        clock.put("range", "LOOP_STOP");
        clock.put("step", "SYSTEM_CLOCK_MULTIPLIER");
        packet.put("clock", clock);
        String description = document.attribution() == null || document.attribution().isBlank()
                ? "Data: Foresignal"
                : "Data: " + document.attribution().strip();
        if (document.truncated()) {
            description += " (truncated: at most " + MAX_ENTITIES + " entities)";
        }
        packet.put("description", description);
        return packet;
    }

    /** One entity packet; requires at least one sample. */
    static Map<String, Object> entityPacket(Entity entity) {
        List<Sample> samples = entity.samples();
        Sample first = samples.getFirst();
        Sample last = samples.getLast();
        String title = entity.title() == null || entity.title().isBlank() ? entity.id() : entity.title();
        Map<String, Object> packet = new LinkedHashMap<>();
        packet.put("id", entity.id());
        packet.put("name", title);
        packet.put("availability", interval(first.time(), last.time()));
        Map<String, Object> position = new LinkedHashMap<>();
        if (samples.size() == 1) {
            position.put("cartographicDegrees", List.of(round(first.lon(), 7), round(first.lat(), 7),
                    round(first.height(), 2)));
        } else {
            position.put("epoch", first.time().toString());
            List<Object> values = new ArrayList<>(samples.size() * 4);
            for (Sample sample : samples) {
                double seconds = Duration.between(first.time(), sample.time()).toMillis() / 1000.0;
                values.add(seconds);
                values.add(round(sample.lon(), 7));
                values.add(round(sample.lat(), 7));
                values.add(round(sample.height(), 2));
            }
            position.put("cartographicDegrees", values);
        }
        packet.put("position", position);
        List<Integer> rgba = rgba(entity.color());
        Map<String, Object> label = new LinkedHashMap<>();
        label.put("text", title);
        label.put("font", "12pt sans-serif");
        label.put("fillColor", Map.of("rgba", List.of(255, 255, 255, 255)));
        label.put("outlineColor", Map.of("rgba", List.of(0, 0, 0, 255)));
        label.put("outlineWidth", 2);
        label.put("style", "FILL_AND_OUTLINE");
        label.put("horizontalOrigin", "LEFT");
        label.put("pixelOffset", Map.of("cartesian2", List.of(10, 0)));
        packet.put("label", label);
        Map<String, Object> point = new LinkedHashMap<>();
        point.put("color", Map.of("rgba", rgba));
        point.put("pixelSize", 8);
        point.put("outlineColor", Map.of("rgba", List.of(0, 0, 0, 255)));
        point.put("outlineWidth", 1);
        packet.put("point", point);
        if (entity.kind() != null) {
            packet.put("properties", Map.of("kind", entity.kind()));
        }
        return packet;
    }

    /** {@code [r, g, b, 255]} of {@code #RRGGBB}, or of {@value #DEFAULT_COLOR} when absent or malformed. */
    static List<Integer> rgba(String color) {
        String hex = color != null && HEX.matcher(color.strip()).matches() ? color.strip() : DEFAULT_COLOR;
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        int value = Integer.parseInt(hex, 16);
        return List.of((value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF, 255);
    }

    /** An ISO 8601 interval {@code start/end}. */
    static String interval(Instant start, Instant end) {
        return start + "/" + end;
    }

    /** A playback speed that shows the whole range in about two minutes, at least 1. */
    static long multiplier(Instant start, Instant end) {
        long seconds = Duration.between(start, end).toSeconds();
        return Math.max(1, Math.round(seconds / 120.0));
    }

    private static double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }
}
