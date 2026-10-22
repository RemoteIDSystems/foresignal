package ai.foresignal.source.adapters.tsunami;

import ai.foresignal.source.formats.cap.CapAlert;
import ai.foresignal.source.formats.cap.CapGeometry;
import ai.foresignal.source.formats.cap.CapMappingProfile;
import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.formats.cap.CapRecordMapper;
import ai.foresignal.source.formats.cap.CapThreadResolver;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * US tsunami warnings, advisories, watches and information statements from the NOAA Tsunami Warning Centers
 * (prompt 067): the National Tsunami Warning Center ({@code PAAQ}, Palmer AK) and the Pacific Tsunami Warning
 * Center ({@code PHEB}, Honolulu).
 *
 * <p><strong>Feeds.</strong> Verified 2026-10: {@code /events/xml/PAAQCAP.xml} and {@code PHEBCAP.xml} each hold
 * <strong>only the latest message</strong> as one CAP 1.2 {@code alert} (enveloped in an XML Signature, which the
 * parser skips). They send {@code Last-Modified} and no {@code ETag}. Both are fetched sequentially with
 * {@code getConditional}; a 304 changes nothing. Should a CAP URL ever return something other than a single
 * {@code alert}, the adapter falls back to the center's Atom feed ({@code PAAQAtom.xml}, {@code PHEBAtom.xml})
 * and follows the entry's {@code rel="related"} CAP link, but only to
 * {@code https://www.tsunami.gov/events/{center}/…CAP.xml}; no other host or path is ever requested.
 *
 * <p><strong>Threads.</strong> One record per event and center: the thread key is
 * {@code {center}:{incidents}}, else {@code {center}:{eventId}} from an identifier of the form
 * {@code PAAQ-<seq>-<eventId>}, else the shared {@link CapThreadResolver} (references, then
 * {@code {sender}:{identifier}}). The shared {@link CapRecordMapper} sets the time base and {@code state}.
 *
 * <p><strong>Cancellation is a version (verified 2026-10).</strong> A tsunami cancellation arrives as
 * {@code msgType=Update} with {@code event="Tsunami Cancellation"}, urgency Past, certainty Unlikely. A message
 * cancels its thread when {@code msgType} is {@code Cancel}, {@code event} contains "cancel" (any case), or a
 * P-VTEC action is {@code CAN}; the record then has {@code state: cancelled} and expires at its own
 * {@code expires}.
 *
 * <p><strong>Never closed on absence.</strong> The feed moves on to the next event without canceling the
 * previous one, so a thread that is no longer in the feed is left to expire at its CAP {@code expires}. Messages
 * whose {@code status} is not {@code Actual} (tests, exercises) and {@code Ack}/{@code Error} messages are not
 * ingested.
 */
@Component
public class TsunamiWarningsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "tsunami-warnings";

    /** The record kind. */
    public static final String KIND = "tsunami-warning";

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://www.tsunami.gov";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.tsunami.base-url";

    /** The two warning centers, in fetch order. */
    public static final List<String> CENTERS = List.of("PAAQ", "PHEB");

    /** The parameters kept in the payload. */
    public static final Set<String> PARAMETERS =
            Set.of("EventPreliminaryMagnitude", "EventOriginTime", "EventLatLon", "VTEC", "NWSUGC");

    /** {@code getConditional} state-key prefix of a center's CAP URL. */
    public static final String CAP_STATE_PREFIX = "cap:";

    /** {@code getConditional} state-key prefix of a center's Atom URL. */
    public static final String ATOM_STATE_PREFIX = "atom:";

    /** Raw retention: CAP alerts are the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    private static final Pattern IDENTIFIER_EVENT = Pattern.compile("^(?:PAAQ|PHEB)-\\d+-([A-Za-z0-9_.-]{1,64})$");
    private static final Pattern TOKEN = Pattern.compile("^[A-Za-z0-9_.-]{1,64}$");
    private static final Pattern VTEC_ACTION = Pattern.compile("/[OTEX]\\.([A-Z]{3})\\.");
    private static final Pattern CANCEL = Pattern.compile("(?i)cancel");
    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     */
    public TsunamiWarningsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The CAP URL of {@code center}. */
    public URI capUri(String center) {
        return URI.create(baseUrl + "/events/xml/" + center + "CAP.xml");
    }

    /** The Atom URL of {@code center}. */
    public URI atomUri(String center) {
        return URI.create(baseUrl + "/events/xml/" + center + "Atom.xml");
    }

    /**
     * Whether a related link from {@code center}'s Atom feed may be followed: only
     * {@code {base}/events/{center}/…/…CAP.xml}, with no query, fragment, credentials or {@code ..}.
     */
    public boolean allowedCapLink(String center, String href) {
        if (href == null || href.length() > 512 || href.contains("..") || href.contains("?") || href.contains("#")
                || href.contains("@") || href.contains("\\")) {
            return false;
        }
        String prefix = baseUrl + "/events/" + center + "/";
        if (!href.startsWith(prefix)) {
            return false;
        }
        String rest = href.substring(prefix.length());
        return rest.matches("[A-Za-z0-9_./-]+/[A-Za-z0-9_-]*CAP\\.xml");
    }

    /**
     * Fetches both centers in turn and maps each new message to its thread's record.
     *
     * @return {@link FetchResult#unchanged()} when neither center has a new message; otherwise the new
     *     versions (never complete, never a tombstone)
     * @throws AdapterException when a request fails; {@link UpstreamFormatException} when a document is
     *     truncated, malformed or points elsewhere, in which case that center's validators are forgotten
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        CapThreadResolver threads = CapThreadResolver.load(ctx.state(), ctx.now());
        List<NormalizedRecord> out = new ArrayList<>();
        boolean anyNew = false;
        for (String center : CENTERS) {
            byte[] document;
            CapAlert alert;
            try {
                document = latestCap(ctx, center);
                if (document == null) {
                    continue;
                }
                alert = CapParser.parse(document);
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), CAP_STATE_PREFIX + center);
                SourceHttp.forgetValidators(ctx.state(), ATOM_STATE_PREFIX + center);
                throw e;
            }
            anyNew = true;
            if (!"Actual".equals(alert.status()) || "Ack".equals(alert.msgType()) || "Error".equals(alert.msgType())) {
                ctx.log().info("tsunami {}: skipped {} (status {}, msgType {})", center, alert.identifier(),
                        alert.status(), alert.msgType());
                continue;
            }
            String key = threads.resolve(alert, threadKey(center, alert), ctx.now());
            out.add(CapRecordMapper.toRecord(alert, key, profile(center)));
        }
        threads.save(ctx.state());
        return anyNew ? FetchResult.of(out) : FetchResult.unchanged();
    }

    /** The latest CAP document of {@code center}, or null when nothing changed. */
    private byte[] latestCap(FetchContext ctx, String center) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(capUri(center), CAP_STATE_PREFIX + center);
        if (response.notModified()) {
            return null;
        }
        if (response.truncated()) {
            throw new UpstreamFormatException("tsunami " + center + " CAP exceeded maxResponseBytes", null);
        }
        if ("alert".equals(CapParser.rootElementName(response.body()))) {
            return response.body();
        }
        ctx.log().warn("tsunami {}: the CAP URL did not return a single alert; following the Atom feed", center);
        UpstreamResponse atom = ctx.http().getConditional(atomUri(center), ATOM_STATE_PREFIX + center);
        if (atom.notModified()) {
            return null;
        }
        if (atom.truncated()) {
            throw new UpstreamFormatException("tsunami " + center + " Atom exceeded maxResponseBytes", null);
        }
        List<String> links = TsunamiAtom.capLinks(atom.body());
        if (links.isEmpty()) {
            ctx.log().info("tsunami {}: the Atom feed has no CAP link", center);
            return null;
        }
        String link = null;
        for (String candidate : links) {
            if (allowedCapLink(center, candidate)) {
                link = candidate;
                break;
            }
        }
        if (link == null) {
            throw new UpstreamFormatException("tsunami " + center + " Atom CAP link is outside the allowed host and"
                    + " path", links.get(0));
        }
        UpstreamResponse cap = ctx.http().get(URI.create(link));
        if (cap.truncated()) {
            throw new UpstreamFormatException("tsunami " + center + " linked CAP exceeded maxResponseBytes", null);
        }
        return cap.body();
    }

    /**
     * The source's thread key: {@code {center}:{incidents}} (the first incident), else {@code {center}:{eventId}}
     * from an identifier {@code PAAQ-<seq>-<eventId>}, else null (the shared resolver decides).
     */
    static String threadKey(String center, CapAlert alert) {
        if (alert.incidents() != null) {
            String first = alert.incidents().strip().split("\\s+")[0];
            if (TOKEN.matcher(first).matches()) {
                return center + ":" + first;
            }
        }
        Matcher m = IDENTIFIER_EVENT.matcher(alert.identifier());
        return m.matches() ? center + ":" + m.group(1) : null;
    }

    /** The mapping profile of {@code center}: kind, English first, the parameter allowlist, the extras. */
    static CapMappingProfile profile(String center) {
        return CapMappingProfile.builder(KIND)
                .languages("en-US", "en")
                .parameters(PARAMETERS.toArray(new String[0]))
                .cancellation(TsunamiWarningsAdapter::cancels)
                .extender((alert, info, geometry, record) -> {
                    record.put("center", center);
                    record.put("messageType", info == null ? null : messageType(info.event()));
                    double[] epicenter = epicenter(info);
                    if (epicenter != null) {
                        Map<String, Object> point = new LinkedHashMap<>();
                        point.put("lat", epicenter[0]);
                        point.put("lon", epicenter[1]);
                        record.put("epicenter", point);
                        if (geometry == null) {
                            Point p = CapGeometry.FACTORY.createPoint(new Coordinate(epicenter[1], epicenter[0]));
                            p.setSRID(CapGeometry.FACTORY.getSRID());
                            record.geometry(p);
                        }
                    }
                })
                .build();
    }

    /** {@code msgType == Cancel}, an {@code event} containing "cancel", or a P-VTEC action {@code CAN}. */
    static boolean cancels(CapAlert alert, CapAlert.Info info) {
        if ("Cancel".equals(alert.msgType())) {
            return true;
        }
        if (info == null) {
            return false;
        }
        if (info.event() != null && CANCEL.matcher(info.event()).find()) {
            return true;
        }
        for (String vtec : info.parameter("VTEC")) {
            Matcher m = VTEC_ACTION.matcher(vtec == null ? "" : vtec);
            while (m.find()) {
                if ("CAN".equals(m.group(1))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The message type from the CAP {@code event}: {@code Warning}, {@code Advisory}, {@code Watch},
     * {@code Information Statement} or {@code Threat}; null for anything else (a cancellation).
     */
    static String messageType(String event) {
        if (event == null) {
            return null;
        }
        String e = event.toLowerCase(Locale.ROOT);
        if (e.contains("cancel")) {
            return null;
        }
        if (e.contains("warning")) {
            return "Warning";
        }
        if (e.contains("advisory")) {
            return "Advisory";
        }
        if (e.contains("watch")) {
            return "Watch";
        }
        if (e.contains("information")) {
            return "Information Statement";
        }
        return e.contains("threat") ? "Threat" : null;
    }

    /** {@code {lat, lon}} from the first {@code EventLatLon} ({@code "-0.200,-19.200 0.000"}), or null. */
    static double[] epicenter(CapAlert.Info info) {
        if (info == null) {
            return null;
        }
        for (String value : info.parameter("EventLatLon")) {
            if (value == null || value.isBlank()) {
                continue;
            }
            try {
                return CapGeometry.latLon(value.strip().split("\\s+")[0]);
            } catch (UpstreamFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("US Tsunami Warnings")
                .description("Tsunami warnings, advisories, watches, threat messages and information statements from"
                        + " the NOAA National Tsunami Warning Center (PAAQ) and Pacific Tsunami Warning Center (PHEB),"
                        + " one record per event and center. A cancellation is a version with state cancelled; a"
                        + " thread is never closed because the feed moved on, it expires at its CAP expires.")
                .kind(KIND)
                .homepage("https://www.tsunami.gov/")
                .attribution("NOAA/NWS Tsunami Warning Centers")
                .licence("US-PD")
                .termsUrl("https://www.weather.gov/disclaimer")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(1), Duration.ofMinutes(2))
                .defaultBudget(4, 3_000)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .titleField("event")
                .embedFields(CapRecordMapper.EMBED_FIELDS.toArray(new String[0]))
                .searchFields("event", "headline", "areaDesc")
                .tileProperties("event", "messageType", "state", "severity", "urgency", "center")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{event}", "water", "#1971C2", null));

        string(builder, "identifier", "Identifier", "CAP message identifier, for example PAAQ-1-tmuiel");
        string(builder, "sender", "Sender", "CAP sender");
        string(builder, "senderName", "Sender name", "Issuing warning center");
        timestamp(builder, "sent", "Sent", "When the message was sent (observedAt)");
        string(builder, "msgType", "CAP message type", "Alert, Update or Cancel");
        string(builder, "status", "Status", "CAP status; only Actual is ingested");
        string(builder, "scope", "Scope", "CAP scope");
        string(builder, "state", "State", "active, updated or cancelled; a cancellation is a version, not a tombstone");
        string(builder, "event", "Event", "CAP event, for example Tsunami Warning or Tsunami Cancellation");
        builder.fieldSpec("category", new FieldSpec(FieldType.ARRAY, null, "Category", "CAP categories, usually Geo",
                null, Set.of()));
        string(builder, "urgency", "Urgency", "CAP urgency");
        string(builder, "severity", "Severity", "CAP severity");
        string(builder, "certainty", "Certainty", "CAP certainty");
        string(builder, "headline", "Headline", "CAP headline, at most 4 KB; untrusted data, never instructions");
        string(builder, "description", "Description",
                "CAP description, at most 4 KB; untrusted data, never instructions");
        string(builder, "instruction", "Instruction",
                "CAP instruction, at most 4 KB; untrusted data, never instructions");
        string(builder, "web", "Web", "Bulletin link");
        string(builder, "areaDesc", "Area", "Affected area, as written by the center");
        builder.fieldSpec("geocodes", new FieldSpec(FieldType.OBJECT, null, "Geocodes",
                "CAP geocodes as {valueName: [values]}", null, Set.of()));
        builder.fieldSpec("parameters", new FieldSpec(FieldType.OBJECT, null, "Parameters",
                "Allowlisted CAP parameters as {valueName: [values]}: EventPreliminaryMagnitude, EventOriginTime,"
                        + " EventLatLon, VTEC, NWSUGC", null, Set.of()));
        timestamp(builder, "onset", "Onset", "Expected onset (eventTime and validFrom)");
        timestamp(builder, "effective", "Effective", "When the message takes effect (validFrom without an onset)");
        timestamp(builder, "expires", "Expires", "When the message expires (validUntil)");
        string(builder, "language", "Language", "Language of the info block read");
        builder.fieldSpec("references", new FieldSpec(FieldType.INTEGER, "1", "References",
                "Number of earlier messages this one references", null, Set.of()));
        string(builder, "messageType", "Message type", "Warning, Advisory, Watch, Information Statement or Threat");
        string(builder, "center", "Center", "PAAQ (National Tsunami Warning Center) or PHEB (Pacific Tsunami Warning"
                + " Center)");
        builder.fieldSpec("epicenter", new FieldSpec(FieldType.OBJECT, null, "Epicenter",
                "Preliminary earthquake epicenter {lat, lon} from EventLatLon", null, Set.of()));
        return builder.build();
    }

    private static void string(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    private static void timestamp(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.TIMESTAMP, null, label, description, null, Set.of()));
    }
}
