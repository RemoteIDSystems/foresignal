# world-port-index fixtures

- `ports.json`: `GET https://msi.nga.mil/api/publications/world-port-index?output=json` (about 6.3 MB, 2,951
  ports).

This fixture is a **synthetic** four-port sample with the real field names: a full "L" port with decimal
`xcoord`/`ycoord` and DMS `latitude`/`longitude` strings, a "V" port without depth fields, an unknown
harbor size, and a port without coordinates.

`synthetic: true` — recapture with a trimmed real slice per prompt 143.
