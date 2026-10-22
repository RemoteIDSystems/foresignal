package ai.foresignal.retention;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

/**
 * Prompt 109, task 2 ("keep the evidence"), extending 019's {@code fetch_log} pruning: a fetch row that wrote
 * versions is kept for {@code max(retention.provenance, the largest version window)}, so the license snapshotted on
 * it outlives every version it describes; a row that wrote nothing still goes after 7 days.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class FetchLogRetentionIT extends RetentionItSupport {

    /** The {@code retention.provenance} default (019). */
    static final Duration PROVENANCE = Duration.ofDays(30);

    @Autowired RetentionResolver resolver;

    @Test
    @DisplayName("a versioned fetch row survives while its versions are retained, then goes after the keep window")
    void versionedRowsOutliveTheirVersions() {
        ingestAt(STANDARD, T0, record("x", STANDARD_KIND, T0, 1));
        Instant again = T0.plus(Duration.ofSeconds(10));
        ingestAt(STANDARD, again, record("x", STANDARD_KIND, again, 1));
        assertThat(fetchRows(true)).isEqualTo(1);
        assertThat(fetchRows(false)).isEqualTo(1);

        runAt(T0.plus(Duration.ofDays(8)));
        assertThat(fetchRows(false)).as("an empty fetch row goes after 7 days").isZero();
        assertThat(fetchRows(true)).isEqualTo(1);

        Duration keep = resolver.largestWindow().compareTo(PROVENANCE) > 0 ? resolver.largestWindow() : PROVENANCE;
        for (Instant at : new Instant[] {T0.plus(PROVENANCE).plus(Duration.ofDays(1)),
                T0.plus(keep).minus(Duration.ofDays(1))}) {
            runAt(at);
            if (versions(STANDARD, "x") > 0) {
                assertThat(fetchRows(true)).as("versions still retained at " + at + ": the batch license stays")
                        .isEqualTo(1);
            }
        }
        assertThat(fetchRows(true)).as("kept until max(provenance, largest window)").isEqualTo(1);

        runAt(T0.plus(keep).plus(Duration.ofDays(1)));
        assertThat(fetchRows(true)).isZero();
    }

    private long fetchRows(boolean withVersions) {
        return jdbc.queryForObject("SELECT count(*) FROM fetch_log WHERE source_id = ? AND "
                + (withVersions ? "versions_written > 0" : "versions_written = 0"), Long.class, STANDARD);
    }
}
