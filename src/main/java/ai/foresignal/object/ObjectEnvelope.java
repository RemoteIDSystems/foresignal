package ai.foresignal.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * The uniform summary of any Foresignal object (docs/Client-Contract.md §3, OpenAPI {@code ObjectEnvelope}).
 *
 * @param ref the canonical reference
 * @param type the object type
 * @param title a short title
 * @param summary at most 280 characters, or null
 * @param textOrigin {@code deterministic} or {@code llm}, for {@code summary}
 * @param status the type-specific status, or null
 * @param severity the type-specific severity, or null
 * @param time event and observed time
 * @param geometry a simplified GeoJSON geometry (≤ 500 vertices) or a centroid point, or null
 * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or null
 * @param privateObject true when the object touches a private source (039)
 * @param tags the caller's visible tags (filled by prompt 130; empty here)
 * @param provenance sources, manifest and the seq the envelope reflects
 * @param links the type-specific API URL and the related-objects URL
 * @param actions what the calling token may do with the object
 */
public record ObjectEnvelope(
        String ref,
        String type,
        String title,
        String summary,
        String textOrigin,
        String status,
        String severity,
        Time time,
        Map<String, Object> geometry,
        List<Double> bbox,
        @JsonProperty("private") boolean privateObject,
        List<String> tags,
        Provenance provenance,
        Map<String, String> links,
        List<ObjectAction> actions) {

    /** Longest summary, in characters. */
    public static final int SUMMARY_MAX = 280;

    /**
     * Event and observed time.
     *
     * @param start when it started, or null
     * @param end when it ended (null while open)
     * @param observedAt when it was last observed, or null
     */
    public record Time(Instant start, Instant end, Instant observedAt) {

        /** No time. */
        public static final Time NONE = new Time(null, null, null);
    }

    /**
     * Where the object came from.
     *
     * @param sourceIds the contributing sources
     * @param manifestId the 116 analysis manifest, or null
     * @param asOfSeq the transaction seq the envelope reflects, or null
     */
    public record Provenance(List<String> sourceIds, String manifestId, Long asOfSeq) {

        /** No provenance. */
        public static final Provenance NONE = new Provenance(List.of(), null, null);
    }

    /** This envelope with {@code actions}. */
    public ObjectEnvelope withActions(List<ObjectAction> newActions) {
        return new ObjectEnvelope(ref, type, title, summary, textOrigin, status, severity, time, geometry, bbox,
                privateObject, tags, provenance, links, List.copyOf(newActions));
    }

    /** The parsed reference. */
    public ObjectRef objectRef() {
        return ObjectRef.parse(ref);
    }

    /** A builder for resolvers. */
    public static Builder builder(ObjectRef ref) {
        return new Builder(ref);
    }

    /** Builds an envelope; every field but the reference and title is optional. */
    public static final class Builder {
        private final ObjectRef ref;
        private String title;
        private String summary;
        private String textOrigin = "deterministic";
        private String status;
        private String severity;
        private Time time = Time.NONE;
        private Map<String, Object> geometry;
        private List<Double> bbox;
        private boolean privateObject;
        private Provenance provenance = Provenance.NONE;
        private String self;

        private Builder(ObjectRef ref) {
            this.ref = ref;
        }

        /** The title (falls back to the reference when blank). */
        public Builder title(String value) {
            this.title = value;
            return this;
        }

        /** The summary, cut to {@value ObjectEnvelope#SUMMARY_MAX} characters. */
        public Builder summary(String value) {
            this.summary = clip(value);
            return this;
        }

        /** {@code deterministic} (the default) or {@code llm}. */
        public Builder textOrigin(String value) {
            this.textOrigin = value;
            return this;
        }

        /** The status. */
        public Builder status(String value) {
            this.status = value;
            return this;
        }

        /** The severity. */
        public Builder severity(String value) {
            this.severity = value;
            return this;
        }

        /** The time. */
        public Builder time(Instant start, Instant end, Instant observedAt) {
            this.time = new Time(start, end, observedAt);
            return this;
        }

        /** The geometry and bbox from a JTS geometry (simplified to ≤ 500 vertices). */
        public Builder geometry(org.locationtech.jts.geom.Geometry value) {
            EnvelopeGeometry.Shape shape = EnvelopeGeometry.of(value);
            if (shape != null) {
                this.geometry = shape.geoJson();
                this.bbox = shape.bbox();
            }
            return this;
        }

        /** A point geometry. */
        public Builder point(Double lon, Double lat) {
            if (lon != null && lat != null && Double.isFinite(lon) && Double.isFinite(lat)) {
                this.geometry = Map.of("type", "Point", "coordinates", List.of(lon, lat));
                this.bbox = List.of(lon, lat, lon, lat);
            }
            return this;
        }

        /** The private flag. */
        public Builder privateObject(boolean value) {
            this.privateObject = value;
            return this;
        }

        /** The provenance. */
        public Builder provenance(List<String> sourceIds, String manifestId, Long asOfSeq) {
            this.provenance = new Provenance(sourceIds == null ? List.of() : List.copyOf(sourceIds), manifestId,
                    asOfSeq);
            return this;
        }

        /** The type-specific API path. */
        public Builder self(String path) {
            this.self = path;
            return this;
        }

        /** The envelope, without actions. */
        public ObjectEnvelope build() {
            String text = ref.format();
            java.util.LinkedHashMap<String, String> links = new java.util.LinkedHashMap<>();
            if (self != null) {
                links.put("self", self);
            }
            links.put("related", "/v1/objects/" + text + "/related");
            String t = title == null || title.isBlank() ? text : title.strip();
            return new ObjectEnvelope(text, ref.type(), t, summary, textOrigin, status, severity, time, geometry,
                    bbox, privateObject, List.of(), provenance, Map.copyOf(links), List.of());
        }

        private static String clip(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            String s = value.strip();
            return s.length() <= SUMMARY_MAX ? s : s.substring(0, SUMMARY_MAX - 1) + "…";
        }
    }
}
