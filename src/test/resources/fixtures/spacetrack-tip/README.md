# `spacetrack-tip` fixtures

`synthetic: true` — **recapture with a real key** (a Space-Track account).

| File | What it is |
|---|---|
| `tip.json` | Hand-made from the Space-Track `class/tip` documentation: two messages for one object and one for another, with string values as Space-Track sends them. `NORAD_CAT_ID`, `MSG_EPOCH`, `DECAY_EPOCH` and `WINDOW` are confirmed fields; `LAT`, `LON`, `INCL`, `DIRECTION`, `REV`, `HIGH_INTEREST` and `NEXT_REPORT` are unverified and must be checked against the capture |

**Capture procedure (prompt 143, with `FORESIGNAL_VERIFY_*` credentials):** log in with
`POST https://www.space-track.org/ajaxauth/login` (form `identity`, `password`), then
`GET https://www.space-track.org/basicspacedata/query/class/tip/INSERT_EPOCH/%3Enow-30/format/json` with the
session cookie, once (TIP allows one query an hour).
