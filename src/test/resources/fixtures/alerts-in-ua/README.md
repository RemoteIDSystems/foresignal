# `alerts-in-ua` fixtures

**`synthetic: true` — recapture with a real key.** No alerts.in.ua token was available during construction (the API
answers `401 {"message":"API token required. Please contact api@alerts.in.ua for details."}` without one).

| File | What it is |
|---|---|
| `active.json` | A `/v1/alerts/active.json` document built from the developer docs (`https://devs.alerts.in.ua/`, the `Alert` model and the response example, read 2026-10-13), with real location UIDs from the alerts.in.ua location list |

Alerts: `10` Luhansk oblast (the docs example, in force since 2022); `1001` Kyiv city (`31`) air raid with three threats
(two `drones`, one `ballistic_missiles`, each with a `source_message` that must not be stored); `1002` Kharkiv oblast
(`22`); `1003` an `artillery_shelling` in Volodymyrskyi raion (`38`) with a note longer than 1 KB; `1004` an air raid in
the Volodymyr hromada (`255`, approximated by its raion); `1005` `urban_fights` with an unknown UID (`99999`) and a note
holding markup and a link (stored as untrusted text); `1006` a Sumy oblast alert with `finished_at` set (a tombstone at
once).
