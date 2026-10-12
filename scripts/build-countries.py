"""Generate Foresignal's bundled country table and simplified country polygons (prompt 040).

Usage:
  python3 -I scripts/build-countries.py \
      --admin0 ne_10m_admin_0_countries.geojson \
      --country-info countryInfo.txt \
      --out src/main/resources/reference

Inputs (downloaded by hand from the official locations, never committed):
  * Natural Earth 10m admin-0 countries, GeoJSON, release v5.1.2 of
    https://github.com/nvkelso/natural-earth-vector (public domain);
  * GeoNames countryInfo.txt from https://download.geonames.org/export/dump/ (CC BY 4.0).

Outputs:
  * countries.csv: iso2, iso3, isoNumeric, fips, name, nameLong, continent, labelLon, labelLat,
    aliases. iso2 is NE's ISO_A2_EH (never the -99 values of ISO_A2); features whose ISO_A2_EH is
    -99 are left out; when several features share an iso2 the home part (HOMEPART = 1, largest
    POP_EST) supplies the row. Aliases are that feature's NE names, the GeoNames countryInfo name and a few
    common variants, pipe-separated. The first line is a '#' comment recording the inputs.
  * countries-simple.geojson.gz: one feature per iso2 from the 10m file, simplified with
    Douglas-Peucker at 0.01 degrees (about 1 km) and rounded to 0.01 degrees (rings too small for
    that keep 4 decimals), for the offline iso2 fallback of RegionTagger when no reference data is
    loaded.

Uses only the Python standard library.
"""
import argparse
import csv
import gzip
import hashlib
import io
import json
import pathlib

# Common variants that neither Natural Earth nor GeoNames spells (prompt 040 names the first three).
VARIANTS = {
    "TR": ["Türkiye", "Turkiye", "Turkey"],
    "CZ": ["Czechia", "Czech Republic"],
    "CI": ["Côte d'Ivoire", "Cote d'Ivoire", "Ivory Coast"],
    "US": ["USA", "United States of America", "United States", "U.S.", "America"],
    "GB": ["UK", "United Kingdom", "Great Britain", "Britain"],
    "RU": ["Russia", "Russian Federation"],
    "KR": ["South Korea", "Republic of Korea", "Korea, Republic of"],
    "KP": ["North Korea", "DPRK", "Democratic People's Republic of Korea"],
    "IR": ["Iran", "Islamic Republic of Iran"],
    "SY": ["Syria", "Syrian Arab Republic"],
    "LA": ["Laos", "Lao PDR", "Lao People's Democratic Republic"],
    "VN": ["Vietnam", "Viet Nam"],
    "MM": ["Myanmar", "Burma"],
    "SZ": ["Eswatini", "Swaziland"],
    "MK": ["North Macedonia", "Macedonia"],
    "CV": ["Cabo Verde", "Cape Verde"],
    "CD": ["DR Congo", "DRC", "Democratic Republic of the Congo", "Congo-Kinshasa", "Congo (Kinshasa)"],
    "CG": ["Republic of the Congo", "Congo-Brazzaville", "Congo (Brazzaville)", "Congo"],
    "BO": ["Bolivia"],
    "VE": ["Venezuela"],
    "TZ": ["Tanzania"],
    "MD": ["Moldova"],
    "PS": ["Palestine", "State of Palestine", "Palestinian Territories"],
    "VA": ["Vatican", "Holy See", "Vatican City"],
    "FM": ["Micronesia", "Federated States of Micronesia"],
    "TL": ["Timor-Leste", "East Timor"],
    "BN": ["Brunei"],
    "GM": ["The Gambia", "Gambia"],
    "BS": ["The Bahamas", "Bahamas"],
    "XK": ["Kosovo"],
    "TW": ["Taiwan"],
}

NE_NAME_FIELDS = ["NAME", "NAME_LONG", "ADMIN", "FORMAL_EN", "BRK_NAME", "NAME_SORT", "NAME_ALT", "GEOUNIT",
                  "SUBUNIT", "NAME_EN"]


def missing(value):
    return value is None or str(value).strip() in ("", "-99", "-099")


