package ai.foresignal.security.restrict;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a read path that returns or streams records or alerts and applies the caller's
 * {@link ReadRestriction} through {@link RestrictionClamp} (prompt 035).
 *
 * <p>Put it on every controller method and MCP tool class that returns records or alerts.
 * {@code RestrictedReadCoverageTest} fails when one is missing, and {@code docs/Architecture.md}
 * §15 requires it of every new read endpoint.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RestrictedRead {

    /** How the restriction is applied here, for readers of the code (optional). */
    String value() default "";
}
