package ai.foresignal.source.adapters.ioda;

import ai.foresignal.reference.Admin1Centroids;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
import ai.foresignal.source.spi.Geo;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Optional;
import org.locationtech.jts.geom.Point;

/**
 * Places an IODA alert on the map without any network call (prompt 068).
 *
 * <ul>
 *   <li>{@code country}: the bundled country table's label point ({@link CountryCentroids#byIso2}, prompt 040).
 *       IODA country codes are ISO 3166-1 alpha-2 (verified on the 2026-10-13 capture).</li>
 *   <li>{@code region}: the Natural Earth admin-1 label point of the region with the same (country, normalized
 *       name), through {@link Admin1Centroids}. IODA region codes are NetAcuity ids with no crosswalk in 040, so
 *       the name is the only key. No match gives null geometry and counts {@value #REGION_UNMATCHED}.</li>
 *   <li>{@code asn} (and anything else): null geometry.</li>
 * </ul>
 *
 * <p>Every geometry is a label point, not a measured position, so the record carries
 * {@code geometryApproximate: true} ({@link IodaAlertsParser#toRecord}).
 */
public class IodaGeometry {

    /** The counter of regions without an admin-1 match. */
    public static final String REGION_UNMATCHED = "foresignal.ioda.region_unmatched";

    private final CountryCentroids countries;
    private final Admin1Centroids admin1;
    private final Counter regionUnmatched;

    /**
     * Creates the resolver.
     *
     * @param countries the bundled country table; null places no country
     * @param admin1 the admin-1 label points; null places no region
     * @param meters the meter registry; null uses a private one
     */
    public IodaGeometry(CountryCentroids countries, Admin1Centroids admin1, MeterRegistry meters) {
        this.countries = countries;
        this.admin1 = admin1;
        this.regionUnmatched = Counter.builder(REGION_UNMATCHED)
                .description("IODA region alerts whose region has no Natural Earth admin-1 match, kept without geometry")
                .register(meters == null ? new SimpleMeterRegistry() : meters);
    }

    /**
     * The label point of {@code alert}'s entity.
     *
     * @param alert a folded alert
     * @return the point, or null for an ASN, an unknown country or an unmatched region
     */
    public Point resolve(IodaAlertsParser.Alert alert) {
        return switch (alert.entityType()) {
            case "country" -> country(alert.entityCode());
            case "region" -> region(alert);
            default -> null;
        };
    }

    private Point country(String code) {
        if (countries == null) {
            return null;
        }
        Optional<CountryRef> ref = countries.byIso2(code);
        return ref.map(c -> Geo.point(c.labelLon(), c.labelLat())).orElse(null);
    }

    private Point region(IodaAlertsParser.Alert alert) {
        String iso2 = IodaAlertsParser.country(alert);
        Optional<Admin1Centroids.Centroid> match = admin1 == null || iso2 == null
                ? Optional.empty() : admin1.byName(iso2, alert.entityName());
        if (match.isEmpty()) {
            regionUnmatched.increment();
            return null;
        }
        return Geo.point(match.get().lon(), match.get().lat());
    }
}
