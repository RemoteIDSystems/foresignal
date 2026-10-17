# `wikimedia-pageviews` fixtures

Real captures, 2026-10-16, with `curl -A "Foresignal/0.1.0-SNAPSHOT (+https://foresignal.ai; install=fixture)"`:

- `per-article.json` — `GET https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia/all-access/user/Strait_of_Hormuz/daily/20261005/20261008`.
- `top.json` — `GET https://wikimedia.org/api/rest_v1/metrics/pageviews/top/en.wikipedia/all-access/2026/10/14`,
  trimmed from 1,000 to the first 120 articles.
- `not-found.json` — the 404 problem body for an article with no data in the range
  (`…/per-article/en.wikipedia/all-access/user/No_such_article_xyzzy_123/daily/20261005/20261006`).

License: CC0. User-Agent policy: <https://meta.wikimedia.org/wiki/User-Agent_policy>.
