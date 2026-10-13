# abusech-urlhaus fixtures

`synthetic: true`. No Auth-Key was available during construction (Conventions, "No credentials during
construction"); recapture with a real key.

- `recent.json`: built from the documented response of `GET https://urlhaus-api.abuse.ch/v1/urls/recent/` on
  `https://urlhaus-api.abuse.ch/` (read 2026-10-13). The first two entries are the documentation's example
  (`urlhaus_reference` written as https without escaped slashes); the third is hand-made (an https URL with a port and
  a query, no tags, listed blacklists, documentation host under `example.com`); the last two are edge cases (an empty
  id, an unreadable `date_added`).

Verified 2026-10-13 without a key: `https://urlhaus-api.abuse.ch/v1/urls/recent/limit/3/` answered
`401 {"error": "Unauthorized"}`. No key is kept anywhere in this directory.
