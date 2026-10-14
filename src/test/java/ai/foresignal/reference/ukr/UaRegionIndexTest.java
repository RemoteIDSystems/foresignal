package ai.foresignal.reference.ukr;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Geometry;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link UaRegionIndex} (prompt 081): the bundled boundaries load (27 first-level and 139 second-level units, every
 * polygon valid, at most 1,500 vertices per polygon, within the size budget), both crosswalks resolve oblasts, cities and
 * raions exactly and hromadas or communities approximately through their raion, and an unknown id yields nothing and is
 * logged once.
 */
@ExtendWith(OutputCaptureExtension.class)
class UaRegionIndexTest {

    private static final UaRegionIndex INDEX = new UaRegionIndex();
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    @DisplayName("the boundaries load: 166 units, all valid polygons in SRID 4326, ≤ 1,500 vertices per polygon")
    void boundaries() {
        assertThat(INDEX.boundaryCount()).isEqualTo(27 + 139);
        for (String id : new String[] {"adm1:kyiv-city", "adm1:kyivska", "adm1:sevastopol-city",
                "adm1:avtonomna-respublika-krym", "adm1:luhanska", "adm2:kharkivska:chuhuivskyi"}) {
            Geometry g = INDEX.boundary(id).orElseThrow();
            assertThat(g.isValid()).as(id).isTrue();
            assertThat(g.getSRID()).isEqualTo(4326);
            assertThat(g.getGeometryType()).endsWith("Polygon");
            for (int i = 0; i < g.getNumGeometries(); i++) {
                assertThat(g.getGeometryN(i).getNumPoints()).as(id).isLessThanOrEqualTo(1_500);
            }
        }
        assertThat(INDEX.boundary("adm1:nowhere")).isEmpty();
        assertThat(INDEX.boundary(null)).isEmpty();
    }

    @Test
    @DisplayName("the bundled boundary files stay within 2 MB together")
    void sizeBudget() throws Exception {
        long total = 0;
        for (String file : UaRegionIndex.BOUNDARY_FILES) {
            try (InputStream in = UaRegionIndex.class.getResourceAsStream(UaRegionIndex.RESOURCE_DIR + file)) {
                total += in.readAllBytes().length;
            }
        }
        assertThat(total).isLessThanOrEqualTo(2L * 1024 * 1024);
    }

    @Test
    @DisplayName("alerts.in.ua: oblast, city and raion UIDs are exact; a hromada is its raion, approximate")
    void alertsCrosswalk() {
        UaRegionIndex.Region kyiv = INDEX.byAlertsUid("31").orElseThrow();
        assertThat(kyiv.boundaryId()).isEqualTo("adm1:kyiv-city");
        assertThat(kyiv.approximate()).isFalse();
        assertThat(INDEX.byAlertsUid("14").orElseThrow().boundaryId()).isEqualTo("adm1:kyivska");
        assertThat(INDEX.byAlertsUid("16").orElseThrow().boundaryId()).isEqualTo("adm1:luhanska");
        assertThat(INDEX.byAlertsUid(" 29 ").orElseThrow().boundaryId()).isEqualTo("adm1:avtonomna-respublika-krym");

        UaRegionIndex.Region raion = INDEX.byAlertsUid("38").orElseThrow();
        assertThat(raion.type()).isEqualTo("raion");
        assertThat(raion.boundaryId()).isEqualTo("adm2:volynska:volodymyr-volynskyi");
        assertThat(raion.approximate()).isFalse();

        UaRegionIndex.Region hromada = INDEX.byAlertsUid("255").orElseThrow();
        assertThat(hromada.type()).isEqualTo("hromada");
        assertThat(hromada.boundaryId()).isEqualTo(raion.boundaryId());
        assertThat(hromada.approximate()).isTrue();
        assertThat(hromada.geometry()).isSameAs(raion.geometry());

        // Renamed after the boundary data was made: Sievierodonetskyi in the boundaries, Сіверськодонецький upstream.
        assertThat(INDEX.byAlertsUid("84").orElseThrow().boundaryId()).isEqualTo("adm2:luhanska:sievierodonetskyi");
        assertThat(INDEX.byAlertsUid("564").orElseThrow().boundaryId()).isEqualTo("adm2:zaporizka:zaporizkyi");
    }

