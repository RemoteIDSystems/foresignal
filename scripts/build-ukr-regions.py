"""Generate Foresignal's bundled Ukraine boundaries and region crosswalks (prompt 081).

Usage:
  python3 -I scripts/build-ukr-regions.py \
      --adm1 geoBoundaries-UKR-ADM1_simplified.geojson \
      --adm2 geoBoundaries-UKR-ADM2_simplified.geojson \
      --alerts-locations alerts-in-ua-locations.csv \
      --siren-regions siren-regions.json \
      --out src/main/resources/geo/ukr

Inputs (downloaded by hand from the official locations, never committed):
  * geoBoundaries gbHumanitarian UKR ADM1 and ADM2, the "_simplified" GeoJSON of release commit 9469f09
    (https://github.com/wmgeolab/geoBoundaries/raw/9469f09/releaseData/gbHumanitarian/UKR/ADM1/
    geoBoundaries-UKR-ADM1_simplified.geojson and the same path for ADM2). Source: State Scientific Production
    Enterprise "Kartographia" via HDX (OCHA COD-AB), year represented 2022, license CC BY 3.0 IGO. 27 ADM1 units
    (24 oblasts, Crimea, Kyiv city, Sevastopol) and 139 ADM2 units (the 2020 raions, Kyiv, Sevastopol and the
    Chornobyl exclusion zone);
  * the alerts.in.ua location list linked from https://devs.alerts.in.ua/ ("Повний список локацій", a Google
    Sheet exported as CSV): UID, name, type (oblast, raion, hromada, city with special status), ordered oblast,
    raion, its hromadas;
  * https://siren.pp.ua/api/v3/regions: the siren.pp.ua region tree (State, District, Community).

Outputs:
  * adm1.geojson, adm2.geojson: one feature per unit with properties {id, name}; every polygon simplified with
    Douglas-Peucker at 0.001 degrees (about 100 m), doubling the tolerance until it has at most 1,500 vertices,
    coordinates rounded to 4 decimals (about 11 m). ADM2 ids are "adm2:{oblast}:{raion}", the oblast found by containment of the unit's
    interior point;
  * alerts-in-ua-uids.json, siren-region-ids.json: upstream id -> {name, type, boundary, parent}. "boundary" is
    the id of the unit whose polygon is the region (oblasts and raions), "parent" the id of the polygon used,
    approximately, for a smaller region (hromadas use their raion). Names are matched by the national
    romanization of 2010 (Cabinet of Ministers resolution 55) with a short table of raions renamed after the
    boundary data was made. Unmatched ids are listed on stderr and stored with neither.

Uses only the Python standard library.
"""
import argparse
import csv
import hashlib
import json
import math
import pathlib
import re
import sys

MAX_VERTICES = 1500
DECIMALS = 4
BASE_TOLERANCE = 0.001

# The 2010 national romanization (KMU resolution 55). Initial forms differ for є, ї, й, ю, я.
LATIN = {
    "а": "a", "б": "b", "в": "v", "г": "h", "ґ": "g", "д": "d", "е": "e", "є": "ie", "ж": "zh", "з": "z",
    "и": "y", "і": "i", "ї": "i", "й": "i", "к": "k", "л": "l", "м": "m", "н": "n", "о": "o", "п": "p",
    "р": "r", "с": "s", "т": "t", "у": "u", "ф": "f", "х": "kh", "ц": "ts", "ч": "ch", "ш": "sh",
    "щ": "shch", "ь": "", "ю": "iu", "я": "ia", "'": "", "’": "", "ʼ": "",
}
INITIAL = {"є": "ye", "ї": "yi", "й": "y", "ю": "yu", "я": "ya"}

# Raions renamed after the 2022 boundary data (Verkhovna Rada resolutions of 2024), and the source's own typo.
RENAMED = {
    "sheptytskyi": "chervonohradskyi",
    "zviahelskyi": "novohrad-volynskyi",
    "volodymyrskyi": "volodymyr-volynskyi",
    "samarivskyi": "novomoskovskyi",
    "berestynskyi": "krasnohradskyi",
    "chernivetskyi": "cnernivetskyi",
    "siverskodonetskyi": "sievierodonetskyi",
}


