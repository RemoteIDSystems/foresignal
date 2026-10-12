package ai.foresignal.inbound.format;

import ai.foresignal.source.spi.SourceDescriptor;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Turns one inbound request into records (prompt 024, Gap-Analysis §4c).
 *
 * <p>Formats are Spring beans collected by {@link IngestFormatRegistry} and served at
 * {@code POST /v1/ingest/{sourceId}/{format}}. A format must not write anything: it parses and maps,
 * and the shared push pipeline does the rest. A body that cannot be read at all is a whole-request
 * failure ({@code ai.foresignal.api.error.InvalidInputException}, 400); an item that cannot be
 * mapped is an {@link ItemRejection} and never fails the request.
 */
public interface IngestFormat {

    /** The format id used in the path, kebab-case. */
    String id();

    /** The media types this format reads. */
    Set<MediaType> mediaTypes();

    /** A one-line description for {@code GET /v1/ingest/formats}. */
    default String description() {
        return id();
    }

    /** The options it accepts under {@code formats.{id}}. */
    default List<FormatOptionSpec> options() {
        return List.of();
    }

    /** The HTTP methods it accepts. */
    default Set<HttpMethod> methods() {
        return Set.of(HttpMethod.POST);
    }

    /**
     * Parses {@code request} for the source described by {@code descriptor}.
     *
     * @param request the raw request
     * @param descriptor the target source, for its kinds and time base
     * @param options the source's options for this format
     * @return the records and per-item rejections
     */
    ParsedIngest parse(IngestRequest request, SourceDescriptor descriptor, FormatOptions options);

    /**
     * The reply the sending app expects (prompt 039), or null for Foresignal's {@code IngestOutcome}
     * JSON. It is sent whenever the request was valid, even when some items were rejected (rejections
     * are counted, not retried).
     *
     * @param parsed what {@link #parse} returned
     */
    default NativeReply nativeReply(ParsedIngest parsed) {
        return null;
    }

    /**
     * True when this format may be sent without credentials in a header: such requests carry the key in
     * the path ({@code /v1/ingest/{sourceId}/k/{key}/{format}}, prompt 039). Informational, for
     * {@code listIngestFormats}.
     */
    default boolean pathKeyFriendly() {
        return false;
    }
}
