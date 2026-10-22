package ai.foresignal.source.adapters.gdacs;

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
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Multi-hazard disaster alerts from GDACS, the Global Disaster Alert and Coordination System (prompt 067).
 *
 * <p>Verified 2026-10: {@code GET /gdacsapi/api/events/geteventlist/MAP?eventtype={T}} returns a GeoJSON
 * FeatureCollection of the current events of one type. {@code eventtype} is <strong>required</strong> and takes a
 * single value: without it GDACS answers 400 {@code {"message":"Eventtype is required."}}, and a
 * comma-separated list is refused the same way. So the adapter sends one request per configured type
 * ({@code eventTypes}, default all six), <strong>sequentially</strong>, in the order given; a 400 or any
 * other failure is surfaced and fails the run. The response carries no {@code ETag} or
 * {@code Last-Modified}.
 *
 * <p><strong>Expiry, not tombstones.</strong> Every poll re-observes the listed events (a touch when nothing
 * changed); an event missing from the list for {@code recordTtl} (48 h, anchored on receipt) simply expires.
 * Green alerts are kept (World Monitor drops them; Foresignal lets triggers filter on {@code alertLevel}).
 *
 * <p><strong>Terms.</strong> GDACS publishes terms of use with a disclaimer and no explicit license: its alerts
 * are computed automatically, are not reviewed, and are no substitute for official warnings.
 */
