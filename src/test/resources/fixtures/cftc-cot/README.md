# `cftc-cot` fixtures

Real captures, 2026-10-16, with `curl -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai; install=fixture)"` from
the CFTC Public Reporting Environment (Socrata):

- `disaggregated.json` — `GET https://publicreporting.cftc.gov/resource/72hh-3qpy.json` with
  `$where=report_date_as_yyyy_mm_dd > '2026-09-28' AND cftc_contract_market_code in (…12 default codes…)`,
  `$order=report_date_as_yyyy_mm_dd`, `$limit=1000`; trimmed to WTI (`067651`) and gold (`088691`), the reports of
  2026-10-06 and 2026-10-13. All columns kept.
- `legacy.json` — the same query on `6dca-aqww`, trimmed to WTI (`067651`) and E-mini S&P 500 (`13874A`).
- `tff.json` — the same query on `gpe5-46if`, trimmed to E-mini S&P 500 (`13874A`) and the 10-year T-note (`043602`).

Every default contract code in the prompt was present under its expected name in the 2026-10-13 report:
`067651` WTI-PHYSICAL (NYMEX), `023651` NAT GAS NYME, `111659` GASOLINE RBOB, `022651` NY HARBOR ULSD, `088691` GOLD
(COMEX), `085692` COPPER- #1, `001602` WHEAT-SRW, `002602` CORN, `13874A` E-MINI S&P 500, `043602` UST 10Y NOTE,
`098662` USD INDEX (ICE Futures U.S.), `099741` EURO FX. The commodity codes appear in the legacy and disaggregated
reports, the financial codes in the legacy and TFF reports. The futures-and-options datasets were confirmed as legacy
`jun7-fc8e`, disaggregated `kh3c-gbw2` and TFF `yw9f-hn96` (`futonly_or_combined = Combined`).

License: US government work, public domain.
