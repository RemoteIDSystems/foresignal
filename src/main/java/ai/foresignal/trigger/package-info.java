/**
 * Trigger rules and alerts (prompt 017, Core-Data-Model §11).
 *
 * <ul>
 *   <li><b>Rules:</b> {@link ai.foresignal.trigger.RuleValidator} normalizes an API rule (operators from
 *       the {@link ai.foresignal.trigger.OperatorRegistry}, geofences from the
 *       {@link ai.foresignal.trigger.GeofenceMaterializer}); {@link ai.foresignal.trigger.RuleSpec} is the
 *       compiled form every evaluator shares.
 *   <li><b>Evaluation:</b> {@link ai.foresignal.trigger.RuleSqlBuilder} renders one statement per rule
 *       over persisted versions; {@link ai.foresignal.trigger.AlertPass} applies cooldown, snoozes,
 *       active windows, hysteresis and the storm cap in seq order; the
 *       {@link ai.foresignal.trigger.TriggerEngine} runs both from the change bus with a durable cursor.
 *   <li><b>The gate:</b> {@link ai.foresignal.trigger.TriggerPersistGate} makes the writer persist every
 *       predicate flip (invariant I8), with the same operator semantics in Java.
 *   <li><b>Alerts:</b> {@link ai.foresignal.trigger.AlertService} owns the lifecycle,
 *       {@link ai.foresignal.trigger.AlertResolver} resolves on expiry and staleness, and the
 *       {@link ai.foresignal.trigger.AlertBus} fans {@link ai.foresignal.trigger.AlertEvent}s out.
 * </ul>
 *
 * <p>Services take and return plain values, never web types, so prompt 025's MCP tools can call them.
 */
package ai.foresignal.trigger;
