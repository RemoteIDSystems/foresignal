package ai.foresignal.notify.mesh;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.DigestBuilder;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.Truncator;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The compact mesh text (prompt 084): the line's parts, the MeshCore 133-character and 127-byte caps and the
 * Meshtastic 200-byte cap with multi-byte input, the {@code #code} suffix always kept whole, and the resolved and
 * digest forms.
 */
class MeshShortTextFormatterTest {

    static final Instant FIRED = Instant.parse("2026-05-01T12:05:00Z");

    static NotificationMessage fired(long alertId, String severity, String shortTitle, Map<String, Object> extra) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("alertId", alertId);
        data.put("ruleName", shortTitle);
        data.put("firedAt", FIRED.toString());
        data.putAll(extra);
        return NotificationMessage.builder("alert", "alert.fired", "[" + severity + "] " + shortTitle, severity)
                .id(42L).shortTitle(shortTitle).data(data).createdAt(FIRED).alertId(alertId).build();
    }

    @Test
    @DisplayName("an alert line: ASCII severity, short title, nearest place, UTC time and the short code")
    void line() {
        NotificationMessage m = fired(7, "warning", "Quakes",
                Map.of("region", Map.of("near", Map.of("name", "Austin"))));
        assertThat(MeshShortTextFormatter.format(m, MeshShortTextFormatter.Limit.MESHTASTIC))
                .isEqualTo("!! Quakes Austin 1205Z #0007");
        assertThat(MeshShortTextFormatter.format(fired(31, "critical", "Fire", Map.of()),
                MeshShortTextFormatter.Limit.MESHCORE_CHANNEL)).isEqualTo("!!! Fire 1205Z #000Z");
        assertThat(MeshShortTextFormatter.format(fired(32, "info", "Info", Map.of()),
                MeshShortTextFormatter.Limit.MESHCORE_CHANNEL)).isEqualTo("i Info 1205Z #0010");
    }

    @Test
    @DisplayName("without a near place the record position is the 2-decimal lat,lon")
    void coordinates() {
        NotificationMessage m = fired(7, "warning", "Quakes", Map.of("record",
                Map.of("geometry", Map.of("type", "Point", "coordinates", List.of(-97.7431, 30.2672)))));
        assertThat(MeshShortTextFormatter.format(m, MeshShortTextFormatter.Limit.MESHTASTIC))
                .isEqualTo("!! Quakes 30.27,-97.74 1205Z #0007");
        NotificationMessage polygon = fired(7, "warning", "Zone", Map.of("record", Map.of("geometry", Map.of("type",
                "Polygon", "coordinates", List.of(List.of(List.of(0.0, 0.0), List.of(2.0, 0.0), List.of(2.0, 2.0),
                        List.of(0.0, 2.0)))))));
        assertThat(MeshShortTextFormatter.format(polygon, MeshShortTextFormatter.Limit.MESHTASTIC))
                .isEqualTo("!! Zone 1.00,1.00 1205Z #0007");
    }

    @Test
    @DisplayName("MeshCore: a long multi-byte title is cut to 133 characters and 127 bytes; the code survives whole")
    void meshcoreCaps() {
        String title = "Überschwemmung 🌊 ".repeat(20).strip();
        String out = MeshShortTextFormatter.format(fired(123456789, "critical", title, Map.of()),
                MeshShortTextFormatter.Limit.MESHCORE_CHANNEL);
        assertThat(out).endsWith(" #QK8N").startsWith("!!! Überschwemmung");
        assertThat(out.length()).isLessThanOrEqualTo(133);
        assertThat(out.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(127);
        assertThat(out).doesNotContain("�");
        assertThat(out.codePoints().noneMatch(cp -> cp >= 0xD800 && cp <= 0xDFFF)).as("no split code point").isTrue();
        String ascii = MeshShortTextFormatter.format(fired(1, "info", "x".repeat(300), Map.of()),
                MeshShortTextFormatter.Limit.MESHCORE_DIRECT);
        assertThat(ascii.length()).isBetween(120, 133);
        assertThat(ascii).endsWith(Truncator.ELLIPSIS + " #0001");
    }

    @Test
    @DisplayName("Meshtastic: 200 UTF-8 bytes at most with multi-byte input; the code suffix is kept")
    void meshtasticCap() {
        String title = "火事".repeat(120);
        String out = MeshShortTextFormatter.format(fired(5, "warning", title, Map.of()),
                MeshShortTextFormatter.Limit.MESHTASTIC);
        assertThat(out.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(200);
        assertThat(out).endsWith(" #0005");
        assertThat(out.codePoints().noneMatch(cp -> cp >= 0xD800 && cp <= 0xDFFF)).isTrue();
    }

    @Test
    @DisplayName("a resolved alert renders OK {title} #{code} with the rule name")
    void resolved() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("alertId", 7);
        data.put("ruleName", "Quakes");
        NotificationMessage m = NotificationMessage.builder("alert", "alert.resolved", "✅ Resolved", "warning")
                .shortTitle("Resolved: Quakes").data(data).alertId(7L).updateOf(42L).build();
        assertThat(MeshShortTextFormatter.format(m, MeshShortTextFormatter.Limit.MESHCORE_CHANNEL))
                .isEqualTo("OK Quakes #0007");
    }

    @Test
    @DisplayName("a digest is one line naming the top alert (highest severity, newest)")
    void digest() {
        NotificationMessage a = fired(10, "warning", "Quakes", Map.of()).withId(1L);
        NotificationMessage b = fired(11, "critical", "Fire", Map.of()).withId(2L);
        NotificationMessage c = fired(12, "info", "Info", Map.of()).withId(3L);
        NotificationMessage digest = DigestBuilder.build(List.of(a, b, c), 20);
        assertThat(digest.subtype()).isEqualTo(DigestBuilder.SUBTYPE);
        assertThat(MeshShortTextFormatter.format(digest, MeshShortTextFormatter.Limit.MESHCORE_CHANNEL))
                .isEqualTo("3 alerts, top: Fire #000B");
        assertThat(MeshRenderings.folded(digest)).isEqualTo(2);
        assertThat(MeshRenderings.shortCode(digest)).isEqualTo("000B");
    }

    @Test
    @DisplayName("a system message has no code; line breaks never reach the air")
    void system() {
        NotificationMessage m = NotificationMessage.builder("system", "system.source_failing", "Source\nfailing",
                "warning").shortTitle("Source\nfailing").createdAt(FIRED).build();
        assertThat(MeshShortTextFormatter.format(m, MeshShortTextFormatter.Limit.MESHTASTIC))
                .isEqualTo("!! Source failing 1205Z");
    }

    @Test
    @DisplayName("short codes: Crockford base32 of the low 20 bits; look-alikes are normalized")
    void codes() {
        assertThat(MeshShortCode.of(0)).isEqualTo("0000");
        assertThat(MeshShortCode.of(1_048_575)).isEqualTo("ZZZZ");
        assertThat(MeshShortCode.of(1_048_576)).isEqualTo("0000");
        assertThat(MeshShortCode.normalize("#qk8n")).isEqualTo("QK8N");
        assertThat(MeshShortCode.normalize("o0il")).isEqualTo("0011");
        assertThat(MeshShortCode.normalize("ABCU")).isNull();
        assertThat(MeshShortCode.normalize("ABC")).isNull();
    }
}
