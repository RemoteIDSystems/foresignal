# Foresignal NOAA Weather Radio SAME recipe

Receive NOAA Weather Radio with an RTL-SDR, decode its SAME headers with multimon-ng and post them to Foresignal's
`same` ingest format (prompt 086). MIT, like Foresignal.

1. In Foresignal, create a custom source that accepts the kind: `POST /v1/custom-sources` with
   `{"id": "custom-nwr", "name": "NOAA Weather Radio", "kinds": ["same-alert"], "observedAtMode": "UPSTREAM"}`, and keep
   the returned ingest key.
2. Install `rtl-sdr` (for `rtl_fm`) and `multimon-ng`, then run
   `NWR_FREQ=162.550M FORESIGNAL_URL=https://foresignal.example FORESIGNAL_SOURCE=custom-nwr FORESIGNAL_INGEST_KEY=eik_... ./receive-same.sh`.

The pipeline is `rtl_fm -f $NWR_FREQ -M fm -s 22050 - | multimon-ng -a EAS -t raw - | foresignal-post`. multimon-ng
prints `EAS: ZCZC-WXR-TOR-048453+0030-2861504-KEWX/NWS-`; each header line is posted once, and Foresignal collapses the
three repetitions (identical within 10 s) and ignores `NNNN`. Records are drawn on the named counties (the whole state for
a `000` county) when the reference data is loaded.
