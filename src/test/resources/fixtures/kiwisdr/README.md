# `kiwisdr` fixtures

| File | What it is |
|---|---|
| `kiwisdr_com.js` | Real. `GET http://rx.linkfanel.net/kiwisdr_com.js`, captured 2026-10-14T00:26:09Z: 934,996 bytes, 882 receivers. Trimmed to the first 30 receivers; the four header `//` comment lines, the trailing comma after the last entry and the `;` on its own line are kept as served |
| `edge.js` | Hand-made. Line and block comments (and `//` and `/* */` inside strings, which must survive), single-quoted and unquoted names, trailing commas, a receiver without a position, offline, with an unparseable range and capacity, an entry without an id and a repeated id |

**Capture command** (prompt 082):

```sh
curl -s -D - -A "Foresignal-fixture-capture (+https://foresignal.ai)" http://rx.linkfanel.net/kiwisdr_com.js
```

Response headers: `HTTP/1.1 200 OK`, `Server: Apache/2.4.10 (Debian)`, `Last-Modified: Wed, 14 Oct 2026 00:20:16 GMT`,
`ETag: "e4454-65dc1e5383000"`, `Content-Length: 934996`, `Content-Type: application/javascript`. Plain HTTP is used
because HTTPS to this host timed out when checked (2026-10). Every value in the list is a string; `status` was
`active` and `offline` `no` for all 882 entries.
