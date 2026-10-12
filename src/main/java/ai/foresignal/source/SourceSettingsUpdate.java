package ai.foresignal.source;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.IngestPolicy;
import java.time.Duration;
import java.util.Map;

/**
 * A partial change to one source's settings.
 *
 * <p>Every field is nullable and null means "leave this alone", which is what makes
 * {@code PUT /v1/sources/{id}} safe to call with only the field an operator meant to change.
 *
 * <p>Two fields need a way to say "clear this", which null cannot express here. {@link #clearRetention}
 * and {@link #clearIngestPolicy} carry that intent, so a caller sending an explicit JSON
 * {@code null} is distinguishable from one that omitted the key.
 *
 * @param enabled whether the source should be collected
 * @param interval poll interval; must be at least the descriptor's {@code minInterval}
 * @param params adapter parameters, replacing the stored set wholesale
 * @param budget request budget; both values must be at least 1
 * @param retention per-source retention override
 * @param clearRetention true to revert to the global default
 * @param ingestPolicy per-source ingest policy override
 * @param clearIngestPolicy true to revert to the descriptor's default policy
 * @param embed whether the source's records are embedded for semantic search (prompt 027); null
 *     leaves it unchanged
 */
public record SourceSettingsUpdate(
        Boolean enabled,
        Duration interval,
        Map<String, Object> params,
        Budget budget,
        Duration retention,
        boolean clearRetention,
        IngestPolicy ingestPolicy,
        boolean clearIngestPolicy,
        Boolean embed) {

    public SourceSettingsUpdate {
        params = params == null ? null : Map.copyOf(params);
    }

    /** An update that leaves {@code embed} unchanged (the members before prompt 027). */
    public SourceSettingsUpdate(
            Boolean enabled,
            Duration interval,
            Map<String, Object> params,
            Budget budget,
            Duration retention,
            boolean clearRetention,
            IngestPolicy ingestPolicy,
            boolean clearIngestPolicy) {
        this(enabled, interval, params, budget, retention, clearRetention, ingestPolicy, clearIngestPolicy, null);
    }

    /** Only the {@code embed} flag. */
    public static SourceSettingsUpdate embed(boolean embed) {
        return new SourceSettingsUpdate(null, null, null, null, null, false, null, false, embed);
    }

    /** Changes nothing. */
    public static SourceSettingsUpdate empty() {
        return new SourceSettingsUpdate(null, null, null, null, null, false, null, false);
    }

    /** Only the enabled flag. */
    public static SourceSettingsUpdate enabled(boolean enabled) {
        return new SourceSettingsUpdate(enabled, null, null, null, null, false, null, false);
    }

    /** Only the parameters. */
    public static SourceSettingsUpdate params(Map<String, Object> params) {
        return new SourceSettingsUpdate(null, null, params, null, null, false, null, false);
    }

    /** Only the interval. */
    public static SourceSettingsUpdate interval(Duration interval) {
        return new SourceSettingsUpdate(null, interval, null, null, null, false, null, false);
    }

    /** Only the ingest policy. */
    public static SourceSettingsUpdate ingestPolicy(IngestPolicy policy) {
        return new SourceSettingsUpdate(null, null, null, null, null, false, policy, false);
    }

    /** Reverts the ingest policy to the descriptor default. */
    public static SourceSettingsUpdate clearingIngestPolicy() {
        return new SourceSettingsUpdate(null, null, null, null, null, false, null, true);
    }

    /** Reverts retention to the global default. */
    public static SourceSettingsUpdate clearingRetention() {
        return new SourceSettingsUpdate(null, null, null, null, null, true, null, false);
    }

    /** This update with {@code retention} set. */
    public SourceSettingsUpdate withRetention(Duration value) {
        return new SourceSettingsUpdate(
                enabled, interval, params, budget, value, false, ingestPolicy, clearIngestPolicy, embed);
    }

    /** This update with {@code enabled} set. */
    public SourceSettingsUpdate withEnabled(boolean value) {
        return new SourceSettingsUpdate(
                value, interval, params, budget, retention, clearRetention, ingestPolicy, clearIngestPolicy, embed);
    }

    /** True when nothing would change. */
    public boolean isEmpty() {
        return enabled == null
                && interval == null
                && params == null
                && budget == null
                && retention == null
                && !clearRetention
                && ingestPolicy == null
                && !clearIngestPolicy
                && embed == null;
    }
}
