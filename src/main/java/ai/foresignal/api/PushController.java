package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.notify.push.PushSubscriptionService;
import ai.foresignal.notify.push.VapidKeyService;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.CurrentToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The Web Push operations of the {@code Push} tag (prompt 031): {@code getVapidPublicKey} (open),
 * {@code listPushSubscriptions} (admin), {@code createPushSubscription},
 * {@code verifyPushSubscription} and {@code deletePushSubscription} (read scope, always
 * authenticated, so a token is required even under {@code openReads}). Responses never contain an
 * endpoint, only its host.
 */
@RestController
@Tag(name = "Push")
@RequestMapping("/v1/push")
public class PushController {

    private final VapidKeyService vapid;
    private final PushSubscriptionService subscriptions;

    /** Creates the controller. */
    public PushController(VapidKeyService vapid, PushSubscriptionService subscriptions) {
        this.vapid = vapid;
        this.subscriptions = subscriptions;
    }

    /** {@code getVapidPublicKey}: the base64url uncompressed P-256 point. */
    @GetMapping("/vapid-public-key")
    @Operation(operationId = "getVapidPublicKey", tags = "Push")
    public Map<String, Object> getVapidPublicKey() {
        return Map.of("publicKey", vapid.publicKey());
    }

    /** {@code listPushSubscriptions}: oldest first. */
    @GetMapping("/subscriptions")
    @Operation(operationId = "listPushSubscriptions", tags = "Push")
    public List<Map<String, Object>> listPushSubscriptions() {
        return subscriptions.list().stream().map(PushSubscriptionService::view).toList();
    }

    /** {@code createPushSubscription}: 201 (pending) with {@code Location}; the verify push is sent. */
    @PostMapping("/subscriptions")
    @Operation(operationId = "createPushSubscription", tags = "Push")
    public ResponseEntity<Map<String, Object>> createPushSubscription(@RequestBody JsonNode body,
            HttpServletRequest request) {
        PushSubscriptionService.Created created = subscriptions.create(body, caller(request));
        Map<String, Object> view = PushSubscriptionService.view(created.subscription());
        return ResponseEntity.created(URI.create("/v1/push/subscriptions/" + created.subscription().getId())).body(view);
    }

    /** {@code verifyPushSubscription}: the echoed code activates the subscription. */
    @PostMapping("/subscriptions/{subscriptionId}/verify")
    @Operation(operationId = "verifyPushSubscription", tags = "Push")
    public Map<String, Object> verifyPushSubscription(@PathVariable String subscriptionId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return PushSubscriptionService.view(subscriptions.verify(id(subscriptionId), body, caller(request)));
    }

    /** {@code deletePushSubscription}: the creating token or an admin; 404 otherwise. */
    @DeleteMapping("/subscriptions/{subscriptionId}")
    @Operation(operationId = "deletePushSubscription", tags = "Push")
    public ResponseEntity<Void> deletePushSubscription(@PathVariable String subscriptionId, HttpServletRequest request) {
        subscriptions.delete(id(subscriptionId), caller(request));
        return ResponseEntity.noContent().build();
    }

    private static AuthenticatedToken caller(HttpServletRequest request) {
        return CurrentToken.of(request).map(CurrentToken::token).orElse(null);
    }

    private static UUID id(String text) {
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Push subscription", text);
        }
    }
}
