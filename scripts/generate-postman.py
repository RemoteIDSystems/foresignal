"""Generate the Postman v2.1 collection and local environment from docs/OpenAPI.json.

Usage: python3 scripts/generate-postman.py docs/OpenAPI.json postman
The collection is derived, never hand-edited. Regenerate it whenever docs/OpenAPI.json changes.
Request bodies come from BODIES; ID capture scripts come from CAPTURE.
"""
import json, sys, uuid, re, pathlib

spec = json.load(open(sys.argv[1]))
outdir = pathlib.Path(sys.argv[2]); outdir.mkdir(parents=True, exist_ok=True)

PATH_VARS = {"sourceId": "sourceId", "externalId": "externalId", "ruleId": "ruleId", "alertId": "alertId",
             "channelId": "channelId", "tokenId": "tokenId", "providerId": "providerId", "threadId": "threadId",
             "actionId": "actionId", "briefingId": "briefingId", "runId": "runId", "subscriptionId": "subscriptionId",
             "layer": "tileLayer", "z": "tileZ", "x": "tileX", "y": "tileY"}

BODIES = {
  "updateSource": {"enabled": True, "interval": "60s", "params": {"feed": "all_day"}, "budget": {"perMinute": 4, "perDay": 2000}, "retention": "365d"},
  "setSourceCredentials": {"values": {"apiKey": "REPLACE_ME"}},
  "createTrigger": {"name": "Big quake", "enabled": True, "source": "usgs-earthquakes", "kind": "earthquake",
                    "conditions": [{"path": "mag", "op": ">=", "value": 6}], "mode": "enter", "cooldown": "0s", "severity": "critical", "enrich": True,
                    "messageTemplate": "M{{payload.mag}} — {{payload.place}}"},
  "updateTrigger": {"name": "Vessels entering Strait of Hormuz", "enabled": True, "kind": "vessel", "mode": "enter",
                    "geofence": {"type": "Polygon", "coordinates": [[[55.8, 26.0], [57.2, 26.0], [57.2, 27.0], [55.8, 27.0], [55.8, 26.0]]]},
                    "cooldown": "1h", "messageTemplate": "{{payload.name}} ({{externalId}}) entered Hormuz"},
  "createChannel": {"name": "ntfy phone", "type": "ntfy", "config": {"serverUrl": "https://ntfy.sh", "topic": "REPLACE_TOPIC"},
                    "secrets": {}, "events": ["alert", "briefing", "system"], "ruleIds": None, "minSeverity": "warning", "enabled": True},
  "updateChannel": {"name": "Slack #ops", "type": "slack", "config": {"username": "Foresignal"},
                    "secrets": {"webhookUrl": "https://hooks.slack.com/services/REPLACE"}, "events": ["alert", "briefing"], "ruleIds": None,
                    "minSeverity": "info", "waitForEnrichmentSeconds": 20, "enabled": True},
  "createToken": {"name": "openclaw-agent", "scope": "agent", "expiresAt": None},
  "createCustomSource": {"id": "custom-home-sensors", "name": "Home sensors", "kinds": ["sensor-reading"], "attribution": "My bench",
                         "recordTtl": "1h", "tileProperties": ["value", "unit"], "searchFields": ["name"], "embedFields": [], "rateLimitPerMinute": 60},
  "updateCustomSource": {"id": "custom-home-sensors", "name": "Home sensors", "kinds": ["sensor-reading", "sensor-alarm"], "attribution": "My bench",
                         "recordTtl": "1h", "tileProperties": ["value", "unit"], "searchFields": ["name"], "embedFields": [], "rateLimitPerMinute": 60},
  "ingestRecords": {"type": "FeatureCollection", "features": [{"type": "Feature", "id": "garage-temp",
                    "geometry": {"type": "Point", "coordinates": [-97.74, 30.27]},
                    "properties": {"kind": "sensor-reading", "observedAt": "2026-10-10T12:00:00Z", "name": "Garage temperature", "value": 31.5, "unit": "C"}}]},
  "createLlmProvider": {"id": "ollama", "type": "ollama", "baseUrl": "http://ollama:11434", "chatModel": "qwen3:14b",
                        "embeddingModel": "nomic-embed-text", "embeddingDimensions": 768, "requestsPerMinute": 60, "tokensPerDay": 5000000, "enabled": True},
  "updateLlmProvider": {"id": "claude", "type": "anthropic", "chatModel": "claude-opus-5-5", "apiKey": "REPLACE_ME",
                        "options": {"effort": "medium", "maxTokens": 16000}, "requestsPerMinute": 30, "tokensPerDay": 2000000, "enabled": True},
  "updateLlmSettings": {"chatProvider": "claude", "embeddingProvider": "ollama", "askEnabled": True, "enrichmentEnabled": True, "logContent": False},
  "ask": {"question": "Were there any earthquakes above magnitude 5 in the last 24 hours, and where?", "threadId": None,
          "scope": {"sources": ["usgs-earthquakes"], "since": None}, "allowActions": False},
  "askStream": {"question": "Summarize significant seismic activity this week and suggest a watch rule for M6+ near Japan.", "allowActions": False},
  "createBriefing": {"name": "Morning brief", "enabled": True, "schedule": "0 7 * * *", "timezone": "America/Chicago",
                     "scope": {"sources": [], "kinds": ["earthquake"], "includeHealth": True}, "lookback": "24h", "mode": "digest",
                     "instructions": None, "channelIds": ["{{channelId}}"], "maxItemsPerKind": 5},
  "updateBriefing": {"name": "Morning brief (LLM)", "enabled": True, "schedule": "0 7 * * 1-5", "timezone": "America/Chicago",
                     "scope": {"kinds": ["earthquake", "aircraft", "vessel"], "includeHealth": True}, "lookback": "24h", "mode": "llm",
                     "instructions": "Lead with anything critical. Keep it under 250 words.", "channelIds": ["{{channelId}}"]},
  "previewBriefing": {"name": "Preview", "schedule": "0 7 * * *", "scope": {"kinds": ["earthquake"]}, "lookback": "24h", "mode": "digest"},
  "createPushSubscription": {"name": "Laptop browser", "subscription": {"endpoint": "https://push.example/REPLACE",
                             "keys": {"p256dh": "REPLACE", "auth": "REPLACE"}}, "filters": {"events": ["alert"], "minSeverity": "warning"}},
  "updateSecuritySettings": {"openReads": True},
  "updateServerSettings": {"publicBaseUrl": "http://foresignal.local:8080"},
  "updateRetention": {"default": "7d", "alerts": "90d", "usage": "30d", "briefings": "180d", "ask": "90d", "sources": {"usgs-earthquakes": "3650d"}},
  "updateCors": {"readOrigins": ["*"], "writeOrigins": ["http://localhost:5173"]},
  "importSettings": {"sources": {"usgs-earthquakes": {"enabled": True, "interval": "60s"}},
                     "retention": {"default": "7d", "alerts": "90d", "usage": "30d", "sources": {}},
                     "cors": {"readOrigins": ["*"], "writeOrigins": []}, "triggers": [], "channels": []},
}

