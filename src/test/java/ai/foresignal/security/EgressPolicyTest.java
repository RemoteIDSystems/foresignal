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
    @DisplayName("allows(path): public sources always; private only for viewers that see them; FEDERATION never")
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
            boolean expected = path != EgressPath.FEDERATION;
            assertThat(policy.allows(path, "family", readGrant)).as(path.name()).isEqualTo(expected);
            assertThat(policy.allows(path, "family", admin)).as(path.name()).isEqualTo(expected);
        }
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
}
