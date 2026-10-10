"""Generate the Postman v2.1 collection and local environment from docs/OpenAPI.json.

Usage: python3 scripts/generate-postman.py docs/OpenAPI.json postman
The collection is derived, never hand-edited. Regenerate it whenever docs/OpenAPI.json changes.
Request bodies come from BODIES; ID capture scripts come from CAPTURE.
"""
import json, sys, uuid, re, pathlib

spec = json.load(open(sys.argv[1]))
outdir = pathlib.Path(sys.argv[2]); outdir.mkdir(parents=True, exist_ok=True)

PATH_VARS = {"sourceId": "sourceId", "externalId": "externalId", "ruleId": "ruleId", "alertId": "alertId",
             "webhookId": "webhookId", "layer": "tileLayer", "z": "tileZ", "x": "tileX", "y": "tileY"}

BODIES = {
  "updateSource": {"enabled": True, "interval": "60s", "params": {"feed": "all_day"}, "budget": {"perMinute": 4, "perDay": 2000}, "retention": "365d"},
  "setSourceCredentials": {"values": {"apiKey": "REPLACE_ME"}},
  "createTrigger": {"name": "Big quake", "enabled": True, "source": "usgs-earthquakes", "kind": "earthquake",
                    "conditions": [{"path": "mag", "op": ">=", "value": 6}], "mode": "enter", "cooldown": "0s",
                    "messageTemplate": "M{{payload.mag}} — {{payload.place}}"},
  "updateTrigger": {"name": "Vessels entering Strait of Hormuz", "enabled": True, "kind": "vessel", "mode": "enter",
                    "geofence": {"type": "Polygon", "coordinates": [[[55.8, 26.0], [57.2, 26.0], [57.2, 27.0], [55.8, 27.0], [55.8, 26.0]]]},
                    "cooldown": "1h", "messageTemplate": "{{payload.name}} ({{externalId}}) entered Hormuz"},
  "createWebhook": {"name": "ntfy", "url": "https://ntfy.sh/REPLACE_TOPIC", "secret": "change-me", "ruleIds": None, "enabled": True},
  "updateWebhook": {"name": "ntfy", "url": "https://ntfy.sh/REPLACE_TOPIC", "ruleIds": None, "enabled": True},
  "updateRetention": {"default": "7d", "alerts": "90d", "usage": "30d", "sources": {"usgs-earthquakes": "3650d"}},
  "updateCors": {"readOrigins": ["*"], "writeOrigins": ["http://localhost:5173"]},
  "importSettings": {"sources": {"usgs-earthquakes": {"enabled": True, "interval": "60s"}},
                     "retention": {"default": "7d", "alerts": "90d", "usage": "30d", "sources": {}},
                     "cors": {"readOrigins": ["*"], "writeOrigins": []}, "triggers": [], "webhooks": []},
}

# test scripts: capture ids / assert status
CAPTURE = {
  "createTrigger": "const j = pm.response.json(); pm.collectionVariables.set('ruleId', j.id);",
  "createWebhook": "const j = pm.response.json(); pm.collectionVariables.set('webhookId', j.id);",
  "listAlerts": "const j = pm.response.json(); if (j.items && j.items.length) pm.collectionVariables.set('alertId', j.items[0].id);",
  "listChanges": "const j = pm.response.json(); pm.collectionVariables.set('lastSeq', j.lastSeq);",
  "listRecords": "const j = pm.response.json(); const it = (j.items || j.features || [])[0]; if (it) { const ext = it.externalId || (it.properties && it.properties.externalId); pm.collectionVariables.set('externalId', ext); }\nif (j.nextCursor) pm.collectionVariables.set('cursor', j.nextCursor);",
  "rotateAdminToken": "const j = pm.response.json(); pm.collectionVariables.set('adminToken', j.token); console.log('New admin token stored in collection variable adminToken');",
}

QUERY_DEFAULTS = {"after": "{{lastSeq}}", "cursor": "{{cursor}}", "source": "{{sourceId}}", "ruleId": "{{ruleId}}"}

