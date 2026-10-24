package ai.foresignal.chronicle.network;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleRepository;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.collab.ShareableObjectEntity;
import ai.foresignal.collab.Visibility;
import ai.foresignal.entity.EntityKeyRepository;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectService;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.Tlp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Disclosure (prompt 134, DISC-1): full, opaque or nothing per visibility, discoverability and TLP; RED never
 * discoverable; title and owner only when revealed; strength bands without finer counts.
 */
class DisclosureTest {

    private static final Instant T = Instant.parse("2026-10-12T12:00:00Z");
    private static final String A = "01JA8Q3V7S2K9W4T6Y1B5N0M3A";
    private static final String B = "01JA8Q3V7S2K9W4T6Y1B5N0M3B";

    @Test
    @DisplayName("full when the viewer sees both, opaque when the other is discoverable, nothing otherwise")
    void levels() {
        assertThat(Disclosure.level(true, false, DiscoverabilityLevel.NONE)).isEqualTo(Disclosure.Level.FULL);
        assertThat(Disclosure.level(false, false, DiscoverabilityLevel.INSTANCE)).isEqualTo(Disclosure.Level.OPAQUE);
        assertThat(Disclosure.level(false, false, DiscoverabilityLevel.NETWORK)).isEqualTo(Disclosure.Level.OPAQUE);
        assertThat(Disclosure.level(false, false, DiscoverabilityLevel.NONE)).isEqualTo(Disclosure.Level.NONE);
        assertThat(Disclosure.level(false, true, null)).as("peers only send network sketches")
                .isEqualTo(Disclosure.Level.OPAQUE);
        assertThat(Disclosure.level(true, true, null)).as("a remote chronicle is never shown in full")
                .isEqualTo(Disclosure.Level.OPAQUE);
    }

    @Test
    @DisplayName("TLP caps discoverability: RED never, AMBER+STRICT at most instance, the rest anything")
    void tlpCaps() {
        assertThat(DiscoverabilityLevel.maxAllowed(Tlp.RED)).isEqualTo(DiscoverabilityLevel.NONE);
        assertThat(DiscoverabilityLevel.maxAllowed(Tlp.AMBER_STRICT)).isEqualTo(DiscoverabilityLevel.INSTANCE);
        for (Tlp t : List.of(Tlp.AMBER, Tlp.GREEN, Tlp.CLEAR)) {
            assertThat(DiscoverabilityLevel.maxAllowed(t)).isEqualTo(DiscoverabilityLevel.NETWORK);
        }
        assertThat(DiscoverabilityLevel.cap(DiscoverabilityLevel.NETWORK, Tlp.RED)).isEqualTo(DiscoverabilityLevel.NONE);
        assertThat(DiscoverabilityLevel.cap(DiscoverabilityLevel.NETWORK, Tlp.AMBER_STRICT))
                .isEqualTo(DiscoverabilityLevel.INSTANCE);
        assertThat(DiscoverabilityLevel.cap(DiscoverabilityLevel.INSTANCE, Tlp.GREEN))
                .isEqualTo(DiscoverabilityLevel.INSTANCE);
        assertThat(DiscoverabilityLevel.fromWire(null)).isEqualTo(DiscoverabilityLevel.NONE);
        DiscoverabilityLevel.check(DiscoverabilityLevel.NONE, Tlp.RED);
        DiscoverabilityLevel.check(DiscoverabilityLevel.INSTANCE, Tlp.AMBER_STRICT);
        ForesignalException red = catchThrowableOfType(ForesignalException.class,
                () -> DiscoverabilityLevel.check(DiscoverabilityLevel.INSTANCE, Tlp.RED));
        assertThat(red.getCode()).isEqualTo(ErrorCodes.DISCOVERABILITY_NOT_ALLOWED);
        assertThat(red.getProperties()).containsKey("rule");
        ForesignalException strict = catchThrowableOfType(ForesignalException.class,
                () -> DiscoverabilityLevel.check(DiscoverabilityLevel.NETWORK, Tlp.AMBER_STRICT));
        assertThat(String.valueOf(strict.getProperties().get("rule"))).contains("AMBER+STRICT");
    }

