package ai.foresignal.settings;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.ChannelMapper;
import ai.foresignal.api.mapper.SourceMapper;
import ai.foresignal.api.mapper.TriggerMapper;
import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.NotificationSettingsService;
import ai.foresignal.retention.RetentionSettingsService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code exportSettings} and {@code importSettings} (prompt 019): the {@code SettingsBundle}.
 *
 * <p><strong>Export</strong> carries source settings (never credentials), retention (with
 * {@code kinds} and the per-source view), CORS, notification settings, the watchdog, every trigger
 * rule (with its id) and every channel without its secrets, plus {@code exportedAt} and
 * {@code foresignalVersion}, and (prompt 023) {@code security} and {@code server}. Tokens, tickets
 * and pairing codes are never exported.
 *
 * <p><strong>Import</strong> runs in one transaction. Known sources are applied and unknown ones
 * listed in {@code sourcesUnknown}; enabling a source whose required credentials are missing is
 * applied as {@code enabled=false} with a warning. Triggers and channels are upserted by id (a
 * channel without secrets keeps the ones it has). Retention, CORS, notifications, the watchdog,
 * security and server are applied when present. Any validation error is a 400 listing every error, and nothing is
 * applied (the transaction rolls back).
 */
@Service
public class SettingsTransferService {

    /** Sections handled by this version. */
    static final Set<String> HANDLED = Set.of("exportedAt", "foresignalVersion", "sources", "retention", "cors",
            "notifications", "watchdog", "triggers", "channels", "security", "server");

    /** Channel members that are output only, dropped before a channel is upserted. */
    static final Set<String> CHANNEL_OUTPUT_ONLY = Set.of("id", "secretFields", "createdAt", "updatedAt",
            "lastDeliveryAt", "failedLast24h");

    private final SourceRegistry registry;
    private final SourceService sources;
    private final SourceMapper sourceMapper;
    private final RetentionSettingsService retention;
    private final CorsSettingsService cors;
    private final NotificationSettingsService notifications;
    private final WatchdogSettingsService watchdog;
    private final TriggerRuleService triggers;
    private final TriggerMapper triggerMapper;
    private final ChannelService channels;
    private final ChannelMapper channelMapper;
    private final ForesignalVersion version;
    private final SecuritySettingsService security;
    private final ServerSettingsService server;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public SettingsTransferService(SourceRegistry registry, SourceService sources, SourceMapper sourceMapper,
            RetentionSettingsService retention, CorsSettingsService cors, NotificationSettingsService notifications,
            WatchdogSettingsService watchdog, TriggerRuleService triggers, TriggerMapper triggerMapper,
            ChannelService channels, ChannelMapper channelMapper, ForesignalVersion version,
            SecuritySettingsService security, ServerSettingsService server, JsonMapper json, Clock clock) {
        this.security = security;
        this.server = server;
        this.registry = registry;
        this.sources = sources;
        this.sourceMapper = sourceMapper;
        this.retention = retention;
        this.cors = cors;
        this.notifications = notifications;
        this.watchdog = watchdog;
        this.triggers = triggers;
        this.triggerMapper = triggerMapper;
        this.channels = channels;
        this.channelMapper = channelMapper;
        this.version = version;
        this.json = json;
        this.clock = clock;
    }

    /**
     * What an import did.
     *
     * @param sourcesApplied source ids whose settings were applied, in bundle order
     * @param sourcesUnknown source ids that are not registered here
     * @param triggersUpserted rules created or replaced
     * @param channelsUpserted channels created or replaced
     * @param warnings human-readable warnings
     */
    public record ImportReport(List<String> sourcesApplied, List<String> sourcesUnknown, int triggersUpserted,
            int channelsUpserted, List<String> warnings) {}

