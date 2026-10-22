# 147-CR-Correct-Stale-Source-Descriptions

| | |
|---|---|
| Prompt | 147-Correct-Stale-Source-Descriptions.md |
| Status | Complete (compile only; no test, `lintOpenApi` or `checkApiCompat` was run, per the prompt) |
| Implementation commit | `13c0a0876e08b70ee264a57964452652c5a94ef0` |
| Date | 2026-10-21 |

## How the stale strings were found

`grep -rn -E "\"[^\"]*([Dd]isabled|[Oo]ff by|opt in|opt-in)[^\"]*\""` over `source/adapters`, plus a search for
claims split across two string literals (`(isabled|Off|off)\s*"\s*$`), for "deliberately", "must be enabled" and
"until you enable", and over `src/main/resources`. Every description string that makes a default claim was
classified by its descriptor (`.credential(CredentialField.required…)`, `ParamSpec.required`, `checkParams`,
`autoEnable`):

| Source | Gate | Seeds | Action |
|---|---|---|---|
| `amtraker` | none (no credential, no param) | enabled | corrected |
| `gpsjam-daily` | two optional params with defaults | enabled | corrected |
| `submarine-cables` | none | enabled | corrected |
| `meshtastic-mqtt` | optional params; `checkParams` → `MeshtasticMqttRules.problems` passes with the default root `msh/US` and 1 subscription (`MeshtasticMqttRules.java:83-108`) | enabled | corrected |
| `bluesky-search` | optional credentials only (`BlueskySearchAdapter.java:340-341`) | enabled | corrected, description and javadoc |
| `wiki-edit-surge` | `autoEnable(false)` | disabled | unchanged |
| `ebird-notable`, `grid-frequency-fingrid`, `ercot-public`, `youtube`, `mastodon`, `threads`, `reddit`, `instagram-hashtags`, `x`, `airnow-hourly` | required credential | disabled | unchanged |
| `local-acars-*`, `local-aprs`, `local-sonde`, `local-remoteid`, `local-ais-*`, `airq-local-*`, the local receiver slots | refuse an empty configuration / LAN params | disabled | unchanged |
| `adsbfi-region-*`, `adsblol-area-*` ("Disabled until …") | required `lat`/`lon` | disabled | unchanged |
| `aifs` products, `sst` ("off by default on the small profile") | profile registration | — | unchanged, verbatim |

`LocalAisAdapter.java:260` ("off by default, and the position is then …") describes a parameter, not the source;
unchanged.

## What changed

The trailing default claim was dropped from each corrected description; nothing else in them changed:

- `AmtrakerAdapter.java:128`: "…polls every 60 s ~~and is disabled by default~~."
- `GpsjamAdapter.java:200`: "…local-only ~~and disabled by default~~."
- `SubmarineCablesAdapter.java:152` and `MeshtasticMqttAdapter.java:217`: "~~Disabled by default.~~"
- `BlueskySearchAdapter.java:334`: "~~Off by default.~~"; the class javadoc (`:46`) now says the source is free and
  starts enabled, and that whether social cues may use the platform is the platform row's separate switch
  (`SocialPlatformService.requireEnabled`, `SocialPlatformService.java:726-733`, used by `SocialCueService.java:438`).

No test pins any edited string (`grep` over `src/test`, `docs/OpenAPI.json` and `src/main/resources` for each phrase
found none).

## What ran

| Check | Result |
|---|---|
| `./gradlew compileJava compileTestJava` | exit 0 |
| `check_us_english.py`, HEAD vs. now, per touched file | Amtraker 1 → 1, Bluesky search 1 → 1, Gpsjam 3 → 3, Meshtastic MQTT 1 → 1, Submarine cables 2 → 2: none added |

## For the owner

- **The words the guard flags in these strings were left as they are:** `licence` in the gpsjam description and
  `stylised` in the submarine-cables description. Descriptions are returned by the API, and the project rule is not
  to correct what crosses a wire without your say. Correct them?
- **Javadoc comments outside the API strings still say "disabled by default"** for sources that now seed enabled
  (`CapFeedAdapterFamily`, `FdsnEventFamily`, `CwfisFamily`, `RoadEventDescriptors`, `CctvDescriptors`/`CctvPacks`,
  `MeteoAlarmFamily`, `WzdxFamily`, `Open511Family`, `NdwFamily`, `amtraker/package-info`, `space/package-info`,
  `hydro/package-info`, `env/package-info`, `uasiren/package-info`, `GpsjamAdapter:42`,
  `SubmarineCablesAdapter:26`, `MeshtasticMqttAdapter:41`, `stac/StacSource` and others). The prompt scoped this to
  API-visible strings plus the Bluesky comment, so they are unchanged.
- **Several test display names say "disabled by default"** (`FireAndCycloneIngestIT:103`, `CyberIngestIT:90`,
  `RoadEventsIngestIT:65`, `KitIngestIT:68`, `CctvFamilyRegistryTest:36`, `OsmInfraAdapterIT:17`). They still hold
  under the test profile, which seeds every row disabled (prompt 146), but no longer describe production.