def latin(text):
    """Romanizes Ukrainian text word by word (2010 national system)."""
    out = []
    for word in re.split(r"(\s+|-)", text.lower()):
        chars = []
        for i, ch in enumerate(word):
            if ch == "г" and i > 0 and word[i - 1] == "з":
                chars.append("gh")
            elif i == 0 and ch in INITIAL:
                chars.append(INITIAL[ch])
            else:
                chars.append(LATIN.get(ch, ch))
        out.append("".join(chars))
    return "".join(out)


def slug(text):
    return re.sub(r"[^a-z0-9]+", "-", text.lower()).strip("-")


def key(text):
    return re.sub(r"[^a-z0-9]", "", text.lower())


# ------------------------------------------------------------------------------------------------ geometry

def rings_of(geometry):
    if geometry["type"] == "Polygon":
        return [geometry["coordinates"]]
    return geometry["coordinates"]


def perpendicular(p, a, b):
    if a == b:
        return math.hypot(p[0] - a[0], p[1] - a[1])
    dx, dy = b[0] - a[0], b[1] - a[1]
    t = max(0.0, min(1.0, ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / (dx * dx + dy * dy)))
    return math.hypot(p[0] - (a[0] + t * dx), p[1] - (a[1] + t * dy))


def douglas_peucker(points, tolerance):
    keep = [False] * len(points)
    keep[0] = keep[-1] = True
    stack = [(0, len(points) - 1)]
    while stack:
        first, last = stack.pop()
        best, index = 0.0, -1
        for i in range(first + 1, last):
            d = perpendicular(points[i], points[first], points[last])
            if d > best:
                best, index = d, i
        if index >= 0 and best > tolerance:
            keep[index] = True
            stack.append((first, index))
            stack.append((index, last))
    return [p for p, k in zip(points, keep) if k]


def simplify_ring(ring, tolerance):
    if tolerance <= 0:
        out = ring
    else:
        out = douglas_peucker(ring, tolerance)
    out = [[round(x, DECIMALS), round(y, DECIMALS)] for x, y in out]
    dedup = [out[0]]
    for p in out[1:]:
        if p != dedup[-1]:
            dedup.append(p)
    if dedup[0] != dedup[-1]:
        dedup.append(dedup[0])
    return dedup if len(dedup) >= 4 else None


def simplify_polygon(polygon):
    tolerance = BASE_TOLERANCE
    while True:
        rings = []
        for i, ring in enumerate(polygon):
            simple = simplify_ring(ring, tolerance)
            if simple is None:
                if i == 0:
                    return None
                continue
            rings.append(simple)
        if sum(len(r) for r in rings) <= MAX_VERTICES:
            return rings
        tolerance *= 2


def simplify(geometry):
    polygons = [p for p in (simplify_polygon(poly) for poly in rings_of(geometry)) if p]
    if len(polygons) == 1:
        return {"type": "Polygon", "coordinates": polygons[0]}
    return {"type": "MultiPolygon", "coordinates": polygons}


def ring_area(ring):
    return 0.5 * sum(a[0] * b[1] - b[0] * a[1] for a, b in zip(ring, ring[1:]))


def area(geometry):
    return sum(abs(ring_area(p[0])) - sum(abs(ring_area(h)) for h in p[1:]) for p in rings_of(geometry))


def in_ring(point, ring):
    x, y = point
    inside = False
    for (x1, y1), (x2, y2) in zip(ring, ring[1:]):
        if (y1 > y) != (y2 > y) and x < (x2 - x1) * (y - y1) / (y2 - y1) + x1:
            inside = not inside
    return inside


def contains(geometry, point):
    for polygon in rings_of(geometry):
        if in_ring(point, polygon[0]) and not any(in_ring(point, h) for h in polygon[1:]):
            return True
    return False


def interior_point(geometry):
    """A point inside the largest polygon: the middle of the widest horizontal chord through its centroid."""
    polygon = max(rings_of(geometry), key=lambda p: abs(ring_area(p[0])))
    outer = polygon[0]
    y = sum(p[1] for p in outer) / len(outer)
    xs = []
    for (x1, y1), (x2, y2) in zip(outer, outer[1:]):
        if (y1 > y) != (y2 > y):
            xs.append((x2 - x1) * (y - y1) / (y2 - y1) + x1)
    xs.sort()
    best = max(((xs[i + 1] - xs[i], (xs[i] + xs[i + 1]) / 2) for i in range(0, len(xs) - 1, 2)), default=None)
    return (best[1], y) if best else tuple(outer[0])


