package ai.foresignal.kml;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.ingest.TitleResolver;
import ai.foresignal.map.SpriteCatalog;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.query.AsOfQueryService;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import ai.foresignal.store.track.TrackQuery;
import ai.foresignal.store.track.TrackResult;
import ai.foresignal.store.track.TrackService;
import ai.foresignal.trigger.GeofenceRef;
import ai.foresignal.trigger.RegionFences;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds the KML and KMZ documents of prompt 058 for Google Earth and Cesium.
 *
 * <ul>
 *   <li>{@link #records}: current records (or those known at {@code asOf}) of a layer, sources or kinds,
 *       newest {@code observedAt} first, at most {@value #MAX_PLACEMARKS} Placemarks; a bbox across the
 *       antimeridian is already split into two envelopes by the shared bbox parser (014).
 *   <li>{@link #tracks}: {@code gx:Track} per entity from the track service with no tolerance (037), at
 *       most {@value #MAX_ENTITIES} entities and {@value #MAX_TRACK_POINTS} points each (the newest kept).
 *   <li>{@link #geofence}: a trigger rule's fence, simplified to at most {@value #MAX_FENCE_VERTICES}
 *       vertices.
 * </ul>
 *
 * <p>Every query carries the caller's excluded (private) sources and restriction, and every record must
 * also pass {@link EgressPolicy#allows} on {@link EgressPath#FEED}.
 */
@Service
public class KmlService {

    /** The most Placemarks of a records document. */
    public static final int MAX_PLACEMARKS = 5_000;

    /** The most entities of a tracks document. */
    public static final int MAX_ENTITIES = 100;

    /** The most points of one track. */
    public static final int MAX_TRACK_POINTS = 5_000;

    /** The most vertices of an exported geofence. */
    public static final int MAX_FENCE_VERTICES = 10_000;

    /** The most asOf pages read to order a snapshot by {@code observedAt}. */
    static final int MAX_AS_OF_PAGES = 10;

    /** The most fields listed in a Placemark's description and ExtendedData. */
    static final int MAX_FIELDS = 24;

    /** The vertical datums whose fields give a track its altitude (Adapter Guide §5). */
    static final Set<String> ALTITUDE_DATUMS = Set.of("MSL", "WGS84", "AGL", "PRESSURE");

    private final RecordQueryService queries;
    private final AsOfQueryService asOfQueries;
    private final TrackService trackService;
    private final SourceRegistry registry;
    private final EgressPolicy egress;
    private final SpriteCatalog sprite;
    private final KmlWriter writer;
    private final TriggerRuleService rules;
    private final JsonMapper json;
    private final GeoJsonReader geoJson = new GeoJsonReader();

    /** The 040 region geometries; null in hand-built unit tests or without reference data. */
    @Autowired(required = false)
    private RegionFences regionFences;

    /** Creates the service. */
    public KmlService(RecordQueryService queries, AsOfQueryService asOfQueries, TrackService trackService,
            SourceRegistry registry, EgressPolicy egress, SpriteCatalog sprite, KmlWriter writer,
            TriggerRuleService rules, JsonMapper json) {
        this.queries = queries;
        this.asOfQueries = asOfQueries;
        this.trackService = trackService;
        this.registry = registry;
        this.egress = egress;
        this.sprite = sprite;
        this.writer = writer;
        this.rules = rules;
        this.json = json;
    }

    /**
     * A finished document.
     *
     * @param body the bytes (UTF-8 KML, or a KMZ zip)
     * @param mediaType {@link KmlWriter#KML_MEDIA_TYPE} or {@link KmlWriter#KMZ_MEDIA_TYPE}
     */
    public record Output(byte[] body, String mediaType) {}

    /**
     * What a records or tracks request reads.
     *
     * @param name the document name
     * @param sources source filter; empty for all visible
     * @param kinds kind filter; empty for all
     * @param bbox the spatial filter, or null
     * @param excluded the caller's hidden sources
     * @param restriction the caller's restriction
     * @param viewer the private-gate viewer
     */
    public record Selection(String name, List<String> sources, List<String> kinds, BboxFilter bbox,
            Set<String> excluded, ReadRestriction restriction, Viewer viewer) {

        /** Copies the collections. */
        public Selection {
            sources = sources == null ? List.of() : List.copyOf(sources);
            kinds = kinds == null ? List.of() : List.copyOf(kinds);
            excluded = excluded == null ? Set.of() : Set.copyOf(excluded);
            restriction = restriction == null ? ReadRestriction.NONE : restriction;
        }
    }

    // ------------------------------------------------------------------------- NetworkLink

    /** The NetworkLink document: one link per layer to its records URL. */
    public Output networkLinks(List<String> layerIds, int refreshSeconds, PublicUrls urls) {
        List<KmlModel.Link> links = new ArrayList<>();
        for (String id : layerIds) {
            links.add(new KmlModel.Link(id, urls.api("/v1/kml/records.kml") + "?layer="
                    + java.net.URLEncoder.encode(id, java.nio.charset.StandardCharsets.UTF_8)));
        }
        return kml(writer.networkLinks("Foresignal", links, refreshSeconds));
    }

    // ------------------------------------------------------------------------- records

    /**
     * The records document.
     *
     * @param selection what to read
     * @param asOf a transaction time to read at, or null for current state
     * @param urls the request's public URLs (icon hrefs)
     * @param kmz true for a KMZ with the icons bundled
     */
    public Output records(Selection selection, Instant asOf, PublicUrls urls, boolean kmz) {
        List<RecordRow> rows;
        long total;
        boolean exact = true;
        if (asOf == null) {
            CurrentQuery query = new CurrentQuery(selection.sources(), selection.kinds(), null, selection.bbox(), null,
                    null, null, null, null, false, ListOrder.OBSERVED, null, null, MAX_PLACEMARKS, FieldSelection.ALL,
                    GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false, selection.excluded(),
                    selection.restriction());
            Slices.RecordSlice slice = queries.list(query);
            rows = slice.rows();
            total = slice.nextCursor() == null ? rows.size() : queries.countCurrent(query).count();
        } else {
            List<RecordRow> all = new ArrayList<>();
            String cursor = null;
            int pages = 0;
            do {
                AsOfQueryService.AsOfSlice slice = asOfQueries.list(new AsOfQueryService.AsOfQuery(asOf,
                        selection.sources(), selection.kinds(), null, selection.bbox(), null, cursor, MAX_PLACEMARKS,
                        FieldSelection.ALL, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false,
                        selection.excluded(), selection.restriction()));
                all.addAll(slice.rows());
                cursor = slice.nextCursor();
                pages++;
            } while (cursor != null && pages < MAX_AS_OF_PAGES);
            exact = cursor == null;
            all.sort(Comparator.comparing(RecordRow::observedAt, Comparator.nullsLast(Comparator.reverseOrder())));
            total = all.size();
            rows = all.size() > MAX_PLACEMARKS ? all.subList(0, MAX_PLACEMARKS) : all;
        }

        Map<String, KmlModel.Style> styles = new LinkedHashMap<>();
        Set<String> sourcesSeen = new LinkedHashSet<>();
        List<KmlModel.Placemark> placemarks = new ArrayList<>();
        for (RecordRow row : rows) {
            if (!egress.allows(EgressPath.FEED, row.sourceId(), selection.viewer())) {
                total--;
                continue;
            }
            SourceDescriptor descriptor = registry.descriptor(row.sourceId());
            sourcesSeen.add(row.sourceId());
            KmlModel.Style style = styles.computeIfAbsent(row.kind(), kind -> style(kind, descriptor));
            placemarks.add(placemark(row, descriptor, style));
        }
        boolean truncated = total > placemarks.size();
        KmlModel.Records doc = new KmlModel.Records(selection.name(), attributions(sourcesSeen),
                List.copyOf(styles.values()), placemarks, truncated, Math.max(total, placemarks.size()), exact);
        return finish(kmz, styles.values(), href -> writer.records(doc, href), urls);
    }

    /** The shared style of {@code kind}: its display icon (when in the sprite) and color. */
    KmlModel.Style style(String kind, SourceDescriptor descriptor) {
        KindDisplay display = descriptor == null ? null : descriptor.kindDisplay().get(kind);
        String icon = display != null && sprite.contains(display.icon()) ? display.icon() : null;
        return new KmlModel.Style(kind, icon, display == null || display.color() == null ? KmlWriter.DEFAULT_COLOR
                : KmlWriter.color(display.color()));
    }

    /** One record as a Placemark: title, observedAt, the declared fields and its heading. */
    KmlModel.Placemark placemark(RecordRow row, SourceDescriptor descriptor, KmlModel.Style style) {
        JsonNode payload = parse(row.payload());
        String title = row.title();
        if (title == null && payload != null && descriptor != null) {
            title = TitleResolver.resolve(descriptor.titleField(), json.convertValue(payload, Map.class));
        }
        if (title == null || title.isBlank()) {
            title = row.sourceId() + ":" + row.externalId();
        }
        List<KmlModel.Data> data = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        for (String field : fields(descriptor, row.kind())) {
            JsonNode value = payload == null ? null : payload.get(field);
            String text = valueText(value);
            if (text == null) {
                continue;
            }
            FieldSpec spec = descriptor.fieldSpecs().get(field);
            String label = spec != null && spec.label() != null && !spec.label().isBlank() ? spec.label().strip() : field;
            String unit = spec != null && spec.unit() != null && !spec.unit().isBlank() && !"1".equals(spec.unit())
                    ? " " + spec.unit().strip() : "";
            data.add(new KmlModel.Data(field, label, text));
            if (!description.isEmpty()) {
                description.append('\n');
            }
            description.append(label).append(": ").append(text).append(unit);
        }
        Double heading = null;
        KindDisplay display = descriptor == null ? null : descriptor.kindDisplay().get(row.kind());
        if (display != null && display.rotateField() != null && payload != null) {
            JsonNode value = payload.get(display.rotateField());
            if (value != null && value.isNumber()) {
                heading = value.asDouble();
            }
        }
        Geometry geometry = null;
        if (row.geometry() != null) {
            try {
                geometry = geoJson.read(row.geometry());
            } catch (RuntimeException e) {
                geometry = null;
            }
        }
        return new KmlModel.Placemark(title, row.observedAt(), description.toString(), style, heading, data, geometry);
    }

    /** The fields a Placemark lists: the source's tile properties, else its field specs for the kind. */
    static List<String> fields(SourceDescriptor descriptor, String kind) {
        if (descriptor == null) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        if (!descriptor.tileProperties().isEmpty()) {
            out.addAll(descriptor.tileProperties());
        } else {
            for (Map.Entry<String, FieldSpec> e : descriptor.fieldSpecs().entrySet()) {
                if (e.getValue().appliesTo(kind)) {
                    out.add(e.getKey());
                }
            }
        }
        return out.size() > MAX_FIELDS ? out.subList(0, MAX_FIELDS) : out;
    }

    /** A scalar payload value as text; null for absent, null, objects and arrays. */
    static String valueText(JsonNode value) {
        if (value == null || value.isNull() || value.isContainer()) {
            return null;
        }
        return value.asString();
    }

    // ------------------------------------------------------------------------- tracks

    /**
     * The tracks document: one entity ({@code sourceId} and {@code externalId}) or up to
     * {@value #MAX_ENTITIES} entities of {@code kind} inside {@code bbox}.
     */
    public Output tracks(String sourceId, String externalId, Selection selection, Instant from, Instant to,
            PublicUrls urls, boolean kmz) {
        List<RecordRow> entities = new ArrayList<>();
        boolean entitiesTruncated = false;
        if (sourceId != null) {
            if (!registry.contains(sourceId) || selection.excluded().contains(sourceId)) {
                throw NotFoundException.of("Record", sourceId + ":" + externalId);
            }
            RecordRow current = queries.currentByKeys(List.of(Map.entry(sourceId, externalId)), selection.excluded(),
                    selection.restriction()).get(sourceId + ":" + externalId);
            entities.add(current != null ? current : stub(sourceId, externalId));
        } else {
            CurrentQuery query = new CurrentQuery(List.of(), selection.kinds(), null, selection.bbox(), null, null,
                    null, null, null, false, ListOrder.OBSERVED, null, null, MAX_ENTITIES, FieldSelection.ALL,
                    GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false, selection.excluded(),
                    selection.restriction());
            Slices.RecordSlice slice = queries.list(query);
            entities.addAll(slice.rows());
            entitiesTruncated = slice.nextCursor() != null;
        }
        Map<String, KmlModel.Style> styles = new LinkedHashMap<>();
        Set<String> sourcesSeen = new LinkedHashSet<>();
        List<KmlModel.Track> tracks = new ArrayList<>();
        for (RecordRow entity : entities) {
            if (!egress.allows(EgressPath.FEED, entity.sourceId(), selection.viewer())) {
                if (sourceId != null) {
                    throw NotFoundException.of("Record", sourceId + ":" + externalId);
                }
                continue;
            }
            SourceDescriptor descriptor = registry.descriptor(entity.sourceId());
            String altitudeField = altitudeField(descriptor, entity.kind());
            TrackResult result;
            try {
                result = trackService.track(new TrackQuery(entity.sourceId(), entity.externalId(), from, to, 0.0,
                        TrackService.MAX_POINTS, null, true, altitudeField == null ? List.of() : List.of(altitudeField),
                        selection.excluded(), selection.restriction()));
            } catch (NotFoundException | ValidationException e) {
                if (sourceId != null) {
                    throw e;
                }
                continue;
            }
            String kind = entity.kind() != null ? entity.kind() : result.kind();
            KmlModel.Style style = styles.computeIfAbsent(kind == null ? "track" : kind, k -> style(k, descriptor));
            sourcesSeen.add(entity.sourceId());
            String name = entity.title() != null && !entity.title().isBlank() ? entity.title()
                    : entity.sourceId() + ":" + entity.externalId();
            tracks.add(track(name, style, result, altitudeField, descriptor));
        }
        String name = sourceId != null ? "Foresignal track " + sourceId + ":" + externalId : selection.name();
        KmlModel.Tracks doc = new KmlModel.Tracks(name, attributions(sourcesSeen), List.copyOf(styles.values()), tracks,
                entitiesTruncated);
        return finish(kmz, styles.values(), href -> writer.tracks(doc, href), urls);
    }

    /** One track: the segments flattened oldest first, the newest {@value #MAX_TRACK_POINTS} kept. */
    static KmlModel.Track track(String name, KmlModel.Style style, TrackResult result, String altitudeField,
            SourceDescriptor descriptor) {
        List<Instant> whens = new ArrayList<>();
        List<double[]> coords = new ArrayList<>();
        List<Double> altitudes = new ArrayList<>();
        List<List<Double>> values = altitudeField == null ? null : result.values().get(altitudeField);
        long lastTime = Long.MIN_VALUE;
        for (int s = 0; s < result.coordinates().size(); s++) {
            List<double[]> segment = result.coordinates().get(s);
            List<Long> times = result.times().get(s);
            for (int i = 0; i < segment.size(); i++) {
                long t = times.get(i);
                if (t == lastTime) {
                    continue; // a lone point is repeated to make a LineString; KML needs it once
                }
                lastTime = t;
                whens.add(Instant.ofEpochMilli(t));
                coords.add(new double[] {segment.get(i)[0], segment.get(i)[1], 0});
                Double alt = null;
                if (values != null && s < values.size() && i < values.get(s).size()) {
                    alt = values.get(s).get(i);
                }
                altitudes.add(alt);
            }
        }
        boolean truncated = whens.size() > MAX_TRACK_POINTS;
        if (truncated) {
            int from = whens.size() - MAX_TRACK_POINTS;
            whens = new ArrayList<>(whens.subList(from, whens.size()));
            coords = new ArrayList<>(coords.subList(from, coords.size()));
            altitudes = new ArrayList<>(altitudes.subList(from, altitudes.size()));
        }
        String mode = "clampToGround";
        double factor = altitudeField == null ? 0 : metersPerUnit(descriptor.fieldSpecs().get(altitudeField).unit());
        if (altitudeField != null && altitudes.stream().anyMatch(a -> a != null)) {
            String datum = descriptor.fieldSpecs().get(altitudeField).datum();
            mode = "AGL".equals(datum) ? "relativeToGround" : "absolute";
            Double last = altitudes.stream().filter(a -> a != null).findFirst().orElse(0.0);
            for (int i = 0; i < coords.size(); i++) {
                Double a = altitudes.get(i);
                last = a != null ? a : last;
                coords.get(i)[2] = last * factor;
            }
        }
        List<KmlModel.Data> data = List.of(new KmlModel.Data("pointsScanned", null, Integer.toString(result.pointsScanned())),
                new KmlModel.Data("scanTruncated", null, Boolean.toString(result.truncated())));
        return new KmlModel.Track(name, style, whens, coords, mode, truncated, data);
    }

    /**
     * The payload field giving a track its altitude (Adapter Guide §5): the first numeric field spec for
     * {@code kind} with a datum of MSL, WGS84, AGL or PRESSURE in meters, kilometers or feet; null when none.
     */
    static String altitudeField(SourceDescriptor descriptor, String kind) {
        if (descriptor == null) {
            return null;
        }
        for (Map.Entry<String, FieldSpec> e : descriptor.fieldSpecs().entrySet()) {
            FieldSpec spec = e.getValue();
            if (spec.datum() != null && ALTITUDE_DATUMS.contains(spec.datum())
                    && (spec.type() == FieldType.NUMBER || spec.type() == FieldType.INTEGER)
                    && metersPerUnit(spec.unit()) > 0 && (kind == null || spec.appliesTo(kind))
                    && TrackService.FIELD.matcher(e.getKey()).matches()) {
                return e.getKey();
            }
        }
        return null;
    }

    /** Meters per unit for {@code m}, {@code km} and {@code [ft_i]}; 0 for any other unit. */
    static double metersPerUnit(String unit) {
        if (unit == null) {
            return 0;
        }
        return switch (unit.strip()) {
            case "m" -> 1;
            case "km" -> 1000;
            case "[ft_i]" -> 0.3048;
            default -> 0;
        };
    }

    // ------------------------------------------------------------------------- geofence

    /** A trigger rule's geofence; {@code 404} when the rule has none. */
    public Output geofence(UUID ruleId) {
        TriggerRuleEntity rule = rules.load(ruleId);
        List<Geometry> geometries = new ArrayList<>();
        String description = null;
        GeofenceRef ref = GeofenceRef.fromMap(rule.getGeofenceRef());
        if (ref != null) {
            if (regionFences == null || !regionFences.available(ref)) {
                throw new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND,
                        "The reference data of this rule's geofenceRef is not loaded.");
            }
            geometries.add(regionFences.union(ref));
            description = "geofenceRef " + ref.canonical();
        } else if (rule.getGeofence() != null) {
            geometries.add(rule.getGeofence());
            if (rule.getGeofenceRoute() != null) {
                try {
                    geometries.add(geoJson.read(json.valueToTree(rule.getGeofenceRoute())));
                } catch (RuntimeException e) {
                    // The corridor polygon alone still shows the fence.
                }
                description = "route corridor";
            }
        }
        if (geometries.isEmpty()) {
            throw new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND,
                    "Trigger rule '" + ruleId + "' has no geofence.");
        }
        return kml(writer.geofence(rule.getName(), description, simplify(geometries, MAX_FENCE_VERTICES)));
    }

    /**
     * {@code geometries} simplified with a growing tolerance (topology preserving, from 0.0001°) until
     * their vertices together number at most {@code maxVertices}.
     */
    static List<Geometry> simplify(List<Geometry> geometries, int maxVertices) {
        List<Geometry> current = geometries;
        double tolerance = 0.0001;
        while (vertices(current) > maxVertices && tolerance < 10) {
            List<Geometry> next = new ArrayList<>();
            for (Geometry g : geometries) {
                next.add(TopologyPreservingSimplifier.simplify(g, tolerance));
            }
            current = next;
            tolerance *= 2;
        }
        return current;
    }

    private static int vertices(List<Geometry> geometries) {
        int n = 0;
        for (Geometry g : geometries) {
            n += g.getNumPoints();
        }
        return n;
    }

    // ------------------------------------------------------------------------- helpers

    private Output finish(boolean kmz, java.util.Collection<KmlModel.Style> styles,
            Function<Function<String, String>, String> render, PublicUrls urls) {
        if (!kmz) {
            return kml(render.apply(name -> urls.api("/v1/map/icons/" + name + ".png")));
        }
        Map<String, byte[]> icons = new TreeMap<>();
        for (KmlModel.Style style : styles) {
            if (style.icon() != null) {
                sprite.icon(style.icon()).ifPresent(png -> icons.put(style.icon(), png));
            }
        }
        String doc = render.apply(name -> KmlWriter.KMZ_ICON_DIR + name + ".png");
        return new Output(writer.kmz(new KmlModel.Kmz(doc, icons)), KmlWriter.KMZ_MEDIA_TYPE);
    }

    private static Output kml(String text) {
        return new Output(text.getBytes(java.nio.charset.StandardCharsets.UTF_8), KmlWriter.KML_MEDIA_TYPE);
    }

    /** The distinct descriptor attributions of {@code sourceIds}, joined with {@code " · "}; null when none. */
    String attributions(Set<String> sourceIds) {
        Set<String> parts = new LinkedHashSet<>();
        for (String id : sourceIds) {
            SourceDescriptor d = registry.contains(id) ? registry.descriptor(id) : null;
            if (d != null && d.attribution() != null && !d.attribution().isBlank()) {
                parts.add(d.attribution().strip());
            }
        }
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }

    private JsonNode parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            JsonNode node = json.readTree(text);
            return node != null && node.isObject() ? node : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** A placeholder row for an entity with no current head (the track may still have versions). */
    private static RecordRow stub(String sourceId, String externalId) {
        return new RecordRow(sourceId, externalId, null, 0, 0, null, null, null, null, null, false, null, null, null,
                null, null, null, 0, null, null, null, null, null, null, null, null, null, null);
    }
}
