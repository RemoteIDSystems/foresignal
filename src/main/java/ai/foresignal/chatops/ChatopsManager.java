package ai.foresignal.chatops;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.notify.ChannelChangedEvent;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.NotificationSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.json.JsonMapper;

/**
 * Supervises the connectors of interactive channels (prompt 061).
 *
 * <p>A channel is served while it is enabled, of type {@code telegram} or {@code matrix}, has
 * {@code interactive: true}, and the global kill switch {@code NotificationSettings.chatops.enabled} is on. Each
 * served channel gets one {@link ChatConnector} on its own virtual thread, on the pattern of the 010 streaming
 * supervisor: {@link ChatConnector#start} once, then {@link ChatConnector#poll} in a loop.
 *
 * <ul>
 *   <li><b>Backoff:</b> after a failure, 1 s doubling to 5 min; a blocked connector (a foreign Telegram
 *       webhook) waits the longest backoff and starts again.
 *   <li><b>Credentials refused</b> (401/403): the connector stops, a {@code system.chatops_auth_failed} system
 *       event is raised and its health is {@code failing} until the channel is saved again.
 *   <li><b>Sync:</b> at startup, every {@code reconcileInterval}, and after a {@link ChannelChangedEvent}; a
 *       changed config or secret restarts the connector.
 *   <li><b>Commands</b> go to the {@link CommandRouter}, each on its own virtual thread ({@code asyncCommands}),
 *       so a slow {@code /ask} never holds up the poll. The kill switch is checked again for every command.
 * </ul>
 */
@Component
public class ChatopsManager {

    private static final Logger log = LoggerFactory.getLogger(ChatopsManager.class);

    /** The system-event subtype raised when a platform refuses the connector's credentials. */
    public static final String AUTH_FAILED_EVENT = "system.chatops_auth_failed";

    private final NotificationChannelRepository channels;
    private final ChannelService channelService;
    private final AppSettingService settings;
    private final ChatConnectorFactory factory;
    private final CommandRouter router;
    private final DiscoveryMode discovery;
    private final PendingTokens pending;
    private final ChatopsCursorStore cursors;
    private final ApplicationEventPublisher events;
    private final ChatopsProperties properties;
    private final JsonMapper json;
    private final Clock clock;
    private final Map<UUID, Runner> runners = new ConcurrentHashMap<>();
    private final ExecutorService commands = Executors.newVirtualThreadPerTaskExecutor();
    private volatile boolean shutdown;

