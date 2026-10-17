package ai.foresignal.collab;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.Ulids;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * A principal's notification preferences (prompt 130): which personal events reach which channel the principal owns.
 * Each preference owns one 018 channel definition (an ntfy topic, an email address, a Matrix room, a webhook…),
 * validated exactly as an instance channel (including the 035 outbound URL policy) and delivered by 018's worker,
 * with the channel's quiet hours. An email channel without recipients is addressed to the principal's own email.
 */
@Service
public class NotificationPreferenceService {

    /** The personal event types a preference may carry. */
    public static final List<String> EVENT_TYPES = List.of("mention", "share", "report", "chronicle", "assessment",
            "comment-reply");

    /** Most preferences per principal. */
    public static final int MAX_ITEMS = 20;

    private static final Set<String> ITEM_FIELDS = Set.of("id", "eventTypes", "channel", "quietHours", "enabled");
    private static final Set<String> CHANNEL_FIELDS = Set.of("type", "config", "secrets", "language");

    private final PrincipalNotificationRepository preferences;
    private final NotificationChannelRepository channels;
    private final ChannelService channelService;
    private final PrincipalAdminService principals;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public NotificationPreferenceService(PrincipalNotificationRepository preferences,
            NotificationChannelRepository channels, ChannelService channelService, PrincipalAdminService principals,
            JsonMapper json, Clock clock) {
        this.preferences = preferences;
        this.channels = channels;
        this.channelService = channelService;
        this.principals = principals;
        this.json = json;
        this.clock = clock;
    }

    /** The caller's preferences ({@code NotificationPreference} views), oldest first. */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String principalId) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (principalId == null) {
            return out;
        }
        for (PrincipalNotificationEntity p : preferences.findByPrincipalIdOrderByCreatedAtAscIdAsc(principalId)) {
            out.add(view(p));
        }
        return out;
    }

    /**
     * Replaces the caller's preferences ({@code replaceMyNotificationPreferences}). An item with the id of an existing
     * preference updates it (omitted secrets keep their stored values); existing preferences not listed are deleted
     * with their channels.
     */
    @Transactional
    public List<Map<String, Object>> replace(ObjectCaller caller, JsonNode body) {
        PrincipalEntity principal = principals.get(caller.principal());
        CollabInputs.object(body, "NotificationPreferencesInput", Set.of("items"));
        JsonNode list = body.get("items");
        if (list == null || !list.isArray()) {
            throw ValidationException.forField("items", "must be an array");
        }
        if (list.size() > MAX_ITEMS) {
            throw ValidationException.forField("items", "holds more than " + MAX_ITEMS + " preferences");
        }
        Map<String, PrincipalNotificationEntity> existing = new LinkedHashMap<>();
        for (PrincipalNotificationEntity p : preferences.findByPrincipalIdOrderByCreatedAtAscIdAsc(
                principal.getId())) {
            existing.put(p.getId(), p);
        }
        Set<String> kept = new HashSet<>();
        Instant now = clock.instant();
        int n = 0;
        for (JsonNode item : list) {
            n++;
            CollabInputs.object(item, "NotificationPreferenceInput", ITEM_FIELDS);
            List<String> types = CollabInputs.strings(item, "eventTypes", EVENT_TYPES.size());
            if (types.isEmpty()) {
                throw ValidationException.forField("items.eventTypes", "must hold at least one event type");
            }
            for (String t : types) {
                if (!EVENT_TYPES.contains(t)) {
                    throw ValidationException.forField("items.eventTypes", "must be among " + EVENT_TYPES);
                }
            }
            JsonNode channel = item.get("channel");
            CollabInputs.object(channel, "channel", CHANNEL_FIELDS);
            String id = CollabInputs.text(item, "id", 1, 26);
            PrincipalNotificationEntity pref = id == null ? null : existing.get(id);
            ObjectNode channelBody = channelBody(principal, channel, item.get("quietHours"),
                    CollabInputs.bool(item, "enabled", true), n);
            UUID channelId = channelService.savePersonal(pref == null ? null : pref.getChannelId(), channelBody,
                    principal.getId());
            if (pref == null) {
                pref = new PrincipalNotificationEntity(Ulids.next(now), principal.getId(), channelId, now);
            }
            pref.setEventTypes(types);
            pref.setLanguage(CollabInputs.text(channel, "language", 1, 35));
            JsonNode quiet = item.get("quietHours");
            pref.setQuietHours(quiet == null || quiet.isNull() ? null
                    : json.convertValue(quiet, new tools.jackson.core.type.TypeReference<Map<String, Object>>() {}));
            pref.setEnabled(CollabInputs.bool(item, "enabled", true));
            pref.setUpdatedAt(now);
            preferences.save(pref);
            kept.add(pref.getId());
        }
        for (PrincipalNotificationEntity old : existing.values()) {
            if (!kept.contains(old.getId())) {
                preferences.delete(old);
                if (channels.existsById(old.getChannelId())) {
                    channelService.delete(old.getChannelId());
                }
            }
        }
        return list(principal.getId());
    }

    private ObjectNode channelBody(PrincipalEntity principal, JsonNode channel, JsonNode quietHours, boolean enabled,
            int n) {
        ObjectNode out = json.createObjectNode();
        out.put("name", "personal:" + principal.getHandle() + ":" + n);
        String type = CollabInputs.required(channel, "type", 1, 32);
        out.put("type", type);
        JsonNode config = channel.get("config");
        ObjectNode cfg = config != null && config.isObject() ? ((ObjectNode) config).deepCopy()
                : json.createObjectNode();
        if ("email".equals(type) && !cfg.has("to") && principal.getEmail() != null) {
            ArrayNode to = cfg.putArray("to");
            to.add(principal.getEmail());
        }
        out.set("config", cfg);
        if (channel.has("secrets") && !channel.get("secrets").isNull()) {
            out.set("secrets", channel.get("secrets"));
        }
        if (quietHours != null && !quietHours.isNull()) {
            out.set("quietHours", quietHours);
        }
        if (principal.getTimezone() != null) {
            out.put("timezone", principal.getTimezone());
        }
        out.put("enabled", enabled);
        return out;
    }

    private Map<String, Object> view(PrincipalNotificationEntity p) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.getId());
        out.put("principalId", p.getPrincipalId());
        out.put("eventTypes", p.getEventTypes());
        Map<String, Object> channel = new LinkedHashMap<>();
        channel.put("channelId", p.getChannelId().toString());
        NotificationChannelEntity e = channels.findById(p.getChannelId()).orElse(null);
        channel.put("type", e == null ? null : e.getType());
        channel.put("config", e == null ? Map.of() : e.getConfig());
        channel.put("secretFields", e == null ? List.of() : new ArrayList<>(channelService.secrets(e).keySet()));
        channel.put("language", p.getLanguage());
        out.put("channel", channel);
        out.put("quietHours", p.getQuietHours());
        out.put("enabled", p.isEnabled());
        out.put("createdAt", p.getCreatedAt());
        out.put("updatedAt", p.getUpdatedAt());
        return out;
    }
}
