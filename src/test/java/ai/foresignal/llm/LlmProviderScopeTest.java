package ai.foresignal.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ForesignalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Who sees, manages and creates which LLM provider (prompt R2-011). */
class LlmProviderScopeTest {

    static final String ALICE = "01ALICEPRINCIPAL0000000000";
    static final String BOB = "01BOBPRINCIPAL000000000000";

    final LlmProviderScope editor = new LlmProviderScope(ALICE, false, false, true);
    final LlmProviderScope operator = new LlmProviderScope(BOB, false, true, false);
    final LlmProviderScope admin = new LlmProviderScope(BOB, true, true, true);

    @Test
    @DisplayName("a user sees their own and the shared providers, not another user's; an admin sees all")
    void sees() {
        assertThat(editor.sees(ALICE)).isTrue();
        assertThat(editor.sees(null)).isTrue();
        assertThat(editor.sees(BOB)).isFalse();
        assertThat(admin.sees(ALICE)).isTrue();
        assertThat(LlmProviderScope.MACHINE.sees(ALICE)).isTrue();
    }

    @Test
    @DisplayName("shared providers are managed with llm.shared.manage; personal ones by their owner")
    void manages() {
        assertThat(editor.manages(ALICE)).isTrue();
        assertThat(editor.manages(null)).isFalse();
        assertThat(operator.manages(null)).isTrue();
        assertThat(operator.manages(ALICE)).isFalse();
        assertThat(admin.manages(ALICE)).isTrue();
    }

    @Test
    @DisplayName("the kind of a new provider follows the body, the permissions and the caller")
    void ownerForNew() {
        assertThat(editor.ownerForNew(null)).isEqualTo(ALICE);
        assertThat(operator.ownerForNew(null)).isNull();
        assertThat(LlmProviderScope.MACHINE.ownerForNew(null)).isNull();
        assertThat(LlmProviderScope.MACHINE.ownerForNew(true)).isNull();
        assertThat(admin.ownerForNew(false)).isEqualTo(BOB);
        assertThat(admin.ownerForNew(true)).isNull();
        assertThatThrownBy(() -> editor.ownerForNew(true)).isInstanceOfSatisfying(ForesignalException.class,
                e -> assertThat(e.getStatus().value()).isEqualTo(403));
        assertThatThrownBy(() -> operator.ownerForNew(false)).isInstanceOf(ForesignalException.class);
        assertThatThrownBy(() -> LlmProviderScope.MACHINE.ownerForNew(false)).isInstanceOf(ForesignalException.class);
    }
}
