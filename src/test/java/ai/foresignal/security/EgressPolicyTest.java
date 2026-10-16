package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.llm.LlmSettings;
import ai.foresignal.net.HostResolver;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.security.visibility.VisibilitySettings;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordRow;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * {@link EgressPolicy} (prompts 026, 027 and 039, EGR-1): the LLM-context rules (remote never, local only
 * with {@code allowPrivateLocal}, a declared-local provider on a public address is remote), embeddings,
 * notification and push payloads, and the later-path hook for every {@link EgressPath}.
 */
class EgressPolicyTest {

    static final EgressPolicy.LlmDestination ANTHROPIC = new EgressPolicy.LlmDestination("claude", "anthropic", false);
    static final EgressPolicy.LlmDestination OPENAI = new EgressPolicy.LlmDestination("router", "openai-compatible", false);
    static final EgressPolicy.LlmDestination OLLAMA = new EgressPolicy.LlmDestination("ollama", "ollama", true);
    static final EgressPolicy.LlmDestination LOCAL_LAN =
            new EgressPolicy.LlmDestination("vllm", "openai-compatible", true, "http://10.0.0.5:8000/v1");
    static final EgressPolicy.LlmDestination LOCAL_ON_PUBLIC_HOST =
            new EgressPolicy.LlmDestination("tunnel", "ollama", true, "https://llm.example.com");
    static final EgressPolicy.LlmDestination LOCAL_UNRESOLVABLE =
            new EgressPolicy.LlmDestination("ghost", "ollama", true, "http://nowhere.invalid:11434");

    final AtomicReference<LlmSettings> llm = new AtomicReference<>(LlmSettings.DEFAULTS);
    final AtomicReference<VisibilitySettings> operator = new AtomicReference<>(VisibilitySettings.defaults());
    EgressPolicy policy;

    @BeforeEach
    void setUp() {
        SourceDescriptor open = SourceDescriptor.builder("quakes").name("Quakes").kind("earthquake").mode(Mode.PUSH)
                .build();
        SourceDescriptor family = SourceDescriptor.builder("family").name("Family").kind("position")
                .mode(Mode.PUSH).privateSource(true).build();
        SourceDescriptor home = SourceDescriptor.builder("home").name("Home").kind("sensor").mode(Mode.PUSH).build();
        Map<String, SourceDescriptor> all = Map.of("quakes", open, "family", family, "home", home);
        SourceRegistry registry = mock(SourceRegistry.class);
        when(registry.descriptors()).thenReturn(List.of(open, family, home));
        when(registry.contains(any())).thenAnswer(i -> all.containsKey(i.<String>getArgument(0)));
        when(registry.find(any())).thenAnswer(i -> Optional.ofNullable(all.get(i.<String>getArgument(0)))
                .map(d -> new SourceRegistry.Registration(d, null, false)));
        when(registry.descriptor(any())).thenAnswer(i -> all.get(i.<String>getArgument(0)));

        AppSettingService settings = mock(AppSettingService.class);
        when(settings.getOrDefault(eq(SettingKeys.LLM), eq(LlmSettings.class), any())).thenAnswer(i -> llm.get());
        when(settings.getOrDefault(eq(SettingKeys.VISIBILITY), eq(VisibilitySettings.class), any()))
                .thenAnswer(i -> operator.get());

        HostResolver resolver = host -> {
            if (host.equals("llm.example.com")) {
                return new InetAddress[] {InetAddress.getByName("93.184.216.34")};
            }
            throw new UnknownHostException(host);
        };
        OutboundUrlPolicy outbound = OutboundUrlPolicy.withoutOverrides(resolver);
        @SuppressWarnings("unchecked")
        ObjectProvider<OutboundUrlPolicy> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(outbound);

        policy = new EgressPolicy(registry, new SourceVisibility(registry, settings, null), settings, provider);
    }

    static RecordRow row(String sourceId, String externalId) {
        return new RecordRow(sourceId, externalId, "k", 1, 1L, null, null, null, null, null, false, null, null, null,
                null, null, null, 0, null, null, null, null, null, null, null, null, null, null);
    }

