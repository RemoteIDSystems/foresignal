# Foresignal — Cross-cutting build conventions

Every build prompt follows these conventions. When a prompt's text disagrees with this file, this file wins, unless the prompt explicitly says it is overriding a convention.

**Sources of truth** (one owner per concern):

| Concern | Authoritative document |
|---|---|
| Cross-cutting build rules | this file |
| Invariants and the tests that assert them | [Invariants](Invariants.md) |
| Ingest, history, live state, triggers, SSE, asOf, retention | [Core Data Model](Core-Data-Model.md) |
| Weather, imagery, SatDump | [Weather & Imagery Design](Weather-Imagery-Design.md) |
| Intelligence layer (098–139) | [Intelligence Design](Intelligence-Design.md) |
| Client-facing object contract, relations, workspaces, deep links | [Client Contract](Client-Contract.md) |
| Overall architecture, scope, sources, order | [Architecture](Architecture.md), [Product Charter](Product-Charter.md), [Source Catalog](Source-Catalog.md), [Roadmap](Roadmap.md) |
| API surface | [OpenAPI](OpenAPI.json), kept in step by each prompt |

**Name:** the product was renamed from Elaro to **Foresignal** on 2026-10-11 (domain `foresignal.ai`). Completion Reports 001–005 use the old identifiers; the translation table is in `docs/Roadmap.md` §5.

## Shared components (created by whichever prompt needs them first, then extended)

| Component | Convention |
|---|---|
| **`EgressPolicy`** (`ai.foresignal.security.EgressPolicy`) | **One** component decides whether a record may leave Foresignal on a given path: API reads, tiles, SSE, search/embeddings, LLM context, MCP, exports, sinks, federation, share links. Inputs: the source's `private` flag (039), descriptor `egress` `OPEN \| LOCAL_ONLY` (062), and licence policy (109). Prompts 026 (LLM context), 039, 062 and 109 each **extend** this single class. Never create a parallel policy class such as `LlmEgressPolicy` or `PrivateEgressPolicy`; if an earlier prompt created one, merge it into `EgressPolicy`. |
| **Key-in-path URLs** | Every key carried in a URL path (feed keys, tile keys, share-link keys) uses **`/v1/k/{key}/…`**. Key prefixes: `fsf_` feed (035), `fsk_` tile (050), `fss_` share (112). One filter (035) resolves them; 050 and 112 reuse it. Other credentials: ingest keys `eik_` (024, header, bearer or basic), device ingest keys `eid_` (039, in the path `/v1/ingest/{sourceId}/k/{key}/{format}`), federation peer credentials `fsp_` (113, bearer). |
| **Pending operations** | `src/test/resources/openapi/pending-operations.txt` (created in 020) lists spec operations not yet implemented, one per line with the prompt that builds them. **Every prompt that implements a listed operation deletes its line.** Prompt 142 requires the file to be empty. |
| **`RetentionState.purgeSeq`** | Advances **only** when retention purges heads (entities disappear without a tombstone). Routine version deletes and partition drops don't advance it. Clients get `410 RESYNC_REQUIRED` only when `after < purgeSeq`. |
| **Startup order** | `ai.foresignal.config.StartupOrder` (009): schema → partitions → writer → trigger engine → embedding/derived workers → scheduler. New background services register their phase there. |
| **Shared parsers** | Format parsers shared by several adapters live under `ai.foresignal.source.formats.*`: readsb JSON, CAP 1.2, AIVDM, GeoJSON, KISS/AX.25. Adapters never import each other. |
| **GeoJSON reading** | Use Foresignal's own Jackson 3 GeoJSON reader (`ai.foresignal.source.formats.geojson.GeoJsonReader`, producing JTS geometries with SRID 4326), **not** `jts-io-common` (it pulls in the unmaintained `json-simple`). Prompt 008 replaces any use introduced by 004 and removes the dependency. |
| **`FetchResult` 304 factory** | `FetchResult.unchanged()` (304 / not modified). A record accessor already uses the name `notModified()`. |
| **SatDump product ids** | `satdump-{station}-…` for every SatDump product: geostationary (049) and polar (054). |
| **Intelligence outputs** | Analytic outputs are typed tables **plus** records of a DERIVED source, so triggers, SSE, tiles, search and MCP work unchanged: `foresignal-intel` (intel events 101, situations 118, hypothesis sets 124), `foresignal-analytics` (`analytic-finding`, 119 onward), `foresignal-outlook` (regional outlooks and indices, 125). Signal families come from one registry (`intel/signal-families.json`, 119). Forecast questions and forecasts are ledger rows (122), not records. |
| **API-first (spec complete up front)** | `docs/OpenAPI.json` is the complete target API for every prompt, authored before implementation so clients can be built in parallel. Every operation carries `x-foresignal-scope`, `x-foresignal-prompt` (the prompt that implements it), optional `x-foresignal-extended-by` (later prompts that add parameters, responses or fields to it; each implements its own additions) and `x-foresignal-status` (`planned` until built, then `implemented`). A prompt **implements the operations and schemas the spec already defines for it**; where a prompt's text says "add to `docs/OpenAPI.json`", read it as "verify and reconcile". If implementation shows the spec must change, the prompt amends the spec in the same commit, records the change under "Deviations" and in the spec's `info.x-foresignal-changelog`, and never removes or renames a published operation without the deprecation path. Enums added in code must appear in the spec. On implementing an operation, set its status to `implemented` and remove it from `pending-operations.txt` (020). Regenerate Postman after any spec change. |
| **Operator choice** | Foresignal is self-hosted software for one operator's own, non-commercial use. Sources whose terms restrict collection, cost money, use unofficial endpoints, scraping or a headless browser ship as **opt-in adapters** (off by default; route, terms, costs and fragility in the descriptor's `access` block and `docs/Source-Catalog.md`), never as exclusions; licence egress flags are defaults the operator can override per source (109); adapters never solve CAPTCHAs or spoof client fingerprints, they report `BLOCKED` and the operator may import a session or set a proxy (137); compliance aids such as deletion sync and retention are settings with safe defaults, not hard blocks. Paid adapters budget in money as well as calls (005) and estimate each call before making it. |

