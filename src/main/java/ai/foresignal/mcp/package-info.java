/**
 * The MCP server at {@code /mcp} (Architecture §18, FR-43, prompt 025): agents connect over
 * streamable HTTP (and, behind {@code foresignal.mcp.legacy-sse.enabled}, the older HTTP+SSE
 * transport) and get Foresignal's data, the alert lifecycle and watch management as tools, within
 * their token's scope.
 *
 * <p>Built directly on the MCP Java SDK core: the SDK's servlet transports and session classes carry
 * the protocol, while {@link ai.foresignal.mcp.McpProtocol} answers the requests so the tool list
 * can depend on the caller. The extension points are beans: a {@link ai.foresignal.mcp.ForesignalMcpTool},
 * {@link ai.foresignal.mcp.ForesignalMcpResource} or {@link ai.foresignal.mcp.ForesignalMcpPrompt}
 * declared anywhere is picked up at startup (027 {@code search}, 029 briefings, 036 places, 037 time
 * travel, 052 weather, 098 entities). The tools themselves live in {@code ai.foresignal.mcp.tools}.
 */
package ai.foresignal.mcp;
