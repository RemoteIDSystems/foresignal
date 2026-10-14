# `acled` fixtures

**`synthetic: true` — recapture with your ACLED account (all secrets stripped).** No myACLED account was available
during construction.

| File | What it is |
|---|---|
| `token.json`, `token-refreshed.json` | `/oauth/token` responses in the shape of the getting-started guide (`token_type`, `expires_in` 86400, `access_token`, `refresh_token`), with placeholder tokens |
| `events.json` | An `/api/acled/read?_format=json` page in the envelope of the ACLED endpoint guide (Table 7: `status, success, last_update, count, messages, data, filename, data_query_restrictions`); every row value is a string, as ACLED sends them. Rows: `geo_precision` 1, 2 and 3, a note longer than 2 KB, and a row with no coordinates |
| `events-empty.json`, `deleted-empty.json` | Empty pages |
| `deleted.json` | An `/api/deleted/read?_format=json` page (`event_id_cnty`, `deleted_timestamp`): two Ukrainian ids and one Syrian id |
| `forbidden-403.json` | The real body of an unauthenticated `GET https://acleddata.com/api/acled/read?_format=json&limit=1`, captured 2026-10-13: HTTP 403 `{"message":"Access denied"}` |

Field and filter names were checked against the ACLED endpoint and deleted endpoint guides on 2026-10-13: the deleted
endpoint filters on `deleted_timestamp` (not `timestamp`).
