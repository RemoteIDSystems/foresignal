package ai.foresignal.space;

import ai.foresignal.collect.HealthRule;
import ai.foresignal.collect.HealthRuleContext;
import ai.foresignal.collect.HealthVerdict;
import ai.foresignal.collect.UsageMeter;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Watches {@code celestrak-gp}'s daily download volume (prompt 064): CelesTrak's usage policy risks a block
 * above 100 MB a day, so health is {@code degraded} above {@value #DEGRADED_MB} MB a day, read from the
 * {@link UsageMeter}. The day's bytes are logged once when the threshold is crossed.
 */
@Component
public class CelestrakBytesHealthRule implements HealthRule {

    private static final Logger log = LoggerFactory.getLogger(CelestrakBytesHealthRule.class);

    /** The source watched. */
    public static final String SOURCE = "celestrak-gp";

    /** The daily volume above which the source is degraded, MB. */
    public static final long DEGRADED_MB = 60;

    private static final long MB = 1024L * 1024L;

    private final UsageMeter usage;
    private final AtomicReference<LocalDate> warnedOn = new AtomicReference<>();

    /** Creates the rule. */
    public CelestrakBytesHealthRule(UsageMeter usage) {
        this.usage = usage;
    }

    @Override
    public Optional<HealthVerdict> evaluate(HealthRuleContext context) {
        if (!SOURCE.equals(context.sourceId())) {
            return Optional.empty();
        }
        long bytes = usage.bytesToday(SOURCE);
        log.debug("{}: {} bytes downloaded today", SOURCE, bytes);
        if (bytes <= DEGRADED_MB * MB) {
            return Optional.empty();
        }
        LocalDate today = LocalDate.ofInstant(context.now(), ZoneOffset.UTC);
        if (!today.equals(warnedOn.getAndSet(today))) {
            log.warn("{}: {} MB downloaded today, above {} MB (CelesTrak may block above 100 MB a day)", SOURCE,
                    bytes / MB, DEGRADED_MB);
        }
        return Optional.of(HealthVerdict.degraded(String.format(java.util.Locale.ROOT,
                "%d MB downloaded from CelesTrak today, above %d MB; CelesTrak may block above 100 MB a day",
                bytes / MB, DEGRADED_MB), null));
    }
}
