# EDP 1.0 JSON Schemas

The `*.schema.json` files are generated from `docs/OpenAPI.json` (`Edp*` components); do not edit them by hand. Regenerate them with `python3 scripts/export-edp-schemas.py docs/OpenAPI.json docs/protocol/edp-1.0` whenever the spec changes; prompt 024 serves them at `/v1/external/protocol/schemas/{name}`. The protocol itself is described in [External Data Protocol](../../External-Data-Protocol.md).

`examples/` is the provider kit of prompt 138, written by hand: one minimal annotated provider per item type and a Python reference provider that serves a CSV by push and pull. See [examples/README.md](examples/README.md).
