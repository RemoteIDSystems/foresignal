package ai.foresignal.source;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
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
 * @param downsampleInterval the history downsample interval override (prompt 044, TRACK sources only);
 *     null leaves it unchanged
 * @param clearDownsampleInterval true to revert to {@code retention.trackDownsampleInterval}
 * @param egress the egress override (prompt 069), accepted only for an {@code egressOverridable}
 *     descriptor; null leaves it unchanged
 * @param clearEgress true to revert to the descriptor's {@code egress}
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
        Boolean embed,
        Duration downsampleInterval,
        boolean clearDownsampleInterval,
        Egress egress,
        boolean clearEgress) {

    public SourceSettingsUpdate {
        params = params == null ? null : Map.copyOf(params);
    }

    /** An update that leaves the egress override unchanged (the members before prompt 069). */
    public SourceSettingsUpdate(
            Boolean enabled,
            Duration interval,
            Map<String, Object> params,
            Budget budget,
            Duration retention,
            boolean clearRetention,
            IngestPolicy ingestPolicy,
            boolean clearIngestPolicy,
            Boolean embed,
            Duration downsampleInterval,
            boolean clearDownsampleInterval) {
        this(enabled, interval, params, budget, retention, clearRetention, ingestPolicy, clearIngestPolicy, embed,
                downsampleInterval, clearDownsampleInterval, null, false);
    }

    /** An update that leaves the downsample interval unchanged (the members before prompt 044). */
    public SourceSettingsUpdate(
            Boolean enabled,
            Duration interval,
            Map<String, Object> params,
            Budget budget,
            Duration retention,
            boolean clearRetention,
            IngestPolicy ingestPolicy,
            boolean clearIngestPolicy,
            Boolean embed) {
        this(enabled, interval, params, budget, retention, clearRetention, ingestPolicy, clearIngestPolicy, embed,
                null, false);
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

    /** Only the downsample interval (prompt 044). */
    public static SourceSettingsUpdate downsampleInterval(Duration value) {
        return empty().withDownsampleInterval(value);
    }

    /** Reverts the downsample interval to {@code retention.trackDownsampleInterval} (prompt 044). */
    public static SourceSettingsUpdate clearingDownsampleInterval() {
        return new SourceSettingsUpdate(null, null, null, null, null, false, null, false, null, null, true);
    }

    /** Only the egress override (prompt 069). */
    public static SourceSettingsUpdate egress(Egress value) {
        return empty().withEgress(value);
    }

    /** Reverts the egress override to the descriptor's {@code egress} (prompt 069). */
    public static SourceSettingsUpdate clearingEgress() {
        return new SourceSettingsUpdate(null, null, null, null, null, false, null, false, null, null, false, null,
                true);
    }

    /** This update with {@code retention} set. */
    public SourceSettingsUpdate withRetention(Duration value) {
        return new SourceSettingsUpdate(enabled, interval, params, budget, value, false, ingestPolicy,
                clearIngestPolicy, embed, downsampleInterval, clearDownsampleInterval, egress, clearEgress);
    }

    /** This update with {@code enabled} set. */
    public SourceSettingsUpdate withEnabled(boolean value) {
        return new SourceSettingsUpdate(value, interval, params, budget, retention, clearRetention, ingestPolicy,
                clearIngestPolicy, embed, downsampleInterval, clearDownsampleInterval, egress, clearEgress);
    }

    /** This update with {@code downsampleInterval} set (prompt 044). */
    public SourceSettingsUpdate withDownsampleInterval(Duration value) {
        return new SourceSettingsUpdate(enabled, interval, params, budget, retention, clearRetention, ingestPolicy,
                clearIngestPolicy, embed, value, false, egress, clearEgress);
    }

    /** This update with the egress override set (prompt 069). */
    public SourceSettingsUpdate withEgress(Egress value) {
        return new SourceSettingsUpdate(enabled, interval, params, budget, retention, clearRetention, ingestPolicy,
                clearIngestPolicy, embed, downsampleInterval, clearDownsampleInterval, value, false);
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
                && embed == null
                && downsampleInterval == null
                && !clearDownsampleInterval
                && egress == null
                && !clearEgress;
    }
}
