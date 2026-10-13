package ai.foresignal.source.codec;

import ai.foresignal.source.spi.ValidatedParams;
import java.util.Objects;

/**
 * What a codec session knows about the source it decodes for (prompt 069).
 *
 * @param sourceId the source; used as the receiver id of {@code Observer}s the codec attaches
 * @param params the source's validated parameters, for codec options
 */
public record CodecContext(String sourceId, ValidatedParams params) {

    /** Validates the source id and defaults the parameters. */
    public CodecContext {
        Objects.requireNonNull(sourceId, "sourceId");
        params = params == null ? ValidatedParams.empty() : params;
    }

    /** A context without parameters. */
    public static CodecContext of(String sourceId) {
        return new CodecContext(sourceId, ValidatedParams.empty());
    }
}
