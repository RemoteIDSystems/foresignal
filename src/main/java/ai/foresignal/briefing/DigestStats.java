package ai.foresignal.briefing;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.trigger.AlertView;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Digest statistics shared by briefing runs and {@code GET /v1/digest} (OpenAPI {@code DigestStats},
 * prompt 029).
 *
 * <p>Version counts come from {@code record_version} between {@code fromSeq} (exclusive) and
 * {@code toSeq} (inclusive), grouped by persist reason: {@code new} = FIRST + REAPPEAR,
 * {@code changed} = SIGNIFICANT + CHANGE, {@code removed} = TOMBSTONE, {@code edges} = TRIGGER_EDGE;
 * {@code routine} = SAMPLE + KEYFRAME + HEARTBEAT is reported separately and is never change.
 * Activity comes from {@code record_live} (touch-only entities included). Alerts are keyed on alert
 * id. {@link #toMap()} writes the spec's JSON.
 *
 * @param fromSeq the exclusive lower seq bound
 * @param toSeq the inclusive upper seq bound ({@code ChangeBus.lastPublishedSeq()} read before the queries)
 * @param fromTime the period start
 * @param toTime the period end
 * @param gap true when {@code fromSeq} is below the retention floor of a scope source
 * @param gapReason why, or null
 * @param counts version counts per source and kind
 * @param active entities observed per source and kind
 * @param alerts the alert section
 * @param notable notable records per kind
 * @param health degraded or failing sources, or null when health is not included
 * @param healthSnapshot true when {@code health} is the current snapshot rather than stored transitions
 * @param sections sections from {@link BriefingSectionContributor} beans
 */
public record DigestStats(long fromSeq, long toSeq, Instant fromTime, Instant toTime, boolean gap, String gapReason,
        List<Count> counts, List<Active> active, Alerts alerts, List<NotableGroup> notable, List<HealthItem> health,
        boolean healthSnapshot, List<BriefingSection> sections) {

    /** Copies the lists. */
    public DigestStats {
        counts = counts == null ? List.of() : List.copyOf(counts);
        active = active == null ? List.of() : List.copyOf(active);
        notable = notable == null ? List.of() : List.copyOf(notable);
        health = health == null ? null : List.copyOf(health);
        sections = sections == null ? List.of() : List.copyOf(sections);
        alerts = alerts == null ? Alerts.empty() : alerts;
    }

    /** A copy with {@code newSections}. */
    public DigestStats withSections(List<BriefingSection> newSections) {
        return new DigestStats(fromSeq, toSeq, fromTime, toTime, gap, gapReason, counts, active, alerts, notable,
                health, healthSnapshot, newSections);
    }

    /** The {@code DigestStats} JSON. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("fromSeq", fromSeq);
        out.put("toSeq", toSeq);
        out.put("gap", gap);
        out.put("gapReason", gapReason);
        out.put("counts", counts.stream().map(Count::toMap).toList());
        out.put("active", active.stream().map(Active::toMap).toList());
        out.put("alerts", alerts.toMap());
        out.put("notable", notable.stream().map(NotableGroup::toMap).toList());
        out.put("health", health == null ? null : health.stream().map(HealthItem::toMap).toList());
        out.put("healthSnapshot", healthSnapshot);
        Map<String, Object> sectionData = new LinkedHashMap<>();
        for (BriefingSection section : sections) {
            sectionData.put(section.key(), section.data());
        }
        out.put("sections", sectionData);
        return out;
    }

    /** Every notable item, in kind order. */
    public List<NotableItem> notableItems() {
        List<NotableItem> out = new ArrayList<>();
        notable.forEach(group -> out.addAll(group.items()));
        return out;
    }

    /**
     * Version counts for one source and kind.
     *
     * @param sourceId the source
     * @param kind the kind
     * @param byReason versions per persist reason name ({@code FIRST} … {@code TRIGGER_EDGE}), zeros omitted
     * @param created FIRST + REAPPEAR ({@code new})
     * @param changed SIGNIFICANT + CHANGE
     * @param removed TOMBSTONE
     * @param edges TRIGGER_EDGE
     * @param routine SAMPLE + KEYFRAME + HEARTBEAT
     * @param entities distinct entities per group
     * @param previous the previous equal-length period, or null when it is below the retention floor
     */
    public record Count(String sourceId, String kind, Map<String, Long> byReason, long created, long changed,
            long removed, long edges, long routine, Entities entities, Previous previous) {

        /** The {@code DigestCount} JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("sourceId", sourceId);
            out.put("kind", kind);
            out.put("new", created);
            out.put("changed", changed);
            out.put("removed", removed);
            out.put("edges", edges);
            out.put("routine", routine);
            out.put("entities", entities.toMap());
            out.put("byReason", byReason);
            out.put("previous", previous == null ? null : previous.toMap());
            return out;
        }

        /**
         * The count of {@code reasons} from per-reason counts indexed by persist reason code.
         *
         * @param byCode counts indexed by code (index 0 unused)
         */
        static long sum(long[] byCode, PersistReason... reasons) {
            long total = 0;
            for (PersistReason reason : reasons) {
                total += byCode[reason.code()];
            }
            return total;
        }
    }

    /**
     * Distinct entities per group.
     *
     * @param created FIRST + REAPPEAR
     * @param changed SIGNIFICANT + CHANGE
     * @param removed TOMBSTONE
     * @param edges TRIGGER_EDGE
     */
    public record Entities(long created, long changed, long removed, long edges) {

        /** The JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("new", created);
            out.put("changed", changed);
            out.put("removed", removed);
            out.put("edges", edges);
            return out;
        }
    }

    /**
     * The previous period's counts.
     *
     * @param created FIRST + REAPPEAR
     * @param changed SIGNIFICANT + CHANGE
     * @param removed TOMBSTONE
     * @param active entities active then, or null (record_live keeps only the latest receipt)
     */
    public record Previous(long created, long changed, long removed, Long active) {

        /** The JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("new", created);
            out.put("changed", changed);
            out.put("removed", removed);
            out.put("active", active);
            return out;
        }
    }

    /**
     * Entities observed in the period for one source and kind (from {@code record_live}).
     *
     * @param sourceId the source
     * @param kind the kind
     * @param entities the count
     * @param previous the previous period's count, or null when not known
     */
    public record Active(String sourceId, String kind, long entities, Long previous) {

        /** The {@code DigestActive} JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("sourceId", sourceId);
            out.put("kind", kind);
            out.put("entities", entities);
            out.put("previous", previous);
            return out;
        }
    }

    /**
     * The alert section, keyed on alert id.
     *
     * @param sinceAlertId alerts after this id
     * @param untilAlertId up to and including this id
     * @param bySeverity counts per severity
     * @param byStatus counts per lifecycle state
     * @param byRule counts per rule, largest first
     * @param items at most 20 alerts, critical first, then newest
     * @param total every alert in the window and scope
     */
    public record Alerts(Long sinceAlertId, Long untilAlertId, Map<String, Long> bySeverity,
            Map<String, Long> byStatus, List<RuleCount> byRule, List<AlertItem> items, long total) {

        /** Copies the collections. */
        public Alerts {
            bySeverity = bySeverity == null ? Map.of() : Map.copyOf(bySeverity);
            byStatus = byStatus == null ? Map.of() : Map.copyOf(byStatus);
            byRule = byRule == null ? List.of() : List.copyOf(byRule);
            items = items == null ? List.of() : List.copyOf(items);
        }

        /** No alerts. */
        public static Alerts empty() {
            return new Alerts(null, null, Map.of(), Map.of(), List.of(), List.of(), 0);
        }

        /** The {@code DigestAlerts} JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("sinceAlertId", sinceAlertId);
            out.put("untilAlertId", untilAlertId);
            Map<String, Object> severity = new LinkedHashMap<>();
            for (String s : List.of("critical", "warning", "info")) {
                severity.put(s, bySeverity.getOrDefault(s, 0L));
            }
            out.put("bySeverity", severity);
            Map<String, Object> status = new LinkedHashMap<>();
            for (String s : List.of("firing", "acknowledged", "resolved")) {
                status.put(s, byStatus.getOrDefault(s, 0L));
            }
            out.put("byStatus", status);
            out.put("byRule", byRule.stream().map(RuleCount::toMap).toList());
            out.put("items", items.stream().map(AlertItem::json).toList());
            return out;
        }

        /** Count of {@code severity}. */
        public long severity(String severity) {
            return bySeverity.getOrDefault(severity, 0L);
        }

        /** Count of {@code state}. */
        public long status(String state) {
            return byStatus.getOrDefault(state, 0L);
        }
    }

    /**
     * Alerts of one rule.
     *
     * @param ruleId the rule
     * @param ruleName its name, or null when deleted
     * @param count the alerts
     */
    public record RuleCount(UUID ruleId, String ruleName, long count) {

        /** The JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("ruleId", ruleId == null ? null : ruleId.toString());
            out.put("ruleName", ruleName);
            out.put("count", count);
            return out;
        }
    }

    /**
     * One listed alert.
     *
     * @param view the alert
     * @param json its {@code Alert} JSON
     * @param link {@code publicBaseUrl/v1/alerts/{id}}
     */
    public record AlertItem(AlertView view, Map<String, Object> json, String link) {}

    /**
     * The notable records of one kind.
     *
     * @param kind the kind
     * @param items ranked items
     */
    public record NotableGroup(String kind, List<NotableItem> items) {

        /** Copies the items. */
        public NotableGroup {
            items = items == null ? List.of() : List.copyOf(items);
        }

        /** The JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("kind", kind);
            out.put("items", items.stream().map(NotableItem::toMap).toList());
            return out;
        }
    }

    /**
     * A notable current record.
     *
     * @param sourceId the source
     * @param externalId the external id
     * @param kind the kind
     * @param seq the persisted head seq (cite with it)
     * @param title the title (025 {@code TitleResolver}), or null
     * @param live true when live state differs from the persisted version
     * @param observedAt the latest observation
     * @param effectiveTime {@code coalesce(event_time, observed_at)}
     * @param rankValue the descriptor's {@code rankField} value, or null
     * @param rankField the field it came from, or null
     * @param link {@code publicBaseUrl/v1/records/{source}/{externalId}}
     */
    public record NotableItem(String sourceId, String externalId, String kind, long seq, String title, boolean live,
            Instant observedAt, Instant effectiveTime, Double rankValue, String rankField, String link) {

        /** {@code <sourceId>:<externalId>}. */
        public String id() {
            return sourceId + ":" + externalId;
        }

        /** The {@code DigestNotableItem} JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("id", id());
            out.put("source", sourceId);
            out.put("externalId", externalId);
            out.put("kind", kind);
            out.put("seq", seq);
            out.put("title", title);
            out.put("live", live);
            out.put("observedAt", observedAt == null ? null : observedAt.toString());
            out.put("effectiveTime", effectiveTime == null ? null : effectiveTime.toString());
            out.put("rankValue", rankValue);
            out.put("link", link);
            return out;
        }
    }

    /**
     * A source that was degraded or failing.
     *
     * @param sourceId the source
     * @param status {@code degraded} or {@code failing}
     * @param since since when, or null when not known
     * @param lastError the last error or status reason, or null
     */
    public record HealthItem(String sourceId, String status, Instant since, String lastError) {

        /** The {@code DigestHealthItem} JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("sourceId", sourceId);
            out.put("status", status);
            out.put("since", since == null ? null : since.toString());
            out.put("lastError", lastError);
            return out;
        }
    }
}
