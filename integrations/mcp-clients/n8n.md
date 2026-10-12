# n8n

Two directions: an n8n AI agent calling Foresignal's MCP tools, and an n8n workflow woken by a
Foresignal webhook notification.

## n8n → Foresignal (MCP Client Tool)

Attach an **MCP Client Tool** sub-node (`@n8n/n8n-nodes-langchain.mcpClientTool`) to an AI Agent
node:

| Parameter | Value |
|---|---|
| Endpoint | `http://foresignal.local:8080/mcp` |
| Server Transport | HTTP Streamable |
| Authentication | Bearer Auth, with an n8n "Bearer Auth" credential holding the token |
| Tools to Include | All, or Selected (for example `query_records`, `list_alerts`, `get_alert`) |

Use an **agent** token only for workflows that create watches, briefings or change alerts; a
**read** token otherwise. n8n's own documentation page for this node still describes an "SSE
Endpoint"; the node itself (version 1.2 and later) defaults to HTTP Streamable.

To call one tool as an ordinary workflow step without an AI agent, use the **MCP Client** node with
the same endpoint, transport and authentication, and pick the tool.

## Foresignal → n8n (webhook notifications)

1. In n8n, add a **Webhook** trigger: method POST, a path such as `foresignal`, and in Options turn
   on **Raw Body**, so the body arrives untouched as binary data (`binary.data`). The HMAC is over
   the exact bytes; a re-serialized JSON body would not verify.
2. With the admin token, create a `webhook` channel pointing at the production webhook URL, with an
   HMAC secret (and `config.actions.enabled: true` if the workflow should acknowledge through
   capability URLs):

   ```sh
   curl -fsS -X POST -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
     http://foresignal.local:8080/v1/channels -d '{"name": "n8n", "type": "webhook",
       "events": ["alert"], "config": {"url": "https://n8n.example.lan/webhook/foresignal",
       "actions": {"enabled": true}}, "secrets": {"hmacSecret": "a-long-random-secret"}}'
   ```

3. Add a **Code** node (JavaScript) right after the Webhook node to verify the signature:

   ```js
   const crypto = require('crypto');
   const SECRET = $env.FORESIGNAL_WEBHOOK_SECRET; // or paste it, if $env access is blocked
   const out = [];
   for (let i = 0; i < $input.all().length; i++) {
     const item = $input.all()[i];
     const headers = item.json.headers;
     const ts = headers['x-foresignal-timestamp'];
     const sig = headers['x-foresignal-signature'] || '';
     const body = await this.helpers.getBinaryDataBuffer(i, 'data');
     const age = Math.abs(Date.now() / 1000 - Number(ts));
     if (!ts || !Number.isFinite(age) || age > 300) {
       throw new Error('stale or missing X-Foresignal-Timestamp');
     }
     const mac = crypto.createHmac('sha256', SECRET);
     mac.update(ts + '.');
     mac.update(body);
     const expected = Buffer.from('sha256=' + mac.digest('hex'));
     const given = Buffer.from(sig);
     if (expected.length !== given.length || !crypto.timingSafeEqual(expected, given)) {
       throw new Error('bad X-Foresignal-Signature');
     }
     out.push({ json: JSON.parse(body.toString('utf8')) });
   }
   return out;
   ```

   The signature is `X-Foresignal-Signature: sha256=<hex HMAC-SHA256(secret, "<X-Foresignal-Timestamp>.<raw body>")>`.
   Reject timestamps more than 5 minutes from your clock; a replayed delivery then fails. n8n's
   docs both list `crypto` as available in the Code node and say built-in modules are blocked by
   default; on a self-hosted n8n set `NODE_FUNCTION_ALLOW_BUILTIN=crypto` to be sure.
4. The verified body is `{event, subtype, message, data, foresignalOrigin, actions}`. `subtype` is
   `alert.fired` for a new alert and `alert.acknowledged`, `alert.resolved` or `alert.snoozed` for
   lifecycle updates; branch on it. `actions` (present when the channel enables them) lists
   `{action, label, url, expiresAt}`: an **HTTP Request** node that POSTs to an `ack` URL with
   `Accept: application/json` acknowledges the alert without any Foresignal token in n8n.

The Webhook node responds as soon as it receives the request unless you configure otherwise;
Foresignal only needs a 2xx.
