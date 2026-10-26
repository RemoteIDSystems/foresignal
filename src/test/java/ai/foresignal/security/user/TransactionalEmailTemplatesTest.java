package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The four account email templates of {@link TransactionalEmailService} (prompt R2-007). */
class TransactionalEmailTemplatesTest {

    private static final Instant EXPIRES = Instant.parse("2026-10-29T14:05:00Z");

    @Test
    @DisplayName("welcome greets by name, lists the roles, carries the link and its expiry")
    void welcome() {
        TransactionalEmailService.Content c = TransactionalEmailService.welcome("Ada <Admin>",
                List.of("Editor", "Viewer"), "https://fs.example/activate?t=fsv_x", EXPIRES, "Foresignal");
        assertThat(c.subject()).isEqualTo("Your Foresignal account");
        assertThat(c.text()).contains("Hello Ada <Admin>,", "Your roles: Editor, Viewer.",
                "https://fs.example/activate?t=fsv_x", "expires on 2026-10-29 14:05 UTC");
        assertThat(c.html()).contains("Ada &lt;Admin&gt;").doesNotContain("<Admin>")
                .contains("href=\"https://fs.example/activate?t=fsv_x\"");
        assertThat(TransactionalEmailService.welcome("A", List.of(), "l", EXPIRES, "Foresignal").text())
                .contains("Your roles: none yet.");
    }

    @Test
    @DisplayName("reset carries the link, its expiry and the ignore-if-not-you line")
    void reset() {
        TransactionalEmailService.Content c = TransactionalEmailService.reset("Bo", "link-r", EXPIRES, "Foresignal");
        assertThat(c.subject()).isEqualTo("Reset your Foresignal password");
        assertThat(c.text()).contains("link-r", "2026-10-29 14:05 UTC", "If you did not request this");
    }

    @Test
    @DisplayName("role changed names the new roles, or says there are none")
    void roleChanged() {
        assertThat(TransactionalEmailService.roleChanged("Cy", List.of("Auditor"), "Foresignal").text())
                .contains("Your roles are now: Auditor.");
        assertThat(TransactionalEmailService.roleChanged("Cy", List.of(), "Foresignal").text())
                .contains("You no longer hold any role.");
    }

    @Test
    @DisplayName("suspended is a short factual notice")
    void suspended() {
        TransactionalEmailService.Content c = TransactionalEmailService.suspended("Di", "Foresignal");
        assertThat(c.subject()).isEqualTo("Your Foresignal account was suspended");
        assertThat(c.text()).contains("suspended", "settings are kept");
    }
}
