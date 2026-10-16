# `status-openai` fixtures

Captured 2026-10-16 from OpenAI's status page, an incident.io page with a Statuspage v2 shim. Endpoint:
`https://status.openai.com/api/v2/{summary,incidents}.json`. Licence: provider terms.

summary.json = the live page trimmed to its first three components; it carries no `incidents` array (the shim's
difference from Statuspage) and the indicator sits in the top-level `status` object. incidents.json = the first two
incidents, each trimmed to its newest update; both are resolved and carry `resolved_at`. The shim sent no `ETag`.

**Recapture (prompt 143):** repeat the GET above with Foresignal's identifiable User-Agent.
