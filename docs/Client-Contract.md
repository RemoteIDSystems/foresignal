# Foresignal — Client Contract (v1)

How applications built on Foresignal name, fetch, relate, annotate and share focus on the same objects. It is the reference for every frontend (3D globe, 2D map, analyst workstation, satellite and RF consoles, mobile, embedded widgets) and for agents. Prompt 129 implements it; prompt 141's SDKs ship helpers for it. `GET /v1/contract` reports the versions in force.

---

## 1. Principles

1. **Foresignal is the intelligence foundation; clients are views.** Entity resolution, correlation, history, forecasts, investigations and validation live in Foresignal. A client never re-implements them; it renders, filters and interacts.
2. **One owner for shared state.** Workspaces, annotations, relations, saved views (036) and investigations (101) are owned by Foresignal. Clients cache them, never fork them.
3. **Headless stays headless.** Foresignal stores client layout only as opaque JSON and never interprets UI state.
4. **Works without an LLM.** Everything in this contract is deterministic; AI enriches, it is never needed to render or investigate.
5. **Deployment-neutral.** The same contract supports one application with switchable workspaces, several independent applications, or a hybrid of standalone modules inside an optional shell. Recommended path: build the first frontend standalone on the shared SDK and this contract; add a unifying shell only when several frontends exist.

## 2. Object references

Grammar: `{type}:{id}`. Ids are opaque to clients; compare references as strings. In a URL path a reference is **one segment**: percent-encode `/` as `%2F` (`/v1/objects/record:adsblol-mil%2Fae1234`), and a `%` already inside the reference as `%25`.

| Type | Id format | Example | Owner |
|---|---|---|---|
| `record` | `{sourceId}/{externalId}` with `externalId` percent-encoded (`/` → `%2F`, `@` → `%40`, `%` → `%25`, whitespace and control characters as their UTF-8 bytes); optional `@{seq}` pins a version | `record:adsblol-mil/ae1234@88120391` | 014 |
| `entity` | registry id (a merged entity resolves to the entity it was merged into) | `entity:01J…` | 098 |
| `canonical-event` | ULID | `canonical-event:01J…` | 116 |
| `intel-event` | ULID | `intel-event:01J…` | 101 |
| `situation` | ULID | `situation:01J…` | 118 |
| `finding` | ULID | `finding:01J…` | 119 |
| `precursor` | id | `precursor:…` | 120 |
| `base-rate` | id | `base-rate:…` | 121 |
| `question`, `forecast` | ULID | `question:01J…` | 122 |
| `hypothesis-set`, `hypothesis`, `indicator` | ULID | `hypothesis-set:01J…` | 125 |
| `assessment` | ULID | `assessment:01J…` | 128 |
| `alert` | id | `alert:42` | 017 |
| `investigation` | id (display id `INV-yyyy-nnnn` also accepted) | `investigation:01J…` | 101 |
| `place`, `view`, `watchlist` | id (a view also by slug) | `place:home` | 036 |
| `source` | source id | `source:usgs-earthquakes` | 004 |
| `sensor` | `{kind}:{id}` with kind `source`, `receiver`, `station`, `satellite` (catalog number), `scene` (`{sourceId}/{externalId}`) or `raster-product` | `sensor:satellite:40697` | 129 |
| `workspace`, `annotation` | ULID | `workspace:01J…` | 129 |
| `principal`, `group`, `tag`, `attachment`, `evidence-item`, `saved-search` | id | `principal:jsmith` | 023, 130 |
| `chronicle` | ULID (display id `CHR-yyyy-nnnn` also accepted) | `chronicle:01J…` | 131 |
| `report` | ULID (display id `RPT-yyyy-nnnn`) | `report:01J…` | 132 |
| `news-story`, `outlet` | ULID / id | `news-story:01J…` | 133 |
| `narrative`, `claim`, `social-comparison` | ULID | `narrative:01J…` | 136, 137 |
| `remote` | `{instanceId}/{type}:{id}`, read-only material received from another instance | `remote:fs-eu-1/report:01J…` | 132 |

