# nuclear-plants-wikidata fixtures

- `sparql.json`: a Wikidata SPARQL JSON result for the fixed query in `WikidataNuclearParser.QUERY`
  (`POST https://query.wikidata.org/sparql`, `Accept: application/sparql-results+json`).

This fixture is a **synthetic** two-binding sample (one with a capacity and inception, one without) using
the real SPARQL results JSON shape.

`synthetic: true` — recapture with a real response per prompt 143. Wikidata is CC0.
