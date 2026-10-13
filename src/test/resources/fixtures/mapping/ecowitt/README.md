# ecowitt fixture

- synthetic: true — recapture from a real device: point it at a source with `captureSamples: true`, download `GET /v1/custom-sources/{id}/samples`, scrub identifiers, and replace `sample.json`.
- Built from the Ecowitt "Customized" upload in the Ecowitt protocol (form POST) as documented by Ecowitt and seen in community captures; field names from that protocol.
- `sample.json` has the `CapturedSample` shape; `expected.json` holds the preset parameters, the golden records (`observedAt` 2026-10-13T15:05:00Z is the receive time), values that must never be stored, and input keys that must not appear in any payload.
