# digitraffic-road fixtures (prompt 074)

Captured 2026-10-13 18:43 UTC with
`curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" -H "Digitraffic-User: Foresignal-fixture/0.1.0-SNAPSHOT" <url>`
from Digitraffic's v2 per-type routes (the v1 `messages?situationType=…` route the prompt names is marked deprecated in
`https://tie.digitraffic.fi/swagger/openapi.json`, "Will be removed after 2026-10-20"; its v1 response had the same
shape with upper-case enumerations):

| File | URL | Response | Kept |
|---|---|---|---|
| `traffic-announcements.json` | `https://tie.digitraffic.fi/api/traffic-message/v2/traffic-announcements` | 200, 26,232 bytes, `ETag: W/"02fbf6f3c2d12ebdecf77faa8cb8c334c"`, `Last-Modified: Tue, 13 Oct 2026 14:48:11 GMT`, 8 situations | all 8 |
| `roadworks.json` | `https://tie.digitraffic.fi/api/traffic-message/v2/roadworks` | 200, 6,944,388 bytes, `ETag: W/"0cd557bbe3a47a59474e7648f18fd6164"`, `Last-Modified: Tue, 13 Oct 2026 17:38:31 GMT`, 569 situations | 17 whole situations: the 3 with `earlyClosing: closed`, Points, every direction value, then small ones |
| `weight-restrictions.json` | `https://tie.digitraffic.fi/api/traffic-message/v2/weight-restrictions` | 200, 104 bytes, empty FeatureCollection | as is |

The `contact` members (Fintraffic traffic centers' switchboard number and e-mail addresses) are kept on purpose: the
tests assert they never reach a payload.

`edge-cases.json` is hand-made: a Swedish accident report naming a road closure with two km/h limits, `ended` and
`retracted` announcements, a situation without `versionTime`, a road work closed by a phase restriction, a weight
restriction without geometry, and a situation without `situationId`. Its contact values are fictitious.

License CC BY 4.0, attribution "Fintraffic / digitraffic.fi" (`https://www.digitraffic.fi/en/terms-of-service/`,
checked 2026-10-13).
