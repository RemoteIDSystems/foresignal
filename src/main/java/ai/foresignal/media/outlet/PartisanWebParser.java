package ai.foresignal.media.outlet;

import ai.foresignal.source.formats.news.ArticleUrls;
import ai.foresignal.source.formats.news.NewsNames;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses one organization file of The Partisan Web data ({@code github.com/stockHuman/tpw-data},
 * {@code organisations/*.md}, CC BY 4.0) into ratings (prompt 133). Each file starts with a front-matter block between
 * {@code ---} lines of {@code key: value} pairs (lists as {@code [a, b]}).
 *
 * <p>Field meanings (the repository README, read 2026-10-17) and the mapping:
 *
 * <ul>
 *   <li>{@code bias_social} (−10…10, negative left, positive right: "the commonly understood social left/right
 *       spectrum") → {@code lean} = value ÷ 5 on −2…+2;
 *   <li>{@code fact_sourcing} (0…10 citation quality, −1 never assessed) → {@code factuality} = 1 + 4 × value ÷ 10 on
 *       1…5;
 *   <li>{@code bias_governmental} (−10…10, authoritarian/libertarian and pro/anti-establishment, relative to the
 *       country) → {@code partisan-web:governmental}, never forced into lean; value as published;
 *   <li>{@code fact_editorial} (0…10, how easily writers and editors can be identified; −1 not applicable) →
 *       {@code partisan-web:editorial};
 *   <li>{@code subj_pseudoscience}, {@code subj_conspiracy} (0…10) and {@code subj_message} (0…10, 0 an impartial
 *       aggregator, 10 a propaganda machine; −1 not assessed) → {@code partisan-web:pseudoscience},
 *       {@code partisan-web:conspiracy}, {@code partisan-web:message}, values as published;
 *   <li>{@code bias_ideological} (a descriptive affiliation) → {@code partisan-web:ideological} as a label;
 *   <li>{@code tags} {@code state-owned} or {@code state-sponsored} → {@code state-affiliation} 1 with the tag as label.
 * </ul>
 *
 * <p>A file still at the template's defaults ({@code bias_social} and {@code bias_governmental} 0, every fact and
 * message score −1) is unassessed and gives no rating. {@code ownership} is not imported: it may name people (the
 * registry keeps organization names only). {@code country} (ISO alpha-3), {@code name} and {@code lang} become registry
 * facts. Pure.
 */
public final class PartisanWebParser {

    private PartisanWebParser() {}

    /**
     * The ratings of one file.
     *
     * @param markdown the file's text
     * @param link the file's page, recorded as each rating's URL, or null
     */
    public static List<RatingRow> parse(String markdown, String link) {
        Map<String, String> front = frontMatter(markdown);
        String url = front.get("url");
        String domain = url == null ? null : ArticleUrls.domain(url.contains("://") ? url : "https://" + url);
        if (domain == null) {
            return List.of();
        }
        Double social = number(front.get("bias_social"));
        Double governmental = number(front.get("bias_governmental"));
        Double sourcing = number(front.get("fact_sourcing"));
        Double editorial = number(front.get("fact_editorial"));
        Double message = number(front.get("subj_message"));
        boolean template = zeroOrNull(social) && zeroOrNull(governmental) && minusOneOrNull(sourcing)
                && minusOneOrNull(editorial) && minusOneOrNull(message);
        List<String> tags = list(front.get("tags"));
        List<String> languages = new ArrayList<>();
        for (String l : list(front.get("lang"))) {
            String code = NewsNames.languageCode(l);
            if (code != null && !languages.contains(code)) {
                languages.add(code);
            }
        }
        RatingRow.OutletFacts facts = new RatingRow.OutletFacts(blank(front.get("name")),
                NewsNames.countryIso2(front.get("country")), languages, null);
        List<RatingRow> out = new ArrayList<>();
        if (!template) {
            if (social != null) {
                double v = clamp(social / 5.0, -2, 2);
                out.add(row(domain, MediaVocabulary.LEAN, round(v), "bias_social=" + fmt(social),
                        MediaVocabulary.leanLabel(v), link, facts));
            }
            if (sourcing != null && sourcing >= 0) {
                out.add(row(domain, MediaVocabulary.FACTUALITY, round(1 + 4 * clamp(sourcing, 0, 10) / 10.0),
                        "fact_sourcing=" + fmt(sourcing), null, link, facts));
            }
            if (governmental != null) {
                out.add(row(domain, "partisan-web:governmental", governmental, "bias_governmental=" + fmt(governmental),
                        "authoritarian/libertarian axis (as published)", link, facts));
            }
            if (editorial != null && editorial >= 0) {
                out.add(row(domain, "partisan-web:editorial", editorial, "fact_editorial=" + fmt(editorial), null,
                        link, facts));
            }
            Double pseudo = number(front.get("subj_pseudoscience"));
            if (pseudo != null) {
                out.add(row(domain, "partisan-web:pseudoscience", pseudo, "subj_pseudoscience=" + fmt(pseudo), null,
                        link, facts));
            }
            Double conspiracy = number(front.get("subj_conspiracy"));
            if (conspiracy != null) {
                out.add(row(domain, "partisan-web:conspiracy", conspiracy, "subj_conspiracy=" + fmt(conspiracy), null,
                        link, facts));
            }
            if (message != null && message >= 0) {
                out.add(row(domain, "partisan-web:message", message, "subj_message=" + fmt(message), null, link,
                        facts));
            }
            String ideological = blank(front.get("bias_ideological"));
            if (ideological != null) {
                out.add(row(domain, "partisan-web:ideological", null, ideological, ideological, link, facts));
            }
        }
        for (String tag : tags) {
            String t = tag.toLowerCase(Locale.ROOT);
            if (t.equals("state-owned") || t.equals("state-sponsored")) {
                out.add(row(domain, MediaVocabulary.STATE_AFFILIATION, 1.0, "tags=" + t, t, link, facts));
                break;
            }
        }
        return out;
    }

    /** The front-matter pairs of a file (keys lower-case), empty without a block. */
    static Map<String, String> frontMatter(String markdown) {
        Map<String, String> out = new LinkedHashMap<>();
        if (markdown == null) {
            return out;
        }
        String[] lines = markdown.replace("\r\n", "\n").split("\n");
        int i = 0;
        while (i < lines.length && lines[i].isBlank()) {
            i++;
        }
        if (i >= lines.length || !lines[i].strip().equals("---")) {
            return out;
        }
        for (i = i + 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.strip().equals("---")) {
                break;
            }
            int colon = line.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            out.put(line.substring(0, colon).strip().toLowerCase(Locale.ROOT), line.substring(colon + 1).strip());
        }
        return out;
    }

    private static RatingRow row(String domain, String dimension, Double value, String raw, String label, String link,
            RatingRow.OutletFacts facts) {
        return new RatingRow(domain, dimension, value, raw, label, null, null, null, link, facts);
    }

    private static List<String> list(String value) {
        List<String> out = new ArrayList<>();
        if (value == null) {
            return out;
        }
        String v = value.strip();
        if (v.startsWith("[") && v.endsWith("]")) {
            v = v.substring(1, v.length() - 1);
        }
        for (String part : v.split(",")) {
            String p = part.strip().replaceAll("^['\"]|['\"]$", "");
            if (!p.isEmpty()) {
                out.add(p);
            }
        }
        return out;
    }

    private static Double number(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            double d = Double.parseDouble(value.strip());
            return Double.isFinite(d) ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean zeroOrNull(Double v) {
        return v == null || v == 0;
    }

    private static boolean minusOneOrNull(Double v) {
        return v == null || v == -1;
    }

    private static String blank(String v) {
        if (v == null) {
            return null;
        }
        String s = v.strip().replaceAll("^['\"]|['\"]$", "");
        return s.isEmpty() ? null : s;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double round(double v) {
        return Math.round(v * 100) / 100.0;
    }

    private static String fmt(double v) {
        return v == Math.rint(v) ? Long.toString((long) v) : Double.toString(v);
    }
}
