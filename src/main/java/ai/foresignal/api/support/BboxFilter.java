package ai.foresignal.api.support;

import java.util.List;
import org.locationtech.jts.geom.Envelope;

/**
 * A parsed {@code bbox}: one envelope, or two when the box crosses the antimeridian. A geometry
 * matches when it intersects any of them (the SQL ORs the envelopes).
 *
 * @param envelopes one or two WGS84 envelopes, each with {@code minX <= maxX}
 */
public record BboxFilter(List<Envelope> envelopes) {

    /** Copies the list and rejects an empty one. */
    public BboxFilter {
        if (envelopes == null || envelopes.isEmpty()) {
            throw new IllegalArgumentException("A bbox filter needs at least one envelope");
        }
        envelopes = List.copyOf(envelopes);
    }

    /** True when the box was split at the antimeridian. */
    public boolean crossesAntimeridian() {
        return envelopes.size() > 1;
    }

    /** True when the point {@code (lon, lat)} lies in any envelope (edges included). */
    public boolean contains(double lon, double lat) {
        for (Envelope envelope : envelopes) {
            if (envelope.contains(lon, lat)) {
                return true;
            }
        }
        return false;
    }

    /** A stable text form, for cursor fingerprints and entity tags. */
    public String canonical() {
        StringBuilder text = new StringBuilder();
        for (Envelope e : envelopes) {
            if (!text.isEmpty()) {
                text.append('|');
            }
            text.append(e.getMinX()).append(',').append(e.getMinY()).append(',')
                    .append(e.getMaxX()).append(',').append(e.getMaxY());
        }
        return text.toString();
    }
}
