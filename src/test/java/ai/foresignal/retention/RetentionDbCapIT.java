package ai.foresignal.retention;

import static ai.foresignal.testsupport.retention.RetentionTestSources.KINDS;
import static ai.foresignal.testsupport.retention.RetentionTestSources.SHORT_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.SystemEvent;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.system.OperationalAlertService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

/**
 * The database-size cap end to end (prompt R2-015): a cap far below the real database steps the window down and
 * persists its working state, a cap at the floor raises {@code system.retention_cap_unmet} once, {@code
 * overwriteOldest: false} prunes nothing for the cap, {@code system.retention_cap_met} clears the alert, and switching
 * the cap off clears the state.
 *
 * <p>The database cannot be filled in a test, so the breach is made by the cap: {@code maxDbGb} of a millionth of a
 * GB (about 1 KB) is below any real database.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
@RecordApplicationEvents
class RetentionDbCapIT extends RetentionItSupport {

    /** About 1 KB. */
    static final double TINY_GB = 0.000001;

    @Autowired RetentionDbCap dbCap;
    @Autowired OperationalAlertService alerts;
    @Autowired ApplicationEvents events;

    /** Starts with no cap state and no raised alert. */
    @BeforeEach
    void clearCapState() {
        settings.remove(SettingKeys.RETENTION_DB_CAP);
        settings.invalidateCache();
        alerts.reset();
    }

    /** Puts the stored settings back to the defaults. */
    @AfterEach
    void clearSettings() {
        settings.remove(SettingKeys.RETENTION);
        settings.remove(SettingKeys.RETENTION_DB_CAP);
        settings.invalidateCache();
        alerts.reset();
    }

    private void store(Integer maxDays, Double maxDbGb, Boolean overwriteOldest) {
        settings.put(SettingKeys.RETENTION, RetentionPreviewIT.capped(maxDays, maxDbGb, overwriteOldest));
        settings.invalidateCache();
    }

    private List<SystemEvent> published(String subtype) {
        return events.stream(SystemEvent.class).filter(e -> e.subtype().equals(subtype)).toList();
    }

    @Test
    @DisplayName("over the cap and growing: the window steps down and the working state is persisted")
    void stepsDown() {
        ingestAt(KINDS, T0, record("cap1", SHORT_KIND, T0, 1));
        store(64, TINY_GB, true);

        RetentionRunResult result = runAt(T0.plus(Duration.ofDays(2)));

        RetentionDbCap.Outcome outcome = result.dbCap();
        assertThat(outcome).isNotNull();
        assertThat(outcome.capBytes()).isEqualTo((long) (TINY_GB * (1024d * 1024d * 1024d)));
        assertThat(outcome.sizeBeforeBytes()).isGreaterThan(outcome.capBytes());
        // "floor" only if every halving shrank the database (partition drops) all the way down to one day.
        assertThat(outcome.action()).isIn("stepped", "floor");
        assertThat(outcome.steps()).isPositive();
        assertThat(outcome.windowDays()).isLessThanOrEqualTo(32).isGreaterThanOrEqualTo(RetentionDbCap.FLOOR_DAYS);
        assertThat(outcome.met()).isFalse();

        RetentionDbCap.State state = dbCap.state();
        assertThat(state.windowDays()).isEqualTo(outcome.windowDays());
        assertThat(state.lastSizeBytes()).isEqualTo(outcome.sizeAfterBytes());
        assertThat(dbCap.tighten(RetentionPreviewIT.capped(64, TINY_GB, true)).maxDays())
                .isEqualTo(outcome.windowDays());
    }

    @Test
    @DisplayName("at the floor and still over: system.retention_cap_unmet fires once and the entity reported today stays")
    void floorRaisesUnmetOnce() {
        Instant now = T0.plus(Duration.ofDays(3));
        ingestAt(KINDS, now.minus(Duration.ofHours(1)), record("cap-fresh", SHORT_KIND, now.minus(Duration.ofHours(1)),
                1));
        store(1, TINY_GB, true);

        RetentionRunResult first = runAt(now);
        assertThat(first.dbCap().action()).isEqualTo("floor");
        assertThat(first.dbCap().steps()).isZero();
        assertThat(published("system.retention_cap_unmet")).hasSize(1);
        assertThat(published("system.retention_cap_unmet").get(0).severity()).isEqualTo("warning");
        assertThat(head(KINDS, "cap-fresh")).isTrue();
        assertThat(versions(KINDS, "cap-fresh")).isEqualTo(1);

        runAt(now.plus(Duration.ofMinutes(5)));
        assertThat(published("system.retention_cap_unmet")).hasSize(1);
    }

    @Test
    @DisplayName("overwriteOldest false: nothing is pruned for the cap, and the alert says so")
    void overwriteOldestOff() {
        store(64, TINY_GB, false);

        RetentionRunResult result = runAt(T0.plus(Duration.ofDays(2)));

        assertThat(result.dbCap().action()).isEqualTo("disabled");
        assertThat(result.dbCap().steps()).isZero();
        assertThat(result.dbCap().windowDays()).isNull();
        assertThat(published("system.retention_cap_unmet")).hasSize(1);
        assertThat(published("system.retention_cap_unmet").get(0).body()).contains("overwriteOldest is off");
    }

    @Test
    @DisplayName("back under the cap: system.retention_cap_met clears the alert; switching the cap off clears the state")
    void metAndOff() {
        store(1, TINY_GB, true);
        runAt(T0.plus(Duration.ofDays(2)));
        assertThat(published("system.retention_cap_unmet")).hasSize(1);
        assertThat(dbCap.state().lastSizeBytes()).isNotNull();

        store(1, 1_000_000d, true);
        RetentionRunResult under = runAt(T0.plus(Duration.ofDays(2)).plus(Duration.ofMinutes(5)));
        assertThat(under.dbCap().action()).isEqualTo("under");
        assertThat(under.dbCap().met()).isTrue();
        assertThat(published("system.retention_cap_met")).hasSize(1);

        store(1, null, true);
        RetentionRunResult off = runAt(T0.plus(Duration.ofDays(2)).plus(Duration.ofMinutes(10)));
        assertThat(off.dbCap()).isNull();
        assertThat(dbCap.state()).isEqualTo(RetentionDbCap.State.NONE);
    }
}