    /** The bundle of every exportable setting; never a secret or a credential. */
    @Transactional(readOnly = true)
    public Map<String, Object> export() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("exportedAt", clock.instant().toString());
        out.put("foresignalVersion", version.version());
        Map<String, Object> sourceSettings = new LinkedHashMap<>();
        for (SourceView view : sources.list()) {
            sourceSettings.put(view.id(), source(view));
        }
        out.put("sources", sourceSettings);
        out.put("retention", retention.get());
        out.put("cors", cors.get());
        out.put("notifications", NotificationSettingsService.body(notifications.get()));
        out.put("watchdog", watchdog.get());
        out.put("security", security.get());
        out.put("server", server.get());
        out.put("triggers", triggers.list(null, null).stream().map(triggerMapper::rule).toList());
        List<Map<String, Object>> channelList = new ArrayList<>();
        for (ChannelService.View view : channels.list()) {
            Map<String, Object> channel = new LinkedHashMap<>(channelMapper.channel(view));
            String id = (String) channel.get("id");
            CHANNEL_OUTPUT_ONLY.forEach(channel::remove);
            Map<String, Object> input = new LinkedHashMap<>();
            input.put("id", id);
            input.putAll(channel);
            channelList.add(input);
        }
        out.put("channels", channelList);
        return out;
    }

    private Map<String, Object> source(SourceView view) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", view.enabled());
        Duration interval = view.interval();
        if (interval != null && !interval.isZero() && !interval.isNegative()) {
            out.put("interval", Durations.format(interval));
        }
        out.put("params", view.params());
        if (view.budget() != null && view.budget().perMinute() >= 1 && view.budget().perDay() >= 1) {
            Map<String, Object> budget = new LinkedHashMap<>();
            budget.put("perMinute", view.budget().perMinute());
            budget.put("perDay", view.budget().perDay());
            out.put("budget", budget);
        }
        sources.retentionOverride(view.id()).ifPresent(r -> out.put("retention", Durations.format(r)));
        sources.ingestPolicyOverride(view.id()).ifPresent(p -> out.put("ingestPolicy", sourceMapper.policy(p)));
        return out;
    }

    /**
     * Applies {@code bundle} atomically.
     *
     * @throws ValidationException listing every error; nothing is applied
     */
    @Transactional
    public ImportReport importBundle(JsonNode bundle) {
        if (bundle == null || !bundle.isObject()) {
            throw new ValidationException("The request body must be a SettingsBundle object.");
        }
        Errors errors = new Errors();
        List<String> warnings = new ArrayList<>();
        List<String> applied = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        int triggerCount = 0;
        int channelCount = 0;

        for (String field : bundle.propertyNames()) {
            if (!HANDLED.contains(field)) {
                warnings.add("'" + field + "' is not imported by this version and was ignored");
            }
        }

        JsonNode sourcesNode = bundle.get("sources");
        if (sourcesNode == null || !sourcesNode.isObject()) {
            errors.add(new FieldError("sources", "is required and must be an object of source id to settings"));
        } else {
            for (String id : sourcesNode.propertyNames()) {
                if (!registry.contains(id)) {
                    unknown.add(id);
                    continue;
                }
                try {
                    SourceDescriptor descriptor = registry.descriptor(id);
                    var body = json.treeToValue(sourcesNode.get(id), ai.foresignal.api.dto.SourceSettingsUpdate.class);
                    SourceSettingsUpdate update = sourceMapper.update(body, descriptor);
                    if (Boolean.TRUE.equals(update.enabled()) && descriptor.requiresCredentials()
                            && !sources.credentialsSet(id)) {
                        update = update.withEnabled(false);
                        warnings.add("Source " + id + " was imported disabled: its required credentials are not set."
                                + " Set them with PUT /v1/sources/" + id + "/credentials, then enable it.");
                    }
                    sources.update(id, update);
                    applied.add(id);
                } catch (RuntimeException e) {
                    errors.add("sources." + id, e);
                }
            }
        }

        JsonNode retentionNode = present(bundle, "retention");
        if (retentionNode != null) {
            try {
                retention.apply(retention.parse(retentionNode));
            } catch (RuntimeException e) {
                errors.add("retention", e);
            }
        }
        JsonNode corsNode = present(bundle, "cors");
        if (corsNode != null) {
            try {
                cors.update(json.treeToValue(corsNode, CorsSettings.class));
            } catch (RuntimeException e) {
                errors.add("cors", e);
            }
        }
        JsonNode notificationsNode = present(bundle, "notifications");
        if (notificationsNode != null) {
            try {
                notifications.apply(NotificationSettingsService.parse(notificationsNode));
            } catch (RuntimeException e) {
                errors.add("notifications", e);
            }
        }
        JsonNode watchdogNode = present(bundle, "watchdog");
        if (watchdogNode != null) {
            try {
                watchdog.put(WatchdogSettingsService.parse(watchdogNode));
            } catch (RuntimeException e) {
                errors.add("watchdog", e);
            }
        }
        JsonNode securityNode = present(bundle, "security");
        if (securityNode != null) {
            try {
                security.apply(SecuritySettingsService.parse(securityNode));
            } catch (RuntimeException e) {
                errors.add("security", e);
            }
        }
        JsonNode serverNode = present(bundle, "server");
        if (serverNode != null) {
            try {
                JsonNode serverInput = serverNode;
                if (serverNode.isObject() && !serverNode.has("publicBaseUrl")) {
                    // A bundle written with nulls left out still means "no public base URL".
                    serverInput = ((ObjectNode) serverNode).deepCopy().putNull("publicBaseUrl");
                }
                server.apply(ServerSettingsService.parse(serverInput));
            } catch (RuntimeException e) {
                errors.add("server", e);
            }
        }
        JsonNode triggersNode = present(bundle, "triggers");
        if (triggersNode != null) {
            if (!triggersNode.isArray()) {
                errors.add(new FieldError("triggers", "must be an array of trigger rules"));
            } else {
                for (int i = 0; i < triggersNode.size(); i++) {
                    JsonNode rule = triggersNode.get(i);
                    UUID id = id(rule, "triggers[" + i + "]", errors);
                    if (id == null) {
                        continue;
                    }
                    try {
                        triggers.upsert(id, rule);
                        triggerCount++;
                    } catch (RuntimeException e) {
                        errors.add("triggers[" + i + "]", e);
                    }
                }
            }
        }
        JsonNode channelsNode = present(bundle, "channels");
        if (channelsNode != null) {
            if (!channelsNode.isArray()) {
                errors.add(new FieldError("channels", "must be an array of channels"));
            } else {
                for (int i = 0; i < channelsNode.size(); i++) {
                    JsonNode channel = channelsNode.get(i);
                    UUID id = id(channel, "channels[" + i + "]", errors);
                    if (id == null) {
                        continue;
                    }
                    ObjectNode input = ((ObjectNode) channel).deepCopy();
                    CHANNEL_OUTPUT_ONLY.forEach(input::remove);
                    try {
                        channels.upsert(id, input);
                        channelCount++;
                    } catch (RuntimeException e) {
                        errors.add("channels[" + i + "]", e);
                    }
                }
            }
        }

        if (!errors.list.isEmpty()) {
            throw new ValidationException(errors.code(), "The settings bundle is not valid; nothing was applied.",
                    errors.list);
        }
        return new ImportReport(applied, unknown, triggerCount, channelCount, warnings);
    }

    private static JsonNode present(JsonNode bundle, String field) {
        JsonNode node = bundle.get(field);
        return node == null || node.isNull() ? null : node;
    }

    private static UUID id(JsonNode item, String path, Errors errors) {
        if (item == null || !item.isObject()) {
            errors.add(new FieldError(path, "must be an object"));
            return null;
        }
        JsonNode id = item.get("id");
        if (id == null || !id.isString()) {
            errors.add(new FieldError(path + ".id", "is required to upsert by id"));
            return null;
        }
        try {
            return UUID.fromString(id.asString());
        } catch (IllegalArgumentException e) {
            errors.add(new FieldError(path + ".id", "must be a UUID"));
            return null;
        }
    }

    /** Collected errors, prefixed with the bundle path they came from. */
    private static final class Errors {

        private final List<FieldError> list = new ArrayList<>();
        private boolean tierFixed;
        private boolean policyInvalid;

        void add(FieldError error) {
            list.add(error);
        }

        void add(String prefix, RuntimeException e) {
            if (e instanceof ValidationException v) {
                tierFixed |= ErrorCodes.RETENTION_TIER_FIXED.equals(v.getCode());
                policyInvalid |= ErrorCodes.INGEST_POLICY_INVALID.equals(v.getCode());
                if (v.getErrors().isEmpty()) {
                    list.add(new FieldError(prefix, v.getMessage()));
                }
                for (FieldError error : v.getErrors()) {
                    list.add(new FieldError(prefix + "." + error.field(), error.message()));
                }
                return;
            }
            if (e instanceof ForesignalException f) {
                tierFixed |= ErrorCodes.RETENTION_TIER_FIXED.equals(f.getCode());
                list.add(new FieldError(prefix, f.getMessage()));
                return;
            }
            list.add(new FieldError(prefix, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }

        String code() {
            if (tierFixed) {
                return ErrorCodes.RETENTION_TIER_FIXED;
            }
            return policyInvalid ? ErrorCodes.INGEST_POLICY_INVALID : ErrorCodes.VALIDATION_FAILED;
        }
    }
}
