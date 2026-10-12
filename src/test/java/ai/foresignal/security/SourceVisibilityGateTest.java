package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The private-source gate hook (prompt 023). */
class SourceVisibilityGateTest {

    private final SourceRegistry registry = mock(SourceRegistry.class);
    private final SourceVisibilityGate gate = new SourceVisibilityGate(registry);

    @BeforeEach
    void setUp() {
        SourceDescriptor open = SourceDescriptor.builder("open").name("Open").kind("k").mode(Mode.PUSH).build();
        SourceDescriptor secret =
                SourceDescriptor.builder("secret").name("Secret").kind("k").mode(Mode.PUSH).privateSource(true).build();
        when(registry.descriptors()).thenReturn(List.of(open, secret));
        when(registry.find("open")).thenReturn(Optional.of(new SourceRegistry.Registration(open, null, false)));
        when(registry.find("secret")).thenReturn(Optional.of(new SourceRegistry.Registration(secret, null, false)));
        when(registry.find("unknown")).thenReturn(Optional.empty());
    }

    private static Optional<CurrentToken> token(Scope scope, TokenRestrictions restrictions) {
        return Optional.of(new CurrentToken(new AuthenticatedToken(UUID.randomUUID(), "t", scope, restrictions, null,
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C", false), false));
    }

    @Test
    @DisplayName("a private source is hidden from anonymous callers and read tokens")
    void hiddenFromAnonymousAndRead() {
        assertThat(gate.visible("secret", Optional.empty())).isFalse();
        assertThat(gate.visible("secret", token(Scope.READ, TokenRestrictions.NONE))).isFalse();
        assertThat(gate.excludedSources(null, TokenRestrictions.NONE)).containsExactly("secret");
        assertThat(gate.excludedSources(Scope.READ, TokenRestrictions.NONE)).containsExactly("secret");
    }

    @Test
    @DisplayName("a private source is visible to agent and admin tokens")
    void visibleToAgentAndAdmin() {
        assertThat(gate.visible("secret", token(Scope.AGENT, TokenRestrictions.NONE))).isTrue();
        assertThat(gate.visible("secret", token(Scope.ADMIN, TokenRestrictions.NONE))).isTrue();
        assertThat(gate.excludedSources(Scope.AGENT, TokenRestrictions.NONE)).isEmpty();
        assertThat(gate.excludedSources(Scope.ADMIN, null)).isEmpty();
    }

    @Test
    @DisplayName("a source restriction that leaves the private source out hides it even from admin")
    void sourceRestrictionExcludes() {
        TokenRestrictions onlyOpen = new TokenRestrictions(List.of("open"), null, null, null);
        TokenRestrictions withSecret = new TokenRestrictions(List.of("secret"), null, null, null);

        assertThat(gate.visible("secret", token(Scope.ADMIN, onlyOpen))).isFalse();
        assertThat(gate.visible("secret", token(Scope.AGENT, withSecret))).isTrue();
        assertThat(gate.excludedSources(Scope.ADMIN, onlyOpen)).containsExactly("secret");
    }

    @Test
    @DisplayName("public and unknown sources are always reported visible")
    void publicAndUnknown() {
        assertThat(gate.visible("open", Optional.empty())).isTrue();
        assertThat(gate.visible("unknown", Optional.empty())).isTrue();
        assertThat(SourceVisibilityGate.privateVisible("secret", null, null)).isFalse();
    }
}
