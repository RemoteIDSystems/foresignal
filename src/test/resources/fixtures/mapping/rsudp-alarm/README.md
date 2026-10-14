# rsudp-alarm fixture

- synthetic: true — recapture from a real Raspberry Shake: create a source from the `rsudp-alarm` preset with
  `captureSamples: true`, let `contrib/rsudp/foresignal_alarm.py` post one alarm, download
  `GET /v1/custom-sources/{id}/samples`, scrub the station name if it is not public, and replace `sample.json`.
- Built from the body `foresignal_alarm.py` sends (`event`, `time`, `station`, `channel`); the station name `R0FS1` is
  fictitious. UNVERIFIED (2026-10): no Shake capture exists yet.
- `expected.json` holds the preset parameters (the station point, rounded to 0.01° by default), the golden record and
  the input key that must not appear as a payload field (`time` becomes `observedAt` and `eventTime`).
