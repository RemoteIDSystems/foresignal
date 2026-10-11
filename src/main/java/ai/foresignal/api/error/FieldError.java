package ai.foresignal.api.error;

/**
 * One field-level problem, matching the {@code errors} array in the OpenAPI {@code Problem} schema.
 *
 * @param field the offending field, in the request's own naming
 * @param message what is wrong with it
 */
public record FieldError(String field, String message) {}
