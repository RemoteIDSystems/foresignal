# Foresignal — Client Contract (v1)

How applications built on Foresignal name, fetch, relate, annotate and share focus on the same objects. It is the reference for every frontend (3D globe, 2D map, analyst workstation, satellite and RF consoles, mobile, embedded widgets) and for agents. Prompt 128 implements it; prompt 131's SDKs ship helpers for it. `GET /v1/contract` reports the versions in force.

---

## 1. Principles

1. **Foresignal is the intelligence foundation; clients are views.** Entity resolution, correlation, history, forecasts, investigations and validation live in Foresignal. A client never re-implements them; it renders, filters and interacts.
2. **One owner for shared state.** Workspaces, annotations, relations, saved views (036) and cases (101) are owned by Foresignal. Clients cache them, never fork them.
3. **Headless stays headless.** Foresignal stores client layout only as opaque JSON and never interprets UI state.
4. **Works without an LLM.** Everything in this contract is deterministic; AI enriches, it is never needed to render or investigate.
5. **Deployment-neutral.** The same contract supports one application with switchable workspaces, several independent applications, or a hybrid of standalone modules inside an optional shell. Recommended path: build the first frontend standalone on the shared SDK and this contract; add a unifying shell only when several frontends exist.

## 2. Object references

Grammar: `{type}:{id}`. Ids are opaque to clients; compare references as strings.

| Type | Id format | Example | Owner |
|---|---|---|---|
| `record` | `{sourceId}/{externalId}` with `externalId` percent-encoded (`/` → `%2F`, `@` → `%40`, `%` → `%25`); optional `@{seq}` pins a version | `record:adsblol-mil/ae1234@88120391` | 014 |
| `entity` | registry id | `entity:01J…` | 098 |
| `canonical-event` | ULID | `canonical-event:01J…` | 116 |
| `intel-event` | ULID | `intel-event:01J…` | 101 |
| `situation` | ULID | `situation:01J…` | 118 |
| `finding` | ULID | `finding:01J…` | 119 |
| `precursor` | id | `precursor:…` | 120 |
| `base-rate` | id | `base-rate:…` | 121 |
| `question`, `forecast` | ULID | `question:01J…` | 122 |
| `hypothesis-set`, `hypothesis`, `indicator` | ULID | `hypothesis-set:01J…` | 124 |
| `assessment` | ULID | `assessment:01J…` | 127 |
| `alert` | id | `alert:…` | 017 |
| `case` | id | `case:…` | 101 |
| `place`, `view`, `watchlist` | id | `place:…` | 036 |
| `source` | source id | `source:usgs-earthquakes` | 004 |
| `sensor` | `{kind}:{id}` (source, receiver, station, satellite) | `sensor:satellite:25544` | 128 |
| `workspace`, `annotation` | ULID | `workspace:01J…` | 128 |

Unknown types are rejected with `400 OBJECT_REF_INVALID`. New types are added only with a contract minor version bump.

## 3. Object envelope

Every type resolves to the same envelope (`GET /v1/objects/{ref}`, `POST /v1/objects/resolve`):

```json
{
  "ref": "situation:01JA8…",
  "type": "situation",
  "title": "Red Sea shipping disruption",
  "summary": "14 signals from 4 families across YE, ER, DJ since 2026-10-02 …",
  "status": "active",
  "severity": "warning",
  "time": { "start": "2026-10-02T03:10:00Z", "end": null, "observedAt": "2026-10-11T08:00:00Z" },
  "geometry": { "type": "Polygon", "coordinates": [[…]] },
  "bbox": [32.1, 11.2, 45.0, 20.4],
  "private": false,
  "provenance": { "sourceIds": ["aisstream", "nga-msi-warnings"], "manifestId": "01JA9…", "asOfSeq": 88120391 },
  "textOrigin": "deterministic",
  "links": { "self": "/v1/situations/01JA8…", "related": "/v1/objects/situation:01JA8…/related" },
  "actions": [
    { "id": "add-to-workspace", "label": "Add to workspace", "method": "PATCH", "href": "/v1/workspaces/{id}/context", "scope": "read" },
    { "id": "annotate", "label": "Annotate", "method": "POST", "href": "/v1/objects/situation:01JA8…/annotations", "scope": "read" },
    { "id": "verdict", "label": "Confirm or reject", "method": "POST", "href": "/v1/objects/situation:01JA8…/annotations", "scope": "agent" }
  ]
}
```

The type-specific API (`links.self`) has the full object. Envelopes are `asOf`-aware. `actions` lists only what the **calling token** may do with this object, so clients render buttons from it instead of hard-coding permissions. `textOrigin` says whether `summary` is deterministic or LLM-written.

### 3.1 Composed reads for panels

- **Dossier** (`GET /v1/objects/{ref}/dossier?include=overview,imagery,forecast,impacts,related,timeline,analysis`): everything a "selected object" panel needs in one bounded response (≤ 64 KB), section by section, each with a `status` so panels render partially. `related` is grouped as nearby, operational, infrastructure and historical. Served by Foresignal's own spatial queries, relations and analogs; clients never re-run proximity or correlation logic.
- **Context summary** (`POST /v1/context/summary`, or `GET /v1/workspaces/{id}/summary`): for a viewport, time window and filters, the layer counts, a merged activity feed tagged by signal family, insights (findings, forecasts, hypothesis band changes) and top related objects, in ≤ 32 KB.

### 3.2 Labelling forecasts and text

