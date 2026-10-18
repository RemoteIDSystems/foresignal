# FDSN / QuakeML test resources (prompt 141)

- `QuakeML-BED-1.2.xsd`: the official QuakeML 1.2 Basic Event Description schema, downloaded unmodified on
  2026-10-18 from
  `https://quake.ethz.ch/quakeml/docs/xml?action=AttachFile&do=get&target=QuakeML-BED-1.2.xsd`
  (SHA-256 `98d33f02998a82681ff55847d1e90471998dab79cdff28d65a582d3dfb81de41`).
  `FdsnEventIT` validates the `eventParameters` element of `GET /fdsnws/event/1/query` against it. The BED schema
  declares `eventParameters` as its root element; the `q:quakeml` wrapper belongs to the separate QuakeML root schema
  and is checked by the test's namespace assertions instead.
