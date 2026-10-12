package ai.foresignal.inbound.edp;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;

/** A stream's {@code payloadSchema}, vetted against EDP §7 and compiled. Thread-safe. */
public final class CompiledPayloadSchema {

    private final Schema schema;

    CompiledPayloadSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * Validates {@code instance} (an item's {@code properties}).
     *
     * @return every problem with JSON Pointer paths into the instance; empty when it conforms
     */
    public List<EdpSchemaError> validate(JsonNode instance) {
        return errors(schema.validate(instance));
    }

    static List<EdpSchemaError> errors(List<Error> errors) {
        List<EdpSchemaError> out = new ArrayList<>(errors.size());
        for (Error error : errors) {
            String path = error.getInstanceLocation() == null ? "" : error.getInstanceLocation().toString();
            if ("$".equals(path) || "/".equals(path)) {
                path = "";
            }
            out.add(new EdpSchemaError(path, error.getMessage()));
        }
        return out;
    }
}