# test scripts: capture ids / assert status
CAPTURE = {
  "createTrigger": "const j = pm.response.json(); pm.collectionVariables.set('ruleId', j.id);",
  "createChannel": "const j = pm.response.json(); pm.collectionVariables.set('channelId', j.id);",
  "createToken": "const j = pm.response.json(); pm.collectionVariables.set('tokenId', j.id); console.log('New token (shown once):', j.token);",
  "createCustomSource": "const j = pm.response.json(); pm.collectionVariables.set('ingestKey', j.ingestKey); pm.collectionVariables.set('sourceId', j.id);",
  "rotateIngestKey": "const j = pm.response.json(); pm.collectionVariables.set('ingestKey', j.ingestKey);",
  "createLlmProvider": "const j = pm.response.json(); pm.collectionVariables.set('providerId', j.id);",
  "ask": "const j = pm.response.json(); pm.collectionVariables.set('threadId', j.threadId); if (j.proposedActions && j.proposedActions.length) pm.collectionVariables.set('actionId', j.proposedActions[0].id);",
  "createBriefing": "const j = pm.response.json(); pm.collectionVariables.set('briefingId', j.id);",
  "runBriefing": "const j = pm.response.json(); if (j.id) pm.collectionVariables.set('runId', j.id);",
  "createPushSubscription": "const j = pm.response.json(); pm.collectionVariables.set('subscriptionId', j.id);",
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
    scope = op.get("x-foresignal-scope", "open")
    admin = scope in ("agent", "admin")
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
               "description": (op.get("description") or op.get("summary", "")) + f"\n\noperationId: `{opid}`"}
    if opid == "ingestRecords":
        request["auth"] = {"type": "noauth"}
        request["header"].append({"key": "X-Foresignal-Ingest-Key", "value": "{{ingestKey}}"})
    elif admin:
        var = "{{agentToken}}" if scope == "agent" else "{{adminToken}}"
        request["auth"] = {"type": "bearer", "bearer": [{"key": "token", "value": var, "type": "string"}]}
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
  "description": "Run in order (Collection Runner works). Set `adminToken` first (from `docker compose logs foresignal | grep -A3 \"FORESIGNAL ADMIN TOKEN\"`). Enables USGS earthquakes, creates an enter-mode M≥4.5 rule, waits, and reads data back. SSE requests are left in the Streams folder (run them manually; Postman shows events live).",
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
quickstart2 = {"name": "01 Quickstart — Channels, briefings, Ask",
  "description": "After the USGS quickstart: add an ntfy channel, create and run a digest briefing, register a local Ollama provider and ask a question. Edit the ntfy topic first.",
  "item": [
    step(1, "listChannelTypes", "Channel types"),
    step(2, "createChannel", "Create ntfy channel"),
    step(3, "testChannel", "Send test notification"),
    step(4, "createBriefing", "Create morning digest"),
    step(5, "runBriefing", "Run it now"),
    step(6, "createLlmProvider", "Add local Ollama provider"),
    step(7, "updateLlmSettings", "Assign roles", {"body": {"chatProvider": "ollama", "embeddingProvider": "ollama", "askEnabled": True, "enrichmentEnabled": True}}),
    step(8, "testLlmProvider", "Test the provider"),
    step(9, "ask", "Ask about recent quakes"),
    step(10, "search", "Hybrid search", {"query": {"q": "earthquake", "mode": "hybrid"}}),
  ]}
