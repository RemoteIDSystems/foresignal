# wiki-edit-surge fixtures

`recentchange-data.json` — one Wikimedia EventStreams `recentchange` SSE `data:` payload.

- URL: `https://stream.wikimedia.org/v2/stream/recentchange`
- Captured: synthetic — **synthetic: true — recapture with a real capture per prompt 143**.
- Method: hand-built from the documented `recentchange` schema
  (`https://wikitech.wikimedia.org/wiki/Event_Platform/EventStreams_HTTP_Service`). The `meta.id` value
  is the verbatim JSON array a client stores and sends back as `Last-Event-ID`. No network call was
  made during the build.
