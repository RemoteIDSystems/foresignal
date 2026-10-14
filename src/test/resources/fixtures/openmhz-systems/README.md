# `openmhz-systems` fixtures

| File | What it is |
|---|---|
| `challenge-403.html` | Real. The body of `GET https://api.openmhz.com/systems` as served to Foresignal's identifiable client, captured 2026-10-14T00:26:05Z: **HTTP 403**, 5,501 bytes, a Cloudflare "Just a moment..." challenge page |

**Capture command** (prompt 082):

```sh
curl -s -D - -A "Foresignal-fixture-capture (+https://foresignal.ai)" https://api.openmhz.com/systems
```

Response headers included `content-type: text/html; charset=UTF-8`, `cf-mitigated: challenge`, `server: cloudflare`
and a `content-security-policy` naming `challenges.cloudflare.com`. No other User-Agent, header or route was tried,
and the challenge was not solved. There is no JSON fixture: the catalog shape in `OpenMhzAdapterTest` is hand-made
from the OpenMHz web client's use of the endpoint (`synthetic: true`); recapture it if OpenMHz admits API clients.