# ------------------------------------------------------------------------------------------------ units

def build_units(adm1_path, adm2_path):
    adm1 = json.loads(pathlib.Path(adm1_path).read_text(encoding="utf-8"))["features"]
    adm2 = json.loads(pathlib.Path(adm2_path).read_text(encoding="utf-8"))["features"]
    oblasts = []
    kyiv = [f for f in adm1 if f["properties"]["shapeName"] == "Kyivska"]
    kyiv_city = min(kyiv, key=lambda f: area(f["geometry"]))
    for f in adm1:
        name = f["properties"]["shapeName"]
        if f is kyiv_city:
            uid, label = "adm1:kyiv-city", "Kyiv (city)"
        elif name == "Sevastopilska":
            uid, label = "adm1:sevastopol-city", "Sevastopol (city)"
        else:
            uid, label = "adm1:" + slug(name), name
        oblasts.append({"id": uid, "name": label, "geometry": f["geometry"], "match": key(name)})
    raions = []
    for f in adm2:
        name = f["properties"]["shapeName"]
        point = interior_point(f["geometry"])
        parent = next((o for o in oblasts if contains(o["geometry"], point)), None)
        if parent is None:
            parent = min(oblasts, key=lambda o: min(math.hypot(point[0] - c[0], point[1] - c[1])
                                                     for poly in rings_of(o["geometry"]) for c in poly[0]))
        if name in ("Kyivska", "Sevastopilska"):
            uid = "adm2:" + parent["id"][5:] + ":city"
        else:
            uid = "adm2:" + parent["id"][5:] + ":" + slug(name)
        raions.append({"id": uid, "name": name, "geometry": f["geometry"], "match": key(name),
                       "oblast": parent["id"]})
    return oblasts, raions


def write_units(units, path):
    features = []
    vertices = []
    for u in units:
        geometry = simplify(u["geometry"])
        vertices.append(max(sum(len(r) for r in p) for p in rings_of(geometry)))
        features.append({"type": "Feature", "properties": {"id": u["id"], "name": u["name"]},
                         "geometry": geometry})
    text = json.dumps({"type": "FeatureCollection", "features": features}, ensure_ascii=False,
                      separators=(",", ":"))
    pathlib.Path(path).write_text(text + "\n", encoding="utf-8")
    return len(text), max(vertices)


# ------------------------------------------------------------------------------------------------ matching

class Matcher:
    def __init__(self, oblasts, raions):
        self.oblasts = {o["match"]: o for o in oblasts if not o["id"].endswith("-city")}
        self.raions = {}
        for r in raions:
            self.raions.setdefault(r["match"], []).append(r)

    def oblast(self, name):
        n = name.strip()
        if n == "м. Київ":
            return "adm1:kyiv-city"
        if n == "м. Севастополь":
            return "adm1:sevastopol-city"
        if n == "Автономна Республіка Крим":
            return "adm1:avtonomna-respublika-krym"
        word = n.replace(" область", "")
        found = self.oblasts.get(key(latin(word)))
        return found["id"] if found else None

    def raion(self, name, oblast=None):
        word = latin(name.strip().replace(" район", ""))
        word = RENAMED.get(word, word)
        found = self.raions.get(key(word), [])
        if oblast:
            narrowed = [r for r in found if r["oblast"] == oblast]
            found = narrowed or found
        return found[0]["id"] if len(found) == 1 else None


def alerts_crosswalk(path, matcher, unmatched):
    rows = list(csv.reader(pathlib.Path(path).open(encoding="utf-8")))
    header = next(i for i, r in enumerate(rows) if r and r[0] == "UID")
    out = {}
    raion_boundary = None
    for row in rows[header + 1:]:
        if not row or not row[0].strip().isdigit():
            continue
        uid, name, kind = row[0].strip(), row[1].strip(), row[2].strip()
        if kind == "Область" or kind.startswith("Місто з спеціальним"):
            boundary = matcher.oblast(name)
            entry = {"name": name, "type": "city" if kind.startswith("Місто") else "oblast"}
            raion_boundary = None
        elif kind == "Район":
            boundary = matcher.raion(name)
            raion_boundary = boundary
            entry = {"name": name, "type": "raion"}
        else:
            boundary = None
            entry = {"name": name, "type": "hromada"}
            if raion_boundary:
                entry["parent"] = raion_boundary
        if boundary:
            entry["boundary"] = boundary
        elif entry["type"] != "hromada" or "parent" not in entry:
            unmatched.append(("alerts.in.ua", uid, name))
        out[uid] = entry
    return out


