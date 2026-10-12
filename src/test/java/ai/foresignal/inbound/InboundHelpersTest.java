package ai.foresignal.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.Rejection;
import ai.foresignal.ingest.RejectionReason;
import ai.foresignal.security.IngestCaller;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * The small pieces of the push pipeline (prompt 024): URL redaction, credential and path parsing,
 * key handling, rejection merging, and the ntfy and Gotify helpers.
 */
class InboundHelpersTest {

    @Test
    @DisplayName("auth and token query values are masked; other parameters stay")
    void redaction() {
        assertThat(IngestUrls.redacted("/compat/ntfy/x/publish", "message=hi&auth=QmVhcmVy&title=t"))
                .isEqualTo("/compat/ntfy/x/publish?message=hi&auth=***&title=t");
        assertThat(IngestUrls.redacted("/compat/gotify/message", "TOKEN=eik_abc")).isEqualTo("/compat/gotify/message?TOKEN=***");
        assertThat(IngestUrls.redacted("/v1/ingest/x", null)).isEqualTo("/v1/ingest/x");
    }

    @Test
    @DisplayName("paths name the source and format; compat paths say which protocol")
    void paths() {
        assertThat(IngestAuthenticator.IngestPath.of("/v1/ingest/custom-a"))
                .isEqualTo(new IngestAuthenticator.IngestPath("custom-a", "native", null));
        assertThat(IngestAuthenticator.IngestPath.of("/v1/ingest/custom-a/cap?x=1"))
                .isEqualTo(new IngestAuthenticator.IngestPath("custom-a", "cap", null));
        assertThat(IngestAuthenticator.IngestPath.of("/v1/external/sources/ext-a/items").sourceId()).isEqualTo("ext-a");
        assertThat(IngestAuthenticator.IngestPath.of("/compat/ntfy/custom-a/publish"))
                .isEqualTo(new IngestAuthenticator.IngestPath("custom-a", null, IngestCaller.COMPAT_NTFY));
        assertThat(IngestAuthenticator.IngestPath.of("/compat/ntfy").sourceId()).isNull();
        assertThat(IngestAuthenticator.IngestPath.of("/compat/gotify/message").compat()).isEqualTo(IngestCaller.COMPAT_GOTIFY);
    }

    @Test
    @DisplayName("credentials: header, bearer, Basic password, ntfy ?auth= and Gotify header and ?token=")
    void credentials() {
        MockHttpServletRequest header = new MockHttpServletRequest("POST", "/v1/ingest/custom-a");
        header.addHeader("X-Foresignal-Ingest-Key", "eik_header");
        assertThat(credential(header).key()).isEqualTo("eik_header");

        MockHttpServletRequest bearer = new MockHttpServletRequest("POST", "/v1/ingest/custom-a");
        bearer.addHeader("Authorization", "Bearer eik_bearer");
        assertThat(credential(bearer).key()).isEqualTo("eik_bearer");

        MockHttpServletRequest basic = new MockHttpServletRequest("POST", "/v1/ingest/custom-a");
        basic.addHeader("Authorization", "Basic " + b64("user:eik_basic"));
        assertThat(credential(basic).key()).isEqualTo("eik_basic");

        MockHttpServletRequest ntfy = new MockHttpServletRequest("GET", "/compat/ntfy/custom-a/publish");
        ntfy.setParameter("auth", b64("Bearer eik_ntfy"));
        IngestAuthenticator.Credential fromAuth = credential(ntfy);
        assertThat(fromAuth.key()).isEqualTo("eik_ntfy");
        assertThat(fromAuth.via()).isEqualTo(IngestCaller.COMPAT_NTFY);

        MockHttpServletRequest gotify = new MockHttpServletRequest("POST", "/compat/gotify/message");
        gotify.addHeader("X-Gotify-Key", "eik_gotify");
        assertThat(credential(gotify).key()).isEqualTo("eik_gotify");
        MockHttpServletRequest token = new MockHttpServletRequest("POST", "/compat/gotify/message");
        token.setParameter("token", "eik_token");
        assertThat(credential(token).via()).isEqualTo(IngestCaller.COMPAT_GOTIFY);

        assertThat(credential(new MockHttpServletRequest("POST", "/v1/ingest/custom-a"))).isNull();
        MockHttpServletRequest garbage = new MockHttpServletRequest("POST", "/v1/ingest/custom-a");
        garbage.addHeader("Authorization", "Basic !!!");
        assertThat(credential(garbage).key()).isNull();
    }

