package ai.foresignal.quality;

import ai.foresignal.briefing.BriefingSection;
import ai.foresignal.briefing.BriefingSectionContributor;
import ai.foresignal.notify.MarkdownRenderer;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * The weekly alert-quality section of a briefing (prompt 115), only when its scope sets
 * {@code includeAlertQuality} (off by default): the rules with the lowest precision (at least
 * {@value #MIN_LABELED} labeled in the last 7 days), the noisiest rules by alerts per day, and the rules with
 * open tuning suggestions. Exercise alerts never count ({@code rule_quality_daily} excludes them).
 */
@Component
@Order(200)
public class AlertQualitySection implements BriefingSectionContributor {

    /** The section key. */
    public static final String KEY = "alertQuality";

    /** The fewest labeled alerts for a precision entry. */
    public static final int MIN_LABELED = 10;

    /** Entries per list. */
    public static final int TOP = 5;

    private final RuleQualityDailyStore daily;
    private final TuningSuggestionRepository suggestions;

    /** Creates the section. */
    public AlertQualitySection(RuleQualityDailyStore daily, TuningSuggestionRepository suggestions) {
        this.daily = daily;
        this.suggestions = suggestions;
    }

    @Override
    public Optional<BriefingSection> contribute(Context context) {
        if (context.briefing() == null || !context.briefing().includeAlertQuality()) {
            return Optional.empty();
        }
        LocalDate since = context.periodTo().atZone(ZoneOffset.UTC).toLocalDate().minusDays(7);
        List<Map<String, Object>> totals = daily.totals(since);
        List<Map<String, Object>> lowest = new ArrayList<>();
        for (Map<String, Object> row : totals) {
            long positive = number(row.get("useful"));
            long negative = number(row.get("noise")) + number(row.get("falsePositive"));
            if (positive + negative >= MIN_LABELED) {
                Map<String, Object> entry = new LinkedHashMap<>(row);
                entry.put("precision", Math.round((double) positive / (positive + negative) * 1000) / 1000.0);
                lowest.add(entry);
            }
        }
        lowest.sort(Comparator.comparingDouble((Map<String, Object> m) -> (Double) m.get("precision"))
                .thenComparing(m -> String.valueOf(m.get("ruleId"))));
        List<Map<String, Object>> noisiest = new ArrayList<>();
        for (Map<String, Object> row : totals) {
            Map<String, Object> entry = new LinkedHashMap<>(row);
            entry.put("alertsPerDay", Math.round(number(row.get("fired")) / 7.0 * 10) / 10.0);
            noisiest.add(entry);
        }
        noisiest.sort(Comparator.comparingDouble((Map<String, Object> m) -> -(Double) m.get("alertsPerDay"))
                .thenComparing(m -> String.valueOf(m.get("ruleId"))));
        Map<String, Integer> pending = new java.util.TreeMap<>();
        for (TuningSuggestionEntity s : suggestions.findByStatus(TuningSuggestionEntity.OPEN)) {
            pending.merge(s.getRuleId().toString(), 1, Integer::sum);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("lowestPrecision", lowest.subList(0, Math.min(TOP, lowest.size())));
        data.put("noisiest", noisiest.subList(0, Math.min(TOP, noisiest.size())));
        data.put("pendingSuggestions", pending);
        return Optional.of(new BriefingSection(KEY, data, markdown(data), null));
    }

    @SuppressWarnings("unchecked")
    private static String markdown(Map<String, Object> data) {
        StringBuilder out = new StringBuilder("## Alert quality (last 7 days)\n\n");
        List<Map<String, Object>> lowest = (List<Map<String, Object>>) data.get("lowestPrecision");
        List<Map<String, Object>> noisiest = (List<Map<String, Object>>) data.get("noisiest");
        Map<String, Integer> pending = (Map<String, Integer>) data.get("pendingSuggestions");
        if (lowest.isEmpty() && noisiest.isEmpty() && pending.isEmpty()) {
            return out.append("No alerts or feedback in the last 7 days.\n").toString();
        }
        if (!lowest.isEmpty()) {
            out.append("Lowest precision:\n");
            for (Map<String, Object> row : lowest) {
                out.append("- ").append(name(row)).append(": ").append(Math.round((Double) row.get("precision") * 100))
                        .append("%\n");
            }
            out.append('\n');
        }
        if (!noisiest.isEmpty()) {
            out.append("Noisiest rules:\n");
            for (Map<String, Object> row : noisiest) {
                out.append("- ").append(name(row)).append(": ").append(row.get("alertsPerDay")).append(" alerts/day\n");
            }
            out.append('\n');
        }
        if (!pending.isEmpty()) {
            out.append("Rules with open tuning suggestions: ").append(pending.size()).append('\n');
        }
        return out.toString();
    }

    private static String name(Map<String, Object> row) {
        Object name = row.get("ruleName");
        return MarkdownRenderer.escapeMarkdown(name == null ? String.valueOf(row.get("ruleId")) : String.valueOf(name));
    }

    private static long number(Object value) {
        return value instanceof Number n ? n.longValue() : 0;
    }
}
