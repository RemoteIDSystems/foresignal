"""Writes the hand-built SatDump fixtures for prompt 049 (no SatDump output is copied)."""
import json, os, struct, sys, math
from PIL import Image

root = sys.argv[1]

def cbor(v):
    def head(major, n):
        if n < 24: return bytes([major << 5 | n])
        if n < 256: return bytes([major << 5 | 24, n])
        if n < 65536: return bytes([major << 5 | 25]) + struct.pack('>H', n)
        if n < 2**32: return bytes([major << 5 | 26]) + struct.pack('>I', n)
        return bytes([major << 5 | 27]) + struct.pack('>Q', n)
    if v is None: return b'\xf6'
    if v is True: return b'\xf5'
    if v is False: return b'\xf4'
    if isinstance(v, int):
        return head(0, v) if v >= 0 else head(1, -1 - v)
    if isinstance(v, float): return b'\xfb' + struct.pack('>d', v)
    if isinstance(v, str):
        b = v.encode('utf-8'); return head(3, len(b)) + b
    if isinstance(v, list): return head(4, len(v)) + b''.join(cbor(x) for x in v)
    if isinstance(v, dict): return head(5, len(v)) + b''.join(cbor(k) + cbor(x) for k, x in v.items())
    raise TypeError(type(v))

def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f: f.write(data)

v = os.path.join(root, '2.0.0-alpha')

# /api, live and autotrack (keys observed in the SatDump 2.0.0-alpha legacy web server).
live = {
  "psk_demod": {"snr": 11.42, "peak_snr": 14.8, "freq": 137899850.0},
  "meteor_lrpt_decoder": {"deframer_lock": True, "viterbi_ber": 0.0841, "viterbi_lock": 1, "rs_avg": 3,
                          "viterbi_state": "SYNCED", "deframer_state": "SYNCED"}
}
write(os.path.join(v, 'api', 'live.json'), json.dumps(live, indent=4).encode())
autotrack = {
  "live_pipeline": {
    "psk_demod": {"snr": "9.6", "peak_snr": [12.1, 13.4], "freq": 137899850.0},
    "meteor_lrpt_decoder": {"deframer_lock": True, "viterbi_ber": [0.08, 0.12], "viterbi_lock": 1,
                            "rs_avg": [2, 4, 3, 3], "viterbi_state": "SYNCED", "deframer_state": "SYNCED"}
  },
  "object_tracker": {
    "object_name": "METEOR-M2 3",
    "sat_current_pos": {"az": 212.37, "el": 38.62},
    "sat_azimuth_rate": 0.41, "sat_elevation_rate": -0.12, "sat_current_range": 1186.4,
    "next_aos_time": 1791832865.0, "next_los_time": 1791833620.0,
    "next_event_in": 455.2, "next_event_is_aos": False,
    "rotator_engaged": True, "rotator_tracking": True,
    "rot_current_pos": {"az": 212.0, "el": 38.5}, "rot_current_req_pos": {"az": 212.4, "el": 38.6}
  },
  "frequency": 137900000
}
write(os.path.join(v, 'api', 'autotrack.json'), json.dumps(autotrack, indent=4).encode())
idle = {
  "object_tracker": {
    "object_name": "METEOR-M2 3", "sat_current_pos": {"az": 10.0, "el": -12.5},
    "next_aos_time": 1791832865.0, "next_los_time": 1791833620.0, "next_event_in": 600.0,
    "next_event_is_aos": True, "rotator_engaged": False, "rotator_tracking": False,
    "rot_current_pos": {"az": 0.0, "el": 0.0}, "rot_current_req_pos": {"az": 0.0, "el": 0.0}
  },
  "frequency": 137900000
}
write(os.path.join(v, 'api', 'autotrack-idle.json'), json.dumps(idle, indent=4).encode())

