# tts-v3 fixture

- synthetic: true — recapture from a real device: point it at a source with `captureSamples: true`, download `GET /v1/custom-sources/{id}/samples`, scrub identifiers, and replace `sample.json`.
- Built from a The Things Stack v3 webhook uplink message as documented by The Things Industries; no frm-payload location, so the configured user location applies.
- `sample.json` has the `CapturedSample` shape; `expected.json` holds the preset parameters, the golden records (`observedAt` 2026-10-13T15:05:00Z is the receive time), values that must never be stored, and input keys that must not appear in any payload.
