# `gdelt-articles` fixtures

- `artlist-live.json` — **live capture**, 2026-10-18 01:30 UTC:
  `https://api.gdeltproject.org/api/v2/doc/doc?query=hormuz&mode=ArtList&format=json&maxrecords=3&sort=DateDesc&timespan=1d`
  (the first attempt answered HTTP 429 "Please limit requests to one every 5 seconds…"; the second, 8 seconds later,
  answered 200). It confirms the ArtList shape: `{"articles": [{url, url_mobile, title, seendate
  ("20261018T013000Z"), socialimage, domain, language ("English"), sourcecountry ("Russia")}]}`.
- `artlist.json` — **synthetic: true**, in the live shape: a wire article and a reprint with a near-identical title
  (one with tracking parameters, one with an upper-case host and a fragment), a third outlet, an entry with an
  unusable URL and one without `seendate` (both skipped). Outlet names and domains are fictitious except `apnews.com`.

Recapture with the URL above (at most one request every 5 seconds to `api.gdeltproject.org`).