def siren_crosswalk(path, matcher, alerts, unmatched):
    states = json.loads(pathlib.Path(path).read_text(encoding="utf-8"))["states"]
    by_name = {v["name"]: v for v in alerts.values()}
    out = {}

    def visit(region, oblast, raion):
        rid, name, kind = region["regionId"], region["regionName"].strip(), region["regionType"]
        entry = {"name": name, "type": kind}
        if kind == "State":
            boundary = matcher.oblast(name)
            oblast, raion = boundary, None
            if boundary:
                entry["boundary"] = boundary
        elif kind == "District":
            boundary = matcher.raion(name, oblast)
            raion = boundary
            if boundary:
                entry["boundary"] = boundary
        else:
            parent = raion
            if parent is None and name in by_name:
                parent = by_name[name].get("parent")
            if parent:
                entry["parent"] = parent
        if "boundary" not in entry and "parent" not in entry and name in by_name:
            # Kharkiv and Zaporizhzhia city communities are top-level "State" entries in the siren tree.
            for field in ("boundary", "parent"):
                if field in by_name[name]:
                    entry[field] = by_name[name][field]
        if "boundary" not in entry and "parent" not in entry:
            unmatched.append(("siren.pp.ua", rid, name))
        out[rid] = entry
        for child in region.get("regionChildIds") or []:
            visit(child, oblast, raion)

    for state in states:
        visit(state, None, None)
    return out


def sha256(path):
    return hashlib.sha256(pathlib.Path(path).read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    parser.add_argument("--adm1", required=True)
    parser.add_argument("--adm2", required=True)
    parser.add_argument("--alerts-locations", required=True)
    parser.add_argument("--siren-regions", required=True)
    parser.add_argument("--out", required=True)
    args = parser.parse_args()
    out = pathlib.Path(args.out)
    out.mkdir(parents=True, exist_ok=True)

    oblasts, raions = build_units(args.adm1, args.adm2)
    sizes = [write_units(oblasts, out / "adm1.geojson"), write_units(raions, out / "adm2.geojson")]
    matcher = Matcher(oblasts, raions)
    unmatched = []
    alerts = alerts_crosswalk(args.alerts_locations, matcher, unmatched)
    siren = siren_crosswalk(args.siren_regions, matcher, alerts, unmatched)
    inputs = {
        "adm1": sha256(args.adm1), "adm2": sha256(args.adm2),
        "alertsLocations": sha256(args.alerts_locations), "sirenRegions": sha256(args.siren_regions),
    }
    for name, table in (("alerts-in-ua-uids.json", alerts), ("siren-region-ids.json", siren)):
        document = {"generatedBy": "scripts/build-ukr-regions.py", "inputsSha256": inputs,
                    "regions": dict(sorted(table.items(), key=lambda kv: int(kv[0])))}
        lines = [json.dumps(k, ensure_ascii=False) + ":" + json.dumps(v, ensure_ascii=False, separators=(",", ":"))
                 for k, v in document.pop("regions").items()]
        head = json.dumps(document, ensure_ascii=False, separators=(",", ":"))[:-1]
        (out / name).write_text(head + ',"regions":{\n' + ",\n".join(lines) + "\n}}\n", encoding="utf-8")
    for source, uid, name in unmatched:
        print(f"unmatched {source} {uid} {name}", file=sys.stderr)
    print(f"adm1 {sizes[0][0]} bytes (max {sizes[0][1]} vertices), adm2 {sizes[1][0]} bytes (max {sizes[1][1]}"
          f" vertices); alerts {len(alerts)} ids, siren {len(siren)} ids, {len(unmatched)} unmatched")


if __name__ == "__main__":
    main()
