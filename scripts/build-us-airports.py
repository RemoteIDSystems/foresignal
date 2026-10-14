"""Generate Foresignal's bundled US airport table (prompt 087).

Usage:
  python3 -I scripts/build-us-airports.py --airports airports.csv \
      --out src/main/resources/reference/us-airports.csv

Input (downloaded by hand, never committed):
  * OurAirports airports.csv, https://davidmegginson.github.io/ourairports-data/airports.csv
    (https://ourairports.com/data/). OurAirports releases its data to the public domain.

Output: us-airports.csv with the header "faa_lid,icao,name,lat,lon", one row per open airport (types
large_airport, medium_airport and small_airport) in the United States and its territories (iso_country US, PR,
GU, VI, AS, MP) that has an FAA location identifier (OurAirports "local_code"). "icao" is the ICAO code, else the
GPS code when it is four letters, else empty. Coordinates are rounded to 5 decimals (about 1 m). Rows are sorted by
the FAA identifier; a duplicate identifier keeps the larger airport type, then the first row.

The faa-nas-status adapter looks up the FAA identifier (ARPT) of each delay; an identifier missing from the table
gives a record without geometry.

Uses only the Python standard library.
"""
import argparse
import csv
import sys

COUNTRIES = {"US", "PR", "GU", "VI", "AS", "MP"}
TYPES = {"large_airport": 0, "medium_airport": 1, "small_airport": 2}


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--airports", required=True, help="OurAirports airports.csv")
    parser.add_argument("--out", required=True, help="the us-airports.csv to write")
    args = parser.parse_args()

    chosen = {}
    with open(args.airports, encoding="utf-8", newline="") as f:
        for row in csv.DictReader(f):
            if row["iso_country"] not in COUNTRIES or row["type"] not in TYPES:
                continue
            lid = row["local_code"].strip().upper()
            if not lid:
                continue
            try:
                lat = round(float(row["latitude_deg"]), 5)
                lon = round(float(row["longitude_deg"]), 5)
            except ValueError:
                continue
            icao = row.get("icao_code", "").strip().upper()
            gps = row["gps_code"].strip().upper()
            if not icao and len(gps) == 4 and gps.isalpha():
                icao = gps
            rank = TYPES[row["type"]]
            current = chosen.get(lid)
            if current is None or rank < current[0]:
                chosen[lid] = (rank, icao, " ".join(row["name"].split()), lat, lon)

    with open(args.out, "w", encoding="utf-8", newline="") as f:
        writer = csv.writer(f, lineterminator="\n")
        writer.writerow(["faa_lid", "icao", "name", "lat", "lon"])
        for lid in sorted(chosen):
            _, icao, name, lat, lon = chosen[lid]
            writer.writerow([lid, icao, name, f"{lat:.5f}".rstrip("0").rstrip("."), f"{lon:.5f}".rstrip("0").rstrip(".")])
    print(f"wrote {len(chosen)} airports to {args.out}", file=sys.stderr)


if __name__ == "__main__":
    main()
