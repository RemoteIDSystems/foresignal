package ai.foresignal.trigger;

import ai.foresignal.notify.SystemEvent;
import ai.foresignal.store.TriggerMode;
import ai.foresignal.store.query.PgArrays;
import ai.foresignal.store.query.ReadQueryRunner;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The anchor side of moving-anchor ("near me") rules (prompt 036, task 5): when an anchor moves, the
 * targets it now reaches, or no longer reaches, fire {@code enter} and {@code exit} edges although
 * the targets themselves did not change.
 *
 * <p>The {@link TriggerEngine} calls {@link #evaluate} after each batch and on its anchor tick, with
 * the anchors that changed. For each enabled anchored rule:
 *
 * <ol>
 *   <li><b>Staleness.</b> An anchor older than {@code maxAnchorAge}, tombstoned or expired suspends
 *       the rule ({@code status = stale}, {@code trigger_rule.last_error} set, one
 *       {@code system.anchor_stale} notification per transition); a fresh observation resumes it.
 *   <li><b>Movement.</b> Nothing runs until the anchor moved at least {@code minMoveMeters} from
 *       {@code trigger_anchor_state.eval_geom} (or 60 s passed, which only refreshes the state).
 *   <li><b>Edges.</b> Current targets ({@code record_head ⋈ record_live}, the rule's sources and
 *       kinds, its conditions over the merged current payload) with {@code enter} = near(new) ∧
 *       ¬near(old) and {@code exit} = near(old) ∧ ¬near(new). Only enter and exit rules have a
 *       reverse side; match rules evaluate the target side only.
 * </ol>
 *
 * <p>Rows reference the target's head {@code seq} and current payload; the engine runs them through
 * the same {@link AlertPass} (cooldown, snoozes, storm cap). Reverse alerts write no record
 * versions. The first time a rule sees its anchor, the position is recorded without firing.
 */
@Component
public class AnchorEvaluator {

    private static final Logger log = LoggerFactory.getLogger(AnchorEvaluator.class);

    /** How long a still anchor goes before its state is refreshed anyway. */
    public static final Duration REFRESH = Duration.ofSeconds(60);

    /** The system notification subtype of a stale anchor. */
    public static final String STALE_SUBTYPE = "system.anchor_stale";

    static final String MERGED = "jsonb_strip_nulls(h.payload || l.kinematic)";

    static final String LOAD_STATE = "SELECT ST_AsBinary(eval_geom) AS geom, eval_seq, eval_at, status"
            + " FROM trigger_anchor_state WHERE rule_id = :rule AND anchor_source = :source AND anchor_external_id = :id";

    static final String UPSERT_STATE = "INSERT INTO trigger_anchor_state (rule_id, anchor_source, anchor_external_id,"
            + " eval_geom, eval_seq, eval_at, status) VALUES (:rule, :source, :id,"
            + " ST_GeomFromWKB(CAST(:geom AS bytea), 4326), :seq, :at, :status)"
            + " ON CONFLICT (rule_id, anchor_source, anchor_external_id) DO UPDATE SET"
            + " eval_geom = COALESCE(EXCLUDED.eval_geom, trigger_anchor_state.eval_geom),"
            + " eval_seq = COALESCE(EXCLUDED.eval_seq, trigger_anchor_state.eval_seq),"
            + " eval_at = EXCLUDED.eval_at, status = EXCLUDED.status";

    /**
     * The reverse rows of one rule.
     *
     * @param rule the rule
     * @param rows the targets' edges, in seq order
     * @param anchorSeq the anchor's seq at this evaluation
     * @param anchor the anchor's position at this evaluation
     */
    public record Outcome(RuleSpec rule, List<MatchRow> rows, long anchorSeq, Point anchor) {}

    /** The stored evaluation state. */
    record State(Point geom, Long seq, Instant at, String status) {}

    private final ReadQueryRunner reads;
    private final NamedParameterJdbcTemplate jdbc;
    private final AnchorPositions anchors;
    private final ApplicationEventPublisher events;

    /** Creates the evaluator. */
    public AnchorEvaluator(ReadQueryRunner reads, NamedParameterJdbcTemplate jdbc, AnchorPositions anchors,
            ApplicationEventPublisher events) {
        this.reads = reads;
        this.jdbc = jdbc;
        this.anchors = anchors;
        this.events = events;
    }

    /**
     * Evaluates every anchored rule of {@code rules}.
     *
     * @param rules the enabled rules
     * @param changed anchor keys that moved or changed since the last call
     * @param now the evaluation time
     * @return the reverse edges per rule (rules without edges are left out)
     */
    public List<Outcome> evaluate(List<RuleSpec> rules, Set<String> changed, Instant now) {
        List<Outcome> out = new ArrayList<>();
        for (RuleSpec rule : rules) {
            if (!rule.anchored()) {
                continue;
            }
            try {
                Outcome outcome = evaluate(rule, changed.contains(rule.anchor().key()), now);
                if (outcome != null && !outcome.rows().isEmpty()) {
                    out.add(outcome);
                }
            } catch (RuntimeException e) {
                log.error("Anchor evaluation of rule {} failed: {}", rule.id(), e.getMessage());
            }
        }
        return out;
    }

    private Outcome evaluate(RuleSpec rule, boolean changed, Instant now) {
        NearAnchor anchor = rule.anchor();
        AnchorPositions.Fix fix = anchors.fix(anchor.source(), anchor.externalId()).orElse(null);
        State state = state(rule.id(), anchor);
        boolean fresh = fix != null && fix.fresh(anchor, now);
        if (!fresh) {
            if (state == null || !"stale".equals(state.status())) {
                markStale(rule, fix, now, state);
            }
            return null;
        }
        if (state == null || state.geom() == null) {
            save(rule.id(), anchor, fix.point(), fix.seq(), now, "active");
            return null;
        }
        boolean resumed = "stale".equals(state.status());
        if (resumed) {
            resume(rule, now);
        }
        double moved = AnchorDistance.haversine(state.geom().getY(), state.geom().getX(),
                fix.point().getY(), fix.point().getX());
        boolean due = state.at() == null || !state.at().isAfter(now.minus(REFRESH));
        if (!changed && !due && !resumed) {
            return null;
        }
        if (moved < anchor.minMoveMeters()) {
            if (due || resumed) {
                save(rule.id(), anchor, null, fix.seq(), now, "active");
            }
            return null;
        }
        List<MatchRow> rows = rule.mode() == TriggerMode.MATCH ? List.of() : edges(rule, state.geom(), fix.point(), now);
        save(rule.id(), anchor, fix.point(), fix.seq(), now, "active");
        return new Outcome(rule, rows, fix.seq(), fix.point());
    }

    /** The targets whose nearness changed between {@code previous} and {@code current}. */
    List<MatchRow> edges(RuleSpec rule, Point previous, Point current, Instant now) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        p.addValue("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC));
        p.addValue("anchorSource", rule.anchor().source());
        p.addValue("anchorId", rule.anchor().externalId());
        List<String> where = new ArrayList<>();
        where.add("NOT h.tombstone");
        where.add("(l.valid_until IS NULL OR l.valid_until > CAST(:now AS timestamptz))");
        where.add("NOT (h.source_id = :anchorSource AND h.external_id = :anchorId)");
        if (!rule.sources().isEmpty()) {
            p.addValue("sources", PgArrays.text(rule.sources().stream().sorted().toList()));
            where.add("h.source_id = ANY(CAST(:sources AS text[]))");
        }
        if (!rule.kinds().isEmpty()) {
            p.addValue("kinds", PgArrays.text(rule.kinds().stream().sorted().toList()));
            where.add("h.kind = ANY(CAST(:kinds AS text[]))");
        }
        if (!rule.conditions().isEmpty()) {
            where.add(RecordPredicate.conditionsSql(rule.conditions(), "h.source_id", MERGED, (key, value, type) -> {
                if (!p.hasValue(key)) {
                    p.addValue(key, value);
                }
                return "CAST(:" + key + " AS " + type + ")";
            }));
        }
        String nearNew = near(rule.anchor(), current, "n", p);
        String nearOld = near(rule.anchor(), previous, "o", p);
        String enter = nearNew + " AND NOT " + nearOld;
        String exit = nearOld + " AND NOT " + nearNew;
        where.add(rule.mode() == TriggerMode.ENTER ? "(" + enter + ")" : "(" + exit + ")");
        String sql = "SELECT h.seq, CAST(NULL AS bigint) AS prev_seq, h.source_id, h.external_id, h.kind, h.version,"
                + " CAST(0 AS smallint) AS persist_reason, l.observed_at, h.event_time, h.valid_from, l.valid_until,"
                + " h.tombstone, ST_AsGeoJSON(l.geom) AS geometry, " + MERGED + "::text AS payload, "
                + (rule.mode() == TriggerMode.ENTER ? "'enter'" : "'exit'") + " AS edge"
                + " FROM record_head h JOIN record_live l ON l.source_id = h.source_id AND l.external_id = h.external_id"
                + " WHERE " + String.join(" AND ", where) + " ORDER BY h.seq";
        return reads.read(t -> t.query(sql, p, MatchRow.MAPPER));
    }

    /** near(x) against {@code point}, with parameters prefixed {@code prefix}. */
    static String near(NearAnchor anchor, Point point, String prefix, MapSqlParameterSource p) {
        p.addValue(prefix + "Point", new WKBWriter(2).write(point));
        String at = "ST_GeomFromWKB(CAST(:" + prefix + "Point AS bytea), 4326)";
        if (anchor.inside()) {
            return "(l.geom IS NOT NULL AND ST_Intersects(l.geom, " + at + "))";
        }
        double[] degrees = RuleSqlBuilder.expandDegrees(point.getY(), anchor.radiusMeters());
        p.addValue(prefix + "DLon", degrees[0]);
        p.addValue(prefix + "DLat", degrees[1]);
        p.addValue(prefix + "M", anchor.radiusMeters());
        return "(l.geom IS NOT NULL AND l.geom && ST_Expand(" + at + ", CAST(:" + prefix + "DLon AS double precision),"
                + " CAST(:" + prefix + "DLat AS double precision)) AND fs_dwithin(l.geom, " + at
                + ", CAST(:" + prefix + "M AS double precision)))";
    }

    // ------------------------------------------------------------------------- state

    State state(UUID ruleId, NearAnchor anchor) {
        MapSqlParameterSource p = new MapSqlParameterSource("rule", ruleId)
                .addValue("source", anchor.source())
                .addValue("id", anchor.externalId());
        List<State> rows = jdbc.query(LOAD_STATE, p, (rs, i) -> {
            byte[] wkb = rs.getBytes("geom");
            long seq = rs.getLong("eval_seq");
            Long evalSeq = rs.wasNull() ? null : seq;
            OffsetDateTime at = rs.getObject("eval_at", OffsetDateTime.class);
            return new State(point(wkb), evalSeq, at == null ? null : at.toInstant(), rs.getString("status"));
        });
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void save(UUID ruleId, NearAnchor anchor, Point geom, Long seq, Instant at, String status) {
        jdbc.update(UPSERT_STATE, new MapSqlParameterSource("rule", ruleId)
                .addValue("source", anchor.source())
                .addValue("id", anchor.externalId())
                .addValue("geom", geom == null ? null : new WKBWriter(2).write(geom))
                .addValue("seq", seq)
                .addValue("at", OffsetDateTime.ofInstant(at, ZoneOffset.UTC))
                .addValue("status", status));
    }

    private void markStale(RuleSpec rule, AnchorPositions.Fix fix, Instant now, State state) {
        String why = fix == null ? "has never been observed"
                : fix.tombstone() ? "is gone (tombstoned)"
                : fix.point() == null ? "has no position"
                : fix.validUntil() != null && !fix.validUntil().isAfter(now) ? "has expired"
                : "was last observed more than " + rule.anchor().maxAnchorAge() + " ago";
        String message = "ANCHOR_STALE: the anchor " + rule.anchor().source() + ":" + rule.anchor().externalId() + " "
                + why + "; the rule is suspended until it is observed again";
        save(rule.id(), rule.anchor(), null, fix == null ? null : fix.seq(), now, "stale");
        jdbc.update("UPDATE trigger_rule SET last_error = :message, last_error_at = :at WHERE id = :id",
                new MapSqlParameterSource("message", message)
                        .addValue("at", OffsetDateTime.ofInstant(now, ZoneOffset.UTC))
                        .addValue("id", rule.id()));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ruleId", rule.id().toString());
        data.put("code", "ANCHOR_STALE");
        try {
            events.publishEvent(new SystemEvent(STALE_SUBTYPE, "warning", "A \"near me\" watch is suspended",
                    "The watch '" + rule.name().replace("*", "").replace("_", "") + "' is suspended: its anchor " + why
                            + ".", "/v1/triggers/" + rule.id(), data));
        } catch (RuntimeException e) {
            log.warn("Could not publish the stale-anchor notification of rule {}", rule.id(), e);
        }
        log.info("Rule {} suspended: anchor {}:{} {}", rule.id(), rule.anchor().source(), rule.anchor().externalId(), why);
    }

    private void resume(RuleSpec rule, Instant now) {
        jdbc.update("UPDATE trigger_rule SET last_error = NULL, last_error_at = NULL WHERE id = :id"
                + " AND last_error LIKE 'ANCHOR_STALE%'", new MapSqlParameterSource("id", rule.id()));
        log.info("Rule {} resumed: anchor {}:{} is fresh again", rule.id(), rule.anchor().source(),
                rule.anchor().externalId());
    }

    private static Point point(byte[] wkb) {
        if (wkb == null) {
            return null;
        }
        try {
            Point p = (Point) new WKBReader(Antimeridian.FACTORY).read(wkb);
            p.setSRID(4326);
            return p;
        } catch (ParseException | ClassCastException e) {
            return null;
        }
    }
}
