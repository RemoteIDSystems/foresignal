package ai.foresignal.collect;

import ai.foresignal.source.spi.AdapterStateStore;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * "Newest file age" health (prompt 048, on the prompt 006 {@link HealthRule} SPI): the frame-age rule
 * ({@link FrameAgeRule}) applied to the newest upstream input an adapter recorded with
 * {@link AdapterStateStore#recordInput}, for sources whose files are not raster frames.
 *
 * <p>{@code goes-glm} records the coverage end of each processed GLM file with a 2-minute gap,
 * {@code goes-fdc} each processed FDC scan with 15 minutes. When the input is older than its gap the
 * source shows {@code degraded} ("newest input file is …s old"), and the source-health change event
 * fires; the adapters themselves freeze their derived records meanwhile, so a stalled feed never reads
 * as an all-clear. A source that has recorded nothing gets no verdict.
 *
 * <p>Both values live in {@code source_health.adapter_state} under
 * {@value AdapterStateStore#NEWEST_INPUT_KEY} and {@value AdapterStateStore#MAX_INPUT_GAP_KEY}.
 */
@Component
public class InputAgeHealthRule implements HealthRule {

    private static final Logger log = LoggerFactory.getLogger(InputAgeHealthRule.class);

    static final String SQL = "select adapter_state ->> ?, adapter_state ->> ? from source_health where source_id = ?";

    private final JdbcTemplate jdbc;

    /** Creates the rule. */
    public InputAgeHealthRule(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<HealthVerdict> evaluate(HealthRuleContext context) {
        String[] values = read(context.sourceId());
        return values == null ? Optional.empty() : verdict(values[0], values[1], context.now());
    }

    /**
     * The verdict for the recorded values (pure, for tests).
     *
     * @param newestInputAt the recorded ISO instant, or null
     * @param maxGap the recorded ISO duration, or null
     * @param now the evaluation instant
     */
    static Optional<HealthVerdict> verdict(String newestInputAt, String maxGap, Instant now) {
        if (newestInputAt == null || maxGap == null) {
            return Optional.empty();
        }
        Instant at;
        Duration gap;
        try {
            at = Instant.parse(newestInputAt);
            gap = Duration.parse(maxGap);
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
        return FrameAgeRule.verdict(at, null, gap, now).map(v -> v.status() == ai.foresignal.source.HealthStatus.HEALTHY
                ? v
                : HealthVerdict.degraded(v.reason().replace("newest frame", "newest input file"), at));
    }

    /** The two recorded values of {@code sourceId}, or null when the source has no row. */
    private String[] read(String sourceId) {
        try {
            return jdbc.query(SQL, rs -> rs.next() ? new String[] {rs.getString(1), rs.getString(2)} : null,
                    AdapterStateStore.NEWEST_INPUT_KEY, AdapterStateStore.MAX_INPUT_GAP_KEY, sourceId);
        } catch (RuntimeException e) {
            log.debug("Could not read the adapter state of {}", sourceId, e);
            return null;
        }
    }
}
