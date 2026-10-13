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
             "layer": "tileLayer", "z": "tileZ", "x": "tileX", "y": "tileY", "pairingCodeId": "pairingCodeId",
             "topic": "sourceId", "format": "ingestFormat", "action": "ntfyAction", "deviceId": "pushDeviceId",
             # 033: action capabilities, inbox items and escalation policies.
             "token": "actionToken", "notificationId": "notificationId", "policyId": "escalationPolicyId",
             # 035: feed keys.
             "feedKeyId": "feedKeyId",
             # 050: tile keys.
             "tileKeyId": "tileKeyId",
             # 036: places, saved views and watchlists.
             "placeId": "placeId", "viewId": "viewId", "watchlistId": "watchlistId"}

# Path variables that mean something else under a prefix (024): EDP sources and registration tokens.
PATH_VARS_BY_PREFIX = [("/v1/external/sources/", {"sourceId": "edpSourceId"}),
                       ("/v1/external/registration-tokens/", {"tokenId": "registrationTokenId"}),
                       # Native push providers (032) are not the LLM providers of {{providerId}}.
                       ("/v1/push/providers/", {"providerId": "pushProviderId"}),
                       # 039: ingest devices are not the push devices of {{pushDeviceId}}; the path key is a
                       # device key.
                       ("/v1/custom-sources/", {"deviceId": "ingestDeviceId"}),
                       ("/v1/ingest/", {"key": "deviceKey"})]

# The EDP manifest of docs/External-Data-Protocol.md §2 (024).
EDP_MANIFEST = {
  "protocol": "foresignal-edp/1.0",
  "source": {"id": "hormuz-transits", "name": "Strait of Hormuz transits",
             "description": "Daily tanker and cargo transits counted from public dashboards", "version": "0.3.1",
             "publisher": {"name": "Example OSINT Lab", "url": "https://example.org", "contact": "ops@example.org"},
             "homepage": "https://github.com/example/hormuz-edp",
             "licence": {"id": "CC-BY-4.0", "url": "https://creativecommons.org/licenses/by/4.0/", "attribution": "Example OSINT Lab", "redistribution": "allowed"},
             "terms": {"summary": "Derived from a public dashboard; upstream terms unclear", "url": None},
             "access": {"route": "scrape", "fragility": "high"},
             "originator": {"id": "example-osint-lab", "class": "aggregator", "aggregates": []}, "private": False},
  "transport": {"push": True, "pull": None, "search": None},
  "streams": [
    {"id": "transits", "mode": "atomic", "itemType": "observation", "kind": "transit-count", "title": "Daily transits by vessel class",
     "unit": "vessels/day", "update": "incremental", "payloadSchema": {"type": "object", "properties": {"vesselClass": {"type": "string"}}},
     "suggestedBindings": [{"target": "series"}]},
    {"id": "incidents", "mode": "layer", "itemType": "event", "kind": "maritime-incident", "title": "Reported incidents", "geometry": "Point",
     "update": "incremental",
     "payloadSchema": {"type": "object", "required": ["category"], "properties": {"category": {"type": "string"}, "vessel": {"type": ["string", "null"]}}},
     "fields": {"title": "category", "significant": ["category"], "volatile": []}, "ttl": None, "retentionClass": "event",
     "presentation": {"color": "#d9480f", "icon": "ship", "labelField": "vessel", "minZoom": 3, "tileProperties": ["category"]}}]}

# The EDP batch of §3, with observedAt on the observation (the stream's time base is upstream).
EDP_ITEMS = [
  {"stream": "transits", "type": "observation", "id": "tanker:2026-10-10", "observedAt": "2026-10-11T06:00:00Z", "seriesKey": "tanker",
   "t": "2026-10-10", "value": 31, "knownAt": "2026-10-11T06:00:00Z", "properties": {"vesselClass": "tanker"}},
  {"stream": "incidents", "type": "event", "id": "inc-8812", "observedAt": "2026-10-11T17:41:00Z", "eventTime": "2026-10-11T16:55:00Z",
   "geometry": {"type": "Point", "coordinates": [56.4, 26.5]}, "properties": {"category": "approach-by-small-craft", "vessel": "MV Example"}},
  {"stream": "incidents", "type": "delete", "id": "inc-8790"},
  {"stream": "transits", "type": "status", "health": "degraded", "message": "dashboard layout changed; tanker counts missing for 2026-10-11"}]
EDP_BATCH = {"protocol": "foresignal-edp/1.0", "sourceId": "hormuz-transits", "sentAt": "2026-10-11T18:00:00Z", "snapshot": None,
             "items": EDP_ITEMS, "nextCursor": None}

ALERTMANAGER = {"version": "4", "groupKey": "{}:{alertname=\"HighTemp\"}", "truncatedAlerts": 0, "status": "firing", "receiver": "foresignal",
                "groupLabels": {"alertname": "HighTemp"}, "commonLabels": {"alertname": "HighTemp"}, "commonAnnotations": {},
                "externalURL": "http://alertmanager.local:9093",
                "alerts": [{"status": "firing", "labels": {"alertname": "HighTemp", "instance": "garage", "latitude": "30.27", "longitude": "-97.74"},
                            "annotations": {"summary": "Garage above 40 C"}, "startsAt": "2026-10-10T12:00:00Z", "endsAt": "0001-01-01T00:00:00Z",
                            "generatorURL": "http://prometheus.local:9090/graph", "fingerprint": "6a8f1c2b9d0e4f31"}]}

CAP_ALERT = """<?xml version="1.0" encoding="UTF-8"?>
<alert xmlns="urn:oasis:names:tc:emergency:cap:1.2">
  <identifier>EXAMPLE-2026-10-10-001</identifier>
  <sender>alerts@example.org</sender>
  <sent>2026-10-10T12:00:00-00:00</sent>
  <status>Actual</status>
  <msgType>Alert</msgType>
  <scope>Public</scope>
  <info>
    <language>en-US</language>
    <category>Met</category>
    <event>Flash Flood Warning</event>
    <urgency>Immediate</urgency>
    <severity>Severe</severity>
    <certainty>Likely</certainty>
    <onset>2026-10-10T12:00:00-00:00</onset>
    <expires>2026-10-10T18:00:00-00:00</expires>
    <headline>Flash flood warning for the river valley</headline>
    <description>Heavy rain is causing flash flooding.</description>
    <instruction>Move to higher ground.</instruction>
    <area>
      <areaDesc>River valley</areaDesc>
      <polygon>30.20,-97.80 30.20,-97.60 30.35,-97.60 30.35,-97.80 30.20,-97.80</polygon>
    </area>
  </info>
</alert>
"""