    static final List<RecordRow> ROWS = List.of(row("quakes", "q1"), row("family", "phone"), row("quakes", "q2"),
            row("gone", "x"));

    void allowPrivateLocal(boolean on) {
        llm.set(new LlmSettings(null, null, true, true, false, 200_000, on));
    }

    @Test
    @DisplayName("private-source records are never sent to a remote provider, even with allowPrivateLocal")
    void remoteDropsPrivate() {
        allowPrivateLocal(true);
        assertThat(policy.filter(ROWS, ANTHROPIC)).extracting(RecordRow::externalId).containsExactly("q1", "q2");
        assertThat(policy.filter(ROWS, OPENAI)).extracting(RecordRow::externalId).containsExactly("q1", "q2");
        assertThat(policy.llmExcludedSources(ANTHROPIC)).containsExactly("family");
    }

    @Test
    @DisplayName("a local provider gets private records only when llm.allowPrivateLocal is true (default false)")
    void localNeedsTheSetting() {
        assertThat(policy.filter(ROWS, OLLAMA)).extracting(RecordRow::externalId).containsExactly("q1", "q2");
        assertThat(policy.llmExcludedSources(OLLAMA)).containsExactly("family");
        allowPrivateLocal(true);
        assertThat(policy.filter(ROWS, OLLAMA)).extracting(RecordRow::externalId)
                .containsExactly("q1", "phone", "q2");
        assertThat(policy.filter(ROWS, LOCAL_LAN)).extracting(RecordRow::externalId)
                .containsExactly("q1", "phone", "q2");
        assertThat(policy.llmExcludedSources(OLLAMA)).isEmpty();
        assertThat(policy.allowsLlmContext("family", LOCAL_LAN)).isTrue();
    }

    @Test
    @DisplayName("a declared-local provider whose base URL is public or does not resolve is remote (fail closed)")
    void declaredLocalOnPublicHostIsRemote() {
        allowPrivateLocal(true);
        assertThat(policy.isLocal(LOCAL_ON_PUBLIC_HOST)).isFalse();
        assertThat(policy.isLocal(LOCAL_UNRESOLVABLE)).isFalse();
        assertThat(policy.isLocal(LOCAL_LAN)).isTrue();
        assertThat(policy.allowsLlmContext("family", LOCAL_ON_PUBLIC_HOST)).isFalse();
        assertThat(policy.llmExcludedSources(LOCAL_ON_PUBLIC_HOST)).containsExactly("family");
    }

    @Test
    @DisplayName("records of unregistered sources (or without a source) are dropped everywhere: fail closed")
    void unknownSourceDropped() {
        allowPrivateLocal(true);
        assertThat(policy.allowsLlmContext("gone", OLLAMA)).isFalse();
        assertThat(policy.allowsLlmContext(null, OLLAMA)).isFalse();
        assertThat(policy.allowsEmbedding("gone")).isFalse();
        assertThat(policy.allowsEmbedding(null)).isFalse();
    }

    @Test
    @DisplayName("the generic filter works on any item type and keeps order")
    void generic() {
        allowPrivateLocal(true);
        List<Map<String, String>> items = List.of(Map.of("source", "family"), Map.of("source", "quakes"));

        assertThat(policy.filter(items, m -> m.get("source"), ANTHROPIC)).containsExactly(Map.of("source", "quakes"));
        assertThat(policy.filter(items, m -> m.get("source"), OLLAMA)).hasSize(2);
    }

    @Test
    @DisplayName("an operator-private source is private on every path: no embedding, no remote prompt")
    void operatorTightening() {
        assertThat(policy.allowsEmbedding("home")).isTrue();
        operator.set(VisibilitySettings.defaults().with("home", true));
        SourceVisibility visibility = visibilityOf(policy);
        visibility.refresh();
        assertThat(policy.isPrivate("home")).isTrue();
        assertThat(policy.allowsEmbedding("home")).isFalse();
        assertThat(policy.allowsLlmContext("home", ANTHROPIC)).isFalse();
        assertThat(policy.llmExcludedSources(ANTHROPIC)).containsExactlyInAnyOrder("family", "home");
    }

