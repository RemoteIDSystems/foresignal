# chirpstack-v4 fixture

- synthetic: true — recapture from a real device: point it at a source with `captureSamples: true`, download `GET /v1/custom-sources/{id}/samples`, scrub identifiers, and replace `sample.json`.
- Built from the ChirpStack v4 HTTP integration's JSON encoding (event=up) as documented by ChirpStack.
- `sample.json` has the `CapturedSample` shape; `expected.json` holds the preset parameters, the golden records (`observedAt` 2026-10-13T15:05:00Z is the receive time), values that must never be stored, and input keys that must not appear in any payload.
