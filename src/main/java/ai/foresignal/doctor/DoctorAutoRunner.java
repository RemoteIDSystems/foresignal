package ai.foresignal.doctor;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.collect.HealthEvaluator;
import ai.foresignal.collect.SourceHealthChangedEvent;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.Durations;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.HealthStatus;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Automatic doctor runs (prompt 140, task 6). Listens to the health transitions {@code HealthService} publishes
 * ({@link SourceHealthChangedEvent}, the 006/018 hook): when a source becomes {@code degraded} or {@code failing} with a
 * format-change, coverage-dropped or parse-error reason, the doctor runs with {@code trigger=AUTO}, a probe, and the
 * LLM only when the operator opted in with {@code doctor.autoLlm} (default false; NFR-13). At most once per source per
 * {@code doctor.autoCooldown} (default 6 h).
 *
 * <p>A {@code BROKEN} automatic report publishes a {@code system} notification (018), subtype
 * {@value #SUBTYPE}: "{source}: {top finding} — {suggested action}", linking the report.
 *
 * <p>Runs happen off the publishing thread (a health read must not wait on a probe) unless
 * {@code foresignal.doctor.auto-async} is false; {@code foresignal.doctor.auto-enabled=false} turns the hook off (the
 * test profile, so no background run probes an upstream).
 */
@Component
public class DoctorAutoRunner implements ai.foresignal.ingest.IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(DoctorAutoRunner.class);

    /** The notification subtype of a broken automatic report. */
    public static final String SUBTYPE = "system.doctor_broken";

    /** The default {@code doctor.autoCooldown}. */
    public static final Duration DEFAULT_COOLDOWN = Duration.ofHours(6);

    private final DoctorService doctor;
    private final AppSettingService settings;
    private final ObjectProvider<NotificationService> notifications;
    private final Clock clock;
    private final boolean async;
    private final boolean enabled;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /** Source id → when an automatic run was last attempted in this process. */
    private final Map<String, Instant> attempted = new ConcurrentHashMap<>();

    /** Creates the runner. */
    public DoctorAutoRunner(DoctorService doctor, AppSettingService settings,
            ObjectProvider<NotificationService> notifications, Clock clock,
            @Value("${foresignal.doctor.auto-async:true}") boolean async,
            @Value("${foresignal.doctor.auto-enabled:true}") boolean enabled) {
        this.doctor = doctor;
        this.settings = settings;
        this.notifications = notifications;
        this.clock = clock;
        this.async = async;
        this.enabled = enabled;
    }

    /** Stops the executor. */
    @PreDestroy
    void close() {
        executor.shutdownNow();
    }

    /** Forgets the in-process cool-off; the persisted automatic reports still count (a database reset clears them). */
    @Override
    public void resetState() {
        attempted.clear();
    }

    /** {@code doctor.autoLlm}: false unless the operator opted in. */
    public boolean autoLlm() {
        try {
            return settings.get(SettingKeys.DOCTOR_AUTO_LLM, Boolean.class).orElse(false);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** {@code doctor.autoCooldown}: 6 h unless set. */
    public Duration cooldown() {
        try {
            return settings.get(SettingKeys.DOCTOR_AUTO_COOLDOWN, String.class).map(Durations::parsePositive)
                    .orElse(DEFAULT_COOLDOWN);
        } catch (RuntimeException e) {
            return DEFAULT_COOLDOWN;
        }
    }

    /** True for a transition the doctor should look at: degraded or failing for a format, coverage or parse reason. */
    public static boolean qualifies(SourceHealthChangedEvent event) {
        if (event == null || (event.current() != HealthStatus.DEGRADED && event.current() != HealthStatus.FAILING)) {
            return false;
        }
        String reason = event.reason() == null ? "" : event.reason();
        String lower = reason.toLowerCase(Locale.ROOT);
        return reason.equals(HealthEvaluator.REASON_FORMAT_CHANGE) || lower.contains("format change")
                || lower.startsWith("coverage dropped") || reason.contains("UpstreamFormatException")
                || lower.contains("parse");
    }

    /** The health hook: hands a qualifying transition to {@link #consider}. */
    @EventListener
    public void onHealthChanged(SourceHealthChangedEvent event) {
        if (!enabled || !qualifies(event)) {
            return;
        }
        if (async) {
            executor.submit(() -> {
                try {
                    consider(event);
                } catch (RuntimeException e) {
                    log.warn("The automatic doctor run for {} failed: {}", event.sourceId(), e.toString());
                }
            });
        } else {
            consider(event);
        }
    }

    /**
     * Runs the doctor for a qualifying transition unless the cool-off holds; notifies when the report is
     * {@code BROKEN}.
     *
     * @return the report, or empty when nothing ran (not qualifying, cooling off, busy)
     */
    public Optional<DoctorReportView> consider(SourceHealthChangedEvent event) {
        if (!qualifies(event)) {
            return Optional.empty();
        }
        String sourceId = event.sourceId();
        Instant now = clock.instant();
        Duration cooldown = cooldown();
        Instant previous = attempted.get(sourceId);
        if (previous != null && previous.isAfter(now.minus(cooldown))) {
            return Optional.empty();
        }
        Optional<DoctorReportEntity> lastAuto = doctor.latestAuto(sourceId);
        if (lastAuto.isPresent() && lastAuto.get().getCreatedAt().isAfter(now.minus(cooldown))) {
            attempted.put(sourceId, lastAuto.get().getCreatedAt());
            return Optional.empty();
        }
        attempted.put(sourceId, now);
        DoctorOptions options = new DoctorOptions(true, autoLlm() ? DoctorOptions.LLM_AUTO : DoctorOptions.LLM_OFF,
                DoctorOptions.COMPACT);
        DoctorReportView report;
        try {
            report = doctor.run(sourceId, options, DoctorTrigger.AUTO);
        } catch (ConflictException e) {
            log.info("Automatic doctor run for {} skipped: {}", sourceId, e.getMessage());
            return Optional.empty();
        }
        if (report.status() == DoctorStatus.BROKEN) {
            notifyBroken(report);
        }
        return Optional.of(report);
    }

    /** The {@code system} notification of a broken automatic report. Pure. */
    static NotificationMessage brokenMessage(DoctorReportView report) {
        DoctorFinding top = report.topFinding();
        String code = top == null ? "BROKEN" : top.code().name();
        String action = top == null ? "" : top.suggestedAction();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reportId", report.id());
        data.put("sourceId", report.sourceId());
        data.put("status", report.status().name());
        data.put("topFinding", code);
        return NotificationMessage.builder(NotificationMessage.SYSTEM, SUBTYPE,
                        report.sourceId() + ": " + code + " — " + StructureExtractor.cap(action, 300), "warning")
                .body("The adapter doctor ran automatically after a health change and found the source broken. Top"
                        + " finding " + code + ": " + action + " Any patch in the report is proposed, not applied.")
                .url("/v1/doctor/reports/" + report.id())
                .data(data)
                .tags(List.of("doctor"))
                .build();
    }

    private void notifyBroken(DoctorReportView report) {
        NotificationService service = notifications.getIfAvailable();
        if (service == null) {
            return;
        }
        try {
            service.publish(brokenMessage(report), null);
        } catch (RuntimeException e) {
            log.warn("The notification for doctor report {} failed: {}", report.id(), e.toString());
        }
    }
}
