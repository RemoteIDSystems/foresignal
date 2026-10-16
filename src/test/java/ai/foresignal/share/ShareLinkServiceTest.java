package ai.foresignal.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.export.ExportJobService;
import ai.foresignal.provenance.AttributionService;
import ai.foresignal.provenance.LicenceResolver;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.TokenHasher;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.security.ratelimit.ClientRateLimiter;
import ai.foresignal.security.ratelimit.RateClass;
import ai.foresignal.security.restrict.ReadScopes;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.track.TrackService;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.view.SavedViewService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;

/** The use path of share links (prompt 112): the per-link rate limit, status checks and the atomic use count. */
class ShareLinkServiceTest {

    static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");
    static final String KEY = "fss_" + "k".repeat(43);

    ShareLinkRepository repository;
    TokenHasher hasher;
    ClientRateLimiter limiter;
    AuditService audit;
    ShareLinkService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        repository = mock(ShareLinkRepository.class);
        hasher = mock(TokenHasher.class);
        limiter = mock(ClientRateLimiter.class);
        audit = mock(AuditService.class);
        when(hasher.hash(KEY)).thenReturn("hash");
        when(hasher.matches(KEY, "hash")).thenReturn(true);
        service = new ShareLinkService(repository, hasher, mock(EgressPolicy.class), mock(SourceRegistry.class),
                mock(RecordQueryService.class), mock(LicenceResolver.class), mock(AttributionService.class),
                mock(AlertStore.class), mock(SavedViewService.class), mock(TrackService.class),
                mock(BriefingService.class), mock(ExportJobService.class), mock(ObjectProvider.class), limiter, audit,
                JsonMapper.builder().build(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    static ShareLinkEntity link(Instant expiresAt, Integer maxUses) {
        return new ShareLinkEntity("01JABCDEF0123456789ABCDEFG", "hash", KEY.substring(0, 12), "record", "s/e", null,
                null, false, expiresAt, maxUses, "token", NOW.minusSeconds(60), null);
    }

    @Test
    @DisplayName("above 60 uses a minute a link is 429 with Retry-After, and the use is not counted")
    void rateLimited() {
        ShareLinkEntity link = link(NOW.plusSeconds(3600), null);
        when(repository.findByKeyHash("hash")).thenReturn(Optional.of(link));
        when(limiter.tryConsume(eq("share:" + link.getId()), eq(RateClass.READ), eq(ShareLinkService.USES_PER_MINUTE)))
                .thenReturn(new ClientRateLimiter.Decision(false, RateClass.READ, 7));
        assertThatThrownBy(() -> service.use(KEY, "192.0.2.1")).isInstanceOf(ForesignalException.class)
                .satisfies(e -> {
                    ForesignalException f = (ForesignalException) e;
                    assertThat(f.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                    assertThat(f.getCode()).isEqualTo(ErrorCodes.RATE_LIMITED);
                    assertThat(f.getHeaders()).containsEntry("Retry-After", "7");
                });
        verify(repository, never()).use(anyString(), any());
    }

    @Test
    @DisplayName("a use is counted atomically; a lost race is 410 SHARE_EXPIRED")
    void counted() {
        ShareLinkEntity link = link(NOW.plusSeconds(3600), 1);
        when(repository.findByKeyHash("hash")).thenReturn(Optional.of(link));
        when(repository.findById(link.getId())).thenReturn(Optional.of(link));
        when(limiter.tryConsume(anyString(), any(), anyInt())).thenReturn(new ClientRateLimiter.Decision(true, RateClass.READ, 0));
        when(repository.use(eq(link.getId()), eq(NOW))).thenReturn(1).thenReturn(0);
        assertThat(service.use(KEY, "192.0.2.1")).isSameAs(link);
        verify(audit).record(any());
        assertThatThrownBy(() -> service.use(KEY, "192.0.2.1")).isInstanceOf(ForesignalException.class)
                .satisfies(e -> assertThat(((ForesignalException) e).getCode()).isEqualTo(ErrorCodes.SHARE_EXPIRED));
    }

    @Test
    @DisplayName("expired, revoked and exhausted links are 410 before the limiter; unknown keys are 404")
    void statuses() {
        ShareLinkEntity expired = link(NOW.minusSeconds(1), null);
        when(repository.findByKeyHash("hash")).thenReturn(Optional.of(expired));
        assertThat(expired.status(NOW)).isEqualTo("expired");
        assertThatThrownBy(() -> service.use(KEY, null)).isInstanceOf(ForesignalException.class)
                .satisfies(e -> assertThat(((ForesignalException) e).getCode()).isEqualTo(ErrorCodes.SHARE_EXPIRED));
        ShareLinkEntity revoked = link(NOW.plusSeconds(60), null);
        revoked.revoke(NOW);
        assertThat(revoked.status(NOW)).isEqualTo("revoked");
        assertThat(link(NOW.plusSeconds(60), 0).status(NOW)).isEqualTo("exhausted");
        assertThat(link(NOW.plusSeconds(60), 5).status(NOW)).isEqualTo("active");
        verify(limiter, never()).tryConsume(anyString(), any(), anyInt());
        assertThat(service.find("fsf_" + "x".repeat(43))).isEmpty();
    }
}
