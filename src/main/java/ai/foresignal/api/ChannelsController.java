package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.mapper.ChannelMapper;
import ai.foresignal.api.support.Cursor;
import ai.foresignal.api.support.CursorCodec;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The {@code Channels} operations (prompt 018): {@code listChannelTypes}, {@code listChannels},
 * {@code createChannel}, {@code getChannel}, {@code updateChannel}, {@code deleteChannel},
 * {@code testChannel} and {@code listChannelDeliveries}. Every operation needs {@code admin}
 * ({@code /v1/channels/**} in {@code AccessPolicy}); responses never include secret values.
 */
@RestController
@Tag(name = "Channels")
@RequestMapping("/v1/channels")
public class ChannelsController {

    /** The largest deliveries page. */
    static final int MAX_LIMIT = 5_000;

    /** The default deliveries page. */
    static final int DEFAULT_LIMIT = 500;

    private final ChannelService channels;
    private final ChannelMapper mapper;
    private final NotificationDeliveryRepository deliveries;

    /** Creates the controller. */
    public ChannelsController(ChannelService channels, ChannelMapper mapper, NotificationDeliveryRepository deliveries) {
        this.channels = channels;
        this.mapper = mapper;
        this.deliveries = deliveries;
    }

    /** {@code listChannelTypes}. */
    @GetMapping("/types")
    @Operation(operationId = "listChannelTypes", tags = "Channels")
    public List<Map<String, Object>> listChannelTypes() {
        return channels.types().stream().map(mapper::type).toList();
    }

    /** {@code listChannels}: oldest first. */
    @GetMapping
    @Operation(operationId = "listChannels", tags = "Channels")
    public List<Map<String, Object>> listChannels() {
        return channels.list().stream().map(mapper::channel).toList();
    }

    /** {@code createChannel}: 201 with {@code Location}. */
    @PostMapping
    @Operation(operationId = "createChannel", tags = "Channels")
    public ResponseEntity<Map<String, Object>> createChannel(@RequestBody JsonNode body) {
        ChannelService.View view = channels.create(body);
        return ResponseEntity.created(URI.create("/v1/channels/" + view.channel().id())).body(mapper.channel(view));
    }

    /** {@code getChannel}. */
    @GetMapping("/{channelId}")
    @Operation(operationId = "getChannel", tags = "Channels")
    public Map<String, Object> getChannel(@PathVariable String channelId) {
        return mapper.channel(channels.get(id(channelId)));
    }

    /** {@code updateChannel}: a full replace; omitted {@code secrets} keep the stored ones. */
    @PutMapping("/{channelId}")
    @Operation(operationId = "updateChannel", tags = "Channels")
    public Map<String, Object> updateChannel(@PathVariable String channelId, @RequestBody JsonNode body) {
        return mapper.channel(channels.replace(id(channelId), body));
    }

    /** {@code deleteChannel}: 204; pending deliveries fail with "channel deleted". */
    @DeleteMapping("/{channelId}")
    @Operation(operationId = "deleteChannel", tags = "Channels")
    public ResponseEntity<Void> deleteChannel(@PathVariable String channelId) {
        channels.delete(id(channelId));
        return ResponseEntity.noContent().build();
    }

    /** {@code testChannel}: synchronous; persists nothing. */
    @PostMapping("/{channelId}/test")
    @Operation(operationId = "testChannel", tags = "Channels")
    public Map<String, Object> testChannel(@PathVariable String channelId) {
        ChannelService.TestResult result = channels.test(id(channelId));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", result.success());
        out.put("status", result.status());
        out.put("durationMs", result.durationMs());
        out.put("error", result.error());
        return out;
    }

    /** {@code listChannelDeliveries}: newest first, keyset on id, optional {@code status}. */
    @GetMapping("/{channelId}/deliveries")
    @Operation(operationId = "listChannelDeliveries", tags = "Channels")
    public Map<String, Object> listChannelDeliveries(
            @PathVariable String channelId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        UUID id = id(channelId);
        channels.load(id);
        String wire = status == null ? null : QueryParams.oneOf("status", status, List.of("pending", "delivered", "failed"), null);
        List<DeliveryStatus> statuses = wire == null ? Arrays.asList(DeliveryStatus.values())
                : List.of(DeliveryStatus.fromWire(wire));
        int size = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT);
        String fingerprint = CursorCodec.fingerprint("deliveries|" + id + "|" + wire + "|" + size);
        Cursor.Seq position = CursorCodec.decode(cursor, Cursor.Seq.class, fingerprint);
        List<NotificationDeliveryEntity> page = deliveries.page(id, position == null ? Long.MAX_VALUE : position.q(),
                statuses, Limit.of(size + 1));
        String next = null;
        if (page.size() > size) {
            page = page.subList(0, size);
            next = CursorCodec.encode(new Cursor.Seq(page.getLast().getId(), null), fingerprint);
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (NotificationDeliveryEntity d : page) {
            items.add(mapper.delivery(d));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", next);
        return out;
    }

    static UUID id(String channelId) {
        try {
            return UUID.fromString(channelId);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Channel", channelId);
        }
    }
}