# Request bodies that are not JSON (024): content type, raw text, Postman language.
RAW_BODIES = {
  "ntfyPublish": ("text/plain", "Backup finished on pve1", "text"),
  "ntfyPublishPut": ("text/plain", "Disk usage above 90% on nas", "text"),
  # 036: a watchlist CSV import (header value[,label,notes,tags]; tags separated by ;).
  "importWatchlist": ("text/csv", "value,label,notes,tags\nA1B2C3,N123AB,Survey aircraft,survey;local\n"
                      "ADF7C8,N456CD,\"Medevac, county\",medical\n", "text"),
  # 073: a WebSub content delivery, as a hub sends it (an Atom document). The X-Hub-Signature must be the HMAC of
  # this exact body with the subscription secret; the placeholder below is refused with 403.
  "receiveWebSubNotification": ("application/atom+xml",
                                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                                "<feed xmlns=\"http://www.w3.org/2005/Atom\">\n"
                                "  <title>Example bulletins</title>\n"
                                "  <id>tag:bulletins.example,2026:feed</id>\n"
                                "  <updated>2026-10-13T12:00:00Z</updated>\n"
                                "  <link rel=\"self\" href=\"https://bulletins.example/atom.xml\"/>\n"
                                "  <link rel=\"hub\" href=\"https://hub.example/\"/>\n"
                                "  <entry>\n"
                                "    <title>Flood warning</title>\n"
                                "    <id>tag:bulletins.example,2026:entry-1</id>\n"
                                "    <link href=\"https://bulletins.example/e/1\"/>\n"
                                "    <updated>2026-10-13T11:30:00Z</updated>\n"
                                "    <summary>River levels rising.</summary>\n"
                                "  </entry>\n"
                                "</feed>\n", "xml"),
}

# multipart/form-data bodies (054, the remote SatDump pushes): Postman formdata entries. File parts are
# placeholders (src empty): pick the files in Postman before sending. Postman sets the boundary itself, so
# no Content-Type header is added.
FORMDATA_BODIES = {
  "ingestSourceMedia": [
    {"key": "meta", "type": "text", "contentType": "application/json",
     "value": json.dumps({"composite": "msu_mr_rgb_221", "timestamp": "2026-10-12T18:02:10Z",
                          "satellite": "METEOR-M2 3"})},
    {"key": "file", "type": "file", "src": "", "description": "PNG, JPEG or TIFF, at most 25 MB"},
  ],
  "ingestSourceRasters": [
    {"key": "meta", "type": "text", "contentType": "application/json",
     "value": json.dumps({"passFolder": "2026-10-12_18-02_meteor_m2-x_lrpt_137.9 MHz",
                          "files": ["dataset.json", "product.cbor", "rgb_MSU-MR_221_Composite.png",
                                    "rgb_MSU-MR_221_Composite_projected.tif"],
                          "product": "MSU-MR"})},
    {"key": "files", "type": "file", "src": "", "description": "dataset.json"},
    {"key": "files", "type": "file", "src": "", "description": "product.cbor"},
    {"key": "files", "type": "file", "src": "", "description": "rgb_MSU-MR_221_Composite.png"},
    {"key": "files", "type": "file", "src": "", "description": "rgb_MSU-MR_221_Composite_projected.tif (EPSG:4326 GeoTIFF)"},
  ],
}

# Request headers for operations that need more than the generic ones (024).
EXTRA_HEADERS = {
  "ntfyPublish": [("X-Title", "Proxmox"), ("X-Priority", "high"), ("X-Tags", "backup,id=pve1-backup")],
  "ntfyPublishPut": [("Title", "NAS"), ("Priority", "4"), ("Tags", "storage")],
}

