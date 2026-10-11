# Elaro — Cross-cutting build conventions

Every build prompt follows these conventions. When a prompt's text disagrees with this file, this file wins, unless the prompt explicitly says it is overriding a convention.

## Shared components (created by whichever prompt needs them first, then extended)

| Component | Convention |
|---|---|
| **`EgressPolicy`** (`ai.elaro.security.EgressPolicy`) | **One** component decides whether a record may leave Elaro on a given path: API reads, tiles, SSE, search/embeddings, LLM context, MCP, exports, sinks, federation, share links. Inputs: the source's `private` flag (039), descriptor `egress` `OPEN \| LOCAL_ONLY` (062), and licence policy (109). Prompts 026 (LLM context), 039, 062 and 109 each **extend** this single class. Never create a parallel policy class such as `LlmEgressPolicy` or `PrivateEgressPolicy`; if an earlier prompt created one, merge it into `EgressPolicy`. |
| **Key-in-path URLs** | Every key carried in a URL path (feed keys, tile keys, share-link keys) uses **`/v1/k/{key}/…`**. Key prefixes: `elf_` feed/tile, `els_` share. One filter (035) resolves them; 050 and 112 reuse it. |
| **Pending operations** | `src/test/resources/openapi/pending-operations.txt` (created in 020) lists spec operations not yet implemented, one per line with the prompt that builds them. **Every prompt that implements a listed operation deletes its line.** Prompt 118 requires the file to be empty. |
| **`RetentionState.purgeSeq`** | Advances **only** when retention purges heads (entities disappear without a tombstone). Routine version deletes and partition drops don't advance it. Clients get `410 RESYNC_REQUIRED` only when `after < purgeSeq`. |
| **Startup order** | `ai.elaro.config.StartupOrder` (009): schema → partitions → writer → trigger engine → embedding/derived workers → scheduler. New background services register their phase there. |
| **Shared parsers** | Format parsers shared by several adapters live under `ai.elaro.source.formats.*`: readsb JSON, CAP 1.2, AIVDM, GeoJSON, KISS/AX.25. Adapters never import each other. |
| **GeoJSON reading** | Use Elaro's own Jackson 3 GeoJSON reader (`ai.elaro.source.formats.geojson.GeoJsonReader`, producing JTS geometries with SRID 4326), **not** `jts-io-common` (it pulls in the unmaintained `json-simple`). Prompt 008 replaces any use introduced by 004 and removes the dependency. |
| **`FetchResult` 304 factory** | `FetchResult.unchanged()` (304 / not modified). A record accessor already uses the name `notModified()`. |
| **SatDump product ids** | `satdump-{station}-…` for every SatDump product: geostationary (049) and polar (054). |
| **Spec drift** | Each prompt that adds or changes code-visible API types updates `docs/OpenAPI.json` for **those** types, with `x-elaro-scope` on every operation, then regenerates Postman. Enums added in code (e.g. `Mode.DERIVED`, `ParamType.LAN_URL`/`INBOX_PATH`) must appear in the spec. 013 catches up the source-related gaps from 004. |

## Defaults that override earlier design text

| Item | Value | Why |
|---|---|---|
| Aircraft TTL (polled readsb/adsb.lol areas) | **6 min**, poll 60 s, keyframe 240 s | `keyframe ≤ ttl − poll − 30 s` must hold. 5 min fails at a 60 s poll (from the 004 report). |
| OpenSky TTL | 15 min (300 s poll) | Same constraint |
| Unmatched `/v1/**` paths | `admin` (fail closed) | From the 003 report |
| Notification channel timezone | Falls back to the `server` setting | Single source of truth |

## Test-run prompts

Feature prompts write tests but don't run them. Tests run in **012, 022, 041, 057, 097, 118**, and also in **119** and **120**, which may change production code or schema ownership after 118.
