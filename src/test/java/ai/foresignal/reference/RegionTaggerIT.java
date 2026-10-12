package ai.foresignal.reference;

import static ai.foresignal.testsupport.reference.ReferenceFixtures.AUSTIN;
import static ai.foresignal.testsupport.reference.ReferenceFixtures.COAST;
import static ai.foresignal.testsupport.reference.ReferenceFixtures.MEXICO;
import static ai.foresignal.testsupport.reference.ReferenceFixtures.NNE_OF_AUSTIN;
import static ai.foresignal.testsupport.reference.ReferenceFixtures.SEA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.reference.ReferenceFixtures;
import ai.foresignal.testsupport.reference.RegionTestSources;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * {@link RegionTagger} over the fixture reference data (prompt 040): an inland point gets country,
 * first-level region and county; a point just off a coast gets the nearest country within 5 km; a point
 * at sea gets no country and a sea area; the nearest place gives the distance, bearing and text; and with
 * no reference data the country still comes from the bundled polygons and everything else is null.
 */
@IntegrationTest
@Import(RegionTestSources.class)
class RegionTaggerIT {

    static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired RegionTagger tagger;
    @Autowired ApplicationContext context;

    @Test
    @DisplayName("inland: country, admin1, county and their names; within 1 km of a place the text says in")
    void inland() {
        ReferenceFixtures.loadAll(context);
        RegionTags tags = tagger.tag(AUSTIN[0], AUSTIN[1]);
        assertThat(tags.iso2()).isEqualTo("US");
        assertThat(tags.countryName()).isEqualTo("United States of America");
        assertThat(tags.admin1()).isEqualTo("US-TX");
        assertThat(tags.admin1Name()).isEqualTo("Texas");
        assertThat(tags.admin2()).isEqualTo("48453");
        assertThat(tags.countyName()).isEqualTo("Travis");
        assertThat(tags.sea()).isNull();
        assertThat(tags.near().name()).isEqualTo("Austin");
        assertThat(tags.nearText()).isEqualTo("in Austin, TX");
        assertThat(tags.toMap()).containsEntry("attribution", RegionAttribution.GEONAMES);
    }

    @Test
    @DisplayName("KNN near: 12 km NNE of Austin, TX, with the bearing from the place, ISO admin1 and population")
    void near() {
        ReferenceFixtures.loadAll(context);
        RegionNear near = tagger.tag(NNE_OF_AUSTIN[0], NNE_OF_AUSTIN[1]).near();
        assertThat(near.geonameid()).isEqualTo(4671654L);
        assertThat(near.distanceKm()).isCloseTo(12.2, within(0.6));
        assertThat(near.bearingDeg()).isBetween(11.25, 33.75);
        assertThat(near.compass()).isEqualTo("NNE");
        assertThat(near.admin1()).as("GeoNames US.TX mapped to ISO through ref_admin1.gn_code").isEqualTo("US-TX");
        assertThat(near.population()).isEqualTo(931830L);
        assertThat(near.text()).isEqualTo("12 km NNE of Austin, TX");
        assertThat(near.text(true)).isEqualTo("8 mi NNE of Austin, TX");
    }

    @Test
    @DisplayName("outside the US the text names the country: … of Monterrey, Mexico")
    void foreignSuffix() {
        ReferenceFixtures.loadAll(context);
        RegionTags tags = tagger.tag(MEXICO[0], MEXICO[1]);
        assertThat(tags.iso2()).isEqualTo("MX");
        assertThat(tags.admin2()).as("counties are US only").isNull();
        assertThat(tags.nearText()).endsWith(" of Monterrey, Mexico");
    }

    @Test
    @DisplayName("coastal fallback: 3 km off the coast is the nearest country, with no sea")
    void coastal() {
        ReferenceFixtures.loadAll(context);
        RegionTags tags = tagger.tag(COAST[0], COAST[1]);
        assertThat(tags.iso2()).isEqualTo("US");
        assertThat(tags.sea()).as("sea is set only at sea").isNull();
        assertThat(tags.admin1()).as("no admin1 within 5 km").isNull();
    }

    @Test
    @DisplayName("at sea: no country, the sea area, and still a nearest place")
    void sea() {
        ReferenceFixtures.loadAll(context);
        RegionTags tags = tagger.tag(SEA[0], SEA[1]);
        assertThat(tags.iso2()).isNull();
        assertThat(tags.countryName()).isNull();
        assertThat(tags.sea()).isEqualTo("Gulf of Mexico");
        assertThat(tags.near().name()).isEqualTo("Houston");
    }

    @Test
    @DisplayName("no reference data: the country comes from the bundled polygons, everything else is null")
    void emptyReference() {
        assertThat(tagger.referenceLoaded()).isFalse();
        RegionTags tags = tagger.tag(AUSTIN[0], AUSTIN[1]);
        assertThat(tags.iso2()).isEqualTo("US");
        assertThat(tags.countryName()).isEqualTo("United States of America");
        assertThat(tags.admin1()).isNull();
        assertThat(tags.admin2()).isNull();
        assertThat(tags.near()).isNull();
        assertThat(tags.toMap()).containsEntry("attribution", null);
        RegionTags open = tagger.tag(-40, 30);
        assertThat(open.isEmpty()).isTrue();
        assertThat(tagger.tag(Double.NaN, 0)).isSameAs(RegionTags.EMPTY);
        assertThat(tagger.tag(200, 0)).isSameAs(RegionTags.EMPTY);
    }

    @Test
    @DisplayName("a reference load drops the cache: bundled tags are replaced by full ones")
    void cacheDroppedOnLoad() {
        RegionTags first = tagger.tag(AUSTIN[0], AUSTIN[1]);
        assertThat(first.admin1()).isNull();
        assertThat(tagger.tag(AUSTIN[0] + 0.001, AUSTIN[1] - 0.001)).as("same rounded point, cached").isSameAs(first);
        assertThat(tagger.cacheSize()).isPositive();
        ReferenceFixtures.load(context, List.of("ne-admin0", "ne-admin1"));
        assertThat(tagger.tag(AUSTIN[0], AUSTIN[1]).admin1()).isEqualTo("US-TX");
    }

    @Test
    @DisplayName("areas and lines are tagged at a point on them")
    void representativePoint() {
        ReferenceFixtures.loadAll(context);
        Polygon travis = WGS84.createPolygon(new Coordinate[] {new Coordinate(-98.1, 30.1),
            new Coordinate(-97.5, 30.1), new Coordinate(-97.5, 30.5), new Coordinate(-98.1, 30.5),
            new Coordinate(-98.1, 30.1)});
        assertThat(tagger.tag(travis).admin2()).isEqualTo("48453");
        LineString road = WGS84.createLineString(new Coordinate[] {new Coordinate(-98.0, 30.2),
            new Coordinate(-97.6, 30.4)});
        assertThat(tagger.tag(road).admin2()).isEqualTo("48453");
        assertThat(tagger.tag((org.locationtech.jts.geom.Geometry) null)).isSameAs(RegionTags.EMPTY);
    }
}
