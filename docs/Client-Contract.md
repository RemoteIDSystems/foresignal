# Foresignal — Client Contract (v1)

How applications built on Foresignal name, fetch, relate, annotate and share focus on the same objects. It is the reference for every frontend (3D globe, 2D map, analyst workstation, satellite and RF consoles, mobile, embedded widgets) and for agents. Prompt 129 implements it, prompt 130 adds collaboration (principals, access lists, TLP, tags, comment threads, attachments, evidence items and saved searches), prompt 131 adds chronicles (monitoring threads with their own recorded history), prompt 132 adds reports and the exchange of signed intelligence packages between instances, and prompt 141's SDKs ship helpers for it. `GET /v1/contract` reports the versions in force.

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
| `principal` | ULID (the handle also accepted) | `principal:jsmith` | 023, 130 |
| `group`, `tag`, `attachment`, `evidence-item`, `saved-search` | ULID | `evidence-item:01J…` | 130 |
| `chronicle` | ULID (display id `CHR-yyyy-nnnn` also accepted) | `chronicle:01J…` | 131 |
| `report` | ULID (display id `RPT-yyyy-nnnn` also accepted) | `report:01J…` | 132 |
| `news-story`, `outlet` | ULID / id | `news-story:01J…` | 133 |
| `narrative`, `claim`, `social-comparison` | ULID | `narrative:01J…` | 136, 137 |
| `remote` | `{instanceId}/{type}:{id}`, read-only material received from another instance | `remote:fs-eu-1/report:01J…` | 132 |

A type is accepted from the prompt that owns it on: `GET /v1/contract` lists the types in force (contract 1.3: the 23 types of prompt 129, the six of prompt 130, `chronicle` of prompt 131, and `report` and `remote` of prompt 132; prompts 133–137 add theirs with a contract minor version bump). Unknown types and malformed ids are rejected with `400 OBJECT_REF_INVALID`. A reference to an object that does not exist, did not exist at `asOf`, or that the caller may not see is `404 UNKNOWN_OBJECT`: private and unshared objects are invisible, not forbidden.

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
  "tlp": null,
  "tags": ["red-sea"],
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
- `tags` lists the names of the caller's visible tags (its personal tags and the shared tags it may see, §10).
- `tlp` is the TLP 2.0 label of a work product (§10) and of received material (`remote:`, §12), and `null` for data and analytic objects. A work product the caller may not see is not returned at all.
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
| `share` | owner or editor of a work product | investigations, workspaces, chronicles, reports, saved searches, notes, attachments, evidence items, shared tags | `POST /v1/objects/{ref}/share` (agent) |
| `edit-acl` | owner or editor of a work product | the same | `PUT /v1/objects/{ref}/acl` (agent) |

Anonymous callers (`openReads`) get no actions and see no work products. Principals, groups, tags and saved searches are not annotated or added to workspaces.

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

**Roles.** The creator owns a workspace; members are `viewer` or `editor` (principal id → role), and prompt 130's access list adds principal, group and instance grants (an access-list `owner` or `editor` counts as an editor, a `commenter` or `viewer` as a viewer; ownership transfers through the access list). Reading needs ownership, membership or a grant (an admin token may read any workspace), and a private workspace is also invisible without private access; everyone else gets `404 UNKNOWN_WORKSPACE`. `GET /v1/workspaces` lists owned and member workspaces; workspaces shared through the access list are listed in `/v1/shared-with-me`. Context and layout changes need the owner or an editor; metadata changes (name, members, saved views, investigation, `private`) need the owner, or an editor whose token holds agent scope; deleting needs the owner or an admin. A missing role is `403 WORKSPACE_FORBIDDEN`. Every workspace operation needs a token. A chronicle's workspace (§11) also gives the chronicle's editors and owners the editor role and its viewers and commenters the viewer role.

