package ai.foresignal.security.visibility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceRegistryChangedEvent;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/**
 * {@link SourceVisibility} (prompt 039, §1): effective privacy is the descriptor flag or the operator's
 * tightening, the operator cannot make a descriptor-private source public, who may see private sources,
 * and the {@link SourceBecamePrivateEvent} on refresh.
 */
class SourceVisibilityTest {

    final List<SourceDescriptor> descriptors = new CopyOnWriteArrayList<>();
    final AtomicReference<VisibilitySettings> stored = new AtomicReference<>(VisibilitySettings.defaults());
    final List<Object> published = new ArrayList<>();
    SourceVisibility visibility;

    static SourceDescriptor source(String id, boolean isPrivate) {
        return SourceDescriptor.builder(id).name(id).kind("k").mode(Mode.PUSH).privateSource(isPrivate).build();
    }

    @BeforeEach
    void setUp() {
        descriptors.add(source("quakes", false));
        descriptors.add(source("family", true));
        descriptors.add(source("home", false));
        SourceRegistry registry = mock(SourceRegistry.class);
        when(registry.descriptors()).thenAnswer(i -> List.copyOf(descriptors));
        when(registry.find(any())).thenAnswer(i -> descriptors.stream()
                .filter(d -> d.id().equals(i.getArgument(0))).findFirst()
                .map(d -> new SourceRegistry.Registration(d, null, false)));
        when(registry.descriptor(any())).thenAnswer(i -> descriptors.stream()
                .filter(d -> d.id().equals(i.getArgument(0))).findFirst().orElseThrow());
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.getOrDefault(eq(SettingKeys.VISIBILITY), eq(VisibilitySettings.class), any()))
                .thenAnswer(i -> stored.get());
        doAnswer(i -> {
            stored.set(i.getArgument(1));
            return null;
        }).when(settings).put(eq(SettingKeys.VISIBILITY), any());
        ApplicationEventPublisher events = published::add;
        visibility = new SourceVisibility(registry, settings, events);
    }

    static Optional<CurrentToken> token(Scope scope, boolean privateAccess, TokenRestrictions restrictions) {
        return Optional.of(new CurrentToken(new AuthenticatedToken(UUID.randomUUID(), "t", scope, restrictions, null,
                "p", false, privateAccess), false));
    }

    @Test
    @DisplayName("effective privacy is the descriptor flag OR the operator's tightening")
    void effectivePrivacy() {
        assertThat(visibility.privateSourceIds()).containsExactly("family");
        assertThat(visibility.isPrivate("family")).isTrue();
        assertThat(visibility.isPrivate("unknown")).isFalse();
        assertThat(visibility.isPrivate(null)).isFalse();

        visibility.setOperatorPrivate("home", true);
        visibility.onSettingChanged(new AppSettingChangedEvent(SettingKeys.VISIBILITY));
        assertThat(visibility.privateSourceIds()).containsExactly("family", "home");
        assertThat(visibility.operatorPrivate("home")).isTrue();
        assertThat(visibility.descriptorPrivate("home")).isFalse();

        visibility.setOperatorPrivate("home", false);
        visibility.onSettingChanged(new AppSettingChangedEvent(SettingKeys.VISIBILITY));
        assertThat(visibility.privateSourceIds()).containsExactly("family");
        visibility.setOperatorPrivate("home", null);
        assertThat(stored.get().privateSources()).isEmpty();
    }

    @Test
    @DisplayName("the operator cannot make a descriptor-private source public")
    void cannotUnsetDescriptorPrivate() {
        assertThatThrownBy(() -> visibility.setOperatorPrivate("family", false))
                .isInstanceOf(ValidationException.class);
        visibility.setOperatorPrivate("family", true);
        assertThat(visibility.isPrivate("family")).isTrue();
    }

    @Test
    @DisplayName("canSeePrivate: admin always; read and agent only with the grant; anonymous never")
    void canSeePrivate() {
        assertThat(SourceVisibility.canSeePrivate(null, true)).isFalse();
        assertThat(SourceVisibility.canSeePrivate(Scope.READ, false)).isFalse();
        assertThat(SourceVisibility.canSeePrivate(Scope.AGENT, false)).isFalse();
        assertThat(SourceVisibility.canSeePrivate(Scope.READ, true)).isTrue();
        assertThat(SourceVisibility.canSeePrivate(Scope.AGENT, true)).isTrue();
        assertThat(SourceVisibility.canSeePrivate(Scope.ADMIN, false)).isTrue();
        assertThat(SourceVisibility.canSeePrivate(Optional.empty())).isFalse();
        assertThat(SourceVisibility.canSeePrivate(token(Scope.READ, true, null))).isTrue();
        assertThat(SourceVisibility.canSeePrivate(RequestPrincipal.anonymous(ReadRestriction.NONE))).isFalse();
        assertThat(SourceVisibility.canSeePrivate(new RequestPrincipal(RequestPrincipal.Kind.FEED_KEY,
                UUID.randomUUID(), "kiosk", Scope.READ, null, true))).isTrue();
        assertThat(SourceVisibility.canSeePrivate((RequestPrincipal) null)).isFalse();
    }

    @Test
    @DisplayName("hiddenFor keeps a token's source restriction: a private source outside it stays hidden")
    void hiddenFor() {
        assertThat(visibility.hiddenFor(null, false, null)).containsExactly("family");
        assertThat(visibility.hiddenFor(Scope.AGENT, false, null)).containsExactly("family");
        assertThat(visibility.hiddenFor(Scope.READ, true, null)).isEmpty();
        assertThat(visibility.hiddenFor(Scope.READ, true, new TokenRestrictions(List.of("quakes"), null, null, null)))
                .containsExactly("family");
        assertThat(visibility.hiddenFor(Scope.READ, true, new TokenRestrictions(List.of("family"), null, null, null)))
                .isEmpty();
        assertThat(visibility.filterFor(null, false, null).hidden()).containsExactly("family");
        assertThat(visibility.publicFilter().hidden()).containsExactly("family");
    }

    @Test
    @DisplayName("visible(...): tokens, read restrictions and feed-key principals")
    void visibleOverloads() {
        assertThat(visibility.visible("quakes", Optional.empty())).isTrue();
        assertThat(visibility.visible("unknown", Optional.empty())).isTrue();
        assertThat(visibility.visible("family", Optional.empty())).isFalse();
        assertThat(visibility.visible("family", token(Scope.AGENT, false, null))).isFalse();
        assertThat(visibility.visible("family", token(Scope.AGENT, true, null))).isTrue();
        assertThat(visibility.visible("family", token(Scope.ADMIN, false, null))).isTrue();
        assertThat(visibility.visible("family",
                token(Scope.READ, true, new TokenRestrictions(List.of("quakes"), null, null, null)))).isFalse();

        ReadRestriction excludePrivate = new ReadRestriction(null, null, null, null, null, true, null);
        assertThat(visibility.visible("family", token(Scope.ADMIN, false, null), excludePrivate)).isFalse();
        ReadRestriction onlyQuakes = new ReadRestriction(List.of("quakes"), null, null, null, null, false, null);
        assertThat(visibility.visible("home", token(Scope.ADMIN, false, null), onlyQuakes)).isFalse();
        assertThat(visibility.visible("quakes", token(Scope.ADMIN, false, null), (ReadRestriction) null)).isTrue();

        RequestPrincipal feed = new RequestPrincipal(RequestPrincipal.Kind.FEED_KEY, UUID.randomUUID(), "kiosk",
                Scope.READ, ReadRestriction.NONE, false);
        RequestPrincipal feedGrant = new RequestPrincipal(RequestPrincipal.Kind.FEED_KEY, UUID.randomUUID(), "kiosk",
                Scope.READ, ReadRestriction.NONE, true);
        assertThat(visibility.visible("family", Optional.empty(), feed)).isFalse();
        assertThat(visibility.visible("family", Optional.empty(), feedGrant)).isTrue();
        assertThat(visibility.visible("quakes", Optional.empty(), feed)).isTrue();
        assertThat(visibility.visible("family", Optional.empty(), (RequestPrincipal) null)).isFalse();
    }

    @Test
    @DisplayName("refresh publishes SourceBecamePrivateEvent once per newly private source")
    void becamePrivateEvents() {
        visibility.privateSourceIds();
        descriptors.add(source("tracker", true));
        visibility.onRegistryChanged(SourceRegistryChangedEvent.registered("tracker"));
        stored.set(VisibilitySettings.defaults().with("home", true));
        visibility.onSettingChanged(new AppSettingChangedEvent(SettingKeys.VISIBILITY));
        visibility.refresh();
        visibility.onSettingChanged(new AppSettingChangedEvent(SettingKeys.SECURITY));

        assertThat(published).containsExactly(new SourceBecamePrivateEvent("tracker"),
                new SourceBecamePrivateEvent("home"));
    }

    @Test
    @DisplayName("without settings or events (unit tests) only descriptor flags count")
    void standalone() {
        SourceRegistry registry = mock(SourceRegistry.class);
        when(registry.descriptors()).thenReturn(List.of(source("a", true), source("b", false)));
        SourceVisibility bare = new SourceVisibility(registry, null, null);
        assertThat(bare.privateSourceIds()).containsExactly("a");
        bare.refresh();
        assertThat(bare.operatorPrivate("b")).isFalse();
        assertThat(Map.of()).isEmpty();
    }
}
