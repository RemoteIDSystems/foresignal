package ai.foresignal.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

/**
 * Restrictions stored on an API token or a pairing code (OpenAPI {@code TokenRestrictions},
 * prompt 023).
 *
 * <p>{@code endpoints} is enforced by the token filter from 023. {@code sources}, {@code kinds}
 * and {@code bbox} are <em>data</em> restrictions, enforced on every read path from prompt 035;
 * until then a token carrying any of them is refused on data reads with
 * {@code 403 RESTRICTION_NOT_ENFORCED} ({@link RestrictionEnforcer}), so a restriction never
 * silently fails open.
 *
 * @param sources allowed source ids, or null for any
 * @param kinds allowed kinds, or null for any
 * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or null for anywhere
 * @param endpoints allowed operations as {@code METHOD /v1/path/pattern} (Ant style, method may be
 *     {@code *}), or null for any the scope reaches
 */
public record TokenRestrictions(List<String> sources, List<String> kinds, List<Double> bbox, List<String> endpoints) {

    /** No restriction at all. */
    public static final TokenRestrictions NONE = new TokenRestrictions(null, null, null, null);

    /** Copies the lists. */
    public TokenRestrictions {
        sources = sources == null ? null : List.copyOf(sources);
        kinds = kinds == null ? null : List.copyOf(kinds);
        bbox = bbox == null ? null : List.copyOf(bbox);
        endpoints = endpoints == null ? null : List.copyOf(endpoints);
    }

    /** {@code restrictions}, or {@link #NONE} when null. */
    public static TokenRestrictions orNone(TokenRestrictions restrictions) {
        return restrictions == null ? NONE : restrictions;
    }

    /** True when a source, kind or bbox restriction is set (enforced from prompt 035). */
    @JsonIgnore
    public boolean hasDataRestrictions() {
        return sources != null || kinds != null || bbox != null;
    }

    /** True when an endpoint restriction is set. */
    @JsonIgnore
    public boolean hasEndpointRestrictions() {
        return endpoints != null;
    }

    /** True when nothing is restricted. */
    @JsonIgnore
    public boolean isEmpty() {
        return !hasDataRestrictions() && !hasEndpointRestrictions();
    }

    /** This value, or null when it restricts nothing (the API form of "no restrictions"). */
    public TokenRestrictions orNull() {
        return isEmpty() ? null : this;
    }
}