Rules:
- **Updates** are JSON merge patches (`application/merge-patch+json`) to `PATCH /v1/workspaces/{id}/context` with `If-Match: <contextVersion>` (`42`, `"42"` or `W/"42"`) and, optionally, `Foresignal-Client-Id: <clientId>`. A missing `If-Match` is `400`; a stale version gets `412 WORKSPACE_CONTEXT_STALE` with the current `contextVersion`, and the client re-reads, re-applies its intent and retries. The response is the new state, and its ETag is the new version. `GET /v1/workspaces/{id}` carries the version as its ETag too.
- **Validation** (schema version 1): unknown members are rejected; references must be well formed (`OBJECT_REF_INVALID`); `timeRange.start` is required and `end` (null for "until now") must not precede it; `viewport` is either `bbox` (`[minLon, minLat, maxLon, maxLat]`) or `center` + `radiusKm` (≤ 20,000), and camera details belong in `layout`; `playback.state` is `playing` or `paused`; the active ids are ULIDs; `filters` hold strings, numbers, booleans or arrays of them (≤ 4 KB); ≤ 200 `layers` and ≤ 200 `selectedRefs`; the whole context ≤ 16 KB. Null members are dropped when stored.
- **Sync:** clients subscribe to the `workspace` topic on `/v1/events/stream` with `workspace.workspaceId`, `workspace.clientId` (generated when absent) and `workspace.clientKind`. Each `workspace.context` event carries `{workspaceId, version, patch, originClientId, principal, at}`; every follower receives it, the sender included, so a client ignores its own `originClientId`. A client applies patches in version order; a gap in versions (or a reconnect: workspace events are id-less and are not replayed) → re-read the workspace.
- **Presence:** `workspace.presence` events list the connected clients (`clientId`, `clientKind` ∈ globe | map | analyst | satellite | rf | mobile | agent | other, `principal`, `connectedAt`) with the `change` (`joined` or `left` and the client id). Foresignal compares the followers of each workspace every two seconds and sends one event per change.
- **Layers and filters** are strings and simple values the clients agree on; Foresignal validates shape and size only. Common layer ids are the source ids, kinds and `observability:{family}`.
- Agents may set `focusRef` (MCP `set_workspace_focus`, agent scope, owner or editor role) to show the operator what they mean on every open client; it also adds the object to `selectedRefs`, and the event's `originClientId` is `mcp:{session}`.

## 6. Deep links

A context travels between applications as a deep link payload: `{"v":1,"ws":"01JAB…","ctx":{…context…}}`, JSON, base64url-encoded without padding, ≤ 2 KB (`413` beyond), with null members left out. Applications accept it in a URL fragment, `#fs=<payload>`, on their own host. `GET /v1/workspaces/{id}/context-link` and `POST /v1/context-links {context, workspaceId?}` produce validated payloads (`version`, `workspaceId`, `payload`, `fragment`, `bytes`). `POST /v1/workspaces/{id}/share` (agent; owner or editor) issues a read-only 112 share link (`/v1/k/{key}`, target type `workspace`, `expiresAt` up to 30 days, `maxUses`) that shows the workspace's name, context version, context and deep link, never its layout, owner or members; a private workspace is never shared. A share link is public, so the workspace's TLP label must be `CLEAR` (`403 TLP_VIOLATION` otherwise; set the label with `PUT /v1/objects/workspace:{id}/acl`).

## 7. Time model for clients

- **Event and observed time** describe the world; **transaction time** (`seq`) describes what Foresignal knew. `asOf` replays transaction time: "what did we know then".
- `timeRange` selects the window to show; `asOf` (optional) pins the knowledge cutoff; `playback` animates within `timeRange`.
- Live updates come over SSE with resume and resync (015); clients never poll for changes they can stream.

## 8. Using the contract well

- Fetch envelopes in batches (`resolve`, ≤ 500 refs; missing and invisible refs come back in `missing`, not as errors) and cache by `ref` with ETags.
- Show provenance (sources, manifest) and caveats wherever you show a finding, a probability or a relation; show ICD 203 terms exactly as returned.
- Show coverage (`observability` layers) next to any absence-based view.
- Validation is how analysts teach the system; make it one click on any finding, event or situation. Notes need any token (on a work product, the commenter role); verdicts (`confirmed`, `rejected`, `needs-review`, `duplicate-of` with `duplicateOfRef`) need agent scope. There is no `tag` annotation kind: labels are 130 tags, and a note may carry free tags. The author or an admin edits (a merge patch) or deletes an annotation; changing a verdict still needs agent scope. Annotations of a private object are private. Notes are comment threads (§10).
- Verdicts feed `GET /v1/analytics/precision?component&since`: per finding detector, intel-event recipe, situation linker and precursor (precursor-active findings), the share confirmed among the latest verdict per object, with a 95% Wilson interval; `needs-review` counts as unlabeled and `duplicate-of` as labeled but not confirmed. A verdict on an object with a time window proposes a historical 123 benchmark case that an operator accepts; nothing is added automatically. A `rejected` verdict dismisses a finding or intel event only when its agent-scope author sends `applyStatus: true`.
- Sensor discovery (`GET /v1/sensors?bbox|placeId&from&to&kinds&limit`, window ≤ 7 days) lists `sensor` envelopes: enabled sources whose 116 detection probability for the area is above 0.2 (with `pDetect`, method and families), local receivers whose last reported position is within 400 km, SatDump stations within 2,500 km, curated satellites (`intel/satellite-capabilities.json`, operator-extensible) with predicted passes above 10° (`passes`, `capabilities`), STAC scenes in the window (`footprint`, `acquiredAt`, thumbnail link) and raster products over the area. Foresignal cannot task public satellites or third-party sensors.
- Offline and edge clients use offline packs (111) and federation (113); the context and references are the same.

