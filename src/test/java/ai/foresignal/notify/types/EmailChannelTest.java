package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.assertGolden;
import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.PreparedRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The {@code email} type (prompt 018); sending is covered by {@code DeliveryWorkerIT} with GreenMail. */
class EmailChannelTest {

    final EmailChannel type = new EmailChannel();
    final Map<String, Object> config = Map.of("smtpHost", "smtp.example.invalid", "smtpPort", 587, "tls", "starttls",
            "from", "foresignal@example.invalid", "to", List.of("ops@example.invalid"));

    PreparedRequest.Mail mail(PreparedRequest request) {
        return (PreparedRequest.Mail) request.body();
    }

    @Test
    @DisplayName("matches the golden multipart email")
    void golden() {
        assertGolden("email", type.render(message(), settings(config, Map.of())));
    }

    @Test
    @DisplayName("plainTextOnly sends text/plain only (Winlink)")
    void plainTextOnly() {
        Map<String, Object> plain = new java.util.HashMap<>(config);
        plain.put("plainTextOnly", true);
        assertThat(mail(type.render(message(), settings(plain, Map.of()))).html()).isNull();
    }

    @Test
    @DisplayName("HTML is escaped; emoji pass; a 5,000-character body is sent whole; the origin header is set")
    void escapingAndLength() {
        PreparedRequest.Mail m = mail(type.render(NotifyFixtures.message("critical", "🔥 <x> & **b**"), settings(config, Map.of())));
        assertThat(m.html()).contains("🔥 &lt;x&gt; &amp; <strong>b</strong>").contains("critical");
        assertThat(m.headers()).containsEntry("X-Foresignal-Origin", "abcd1234");
        assertThat(mail(type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(5000)), settings(config, Map.of()))).text())
                .contains(NotifyFixtures.longBody(5000).strip());
    }

    @Test
    @DisplayName("validation: port range, at least one recipient, credentials in pairs")
    void validation() {
        assertThat(type.validate(Map.of("smtpPort", 70000, "to", List.of()), Map.of("username", "u"))).hasSize(3);
        assertThat(type.validate(config, Map.of("username", "u", "password", "p"))).isEmpty();
    }
}
