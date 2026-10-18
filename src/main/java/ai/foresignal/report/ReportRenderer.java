package ai.foresignal.report;

import ai.foresignal.briefing.BriefingHtmlRenderer;
import ai.foresignal.security.Tlp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * Renders a report document to Markdown and HTML (prompt 132), reusing the 029 renderer for the HTML of every text
 * part (CommonMark with tables, raw HTML escaped, then the OWASP allowlist), so report text stays inert.
 *
 * <ul>
 *   <li>The TLP label is printed as a banner at the top and the bottom of every rendition, and in HTML it is also a
 *       fixed print header and footer so it appears on every printed page (COL-2).</li>
 *   <li>A rendition with any machine-generated block ends with the footer {@value ReportVocabulary#FOOTER}, and a
 *       labeled LLM narrative block carries its own label.</li>
 *   <li>Map images are embedded in the HTML as {@code data:} URIs (the rendition's Content-Security-Policy allows
 *       {@code img-src data:}) and referenced as {@code attachments/{sha256}.png} in the Markdown, the layout of an
 *       exchange package's {@code content/} folder.</li>
 * </ul>
 */
@Component
public class ReportRenderer {

    /** Images larger than this are referenced, not embedded, in HTML. */
    static final int MAX_EMBEDDED_IMAGE = 2 * 1024 * 1024;

    static final String STYLE = """
            :root{color-scheme:light dark;--fg:#1d1f23;--bg:#ffffff;--muted:#5b6170;--rule:#d9dce3;--link:#0b5cad;--code:#f2f4f7}
            @media (prefers-color-scheme:dark){:root{--fg:#e6e8ec;--bg:#14161a;--muted:#a3a9b6;--rule:#2f333b;--link:#7ab7ff;--code:#1f2228}}
            *{box-sizing:border-box}
            body{margin:0;background:var(--bg);color:var(--fg);font:16px/1.55 system-ui,-apple-system,"Segoe UI",Roboto,sans-serif}
            main{max-width:52rem;margin:0 auto;padding:1.5rem 1rem 3rem}
            h1{font-size:1.6rem;margin:.2rem 0 .6rem}h2{font-size:1.25rem;margin:1.8rem 0 .6rem;border-bottom:1px solid var(--rule);padding-bottom:.25rem}h3{font-size:1.05rem;margin:1.2rem 0 .4rem}
            a{color:var(--link)}em{color:var(--muted)}
            table{border-collapse:collapse;width:100%;margin:.6rem 0;display:block;overflow-x:auto}
            th,td{border:1px solid var(--rule);padding:.3rem .55rem;text-align:left}
            code,pre{background:var(--code);border-radius:4px;font:.9em ui-monospace,SFMono-Regular,Menlo,monospace}code{padding:.1em .3em}
            blockquote{margin:1rem 0;padding:.25rem 1rem;border-left:3px solid var(--rule);color:var(--muted)}
            figure{margin:1rem 0}figure img{max-width:100%;height:auto;border:1px solid var(--rule)}
            .tlp{font:700 .95rem/1.4 ui-monospace,Menlo,monospace;padding:.35rem 1rem;background:#000;text-align:center}
            .tlp-RED{color:#FF2B2B}.tlp-AMBER,.tlp-AMBER-STRICT{color:#FFC000}.tlp-GREEN{color:#33FF00}.tlp-CLEAR{color:#FFFFFF}
            .meta{color:var(--muted);font-size:.9rem}.origin{border:1px solid var(--rule);padding:.5rem 1rem;margin:.5rem 0}
            footer{max-width:52rem;margin:0 auto;padding:0 1rem 2rem;color:var(--muted);font-size:.85rem}
            @media print{.tlp-top{position:fixed;top:0;left:0;right:0}.tlp-bottom{position:fixed;bottom:0;left:0;right:0}main{padding-top:2.5rem;padding-bottom:2.5rem}}
            """;

    private final BriefingHtmlRenderer html;

    /** Creates the renderer. */
    public ReportRenderer(BriefingHtmlRenderer html) {
        this.html = html;
    }

    /**
     * An image a map or attachments block shows.
     *
     * @param sha256 the content hash
     * @param contentType the media type
     * @param bytes the content
     */
    public record Image(String sha256, String contentType, byte[] bytes) {}

    /**
     * What is rendered.
     *
     * @param title the title
     * @param subtitle the subtitle, or null
     * @param displayId the display id
     * @param revision the revision, or null for a draft preview
     * @param tlp the TLP label
     * @param publishedAt when, or null
     * @param publishedBy the publisher's handle, or null
     * @param authors the authors' display names
     * @param blocks the filled blocks
     * @param images map and attachment images by attachment id
     * @param origin an origin note printed under the banner (received material), or null
     * @param retracted a retraction note, or null
     */
    public record Document(String title, String subtitle, String displayId, Integer revision, String tlp,
            String publishedAt, String publishedBy, List<String> authors, List<Map<String, Object>> blocks,
            Map<String, Image> images, String origin, String retracted) {}

    /** The TLP banner line. */
    public static String banner(String tlp) {
        String label = tlp == null ? Tlp.AMBER.wireValue() : tlp;
        return "TLP:" + label + " — " + meaning(label);
    }

    /** What a TLP label means for a reader. */
    static String meaning(String tlp) {
        return switch (tlp) {
            case "CLEAR" -> "disclosure is not limited";
            case "GREEN" -> "limited disclosure, recipients may share within their community but not publicly";
            case "AMBER" -> "limited disclosure, recipients may share within their organization and its clients on"
                    + " a need-to-know basis";
            case "AMBER+STRICT" -> "limited disclosure, recipients may share only within their organization";
            case "RED" -> "for the eyes and ears of the named recipients only, no further disclosure";
            default -> "handle according to the label";
        };
    }

    /** True when any block is machine-generated. */
    static boolean machine(Document d) {
        return ReportBlocks.machineGenerated(d.blocks());
    }

    // ------------------------------------------------------------------------- Markdown

    /** The Markdown rendition. */
    public String markdown(Document d) {
        StringBuilder out = new StringBuilder(4096);
        out.append("> **").append(banner(d.tlp())).append("**\n\n");
        if (d.origin() != null) {
            out.append("> ").append(oneLine(d.origin())).append("\n\n");
        }
        if (d.retracted() != null) {
            out.append("> **Retracted:** ").append(oneLine(d.retracted())).append("\n\n");
        }
        out.append("# ").append(oneLine(d.title())).append("\n\n");
        if (d.subtitle() != null && !d.subtitle().isBlank()) {
            out.append('*').append(oneLine(d.subtitle())).append("*\n\n");
        }
        out.append(meta(d)).append("\n\n");
        for (Map<String, Object> b : d.blocks()) {
            out.append(block(b)).append('\n');
        }
        out.append("---\n\n> **").append(banner(d.tlp())).append("**\n");
        if (machine(d)) {
            out.append("\n*").append(ReportVocabulary.FOOTER).append(".*\n");
        }
        return out.toString();
    }

    private static String meta(Document d) {
        List<String> parts = new ArrayList<>();
        if (d.displayId() != null) {
            parts.add(d.displayId());
        }
        parts.add(d.revision() == null ? "draft preview" : "revision " + d.revision());
        if (d.publishedAt() != null) {
            parts.add("published " + d.publishedAt() + (d.publishedBy() == null ? "" : " by @" + d.publishedBy()));
        }
        if (d.authors() != null && !d.authors().isEmpty()) {
            parts.add("authors: " + String.join(", ", d.authors()));
        }
        return String.join(" · ", parts);
    }

    /** The Markdown of one block, with its heading. */
    @SuppressWarnings("unchecked")
    String block(Map<String, Object> b) {
        String type = String.valueOf(b.get("type"));
        StringBuilder out = new StringBuilder();
        String title = b.get("title") instanceof String t && !t.isBlank() ? t : defaultTitle(type);
        if (title != null) {
            out.append("## ").append(oneLine(title)).append("\n\n");
        }
        if (ReportVocabulary.LLM.equals(b.get("textOrigin"))) {
            out.append("*Narrative written by a language model (labeled); its citations were verified, but verify the"
                    + " text before acting.*\n\n");
        }
        switch (type) {
            case "markdown" -> {
                out.append(links(String.valueOf(b.getOrDefault("markdown", "")))).append("\n\n");
                List<Object> citations = ReportBlocks.asList(b.get("citations"));
                if (!citations.isEmpty()) {
                    out.append("Citations: ");
                    List<String> cs = new ArrayList<>();
                    for (Object c : citations) {
                        cs.add("`" + c + "`");
                    }
                    out.append(String.join(", ", cs)).append("\n\n");
                }
            }
            case "bluf" -> {
                for (Object o : ReportBlocks.asList(b.get("judgments"))) {
                    if (o instanceof Map<?, ?> j) {
                        out.append("- ").append(oneLine(String.valueOf(j.get("text"))));
                        if (j.get("probability") instanceof Number p) {
                            out.append(" — *").append(j.get("icdTerm")).append("* (")
                                    .append(Math.round(p.doubleValue() * 100)).append("%)");
                        }
                        if (j.get("confidence") != null) {
                            out.append(", ").append(j.get("confidence")).append(" confidence");
                        }
                        Object basis = j.get("questionRef") != null ? j.get("questionRef") : j.get("hypothesisSetRef");
                        if (basis != null) {
                            out.append(" [`").append(basis).append("`]");
                        }
                        out.append('\n');
                    }
                }
                out.append('\n');
            }
            case "objects" -> {
                out.append("| Object | Type | Status | Time | Reference |\n|---|---|---|---|---|\n");
                for (Object o : ReportBlocks.asList(b.get("items"))) {
                    if (o instanceof Map<?, ?> item) {
                        Map<String, Object> env = item.get("envelope") instanceof Map<?, ?> e
                                ? (Map<String, Object>) e : Map.of();
                        out.append("| ").append(cell(env.getOrDefault("title", "(not visible)"))).append(" | ")
                                .append(cell(env.get("type"))).append(" | ").append(cell(env.get("status")))
                                .append(" | ").append(cell(time(env))).append(" | `").append(cell(item.get("ref")))
                                .append("` |\n");
                    }
                }
                out.append('\n');
            }
            case "map" -> {
                if (b.get("mediaId") instanceof String media) {
                    String sha = media.startsWith("sha256:") ? media.substring(7) : media;
                    out.append("![Map").append(layers(b)).append("](attachments/").append(sha).append(".png)\n\n");
                } else {
                    out.append("*Map").append(layers(b)).append(": rendered when the report is published.*\n\n");
                }
                if (b.get("caveat") instanceof String caveat) {
                    out.append("*").append(oneLine(caveat)).append("*\n\n");
                }
            }
            case "timeline" -> {
                for (Object o : ReportBlocks.asList(b.get("items"))) {
                    if (o instanceof Map<?, ?> item) {
                        out.append("- ").append(item.get("at")).append(" — ")
                                .append(oneLine(String.valueOf(item.get("title"))));
                        if (item.get("ref") != null) {
                            out.append(" (`").append(item.get("ref")).append("`)");
                        }
                        out.append('\n');
                    }
                }
                out.append('\n');
            }
            case "hypotheses" -> {
                Map<String, Object> snap = b.get("snapshot") instanceof Map<?, ?> m ? (Map<String, Object>) m
                        : Map.of();
                out.append("**").append(oneLine(String.valueOf(snap.getOrDefault("question",
                        b.get("hypothesisSetRef"))))).append("** (`").append(b.get("hypothesisSetRef")).append("`)\n\n");
                List<Object> hyps = ReportBlocks.asList(snap.get("hypotheses"));
                if (!hyps.isEmpty()) {
                    out.append("| Hypothesis | Probability | Estimate | Confidence |\n|---|---|---|---|\n");
                    for (Object o : hyps) {
                        if (o instanceof Map<?, ?> h) {
                            out.append("| ").append(cell(h.get("label"))).append(": ").append(cell(h.get("statement")))
                                    .append(" | ").append(percent(h.get("probability"))).append(" | ")
                                    .append(cell(h.get("estimativeTerm"))).append(" | ").append(cell(h.get("confidence")))
                                    .append(" |\n");
                        }
                    }
                    out.append('\n');
                }
                List<Object> revs = ReportBlocks.asList(b.get("revisionIds"));
                if (!revs.isEmpty()) {
                    out.append("Revisions cited: ").append(revs.size()).append(" (newest `").append(revs.get(0))
                            .append("`)\n\n");
                }
            }
            case "forecasts" -> {
                out.append("| Question | Ensemble | Estimate | Status | Reference |\n|---|---|---|---|---|\n");
                for (Object o : ReportBlocks.asList(b.get("questions"))) {
                    if (o instanceof Map<?, ?> q) {
                        Map<String, Object> snap = q.get("snapshot") instanceof Map<?, ?> m ? (Map<String, Object>) m
                                : Map.of();
                        out.append("| ").append(cell(snap.getOrDefault("title", "(not visible)"))).append(" | ")
                                .append(percent(snap.get("ensembleProbability"))).append(" | ")
                                .append(cell(snap.get("estimativeTerm"))).append(" | ").append(cell(snap.get("status")))
                                .append(" | `").append(cell(q.get("questionRef"))).append("` |\n");
                    }
                }
                out.append('\n');
            }
            case "evidence" -> {
                out.append("| Evidence | Grade | Reference |\n|---|---|---|\n");
                for (Object o : ReportBlocks.asList(b.get("items"))) {
                    if (o instanceof Map<?, ?> e) {
                        out.append("| ").append(cell(e.get("title") == null ? "(not visible)" : e.get("title"))).append(" | ")
                                .append(cell(e.get("grade"))).append(" | `evidence-item:")
                                .append(cell(e.get("evidenceItemId"))).append("` |\n");
                    }
                }
                out.append('\n');
            }
            case "coverage" -> {
                List<Object> caveats = ReportBlocks.asList(b.get("caveats"));
                if (caveats.isEmpty()) {
                    out.append("No coverage caveats were recorded.\n\n");
                }
                for (Object o : caveats) {
                    if (o instanceof Map<?, ?> c) {
                        out.append("- **").append(cell(c.get("code"))).append(":** ")
                                .append(oneLine(String.valueOf(c.get("text")))).append('\n');
                    }
                }
                // Prompt 133: news coverage statistics (describe reporting, never truth).
                for (Object o : ReportBlocks.asList(b.get("coverage"))) {
                    if (o instanceof Map<?, ?> st && st.get("coverageBalance") instanceof Map<?, ?> bal) {
                        out.append("- News coverage (evidence groups): left ").append(percent(bal.get("left")))
                                .append(", center ").append(percent(bal.get("center"))).append(", right ")
                                .append(percent(bal.get("right"))).append(", unrated ")
                                .append(percent(bal.get("unrated"))).append("; state-affiliated ")
                                .append(percent(st.get("stateAffiliatedShare"))).append("; blindspot ")
                                .append(st.get("blindspot") == null ? "none" : cell(st.get("blindspot")))
                                .append('\n');
                    }
                }
                out.append('\n');
            }
            case "attachments" -> {
                for (Object id : ReportBlocks.asList(b.get("attachmentIds"))) {
                    out.append("- Attachment `").append(id).append("`\n");
                }
                out.append('\n');
            }
            case "sources" -> {
                for (Object o : ReportBlocks.asList(b.get("attributions"))) {
                    if (o instanceof Map<?, ?> a) {
                        out.append("- `").append(cell(a.get("sourceId"))).append("`: ")
                                .append(oneLine(String.valueOf(a.get("attribution"))));
                        if (a.get("licence") != null) {
                            out.append(" (").append(a.get("licence")).append(')');
                        }
                        if (a.get("url") != null) {
                            out.append(' ').append(a.get("url"));
                        }
                        out.append('\n');
                    }
                }
                out.append('\n');
            }
            default -> {
                // unknown types render only their heading
            }
        }
        return out.toString();
    }

    /** The default heading of a block type. */
    static String defaultTitle(String type) {
        return switch (type) {
            case "bluf" -> "Key judgments";
            case "objects" -> "Cited objects";
            case "map" -> "Map";
            case "timeline" -> "Timeline";
            case "hypotheses" -> "Hypotheses";
            case "forecasts" -> "Forecasts";
            case "evidence" -> "Evidence";
            case "coverage" -> "Coverage and caveats";
            case "attachments" -> "Attachments";
            case "sources" -> "Sources and licenses";
            default -> null;
        };
    }

    private static String layers(Map<String, Object> b) {
        List<Object> layers = ReportBlocks.asList(b.get("layers"));
        return layers.isEmpty() ? "" : " (" + String.join(", ", layers.stream().map(String::valueOf).toList()) + ")";
    }

    /** {@code [[ref]]} object links as inline code, so they read as references and never as markup. */
    static String links(String markdown) {
        return markdown.replaceAll("\\[\\[([^\\[\\]\\s]{3,1024})]]", "`$1`");
    }

    private static String time(Map<String, Object> env) {
        if (env.get("time") instanceof Map<?, ?> t) {
            Object v = t.get("start") != null ? t.get("start") : t.get("observedAt");
            return v == null ? "" : String.valueOf(v);
        }
        return "";
    }

    private static String percent(Object p) {
        return p instanceof Number n ? Math.round(n.doubleValue() * 100) + "%" : "";
    }

    private static String cell(Object v) {
        return v == null ? "" : oneLine(String.valueOf(v)).replace("|", "\\|");
    }

    private static String oneLine(String s) {
        return s == null ? "" : s.replace('\r', ' ').replace('\n', ' ').strip();
    }

    // ------------------------------------------------------------------------- HTML

    /** The HTML rendition (a whole page). */
    public String html(Document d) {
        String tlp = d.tlp() == null ? Tlp.AMBER.wireValue() : d.tlp();
        String cls = "tlp tlp-" + tlp.replace('+', '-');
        String banner = HtmlUtils.htmlEscape(banner(tlp));
        StringBuilder out = new StringBuilder(8192);
        out.append("<!DOCTYPE html>\n<html lang=\"en\"><head><meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<meta name=\"color-scheme\" content=\"light dark\">")
                .append("<meta name=\"referrer\" content=\"no-referrer\">")
                .append("<title>").append(HtmlUtils.htmlEscape("TLP:" + tlp + " " + nonNull(d.title())))
                .append("</title><style>").append(STYLE).append("</style></head><body>")
                .append("<div class=\"").append(cls).append(" tlp-top\" role=\"note\">").append(banner).append("</div>")
                .append("<main>");
        if (d.origin() != null) {
            out.append("<p class=\"origin\">").append(HtmlUtils.htmlEscape(d.origin())).append("</p>");
        }
        if (d.retracted() != null) {
            out.append("<p class=\"origin\"><strong>Retracted:</strong> ").append(HtmlUtils.htmlEscape(d.retracted()))
                    .append("</p>");
        }
        out.append("<h1>").append(HtmlUtils.htmlEscape(nonNull(d.title()))).append("</h1>");
        if (d.subtitle() != null && !d.subtitle().isBlank()) {
            out.append("<p><em>").append(HtmlUtils.htmlEscape(d.subtitle())).append("</em></p>");
        }
        out.append("<p class=\"meta\">").append(HtmlUtils.htmlEscape(meta(d))).append("</p>");
        for (Map<String, Object> b : d.blocks()) {
            out.append("<section>").append(html.fragment(block(b)));
            if ("map".equals(b.get("type")) && b.get("attachmentId") instanceof String id && d.images() != null
                    && d.images().get(id) != null) {
                Image img = d.images().get(id);
                if (img.bytes() != null && img.bytes().length <= MAX_EMBEDDED_IMAGE) {
                    out.append("<figure><img alt=\"Map\" src=\"data:").append(HtmlUtils.htmlEscape(
                            img.contentType().toLowerCase(Locale.ROOT))).append(";base64,")
                            .append(Base64.getEncoder().encodeToString(img.bytes())).append("\"></figure>");
                }
            }
            out.append("</section>");
        }
        out.append("</main><footer>");
        if (machine(d)) {
            out.append("<p>").append(HtmlUtils.htmlEscape(ReportVocabulary.FOOTER)).append(".</p>");
        }
        out.append("</footer><div class=\"").append(cls).append(" tlp-bottom\" role=\"note\">").append(banner)
                .append("</div></body></html>\n");
        return out.toString();
    }

    private static String nonNull(String s) {
        return s == null ? "" : s;
    }
}