## 9. Recommended frontend architecture (informative)

- Keep Foresignal headless; build each frontend (globe, analyst workstation, satellite, RF/sensors, intelligence, operations) as an independently runnable module on a shared client SDK generated from `docs/OpenAPI.json`.
- Share state only through Foresignal: workspaces and their synchronized context (§5), deep links (§6), investigations, chronicles and reports. A unifying shell, when it comes, coordinates layout and navigation, not data.
- Develop against the complete target spec from day one: generate types and clients from `docs/OpenAPI.json`, run a mock server from it, and read `x-foresignal-status` (`planned` or `implemented`) and `x-foresignal-prompt` to know when each operation goes live.
- The SDKs (141) expose `ObjectRef` (parse and format with the encoding rules of §2), the envelope, and a `WorkspaceSync` helper that subscribes to the `workspace` topic, applies remote patches in version order, sends local patches with `If-Match` and a client id, re-reads on a gap or a `412`, and ignores its own `originClientId`.

## 10. Collaboration (prompt 130)

- **Who:** every request runs as a principal (person, agent or service) through its token; there are no passwords. `GET /v1/me` (MCP `whoami`) returns the principal, the token, its scopes, groups, notification preferences and the instance's default TLP label. `GET /v1/principals` lists handles, display names, kinds and avatars; admins create principals and issue their tokens (`POST /v1/principals/{id}/tokens`). A disabled principal's tokens stop working.
- **Groups** (`/v1/groups`) are named sets of principals used as grantees; the creator is a manager, and managers or admins change them.
- **Data vs work products:** records and analytic objects stay readable per scope. Work products (investigations, workspaces, chronicles, reports, saved searches, notes, attachments, evidence items, shared tags) have an owner, a `visibility` (`private`: owner only; `shared`: owner and access list; `instance`: also every principal as a viewer) and an access list of grants (`principal:{id}`, `group:{id}` or `instance`, as `viewer`, `commenter`, `editor` or `owner`, optionally expiring). Viewers read, commenters also comment, editors also edit and share, owners also delete and transfer ownership. Without access a work product is invisible (`404`), everywhere: envelopes, listings, search, saved searches, SSE and exports. Admins see everything.
- **Access lists:** `GET/PUT /v1/objects/{ref}/acl` (owner or editor) and `POST /v1/objects/{ref}/share {grantees, role, message, expiresAt}` (notifies the grantees). Every change is audited. New investigations are `instance`-visible (as before prompt 130); new chronicles, saved searches, attachments and evidence items are `private`; personal tags are visible only to their owner and shared tags default to `instance`.
- **TLP:** every work product carries `tlp` (`CLEAR`, `GREEN`, `AMBER`, `AMBER+STRICT`, `RED`; default `AMBER`). The label bounds who may receive it: `RED` only principals named in the access list; `AMBER+STRICT` named principals, and groups or the instance only when everyone reached belongs to the owner's organization; `AMBER` likewise here, plus named recipients' organizations on other instances; `GREEN` the instance and federated peers but no public link; `CLEAR` anyone, public share links included. The API refuses anything else with `403 TLP_VIOLATION` and the rule; never offer a target the label forbids. Derived products inherit the strictest label of their inputs (an evidence item with a `RED` attachment is `RED`; an investigation that receives a `RED` item becomes `RED`). Show the label prominently on screen, in exports (investigation exports print it at the top) and in every rendition.
- **Tags** (`/v1/tags`): personal or shared, assigned to up to 500 references at once (`POST /v1/tags/{id}/assign` and `/unassign`; references the caller cannot see are skipped and listed); `GET /v1/tags/{id}/objects` and `GET /v1/objects/{ref}/tags`. Envelopes carry the caller's visible tag names.
- **Comments:** notes are thread roots; `POST /v1/annotations/{id}/replies`, `POST /v1/annotations/{id}/resolve {resolved}` and `GET /v1/objects/{ref}/annotations?thread=true` (`AnnotationThread`s). `@handle` mentions notify only principals who can see the target; the others come back to the author in `droppedMentions`. A reply notifies the thread's earlier participants. A note on a work product carries its label.
- **Attachments** (`POST /v1/attachments`, multipart, ≤ 50 MB by default): PNG and JPEG (checked by ImageGuard, EXIF stripped), PDF, and UTF-8 plain text, CSV, JSON, GeoJSON, KML and Markdown, decided from the bytes; anything else is `415`. Files are stored content-addressed, their text extracted for search, and served only by `GET /v1/attachments/{id}/content` as an attachment (inline only for images) with `X-Content-Type-Options: nosniff`. Never render one as active content.
- **Evidence items** (`/v1/evidence-items`): title, description, attachments, a link (stored, never fetched), the Admiralty grade (source reliability `A`–`F`, information credibility `1`–`6`, shown as `grade`, e.g. `B2`), geometry, event time and tags. They are investigation items, ACH evidence (`source: evidence-item`) and relation endpoints; their originator for 116 independence is `operator:{handle}` unless they cite a cataloged source. Items labeled `CLEAR` or `GREEN` are embedded for semantic search.
- **Saved searches** (`/v1/saved-searches`): text, types, structured filters, area, time window and tags, run on demand (`/results`, envelopes the caller can see); with `notify`, new matches reach the owner at most hourly.
- **Shared with me** and **my items** are single lists (`/v1/shared-with-me`, `/v1/my`, MCP `list_shared_with_me`), so a workbench's navigation (my investigations, shared with me, recent) needs no client bookkeeping. `GET /v1/investigations?scope=mine|shared|all&status` lists investigations, and display ids (`INV-yyyy-nnnn`) work in every investigation path.
- **Notifications:** `GET /v1/inbox` is the caller's personal inbox (mentions, shares, replies, saved-search matches), and the `me` topic on `/v1/events/stream` streams the same items (`share.received`, `mention`, `comment.reply`, `saved-search.matches`), resumable by id. `PUT /v1/me/notification-preferences` routes personal events to channels the principal owns (its own ntfy topic, email address, Matrix room, webhook…) with quiet hours; those channels never receive the instance's alert fan-out.

