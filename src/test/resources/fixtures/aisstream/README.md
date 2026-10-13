# aisstream fixtures

synthetic: true. No aisstream.io key was available when these were made (prompt 063, 2026-10-13), so every
file here was written by hand from aisstream's published documentation (<https://aisstream.io/documentation>)
and message models (`type-definition.yaml` in <https://github.com/aisstream/ais-message-models>). **Recapture
`frames.jsonl` with a real key** (procedure below) and keep the hand-made files.

| File | What it holds |
|---|---|
| `frames.jsonl` | One message per line, as aisstream sends them: a `SubscriptionConfirmation`, then each of the five merged types (`PositionReport`, `ShipStaticData`, `StandardClassBPositionReport`, `StaticDataReport` parts A and B, `ExtendedClassBPositionReport`) and one `BaseStationReport`, around the Solent. `MetaData` uses lower-case `latitude`/`longitude`/`time_utc` on some lines and `Latitude`/`Longitude`/`TimeUtc` on others, because verified samples differ. |
| `sentinels.jsonl` | Hand-made: lat 91 / lon 181, latitude out of range, SOG 102.3 and above, COG 360 and above, heading 511 and 400, navigational status 15, IMO 0, type 0, zero dimensions, ETA "not available" (month 0, day 0, hour 24, minute 60), an impossible ETA (30 February), draught 0, and `@`/space padding. |
| `static-before-position.jsonl` | Hand-made: `ShipStaticData` for an MMSI with no position yet, then its first `PositionReport`. |
| `classb-parts.jsonl` | Hand-made: a Class B position, then `StaticDataReport` part A (name) and part B (type, call sign, dimensions). |
| `error-auth.json` | Hand-made: the `{"error": "…"}` envelope with a key-related text. |
| `untimed.json` | Hand-made: a `PositionReport` whose `MetaData` has no `time_utc`. |

## Capturing `frames.jsonl` with a real key

1. Run `websocat -B 1048576 --binary wss://stream.aisstream.io/v0/stream` (aisstream sends binary frames of
   UTF-8 JSON; `-B` raises the buffer for large frames).
2. Within 3 seconds, paste one subscription line with a **test** key and a small box, for example the Solent:

   ```json
   {"APIKey":"<test key>","BoundingBoxes":[[[50.6,-1.8],[50.95,-0.9]]],"FilterMessageTypes":["PositionReport","StandardClassBPositionReport","ExtendedClassBPositionReport","ShipStaticData","StaticDataReport"]}
   ```

   Boxes are latitude first: `[[minLat,minLon],[maxLat,maxLon]]`.
3. Save about 200 frames, one per line, to `frames.jsonl` (the first line is the `SubscriptionConfirmation`).
4. Record here the capture date and the `MetaData` key casing observed (`latitude` or `Latitude`, `time_utc` or
   `TimeUtc`).
5. **Never commit the key.** Check the file for it before committing: the key is only in the subscription you
   typed, never in what aisstream sends back.