BODIES = {
  # downsampleInterval: TRACK sources only (STANDARD -> 400); null clears the override and is accepted for any source.
  "updateSource": {"enabled": True, "interval": "60s", "params": {"feed": "all_day"}, "budget": {"perMinute": 4, "perDay": 2000}, "retention": "365d",
                   "ingestPolicy": {"mode": "SAMPLED", "minInterval": "60s", "keyframe": "240s", "minDeviationMeters": 1000, "thresholds": True, "edgeMinInterval": "30s"},
                   "downsampleInterval": None},
  "setSourceCredentials": {"values": {"apiKey": "REPLACE_ME"}},
  "createTrigger": {"name": "Watched tails on the corridor", "enabled": True, "kinds": ["aircraft"],
                    "conditions": [{"path": "callsign", "op": "in", "value": ["N123AB", "N456CD"]}],
                    "geofence": {"type": "LineString", "coordinates": [[-97.75, 30.25], [-97.0, 32.9]]}, "bufferMeters": 5000,
                    "mode": "enter", "hysteresisMeters": 200, "cooldown": "15m", "severity": "warning", "tags": ["watchlist"],
                    "messageTemplate": "{{payload.callsign}} entered the corridor at {{observedAt}}", "weatherContext": "off"},
  "updateTrigger": {"name": "Vessels entering Strait of Hormuz", "enabled": True, "kind": "vessel", "mode": "enter",
                    "geofence": {"type": "Polygon", "coordinates": [[[55.8, 26.0], [57.2, 26.0], [57.2, 27.0], [55.8, 27.0], [55.8, 26.0]]]},
                    "cooldown": "1h", "messageTemplate": "{{payload.name}} ({{externalId}}) entered Hormuz. {{context.weather.summary}}",
                    "weatherContext": "auto"},
  "snoozeTrigger": {"for": "2h"},
  "acknowledgeAlert": {"note": "Looking into it"},
  "resolveAlert": {"note": "Handled on site", "feedback": "useful"},
  "snoozeAlert": {"for": "8h"},
  "setAlertFeedback": {"feedback": "noise", "note": "Routine traffic"},
  "createChannel": {"name": "ntfy phone", "type": "ntfy", "config": {"serverUrl": "https://ntfy.sh", "topic": "REPLACE_TOPIC"},
                    "secrets": {}, "events": ["alert", "briefing", "system"], "ruleIds": None, "minSeverity": "warning", "enabled": True,
                    "timezone": "America/Chicago", "quietHours": {"start": "22:00", "end": "07:00", "bypassSeverity": "critical"},
                    "backlogCap": 20, "groupWindowSeconds": 0, "editInPlace": True},
  "updateNotificationSettings": {"pausedUntil": "2026-10-12T06:00:00Z", "pauseBypassSeverity": "critical"},
  "updateChannel": {"name": "Slack #ops", "type": "slack", "config": {"username": "Foresignal"},
                    "secrets": {"webhookUrl": "https://hooks.slack.com/services/REPLACE"}, "events": ["alert", "briefing"], "ruleIds": None,
                    "minSeverity": "info", "waitForEnrichmentSeconds": 20, "enabled": True},
  "createToken": {"name": "openclaw-agent", "scope": "agent", "expiresAt": None},
  "createCustomSource": {"id": "custom-home-sensors", "name": "Home sensors", "kinds": ["sensor-reading"], "attribution": "My bench",
                         "recordTtl": "1h", "tileProperties": ["value", "unit"], "searchFields": ["name"], "embedFields": [], "rateLimitPerMinute": 60,
                         "observedAtMode": "UPSTREAM", "ttlAnchor": "RECEIVED", "volatileFields": ["rssi"], "significantFields": [],
                         "titleField": "name", "fieldSpecs": {"value": {"type": "NUMBER", "unit": "Cel", "label": "Temperature"}},
                         "private": False, "licence": None, "recordsPerMinute": 6000,
                         "formats": {"alertmanager": {"resolved": "tombstone"}},
                         "verifiers": {"native": {"scheme": "hmac-sha256", "header": "X-Signature-256", "secret": "REPLACE_ME", "keyOptional": False}}},
  "updateCustomSource": {"id": "custom-home-sensors", "name": "Home sensors", "kinds": ["sensor-reading", "sensor-alarm"], "attribution": "My bench",
                         "recordTtl": "1h", "tileProperties": ["value", "unit"], "searchFields": ["name"], "embedFields": [], "rateLimitPerMinute": 60},
  "ingestRecords": {"type": "FeatureCollection", "features": [{"type": "Feature", "id": "garage-temp",
                    "geometry": {"type": "Point", "coordinates": [-97.74, 30.27]},
                    "properties": {"kind": "sensor-reading", "observedAt": "2026-10-10T12:00:00Z", "name": "Garage temperature", "value": 31.5, "unit": "C"}}]},
  "ingestRecordsFormat": ALERTMANAGER,
  # 059: a RecordMapping previewed over an Ecowitt form sample, and an LLM draft for an airrohr sample.
  "previewMapping": {"mapping": {"version": 1, "externalId": {"const": "station"}, "kind": {"const": "weather-observation"},
                                 "observedAt": {"path": "$.dateutc", "format": "pattern:yyyy-MM-dd HH:mm:ss", "zone": "UTC"},
                                 "geometry": {"const": [-97.74, 30.27]},
                                 "fields": [{"name": "tempC", "path": "$.tempf", "type": "number", "unit": {"from": "[degF]", "to": "Cel"}},
                                            {"name": "rh", "path": "$.humidity", "type": "number"},
                                            {"name": "model", "path": "$.stationtype", "type": "string"}],
                                 "drop": ["PASSKEY"]},
                     "sample": {"contentType": "application/x-www-form-urlencoded",
                                "body": "PASSKEY=REDACTED&stationtype=GW2000A_V3.1.4&dateutc=2026-10-13+12%3A00%3A00&tempf=68.0&humidity=55"},
                     "sourceId": None},
  "suggestMapping": {"sample": {"contentType": "application/json",
                                "body": {"esp8266id": "1234567", "software_version": "NRZ-2020-133",
                                         "sensordatavalues": [{"value_type": "SDS_P1", "value": "12.4"}, {"value_type": "SDS_P2", "value": "6.1"},
                                                              {"value_type": "signal", "value": "-63"}]}},
                     "hints": {"kind": "air-quality", "description": "Sensor.Community airrohr on the balcony", "sourceId": None},
                     "confirmEgress": False},
  "testCustomSourceMapping": {"mapping": None},
  # A device key in the path (039): an OwnTracks location message, the format the personal-location preset uses.
  "ingestRecordsPathKey": {"_type": "location", "lat": 30.27, "lon": -97.74, "tst": 1760097600, "acc": 12, "batt": 81,
                           "tid": "ph"},
  "ntfyPublishJson": {"topic": "{{sourceId}}", "message": "Monitor 'NAS' is down", "title": "Uptime Kuma", "priority": 4,
                      "tags": ["warning", "id=kuma-nas"], "click": "https://kuma.local/dashboard/1"},
  "gotifyPublish": {"title": "vzdump", "message": "Backup of VM 101 finished", "priority": 5,
                    "extras": {"foresignal::record": {"externalId": "pve-backup-101"}}},
  "registerExternalSource": {"manifest": EDP_MANIFEST, "manifestUrl": None, "approve": False, "allowPrivateNetwork": False},
  "selfRegisterExternalSource": EDP_MANIFEST,
  "updateExternalSourceManifest": dict(EDP_MANIFEST, source=dict(EDP_MANIFEST["source"], version="0.3.2")),
  "pushExternalItems": EDP_BATCH,
  "createRegistrationToken": {"name": "Hormuz provider", "expiresAt": None, "maxUses": 1, "sourceIdPrefix": "hormuz"},
  "validateEdp": {"manifest": EDP_MANIFEST, "sourceId": None, "items": EDP_ITEMS},
  "createLlmProvider": {"id": "ollama", "type": "ollama", "baseUrl": "http://ollama:11434", "chatModel": "qwen3:14b",
                        "embeddingModel": "nomic-embed-text", "embeddingDimensions": 768, "requestsPerMinute": 60, "tokensPerDay": 5000000, "enabled": True},
  # The path's {{providerId}} is the ollama provider created above; a body id must match the path (026).
  "updateLlmProvider": {"id": "ollama", "type": "ollama", "baseUrl": "http://ollama:11434", "chatModel": "qwen3:14b",
                        "embeddingModel": "nomic-embed-text", "embeddingDimensions": 768, "options": {"maxTokens": 8000, "temperature": 0.2},
                        "requestsPerMinute": 60, "tokensPerDay": 5000000, "enabled": True, "knowledgeCutoff": "2025-04-01"},
  "updateLlmSettings": {"chatProvider": "ollama", "embeddingProvider": "ollama", "askEnabled": True, "enrichmentEnabled": True,
                        "logContent": False, "askMaxTokens": 200000},
  "ask": {"question": "Were there any earthquakes above magnitude 5 in the last 24 hours, and where?", "threadId": None,
          "scope": {"sources": ["usgs-earthquakes"], "since": None}, "allowActions": False},
  "askStream": {"question": "Summarize significant seismic activity this week and suggest a watch rule for M6+ near Japan.", "allowActions": False},
  "createBriefing": {"name": "Morning brief", "enabled": True, "schedule": "0 7 * * *", "timezone": "America/Chicago",
                     "scope": {"sources": [], "kinds": ["earthquake"], "includeHealth": True}, "lookback": "24h", "mode": "digest",
                     "instructions": None, "channelIds": ["{{channelId}}"], "maxItemsPerKind": 5},
  # Kinds must belong to a registered source (BriefingValidator); aircraft and vessel adapters arrive later (041).
  "updateBriefing": {"name": "Morning brief (LLM)", "enabled": True, "schedule": "0 7 * * 1-5", "timezone": "America/Chicago",
                     # Prompt 056: "Weather at places" for a place created with createPlace (placeId); products optional.
                     "scope": {"kinds": ["earthquake"], "includeHealth": True, "places": ["{{placeId}}"],
                               "weatherProducts": ["mrms-reflectivity", "mrms-mesh-60m", "gfs-gust", "hrrr-smoke"]},
                     "lookback": "24h", "mode": "llm",
                     "instructions": "Lead with anything critical. Keep it under 250 words.", "channelIds": ["{{channelId}}"]},
  "previewBriefing": {"name": "Preview", "schedule": "0 7 * * *", "scope": {"kinds": ["earthquake"]}, "lookback": "24h", "mode": "digest"},
  "createPushSubscription": {"name": "Laptop browser", "subscription": {"endpoint": "https://push.example/REPLACE",
                             "keys": {"p256dh": "REPLACE", "auth": "REPLACE"}}, "filters": {"events": ["alert"], "minSeverity": "warning"},
                             "transportLabel": "browser", "payloadFormat": "foresignal", "lang": "en"},
  # Replace with the code the verify push delivered to the subscription.
  "verifyPushSubscription": {"code": "REPLACE_WITH_VERIFY_CODE"},
  # Native push (032). The operator's own APNs key (.p8 PEM text) or Firebase service-account JSON; never commit them.
  "putPushProvider": {"type": "apns", "appIds": ["ai.foresignal.app"],
                      "config": {"teamId": "ABCDE12345", "keyId": "KEY1234567", "keyEnvironment": "production", "timeSensitive": False},
                      "secrets": {"p8": "-----BEGIN PRIVATE KEY-----\nREPLACE_WITH_YOUR_P8_KEY\n-----END PRIVATE KEY-----\n"},
                      "enabled": True},
  "testPushProvider": {"deviceId": None},
  # The token comes from the app: replace the all-zero placeholder with the APNs device token (64-200 hex characters).
  "registerPushDevice": {"transport": "apns", "platform": "ios", "appId": "ai.foresignal.app", "environment": "production",
                         "token": "0" * 64, "name": "My iPhone",
                         "filters": {"events": ["alert", "briefing"], "minSeverity": "warning"}, "contentLevel": "minimal", "lang": "en"},
  "updatePushDevice": {"name": "My iPhone", "filters": {"events": ["alert"], "minSeverity": "critical"}, "contentLevel": "short",
                       "lang": "en"},
  # 033: the inbox, presence, escalation policies and rule mutes. Add channelIds to a step to reach further channels.
  "markNotificationsRead": {"upToId": 1},
  "updatePresence": {"kind": "desktop", "state": "active", "ttlSeconds": 120},
  "createEscalationPolicy": {"name": "Wake me up", "steps": [{"afterMinutes": 10, "channelIds": [], "includePush": True},
                                                            {"afterMinutes": 30, "channelIds": [], "includePush": True}],
                             "repeat": {"everyMinutes": 30, "maxTimes": 3}, "stopOn": {"resolve": True, "snooze": True}},
  "updateEscalationPolicy": {"name": "Wake me up", "steps": [{"afterMinutes": 15, "channelIds": [], "includePush": True}],
                             "repeat": None, "stopOn": {"resolve": True, "snooze": False}},
  "muteTrigger": {"for": "4h"},
  "executeAction": {},
  "updateEventStreamFilter": {"changes": {"bbox": "-97.5,32.5,-96.5,33.2", "live": True}},
  "updateSecuritySettings": {"openReads": True, "exposeObserver": False, "metricsOpen": True},
  # 035: access perimeter.
  "createFeedKey": {"name": "home-assistant-map", "prefixes": ["/v1/records"],
                    "restrictions": {"kinds": ["earthquake"], "bbox": [-100.5, 32.0, -91.5, 40.0]},
                    "expiresAt": "2027-10-01T00:00:00Z"},
  # 050: tile keys for header-less map clients (MapLibre, maplibre_gl, Cesium, EventSource).
  "createTileKey": {"name": "living-room-tablet", "prefixes": ["/v1/products/", "/v1/styles", "/v1/changes/stream"],
                    "ttl": "7d", "restrictions": {"bbox": [-100.5, 32.0, -91.5, 40.0]}},
  "updateRateLimits": {"read": 600, "tiles": 1200, "stream": {"opensPerMinute": 30, "concurrent": 8}, "expensive": 20,
                       "write": 120, "anonymous": 120, "metricsPerMinute": None},
  "updateOutboundSettings": {"allowHosts": ["ollama"], "allowCidrs": [], "denyHosts": [], "denyCidrs": ["192.168.50.0/24"]},
  # 036: places, saved views and watchlists.
  "createPlace": {"id": "home", "name": "Home", "shape": "point", "geometry": {"type": "Point", "coordinates": [-97.74, 30.27]},
                  "radiusKm": 25, "timezone": "America/Chicago", "kind": "user", "tags": ["family"], "sensitive": True},
  "updatePlace": {"id": "home", "name": "Home", "shape": "point", "geometry": {"type": "Point", "coordinates": [-97.74, 30.27]},
                  "radiusKm": 40, "timezone": "America/Chicago", "kind": "user", "tags": ["family"], "sensitive": True},
  "createView": {"slug": "quakes-near-home", "name": "Quakes near home", "kinds": ["earthquake"], "placeIds": ["home"],
                 "conditions": [{"path": "mag", "op": ">=", "value": 2.5}], "timeField": "effective",
                 "fields": ["title", "observedAt", "geometry", "payload.mag"]},
  "updateView": {"slug": "quakes-near-home", "name": "Quakes near home (M3+)", "kinds": ["earthquake"], "placeIds": ["home"],
                 "conditions": [{"path": "mag", "op": ">=", "value": 3}]},
  "createWatchlist": {"name": "Tails to watch", "identity": "icao24", "description": "Aircraft followed across every source"},
  "updateWatchlist": {"name": "Tails to watch", "identity": "icao24", "normalize": "upper",
                      "description": "Aircraft followed across every source"},
  "addWatchlistItems": {"items": [{"value": "a1b2c3", "label": "N123AB", "notes": "Survey aircraft", "tags": ["survey"]},
                                  {"value": "ADF7C8", "label": "N456CD", "tags": ["medical"]}]},
  "updateServerSettings": {"publicBaseUrl": "http://foresignal.local:8080", "timezone": "UTC",
                           "clientLinks": {"alert": "foresignal://alerts/{id}", "pair": "foresignal://pair?u={publicBaseUrl}&c={code}"},
                           "push": {"contact": "mailto:admin@localhost", "allowedPrivateHosts": [], "defaultLang": "en"}},
  "createStreamTicket": {"purpose": "stream"},
  "updateCurrentTokenDevice": {"deviceName": "Pixel 9", "platform": "android", "appVersion": "1.0.0"},
  "createPairingCode": {"name": "Phone", "scope": "read", "ttl": "10m"},
  # Replace the example code with the one createPairingCode returned (stored in {{pairingCode}}).
  "claimPairingCode": {"code": "ABCD-EFGH", "device": {"deviceName": "Pixel 9", "platform": "android", "appVersion": "1.0.0"}},
  "updateRetention": {"default": "7d", "trackWindow": "3d", "provenance": "30d", "alerts": "90d", "usage": "30d",
                      "notifications": "90d", "briefings": "180d", "ask": "90d",
                      "kinds": {"earthquake": "3650d"}, "sources": {"usgs-earthquakes": "1825d"},
                      "trackRawWindow": "24h", "trackDownsampleInterval": "5m"},
  "updateWatchdogSettings": {"heartbeatUrl": "https://hc-ping.com/REPLACE-WITH-CHECK-UUID",
                             "failUrl": "https://hc-ping.com/REPLACE-WITH-CHECK-UUID/fail", "interval": "60s",
                             "method": "GET", "canary": {"enabled": False, "interval": "1h"}},
  "updateCors": {"readOrigins": ["*"], "writeOrigins": ["http://localhost:5173"]},
  # Prompt 056: changing defaultAoi or aoiBufferKm recomputes the places AOI.
  "updateWeatherSettings": {"alertContext": "auto", "defaultAoi": "-106.65,25.84,-93.51,36.50", "aoiBufferKm": 150},
  # 039: a device-scoped ingest key bound to one entity, and entity purges (keep alerts, redacted).
  "createIngestDevice": {"externalId": "family-phone-1", "name": "Family phone 1"},
  "purgeDevice": {"purgeAlerts": False},
  "purgeRecord": {"purgeAlerts": False},
  # 040: load the Natural Earth admin-1 boundaries and the GeoNames cities from the bundled manifest.
  "loadReference": {"datasets": ["ne-admin1", "geonames-cities15000"]},
  # 038: stats rollup rebuild and metric projections (a USGS magnitude gauge).
  "rebuildStatsRollup": {"from": "2026-10-01T00:00:00Z", "to": "2026-10-02T00:00:00Z"},
  "updateMetricSettings": {"projections": [{"name": "usgs_magnitude", "source": "usgs-earthquakes", "kind": "earthquake",
                                            "path": "mag", "unit": "magnitude",
                                            "help": "Magnitude of current USGS earthquakes", "maxSeries": 500}]},
  "importSettings": {"sources": {"usgs-earthquakes": {"enabled": True, "interval": "60s"}},
                     "retention": {"default": "7d", "alerts": "90d", "usage": "30d", "sources": {}},
                     "cors": {"readOrigins": ["*"], "writeOrigins": []}, "triggers": [], "channels": []},
  "postSample": {"points": [{"lon": -97.74, "lat": 30.27}, {"place": "home"}],
                 "products": ["mrms-reflectivity", "gfs-gust"], "time": "latest", "units": "metric"},
  "postProductStats": {"bbox": "-98.5,29.5,-97.0,31.0", "time": "latest", "thresholds": [40, 50], "units": "metric"},
}

