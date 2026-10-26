package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ForesignalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link ChannelScope}: who sees, manages and creates which channel (prompt R2-009). */
class ChannelScopeTest {

    static final String ALICE = "01ALICE";
    static final String BOB = "01BOB";

    private static final ChannelScope EDITOR = new ChannelScope(ALICE, false, false, true);
    private static final ChannelScope OPERATOR = new ChannelScope(BOB, false, true, false);
    private static final ChannelScope ADMIN = new ChannelScope(ALICE, true, true, true);

    @Test
    @DisplayName("a user sees their own and the global channels, and manages only their own")
    void editor() {
        assertThat(EDITOR.sees(ALICE)).isTrue();
        assertThat(EDITOR.sees(null)).isTrue();
        assertThat(EDITOR.sees(BOB)).isFalse();
        assertThat(EDITOR.manages(ALICE)).isTrue();
        assertThat(EDITOR.manages(null)).isFalse();
        assertThat(EDITOR.ownerForNew(null)).isEqualTo(ALICE);
        assertThat(EDITOR.ownerForNew(false)).isEqualTo(ALICE);
        assertThatThrownBy(() -> EDITOR.ownerForNew(true)).isInstanceOf(ForesignalException.class)
                .satisfies(e -> assertThat(((ForesignalException) e).getProperties())
                        .containsEntry("requiredPermission", "channels.global"));
    }

    @Test
    @DisplayName("an operator manages global channels, never another user's, and creates global ones")
    void operator() {
        assertThat(OPERATOR.manages(null)).isTrue();
        assertThat(OPERATOR.sees(ALICE)).isFalse();
        assertThat(OPERATOR.ownerForNew(null)).isNull();
        assertThatThrownBy(() -> OPERATOR.ownerForNew(false)).isInstanceOf(ForesignalException.class);
    }

    @Test
    @DisplayName("an admin sees and manages everything; an API token too, and only creates global channels")
    void adminAndMachine() {
        assertThat(ADMIN.sees(BOB)).isTrue();
        assertThat(ADMIN.manages(BOB)).isTrue();
        assertThat(ADMIN.ownerForNew(null)).isEqualTo(ALICE);
        assertThat(ADMIN.ownerForNew(true)).isNull();
        assertThat(ChannelScope.MACHINE.manages(BOB)).isTrue();
        assertThat(ChannelScope.MACHINE.ownerForNew(null)).isNull();
        assertThatThrownBy(() -> ChannelScope.MACHINE.ownerForNew(false)).isInstanceOf(ForesignalException.class);
        assertThat(ChannelScope.of((jakarta.servlet.http.HttpServletRequest) null)).isEqualTo(ChannelScope.MACHINE);
    }
}
