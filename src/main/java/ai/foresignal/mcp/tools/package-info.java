/**
 * The MCP tools, resources and prompt of prompt 025: records ({@code query_records},
 * {@code get_record}, {@code get_record_history}, {@code get_changes}, {@code list_kinds}),
 * sources ({@code list_sources}, {@code source_health}), alerts ({@code list_alerts},
 * {@code get_alert}, {@code acknowledge_alert}, {@code set_alert_status}), watches
 * ({@code list_watches}, {@code create_watch}, {@code update_watch}, {@code delete_watch},
 * {@code test_watch}) and {@code system_status}; the resources {@code foresignal://sources} and
 * {@code foresignal://kinds}; and the {@code situation-report} prompt.
 *
 * <p>Each tool is a thin wrapper over the service the REST controller uses, with the same
 * validation and the same private-source filtering; none calls the HTTP API.
 */
package ai.foresignal.mcp.tools;
