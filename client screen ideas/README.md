# Client screen ideas

Parked design material for the future Foresignal client applications (3D globe, analyst workstation, satellite, RF/sensors, intelligence and operations workspaces). These are **not** part of the broker build: no prompt reads or implements them. They will move to the client projects when those repositories exist.

| File | What it shows | Served by (Foresignal) |
|---|---|---|
| `Foresignal Global Intelligence Dashboard.png` | Global situational-awareness view: layers, live globe, selected-object panel, activity timeline, insights, related entities | records, tiles and SSE; dossier and context summary (128); situations (118); ledger (122) |
| `Foresignal Conflict Intelligence Dashboard.png` | Situation "full analysis" (Russia–Ukraine): conflict layers, AI analysis tabs, timeline, territorial change, metrics, evidence, competing hypotheses, forecasts | situations (118), assessments incl. on-demand (127), findings and series (119), hypotheses (124), ledger history (122), evidence items (129), news coverage (132) |
| `Foresignal Tropical Storm Intelligence Dashboard.png` | Investigation workspace (tropical storm): key findings, filters, map with official track and cone, evidence, hypotheses, forecasts, related intelligence | investigations (101, 129), dossier (128), impact projection (126), ledger (122) |
| `Foresignal Intelligence Dashboard Split-Screen.png` | Analyst workbench (evidence graph, timeline, hypotheses, notes) and satellite operations (constellation, passes, imagery, comparison) | relation graph (128), comments and TLP (129), chronicles (130), reports (131), sensor discovery (128), raster and STAC products (042–046, 096) |

Every panel is meant to be served by Foresignal through `docs/Client-Contract.md`; the client owns presentation only. Satellite tasking and imagery change detection are outside the broker (see `docs/Intelligence-Design.md` §16).
