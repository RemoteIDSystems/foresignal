# Base-rate fixtures (prompt 121)

- `ucdp-ged-sample.csv`: **synthetic** rows with the exact column header of the UCDP GED 26.1 CSV
  (`GEDEvent_v26_1.csv` inside `https://ucdp.uu.se/downloads/ged/ged261-csv.zip`, header read on 2026-10-17). The
  first row's values follow the real file's first event; the others are made up to exercise the mapping (violence
  types 1–4, a former country name in parentheses, an unknown country). Recapture from a real download if the header
  changes.
- `usgs-2023.csv`, `usgs-2024-h1.csv`, `usgs-2024-h2.csv`: **synthetic** rows in the column layout of the USGS FDSN
  event service CSV (`format=csv`, header verified with a live query on 2026-10-17).
- `gdacs-search-eq-2023.json`: the **real** first page of
  `https://www.gdacs.org/gdacsapi/api/Events/geteventlist/search?eventlist=EQ&alertlevel=orange;red&fromDate=2023-01-01&toDate=2023-12-31&pageSize=100&pageNumber=1`,
  fetched on 2026-10-17 (GDACS, open with attribution).
- `acled-page.json`: **synthetic** page in the shape of the ACLED `/api/acled/read` response (`success`, `data`) with
  the fields the history import asks for. Recapture with a real key.
