"""Regenerate the vendored Mobility Database GTFS-RT snapshot of the transit-* sources (prompt 070).

Usage:
    python3 scripts/update-mobility-catalog.py [feeds_v2.csv path or URL]

Without an argument it downloads https://files.mobilitydatabase.org/feeds_v2.csv (MobilityData's catalog
export; the metadata is CC0, schema https://github.com/MobilityData/mobility-feed-api/blob/main/docs/
SpreadsheetSchemaV2.md). The legacy bit.ly/catalogs-csv sources.csv (mdb_source_id) is deprecated and is
not read.

Keeps the rows with data_type = gtfs_rt, status = active, urls.authentication_type = 0 (no key) and an
https urls.direct_download, and only the columns Foresignal uses:
    id, provider, location.country_code, entity_type, urls.direct_download, urls.license, static_reference

Writes src/main/resources/transit/mobility-gtfs-rt.csv (sorted by id) and updates the snapshot date in
src/main/resources/transit/README.md. Run it at development time only; Foresignal never downloads the
catalog at runtime, and URLs are never accepted from an API caller.
"""
import csv
import datetime
import io
import pathlib
import re
import sys
import urllib.request

SOURCE_URL = "https://files.mobilitydatabase.org/feeds_v2.csv"
COLUMNS = ["id", "provider", "location.country_code", "entity_type", "urls.direct_download", "urls.license",
           "static_reference"]
ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "src/main/resources/transit/mobility-gtfs-rt.csv"
README = ROOT / "src/main/resources/transit/README.md"
USER_AGENT = "Foresignal/dev (+https://foresignal.ai; update-mobility-catalog.py)"


def read_source(arg):
    """The feeds_v2.csv text from a local path or an https URL."""
    if arg is None or arg.startswith("https://"):
        url = arg or SOURCE_URL
        request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
        with urllib.request.urlopen(request, timeout=120) as response:
            return response.read().decode("utf-8")
    return pathlib.Path(arg).read_text(encoding="utf-8")


def keep(row):
    """True for an active, keyless GTFS-RT feed with an https direct download."""
    return (row.get("data_type") == "gtfs_rt"
            and row.get("status") == "active"
            and row.get("urls.authentication_type", "").strip() == "0"
            and row.get("urls.direct_download", "").strip().lower().startswith("https://"))


def main():
    text = read_source(sys.argv[1] if len(sys.argv) > 1 else None)
    reader = csv.DictReader(io.StringIO(text))
    missing = [c for c in COLUMNS + ["data_type", "status", "urls.authentication_type"]
               if c not in (reader.fieldnames or [])]
    if missing:
        sys.exit("feeds_v2.csv lacks the columns " + ", ".join(missing) + "; check the schema before vendoring")
    rows = sorted((r for r in reader if keep(r)), key=lambda r: r["id"])
    OUT.parent.mkdir(parents=True, exist_ok=True)
    with OUT.open("w", encoding="utf-8", newline="") as out:
        writer = csv.writer(out, lineterminator="\n")
        writer.writerow(COLUMNS)
        for row in rows:
            writer.writerow([row.get(c, "").strip() for c in COLUMNS])
    today = datetime.date.today().isoformat()
    if README.exists():
        readme = README.read_text(encoding="utf-8")
        readme = re.sub(r"(\*\*Snapshot date:\*\* )\d{4}-\d{2}-\d{2}", r"\g<1>" + today, readme)
        readme = re.sub(r"(\*\*Rows:\*\* )\d+", r"\g<1>" + str(len(rows)), readme)
        README.write_text(readme, encoding="utf-8")
    print(f"{len(rows)} feeds written to {OUT.relative_to(ROOT)} ({today})")


if __name__ == "__main__":
    main()
