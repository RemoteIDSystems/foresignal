package ai.foresignal.api.mapper;

import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.ChannelType;
import ai.foresignal.notify.NotificationChannel;
import ai.foresignal.store.NotificationDeliveryEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Maps channels, channel types and deliveries to the OpenAPI {@code Channel}, {@code ChannelType}
 * and {@code NotificationDelivery} schemas (prompt 018). Secret values never appear; a channel lists
 * the names of its set secrets in {@code secretFields}, and, when there are any, its type's non-fatal {@code warnings} (prompt 060).
 * A {@code telegram} or {@code matrix} channel also carries its {@code interactive} status (prompt 061).
 */
@Component
public class ChannelMapper {

    /** The types, for their {@code warnings} (prompt 060); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.notify.ChannelTypeRegistry types;

    /** The interactive chat supervisor (prompt 061); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.beans.factory.ObjectProvider<ai.foresignal.chatops.ChatopsManager> chatops;

    /** The channel rows, for the interactive status (prompt 061); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.store.NotificationChannelRepository channelRows;

    /** {@code Channel}. */
    public Map<String, Object> channel(ChannelService.View view) {
        NotificationChannel c = view.channel();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", c.id().toString());
        out.put("name", c.name());
        out.put("type", c.type());
        out.put("config", c.config());
        out.put("secretFields", c.secretFields().stream().sorted().toList());
        out.put("events", c.events());
        out.put("ruleIds", c.ruleIds() == null ? null : c.ruleIds().stream().map(Object::toString).toList());
        out.put("minSeverity", c.minSeverity());
        out.put("titleTemplate", c.titleTemplate());
        out.put("bodyTemplate", c.bodyTemplate());
        out.put("waitForEnrichmentSeconds", c.waitForEnrichmentSeconds());
        out.put("enabled", c.enabled());
        out.put("createdAt", time(c.createdAt()));
        out.put("updatedAt", time(c.updatedAt()));
        out.put("lastDeliveryAt", time(view.stats().lastDeliveryAt()));
        out.put("failedLast24h", view.stats().failedLast24h());
        out.put("timezone", c.timezone());
        out.put("quietHours", c.quietHours() == null ? null : c.quietHours().toMap());
        out.put("groupWindowSeconds", c.groupWindowSeconds());
        out.put("backlogCap", c.backlogCap());
        out.put("editInPlace", c.editInPlace());
        out.put("deferWhileActiveSeconds", c.deferWhileActiveSeconds());
        out.put("acceptExercise", c.acceptExercise());
        out.put("language", c.language());
        // R2-009: a personal channel names its owner; a global one has none.
        out.put("ownerPrincipalId", view.ownerPrincipalId());
        out.put("global", view.ownerPrincipalId() == null);
        List<Map<String, Object>> warnings = warnings(c);
        if (!warnings.isEmpty()) {
            out.put("warnings", warnings);
        }
        Map<String, Object> interactive = interactive(c);
        if (interactive != null) {
            out.put("interactive", interactive);
        }
        return out;
    }

    /**
     * {@code Channel.interactive} (prompt 061): the {@code ChannelInteractiveStatus} of a {@code telegram} or
     * {@code matrix} channel; null for other types.
     */
    Map<String, Object> interactive(NotificationChannel c) {
        if (chatops == null || channelRows == null || !ai.foresignal.chatops.InteractiveConfig.supports(c.type())) {
            return null;
        }
        ai.foresignal.chatops.ChatopsManager manager = chatops.getIfAvailable();
        if (manager == null) {
            return null;
        }
        return channelRows.findById(c.id()).map(manager::status).orElse(null);
    }

    /** The type's non-fatal warnings about {@code c} (prompt 060: {@code MQTT_INSECURE_PUBLIC_BROKER}). */
    List<Map<String, Object>> warnings(NotificationChannel c) {
        if (types == null) {
            return List.of();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        types.find(c.type()).ifPresent(type -> {
            for (ai.foresignal.notify.ChannelWarning w : type.warnings(c.config() == null ? Map.of() : c.config(),
                    c.secretFields())) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("code", w.code());
                item.put("message", w.message());
                out.add(item);
            }
        });
        return out;
    }

    /** {@code ChannelType}. */
    public Map<String, Object> type(ChannelType type) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", type.id());
        out.put("name", type.spec().name());
        out.put("description", type.spec().description());
        out.put("configFields", fields(type.spec().configFields()));
        out.put("secretFields", fields(type.spec().secretFields()));
        out.put("events", type.spec().events());
        Map<String, Object> capabilities = new LinkedHashMap<>();
        capabilities.put("editInPlace", type.capabilities().editInPlace());
        capabilities.put("attachments", type.capabilities().attachments());
        capabilities.put("fanOut", type.capabilities().fanOut());
        out.put("capabilities", capabilities);
        return out;
    }

    private static List<Map<String, Object>> fields(List<ChannelFieldSpec> specs) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ChannelFieldSpec f : specs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", f.name());
            item.put("type", f.type());
            item.put("required", f.required());
            item.put("description", f.description());
            if (f.defaultValue() != null) {
                item.put("default", f.defaultValue());
            }
            if (!f.allowed().isEmpty()) {
                item.put("allowed", f.allowed());
            }
            out.add(item);
        }
        return out;
    }

    /** {@code NotificationDelivery}. */
    public Map<String, Object> delivery(NotificationDeliveryEntity d) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", d.getId());
        out.put("event", d.getEvent());
        out.put("alertId", d.getAlertId());
        out.put("briefingRunId", d.getBriefingRunId() == null ? null : d.getBriefingRunId().toString());
        out.put("status", d.getStatus().wire());
        out.put("attempts", d.getAttempts());
        out.put("lastError", d.getLastError());
        out.put("nextAttemptAt", time(d.getNextAttemptAt()));
        out.put("deliveredAt", time(d.getDeliveredAt()));
        out.put("notificationId", d.getNotificationId());
        out.put("kind", d.getKind());
        out.put("deferredReason", d.getDeferredReason());
        out.put("edited", d.isEdited());
        out.put("note", d.getNote());
        out.put("escalationStep", d.getEscalationStep());
        out.put("attachmentLevel", d.getAttachmentLevel());
        out.put("attachments", d.getAttachments() == null ? List.of() : d.getAttachments());
        return out;
    }

    private static String time(Instant value) {
        return value == null ? null : value.toString();
    }
}
