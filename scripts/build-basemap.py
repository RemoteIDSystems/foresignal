#!/usr/bin/env python3
"""Build the static-map basemap asset src/main/resources/basemap/ne50m.bin (prompt 055).

Usage: python3 scripts/build-basemap.py [output]

Downloads five Natural Earth 1:50m layers (public domain) from the Natural Earth vector repository at a pinned
tag, quantizes every coordinate to int32 microdegrees, delta-encodes each ring as zigzag varints and writes one
little-endian binary. The build never runs this: the generated file is committed, and the application only reads
it (ai.foresignal.raster.render.BasemapStore).

File layout (all integers little-endian):

  offset 0   4 bytes  magic "FSBM"
  offset 4   u16      version (1)
  offset 6   u16      layer count
  offset 8   u32      reserved (0)
  offset 12  layer table, 48 bytes per layer:
               16 bytes  name, ASCII, NUL-padded
               u8        geometry type: 1 polygon (ring 0 outer, the rest holes), 2 line
               3 bytes   reserved (0)
               u32       part count
               u32       byte offset of the first part (from the start of the file)
               u32       byte length of the layer's parts
               4 x i32   layer bounding box: min lon, min lat, max lon, max lat (microdegrees)
  parts, per layer, each:
               4 x i32   part bounding box (microdegrees)
               u16       ring count
               u16       reserved (0)
               per ring: u32 point count, i32 lon, i32 lat of the first point, then point count - 1 pairs of
                         zigzag LEB128 varints: the lon and lat deltas from the previous point.

Standard library only.
"""
import json
import pathlib
import struct
import sys
import urllib.request

TAG = "v5.1.2"
BASE = "https://raw.githubusercontent.com/nvkelso/natural-earth-vector/" + TAG + "/geojson/"
# (layer name, Natural Earth file, geometry type)
LAYERS = [
    ("land", "ne_50m_land", 1),
    ("lakes", "ne_50m_lakes", 1),
    ("coastline", "ne_50m_coastline", 2),
    ("admin0", "ne_50m_admin_0_boundary_lines_land", 2),
    ("admin1", "ne_50m_admin_1_states_provinces_lines", 2),
]
MAGIC = b"FSBM"
VERSION = 1
ENTRY = 48


def quantize(v):
    return int(round(v * 1_000_000))


def zigzag(n):
    return (n << 1) ^ (n >> 63)


def varint(n, out):
    while True:
        b = n & 0x7F
        n >>= 7
        if n:
            out.append(b | 0x80)
        else:
            out.append(b)
            return


def rings_of(geometry, gtype):
    """Yields parts, each a list of rings, each a list of (lon, lat) microdegree pairs."""
    kind = geometry["type"]
    coords = geometry["coordinates"]
    if gtype == 1:
        polys = [coords] if kind == "Polygon" else coords if kind == "MultiPolygon" else []
        for poly in polys:
            yield [[(quantize(x), quantize(y)) for x, y, *_ in ring] for ring in poly]
    else:
        lines = [coords] if kind == "LineString" else coords if kind == "MultiLineString" else []
        for line in lines:
            yield [[(quantize(x), quantize(y)) for x, y, *_ in line]]


def dedupe(ring):
    out = []
    for p in ring:
        if not out or out[-1] != p:
            out.append(p)
    return out


def encode_part(rings):
    rings = [dedupe(r) for r in rings]
    rings = [r for r in rings if len(r) >= 2]
    if not rings:
        return None, None
    xs = [p[0] for r in rings for p in r]
    ys = [p[1] for r in rings for p in r]
    bbox = (min(xs), min(ys), max(xs), max(ys))
    out = bytearray(struct.pack("<4iHH", *bbox, len(rings), 0))
    for ring in rings:
        out += struct.pack("<I2i", len(ring), ring[0][0], ring[0][1])
        px, py = ring[0]
        for x, y in ring[1:]:
            varint(zigzag(x - px), out)
            varint(zigzag(y - py), out)
            px, py = x, y
    return bytes(out), bbox


def main():
    target = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "src/main/resources/basemap/ne50m.bin")
    encoded = []
    for name, file, gtype in LAYERS:
        url = BASE + file + ".geojson"
        print("fetching", url, file=sys.stderr)
        with urllib.request.urlopen(url, timeout=120) as response:
            doc = json.load(response)
        data = bytearray()
        parts = 0
        box = [2**31 - 1, 2**31 - 1, -(2**31), -(2**31)]
        for feature in doc["features"]:
            geometry = feature.get("geometry")
            if not geometry:
                continue
            for rings in rings_of(geometry, gtype):
                part, bbox = encode_part(rings)
                if part is None:
                    continue
                data += part
                parts += 1
                box = [min(box[0], bbox[0]), min(box[1], bbox[1]), max(box[2], bbox[2]), max(box[3], bbox[3])]
        encoded.append((name, gtype, parts, bytes(data), box))
        print(f"  {name}: {parts} parts, {len(data)} bytes", file=sys.stderr)
    header = bytearray(MAGIC + struct.pack("<HHI", VERSION, len(encoded), 0))
    offset = len(header) + ENTRY * len(encoded)
    body = bytearray()
    for name, gtype, parts, data, box in encoded:
        header += name.encode("ascii").ljust(16, b"\0")
        header += struct.pack("<B3xIII4i", gtype, parts, offset + len(body), len(data), *box)
        body += data
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(bytes(header + body))
    print(f"wrote {target}: {len(header) + len(body)} bytes (Natural Earth {TAG})", file=sys.stderr)


if __name__ == "__main__":
    main()
