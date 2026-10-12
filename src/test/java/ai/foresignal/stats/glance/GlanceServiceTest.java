package ai.foresignal.stats.glance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.briefing.BriefingLinks;
import ai.foresignal.briefing.BriefingRepository;
import ai.foresignal.briefing.BriefingRunRepository;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.ingest.IngestWriter;
import ai.foresignal.ingest.WriterState;
import ai.foresignal.security.Scope;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.ServerSettingsService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.stats.StatsService;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.system.ReadinessCheck;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link GlanceService} and {@link GlanceAssembler} (prompt 038) over stubbed collaborators: the
 * 2,048-byte limit under a flood of alerts and contributor sections, the anonymous view without counts,
 * the opt-in setting, the text form's line and width limits, the status rule and the contributor
 * budgets.
 */
class GlanceServiceTest {

    static final Instant NOW = Instant.parse("2026-10-12T14:05:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    final AlertStore alerts = mock(AlertStore.class);
    final AppSettingService settings = mock(AppSettingService.class);
    final List<GlanceContributor> contributors = new ArrayList<>();

    static AlertView alert(long id, String rule, String severity) {
        return new AlertView(id, UUID.fromString("00000000-0000-0000-0000-0000000000a1"), rule, 70 + id, "quakes",
                "q" + id, "earthquake", NOW, NOW, "message " + id, severity, "firing", 1, null, null, null, null, null,
                null, null, null, null, false, null, List.of(), null);
    }

    static SourceHealthView health(String id, HealthStatus status) {
        SourceHealthView view = mock(SourceHealthView.class);
        when(view.sourceId()).thenReturn(id);
        when(view.status()).thenReturn(status);
        return view;
    }

    @SuppressWarnings("unchecked")
    GlanceService service(boolean exposeCounts) {
        ReadinessCheck readiness = mock(ReadinessCheck.class);
        when(readiness.ready()).thenReturn(true);
        ObjectProvider<IngestWriter> writer = mock(ObjectProvider.class);
        HealthService health = mock(HealthService.class);
        // Built before stubbing: health(...) stubs its own mock, and Mockito refuses stubbing inside thenReturn's
        // argument list (UnfinishedStubbingException, checkpoint 041).
        List<SourceHealthView> views = List.of(health("a", HealthStatus.HEALTHY), health("b", HealthStatus.FAILING),
                health("c", HealthStatus.DEGRADED), health("hidden", HealthStatus.FAILING));
        when(health.all(any())).thenReturn(views);
        when(alerts.countBySeverity(any())).thenReturn(Map.of("critical", 2L, "warning", 1L));
        BriefingRunRepository runs = mock(BriefingRunRepository.class);
        when(runs.findTop10ByStatusInOrderByStartedAtDescIdDesc(any())).thenReturn(List.of());
        ServerSettingsService server = mock(ServerSettingsService.class);
        when(server.get()).thenReturn(ServerSettings.defaults());
        StatsService stats = mock(StatsService.class);
        when(stats.versionsSince(any(), any(), any())).thenReturn(42L);
        ChangeBus bus = mock(ChangeBus.class);
        when(bus.lastPublishedSeq()).thenReturn(99L);
        when(settings.getOrDefault(eq(SettingKeys.GLANCE), eq(GlanceSettings.class), any()))
                .thenReturn(new GlanceSettings(exposeCounts));
        ObjectProvider<GlanceContributor> provider = mock(ObjectProvider.class);
        when(provider.orderedStream()).thenAnswer(i -> contributors.stream());
        return new GlanceService(Clock.fixed(NOW, ZoneOffset.UTC), readiness, writer, health, alerts, runs,
                mock(BriefingRepository.class), mock(BriefingLinks.class), server, stats, bus, settings, provider, JSON);
    }

    static GlanceService.Request request(Scope scope) {
        return new GlanceService.Request(new ReadCaller(scope, false, true), Set.of("hidden"), null);
    }

    static GlanceContributor contributor(String key, int budget, Object value) {
        return new GlanceContributor() {
            @Override
            public String key() {
                return key;
            }

            @Override
            public int budgetBytes() {
                return budget;
            }

            @Override
            public Optional<Object> contribute(Context context) {
                if (value instanceof RuntimeException e) {
                    throw e;
                }
                return Optional.ofNullable(value);
            }
        };
    }

    @Test
    @DisplayName("never more than 2,048 bytes under a flood of alerts and contributor sections")
    void sizeLimit() {
        when(alerts.list(any(), any(), anyInt())).thenReturn(List.of(alert(3, "x".repeat(500), "critical"),
                alert(2, "ü".repeat(500), "warning"), alert(1, "y".repeat(500), "info")));
        for (int i = 0; i < 20; i++) {
            contributors.add(contributor("section" + i, 600, Map.of("text", "z".repeat(500))));
        }

        String body = service(false).json(request(Scope.READ));

        assertThat(body.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(GlanceAssembler.MAX_BYTES);
        JsonNode doc = JSON.readTree(body);
        assertThat(doc.get("status").asString()).isEqualTo("degraded");
        assertThat(doc.get("lastSeq").asLong()).isEqualTo(99);
        assertThat(doc.has("serverTime")).isTrue();
        assertThat(doc.get("alerts").get("open").get("critical").asLong()).isEqualTo(2);
    }

    @Test
    @DisplayName("latest entries are dropped before contributor sections, and titles are cut to 60 characters")
    void dropOrder() {
        GlanceAssembler assembler = new GlanceAssembler(JSON);
        LinkedHashMap<String, Object> sections = new LinkedHashMap<>();
        sections.put("small", Map.of("t", "s"));
        List<GlanceData.Latest> latest = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            latest.add(new GlanceData.Latest(i, "info", "t".repeat(700), NOW, null));
        }
        GlanceData data = new GlanceData(NOW, "ok", null, latest, null, null, null, 1, null, sections);

        JsonNode doc = JSON.readTree(assembler.json(data));

        assertThat(doc.get("alerts").get("latest").size()).isLessThan(3);
        assertThat(doc.get("sections").has("small")).as("sections outlive latest entries").isTrue();
        assertThat(GlanceAssembler.cut("a  b\nc " + "d".repeat(100), 10)).hasSize(10).startsWith("a b c").endsWith("…");
    }

    @Test
    @DisplayName("the anonymous view has status, serverTime, latest alerts and lastSeq, and no counts")
    void anonymous() {
        when(alerts.list(any(), any(), anyInt())).thenReturn(List.of(alert(1, "Big quake", "critical")));
        contributors.add(contributor("weather", 200, Map.of("t", 1)));

        JsonNode doc = JSON.readTree(service(false).json(request(null)));

        assertThat(doc.get("alerts").has("open")).isFalse();
        assertThat(doc.get("alerts").get("latest").get(0).get("shortTitle").asString()).isEqualTo("Big quake");
        assertThat(doc.has("sources")).isFalse();
        assertThat(doc.has("activity")).isFalse();
        assertThat(doc.has("briefing")).isFalse();
        assertThat(doc.has("links")).isFalse();
        assertThat(doc.has("sections")).isFalse();
        assertThat(doc.get("status").asString()).isEqualTo("degraded");
    }

    @Test
    @DisplayName("glance.exposeCountsAnonymously adds the counts for anonymous callers")
    void optIn() {
        when(alerts.list(any(), any(), anyInt())).thenReturn(List.of());

        JsonNode doc = JSON.readTree(service(true).json(request(null)));

        assertThat(doc.get("alerts").get("open").get("warning").asLong()).isEqualTo(1);
        assertThat(doc.get("alerts").get("open").get("info").asLong()).isZero();
        assertThat(doc.get("sources").get("healthy").asLong()).isEqualTo(1);
        assertThat(doc.get("sources").get("failing").asLong()).as("the hidden source is not counted").isEqualTo(1);
        assertThat(doc.get("activity").get("versionsLastHour").asLong()).isEqualTo(42);
        assertThat(doc.has("links")).isFalse();
    }

    @Test
    @DisplayName("a token caller gets links; contributor sections over budget or failing are skipped")
    void tokenCallerAndContributors() {
        when(alerts.list(any(), any(), anyInt())).thenReturn(List.of(alert(7, "Rule", "warning")));
        contributors.add(contributor("fits", 100, Map.of("t", 1)));
        contributors.add(contributor("tooBig", 10, Map.of("t", "x".repeat(50))));
        contributors.add(contributor("broken", 100, new IllegalStateException("boom")));
        contributors.add(contributor("empty", 100, null));

        JsonNode doc = JSON.readTree(service(false).json(request(Scope.READ)));

        assertThat(doc.get("links").get("latestAlert").asString()).isEqualTo("foresignal://alerts/7");
        assertThat(doc.get("sections").propertyNames()).containsExactly("fits");
        assertThat(doc.get("activity").get("versionsLastHour").asLong()).isEqualTo(42);
    }

    @Test
    @DisplayName("the text form has at most 8 lines of at most 40 ASCII characters, ending with the seq")
    void textLimits() {
        List<GlanceData.Latest> latest = List.of(
                new GlanceData.Latest(1, "critical", "Séisme très fort près de Québec — magnitude 7,2 ☢", NOW, null),
                new GlanceData.Latest(2, "warning", "w".repeat(80), NOW, null),
                new GlanceData.Latest(3, "info", "Line\nbreak", NOW, null));
        GlanceData data = new GlanceData(NOW, "degraded", Map.of("critical", 1L), latest,
                new GlanceData.Sources(10, 2, 1), new GlanceData.Briefing("Morning brief with a very long title indeed",
                        NOW, "http://x"), 123456L, 987654321L, null, null);

        String text = GlanceAssembler.text(data);
        String[] lines = text.split("\n");

        assertThat(lines.length).isLessThanOrEqualTo(GlanceAssembler.MAX_LINES);
        assertThat(lines).allSatisfy(line -> {
            assertThat(line.length()).isLessThanOrEqualTo(GlanceAssembler.MAX_WIDTH);
            assertThat(line.chars().allMatch(c -> c >= 0x20 && c < 0x7f)).as("printable ASCII: %s", line).isTrue();
        });
        assertThat(lines[0]).startsWith("FORESIGNAL DEGRADED").endsWith("14:05Z");
        assertThat(lines[2]).startsWith("!CRIT Seisme tres fort pres de Quebec");
        assertThat(lines[lines.length - 1]).isEqualTo("SEQ 987654321");

        String anonymous = GlanceAssembler.text(new GlanceData(NOW, "ok", null, List.of(), null, null, null, 5, null,
                null));
        assertThat(anonymous.split("\n")).containsExactly(GlanceAssembler.pad("FORESIGNAL OK", "14:05Z"), "SEQ 5");
    }

    @Test
    @DisplayName("status: failing when not ready or the writer cannot write, degraded on a clock hold or a failing source")
    void status() {
        assertThat(GlanceService.status(WriterState.RUNNING, true, false)).isEqualTo("ok");
        assertThat(GlanceService.status(null, true, false)).isEqualTo("ok");
        assertThat(GlanceService.status(WriterState.RUNNING, true, true)).isEqualTo("degraded");
        assertThat(GlanceService.status(WriterState.CLOCK_BEHIND, true, false)).isEqualTo("degraded");
        assertThat(GlanceService.status(WriterState.STOPPED, true, false)).isEqualTo("failing");
        assertThat(GlanceService.status(WriterState.LOCK_LOST, true, false)).isEqualTo("failing");
        assertThat(GlanceService.status(WriterState.RUNNING, false, false)).isEqualTo("failing");
    }
}
