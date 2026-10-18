package ai.foresignal.media.outlet;

import ai.foresignal.source.formats.csv.CsvReader;
import ai.foresignal.source.formats.news.ArticleUrls;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * Reads ratings rows the operator imports (prompt 133): the {@code operator} provider's CSV or JSON
 * ({@code domain, dimension, value, label, licence, attribution}), and the same row shape for the opt-in
 * {@code allsides} and {@code mbfc} providers, whose values are mapped from the provider's published labels. Pure.
 *
 * <ul>
 *   <li><strong>Values:</strong> a number is the normalized value (lean −2…+2, factuality 1…5, other dimensions as
 *       given); a lean label maps through {@link #LEAN_LABELS}; a factuality label through {@link #FACTUALITY_LABELS};
 *       any other text is kept as a label with no value.
 *   <li><strong>AllSides</strong> labels: Left −2, Lean Left −1, Center 0, Lean Right +1, Right +2; Mixed is a label
 *       without a value.
 *   <li><strong>MBFC</strong> bias labels: Extreme Left and Left −2, Left-Center −1, Least Biased and Center 0,
 *       Right-Center +1, Right and Extreme Right +2; its other categories (Pro-Science, Conspiracy-Pseudoscience,
 *       Questionable, Satire) are not left–right and go to {@code mbfc:category}; factual reporting Very High 5, High
 *       4, Mostly Factual 3.5, Mixed 2.5, Low 2, Very Low 1.
 * </ul>
 * Each refused row is reported with its number and reason; at most {@value #MAX_ROWS} rows are read.
 */
public final class OperatorRatingsParser {

    /** The most rows one import reads. */
    public static final int MAX_ROWS = 50_000;

    /** Lean labels (lower-case) of every provider. */
    public static final Map<String, Double> LEAN_LABELS = Map.ofEntries(
            Map.entry("left", -2.0), Map.entry("extreme left", -2.0), Map.entry("far left", -2.0),
            Map.entry("lean left", -1.0), Map.entry("lean-left", -1.0), Map.entry("left-center", -1.0),
            Map.entry("left center", -1.0), Map.entry("center", 0.0), Map.entry("least biased", 0.0),
            Map.entry("lean right", 1.0), Map.entry("lean-right", 1.0), Map.entry("right-center", 1.0),
            Map.entry("right center", 1.0), Map.entry("right", 2.0), Map.entry("extreme right", 2.0),
            Map.entry("far right", 2.0));

    /** Factuality labels (lower-case) on the 1…5 scale. */
    public static final Map<String, Double> FACTUALITY_LABELS = Map.of("very high", 5.0, "high", 4.0,
            "mostly factual", 3.5, "mixed", 2.5, "low", 2.0, "very low", 1.0);

    /** MBFC categories that are not left–right positions. */
    static final List<String> MBFC_CATEGORIES = List.of("pro-science", "conspiracy-pseudoscience", "questionable",
            "satire");

    private OperatorRatingsParser() {}

    /**
     * The parsed rows and the refused ones.
     *
     * @param rows the usable rows
     * @param errors {@code {row, message}} for each refused row (at most 100)
     * @param rejected the number of refused rows
     */
    public record Parsed(List<RatingRow> rows, List<Map<String, Object>> errors, int rejected) {}

    /**
     * Reads CSV text with a header naming {@code domain, dimension, value, label, licence, attribution}.
     *
     * @param provider the importing provider
     */
    public static Parsed csv(String text, String provider) {
        List<RatingRow> rows = new ArrayList<>();
        Errors errors = new Errors();
        try (CsvReader reader = new CsvReader(new BufferedReader(new StringReader(text == null ? "" : text)))) {
            List<String> header = reader.next();
            if (header == null) {
                errors.add(null, "the file is empty");
                return errors.result(rows);
            }
            Map<String, Integer> index = CsvReader.index(header);
            for (String required : List.of("DOMAIN", "DIMENSION", "VALUE")) {
                if (!index.containsKey(required)) {
                    errors.add(null, "the header has no " + required.toLowerCase(Locale.ROOT) + " column");
                }
            }
            if (!errors.list.isEmpty()) {
                return errors.result(rows);
            }
            List<String> row;
            int n = 1;
            while ((row = reader.next()) != null && n <= MAX_ROWS) {
                n++;
                if (row.size() == 1 && row.get(0).isBlank()) {
                    continue;
                }
                accept(rows, errors, n, provider, CsvReader.get(row, index, "DOMAIN"),
                        CsvReader.get(row, index, "DIMENSION"), CsvReader.get(row, index, "VALUE"),
                        CsvReader.get(row, index, "LABEL"), CsvReader.get(row, index, "LICENCE") != null
                                ? CsvReader.get(row, index, "LICENCE") : CsvReader.get(row, index, "LICENSE"),
                        CsvReader.get(row, index, "ATTRIBUTION"), CsvReader.get(row, index, "URL"));
            }
        } catch (RuntimeException | java.io.IOException e) {
            errors.add(null, "the CSV could not be read: " + e.getMessage());
        }
        return errors.result(rows);
    }

    /**
     * Reads the JSON body {@code {"rows": [{domain, dimension, value, label, licence, attribution}]}}.
     *
     * @param provider the importing provider
     */
    public static Parsed json(JsonNode body, String provider) {
        List<RatingRow> rows = new ArrayList<>();
        Errors errors = new Errors();
        JsonNode list = body == null ? null : body.get("rows");
        if (list == null || !list.isArray()) {
            errors.add(null, "the body must be {\"rows\": [...]}");
            return errors.result(rows);
        }
        int n = 0;
        for (JsonNode r : list) {
            n++;
            if (n > MAX_ROWS) {
                break;
            }
            if (!r.isObject()) {
                errors.add(n, "a row must be an object");
                continue;
            }
            JsonNode value = r.get("value");
            accept(rows, errors, n, provider, text(r, "domain"), text(r, "dimension"),
                    value == null || value.isNull() ? null : value.isNumber() ? value.asString() : value.asString(),
                    text(r, "label"), text(r, "licence") != null ? text(r, "licence") : text(r, "license"),
                    text(r, "attribution"), text(r, "url"));
        }
        return errors.result(rows);
    }

    private static void accept(List<RatingRow> rows, Errors errors, int n, String provider, String rawDomain,
            String rawDimension, String value, String label, String license, String attribution, String url) {
        String domain = ArticleUrls.normalizeDomain(rawDomain == null ? null : rawDomain.contains("://")
                ? ArticleUrls.domain(rawDomain) : rawDomain);
        if (domain == null || !domain.contains(".")) {
            errors.add(n, "domain is missing or not a domain");
            return;
        }
        String dimension = rawDimension == null ? null : rawDimension.strip().toLowerCase(Locale.ROOT);
        if (!MediaVocabulary.validDimension(dimension)) {
            errors.add(n, "dimension must be one of " + MediaVocabulary.DIMENSIONS + " or {provider}:{axis}");
            return;
        }
        if (value == null || value.isBlank()) {
            errors.add(n, "value is required");
            return;
        }
        if (MediaVocabulary.OPERATOR.equals(provider)) {
            if (license == null || license.isBlank()) {
                errors.add(n, "licence is required for operator rows");
                return;
            }
            if (attribution == null || attribution.isBlank()) {
                errors.add(n, "attribution is required for operator rows");
                return;
            }
        }
        String v = value.strip();
        Double number = number(v);
        String lower = v.toLowerCase(Locale.ROOT);
        if (MediaVocabulary.MBFC.equals(provider) && MediaVocabulary.LEAN.equals(dimension)
                && MBFC_CATEGORIES.contains(lower)) {
            dimension = "mbfc:category";
        }
        Double norm = number;
        String outLabel = label;
        if (number == null) {
            if (MediaVocabulary.LEAN.equals(dimension)) {
                norm = LEAN_LABELS.get(lower);
            } else if (MediaVocabulary.FACTUALITY.equals(dimension)) {
                norm = FACTUALITY_LABELS.get(lower);
            }
            if (outLabel == null) {
                outLabel = v;
            }
        } else if (MediaVocabulary.LEAN.equals(dimension) && (number < -2 || number > 2)) {
            errors.add(n, "a lean value must lie between -2 and 2");
            return;
        } else if (MediaVocabulary.FACTUALITY.equals(dimension) && (number < 1 || number > 5)) {
            errors.add(n, "a factuality value must lie between 1 and 5");
            return;
        }
        if (outLabel == null && MediaVocabulary.LEAN.equals(dimension)) {
            outLabel = MediaVocabulary.leanLabel(norm);
        }
        rows.add(new RatingRow(domain, dimension, norm, v, outLabel, null, license, attribution, url, null));
    }

    private static Double number(String v) {
        try {
            double d = Double.parseDouble(v);
            return Double.isFinite(d) ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asString();
    }

    private static final class Errors {
        final List<Map<String, Object>> list = new ArrayList<>();
        int rejected;

        void add(Integer row, String message) {
            rejected++;
            if (list.size() < 100) {
                java.util.LinkedHashMap<String, Object> e = new java.util.LinkedHashMap<>();
                e.put("row", row);
                e.put("message", message);
                list.add(e);
            }
        }

        Parsed result(List<RatingRow> rows) {
            return new Parsed(rows, list, rejected);
        }
    }
}
