package ai.foresignal.exchange;

import ai.foresignal.report.ReportRenderer;
import ai.foresignal.report.ReportVocabulary;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Renders the content of a package (prompt 132) with {@link ReportRenderer}, the sender before signing and the
 * receiver again on import, so a received rendition is always this instance's own sanitized output, marked with its
 * origin, and never the sender's HTML. A report renders its frozen blocks; an investigation its items as a timeline
 * and cited objects; an evidence item its grade and description; a chronicle extract a summary of what it holds.
 */
@Component
public class ExchangeRenditions {

    private final ReportRenderer renderer;

    /** Creates the component. */
    public ExchangeRenditions(ReportRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * The renditions of a package's content.
     *
     * @param markdown the Markdown rendition
     * @param html the HTML rendition
     * @param title the title
     */
    public record Rendered(String markdown, String html, String title) {}

    /**
     * Renders a package's bundle.
     *
     * @param type the package type
     * @param bundle the JSON bundle
     * @param tlp the TLP label
     * @param images images by attachment id (map blocks)
     * @param origin the origin note (received material), or null
     * @return the renditions
     */
    public Rendered render(String type, Map<String, Object> bundle, String tlp, Map<String, ReportRenderer.Image> images,
            String origin) {
        ReportRenderer.Document doc = document(type, bundle, tlp, images, origin);
        return new Rendered(renderer.markdown(doc), renderer.html(doc), doc.title());
    }

    /** The document of a package's bundle. */
    @SuppressWarnings("unchecked")
    static ReportRenderer.Document document(String type, Map<String, Object> bundle, String tlp,
            Map<String, ReportRenderer.Image> images, String origin) {
        Map<String, Object> b = bundle == null ? Map.of() : bundle;
        switch (type) {
            case "report" -> {
                Map<String, Object> report = map(b.get("report"));
                List<Map<String, Object>> blocks = new ArrayList<>();
                for (Object o : list(report.get("blocks"))) {
                    if (o instanceof Map<?, ?> m) {
                        blocks.add((Map<String, Object>) m);
                    }
                }
                List<String> authors = new ArrayList<>();
                for (Object a : list(report.get("authors"))) {
                    Map<String, Object> m = map(a);
                    authors.add(String.valueOf(m.getOrDefault("displayName", m.getOrDefault("handle", m.get("id")))));
                }
                Map<String, Object> publisher = map(report.get("owner"));
                return new ReportRenderer.Document(text(report.get("title"), "Report"), (String) report.get("subtitle"),
                        (String) report.get("displayId"), b.get("revision") instanceof Number n ? n.intValue() : null,
                        tlp, (String) report.get("publishedAt"), (String) publisher.get("handle"), authors, blocks,
                        images, origin, null);
            }
            case "investigation" -> {
                Map<String, Object> inv = map(b.get("investigation"));
                List<Map<String, Object>> blocks = new ArrayList<>();
                blocks.add(block("markdown", "Summary", Map.of("markdown", text(inv.get("summary"),
                        "Investigation " + text(inv.get("displayId"), "") + " (" + text(inv.get("status"), "") + ")"),
                        "citations", List.of())));
                List<Map<String, Object>> timeline = new ArrayList<>();
                List<Map<String, Object>> objects = new ArrayList<>();
                for (Object o : list(b.get("items"))) {
                    Map<String, Object> item = map(o);
                    Map<String, Object> t = new LinkedHashMap<>();
                    t.put("at", item.get("addedAt"));
                    t.put("title", text(item.get("title"), String.valueOf(item.get("itemType"))));
                    t.put("ref", item.get("ref"));
                    timeline.add(t);
                    if (item.get("ref") != null) {
                        Map<String, Object> obj = new LinkedHashMap<>();
                        obj.put("ref", item.get("ref"));
                        obj.put("envelope", item.get("envelope"));
                        objects.add(obj);
                    }
                }
                blocks.add(block("timeline", "Items", Map.of("items", timeline)));
                blocks.add(block("objects", "Cited objects", Map.of("items", objects)));
                return doc(text(inv.get("title"), "Investigation"), (String) inv.get("displayId"), tlp, blocks,
                        images, origin);
            }
            case "evidence" -> {
                Map<String, Object> e = map(b.get("evidenceItem"));
                List<Map<String, Object>> blocks = new ArrayList<>();
                blocks.add(block("markdown", "Description", Map.of("markdown", text(e.get("description"),
                        "(no description)"), "citations", List.of())));
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("evidenceItemId", e.get("id"));
                item.put("title", e.get("title"));
                item.put("grade", e.get("grade"));
                blocks.add(block("evidence", "Grade", Map.of("items", List.of(item))));
                return doc(text(e.get("title"), "Evidence item"), null, tlp, blocks, images, origin);
            }
            default -> {
                Map<String, Object> c = map(b.get("chronicle"));
                StringBuilder md = new StringBuilder();
                md.append("Chronicle extract of **").append(text(c.get("displayId"), "")).append(' ')
                        .append(text(c.get("name"), "")).append("**: ").append(b.getOrDefault("rows", 0))
                        .append(" captured versions");
                Map<String, Object> window = map(b.get("window"));
                if (window.get("from") != null || window.get("to") != null) {
                    md.append(" from ").append(window.getOrDefault("from", "the start")).append(" to ")
                            .append(window.getOrDefault("to", "now"));
                }
                md.append(". Sources included: ").append(list(b.get("includedSources"))).append('.');
                if (!list(b.get("excludedSources")).isEmpty()) {
                    md.append(" Left out for egress: ").append(list(b.get("excludedSources"))).append('.');
                }
                List<Map<String, Object>> blocks = List.of(block("markdown", "Extract",
                        Map.of("markdown", md.toString(), "citations", List.of())));
                return doc(text(c.get("name"), "Chronicle extract"), (String) c.get("displayId"), tlp, blocks, images,
                        origin);
            }
        }
    }

    private static ReportRenderer.Document doc(String title, String displayId, String tlp,
            List<Map<String, Object>> blocks, Map<String, ReportRenderer.Image> images, String origin) {
        return new ReportRenderer.Document(title, null, displayId, null, tlp, null, null, List.of(), blocks,
                images == null ? Map.of() : images, origin, null);
    }

    private static Map<String, Object> block(String type, String title, Map<String, Object> fields) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("type", type);
        b.put("title", title);
        b.put("textOrigin", ReportVocabulary.AUTHOR);
        b.putAll(fields);
        return b;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> map(Object o) {
        return o instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    @SuppressWarnings("unchecked")
    static List<Object> list(Object o) {
        return o instanceof List<?> l ? (List<Object>) l : List.of();
    }

    private static String text(Object o, String fallback) {
        return o instanceof String s && !s.isBlank() ? s : fallback;
    }
}