# Streams convenience folder
streams = {"name": "Streams (SSE — run manually)", "description": "Postman displays Server-Sent Events live. Set Last-Event-ID (header) to resume.",
           "item": [dict(by_id["streamChanges"], name="Change stream — all"), dict(by_id["streamAlerts"], name="Alert stream"),
                    dict(by_id["askStream"], name="Ask (streaming)")]}

collection = {
  "info": {"_postman_id": str(uuid.uuid5(uuid.NAMESPACE_URL, "https://foresignal.ai/postman")), "name": "Foresignal API",
           "description": f"{spec['info']['description']}\n\nGenerated from docs/OpenAPI.json (v{spec['info']['version']}). Every operation is included, grouped by tag. Admin operations use Bearer `{{{{adminToken}}}}`, agent operations `{{{{agentToken}}}}` (defaults to the admin token; set it to an agent-scope token to test scopes), ingest uses `X-Foresignal-Ingest-Key: {{{{ingestKey}}}}`, and reads use no auth (open while openReads is on). Test scripts capture ids into collection variables (ruleId, channelId, alertId, briefingId, threadId, ingestKey, externalId, lastSeq, cursor…).",
           "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"},
  "item": [quickstart, quickstart2, streams] + folders,
  "variable": [{"key": k, "value": v} for k, v in [
      ("baseUrl", "http://localhost:8080"), ("adminToken", ""), ("agentToken", "{{adminToken}}"), ("ingestKey", ""),
      ("sourceId", "usgs-earthquakes"), ("externalId", ""), ("ruleId", ""), ("alertId", ""), ("channelId", ""), ("tokenId", ""),
      ("providerId", "ollama"), ("threadId", ""), ("actionId", ""), ("briefingId", ""), ("runId", ""), ("subscriptionId", ""),
      ("lastSeq", "0"), ("cursor", ""),
      ("tileLayer", "earthquake"), ("tileZ", "0"), ("tileX", "0"), ("tileY", "0")]],
}
env = {"id": str(uuid.uuid5(uuid.NAMESPACE_URL, "https://foresignal.ai/postman/env/local")), "name": "Foresignal — Local",
       "values": [{"key": "baseUrl", "value": "http://localhost:8080", "type": "default", "enabled": True},
                  {"key": "adminToken", "value": "", "type": "secret", "enabled": True}],
       "_postman_variable_scope": "environment"}
(outdir / "Foresignal.postman_collection.json").write_text(json.dumps(collection, indent=2))
(outdir / "Foresignal-Local.postman_environment.json").write_text(json.dumps(env, indent=2))
ops = {i["_opid"] for i in items}
print(len(items), "requests across", len(folders), "tag folders; quickstart", len(quickstart["item"]), "steps; covers all ops:", ops == {op["operationId"] for p in spec["paths"].values() for m, op in p.items() if m != "parameters"})