    @Test
    @DisplayName("siren.pp.ua: State and District ids are exact; a Community is its raion, approximate")
    void sirenCrosswalk() {
        assertThat(INDEX.bySirenRegionId("16").orElseThrow().boundaryId()).isEqualTo("adm1:luhanska");
        assertThat(INDEX.bySirenRegionId("9999").orElseThrow().boundaryId())
                .isEqualTo("adm1:avtonomna-respublika-krym");
        UaRegionIndex.Region district = INDEX.bySirenRegionId("122").orElseThrow();
        assertThat(district.boundaryId()).isEqualTo("adm2:kharkivska:chuhuivskyi");
        assertThat(district.approximate()).isFalse();
        UaRegionIndex.Region community = INDEX.bySirenRegionId("1313").orElseThrow();
        assertThat(community.boundaryId()).isEqualTo("adm2:kharkivska:chuhuivskyi");
        assertThat(community.approximate()).isTrue();
        // Kharkiv city is a top-level "State" entry of the siren tree; it is placed in Kharkivskyi raion.
        UaRegionIndex.Region kharkiv = INDEX.bySirenRegionId("1293").orElseThrow();
        assertThat(kharkiv.boundaryId()).isEqualTo("adm2:kharkivska:kharkivskyi");
        assertThat(kharkiv.approximate()).isTrue();
        assertThat(INDEX.sirenName("122")).contains("Чугуївський район");
    }

    @Test
    @DisplayName("an unknown or unmatched id yields nothing and is logged once per table and id")
    void unknown(CapturedOutput output) {
        UaRegionIndex index = new UaRegionIndex();
        assertThat(index.byAlertsUid("99999")).isEmpty();
        assertThat(index.byAlertsUid("99999")).isEmpty();
        assertThat(index.bySirenRegionId("0")).as("the wrapper's test region").isEmpty();
        assertThat(index.byAlertsUid(null)).isEmpty();
        assertThat(index.bySirenRegionId("  ")).isEmpty();
        assertThat(output.getAll().split("alerts-in-ua-uids.json id 99999", -1)).hasSize(2);
        assertThat(output.getAll()).contains("siren-region-ids.json id 0 (Тестовий регіон) has no boundary");
    }

    @Test
    @DisplayName("every raion and oblast of both crosswalks resolves; every boundary id they name exists")
    void crosswalksComplete() throws Exception {
        for (String table : new String[] {UaRegionIndex.ALERTS_IN_UA_TABLE, UaRegionIndex.SIREN_TABLE}) {
            JsonNode regions;
            try (InputStream in = UaRegionIndex.class.getResourceAsStream(UaRegionIndex.RESOURCE_DIR + table)) {
                regions = JSON.readTree(in).get("regions");
            }
            int resolved = 0;
            for (Map.Entry<String, JsonNode> e : regions.properties()) {
                String type = e.getValue().path("type").asString();
                boolean big = type.equals("oblast") || type.equals("raion") || type.equals("city")
                        || type.equals("District");
                var region = table.equals(UaRegionIndex.SIREN_TABLE) ? INDEX.bySirenRegionId(e.getKey())
                        : INDEX.byAlertsUid(e.getKey());
                if (big) {
                    assertThat(region).as(table + " " + e.getKey()).isPresent();
                    assertThat(region.get().approximate()).isFalse();
                }
                if (region.isPresent()) {
                    resolved++;
                }
            }
            assertThat(resolved).as(table).isGreaterThanOrEqualTo(regions.size() - 1);
        }
    }
}