A type is accepted from the prompt that owns it on: `GET /v1/contract` lists the types in force (the 23 types of prompt 129 today; prompts 130–137 add theirs with a contract minor version bump). Unknown types and malformed ids are rejected with `400 OBJECT_REF_INVALID`. A reference to an object that does not exist, did not exist at `asOf`, or that the caller may not see is `404 UNKNOWN_OBJECT`: private and unshared objects are invisible, not forbidden.

## 3. Object envelope

Every type resolves to the same envelope (`GET /v1/objects/{ref}`, `POST /v1/objects/resolve`):

```json
{
  "ref": "situation:01JA8…",
  "type": "situation",
  "title": "Red Sea shipping disruption",
  "summary": "14 signals from 4 families across YE, ER, DJ since 2026-10-02 …",
  "textOrigin": "deterministic",
  "status": "active",
  "severity": "warning",
  "time": { "start": "2026-10-02T03:10:00Z", "end": null, "observedAt": "2026-10-11T08:00:00Z" },
  "geometry": { "type": "Polygon", "coordinates": [[…]] },
  "bbox": [32.1, 11.2, 45.0, 20.4],
  "private": false,
  "tags": [],
  "provenance": { "sourceIds": ["aisstream", "nga-msi-warnings"], "manifestId": "01JA9…", "asOfSeq": 88120391 },
  "links": { "self": "/v1/situations/01JA8…", "related": "/v1/objects/situation:01JA8…/related" },
  "actions": [
    { "id": "add-to-workspace", "label": "Add to workspace", "method": "PATCH", "href": "/v1/workspaces/{workspaceId}/context", "scope": "read" },
    { "id": "annotate", "label": "Annotate", "method": "POST", "href": "/v1/objects/situation:01JA8…/annotations", "scope": "read" },
    { "id": "verdict", "label": "Confirm or reject", "method": "POST", "href": "/v1/objects/situation:01JA8…/annotations", "scope": "agent" },
    { "id": "open-investigation", "label": "Open an investigation", "method": "POST", "href": "/v1/investigations", "scope": "agent" }
  ]
}
```

