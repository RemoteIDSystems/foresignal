package ai.foresignal.source.adapters.maritime;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Global Fishing Watch vessel events ({@code gfw-events}, prompt 088): apparent fishing, encounters, loitering and
 * port visits from the GFW Events API v3, under GFW's non-commercial license (CC BY-NC 4.0).
 *
 * <p><strong>Request</strong> (GFW API documentation, read 2026-10-14): {@code GET {@value #API_URL}?datasets[0]=
 * {dataset}&start-date={d}&end-date={d}&limit=500&offset={n}} with the required credential {@value #TOKEN} as
 * {@code Authorization: Bearer} (never in a URL). With param {@value #BBOX_PARAM} the documented POST form is used
 * instead: {@code POST {@value #API_URL}?offset={n}&limit=500} with {@code {"datasets":[…],"startDate":…,"endDate":…,
 * "geometry":{Polygon}}}. A 401 or 403 is {@link CredentialsRejectedException}; a 429 puts the source in backoff
 * through {@code Retry-After} in the upstream client.
 *
 * <p><strong>Window and paging:</strong> each run reads the last {@value #WINDOW_PARAM} days (default 10; GFW
 * publishes with a lag of several days), one dataset after another, following {@code nextOffset} until {@code total}.
 * At most {@value #MAX_PAGES_PER_RUN} pages are read per run; a run cut there stores its place ({@value #CURSOR_KEY})
 * and the next run resumes the same window, so 24 hourly runs stay inside the 300-a-day budget. Re-read events are
 * identical and the ingest hash drops them.
 *
 * <p><strong>Record:</strong> kind by event type ({@code fishing} {@value #FISHING_KIND}, {@code encounter}
 * {@value #ENCOUNTER_KIND}, {@code loitering} {@value #LOITERING_KIND}, {@code port_visit} {@value #PORT_VISIT_KIND};
 * other types are skipped), externalId the event {@code id}, {@code eventTime} = {@code start}, {@code observedAt} =
 * {@code end} (else {@code start}), Point from {@code position}, payload {@code start, end} (the interval fields),
 * {@code vesselId, mmsi} ({@code ssvid}), {@code vesselName, flag, durationH, distanceKm}, {@code encounterMmsi} (the
 * other vessel of an encounter) and {@code portName} (a port visit's anchorage).
 *
 * <p>1-hour poll (minimum 30 minutes), budget 10/min and 300/day, retention 1825 days ({@code event}).
 */
@Component
public class GfwEventsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "gfw-events";

    /** The events endpoint. */
    public static final String API_URL = "https://gateway.api.globalfishingwatch.org/v3/events";

    /** The token credential. */
    public static final String TOKEN = "apiToken";

    /** The datasets parameter. */
    public static final String DATASETS_PARAM = "datasets";

    /** The window parameter. */
    public static final String WINDOW_PARAM = "windowDays";

    /** The box parameter. */
    public static final String BBOX_PARAM = "bbox";

    /** Apparent fishing events. */
    public static final String FISHING_DATASET = "public-global-fishing-events:latest";

    /** Encounters between vessels. */
    public static final String ENCOUNTERS_DATASET = "public-global-encounters-events:latest";

    /** Loitering events. */
    public static final String LOITERING_DATASET = "public-global-loitering-events:latest";

    /** Port visits. */
    public static final String PORT_VISITS_DATASET = "public-global-port-visits-events:latest";

    /** The datasets the source may read (the event datasets of the GFW key-concepts page). */
    public static final List<String> ALLOWED_DATASETS = List.of(FISHING_DATASET, ENCOUNTERS_DATASET, LOITERING_DATASET,
            PORT_VISITS_DATASET);

    /** The fishing kind. */
    public static final String FISHING_KIND = "fishing-event";

    /** The encounter kind. */
    public static final String ENCOUNTER_KIND = "vessel-encounter";

    /** The loitering kind. */
    public static final String LOITERING_KIND = "loitering-event";

    /** The port visit kind. */
    public static final String PORT_VISIT_KIND = "port-visit";

    /** GFW event type to record kind. */
    static final Map<String, String> KINDS = Map.of("fishing", FISHING_KIND, "encounter", ENCOUNTER_KIND,
            "loitering", LOITERING_KIND, "port_visit", PORT_VISIT_KIND);

    /** The license: GFW's non-commercial terms. */
    public static final String LICENSE = "CC BY-NC 4.0";

    /** The page size. */
    public static final int LIMIT = 500;

    /** Pages per run. */
    public static final int MAX_PAGES_PER_RUN = 10;

    /** The default window, days. */
    public static final int DEFAULT_WINDOW_DAYS = 10;

    /** The largest window, days. */
    public static final int MAX_WINDOW_DAYS = 30;

    /** The state key of an unfinished run's place. */
    public static final String CURSOR_KEY = "gfw:cursor";

    /** An event id. */
    static final Pattern EVENT_ID = Pattern.compile("^[A-Za-z0-9._:-]{1,128}$");

    private final URI api;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value MaritimeSources#BASE_URL_PROPERTY}; blank in production
     */
    public GfwEventsAdapter(@Value("${" + MaritimeSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.api = MaritimeSources.uri(API_URL, baseUrlOverride == null ? "" : baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Accepts only the event datasets and a window of 1 to 30 days. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params == null) {
            return problems;
        }
        if (params.has(DATASETS_PARAM)) {
            List<String> datasets = params.stringList(DATASETS_PARAM);
            if (datasets == null || datasets.isEmpty()) {
                problems.put(DATASETS_PARAM, "name at least one dataset");
            } else if (!ALLOWED_DATASETS.containsAll(datasets.stream().map(String::strip).toList())) {
                problems.put(DATASETS_PARAM, "allowed: " + String.join(", ", ALLOWED_DATASETS));
            }
        }
        if (params.has(WINDOW_PARAM)) {
            Integer days = params.integer(WINDOW_PARAM);
            if (days == null || days < 1 || days > MAX_WINDOW_DAYS) {
                problems.put(WINDOW_PARAM, "must be between 1 and " + MAX_WINDOW_DAYS);
            }
        }
        return problems;
    }

    /**
     * Reads the window's events, dataset by dataset, resuming an unfinished run.
     *
     * @throws CredentialsRejectedException when the token is missing or refused (401, 403)
     * @throws AdapterException when a request fails or the parameters are invalid
     * @throws UpstreamFormatException when a page does not parse
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        String token = ctx.credentials().get(TOKEN);
        if (token == null || token.isBlank()) {
            throw new CredentialsRejectedException(ID + " needs the " + TOKEN + " credential (a Global Fishing Watch"
                    + " API access token)");
        }
        ValidatedParams params = ctx.params() == null ? ValidatedParams.empty() : ctx.params();
        Map<String, String> problems = checkParams(params);
        if (!problems.isEmpty()) {
            throw new AdapterException(ID + ": " + problems);
        }
        List<String> datasets = datasets(params);
        int window = params.has(WINDOW_PARAM) ? params.integer(WINDOW_PARAM) : DEFAULT_WINDOW_DAYS;
        Envelope box = params.has(BBOX_PARAM) ? params.bbox(BBOX_PARAM) : null;
        String scope = String.join(",", datasets) + "|" + window + "|" + (box == null ? "" : boxText(box));
        AdapterStateStore state = ctx.state();
        Cursor cursor = Cursor.read(state.get(CURSOR_KEY).orElse(null));
        if (cursor == null || !cursor.scope().equals(scope) || cursor.dataset() >= datasets.size()) {
            LocalDate today = ctx.now().atZone(ZoneOffset.UTC).toLocalDate();
            cursor = new Cursor(scope, today.minusDays(window), today, 0, 0);
        }
        Map<String, String> headers = Map.of("Authorization", "Bearer " + token.strip());
        List<NormalizedRecord> records = new ArrayList<>();
        int pages = 0;
        int skipped = 0;
        for (int d = cursor.dataset(); d < datasets.size(); d++) {
            int offset = d == cursor.dataset() ? cursor.offset() : 0;
            while (true) {
                if (pages >= MAX_PAGES_PER_RUN) {
                    state.put(CURSOR_KEY, new Cursor(scope, cursor.start(), cursor.end(), d, offset).write());
                    ctx.log().info("{}: {} pages read; the next run resumes {} at offset {}", ctx.sourceId(), pages,
                            datasets.get(d), offset);
                    return FetchResult.of(records);
                }
                JsonNode page = page(ctx, datasets.get(d), cursor.start(), cursor.end(), offset, box, headers);
                pages++;
                JsonNode entries = page.get("entries");
                if (entries == null || !entries.isArray()) {
                    throw new UpstreamFormatException("GFW events page has no entries array", "");
                }
                for (JsonNode entry : entries) {
                    NormalizedRecord record = record(entry);
                    if (record == null) {
                        skipped++;
                    } else {
                        records.add(record);
                    }
                }
                Long total = MaritimeSources.integer(page.get("total"));
                Long next = MaritimeSources.integer(page.get("nextOffset"));
                if (entries.isEmpty() || next == null || next <= offset || (total != null && next >= total)) {
                    break;
                }
                offset = (int) Math.min(Integer.MAX_VALUE, next);
            }
        }
        state.remove(CURSOR_KEY);
        ctx.log().debug("{}: {} event(s) from {} page(s), {} skipped", ctx.sourceId(), records.size(), pages, skipped);
        return FetchResult.of(records);
    }

    /** One page: the GET form, or the POST form with a box. */
    private JsonNode page(FetchContext ctx, String dataset, LocalDate start, LocalDate end, int offset, Envelope box,
            Map<String, String> headers) throws AdapterException {
        UpstreamResponse response;
        try {
            if (box == null) {
                response = ctx.http().get(getUri(dataset, start, end, offset), headers);
            } else {
                response = ctx.http().post(postUri(offset), "application/json",
                        postBody(dataset, start, end, box).getBytes(StandardCharsets.UTF_8), headers);
            }
        } catch (CredentialsRejectedException e) {
            throw e;
        } catch (AdapterException e) {
            int status = e.httpStatus().orElse(0);
            if (status == 401 || status == 403) {
                throw new CredentialsRejectedException("Global Fishing Watch refused the token (HTTP " + status
                        + "); check the " + TOKEN + " credential", e);
            }
            throw e;
        }
        return MaritimeSources.json(MaritimeSources.body(response, "GFW events page"), "GFW events page");
    }

    /**
     * The GET URL of one page.
     *
     * @param dataset the dataset
     * @param start the first day
     * @param end the last day
     * @param offset the offset
     * @return the URL
     */
    URI getUri(String dataset, LocalDate start, LocalDate end, int offset) {
        String url = api.toString();
        url = KitUrls.withParam(url, "datasets[0]", dataset);
        url = KitUrls.withParam(url, "start-date", start.toString());
        url = KitUrls.withParam(url, "end-date", end.toString());
        url = KitUrls.withParam(url, "limit", LIMIT);
        url = KitUrls.withParam(url, "offset", offset);
        return URI.create(url);
    }

    /**
     * The POST URL of one page.
     *
     * @param offset the offset
     * @return the URL
     */
    URI postUri(int offset) {
        return URI.create(KitUrls.withParam(KitUrls.withParam(api.toString(), "offset", offset), "limit", LIMIT));
    }

    /**
     * The POST body: the dataset, the dates and the box as a GeoJSON Polygon.
     *
     * @param dataset the dataset
     * @param start the first day
     * @param end the last day
     * @param box the box
     * @return the JSON text
     */
    static String postBody(String dataset, LocalDate start, LocalDate end, Envelope box) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.putArray("datasets").add(dataset);
        body.put("startDate", start.toString());
        body.put("endDate", end.toString());
        ObjectNode geometry = body.putObject("geometry");
        geometry.put("type", "Polygon");
        ArrayNode ring = geometry.putArray("coordinates").addArray();
        double[][] corners = {{box.getMinX(), box.getMinY()}, {box.getMaxX(), box.getMinY()},
            {box.getMaxX(), box.getMaxY()}, {box.getMinX(), box.getMaxY()}, {box.getMinX(), box.getMinY()}};
        for (double[] c : corners) {
            ring.addArray().add(c[0]).add(c[1]);
        }
        return body.toString();
    }

    /** The validated datasets, or the default. */
    static List<String> datasets(ValidatedParams params) {
        if (!params.has(DATASETS_PARAM) || params.stringList(DATASETS_PARAM) == null) {
            return List.of(FISHING_DATASET);
        }
        List<String> out = new ArrayList<>();
        for (String d : params.stringList(DATASETS_PARAM)) {
            String s = d.strip();
            if (!out.contains(s)) {
                out.add(s);
            }
        }
        return out.isEmpty() ? List.of(FISHING_DATASET) : List.copyOf(out);
    }

    /**
     * The record of one event.
     *
     * @param entry the event
     * @return the record, or null for an unknown type, a missing id or a missing start
     */
    static NormalizedRecord record(JsonNode entry) {
        String kind = KINDS.get(MaritimeSources.text(entry.get("type")));
        String id = MaritimeSources.text(entry.get("id"));
        Instant start = instant(MaritimeSources.text(entry.get("start")));
        Instant end = instant(MaritimeSources.text(entry.get("end")));
        if (kind == null || id == null || !EVENT_ID.matcher(id).matches() || start == null) {
            return null;
        }
        JsonNode vessel = entry.path("vessel");
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, kind)
                .eventTime(start)
                .observedAt(end != null && !end.isBefore(start) ? end : start)
                .put("start", start.toString())
                .put("end", end == null ? null : end.toString())
                .put("vesselId", MaritimeSources.text(vessel.get("id")))
                .put("mmsi", MaritimeSources.text(vessel.get("ssvid")))
                .put("vesselName", MaritimeSources.text(vessel.get("name")))
                .put("flag", MaritimeSources.text(vessel.get("flag")))
                .put("durationH", end == null || end.isBefore(start) ? null
                        : round(Duration.between(start, end).toSeconds() / 3600.0));
        Double distance = switch (kind) {
            case FISHING_KIND -> MaritimeSources.number(entry.path("fishing").get("totalDistanceKm"));
            case LOITERING_KIND -> MaritimeSources.number(entry.path("loitering").get("totalDistanceKm"));
            default -> null;
        };
        b.put("distanceKm", distance == null ? null : round(distance));
        if (kind.equals(ENCOUNTER_KIND)) {
            b.put("encounterMmsi", MaritimeSources.text(entry.path("encounter").path("vessel").get("ssvid")));
        }
        if (kind.equals(PORT_VISIT_KIND)) {
            JsonNode visit = entry.path("port_visit");
            String port = MaritimeSources.text(visit.path("startAnchorage").get("name"));
            if (port == null) {
                port = MaritimeSources.text(visit.path("intermediateAnchorage").get("name"));
            }
            if (port == null) {
                port = MaritimeSources.text(visit.path("endAnchorage").get("name"));
            }
            b.put("portName", port);
        }
        JsonNode position = entry.path("position");
        Double lat = MaritimeSources.number(position.get("lat"));
        Double lon = MaritimeSources.number(position.get("lon"));
        if (lat != null && lon != null && Math.abs(lat) <= 90 && Math.abs(lon) <= 180) {
            b.point(lon, lat);
        }
        return b.build();
    }

    private static Double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static Instant instant(String text) {
        try {
            return text == null ? null : Instant.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** The box as {@code west,south,east,north}. */
    static String boxText(Envelope box) {
        return box.getMinX() + "," + box.getMinY() + "," + box.getMaxX() + "," + box.getMaxY();
    }

    /**
     * Where an unfinished run stopped.
     *
     * @param scope the datasets, window and box it belongs to
     * @param start the window's first day
     * @param end the window's last day
     * @param dataset the index of the dataset being read
     * @param offset the next offset in it
     */
    record Cursor(String scope, LocalDate start, LocalDate end, int dataset, int offset) {

        /** The stored form. */
        String write() {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("scope", scope);
            node.put("start", start.toString());
            node.put("end", end.toString());
            node.put("dataset", dataset);
            node.put("offset", offset);
            return node.toString();
        }

        /** The cursor stored by {@link #write}, or null when none is stored or it cannot be read. */
        static Cursor read(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            try {
                JsonNode node = MaritimeSources.JSON.readTree(value);
                return new Cursor(node.get("scope").asString(), LocalDate.parse(node.get("start").asString()),
                        LocalDate.parse(node.get("end").asString()), node.get("dataset").intValue(),
                        node.get("offset").intValue());
            } catch (RuntimeException e) {
                return null;
            }
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Global Fishing Watch vessel events")
                .description("Vessel events from Global Fishing Watch (apparent fishing, encounters between vessels,"
                        + " loitering and port visits) over the last days, worldwide or inside a box. Needs your own"
                        + " GFW API access token (free registration); the token stays on this server. GFW publishes"
                        + " with a lag of several days. License CC BY-NC 4.0: non-commercial use only, with"
                        + " attribution to Global Fishing Watch.")
                .kinds(FISHING_KIND, ENCOUNTER_KIND, LOITERING_KIND, PORT_VISIT_KIND)
                .homepage("https://globalfishingwatch.org/our-apis/")
                .attribution("Global Fishing Watch")
                .licence(LICENSE)
                .termsUrl("https://globalfishingwatch.org/our-apis/documentation/docs/license-rate-limits")
                .mode(Mode.POLLING)
                .credential(CredentialField.required(TOKEN, "Your Global Fishing Watch API access token (register at"
                        + " https://globalfishingwatch.org/our-apis/tokens); sent as Authorization: Bearer"))
                .param(ParamSpec.optional(DATASETS_PARAM, ParamType.STRING_LIST, "Event datasets to read: "
                        + String.join(", ", ALLOWED_DATASETS), List.of(FISHING_DATASET)))
                .param(ParamSpec.optional(WINDOW_PARAM, ParamType.INTEGER, "Days read back from today, 1 to 30",
                        DEFAULT_WINDOW_DAYS))
                .param(ParamSpec.optional(BBOX_PARAM, ParamType.BBOX, "Only events inside this box,"
                        + " west,south,east,north (sent as a GeoJSON polygon); empty reads worldwide", null))
                .interval(Duration.ofMinutes(30), Duration.ofHours(1))
                .defaultBudget(Budget.of(10, 300))
                .maxResponseBytes(8 * 1024 * 1024)
                .fetchTimeout(Duration.ofSeconds(60))
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(MaritimeSources.EVENT_RETENTION)
                .kindRetention(FISHING_KIND, MaritimeSources.EVENT_RETENTION)
                .kindRetention(ENCOUNTER_KIND, MaritimeSources.EVENT_RETENTION)
                .kindRetention(LOITERING_KIND, MaritimeSources.EVENT_RETENTION)
                .kindRetention(PORT_VISIT_KIND, MaritimeSources.EVENT_RETENTION)
                .zeroResultsNormal(true)
                .intervalFields("start", "end")
                .titleField("vesselName")
                .searchFields("vesselName", "mmsi", "flag", "portName")
                .tileProperties("vesselName", "flag", "durationH")
                .kindDisplay(FISHING_KIND, new KindDisplay("{vesselName}: fishing", "ship", "#1C7ED6", null))
                .kindDisplay(ENCOUNTER_KIND, new KindDisplay("{vesselName}: encounter", "ship", "#AE3EC9", null))
                .kindDisplay(LOITERING_KIND, new KindDisplay("{vesselName}: loitering", "ship", "#F08C00", null))
                .kindDisplay(PORT_VISIT_KIND, new KindDisplay("{vesselName}: port visit", "ship", "#2F9E44", null));
        MaritimeSources.timestamp(b, "start", "Start", "When the event started, ISO-8601");
        MaritimeSources.timestamp(b, "end", "End", "When the event ended, ISO-8601");
        MaritimeSources.text(b, "vesselId", "GFW vessel id", "Global Fishing Watch's vessel id");
        MaritimeSources.text(b, "mmsi", "MMSI", "The vessel's MMSI (GFW ssvid)");
        MaritimeSources.text(b, "vesselName", "Vessel", "The vessel name");
        MaritimeSources.text(b, "flag", "Flag", "The flag state, ISO 3166-1 alpha-3, when GFW gives it");
        MaritimeSources.number(b, "durationH", "h", "Duration", "The event's duration, hours");
        MaritimeSources.number(b, "distanceKm", "km", "Distance", "The distance covered during a fishing or loitering"
                + " event, kilometers");
        MaritimeSources.text(b, "encounterMmsi", "Other vessel MMSI", "The MMSI of the other vessel of an encounter");
        MaritimeSources.text(b, "portName", "Port", "The anchorage of a port visit");
        return b.build();
    }
}
