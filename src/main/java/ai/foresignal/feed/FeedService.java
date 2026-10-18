package ai.foresignal.feed;

import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.briefing.BriefingRunEntity;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ClientLinks;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertView;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds the Atom and JSON feeds of prompt 058 for alerts and briefing runs.
 *
 * <p><strong>Alert entries</strong>, newest first by last lifecycle change ({@link AlertService#feed}):
 *
 * <ul>
 *   <li>{@code id} {@code tag:foresignal,2026:{installId}/alert/{id}};
 *   <li>{@code updated} the last lifecycle change, {@code published} {@code firedAt};
 *   <li>{@code title} the short title (the rule name, as notifications use it), else the message's first
 *       line, else {@code Alert {id}};
 *   <li>{@code link} the operator's {@code clientLinks.alert} deep link when {@code clientLinks} is set,
 *       else {@code publicBaseUrl/v1/alerts/{id}} (the rule notifications follow);
 *   <li>categories: the severity and the rule tags;
 *   <li>content: the message, plus the enrichment summary, as sanitized HTML and plain text;
 *   <li>geometry: the record snapshot's, for GeoRSS and the {@code _foresignal} extension.
 * </ul>
 *
 * <p>Alerts of private sources are already excluded from the query for callers that may not see them;
 * each remaining entry must also pass {@link EgressPolicy#allows} on {@link EgressPath#FEED}.
 *
 * <p><strong>Briefing run entries</strong> embed the whole run markdown, sanitized the same way.
 */
@Service
public class FeedService {

    /** The tag URI authority and date of every id. */
    public static final String TAG_PREFIX = "tag:foresignal,2026:";

    /** The scheme of severity categories. */
    public static final String SEVERITY_SCHEME = TAG_PREFIX + "severity";

    /** The scheme of rule tag categories. */
    public static final String TAG_SCHEME = TAG_PREFIX + "tag";

    /** The HTTP date format of {@code Last-Modified}. */
    public static final DateTimeFormatter HTTP_DATE = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);

    private final AlertService alerts;
    private final BriefingService briefings;
    private final FeedSanitizer sanitizer;
    private final InstallIdService installId;
    private final EgressPolicy egress;
    private final AppSettingService settings;
    private final JsonMapper json;
    private final GeoJsonReader geoJson = new GeoJsonReader();

    /** Creates the service. */
    public FeedService(AlertService alerts, BriefingService briefings, FeedSanitizer sanitizer,
            InstallIdService installId, EgressPolicy egress, AppSettingService settings, JsonMapper json) {
        this.alerts = alerts;
        this.briefings = briefings;
        this.sanitizer = sanitizer;
        this.installId = installId;
        this.egress = egress;
        this.settings = settings;
        this.json = json;
    }

    /**
     * The alert feed.
     *
     * @param query the filters, with the caller's excluded sources and restriction
     * @param limit 1 to 200 entries
     * @param viewer who reads it (the private gate)
     * @param urls the request's public URLs
     * @param selfPath the canonical path of the feed itself, for example {@code /v1/alerts/feed.atom}
     */
    public Feed alerts(AlertQuery query, int limit, Viewer viewer, PublicUrls urls, String selfPath) {
        ServerSettings server = server();
        List<FeedEntry> entries = new ArrayList<>();
        for (AlertView alert : alerts.feed(query, limit)) {
            if (egress.allows(EgressPath.FEED, alert.sourceId(), viewer)) {
                entries.add(alertEntry(alert, server, urls));
            }
        }
        return new Feed(TAG_PREFIX + installId.installId() + "/alerts", "Foresignal alerts",
                "Alerts of the Foresignal instance, newest first by last change", urls.api(selfPath),
                urls.plain("/v1/alerts"), newest(entries), entries);
    }

    /**
     * The feed of one briefing's runs, newest first.
     *
     * @param briefingId the briefing
     * @param limit 1 to 200 entries
     * @param seesPrivate true when the caller may see private sources (runs citing one are kept)
     * @param urls the request's public URLs
     * @param selfPath the canonical path of the feed itself
     */
    public Feed briefingRuns(UUID briefingId, int limit, boolean seesPrivate, PublicUrls urls, String selfPath) {
        ServerSettings server = server();
        List<BriefingRunEntity> runs = briefings.feedRuns(briefingId, limit, seesPrivate);
        List<FeedEntry> entries = new ArrayList<>();
        String name = null;
        for (BriefingRunEntity run : runs) {
            name = name == null ? run.getBriefingName() : name;
            entries.add(runEntry(run, server, urls));
        }
        String title = "Foresignal briefing" + (name == null ? "" : ": " + name);
        return new Feed(TAG_PREFIX + installId.installId() + "/briefing/" + briefingId, title,
                "Runs of a Foresignal briefing, newest first", urls.api(selfPath),
                urls.plain("/v1/briefings/" + briefingId), newest(entries), entries);
    }

    /** The audio media store (prompt 141); absent only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.media.MediaStore media;

    /**
     * The podcast of one briefing (prompt 141, {@code getBriefingPodcast}): its runs with stored audio
     * ({@code audio.status = ok} and a media row that still exists), newest first, each with an enclosure to
     * {@code /v1/media/{id}} through {@link PublicUrls#api} (so a keyed request keeps its feed key).
     *
     * @param briefingId the briefing
     * @param limit the most items
     * @param seesPrivate whether runs citing private sources may appear
     * @param urls the URLs of the request
     * @param selfPath the canonical path of the feed
     */
    public PodcastFeed briefingPodcast(UUID briefingId, int limit, boolean seesPrivate, PublicUrls urls,
            String selfPath) {
        ServerSettings server = server();
        List<FeedEntry> entries = new ArrayList<>();
        List<PodcastFeed.Enclosure> enclosures = new ArrayList<>();
        String name = null;
        for (BriefingRunEntity run : briefings.feedRuns(briefingId, PODCAST_SCAN, seesPrivate)) {
            name = name == null ? run.getBriefingName() : name;
            PodcastFeed.Enclosure enclosure = enclosure(run, urls);
            if (enclosure == null) {
                continue;
            }
            entries.add(runEntry(run, server, urls));
            enclosures.add(enclosure);
            if (entries.size() >= limit) {
                break;
            }
        }
        String title = "Foresignal briefing audio" + (name == null ? "" : ": " + name);
        Feed feed = new Feed(TAG_PREFIX + installId.installId() + "/briefing/" + briefingId + "/podcast", title,
                "Audio of a Foresignal briefing's runs, newest first", urls.api(selfPath),
                urls.plain("/v1/briefings/" + briefingId), newest(entries), entries);
        return new PodcastFeed(feed, enclosures);
    }

    /** How many recent runs the podcast looks through for audio. */
    static final int PODCAST_SCAN = 500;

    /** The enclosure of a run with stored audio, or null. */
    PodcastFeed.Enclosure enclosure(BriefingRunEntity run, PublicUrls urls) {
        Map<String, Object> audio = run.getAudio();
        if (audio == null || !"ok".equals(audio.get("status")) || !(audio.get("mediaId") instanceof String id)) {
            return null;
        }
        if (media == null) {
            return null;
        }
        java.util.Optional<ai.foresignal.media.MediaRow> row;
        try {
            row = media.find(id);
        } catch (RuntimeException e) {
            return null;
        }
        if (row.isEmpty()) {
            return null;
        }
        return new PodcastFeed.Enclosure(urls.api("/v1/media/" + id), row.get().bytes(), row.get().contentType());
    }

    /** One alert as an entry. */
    FeedEntry alertEntry(AlertView alert, ServerSettings server, PublicUrls urls) {
        String link = server.clientLinks() != null
                ? ClientLinks.fill(server.effectiveClientLinks().alert(), Map.of("id", Long.toString(alert.id())))
                : urls.plain("/v1/alerts/" + alert.id());
        String markdown = alert.message() == null ? "" : alert.message();
        if (alert.summary() != null && !alert.summary().isBlank()) {
            markdown = markdown + "\n\n" + alert.summary();
        }
        List<FeedEntry.Category> categories = new ArrayList<>();
        if (alert.severity() != null) {
            categories.add(new FeedEntry.Category(alert.severity(), SEVERITY_SCHEME));
        }
        for (String tag : alert.tags()) {
            categories.add(new FeedEntry.Category(tag, TAG_SCHEME));
        }
        JsonNode geometryNode = snapshotGeometry(alert.recordSnapshot());
        Geometry geometry = null;
        if (geometryNode != null) {
            try {
                geometry = geoJson.read(geometryNode);
            } catch (RuntimeException e) {
                geometry = null;
            }
        }
        Map<String, Object> extension = new LinkedHashMap<>();
        extension.put("severity", alert.severity());
        extension.put("state", alert.state());
        extension.put("sourceId", alert.sourceId());
        extension.put("externalId", alert.externalId());
        extension.put("geometry", geometryNode == null ? null : json.convertValue(geometryNode, Map.class));
        return new FeedEntry(TAG_PREFIX + installId.installId() + "/alert/" + alert.id(), alertTitle(alert), link,
                alert.firedAt(), AlertService.lastChange(alert), sanitizer.html(markdown), sanitizer.text(markdown),
                categories, geometry, extension);
    }

    /** One briefing run as an entry. */
    FeedEntry runEntry(BriefingRunEntity run, ServerSettings server, PublicUrls urls) {
        String link = server.clientLinks() != null
                ? ClientLinks.fill(server.effectiveClientLinks().briefingRun(),
                        Map.of("briefingId", run.getBriefingId().toString(), "id", run.getId().toString()))
                : urls.plain("/v1/briefings/" + run.getBriefingId() + "/runs/" + run.getId() + "?format=html");
        Instant updated = run.getFinishedAt() == null ? run.getStartedAt() : run.getFinishedAt();
        String title = run.getBriefingName() + " — " + run.getPeriodTo().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                + (BriefingRunEntity.FAILED.equals(run.getStatus()) ? " (failed)" : "");
        String markdown = run.getContentMarkdown() == null ? "" : run.getContentMarkdown();
        return new FeedEntry(TAG_PREFIX + installId.installId() + "/briefing-run/" + run.getId(), title, link,
                run.getStartedAt(), updated, sanitizer.html(markdown), sanitizer.text(markdown),
                List.of(new FeedEntry.Category(run.getStatus(), TAG_PREFIX + "status")), null, null);
    }

    /** The short title of an alert: the rule name, else the message's first line, else {@code Alert {id}}. */
    static String alertTitle(AlertView alert) {
        if (alert.ruleName() != null && !alert.ruleName().isBlank()) {
            return alert.ruleName().strip();
        }
        if (alert.message() != null && !alert.message().isBlank()) {
            String first = alert.message().strip().lines().findFirst().orElse("").strip();
            if (!first.isEmpty()) {
                return first.length() > 200 ? first.substring(0, 200) : first;
            }
        }
        return "Alert " + alert.id();
    }

    /**
     * The strong ETag of a feed: a hash of the newest change, the highest entry id, the filters and the
     * entry count.
     */
    public static String etag(Feed feed, String filters) {
        String maxId = feed.entries().stream().map(FeedEntry::id).max(String::compareTo).orElse("");
        return ETags.forDocument(feed.updated() + "|" + maxId + "|" + feed.entries().size() + "|" + filters);
    }

    /** The {@code Last-Modified} value of a feed. */
    public static String lastModified(Feed feed) {
        return HTTP_DATE.format(feed.updated());
    }

    /**
     * True when the request's validators say the client has this feed: {@code If-None-Match} matches the
     * ETag, or, without {@code If-None-Match}, {@code If-Modified-Since} is not before the newest change.
     */
    public static boolean notModified(Feed feed, String etag, String ifNoneMatch, String ifModifiedSince) {
        if (ifNoneMatch != null && !ifNoneMatch.isBlank()) {
            return ETags.matches(ifNoneMatch, etag);
        }
        if (ifModifiedSince == null || ifModifiedSince.isBlank()) {
            return false;
        }
        try {
            Instant since = java.time.ZonedDateTime.parse(ifModifiedSince.strip(), DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant();
            return !feed.updated().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).isAfter(since);
        } catch (java.time.format.DateTimeParseException e) {
            return false;
        }
    }

    private JsonNode snapshotGeometry(String snapshot) {
        if (snapshot == null || snapshot.isBlank()) {
            return null;
        }
        try {
            JsonNode geometry = json.readTree(snapshot).get("geometry");
            return geometry == null || !geometry.isObject() ? null : geometry;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Instant newest(List<FeedEntry> entries) {
        Instant newest = Instant.EPOCH;
        for (FeedEntry entry : entries) {
            if (entry.updated() != null && entry.updated().isAfter(newest)) {
                newest = entry.updated();
            }
        }
        return newest;
    }

    private ServerSettings server() {
        return settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
    }
}
