# `meteoalarm-greece` fixtures

Captured 2026-10-14 from the MeteoAlarm JSON API,
<https://feeds.meteoalarm.org/api/v1/warnings/feeds-greece>, trimmed to the first 20 warnings. The alert members
are CAP 1.2 as JSON; `info` blocks carry `en-GB` and `el-GR` languages and areas carry `EMMA_ID` geocodes.
MeteoAlarm / EUMETNET terms: <https://www.meteoalarm.org/en/live/page/terms-of-use>.

**Recapture (prompt 143):** `curl -s https://feeds.meteoalarm.org/api/v1/warnings/feeds-greece | python3 -c
"import json,sys; d=json.load(sys.stdin); d['warnings']=d['warnings'][:20]; print(json.dumps(d,indent=1))" >
warnings.json`.
