# Fixtures: `spc-storm-reports` (prompt 053)

| File | Origin |
|---|---|
| `yesterday-20261011.csv` | Real `https://www.spc.noaa.gov/climo/reports/yesterday.csv`, captured 2026-10-13 04:42 UTC (`Last-Modified: Mon, 12 Oct 2026 23:51:04 GMT`, `ETag: "1c80-65dad5ef3df30"`). It covers convective day 2026-10-11 (12 UTC 11 Oct to 12 UTC 12 Oct): 15 tornado and 43 wind reports, and a header-only hail section. |
| `today-20261012.csv` | Real `today.csv`, captured at the same time (`Last-Modified: Tue, 13 Oct 2026 04:38:06 GMT`, `ETag: "cc-65db161804de8"`, `Cache-Control: max-age=300`): convective day 2026-10-12, one wind report at `0353` (so 2026-10-13T03:53Z) and two header-only sections. |
| `three-sections.csv` | **Synthetic**, assembled from real rows of the two files above plus two hail rows: the hail row of the design's example (`175`, 5 WNW Oliver, GA) and an Odessa row whose comment holds an unquoted comma; one wind comment is quoted and contains commas. Used for the section, magnitude and CSV-quoting cases. |
| `header-only.csv` | The three section headers with no rows, as SPC publishes before the first report of a day. |

```sh
UA="Foresignal fixture capture (+https://foresignal.ai)"
curl -sS -D today.h -A "$UA" -o today.csv https://www.spc.noaa.gov/climo/reports/today.csv
curl -sS -D yesterday.h -A "$UA" -o yesterday.csv https://www.spc.noaa.gov/climo/reports/yesterday.csv
```

**Units.** Hail `Size` is hundredths of an inch (the report pages say "Sizes in 1/100 of an Inch (175 =
1.75")"). Wind `Speed` is **mph**: the row `1215,73,22 SE Meadview,…` says "measured a gust of 73 mph". The
pages do not state the unit; the prompt expected knots.
