package ai.foresignal.source.adapters.adsbfi;

import ai.foresignal.source.formats.readsb.ReadsbAircraftMapper;
import ai.foresignal.source.formats.readsb.ReadsbResponses;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamResponse;
import java.time.Duration;

/**
 * What the adsb.fi sources share (prompt 080): the origin, the descriptor fields of the upstream and the response
 * check.
 */
final class AdsbFi {

    /** The production origin. */
    static final String DEFAULT_ORIGIN = "https://opendata.adsb.fi";

    /** Test-only override of {@link #DEFAULT_ORIGIN} (Adapter Guide §8); the request paths are kept. */
    static final String BASE_URL_PROPERTY = "foresignal.test.adapters.adsbfi.base-url";

    /** The upstream's name in messages. */
    static final String NAME = "adsb.fi";

    /** The attribution line: adsb.fi asks to be cited with a link to its home page. */
    static final String ATTRIBUTION = "adsb.fi (https://adsb.fi)";

    /** The data terms, as the license string prompt 109 reads. */
    static final String LICENSE = "adsb.fi open data terms: personal, non-commercial use only; attribution with a"
            + " link to adsb.fi required";

    /** The API's terms. */
    static final String TERMS_URL = "https://github.com/adsbfi/opendata";

    /** The spacing between request starts on {@code opendata.adsb.fi} (1 request per second, plus a margin). */
    static final Duration HOST_PACING = Duration.ofMillis(1100);

    /** The default and minimum poll interval. */
    static final Duration INTERVAL = Duration.ofSeconds(30);

    /** Requests per minute per source. */
    static final int PER_MINUTE = 2;

    /** Requests per day per source (one every 30 s). */
    static final int PER_DAY = 2_880;

    private AdsbFi() {}

    /** {@code override}, or the production origin when blank, without a trailing slash. */
    static String origin(String override) {
        String chosen = override == null || override.isBlank() ? DEFAULT_ORIGIN : override.trim();
        return chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    /**
     * Applies the upstream's descriptor fields: polling every 30 s (minimum 30 s), budget 2/min and 2,880/day,
     * attribution, terms, egress {@code LOCAL_ONLY} (non-commercial terms), a 403 that means "this client is
     * refused", and 1.1 s host pacing.
     */
    static SourceDescriptor.Builder upstream(SourceDescriptor.Builder builder) {
        return builder.mode(Mode.POLLING)
                .homepage("https://adsb.fi")
                .attribution(ATTRIBUTION)
                .licence(LICENSE)
                .termsUrl(TERMS_URL)
                .egress(Egress.LOCAL_ONLY)
                .accessDeniedIsTerminal(true)
                .hostPacing(HOST_PACING)
                .interval(INTERVAL, INTERVAL)
                .defaultBudget(PER_MINUTE, PER_DAY);
    }

    /**
     * Checks a 200 and maps it.
     *
     * @throws ai.foresignal.source.spi.UpstreamFormatException when the body was truncated or is not a readsb
     *     JSON document
     */
    static ReadsbAircraftMapper.MapResult parse(FetchContext ctx, UpstreamResponse response) {
        ReadsbAircraftMapper.MapResult result = ReadsbResponses.map(response, NAME);
        if (result.skipped() > 0) {
            ctx.log().debug("{}: skipped {} aircraft without hex", ctx.sourceId(), result.skipped());
        }
        return result;
    }
}
