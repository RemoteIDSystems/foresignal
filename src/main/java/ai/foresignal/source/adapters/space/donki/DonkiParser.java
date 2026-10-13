package ai.foresignal.source.adapters.space.donki;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * NASA DONKI responses ({@code https://ccmc.gsfc.nasa.gov/DONKI-API/get/{type}}) as records (prompt 077). Pure.
 *
 * <ul>
 *   <li><strong>Kinds:</strong> {@value #EVENT_KIND} for every event type, {@value #NOTICE_KIND} for
 *       {@code notifications}.
 *   <li><strong>Identity:</strong> {@code flrID}, {@code activityID} (CME, IPS), {@code gstID}, {@code sepID},
 *       {@code mpcID}, {@code rbeID}, {@code hssID}, {@code simulationID} (WSA-ENLIL) or {@code messageID}; a CME
 *       analysis has no id of its own and is keyed {@code CMEAnalysis/{n}} from its {@code link}.
 *   <li><strong>Time:</strong> {@code observedAt = submissionTime} ({@code messageIssueTime} for notices,
 *       {@code modelCompletionTime} for WSA-ENLIL simulations, which have no submission time); {@code eventTime} =
 *       {@code beginTime}, else {@code startTime}, else {@code eventTime} (a CME analysis: its CME's start; a
 *       simulation: its first input CME's start).
 *   <li><strong>Payload:</strong> {@code type, catalog, beginTime, peakTime, endTime, classType, sourceLocation,
 *       activeRegionNum, instruments} (display names), {@code kpMax} (largest GST {@code allKpIndex[].kpIndex}),
 *       {@code cmeSpeedKms, cmeHalfAngle} (the most accurate analysis), {@code linkedEvents} (ids),
 *       {@code link, versionId}; notices {@code type, messageType, messageBody} (at most {@value #MAX_BODY}
 *       characters, untrusted), {@code link}. An empty array is an empty, valid result.
 * </ul>
 */
public final class DonkiParser {

    /** The event kind. */
    public static final String EVENT_KIND = "space-weather-event";

    /** The notification kind. */
    public static final String NOTICE_KIND = "space-weather-notice";

    /** The notification type name. */
    public static final String NOTIFICATIONS = "notifications";

    /** Every DONKI type the API serves. */
    public static final List<String> TYPES = List.of("FLR", "CME", "CMEAnalysis", "GST", "IPS", "SEP", "MPC", "RBE",
            "HSS", "WSAEnlilSimulations", NOTIFICATIONS);

    /** The longest notification body kept, in characters. */
    public static final int MAX_BODY = 4096;

    /** The id members, in the order they are tried. */
    static final List<String> ID_FIELDS = List.of("flrID", "activityID", "gstID", "sepID", "mpcID", "rbeID", "hssID",
            "simulationID", "messageID");

    private static final Pattern ANALYSIS_LINK = Pattern.compile("/CMEAnalysis/(\\d+)");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One parsed response.
     *
     * @param records one record per item
     * @param skipped skip reason to count
     */
    public record Parsed(List<NormalizedRecord> records, Map<String, Integer> skipped) {}

    private DonkiParser() {}

    /**
     * Parses one type's response.
     *
     * @param type the DONKI type requested, one of {@link #TYPES}
     * @param body the response body
     * @throws UpstreamFormatException when the body is not a JSON array (the API answers {@code API Error: …} text
     *     on a bad request)
     */
    public static Parsed parse(String type, byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("DONKI " + type + " is not JSON: " + e.getOriginalMessage(),
                    new String(body, 0, Math.min(body.length, 200), java.nio.charset.StandardCharsets.UTF_8), e);
        }
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("DONKI " + type + " is not a JSON array", "");
        }
        List<NormalizedRecord> out = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        Set<String> ids = new HashSet<>();
        for (JsonNode item : root) {
            MappedRecord mapped = item(type, item);
            if (!mapped.accepted()) {
                skipped.merge(mapped.reason(), 1, Integer::sum);
            } else if (ids.add(mapped.record().externalId())) {
                out.add(mapped.record());
            } else {
                skipped.merge(MappedRecord.DUPLICATE_ID, 1, Integer::sum);
            }
        }
        return new Parsed(out, skipped);
    }

    /** One item of a {@code type} response (see the class note). */
    public static MappedRecord item(String type, JsonNode item) {
        if (item == null || !item.isObject()) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        return NOTIFICATIONS.equals(type) ? notice(item) : event(type, item);
    }

    private static MappedRecord notice(JsonNode item) {
        String id = JsonProps.text(item, "messageID");
        if (id == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Instant issued = SpaceSources.utc(JsonProps.text(item, "messageIssueTime"));
        if (issued == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        return MappedRecord.of(NormalizedRecord.builder(id, NOTICE_KIND)
                .observedAt(issued)
                .put("type", NOTIFICATIONS)
                .put("messageType", JsonProps.text(item, "messageType"))
                .put("messageBody", body(JsonProps.text(item, "messageBody")))
                .put("link", JsonProps.text(item, "messageURL"))
                .build());
    }

    private static MappedRecord event(String type, JsonNode item) {
        String id = id(type, item);
        if (id == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        JsonNode firstInput = first(item.get("cmeInputs"));
        Instant observed = SpaceSources.utc(JsonProps.text(item, "submissionTime"));
        if (observed == null) {
            observed = SpaceSources.utc(JsonProps.text(item, "modelCompletionTime"));
        }
        if (observed == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        Instant begin = firstTime(item, "beginTime", "startTime", "eventTime", "associatedCMEstartTime");
        if (begin == null && firstInput != null) {
            begin = SpaceSources.utc(JsonProps.text(firstInput, "cmeStartTime"));
        }
        JsonNode analysis = analysis(type, item);
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, EVENT_KIND)
                .observedAt(observed)
                .eventTime(begin)
                .put("type", type)
                .put("catalog", JsonProps.text(item, "catalog"))
                .put("beginTime", begin == null ? null : begin.toString())
                .put("peakTime", iso(JsonProps.text(item, "peakTime")))
                .put("endTime", iso(JsonProps.text(item, "endTime")))
                .put("classType", JsonProps.text(item, "classType"))
                .put("sourceLocation", JsonProps.text(item, "sourceLocation"))
                .put("activeRegionNum", JsonProps.integer(item, "activeRegionNum"))
                .put("instruments", instruments(item.get("instruments")))
                .put("kpMax", kpMax(item.get("allKpIndex")))
                .put("linkedEvents", linkedEvents(item))
                .put("link", JsonProps.text(item, "link"))
                .put("versionId", JsonProps.integer(item, "versionId"));
        if (analysis != null) {
            b.put("cmeSpeedKms", SpaceSources.round(JsonProps.number(analysis, "speed"), 1))
                    .put("cmeHalfAngle", SpaceSources.round(JsonProps.number(analysis, "halfAngle"), 1));
        }
        return MappedRecord.of(b.build());
    }

    /** The id of an event item, or null. */
    static String id(String type, JsonNode item) {
        for (String field : ID_FIELDS) {
            String id = JsonProps.text(item, field);
            if (id != null) {
                return id;
            }
        }
        if ("CMEAnalysis".equals(type)) {
            String link = JsonProps.text(item, "link");
            Matcher m = link == null ? null : ANALYSIS_LINK.matcher(link);
            if (m != null && m.find()) {
                return "CMEAnalysis/" + m.group(1);
            }
            String cme = JsonProps.text(item, "associatedCMEID");
            String at = JsonProps.text(item, "time21_5");
            return cme == null || at == null ? null : "CMEAnalysis/" + cme + "@" + at;
        }
        return null;
    }

    /**
     * The analysis whose speed and half angle are reported: a CME's most accurate analysis (else its last), a CME
     * analysis itself, a simulation's most accurate input CME; null for other types.
     */
    static JsonNode analysis(String type, JsonNode item) {
        if ("CMEAnalysis".equals(type)) {
            return item;
        }
        JsonNode list = "WSAEnlilSimulations".equals(type) ? item.get("cmeInputs") : item.get("cmeAnalyses");
        if (list == null || !list.isArray() || list.isEmpty()) {
            return null;
        }
        JsonNode last = null;
        for (JsonNode a : list) {
            JsonNode accurate = a.get("isMostAccurate");
            if (accurate != null && accurate.isBoolean() && accurate.booleanValue()) {
                return a;
            }
            last = a;
        }
        return last;
    }

    /** The {@code displayName}s of an {@code instruments} array, or null when there are none. */
    static List<String> instruments(JsonNode array) {
        Set<String> out = new LinkedHashSet<>();
        if (array != null && array.isArray()) {
            for (JsonNode i : array) {
                String name = JsonProps.text(i, "displayName");
                if (name != null && out.size() < 20) {
                    out.add(name);
                }
            }
        }
        return out.isEmpty() ? null : List.copyOf(out);
    }

    /** The largest {@code kpIndex} of an {@code allKpIndex} array, or null. */
    static Double kpMax(JsonNode array) {
        Double max = null;
        if (array != null && array.isArray()) {
            for (JsonNode k : array) {
                Double kp = JsonProps.number(k, "kpIndex");
                if (kp != null && (max == null || kp > max)) {
                    max = kp;
                }
            }
        }
        return SpaceSources.round(max, 2);
    }

    /**
     * The ids an item links to: {@code linkedEvents[].activityID}, a CME analysis's {@code associatedCMEID} and a
     * simulation's {@code cmeInputs[].CMEID}; null when there are none.
     */
    static List<String> linkedEvents(JsonNode item) {
        Set<String> out = new LinkedHashSet<>();
        JsonNode linked = item.get("linkedEvents");
        if (linked != null && linked.isArray()) {
            for (JsonNode l : linked) {
                String id = JsonProps.text(l, "activityID");
                if (id != null) {
                    out.add(id);
                }
            }
        }
        String cme = JsonProps.text(item, "associatedCMEID");
        if (cme != null) {
            out.add(cme);
        }
        JsonNode inputs = item.get("cmeInputs");
        if (inputs != null && inputs.isArray()) {
            for (JsonNode in : inputs) {
                String id = JsonProps.text(in, "CMEID");
                if (id != null) {
                    out.add(id);
                }
            }
        }
        List<String> list = new ArrayList<>(out);
        return list.isEmpty() ? null : List.copyOf(list.subList(0, Math.min(list.size(), 50)));
    }

    /**
     * A notification body cut to {@value #MAX_BODY} characters; null when it is blank or only Markdown heading
     * marks (DONKI answered {@code "## "} for every body on 2026-10-13).
     */
    static String body(String text) {
        String s = SpaceSources.cut(text, MAX_BODY);
        return s == null || s.replace("#", "").isBlank() ? null : s;
    }

    private static Instant firstTime(JsonNode item, String... fields) {
        for (String f : fields) {
            Instant at = SpaceSources.utc(JsonProps.text(item, f));
            if (at != null) {
                return at;
            }
        }
        return null;
    }

    private static JsonNode first(JsonNode array) {
        return array != null && array.isArray() && !array.isEmpty() ? array.get(0) : null;
    }

    private static String iso(String text) {
        Instant at = SpaceSources.utc(text);
        return at == null ? null : at.toString();
    }
}
