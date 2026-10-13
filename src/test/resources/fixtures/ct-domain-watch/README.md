# ct-domain-watch fixtures

- `issuances.json`: `GET https://api.certspotter.com/v1/issuances?domain=example.com&include_subdomains=true&match_wildcards=true&expand=dns_names&expand=issuer&expand=issuer.caa_domains&expand=issuer.operator`,
  captured 2026-10-13 unauthenticated (`X-RateLimit-Limit: 10`, a `Link: rel="next"` header with
  `after=17433446883`): 13 issuances, issuers Sectigo and SSL.com. `example.com` is the IANA-reserved example domain,
  not a domain Foresignal's builder controls; `foresignal.ai`, the product domain, returned no issuances.
- `issuances-empty.json`: the empty array the API answers when there is nothing new (captured for `foresignal.ai`
  the same day, with `Retry-After: 3600`).

Recapture per prompt 143 for a domain the operator controls, with an API key.
