# cctv-ontario511 fixtures (prompt 072)

**synthetic: true. Recapture with a real key.** On 2026-10-13 `https://511on.ca/api/v2/get/cameras?format=json&lang=en` without a key answered
`400` with `<Error><Message>Invalid Key</Message></Error>`, and no key was available during construction.

- `cameras.json`: the two cameras of the published JSON sample at `https://511on.ca/help/endpoint/cameras` (fields `Id, Source, SourceId, Roadway, Direction, Latitude, Longitude, Location, SortOrder, Views[{Id, Url, Status, Description}]`), plus hand-made rows: a camera whose first view is "down", a disabled-only camera, an off-host view URL, an out-of-bounds camera and a row without an `Id`.

Frame-access probe: `https://511on.ca/map/Cctv/815` (no key) → 200 `image/jpeg`, `Access-Control-Allow-Origin: *` → `direct`.
Published throttle: ten calls every 60 seconds (`https://511on.ca/developers/doc`).
