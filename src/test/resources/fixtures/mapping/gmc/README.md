# gmc fixture

- synthetic: true — recapture from a real device: point it at a source with `captureSamples: true`, download `GET /v1/custom-sources/{id}/samples`, scrub identifiers, and replace `sample.json`.
- Built from the GQ GMC custom-server upload parameters (AID, GID, CPM, ACPM, uSV). UNVERIFIED (2026-10): no device capture exists yet.
- `sample.json` has the `CapturedSample` shape; `expected.json` holds the preset parameters, the golden records (`observedAt` 2026-10-13T15:05:00Z is the receive time), values that must never be stored, and input keys that must not appear in any payload.
