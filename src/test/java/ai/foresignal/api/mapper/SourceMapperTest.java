package ai.foresignal.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.dto.BudgetDto;
import ai.foresignal.api.dto.IngestPolicyDto;
import ai.foresignal.api.dto.SourceDescriptorDto;
import ai.foresignal.api.dto.SourceSettingsDto;
import ai.foresignal.api.dto.SourceSummary;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.DurationShorthandModule;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.IngestPolicyCodec;
import ai.foresignal.source.SourceHealthSummary;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class SourceMapperTest {

    private static final Instant T0 = Instant.parse("2026-04-01T12:00:00Z");

    private final JsonMapper json = JsonMapper.builder().addModule(new DurationShorthandModule()).build();
    private final IngestPolicyCodec codec = new IngestPolicyCodec(json);
    private AppSettingService appSettings;
    private SourceMapper mapper;

    private static final SourceDescriptor TRACK =
            SourceDescriptor.builder("track-src")
                    .name("Track source")
                    .kinds("vessel", "aircraft")
                    .mode(Mode.STREAMING)
                    .historyTier(HistoryTier.TRACK)
                    .recordTtl(Duration.ofMinutes(6))
                    .licence("ODbL-1.0")
                    .termsUrl("https://example.org/terms")
                    .significantFields("callsign")
                    .volatileFields("seen")
                    .motion(new MotionSpec("gs", MotionSpec.SpeedUnit.KNOTS, "track"))
                    .threshold(FieldThreshold.angular("track", 20))
                    .defaultIngestPolicy(
                            new IngestPolicy.Sampled(Duration.ofSeconds(60), Duration.ofSeconds(240), 1000.0, true, null))
                    .credential(CredentialField.required("apiKey", "The key"))
                    .credential(CredentialField.optional("user", "Optional user"))
                    .param(ParamSpec.optional("window", ParamType.DURATION, "Window", Duration.ofMinutes(5)))
                    .defaultBudget(Budget.of(10, 1000, 50))
                    .product(new ProductSpec("p-1", "Product", "raster"))
                    .titleField("callsign")
                    .entityKey("icao24", "hex")
                    .fieldSpec("alt", FieldSpec.of(FieldType.NUMBER, "[ft_i]"))
                    .kindDisplay("aircraft", new KindDisplay("{callsign}", "plane", "#112233", null))
                    .aggBelowZoom(6)
                    .intervalFields("start", "end")
                    .build();

    private static final SourceDescriptor STANDARD =
            SourceDescriptor.builder("std-src")
                    .kind("event")
                    .interval(Duration.ofSeconds(30), Duration.ofMinutes(5))
                    .defaultBudget(Budget.of(4, 100))
                    .defaultRetention(Duration.ofDays(1825))
                    .kindRetention("event", Duration.ofDays(90))
                    .build();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        appSettings = mock(AppSettingService.class);
        when(appSettings.getOrDefault(eq(SettingKeys.RETENTION), eq(RetentionSettings.class), any(Supplier.class)))
                .thenReturn(new RetentionSettings(Duration.ofDays(7), null, null, Map.of(), Duration.ofDays(4)));
        mapper = new SourceMapper(codec, json, appSettings);
    }

    // ---------- descriptor ----------

    @Nested
    @DisplayName("descriptor")
    class Descriptor {

        @Test
        @DisplayName("carries every v1.2 field and the 008 extras")
        void allFields() {
            SourceDescriptorDto d = mapper.descriptor(TRACK);

            assertThat(d.id()).isEqualTo("track-src");
            assertThat(d.kinds()).containsExactly("aircraft", "vessel");
            assertThat(d.mode()).isEqualTo("streaming");
            assertThat(d.custom()).isFalse();
            assertThat(d.historyTier()).isEqualTo("TRACK");
            assertThat(d.observedAtMode()).isEqualTo("UPSTREAM");
            assertThat(d.ttlAnchor()).isEqualTo("OBSERVED");
            assertThat(d.licence()).isEqualTo("ODbL-1.0");
            assertThat(d.termsUrl()).isEqualTo("https://example.org/terms");
            assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(6));
            assertThat(d.significantFields()).containsExactly("callsign");
            assertThat(d.volatileFields()).containsExactly("seen");
            assertThat(d.motion().speedUnit()).isEqualTo("knots");
            assertThat(d.motion().courseField()).isEqualTo("track");
            assertThat(d.thresholds()).singleElement().satisfies(t -> {
                assertThat(t.field()).isEqualTo("track");
                assertThat(t.angular()).isTrue();
                assertThat(t.delta()).isEqualTo(20.0);
            });
            assertThat(d.credentialFields()).extracting("name").containsExactly("apiKey", "user");
            assertThat(d.params()).singleElement().satisfies(p -> {
                assertThat(p.type()).isEqualTo("duration");
                assertThat(p.defaultValue()).isEqualTo("5m");
            });
            assertThat(d.defaultBudget()).isEqualTo(new BudgetDto(10, 1000));
            assertThat(d.products()).containsExactly("p-1");
            assertThat(d.titleField()).isEqualTo("callsign");
            assertThat(d.entityKeys()).containsEntry("icao24", "hex");
            assertThat(d.fieldSpecs()).containsKey("alt");
            assertThat(d.kindDisplay()).containsKey("aircraft");
            assertThat(d.aggBelowZoom()).isEqualTo(6);
            assertThat(d.startField()).isEqualTo("start");
            assertThat(d.endField()).isEqualTo("end");
            assertThat(d.privateSource()).isFalse();
            assertThat(d.privatePrecisionDecimals()).isEqualTo(4);
            assertThat(d.skewTolerance()).isEqualTo(SourceDescriptor.DEFAULT_SKEW_TOLERANCE);
            assertThat(d.defaultIngestPolicy().mode()).isEqualTo("SAMPLED");
            assertThat(d.defaultIngestPolicy().edgeMinInterval()).as("defaults filled in").isEqualTo("30s");
        }

        @Test
        @DisplayName("serializes with spec names, shorthand durations and private")
        void json() {
            JsonNode tree = json.valueToTree(mapper.descriptor(TRACK));

            assertThat(tree.get("recordTtl").asString()).isEqualTo("6m");
            assertThat(tree.get("skewTolerance").asString()).isEqualTo("2m");
            assertThat(tree.has("private")).isTrue();
            assertThat(tree.has("privateSource")).isFalse();
            assertThat(tree.get("params").get(0).get("default").asString()).isEqualTo("5m");
            assertThat(tree.get("kindDisplay").get("aircraft").get("colour").asString()).isEqualTo("#112233");
            assertThat(tree.get("defaultIngestPolicy").get("keyframe").asString()).isEqualTo("4m");
            assertThat(tree.get("defaultIngestPolicy").get("minInterval").asString()).isEqualTo("1m");
            assertThat(tree.get("defaultBudget").has("mbPerDay")).isFalse();

            JsonNode std = json.valueToTree(mapper.descriptor(STANDARD));
            assertThat(std.get("defaultRetention").asString()).isEqualTo("1825d");
            assertThat(std.get("kindRetention").get("event").asString()).isEqualTo("90d");
            assertThat(std.get("motion").isNull()).isTrue();
            assertThat(std.get("defaultIngestPolicy").get("mode").asString()).isEqualTo("ALL");
            assertThat(std.get("defaultIngestPolicy").get("keyframe").asString()).isEqualTo("none");
            assertThat(std.get("defaultIngestPolicy").has("minInterval")).as("ALL has no minInterval").isFalse();
        }
    }

    // ---------- summary and settings ----------

    private static SourceView view(SourceDescriptor d, IngestPolicy effective) {
        return new SourceView(
                d, true, Duration.ofMinutes(5), Map.of("window", "5m"), Budget.of(4, 100, 9), null, effective, true, T0,
                new SourceHealthSummary(HealthStatus.HEALTHY, "ok", T0.minusSeconds(60), T0.minusSeconds(30), 12));
    }

    @Test
    @DisplayName("summary carries tier, licence, status, lastDataAt and credentialsRequired")
    void summary() {
        SourceSummary s = mapper.summary(view(TRACK, IngestPolicy.all()));

        assertThat(s.historyTier()).isEqualTo("TRACK");
        assertThat(s.licence()).isEqualTo("ODbL-1.0");
        assertThat(s.termsUrl()).isEqualTo("https://example.org/terms");
        assertThat(s.status()).isEqualTo(HealthStatus.HEALTHY);
        assertThat(s.lastDataAt()).isEqualTo(T0.minusSeconds(30));
        assertThat(s.lastSuccessAt()).isEqualTo(T0.minusSeconds(60));
        assertThat(s.budgetUsedToday()).isEqualTo(12);
        assertThat(s.credentialsRequired()).isTrue();
        assertThat(s.credentialsSet()).isTrue();
        assertThat(s.budget()).isEqualTo(new BudgetDto(4, 100));
        assertThat(s.mode()).isEqualTo("streaming");
    }

    @Test
    @DisplayName("credentialsRequired is false when every credential is optional")
    void optionalCredentials() {
        SourceDescriptor optional =
                SourceDescriptor.builder("opt").kind("k").credential(CredentialField.optional("user", "u")).build();
        assertThat(mapper.summary(view(optional, IngestPolicy.all())).credentialsRequired()).isFalse();
    }

    @Test
    @DisplayName("settings: override and effective policy, TRACK retention from retention.trackWindow")
    void settingsTrack() {
        IngestPolicy override = IngestPolicy.latest(Duration.ofMinutes(4));
        SourceSettingsDto s =
                mapper.settings(view(TRACK, override), Optional.of(override), Optional.empty(), Optional.empty());

        assertThat(s.ingestPolicy().mode()).isEqualTo("LATEST");
        assertThat(s.effectiveIngestPolicy().mode()).isEqualTo("LATEST");
        assertThat(s.effectiveRetention()).isEqualTo(Duration.ofDays(4));
        assertThat(s.retention()).isNull();
        assertThat(s.enabled()).isTrue();
        assertThat(s.credentialsSet()).isTrue();
        assertThat(s.updatedAt()).isEqualTo(T0);
        assertThat(s.params()).containsEntry("window", "5m");
    }

    @Test
    @DisplayName("settings: STANDARD retention is the source's own, else the global default")
    void settingsStandard() {
        SourceSettingsDto own =
                mapper.settings(
                        view(STANDARD, IngestPolicy.all()),
                        Optional.empty(),
                        Optional.of(Duration.ofDays(30)),
                        Optional.of(Duration.ofDays(30)));
        assertThat(own.retention()).isEqualTo(Duration.ofDays(30));
        assertThat(own.effectiveRetention()).isEqualTo(Duration.ofDays(30));
        assertThat(own.ingestPolicy()).isNull();

        SourceSettingsDto global =
                mapper.settings(view(STANDARD, IngestPolicy.all()), Optional.empty(), Optional.empty(), Optional.empty());
        assertThat(global.effectiveRetention()).isEqualTo(Duration.ofDays(7));
    }

    @Test
    @DisplayName("unreadable retention settings fall back to the shipped defaults")
    @SuppressWarnings("unchecked")
    void retentionSettingsFailure() {
        when(appSettings.getOrDefault(eq(SettingKeys.RETENTION), eq(RetentionSettings.class), any(Supplier.class)))
                .thenThrow(new IllegalStateException("db down"));
        assertThat(mapper.effectiveRetention(TRACK, Optional.empty())).isEqualTo(RetentionSettings.DEFAULT_TRACK_WINDOW);
    }

    // ---------- policy ----------

    @Nested
    @DisplayName("ingest policy")
    class Policy {

        @Test
        @DisplayName("SAMPLED round-trips through the discriminated JSON shape")
        void sampledRoundTrip() {
            IngestPolicyDto dto = new IngestPolicyDto("SAMPLED", "60s", "240s", 1000.0, true, "30s");

            IngestPolicy policy = mapper.policy(dto, TRACK);

            assertThat(policy).isEqualTo(
                    new IngestPolicy.Sampled(Duration.ofSeconds(60), Duration.ofSeconds(240), 1000.0, true, Duration.ofSeconds(30)));
            IngestPolicyDto back = mapper.policy(policy);
            assertThat(back).isEqualTo(new IngestPolicyDto("SAMPLED", "1m", "4m", 1000.0, true, "30s"));
        }

        @Test
        @DisplayName("keyframe none maps to no keyframe and back")
        void keyframeNone() {
            IngestPolicy policy = mapper.policy(new IngestPolicyDto("LATEST", null, "none", null, null, null), STANDARD);
            assertThat(policy).isEqualTo(new IngestPolicy.Latest(null, null));
            assertThat(mapper.policy(policy).keyframe()).isEqualTo("none");
            assertThat(mapper.policy(IngestPolicy.all()))
                    .isEqualTo(new IngestPolicyDto("ALL", null, "none", null, null, null));
        }

        @Test
        @DisplayName("mode is case-insensitive; ISO-8601 durations are accepted")
        void lenientInput() {
            IngestPolicy policy = mapper.policy(new IngestPolicyDto("all", null, "PT4M", null, null, null), TRACK);
            assertThat(policy).isEqualTo(IngestPolicy.all(Duration.ofMinutes(4)));
        }

        @Test
        @DisplayName("shape problems are INGEST_POLICY_INVALID with field paths")
        void shapeProblems() {
            assertThatThrownBy(() -> mapper.policy(new IngestPolicyDto("NEVER", "x", "soon", null, null, "y"), TRACK))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(e -> {
                        ValidationException v = (ValidationException) e;
                        assertThat(v.getCode()).isEqualTo(ErrorCodes.INGEST_POLICY_INVALID);
                        assertThat(v.getErrors()).extracting(FieldError::field)
                                .containsExactlyInAnyOrder(
                                        "ingestPolicy.mode",
                                        "ingestPolicy.minInterval",
                                        "ingestPolicy.keyframe",
                                        "ingestPolicy.edgeMinInterval");
                    });
        }

        @Test
        @DisplayName("SAMPLED needs minInterval; ALL with keyframe none is refused when the source has a TTL")
        void requiredPieces() {
            assertThatThrownBy(() -> mapper.policy(new IngestPolicyDto("SAMPLED", null, "4m", null, null, null), TRACK))
                    .satisfies(e -> assertThat(((ValidationException) e).getErrors())
                            .extracting(FieldError::field).containsExactly("ingestPolicy.minInterval"));
            assertThatThrownBy(() -> mapper.policy(new IngestPolicyDto("ALL", null, "none", null, null, null), TRACK))
                    .satisfies(e -> assertThat(((ValidationException) e).getErrors())
                            .extracting(FieldError::field).containsExactly("ingestPolicy.keyframe"));
            assertThat(mapper.policy(new IngestPolicyDto("ALL", null, "none", null, null, null), STANDARD))
                    .isEqualTo(IngestPolicy.all());
        }

        @Test
        @DisplayName("null maps to null both ways")
        void nulls() {
            assertThat(mapper.policy((IngestPolicy) null)).isNull();
            assertThat(mapper.policy((IngestPolicyDto) null, TRACK)).isNull();
        }

        @Test
        @DisplayName("the DTO serializes as the discriminated union, omitting absent fields")
        void dtoJson() {
            JsonNode tree = json.valueToTree(new IngestPolicyDto("ALL", null, "4m", null, null, null));
            assertThat(tree.propertyNames()).containsExactlyInAnyOrder("mode", "keyframe");
        }
    }

    // ---------- updates ----------

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("an empty body changes nothing")
        void empty() {
            assertThat(mapper.update(new ai.foresignal.api.dto.SourceSettingsUpdate(), STANDARD).isEmpty()).isTrue();
            assertThat(mapper.update(null, STANDARD).isEmpty()).isTrue();
        }

        @Test
        @DisplayName("parses durations, keeps mbPerDay from the descriptor and drops null params")
        void fields() {
            var body = new ai.foresignal.api.dto.SourceSettingsUpdate();
            body.setEnabled(true);
            body.setInterval("15m");
            body.setRetention("P30D");
            body.setBudget(new BudgetDto(2, 50));
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("window", "10m");
            params.put("unset", null);
            body.setParams(params);

            var update = mapper.update(body, TRACK);

            assertThat(update.enabled()).isTrue();
            assertThat(update.interval()).isEqualTo(Duration.ofMinutes(15));
            assertThat(update.retention()).isEqualTo(Duration.ofDays(30));
            assertThat(update.clearRetention()).isFalse();
            assertThat(update.budget()).isEqualTo(new Budget(2, 50, 50));
            assertThat(update.params()).containsOnlyKeys("window");
            assertThat(update.ingestPolicy()).isNull();
            assertThat(update.clearIngestPolicy()).isFalse();
        }

        @Test
        @DisplayName("explicit nulls clear retention and the policy override; omitted keys do not")
        void explicitNulls() {
            var body = new ai.foresignal.api.dto.SourceSettingsUpdate();
            body.setRetention(null);
            body.setIngestPolicy(null);

            var update = mapper.update(body, STANDARD);

            assertThat(update.clearRetention()).isTrue();
            assertThat(update.clearIngestPolicy()).isTrue();
            assertThat(update.retention()).isNull();
            assertThat(update.ingestPolicy()).isNull();
        }

        @Test
        @DisplayName("a policy in the body becomes the domain policy")
        void policy() {
            var body = new ai.foresignal.api.dto.SourceSettingsUpdate();
            body.setIngestPolicy(new IngestPolicyDto("LATEST", null, "20m", null, null, "15s"));

            assertThat(mapper.update(body, STANDARD).ingestPolicy())
                    .isEqualTo(new IngestPolicy.Latest(Duration.ofMinutes(20), Duration.ofSeconds(15)));
        }

        @Test
        @DisplayName("every malformed value is reported in one VALIDATION_FAILED")
        void malformed() {
            var body = new ai.foresignal.api.dto.SourceSettingsUpdate();
            body.setInterval("soon");
            body.setRetention("forever");
            body.setBudget(new BudgetDto(null, null));

            assertThatThrownBy(() -> mapper.update(body, STANDARD))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(e -> {
                        ValidationException v = (ValidationException) e;
                        assertThat(v.getCode()).isEqualTo(ErrorCodes.VALIDATION_FAILED);
                        assertThat(v.getErrors()).extracting(FieldError::field)
                                .containsExactlyInAnyOrder("interval", "retention", "budget.perMinute", "budget.perDay");
                    });
        }
    }
}