    @Test
    @DisplayName("ingest keys: eik_ prefix, SHA-256 storage, constant-time match")
    void keys() {
        IngestKeys keys = new IngestKeys();
        String key = keys.generate();
        assertThat(key).startsWith("eik_").hasSize(4 + 43);
        assertThat(keys.hash(key)).hasSize(64);
        assertThat(keys.matches(key, keys.hash(key))).isTrue();
        assertThat(keys.matches("eik_other", keys.hash(key))).isFalse();
        assertThat(IngestKeys.prefixOf(key)).hasSize(8);
        assertThat(keys.generate("fsx_")).startsWith("fsx_");
    }

    @Test
    @DisplayName("format and writer rejections merge by request index")
    void mergeRejections() {
        ParsedIngest parsed = ParsedIngest.builder()
                .reject(0, "bad", "EXTERNAL_ID_INVALID", "x")
                .add(1, NormalizedRecord.builder("a", "k").build())
                .add(2, NormalizedRecord.builder("b", "k").build())
                .build();
        IngestOutcome outcome = new IngestOutcome(2, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, null, 0,
                List.of(new Rejection(1, "b", RejectionReason.MISSING_OBSERVED_AT, "no time")));

        List<IngestOutcomeView.RejectionView> merged = PushIngestService.merge(parsed, outcome);

        assertThat(merged).containsExactly(new IngestOutcomeView.RejectionView(0, "bad", "EXTERNAL_ID_INVALID"),
                new IngestOutcomeView.RejectionView(2, "b", "MISSING_OBSERVED_AT"));
        IngestOutcomeView view = IngestOutcomeView.of(outcome, 3, merged);
        assertThat(view.seen()).isEqualTo(3);
        assertThat(view.rejected()).isEqualTo(2);
        assertThat(view.changed()).isEqualTo(view.persisted());
        assertThat(view.fromSeq()).isNull();
        assertThat(view.byReason()).hasSize(9);
    }

    @Test
    @DisplayName("ntfy priorities, tags with id=, and the compat kind rule")
    void compatHelpers() {
        assertThat(CompatPublishService.ntfyPriority("urgent")).isEqualTo(5);
        assertThat(CompatPublishService.ntfyPriority("min")).isEqualTo(1);
        assertThat(CompatPublishService.ntfyPriority("3")).isEqualTo(3);
        assertThat(CompatPublishService.ntfyPriority(null)).isNull();
        assertThatThrownBy(() -> CompatPublishService.ntfyPriority("loud")).isInstanceOf(InvalidInputException.class);

        CompatPublishService.Tags tags = CompatPublishService.ntfyTags("backup, id=pve1 ,,warning");
        assertThat(tags.tags()).containsExactly("backup", "warning");
        assertThat(tags.externalId()).isEqualTo("pve1");

        assertThat(CompatPublishService.kind(source("alert"))).isEqualTo("alert");
        assertThat(CompatPublishService.kind(source("a", "notification"))).isEqualTo("notification");
        assertThatThrownBy(() -> CompatPublishService.kind(source("a", "b"))).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a caller may push into its own source; an admin token anywhere")
    void callers() {
        IngestCaller key = new IngestCaller(IngestCaller.INGEST_KEY, "custom-a", "ingest-key:eik_abcd", "eik_abcd");
        IngestCaller admin = new IngestCaller(IngestCaller.ADMIN_TOKEN, null, "token:ops", null);
        assertThat(key.mayPushTo("custom-a")).isTrue();
        assertThat(key.mayPushTo("custom-b")).isFalse();
        assertThat(key.viaKey()).isTrue();
        assertThat(admin.mayPushTo("custom-b")).isTrue();
        assertThat(admin.admin()).isTrue();
    }

    private static SourceDescriptor source(String... kinds) {
        return SourceDescriptor.builder("custom-x").kinds(kinds).mode(Mode.PUSH).defaultBudget(1, 1).build();
    }

    private static IngestAuthenticator.Credential credential(MockHttpServletRequest request) {
        return IngestAuthenticator.credential(request, IngestAuthenticator.IngestPath.of(request.getRequestURI()));
    }

    private static String b64(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
}
