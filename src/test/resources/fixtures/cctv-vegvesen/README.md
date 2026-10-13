# cctv-vegvesen fixtures (prompt 072)

- `items.json`: captured 2026-10-13 17:17 UTC from `https://ogckart-sn1.atlas.vegvesen.no/ogc/features/v1/collections/datex_3_1:CctvSimple/items?f=application/geo%2Bjson&limit=5000` with `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>`.
  HTTP 200, 799,422 bytes, 897 features in one page (`numberMatched = numberReturned = 897`, no `rel=next`), no `ETag` or `Last-Modified`.
  Trimmed to 20 features (whatever their status), two HLS cameras and three `videoOrImagesUnavailableDueToCameraFault` cameras; the envelope and links are kept. Paging is exercised with hand-made pages in `CctvPackAdapterTest`.

Frame-access probe: `https://kamera.atlas.vegvesen.no/api/images/3000063_1` → 200 `image/jpeg` → `direct`; `https://kamera.vegvesen.no/public/3000061_1/manifest.m3u8` → 200 `application/vnd.apple.mpegurl`, `Access-Control-Allow-Origin: *` → `direct`.
