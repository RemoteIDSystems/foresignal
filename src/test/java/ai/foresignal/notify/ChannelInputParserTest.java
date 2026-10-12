package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.notify.types.SlackChannel;
import ai.foresignal.notify.types.WebhookChannel;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** {@link ChannelInputParser} (prompt 018). */
class ChannelInputParserTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    final ChannelInputParser parser = new ChannelInputParser(
            new ChannelTypeRegistry(List.of(new WebhookChannel(), new SlackChannel())), JSON,
            ai.foresignal.testsupport.net.TestOutbound.offlinePolicy());

    ChannelInput parse(String json, Map<String, String> stored) {
        return parser.parse(JSON.readTree(json.replace('\'', '"')), stored);
    }

    ValidationException fails(String json) {
        try {
            parse(json, null);
        } catch (ValidationException e) {
            return e;
        }
        throw new AssertionError("expected a validation failure for " + json);
    }

    @Test
    @DisplayName("defaults: events [alert], minSeverity info, enabled, cap 20, edit in place; secrets null when omitted")
    void defaults() {
        ChannelInput input = parse("{'name':' w ','type':'webhook','config':{'url':'https://r.example.invalid'}}", null);
        assertThat(input.name()).isEqualTo("w");
        assertThat(input.events()).containsExactly("alert");
        assertThat(input.minSeverity()).isEqualTo("info");
        assertThat(input.enabled()).isTrue();
        assertThat(input.backlogCap()).isEqualTo(20);
        assertThat(input.editInPlace()).isTrue();
        assertThat(input.secrets()).isNull();
        assertThat(input.ruleIds()).isNull();
    }

    @Test
    @DisplayName("stored secrets satisfy required secret fields on a PUT that omits secrets")
    void storedSecrets() {
        assertThat(fails("{'name':'s','type':'slack'}").getErrors()).extracting(e -> e.field()).contains("secrets.webhookUrl");
        assertThat(parse("{'name':'s','type':'slack'}", Map.of("webhookUrl", "https://hooks.example.invalid/x")).secrets()).isNull();
    }

    @Test
    @DisplayName("field types, enums, unknown secrets and the header.<Name> wildcard")
    void fieldChecks() {
        var e = fails("{'name':'w','type':'webhook','config':{'url':'ftp://x','method':'GET','headers':{'a':1}},"
                + "'secrets':{'nope':'x','header.X-A':'ok'}}");
        assertThat(e.getErrors()).extracting(f -> f.field())
                .containsExactlyInAnyOrder("config.url", "config.method", "config.headers", "secrets.nope");
    }

    @Test
    @DisplayName("top-level members: unknown rejected, later prompts' members accepted, ranges and quiet hours checked")
    void members() {
        assertThat(fails("{'name':'w','type':'webhook','config':{'url':'https://r.example.invalid'},'bogus':1}").getErrors())
                .extracting(f -> f.field()).containsExactly("bogus");
        ChannelInput input = parse("{'name':'w','type':'webhook','config':{'url':'https://r.example.invalid'},"
                + "'acceptExercise':true,'language':'de','deferWhileActiveSeconds':30,"
                + "'quietHours':{'start':'22:00','end':'07:00','days':['MON','SUN'],'bypassSeverity':null}}", null);
        assertThat(input.quietHours().bypassSeverity()).isNull();
        assertThat(input.quietHours().days()).hasSize(2);
        assertThat(fails("{'name':'w','type':'webhook','config':{'url':'https://r.example.invalid'},'waitForEnrichmentSeconds':61,"
                + "'events':['alert','mention'],'ruleIds':['x'],'quietHours':{'start':'22:00','end':'22:00','days':['XYZ']}}")
                .getErrors()).extracting(f -> f.field())
                .contains("waitForEnrichmentSeconds", "events", "ruleIds", "quietHours.end", "quietHours.days");
    }

    @Test
    @DisplayName("later types and unknown types are refused; a non-object body is refused")
    void types() {
        assertThat(fails("{'name':'m','type':'mqtt'}").getErrors().getFirst().message()).contains("060");
        assertThat(fails("{'name':'m','type':'fax'}").getErrors().getFirst().message()).contains("unknown");
        assertThatThrownBy(() -> parser.parse(JSON.readTree("[]"), null)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("033: deferWhileActiveSeconds (0-600) is stored; config.actions and the allow lists are validated")
    void actionsAndDeferral() {
        String base = "{'name':'w','type':'webhook','config':{'url':'https://r.example.invalid'";
        assertThat(parse(base + "},'deferWhileActiveSeconds':120}", null).deferWhileActiveSeconds()).isEqualTo(120);
        assertThat(parse(base + "}}", null).deferWhileActiveSeconds()).isZero();
        assertThat(fails(base + "},'deferWhileActiveSeconds':601}").getErrors().getFirst().field())
                .isEqualTo("deferWhileActiveSeconds");
        assertThat(parse(base + ",'actions':{'enabled':true,'list':['ack','snooze'],'links':false,'reactions':false}}}",
                null).config()).containsKey("actions");
        assertThat(fails(base + ",'actions':{'list':['ack','delete']}}}").getErrors().getFirst().field())
                .isEqualTo("config.actions.list");
        assertThat(fails(base + ",'actions':{'enabled':'yes'}}}").getErrors().getFirst().field())
                .isEqualTo("config.actions.enabled");
        assertThat(fails(base + ",'actions':{'bogus':1}}}").getErrors().getFirst().field())
                .isEqualTo("config.actions.bogus");
        assertThat(fails(base + ",'actions':[]}}").getErrors().getFirst().field()).isEqualTo("config.actions");
        assertThat(fails(base + ",'allowedUserIds':['x']}}").getErrors().getFirst().field())
                .isEqualTo("config.allowedUserIds");
        assertThat(fails(base + ",'allowedSenders':[1]}}").getErrors().getFirst().field())
                .isEqualTo("config.allowedSenders");
    }
}