    @Test
    @DisplayName("embeddings: never for a private source")
    void embeddings() {
        assertThat(policy.allowsEmbedding("quakes")).isTrue();
        assertThat(policy.allowsEmbedding("family")).isFalse();
    }

    @Test
    @DisplayName("notifications carry a private payload only with the channel's privatePayload; push never")
    void payloads() {
        assertThat(policy.allowsNotificationPayload("quakes", false)).isTrue();
        assertThat(policy.allowsNotificationPayload("family", false)).isFalse();
        assertThat(policy.allowsNotificationPayload("family", true)).isTrue();
        assertThat(policy.allowsNotificationPayload(null, false)).isTrue();
        assertThat(policy.allowsPushPayload("quakes")).isTrue();
        assertThat(policy.allowsPushPayload("family")).isFalse();
    }

    @Test
    @DisplayName("allows(path): public sources always; private only on API/TILE/SSE/MCP for viewers that see"
            + " them, and ARCHIVE (109)")
    void laterPaths() {
        Viewer anonymous = Viewer.anonymous();
        Viewer read = new Viewer(Scope.READ, false, TokenRestrictions.NONE);
        Viewer readGrant = new Viewer(Scope.READ, true, TokenRestrictions.NONE);
        Viewer agent = new Viewer(Scope.AGENT, false, TokenRestrictions.NONE);
        Viewer admin = Viewer.admin();
        for (EgressPath path : EgressPath.values()) {
            assertThat(policy.allows(path, "quakes", anonymous)).as(path.name()).isTrue();
            assertThat(policy.allows(path, "gone", admin)).as(path.name()).isFalse();
            assertThat(policy.allows(path, "family", anonymous)).as(path.name()).isFalse();
            assertThat(policy.allows(path, "family", read)).as(path.name()).isFalse();
            assertThat(policy.allows(path, "family", agent)).as(path.name()).isFalse();
            boolean viewerPath = EgressPolicy.PRIVATE_VIEWER_PATHS.contains(path) || path == EgressPath.ARCHIVE;
            assertThat(policy.allows(path, "family", readGrant)).as(path.name()).isEqualTo(viewerPath);
            assertThat(policy.allows(path, "family", admin)).as(path.name()).isEqualTo(viewerPath);
        }
        assertThat(policy.allows(EgressPath.ARCHIVE, "family", null)).as("operator-owned archive").isTrue();
    }

    @Test
    @DisplayName("private: the explicit 111 export and 110 sink exceptions, and nothing else")
    void privateExceptions() {
        Viewer admin = Viewer.admin();
        Viewer readGrant = new Viewer(Scope.READ, true, TokenRestrictions.NONE);
        EgressPolicy.EgressContext adminExport = EgressPolicy.EgressContext.export(admin, true, false);
        assertThat(policy.decide(EgressPath.EXPORT, "family", null, null, adminExport).allowed()).isTrue();
        assertThat(policy.decide(EgressPath.EXPORT, "family", null, null,
                EgressPolicy.EgressContext.export(admin, false, false)).reason())
                .isEqualTo(EgressPolicy.REASON_PRIVATE);
        assertThat(policy.decide(EgressPath.EXPORT, "family", null, null,
                EgressPolicy.EgressContext.export(readGrant, true, false)).allowed()).as("admin only").isFalse();
        for (EgressPath sink : List.of(EgressPath.SINK, EgressPath.SINK_SERVE_LAN, EgressPath.SINK_PUSH)) {
            assertThat(policy.decide(sink, "family", null, null, EgressPolicy.EgressContext.sink(true, true))
                    .allowed()).as(sink.name()).isTrue();
            assertThat(policy.decide(sink, "family", null, null, EgressPolicy.EgressContext.sink(true, false))
                    .allowed()).as(sink + " remote target").isFalse();
            assertThat(policy.decide(sink, "family", null, null, EgressPolicy.EgressContext.sink(false, true))
                    .allowed()).as(sink + " not opted in").isFalse();
        }
        EgressPolicy.EgressContext everything = new EgressPolicy.EgressContext(admin, true, true, true);
        for (EgressPath path : List.of(EgressPath.FEED, EgressPath.SHARE, EgressPath.PUBLIC_SHARE,
                EgressPath.FEDERATION, EgressPath.NOTIFICATION, EgressPath.LLM_LOCAL, EgressPath.LLM_REMOTE)) {
            assertThat(policy.decide(path, "family", null, null, everything).allowed()).as(path.name()).isFalse();
        }
        assertThat(policy.decide(EgressPath.FEED, "gone", null, null, everything).reason())
                .isEqualTo(EgressPolicy.REASON_UNREGISTERED);
    }

