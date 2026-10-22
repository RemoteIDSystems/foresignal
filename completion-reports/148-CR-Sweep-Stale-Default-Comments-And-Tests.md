# 148-CR-Sweep-Stale-Default-Comments-And-Tests

| | |
|---|---|
| Prompt | 148-Sweep-Stale-Default-Comments-And-Tests.md |
| Status | Complete (compile only; no test, `lintOpenApi` or `checkApiCompat` was run, per the prompt) |
| Implementation commit | `1effe9867ca54f6de287169a62caad8391de2e9a` |
| Date | 2026-10-21 |

## How the leftovers were found

```
grep -rn -i -E "(disabled|off) by default|ships? disabled|starts? disabled|start(s)? off|stays? disabled|installed disabled|disabled until|all disabled|every one disabled|seeded disabled" src/main/java/ai/foresignal/source
grep -rn -i -E "opt-in|opt in|until (the operator|you|an operator) enables?|sources start disabled|must be enabled|enabled deliberately" src/main/java/ai/foresignal/source
grep -rln -i -E "(disabled|off) by default|ships? disabled|start(s)? disabled|seeded disabled" src/test/java/ai/foresignal/source
grep -n -i -E "disabled by default|starts? disabled|off by default|opt in|opt-in|…" docs/Operator-Guide.md docs/Source-Catalog.md docs/Source-Onboarding.md
```

Each hit was classified by the rule `runsWithoutOperatorInput && autoEnable`. Where a family is mixed, or a member's
gate was not checked one by one, the comment now states the rule ("each starts enabled unless it needs a credential
or a parameter, the seeding rule of prompt 145"), which is true for every member, instead of a per-member claim. A
specific "starts enabled" claim is made only where it was checked:

| Claim | Evidence |
|---|---|
| feed-kit sources: no credential, no parameter | `grep -rn "CredentialField\|ParamSpec.required\|credential\|\.param(\|checkParams" source/kit/feed` finds none |
| WZDx feeds with an `apiKey` wait for their key | `WzdxAdapter.java:77-81` adds `CredentialField.required` when the registry entry has `apiKey` |
| `cctv-ontario511` waits for its key | `CctvPacks.java:99` `CredentialField.required("key", …)` |
| `nexrad-l3`, `amtraker`, `ua-air-raid-siren`, `gpsjam-daily`, `submarine-cables`, `meshtastic-mqtt` free | only optional params, no credential, no `checkParams` refusing defaults (147 report; `NexradL3Adapter.java:380-384`, `uasiren` has none) |
| derived layers enabled unless they need a parameter | `LayerGenerator.layer` adds only optional params (`maxEntities`, `includePrivate`); `derived-cpa` adds a required `aoi` (`CpaGenerator.java:56`) |

## What changed

**Comments** (`src/main/java/ai/foresignal/source/**`): `CapFeedAdapterFamily`, `FdsnEventFamily`, `CwfisFamily`,
`MeteoAlarmFamily`, `NdwFamily`, `Open511Family`, `Open511Server` ("all disabled" removed), `WzdxFamily`,
`RoadEventDescriptors`, `CctvPacks`, `CctvCatalogFamily`, `CctvDescriptors`, `FeedDescriptors`, `FeedKitFamily`,
`LayerGenerator` (class and `layer()`), `env`/`hydro`/`space`/`amtraker`/`uasiren` `package-info`, `GpsjamAdapter`,
`MeshtasticMqttAdapter`, `SubmarineCablesAdapter`, `NexradL3Adapter`, `StacSource` (`@param enabledByDefault`: the
field is recorded only; `grep` finds no reader).

**Also in this domain, fixed in the same pass** (the prompt asks for that):
- `NexradL3Adapter` description: "Opt-in on both profiles." dropped. `nexrad-l3` needs no key and no required
  parameter, so it starts enabled, and the description is returned by `GET /v1/sources`. This is the one API
  description edit beyond the two spellings.
- `EnableFreeSourcesController` javadoc: names the held-off exception.

**Tests** (`src/test/java/ai/foresignal/source/**`). Each one asserts the test-profile state (`enabled` false from
`sources.list()` or `sources.view()`, where the test profile seeds every row disabled, or no row exists), so each was
renamed to say so, with a comment on the production default. The assertions are unchanged.
- `CyberIngestIT.registered`
- `FireAndCycloneIngestIT.registered`
- `RoadEventsIngestIT.registered` and the class javadoc
- `CctvFamilyRegistryTest.registered`, its class javadoc and assertion message
- `KitIngestIT.registered`

`OsmInfraAdapterIT.descriptor`'s name claimed "disabled by default" but it asserts nothing about enablement, so the
claim was dropped from the name.

**Spellings (owner-approved):** the `gpsjam-daily` description says "license", the `submarine-cables` description
"stylized".

**OpenAPI:** `enableFreeSources` and `EnableFreeSourcesResult.skipped` now say a held-off source (`wiki-edit-surge`)
is skipped; a 148 changelog entry (1.1.0, documentation only). Postman regenerated.

**Docs:** the three source docs were re-scanned; every remaining "disabled by default" in them is a keyed,
parameter-gated, small-profile or non-source statement (Source Catalog lines 55, 61, 287, 439, 460, 464, 522-534,
896; Operator Guide 825-826, 5740). No change.

## Left alone (out of scope or still true)

- Keyed, required-parameter and LAN sources (`aisstream`, `ebird`, `fingrid`, `ercot`, `spacetrack`, `ris-live`,
  `mastodon`, `threads`, `reddit`, `instagram`, `x`, `youtube`, the `local-*`/`airq-local`/`adsbfi-region`/
  `adsblol-area` families), `wikistream`, the AIFS/SST/GOES-West small-profile lines, `StacSources` and the stac
  `package-info` (Sentinel-1, AOI-gated), `StacDescriptorsTest` ("Sentinel-1 is off by default", about the
  `enabledByDefault` field), `KitOverlay`, the websub kit, `source/kit/feature/**`, `reddit` deletion sync,
  `LocalRemoteIdAdapter` egress, `SystemCanaryAdapter` and `fred` "opt-in" (other meanings), `AutoEnableDefaultsTest`
  and `SourceServiceIT:970` (accurate).

## What ran

| Check | Result |
|---|---|
| `./gradlew compileJava compileTestJava` | exit 0 |
| `./gradlew generatePostman` | "845 requests across 80 tag folders; quickstart 12 steps; covers all ops: True" |
| `check_us_english.py`, HEAD vs. now, every touched file | none increased; `GpsjamAdapter` 3 → 2. The scanner has no entry for "stylised", so `SubmarineCablesAdapter` stays 2 → 2 (its two are `licence` in the `.licence(...)` builder call and "colour" in a field label) |

## For the owner

- `SubmarineCablesAdapter.java:29` and `GpsjamAdapter.java:41` (comments) still say "stylised" and "licence". The
  prompt approved the two descriptions only. `SubmarineCablesAdapter.java:173`'s field label "The map colour"
  crosses the wire (a `fieldSpec` description). Correct these?
