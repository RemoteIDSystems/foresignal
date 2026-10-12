# EDP 1.0 conformance fixtures

Labeled examples for provider developers and for Foresignal's own tests (`EdpSchemaExportTest`,
`EdpValidationTest`). They follow docs/External-Data-Protocol.md and the JSON Schemas in
docs/protocol/edp-1.0/.

- `manifests/valid/` must register: the §2 example (`hormuz-example.json`), a push-only source with a
  snapshot layer stream (`push-layer.json`) and a pull-only atomic source with header auth
  (`pull-atomic.json`).
- `manifests/invalid/` must be refused, each for the reason its name gives: protocol minor too high
  (`PROTOCOL_UNSUPPORTED`), no delivery mechanism, duplicate stream ids, a layer stream with
  observation items, a remote `$ref` in a payload schema, and a `pattern` over 256 characters.
- `batches/valid/` holds one batch per item type. Every item in them must be accepted. The batch
  member `x-manifest` names the manifest in `manifests/valid/` it targets; providers may ignore it,
  because unknown members are ignored. `event-and-delete.ndjson` is the NDJSON form, with a header
  line first; it targets `hormuz-example.json`.
- `batches/invalid/` holds one batch per failure. Each has exactly one item, which must be rejected
  with the reason in `x-expect`.