    @Test
    @DisplayName("hidden sources: everything private for anonymous, read and agent; nothing for admin or the grant")
    void hidden() {
        assertThat(policy.hiddenSources(Viewer.anonymous())).containsExactly("family");
        assertThat(policy.hiddenSources(new Viewer(Scope.AGENT, false, null))).containsExactly("family");
        assertThat(policy.hiddenSources(new Viewer(Scope.READ, true, null))).isEmpty();
        assertThat(policy.hiddenSources(Viewer.admin())).isEmpty();
        assertThat(policy.privateFilter(Viewer.anonymous()).allows("family")).isFalse();
        assertThat(policy.visible("quakes", Viewer.anonymous())).isTrue();
        assertThat(policy.hiddenSources(new Viewer(Scope.READ, true,
                new TokenRestrictions(List.of("quakes"), null, null, null)))).containsExactly("family");
    }

    private static SourceVisibility visibilityOf(EgressPolicy policy) {
        try {
            var field = EgressPolicy.class.getDeclaredField("visibility");
            field.setAccessible(true);
            return (SourceVisibility) field.get(policy);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Prompt 069: the effective egress of an {@code egressOverridable} source, MLAT never leaving on a
     * re-serving path even from an {@code OPEN} source, and the {@code security.exposeMlat} filter of anonymous
     * reads (EGR-2).
     */
    @Nested
    @DisplayName("local receivers and MLAT (069)")
    class LocalReceiversAndMlat {

        final AtomicReference<ai.foresignal.source.spi.Egress> override = new AtomicReference<>();
        final AtomicReference<ai.foresignal.settings.SecuritySettings> security =
                new AtomicReference<>(ai.foresignal.settings.SecuritySettings.defaults());
        final java.util.concurrent.atomic.AtomicInteger lookups = new java.util.concurrent.atomic.AtomicInteger();
        EgressPolicy local;

        @BeforeEach
        void setUpLocal() {
            SourceDescriptor receiver = SourceDescriptor.builder("local-adsb-1").name("Receiver").kind("aircraft")
                    .mode(Mode.PUSH).egress(ai.foresignal.source.spi.Egress.LOCAL_ONLY).egressOverridable(true)
                    .fieldSpec("posSource", ai.foresignal.source.spi.FieldSpec.of(
                            ai.foresignal.source.spi.FieldType.STRING, null))
                    .build();
            SourceDescriptor licensed = SourceDescriptor.builder("opensky-states").name("OpenSky").kind("aircraft")
                    .mode(Mode.PUSH).egress(ai.foresignal.source.spi.Egress.LOCAL_ONLY).build();
            SourceDescriptor open = SourceDescriptor.builder("quakes").name("Quakes").kind("earthquake").mode(Mode.PUSH)
                    .build();
            Map<String, SourceDescriptor> all = Map.of("local-adsb-1", receiver, "opensky-states", licensed,
                    "quakes", open);
            SourceRegistry registry = mock(SourceRegistry.class);
            when(registry.descriptors()).thenReturn(List.copyOf(all.values()));
            when(registry.contains(any())).thenAnswer(i -> all.containsKey(i.<String>getArgument(0)));
            when(registry.find(any())).thenAnswer(i -> Optional.ofNullable(all.get(i.<String>getArgument(0)))
                    .map(d -> new SourceRegistry.Registration(d, null, false)));
            when(registry.descriptor(any())).thenAnswer(i -> all.get(i.<String>getArgument(0)));

            AppSettingService settings = mock(AppSettingService.class);
            when(settings.getOrDefault(eq(SettingKeys.VISIBILITY), eq(VisibilitySettings.class), any()))
                    .thenAnswer(i -> VisibilitySettings.defaults());
            when(settings.getOrDefault(eq(SettingKeys.SECURITY), eq(ai.foresignal.settings.SecuritySettings.class),
                    any())).thenAnswer(i -> security.get());

            ai.foresignal.source.SourceService service = mock(ai.foresignal.source.SourceService.class);
            when(service.effectiveEgress(any())).thenAnswer(i -> {
                lookups.incrementAndGet();
                ai.foresignal.source.spi.Egress value = override.get();
                return value == null ? all.get(i.<String>getArgument(0)).egress() : value;
            });
            @SuppressWarnings("unchecked")
            ObjectProvider<ai.foresignal.source.SourceService> services = mock(ObjectProvider.class);
            when(services.getIfAvailable()).thenReturn(service);
            @SuppressWarnings("unchecked")
            ObjectProvider<OutboundUrlPolicy> outbound = mock(ObjectProvider.class);
            local = new EgressPolicy(registry, new SourceVisibility(registry, settings, null), settings, outbound,
                    services);
        }

        RecordRow aircraft(String externalId, String payload, String titlePayload) {
            return new RecordRow("local-adsb-1", externalId, "aircraft", 1, 1L, null, null, null, null, null, false,
                    null, null, payload, titlePayload, null, null, 0, null, null, null, null, null, null, null, null,
                    null, null);
        }

        @Test
        @DisplayName("LOCAL_ONLY denies every re-serving path and allows an export to the instance's own client")
        void localOnlyDeniesRemotePaths() {
            for (EgressPath path : List.of(EgressPath.SINK, EgressPath.FEDERATION, EgressPath.SHARE, EgressPath.FEED)) {
                assertThat(local.allows("local-adsb-1", path)).as(path.name()).isFalse();
            }
            assertThat(local.allows("local-adsb-1", EgressPath.EXPORT)).isTrue();
        }

        @Test
        @DisplayName("an OPEN override applies only to an overridable descriptor, and is cached until settings change")
        void openOverride() {
            override.set(ai.foresignal.source.spi.Egress.OPEN);
            assertThat(local.isLocalOnly("local-adsb-1")).isFalse();
            assertThat(local.allows("local-adsb-1", EgressPath.SINK)).isTrue();
            assertThat(local.isLocalOnly("opensky-states")).as("not overridable: the descriptor decides").isTrue();
            int before = lookups.get();
            local.isLocalOnly("local-adsb-1");
            assertThat(lookups.get()).as("cached").isEqualTo(before);

            override.set(null);
            assertThat(local.isLocalOnly("local-adsb-1")).as("still cached").isFalse();
            local.onSourceSettingsChanged(new ai.foresignal.source.SourceSettingsChangedEvent("local-adsb-1", null, null));
            assertThat(local.isLocalOnly("local-adsb-1")).isTrue();
        }

        @Test
        @DisplayName("MLAT is denied on every non-local path even when the source is OPEN")
        void mlatNeverLeaves() {
            override.set(ai.foresignal.source.spi.Egress.OPEN);
            Map<String, Object> mlat = Map.of("posSource", "mlat");
            Map<String, Object> adsb = Map.of("posSource", "adsb");
            for (EgressPath path : List.of(EgressPath.SINK, EgressPath.FEDERATION, EgressPath.SHARE, EgressPath.FEED)) {
                assertThat(local.allows(path, "local-adsb-1", mlat, null)).as(path.name()).isFalse();
                assertThat(local.allows(path, "local-adsb-1", adsb, null)).as(path.name()).isTrue();
            }
            assertThat(local.allows(EgressPath.EXPORT, "local-adsb-1", mlat, null)).isTrue();
            assertThat(local.allows(EgressPath.FEED, "quakes", mlat, null)).as("any source").isFalse();

            assertThat(local.allows(aircraft("m", "{\"posSource\":\"mlat\"}", null), EgressPath.SINK)).isFalse();
            assertThat(local.allows(aircraft("a", "{\"posSource\":\"adsb\"}", null), EgressPath.SINK)).isTrue();
            assertThat(local.allows(aircraft("n", "{\"hex\":\"abc\"}", null), EgressPath.SINK))
                    .as("a whole payload without posSource is not MLAT").isTrue();
            assertThat(local.allows(aircraft("x", null, null), EgressPath.SINK)).as("unknown: fail closed").isFalse();
        }

        @Test
        @DisplayName("exposeMlat false strips MLAT rows from anonymous reads; true, or a token, keeps them")
        void exposeMlatFilter() {
            List<RecordRow> rows = List.of(aircraft("a", "{\"posSource\":\"adsb\"}", null),
                    aircraft("m", "{\"posSource\":\"mlat\"}", null),
                    aircraft("p", "{\"flight\":\"X\"}", "{\"flight\":\"X\",\"posSource\":\"mlat\"}"),
                    aircraft("q", "{\"flight\":\"Y\"}", null));
            assertThat(local.withoutMlatForAnonymous(rows, true, true)).extracting(RecordRow::externalId)
                    .containsExactly("a", "q");
            assertThat(local.withoutMlatForAnonymous(rows, true, false)).extracting(RecordRow::externalId)
                    .as("projected without posSource: fail closed").containsExactly("a");
            assertThat(local.withoutMlatForAnonymous(rows, false, true)).hasSize(4);

            security.set(new ai.foresignal.settings.SecuritySettings(true, false, null, null, true));
            assertThat(local.exposeMlat()).isTrue();
            assertThat(local.withoutMlatForAnonymous(rows, true, true)).hasSize(4);
        }
    }

    /**
     * Prompt 109 (post-review): the license table applied inside {@link EgressPolicy#decide} on every path, with
     * the descriptor {@code egress}, the operator's {@code OPEN} override, the MLAT downgrade that keeps
     * attribution, per-record terms, {@code personalUse} and {@code derivedFrom}.
     */
    @Nested
    @DisplayName("licence (109) inside the one decision")
    class Licence {

        final AtomicReference<ai.foresignal.source.spi.Egress> override = new AtomicReference<>();
        EgressPolicy licensed;

        @BeforeEach
        void setUpLicensed() {
            SourceDescriptor opensky = SourceDescriptor.builder("opensky-states").name("OpenSky").kind("aircraft")
                    .mode(Mode.PUSH).egress(ai.foresignal.source.spi.Egress.LOCAL_ONLY)
                    .licence("OpenSky Network terms (non-commercial research)").build();
            SourceDescriptor eia = SourceDescriptor.builder("eia-petroleum").name("EIA").kind("series-point")
                    .mode(Mode.PUSH).egress(ai.foresignal.source.spi.Egress.LOCAL_ONLY).licence("US-PD").build();
            SourceDescriptor receiver = SourceDescriptor.builder("local-adsb-1").name("Receiver").kind("aircraft")
                    .mode(Mode.PUSH).egress(ai.foresignal.source.spi.Egress.LOCAL_ONLY).egressOverridable(true)
                    .licence("Operator's own data (no upstream license)").build();
            SourceDescriptor adsblol = SourceDescriptor.builder("adsblol-area").name("adsb.lol").kind("aircraft")
                    .mode(Mode.PUSH).licence("ODbL-1.0").build();
            SourceDescriptor quakes = SourceDescriptor.builder("quakes").name("Quakes").kind("earthquake")
                    .mode(Mode.PUSH).licence("CC BY 4.0").build();
            SourceDescriptor derived = SourceDescriptor.builder("derived-mix").name("Mix").kind("finding")
                    .mode(Mode.DERIVED).licence("derived (see inputs)").derivedFrom("opensky-states", "quakes")
                    .build();
            Map<String, SourceDescriptor> all = Map.of("opensky-states", opensky, "eia-petroleum", eia,
                    "local-adsb-1", receiver, "adsblol-area", adsblol, "quakes", quakes, "derived-mix", derived);
            SourceRegistry registry = mock(SourceRegistry.class);
            when(registry.descriptors()).thenReturn(List.copyOf(all.values()));
            when(registry.contains(any())).thenAnswer(i -> all.containsKey(i.<String>getArgument(0)));
            when(registry.find(any())).thenAnswer(i -> Optional.ofNullable(all.get(i.<String>getArgument(0)))
                    .map(d -> new SourceRegistry.Registration(d, null, false)));
            when(registry.descriptor(any())).thenAnswer(i -> all.get(i.<String>getArgument(0)));
            AppSettingService settings = mock(AppSettingService.class);
            when(settings.getOrDefault(eq(SettingKeys.VISIBILITY), eq(VisibilitySettings.class), any()))
                    .thenAnswer(i -> VisibilitySettings.defaults());
            ai.foresignal.source.SourceService service = mock(ai.foresignal.source.SourceService.class);
            when(service.effectiveEgress(any())).thenAnswer(i -> {
                ai.foresignal.source.spi.Egress value = override.get();
                return value == null ? all.get(i.<String>getArgument(0)).egress() : value;
            });
            @SuppressWarnings("unchecked")
            ObjectProvider<ai.foresignal.source.SourceService> services = mock(ObjectProvider.class);
            when(services.getIfAvailable()).thenReturn(service);
            @SuppressWarnings("unchecked")
            ObjectProvider<OutboundUrlPolicy> outbound = mock(ObjectProvider.class);
            licensed = new EgressPolicy(registry, new SourceVisibility(registry, settings, null), settings, outbound,
                    services);
            ai.foresignal.provenance.LicenceCatalog catalog = new ai.foresignal.provenance.LicenceCatalog();
            licensed.setLicenceGuard(new ai.foresignal.provenance.LicenceEgressGuard(),
                    new ai.foresignal.provenance.SourceLicences(registry, catalog));
        }

        EgressPolicy.Decision decide(EgressPath path, String source, Map<String, ?> payload,
                EgressPolicy.EgressContext ctx) {
            return licensed.decide(path, source, null, payload, ctx);
        }

        @Test
        @DisplayName("EXPORT is licence-guarded: FORBIDDEN needs personalUse; ARCHIVE allows every licence")
        void exportAndArchive() {
            EgressPolicy.Decision plain = decide(EgressPath.EXPORT, "opensky-states", null,
                    EgressPolicy.EgressContext.export(Viewer.admin(), false, false));
            assertThat(plain.allowed()).isFalse();
            assertThat(plain.reason()).isEqualTo(EgressPolicy.REASON_LICENCE_FORBIDDEN);
            assertThat(licensed.allows("opensky-states", EgressPath.EXPORT)).isFalse();
            assertThat(decide(EgressPath.EXPORT, "opensky-states", null,
                    EgressPolicy.EgressContext.export(Viewer.admin(), false, true)).allowed()).isTrue();
            assertThat(decide(EgressPath.ARCHIVE, "opensky-states", null, EgressPolicy.EgressContext.NONE).allowed())
                    .isTrue();
            assertThat(licensed.allows(EgressPath.EXPORT, "quakes", Viewer.anonymous())).isTrue();
        }

        @Test
        @DisplayName("descriptor LOCAL_ONLY wins over an open licence: no push, LAN serve only on a local bind")
        void descriptorEgressOverOpenLicence() {
            EgressPolicy.Decision push = decide(EgressPath.SINK_PUSH, "eia-petroleum", null,
                    EgressPolicy.EgressContext.sink(false, false));
            assertThat(push.allowed()).isFalse();
            assertThat(push.reason()).isEqualTo(EgressPolicy.REASON_LOCAL_ONLY);
            assertThat(decide(EgressPath.SINK_PUSH, "eia-petroleum", null, EgressPolicy.EgressContext.sink(false, true))
                    .allowed()).as("a private push target is still a push").isFalse();
            assertThat(decide(EgressPath.SINK_SERVE_LAN, "eia-petroleum", null,
                    EgressPolicy.EgressContext.sink(false, true)).allowed()).isTrue();
            assertThat(decide(EgressPath.SINK_SERVE_LAN, "eia-petroleum", null,
                    EgressPolicy.EgressContext.sink(false, false)).allowed()).isFalse();
            assertThat(decide(EgressPath.SINK_SERVE_LAN, "opensky-states", null,
                    EgressPolicy.EgressContext.sink(false, true)).reason())
                    .isEqualTo(EgressPolicy.REASON_LICENCE_FORBIDDEN);
        }

        @Test
        @DisplayName("the operator's OPEN override lifts a LOCAL_ONLY licence default; MLAT stays local")
        void operatorOverride() {
            assertThat(decide(EgressPath.SINK_PUSH, "local-adsb-1", null, EgressPolicy.EgressContext.sink(false, false))
                    .allowed()).isFalse();
            override.set(ai.foresignal.source.spi.Egress.OPEN);
            assertThat(decide(EgressPath.SINK_PUSH, "local-adsb-1", Map.of("posSource", "adsb"),
                    EgressPolicy.EgressContext.sink(false, false)).allowed()).isTrue();
            assertThat(decide(EgressPath.SINK_PUSH, "local-adsb-1", Map.of("posSource", "mlat"),
                    EgressPolicy.EgressContext.sink(false, false)).reason()).isEqualTo(EgressPolicy.REASON_MLAT);
        }

        @Test
        @DisplayName("MLAT downgrade keeps the licence id and attribution, and allows a local LAN serve only")
        void mlatKeepsAttribution() {
            EgressPolicy.Decision lan = decide(EgressPath.SINK_SERVE_LAN, "adsblol-area", Map.of("posSource", "mlat"),
                    EgressPolicy.EgressContext.sink(false, true));
            assertThat(lan.allowed()).isTrue();
            assertThat(lan.terms().id()).isEqualTo("ODbL-1.0");
            assertThat(lan.terms().attributionRequired()).isTrue();
            assertThat(lan.terms().shareAlike()).isTrue();
            assertThat(lan.terms().redistribution())
                    .isEqualTo(ai.foresignal.provenance.LicenceTerms.Redistribution.LOCAL_ONLY);
            assertThat(decide(EgressPath.SINK_SERVE_LAN, "adsblol-area", Map.of("posSource", "mlat"),
                    EgressPolicy.EgressContext.sink(false, false)).reason()).isEqualTo(EgressPolicy.REASON_MLAT);
            assertThat(decide(EgressPath.EXPORT, "adsblol-area", Map.of("posSource", "mlat"),
                    EgressPolicy.EgressContext.export(Viewer.admin(), false, false)).reason())
                    .isEqualTo(EgressPolicy.REASON_LICENCE_LOCAL_ONLY);
            assertThat(decide(EgressPath.SINK_PUSH, "adsblol-area", Map.of("posSource", "adsb"),
                    EgressPolicy.EgressContext.sink(false, false)).allowed()).isTrue();
        }

        @Test
        @DisplayName("per-record terms win over the source's; derivedFrom inherits the strictest input")
        void recordTermsAndDerived() {
            ai.foresignal.provenance.LicenceTerms forbidden = new ai.foresignal.provenance.LicenceCatalog()
                    .terms("acled-terms");
            EgressPolicy.Decision decision = licensed.decide(EgressPath.SINK_PUSH, "quakes", forbidden, null,
                    EgressPolicy.EgressContext.sink(false, false));
            assertThat(decision.reason()).isEqualTo(EgressPolicy.REASON_LICENCE_FORBIDDEN);
            assertThat(licensed.sourceTerms("derived-mix").id()).isEqualTo("opensky-terms");
            assertThat(decide(EgressPath.SINK_PUSH, "derived-mix", null, EgressPolicy.EgressContext.sink(false, false))
                    .allowed()).isFalse();
        }
    }
}
