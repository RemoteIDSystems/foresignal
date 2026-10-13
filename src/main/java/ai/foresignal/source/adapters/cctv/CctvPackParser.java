package ai.foresignal.source.adapters.cctv;

import java.util.List;

/**
 * Reads one pack's catalog response into camera entries (prompt 072).
 *
 * <p>Pure: no clock, no network, no state beyond the {@link PackContext} counters. A body that is not the expected
 * document throws {@link ai.foresignal.source.spi.UpstreamFormatException}; an entry that cannot be used is skipped
 * and counted with {@link PackContext#skip}.
 */
@FunctionalInterface
public interface CctvPackParser {

    /**
     * Parses one response.
     *
     * @param body the response body of the pack's first catalog URL (or of the current page)
     * @param pc the pack, the bodies of its further catalog URLs, and the skip counters
     * @return the entries, in upstream order
     */
    List<CameraEntry> parse(byte[] body, PackContext pc);
}