## No credentials during construction

- **Building and testing never needs the owner's keys.** All tests use WireMock and recorded fixtures; no test calls a real upstream.
- **Keyed sources** (e.g. AISStream, FIRMS, OpenSky, UCDP, ACLED, URLhaus, Ontario 511, AirNow, OpenAQ, eBird, GFW, Cloudflare Radar, ReliefWeb/HDX app ids): if no key is available, build the fixture from the provider's published documentation or examples. Mark it `synthetic: true` in the fixture's README, with "recapture with a real key". Never stop, prompt for a key, or use someone else's key.
- **Runtime keys** are entered later through the API (`PUT /v1/sources/{id}/credentials`). Only prompt 143 can optionally use them, through `FORESIGNAL_VERIFY_*` environment variables; it skips those checks when the variables are absent.

## Defaults that override earlier design text

| Item | Value | Why |
|---|---|---|
| Aircraft TTL (polled readsb/adsb.lol areas) | **6 min**, poll 60 s, keyframe 240 s | `keyframe ≤ ttl − poll − 30 s` must hold. 5 min fails at a 60 s poll (from the 004 report). |
| OpenSky TTL | 15 min (300 s poll) | Same constraint |
| Unmatched `/v1/**` paths | `admin` (fail closed) | From the 003 report |
| Notification channel timezone | Falls back to the `server` setting | Single source of truth |

## Analytic retention

