"""Writes the synthetic SatDump 1.2.2 polar fixtures for prompt 054 (standard library only).

Nothing here is SatDump output: the GeoTIFF carries the same tags SatDump 1.2.2's GeoTIFF writer emits for an
equirectangular composite projection (read in the SatDump source for format facts only: one ModelTiepoint at
raster (0, 0), ModelPixelScale (scale_x, scale_y, 0), GTModelTypeGeoKey = 2, GeogAngularUnitsGeoKey = 9102,
GeographicTypeGeoKey = 4326; uncompressed strips, PlanarConfig 1, 8-bit RGB with zero outside the swath).

Usage: python3 generate-geotiff.py src/test/resources/fixtures/satdump
"""
import json, os, struct, sys, zlib

root = sys.argv[1]
v = os.path.join(root, '1.2.2')


def cbor(value):
    def head(major, n):
        if n < 24: return bytes([major << 5 | n])
        if n < 256: return bytes([major << 5 | 24, n])
        if n < 65536: return bytes([major << 5 | 25]) + struct.pack('>H', n)
        if n < 2**32: return bytes([major << 5 | 26]) + struct.pack('>I', n)
        return bytes([major << 5 | 27]) + struct.pack('>Q', n)
    if value is None: return b'\xf6'
    if value is True: return b'\xf5'
    if value is False: return b'\xf4'
    if isinstance(value, int): return head(0, value) if value >= 0 else head(1, -1 - value)
    if isinstance(value, float): return b'\xfb' + struct.pack('>d', value)
    if isinstance(value, str):
        b = value.encode('utf-8'); return head(3, len(b)) + b
    if isinstance(value, list): return head(4, len(value)) + b''.join(cbor(x) for x in value)
    if isinstance(value, dict): return head(5, len(value)) + b''.join(cbor(k) + cbor(x) for k, x in value.items())
    raise TypeError(type(value))


def tiff(width, height, samples, pixel, geo):
    """A little-endian classic TIFF, one uncompressed strip, 8-bit samples; geo = extra (tag, type, values)."""
    data = bytearray()
    for y in range(height):
        for x in range(width):
            data += bytes(pixel(x, y))
    tags = [
        (256, 4, [width]), (257, 4, [height]), (258, 3, [8] * samples), (259, 3, [1]),
        (262, 3, [2 if samples >= 3 else 1]), (273, 4, [8]), (277, 3, [samples]), (278, 4, [height]),
        (279, 4, [len(data)]), (284, 3, [1]),
    ]
    if samples in (2, 4):
        tags.append((338, 3, [2]))
    tags += geo
    tags.sort(key=lambda t: t[0])
    ifd_offset = 8 + len(data)
    ifd_offset += ifd_offset % 2
    extra_offset = ifd_offset + 2 + 12 * len(tags) + 4
    entries = b''
    extra = b''
    sizes = {2: 1, 3: 2, 4: 4, 12: 8}
    for tag, typ, values in tags:
        if typ == 2:
            payload = values.encode('ascii') + b'\x00'
            count = len(payload)
        else:
            fmt = {3: 'H', 4: 'I', 12: 'd'}[typ]
            payload = struct.pack('<' + fmt * len(values), *values)
            count = len(values)
        if len(payload) <= 4:
            entries += struct.pack('<HHI', tag, typ, count) + payload.ljust(4, b'\x00')
        else:
            entries += struct.pack('<HHII', tag, typ, count, extra_offset + len(extra))
            extra += payload
            if len(extra) % 2: extra += b'\x00'
    out = bytearray(b'II*\x00' + struct.pack('<I', ifd_offset))
    out += data
    if len(out) < ifd_offset: out += b'\x00'
    out += struct.pack('<H', len(tags)) + entries + struct.pack('<I', 0) + extra
    return bytes(out)


