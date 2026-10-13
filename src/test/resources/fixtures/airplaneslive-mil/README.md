# `airplaneslive-mil` fixtures

| File | What it is |
|---|---|
| `forbidden-403.json` | The real body of `GET https://api.airplanes.live/v2/mil`, captured 2026-10-13T22:50:10Z: **HTTP 403** |
| `mil-synthetic.json` | **`synthetic: true`.** Three entries of `../adsbfi-mil/mil.json` (`ae57d5`, `ae05ac`, `ae4ff0`) in the v2 envelope (`ac`, `msg`, `now` in milliseconds, `total`, `ctime`, `ptime`) that airplanes.live shares with adsb.fi (both are ADSBexchange v2 compatible). Recapture once airplanes.live grants access |

**Capture command** (prompt 080):

```sh
curl -s -D - -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai; install=fixture)" https://api.airplanes.live/v2/mil
```

The response was `403`, `content-type: application/json`, `content-length: 164`, with
`{"error": "Please contact us at contact@airplanes.live. Your email MUST include any links, a description of the project, and any information you deem appropriate."}`.
No other header or route was tried. The API guide (`https://airplanes.live/api-guide/`) answered a browser
challenge page ("Enable JavaScript and cookies to continue") and was not read further.