def sha256(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def read_country_info(path):
    rows = {}
    with open(path, encoding="utf-8") as f:
        for line in f:
            if line.startswith("#") or not line.strip():
                continue
            cols = line.rstrip("\n").split("\t")
            rows[cols[0]] = {"iso3": cols[1], "isoNumeric": cols[2], "fips": cols[3], "name": cols[4]}
    return rows


def perpendicular(p, a, b):
    """The distance of p from segment a-b, in degrees."""
    (x, y), (x1, y1), (x2, y2) = p, a, b
    dx, dy = x2 - x1, y2 - y1
    if dx == 0 and dy == 0:
        return ((x - x1) ** 2 + (y - y1) ** 2) ** 0.5
    t = max(0.0, min(1.0, ((x - x1) * dx + (y - y1) * dy) / (dx * dx + dy * dy)))
    return ((x - (x1 + t * dx)) ** 2 + (y - (y1 + t * dy)) ** 2) ** 0.5


def douglas_peucker(points, tolerance):
    """Iterative Douglas-Peucker simplification of an open polyline."""
    if len(points) < 3:
        return points
    keep = [False] * len(points)
    keep[0] = keep[-1] = True
    stack = [(0, len(points) - 1)]
    while stack:
        first, last = stack.pop()
        best, index = 0.0, None
        for i in range(first + 1, last):
            d = perpendicular(points[i], points[first], points[last])
            if d > best:
                best, index = d, i
        if index is not None and best > tolerance:
            keep[index] = True
            stack.append((first, index))
            stack.append((index, last))
    return [p for p, k in zip(points, keep) if k]


def simplify_ring(ring, tolerance):
    """A closed ring simplified and rounded to 0.01 degrees; small rings keep their rounded vertices."""
    simplified = douglas_peucker(ring[:-1], tolerance) if len(ring) > 8 else ring[:-1]
    out = []
    for x, y in simplified:
        p = [round(x, 2), round(y, 2)]
        if not out or out[-1] != p:
            out.append(p)
    if len(out) < 3:
        out = []
        for x, y in ring[:-1]:
            p = [round(x, 4), round(y, 4)]
            if not out or out[-1] != p:
                out.append(p)
    if len(out) < 3:
        return None
    out.append(out[0])
    return out


def simplify(geometry, tolerance=0.01):
    polygons = geometry["coordinates"] if geometry["type"] == "MultiPolygon" else [geometry["coordinates"]]
    result = []
    for polygon in polygons:
        shell = simplify_ring(polygon[0], tolerance)
        if shell is None:
            continue
        holes = [h for h in (simplify_ring(ring, tolerance) for ring in polygon[1:]) if h]
        result.append([shell] + holes)
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--admin0", required=True)
    parser.add_argument("--country-info", required=True)
    parser.add_argument("--out", required=True)
    parser.add_argument("--edition", default="v5.1.2")
    args = parser.parse_args()

    info = read_country_info(args.country_info)
    features = json.load(open(args.admin0, encoding="utf-8"))["features"]

    best = {}
    for feature in features:
        p = feature["properties"]
        iso2 = p.get("ISO_A2_EH")
        if missing(iso2) or len(iso2) != 2:
            continue
        rank = (1 if p.get("HOMEPART") == 1 else 0, p.get("POP_EST") or 0)
        if iso2 not in best or rank > best[iso2][0]:
            best[iso2] = (rank, p)

    out = pathlib.Path(args.out)
    out.mkdir(parents=True, exist_ok=True)
    buffer = io.StringIO()
    buffer.write("# Generated by scripts/build-countries.py from Natural Earth ne_10m_admin_0_countries "
                 f"{args.edition} (sha256 {sha256(args.admin0)}, public domain) and GeoNames countryInfo.txt "
                 f"(sha256 {sha256(args.country_info)}, CC BY 4.0, https://www.geonames.org/)\n")
    writer = csv.writer(buffer, lineterminator="\n")
    writer.writerow(["iso2", "iso3", "isoNumeric", "fips", "name", "nameLong", "continent", "labelLon", "labelLat",
                     "aliases"])
    for iso2 in sorted(best):
        p = best[iso2][1]
        gn = info.get(iso2, {})
        iso3 = p.get("ISO_A3_EH") if not missing(p.get("ISO_A3_EH")) else gn.get("iso3")
        numeric = p.get("ISO_N3_EH") if not missing(p.get("ISO_N3_EH")) else gn.get("isoNumeric")
        if numeric is not None and numeric.strip("0") == "":
            numeric = None
        fips = p.get("FIPS_10") if not missing(p.get("FIPS_10")) else (gn.get("fips") or None)
        if missing(iso3):
            raise SystemExit(f"{iso2} has no ISO3 code in either input")
        ne_names = [str(p[field]).strip() for field in NE_NAME_FIELDS if not missing(p.get(field))]
        aliases = []
        for alias in ne_names + ([gn["name"]] if gn.get("name") else []) + VARIANTS.get(iso2, []):
            if alias and alias not in aliases and alias != p["NAME"] and "|" not in alias:
                aliases.append(alias)
        writer.writerow([iso2, iso3, numeric or "", fips or "", p["NAME"], p.get("NAME_LONG") or "",
                         p.get("CONTINENT") or "", f"{p['LABEL_X']:.6f}", f"{p['LABEL_Y']:.6f}", "|".join(aliases)])
    (out / "countries.csv").write_text(buffer.getvalue(), encoding="utf-8")

    # Simplified polygons: the 10m boundaries, Douglas-Peucker at 0.01 degrees, rounded to 0.01 degrees.
    polygons = {}
    for feature in features:
        iso2 = feature["properties"].get("ISO_A2_EH")
        if missing(iso2) or len(iso2) != 2 or iso2 not in best:
            continue
        polygons.setdefault(iso2, []).extend(simplify(feature["geometry"]))
    collection = {"type": "FeatureCollection", "features": [
        {"type": "Feature", "properties": {"iso2": iso2},
         "geometry": {"type": "MultiPolygon", "coordinates": polygons[iso2]}}
        for iso2 in sorted(polygons) if polygons[iso2]]}
    data = json.dumps(collection, separators=(",", ":")).encode("utf-8")
    with gzip.GzipFile(out / "countries-simple.geojson.gz", "wb", mtime=0) as gz:
        gz.write(data)
    print(f"{len(best)} countries, {len(collection['features'])} simplified polygons")


if __name__ == "__main__":
    main()