Every probability, track or cone a client shows comes from Foresignal with its **issuer** and **knowledge cutoff**:
- `issuer: official` for upstream products (NHC track and cone, SPC outlooks, WPC);
- `issuer: foresignal-ensemble`, or a specific forecaster id (`statistical`, `impact`, `llm:{profile}`), for ledger forecasts, with the question's resolution criteria and the forecaster's track record one link away;
- the ICD 203 term exactly as returned, and the base rate when there is one.

Clients never compute or invent probabilities. Text marked `textOrigin: llm` is optional decoration; every panel must still render without it (LLM offline or over budget).

## 4. Relations

Time-aware edges with `valid_from`/`valid_to`, `confidence`, `label` (confirmed | likely | possible | speculative) and provenance (component and manifest, or operator).

| Type | Meaning | Typical producer |
|---|---|---|
| `member-of` | object belongs to a situation or case | 118, 101 |
| `observed-by` | an observation of a canonical event or entity | 116, 098 |
| `same-as` | two references denote the same thing (re-reports, entity links) | 116, 098 |
| `located-in` | within a place or region (computed on demand) | 036, 040 |
| `near` | within a distance at a time | computed |
| `depends-on` | infrastructure dependency | 104 |
| `affects` | projected or observed impact | 126 |
| `derived-from` | a finding derived from series or records | 119 |
| `evidence-for` / `evidence-against` | supports or contradicts an event or hypothesis | 101, 124 |
| `about` | a question or assessment concerns an object | 122, 127 |
| `mentions` | a text record names an object | 118 |
| `related-to` | operator-asserted link | operators, agents |

Queries: neighbourhood (`/v1/objects/{ref}/related`), subgraph (`POST /v1/graph`), common dependencies (`POST /v1/graph/common-dependencies`).

## 5. Workspace context

A workspace is a shared investigation focus. Its **context** is small, versioned and synchronised; its **layout** is opaque client JSON.

```json
{
  "workspaceId": "01JAB…",
  "contextVersion": 42,
  "context": {
    "selectedRefs": ["record:adsblol-mil/ae1234", "situation:01JA8…"],
    "focusRef": "record:adsblol-mil/ae1234",
    "timeRange": { "start": "2026-10-10T00:00:00Z", "end": "2026-10-11T00:00:00Z" },
    "asOf": null,
    "playback": { "state": "paused", "rate": 60 },
    "viewport": { "center": [-96.55, 33.75], "radiusKm": 150 },
    "activeSituationId": "01JA8…",
    "activeHypothesisSetId": null,
    "filters": { "kinds": ["aircraft", "vessel"], "minSeverity": "warning" },
    "layers": ["aircraft", "jamming-cells", "observability:aircraft"]
  }
}
```

Rules:
- **Updates** are JSON merge patches to `PATCH /v1/workspaces/{id}/context` with `If-Match: <contextVersion>`; a stale version gets `412`, and the client re-reads, re-applies its intent and retries.
- **Sync:** clients subscribe to the `workspace` topic on `/v1/events/stream` (filter `workspaceId`). Each `workspace.context` event carries `{version, patch, originClientId}`. A client applies patches in version order and ignores its own `originClientId`. A gap in versions → re-read the workspace.
- **Presence:** `workspace.presence` events list connected clients (`clientId`, `clientKind` ∈ globe | map | analyst | satellite | rf | mobile | agent | other).
- **Selection limits:** ≤ 200 `selectedRefs`; `viewport` is either `bbox` or `center` + `radiusKm`; camera details belong in `layout`.
- **Layers and filters** are strings and simple values the clients agree on; Foresignal validates shape and size only. Common layer ids are the source ids, kinds and `observability:{family}`.
- Agents may set `focusRef` (MCP `set_workspace_focus`) to show the operator what they mean on every open client.

## 6. Deep links

A context travels between applications as a deep link payload: `{"v":1,"ws":"01JAB…","ctx":{…context…}}`, JSON, base64url-encoded, ≤ 2 KB. Applications accept it in a URL fragment, `#fs=<payload>`, on their own host. `GET /v1/workspaces/{id}/context-link` and `POST /v1/context-links` produce validated payloads; when share links (112) exist, `POST /v1/workspaces/{id}/share` issues a read-only share key.

## 7. Time model for clients

- **Event and observed time** describe the world; **transaction time** (`seq`) describes what Foresignal knew. `asOf` replays transaction time: "what did we know then".
- `timeRange` selects the window to show; `asOf` (optional) pins the knowledge cutoff; `playback` animates within `timeRange`.
- Live updates come over SSE with resume and resync (015); clients never poll for changes they can stream.

## 8. Using the contract well

- Fetch envelopes in batches (`resolve`, ≤ 500 refs) and cache by `ref` with ETags.
- Show provenance (sources, manifest) and caveats wherever you show a finding, a probability or a relation; show ICD 203 terms exactly as returned.
- Show coverage (`observability` layers) next to any absence-based view.
- Validation (annotations with verdicts) is how analysts teach the system; make it one click on any finding, event or situation.
- Offline and edge clients use offline packs (111) and federation (113); the context and references are the same.

## 9. Versioning

- `GET /v1/contract` → `{contractVersion, objectTypes, relationTypes, contextSchemaVersion, deepLinkVersion}`.
- Within `/v1`, changes are additive (new types, relation types, optional context fields). Removing or renaming anything requires a new major contract version and a deprecation period announced through `Deprecation` headers.
