package ai.foresignal.cli.sse;

import ai.foresignal.cli.http.Query;
import java.util.Map;

/**
 * One connection attempt of a {@link ResilientSseStream}: the path, query and headers (such as
 * {@code Last-Event-ID}) built from the stream's current resume position.
 *
 * @param path    the API path, e.g. {@code /v1/changes/stream}
 * @param query   the query
 * @param headers extra request headers
 */
public record StreamRequest(String path, Query query, Map<String, String> headers) {
}
