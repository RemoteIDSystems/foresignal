package ai.foresignal.inbound.edp;

/**
 * One validation problem (OpenAPI {@code EdpItemRejection.schemaErrors}, {@code EdpValidationReport.manifestErrors}).
 *
 * @param path a JSON Pointer to the offending value ({@code /streams/0/kind}), or an empty string
 *     for the document root
 * @param message what is wrong
 */
public record EdpSchemaError(String path, String message) {}