# A Meteor LRPT pass folder.
pf = os.path.join(v, 'pass', '2026-10-12_18-02_meteor_m2-x_lrpt_137.9 MHz')
dataset = {"satellite": "METEOR-M2 3", "timestamp": 1791828130.0, "products": ["MSU-MR"]}
write(os.path.join(pf, 'dataset.json'), json.dumps(dataset, indent=4).encode())
product = {
  "instrument": "msu_mr", "type": "image",
  "product_timestamp": 1791828130.0, "product_source": "METEOR-M2 3",
  "images": [
    {"file": "MSU-MR-1.png", "name": "1", "abs_index": 0, "bit_depth": 8, "transform": {"type": "none"}},
    {"file": "MSU-MR-2.png", "name": "2", "abs_index": 1, "bit_depth": 8, "transform": {"type": "none"}}
  ],
  "projection_cfg": {
    "type": "normal_single_line",
    "tle": {"name": "METEOR-M2 3",
            "line1": "1 57166U 23091A   26285.50000000  .00000000  00000-0  00000-0 0  9990",
            "line2": "2 57166  98.7000 300.0000 0001000  90.0000 270.0000 14.24000000 99990"},
    "timestamps": [1791828130.0, 1791828131.0]
  }
}
write(os.path.join(pf, 'MSU-MR', 'product.cbor'), cbor(product))
for i, name in enumerate(['MSU-MR-1.png', 'MSU-MR-2.png']):
    img = Image.new('L', (48, 32))
    img.putdata([(x * 5 + y * 3 + i * 40) % 256 for y in range(32) for x in range(48)])
    p = os.path.join(pf, 'MSU-MR', name); os.makedirs(os.path.dirname(p), exist_ok=True); img.save(p)
comp = Image.new('RGB', (96, 64))
comp.putdata([((x * 2) % 256, (y * 4) % 256, ((x + y) * 3) % 256) for y in range(64) for x in range(96)])
comp.save(os.path.join(pf, 'MSU-MR', 'rgb_MSU-MR_221_Composite.png'))
write(os.path.join(pf, 'meteor_m2-x_lrpt.cadu'), bytes(range(256)) * 4)

# A GOES-19 HRIT full-disk timestamp folder with two channels (downscaled to 64 x 64).
xf = os.path.join(v, 'xrit', 'IMAGES', 'GOES-19', 'Full Disk', '2026-10-12_18-00-20')
scalar = 4008.0  # meters per column of the 2712-column (112 microradian) full disk
proj = {"type": "geos", "lon0": -75.2, "sweep_x": True, "scalar_x": scalar, "scalar_y": -scalar,
        "offset_x": 1356.0 * -scalar, "offset_y": 1356.0 * scalar, "width": 2712, "height": 2712,
        "altitude": 35786023.0}
xproduct = {"instrument": "abi", "type": "image", "product_timestamp": 1791828020.0,
            "images": [
              {"file": "abi_2.png", "name": "2", "abs_index": 0, "bit_depth": 8},
              {"file": "abi_13.png", "name": "13", "abs_index": 1, "bit_depth": 8}],
            "projection_cfg": proj}
write(os.path.join(xf, 'product.cbor'), cbor(xproduct))
for name, bias in [('abi_2.png', 30), ('abi_13.png', 90)]:
    img = Image.new('L', (64, 64))
    data = []
    for y in range(64):
        for x in range(64):
            r = math.hypot(x - 31.5, y - 31.5)
            data.append(0 if r > 30 else (bias + int(r * 4)) % 255 + 1)
    img.putdata(data); img.save(os.path.join(xf, name))
# A truncated product.cbor, as SatDump leaves it while rewriting.
write(os.path.join(v, 'xrit-truncated', 'product.cbor'), cbor(xproduct)[:40])

# FY-4A: no usable projection.
ff = os.path.join(v, 'xrit-fy4', 'IMAGES', 'FY-4A', 'Full Disk', '2026-10-12_18-00-00')
write(os.path.join(ff, 'product.cbor'), cbor({"instrument": "agri", "type": "image",
      "images": [{"file": "agri_12.png", "name": "12", "abs_index": 0, "bit_depth": 8}]}))
img = Image.new('L', (16, 16), 120); img.save(os.path.join(ff, 'agri_12.png'))
print('ok')
