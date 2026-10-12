package ai.foresignal.system;

import ai.foresignal.settings.WatchdogSettings;
import ai.foresignal.settings.WatchdogSettingsService;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The off-box dead-man watchdog (prompt 019). Every {@code interval} it pings {@code heartbeatUrl}
 * when readiness is UP and the canary (when enabled) last passed; otherwise it pings {@code failUrl}
 * when one is set. A monitor such as a Healthchecks.io check or an Uptime Kuma push monitor alerts
 * when the heartbeats stop, which also covers the host being down.
 *
 * <p>URLs are redacted in logs. Metric {@code foresignal.watchdog.pings{kind,result}}.
 */
@Component
public class WatchdogService {

    private static final Logger log = LoggerFactory.getLogger(WatchdogService.class);

    private final WatchdogSettingsService settings;
    private final ReadinessCheck readiness;
    private final CanaryService canary;
    private final HeartbeatPinger pinger;
    private final WatchdogProperties properties;
    private final MeterRegistry meters;
    private final Clock clock;
    private Instant nextPingAt;
    private Instant nextCanaryAt;

    /** Creates the watchdog. */
    public WatchdogService(WatchdogSettingsService settings, ReadinessCheck readiness, CanaryService canary,
            HeartbeatPinger pinger, WatchdogProperties properties, MeterRegistry meters, Clock clock) {
        this.settings = settings;
        this.readiness = readiness;
        this.canary = canary;
        this.pinger = pinger;
        this.properties = properties;
        this.meters = meters;
        this.clock = clock;
    }

    /** Ticks every 5 seconds when scheduled; each tick does only what is due. */
    @Scheduled(fixedDelay = 5_000, initialDelay = 30_000)
    public void scheduled() {
        if (properties.scheduled()) {
            tick();
        }
    }

    /**
     * Runs the canary and sends the ping when they are due.
     *
     * @return what was pinged: {@code heartbeat}, {@code fail}, or null when nothing was
     */
    public synchronized String tick() {
        WatchdogSettings value = settings.get();
        Instant now = clock.instant();
        if (value.canary().enabled()) {
            if (nextCanaryAt == null || !now.isBefore(nextCanaryAt)) {
                nextCanaryAt = now.plus(value.canary().interval());
                canary.runOnce();
            }
        } else {
            nextCanaryAt = null;
            canary.reset();
        }
        if (!value.configured()) {
            nextPingAt = null;
            return null;
        }
        if (nextPingAt != null && now.isBefore(nextPingAt)) {
            return null;
        }
        nextPingAt = now.plus(value.interval());
        boolean healthy = readiness.ready() && (!value.canary().enabled() || !Boolean.FALSE.equals(canary.lastOk()));
        String kind = healthy ? "heartbeat" : "fail";
        String url = healthy ? value.heartbeatUrl() : value.failUrl();
        if (url == null || url.isBlank()) {
            return null;
        }
        String result;
        try {
            int status = pinger.ping(url, value.method(), (int) properties.timeout().toMillis());
            result = status >= 200 && status < 300 ? "ok" : "failed";
            if (!"ok".equals(result)) {
                log.warn("Watchdog {} ping to {} answered HTTP {}", kind, HeartbeatPinger.redact(url), status);
            }
        } catch (Exception e) {
            result = "failed";
            log.warn("Watchdog {} ping to {} failed: {}", kind, HeartbeatPinger.redact(url), e.getClass().getSimpleName());
        }
        meters.counter("foresignal.watchdog.pings", "kind", kind, "result", result).increment();
        return kind;
    }

    /** Makes the next tick ping and run the canary regardless of the intervals (tests, settings change). */
    public synchronized void resetSchedule() {
        nextPingAt = null;
        nextCanaryAt = null;
    }
}
