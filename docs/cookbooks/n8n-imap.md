# Cookbook: n8n mail to ingest

Turn notification mail (status pages, vendor alerts, mailing lists) into Foresignal records, so
watches and briefings can use them. The importable workflow is
[`integrations/n8n/imap-to-ingest.json`](../../integrations/n8n/imap-to-ingest.json):

**Email Trigger (IMAP)** → **Code** (allowlist and authentication gate, text cleanup) → **HTTP
Request** (`POST /v1/ingest/custom-mail`).

**Token scope:** an **ingest key** bound to `custom-mail`, stored as an n8n "Header Auth"
credential (`Authorization` = `Bearer eik_…`). No API token. The admin token is used once to create
the source.

**URLs used:** `/v1/ingest/{sourceId}`, and for setup `/v1/custom-sources` and
`/v1/sources/{sourceId}`.

## 1. A dedicated mailbox

Use a mailbox (or alias) that receives only the notices you want: the workflow marks every message
it reads as read. Create an n8n IMAP credential for it.

## 2. The source

```sh
curl -fsS -X POST http://foresignal.local:8080/v1/custom-sources \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d '{
    "id": "custom-mail", "name": "Mail notices", "kinds": ["mail-notice"],
    "observedAtMode": "RECEIVED", "titleField": "subject", "recordTtl": "14d"}'
curl -fsS -X PUT http://foresignal.local:8080/v1/sources/custom-mail \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d '{"retention": "14d"}'
```

- `observedAtMode: RECEIVED`: Foresignal stamps the time it received the record; the mail's own
  `Date` goes into `eventTime`.
- No `embedFields`: mail text is never embedded for semantic search.
- 14 days of retention and a 14-day TTL.

## 3. Import and edit the workflow

Import the JSON, attach your IMAP and Header Auth credentials, and edit two constants at the top of
the Code node, and the URL of the HTTP Request node:

- `ALLOWED_SENDERS`: the exact sender addresses to accept.
- `AUTHSERV_ID`: the host name your own receiving mail server writes at the start of its
  `Authentication-Results` header (look at the raw headers of one message).

The trigger uses the **Resolved** format, which gives the parsed message (`from`, `subject`,
`text`, `html`, `date`, `messageId`) and `headers`, a map from each lowercase header name to its
full line.

## What the gate does

1. **Sender allowlist:** the first `From` address must be in `ALLOWED_SENDERS`.
2. **Authentication:** the `Authentication-Results` line must start with your `AUTHSERV_ID` and
   contain both `dkim=pass` and `dmarc=pass`. Receiving servers remove `Authentication-Results`
   headers that claim their own id (RFC 8601), so a sender cannot forge yours. n8n keeps only one
   line per header name, so a message carrying an extra `Authentication-Results` header (for
   example from a forwarding hop) fails the check: it fails closed.
3. **Content:** the plain-text part, or the HTML with scripts, styles and tags removed. Nothing is
   rendered and no remote content (images, tracking pixels, links) is fetched.
4. **Record:** `externalId` = the `Message-ID`, `kind: mail-notice`, `eventTime` = the mail date,
   `payload` = `subject`, `from`, the first `http(s)` link in the text, and a 280-character
   `excerpt`.

Everything else is dropped silently; check n8n's executions list when a notice does not arrive.

## Limits

- The IMAP trigger fetches `UNSEEN` messages; it reconnects every 60 minutes by default.
- Ingest: 5 MB and 10,000 records per request, `recordsPerMinute` per source (default 6,000).
- n8n's Code node: `crypto` is not needed here; no built-in modules are required.

## Privacy

- **Mail text is untrusted.** Subjects and excerpts can contain anything a sender writes, including
  instructions aimed at a model. They reach Ask and LLM briefings as data like any other record;
  Foresignal's prompts treat tool output as data, and a model can never change anything without a
  confirmed proposal. Keep the allowlist tight.
- Only the subject, sender, first link and a short excerpt are stored; attachments and the full
  body never leave n8n.
- Mark the source `"private": true` if the notices are personal.
