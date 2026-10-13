package ai.foresignal.source.adapters.goes;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code fire-cluster} from {@code goes-fdc} end to end (prompt 048): 22 clusters over the captured CONUS
 * scan, polygons for multi-pixel clusters, identities carried by pixel overlap (no new versions for an
 * identical scan), and tombstones after three processed scans without members.
 */
class FireClusterIngestIT extends WeatherIngestSupport {

    static final String ID = GoesFdcAdapter.ID;

    int clusterVersions(PersistReason reason) {
        Integer n = jdbc.queryForObject("select count(*) from record_version where source_id = ? and kind = 'fire-cluster'"
                + " and persist_reason = ?", Integer.class, ID, reason.code());
        return n == null ? 0 : n;
    }

    List<String> liveClusters() {
        return jdbc.queryForList("select h.external_id from record_head h where h.source_id = ? and h.kind = 'fire-cluster'"
                + " and not h.tombstone order by h.external_id", String.class, ID);
    }

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-12T21:59:30Z"));
        configure(ID, false, Map.of(GoesFdcAdapter.SCOPE_PARAM, "conus"));
        FireDetectionIngestIT.emptyListings();
    }

    @Test
    @DisplayName("22 clusters; identities carried by overlap; tombstones after 3 processed scans without members")
    void lifecycle() {
        FireDetectionIngestIT.scans(FireDetectionIngestIT.HOUR_21, null, FireDetectionIngestIT.key("215117"));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(clusterVersions(PersistReason.FIRST)).isEqualTo(22);
        List<String> ids = liveClusters();
        assertThat(ids).hasSize(22).allMatch(id -> id.startsWith("fc-"));
        Integer polygons = jdbc.queryForObject("select count(*) from record_live l join record_head h"
                + " on h.source_id = l.source_id and h.external_id = l.external_id where l.source_id = ?"
                + " and h.kind = 'fire-cluster' and ST_GeometryType(l.geom) = 'ST_Polygon'", Integer.class, ID);
        assertThat(polygons).isEqualTo(3);

        // An identical scan: the same ids, no new versions.
        FireDetectionIngestIT.scans(FireDetectionIngestIT.HOUR_21, FireDetectionIngestIT.key("215117"),
                FireDetectionIngestIT.key("215617"));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(liveClusters()).isEqualTo(ids);
        assertThat(clusterVersions(PersistReason.FIRST)).isEqualTo(22);
        Integer all = jdbc.queryForObject("select count(*) from record_version where source_id = ? and kind = 'fire-cluster'",
                Integer.class, ID);
        assertThat(all).isEqualTo(22);

        // Only the two Texas pixels from now on: 20 clusters lose their members.
        configure(ID, false, Map.of());
        clock.setInstant(Instant.parse("2026-10-12T22:02:30Z"));
        FireDetectionIngestIT.scans(FireDetectionIngestIT.HOUR_22, null, FireDetectionIngestIT.key("220117"));
        assertThat(fetch(ID).success()).isTrue();
        clock.setInstant(Instant.parse("2026-10-12T22:07:30Z"));
        FireDetectionIngestIT.scans(FireDetectionIngestIT.HOUR_22, FireDetectionIngestIT.key("220117"),
                FireDetectionIngestIT.key("220617"));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(clusterVersions(PersistReason.TOMBSTONE)).isZero();
        clock.setInstant(Instant.parse("2026-10-12T22:12:30Z"));
        FireDetectionIngestIT.scans(FireDetectionIngestIT.HOUR_22, FireDetectionIngestIT.key("220617"),
                FireDetectionIngestIT.key("221117"));
        assertThat(fetch(ID).success()).isTrue();

        assertThat(clusterVersions(PersistReason.TOMBSTONE)).isEqualTo(20);
        assertThat(liveClusters()).hasSize(2).isSubsetOf(ids);
    }
}
