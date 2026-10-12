package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.ask.AskActionService;
import ai.foresignal.ask.AskCaller;
import ai.foresignal.ask.AskInput;
import ai.foresignal.ask.AskListener;
import ai.foresignal.ask.AskRequestParser;
import ai.foresignal.ask.AskService;
import ai.foresignal.ask.AskStreamService;
import ai.foresignal.ask.AskThreadService;
import ai.foresignal.ask.AskViews;
import ai.foresignal.mcp.McpCallerResolver;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.stream.sse.StreamPrincipalResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.JsonNode;

/**
 * The {@code Ask} operations (prompt 028, Architecture §21): {@code ask}, {@code askStream},
 * {@code listAskThreads}, {@code getAskThread}, {@code deleteAskThread}, {@code confirmAskAction}
 * and {@code rejectAskAction}.
 *
 * <p>Asking and reading threads need a {@code read} token and are always authenticated, because Ask
 * spends LLM budget (invariant SEC-5); deleting a thread and confirming or rejecting an action need
 * {@code agent} ({@code AccessPolicy}). A thread or action of another non-admin token is {@code 404}.
 * Ids that are not UUIDs are simply not found.
 */
@RestController
@Tag(name = "Ask")
public class AskController {

    private final AskService ask;
    private final AskStreamService streams;
    private final AskThreadService threads;
    private final AskActionService actions;
    private final McpCallerResolver callers;
    private final StreamPrincipalResolver principals;
    private final SourceRegistry registry;

    /** Creates the controller. */
    public AskController(AskService ask, AskStreamService streams, AskThreadService threads, AskActionService actions,
            McpCallerResolver callers, StreamPrincipalResolver principals, SourceRegistry registry) {
        this.ask = ask;
        this.streams = streams;
        this.threads = threads;
        this.actions = actions;
        this.callers = callers;
        this.principals = principals;
        this.registry = registry;
    }

    /** {@code ask}: answers a question; citations verified, warnings never stripped. */
    @PostMapping(value = "/v1/ask", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "ask", tags = "Ask")
    @ai.foresignal.security.restrict.RestrictedRead("the tool loop and retrieval run with the caller's restriction")
    public Map<String, Object> ask(@RequestBody JsonNode body, HttpServletRequest request) {
        AskCaller caller = caller(request);
        AskInput input = AskRequestParser.parse(body, registry, caller.excludedSources());
        return ask.ask(input, caller, AskListener.NONE);
    }

    /**
     * {@code askStream}: the same over Server-Sent Events. Everything checkable before the model runs
     * (body, configuration, scope, thread) is answered as a problem status, not a stream.
     */
    @PostMapping(value = "/v1/ask/stream", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "askStream", tags = "Ask")
    @ai.foresignal.security.restrict.RestrictedRead("the tool loop and retrieval run with the caller's restriction")
    public SseEmitter askStream(@RequestBody JsonNode body, HttpServletRequest request, HttpServletResponse response) {
        AskCaller caller = caller(request);
        AskInput input = AskRequestParser.parse(body, registry, caller.excludedSources());
        AskService.Prepared prepared = ask.prepare(input, caller);
        SseEmitter emitter = streams.open(prepared, principals.resolve(request));
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        return emitter;
    }

    /** {@code listAskThreads}: the caller's threads (every thread for admin), most recent first. */
    @GetMapping("/v1/ask/threads")
    @Operation(operationId = "listAskThreads", tags = "Ask")
    public Map<String, Object> listAskThreads(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        int size = QueryParams.intInRange("limit", limit, 1, AskThreadService.MAX_LIMIT, AskThreadService.DEFAULT_LIMIT);
        return threads.list(caller(request), cursor, size);
    }

    /** {@code getAskThread}: the thread with its messages, citations, warnings and actions. */
    @GetMapping("/v1/ask/threads/{threadId}")
    @Operation(operationId = "getAskThread", tags = "Ask")
    public Map<String, Object> getAskThread(@PathVariable String threadId, HttpServletRequest request) {
        return threads.get(id("Ask thread", threadId), caller(request));
    }

    /** {@code deleteAskThread}: 204; the thread, its messages and its actions. */
    @DeleteMapping("/v1/ask/threads/{threadId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteAskThread", tags = "Ask")
    public void deleteAskThread(@PathVariable String threadId, HttpServletRequest request) {
        threads.delete(id("Ask thread", threadId), caller(request));
    }

    /** {@code confirmAskAction}: executes a pending action through the same services as REST; audited. */
    @PostMapping("/v1/ask/actions/{actionId}/confirm")
    @Operation(operationId = "confirmAskAction", tags = "Ask")
    public Map<String, Object> confirmAskAction(@PathVariable String actionId, HttpServletRequest request) {
        return AskViews.action(actions.confirm(id("Ask action", actionId), caller(request)));
    }

    /** {@code rejectAskAction}: discards a pending action. */
    @PostMapping("/v1/ask/actions/{actionId}/reject")
    @Operation(operationId = "rejectAskAction", tags = "Ask")
    public Map<String, Object> rejectAskAction(@PathVariable String actionId, HttpServletRequest request) {
        return AskViews.action(actions.reject(id("Ask action", actionId), caller(request)));
    }

    private AskCaller caller(HttpServletRequest request) {
        return new AskCaller(callers.resolve(request));
    }

    private static UUID id(String what, String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of(what, value);
        }
    }
}
