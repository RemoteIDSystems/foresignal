package ai.foresignal.chronicle.network;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.chronicle.ChronicleRepository;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.entity.EntityKeyRepository;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectService;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Anti-probing (prompt 134): the minimum shared keys and containment, the minimum key count before discovery, the daily
 * cap of new opaque links, and churn that does not buy fresh discovery.
 */
class AntiProbingTest {

    private static final Instant T = Instant.parse("2026-10-12T12:00:00Z");

    @Test
    @DisplayName("a link needs at least the minimum shared keys and a containment of 0.05 of the smaller chronicle")
    void minimumOverlap() {
        LinkScores.Decision four = LinkScores.decide(Map.of(NetworkVocabulary.EVIDENCE, LinkScores.exact(4, 10, 10)), 5);
        assertThat(four.linked()).isFalse();
        LinkScores.Decision thin = LinkScores.decide(Map.of(NetworkVocabulary.EVIDENCE,
                LinkScores.exact(5, 200, 10_000)), 5);
        assertThat(thin.linked()).as("5 of 200 is a containment of 0.025").isFalse();
        LinkScores.Decision enough = LinkScores.decide(Map.of(NetworkVocabulary.EVIDENCE,
                LinkScores.exact(5, 100, 10_000)), 5);
        assertThat(enough.linked()).isTrue();
        assertThat(enough.dimensions()).containsExactly(NetworkVocabulary.EVIDENCE);
        assertThat(enough.strength()).isEqualTo("low");
        LinkScores.Decision strong = LinkScores.decide(Map.of(NetworkVocabulary.EVIDENCE, LinkScores.exact(60, 60, 60),
                NetworkVocabulary.AREA_TIME, LinkScores.exact(2, 60, 60)), 5);
        assertThat(strong.strength()).isEqualTo("high");
        assertThat(strong.dimensions()).containsExactly(NetworkVocabulary.EVIDENCE);
        assertThat(strong.scores()).containsKeys(NetworkVocabulary.EVIDENCE, NetworkVocabulary.AREA_TIME);
        assertThat(LinkScores.decide(Map.of(NetworkVocabulary.EVIDENCE, LinkScores.exact(6, 10, 10)), 7).linked())
                .as("network.minSharedKeys raises the bar").isFalse();
    }

    @Test
    @DisplayName("discovery runs only from 50 captured keys, and churn holds young chronicles back")
    void eligibility() {
        assertThat(AntiProbing.eligible(49, T.minus(Duration.ofDays(3)), List.of(), T)).isFalse();
        assertThat(AntiProbing.eligible(50, T.minus(Duration.ofDays(3)), List.of(), T)).isTrue();
        List<AntiProbing.Churn> churn = List.of(new AntiProbing.Churn(T.minus(Duration.ofHours(5)),
                T.minus(Duration.ofHours(2))));
        assertThat(AntiProbing.churnHold(churn, T)).isTrue();
        assertThat(AntiProbing.eligible(500, T.minus(Duration.ofHours(1)), churn, T))
                .as("created and deleted within a day, then created again: no fresh discovery").isFalse();
        assertThat(AntiProbing.eligible(500, T.minus(Duration.ofDays(2)), churn, T))
                .as("an older chronicle of the same owner is unaffected").isTrue();
        List<AntiProbing.Churn> slow = List.of(new AntiProbing.Churn(T.minus(Duration.ofDays(5)),
                T.minus(Duration.ofHours(2))));
        assertThat(AntiProbing.churnHold(slow, T)).as("a chronicle kept for days is not churn").isFalse();
        List<AntiProbing.Churn> old = List.of(new AntiProbing.Churn(T.minus(Duration.ofDays(3)),
                T.minus(Duration.ofDays(2))));
        assertThat(AntiProbing.churnHold(old, T)).as("churn older than a day").isFalse();
        assertThat(AntiProbing.mayRequest(9)).isTrue();
        assertThat(AntiProbing.mayRequest(10)).isFalse();
    }

    @Test
    @DisplayName("a principal is shown at most 20 new opaque links a day; links already shown stay visible")
    void dailyCap() {
        ChronicleKeyStore store = mock(ChronicleKeyStore.class);
        ChronicleLinkViews views = new ChronicleLinkViews(mock(ChronicleRepository.class),
                mock(ChronicleRemoteSketchRepository.class), mock(ChronicleAccessRequestRepository.class), store,
                mock(NetworkSalts.class), mock(AccessResolver.class), mock(ObjectService.class),
                mock(PrincipalService.class), mock(EntityKeyRepository.class), Clock.fixed(T, ZoneOffset.UTC));
        ObjectCaller viewer = new ObjectCaller("p1", "token:p1", Scope.READ, false, Set.of());
        when(store.disclosed(eq("p1"), any())).thenReturn(List.of());
        when(store.disclosedSince(eq("p1"), any())).thenReturn(19L);
        assertThat(views.allowOpaque(viewer, "link-20", true)).isTrue();
        verify(store).disclose(eq("p1"), eq("link-20"), any());
        when(store.disclosedSince(eq("p1"), any())).thenReturn(20L);
        assertThat(views.allowOpaque(viewer, "link-21", true)).isFalse();
        verify(store, never()).disclose(eq("p1"), eq("link-21"), any());
        when(store.disclosed(eq("p1"), eq(List.of("link-20")))).thenReturn(List.of("link-20"));
        assertThat(views.allowOpaque(viewer, "link-20", true)).as("already shown").isTrue();
        ObjectCaller admin = new ObjectCaller("a1", "admin", Scope.ADMIN, true, Set.of());
        assertThat(views.allowOpaque(admin, "link-99", true)).isTrue();
        verify(store, never()).disclosedSince(eq("a1"), any());
        verify(store, never()).disclose(anyString(), eq("link-99"), any());
    }

    @Test
    @DisplayName("a sketch change above 10% makes discovery due before the nightly run")
    void changeTrigger() {
        assertThat(ChronicleSketcher.discoveryDue(0, 49, 1.0)).isFalse();
        assertThat(ChronicleSketcher.discoveryDue(0, 50, 0)).isTrue();
        assertThat(ChronicleSketcher.discoveryDue(100, 105, 0.02)).isFalse();
        assertThat(ChronicleSketcher.discoveryDue(100, 111, 0)).isTrue();
        assertThat(ChronicleSketcher.discoveryDue(100, 100, 0.2)).isTrue();
    }
}
