package ai.foresignal.reference;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.reference.ReferenceFixtures;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * {@link CountyUnionService} (prompt 047) against the synthetic county fixture (Travis 48453 is the
 * square {@code [-98.2,30.0,-97.4,30.6]}, Harris 48201 another): the union by FIPS code, simplified, and
 * the empty answers.
 */
@IntegrationTest
class CountyUnionServiceIT {

    @Autowired CountyUnionService counties;
    @Autowired RegionTagger tagger;
    @Autowired JdbcTemplate jdbc;
    @Autowired ApplicationContext context;

    @Test
    @DisplayName("without ref_admin2 nothing is available")
    void notLoaded() {
        jdbc.update("TRUNCATE ref_admin2");
        counties.resetState();
        if (!tagger.loaded("ref_admin2")) {
            assertThat(counties.union(List.of(ReferenceFixtures.TRAVIS))).isEmpty();
        }
    }

    @Test
    @DisplayName("the union of the named counties, in SRID 4326; unknown and malformed codes are ignored")
    void union() {
        ReferenceFixtures.load(context, List.of("ne-admin2-us"));
        counties.resetState();
        assertThat(counties.available()).isTrue();

        Optional<Geometry> travis = counties.union(List.of(ReferenceFixtures.TRAVIS, "99999", "48x53"));

        assertThat(travis).isPresent();
        Envelope e = travis.get().getEnvelopeInternal();
        assertThat(e.getMinX()).isCloseTo(-98.2, org.assertj.core.data.Offset.offset(CountyGeometries.SIMPLIFY_DEGREES));
        assertThat(e.getMaxY()).isCloseTo(30.6, org.assertj.core.data.Offset.offset(CountyGeometries.SIMPLIFY_DEGREES));
        assertThat(travis.get().getSRID()).isEqualTo(4326);
        assertThat(counties.union(List.of("48453", "48201")).orElseThrow().getNumGeometries()).isEqualTo(2);
        assertThat(counties.union(List.of("99999"))).isEmpty();
        assertThat(counties.union(List.of())).isEmpty();
        assertThat(counties.cachedSets()).isEqualTo(3);
    }
}
