package ai.foresignal.social.correlation;

import ai.foresignal.analytics.finding.FindingEvent;
import ai.foresignal.analytics.finding.FindingEventBus;
import ai.foresignal.analytics.finding.FindingListener;
import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.situation.SituationEvent;
import ai.foresignal.situation.SituationEventBus;
import ai.foresignal.situation.SituationListener;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertListener;
import ai.foresignal.trigger.AlertView;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The automatic cue triggers (prompt 137, {@code social.cues.triggers}): a situation activated or escalated (118), a
 * {@code significant} finding of a selected type (119: change points in outage, vessel, flight and fire series), a 100
 * anomaly, a critical 017 alert, and a canonical event of a selected family with at least {@code minEvidenceGroups}
 * evidence groups (polled from the hourly pass). Each queues a cue around the trigger's place for the window from
 * 6 hours before to 24 hours after it. Every trigger is physical: cue-found narratives never raise cues, so cues cannot
 * feed themselves. Listening starts only while {@code foresignal.social.correlation-scheduled} is on (tests call the
 * handlers directly).
 */
@Component
public class CueTriggers implements SituationListener, FindingListener, AlertListener, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(CueTriggers.class);

    /** The window before a trigger. */
    static final Duration BEFORE = Duration.ofHours(6);

    /** The window after a trigger. */
    static final Duration AFTER = Duration.ofHours(24);

    private final SocialCueService cues;
    private final SocialSettingsService settings;
    private final ObjectProvider<SituationEventBus> situations;
    private final ObjectProvider<FindingEventBus> findings;
    private final ObjectProvider<AlertBus> alerts;
    private final ObjectProvider<EvidenceStore> evidence;
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final boolean scheduled;
    private final List<Subscription> subscriptions = new ArrayList<>();

    /** Creates the triggers. */
    public CueTriggers(SocialCueService cues, SocialSettingsService settings,
            ObjectProvider<SituationEventBus> situations, ObjectProvider<FindingEventBus> findings,
            ObjectProvider<AlertBus> alerts, ObjectProvider<EvidenceStore> evidence, JdbcTemplate jdbc, Clock clock,
            @Value("${foresignal.social.correlation-scheduled:true}") boolean scheduled) {
        this.cues = cues;
        this.settings = settings;
        this.situations = situations;
        this.findings = findings;
        this.alerts = alerts;
        this.evidence = evidence;
        this.jdbc = jdbc;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** Subscribes to the situation, finding and alert buses once the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void start() {
        if (!scheduled || !subscriptions.isEmpty()) {
            return;
        }
        SituationEventBus s = situations.getIfAvailable();
        if (s != null) {
            subscriptions.add(s.subscribe("social-cues", this, SituationEventBus.DEFAULT_QUEUE));
        }
        FindingEventBus f = findings.getIfAvailable();
        if (f != null) {
            subscriptions.add(f.subscribe("social-cues", this, FindingEventBus.DEFAULT_QUEUE));
        }
        AlertBus a = alerts.getIfAvailable();
        if (a != null) {
            subscriptions.add(a.subscribe("social-cues", this, AlertBus.DEFAULT_QUEUE));
        }
    }

    /** Closes the subscriptions. */
    @Override
    public synchronized void destroy() {
        subscriptions.forEach(Subscription::close);
        subscriptions.clear();
    }

    /** A bus dropped events: those triggers are not cued (cues are a best-effort sample). */
    @Override
    public void onGap() {
        log.warn("Social cue triggers lagged; some physical triggers were not cued");
    }

    private SocialCueService.Target around(Instant at, String situationId, String regionCode, Double lon, Double lat,
            List<String> families) {
        return new SocialCueService.Target(null, regionCode, null, null, situationId, lon, lat, families, List.of(),
                List.of(), at.minus(BEFORE), at.plus(AFTER), null);
    }

    /** A situation activated or escalated. */
    @Override
    public void onEvent(SituationEvent event) {
        SocialSettingsService.Triggers t = settings.cues().triggers();
        boolean activated = SituationEvent.ACTIVATED.equals(event.event()) && t.situationActivated();
        boolean escalated = SituationEvent.ESCALATED.equals(event.event()) && t.situationEscalated();
        if (!activated && !escalated) {
            return;
        }
        Object priv = event.situation() == null ? null : event.situation().get("private");
        if (Boolean.TRUE.equals(priv)) {
            return;
        }
        Instant at = event.at() == null ? clock.instant() : event.at();
        cue(activated ? "situation-activated" : "situation-escalated", "situation:" + event.situationId()
                + (escalated ? "@" + event.version() : ""), around(at, event.situationId(), null, null, null, List.of()));
    }

    /** A significant finding of a selected type. */
    @Override
    public void onEvent(FindingEvent event) {
        if (!FindingEvent.CREATED.equals(event.event()) || event.finding() == null) {
            return;
        }
        Map<String, Object> f = event.finding();
        String type = String.valueOf(f.get("findingType"));
        String family = f.get("signalFamily") == null ? null : f.get("signalFamily").toString();
        SocialSettingsService.Triggers t = settings.cues().triggers();
        if (!"significant".equals(String.valueOf(f.get("severity"))) || !t.findingTypes().contains(type)
                || JdbcPhysicalEvidence.NON_PHYSICAL_FINDINGS.contains(type) || Boolean.TRUE.equals(f.get("private"))) {
            return;
        }
        if ("change-point".equals(type) && !SocialSettingsService.CHANGE_POINT_FAMILIES.contains(family)) {
            return;
        }
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT ST_X(ST_PointOnSurface(geom)) AS lon,"
                + " ST_Y(ST_PointOnSurface(geom)) AS lat, region_codes[1] AS region, detected_at FROM analytic_finding"
                + " WHERE id = ?", event.findingId());
        if (rows.isEmpty()) {
            return;
        }
        Map<String, Object> row = rows.get(0);
        Instant at = row.get("detected_at") instanceof java.sql.Timestamp ts ? ts.toInstant() : clock.instant();
        cue("finding", "finding:" + event.findingId(), around(at, null, row.get("region") == null ? null
                : row.get("region").toString(), number(row.get("lon")), number(row.get("lat")),
                family == null ? List.of() : List.of(family)));
    }

    /** A critical alert or an anomaly. */
    @Override
    public void onAlert(AlertEvent event) {
        if (!AlertEvent.FIRED.equals(event.type()) || event.alert() == null || event.alert().exercise()) {
            return;
        }
        AlertView a = event.alert();
        SocialSettingsService.Triggers t = settings.cues().triggers();
        boolean anomaly = "anomaly".equals(a.kind()) && t.anomalies();
        boolean critical = "critical".equals(a.severity()) && t.criticalAlerts();
        if ((!anomaly && !critical) || a.sensitive() || a.region() == null) {
            return;
        }
        Instant at = a.firedAt() == null ? clock.instant() : a.firedAt();
        cue(anomaly ? "anomaly" : "alert", "alert:" + a.id(), around(at, null, a.region(), null, null, List.of()));
    }

    /**
     * Cues canonical events of the selected families that reached {@code minEvidenceGroups} since {@code since}.
     *
     * @return the cues queued
     */
    public int scanCanonical(Instant since) {
        EvidenceStore store = evidence.getIfAvailable();
        SocialSettingsService.Triggers t = settings.cues().triggers();
        if (store == null || t.canonicalFamilies().isEmpty()) {
            return 0;
        }
        int n = 0;
        for (EvidenceStore.StoredEvent e : store.updatedSince(t.canonicalFamilies(), since, 200)) {
            if (e.mergedInto() != null || e.evidenceGroupCount() < t.minEvidenceGroups()) {
                continue;
            }
            String region = e.regionCodes().isEmpty() ? null : e.regionCodes().get(0);
            if (cue("canonical-event", "canonical-event:" + e.id(), around(e.eventTime(), null, region, e.lon(),
                    e.lat(), List.of(familyOf(e.family()))))) {
                n++;
            }
        }
        return n;
    }

    /** The signal family whose lexicon terms a canonical family's cue uses. */
    static String familyOf(String canonicalFamily) {
        return switch (canonicalFamily) {
            case "weather-alert" -> "weather-severe";
            case "disaster" -> "weather-severe";
            default -> canonicalFamily;
        };
    }

    private boolean cue(String type, String ref, SocialCueService.Target target) {
        try {
            return cues.createAutomatic(type, ref, target) != null;
        } catch (RuntimeException e) {
            log.warn("Automatic cue for {} failed: {}", ref, e.toString());
            return false;
        }
    }

    private static Double number(Object v) {
        return v instanceof Number n ? n.doubleValue() : null;
    }
}
