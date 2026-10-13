package ai.foresignal.source.formats.readsb;

import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * The response check every hosted readsb-format aircraft API shares (prompt 080: adsb.fi and airplanes.live; the
 * same rule as prompt 062's adsb.lol check): a truncated body, a non-JSON content type or a body that does not
 * start with <code>{</code> is a format error, never "no aircraft". A throttling front end answers with an HTML
 * page and status 200, and that must not empty the layer.
 *
 * <p>Both upstreams send the adsb.lol v2 envelope ({@code ac[]}, {@code now} in milliseconds, {@code total},
 * {@code ctime}, {@code msg}, {@code ptime}), which {@link ReadsbDocument#parse} reads unchanged.
 */
public final class ReadsbResponses {

    private ReadsbResponses() {}

    /**
     * Checks a 200 and maps every aircraft with {@link MapperOptions#DEFAULTS}.
     *
     * @param response the upstream's answer
     * @param upstream the upstream's name, for the error message (for example {@code adsb.fi})
     * @return the mapped records and the number of entries skipped for want of a {@code hex}
     * @throws UpstreamFormatException when the body was truncated, is not JSON, or is not a readsb document
     */
    public static ReadsbAircraftMapper.MapResult map(UpstreamResponse response, String upstream) {
        if (response.truncated()) {
            throw new UpstreamFormatException(upstream + " response exceeded maxResponseBytes and was truncated", null);
        }
        String type = response.contentType() == null ? "" : response.contentType().toLowerCase(Locale.ROOT);
        if (!type.contains("json") || !startsWithBrace(response.body())) {
            throw new UpstreamFormatException(nonJsonMessage(upstream),
                    new String(response.body(), 0, Math.min(response.body().length, 500), StandardCharsets.UTF_8));
        }
        return ReadsbAircraftMapper.mapAll(ReadsbDocument.parse(response.body()), MapperOptions.DEFAULTS);
    }

    /** The message of a 200 that is not JSON. */
    public static String nonJsonMessage(String upstream) {
        return upstream + " answered HTTP 200 with a non-JSON body; non-JSON 200, treated as throttling";
    }

    /** True when the first non-whitespace byte is <code>{</code>. */
    static boolean startsWithBrace(byte[] body) {
        if (body == null) {
            return false;
        }
        for (byte b : body) {
            if (b == ' ' || b == '\t' || b == '\r' || b == '\n') {
                continue;
            }
            return b == '{';
        }
        return false;
    }
}
