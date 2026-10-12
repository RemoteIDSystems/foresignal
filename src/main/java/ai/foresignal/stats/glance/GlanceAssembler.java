package ai.foresignal.stats.glance;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * Turns {@link GlanceData} into the glance's two renditions (prompt 038).
 *
 * <ul>
 *   <li><b>JSON</b> ({@code Glance} schema): at most {@value #MAX_BYTES} bytes of UTF-8. When it would
 *       be larger, {@code latest} entries are dropped (oldest first), then contributor sections (last
 *       first), then the links and the briefing.
 *   <li><b>Text:</b> at most {@value #MAX_LINES} lines of at most {@value #MAX_WIDTH} ASCII
 *       characters, for e-ink displays, LED matrices and {@code curl}.
 * </ul>
 *
 * <p>Pure: no clock, no database, so the size and line limits are asserted directly in tests.
 */
public final class GlanceAssembler {

    /** The largest JSON glance. */
    public static final int MAX_BYTES = 2_048;

    /** The most lines of the text glance. */
    public static final int MAX_LINES = 8;

    /** The widest line of the text glance. */
    public static final int MAX_WIDTH = 40;

    /** The longest {@code shortTitle}. */
    public static final int MAX_TITLE = 60;

    /** The longest briefing title. */
    static final int MAX_BRIEFING_TITLE = 80;

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm'Z'", Locale.ROOT)
            .withZone(ZoneOffset.UTC);

    private final JsonMapper json;

    /** Creates an assembler writing with {@code json}. */
    public GlanceAssembler(JsonMapper json) {
        this.json = json;
    }

    // ------------------------------------------------------------------------- JSON

    /** The JSON glance, fitted into {@value #MAX_BYTES} bytes. */
    public String json(GlanceData data) {
        List<GlanceData.Latest> latest = new ArrayList<>(data.latest());
        LinkedHashMap<String, Object> sections = data.sections() == null ? null : new LinkedHashMap<>(data.sections());
        Map<String, String> links = data.links();
        GlanceData.Briefing briefing = data.briefing();
        while (true) {
            String body = json.writeValueAsString(document(data, latest, sections, links, briefing));
            if (body.getBytes(StandardCharsets.UTF_8).length <= MAX_BYTES) {
                return body;
            }
            if (!latest.isEmpty()) {
                latest.removeLast();
            } else if (sections != null && !sections.isEmpty()) {
                sections.remove(sections.lastEntry().getKey());
            } else if (links != null) {
                links = null;
            } else if (briefing != null) {
                briefing = null;
            } else {
                // Nothing optional is left; the required members alone are far below the limit.
                return body;
            }
        }
    }

    private static Map<String, Object> document(GlanceData data, List<GlanceData.Latest> latest,
            LinkedHashMap<String, Object> sections, Map<String, String> links, GlanceData.Briefing briefing) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("serverTime", data.serverTime().toString());
        out.put("status", data.status());
        Map<String, Object> alerts = new LinkedHashMap<>();
        if (data.open() != null) {
            Map<String, Object> open = new LinkedHashMap<>();
            for (String severity : List.of("critical", "warning", "info")) {
                open.put(severity, data.open().getOrDefault(severity, 0L));
            }
            alerts.put("open", open);
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (GlanceData.Latest l : latest) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", l.id());
            item.put("severity", l.severity());
            item.put("shortTitle", l.shortTitle());
            item.put("firedAt", l.firedAt().toString());
            if (l.near() != null) {
                item.put("near", l.near());
            }
            items.add(item);
        }
        alerts.put("latest", items);
        out.put("alerts", alerts);
        if (data.sources() != null) {
            Map<String, Object> sources = new LinkedHashMap<>();
            sources.put("healthy", data.sources().healthy());
            sources.put("degraded", data.sources().degraded());
            sources.put("failing", data.sources().failing());
            out.put("sources", sources);
        }
        if (briefing != null) {
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("title", cut(briefing.title(), MAX_BRIEFING_TITLE));
            b.put("at", briefing.at().toString());
            b.put("url", briefing.url());
            out.put("briefing", b);
        }
        if (data.versionsLastHour() != null) {
            out.put("activity", Map.of("versionsLastHour", data.versionsLastHour()));
        }
        out.put("lastSeq", data.lastSeq());
        if (links != null && !links.isEmpty()) {
            out.put("links", links);
        }
        if (sections != null && !sections.isEmpty()) {
            out.put("sections", sections);
        }
        return out;
    }

    /** The UTF-8 size of {@code value} as JSON (contributor budgets). */
    public int size(Object value) {
        return json.writeValueAsString(value).getBytes(StandardCharsets.UTF_8).length;
    }

    // ------------------------------------------------------------------------- text

    /**
     * The text glance: a status line, the open counts (when shown), up to three alerts, sources,
     * activity, the briefing and the last seq, cut to {@value #MAX_LINES} lines of
     * {@value #MAX_WIDTH} ASCII characters.
     */
    public static String text(GlanceData data) {
        List<String> lines = new ArrayList<>();
        String head = "FORESIGNAL " + data.status().toUpperCase(Locale.ROOT);
        String time = HHMM.format(data.serverTime());
        lines.add(pad(head, time));
        if (data.open() != null) {
            lines.add("ALERTS C" + data.open().getOrDefault("critical", 0L) + " W"
                    + data.open().getOrDefault("warning", 0L) + " I" + data.open().getOrDefault("info", 0L));
        }
        for (GlanceData.Latest l : data.latest()) {
            lines.add(severity(l.severity()) + " " + l.shortTitle());
        }
        if (data.sources() != null) {
            lines.add("SRC " + data.sources().healthy() + " OK " + data.sources().degraded() + " DEG "
                    + data.sources().failing() + " FAIL");
        }
        if (data.versionsLastHour() != null) {
            lines.add("ACT " + data.versionsLastHour() + " VERSIONS/H");
        }
        if (data.briefing() != null) {
            lines.add(pad("BRF " + data.briefing().title(), HHMM.format(data.briefing().at())));
        }
        lines.add("SEQ " + data.lastSeq());
        List<String> out = new ArrayList<>();
        for (String line : lines) {
            if (out.size() == MAX_LINES) {
                break;
            }
            out.add(ascii(line));
        }
        // The seq line is the one a reader checks for freshness; keep it last even when cut.
        String seq = ascii("SEQ " + data.lastSeq());
        if (!out.getLast().equals(seq)) {
            out.set(out.size() - 1, seq);
        }
        return String.join("\n", out) + "\n";
    }

    private static String severity(String severity) {
        return switch (severity == null ? "" : severity) {
            case "critical" -> "!CRIT";
            case "warning" -> "!WARN";
            default -> "!INFO";
        };
    }

    /** {@code left} and {@code right} on one line of at most {@value #MAX_WIDTH} characters. */
    static String pad(String left, String right) {
        String l = ascii(left);
        String r = ascii(right);
        int room = MAX_WIDTH - r.length() - 1;
        if (room < 1) {
            return r.substring(0, Math.min(r.length(), MAX_WIDTH));
        }
        if (l.length() > room) {
            l = l.substring(0, room);
        }
        return l + " ".repeat(MAX_WIDTH - l.length() - r.length()) + r;
    }

    /**
     * {@code text} as printable ASCII of at most {@value #MAX_WIDTH} characters: accents are folded
     * ({@code é} → {@code e}), other characters become {@code ?}, control characters a space.
     */
    static String ascii(String text) {
        if (text == null) {
            return "";
        }
        String folded = Normalizer.normalize(text, Normalizer.Form.NFD);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < folded.length() && out.length() < MAX_WIDTH; i++) {
            char c = folded.charAt(i);
            if (Character.getType(c) == Character.NON_SPACING_MARK) {
                continue;
            }
            if (c < 0x20 || c == 0x7f) {
                out.append(' ');
            } else if (c < 0x7f) {
                out.append(c);
            } else if (!Character.isLowSurrogate(c)) {
                out.append('?');
            }
        }
        return out.toString().stripTrailing();
    }

    /** {@code text} cut to {@code max} characters with an ellipsis. */
    public static String cut(String text, int max) {
        if (text == null) {
            return null;
        }
        String flat = text.replaceAll("\\s+", " ").strip();
        return flat.length() <= max ? flat : flat.substring(0, max - 1) + "…";
    }

    /** The clock-free time format of the text glance (tests). */
    static String hhmm(Instant t) {
        return HHMM.format(t);
    }
}
