package ai.foresignal.security.ratelimit;

/**
 * Per-client rate limits per class, in requests per minute (OpenAPI {@code RateLimitSettings},
 * prompt 035), stored under {@code app_setting['ratelimits']}.
 *
 * <p>A missing or non-positive value takes its default, so a value stored by an older build stays
 * readable; the API itself refuses non-positive values ({@link RateLimitSettingsService#parse}). The
 * components are boxed only so that Jackson 3 (which fails on a missing primitive creator property)
 * can read a partial stored value; after construction none is null except
 * {@link #metricsPerMinute()}.
 *
 * @param read data reads in no other class; a token's {@code maxRequestsPerMinute} overrides it
 * @param tiles map tiles and TileJSON
 * @param stream SSE opens per minute and concurrently open streams
 * @param expensive search, Ask, LLM-backed MCP tools and {@code /v1/sample}
 * @param write every method other than GET, HEAD and OPTIONS
 * @param anonymous every request of a caller without a credential, per client IP, except map tiles (limited by
 *     {@code tiles} alone)
 * @param metricsPerMinute the optional limit of {@code /actuator/prometheus} per client IP; null for
 *     none
 */
public record RateLimitSettings(
        Integer read,
        Integer tiles,
        StreamRates stream,
        Integer expensive,
        Integer write,
        Integer anonymous,
        Integer metricsPerMinute) {

    /** Default {@link #read}. */
    public static final int DEFAULT_READ = 600;

    /** Default {@link #tiles}. */
    public static final int DEFAULT_TILES = 1_200;

    /** Default {@link #expensive}. */
    public static final int DEFAULT_EXPENSIVE = 20;

    /** Default {@link #write}. */
    public static final int DEFAULT_WRITE = 120;

    /** Default {@link #anonymous}. */
    public static final int DEFAULT_ANONYMOUS = 120;

    /** Fills missing or non-positive values with their defaults. */
    public RateLimitSettings {
        read = orDefault(read, DEFAULT_READ);
        tiles = orDefault(tiles, DEFAULT_TILES);
        stream = stream == null ? StreamRates.defaults() : stream;
        expensive = orDefault(expensive, DEFAULT_EXPENSIVE);
        write = orDefault(write, DEFAULT_WRITE);
        anonymous = orDefault(anonymous, DEFAULT_ANONYMOUS);
        metricsPerMinute = metricsPerMinute == null || metricsPerMinute <= 0 ? null : metricsPerMinute;
    }

    /** The shipped defaults: 600, 1,200, 30 opens + 8 concurrent, 20, 120, 120, no metrics limit. */
    public static RateLimitSettings defaults() {
        return new RateLimitSettings(0, 0, null, 0, 0, 0, null);
    }

    /**
     * The per-minute limit of {@code rateClass}; {@link RateClass#STREAM} is the opens per minute and
     * {@link RateClass#METRICS} is 0 when no metrics limit is set.
     */
    public int perMinute(RateClass rateClass) {
        return switch (rateClass) {
            case READ -> read;
            case TILES -> tiles;
            case STREAM -> stream.opensPerMinute();
            case EXPENSIVE -> expensive;
            case WRITE -> write;
            case ANONYMOUS -> anonymous;
            case METRICS -> metricsPerMinute == null ? 0 : metricsPerMinute;
        };
    }

    /**
     * The SSE limits of one client.
     *
     * @param opensPerMinute stream opens per minute
     * @param concurrent streams open at once
     */
    public record StreamRates(Integer opensPerMinute, Integer concurrent) {

        /** Default {@link #opensPerMinute}. */
        public static final int DEFAULT_OPENS_PER_MINUTE = 30;

        /** Default {@link #concurrent}. */
        public static final int DEFAULT_CONCURRENT = 8;

        /** Fills missing or non-positive values with their defaults. */
        public StreamRates {
            opensPerMinute = orDefault(opensPerMinute, DEFAULT_OPENS_PER_MINUTE);
            concurrent = orDefault(concurrent, DEFAULT_CONCURRENT);
        }

        /** 30 opens per minute, 8 concurrent. */
        public static StreamRates defaults() {
            return new StreamRates(0, 0);
        }
    }

    private static Integer orDefault(Integer value, int defaultValue) {
        return value != null && value > 0 ? value : defaultValue;
    }
}
