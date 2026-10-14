# Fixtures: `faa-nas-status` (prompt 087)

| File | What it is |
|---|---|
| `airport-status-information.xml` | A real capture of `https://nasstatus.faa.gov/api/airport-status-information`: one departure delay (ORD) and four closures in two repeated "Airport Closures" blocks. |
| `all-delay-types.xml` | **Partly synthetic.** One document with every delay type: Ground Delay Programs (`Ground_Delay`), Ground Stop Programs (`Program`), arrival and departure delays, closures (the SNA and SAN entries are real) and Deicing, plus an airport (`ZZZQ`) that is not in the airport table. The element names of the types that were not live at capture are the ones the prompt documents; recapture a busy day at prompt 143. |

## Capture procedure

- **Date:** 2026-10-14 04:32 UTC.
- **Command:**

  ```sh
  curl -sS -A "Foresignal/0.1.0 fixture-capture (+https://foresignal.ai)" -D headers.txt \
    -o airport-status-information.xml https://nasstatus.faa.gov/api/airport-status-information
  ```

- **Response:** HTTP 200, `Content-Type: application/xml` (not JSON), `Cache-Control: no-cache, no-store,
  must-revalidate`, 1,630 bytes. `Update_Time` was `Wed Oct 14 04:32:43 2026 GMT`.
