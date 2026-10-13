# Transit fixtures (prompt 070)

Recorded GTFS-Realtime feeds for the `transit-*` tests. Each captured `.pb` was trimmed to its header and first 30
entities with `ai.foresignal.testsupport.transit.GtfsRtFixtures trim` (run as a program after
`./gradlew compileTestJava`), which also writes the `.pb.txt` dump beside it for review. The dumps are protobuf text
format from the gtfs-realtime-bindings classes (field names rather than the field numbers of `protoc --decode_raw`;
`protoc` is not installed on the build machine).

| Directory | Upstream | Captured |
|---|---|---|
| `mbta/` | MBTA (`mbta`) | 2026-10-13 16:18 UTC |
| `entur-norway/` | Entur (`entur-norway`) | 2026-10-13 16:18 UTC |
| `hsl-helsinki/` | HSL (`hsl-helsinki`) | 2026-10-13 16:18 UTC |
| `synthetic/` | hand-made (`synthetic: true`) | `GtfsRtFixtures synthetic` |

Every capture was a plain GET with `User-Agent: Foresignal/dev (+https://foresignal.ai; fixture capture)`; Entur's also
carried `ET-Client-Name: foresignal-devcapture`. Each directory's README names the URLs and response headers.

**Time bases measured on the full captures** (entities with a vehicle timestamp): MBTA 555/555, Entur 8667/8667, HSL
788/788, so all three are `VEHICLE`. Every captured alert feed used only the deprecated `active_period`; the
`impact_period` path is covered by `synthetic/alert-impact-period.pb`.

**Recapture:** `curl -s -A "<Foresignal UA>" [-H "ET-Client-Name: foresignal-<install>"] -o full.pb <url>`, then
`java -cp build/classes/java/test:<gtfs-realtime-bindings jar>:<protobuf-java jar> ai.foresignal.testsupport.transit.GtfsRtFixtures trim full.pb <dir>/<name>.pb`.
Recapturing changes ids and times, so the tests that name entities must be updated with it.
