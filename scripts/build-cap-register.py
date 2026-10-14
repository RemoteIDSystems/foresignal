"""Generate the CAP-feed candidate list for the CAP family (prompt 089).

Usage:
  python3 -I scripts/build-cap-register.py --out build/cap-candidates.json

Input (fetched live, never committed):
  * the WMO Register of Alerting Authorities Atom index, https://alertingauthority.wmo.int/atom.xml
    (one <entry> per authority, with <iso:countrycode> and a link to its record page), and
  * each authority's record page, https://alertingauthority.wmo.int/authorities.php?recId=<n>,
    whose "CAP Feed URL(s):" section lists a Language/URL pair per published CAP feed.

Output: build/cap-candidates.json, a JSON object {"fetchedOn", "registerUpdated", "authorities": [...]}.
Each authority has country, countryCode, authority, recordUrl and feeds: [{language, url}]. The file is a
review list only; the runtime never reads the register (it loads the committed, human-reviewed
src/main/resources/sources/cap-feeds.json instead).

This script is a build-time convenience. It uses only the Python standard library.
"""
import argparse
import html
import json
import re
import urllib.request
from datetime import datetime, timezone

ATOM = "https://alertingauthority.wmo.int/atom.xml"
RECORD = "https://alertingauthority.wmo.int/authorities.php?recId={id}"
USER_AGENT = "Foresignal-cap-register-review/1.0 (+https://foresignal.ai)"

ENTRY_RE = re.compile(r"<entry>(.*?)</entry>", re.S)
COUNTRY_RE = re.compile(r"<iso:countrycode>([^<]*)</iso:countrycode>")
TITLE_RE = re.compile(r"<title>([^<]*)</title>", re.S)
RECID_RE = re.compile(r"authorities\.php\?recId=(\d+)")
# A Language/URL pair in the record page's "CAP Feed URL(s):" section.
LANG_RE = re.compile(r"Language:\s*<span[^>]*>([^<]*)</span>", re.S)
URL_RE = re.compile(r"URL:\s*<span[^>]*>\s*<a\s+href=\"([^\"]+)\"", re.S)


def fetch(url):
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=30) as response:
        return response.read().decode("utf-8", "replace")


def records(page):
    """The (language, url) CAP feeds listed in one authority record page."""
    section = page.split("CAP Feed URL(s):", 1)
    if len(section) < 2:
        return []
    # The section ends at the next heading ("Forecasts URL:" or similar) or the end.
    body = re.split(r"Forecasts URL:", section[1], maxsplit=1)[0]
    languages = LANG_RE.findall(body)
    urls = URL_RE.findall(body)
    feeds = []
    for index, url in enumerate(urls):
        language = html.unescape(languages[index]).strip() if index < len(languages) else ""
        feeds.append({"language": language, "url": html.unescape(url).strip()})
    return feeds


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--out", default="build/cap-candidates.json", help="the JSON file to write")
    args = parser.parse_args()

    atom = fetch(ATOM)
    updated = re.search(r"<updated>([^<]*)</updated>", atom)
    authorities = []
    for block in ENTRY_RE.findall(atom):
        rec_id = RECID_RE.search(block)
        if not rec_id:
            continue
        title = html.unescape(TITLE_RE.search(block).group(1)).strip() if TITLE_RE.search(block) else ""
        country, _, authority = title.partition(":")
        country_code = COUNTRY_RE.search(block)
        record_url = RECORD.format(id=rec_id.group(1))
        try:
            page = fetch(record_url)
            feeds = records(page)
        except Exception as error:  # noqa: BLE001 - a record that cannot be read is skipped, not fatal
            print(f"warn: {record_url}: {error}", flush=True)
            feeds = []
        authorities.append({
            "country": country.strip(),
            "countryCode": (country_code.group(1) if country_code else "").strip(),
            "authority": authority.strip() or title.strip(),
            "recordUrl": record_url,
            "feeds": feeds,
        })

    document = {
        "fetchedOn": datetime.now(timezone.utc).strftime("%Y-%m-%d"),
        "registerUpdated": updated.group(1) if updated else None,
        "authorities": authorities,
    }
    with open(args.out, "w", encoding="utf-8") as out:
        json.dump(document, out, indent=2, ensure_ascii=False)
        out.write("\n")
    with_feeds = sum(1 for a in authorities if a["feeds"])
    print(f"wrote {args.out}: {len(authorities)} authorities, {with_feeds} with CAP feeds")


if __name__ == "__main__":
    main()