    /** Creates the manager. */
    public ChatopsManager(NotificationChannelRepository channels, ChannelService channelService,
            AppSettingService settings, ChatConnectorFactory factory, CommandRouter router, DiscoveryMode discovery,
            PendingTokens pending, ChatopsCursorStore cursors, ApplicationEventPublisher events,
            ChatopsProperties properties, JsonMapper json, Clock clock) {
        this.channels = channels;
        this.channelService = channelService;
        this.settings = settings;
        this.factory = factory;
        this.router = router;
        this.discovery = discovery;
        this.pending = pending;
        this.cursors = cursors;
        this.events = events;
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    /** One served channel. */
    private static final class Runner {
        final ChannelConfig config;
        final String fingerprint;
        final ChatConnector connector;
        volatile Thread thread;
        volatile boolean stopped;

        Runner(ChannelConfig config, String fingerprint, ChatConnector connector) {
            this.config = config;
            this.fingerprint = fingerprint;
            this.connector = connector;
        }
    }

    /** True while the global kill switch allows interactive channels. */
    public boolean chatopsEnabled() {
        return settings.getOrDefault(SettingKeys.NOTIFICATIONS, NotificationSettings.class, NotificationSettings::defaults)
                .chatops().on();
    }

    /**
     * True when {@code channel} is to be served: enabled, interactive, of a supported type, kill switch on. The
     * prompt 033 Telegram poller skips such channels, so only one consumer calls {@code getUpdates} per bot.
     */
    public static boolean served(NotificationChannelEntity channel, boolean chatopsEnabled) {
        return chatopsEnabled && channel != null && channel.isEnabled() && InteractiveConfig.supports(channel.getType())
                && InteractiveConfig.enabledIn(channel.getConfig());
    }

    /** Starts the connectors at startup. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.CHATOPS)
    public void onReady() {
        if (properties.scheduled()) {
            try {
                reconcile();
            } catch (RuntimeException e) {
                log.error("Interactive chat channels could not be started", e);
            }
        }
    }

    /** Reconciles the connectors with the stored channels and purges old tokens. */
    @Scheduled(fixedDelayString = "${foresignal.chatops.reconcile-interval:30s}", initialDelayString = "10s")
    public void scheduledReconcile() {
        if (!properties.scheduled() || shutdown) {
            return;
        }
        try {
            reconcile();
            pending.purge();
        } catch (RuntimeException e) {
            log.warn("Reconciling interactive chat channels failed: {}", e.toString());
        }
    }

    /** Follows a created, replaced or deleted channel once its transaction committed. */
    @TransactionalEventListener(fallbackExecution = true)
    public void onChannelChanged(ChannelChangedEvent event) {
        if (!InteractiveConfig.supports(event.type()) && !runners.containsKey(event.channelId())) {
            return;
        }
        try {
            if (event.deleted()) {
                cursors.delete(event.channelId());
            } else {
                discovery.reset(event.channelId());
            }
            if (properties.scheduled()) {
                sync(event.channelId());
            }
        } catch (RuntimeException e) {
            log.warn("Interactive channel {} could not be synchronized: {}", event.channelId(), e.toString());
        }
    }

    /** Starts, restarts or stops every connector to match the stored channels. */
    public synchronized void reconcile() {
        Set<UUID> seen = new HashSet<>();
        for (NotificationChannelEntity channel : channels.findAll()) {
            if (InteractiveConfig.supports(channel.getType())) {
                seen.add(channel.getId());
                sync(channel.getId());
            }
        }
        for (UUID id : new ArrayList<>(runners.keySet())) {
            if (!seen.contains(id)) {
                stopRunner(runners.remove(id));
            }
        }
    }

    /** Brings the connector of {@code channelId} in line with the stored channel. */
    public synchronized void sync(UUID channelId) {
        Optional<NotificationChannelEntity> stored = channels.findById(channelId);
        Runner current = runners.get(channelId);
        if (shutdown || stored.isEmpty() || !served(stored.get(), chatopsEnabled())) {
            if (current != null) {
                stopRunner(runners.remove(channelId));
            }
            return;
        }
        ChannelConfig config = configOf(stored.get());
        String fingerprint = fingerprint(stored.get());
        if (current != null && current.fingerprint.equals(fingerprint)) {
            return;
        }
        if (current != null) {
            stopRunner(runners.remove(channelId));
        }
        Runner runner = new Runner(config, fingerprint, factory.create(config.type()));
        runners.put(channelId, runner);
        runner.thread = Thread.ofVirtual().name("chatops-" + channelId).start(() -> loop(runner));
    }

    /** The {@link ChannelConfig} of a stored channel (secrets decrypted). */
    public ChannelConfig configOf(NotificationChannelEntity channel) {
        return new ChannelConfig(channel.getId(), channel.getName(), channel.getType(),
                channelService.settingsFor(channel, 0, clock.instant()), InteractiveConfig.of(channel.getConfig()));
    }

    /** The router-backed sink of {@code session}: checks the kill switch, then routes (asynchronously when configured). */
    public CommandSink sink(ChatSession session) {
        return new CommandSink() {
            @Override
            public void onMessage(ChatRef ref, String text, Instant sentAt) {
                dispatch(() -> router.onMessage(session, ref, text));
            }

            @Override
            public void onCallback(ChatRef ref, String callbackRef, String token) {
                dispatch(() -> router.onCallback(session, ref, callbackRef, token));
            }
        };
    }

    private void dispatch(Runnable command) {
        if (!chatopsEnabled()) {
            return;
        }
        Runnable guarded = () -> {
            try {
                command.run();
            } catch (RuntimeException e) {
                log.warn("A chat command failed", e);
            }
        };
        if (properties.asyncCommands()) {
            commands.submit(guarded);
        } else {
            guarded.run();
        }
    }

    private void loop(Runner runner) {
        ChatConnector connector = runner.connector;
        ChatSession session = new ChatSession(runner.config, connector);
        CommandSink sink = sink(session);
        Duration backoff = properties.initialBackoff();
        boolean started = false;
        while (!runner.stopped && !shutdown && !Thread.currentThread().isInterrupted()) {
            try {
                if (!started) {
                    connector.start(runner.config, sink);
                    started = true;
                }
                connector.poll();
                backoff = properties.initialBackoff();
            } catch (ChatAuthException e) {
                connector.state().authFailed(e.getMessage());
                connector.stop();
                raiseAuthFailed(runner.config, e.getMessage());
                return;
            } catch (ConnectorBlockedException e) {
                connector.state().blocked(e.getMessage());
                started = false;
                if (!sleep(properties.maxBackoff())) {
                    return;
                }
            } catch (Exception e) {
                if (runner.stopped) {
                    return;
                }
                String error = connector.describe(e);
                connector.state().failed(error);
                log.warn("Interactive channel {} failed: {}; retrying in {} s", runner.config.channelId(), error,
                        backoff.toSeconds());
                if (!sleep(backoff)) {
                    return;
                }
                backoff = backoff.multipliedBy(2).compareTo(properties.maxBackoff()) > 0 ? properties.maxBackoff()
                        : backoff.multipliedBy(2);
            }
        }
    }

    private void raiseAuthFailed(ChannelConfig config, String reason) {
        log.error("Interactive channel {} stopped: {}", config.channelId(), reason);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("channelId", config.channelId().toString());
        data.put("type", config.type());
        events.publishEvent(new SystemEvent(AUTH_FAILED_EVENT, "warning",
                "Interactive channel " + (config.channelName() == null ? config.channelId() : config.channelName())
                        + " stopped",
                "The " + config.type() + " connector of **" + MarkdownRenderer.escapeMarkdown(String.valueOf(
                        config.channelName())) + "** stopped: " + MarkdownRenderer.escapeMarkdown(reason)
                        + ". Fix the credentials and save the channel to restart it.",
                "/v1/channels/" + config.channelId() + "/interactive", data));
    }

    private static boolean sleep(Duration duration) {
        try {
            Thread.sleep(Math.max(1, duration.toMillis()));
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void stopRunner(Runner runner) {
        if (runner == null) {
            return;
        }
        runner.stopped = true;
        runner.connector.stop();
        if (runner.connector.state().status() != HealthStatus.FAILING) {
            runner.connector.state().stopped();
        }
        Thread thread = runner.thread;
        if (thread != null) {
            thread.interrupt();
        }
    }

    /** Stops every connector. */
    @PreDestroy
    public void shutdown() {
        shutdown = true;
        for (UUID id : new ArrayList<>(runners.keySet())) {
            stopRunner(runners.remove(id));
        }
        commands.shutdownNow();
    }

    /** The running session of {@code channelId}, if its connector is supervised. */
    public Optional<ChatSession> session(UUID channelId) {
        Runner runner = runners.get(channelId);
        return runner == null ? Optional.empty() : Optional.of(new ChatSession(runner.config, runner.connector));
    }

    /**
     * {@code ChannelInteractiveStatus} of a {@code telegram} or {@code matrix} channel: whether it is interactive,
     * the kill switch, the connector's connection, last poll, last error and health, the discovery window and
     * the allowlisted chats with whether each is served.
     */
    public Map<String, Object> status(NotificationChannelEntity channel) {
        InteractiveConfig interactive = InteractiveConfig.of(channel.getConfig());
        boolean enabled = chatopsEnabled();
        Runner runner = runners.get(channel.getId());
        ConnectorState state = runner == null ? null : runner.connector.state();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("interactive", interactive.interactive());
        out.put("chatopsEnabled", enabled);
        out.put("connected", state != null && state.connected());
        out.put("status", (state == null ? HealthStatus.IDLE : state.status()).value());
        String reason = state == null ? idleReason(channel, interactive, enabled) : state.statusReason();
        out.put("statusReason", reason);
        out.put("lastPollAt", state == null || state.lastPollAt() == null ? null : state.lastPollAt().toString());
        out.put("lastError", state == null ? null : state.lastError());
        Instant until = discovery.peek(channel.getId(), interactive.discoveryMode());
        out.put("discoveryModeUntil", until == null ? null : until.toString());
        List<Map<String, Object>> chats = new ArrayList<>();
        Map<String, String> inactive = state == null ? Map.of() : state.inactiveChats();
        for (AllowedChat chat : interactive.allowedChats()) {
            Map<String, Object> item = chat.toMap();
            item.put("active", runner != null && !inactive.containsKey(chat.chatId()));
            item.put("reason", inactive.get(chat.chatId()));
            chats.add(item);
        }
        out.put("chats", chats);
        return out;
    }

    private static String idleReason(NotificationChannelEntity channel, InteractiveConfig interactive, boolean enabled) {
        if (!interactive.interactive()) {
            return "not interactive";
        }
        if (!enabled) {
            return "chatops.enabled is off";
        }
        if (!channel.isEnabled()) {
            return "channel disabled";
        }
        return "not started";
    }

    private String fingerprint(NotificationChannelEntity channel) {
        Map<String, Object> material = new TreeMap<>();
        material.put("type", channel.getType());
        material.put("name", String.valueOf(channel.getName()));
        material.put("config", channel.getConfig() == null ? Map.of() : new TreeMap<>(channel.getConfig()));
        material.put("secrets", new TreeMap<>(channelService.secrets(channel)));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(json.writeValueAsString(material).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
