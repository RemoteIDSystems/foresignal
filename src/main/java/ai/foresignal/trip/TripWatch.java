package ai.foresignal.trip;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.briefing.BriefingSection;
import ai.foresignal.briefing.DigestRenderer;
import ai.foresignal.kinematics.KinematicService;
import ai.foresignal.kinematics.KinematicState;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Pre-trip checks and the in-trip corridor watch (prompt 114, task 2).
 *
 * <p><b>Pre-trip check.</b> On a one-minute tick, each enabled trip's occurrences are examined; at
 * {@code departAt − leadTime} (and before departure) an occurrence without a delivered check gets one: the hazards
 * on and near the corridor and the forecast hazards of the window ({@link TripHazards}), ordered along the route,
 * rendered as a trip briefing by the 029 digest renderer (sections on route, near route, timing) and delivered to
 * the trip's channels (every channel subscribed to briefings when it names none). A quiet result ("route clear") is
 * delivered too unless {@code notifyWhenClear=false}. Each check is stored as a {@code trip_run}.
 *
 * <p><b>Anchor progress.</b> With a fresh moving anchor (036), hazards behind the anchor's projection onto the route
 * are left out and the rest carry an ETA along the route at the anchor's average speed over the last 10 minutes.
 *
 * <p><b>In-trip watch.</b> From the check time to {@code departAt + expectedDuration} an ephemeral trigger rule
 * (017) watches the corridor: geofence = the route with {@code bufferMeters}, kinds = the hazard kinds, mode
 * {@code enter}, {@code activeFrom}/{@code activeUntil} = the window, tag {@code trip-{id}}. While the anchor is still
 * moving at the window's end, the window is extended 15 minutes at a time up to twice the expected duration; then
 * the rule is deleted. Its alerts follow the normal alert routing. Off when {@code foresignal.trips.scheduled} is
 * false (tests call {@link #tick}).
 */
@Component
public class TripWatch {

    private static final Logger log = LoggerFactory.getLogger(TripWatch.class);

    /** {@code subtype} of trip briefing notifications. */
    public static final String SUBTYPE = "trip.briefing";

    /** How much an extension adds while the anchor is moving. */
    static final Duration EXTENSION = Duration.ofMinutes(15);

    /** How close to its end a window is checked for extension. */
    static final Duration EXTENSION_MARGIN = Duration.ofMinutes(5);

    static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);

    private final TripRepository trips;
    private final TripRunRepository runs;
    private final TripService service;
    private final TripHazards hazards;
    private final KinematicService kinematics;
    private final DigestRenderer renderer;
    private final NotificationService notifications;
    private final TriggerRuleService rules;
    private final AppSettingService settings;
    private final JsonMapper json;
    private final Clock clock;
    private final boolean scheduled;

    /** Creates the watch. */
    public TripWatch(TripRepository trips, TripRunRepository runs, TripService service, TripHazards hazards,
            KinematicService kinematics, DigestRenderer renderer, NotificationService notifications,
            TriggerRuleService rules, AppSettingService settings, JsonMapper json, Clock clock,
            @Value("${foresignal.trips.scheduled:true}") boolean scheduled) {
        this.trips = trips;
        this.runs = runs;
        this.service = service;
        this.hazards = hazards;
        this.kinematics = kinematics;
        this.renderer = renderer;
        this.notifications = notifications;
        this.rules = rules;
        this.settings = settings;
        this.json = json;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** The one-minute scheduler tick. */
    @Scheduled(fixedDelayString = "PT60S", initialDelayString = "PT30S")
    public void scheduledTick() {
        if (scheduled) {
            tick();
        }
    }

    /** Runs due pre-trip checks, starts in-trip watches, and extends or ends active ones. */
    public synchronized void tick() {
        Instant now = clock.instant();
        for (TripEntity trip : trips.findByEnabledTrue()) {
            try {
                process(trip, now);
            } catch (RuntimeException e) {
                log.warn("Trip {} failed its tick: {}", trip.getId(), e.getMessage());
            }
        }
        for (TripEntity trip : trips.findByActiveRuleIdIsNotNull()) {
            try {
                endOrExtend(trip, now);
            } catch (RuntimeException e) {
                log.warn("Trip {} could not end or extend its watch: {}", trip.getId(), e.getMessage());
            }
        }
    }

    private void process(TripEntity trip, Instant now) {
        TripSchedule schedule = service.schedule(trip);
        Duration lead = Duration.ofSeconds(trip.getLeadTimeSeconds());
        Duration duration = Duration.ofSeconds(trip.getExpectedDurationSeconds());
        for (Instant occurrence : schedule.occurrences(now.minus(duration), now.plus(lead))) {
            if (now.isBefore(occurrence.minus(lead))) {
                continue;
            }
            if (now.isBefore(occurrence)
                    && !runs.existsByTripIdAndOccurrenceAndStatusNot(trip.getId(), occurrence, "dry-run")) {
                run(trip, occurrence, false, now);
            }
            Instant windowEnd = occurrence.plus(duration);
            if (now.isBefore(windowEnd) && !occurrence.equals(trip.getActiveOccurrence())) {
                startWatch(trip, occurrence, windowEnd);
            }
        }
    }

    // ------------------------------------------------------------------------- checks

    /**
     * Runs the pre-trip check now ({@code checkTrip}).
     *
     * @param tripId the trip
     * @param dryRun return the briefing without delivering
     * @param occurrence the occurrence's departure, or null for the next one
     * @throws ValidationException when there is no occurrence (a past one-off trip without {@code occurrence})
     */
    public TripRunEntity check(String tripId, boolean dryRun, Instant occurrence) {
        TripEntity trip = service.get(tripId);
        Instant now = clock.instant();
        Instant departAt = occurrence != null ? occurrence : service.schedule(trip).next(now);
        if (departAt == null) {
            throw ValidationException.forField("occurrence", "the trip has no future occurrence; pass one");
        }
        return run(trip, departAt, dryRun, now);
    }

    /** Checks one occurrence, stores the run and delivers it unless {@code dryRun}. */
    TripRunEntity run(TripEntity trip, Instant occurrence, boolean dryRun, Instant now) {
        List<TripHazards.Hazard> found = hazards.find(trip, occurrence, now);
        found = withAnchor(trip, found, now);
        boolean clear = found.isEmpty();
        ZoneId zone = service.schedule(trip).zoneOr(serverZone());
        String markdown = render(trip, occurrence, found, zone);
        TripRunEntity run = new TripRunEntity(Ulids.next(now));
        run.setTripId(trip.getId());
        run.setOccurrence(occurrence);
        run.setClear(clear);
        run.setHazards(new ArrayList<>(found.stream().map(TripHazards.Hazard::toMap).toList()));
        run.setBriefing(markdown);
        run.setCreatedAt(now);
        if (dryRun) {
            run.setStatus("dry-run");
        } else if (clear && !trip.isNotifyWhenClear()) {
            run.setStatus("skipped");
        } else if (deliver(trip, run, found)) {
            run.setStatus("delivered");
            run.setDeliveredAt(now);
        } else {
            run.setStatus("failed");
        }
        return runs.saveAndFlush(run);
    }

    /** Anchor progress: only hazards ahead of a fresh anchor, with route ETAs; unchanged without one. */
    List<TripHazards.Hazard> withAnchor(TripEntity trip, List<TripHazards.Hazard> found, Instant now) {
        if (trip.getAnchor() == null) {
            return found;
        }
        int slash = trip.getAnchor().indexOf('/');
        Optional<KinematicState> state = kinematics.state(trip.getAnchor().substring(0, slash),
                trip.getAnchor().substring(slash + 1));
        if (state.isEmpty() || state.get().age(now).compareTo(KinematicService.MAX_AGE) > 0) {
            return found;
        }
        RouteMeasure measure = new RouteMeasure(RouteMeasure.splitAntimeridian((LineString) trip.getRoute()));
        double along = measure.locate(state.get().lon(), state.get().lat()).alongMeters();
        Double speed = kinematics.recentAverageSpeed(state.get().sourceId(), state.get().externalId());
        if (speed == null) {
            speed = state.get().speedMps();
        }
        return TripHazards.ahead(found, along, speed);
    }

    /** The trip briefing markdown (029 digest renderer). */
    String render(TripEntity trip, Instant occurrence, List<TripHazards.Hazard> found, ZoneId zone) {
        List<BriefingSection> sections = new ArrayList<>();
        if (found.isEmpty()) {
            sections.add(new BriefingSection("clear", Map.of(), "## Route clear\n\nNo "
                    + DigestRenderer.escape(String.join(", ", trip.getHazardKinds()))
                    + " on or near the route for this trip.", null));
        } else {
            section(sections, "on-route", "On route", found, zone);
            section(sections, "near-route", "Near route (within " + Math.round(trip.getBufferMeters() * 2 / 100.0) / 10.0
                    + " km)", found, zone);
            section(sections, "timing", "Timing (forecast during the trip)", found, zone);
        }
        Instant arrive = occurrence.plusSeconds(trip.getExpectedDurationSeconds());
        return renderer.renderSections("Trip briefing: " + trip.getName(), zone, occurrence, arrive, sections)
                .markdown();
    }

    private static void section(List<BriefingSection> sections, String key, String heading,
            List<TripHazards.Hazard> found, ZoneId zone) {
        List<TripHazards.Hazard> items = found.stream().filter(h -> key.equals(h.section())).toList();
        if (items.isEmpty()) {
            return;
        }
        StringBuilder md = new StringBuilder("## ").append(heading).append("\n\n");
        for (TripHazards.Hazard h : items) {
            md.append("- **").append(h.severity() == null ? "unrated" : h.severity()).append("** · ")
                    .append(DigestRenderer.escape(h.kind()));
            if (h.title() != null && !h.title().isBlank()) {
                md.append(" · ").append(DigestRenderer.escape(h.title()));
            }
            md.append(" · ").append(String.format(Locale.ROOT, "%.1f", h.alongMeters() / 1000)).append(" km along the route");
            if (!"on-route".equals(key)) {
                md.append(", ").append(String.format(Locale.ROOT, "%.1f", h.offsetMeters() / 1000)).append(" km off it");
            }
            if (h.etaSeconds() != null) {
                md.append(", ETA ").append(Math.round(h.etaSeconds() / 60)).append(" min");
            }
            if ("timing".equals(key) && h.validFrom() != null) {
                md.append(", from ").append(TIME.format(h.validFrom().atZone(zone)));
            }
            md.append('\n');
        }
        sections.add(new BriefingSection(key, Map.of(), md.toString(), null));
    }

    /** Publishes the briefing to the trip's channels; true when the notification was stored. */
    private boolean deliver(TripEntity trip, TripRunEntity run, List<TripHazards.Hazard> found) {
        String severity = "info";
        for (TripHazards.Hazard h : found) {
            if (TripHazards.rank(h.severity()) > TripHazards.rank(severity)) {
                severity = h.severity();
            }
        }
        Set<UUID> channelIds = trip.getChannelIds().stream().map(UUID::fromString).collect(Collectors.toSet());
        Predicate<ai.foresignal.notify.NotificationChannel> filter =
                channelIds.isEmpty() ? c -> true : c -> channelIds.contains(c.id());
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("tripId", trip.getId());
            data.put("occurrence", run.getOccurrence().toString());
            data.put("clear", run.isClear());
            data.put("hazards", found.size());
            NotificationMessage message = notifications.publish(NotificationMessage
                    .builder(NotificationMessage.BRIEFING, SUBTYPE, "Trip briefing: " + trip.getName(), severity)
                    .shortTitle(trip.getName())
                    .body(run.getBriefing())
                    .data(data)
                    .tags(List.of("trip"))
                    .build(), filter);
            return message.id() != null;
        } catch (RuntimeException e) {
            log.error("Trip briefing of {} could not be published: {}", trip.getId(), e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------------- the in-trip watch

    /** Creates the occurrence's ephemeral rule (replacing a previous one). */
    void startWatch(TripEntity trip, Instant occurrence, Instant until) {
        service.removeRule(trip);
        UUID id = rules.create(json.valueToTree(ruleBody(trip, occurrence, until))).rule().getId();
        trip.setActiveRuleId(id);
        trip.setActiveOccurrence(occurrence);
        trip.setActiveUntil(until);
        trips.saveAndFlush(trip);
    }

    /** Extends the window while the anchor moves, or deletes the rule once the window is over. */
    void endOrExtend(TripEntity trip, Instant now) {
        Instant until = trip.getActiveUntil();
        if (until == null || trip.getActiveOccurrence() == null) {
            service.removeRule(trip);
            trips.saveAndFlush(trip);
            return;
        }
        Instant cap = trip.getActiveOccurrence().plusSeconds(2 * trip.getExpectedDurationSeconds());
        if (!now.isBefore(until.minus(EXTENSION_MARGIN)) && until.isBefore(cap) && anchorMoving(trip, now)) {
            Instant extended = until.plus(EXTENSION).isAfter(cap) ? cap : until.plus(EXTENSION);
            rules.update(trip.getActiveRuleId(), json.valueToTree(ruleBody(trip, trip.getActiveOccurrence(), extended)));
            trip.setActiveUntil(extended);
            trips.saveAndFlush(trip);
            return;
        }
        if (!now.isBefore(until)) {
            service.removeRule(trip);
            trips.saveAndFlush(trip);
        }
    }

    /** True when the trip's anchor is fresh and moving (≥ 0.5 m/s). */
    boolean anchorMoving(TripEntity trip, Instant now) {
        if (trip.getAnchor() == null) {
            return false;
        }
        int slash = trip.getAnchor().indexOf('/');
        return kinematics.state(trip.getAnchor().substring(0, slash), trip.getAnchor().substring(slash + 1))
                .filter(s -> s.age(now).compareTo(Duration.ofMinutes(15)) <= 0)
                .map(s -> s.speedMps() != null && s.speedMps() >= 0.5)
                .orElse(false);
    }

    /** The {@code TriggerRuleInput} of the trip's ephemeral rule. */
    Map<String, Object> ruleBody(TripEntity trip, Instant from, Instant until) {
        String name = "Trip: " + trip.getName();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name.length() > 200 ? name.substring(0, 200) : name);
        body.put("kinds", trip.getHazardKinds());
        body.put("geofence", new GeoJsonWriter().write(RouteMeasure.splitAntimeridian((LineString) trip.getRoute())));
        body.put("bufferMeters", trip.getBufferMeters());
        body.put("mode", "enter");
        body.put("activeFrom", from.toString());
        body.put("activeUntil", until.toString());
        body.put("tags", List.of(tag(trip.getId())));
        body.put("severity", "warning");
        body.put("messageTemplate", "{{name}}: {{kind}} {{title}} on the route");
        return body;
    }

    /** The tag of a trip's ephemeral rule ({@code trip-} and the lower-case trip id). */
    public static String tag(String tripId) {
        return "trip-" + tripId.toLowerCase(Locale.ROOT);
    }

    private ZoneId serverZone() {
        String tz = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults).timezone();
        try {
            return tz == null || tz.isBlank() ? ZoneOffset.UTC : ZoneId.of(tz);
        } catch (DateTimeException e) {
            return ZoneOffset.UTC;
        }
    }
}
