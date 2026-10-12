package ai.foresignal.api;

import ai.foresignal.api.dto.FeedKeyDto;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.security.feed.FeedKeyService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code listFeedKeys}, {@code createFeedKey} and {@code revokeFeedKey} (admin, prompt 035): path-bound
 * {@code fsf_} keys for clients that cannot send headers. Creation returns the key and one URL per
 * prefix, once; listings never carry the key.
 */
@RestController
@Tag(name = "Tokens")
public class FeedKeysController {

    private final FeedKeyService feedKeys;
    private final AppSettingService settings;

    /** Creates the controller. */
    public FeedKeysController(FeedKeyService feedKeys, AppSettingService settings) {
        this.feedKeys = feedKeys;
        this.settings = settings;
    }

    /** {@code listFeedKeys}: newest first, without values. */
    @GetMapping("/v1/feed-keys")
    @Operation(operationId = "listFeedKeys", tags = "Tokens")
    public List<FeedKeyDto> listFeedKeys() {
        return feedKeys.list().stream().map(FeedKeyDto::of).toList();
    }

    /** {@code createFeedKey}: 201 with the key and ready-made URLs, shown once. */
    @PostMapping("/v1/feed-keys")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createFeedKey", tags = "Tokens")
    public FeedKeyDto createFeedKey(@RequestBody JsonNode body, HttpServletRequest request) {
        FeedKeyService.CreatedKey created = feedKeys.create(body);
        List<String> prefixes = created.entity().getPrefixes();
        return FeedKeyDto.of(created.entity(), created.key(),
                FeedKeyService.urls(baseUrl(request), created.key(), prefixes));
    }

    /** {@code revokeFeedKey}: 204; the key answers 410 from then on. */
    @DeleteMapping("/v1/feed-keys/{feedKeyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "revokeFeedKey", tags = "Tokens")
    public void revokeFeedKey(@PathVariable String feedKeyId) {
        UUID id;
        try {
            id = UUID.fromString(feedKeyId);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Feed key", feedKeyId);
        }
        feedKeys.revoke(id);
    }

    /** {@code server.publicBaseUrl} when set, else the request's own origin. */
    private String baseUrl(HttpServletRequest request) {
        ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
        String configured = server.normalizedPublicBaseUrl();
        if (configured != null) {
            return configured;
        }
        return TilesController.requestBase(request.getScheme(), request.getServerName(), request.getServerPort());
    }
}
