package ai.foresignal.testsupport.notify;

import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Helpers of the prompt 033 integration tests: trigger rules saved directly and firing alerts
 * inserted with SQL, so actions, the inbox and escalations work on real rows without running the
 * trigger engine.
 */
public final class ActionFixture {

    private final JdbcTemplate jdbc;
    private final TriggerRuleRepository rules;

    /** Creates the fixture. */
    public ActionFixture(JdbcTemplate jdbc, TriggerRuleRepository rules) {
        this.jdbc = jdbc;
        this.rules = rules;
    }

    /** A saved rule named {@code name}. */
    public UUID rule(String name, Instant now) {
        TriggerRuleEntity rule = new TriggerRuleEntity(UUID.randomUUID(), name);
        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);
        return rules.saveAndFlush(rule).getId();
    }

    /** A saved rule with escalation policy {@code policyId}. */
    public UUID rule(String name, Instant now, UUID policyId) {
        TriggerRuleEntity rule = new TriggerRuleEntity(UUID.randomUUID(), name);
        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);
        rule.setEscalationPolicyId(policyId);
        return rules.saveAndFlush(rule).getId();
    }

    /** A firing alert of {@code ruleId}; returns its id. */
    public long alert(UUID ruleId, String severity, Instant firedAt) {
        Long id = jdbc.queryForObject("""
                insert into alert (rule_id, seq, source_id, external_id, kind, fired_at, observed_at, message, severity,
                                   state, suppressed, tags, rev)
                values (?, 1, 'usgs-earthquakes', ?, 'earthquake', ?, ?, 'M5.1 near Austin', ?, 'firing', false,
                        CAST('[]' AS jsonb), nextval('alert_rev'))
                returning id
                """, Long.class, ruleId, "us" + UUID.randomUUID(), Timestamp.from(firedAt), Timestamp.from(firedAt),
                severity);
        return id == null ? 0 : id;
    }

    /** The state of alert {@code id}. */
    public String state(long id) {
        return jdbc.queryForObject("select state from alert where id = ?", String.class, id);
    }

    /** The {@code acknowledged_by} of alert {@code id}. */
    public String acknowledgedBy(long id) {
        return jdbc.queryForObject("select acknowledged_by from alert where id = ?", String.class, id);
    }
}
