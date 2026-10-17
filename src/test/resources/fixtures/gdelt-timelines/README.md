# `gdelt-timelines` fixtures

**synthetic: true** — recapture from the live API. During construction (2026-10-16/17) every request to
`https://api.gdeltproject.org/api/v2/doc/doc` from the build host answered HTTP 429 with the plain-text body
"Please limit requests to one every 5 seconds …", even after waiting several minutes, so no live body could be
captured. The files follow the DOC 2.0 API documentation
(<https://blog.gdeltproject.org/gdelt-doc-2-0-api-debuts/>): `TimelineVolRaw` returns the matching article count
per step plus a `norm` field with all articles GDELT monitored in that step; `TimelineTone` returns the average
tone per step. Steps are 15 minutes for spans under 72 hours, hourly up to a week, daily beyond.

- `vol.json` — `mode=TimelineVolRaw`, four 15-minute steps with `norm`.
- `tone.json` — `mode=TimelineTone`, the same four steps.
- `vol-hourly.json` — hourly steps without `query_details.date_resolution`, so the parser infers the step.

Recapture with: `…/doc/doc?query=%22strait%20of%20hormuz%22&mode=TimelineVolRaw&format=json&timespan=1d&timelinesmooth=0`
(and `mode=TimelineTone`), at most one request every 5 seconds.
