package ai.foresignal.inbound.edp;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Reads an EDP batch from a push body or a pull response (docs/External-Data-Protocol.md §3, §4).
 *
 * <p>{@code application/json} is one batch object; {@code application/x-ndjson} is one JSON object
 * per line, the first of which may be a batch header (an object with {@code protocol} and no
 * {@code type}). Limits: 5 MB, nesting depth 32 (enforced by the parser, so a hostile document
 * cannot exhaust the stack), 10,000 items.
 */
@Component
public class EdpBatchReader {

    /** The NDJSON media type. */
    public static final MediaType NDJSON = MediaType.parseMediaType("application/x-ndjson");

    private final JsonMapper json = JsonMapper.builder(JsonFactory.builder()
                    .streamReadConstraints(StreamReadConstraints.builder()
                            .maxNestingDepth(EdpProtocol.MAX_DEPTH)
                            .build())
                    .build())
            .build();

    /**
     * A parsed batch.
     *
     * @param header the batch members other than {@code items} (an empty object for header-less
     *     NDJSON)
     * @param items the items, in order
     */
    public record EdpBatch(JsonNode header, List<JsonNode> items) {

        /** Copies the item list. */
        public EdpBatch {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }

    /**
     * Parses {@code body}.
     *
     * @throws ForesignalException 413 {@code PAYLOAD_TOO_LARGE} over 5 MB; 400
     *     {@code VALIDATION_FAILED} for malformed JSON, nesting deeper than 32 or over 10,000 items
     */
    public EdpBatch read(byte[] body, MediaType contentType) {
        if (body == null || body.length == 0) {
            throw new InvalidInputException("The batch body is empty.");
        }
        if (body.length > EdpProtocol.MAX_BATCH_BYTES) {
            throw new ForesignalException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.PAYLOAD_TOO_LARGE,
                    "The batch is " + body.length + " bytes; the maximum is " + EdpProtocol.MAX_BATCH_BYTES + ".");
        }
        EdpBatch batch = contentType != null && NDJSON.isCompatibleWith(contentType) ? ndjson(body) : jsonBatch(body);
        if (batch.items().size() > EdpProtocol.MAX_ITEMS) {
            throw new InvalidInputException("The batch has " + batch.items().size() + " items; the maximum is "
                    + EdpProtocol.MAX_ITEMS + ".");
        }
        return batch;
    }

    private EdpBatch jsonBatch(byte[] body) {
        JsonNode root = parse(new String(body, StandardCharsets.UTF_8), "The batch");
        if (!root.isObject()) {
            throw new InvalidInputException("The batch must be a JSON object.");
        }
        JsonNode items = root.get("items");
        if (items != null && !items.isNull() && !items.isArray()) {
            throw new InvalidInputException("The batch's items must be an array.");
        }
        List<JsonNode> list = new ArrayList<>();
        if (items != null && items.isArray()) {
            if (items.size() > EdpProtocol.MAX_ITEMS) {
                throw new InvalidInputException("The batch has " + items.size() + " items; the maximum is "
                        + EdpProtocol.MAX_ITEMS + ".");
            }
            items.forEach(list::add);
        }
        ObjectNode header = ((ObjectNode) root).deepCopy();
        header.remove("items");
        return new EdpBatch(header, list);
    }

    private EdpBatch ndjson(byte[] body) {
        String[] lines = new String(body, StandardCharsets.UTF_8).split("\r?\n");
        JsonNode header = json.createObjectNode();
        List<JsonNode> items = new ArrayList<>();
        boolean first = true;
        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            JsonNode node = parse(line, "Line " + lineNumber);
            if (!node.isObject()) {
                throw new InvalidInputException("Line " + lineNumber + " must be a JSON object.");
            }
            if (first && node.has("protocol") && !node.has("type")) {
                ObjectNode copy = ((ObjectNode) node).deepCopy();
                copy.remove("items");
                header = copy;
            } else {
                items.add(node);
                if (items.size() > EdpProtocol.MAX_ITEMS) {
                    throw new InvalidInputException("The batch has more than " + EdpProtocol.MAX_ITEMS + " items.");
                }
            }
            first = false;
        }
        return new EdpBatch(header, items);
    }

    private JsonNode parse(String text, String what) {
        try {
            return json.readTree(text);
        } catch (JacksonException e) {
            throw new InvalidInputException(what + " is not valid JSON (or nests deeper than "
                    + EdpProtocol.MAX_DEPTH + " levels): " + e.getOriginalMessage(), e);
        }
    }
}
