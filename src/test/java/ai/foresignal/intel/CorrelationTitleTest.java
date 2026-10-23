package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The derived event title and explanation count signals and sources in the right number. */
class CorrelationTitleTest {

    private static IntelEventMemberEntity member(String sourceId, String externalId) {
        IntelEventMemberEntity member = new IntelEventMemberEntity("e1", "record", sourceId, externalId, 1);
        member.setSignalFamily("conflict");
        return member;
    }

    @Test
    @DisplayName("one signal from one source is singular; more are plural")
    void plurals() {
        assertThat(CorrelationEngine.titleOf(List.of(member("acled", "a"))))
                .isEqualTo("1 signal from 1 source: conflict");
        assertThat(CorrelationEngine.titleOf(List.of(member("acled", "a"), member("acled", "b"))))
                .isEqualTo("2 signals from 1 source: conflict");
        assertThat(CorrelationEngine.titleOf(List.of(member("acled", "a"), member("gdelt", "b"))))
                .isEqualTo("2 signals from 2 sources: conflict");
        assertThat(CorrelationEngine.explanationOf(List.of(member("acled", "a")), null, null))
                .isEqualTo("1 signal from 1 independent source (conflict) over 0 h; evidence pinned to versions.");
    }
}