def png(width, height, rgb):
    raw = b''.join(b'\x00' + b''.join(bytes(rgb(x, y)) for x in range(width)) for y in range(height))
    def chunk(kind, body):
        return struct.pack('>I', len(body)) + kind + body + struct.pack('>I', zlib.crc32(kind + body) & 0xffffffff)
    return (b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 2, 0, 0, 0))
            + chunk(b'IDAT', zlib.compress(raw)) + chunk(b'IEND', b''))


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f: f.write(data)


def geokeys(west, north, scale):
    return [
        (33550, 12, [scale, scale, 0.0]),
        (33922, 12, [0.0, 0.0, 0.0, west, north, 0.0]),
        # KeyDirectoryVersion 1, revision 1.0, 3 keys: GTModelType 2, GeogAngularUnits 9102, GeographicType 4326
        (34735, 3, [1, 1, 0, 3, 1024, 0, 1, 2, 2048, 0, 1, 4326, 2054, 0, 1, 9102]),
    ]


def swath(x, y, width, height, half):
    """A descending swath band, slanting west to east from the top: True inside."""
    center = width * 0.3 + (width * 0.4) * y / height
    return abs(x - center) <= half


# The Meteor-M2-3 pass over Texas, as SatDump 1.2.2 would leave it with a composite `project` block.
pf = os.path.join(v, 'pass', '2026-10-12_18-02_meteor_m2-x_lrpt_137.9 MHz')
dataset = {"satellite": "METEOR-M2 3", "timestamp": 1791828130.0, "products": ["MSU-MR"]}
write(os.path.join(pf, 'dataset.json'), json.dumps(dataset, indent=4).encode())
tle = {"name": "METEOR-M2 3",
       "line1": "1 57166U 23091A   26285.92798396  .00000015  00000+0  25251-4 0  9990",
       "line2": "2 57166  98.5949 338.4155 0004029 171.9444 188.1801 14.24054110171272"}
product = {
    "instrument": "msu_mr", "type": "image", "product_timestamp": 1791828130.0,
    "images": [{"file": "MSU-MR-1.png", "name": "1", "bit_depth": 8}],
    "projection_cfg": {"type": "normal_single_line", "tle": tle, "corr_swath": 2800,
                       "timestamps": [1791827830.0 + 10.0 * i for i in range(61)]},
}
write(os.path.join(pf, 'MSU-MR', 'product.cbor'), cbor(product))
W, H, S = 120, 160, 0.25
write(os.path.join(pf, 'MSU-MR', 'MSU-MR-1.png'), png(24, 32, lambda x, y: ((x * 9) % 256,) * 3))
write(os.path.join(pf, 'MSU-MR', 'rgb_MSU-MR_221_Composite.png'),
      png(48, 64, lambda x, y: ((x * 5) % 256, (y * 4) % 256, ((x + y) * 3) % 256)))
write(os.path.join(pf, 'MSU-MR', 'rgb_MSU-MR_221_Composite_projected.tif'),
      tiff(W, H, 3, lambda x, y: ((x * 2) % 255 + 1, (y * 3) % 255 + 1, 90) if swath(x, y, W, H, 20) else (0, 0, 0),
           geokeys(-110.0, 42.0, S)))

# A Bering Sea pass whose GeoTIFF runs past 180 degrees (tie point at 165 E, 40 degrees wide).
bf = os.path.join(v, 'pass-bering', '2026-10-12_09-40_meteor_m2-x_lrpt_137.9 MHz')
write(os.path.join(bf, 'dataset.json'), json.dumps(
    {"satellite": "METEOR-M2 3", "timestamp": 1791797000.0, "products": ["MSU-MR"]}, indent=4).encode())
bering = dict(product, product_timestamp=1791797000.0,
              projection_cfg=dict(product["projection_cfg"], timestamps=[1791796700.0 + 10.0 * i for i in range(61)]))
write(os.path.join(bf, 'MSU-MR', 'product.cbor'), cbor(bering))
write(os.path.join(bf, 'MSU-MR', 'rgb_MSU-MR_221_Composite_projected.tif'),
      tiff(W, H, 3, lambda x, y: (200, 120, 60) if swath(x, y, W, H, 25) else (0, 0, 0), geokeys(165.0, 70.0, S)))
print('ok')