def example_for(schema):
    t = schema.get("type"); t = t[0] if isinstance(t, list) else t
    if "default" in schema: return schema["default"]
    if "enum" in schema: return schema["enum"][0]
    if t == "array": return ""
    if schema.get("format") == "date-time": return "2026-10-10T00:00:00Z"
    return {"integer": "", "number": "", "boolean": "false"}.get(t, "")

def req_item(path, method, op):
    opid = op["operationId"]
    admin = bool(op.get("security"))
    pm_path = re.sub(r"\{(\w+)\}", lambda m: "{{" + PATH_VARS.get(m.group(1), m.group(1)) + "}}", path)
    raw = "{{baseUrl}}" + pm_path
    query = []
    for p in op.get("parameters", []):
        if p["in"] == "query":
            v = QUERY_DEFAULTS.get(p["name"], example_for(p.get("schema", {})))
            query.append({"key": p["name"], "value": str(v).lower() if isinstance(v, bool) else str(v),
                          "description": p.get("description", ""), "disabled": not p.get("required", False)})
    headers = []
    for p in op.get("parameters", []):
        if p["in"] == "header":
            headers.append({"key": p["name"], "value": "{{lastSeq}}" if "Last-Event" in p["name"] else "",
                            "description": p.get("description", ""), "disabled": True})
    accept = None
    for code, r in op.get("responses", {}).items():
        if code.startswith("2") and "content" in r:
            ctypes = list(r["content"])
            accept = "text/event-stream" if "text/event-stream" in ctypes else ctypes[0]
            break
    if accept: headers.append({"key": "Accept", "value": accept})
    request = {"method": method.upper(), "header": headers,
               "url": {"raw": raw + ("?" + "&".join(f"{q['key']}={q['value']}" for q in query if not q["disabled"]) if any(not q["disabled"] for q in query) else ""),
                       "host": ["{{baseUrl}}"], "path": [s for s in pm_path.strip("/").split("/")], "query": query},
               "description": (op.get("description") or op.get("summary", "")) + ("\n\n**Requires admin token.**" if admin else "\n\nOpen (no auth).") + f"\n\noperationId: `{opid}`"}
    if admin:
        request["auth"] = {"type": "bearer", "bearer": [{"key": "token", "value": "{{adminToken}}", "type": "string"}]}
    else:
        request["auth"] = {"type": "noauth"}
    if "requestBody" in op:
        body = BODIES.get(opid, {})
        request["header"].append({"key": "Content-Type", "value": "application/json"})
        request["body"] = {"mode": "raw", "raw": json.dumps(body, indent=2), "options": {"raw": {"language": "json"}}}
    events = []
    ok = sorted(c for c in op.get("responses", {}) if c.startswith("2"))
    if accept != "text/event-stream":
        script = f"pm.test('status is {'/'.join(ok)}', () => pm.expect([{', '.join(ok)}]).to.include(pm.response.code));"
        if opid in CAPTURE: script += "\nif (pm.response.code < 300) {\n" + CAPTURE[opid] + "\n}"
        events.append({"listen": "test", "script": {"type": "text/javascript", "exec": script.split("\n")}})
    return {"name": f"{op.get('summary', opid)}", "event": events, "request": request,
            "_opid": opid, "_tag": op.get("tags", ["Other"])[0]}

items = []
for path, ops in spec["paths"].items():
    for method, op in ops.items():
        if method == "parameters": continue
        items.append(req_item(path, method, op))

folders = []
for tag in [t["name"] for t in spec["tags"]]:
    desc = next(t.get("description", "") for t in spec["tags"] if t["name"] == tag)
    folder_items = [dict((k, v) for k, v in i.items() if not k.startswith("_")) for i in items if i["_tag"] == tag]
    folders.append({"name": tag, "description": desc, "item": folder_items})

by_id = {i["_opid"]: dict((k, v) for k, v in i.items() if not k.startswith("_")) for i in items}
def step(n, opid, name=None, overrides=None):
    it = json.loads(json.dumps(by_id[opid])); it["name"] = f"{n:02d} · " + (name or it["name"])
    if overrides:
        if "body" in overrides: it["request"]["body"]["raw"] = json.dumps(overrides["body"], indent=2)
        if "query" in overrides:
            for q in it["request"]["url"]["query"]:
                if q["key"] in overrides["query"]: q["value"] = overrides["query"][q["key"]]; q["disabled"] = False
            it["request"]["url"]["raw"] = it["request"]["url"]["raw"].split("?")[0] + "?" + "&".join(
                f"{q['key']}={q['value']}" for q in it["request"]["url"]["query"] if not q["disabled"])
    return it

