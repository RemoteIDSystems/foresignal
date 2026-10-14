# `meshcore-bridge` fixtures

| File | What it is |
|---|---|
| `events.json` | **`synthetic: true`.** The events `contrib/meshcore-bridge/bridge.py` would POST for the same companion as `../meshcore-local/companion-tcp.hex`, in one `{"events": [...]}` body: `self_info`, three `contact` entries, two `advert` events (the repeater again, a new sensor), a `channel_msg` on slot 1 with `channel_name` `Ops`, a `contact_msg`, and two items that must be rejected on their own: an `advert` with only a `public_key` and an unknown `telemetry` event. `data` uses meshcore_py's payload keys (`public_key, type, adv_name, last_advert, adv_lat, adv_lon, lastmod`; `channel_idx, text, sender_timestamp, SNR`; `pubkey_prefix`), checked against meshcore_py's `reader.py` on 2026-10-13. |

**Recapture** (prompt 143): run the bridge with `--verbose` against a test endpoint (for example `nc -l 9999` behind
`FORESIGNAL_URL=http://127.0.0.1:9999`) and keep one body, replacing names and keys.
