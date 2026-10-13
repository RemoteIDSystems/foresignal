package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The canonical message and settings of the channel type tests (prompt 018), and the golden request
 * comparison against {@code src/test/resources/fixtures/notify/<type>.json}.
 *
 * <p>The message deliberately contains {@code <}, {@code &}, {@code *} and {@code _} in text, bold,
 * italic, a bullet and inline code, so every fixture pins the destination's escaping. Hosts end in
 * {@code .invalid} and every token is fake.
 */
public final class NotifyFixtures {

    /** The fixed time of the canonical message and settings. */
    public static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    /** The alert link. */
    public static final String URL = "https://fs.example/v1/alerts/7";

    /** The title. */
    public static final String TITLE = "[warning] Quake <M5> & more";

    /** The markdown body. */
    public static final String BODY = "**M5.1** near _Austin_ <TX> & co\n- depth 10 km\n\nsee `a_b*c`";

    /** The install id. */
    public static final String ORIGIN = "abcd1234";

    /** The rule. */
    public static final UUID RULE = UUID.fromString("11111111-1111-1111-1111-111111111111");

    /** The channel. */
    public static final UUID CHANNEL = UUID.fromString("00000000-0000-0000-0000-000000000001");

    static final JsonMapper JSON = JsonMapper.builder().build();

    private NotifyFixtures() {}

    /** The canonical message. */
    public static NotificationMessage message() {
        return message("warning", BODY);
    }

    /** The canonical message with another severity and body. */
    public static NotificationMessage message(String severity, String body) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("alertId", 7);
        data.put("ruleName", "Quakes");
        return NotificationMessage.builder("alert", "alert.fired", TITLE, severity)
                .id(42L).body(body).url(URL).links(List.of(new NotificationMessage.Link("Open alert", URL)))
                .data(data).tags(List.of("quake")).origin(ORIGIN).createdAt(T).alertId(7L).ruleId(RULE).build();
    }

    /** The media URL of the test map (prompt 055). */
    public static final String MAP_URL = "https://fs.example/v1/media/01JA0000000000000000000000";

    /** A small PNG standing in for a rendered map (prompt 055): 4 × 3 pixels. */
    public static byte[] mapPng() {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(4, 3,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        image.setRGB(1, 1, 0xFF2266AA);
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        try {
            javax.imageio.ImageIO.write(image, "png", out);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return out.toByteArray();
    }

    /** {@code message} carrying the test map as a loaded image attachment (prompt 055). */
    public static NotificationMessage withMap(NotificationMessage message) {
        byte[] png = mapPng();
        return message.withAttachments(List.of(new NotificationMessage.Attachment("map.png", "image/png", png,
                "01JA0000000000000000000000", png.length, MAP_URL)));
    }

    /** {@code message} carrying the test map as a link-only attachment (prompt 055). */
    public static NotificationMessage withMapLink(NotificationMessage message) {
        return message.withAttachments(List.of(new NotificationMessage.Attachment("map.png", "image/png", null,
                "01JA0000000000000000000000", 4321, MAP_URL)));
    }

    /** A lifecycle update of the canonical message. */
    public static NotificationMessage update(String subtype) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("alertId", 7);
        data.put("ruleName", "Quakes");
        return NotificationMessage.builder("alert", subtype, "✅ Resolved 12:05 UTC — " + TITLE, "warning")
                .id(43L).body("Resolved.\n\n~~M5.1 near Austin~~").url(URL)
                .links(List.of(new NotificationMessage.Link("Open alert", URL))).data(data).origin(ORIGIN)
                .createdAt(T.plusSeconds(300)).alertId(7L).ruleId(RULE).updateOf(42L).build();
    }

    /** Settings with {@code config} and {@code secrets}, delivery 99, zone UTC. */
    public static ChannelSettings settings(Map<String, Object> config, Map<String, String> secrets) {
        return new ChannelSettings(CHANNEL, "test", config, secrets, ZoneId.of("UTC"), 99, T, ORIGIN,
                "Foresignal/test (+https://foresignal.ai; install=" + ORIGIN + ")");
    }

    /** The golden fixture of {@code type}. */
    public static JsonNode golden(String type) {
        try (InputStream in = NotifyFixtures.class.getResourceAsStream("/fixtures/notify/" + type + ".json")) {
            assertThat(in).as("fixture for " + type).isNotNull();
            return JSON.readTree(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Asserts that {@code actual} equals the golden request of {@code type}. */
    public static void assertGolden(String type, PreparedRequest actual) {
        JsonNode golden = golden(type);
        assertThat(actual.method()).isEqualTo(golden.get("method").asString());
        assertThat(actual.url()).isEqualTo(golden.get("url").asString());
        for (String name : golden.get("headers").propertyNames()) {
            assertThat(actual.headers()).as("header " + name).containsEntry(name, golden.get("headers").get(name).asString());
        }
        if (golden.has("json")) {
            assertThat(JSON.readTree(actual.bodyText())).isEqualTo(golden.get("json"));
        } else if (golden.has("text")) {
            assertThat(actual.bodyText()).isEqualTo(golden.get("text").asString());
        } else if (golden.has("form")) {
            PreparedRequest.Form form = (PreparedRequest.Form) actual.body();
            assertThat((JsonNode) JSON.valueToTree(form.fields())).isEqualTo(golden.get("form"));
        } else if (golden.has("mail")) {
            PreparedRequest.Mail mail = (PreparedRequest.Mail) actual.body();
            JsonNode g = golden.get("mail");
            assertThat(mail.host()).isEqualTo(g.get("host").asString());
            assertThat(mail.port()).isEqualTo(g.get("port").asInt());
            assertThat(mail.tls()).isEqualTo(g.get("tls").asString());
            assertThat(mail.from()).isEqualTo(g.get("from").asString());
            assertThat((JsonNode) JSON.valueToTree(mail.to())).isEqualTo(g.get("to"));
            assertThat(mail.subject()).isEqualTo(g.get("subject").asString());
            assertThat(mail.text()).isEqualTo(g.get("text").asString());
            assertThat(mail.html()).isEqualTo(g.get("html").asString());
            assertThat((JsonNode) JSON.valueToTree(mail.headers())).isEqualTo(g.get("headers"));
        }
    }

    /** The body of {@code request} as JSON. */
    public static JsonNode json(PreparedRequest request) {
        return JSON.readTree(request.bodyText());
    }

    /** Decodes an {@code application/x-www-form-urlencoded} string. */
    public static Map<String, String> decodeForm(String body) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String pair : body.split("&")) {
            int eq = pair.indexOf('=');
            out.put(URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8),
                    URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
        }
        return out;
    }

    /** A body of {@code n} characters of markdown text. */
    public static String longBody(int n) {
        StringBuilder out = new StringBuilder();
        while (out.length() < n) {
            out.append("Lorem ipsum dolor sit amet é ");
        }
        return out.substring(0, n);
    }
}
