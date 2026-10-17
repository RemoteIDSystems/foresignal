package ai.foresignal.object;

/**
 * One operation the calling token may perform on an object (docs/Client-Contract.md §3): clients render buttons from
 * the envelope's {@code actions} instead of hard-coding permissions.
 *
 * @param id the action id, e.g. {@code annotate}
 * @param label a short human label
 * @param method the HTTP method
 * @param href the API path to call ({@code {id}} placeholders are filled by the client, e.g. the workspace id)
 * @param scope the scope the operation needs ({@code read}, {@code agent} or {@code admin})
 */
public record ObjectAction(String id, String label, String method, String href, String scope) {}
