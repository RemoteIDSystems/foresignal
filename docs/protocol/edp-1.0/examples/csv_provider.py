#!/usr/bin/env python3
"""Reference EDP 1.0 provider: serves the observations of a CSV (columns series_key,period,value[,known_at]).

push:  csv_provider.py push FILE BASE_URL INGEST_KEY   (POST batches to Foresignal; register MANIFEST first)
serve: csv_provider.py serve FILE PORT                 (Foresignal pulls GET /edp/items?cursor=&limit=)
Standard library only; documentation, not part of the server. See docs/External-Data-Protocol.md.
"""
import csv, json, sys, urllib.request
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import parse_qs, urlparse

SOURCE, STREAM, KIND = "csv-example", "values", "csv-value"
MANIFEST = {"protocol": "foresignal-edp/1.0",
            "source": {"id": SOURCE, "name": "CSV example", "publisher": {"name": "Operator"},
                       "originator": {"id": SOURCE, "class": "operator"}, "access": {"route": "other"}},
            "transport": {"push": True, "pull": {"itemsUrl": "http://localhost:8099/edp/items", "intervalSeconds": 300}},
            "streams": [{"id": STREAM, "mode": "atomic", "itemType": "observation", "kind": KIND, "geometry": "none",
                         "suggestedBindings": [{"target": "series"}]}]}

def now():
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")

def items(path):
    with open(path, newline="") as f:
        for row in csv.DictReader(f):
            item = {"stream": STREAM, "type": "observation", "id": f"{row['series_key']}:{row['period']}",
                    "observedAt": now(), "seriesKey": row["series_key"], "t": row["period"],
                    "value": float(row["value"]) if row["value"] else None}
            if row.get("known_at"):
                item["knownAt"] = row["known_at"]
            yield item

def batch(page, cursor=None):
    return {"protocol": "foresignal-edp/1.0", "sourceId": SOURCE, "sentAt": now(), "items": page, "nextCursor": cursor}

def push(path, base, key, size=500):
    rows = list(items(path))
    for i in range(0, len(rows), size):
        req = urllib.request.Request(f"{base}/v1/external/sources/ext-{SOURCE}/items", method="POST",
                                     data=json.dumps(batch(rows[i:i + size])).encode(),
                                     headers={"Content-Type": "application/json", "X-Foresignal-Ingest-Key": key})
        with urllib.request.urlopen(req) as resp:
            print(resp.status, json.load(resp).get("accepted"))

def serve(path, port):
    class Handler(BaseHTTPRequestHandler):
        def do_GET(self):
            url, q = urlparse(self.path), parse_qs(urlparse(self.path).query)
            if url.path == "/.well-known/foresignal-edp.json":
                body = MANIFEST
            else:
                start, limit = int(q.get("cursor", ["0"])[0] or 0), min(int(q.get("limit", ["500"])[0]), 10000)
                rows = list(items(path))[start:start + limit]
                body = batch(rows, str(start + len(rows)) if len(rows) == limit else None)
            data = json.dumps(body).encode()
            self.send_response(200); self.send_header("Content-Type", "application/json"); self.end_headers()
            self.wfile.write(data)
    HTTPServer(("", port), Handler).serve_forever()

if __name__ == "__main__":
    push(*sys.argv[2:5]) if sys.argv[1] == "push" else serve(sys.argv[2], int(sys.argv[3]))
