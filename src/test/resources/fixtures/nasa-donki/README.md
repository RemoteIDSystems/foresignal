# nasa-donki fixtures

Captured 2026-10-13 from `https://ccmc.gsfc.nasa.gov/DONKI-API/get/{type}?startDate=…&endDate=…` (keyless, no
`ETag`; the old `kauai.ccmc.gsfc.nasa.gov/DONKI/WS/get/*` answered 301 to a CCMC announcement page). The API refuses
a range longer than 60 days (`400`, body `API Error: Date range cannot exceed 60 days. You requested 73 days.`).

| File | Range | Notes |
|---|---|---|
| `FLR.json`, `CME.json`, `CMEAnalysis.json`, `GST.json`, `IPS.json`, `MPC.json`, `RBE.json`, `notifications.json` | 2026-10-06 to 2026-10-13 | untrimmed |
| `SEP.json` | 2026-10-06 to 2026-10-13 | the real empty answer `[]` |
| `HSS.json`, `WSAEnlilSimulations.json` | 2026-08-15 to 2026-10-13 | trimmed to the first 3 items (none in the 7-day window) |

Every notification `messageBody` was `"## "` on 2026-10-13.

Recapture per prompt 143.
