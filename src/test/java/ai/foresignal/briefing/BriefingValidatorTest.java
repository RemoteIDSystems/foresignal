package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.TriggerRuleRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link BriefingValidator} (prompt 029): the 5-field cron conversion and the 15-minute frequency
 * floor, timezones, lookback bounds, scope ids, channel warnings and the members of later prompts.
 */
class BriefingValidatorTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final UUID RULE = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    static final UUID CHANNEL = UUID.fromString("00000000-0000-0000-0000-0000000000c1");

    SourceRegistry registry;
    TriggerRuleRepository rules;
    NotificationChannelRepository channels;
    BriefingValidator validator;

    @BeforeEach
    void setUp() {
        registry = mock(SourceRegistry.class);
        rules = mock(TriggerRuleRepository.class);
        channels = mock(NotificationChannelRepository.class);
        when(registry.contains("quakes")).thenReturn(true);
        when(registry.contains("secret")).thenReturn(true);
        when(registry.kinds()).thenReturn(Set.of("earthquake"));
        when(rules.existsById(any(UUID.class))).thenAnswer(i -> RULE.equals(i.getArgument(0)));
        when(channels.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(channels.findByEnabledTrue()).thenReturn(List.of());
        validator = new BriefingValidator(registry, rules, channels);
    }

    static List<String> fields(ValidationException e) {
        return e.getErrors().stream().map(FieldError::field).toList();
    }

    @Test
    @DisplayName("a 5-field cron becomes a Spring expression with 0 seconds")
    void cronConversion() {
        var cron = BriefingValidator.cron("0 7 * * *");
        assertThat(cron.toString()).isEqualTo("0 0 7 * * *");
        assertThat(BriefingValidator.cron(" 30  6 * * 1-5 ").toString()).isEqualTo("0 30 6 * * 1-5");
        assertThat(BriefingValidator.normalizeSchedule(" 30  6 * * 1-5 ")).isEqualTo("30 6 * * 1-5");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0 7 * *", "0 0 7 * * *", "@daily", "61 7 * * *", "x y z w v", ""})
    @DisplayName("anything but a valid 5-field cron is refused")
    void cronRefused(String schedule) {
        assertThatThrownBy(() -> BriefingValidator.cron(schedule)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("the frequency floor is 15 minutes: */15 passes, */10 and two runs 5 minutes apart do not")
    void frequencyFloor() {
        BriefingValidator.checkFrequency(BriefingValidator.cron("*/15 * * * *"));
        BriefingValidator.checkFrequency(BriefingValidator.cron("0 7 * * *"));
        BriefingValidator.checkFrequency(BriefingValidator.cron("0 0 1 1 *"));
        assertThat(BriefingValidator.minimumInterval(BriefingValidator.cron("0 7 * * *"))).isEqualTo(Duration.ofDays(1));
        assertThatThrownBy(() -> BriefingValidator.checkFrequency(BriefingValidator.cron("*/10 * * * *")))
                .hasMessageContaining("every 10 minutes");
        assertThatThrownBy(() -> BriefingValidator.checkFrequency(BriefingValidator.cron("0,5 7 * * *")))
                .hasMessageContaining("every 5 minutes");
        assertThatThrownBy(() -> BriefingValidator.checkFrequency(BriefingValidator.cron("* * * * *")))
                .hasMessageContaining("every 1 minutes");
    }

    @Test
    @DisplayName("a schedule that never fires is refused")
    void neverFires() {
        assertThatThrownBy(() -> BriefingValidator.checkFrequency(BriefingValidator.cron("0 0 30 2 *")))
                .hasMessageContaining("never fires");
    }

    @Test
    @DisplayName("the next run is computed in the timezone, across a DST change")
    void nextRunInZone() {
        assertThat(BriefingValidator.nextRun("0 7 * * *", "America/Chicago", Instant.parse("2026-10-12T11:00:00Z")))
                .isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));
        // US daylight saving time ends on Sunday 2026-11-01: 07:00 CST is 13:00 UTC.
        assertThat(BriefingValidator.nextRun("0 7 * * *", "America/Chicago", Instant.parse("2026-10-31T12:30:00Z")))
                .isEqualTo(Instant.parse("2026-11-01T13:00:00Z"));
        assertThat(BriefingValidator.nextRun("0 7 * * *", "UTC", Instant.parse("2026-10-12T07:00:00Z")))
                .as("strictly after").isEqualTo(Instant.parse("2026-10-13T07:00:00Z"));
    }

    @Test
    @DisplayName("timezones must be valid zone ids")
    void timezone() {
        assertThat(BriefingValidator.zone("Europe/Berlin").getId()).isEqualTo("Europe/Berlin");
        assertThat(BriefingValidator.zone(null).getId()).isEqualTo("UTC");
        assertThatThrownBy(() -> BriefingValidator.zone("Mars/Olympus")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("lookback is 1h to 7d, ISO or shorthand")
    void lookbackBounds() {
        assertThat(BriefingValidator.lookback("1h")).isEqualTo(Duration.ofHours(1));
        assertThat(BriefingValidator.lookback("7d")).isEqualTo(Duration.ofDays(7));
        assertThat(BriefingValidator.lookback("P1D")).isEqualTo(Duration.ofDays(1));
        assertThatThrownBy(() -> BriefingValidator.lookback("59m")).hasMessageContaining("1h to 7d");
        assertThatThrownBy(() -> BriefingValidator.lookback("8d")).hasMessageContaining("1h to 7d");
        assertThatThrownBy(() -> BriefingValidator.lookback("soon")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a full valid body: defaults applied, ids kept, the unsubscribed channel is a warning")
    void validBody() {
        NotificationChannelEntity alertsOnly = new NotificationChannelEntity(CHANNEL, "ops", "slack");
        alertsOnly.setEvents(List.of("alert"));
        alertsOnly.setEnabled(true);
        when(channels.findById(CHANNEL)).thenReturn(Optional.of(alertsOnly));

        var valid = validator.validate(JSON.readTree("{\"name\":\" Morning \",\"schedule\":\"0  7 * * *\","
                + "\"timezone\":\"America/Chicago\",\"scope\":{\"sources\":[\"quakes\"],\"kinds\":[\"earthquake\"],"
                + "\"bbox\":\"-10,-10,10,10\",\"ruleIds\":[\"" + RULE + "\"],\"placeIds\":[]},\"lookback\":\"24h\","
                + "\"mode\":\"llm\",\"instructions\":\"Lead with critical\",\"channelIds\":[\"" + CHANNEL + "\"],"
                + "\"id\":\"ignored\",\"warnings\":[]}"), Set.of());

        BriefingInput input = valid.input();
        assertThat(input.name()).isEqualTo("Morning");
        assertThat(input.schedule()).isEqualTo("0 7 * * *");
        assertThat(input.enabled()).isTrue();
        assertThat(input.lookbackDuration()).isEqualTo(Duration.ofHours(24));
        assertThat(input.mode()).isEqualTo("llm");
        assertThat(input.maxItemsPerKind()).isEqualTo(5);
        assertThat(input.scope().ruleIds()).containsExactly(RULE);
        assertThat(input.scope().includeHealth()).isTrue();
        assertThat(input.channelIds()).containsExactly(CHANNEL);
        assertThat(valid.warnings()).singleElement().asString().contains("not subscribed to briefing");
    }

    @Test
    @DisplayName("every problem is reported at once")
    void everyProblem() {
        ValidationException e = org.junit.jupiter.api.Assertions.assertThrows(
                ValidationException.class, () -> validator.validate(JSON.readTree("{\"name\":\"\",\"schedule\":\"*/5 * * * *\","
                        + "\"timezone\":\"Nowhere/Land\",\"scope\":{\"sources\":[\"nope\",\"secret\"],\"kinds\":[\"x\"],"
                        + "\"bbox\":\"1,2,3\",\"ruleIds\":[\"not-a-uuid\",\"" + UUID.randomUUID() + "\"],"
                        + "\"placeIds\":[\"home\"],\"bogus\":1},\"lookback\":\"30m\",\"mode\":\"chat\","
                        + "\"channelIds\":[\"x\"],\"maxItemsPerKind\":51,\"audio\":{\"voice\":\"a\"},\"extra\":true}"),
                        Set.of("secret")));
        assertThat(fields(e)).contains("name", "schedule", "timezone", "scope.sources", "scope.kinds", "scope.bbox",
                "scope.ruleIds[0]", "scope.ruleIds[1]", "scope.placeIds", "scope.bogus", "lookback", "mode",
                "channelIds[0]", "maxItemsPerKind", "audio", "extra");
        assertThat(e.getErrors()).filteredOn(err -> err.field().equals("scope.sources"))
                .extracting(FieldError::message).contains("unknown source: nope", "unknown source: secret");
        assertThat(e.getErrors()).filteredOn(err -> err.field().equals("scope.placeIds"))
                .extracting(FieldError::message).containsExactly("unknown place: home");
    }

    @Test
    @DisplayName("without channels, a warning says when no channel takes briefings")
    void noChannelTakesBriefings() {
        var valid = validator.validate(JSON.readTree("{\"name\":\"b\",\"schedule\":\"0 7 * * *\"}"), Set.of());
        assertThat(valid.input().timezone()).isEqualTo("UTC");
        assertThat(valid.input().mode()).isEqualTo("digest");
        assertThat(valid.input().lookback()).isNull();
        assertThat(valid.warnings()).singleElement().asString().contains("no enabled channel");
    }

    @Test
    @DisplayName("the body must be an object")
    void notAnObject() {
        assertThatThrownBy(() -> validator.validate(JSON.readTree("[1]"), Set.of())).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> validator.validate(null, Set.of())).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("the period: lookback back, else since the previous run capped at 7 days, else 24 hours")
    void period() {
        Instant now = Instant.parse("2026-10-12T12:00:00Z");
        BriefingInput withLookback = new BriefingInput("b", true, "0 7 * * *", "UTC", BriefingScope.all(), "6h",
                Duration.ofHours(6), "digest", null, List.of(), 5);
        BriefingInput without = new BriefingInput("b", true, "0 7 * * *", "UTC", BriefingScope.all(), null, null,
                "digest", null, List.of(), 5);
        assertThat(BriefingRunner.periodFrom(withLookback, now.minusSeconds(60), now)).isEqualTo(now.minus(Duration.ofHours(6)));
        assertThat(BriefingRunner.periodFrom(without, now.minus(Duration.ofHours(3)), now)).isEqualTo(now.minus(Duration.ofHours(3)));
        assertThat(BriefingRunner.periodFrom(without, now.minus(Duration.ofDays(30)), now)).isEqualTo(now.minus(Duration.ofDays(7)));
        assertThat(BriefingRunner.periodFrom(without, null, now)).isEqualTo(now.minus(Duration.ofHours(24)));
    }

    @Test
    @DisplayName("scopes round-trip through their stored form")
    void scopeRoundTrip() {
        BriefingScope scope = new BriefingScope(List.of("quakes"), List.of("earthquake"), "1,2,3,4", List.of(RULE), false);
        assertThat(BriefingScope.fromMap(scope.toMap())).isEqualTo(scope);
        assertThat(BriefingScope.fromMap(null)).isEqualTo(BriefingScope.all());
    }

    @Test
    @DisplayName("040: country, admin1 and county are scope members, upper-cased and carried to the region filter")
    void regionScope() {
        var valid = validator.validate(JSON.readTree("{\"name\":\"b\",\"schedule\":\"0 7 * * *\",\"scope\":"
                + "{\"country\":[\"us\",\"US\"],\"admin1\":[\"us-tx\"],\"county\":[\"48453\"]}}"), Set.of());
        BriefingScope scope = valid.input().scope();
        assertThat(scope.country()).containsExactly("US");
        assertThat(scope.admin1()).containsExactly("US-TX");
        assertThat(scope.county()).containsExactly("48453");
        assertThat(scope.regionFilter().canonical()).isEqualTo("c=US;a=US-TX;n=48453");
        assertThat(BriefingScope.fromMap(scope.toMap())).isEqualTo(scope);
        assertThat(BriefingScope.all().regionFilter()).isNull();
        assertThat(new DefaultScopeResolver().resolve(scope).narrow(
                ai.foresignal.security.restrict.ReadRestriction.NONE).regionFilter()).isEqualTo(scope.regionFilter());
    }
}
