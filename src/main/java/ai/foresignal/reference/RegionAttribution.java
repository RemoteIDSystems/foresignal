package ai.foresignal.reference;

import java.util.ArrayList;
import java.util.List;

/**
 * The credits of the reference data (prompt 040), in one place so prompt 109 can aggregate them with
 * the sources' own attributions.
 *
 * <p>GeoNames is CC BY 4.0: its credit goes in {@code THIRD_PARTY_NOTICES.md}, in {@code GET /v1/reference}
 * and in every response field derived from {@code near}. Natural Earth is public domain and needs no
 * credit; it is credited anyway.
 */
public final class RegionAttribution {

    /** The GeoNames credit (CC BY 4.0). */
    public static final String GEONAMES =
            "Place names from GeoNames (https://www.geonames.org/), CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/)";

    /** The Natural Earth credit (public domain; credited anyway). */
    public static final String NATURAL_EARTH =
            "Made with Natural Earth. Free vector and raster map data @ naturalearthdata.com.";

    /** Natural Earth draws de facto boundaries; shown with the reference status. */
    public static final String BOUNDARY_NOTE = "Natural Earth draws de facto boundaries (who controls the ground),"
            + " which are not an endorsement of any claim. foresignal.reference.ne-admin0-variant selects one of"
            + " Natural Earth's point-of-view files instead.";

    private RegionAttribution() {}

    /** Every credit that applies to the reference data. */
    public static List<String> all() {
        return List.of(NATURAL_EARTH, GEONAMES);
    }

    /** The credits of {@code tags}: GeoNames when it carries a {@code near}, else none. */
    public static List<String> of(RegionTags tags) {
        List<String> out = new ArrayList<>();
        if (tags != null && tags.near() != null) {
            out.add(GEONAMES);
        }
        return out;
    }
}
