# Foresignal rsudp alarm poster

`foresignal_alarm.py` sends a Raspberry Shake's rsudp trigger to a Foresignal custom source created from the
`rsudp-alarm` mapping preset (prompt 086). Standard library only; MIT, like Foresignal.

## Setup

1. In Foresignal, create the source: `POST /v1/custom-sources` with
   `{"id": "custom-shake", "preset": "rsudp-alarm", "presetParams": {"lon": -97.74, "lat": 30.27}}`. The station point
   becomes every record's geometry. The response's `setup.url` is the URL to post to (the key is in the path).
2. On the Shake, copy `foresignal_alarm.py` and set `FORESIGNAL_URL` in it (or in rsudp's environment) to that URL.
3. In rsudp's settings (`~/.config/rsudp/rsudp_settings.json`), enable the `alert` module and set
   `"custom": {"enabled": true, "codefile": "/path/to/foresignal_alarm.py", "win_override": false}`.

rsudp runs the code file on every `ALARM`. The script reads `settings.station` and `alert.channel` from rsudp's settings
file and posts `{"event": "ALARM", "time", "station", "channel"}` on a background thread (5 s timeout). rsudp's custom
module does not run on `RESET` and does not pass the trigger time, so `time` is when the script ran; to record a reset,
run `python3 foresignal_alarm.py RESET` from your own wrapper.

**Unverified (2026-10):** written from rsudp's documentation without a Shake. Turn on `captureSamples` for the source
and check `GET /v1/custom-sources/{id}/samples` after the first alarm.