# test scripts: capture ids / assert status
CAPTURE = {
  "createTrigger": "const j = pm.response.json(); pm.collectionVariables.set('ruleId', j.id);",
  "createChannel": "const j = pm.response.json(); pm.collectionVariables.set('channelId', j.id);",
  "createToken": "const j = pm.response.json(); pm.collectionVariables.set('tokenId', j.id); console.log('New token (shown once):', j.token);",
  "createCustomSource": "const j = pm.response.json(); pm.collectionVariables.set('ingestKey', j.ingestKey); pm.collectionVariables.set('sourceId', j.id);",
  "createIngestDevice": "const j = pm.response.json(); pm.collectionVariables.set('ingestDeviceId', j.id); pm.collectionVariables.set('deviceKey', j.key);",
  "rotateIngestKey": "const j = pm.response.json(); pm.collectionVariables.set('ingestKey', j.ingestKey);",
  "createLlmProvider": "const j = pm.response.json(); pm.collectionVariables.set('providerId', j.id);",
  "ask": "const j = pm.response.json(); pm.collectionVariables.set('threadId', j.threadId); if (j.proposedActions && j.proposedActions.length) pm.collectionVariables.set('actionId', j.proposedActions[0].id);",
  "createBriefing": "const j = pm.response.json(); pm.collectionVariables.set('briefingId', j.id);",
  "runBriefing": "const j = pm.response.json(); if (j.id) pm.collectionVariables.set('runId', j.id);",
  "createPushSubscription": "const j = pm.response.json(); pm.collectionVariables.set('subscriptionId', j.id);",
  "registerPushDevice": "const j = pm.response.json(); pm.collectionVariables.set('pushDeviceId', j.id);",
  "listNotifications": "const j = pm.response.json(); if (j.items && j.items.length) pm.collectionVariables.set('notificationId', j.items[0].id);",
  "getNotification": "const j = pm.response.json(); if (j.actions && j.actions.length) pm.collectionVariables.set('actionToken', j.actions[0].url.split('/v1/actions/')[1]);",
  "createEscalationPolicy": "const j = pm.response.json(); pm.collectionVariables.set('escalationPolicyId', j.id);",
  "createFeedKey": "const j = pm.response.json(); pm.collectionVariables.set('feedKeyId', j.id); console.log('New feed key URLs (shown once):', j.urls);",
  "createTileKey": "const j = pm.response.json(); pm.collectionVariables.set('tileKeyId', j.id); console.log('New tile key URL base (shown once):', j.urlBase);",
  "createPlace": "const j = pm.response.json(); pm.collectionVariables.set('placeId', j.id);",
  "createView": "const j = pm.response.json(); pm.collectionVariables.set('viewId', j.id);",
  "createWatchlist": "const j = pm.response.json(); pm.collectionVariables.set('watchlistId', j.id);",
  "listAlerts": "const j = pm.response.json(); if (j.items && j.items.length) pm.collectionVariables.set('alertId', j.items[0].id);",
  "listChanges": "const j = pm.response.json(); pm.collectionVariables.set('lastSeq', j.lastSeq);",
  "listRecords": "const j = pm.response.json(); const it = (j.items || j.features || [])[0]; if (it) { const ext = it.externalId || (it.properties && it.properties.externalId); pm.collectionVariables.set('externalId', ext); }\nif (j.nextCursor) pm.collectionVariables.set('cursor', j.nextCursor);",
  "rotateAdminToken": "const j = pm.response.json(); pm.collectionVariables.set('adminToken', j.token); console.log('New admin token stored in collection variable adminToken');",
  "createPairingCode": "const j = pm.response.json(); pm.collectionVariables.set('pairingCodeId', j.id); pm.collectionVariables.set('pairingCode', j.code);",
  "claimPairingCode": "const j = pm.response.json(); pm.collectionVariables.set('readToken', j.token); console.log('Paired token stored in collection variable readToken');",
  "createStreamTicket": "const j = pm.response.json(); pm.collectionVariables.set('ticket', j.ticket);",
  "registerExternalSource": "const j = pm.response.json(); pm.collectionVariables.set('edpIngestKey', j.ingestKey); pm.collectionVariables.set('edpSourceId', j.source.sourceId);",
  "selfRegisterExternalSource": "const j = pm.response.json(); pm.collectionVariables.set('edpIngestKey', j.ingestKey); pm.collectionVariables.set('edpSourceId', j.source.sourceId);",
  "createRegistrationToken": "const j = pm.response.json(); pm.collectionVariables.set('registrationToken', j.token); pm.collectionVariables.set('registrationTokenId', j.id);",
}

