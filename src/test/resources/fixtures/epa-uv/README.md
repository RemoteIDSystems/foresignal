# Fixtures: `epa-uv` (checkpoint 124; adapter from prompt 106)

Captured 2026-10-17 with anonymous HTTPS (User-Agent `Foresignal-build`) from EPA Envirofacts, for
ZIP 78701 (downtown Austin, TX; the ZIP the adapter's own verification note uses).

| File | Source | Bytes | SHA-256 |
|---|---|---|---|
| `getEnvirofactsUVHOURLY-ZIP-78701.json` | `https://data.epa.gov/efservice/getEnvirofactsUVHOURLY/ZIP/78701/JSON` (`Content-Type: application/json`) | 1,205 | `f97050774947bb020412d89884fdd5e8ee62789e17d4990823f28627c12675d5` |

## What it holds (read with the production `EpaUvAdapter.fetch`, `EpaUvAdapterContractTest`)

- A JSON array of 12 rows, `ORDER` 2 … 13, each with `ZIP` "78701", `CITY` "Austin", `STATE` "TX",
  `DATE_TIME` "Oct/17/2026 07 AM" … "Oct/17/2026 06 PM" (local time) and integer `UV_VALUE`.
- UV by hour: 0, 0, 0, 2, 3, 5, 7, 7, 6, 4, 2, 1. Read in America/Chicago (CDT) that is 12:00Z … 23:00Z,
  `maxUv` 7 first at 1 PM = 18:00Z.