Adapters set retention defaults by **analytic class** through `defaultRetention` / `kindRetention` (019's resolver order is unchanged; the operator can always override). An adapter prompt that names a **longer** window keeps it; one that names a shorter window for an `event` or `series` kind is overridden by this table. Rationale and storage estimates: `docs/Intelligence-Design.md` §4.1.

| Class | Kinds (examples) | Default raw retention |
|---|---|---|
| `event` | earthquakes, weather and CAP alerts, conflict events (ACLED, UCDP), outages, NOTAMs and TFRs, sanctions entries and changes, filings, disasters, cyclones, launches, KEV entries, travel advisories, navigational warnings, volcano levels | **1825 d** |
| `bulk-event` | GDELT events, FIRMS pixels, GLM lightning, CT-log hits, PSKReporter spots, Wikipedia edit surges, `attention-top` | 90 d |
| `series` | `series-point`, `series-latest` | **3650 d** |
| `state` | SAMPLED status (gauges, sensors, station health) | 365 d |
| `track` | TRACK tier | `trackWindow` (3 d) plus 044 downsampling; never `defaultRetention` |
| `reference` | catalogues (power plants, ports, cables, airports) | 90 d of history |
| `intel` | records of `foresignal-intel`, `foresignal-analytics`, `foresignal-outlook` | 1825 d (the typed tables behind them are never auto-deleted) |
| `social` | `social-post` (134) | 30 d, per platform (`social.postRetention`); narratives, claims, comparisons and volume series are kept as intel and series |

Deploy a collecting instance early (after 022) with these defaults; see `docs/Roadmap.md` §1.

## Intelligence conventions (prompts 098–139)

- **Authority:** `docs/Intelligence-Design.md` wins over older prompt text on the intelligence layer.
- **Evidence, inference, speculation** are separate sections in every analytic response; speculation is labelled.
- **Detectors find, models explain:** statistical components are pure, versioned (`{name, version, params}` stored with outputs) and reproducible from stored data plus `asOfSeq`. LLM output never creates evidence or changes a deterministic score; LLM forecasts are ledger forecasts under their own forecaster ids.
- **Numbers are the truth:** probabilities are stored as numbers; ICD 203 terms are derived by `EstimativeLanguage` (122) and linted (no term that disagrees with its number, no mixing of the two ICD term rows, never a likelihood term and a confidence level in the same sentence).
- **No look-ahead:** live detection only uses data whose `knownAt` ≤ decision time; replays and evaluations read through `asOf`.
- **Coverage:** absence counts only with healthy coverage; otherwise `unknown`. Thin-reporting regions carry `LOW_REPORTING_DENSITY`.
- **No people, no accusations:** analysis covers places, events, vehicles and objects, aggregate markets and organisations' public actions. Market findings say "unusual positioning before the event", never "insider trading".
- **Licensed data stays local:** `LOCAL_ONLY` sources (FRED, LSEG-sourced EIA prices, ACLED) are computed on but their raw values are served only to callers allowed local egress; derived standardised statistics may be shared.
- **Statistics libraries:** Apache Commons Statistics, Commons Numbers, Commons RNG, Hipparchus (Apache-2.0). No GPL libraries (Smile, Weka, JSAT, jdistlib); no Commons Math 3.x.
- **Evidence independence:** count independent evidence groups (116 `EvidenceService`), never raw records or sources, wherever confidence, corroboration, base rates or hypothesis updates are computed.
- **Detectability:** absence and "no event" conclusions call 116 `ObservabilityService.canSeeAbsence`; otherwise `unknown` / `LOW_DETECTABILITY`.
- **Manifests:** every analytic output from 100 onward stores a 116 `manifest_id`.
- **Benchmarks gate autonomy:** autonomous proposals and LLM ensemble weight follow 123 `BenchmarkGate`.
- **Object references:** APIs that return or accept references to other objects use the [Client Contract](Client-Contract.md) `ObjectRef` grammar from 128 on; earlier endpoints keep their ids and gain resolvers in 128.
- **Principals and work products:** every actor is a principal (023); work products (investigations, workspaces, chronicles, reports, saved searches, notes, attachments, evidence items) carry `owner`, `visibility`, an access list and `tlp` (129). `AccessResolver` decides visibility; unshared objects are invisible, not forbidden.
- **TLP** is part of `EgressPolicy`: shares, links, exports, renditions and exchange packages respect it, and derived products inherit the strictest input label.
- **Chronicles own their copies:** captured data is never deleted by global retention (130).
- **Three memories stay separate:** observations, interpretation and chronicles (Intelligence-Design §3.1); nothing downstream mutates an upstream memory.
- **Chronicle discovery** uses keyed fingerprints only and discloses within visibility, discoverability and TLP (133).
- **Social is reporting, not observation** (134–136): social claims never corroborate physical events or themselves, amplification never adds evidence groups, "unobserved" needs `canSeeAbsence` and attention gaps need social coverage; accounts are analysed as sources, never profiled; the social salt (`authorKey`) is stored with the secret key material.
- **Ratings are imported, never generated:** media lean and factuality come only from declared providers with licence and date (132); no bylines or author names are stored.
- **Durable cursors** added by the block (`evidence.cursor`, `situation.cursor`, `stats.region.cursor`, `precursor.cursor`, `impact.cursor`, `relation.cursor`, `social.cursor`, `chronicle.cursor.{id}`) join 019's consumer clamp, including its release-after-2×-window rule with an alarm.

## Test-run prompts

Feature prompts write tests but don't run them. Tests run in **012, 022, 041, 057, 097, 139, 142**, and also in **143** and **144**, which may change production code or schema ownership after 142.
