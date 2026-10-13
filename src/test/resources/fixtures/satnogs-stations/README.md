# satnogs-stations fixtures

- `stations-online.json`: `GET https://network.satnogs.org/api/stations/?status=online`, captured 2026-10-13
  (297 stations, 264 KB, one unpaged array, no `Link` header), trimmed to the first 20 stations. `owner` was replaced
  with `redacted`, `description` emptied and `image` set to null (none of them is stored).

On 2026-10-13 `status=2` and `status=Online` answered `400 {"status":["Select a valid choice. … is not one of the
available choices."]}`; the unfiltered list was 4,517 stations (3.5 MB: 4,169 Offline, 297 Online, 51 Testing).

The `Link`-header paging of `SpaceAdaptersTest` uses stub pages built from this file.

Recapture per prompt 143.