@Component
public class GdacsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "gdacs";

    /** The record kind. */
    public static final String KIND = GdacsEventParser.KIND;

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://www.gdacs.org";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.gdacs.base-url";

    /** The event list path, before the query. */
    public static final String PATH = "/gdacsapi/api/events/geteventlist/MAP";

    /** The {@code eventTypes} parameter. */
    public static final String EVENT_TYPES_PARAM = "eventTypes";

    /** GDACS's terms of use (March 2025). */
    public static final String TERMS_URL = "https://www.gdacs.org/documents/2025/GDACS_Terms_of_use_Mar_25.pdf";

    /** How long an event stays current after it was last listed. */
    static final Duration RECORD_TTL = Duration.ofHours(48);

    /** Raw retention: disasters are the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     */
    public GdacsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The request URL for one event type. */
    public URI eventListUri(String eventType) {
        return URI.create(baseUrl + PATH + "?eventtype=" + eventType);
    }

    /** Refuses an explicitly empty {@code eventTypes} list. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params.has(EVENT_TYPES_PARAM) && params.stringList(EVENT_TYPES_PARAM).isEmpty()) {
            problems.put(EVENT_TYPES_PARAM, "must name at least one of " + String.join(", ", GdacsEventParser.EVENT_TYPES));
        }
        return problems;
    }

    /** The configured types, upper-cased, de-duplicated, in the order given; all six when none are set. */
    static List<String> eventTypes(ValidatedParams params) throws AdapterException {
        List<String> raw = params.has(EVENT_TYPES_PARAM) ? params.stringList(EVENT_TYPES_PARAM) : List.of();
        Set<String> out = new LinkedHashSet<>();
        for (String value : raw) {
            String type = value == null ? "" : value.strip().toUpperCase(Locale.ROOT);
            if (type.isEmpty()) {
                continue;
            }
            if (!GdacsEventParser.EVENT_TYPES.contains(type)) {
                throw new AdapterException("gdacs eventTypes value '" + value + "' is not one of "
                        + GdacsEventParser.EVENT_TYPES);
            }
            out.add(type);
        }
        return out.isEmpty() ? GdacsEventParser.EVENT_TYPES : List.copyOf(out);
    }

    /**
     * Fetches each configured type in turn.
     *
     * @return every event of every type, complete (informational: nothing is tombstoned)
     * @throws AdapterException when a request fails, including a 400; {@link UpstreamFormatException} when a
     *     body is truncated or not a FeatureCollection
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        List<NormalizedRecord> out = new ArrayList<>();
        for (String type : eventTypes(ctx.params())) {
            UpstreamResponse response = ctx.http().get(eventListUri(type), Map.of("Accept", "application/json"));
            if (response.truncated()) {
                throw new UpstreamFormatException("GDACS " + type + " list exceeded maxResponseBytes", null);
            }
            GdacsEventParser.ParseResult parsed = GdacsEventParser.parse(response.body(), type);
            ctx.log().debug("GDACS {}: {} event(s) from {} feature(s), {} non-point, {} skipped", type,
                    parsed.records().size(), parsed.features(), parsed.nonPoint(), parsed.skipped());
            out.addAll(parsed.records());
        }
        return FetchResult.complete(out);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("GDACS Disasters")
                .description("Current earthquakes, tropical cyclones, floods, volcanoes, droughts and wildfires from"
                        + " GDACS, the Global Disaster Alert and Coordination System, with Green, Orange and Red alert"
                        + " levels (Green kept). GDACS disclaimer: alerts are computed automatically, are not reviewed,"
                        + " and are no substitute for official warnings. An event missing from the list for 48 h"
                        + " expires.")
                .kind(KIND)
                .homepage("https://www.gdacs.org/")
                .attribution("GDACS, a cooperation framework of the United Nations and the European Commission")
                .licence("GDACS terms of use (disclaimer; no explicit licence)")
                .termsUrl(TERMS_URL)
                .mode(Mode.POLLING)
                .param(new ParamSpec(EVENT_TYPES_PARAM, ParamType.STRING_LIST,
                        "Event types fetched, one request each, in this order: EQ earthquake, TC tropical cyclone,"
                                + " FL flood, VO volcano, DR drought, WF wildfire",
                        GdacsEventParser.EVENT_TYPES, false, GdacsEventParser.EVENT_TYPES))
                .interval(Duration.ofMinutes(10), Duration.ofMinutes(15))
                .defaultBudget(6, 700)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(RECORD_TTL)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .titleField("name")
                .embedFields("name", "description")
                .searchFields("name", "country")
                .tileProperties("alertLevel", "eventType", "alertScore")
                .rankField("alertScore")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{name}", "danger", "#E8590C", null));

        string(builder, "eventType", "Event type", "EQ, TC, FL, VO, DR or WF");
        integer(builder, "eventId", "Event id", "GDACS event id");
        integer(builder, "episodeId", "Episode id", "GDACS episode id; a new episode updates the event");
        string(builder, "name", "Name", "Event name, for example Earthquake in Panama");
        string(builder, "description", "Description", "GDACS event description; untrusted upstream text");
        string(builder, "alertLevel", "Alert level", "Green, Orange or Red (event)");
        builder.fieldSpec("alertScore", new FieldSpec(FieldType.NUMBER, "1", "Alert score",
                "GDACS event alert score", null, Set.of()));
        string(builder, "episodeAlertLevel", "Episode alert level", "Green, Orange or Red (latest episode)");
        builder.fieldSpec("severity", new FieldSpec(FieldType.NUMBER, null, "Severity",
                "Severity value in severityUnit (for example magnitude M or wind km/h)", null, Set.of()));
        string(builder, "severityText", "Severity text", "Severity in words");
        string(builder, "severityUnit", "Severity unit", "Unit of severity as GDACS writes it");
        string(builder, "country", "Country", "Country name(s)");
        string(builder, "iso3", "ISO3", "ISO 3166-1 alpha-3 code of the main country");
        builder.fieldSpec("affectedCountries", new FieldSpec(FieldType.ARRAY, null, "Affected countries",
                "ISO 3166-1 alpha-3 codes of every affected country", null, Set.of()));
        timestamp(builder, "fromDate", "From", "Event start (eventTime)");
        timestamp(builder, "toDate", "To", "Event end or latest episode time");
        bool(builder, "isCurrent", "Current", "GDACS considers the event current");
        bool(builder, "isTemporary", "Temporary", "GDACS marks the event temporary");
        string(builder, "glide", "GLIDE", "GLIDE number, when assigned");
        string(builder, "reportUrl", "Report", "GDACS event report page");
        bool(builder, "automated", "Automated", "Always true: GDACS alerts are automatic and unreviewed");
        return builder.build();
    }

    private static void string(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    private static void integer(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.INTEGER, "1", label, description, null, Set.of()));
    }

    private static void timestamp(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.TIMESTAMP, null, label, description, null, Set.of()));
    }

    private static void bool(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.BOOLEAN, null, label, description, null, Set.of()));
    }
}
