package ai.foresignal.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Restrictions stored on an API token, a feed key or a pairing code (OpenAPI {@code ReadRestriction},
 * prompts 023 and 035).
 *
 * <p>{@code endpoints} is enforced by the token filter from 023. {@code sources}, {@code kinds},
 * {@code bbox} and {@code area} are <em>data</em> restrictions, enforced on every read path from
 * prompt 035 through {@code RestrictionClamp} (the effective form is
 * {@link ai.foresignal.security.restrict.ReadRestriction}).
 *
 * @param sources allowed source ids, or null for any
 * @param kinds allowed kinds, or null for any
 * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or null for anywhere
 * @param endpoints allowed operations as {@code METHOD /v1/path/pattern} (Ant style, method may be
 *     {@code *}), or null for any the scope reaches
 * @param area a GeoJSON Polygon or MultiPolygon (WGS84, at most 2,000 vertices), or null (035)
 * @param excludePrivate hide private sources; null for the default (true for read tokens, feed keys
 *     and anonymous callers) (035)
 * @param maxRequestsPerMinute overrides the {@code read} rate-limit class, or null (035)
 * @param placeIds places whose areas bound what may be read, or null (036); resolved through
 *     {@link ai.foresignal.security.restrict.PlaceAreas} into the area of the effective restriction
 * @param federation true when the token may call the federation feed endpoints (113: changes, stream, snapshot,
 *     bundle), together with the read scope; null or false otherwise
 * @param includeExercise true when the federation feed also serves exercise records (113, 115); null or false
 *     otherwise
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenRestrictions(List<String> sources, List<String> kinds, List<Double> bbox, List<String> endpoints,
        Map<String, Object> area, Boolean excludePrivate, Integer maxRequestsPerMinute, List<String> placeIds,
        Boolean federation, Boolean includeExercise) {

    /** No restriction at all. */
    public static final TokenRestrictions NONE = new TokenRestrictions(null, null, null, null);

    /** Copies the collections. */
    public TokenRestrictions {
        sources = sources == null ? null : List.copyOf(sources);
        kinds = kinds == null ? null : List.copyOf(kinds);
        bbox = bbox == null ? null : List.copyOf(bbox);
        endpoints = endpoints == null ? null : List.copyOf(endpoints);
        area = area == null ? null : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(area));
        placeIds = placeIds == null ? null : List.copyOf(placeIds);
    }

    /** The 036 members, without the 113 federation flags. */
    public TokenRestrictions(List<String> sources, List<String> kinds, List<Double> bbox, List<String> endpoints,
            Map<String, Object> area, Boolean excludePrivate, Integer maxRequestsPerMinute, List<String> placeIds) {
        this(sources, kinds, bbox, endpoints, area, excludePrivate, maxRequestsPerMinute, placeIds, null, null);
    }

    /** The 035 members, without places. */
    public TokenRestrictions(List<String> sources, List<String> kinds, List<Double> bbox, List<String> endpoints,
            Map<String, Object> area, Boolean excludePrivate, Integer maxRequestsPerMinute) {
        this(sources, kinds, bbox, endpoints, area, excludePrivate, maxRequestsPerMinute, null);
    }

    /** The 023 members only: sources, kinds, bbox and endpoints. */
    public TokenRestrictions(List<String> sources, List<String> kinds, List<Double> bbox, List<String> endpoints) {
        this(sources, kinds, bbox, endpoints, null, null, null, null);
    }

    /** {@code restrictions}, or {@link #NONE} when null. */
    public static TokenRestrictions orNone(TokenRestrictions restrictions) {
        return restrictions == null ? NONE : restrictions;
    }

    /** True when a source, kind, bbox, area or place restriction is set (enforced from prompts 035 and 036). */
    @JsonIgnore
    public boolean hasDataRestrictions() {
        return sources != null || kinds != null || bbox != null || area != null || placeIds != null;
    }

    /** True when an endpoint restriction is set. */
    @JsonIgnore
    public boolean hasEndpointRestrictions() {
        return endpoints != null;
    }

    /** True when nothing is restricted. */
    @JsonIgnore
    public boolean isEmpty() {
        return !hasDataRestrictions() && !hasEndpointRestrictions() && excludePrivate == null
                && maxRequestsPerMinute == null && federation == null && includeExercise == null;
    }

    /** True when the token carries the federation capability (113). */
    @JsonIgnore
    public boolean federationAllowed() {
        return Boolean.TRUE.equals(federation);
    }

    /** True when the federation feed serves this token exercise records too (113). */
    @JsonIgnore
    public boolean exerciseIncluded() {
        return Boolean.TRUE.equals(includeExercise);
    }

    /** This value, or null when it restricts nothing (the API form of "no restrictions"). */
    public TokenRestrictions orNull() {
        return isEmpty() ? null : this;
    }
}