    @Test
    @DisplayName("an opaque link carries a strength band and dimensions, never counts or scores")
    void opaqueShape() {
        Map<String, Object> view = Disclosure.opaque("01JA8Q3V7S2K9W4T6Y1B5N0M3K", A, "medium",
                List.of("evidence", "area-time"), "partial", false, null, null, null);
        assertThat(view).containsOnlyKeys("linkId", "disclosure", "chronicleId", "strength", "dimensions",
                "timeOverlap", "remote", "ownerHandle", "title", "accessRequestStatus");
        assertThat(view.get("disclosure")).isEqualTo("opaque");
        assertThat(view.get("title")).isNull();
        assertThat(view.get("ownerHandle")).isNull();
        assertThat(NetworkVocabulary.strength(0.05)).isEqualTo("low");
        assertThat(NetworkVocabulary.strength(0.1)).isEqualTo("medium");
        assertThat(NetworkVocabulary.strength(0.29)).isEqualTo("medium");
        assertThat(NetworkVocabulary.strength(0.3)).isEqualTo("high");
    }

    @Test
    @DisplayName("capture-span overlap is none, partial or full")
    void timeOverlap() {
        Instant d1 = T;
        Instant d2 = T.plusSeconds(86_400);
        Instant d3 = T.plusSeconds(2 * 86_400);
        Instant d4 = T.plusSeconds(3 * 86_400);
        assertThat(Disclosure.timeOverlap(d1, d2, d3, d4, d4)).isEqualTo("none");
        assertThat(Disclosure.timeOverlap(d1, d3, d2, d4, d4)).isEqualTo("partial");
        assertThat(Disclosure.timeOverlap(d1, d4, d2, d3, d4)).isEqualTo("full");
        // Both running end now, so the one that started first contains the other.
        assertThat(Disclosure.timeOverlap(d1, null, d2, null, d4)).as("both running").isEqualTo("full");
        assertThat(Disclosure.timeOverlap(d1, d3, d2, null, d4)).as("one running past the other's end")
                .isEqualTo("partial");
        assertThat(Disclosure.timeOverlap(null, null, d2, d3, d4)).isEqualTo("none");
    }

    @Test
    @DisplayName("the other chronicle's title and owner appear in an opaque link only when its owner revealed them")
    void revealFlags() {
        ChronicleRepository chronicles = mock(ChronicleRepository.class);
        AccessResolver access = mock(AccessResolver.class);
        PrincipalService principals = mock(PrincipalService.class);
        ChronicleAccessRequestRepository requests = mock(ChronicleAccessRequestRepository.class);
        ChronicleLinkViews views = new ChronicleLinkViews(chronicles, mock(ChronicleRemoteSketchRepository.class),
                requests, mock(ChronicleKeyStore.class), mock(NetworkSalts.class), access, mock(ObjectService.class),
                principals, mock(EntityKeyRepository.class), Clock.fixed(T, ZoneOffset.UTC));
        ChronicleEntity other = new ChronicleEntity(B, "CHR-2026-0002", "Gulf convoys", 1000, T);
        when(chronicles.findById(B)).thenReturn(Optional.of(other));
        when(access.record("chronicle:" + B)).thenReturn(Optional.of(new ShareableObjectEntity("chronicle:" + B,
                "chronicle", "owner-1", Visibility.PRIVATE, "AMBER", T)));
        PrincipalEntity owner = new PrincipalEntity("owner-1", "dana", "Dana", PrincipalKind.PERSON, T);
        when(principals.find(anyString())).thenReturn(Optional.of(owner));
        when(requests.findByLinkIdAndRequesterPrincipalOrderByCreatedAtDescIdDesc(anyString(), any()))
                .thenReturn(List.of());
        ChronicleLinkEntity link = new ChronicleLinkEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3K", A, B, "inst-1", "high", T);
        link.setDimensions(List.of("evidence"));
        link.setSharedKeyCount(42);
        ObjectCaller viewer = new ObjectCaller("viewer-1", "token:viewer", Scope.READ, false, Set.of());

        Map<String, Object> hidden = views.opaque(link, A, viewer);
        assertThat(hidden.get("title")).isNull();
        assertThat(hidden.get("ownerHandle")).isNull();
        assertThat(hidden).doesNotContainKeys("sharedKeyCount", "scores", "sharedCanonicalEvents");

        other.setRevealTitle(true);
        other.setRevealOwner(true);
        Map<String, Object> revealed = views.opaque(link, A, viewer);
        assertThat(revealed.get("title")).isEqualTo("CHR-2026-0002 Gulf convoys");
        assertThat(revealed.get("ownerHandle")).isEqualTo("dana");
        assertThat(revealed.get("strength")).isEqualTo("high");
    }
}
