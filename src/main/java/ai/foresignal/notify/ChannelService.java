package ai.foresignal.notify;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Notification channel management (prompt 018): the type catalog, create, full replace, delete and
 * the synchronous test, plus what the worker needs to render for a channel (decrypted secrets, the
 * effective timezone).
 *
 * <p>Secret settings are encrypted with {@link CredentialCipher} as one JSON object and never leave
 * this class except into a {@link ChannelSettings} for rendering. A PUT that omits {@code secrets}
 * keeps the stored ones; a provided object replaces them whole.
 */
@Service
public class ChannelService {

    /** The fallback zone (a region id, so times print as {@code UTC} rather than {@code Z}). */
    static final ZoneId UTC = ZoneId.of("UTC");

    /** The message a channel test sends. */
    static final String TEST_TITLE = "Foresignal test notification";

    private final NotificationChannelRepository channels;
    private final ChannelTypeRegistry types;
    private final ChannelInputParser parser;
    private final CredentialCipher cipher;
    private final AppSettingService settings;
    private final InstallIdService installId;
    private final ChannelSender sender;
    private final ChannelRateLimiter limiter;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    /** Publishes {@link ChannelChangedEvent} (prompt 060); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.context.ApplicationEventPublisher events;

    /** Creates the service. */
    public ChannelService(NotificationChannelRepository channels, ChannelTypeRegistry types, ChannelInputParser parser,
            CredentialCipher cipher, AppSettingService settings, InstallIdService installId, ChannelSender sender,
            ChannelRateLimiter limiter, NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.channels = channels;
        this.types = types;
        this.parser = parser;
        this.cipher = cipher;
        this.settings = settings;
        this.installId = installId;
        this.sender = sender;
        this.limiter = limiter;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * Delivery statistics of a channel.
     *
     * @param lastDeliveryAt the latest delivery, or null
     * @param failedLast24h deliveries that failed in the last 24 hours
     */
    public record Stats(Instant lastDeliveryAt, long failedLast24h) {}

    /**
     * A channel with its statistics.
     *
     * @param channel the channel
     * @param stats its statistics
     * @param ownerPrincipalId the owning user's principal, or null for a global channel (prompt R2-009)
     */
    public record View(NotificationChannel channel, Stats stats, String ownerPrincipalId) {

        /** A global channel's view (the shape before prompt R2-009). */
        public View(NotificationChannel channel, Stats stats) {
            this(channel, stats, null);
        }
    }

    /** Every type, ordered by id. */
    public List<ChannelType> types() {
        return types.all();
    }

    /** Every channel, oldest first. */
    @Transactional(readOnly = true)
    public List<View> list() {
        return list(ChannelScope.MACHINE);
    }

    /**
     * The channels {@code scope} may see, oldest first (prompt R2-009): a user's own and the global ones, or every
     * channel for an admin or an API token.
     *
     * @param scope the caller's channel scope
     * @return the channels with their statistics
     */
    @Transactional(readOnly = true)
    public List<View> list(ChannelScope scope) {
        List<NotificationChannelEntity> all = channels.findAllByOrderByCreatedAtAscIdAsc().stream()
                .filter(e -> scope.sees(e.getOwnerPrincipalId())).toList();
        Map<UUID, Stats> stats = stats(all.stream().map(NotificationChannelEntity::getId).toList());
        List<View> out = new ArrayList<>();
        for (NotificationChannelEntity e : all) {
            out.add(new View(view(e), stats.getOrDefault(e.getId(), new Stats(null, 0)), e.getOwnerPrincipalId()));
        }
        return out;
    }

    /** One channel, or 404. */
    @Transactional(readOnly = true)
    public View get(UUID id) {
        NotificationChannelEntity e = load(id);
        return new View(view(e), stats(List.of(id)).getOrDefault(id, new Stats(null, 0)), e.getOwnerPrincipalId());
    }

    /**
     * Creates the channel {@code id} of a built-in type if it is missing (prompt 031's Push channel),
     * enabled, with {@code events}; an existing row is left as the operator configured it.
     *
     * @return true when it was created
     */
    @Transactional
    public boolean ensureBuiltIn(UUID id, String name, String type, List<String> events) {
        if (channels.existsById(id)) {
            return false;
        }
        NotificationChannelEntity e = new NotificationChannelEntity(id, name, type);
        Instant now = clock.instant();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        e.setEvents(new ArrayList<>(events));
        e.setEnabled(true);
        channels.saveAndFlush(e);
        return true;
    }

    /** Creates a global channel from a {@code ChannelInput} body. */
    @Transactional
    public View create(JsonNode body) {
        return create(body, null);
    }

    /**
     * Creates a channel from a {@code ChannelInput} body, owned by {@code ownerPrincipalId} (prompt R2-009).
     *
     * @param body the channel
     * @param ownerPrincipalId the owning user's principal, or null for a global channel
     * @return the channel
     */
    @Transactional
    public View create(JsonNode body, String ownerPrincipalId) {
        ChannelInput input = parser.parse(body, null);
        NotificationChannelEntity e = new NotificationChannelEntity(UUID.randomUUID(), input.name(), input.type());
        Instant now = clock.instant();
        e.setCreatedAt(now);
        apply(e, input, Map.of(), now);
        e.setOwnerPrincipalId(ownerPrincipalId);
        channels.save(e);
        changed(e.getId(), e.getType(), false);
        return new View(view(e), new Stats(null, 0), ownerPrincipalId);
    }

    /** The event of every personal notification (prompt 130); instance fan-out never sends it. */
    public static final String PERSONAL_EVENT = "personal";

    /**
     * Creates or replaces a principal's personal channel (prompt 130) from a {@code ChannelInput} body: the type,
     * config, secrets, quiet hours and timezone are validated exactly as for an instance channel, then the channel is
     * bound to {@code principalId} and accepts only {@value #PERSONAL_EVENT} notifications, so the alert, briefing and
     * system fan-out never reaches it. Omitted secrets keep the stored ones on a replace.
     *
     * @param id the channel to replace, or null to create one
     * @param body the channel definition
     * @param principalId the owning principal
     * @return the channel id
     */
    @Transactional
    public UUID savePersonal(UUID id, JsonNode body, String principalId) {
        NotificationChannelEntity e = id == null ? null : channels.findById(id)
                .filter(c -> principalId.equals(c.getOwnerPrincipalId())).orElse(null);
        Map<String, String> stored = e == null ? Map.of() : secrets(e);
        ChannelInput input = parser.parse(body, e == null ? null : stored);
        Instant now = clock.instant();
        if (e == null) {
            e = new NotificationChannelEntity(UUID.randomUUID(), input.name(), input.type());
            e.setCreatedAt(now);
        }
        apply(e, input, stored, now);
        e.setEvents(new ArrayList<>(List.of(PERSONAL_EVENT)));
        e.setRuleIds(null);
        e.setOwnerPrincipalId(principalId);
        channels.save(e);
        limiter.forget(e.getId());
        changed(e.getId(), e.getType(), false);
        return e.getId();
    }

    /**
     * Changes the delivery settings of one personal channel {@code principalId} owns, leaving its definition alone
     * (prompt R2-012: the {@code UserPreferences} notification switches and timezone write through to the channels
     * the user's notification preferences already own, so there is one store).
     *
     * @param id the channel
     * @param principalId the owner; a channel owned by anyone else is left alone
     * @param enabled the new enabled flag, or null to keep it
     * @param setQuietHours true to replace the quiet hours with {@code quietHours}
     * @param quietHours the new quiet hours ({@code QuietHours}), or null for none
     * @param setTimezone true to replace the timezone with {@code timezone}
     * @param timezone the new IANA timezone, or null for the server's
     * @return true when the channel exists, belongs to {@code principalId} and was saved
     */
    @Transactional
    public boolean adjustPersonal(UUID id, String principalId, Boolean enabled, boolean setQuietHours,
            Map<String, Object> quietHours, boolean setTimezone, String timezone) {
        NotificationChannelEntity e = id == null || principalId == null ? null : channels.findById(id)
                .filter(c -> principalId.equals(c.getOwnerPrincipalId())).orElse(null);
        if (e == null) {
            return false;
        }
        if (enabled != null) {
            e.setEnabled(enabled);
        }
        if (setQuietHours) {
            e.setQuietHours(quietHours);
        }
        if (setTimezone) {
            e.setTimezone(timezone);
        }
        e.setUpdatedAt(clock.instant());
        channels.save(e);
        limiter.forget(e.getId());
        changed(e.getId(), e.getType(), false);
        return true;
    }

    /**
     * Creates the channel with {@code id}, or replaces it when it exists; a replaced channel keeps
     * its stored secrets when the body has none (settings import, prompt 019).
     */
    @Transactional
    public View upsert(UUID id, JsonNode body) {
        if (channels.existsById(id)) {
            return replace(id, body);
        }
        ChannelInput input = parser.parse(body, null);
        NotificationChannelEntity e = new NotificationChannelEntity(id, input.name(), input.type());
        Instant now = clock.instant();
        e.setCreatedAt(now);
        apply(e, input, Map.of(), now);
        channels.save(e);
        changed(e.getId(), e.getType(), false);
        return new View(view(e), new Stats(null, 0));
    }

    /** Replaces a channel whole; omitted {@code secrets} keep the stored ones. */
    @Transactional
    public View replace(UUID id, JsonNode body) {
        NotificationChannelEntity e = load(id);
        Map<String, String> stored = secrets(e);
        ChannelInput input = parser.parse(body, stored);
        Instant now = clock.instant();
        apply(e, input, stored, now);
        channels.save(e);
        limiter.forget(id);
        changed(id, e.getType(), false);
        // Quiet hours may have changed: let the worker re-evaluate deferred deliveries now.
        jdbc.update("""
                update notification_delivery set next_attempt_at = :now
                 where channel_id = :id and status = 'PENDING' and deferred_reason = 'quiet_hours'
                """, new MapSqlParameterSource("id", id).addValue("now", Timestamp.from(now)));
        return get(id);
    }

    /** Deletes a channel; its pending deliveries fail with "channel deleted". */
    @Transactional
    public void delete(UUID id) {
        NotificationChannelEntity e = load(id);
        jdbc.update("""
                update notification_delivery
                   set status = 'FAILED', last_error = 'channel deleted', next_attempt_at = null, leased_until = null
                 where channel_id = :id and status = 'PENDING'
                """, new MapSqlParameterSource("id", id));
        String type = e.getType();
        channels.delete(e);
        limiter.forget(id);
        changed(id, type, true);
    }

    private void changed(UUID id, String type, boolean deleted) {
        if (events != null) {
            events.publishEvent(new ChannelChangedEvent(id, type, deleted));
        }
    }

    /**
     * Sends a synthetic {@code test} message synchronously and persists nothing.
     *
     * @return the outcome (OpenAPI {@code ChannelTestResult})
     */
    public TestResult test(UUID id) {
        NotificationChannelEntity e = channels.findById(id).orElseThrow(() -> NotFoundException.of("Channel", id));
        Instant now = clock.instant();
        ChannelSettings s = settingsFor(e, 0, now);
        ChannelType type = types.get(e.getType());
        if (type.capabilities().fanOut()) {
            return new TestResult(false, null, 0, "The " + e.getName() + " channel fans out to its subscriptions and has"
                    + " no single destination to test; register a subscription and send an alert instead.");
        }
        NotificationMessage message = NotificationMessage.builder(NotificationMessage.TEST, "test", TEST_TITLE, "info")
                .body("If you can read this, the **" + MarkdownRenderer.escapeMarkdown(e.getName())
                        + "** channel works.")
                .origin(s.origin())
                .createdAt(now)
                .build();
        long started = System.nanoTime();
        try {
            PreparedRequest request = type.render(message, s);
            ChannelResponse response = sender.send(request);
            long ms = Duration.ofNanos(System.nanoTime() - started).toMillis();
            boolean ok = response.success() || type.partialSuccess(response);
            String error = ok ? null : Redactor.error("HTTP " + response.status() + ": " + response.body(),
                    s.secrets().values());
            // Prompt 060: an MQTT test reports the PUBACK latency and has no HTTP status.
            Integer status = request.body() instanceof PreparedRequest.Mqtt ? null : response.status();
            return new TestResult(ok, status, ms, error);
        } catch (IOException | RuntimeException ex) {
            long ms = Duration.ofNanos(System.nanoTime() - started).toMillis();
            return new TestResult(false, null, ms,
                    Redactor.error(ex.getClass().getSimpleName() + ": " + ex.getMessage(), s.secrets().values()));
        }
    }

    /**
     * The outcome of a channel test.
     *
     * @param success whether the destination accepted it
     * @param status the HTTP status, or null when there was none
     * @param durationMs how long it took
     * @param error the redacted error, or null
     */
    public record TestResult(boolean success, Integer status, long durationMs, String error) {}

    /** The stored channel, or 404. */
    public NotificationChannelEntity load(UUID id) {
        return channels.findById(id).orElseThrow(() -> NotFoundException.of("Channel", id));
    }

    /** The channel without secrets. */
    public NotificationChannel view(NotificationChannelEntity e) {
        return NotificationChannel.of(e, List.copyOf(secrets(e).keySet()));
    }

    /** Every enabled channel. */
    @Transactional(readOnly = true)
    public List<NotificationChannelEntity> enabled() {
        return channels.findByEnabledTrue();
    }

    /** The decrypted secrets of {@code e}; empty when none are stored. */
    public Map<String, String> secrets(NotificationChannelEntity e) {
        if (e.getSecretCiphertext() == null || e.getSecretNonce() == null) {
            return Map.of();
        }
        return new LinkedHashMap<>(cipher.decryptToMap(new Encrypted(e.getSecretCiphertext(), e.getSecretNonce())));
    }

    /** What a type needs to render one delivery of {@code e}. */
    public ChannelSettings settingsFor(NotificationChannelEntity e, long deliveryId, Instant now) {
        return new ChannelSettings(e.getId(), e.getName(), e.getConfig(), secrets(e), zone(e.getTimezone()),
                deliveryId, now, installId.installId(), installId.userAgent());
    }

    /** The channel timezone, else the {@code server} setting's timezone, else UTC. */
    public ZoneId zone(String channelTimezone) {
        ZoneId zone = parseZone(channelTimezone);
        if (zone != null) {
            return zone;
        }
        ZoneId server = parseZone(serverSettings().timezone());
        return server != null ? server : UTC;
    }

    /** The {@code server} setting. */
    public ServerSettings serverSettings() {
        try {
            return settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
        } catch (RuntimeException e) {
            return ServerSettings.defaults();
        }
    }

    private static ZoneId parseZone(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return ZoneId.of(text.strip());
        } catch (DateTimeException e) {
            return null;
        }
    }

