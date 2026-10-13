# Hand-made transit fixtures (`synthetic: true`)

Written by `ai.foresignal.testsupport.transit.GtfsRtFixtures synthetic <dir>`; the builders of that class define
them. Header time `T0` = 1791907200 (2026-10-13T16:00:00Z).

- `untimed-vehicle.pb`: vehicle `v1` with a timestamp (T0 − 10 s) and `v2` without one.
- `alert-impact-period.pb`: alert `A` (communication period, two closed impact periods T0+1h..T0+2h and
  T0+25h..T0+26h, SPECIAL_EVENT / DETOUR / WARNING, English and Spanish header) and alert `B` (one open-ended impact
  period from T0 − 1 h).
- `full-dataset-alert-removed.pb`: the same FULL_DATASET feed at T0 + 60 s with only `A` (B has ended).
- `alert-legacy-active-period.pb`: alert `L` with two deprecated `active_period`s and Finnish and English texts.
- `differential-alert-deleted.pb`: a DIFFERENTIAL feed at T0 + 120 s whose only entity deletes `B`.
- `html-200.html`: an HTML maintenance page served with status 200.