# Extra request items for operations with several useful bodies (one item per variant, placed after the
# operation's main item). createChannel: one per channel type that needs different fields (018).
VARIANTS = {
  # Ingest formats (024): each variant may override the format path segment, the content type and the raw body.
  "ingestRecords": [
    ("records shape", {"records": [{"externalId": "garage-humidity", "kind": "sensor-reading", "observedAt": "2026-10-10T12:00:00Z",
                                    "geometry": {"type": "Point", "coordinates": [-97.74, 30.27]}, "payload": {"name": "Garage humidity", "value": 61, "unit": "%"}}]}),
  ],
  "ingestRecordsFormat": [
    ("CloudEvents structured", {"specversion": "1.0", "id": "evt-0001", "source": "/homeassistant/garage", "type": "sensor.reading",
                                "subject": "garage-door", "time": "2026-10-10T12:00:00Z", "datacontenttype": "application/json",
                                "data": {"state": "open", "lat": 30.27, "lon": -97.74}},
     {"format": "cloudevents", "content_type": "application/cloudevents+json"}),
    ("CloudEvents batch", [{"specversion": "1.0", "id": "evt-0002", "source": "/n8n/flow-7", "type": "sensor.reading", "subject": "porch-light",
                            "time": "2026-10-10T12:01:00Z", "data": {"state": "on"}}],
     {"format": "cloudevents", "content_type": "application/cloudevents-batch+json"}),
    ("CAP 1.2 alert", None, {"format": "cap", "content_type": "application/cap+xml", "raw": CAP_ALERT, "language": "xml"}),
    # 059: the source's RecordMapping reads items[*] (select), device, ts (epochS), pos and pm25.
    ("mapping (RecordMapping)", {"items": [{"device": "aqi-1", "ts": 1760097600, "pm25": 12.5, "pos": {"lat": 30.27, "lon": -97.74}}]},
     {"format": "mapping"}),
  ],
  "pushExternalItems": [
    ("NDJSON", None, {"content_type": "application/x-ndjson", "language": "text",
                      "raw": "\n".join(json.dumps(x) for x in [{"protocol": "foresignal-edp/1.0", "sourceId": "hormuz-transits"}] + EDP_ITEMS[:2]) + "\n"}),
  ],
  # 059: a source created from a device preset; the response's setup holds the device instructions and URL.
  "createCustomSource": [
    ("from the ecowitt preset", {"id": "custom-backyard-weather", "name": "Backyard weather", "kinds": ["weather-observation"],
                                 "preset": "ecowitt",
                                 "presetParams": {"lon": -97.7431, "lat": 30.2672}}),
  ],
  "createToken": [
    ("restricted", {"name": "dashboard-kiosk", "scope": "read", "expiresAt": None,
                    "restrictions": {"endpoints": ["GET /v1/records/**", "GET /v1/tiles/**", "GET /v1/tiles.json"]}}),
    ("data-restricted (035)", {"name": "family-phone", "scope": "read", "expiresAt": None,
                               "restrictions": {"kinds": ["earthquake", "weather-alert"], "bbox": [-80.5, 43.0, -78.5, 44.5],
                                                "maxRequestsPerMinute": 120}}),
  ],
  "updateSecuritySettings": [
    ("anonymous profile (035)", {"openReads": True, "exposeObserver": False, "metricsOpen": False,
                                 "anonymous": {"kinds": ["earthquake"], "maxRequestsPerMinute": 60}}),
  ],
  "createStreamTicket": [
    ("tiles", {"purpose": "tiles", "ttl": "10m"}),
  ],
  # Native push (032): an FCM provider and an Android device.
  "putPushProvider": [
    ("FCM service account", {"type": "fcm", "appIds": ["ai.foresignal.app"],
                             "config": {"projectId": "my-firebase-project", "clientEmail": "fcm-sender@my-firebase-project.iam.gserviceaccount.com",
                                        "tokenUri": "https://oauth2.googleapis.com/token"},
                             "secrets": {"serviceAccountJson": "REPLACE_WITH_THE_WHOLE_SERVICE_ACCOUNT_JSON"}, "enabled": True}),
  ],
  "registerPushDevice": [
    ("Android via FCM", {"transport": "fcm", "platform": "android", "appId": "ai.foresignal.app",
                         "token": "REPLACE_WITH_FCM_REGISTRATION_TOKEN", "name": "Pixel 9", "contentLevel": "minimal"}),
  ],
  "createChannel": [
    ("Discord (edits on resolve)", {"name": "Discord #alerts", "type": "discord", "config": {"username": "Foresignal"},
                                    "secrets": {"webhookUrl": "https://discord.com/api/webhooks/REPLACE/REPLACE"}, "events": ["alert", "system"]}),
    ("Telegram", {"name": "Telegram", "type": "telegram", "config": {"chatId": "REPLACE_CHAT_ID"},
                  "secrets": {"botToken": "REPLACE_BOT_TOKEN"}, "events": ["alert"], "minSeverity": "warning"}),
    ("Telegram, interactive chat commands (061)", {"name": "Telegram ops", "type": "telegram",
                  "config": {"chatId": "REPLACE_CHAT_ID", "interactive": True,
                             "allowedChats": [{"chatId": "REPLACE_PRIVATE_CHAT_ID", "name": "My phone", "scope": "agent"},
                                              {"chatId": "-100REPLACE_GROUP_ID", "name": "Family group", "scope": "read",
                                               "userIds": ["REPLACE_USER_ID"]}],
                             "askRateLimitPerHour": 10, "commandRateLimit": 30, "takeOverWebhook": False,
                             "discoveryMode": True},
                  "secrets": {"botToken": "REPLACE_DEDICATED_BOT_TOKEN"}, "events": ["alert", "system"]}),
    ("Matrix", {"name": "Matrix room", "type": "matrix", "config": {"homeserverUrl": "https://matrix.example.org", "roomId": "!REPLACE:example.org"},
                "secrets": {"accessToken": "REPLACE"}, "events": ["alert"]}),
    ("Teams (Workflows URL only)", {"name": "Teams", "type": "teams", "secrets": {"workflowUrl": "https://REPLACE.logic.azure.com/workflows/REPLACE"},
                                    "events": ["alert"]}),
    ("Pushover", {"name": "Pushover", "type": "pushover", "secrets": {"appToken": "REPLACE", "userKey": "REPLACE"}, "events": ["alert"],
                  "minSeverity": "warning"}),
    ("Email (plain text for Winlink)", {"name": "Winlink", "type": "email", "config": {"smtpHost": "smtp.example.org", "smtpPort": 587,
                                        "tls": "starttls", "from": "foresignal@example.org", "to": ["REPLACE@winlink.org"], "plainTextOnly": True},
                                        "secrets": {"username": "REPLACE", "password": "REPLACE"}, "titleTemplate": "{{severity}} {{name}}"}),
    ("Home Assistant (notify, critical alerts)", {"name": "HA phone", "type": "homeassistant",
                                                  "config": {"baseUrl": "http://homeassistant.local:8123", "mode": "notify", "service": "mobile_app_replace_me"},
                                                  "secrets": {"accessToken": "REPLACE"}, "events": ["alert"]}),
    ("Apprise", {"name": "Apprise", "type": "apprise", "config": {"serverUrl": "http://apprise.local:8000", "configKey": "foresignal"},
                 "events": ["alert", "system"]}),
    ("Zulip (topic per rule)", {"name": "Zulip", "type": "zulip", "config": {"site": "https://REPLACE.zulipchat.com", "stream": "alerts",
                                "botEmail": "foresignal-bot@REPLACE.zulipchat.com", "topicTemplate": "{{ruleName}}"},
                                "secrets": {"apiKey": "REPLACE"}, "events": ["alert"]}),
    ("MQTT with Home Assistant discovery (060)", {"name": "MQTT broker", "type": "mqtt",
                                                  "config": {"brokerUrl": "mqtts://broker.lan:8883", "topicPrefix": "foresignal",
                                                             "qos": 1, "haDiscovery": True, "haPrefix": "homeassistant"},
                                                  "secrets": {"username": "foresignal", "password": "REPLACE"},
                                                  "events": ["alert", "briefing", "system"]}),
    ("Webhook (HMAC signed)", {"name": "Automation webhook", "type": "webhook", "config": {"url": "https://REPLACE/hooks/foresignal", "method": "POST"},
                               "secrets": {"hmacSecret": "REPLACE"}, "events": ["alert", "briefing", "system"]}),
  ],
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
    path_vars = dict(PATH_VARS)
    for prefix, overrides in PATH_VARS_BY_PREFIX:
        if path.startswith(prefix):
            path_vars.update(overrides)
    pm_path = re.sub(r"\{(\w+)\}", lambda m: "{{" + path_vars.get(m.group(1), m.group(1)) + "}}", path)
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
    ingest_key = any("ingestKey" in alt for alt in op.get("security", []))
    if ingest_key:
        # The ingest key of the source (024): the EDP paths use the provider's key, Gotify its own header.
        request["auth"] = {"type": "noauth"}
        key_var = "{{edpIngestKey}}" if path.startswith("/v1/external/") else "{{ingestKey}}"
        key_header = "X-Gotify-Key" if path.startswith("/compat/gotify") else "X-Foresignal-Ingest-Key"
        request["header"].append({"key": key_header, "value": key_var})
    elif opid == "selfRegisterExternalSource":
        request["auth"] = {"type": "noauth"}
        request["header"].append({"key": "Authorization", "value": "Bearer {{registrationToken}}"})
    elif admin:
        var = "{{agentToken}}" if scope == "agent" else "{{adminToken}}"
        request["auth"] = {"type": "bearer", "bearer": [{"key": "token", "value": var, "type": "string"}]}
    elif scope == "read" and op.get("x-foresignal-always-authenticated"):
        # Always authenticated (023): a token is needed even while openReads is on.
        request["auth"] = {"type": "bearer", "bearer": [{"key": "token", "value": "{{readToken}}", "type": "string"}]}
    else:
        request["auth"] = {"type": "noauth"}
    for key, value in EXTRA_HEADERS.get(opid, []):
        request["header"].append({"key": key, "value": value})
    if opid in FORMDATA_BODIES:
        request["body"] = {"mode": "formdata", "formdata": FORMDATA_BODIES[opid]}
    elif opid in RAW_BODIES:
        ctype, raw_body, language = RAW_BODIES[opid]
        request["header"].append({"key": "Content-Type", "value": ctype})
        request["body"] = {"mode": "raw", "raw": raw_body, "options": {"raw": {"language": language}}}
    elif "requestBody" in op:
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
        item = req_item(path, method, op)
        items.append(item)
        for entry in VARIANTS.get(op["operationId"], []):
            label, body = entry[0], entry[1]
            extra = entry[2] if len(entry) > 2 else {}
            variant = json.loads(json.dumps(item))
            variant["name"] = f"{item['name']} ({label})"
            if "raw" in extra:
                variant["request"]["body"]["raw"] = extra["raw"]
            else:
                variant["request"]["body"]["raw"] = json.dumps(body, indent=2)
            variant["request"]["body"]["options"]["raw"]["language"] = extra.get("language", "json")
            if "content_type" in extra:
                for h in variant["request"]["header"]:
                    if h["key"] == "Content-Type":
                        h["value"] = extra["content_type"]
            if "format" in extra:
                url = variant["request"]["url"]
                url["raw"] = url["raw"].replace("{{ingestFormat}}", extra["format"])
                url["path"] = [extra["format"] if seg == "{{ingestFormat}}" else seg for seg in url["path"]]
            variant["_variant"] = True
            items.append(variant)

folders = []
for tag in [t["name"] for t in spec["tags"]]:
    desc = next(t.get("description", "") for t in spec["tags"] if t["name"] == tag)
    folder_items = [dict((k, v) for k, v in i.items() if not k.startswith("_")) for i in items if i["_tag"] == tag]
    folders.append({"name": tag, "description": desc, "item": folder_items})

by_id = {i["_opid"]: dict((k, v) for k, v in i.items() if not k.startswith("_")) for i in items if not i.get("_variant")}
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
                    dict(by_id["streamNotifications"], name="Notification inbox stream"),
                    dict(by_id["askStream"], name="Ask (streaming)")]}

