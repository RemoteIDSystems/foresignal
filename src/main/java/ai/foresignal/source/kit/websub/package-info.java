/**
 * WebSub (W3C Recommendation) subscriptions for feed-kit sources (prompt 073), off by default.
 *
 * <p>Only when {@code foresignal.kits.websub.enabled=true}, only for feed specs with {@code websub: true}, and only
 * when {@code server.publicBaseUrl} is https: a feed that advertises a hub and a self link is subscribed through the
 * source's budgeted client; the hub verifies the intent at {@code GET /v1/websub/{sourceId}/{token}} and delivers
 * content to {@code POST /v1/websub/{sourceId}/{token}}, signed with the subscription secret. While a subscription is
 * active the poll stretches to a 6 h safety net; renewal happens at 80 % of the lease.
 */
package ai.foresignal.source.kit.websub;
