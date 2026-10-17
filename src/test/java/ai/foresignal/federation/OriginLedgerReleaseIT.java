package ai.foresignal.federation;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.records.RecordsTestSources;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * Releasing an origin claim whose write failed (checkpoint 124): {@link OriginLedger#claim} returns a
 * {@link OriginLedger.Release} with an applied claim, and {@link OriginLedger#release} puts the previous entry back
 * (or removes the entry the claim created) so a re-send of the same version is applied instead of counted as a
 * duplicate, but never undoes a newer claim. Runs against the real {@code federation_origin} table, in the context
 * {@code FederationIngestIT} uses.
 */
@IntegrationTest
@Import(RecordsTestSources.class)
class OriginLedgerReleaseIT {

    @Autowired OriginLedger ledger;

    private static String origin() {
        return "origin-" + UUID.randomUUID() + ":ext-1";
    }

    @Test
    @DisplayName("a released first claim removes the entry, so the same seq is applied again, not a duplicate")
    void releasedFirstClaimIsAppliedAgain() {
        String ref = origin();
        OriginLedger.Claim first = ledger.claim(ref, "peer-a", "local-1", 3, List.of("peer-a"));
        assertThat(first.verdict()).isEqualTo(OriginLedger.Verdict.APPLY);
        assertThat(first.release()).isNotNull();
        assertThat(first.release().previousSeq()).isNull();
        assertThat(ledger.claim(ref, "peer-a", "local-1", 3, List.of("peer-a")).verdict())
                .as("before the release, a re-send is a duplicate").isEqualTo(OriginLedger.Verdict.DUPLICATE);

        ledger.release(first.release());
        assertThat(ledger.storedSeq(ref)).isEqualTo(-1L);
        assertThat(ledger.owner(ref)).isNull();

        OriginLedger.Claim again = ledger.claim(ref, "peer-a", "local-1", 3, List.of("peer-a"));
        assertThat(again.verdict()).isEqualTo(OriginLedger.Verdict.APPLY);
        assertThat(ledger.storedSeq(ref)).isEqualTo(3L);
    }

    @Test
    @DisplayName("releasing seq 6 restores seq 5 and its owner; seq 6 is then applied again")
    void releaseRestoresThePreviousEntry() {
        String ref = origin();
        assertThat(ledger.claim(ref, "peer-a", "local-1", 5, List.of("peer-a")).verdict())
                .isEqualTo(OriginLedger.Verdict.APPLY);
        // peer-a is no longer alive, so the newer claim moves ownership to peer-b.
        OriginLedger.Claim six = ledger.claim(ref, "peer-b", "local-1", 6, List.of("peer-b"));
        assertThat(six.verdict()).isEqualTo(OriginLedger.Verdict.APPLY);
        assertThat(six.ownerSourceId()).isEqualTo("peer-b");
        assertThat(six.release().previousSeq()).isEqualTo(5L);
        assertThat(six.release().previousOwner()).isEqualTo("peer-a");
        assertThat(ledger.storedSeq(ref)).isEqualTo(6L);

        ledger.release(six.release());
        assertThat(ledger.storedSeq(ref)).isEqualTo(5L);
        assertThat(ledger.owner(ref)).isEqualTo("peer-a");
        assertThat(ledger.claim(ref, "peer-b", "local-1", 6, List.of("peer-b")).verdict())
                .as("the released version is applied when it arrives again").isEqualTo(OriginLedger.Verdict.APPLY);
    }

    @Test
    @DisplayName("a release after a newer claim leaves the newer claim in place")
    void releaseNeverUndoesANewerClaim() {
        String ref = origin();
        ledger.claim(ref, "peer-a", "local-1", 5, List.of("peer-a"));
        OriginLedger.Claim six = ledger.claim(ref, "peer-a", "local-1", 6, List.of("peer-a"));
        OriginLedger.Claim seven = ledger.claim(ref, "peer-a", "local-1", 7, List.of("peer-a"));
        assertThat(seven.verdict()).isEqualTo(OriginLedger.Verdict.APPLY);

        ledger.release(six.release());
        assertThat(ledger.storedSeq(ref)).isEqualTo(7L);
        assertThat(ledger.owner(ref)).isEqualTo("peer-a");
        assertThat(ledger.claim(ref, "peer-a", "local-1", 6, List.of("peer-a")).verdict())
                .isEqualTo(OriginLedger.Verdict.STALE);

        ledger.release(null);
        assertThat(ledger.storedSeq(ref)).as("a null release does nothing").isEqualTo(7L);
    }
}