quickstart = {"name": "00 Quickstart — USGS end to end",
  "description": "Run in order (Collection Runner works). Set `adminToken` first (from `docker compose logs elaro | grep -A3 \"ELARO ADMIN TOKEN\"`). Enables USGS earthquakes, creates an enter-mode M≥4.5 rule, waits, and reads data back. SSE requests are left in the Streams folder (run them manually; Postman shows events live).",
  "item": [
    step(1, "getSystem", "System status"),
    step(2, "listSources", "List sources"),
    step(3, "updateSource", "Enable USGS earthquakes", {"body": {"enabled": True, "params": {"feed": "all_day"}}}),
    step(4, "createTrigger", "Create rule: M≥4.5 (enter)", {"body": {"name": "M4.5+", "source": "usgs-earthquakes", "conditions": [{"path": "mag", "op": ">=", "value": 4.5}], "mode": "enter", "messageTemplate": "M{{payload.mag}} — {{payload.place}}"}}),
    step(5, "fetchSourceNow", "Fetch now"),
    step(6, "getSourceHealth", "Check health"),
    step(7, "listRecords", "Current quakes (GeoJSON)", {"query": {"source": "usgs-earthquakes", "format": "geojson", "limit": "50"}}),
    step(8, "listRecordVersions", "Version history of first quake"),
    step(9, "listChanges", "Change feed from 0", {"query": {"after": "0", "limit": "100"}}),
    step(10, "testTrigger", "Dry-run the rule over last 24h"),
    step(11, "listAlerts", "Alerts"),
    step(12, "getSourceUsage", "Budget / usage"),
  ]}
# Streams convenience folder
streams = {"name": "Streams (SSE — run manually)", "description": "Postman displays Server-Sent Events live. Set Last-Event-ID (header) to resume.",
           "item": [dict(by_id["streamChanges"], name="Change stream — all"), dict(by_id["streamAlerts"], name="Alert stream")]}

collection = {
  "info": {"_postman_id": str(uuid.uuid5(uuid.NAMESPACE_URL, "https://elaro.ai/postman")), "name": "Elaro API",
           "description": f"{spec['info']['description']}\n\nGenerated from docs/OpenAPI.json (v{spec['info']['version']}). Every operation is included, grouped by tag. Admin operations use Bearer `{{{{adminToken}}}}`; open reads use no auth. Test scripts capture ids into collection variables (ruleId, webhookId, alertId, externalId, lastSeq, cursor).",
           "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"},
  "item": [quickstart, streams] + folders,
  "variable": [{"key": k, "value": v} for k, v in [
      ("baseUrl", "http://localhost:8080"), ("adminToken", ""), ("sourceId", "usgs-earthquakes"), ("externalId", ""),
      ("ruleId", ""), ("alertId", ""), ("webhookId", ""), ("lastSeq", "0"), ("cursor", ""),
      ("tileLayer", "earthquake"), ("tileZ", "0"), ("tileX", "0"), ("tileY", "0")]],
}
env = {"id": str(uuid.uuid5(uuid.NAMESPACE_URL, "https://elaro.ai/postman/env/local")), "name": "Elaro — Local",
       "values": [{"key": "baseUrl", "value": "http://localhost:8080", "type": "default", "enabled": True},
                  {"key": "adminToken", "value": "", "type": "secret", "enabled": True}],
       "_postman_variable_scope": "environment"}
(outdir / "Elaro.postman_collection.json").write_text(json.dumps(collection, indent=2))
(outdir / "Elaro-Local.postman_environment.json").write_text(json.dumps(env, indent=2))
ops = {i["_opid"] for i in items}
print(len(items), "requests across", len(folders), "tag folders; quickstart", len(quickstart["item"]), "steps; covers all ops:", ops == {op["operationId"] for p in spec["paths"].values() for m, op in p.items() if m != "parameters"})
