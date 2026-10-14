# Foresignal MeshCore bridge

`bridge.py` connects to the operator's own MeshCore companion radio over USB serial, BLE or TCP and pushes its events to
Foresignal's `meshcore-bridge` source (prompt 083). Use it for USB and BLE companions, or any companion without the WiFi
companion firmware. A WiFi companion can instead be read directly by the `meshcore-local` source, but not by both at
once: a companion accepts one client.

- **License:** MIT, like Foresignal.
- **Dependency:** the `meshcore` package from PyPI ([meshcore_py](https://github.com/meshcore-dev/meshcore_py), MIT). It
  is installed, not copied into this repository.
- **Python:** 3.10 or later.

## What it sends

`POST {FORESIGNAL_URL}/v1/ingest/meshcore-bridge/meshcore-json` with the header `X-Foresignal-Ingest-Key`, and a body of
`{"events": [{"type": ..., "data": {...}}]}`. `data` is the meshcore_py event payload, with its own keys.

| `type` | When | `data` keys Foresignal reads |
|---|---|---|
| `self_info` | on connect | `public_key, name, adv_type, adv_lat, adv_lon` |
| `contact` | on connect and every 30 minutes, for each stored contact | `public_key, type, adv_name, last_advert, adv_lat, adv_lon, lastmod` |
| `advert` | a contact adverts (the bridge looks the contact up by key) or a new contact appears | as `contact` |
| `channel_msg` | a channel message arrives | `channel_idx, text, sender_timestamp, SNR`, plus `channel_name` |
| `contact_msg` | a direct message arrives | `pubkey_prefix, text, sender_timestamp, SNR` |

Channel secrets are never sent: only channel names.

## What it never does

The bridge only identifies itself (app start), reads the contact list, channel names and single contacts, and fetches
queued messages. It never changes a setting, never sends a message, never reboots or resets the radio, never imports or
exports keys and never sends a login, status, telemetry or path request to another node. Repeater health in Foresignal
comes from adverts alone.

## Setup

1. In Foresignal, choose a random key that starts with `eik_` and store it as the `meshcore-bridge` source's credential
   (`PUT /v1/sources/meshcore-bridge/credentials` with `{"ingestKey": "eik_..."}`), then enable the source.
2. On the machine the radio is plugged into:

   ```sh
   sudo mkdir -p /opt/foresignal-meshcore-bridge
   sudo cp bridge.py requirements.txt /opt/foresignal-meshcore-bridge/
   cd /opt/foresignal-meshcore-bridge
   sudo python3 -m venv venv
   sudo venv/bin/pip install -r requirements.txt
   ```

3. Put the settings in `/etc/foresignal-meshcore-bridge.env` (mode `0600`):

   ```sh
   FORESIGNAL_URL=http://foresignal.lan:8080
   FORESIGNAL_INGEST_KEY=eik_...
   ```

4. Try it in the foreground, then install `meshcore-bridge.service` (edit `--serial`, `--ble` or `--tcp` first):

   ```sh
   set -a; . /etc/foresignal-meshcore-bridge.env; set +a
   venv/bin/python bridge.py --serial /dev/ttyUSB0 --verbose
   sudo cp meshcore-bridge.service /etc/systemd/system/
   sudo systemctl enable --now meshcore-bridge
   ```

The bridge reconnects to the radio on its own (5 s doubling to 5 minutes) and retries a failed POST three times.
