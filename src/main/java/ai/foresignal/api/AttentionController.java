package ai.foresignal.api;

import ai.foresignal.api.support.QueryParams;
import ai.foresignal.attention.AttentionTopicEntity;
import ai.foresignal.attention.AttentionTopicInput;
import ai.foresignal.attention.AttentionTopicService;
import ai.foresignal.security.CurrentToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Attention topics (OpenAPI tag {@code Attention}, prompt 117): reads are {@code read} scope, changes {@code agent}.
 * Enabled topics drive {@code gdelt-timelines} and {@code wikimedia-pageviews}; every change re-syncs those sources'
 * {@code topics} parameter. Topics are configuration, not records.
 */
@RestController
@Tag(name = "Attention")
@RequestMapping("/v1/attention/topics")
public class AttentionController {

    private final AttentionTopicService topics;

    /** Creates the controller. */
    public AttentionController(AttentionTopicService topics) {
        this.topics = topics;
    }

    /** {@code listAttentionTopics}. */
    @GetMapping
    @Operation(operationId = "listAttentionTopics", tags = "Attention")
    public Map<String, Object> listAttentionTopics(
            @RequestParam(required = false) String enabled,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        Boolean filter = enabled == null ? null : QueryParams.bool("enabled", enabled, false);
        int size = QueryParams.intInRange("limit", limit, 1, AttentionTopicService.MAX_LIMIT,
                AttentionTopicService.DEFAULT_LIMIT);
        AttentionTopicService.Page page = topics.list(filter, cursor, size);
        List<Map<String, Object>> items = page.items().stream().map(AttentionTopicService::view).toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code getAttentionTopic}. */
    @GetMapping("/{topicId}")
    @Operation(operationId = "getAttentionTopic", tags = "Attention")
    public Map<String, Object> getAttentionTopic(@PathVariable String topicId) {
        return AttentionTopicService.view(topics.get(topicId));
    }

    /** {@code createAttentionTopic}: 201 with the topic. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createAttentionTopic", tags = "Attention")
    public Map<String, Object> createAttentionTopic(@RequestBody(required = false) AttentionTopicInput body,
            HttpServletRequest request) {
        String tokenId = CurrentToken.of(request).map(c -> c.id().toString()).orElse(null);
        AttentionTopicEntity topic = topics.create(body, tokenId);
        return AttentionTopicService.view(topic);
    }

    /** {@code updateAttentionTopic}. */
    @PutMapping("/{topicId}")
    @Operation(operationId = "updateAttentionTopic", tags = "Attention")
    public Map<String, Object> updateAttentionTopic(@PathVariable String topicId,
            @RequestBody(required = false) AttentionTopicInput body) {
        return AttentionTopicService.view(topics.update(topicId, body));
    }

    /** {@code deleteAttentionTopic}: 204. */
    @DeleteMapping("/{topicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteAttentionTopic", tags = "Attention")
    public void deleteAttentionTopic(@PathVariable String topicId) {
        topics.delete(topicId);
    }
}