collection = {
  "info": {"_postman_id": str(uuid.uuid5(uuid.NAMESPACE_URL, "https://foresignal.ai/postman")), "name": "Foresignal API",
           "description": f"{spec['info']['description']}\n\nGenerated from docs/OpenAPI.json (v{spec['info']['version']}). Every operation is included, grouped by tag. Admin operations use Bearer `{{{{adminToken}}}}`, agent operations `{{{{agentToken}}}}` (defaults to the admin token; set it to an agent-scope token to test scopes), always-authenticated read operations `{{{{readToken}}}}` (defaults to the admin token; claiming a pairing code stores a read token there), ingest uses `X-Foresignal-Ingest-Key: {{{{ingestKey}}}}` (EDP pushes `{{{{edpIngestKey}}}}`, Gotify `X-Gotify-Key`), and other reads use no auth (open while openReads is on). Test scripts capture ids into collection variables (ruleId, channelId, alertId, briefingId, threadId, ingestKey, externalId, lastSeq, cursor…).",
           "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"},
  "item": [quickstart, quickstart2, streams] + folders,
  "variable": [{"key": k, "value": v} for k, v in [
      ("baseUrl", "http://localhost:8080"), ("adminToken", ""), ("agentToken", "{{adminToken}}"), ("readToken", "{{adminToken}}"), ("ingestKey", ""),
      ("sourceId", "usgs-earthquakes"), ("externalId", ""), ("ruleId", ""), ("alertId", ""), ("channelId", ""), ("tokenId", ""),
      ("providerId", "ollama"), ("threadId", ""), ("actionId", ""), ("briefingId", ""), ("runId", ""), ("subscriptionId", ""),
      ("lastSeq", "0"), ("cursor", ""), ("pairingCodeId", ""), ("pairingCode", ""), ("ticket", ""),
      ("ingestFormat", "alertmanager"), ("ntfyAction", "publish"), ("edpSourceId", "ext-hormuz-transits"), ("edpIngestKey", ""),
      ("registrationToken", ""), ("registrationTokenId", ""), ("pushProviderId", "ios-apns"), ("pushDeviceId", ""),
      ("notificationId", ""), ("actionToken", ""), ("escalationPolicyId", ""), ("feedKeyId", ""), ("tileKeyId", ""),
      ("placeId", "home"), ("viewId", ""), ("watchlistId", ""),
      ("tileLayer", "earthquake"), ("tileZ", "0"), ("tileX", "0"), ("tileY", "0")]],
}
env = {"id": str(uuid.uuid5(uuid.NAMESPACE_URL, "https://foresignal.ai/postman/env/local")), "name": "Foresignal — Local",
       "values": [{"key": "baseUrl", "value": "http://localhost:8080", "type": "default", "enabled": True},
                  {"key": "adminToken", "value": "", "type": "secret", "enabled": True},
                  {"key": "readToken", "value": "{{adminToken}}", "type": "secret", "enabled": True}],
       "_postman_variable_scope": "environment"}
(outdir / "Foresignal.postman_collection.json").write_text(json.dumps(collection, indent=2))
(outdir / "Foresignal-Local.postman_environment.json").write_text(json.dumps(env, indent=2))
ops = {i["_opid"] for i in items}
print(len(items), "requests across", len(folders), "tag folders; quickstart", len(quickstart["item"]), "steps; covers all ops:", ops == {op["operationId"] for p in spec["paths"].values() for m, op in p.items() if m != "parameters"})