- The type-specific API (`links.self`) has the full object.
- `summary` is at most 280 characters; `textOrigin` says whether it is deterministic or LLM-written (an alert's enrichment summary is `llm`).
- `geometry` is simplified to at most 500 vertices (topology-preserving), or a point; `bbox` is the unsimplified extent.
- `tags` stays empty until prompt 130 adds the caller's visible tags.
- **`asOf`** (transaction time) is honored for records (the version known then, `400 ASOF_BEYOND_RETENTION` beyond the source's horizon) and situations (status and severity from the daily snapshot on or before that day). Every other type exists at `asOf` only if it was created by then and otherwise shows its current state. A record reference with `@{seq}` always resolves to that version.
- **`actions`** list only what the **calling token** may do with this object, from the access policy and the object's state, so clients render buttons from them instead of hard-coding permissions:

| Action | Who | On | Request |
|---|---|---|---|
| `add-to-workspace` | any token | every object but workspaces and annotations | `PATCH /v1/workspaces/{workspaceId}/context` (add the ref to `selectedRefs`) |
| `annotate` | any token | every object but workspaces and annotations | `POST /v1/objects/{ref}/annotations` with `kind: note` |
| `verdict` | agent | records, entities, canonical events, intel events, situations, findings, precursors, hypotheses, indicators, alerts, assessments | `POST /v1/objects/{ref}/annotations` with `kind: verdict` |
| `track` | agent | records, entities | `POST /v1/triggers` (a 017 watch) |
| `propose-watch` | a token without agent scope | records, entities | `POST /v1/objects/{ref}/annotations`: a note tagged `propose-watch` that an agent-scope operator turns into a watch |
| `acknowledge` | agent | a `firing` alert | `POST /v1/alerts/{id}/ack` |
| `open-investigation` | agent | records, canonical events, intel events, situations, findings, alerts (not when dismissed) | `POST /v1/investigations` |
| `delete` | admin | workspaces, annotations | `DELETE` on the object |

Anonymous callers (`openReads`) get no actions.

### 3.1 Composed reads for panels

- **Dossier** (`GET /v1/objects/{ref}/dossier?include=overview,imagery,forecast,impacts,related,timeline,analysis&asOf&facets`): everything a "selected object" panel needs in one bounded response (≤ 64 KB, with an ETag; `If-None-Match` answers `304`). Each section carries `status` (`ok | empty | unavailable | not-installed`), so panels render partially; a section that fails is `unavailable` with a message while the others still render. When the response would exceed 64 KB the longest lists are halved first and the section and the dossier are marked `truncated`.
  - `overview`: the type's key fields (for a record, its current payload fields that the source declares in `fieldSpecs`).
  - `imagery`: raster frames, SatDump products and STAC scenes intersecting the object's bbox in its time window, newest first (≤ 12), with frame and thumbnail URLs.
  - `forecast`: official products about the object (for an NHC storm or a situation holding one: the NHC cone and track, `issuer: official`) and ledger questions about it (linked by the question or by an `about` relation), each a `LedgerQuestionBrief` with the ensemble probability, ICD 203 term, `issuer: foresignal-ensemble` and every forecaster's latest probability and knowledge cutoff.
  - `impacts`: open 127 projections whose source is the object (or the situation), with probabilities, intervals, dependency-path counts and labels.
  - `related`: `nearby` (open situations, intel events and findings within 100 km, with distance), `operational` (objects linked by relations), `infrastructure` (`depends-on` relations and 104 facilities the object's geometry touches) and `historical` (121 analogs of a situation).
  - `timeline`: versions, alerts, findings derived from the object, situation log entries and annotations, newest first (≤ 50).
  - `analysis`: findings, hypothesis sets and assessments that reference the object.
  - With `facets=true`, `imagery`, `related` and `timeline` count their evidence by class (`imagery | signals | reports | documents | sensor | social | other`, from the 119 signal family of each record, else the 116 originator class of its source) and by source.
- **Context summary** (`POST /v1/context/summary`, or `GET /v1/workspaces/{id}/summary`): for a viewport (bbox, or center and radius), a time window, filters (`kinds`, `minSeverity`) and layers, in ≤ 32 KB and cached per normalized context and caller for 30 s (`cacheAgeSeconds`):
  - `layerCounts`: current head ⋈ live counts per kind (or per requested layer: a kind or a source id), all and unexpired (`liveCount`); aggregates only, never features;
  - `activity`: alerts, findings and situation changes in the window and viewport, merged newest first (≤ 50), each with its signal family;
  - `insights`: top findings (with their base rate), open ledger questions about situations in the viewport (probability, ICD term, `issuer: foresignal-ensemble`, the `base-rate` forecaster's probability as `baseRate`, and the ensemble's knowledge cutoff) and hypothesis band changes;
  - `related`: the top open situations in the viewport;
  - `narrative` is null: this summary is deterministic. Layer counts describe the current state; `asOf` bounds the activity and insights.
  - A workspace without a viewport or time range answers `409 WORKSPACE_CONTEXT_INCOMPLETE`.

### 3.2 Labeling forecasts and text

Every probability, track or cone a client shows comes from Foresignal with its **issuer** and **knowledge cutoff**:
- `issuer: official` for upstream products (NHC track and cone, SPC outlooks, WPC);
- `issuer: foresignal-ensemble`, or a specific forecaster id (`statistical`, `impact`, `llm:{profile}:{provider}`), for ledger forecasts, with the question's resolution criteria and the forecaster's track record one link away;
- the ICD 203 term exactly as returned, and the base rate when there is one.

Clients never compute or invent probabilities. Text marked `textOrigin: llm` is optional decoration; every panel must still render without it (LLM offline or over budget).

## 4. Relations

Time-aware edges with `validFrom`/`validTo`, `confidence` (0–1), `label` (`confirmed | likely | possible | speculative`, the 104 vocabulary), provenance (`{component, version, manifestId}` for projected relations, `{operator}` for operator and agent relations), `projected` and `private` (true when either end is private; hidden from callers without private access).

| Type | Meaning | Producer |
|---|---|---|
| `member-of` | object belongs to a situation, investigation or hypothesis set (hypotheses and indicators) | 118, 101, 125 |
| `observed-by` | a canonical event and each observation of it (`canonical-event` → `record`) | 116 |
| `same-as` | two references denote the same thing: a re-report and the first observation of its evidence group; a linked record and its entity, with the link's validity | 116, 098 |
| `located-in` | within a place (computed on demand from the 036 places, never stored) | 036 |
| `near` | within a distance (computed for dossiers and for the first hop of common dependencies) | computed |
| `depends-on` | infrastructure dependency, dependent → dependency, with the 104 edge's label and score | 104 |
| `affects` | projected or observed impact | operators (127 projections are linked through the dossier's `impacts`) |
| `derived-from` | a finding derived from its member objects and its precursor | 119 |
| `evidence-for` / `evidence-against` | supports or contradicts an intel event (member role `supporting` / `contradicting`; `context` members are `member-of`) or a hypothesis (ACH rating `CC`/`C` / `I`/`II`) | 101, 125 |
| `about` | a question or hypothesis set concerns an object | 122, 125 |
| `mentions` | a text record names an object | operators |
| `precedes` | A tends to come before B (validated precursor) or came before it (timeline) | operators |
| `causes` | asserted causal link; never machine-asserted; an agent's `causes` is labeled at most `possible` (confidence ≤ 0.5) until an admin token (the operator) asserts it | operators |
| `related-to` | operator-asserted link (also used for computed analog links in dossiers) | operators, agents |

**Projection.** `RelationProjector` (1.0.0) materializes relations from the structures above. Each owning structure projects to a set of relations under a stable key, so re-projecting is idempotent; a vanished member's relation is retired and its validity closed at that moment. Record-backed owners (situations, intel events, hypothesis sets, findings) are re-projected when their records change (durable cursor `relation.cursor`); a nightly reconcile (03:40 UTC) re-projects everything, which also covers entity links, facility edges, canonical events, questions and investigations and repairs relations changed by hand. Projected relations are never edited through the API (`409 RELATION_NOT_EDITABLE`); operators and agents add their own (`POST /v1/relations`, agent) and retire them (`DELETE /v1/relations/{id}`).

**Queries.**
- Neighborhood: `GET /v1/objects/{ref}/related?types&direction&asOf&minConfidence&limit`; each item has the relation, its direction, the neighbor's envelope and the distance between the two objects' bbox centers.
- Subgraph: `POST /v1/graph` (≤ 50 seeds, depth ≤ 2, ≤ 2,000 edges, `types`, `minConfidence`, `asOf`): nodes are envelopes, edges to objects the caller may not see are left out and listed in `missing`.
- Common dependencies: `POST /v1/graph/common-dependencies` (2–20 refs): facilities and infrastructure reached by at least two of the objects through `depends-on` and `located-in` paths (up to four hops; an object with a geometry enters the 104 graph through the facilities it touches, a `near` hop labeled `possible`), each with the path from every object and the weakest edge label.
- A relation counts at a time when its validity contains it and it was not retired by then; with `asOf`, it must also have been written by then.

## 5. Workspace context

A workspace is a shared investigation focus. Its **context** is small, versioned and synchronized; its **layout** is opaque client JSON (≤ 64 KB, else `413`).

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

**Roles.** The creator owns a workspace; members are `viewer` or `editor` (principal id → role). Reading needs ownership or membership (an admin token may read any workspace), and a private workspace is also invisible without private access; everyone else gets `404 UNKNOWN_WORKSPACE`. Context and layout changes need the owner or an editor; metadata changes (name, members, saved views, investigation, `private`) need the owner, or an editor whose token holds agent scope; deleting needs the owner or an admin. A missing role is `403 WORKSPACE_FORBIDDEN`. Every workspace operation needs a token.

Rules:
- **Updates** are JSON merge patches (`application/merge-patch+json`) to `PATCH /v1/workspaces/{id}/context` with `If-Match: <contextVersion>` (`42`, `"42"` or `W/"42"`) and, optionally, `Foresignal-Client-Id: <clientId>`. A missing `If-Match` is `400`; a stale version gets `412 WORKSPACE_CONTEXT_STALE` with the current `contextVersion`, and the client re-reads, re-applies its intent and retries. The response is the new state, and its ETag is the new version. `GET /v1/workspaces/{id}` carries the version as its ETag too.
- **Validation** (schema version 1): unknown members are rejected; references must be well formed (`OBJECT_REF_INVALID`); `timeRange.start` is required and `end` (null for "until now") must not precede it; `viewport` is either `bbox` (`[minLon, minLat, maxLon, maxLat]`) or `center` + `radiusKm` (≤ 20,000), and camera details belong in `layout`; `playback.state` is `playing` or `paused`; the active ids are ULIDs; `filters` hold strings, numbers, booleans or arrays of them (≤ 4 KB); ≤ 200 `layers` and ≤ 200 `selectedRefs`; the whole context ≤ 16 KB. Null members are dropped when stored.
- **Sync:** clients subscribe to the `workspace` topic on `/v1/events/stream` with `workspace.workspaceId`, `workspace.clientId` (generated when absent) and `workspace.clientKind`. Each `workspace.context` event carries `{workspaceId, version, patch, originClientId, principal, at}`; every follower receives it, the sender included, so a client ignores its own `originClientId`. A client applies patches in version order; a gap in versions (or a reconnect: workspace events are id-less and are not replayed) → re-read the workspace.
- **Presence:** `workspace.presence` events list the connected clients (`clientId`, `clientKind` ∈ globe | map | analyst | satellite | rf | mobile | agent | other, `principal`, `connectedAt`) with the `change` (`joined` or `left` and the client id). Foresignal compares the followers of each workspace every two seconds and sends one event per change.
- **Layers and filters** are strings and simple values the clients agree on; Foresignal validates shape and size only. Common layer ids are the source ids, kinds and `observability:{family}`.
- Agents may set `focusRef` (MCP `set_workspace_focus`, agent scope, owner or editor role) to show the operator what they mean on every open client; it also adds the object to `selectedRefs`, and the event's `originClientId` is `mcp:{session}`.

## 6. Deep links

A context travels between applications as a deep link payload: `{"v":1,"ws":"01JAB…","ctx":{…context…}}`, JSON, base64url-encoded without padding, ≤ 2 KB (`413` beyond), with null members left out. Applications accept it in a URL fragment, `#fs=<payload>`, on their own host. `GET /v1/workspaces/{id}/context-link` and `POST /v1/context-links {context, workspaceId?}` produce validated payloads (`version`, `workspaceId`, `payload`, `fragment`, `bytes`). `POST /v1/workspaces/{id}/share` (agent; owner or editor) issues a read-only 112 share link (`/v1/k/{key}`, target type `workspace`, `expiresAt` up to 30 days, `maxUses`) that shows the workspace's name, context version, context and deep link, never its layout, owner or members; a private workspace is never shared.

## 7. Time model for clients

- **Event and observed time** describe the world; **transaction time** (`seq`) describes what Foresignal knew. `asOf` replays transaction time: "what did we know then".
- `timeRange` selects the window to show; `asOf` (optional) pins the knowledge cutoff; `playback` animates within `timeRange`.
- Live updates come over SSE with resume and resync (015); clients never poll for changes they can stream.

## 8. Using the contract well

- Fetch envelopes in batches (`resolve`, ≤ 500 refs; missing and invisible refs come back in `missing`, not as errors) and cache by `ref` with ETags.
- Show provenance (sources, manifest) and caveats wherever you show a finding, a probability or a relation; show ICD 203 terms exactly as returned.
- Show coverage (`observability` layers) next to any absence-based view.
- Validation is how analysts teach the system; make it one click on any finding, event or situation. Notes need any token; verdicts (`confirmed`, `rejected`, `needs-review`, `duplicate-of` with `duplicateOfRef`) need agent scope. There is no `tag` annotation kind: labels are 130 tags, and a note may carry free tags. The author or an admin edits (a merge patch) or deletes an annotation; changing a verdict still needs agent scope. Annotations of a private object are private.
- Verdicts feed `GET /v1/analytics/precision?component&since`: per finding detector, intel-event recipe, situation linker and precursor (precursor-active findings), the share confirmed among the latest verdict per object, with a 95% Wilson interval; `needs-review` counts as unlabeled and `duplicate-of` as labeled but not confirmed. A verdict on an object with a time window proposes a historical 123 benchmark case that an operator accepts; nothing is added automatically. A `rejected` verdict dismisses a finding or intel event only when its agent-scope author sends `applyStatus: true`.
- Sensor discovery (`GET /v1/sensors?bbox|placeId&from&to&kinds&limit`, window ≤ 7 days) lists `sensor` envelopes: enabled sources whose 116 detection probability for the area is above 0.2 (with `pDetect`, method and families), local receivers whose last reported position is within 400 km, SatDump stations within 2,500 km, curated satellites (`intel/satellite-capabilities.json`, operator-extensible) with predicted passes above 10° (`passes`, `capabilities`), STAC scenes in the window (`footprint`, `acquiredAt`, thumbnail link) and raster products over the area. Foresignal cannot task public satellites or third-party sensors.
- Offline and edge clients use offline packs (111) and federation (113); the context and references are the same.

## 9. Recommended frontend architecture (informative)

- Keep Foresignal headless; build each frontend (globe, analyst workstation, satellite, RF/sensors, intelligence, operations) as an independently runnable module on a shared client SDK generated from `docs/OpenAPI.json`.
- Share state only through Foresignal: workspaces and their synchronized context (§5), deep links (§6), investigations, chronicles and reports. A unifying shell, when it comes, coordinates layout and navigation, not data.
- Develop against the complete target spec from day one: generate types and clients from `docs/OpenAPI.json`, run a mock server from it, and read `x-foresignal-status` (`planned` or `implemented`) and `x-foresignal-prompt` to know when each operation goes live.
- The SDKs (141) expose `ObjectRef` (parse and format with the encoding rules of §2), the envelope, and a `WorkspaceSync` helper that subscribes to the `workspace` topic, applies remote patches in version order, sends local patches with `If-Match` and a client id, re-reads on a gap or a `412`, and ignores its own `originClientId`.

## 10. Collaboration (prompt 130)

- **Who:** every request runs as a principal (person, agent or service) through its token; `GET /v1/me` returns the principal, scopes, groups and preferences.
- **Data vs work products:** records and analytic objects are readable per scope. Work products (investigations, workspaces, chronicles, reports, saved searches, notes, attachments, evidence items) have an owner, a visibility and an access list; without access they are invisible.
- **TLP:** every work product carries `tlp` (`CLEAR`, `GREEN`, `AMBER`, `AMBER+STRICT`, `RED`). Show it prominently on screen, in exports and in every rendition; never offer a share target the label forbids (the API refuses with `TLP_VIOLATION`).
- **Tags** (personal or shared), **comments** (threads, `@mentions`), **attachments** and **evidence items** (Admiralty grades: source reliability A–F, information credibility 1–6) work on any object reference. Envelopes include the caller's visible tags.
- **Shared with me** and **my items** are single lists (`/v1/shared-with-me`, `/v1/my`), so a workbench's navigation (my investigations, shared with me, recent) needs no client bookkeeping.

## 11. Chronicles (prompt 131)

A chronicle is a saved monitoring thread with its own recorded history. Clients:
- create one from the current workspace (its layers, area and filters become chronicle layers) or accept one the analysis cycles proposed;
- show the projected storage (`/estimate`) before activation and the cap while it runs;
- render the live **thread** (`/thread`, SSE topic `chronicle`) with per-user unread counts;
- replay from chronicle storage only (`/state?at`, `/tiles`, `/tracks`, `/series`, `/frames`), so replay never depends on global retention;
- store their auto-arranged layout in the chronicle's workspace, and open a chronicle with its context;
- offer pause, stop, archive, restore and delete with the owner's confirmation.

## 12. Reports and exchange (prompt 132)

- Build a report from the current selection, a tag, an investigation, a chronicle or a situation (`/v1/reports/draft`); edit blocks; publish to freeze a revision with pinned references; download Markdown, HTML or the JSON bundle.
- Send to principals and groups here, or as a signed package to principals on other instances; received material appears under `remote:` references, read-only, with sender, authors, signature status and TLP.
- Never render a received report as if it were local analysis; show its origin.

## 13. News coverage display (prompt 133)

- Show an outlet's lean and factuality **with the provider and date** of each rating and the consensus; when providers disagree, show the spread.
- Show a story's coverage balance (left, center, right, unrated), state-affiliated share and blindspot flag; link to the articles (outbound links only).
- Treat coverage as context, not truth: a blindspot or skew is a caveat on evidence, never a verdict.

## 14. Display rules for scores and confidence

- A situation has no single probability. Where a design calls for a headline gauge, show the leading hypothesis's probability (with its ICD term) **and**, separately, the evidence quality or confidence; never label a probability as "confidence".
- Every percentage shown must come from a ledger forecast, a hypothesis revision or a finding's statistics, with its issuer and knowledge cutoff (§3.2).

## 15. Chronicle network (prompt 134)

- `GET /v1/chronicles/{id}/related` returns links at the disclosure level the caller is allowed: **full** (both chronicles visible: shared events and entities), or **opaque** (strength band, overlapping dimensions, time overlap; title and owner only if revealed). Render opaque links as "a related thread exists", with a request-access action; never infer or display more.
- Owners set discoverability (`none`, `instance`, `network`) within their TLP's limits; show the effective setting on the chronicle.
- Cluster timelines are already deduplicated by evidence; show which visible chronicles hold each item.
- Hypotheses and reports may show `CIRCULAR_REPORTING`; display it as a caveat on the affected support.

## 16. Social signals (prompts 135–137)

- **Keep the two worlds apart visually.** Narratives and claims are what people say; canonical events and findings are what Foresignal observed. Never draw a claim as an observed event on a map; use a distinct "reported" style, with the place precision (point, locality, admin area, country) and its confidence.
- Show a narrative's **status** (`corroborated`, `unobserved`, `contradicted`, `mixed`, `insufficient-coverage`, `pending`, `unassessed`) with its reason: the matching physical evidence, or the detectability that made "unobserved" meaningful. `unobserved` means "no corresponding change despite adequate coverage", never "false".
- Show volume as **velocity against baseline** with the platforms covered and whether the path is complete or sampled; show origin counts (evidence groups) and descent share next to post counts, so 50,000 posts from three origins read as three.
- Label the earliest post as **earliest observed**, never as the originator. Show coordination as indicators with their null comparison, never as labels on accounts.
- Lead times shown in assessments are out-of-sample statistics; a single case is "observed lead time".
- Live updates: SSE topic `social` (`narrative.*`, `claim.extracted`, `comparison.updated`, `cue.completed`, `attention.gap`, `media.reuse`, `coordination.detected`, `social.velocity`).

## 17. Versioning

- `GET /v1/contract` → `{contractVersion, objectTypes, relationTypes, contextSchemaVersion, deepLinkVersion}` (today `1.0`, the 23 types of §2 owned by prompts up to 129, the 15 relation types of §4, `1`, `1`).
- Within `/v1`, changes are additive (new types, relation types, optional context fields). Removing or renaming anything requires a new major contract version and a deprecation period announced through `Deprecation` headers.
