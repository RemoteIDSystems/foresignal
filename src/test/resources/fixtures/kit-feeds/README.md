# Feed-kit fixtures (prompt 073)

- `nhc-index-at.xml`: captured 2026-10-13 17:51 UTC from `https://www.nhc.noaa.gov/index-at.xml` with
  `curl -s --compressed -A "Foresignal-fixture/0.1.0-SNAPSHOT" <url>`; HTTP 200 `text/xml`, 2,086 bytes, unchanged.
- `ecdc-1307.xml`: captured 2026-10-13 17:51 UTC from `https://www.ecdc.europa.eu/en/taxonomy/term/1307/feed` the same
  way; HTTP 200 `application/rss+xml; charset=utf-8`, 10 items, unchanged (HTML-escaped descriptions, `dc:creator`).
- Hand-made (`synthetic: true`), under the reserved `.example` names:
  - `rss1.rdf`: RSS 1.0 (RDF) with `dc:date`, `dc:creator`, `dc:subject`, a relative link and a date without a time;
  - `atom.xml`: Atom 1.0 with `self` and `hub` links, an html title, an xhtml summary, an enclosure, categories, a GML
    polygon with a hole (lat-lon order) and a CRS84 GML point, and a `javascript:` link that must be dropped;
  - `jsonfeed.json`: JSON Feed 1.1 with `hubs`, `_geo`, attachments, tags, HTML content with a script, a numeric id
    and a date without a zone;
  - `georss-simple.xml`: GeoRSS Simple point, line, polygon (not closed), box, an out-of-range point and
    `content:encoded` in CDATA;
  - `basic-geo.xml`: W3C Basic Geo, direct and inside `geo:Point`;
  - `xxe.xml`: an external-entity payload that must be refused;
  - `odd-dates.xml`: RFC 822 dates with a two-digit year, a named zone, no day name, an offset, a pre-2000 two-digit
    year, an unreadable date, and an item without a guid.

`CISA` (`https://www.cisa.gov/cybersecurity-advisories/all.xml`) answered `403 Access Denied` to this host on
2026-10-13, so no CISA fixture was captured; recapture it at prompt 143.
