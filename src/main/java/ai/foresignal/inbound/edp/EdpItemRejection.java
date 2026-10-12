package ai.foresignal.inbound.edp;

import java.util.List;

/**
 * Why one EDP item was refused (OpenAPI {@code EdpItemRejection}).
 *
 * @param index the item's position in the batch (0-based)
 * @param stream its stream, when it named one
 * @param id its id, when it had one
 * @param reason a stable code: {@code SCHEMA_INVALID}, {@code UNKNOWN_STREAM},
 *     {@code ITEM_TYPE_MISMATCH}, {@code PAYLOAD_INVALID}, {@code GEOMETRY_INVALID},
 *     {@code PERIOD_INVALID}, {@code GRANULARITY_MIXED}, {@code FORECAST_INVALID},
 *     {@code MISSING_OBSERVED_AT}, {@code OBSERVED_AT_IN_FUTURE}, {@code LIMIT_EXCEEDED},
 *     {@code VALIDATION_BUDGET_EXCEEDED}, or a writer rejection code
 * @param schemaErrors the individual problems; empty when the reason says it all
 */
public record EdpItemRejection(int index, String stream, String id, String reason, List<EdpSchemaError> schemaErrors) {

    /** Copies the error list. */
    public EdpItemRejection {
        schemaErrors = schemaErrors == null ? List.of() : List.copyOf(schemaErrors);
    }

    /** A rejection with a single message under the item root. */
    public static EdpItemRejection of(int index, String stream, String id, String reason, String message) {
        return new EdpItemRejection(index, stream, id, reason,
                message == null ? List.of() : List.of(new EdpSchemaError("", message)));
    }
}
