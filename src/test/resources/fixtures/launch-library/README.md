# `launch-library` fixtures

| File | What it is |
|---|---|
| `launches-page1.json` | Anonymous `GET https://ll.thespacedevs.com/2.3.0/launches/?net__gte=2026-10-06T00:00:00Z&net__lte=2026-12-12T00:00:00Z&limit=3&mode=normal&ordering=net`, captured 2026-10-13 (200, `application/json`, 3 of 23 launches, `next` set). Pad `latitude`/`longitude` are JSON numbers in this capture |

Tests rewrite `next` to point at the stub server and build further pages from this one.
