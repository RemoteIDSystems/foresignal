package ai.foresignal.chronicle;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.object.ObjectCaller;
import ai.foresignal.retention.RetentionRunResult;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * Invariant CHR-1 (prompt 131): a chronicle's captured history is never removed by global retention, only by its own
 * retention or its deletion; and an active chronicle's cursor ({@code chronicle.cursor.{id}}) clamps global retention
 * like every durable consumer, until it captures.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class ChronicleRetentionIndependenceIT extends RetentionItSupport {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired ChronicleService chronicles;
    @Autowired ChronicleCaptureService capture;
    @Autowired ChronicleRetentionJob ownRetention;
    @Autowired ChronicleStorage storage;
    @Autowired ChronicleRepository chronicleRows;
    @Autowired ChronicleCursorConsumers cursorConsumers;
    @Autowired PrincipalService principals;

    private ObjectCaller owner() {
        return new ObjectCaller(principals.owner().getId(), "test", Scope.ADMIN, true, Set.of());
    }

    private ChronicleEntity activeChronicle(String retention) {
        ChronicleEntity c = chronicles.create(JSON.readTree(("{'name':'Independent','retention':'" + retention
                + "','layers':[{'name':'Standard','selector':{'type':'records','sources':['" + STANDARD
                + "']},'capture':{'mode':'full'}}]}").replace('\'', '"')), owner(),
                ChronicleVocabulary.ORIGIN_OPERATOR, null);
        return chronicles.activate(c.getId(), null, owner(), false);
    }

    private long captured(String chronicleId) {
        return jdbc.queryForObject("SELECT count(*) FROM chronicle_version WHERE chronicle_id = ?", Long.class,
                chronicleId);
    }

    @Test
    @DisplayName("global retention deletes the versions; the chronicle's copy stays")
    void globalRetentionNeverDeletesTheCopy() {
        ChronicleEntity c = activeChronicle("forever");
        ingestAt(STANDARD, T0, record("x", STANDARD_KIND, T0, 1));
        Instant later = T0.plus(Duration.ofMinutes(2));
        ingestAt(STANDARD, later, record("later", STANDARD_KIND, later, 1));
        capture.catchUp();
        assertThat(captured(c.getId())).isEqualTo(2);

        runAt(T0.plus(Duration.ofDays(7)).plus(Duration.ofMinutes(5)));

        assertThat(versions(STANDARD, "x")).isZero();
        assertThat(captured(c.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("an active chronicle's cursor clamps retention until it captures")
    void cursorClampsRetention() {
        ChronicleEntity c = activeChronicle("forever");
        ingestAt(STANDARD, T0, record("x", STANDARD_KIND, T0, 1));
        Instant later = T0.plus(Duration.ofMinutes(2));
        ingestAt(STANDARD, later, record("later", STANDARD_KIND, later, 1));
        assertThat(cursorConsumers.retentionConsumers()).extracting(rc -> rc.name())
                .containsExactly(ChronicleVocabulary.cursorName(c.getId()));
        assertThat(job.consumers()).extracting(rc -> rc.name()).contains(ChronicleVocabulary.cursorName(c.getId()));

        RetentionRunResult held = runAt(T0.plus(Duration.ofDays(7)).plus(Duration.ofMinutes(5)));
        assertThat(versions(STANDARD, "x")).isEqualTo(1);
        assertThat(held.consumerClamp().active()).isTrue();
        assertThat(held.consumerClamp().consumer()).isEqualTo(ChronicleVocabulary.cursorName(c.getId()));

        capture.catchUp();
        assertThat(captured(c.getId())).isEqualTo(2);
        runAt(T0.plus(Duration.ofDays(7)).plus(Duration.ofMinutes(10)));
        assertThat(versions(STANDARD, "x")).isZero();
        assertThat(captured(c.getId())).isEqualTo(2);

        chronicles.stop(c.getId(), owner());
        assertThat(cursorConsumers.retentionConsumers()).as("a stopped chronicle holds nothing back").isEmpty();
    }

    @Test
    @DisplayName("only the chronicle's own retention removes its history, after the duration since stop")
    void ownRetention() {
        ChronicleEntity c = activeChronicle("P1D");
        ingestAt(STANDARD, T0.plusSeconds(10), record("y", STANDARD_KIND, T0.plusSeconds(10), 1));
        capture.catchUp();
        clock.setInstant(T0.plus(Duration.ofHours(1)));
        chronicles.stop(c.getId(), owner());

        clock.setInstant(T0.plus(Duration.ofHours(24)));
        assertThat(ownRetention.run()).isZero();
        assertThat(captured(c.getId())).isEqualTo(1);

        clock.setInstant(T0.plus(Duration.ofHours(26)));
        assertThat(ownRetention.run()).isEqualTo(1);
        assertThat(storage.leafExists(c.getId())).isFalse();
        ChronicleEntity after = chronicleRows.findById(c.getId()).orElseThrow();
        assertThat(after.getHistoryExpiredAt()).isNotNull();
        assertThat(after.getBytesUsed()).isZero();
        assertThat(after.getStatus()).isEqualTo(ChronicleVocabulary.STOPPED);
        assertThat(ownRetention.run()).as("once").isZero();
    }
}