## 11. Chronicles (prompt 131)

A chronicle is a saved, shareable monitoring thread: a bundle of **layers** over an optional **area** that, while it is active, copies what its layers match into the chronicle's own storage. Its history is independent of global retention: months after the source rows are gone, it still replays exactly as it was recorded. Chronicles are work products (§10): `private` by default, owned by their creator, shared through the access list and labeled with TLP; `chronicle:{id}` refs (also by display id, `CHR-yyyy-nnnn`) resolve to envelopes and appear in `/v1/shared-with-me`.

- **Layers.** Each layer has a `selector` (`records` with sources, kinds and 017 conditions; `saved-view`; `watchlist`; `entities`; `situations`, membership-driven; `findings` by type and family; `series` refs; `raster-products`; `alerts` by rule id or tag), an optional `areaOverride` (else the chronicle's area applies to records, saved views, watchlists, findings and rasters) and a `capture` mode: `full`; `downsampled` (one version per entity per `interval`, default `PT60S`, plus every event version); `events-only` (FIRST, SIGNIFICANT, REAPPEAR, TOMBSTONE, TRIGGER_EDGE); `latest-per-interval`; `rollup-only` (counts per region and hour, no records); `frames` (the first raster frame and every `everyNthFrame`-th after it, by reference). Layer edits apply from the edit's seq onward (`addedAtSeq`); a removed layer keeps its captured rows unless `purge=true`.
- **Create** one from the current workspace (`fromWorkspaceId`: source and kind layers become `records` layers, `view:{id}` layers saved-view layers, the viewport the area, the active situation a `situations` layer) or accept one the analysis cycles proposed (its owner finds it in the inbox as `chronicle.proposed`). `POST /v1/chronicles/suggest {situationId | area, topic}` returns a draft with its storage estimate without creating anything.
- **Storage.** Show the projected storage (`/estimate`: rows and bytes per day per layer, an upper bound from the last day of global history) before activation, and `bytesUsed` against `sizeCapBytes` (default 20 GB) while it runs. `capState` is `warning` at 90% (the owner is notified) and `exceeded` at the cap, when full and downsampled layers capture events only (`effectiveCapture` on the layer, a `cap-fallback` thread item). Raising the cap restores them.
- **Lifecycle:** `proposed` → `active` ⇄ `paused` → `stopped` → `archived` (a verified cold package; `restore` brings it back as `stopped`); anything else is `409 CHRONICLE_STATE_INVALID`. Activation may `backfill` from a time: history still retained globally is copied and marked `backfilled`, with caveats where it was already thinned or gone. A paused interval is not captured. Deleting needs the owner and `confirm` equal to the display id. A chronicle's own `retention` (`forever` by default, or a duration after stop) is the only thing besides deletion that removes its captured history.
- **Thread** (`GET /v1/chronicles/{id}/thread?after&types&cursor`): a chronological feed of alerts, findings, situation changes, hypothesis band changes, forecast resolutions, first appearances, layer edits, capture gaps, cap warnings and fallbacks, status changes and backfills, each with the snapshot kept when it happened and the object's current envelope when it still exists. Unread counts are per principal: move the read cursor with `POST /v1/chronicles/{id}/thread/read {upToId}`. Live: SSE topic `chronicle` on `/v1/events/stream` (`chronicle.item`, `chronicle.status`; cursor key `k`; filter `chronicle.chronicleId`), only for chronicles the stream's principal can see.
- **Replay** from chronicle storage only, so replay never depends on global retention: `/state?at` (per layer, the latest captured version of each entity observed **and** known by T and still valid, paged by layer; rollup layers give their counts), `/tiles/{z}/{x}/{y}.mvt?at&layers` (016 tiles of that state), `/tracks/{sourceId}/{externalId}` (037 track shape), `/series?ref` (captured series points and rollup buckets), `/frames?from&to&step&layers` (counts and changed entities per playback step), and `/summary` (layer counts over time, top entities, linked situations, hypothesis sets and questions, storage, capture gaps). A capture gap (global retention released the clamp before capture reached some versions) is a thread item and a `CAPTURE_GAP` caveat on the state of that interval; show it, never hide it.
- **Workspace.** Each chronicle owns a 129 workspace whose context holds its layer ids and span; store the client's auto-arranged layout there and open the chronicle with that context. Deep links (§6) can carry `chronicle:{id}` in `selectedRefs` or `focusRef`.
- **Export** (`POST /v1/chronicles/{id}/export {format: geoparquet | gpkg | json-bundle, layers, from, to}`) is a 111 export job; private sources the caller may not see and `LOCAL_ONLY` sources are left out, and the TLP label is printed in the file and sent as `Foresignal-TLP`.
- Offer pause, stop, archive, restore and delete only when the caller's role allows them (editor; owner for delete), and only from the states that allow them.

## 12. Reports and exchange (prompt 132)

A report is a work product (§10): owned by its creator, `private` by default, with its other authors as editors, an access list and a TLP label that is never weaker than the strictest label of what it cites or the object it was built from (a request to label it lower is `403 TLP_VIOLATION`). `report:{id}` refs (also by display id, `RPT-yyyy-nnnn`) resolve to envelopes and appear in `/v1/shared-with-me`.

- **Draft** from the current selection, a tag, an investigation, a chronicle, a situation or a workspace (`POST /v1/reports/draft {source, template}`; templates `situation-brief`, `investigation-summary`, `chronicle-recap`, `weekly-assessment` with `assessmentId`, `custom`). The draft is deterministic and needs no LLM: key judgments from the linked hypothesis sets' leading hypotheses and the linked questions' ensembles, a summary, a timeline, the cited objects, maps, hypothesis and forecast snapshots, graded evidence items and coverage caveats. With `narrative=true` (agent scope, a configured chat provider and budget; `409 LLM_NOT_CONFIGURED` otherwise) a narrative block labeled `textOrigin: llm` is added, whose citations were checked against the cited objects and tool results; edit or delete it like any block. Blank reports start with `POST /v1/reports`.
- **Blocks** are ordered and typed: `markdown` (may link objects as `[[ref]]`), `bluf` (key judgments; a judgment with a probability must reference a ledger question or a hypothesis set, and its ICD 203 term is derived, never typed), `objects`, `map` (layers, area or bbox, time range), `timeline`, `hypotheses`, `forecasts`, `evidence` (Admiralty grades), `coverage` (caveats), `attachments` and `sources` (regenerated at publication from the cited sources' attribution and licenses). Every block has an `id`; comment on the report with an annotation on `report:{id}`, or on one block with an annotation on `report:{id}#{blockId}` (percent-encode `#` as `%23` in a path).
- **Edit** with a JSON merge patch (`PATCH /v1/reports/{id}`, editors) and block operations (`POST /v1/reports/{id}/blocks?after`, `PATCH|DELETE /v1/reports/{id}/blocks/{blockId}`). `version` is the draft's optimistic lock (body `version` or `If-Match` with the ETag); a stale one is `409 REPORT_VERSION_CONFLICT`, as is any edit while the report is not `draft` or `review`.
- **Publish** (`POST /v1/reports/{id}/publish`, editors) freezes an immutable revision as the publisher sees it: every reference is pinned (`@{seq}` for records), envelopes and snapshots are stored, maps are rendered and stored as attachments (or carry a `caveat`), and Markdown, HTML and the JSON bundle are written once. Renditions (`GET /v1/reports/{id}/rendition?format=md|html|json&revision`; `draft=true` previews the draft) carry the TLP banner at the top and bottom of every page and, when any block is machine-generated, the footer "Automated analysis inputs; verify before acting". To change a published report, set `status` back to `draft`, edit, and publish the next revision; earlier revisions never change (`GET /v1/reports/{id}/revisions`). `retract` (owner) marks a revision withdrawn and notifies everyone it was sent to (`report.retracted`).
- **Send here** (`POST /v1/reports/{id}/send {principals, groups, channels, message}`, owner or editor, agent scope): each grantee gets the viewer role and a `report.received` notice through their own preferences; `channels` must be recipients' own personal channels. TLP is checked for every recipient; one refusal refuses the send.
- **Read-only links** (`POST /v1/reports/{id}/share-link`, owner): a 112 key serving the latest published HTML at `/v1/k/{key}/report`, only for `CLEAR` reports (or `GREEN` when the instance allows it). A retracted or relabeled report stops being served.
- **Send to other instances** as a signed intelligence package (`POST /v1/exchange/packages {objectRef, recipients, deliver}`; a published report, an investigation, an evidence item, a chronicle extract or received material being forwarded). `recipients` names principal handles per instance, or `*` for GREEN and CLEAR. The package's manifest is signed with the instance key (109), private and `LOCAL_ONLY` material is reduced to counts (`reductions`), and it is delivered to the instances' 113 peers or downloaded (`GET /v1/exchange/packages/{id}/download`) and handed over. A received package never goes back to an instance it came from (`409 PACKAGE_LOOP`); a forward keeps the original authors and adds the forwarder.
- **Received material** (`GET /v1/exchange/received`, `/{id}`, `/{id}/rendition`) is read-only and visible only to its named recipients (`RED`: to them alone), or to everyone for `*`. It appears under `remote:{instanceId}/{type}:{id}` references with the sender's envelope snapshots, its renditions are rendered locally and marked with their origin, and it never enters local evidence, ledger or analytic tables; copying an item into a local investigation (an `object` item with the `remote:` ref) is a deliberate, logged act that keeps its origin and label. Show the sender, authors, signature status and TLP; acknowledge with `POST /v1/exchange/received/{id}/acknowledge`. Recipients are notified with `package.received`.
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

- `GET /v1/contract` → `{contractVersion, objectTypes, relationTypes, contextSchemaVersion, deepLinkVersion}` (today `1.3`, the 32 types of §2 owned by prompts up to 132, the 15 relation types of §4, `1`, `1`).
- Within `/v1`, changes are additive (new types, relation types, optional context fields). Removing or renaming anything requires a new major contract version and a deprecation period announced through `Deprecation` headers.