    private void apply(NotificationChannelEntity e, ChannelInput input, Map<String, String> stored, Instant now) {
        e.setName(input.name());
        e.setType(input.type());
        e.setConfig(new LinkedHashMap<>(input.config()));
        Map<String, String> secrets = input.secrets() == null ? stored : input.secrets();
        if (secrets.isEmpty()) {
            e.setSecretCiphertext(null);
            e.setSecretNonce(null);
        } else {
            Encrypted encrypted = cipher.encrypt(new HashMap<>(secrets));
            e.setSecretCiphertext(encrypted.ciphertext());
            e.setSecretNonce(encrypted.nonce());
        }
        e.setEvents(new ArrayList<>(input.events()));
        e.setRuleIds(input.ruleIds() == null ? null : new ArrayList<>(input.ruleIds()));
        e.setMinSeverity(input.minSeverity());
        e.setTitleTemplate(input.titleTemplate());
        e.setBodyTemplate(input.bodyTemplate());
        e.setWaitForEnrichmentSeconds(input.waitForEnrichmentSeconds());
        e.setEnabled(input.enabled());
        e.setTimezone(input.timezone());
        e.setQuietHours(input.quietHours() == null ? null : input.quietHours().toMap());
        e.setGroupWindowSeconds(input.groupWindowSeconds());
        e.setBacklogCap(input.backlogCap());
        e.setEditInPlace(input.editInPlace());
        e.setDeferWhileActiveSeconds(input.deferWhileActiveSeconds());
        e.setAcceptExercise(input.acceptExercise());
        e.setLanguage(input.language());
        e.setUpdatedAt(now);
    }

    /** Last delivery and failures in the last 24 hours, per channel. */
    Map<UUID, Stats> stats(Collection<UUID> ids) {
        Map<UUID, Stats> out = new HashMap<>();
        if (ids.isEmpty()) {
            return out;
        }
        Instant since = clock.instant().minus(Duration.ofHours(24));
        jdbc.query("""
                select channel_id, max(delivered_at) as last_delivery,
                       count(*) filter (where status = :failed and created_at >= :since) as failed
                  from notification_delivery
                 where channel_id in (:ids)
                 group by channel_id
                """, new MapSqlParameterSource("ids", ids).addValue("failed", DeliveryStatus.FAILED.name())
                        .addValue("since", Timestamp.from(since)),
                rs -> {
                    Timestamp last = rs.getTimestamp("last_delivery");
                    out.put(rs.getObject("channel_id", UUID.class),
                            new Stats(last == null ? null : last.toInstant(), rs.getLong("failed")));
                });
        return out;
    }
}
