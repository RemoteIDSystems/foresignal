package ai.foresignal.briefing;

import ai.foresignal.ask.AskCitation;
import ai.foresignal.notify.MarkdownRenderer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Renders {@link DigestStats} as deterministic markdown, without an LLM (prompt 029, Architecture §22).
 *
 * <p>Sections, in order: the title and the period line (in the briefing's timezone); "Alerts"
 * (critical first); "Activity", a table of kind, new, changed, removed, active entities and the delta
 * against the previous period; "Notable" lists per kind with links to
 * {@code publicBaseUrl/v1/records/{source}/{externalId}}; "Source health" when included; contributor
 * sections; and a "Counts incomplete (retention)" note when {@code gap}. Every listed record and
 * alert is also returned as a citation {@code {id, seq, title}}.
 *
 * <p>Text from records (titles, alert messages, rule names) is untrusted: it is escaped as markdown,
 * and newlines are folded, so it can neither add structure nor links. HTML in it stays literal text;
 * the HTML rendition escapes it again and sanitizes the result.
 */
@Component
public class DigestRenderer {

    /** How dates and times are written. */
    static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);

    /** The note shown when counts are incomplete. */
    public static final String GAP_HEADING = "Counts incomplete (retention)";

    /** The longest alert message shown. */
    static final int MAX_MESSAGE = 200;

    /**
     * Markdown and the citations of everything it lists.
     *
     * @param markdown the markdown
     * @param citations records and alerts, in order of appearance
     */
    public record Rendered(String markdown, List<AskCitation> citations) {}

    /**
     * Renders {@code stats}.
     *
     * @param title the heading (the briefing name)
     * @param stats the statistics
     * @param zone the timezone of the period line
     * @param periodFrom the period start
     * @param periodTo the period end
     */
    public Rendered render(String title, DigestStats stats, ZoneId zone, Instant periodFrom, Instant periodTo) {
        StringBuilder md = new StringBuilder();
        List<AskCitation> citations = new ArrayList<>();
        md.append("# ").append(inline(title)).append("\n\n");
        md.append('_').append(TIME.format(periodFrom.atZone(zone))).append(" – ").append(TIME.format(periodTo.atZone(zone)))
                .append(' ').append(inline(zone.getId())).append("_\n\n");

        alerts(md, stats.alerts(), citations);
        activity(md, stats);
        notable(md, stats, zone, citations);
        if (stats.health() != null) {
            health(md, stats.health());
        }
        for (BriefingSection section : stats.sections()) {
            if (section.markdown() != null && !section.markdown().isBlank()) {
                md.append(section.markdown().strip()).append("\n\n");
            }
        }
        if (stats.gap()) {
            md.append("## ").append(GAP_HEADING).append("\n\n")
                    .append(inline(stats.gapReason() == null ? "Some versions in the period were already deleted."
                            : stats.gapReason()))
                    .append("\n\n");
        }
        return new Rendered(md.toString().strip() + "\n", List.copyOf(citations));
    }

    /**
     * Renders a sectioned brief without the digest's statistics (prompt 114: the trip briefing): the title, the
     * period line in {@code zone}, then each section's markdown in order. Section markdown is the caller's and must
     * already escape untrusted text ({@link #escape}).
     *
     * @param title the heading
     * @param zone the timezone of the period line
     * @param periodFrom the period start
     * @param periodTo the period end
     * @param sections the sections in order
     */
    public Rendered renderSections(String title, ZoneId zone, Instant periodFrom, Instant periodTo,
            List<BriefingSection> sections) {
        StringBuilder md = new StringBuilder();
        md.append("# ").append(inline(title)).append("\n\n");
        md.append('_').append(TIME.format(periodFrom.atZone(zone))).append(" – ").append(TIME.format(periodTo.atZone(zone)))
                .append(' ').append(inline(zone.getId())).append("_\n\n");
        for (BriefingSection section : sections) {
            if (section.markdown() != null && !section.markdown().isBlank()) {
                md.append(section.markdown().strip()).append("\n\n");
            }
        }
        return new Rendered(md.toString().strip() + "\n", List.of());
    }

    /** {@code text} escaped as inline markdown with newlines folded (untrusted record text, prompt 114). */
    public static String escape(String text) {
        return inline(text);
    }

    private static void alerts(StringBuilder md, DigestStats.Alerts alerts, List<AskCitation> citations) {
        md.append("## Alerts\n\n");
        if (alerts.total() == 0) {
            md.append("No alerts in the period.\n\n");
            return;
        }
        md.append("**").append(alerts.total()).append(alerts.total() == 1 ? " alert" : " alerts").append("**: ")
                .append(alerts.severity("critical")).append(" critical, ").append(alerts.severity("warning"))
                .append(" warning, ").append(alerts.severity("info")).append(" info · ")
                .append(alerts.status("firing")).append(" firing, ").append(alerts.status("acknowledged"))
                .append(" acknowledged, ").append(alerts.status("resolved")).append(" resolved\n\n");
        for (DigestStats.AlertItem item : alerts.items()) {
            var a = item.view();
            String rule = a.ruleName() == null ? "deleted rule" : a.ruleName();
            String message = a.message() == null || a.message().isBlank() ? a.sourceId() + ":" + a.externalId()
                    : a.message();
            message = message.length() > MAX_MESSAGE ? message.substring(0, MAX_MESSAGE) + "…" : message;
            md.append("- **").append(a.severity()).append("** · ").append(inline(rule)).append(": ")
                    .append(inline(message)).append(" · ").append(a.state());
            if (item.link() != null) {
                md.append(" · [alert ").append(a.id()).append("](").append(url(item.link())).append(')');
            }
            md.append('\n');
            citations.add(new AskCitation("alert:" + a.id(), null, rule + ": " + message, AskCitation.ALERT));
        }
        if (alerts.total() > alerts.items().size()) {
            md.append("- …and ").append(alerts.total() - alerts.items().size()).append(" more\n");
        }
        md.append('\n');
    }

    private static void activity(StringBuilder md, DigestStats stats) {
        md.append("## Activity\n\n");
        Map<String, long[]> byKind = new LinkedHashMap<>();
        Map<String, long[]> previousByKind = new LinkedHashMap<>();
        Map<String, Boolean> previousKnown = new LinkedHashMap<>();
        long routine = 0;
        for (DigestStats.Count c : sortedCounts(stats)) {
            long[] row = byKind.computeIfAbsent(label(c.kind()), k -> new long[4]);
            row[0] += c.created();
            row[1] += c.changed();
            row[2] += c.removed();
            routine += c.routine();
            long[] prev = previousByKind.computeIfAbsent(label(c.kind()), k -> new long[3]);
            if (c.previous() == null) {
                previousKnown.put(label(c.kind()), false);
            } else {
                previousKnown.putIfAbsent(label(c.kind()), true);
                prev[0] += c.previous().created();
                prev[1] += c.previous().changed();
                prev[2] += c.previous().removed();
            }
        }
        for (DigestStats.Active a : stats.active()) {
            byKind.computeIfAbsent(label(a.kind()), k -> new long[4])[3] += a.entities();
        }
        if (byKind.isEmpty()) {
            md.append("No activity in the period.\n\n");
            return;
        }
        md.append("| Kind | New | Changed | Removed | Active entities | Delta (new/changed/removed) |\n");
        md.append("| --- | ---: | ---: | ---: | ---: | --- |\n");
        List<String> kinds = new ArrayList<>(byKind.keySet());
        kinds.sort(String::compareTo);
        for (String kind : kinds) {
            long[] row = byKind.get(kind);
            String delta;
            if (!previousByKind.containsKey(kind)) {
                delta = "–";
            } else if (!previousKnown.getOrDefault(kind, false)) {
                delta = "n/a";
            } else {
                long[] prev = previousByKind.get(kind);
                delta = signed(row[0] - prev[0]) + "/" + signed(row[1] - prev[1]) + "/" + signed(row[2] - prev[2]);
            }
            md.append("| ").append(cell(kind)).append(" | ").append(row[0]).append(" | ").append(row[1]).append(" | ")
                    .append(row[2]).append(" | ").append(row[3]).append(" | ").append(delta).append(" |\n");
        }
        md.append('\n');
        if (routine > 0) {
            md.append("Routine versions (samples, keyframes, heartbeats), not counted as change: ").append(routine)
                    .append(".\n\n");
        }
    }

    private static void notable(StringBuilder md, DigestStats stats, ZoneId zone, List<AskCitation> citations) {
        if (stats.notable().isEmpty()) {
            return;
        }
        md.append("## Notable\n\n");
        for (DigestStats.NotableGroup group : stats.notable()) {
            md.append("### ").append(inline(label(group.kind()))).append("\n\n");
            for (DigestStats.NotableItem item : group.items()) {
                String title = item.title() == null || item.title().isBlank() ? item.externalId() : item.title();
                md.append("- [").append(inline(title)).append("](").append(url(item.link())).append(')');
                if (item.rankValue() != null && item.rankField() != null) {
                    md.append(" · ").append(inline(item.rankField())).append(' ').append(number(item.rankValue()));
                }
                if (item.effectiveTime() != null) {
                    md.append(" · ").append(TIME.format(item.effectiveTime().atZone(zone)));
                }
                if (item.live()) {
                    md.append(" · live");
                }
                md.append('\n');
                citations.add(new AskCitation(item.id(), item.seq(), title, AskCitation.RECORD));
            }
            md.append('\n');
        }
    }

    private static void health(StringBuilder md, List<DigestStats.HealthItem> health) {
        md.append("## Source health\n\n");
        if (health.isEmpty()) {
            md.append("All sources in scope are healthy.\n\n");
            return;
        }
        for (DigestStats.HealthItem item : health) {
            md.append("- **").append(inline(item.sourceId())).append("** is ").append(item.status());
            if (item.lastError() != null && !item.lastError().isBlank()) {
                md.append(": ").append(inline(item.lastError()));
            }
            md.append('\n');
        }
        md.append('\n');
    }

    // ------------------------------------------------------------------------- helpers

    private static List<DigestStats.Count> sortedCounts(DigestStats stats) {
        List<DigestStats.Count> out = new ArrayList<>(stats.counts());
        out.sort((a, b) -> {
            int kind = label(a.kind()).compareTo(label(b.kind()));
            return kind != 0 ? kind : a.sourceId().compareTo(b.sourceId());
        });
        return out;
    }

    private static String label(String kind) {
        return kind == null || kind.isEmpty() ? "(no kind)" : kind;
    }

    /** Untrusted text as one escaped markdown line. */
    static String inline(String text) {
        if (text == null) {
            return "";
        }
        String folded = text.replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ').strip();
        return MarkdownRenderer.escapeMarkdown(folded);
    }

    /** Untrusted text in a table cell (pipes are escaped by {@link #inline}). */
    private static String cell(String text) {
        return inline(text);
    }

    /** A link target safe inside {@code (…)}. */
    static String url(String link) {
        return link.replace(" ", "%20").replace("(", "%28").replace(")", "%29");
    }

    static String signed(long value) {
        return value > 0 ? "+" + value : Long.toString(value);
    }

    static String number(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
