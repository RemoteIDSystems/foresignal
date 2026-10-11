package ai.foresignal.collect;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Reports whether {@link UsageMeter} is managing to flush, as the Actuator health contributor
 * {@code usageMeter} (the bean name minus the {@code HealthIndicator} suffix).
 *
 * <p>A failed flush loses that minute's counts, and those counts seed the daily budget after a
 * restart, so a flush failing silently widens tomorrow's allowance beyond what the operator
 * configured. This makes it visible: {@code DOWN} with {@code consecutiveFailures} and the last
 * error while flushes fail, {@code UP} once one succeeds.
 *
 * <p>System health, not source health: it never changes any source's status, and it is not part of
 * the OpenAPI contract. The error message is redacted of the database credentials before it is
 * exposed, since a driver error can echo connection details.
 */
@Component
public class UsageMeterHealthIndicator implements HealthIndicator {

    /** Detail key for the failure count. */
    public static final String CONSECUTIVE_FAILURES = "consecutiveFailures";

    /** Detail key for the last error. */
    public static final String LAST_ERROR = "lastError";

    /** The longest exposed error message. */
    static final int MAX_ERROR_LENGTH = 500;

    private final UsageMeter meter;
    private final UrlRedactor redactor;

    /** Creates the indicator, redacting the configured database password. */
    public UsageMeterHealthIndicator(UsageMeter meter, Environment environment) {
        this.meter = meter;
        List<String> secrets = new ArrayList<>();
        secrets.add(environment.getProperty("spring.datasource.password"));
        this.redactor = new UrlRedactor(secrets);
    }

    @Override
    public Health health() {
        int failures = meter.consecutiveFlushFailures();
        if (failures == 0) {
            return Health.up().withDetail(CONSECUTIVE_FAILURES, 0).build();
        }
        String error = meter.lastFlushError();
        String redacted = error == null ? "unknown error" : redactor.redact(error);
        if (redacted.length() > MAX_ERROR_LENGTH) {
            redacted = redacted.substring(0, MAX_ERROR_LENGTH) + "… (truncated)";
        }
        return Health.down()
                .withDetail(CONSECUTIVE_FAILURES, failures)
                .withDetail(LAST_ERROR, redacted)
                .build();
    }
}
