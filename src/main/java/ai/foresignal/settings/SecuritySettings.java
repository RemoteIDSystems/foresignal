package ai.foresignal.settings;

import ai.foresignal.security.TokenRestrictions;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Whether data reads need a token (Architecture §15), and how tokenless reads are restricted.
 *
 * <p>Open by default, because Foresignal is expected to sit on a trusted network and serve whatever
 * frontends are on it. Turning it off makes every data GET require at least the {@code read} scope.
 *
 * <p>Stored under {@link SettingKeys#SECURITY}; {@code GET/PUT /v1/settings/security} (prompt 023,
 * {@code SecuritySettingsService}) read and replace it. {@code exposeMlat} (069) is ignored when
 * stored.
 *
 * @param openReads true when data GETs are served without a token
 * @param exposeObserver true when record observers are returned with {@code include=observer} to
 *     read-scope and anonymous callers too; otherwise only agent and admin tokens see them
 *     (Core-Data-Model §14, prompt 014)
 * @param anonymous the anonymous restriction profile applied to tokenless reads while
 *     {@code openReads} is on (prompt 035), or null for the default: no source, kind or area limit,
 *     private sources excluded, data GETs only, {@value #DEFAULT_ANONYMOUS_RPM} requests per minute
 * @param metricsOpen when false, {@code /actuator/prometheus} requires an admin token (prompt 035);
 *     null reads as {@value #DEFAULT_METRICS_OPEN}
 */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SecuritySettings(boolean openReads, Boolean exposeObserver, TokenRestrictions anonymous,
        Boolean metricsOpen) {

    public static final boolean DEFAULT_OPEN_READS = true;

    public static final boolean DEFAULT_EXPOSE_OBSERVER = false;

    /** {@code metricsOpen} when unset: the LAN assumption. */
    public static final boolean DEFAULT_METRICS_OPEN = true;

    /** The anonymous profile's default {@code maxRequestsPerMinute}. */
    public static final int DEFAULT_ANONYMOUS_RPM = 120;

    /** A value stored before {@code exposeObserver} or {@code metricsOpen} existed reads with the defaults. */
    public SecuritySettings {
        if (exposeObserver == null) {
            exposeObserver = DEFAULT_EXPOSE_OBSERVER;
        }
        if (metricsOpen == null) {
            metricsOpen = DEFAULT_METRICS_OPEN;
        }
    }

    /** {@code openReads} and {@code exposeObserver} with the default anonymous profile (the 023 shape). */
    public SecuritySettings(boolean openReads, Boolean exposeObserver) {
        this(openReads, exposeObserver, null, DEFAULT_METRICS_OPEN);
    }

    /** Only {@code openReads}; observers stay gated. */
    public SecuritySettings(boolean openReads) {
        this(openReads, DEFAULT_EXPOSE_OBSERVER);
    }

    /** The shipped default: data reads are open, observers gated, metrics open. */
    public static SecuritySettings defaults() {
        return new SecuritySettings(DEFAULT_OPEN_READS, DEFAULT_EXPOSE_OBSERVER);
    }

    /**
     * The anonymous profile in force: the stored one with {@code excludePrivate} forced on (private
     * sources are never served anonymously) and {@code maxRequestsPerMinute} defaulted.
     */
    public TokenRestrictions effectiveAnonymous() {
        TokenRestrictions a = TokenRestrictions.orNone(anonymous);
        return new TokenRestrictions(a.sources(), a.kinds(), a.bbox(), a.endpoints(), a.area(), Boolean.TRUE,
                a.maxRequestsPerMinute() == null ? DEFAULT_ANONYMOUS_RPM : a.maxRequestsPerMinute());
    }

    /** True when the anonymous profile limits no source, kind or area. */
    public boolean anonymousUnrestricted() {
        return !TokenRestrictions.orNone(anonymous).hasDataRestrictions();
    }
}
