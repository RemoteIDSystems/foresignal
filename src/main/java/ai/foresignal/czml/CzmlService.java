package ai.foresignal.czml;

import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.track.TrackResult;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Turns history rows and tracks into {@link CzmlWriter} entities (prompt 141).
 *
 * <p>Callers pass rows that were already filtered for the caller (private sources, restriction, license policy and
 * the MLAT gate are applied by the query and the controller): this class only shapes them.
 *
 * <ul>
 *   <li><b>History:</b> versions are grouped by {@code {sourceId}:{externalId}} in order of first appearance;
 *       tombstones and versions without a geometry are skipped; a non-point geometry is placed at the center of its
 *       bounding box. At most {@value CzmlWriter#MAX_ENTITIES} entities are kept and {@link Entities#truncated()} says
 *       whether more existed.
 *   <li><b>Tracks:</b> every segment's points become samples of one entity (Cesium interpolates across gaps; the
 *       {@code availability} still spans the whole track).
 *   <li><b>Color:</b> the {@code kindDisplay} color the record's own source declares for its kind, else the first
 *       declared by any source (sources in id order), else {@link CzmlWriter#DEFAULT_COLOR}.
 * </ul>
 */
@Component
public class CzmlService {

    private final SourceRegistry registry;
    private final RecordQueryService queries;
    private final JsonMapper json;

    /** Creates the service. */
    public CzmlService(SourceRegistry registry, RecordQueryService queries, JsonMapper json) {
        this.registry = registry;
        this.queries = queries;
        this.json = json;
    }

    /**
     * Entities built from rows.
     *
     * @param entities the entities, in order of first appearance
     * @param truncated true when more than {@value CzmlWriter#MAX_ENTITIES} entities existed
     */
    public record Entities(List<CzmlWriter.Entity> entities, boolean truncated) {}

    /**
     * The entities of history rows ordered by time.
     *
     * @param rows versions, already filtered for the caller
     * @param titleOf the title of a version (the mapper's resolved title)
     */
    public Entities fromRows(List<RecordRow> rows, Function<RecordRow, String> titleOf) {
        Map<String, Builder> byId = new LinkedHashMap<>();
        boolean truncated = false;
        for (RecordRow row : rows) {
            if (row.tombstone() || row.geometry() == null) {
                continue;
            }
            double[] position = position(row.geometry());
            if (position == null) {
                continue;
            }
            String id = row.id();
            Builder builder = byId.get(id);
            if (builder == null) {
                if (byId.size() == CzmlWriter.MAX_ENTITIES) {
                    truncated = true;
                    continue;
                }
                builder = new Builder(id, row.sourceId(), row.kind());
                byId.put(id, builder);
            }
            String title = titleOf.apply(row);
            if (title != null && !title.isBlank()) {
                builder.title = title;
            }
            Instant at = row.observedAt() != null ? row.observedAt() : row.persistedObservedAt();
            if (at == null) {
                continue;
            }
            builder.samples.add(new CzmlWriter.Sample(at, position[0], position[1], position[2]));
        }
        List<CzmlWriter.Entity> out = new ArrayList<>(byId.size());
        for (Builder builder : byId.values()) {
            builder.samples.sort(Comparator.comparing(CzmlWriter.Sample::time));
            out.add(new CzmlWriter.Entity(builder.id, builder.title, builder.kind,
                    colorOf(builder.sourceId, builder.kind), builder.samples));
        }
        return new Entities(out, truncated);
    }

    /**
     * The entity of a track.
     *
     * @param track the simplified track
     * @param title the entity's title, or null for its id
     */
    public CzmlWriter.Entity fromTrack(TrackResult track, String title) {
        List<CzmlWriter.Sample> samples = new ArrayList<>();
        for (int s = 0; s < track.coordinates().size(); s++) {
            List<double[]> coordinates = track.coordinates().get(s);
            List<Long> times = track.times().get(s);
            for (int i = 0; i < coordinates.size() && i < times.size(); i++) {
                double[] c = coordinates.get(i);
                Instant at = Instant.ofEpochMilli(times.get(i));
                // A one-point segment repeats its point (Track schema); keep one sample per instant.
                if (!samples.isEmpty() && samples.getLast().time().equals(at)) {
                    continue;
                }
                samples.add(new CzmlWriter.Sample(at, c[0], c[1], c.length > 2 ? c[2] : 0));
            }
        }
        samples.sort(Comparator.comparing(CzmlWriter.Sample::time));
        String id = track.sourceId() + ":" + track.externalId();
        return new CzmlWriter.Entity(id, title, track.kind(), colorOf(track.sourceId(), track.kind()), samples);
    }

    /**
     * The CZML document of one track: the document packet over the track's window and one entity packet.
     *
     * @param track the track
     * @param title the entity title, or null
     * @param attribution the attribution text, or null for the source's
     */
    public List<Map<String, Object>> trackDocument(TrackResult track, String title, String attribution) {
        String text = attribution != null ? attribution : attributionOf(Set.of(track.sourceId()));
        CzmlWriter.Document document = new CzmlWriter.Document("Foresignal track " + track.sourceId() + ":"
                + track.externalId(), track.from(), track.to(), text, false);
        return CzmlWriter.packets(document, List.of(fromTrack(track, title)));
    }

    /**
     * The current title of an entity as the caller may see it, or null when it has no visible current record.
     *
     * @param excludedSources sources the caller may not read
     * @param restriction the caller's restriction
     */
    public String currentTitle(String sourceId, String externalId, Set<String> excludedSources,
            ReadRestriction restriction) {
        Map<String, RecordRow> rows = queries.currentByKeys(List.of(Map.entry(sourceId, externalId)), excludedSources,
                restriction == null ? ReadRestriction.NONE : restriction);
        RecordRow row = rows.get(sourceId + ":" + externalId);
        return row == null ? null : row.title();
    }

    /** The descriptor attributions of {@code sourceIds}, distinct, joined with {@code " · "}; null when none. */
    public String attributionOf(Set<String> sourceIds) {
        Set<String> parts = new LinkedHashSet<>();
        for (String id : sourceIds) {
            if (id != null && registry.contains(id)) {
                String text = registry.descriptor(id).attribution();
                if (text != null && !text.isBlank()) {
                    parts.add(text.strip());
                }
            }
        }
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }

    /** The packets as UTF-8 JSON bytes. */
    public byte[] bytes(List<Map<String, Object>> packets) {
        return json.writeValueAsString(packets).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * The {@code kindDisplay} color of {@code kind}: the one {@code sourceId} declares, else the first declared by any
     * source in id order; null when none declares one.
     */
    public String colorOf(String sourceId, String kind) {
        if (kind == null) {
            return null;
        }
        if (sourceId != null && registry.contains(sourceId)) {
            String own = color(registry.descriptor(sourceId), kind);
            if (own != null) {
                return own;
            }
        }
        return registry.descriptors().stream()
                .sorted(Comparator.comparing(SourceDescriptor::id))
                .map(d -> color(d, kind))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static String color(SourceDescriptor descriptor, String kind) {
        if (descriptor == null || descriptor.kindDisplay() == null) {
            return null;
        }
        KindDisplay display = descriptor.kindDisplay().get(kind);
        return display == null ? null : display.color();
    }

    /**
     * {@code [lon, lat, height]} of a GeoJSON geometry: a point's coordinates (height from a third ordinate, else 0),
     * else the center of the bounding box of every position; null when it has none or cannot be read.
     */
    double[] position(String geoJson) {
        JsonNode geometry;
        try {
            geometry = json.readTree(geoJson);
        } catch (RuntimeException e) {
            return null;
        }
        return position(geometry);
    }

    /** {@link #position(String)} of a parsed geometry. */
    static double[] position(JsonNode geometry) {
        if (geometry == null || !geometry.isObject()) {
            return null;
        }
        JsonNode coordinates = geometry.get("coordinates");
        if ("Point".equals(geometry.path("type").asString()) && coordinates != null && coordinates.isArray()
                && coordinates.size() >= 2) {
            double height = coordinates.size() > 2 && coordinates.get(2).isNumber() ? coordinates.get(2).asDouble() : 0;
            return new double[] {coordinates.get(0).asDouble(), coordinates.get(1).asDouble(), height};
        }
        double[] box = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY};
        collect(geometry, box);
        if (box[0] == Double.POSITIVE_INFINITY) {
            return null;
        }
        return new double[] {(box[0] + box[2]) / 2, (box[1] + box[3]) / 2, 0};
    }

    private static void collect(JsonNode node, double[] box) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            collect(node.get("coordinates"), box);
            JsonNode geometries = node.get("geometries");
            if (geometries != null && geometries.isArray()) {
                for (JsonNode child : geometries) {
                    collect(child, box);
                }
            }
            return;
        }
        if (!node.isArray()) {
            return;
        }
        if (node.size() >= 2 && node.get(0).isNumber() && node.get(1).isNumber()) {
            double lon = node.get(0).asDouble();
            double lat = node.get(1).asDouble();
            box[0] = Math.min(box[0], lon);
            box[1] = Math.min(box[1], lat);
            box[2] = Math.max(box[2], lon);
            box[3] = Math.max(box[3], lat);
            return;
        }
        for (JsonNode child : node) {
            collect(child, box);
        }
    }

    /** One entity being assembled. */
    private static final class Builder {
        private final String id;
        private final String sourceId;
        private final String kind;
        private final List<CzmlWriter.Sample> samples = new ArrayList<>();
        private String title;

        Builder(String id, String sourceId, String kind) {
            this.id = id;
            this.sourceId = sourceId;
            this.kind = kind;
        }
    }
}
