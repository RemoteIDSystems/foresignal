# `media-ratings` fixtures (prompt 133)

- `wikidata-sparql.json` — **live capture**, 2026-10-17, from `https://query.wikidata.org/sparql` with Foresignal's
  query shape (`WikidataParser.query`) for `foxnews.com`, `theguardian.com`, `rt.com`, `reuters.com` and `bbc.co.uk`
  (eight P856 IRIs each). Wikidata content is CC0.
- `partisan-web-afp.md`, `partisan-web-template.md` — **live capture**, 2026-10-17, of
  `organisations/afp.md` and `organisations/_TEMPLATE.md` from `github.com/stockHuman/tpw-data` (master). The
  repository's `LICENSE` file is the Creative Commons Attribution 4.0 International Public License (its README badge
  says CC BY 4.0 while the badge links to CC BY-SA 4.0).
- `partisan-web-example-left.md` — **synthetic: true**, a fictitious organization in the same format.
- `partisan-web-listing.json` — **synthetic: true**, in the shape of the GitHub contents API listing of
  `organisations/` (`name`, `path`, `html_url`, `download_url`, `type`); `{{BASE}}` is replaced by the WireMock base
  URL in tests, and one entry points outside the allowed hosts (skipped).
