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
             "placeId": "placeId", "viewId": "viewId", "watchlistId": "watchlistId",
             # 111: export jobs and holds.
             "exportId": "exportId", "holdId": "holdId",
             # 113: federation peers.
             "peerId": "peerId",
             # 114: pattern-of-life subjects and trips.
             "subjectId": "polSubjectId", "tripId": "tripId",
             # 115: exercise runs, tuning suggestions and tuning log entries.
             "exerciseId": "exerciseId", "suggestionId": "suggestionId", "logId": "tuningLogId",
             # 116: canonical events, analysis manifests and prompt templates.
             "canonicalEventId": "canonicalEventId", "manifestId": "manifestId", "templateId": "templateId",
             # 117: attention topics.
             "topicId": "attentionTopicId",
             # 118: situations and their members.
             "situationId": "situationId", "memberId": "situationMemberId",
             # 119: analytic findings.
             "findingId": "findingId",
             # 120: precursors, mining batches and operator candidates.
             "precursorId": "precursorId", "batchId": "precursorBatchId", "candidateId": "precursorCandidateId",
             # 121: reference-history imports and base rates.
             "importId": "referenceImportId", "baseRateId": "baseRateId",
             # 122: forecast questions, forecasters and retrospective evaluations.
             "questionId": "questionId", "forecasterId": "forecasterId", "evaluationId": "evaluationId",
             # 125: hypothesis sets, hypotheses, indicators, ACH evidence and key assumptions.
             "setId": "hypothesisSetId", "hypothesisId": "hypothesisId", "indicatorId": "indicatorId",
             "evidenceId": "achEvidenceId", "assumptionId": "assumptionId",
             # 126: regional indices.
             "indexId": "indexId",
             # 128: analysis cycles and assessments.
             "cycleId": "cycleId", "assessmentId": "assessmentId",
             # 129: object references (one path segment, / percent-encoded), workspaces, annotations, relations.
             "ref": "objectRef", "workspaceId": "workspaceId", "annotationId": "annotationId",
             "relationId": "relationId",
             # 130: principals, groups, tags, attachments, evidence items and saved searches.
             "principalId": "principalId", "groupId": "groupId", "tagId": "tagId", "attachmentId": "attachmentId",
             "evidenceItemId": "evidenceItemId", "savedSearchId": "savedSearchId",
             # 131: chronicles and their layers.
             "chronicleId": "chronicleId", "layerId": "chronicleLayerId",
             # 132: reports, their blocks, packages built here and packages received.
             "reportId": "reportId", "blockId": "blockId", "packageId": "packageId", "receivedId": "receivedId",
             # 134: chronicle links, clusters and access requests.
             "linkId": "chronicleLinkId", "clusterId": "chronicleClusterId", "requestId": "accessRequestId"}

# Path variables that mean something else under a prefix (024): EDP sources and registration tokens.
PATH_VARS_BY_PREFIX = [("/v1/external/sources/", {"sourceId": "edpSourceId"}),
                       ("/v1/external/registration-tokens/", {"tokenId": "registrationTokenId"}),
                       # Native push providers (032) are not the LLM providers of {{providerId}}.
                       ("/v1/push/providers/", {"providerId": "pushProviderId"}),
                       # 039: ingest devices are not the push devices of {{pushDeviceId}}; the path key is a
                       # device key.
                       ("/v1/custom-sources/", {"deviceId": "ingestDeviceId"}),
                       ("/v1/ingest/", {"key": "deviceKey"}),
                       # 114: a learned pattern-of-life place is not a 036 place of {{placeId}}.
                       ("/v1/pol/places/", {"placeId": "polPlaceId"}),
                       # 123: a benchmark run is not a briefing run of {{runId}}.
                       ("/v1/benchmarks/", {"runId": "benchmarkRunId", "caseId": "benchmarkCaseId"}),
                       # 126: the region and family of a regional outlook.
                       ("/v1/outlooks/", {"region": "outlookRegion", "family": "outlookFamily"}),
                       # 128: a cycle run is not a briefing run of {{runId}}.
                       ("/v1/analysis/", {"runId": "cycleRunId"}),
                       # 161: the code of a country or admin-1 region whose outline is read.
                       ("/v1/reference/areas/", {"code": "regionCode"})]

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
  # 129: JSON merge patches (application/merge-patch+json): an annotation's body, a workspace's name and members, and
  # a workspace context patch (send it with If-Match: {{contextVersion}}).
  "updateAnnotation": ("application/merge-patch+json", json.dumps({"body": "Confirmed by the 06:00 imagery pass.",
                                                                   "tags": ["imagery"]}, indent=2), "json"),
  "updateWorkspace": ("application/merge-patch+json", json.dumps({"name": "Red Sea watch (night shift)",
                                                                  "members": {"principal-analyst-2": "viewer"}},
                                                                 indent=2), "json"),
  "patchWorkspaceContext": ("application/merge-patch+json", json.dumps({
      "focusRef": "situation:{{situationId}}", "selectedRefs": ["situation:{{situationId}}"],
      "timeRange": {"start": "2026-10-16T00:00:00Z", "end": None}, "playback": {"state": "paused", "rate": 60}},
      indent=2), "json"),
  # 130: merge patches of a principal (admin), a group (managers), a tag, an evidence item and a saved search.
  "updatePrincipal": ("application/merge-patch+json", json.dumps({"organization": "Example Analysis Ltd",
                                                                  "timezone": "Europe/London"}, indent=2), "json"),
  "updateGroup": ("application/merge-patch+json", json.dumps({"description": "Red Sea desk, all shifts"}, indent=2),
                  "json"),
  "updateTag": ("application/merge-patch+json", json.dumps({"color": "#d94f30"}, indent=2), "json"),
  "updateEvidenceItem": ("application/merge-patch+json", json.dumps({"sourceReliability": "B",
                                                                     "informationCredibility": 2}, indent=2), "json"),
  "updateSavedSearch": ("application/merge-patch+json", json.dumps({"notify": True}, indent=2), "json"),
  # 132: a report draft's merge patch (the version is the optimistic lock) and a block's.
  "updateReport": ("application/merge-patch+json", json.dumps({"subtitle": "Shipping disruption, week 42 (rev.)",
                                                               "status": "review", "version": 1}, indent=2), "json"),
  "updateReportBlock": ("application/merge-patch+json", json.dumps({"title": "Bottom line up front"}, indent=2),
                        "json"),
  # 131: merge patches of a chronicle (send If-Match with its ETag to guard against a concurrent change) and of a
  # layer (the new definition applies from the edit's seq onward).
  "updateChronicle": ("application/merge-patch+json", json.dumps({"description": "Regional war monitoring, 2026",
                                                                  "sizeCapBytes": 32212254720, "retention": "forever"},
                                                                 indent=2), "json"),
  "updateChronicleLayer": ("application/merge-patch+json", json.dumps({"capture": {"mode": "downsampled",
                                                                                   "interval": "PT120S"}}, indent=2),
                           "json"),
  # 133: an outlet merge patch (registry facts only; ratings are never edited here), a provider update (enabling
  # needs the admin's license acknowledgement) and an operator ratings CSV.
  "updateMediaOutlet": ("application/merge-patch+json", json.dumps({"type": "national", "ownerOrg": "Example Media"
                                                                    " Group", "aggregates": ["ap", "reuters"]},
                                                                   indent=2), "json"),
  "updateMediaProvider": ("application/json", json.dumps({"licenceAcknowledged": True, "enabled": True}, indent=2),
                          "json"),
  "importMediaRatings": ("text/csv", "domain,dimension,value,label,licence,attribution\n"
                         "example-news.com,lean,-1,lean-left,CC BY 4.0,Example ratings project\n"
                         "example-news.com,factuality,4,,CC BY 4.0,Example ratings project\n", "text"),
  # 135: a social platform's settings (replace semantics): X with hourly counts for standing topics, a $25 monthly
  # cap and deletion sync. Set the bearer token first with PUT /v1/sources/x/credentials.
  "updateSocialPlatform": ("application/json", json.dumps({"enabled": True, "mode": "counts", "deletionSync": True,
                                                           "postRetention": "30d", "monthlyCapUsd": 25,
                                                           "options": {"standingCounts": True}}, indent=2), "json"),
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
  # 132: importing a package handed over by other means (admin); jwks pins the sender's keys when it is not a peer.
  "importExchangePackage": [
    {"key": "file", "type": "file", "src": "", "description": "The package ZIP (pick it in Postman)"},
    {"key": "jwks", "type": "text", "value": "{\"keys\":[]}", "description": "The sender's JWKS (optional)"},
  ],
  # 136: match an image against the stored fingerprints (PNG or JPEG; at most 31 bits apart, 20 matches).
  "matchMedia": [
    {"key": "file", "type": "file", "src": "", "description": "The image (pick it in Postman)"},
    {"key": "maxDistance", "type": "text", "value": "31"},
    {"key": "limit", "type": "text", "value": "20"},
  ],
  # 130: an attachment upload (PNG, JPEG, PDF or UTF-8 text, CSV, JSON, GeoJSON, KML, Markdown; at most 50 MB).
  "uploadAttachment": [
    {"key": "file", "type": "file", "src": "", "description": "The document or image (pick it in Postman)"},
    {"key": "tlp", "type": "text", "value": "AMBER"},
    {"key": "visibility", "type": "text", "value": "private"},
  ],
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
  # 129: the context version the patch was made against, and this client's id (its originClientId).
  "patchWorkspaceContext": [("If-Match", "{{contextVersion}}"), ("Foresignal-Client-Id", "postman")],
  "ntfyPublish": [("X-Title", "Proxmox"), ("X-Priority", "high"), ("X-Tags", "backup,id=pve1-backup")],
  "ntfyPublishPut": [("Title", "NAS"), ("Priority", "4"), ("Tags", "storage")],
}

BODIES = {
  # 141: WASM parser plugins; enable one after reviewing its SHA-256 (this is the example plugin's hash). Plugins stay off until updatePluginSettings enables them.
  "updatePlugin": {"enabled": True, "sha256": "232eeeefab43a723a76b0a15f8d1e8b67f8bf9aae0cfa2a7ffa8ba7c70d8f6f8"},
  "updatePluginSettings": {"enabled": False},
  # 141: remote MCP authorization delegated to the operator's identity provider; admin is never mappable.
  "updateMcpAuthSettings": {"mode": "both", "issuer": "https://auth.example.org/realms/home", "audience": "https://foresignal.example.org/mcp",
                            "scopeMap": {"foresignal:read": "read", "foresignal:agent": "agent"}, "scopeClaim": "scope",
                            "allowedAlgs": ["RS256", "ES256", "EdDSA"]},
  # 140: the adapter doctor; llm auto adds an explanation and a proposed (never applied) patch when a chat provider is assigned.
  "runSourceDoctor": {"probe": True, "llm": "auto", "detail": "compact"},
  # 108: raster exports (MBTiles/PMTiles take WebMercatorQuad only; a COG in WebMercatorQuad is EPSG:3857) and the place pack
  "createProductExport": {"format": "pmtiles", "frameKey": "latest", "bbox": [-98.6, 29.6, -96.9, 31.0], "minZoom": 4, "maxZoom": 7,
                          "tms": "WebMercatorQuad"},
  "createPlacePack": {"products": [], "frames": 6, "radiusKm": 150, "maxZoom": 6},
  # 110: outbound sinks. Created disabled; secrets are write-only (an update merges them: omitted fields keep their value, null removes one).
  "createSink": {"id": "home-mqtt", "type": "mqtt-state", "name": "Home Assistant MQTT", "enabled": False,
                 "config": {"brokerUrl": "mqtt://192.168.1.10:1883", "prefix": "foresignal", "changesTopic": False},
                 "secrets": {"username": "foresignal", "password": "REPLACE_ME"},
                 "filter": {"sources": ["usgs-earthquakes"], "kinds": ["earthquake"], "includePrivate": False}},
  "updateSink": {"name": "Home Assistant MQTT", "enabled": False,
                 "config": {"brokerUrl": "mqtt://192.168.1.10:1883", "prefix": "foresignal", "changesTopic": True},
                 "secrets": {"password": "REPLACE_ME"}},
  "testSink": {"kind": "earthquake"},
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
  # 109: replace a token's licence-policy floor (null or [] clears it)
  "updateToken": {"licencePolicy": ["no-nc"]},
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
  # 162: test an unsaved provider and list its models; nothing is stored. Without apiKey the stored key of the
  # provider named by id is used (here the collection's ollama provider, which needs none).
  "probeLlmProvider": {"id": "ollama", "type": "ollama", "baseUrl": "http://ollama:11434", "chatModel": "qwen3:14b",
                       "pricing": {"inputUsdPerMillionTokens": 0, "outputUsdPerMillionTokens": 0}},
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
  # 017: advisory only; the reply rates each source and the rule is not changed.
  "analyzeTriggerSources": {"sourceIds": ["usgs-earthquakes", "emsc-seismic", "celestrak-gp"]},
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
  "sampleTrack": {"record": "local-adsb-1/abc123", "products": ["mrms-reflectivity", "gfs-gust"],
                  "maxGap": "PT20M", "units": "metric"},
  # 111: exports, snapshots, holds and offline packs.
  "createExport": {"name": "Texas quakes, last week", "format": "geoparquet", "sources": ["usgs-earthquakes"],
                   "kinds": ["earthquake"], "bbox": [-106.7, 25.8, -93.5, 36.5], "from": "2026-10-09T00:00:00Z",
                   "to": "2026-10-16T00:00:00Z", "timeField": "observed", "mode": "history", "flatten": True,
                   "fields": ["mag", "place"], "personalUse": False},
  "createSnapshot": {"name": "Before the outage", "asOf": "2026-10-15T12:00:00Z", "sources": ["usgs-earthquakes"],
                     "expiresAt": None},
  "createHold": {"name": "Hurricane review", "reason": "Keep the storm's records for the after-action review",
                 "sources": ["usgs-earthquakes"], "kinds": ["earthquake"], "from": "2026-10-01T00:00:00Z", "to": None,
                 "expiresAt": "2027-10-01T00:00:00Z"},
  "createPack": {"name": "Austin field pack", "bbox": [-97.95, 30.1, -97.55, 30.5], "minZoom": 6, "maxZoom": 12,
                 "vector": {"sources": ["usgs-earthquakes"], "kinds": ["earthquake"], "asOf": None},
                 "raster": {"productId": "mrms-reflectivity", "frame": "latest"}},
  # 112: a share link to one record, 7 days by default, at most 50 uses; the plaintext fss_ key is returned once.
  "createShareLink": {"targetType": "record", "targetRef": "usgs-earthquakes/us7000n7tc", "fieldMask": None,
                      "precisionM": None, "expiresAt": None, "maxUses": 50},
  # 113: a PULL peer replicating earthquakes from a friend's instance (its token needs read + federation=true); the
  # remote key is pinned on the first verified /info. A RECEIVE peer (an edge) needs no token: rotate its ingest key.
  "createPeer": {"id": "friend", "name": "Friend's instance", "baseUrl": "https://foresignal.friend.example",
                 "mode": "PULL", "enabled": True, "token": "fsr_REPLACE_WITH_THE_REMOTE_TOKEN",
                 "selection": {"sources": ["usgs-earthquakes"], "kinds": ["earthquake"], "bbox": None, "placeId": None,
                               "maxAge": "7d", "backfill": "none", "live": False, "maxHops": 3, "trackTtl": "10m"},
                 "sameOperator": False, "reliability": "B"},
  "updatePeer": {"name": "Friend's instance", "baseUrl": "https://foresignal.friend.example", "enabled": True,
                 "selection": {"sources": ["usgs-earthquakes"], "kinds": ["earthquake"], "maxAge": "7d", "maxHops": 3},
                 "sameOperator": False, "reliability": "B"},
  # An edge's batch: in practice the federation-push sink builds and signs it (Foresignal-Signature over the exact
  # body); this example shows the shape and is refused with 403 PEER_KEY_MISMATCH without a valid signature.
  "federationIngest": {"items": [{"seq": 1042, "ref": "urn:foresignal:edge-install-id:local-ais-1:244660000",
                                  "sourceId": "local-ais-1", "externalId": "244660000", "kind": "vessel", "version": 3,
                                  "persistReason": "SIGNIFICANT", "historyTier": "TRACK",
                                  "observedAt": "2026-10-16T08:00:00Z", "eventTime": None, "validUntil": None,
                                  "tombstone": False, "geometry": {"type": "Point", "coordinates": [4.89, 52.37]},
                                  "payload": {"name": "EXAMPLE", "sog": 7.2}, "quality": None,
                                  "licence": "local-only-operator",
                                  "provenance": {"origin": {"instanceId": "edge-install-id", "sourceId": "local-ais-1",
                                                            "externalId": "244660000", "seq": 1042,
                                                            "ingestedAt": "2026-10-16T08:00:01Z"},
                                                 "chain": [{"instanceId": "edge-install-id", "sourceId": "local-ais-1",
                                                            "externalId": "244660000", "seq": 1042,
                                                            "ingestedAt": "2026-10-16T08:00:01Z"}]}}]},
  # 114: opt the operator's own phone (an OwnTracks device record) into pattern of life; it is private, so this
  # needs the admin token. Places and deviations appear on pattern-of-life-private.
  "createPolSubject": {"subjectType": "record", "ref": "owntracks-phone/phone", "enabled": True,
                       "params": {"stayRadiusM": 100, "minDwell": "10m", "maxGap": "30m", "eps": 150, "minPts": 3,
                                  "lookback": "28d"}},
  "updatePolPlace": {"label": "home"},
  # A weekday commute. The route comes from the client's own router (Valhalla can exclude polygons, OSRM cannot);
  # Foresignal only buffers and watches it.
  "createTrip": {"name": "Morning commute", "routePlaceId": None,
                 "route": {"type": "LineString", "coordinates": [[-97.7431, 30.2672], [-97.7300, 30.2900],
                                                                 [-97.7150, 30.3200], [-97.6990, 30.3600]]},
                 "bufferMeters": 2000,
                 "schedule": {"type": "recurring", "days": ["MO", "TU", "WE", "TH", "FR"], "departTime": "07:45",
                              "timezone": "America/Chicago"},
                 "expectedDuration": "40m", "leadTime": "60m",
                 "hazardKinds": ["nws-alert", "road-event", "work-zone", "fire-perimeter", "cyclone-cone", "earthquake",
                                 "incident"],
                 "minSeverity": None, "channelIds": [], "anchor": None, "notifyWhenClear": True,
                 "enabled": True},
  "updateTrip": {"name": "Morning commute", "route": {"type": "LineString", "coordinates": [[-97.7431, 30.2672],
                                                                                         [-97.6990, 30.3600]]},
                 "bufferMeters": 3000,
                 "schedule": {"type": "recurring", "days": ["MO", "TU", "WE", "TH", "FR"], "departTime": "07:30",
                              "timezone": "America/Chicago"},
                 "expectedDuration": "45m", "leadTime": "45m", "notifyWhenClear": False},
  "checkTrip": {"dryRun": True, "occurrence": None},
  # 115: rehearse the alert chain: replay the last day of the rule in {{ruleId}} at sixty times speed to the
  # channel in {{channelId}} (and every channel with acceptExercise) as [EXERCISE] alerts. Set dryRun to true to
  # see the projected counts first.
  "createExercise": {"name": "Morning rehearsal", "ruleIds": ["{{ruleId}}"], "tag": None,
                     "from": "2026-10-15T00:00:00Z", "to": "2026-10-16T00:00:00Z", "speed": "x60",
                     "channelIds": ["{{channelId}}"], "enrich": False, "dryRun": True},
  # 115: apply or dismiss the suggestion in {{suggestionId}} (captured from getTriggerSuggestions).
  "applyTuningSuggestion": {"note": "Applied after reviewing a week of feedback"},
  "dismissTuningSuggestion": {"reason": "The night alerts are wanted"},
  # 117: an attention topic that drives gdelt-timelines and wikimedia-pageviews once enabled (each change re-syncs
  # their topics parameter); the GDELT query puts OR'd terms in parentheses.
  "createAttentionTopic": {"id": "bab-el-mandeb-transits", "name": "Bab el-Mandeb transits",
                           "description": "Shipping attention around the Bab el-Mandeb strait",
                           "gdeltQuery": "(\"Bab el-Mandeb\" OR \"Red Sea\") (tanker OR container OR Houthi)",
                           "wikiArticles": [{"project": "en.wikipedia", "article": "Bab-el-Mandeb"}],
                           "keywords": ["Bab el-Mandeb", "Red Sea", "Houthi"], "regionCodes": ["YE", "DJ", "ER"],
                           "themes": ["shipping"], "enabled": True},
  # The body id must equal the path's topic id and match ^[a-z0-9-]{3,48}$, which a {{variable}} cannot, so it is the
  # literal default of {{attentionTopicId}} (the shipped red-sea-shipping example); change both together.
  "updateAttentionTopic": {"id": "red-sea-shipping", "name": "Red Sea shipping",
                           "description": "Shipping disruption in the Red Sea and the Bab el-Mandeb strait.",
                           "gdeltQuery": "(\"Red Sea\" OR \"Bab el-Mandeb\") (shipping OR tanker OR vessel OR Houthi)",
                           "wikiArticles": [{"project": "en.wikipedia", "article": "Bab-el-Mandeb"},
                                            {"project": "en.wikipedia", "article": "Red_Sea"}],
                           "keywords": ["Red Sea", "Bab el-Mandeb", "Houthi"], "regionCodes": ["YE", "ER", "DJ"],
                           "themes": ["shipping", "maritime-security"], "enabled": True},
  # 117: backfill the CFTC Commitments of Traders history down to 2010 (needs cftc-cot enabled; budget-limited and
  # resumable, so it may take several runs).
  "backfillCftcCot": {"from": "2010-01-01"},
  # 118: a situation created by hand from one pinned record (replace the ref and seq with a record version of
  # yours), then a member added, a merge into {{otherSituationId}} (the older of the two keeps its id) and a split of
  # the member in {{situationMemberId}} into a new situation.
  "createSituation": {"title": "Red Sea shipping disruption", "theme": "maritime", "status": "emerging",
                      "regionCodes": ["YE", "ER", "DJ"], "topicIds": ["{{attentionTopicId}}"],
                      "members": [{"memberType": "record", "ref": "{{sourceId}}/{{externalId}}", "seq": 1,
                                   "role": "core"}]},
  "updateSituation": {"title": "Red Sea and Bab el-Mandeb shipping disruption", "severityOverride": "warning",
                      "reason": "Insurers suspended cover", "version": 0},
  "addSituationMember": {"memberType": "alert", "ref": "{{alertId}}", "role": "core"},
  # Situation and member ids are ULIDs (^[0-9A-HJKMNP-TV-Z]{26}$), which a {{variable}} cannot match: replace the
  # example ULID with the id in {{otherSituationId}} (merge) or {{situationMemberId}} (split).
  "mergeSituation": {"intoId": "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "reason": "The same storyline seen from two chokepoints"},
  "splitSituation": {"memberIds": ["01JA8Q3V7S2K9W4T6Y1B5N0M3C"], "title": "Gulf of Aden piracy",
                     "theme": "maritime"},
  "testSituationLinker": {"fromSeq": 0, "toSeq": 10000},
  # 119: register the CFTC WTI managed-money net position as an operator analysis series with a watch pair (Gulf
  # conflict events checked against it live), run a batch event study of maritime disruptions in Yemen against the
  # FRED WTI spot price (needs fred-series), and dismiss the finding in {{findingId}}.
  "upsertAnalysisSeries": {"ref": "series:cftc-cot/cot.067651.disaggregated.mm_net", "enabled": True,
                           "detectors": ["changepoint-cusum", "changepoint-bocpd"], "frequency": "week",
                           "family": "markets",
                           "watchPairs": [{"eventFamily": "conflict", "regionCodes": ["IR", "SA", "YE", "AE"],
                                           "minEffect": 2.0, "releaseLagDays": 3}]},
  "createEventStudy": {"name": "WTI around Yemen maritime disruptions",
                       "selector": {"intelEventType": "maritime-disruption", "regionCodes": ["YE"]},
                       "target": "series:fred-series/fred.DCOILWTICO", "from": "2025-01-01T00:00:00Z",
                       "to": "2026-10-01T00:00:00Z"},
  "updateFinding": {"status": "dismissed", "reason": "A known holiday effect, not a change in activity"},
  # 120: register an operator precursor candidate (GNSS interference alerts in Yemen followed by maritime disruption
  # events in the same country within three days), and retire the precursor in {{precursorId}}. Mining itself
  # (minePrecursors) takes no body.
  "createPrecursorCandidate": {"name": "GNSS interference before Red Sea disruptions", "kind": "event",
                               "antecedent": {"selector": {"alertRuleTag": "gnss-interference"},
                                              "label": "GNSS interference alerts"},
                               "consequent": {"selector": {"intelEventType": "maritime-disruption"},
                                              "label": "maritime disruption events"},
                               "spatial": {"rule": "sameRegion", "regionLevel": "country"},
                               "lagMin": "P0D", "lagMax": "P3D", "regionCodes": ["YE"]},
  "updatePrecursor": {"status": "retired", "reason": "The pattern reflects a reporting artifact, not activity"},
  # 121: import USGS ComCat M4.5+ history (public domain) as weekly counts, ask for the 28-day base rate of battles
  # in Yemen (and its conditional form after explosions within 14 days), store a custom rate, and find region-period
  # analogs for Yemen's last two weeks with the outcome "battles within 30 days".
  "createReferenceImport": {"dataset": "usgs-comcat", "params": {"yearFrom": 1990, "yearTo": 2025,
                                                                 "minMagnitude": 4.5}},
  "queryBaseRate": {"family": "conflict:battles", "regionCodes": ["YE"], "window": "P28D",
                    "condition": {"family": "conflict:explosions-remote", "withinDays": 14}},
  "createBaseRate": {"name": "Battles in the Horn of Africa per 28 days",
                     "definition": {"family": "conflict:battles", "regionCodes": ["ET", "SO", "ER", "DJ"],
                                    "window": "P28D"}},
  "queryAnalogs": {"regionCode": "YE", "from": "2026-10-01T00:00:00Z", "to": "2026-10-15T00:00:00Z", "k": 10,
                   "outcome": "conflict:battles/P30D"},
  # 122: a binary question resolved from Foresignal's own data (battles in Yemen within four weeks, with region codes
  # so base rates and detectability apply), a forecast, a manual resolution, an annulment and a retrospective
  # evaluation of the base-rate baseline. The other resolver types and forecast shapes are VARIANTS below.
  "createForecastQuestion": {"title": "Will battles be recorded in Yemen between 2026-11-01 and 2026-11-29?",
                             "type": "binary",
                             "resolutionCriteria": "Yes if Foresignal records at least one conflict:battles event"
                                                   " tagged YE between 2026-11-01T00:00Z and 2026-11-29T00:00Z;"
                                                   " no otherwise, provided coverage and detectability are adequate"
                                                   " (else annulled).",
                             "resolver": {"type": "event-exists", "selector": {"referenceFamily": "conflict:battles"},
                                          "area": {"regionCodes": ["YE"]},
                                          "window": {"start": "2026-11-01T00:00:00Z", "end": "2026-11-29T00:00:00Z"}},
                             "closesAt": "2026-11-01T00:00:00Z", "resolveBy": "2026-11-29T00:00:00Z",
                             "category": "conflict", "tags": ["yemen"], "dedupKey": "postman:ye-battles-2026-11"},
  "updateForecastQuestion": {"title": "Will battles be recorded in Yemen in November 2026?", "tags": ["yemen", "monthly"]},
  "annulForecastQuestion": {"reason": "The question duplicates another one on the board."},
  "resolveForecastQuestion": {"outcome": "yes", "evidence": ["alert:1842"],
                              "note": "Confirmed by the operator from the cited alert."},
  "submitForecast": {"probability": 0.72,
                     "rationale": "Base rate: battles occurred in 0.81 of 28-day windows in Yemen (n = 104); adjusted"
                                  " down for the current lull.",
                     "citations": [{"ref": "situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C", "quote": None, "claim": None}]},
  "updateForecaster": {"active": True, "config": {"maxAgeDays": 7, "eta": 2.0, "shrink": 20.0,
                                                  "externalMinResolved": 30}},
  "createForecastEvaluation": {"forecasterId": "base-rate", "from": "2026-01-01T00:00:00Z",
                               "to": "2026-10-01T00:00:00Z", "categories": ["conflict"], "maxQuestions": 500},
  # 123: the synthetic suite over the detectors and the base-rate forecaster with the fixed seed, and a historical
  # case: a known disruption the change-point detector should find in a chokepoint's daily transits.
  "createBenchmarkRun": {"suite": "synthetic", "components": ["change-point", "co-occurrence", "precursor",
                                                              "forecaster:base-rate"], "seed": 1230001},
  "createBenchmarkCase": {"name": "Bab el-Mandeb transits fall, early 2024",
                          "description": "Daily transits through the strait fell sharply as ships rerouted.",
                          "windowFrom": "2024-01-01T00:00:00Z", "windowTo": "2024-02-15T00:00:00Z",
                          "regionCodes": ["YE", "DJ", "ER"],
                          "expectations": {"findings": [{"findingType": "change-point", "present": True,
                                                         "seriesRef": "series:imf-portwatch-chokepoints/chokepoint4",
                                                         "from": "2024-01-01T00:00:00Z",
                                                         "to": "2024-01-31T00:00:00Z", "minSeverity": "notable"}],
                                           "notes": "Operator case from the PortWatch record."}},
  # 125: the outcome set of the worked example (Intelligence-Design §13) with its indicators, assumptions and a
  # machine-resolved ledger question; an added indicator, hypothesis and evidence row; a review; a draft.
  "createHypothesisSet": {
      "question": "Saudi or coalition kinetic response against Houthi targets within 7 days?",
      "description": "Outcome set of the worked example: after the strike on Riyadh, does a response follow, and"
                     " when?",
      "kind": "exclusive", "cadence": "daily", "horizon": "2026-12-31", "regionCodes": ["SA", "YE"],
      "themes": ["conflict"], "private": False,
      "hypotheses": [
          {"label": "within 72 h", "statement": "A Saudi or coalition kinetic response against Houthi targets"
                                                " follows within 72 hours.",
           "type": "outcome", "prior": 0.25, "ledgerOutcome": "yes",
           "priorBasis": {"type": "base-rate", "ref": "{{baseRateId}}",
                          "rationale": "Responses to comparable strikes from the reference-import base rates."}},
          {"label": "3-7 days", "statement": "The response follows after 72 hours but within 7 days.",
           "type": "outcome", "prior": 0.2, "ledgerOutcome": "yes",
           "priorBasis": {"type": "analogs", "ref": "situation-analogs:{{situationId}}",
                          "rationale": "The outcome distribution of the situation's historical analogs."}},
          {"label": "none in 7 days", "statement": "No kinetic response follows within 7 days.",
           "type": "outcome", "prior": 0.5, "ledgerOutcome": "no",
           "priorBasis": {"type": "operator", "ref": None,
                          "rationale": "Restraint has followed most recent strikes during negotiations."}},
          {"label": "other", "statement": "Other / none of these (for example a response by another party).",
           "type": "other", "prior": 0.05, "ledgerOutcome": "no",
           "priorBasis": {"type": "operator", "ref": None,
                          "rationale": "Residual mass for outcomes the other hypotheses do not describe."}}],
      "indicators": [
          {"description": "Airspace restrictions over southern Saudi Arabia",
           "signal": {"type": "intelEvent", "intelEvent": {"type": "aviation-anomaly", "regionCodes": ["SA"]}},
           "signalFamily": "aviation-ops", "credibility": "H", "relevance": "H",
           "expectedBy": "2026-12-03T00:00:00Z",
           "lr": {"within 72 h": {"class": "high", "direction": "supports",
                                  "rationale": "Closures usually precede strike packages by hours."},
                  "3-7 days": {"class": "medium", "direction": "supports"},
                  "none in 7 days": {"class": "high", "direction": "weakens"}}},
          {"description": "Maritime disruption near the Bab el-Mandeb",
           "signal": {"type": "intelEvent", "intelEvent": {"type": "maritime-disruption",
                                                           "regionCodes": ["YE", "DJ", "ER"]}},
           "signalFamily": "ais-behaviour", "credibility": "M", "relevance": "H",
           "lr": {"within 72 h": {"class": "low", "direction": "supports"},
                  "3-7 days": {"class": "low", "direction": "supports"},
                  "none in 7 days": {"class": "low", "direction": "weakens"}}},
          {"description": "Conflict activity rising in Yemen",
           "signal": {"type": "series", "series": {"ref": "rollup:day/*/conflict/YE", "direction": "up"}},
           "signalFamily": "conflict", "credibility": "M", "relevance": "M",
           "lr": {"within 72 h": {"class": "medium", "direction": "supports"},
                  "none in 7 days": {"class": "medium", "direction": "weakens"}}},
          {"description": "Military air and tanker activity over the Red Sea (operator-observed)",
           "signal": {"type": "manual"}, "signalFamily": "aviation-military", "credibility": "M", "relevance": "H",
           "lr": {"within 72 h": {"class": "high", "direction": "supports"},
                  "none in 7 days": {"class": "medium", "direction": "weakens"}}}],
      "assumptions": [
          {"text": "The coalition retains the capacity to strike Houthi targets at short notice.", "linchpin": True,
           "rationale": "Air assets remain deployed in the region.",
           "wouldBeWrongIf": "Assets are withdrawn or grounded.", "status": "solid"},
          {"text": "Negotiations do not produce a ceasefire this week.", "linchpin": False, "status": "caveated"}],
      "ledgerQuestion": {"resolver": {"type": "event-exists",
                                      "params": {"selector": {"referenceFamily": "conflict:battles"},
                                                 "area": {"regionCodes": ["YE"]},
                                                 "window": {"start": "2026-11-01T00:00:00Z",
                                                            "end": "2026-12-31T00:00:00Z"}}},
                         "resolutionCriteria": "Yes if Foresignal records a battles event in Yemen attributed to a"
                                               " Saudi or coalition response by the end of the horizon; no"
                                               " otherwise.",
                         "category": "conflict"}},
  "updateHypothesisSet": {"description": "Outcome set of the worked example, reviewed daily.", "cadence": "daily",
                          "version": 0},
  "addHypothesis": {"label": "proxy response", "statement": "A response comes from a coalition partner rather than"
                                                            " Saudi forces.",
                    "type": "outcome", "prior": 0.05, "ledgerOutcome": "yes",
                    "priorBasis": {"type": "operator", "ref": None,
                                   "rationale": "Partners have responded on the coalition's behalf before."}},
  "updateHypothesis": {"statement": "A Saudi or coalition kinetic response against Houthi targets follows within"
                                    " 72 hours of the strike."},
  "createIndicator": {"description": "Situation escalates to critical",
                      "signal": {"type": "situation", "situation": {"situationId": "01JA8Q3V7S2K9W4T6Y1B5N0M3C",
                                                                    "minSeverity": "critical"}},
                      "signalFamily": "conflict", "credibility": "M", "relevance": "M",
                      "lr": {"{{hypothesisId}}": {"class": "medium", "direction": "supports",
                                                  "rationale": "Escalation precedes most responses."}}},
  "updateIndicator": {"credibility": "H", "expectedBy": "2026-12-10T00:00:00Z"},
  "addAchEvidence": {"source": "record", "ref": "record:{{sourceId}}/{{externalId}}",
                     "description": "Report of strike preparations at a coalition air base.", "credibility": "M",
                     "relevance": "H", "citations": []},
  "createKeyAssumption": {"text": "Houthi forces do not strike again before a response.", "linchpin": False,
                          "rationale": "A second strike would change the decision calculus.",
                          "wouldBeWrongIf": "A second strike is reported.", "status": "solid"},
  "updateKeyAssumption": {"status": "caveated", "rationale": "Reports of a second launch are unconfirmed."},
  "reviewHypothesisSet": {"cause": "review", "rationale": "Daily review: no new indicators; the airspace closure"
                                                           " remains unconfirmed and the priors stand.",
                          "citations": []},
  "draftHypothesisSet": {"text": "Watch whether the Red Sea situation spreads to Hormuz",
                         "regionCodes": ["OM", "IR", "AE"]},
  # 126: an operator-defined regional index (PUT {{indexId}}; the built-ins instability, maritime-risk and
  # infrastructure-stress are seeded disabled: PUT them with "enabled": true to compute them), and the weights change
  # that applies the version a fit proposed (fitRegionalIndex has no body; its 202 is the queued fit).
  "putRegionalIndex": {"name": "Unrest pressure", "description": "Conflict events and internet outages per country.",
                       "components": [
                           {"key": "conflict", "ref": "rollup:day/*/conflict-event/{region}", "transform": "pctrank",
                            "window": "P28D", "direction": 1},
                           {"key": "outages", "ref": "rollup:day/*/internet-outage/{region}", "transform": "pctrank",
                            "window": "P7D", "direction": 1, "floor": None}],
                       "weights": {"conflict": 0.7, "outages": 0.3},
                       "floors": [{"id": "ucdp-war", "description": "A UCDP war in the last 12 months keeps the index"
                                                                   " at 70 or more.",
                                   "condition": {"type": "reference", "family": "conflict:battles", "source": "ucdp",
                                                 "minFatalities": 1000, "withinDays": 365},
                                   "minValue": 70}],
                       "target": "conflict:battles", "enabled": True, "autoApplyFittedWeights": False},
  "applyRegionalIndexWeights": {"source": "fitted", "version": 2,
                                "rationale": "The fit beat the seasonal-naive outlook out of sample."},
  # 098: assert an IMO number (check digit valid) on the entity in {{entityId}}, and merge another entity into it
  # (replace the example ULID with the other entity's id; entity ids are ULIDs, which a {{variable}} cannot match).
  "addEntityKey": {"keyType": "imo", "keyValue": "9074729", "confidence": 1.0,
                   "validFrom": "2026-01-01T00:00:00Z", "validTo": None, "force": False},
  "mergeEntities": {"otherId": "01JA8Q3V7S2K9W4T6Y1B5N0M3C"},
  # 101: an operator intelligence event built from one pinned record version (replace the seq with the seq of the
  # version of {{sourceId}}/{{externalId}} you mean; sourceId and externalId must match that version), a second
  # member, a merge into another event (replace the example ULID with its id), a recipe dry run over the first
  # 10000 seqs, an investigation with an alert in it, and the GHSL population pack import.
  "createIntelEvent": {"type": "maritime-disruption", "title": "Tanker diverted around the Bab el-Mandeb",
                       "severity": "warning", "origin": "operator", "startTime": "2026-10-15T06:00:00Z",
                       "notes": "Diversion reported after a nearby strike.",
                       "members": [{"memberType": "record", "role": "supporting", "sourceId": "{{sourceId}}",
                                    "externalId": "{{externalId}}", "seq": 1}]},
  "addIntelEventMember": {"memberType": "record", "role": "context", "sourceId": "{{sourceId}}",
                          "externalId": "{{externalId}}", "seq": 2},
  "mergeIntelEvent": {"intoId": "01JA8Q3V7S2K9W4T6Y1B5N0M3C"},
  "testRecipe": {"fromSeq": 0, "toSeq": 10000},
  "createInvestigation": {"title": "Red Sea shipping disruption, October 2026",
                          "summary": "Diversions and strikes around the Bab el-Mandeb.", "tags": ["red-sea", "shipping"]},
  "addInvestigationItem": {"itemType": "alert", "ref": "{{alertId}}", "note": "The first strike alert."},
  "importPopulationPack": {"path": "packs/ghsl/GHS_POP_E2025_GLOBE_R2023A_4326_30ss"},
  # 102: an analyst profile that reviews maritime and infrastructure events every 30 minutes over the last week, a
  # note marked read, and feedback on a note and a proposal.
  "createAnalystProfile": {"name": "Red Sea watch", "enabled": True, "schedule": {"every": "30m"},
                           "scope": {"eventTypes": ["maritime-disruption", "infrastructure-disruption"],
                                     "minSeverity": "warning"},
                           "lookback": "7d", "instructions": "Focus on shipping through the Bab el-Mandeb.",
                           "effort": "low", "maxRunsPerDay": 48, "allowActions": False,
                           "notifyMinPriority": "normal"},
  "updateAnalystProfile": {"name": "Red Sea watch", "enabled": True,
                           "schedule": {"cron": "0 */2 * * *", "timezone": "America/Chicago"},
                           "scope": {"eventTypes": ["maritime-disruption"], "minSeverity": "warning"},
                           "lookback": "3d", "effort": "medium", "maxRunsPerDay": 12, "allowActions": False,
                           "notifyMinPriority": "high"},
  "updateAnalystNote": {"status": "read"},
  "analystNoteFeedback": {"rating": "useful", "comment": "Caught the diversion before the news did."},
  "analystProposalFeedback": {"rating": "not_useful", "comment": "Already covered by an existing watch."},
  # 103: a rule backtest of the rule in {{ruleId}} over the first half of October, and a precursor test: alerts of
  # that rule followed by maritime disruption events within six hours and 100 km (lags are ISO-8601 durations).
  "createBacktest": {"type": "rule", "targetId": "{{ruleId}}", "from": "2026-10-01T00:00:00Z",
                     "to": "2026-10-15T00:00:00Z"},
  "createPrecursorTest": {"name": "GNSS jamming before maritime disruption",
                          "statement": "GPS jamming precedes maritime disruption within 6 h within 100 km",
                          "antecedent": {"alertRuleId": "{{ruleId}}"},
                          "consequent": {"intelEventType": "maritime-disruption"},
                          "minLag": "PT0S", "maxLag": "PT6H", "spatial": {"withinKm": 100}},
  "updatePrecursorTest": {"name": "GNSS jamming before maritime disruption",
                          "statement": "GPS jamming precedes maritime disruption within 12 h in the same country",
                          "antecedent": {"alertRuleId": "{{ruleId}}"},
                          "consequent": {"intelEventType": "maritime-disruption"},
                          "minLag": "PT0S", "maxLag": "PT12H", "spatial": {"sameRegion": "country"},
                          "dedupWindow": "PT12H"},
  # 104: tighten plant-to-substation proximity to 3 km and switch off the critical-load rule.
  "updateInfraRules": {"rules": [{"id": "plant-substation", "enabled": True, "params": {"maxKm": 3}},
                                 {"id": "substation-critical-load", "enabled": False}]},
  # 111: archive the USGS source to the local cold archive, keeping the local copy.
  "updateArchiveSettings": {"enabled": True, "sources": ["usgs-earthquakes"], "target": "local", "s3": None,
                            "keepLocal": True},
  # 128: a weekly assessment on Mondays at 07:00 in London over the Red Sea region, with all four roles, three
  # forecaster samples per question and the weekly caps; the replacement narrows it to the analyst and red team;
  # feedback on an assessment; an on-demand assessment of a situation with the analyst only.
  "createAnalysisCycle": {"name": "Red Sea weekly assessment", "cadence": "weekly",
                          "schedule": {"cron": "0 7 * * 1", "timezone": "Europe/London"},
                          "scope": {"regionCodes": ["YE", "SA", "ER", "DJ"], "themes": ["maritime"],
                                    "hypothesisSetIds": "cadence-match"},
                          "roles": ["analyst", "red-team", "forecaster", "proposer"], "samples": 3,
                          "maxQuestions": 40, "maxTokensPerCycle": 1200000, "channelIds": [],
                          "notifyMinPriority": "high", "enabled": True},
  "updateAnalysisCycle": {"name": "Red Sea weekly assessment", "cadence": "weekly",
                          "schedule": {"cron": "0 7 * * 1", "timezone": "Europe/London"},
                          "scope": {"regionCodes": ["YE", "SA"], "hypothesisSetIds": "cadence-match"},
                          "roles": ["analyst", "red-team"], "enabled": True},
  "submitAssessmentFeedback": {"rating": "useful", "comment": "The red-team premortem changed our watch list."},
  "createSituationAssessment": {"roles": ["analyst"], "effort": "medium"},
  # 129: resolve a batch of references, a two-hop subgraph of a situation, a verdict on a finding (agent scope; a
  # note needs only a token), a workspace over the Red Sea with its initial context, an operator relation, shared
  # dependencies of two objects, a viewport summary, an ad-hoc deep link and a one-day share of a workspace.
  "resolveObjects": {"refs": ["situation:{{situationId}}", "finding:{{findingId}}",
                              "record:{{sourceId}}/{{externalId}}"], "asOf": None},
  "queryGraph": {"refs": ["situation:{{situationId}}"], "depth": 2, "types": ["member-of", "evidence-for",
                 "depends-on", "same-as"], "limit": 500},
  "createAnnotation": {"kind": "verdict", "verdict": "confirmed", "body": "Matches the port authority notice.",
                       "tags": ["port"], "applyStatus": False},
  "createWorkspace": {"name": "Red Sea watch", "members": {},
                      "context": {"selectedRefs": ["situation:{{situationId}}"], "focusRef": "situation:{{situationId}}",
                                  "timeRange": {"start": "2026-10-10T00:00:00Z", "end": None}, "asOf": None,
                                  "viewport": {"bbox": [32.0, 11.0, 45.0, 21.0]}, "filters": {"kinds": ["vessel"]},
                                  "layers": ["vessel", "observability:ais-behaviour"]},
                      "layout": {"panels": ["globe", "timeline"]}, "private": False},
  "createRelation": {"fromRef": "finding:{{findingId}}", "toRef": "situation:{{situationId}}",
                     "type": "related-to", "confidence": 0.7, "label": "likely"},
  "findCommonDependencies": {"refs": ["situation:{{situationId}}", "record:{{sourceId}}/{{externalId}}"]},
  "summarizeContext": {"viewport": {"center": [43.1, 14.8], "radiusKm": 300},
                       "timeRange": {"start": "2026-10-16T00:00:00Z", "end": None},
                       "filters": {"minSeverity": "warning"}, "layers": ["vessel", "aircraft"]},
  "createContextLink": {"context": {"focusRef": "situation:{{situationId}}",
                                    "viewport": {"center": [43.1, 14.8], "radiusKm": 150}}},
  "shareWorkspace": {"expiresAt": None, "maxUses": 50},
  # 130: a principal and its token (admin), a group, a share and an access list (agent; {{objectRef}} must name a
  # work product such as investigation:{id}), a tag and a bulk assignment, a comment reply, an evidence item graded
  # with the Admiralty system, a saved search and the caller's notification preferences.
  "createPrincipal": {"handle": "jsmith", "displayName": "Jo Smith", "kind": "person",
                      "email": "jo.smith@example.org", "timezone": "Europe/London",
                      "organization": "Example Analysis Ltd"},
  "createPrincipalToken": {"name": "jsmith-laptop", "scope": "read", "expiresAt": None},
  "createGroup": {"name": "Red Sea desk", "description": "Maritime analysts",
                  "members": [{"principalId": "{{principalId}}", "role": "member"}]},
  "addGroupMembers": {"members": [{"principalId": "{{principalId}}", "role": "manager"}]},
  "shareObject": {"grantees": ["group:{{groupId}}", "principal:{{principalId}}"], "role": "commenter",
                  "message": "Please review the shipping section.", "expiresAt": None},
  "replaceObjectAcl": {"visibility": "shared", "tlp": "AMBER",
                       "entries": [{"grantee": "group:{{groupId}}", "role": "editor", "expiresAt": None}]},
  "createTag": {"name": "red-sea", "namespace": "shared", "color": "#2f7fd9",
                "description": "Red Sea shipping disruption", "visibility": "instance", "tlp": "GREEN"},
  "assignTag": {"refs": ["situation:{{situationId}}", "finding:{{findingId}}", "record:{{sourceId}}/{{externalId}}"]},
  "unassignTag": {"refs": ["finding:{{findingId}}"]},
  "replyToAnnotation": {"body": "Agreed, @jsmith please check the port notice.", "private": False},
  "resolveAnnotation": {"resolved": True},
  "createEvidenceItem": {"title": "Port authority notice of berth closure",
                         "description": "Scanned notice received from a shipping agent.",
                         "attachmentIds": ["{{attachmentId}}"], "url": "https://example.org/notice",
                         "sourceDescription": "Shipping agent in Aden", "sourceReliability": "B",
                         "informationCredibility": 2, "citedRefs": ["situation:{{situationId}}"],
                         "geometry": {"type": "Point", "coordinates": [45.0, 12.8]},
                         "eventTime": "2026-10-16T08:00:00Z", "tagIds": ["{{tagId}}"], "visibility": "private",
                         "tlp": "AMBER"},
  "createSavedSearch": {"name": "Red Sea port closures",
                        "query": {"types": ["record", "evidence-item"], "text": "port closure",
                                  "area": {"bbox": "32,11,45,21"}, "timeWindow": {"relative": "P7D"},
                                  "tags": []},
                        "visibility": "private", "tlp": "AMBER", "notify": True},
  "replaceMyNotificationPreferences": {"items": [{"eventTypes": ["mention", "share", "comment-reply"],
                                                  "channel": {"type": "ntfy",
                                                              "config": {"topic": "jsmith-foresignal"}},
                                                  "quietHours": {"start": "22:00", "end": "07:00"},
                                                  "enabled": True}]},
  "putWorkspaceLayout": {"panels": ["globe", "timeline", "dossier"], "globe": {"pitch": 45}},
  # 131: the Middle East example with five layers (conflict events, military aircraft and vessels near the Gulf and
  # the Red Sea, NOTAMs and TFRs, GPS-jamming cells and the linked situation), created proposed; then activation with a
  # one-week backfill, a further layer, a suggestion from a situation, an export and moving the thread read cursor.
  "createChronicle": {"name": "Middle East war 2026",
                      "description": "Conflict, military movements, airspace, jamming and the linked situation.",
                      "visibility": "private", "tlp": "AMBER",
                      "area": {"regionCodes": ["IL", "LB", "SY", "IQ", "IR", "YE", "SA"],
                               "geometry": {"type": "Polygon", "coordinates": [[[32.0, 12.0], [60.0, 12.0],
                                                                                [60.0, 38.0], [32.0, 38.0],
                                                                                [32.0, 12.0]]]},
                               "bufferKm": 50},
                      "retention": "forever", "sizeCapBytes": 21474836480,
                      "situationIds": ["{{situationId}}"], "hypothesisSetIds": [], "topicIds": [],
                      "layers": [
                          {"name": "Conflict events", "selector": {"type": "records", "sources": ["acled", "ucdp-ged"],
                                                                   "kinds": ["conflict-event"]},
                           "capture": {"mode": "full"}},
                          {"name": "Military aircraft", "selector": {"type": "records", "sources": ["adsblol"],
                                                                     "kinds": ["aircraft"],
                                                                     "conditions": [{"path": "military", "op": "==",
                                                                                     "value": True}]},
                           "capture": {"mode": "downsampled", "interval": "PT60S"}},
                          {"name": "Vessels in the Gulf and the Red Sea",
                           "selector": {"type": "records", "sources": ["aisstream"], "kinds": ["vessel"]},
                           "areaOverride": {"geometry": {"type": "MultiPolygon", "coordinates": [
                               [[[32.0, 12.0], [44.0, 12.0], [44.0, 30.0], [32.0, 30.0], [32.0, 12.0]]],
                               [[[47.0, 23.0], [57.0, 23.0], [57.0, 31.0], [47.0, 31.0], [47.0, 23.0]]]]}},
                           "capture": {"mode": "downsampled", "interval": "PT120S"}},
                          {"name": "NOTAMs, TFRs and GPS jamming",
                           "selector": {"type": "records", "sources": ["faa-notam", "faa-tfr", "gpsjam-daily"]},
                           "capture": {"mode": "events-only"}},
                          {"name": "Situation and members", "selector": {"type": "situations",
                                                                         "situationIds": ["{{situationId}}"]},
                           "capture": {"mode": "full"}}],
                      "activate": False},
  "activateChronicle": {"backfill": {"from": "2026-10-10T00:00:00Z"}},
  "createChronicleLayer": {"name": "Energy prices", "selector": {"type": "series",
                                                                 "refs": ["series:eia-steo/BREPUUS"]},
                           "capture": {"mode": "full"}, "enabled": True},
  "suggestChronicle": {"situationId": "{{situationId}}"},
  "exportChronicle": {"format": "geoparquet", "layers": [], "from": None, "to": None},
  "markChronicleThreadRead": {"upToId": 0},
  # 132: a report drafted from the shared tag (situation-brief template, deterministic, no LLM), a block added, the
  # retraction, a send to a principal and a group here, a read-only link, and a signed package for a principal on
  # another instance (delivered to its 113 peer).
  "createReport": {"title": "Red Sea weekly", "subtitle": "Shipping disruption, week 42", "tlp": "AMBER",
                   "visibility": "private", "source": {"type": "tag", "ref": "tag:{{tagId}}"},
                   "template": "custom", "blocks": [{"type": "markdown", "markdown": "Analyst notes."}]},
  "draftReport": {"source": {"type": "tag", "ref": "tag:{{tagId}}"}, "template": "situation-brief",
                  "title": "Red Sea weekly", "narrative": False},
  "createReportBlock": {"type": "bluf", "judgments": [
      {"text": "Coalition response within 7 days.", "probability": 0.7,
       "hypothesisSetRef": "hypothesis-set:{{hypothesisSetId}}", "confidence": "moderate"}]},
  "retractReport": {"reason": "Superseded by the 06:00 imagery pass."},
  "sendReport": {"principals": ["{{principalId}}"], "groups": ["{{groupId}}"], "channels": [],
                 "message": "Weekly report for your review."},
  "createReportShareLink": {"expiresAt": None},
  "createExchangePackage": {"objectRef": "report:{{reportId}}",
                            "recipients": [{"instanceId": "fs-eu-1", "principals": ["jsmith"]}],
                            "message": "Week 42 assessment for your desk.", "deliver": "peer"},
  "receiveExchangeReceipt": {"packageId": "{{packageId}}", "type": "acknowledged", "receiverInstanceId": "fs-eu-1",
                             "principalHandle": "jsmith", "at": "2026-10-17T09:00:00Z"},
  # 134: the owner makes a chronicle discoverable on this instance (title and owner stay hidden), asks for access to
  # a related chronicle with a share offer, grants a request, and installs a network salt agreed with the peers.
  "setChronicleDiscoverability": {"discoverability": "instance", "revealTitle": False, "revealOwner": False},
  "requestChronicleAccess": {"message": "I follow the same front; may I see your chronicle?",
                             "offerShare": "{{chronicleId}}"},
  "grantChronicleAccessRequest": {"role": "viewer", "expiresAt": None, "deliverAs": "chronicle-extract",
                                  "message": None},
  "setChronicleNetworkSalt": {"networkId": "gulf-analysts", "saltRef": "{{networkSalt}}"},
  # 134: a peer's sketch batch (the 113 peer channel signs it; this unsigned example shows the shape and withdraws
  # one sketch).
  "receiveChronicleSketches": {"instanceId": "fs-eu-1", "networkId": "gulf-analysts", "sketches": [],
                               "withdrawn": ["sk-gulf-0001"]},
  # 136: merge another narrative into this one (replace the example ULID with its id), and split member posts off
  # into a new narrative.
  "mergeNarratives": {"narrativeIds": ["01JA8Q3V7S2K9W4T6Y1B5N0M3C"],
                      "reason": "The same port-closure story on two platforms."},
  "splitNarrative": {"postRefs": ["record:bluesky-jetstream/bluesky%3Apost-1"],
                     "reason": "These posts are about a different port."},
  # 137: a manual cue around Yemen for the last day, capped at half a dollar.
  "createSocialCue": {"regionCode": "YE", "terms": ["Bab el-Mandeb"],
                      "window": {"start": "2026-10-17T00:00:00Z", "end": None}, "maxCostUsd": 0.5},
  # 138: bind a provider's transits stream to a series, and link a provider question to a ledger question.
  "createExternalBinding": {"streamId": "transits", "target": "series", "config": {}, "enabled": True,
                            "backfill": True},
  "updateExternalBinding": {"streamId": "transits", "target": "series", "config": {}, "enabled": False,
                            "backfill": False},
  "createExternalQuestionLink": {"streamId": "questions", "externalQuestionId": "pm-hormuz-closure",
                                 "questionId": "{{questionId}}", "orientation": "same"},
  # 150: two upstream pulls at once across every source (the default).
  "updateCollectorSettings": {"maxConcurrentPulls": 2},
  # 155: name an external forecaster (an id of its own; config pins the provider stream), ask it for a forecast now,
  # and the provider's asynchronous answer.
  "registerExternalForecaster": {"id": "timesfm-transits", "name": "TimesFM transits",
                                 "config": {"sourceId": "ext-timesfm", "streamId": "transits"}},
  "requestAnalysis": {"forecasterId": "timesfm-transits", "questionId": "{{questionId}}",
                      "orientation": "same", "horizon": "P7D"},
  "analysisCallback": {"correlationId": "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "probability": 0.61},
  # 157: a relationship recipe joining companies that share a registered address.
  "createRecipe": {"id": "shell-companies", "enabled": True,
                   "definition": {"type": "custom", "seed": {"kinds": ["company"]},
                                  "joins": [{"kinds": ["company"], "sharedAttribute": "registeredAddress"}],
                                  "quietPeriod": "6h"}},
  # 158: who owns and controls an organization, three hops each way (replace the example ULID with an entity id).
  "ownershipChain": {"refs": ["entity:01JA8Q3V7S2K9W4T6Y1B5N0M3C"], "depth": 3, "direction": "both",
                     "types": ["owns", "controls", "beneficial-owner"], "minConfidence": 0.5},
  # Vessels in a box around Rotterdam's approaches, against one subject vessel: the 20 closest approaches within
  # one nautical mile in the next hour.
  "cpaBatch": {"subject": "aisstream/244660000", "filter": {"kinds": ["vessel"], "sources": [],
                                                           "bbox": [3.9, 51.8, 4.6, 52.1]},
               "horizon": "1h", "maxDcpaM": 1852, "top": 20},
}

# test scripts: capture ids / assert status
CAPTURE = {
  "createExport": "const j = pm.response.json(); pm.collectionVariables.set('exportId', j.id);",
  "createShareLink": "const j = pm.response.json(); pm.collectionVariables.set('shareLinkId', j.id); pm.collectionVariables.set('key', j.key);",
  "createPeer": "const j = pm.response.json(); pm.collectionVariables.set('peerId', j.id);",
  "createPolSubject": "const j = pm.response.json(); pm.collectionVariables.set('polSubjectId', j.id);",
  "listPolPlaces": "const j = pm.response.json(); if (j.items && j.items.length) pm.collectionVariables.set('polPlaceId', j.items[0].id);",
  "createTrip": "const j = pm.response.json(); pm.collectionVariables.set('tripId', j.id);",
  "createExercise": "const j = pm.response.json(); pm.collectionVariables.set('exerciseId', j.id);",
  "getTriggerSuggestions": "const j = pm.response.json(); if (j.suggestions && j.suggestions.length) pm.collectionVariables.set('suggestionId', j.suggestions[0].id);",
  "applyTuningSuggestion": "const j = pm.response.json(); pm.collectionVariables.set('tuningLogId', j.id);",
  # 116: the first canonical event, the manifest of the newest anomaly and the replay job.
  "listCanonicalEvents": "const j = pm.response.json(); if (j.items && j.items.length) pm.collectionVariables.set('canonicalEventId', j.items[0].id);",
  "listAnomalies": "const j = pm.response.json(); const a = (j.items || []).find(i => i.manifestId); if (a) pm.collectionVariables.set('manifestId', a.manifestId);",
  "replayManifest": "const j = pm.response.json(); pm.collectionVariables.set('jobId', j.jobId);",
  # 118: the created situation and the added member.
  "createSituation": "const j = pm.response.json(); pm.collectionVariables.set('situationId', j.id);",
  "addSituationMember": "const j = pm.response.json(); pm.collectionVariables.set('situationMemberId', j.id);",
  # 119: the newest finding and the event-study job.
  "listFindings": "const j = pm.response.json(); if (j.items && j.items.length) pm.collectionVariables.set('findingId', j.items[0].id);",
  "createEventStudy": "const j = pm.response.json(); pm.collectionVariables.set('jobId', j.id);",
  # 120: the newest precursor, batch and candidate, and the mining job.
  "listPrecursors": "const j = pm.response.json(); if (j.items && j.items.length) pm.collectionVariables.set('precursorId', j.items[0].id);",
  "listPrecursorBatches": "const j = pm.response.json(); if (j.items && j.items.length) pm.collectionVariables.set('precursorBatchId', j.items[0].id);",
  "createPrecursorCandidate": "const j = pm.response.json(); pm.collectionVariables.set('precursorCandidateId', j.id);",
  "minePrecursors": "const j = pm.response.json(); pm.collectionVariables.set('jobId', j.id);",
  "retestPrecursors": "const j = pm.response.json(); pm.collectionVariables.set('jobId', j.id);",
  # 121: the import, the stored base rate and the refresh job.
  "createReferenceImport": "const j = pm.response.json(); pm.collectionVariables.set('referenceImportId', j.id);",
  "createBaseRate": "const j = pm.response.json(); pm.collectionVariables.set('baseRateId', j.id);",
  "refreshBaseRates": "const j = pm.response.json(); pm.collectionVariables.set('jobId', j.id);",
  # 122: the question and the evaluation.
  "createForecastQuestion": "const j = pm.response.json(); pm.collectionVariables.set('questionId', j.id);",
  "createForecastEvaluation": "const j = pm.response.json(); pm.collectionVariables.set('evaluationId', j.id);",
  # 123: the benchmark run and the historical case.
  "createBenchmarkRun": "const j = pm.response.json(); pm.collectionVariables.set('benchmarkRunId', j.id);",
  "createBenchmarkCase": "const j = pm.response.json(); pm.collectionVariables.set('benchmarkCaseId', j.id);",
  # 125: the hypothesis set (its first hypothesis and indicator), an added indicator, evidence row and assumption.
  "createHypothesisSet": "const j = pm.response.json(); pm.collectionVariables.set('hypothesisSetId', j.id); if (j.hypotheses && j.hypotheses.length) pm.collectionVariables.set('hypothesisId', j.hypotheses[0].id); if (j.indicators && j.indicators.length) pm.collectionVariables.set('indicatorId', j.indicators[0].id); if (j.assumptions && j.assumptions.length) pm.collectionVariables.set('assumptionId', j.assumptions[0].id);",
  "createIndicator": "const j = pm.response.json(); pm.collectionVariables.set('indicatorId', j.id);",
  "addAchEvidence": "const j = pm.response.json(); pm.collectionVariables.set('achEvidenceId', j.id);",
  "createKeyAssumption": "const j = pm.response.json(); pm.collectionVariables.set('assumptionId', j.id);",
  # 128: the created cycle, its queued run and the newest assessment.
  "createAnalysisCycle": "const j = pm.response.json(); pm.collectionVariables.set('cycleId', j.id);",
  "runAnalysisCycle": "const j = pm.response.json(); pm.collectionVariables.set('cycleRunId', j.id);",
  "listAssessments": "const j = pm.response.json(); if (j.items && j.items.length) pm.collectionVariables.set('assessmentId', j.items[0].id);",
  # 129: the workspace (and its context version for If-Match), the annotation and the relation.
  "createWorkspace": "const j = pm.response.json(); pm.collectionVariables.set('workspaceId', j.id); pm.collectionVariables.set('contextVersion', String(j.contextVersion));",
  "patchWorkspaceContext": "const j = pm.response.json(); pm.collectionVariables.set('contextVersion', String(j.contextVersion));",
  "createAnnotation": "const j = pm.response.json(); pm.collectionVariables.set('annotationId', j.id);",
  "createRelation": "const j = pm.response.json(); pm.collectionVariables.set('relationId', j.id);",
  # 130: the principal, group, tag, attachment, evidence item and saved search.
  "createPrincipal": "const j = pm.response.json(); pm.collectionVariables.set('principalId', j.id);",
  "createGroup": "const j = pm.response.json(); pm.collectionVariables.set('groupId', j.id);",
  "createTag": "const j = pm.response.json(); pm.collectionVariables.set('tagId', j.id);",
  "uploadAttachment": "const j = pm.response.json(); pm.collectionVariables.set('attachmentId', j.id);",
  "createEvidenceItem": "const j = pm.response.json(); pm.collectionVariables.set('evidenceItemId', j.id);",
  "createSavedSearch": "const j = pm.response.json(); pm.collectionVariables.set('savedSearchId', j.id);",
  # 132: the report (from a create or a draft), its added block, the package and an imported package.
  "createReport": "const j = pm.response.json(); pm.collectionVariables.set('reportId', j.id);",
  "draftReport": "const j = pm.response.json(); pm.collectionVariables.set('reportId', j.id);",
  "createReportBlock": "const j = pm.response.json(); pm.collectionVariables.set('blockId', j.id);",
  "createExchangePackage": "const j = pm.response.json(); pm.collectionVariables.set('packageId', j.id);",
  "importExchangePackage": "const j = pm.response.json(); pm.collectionVariables.set('receivedId', j.id);",
  # 134: the first related link and cluster, and the first access request.
  "listRelatedChronicles": "const j = pm.response.json(); if (j.items && j.items.length) pm.collectionVariables.set('chronicleLinkId', j.items[0].linkId);",
  "listChronicleClusters": "const j = pm.response.json(); if (j.items && j.items.length) pm.collectionVariables.set('chronicleClusterId', j.items[0].id);",
  "listChronicleAccessRequests": "const j = pm.response.json(); if (j.items && j.items.length) pm.collectionVariables.set('accessRequestId', j.items[0].id);",
  "requestChronicleAccess": "const j = pm.response.json(); pm.collectionVariables.set('accessRequestId', j.id);",
  # 131: the chronicle (and its display id, the confirmation of a delete) and the added layer.
  "createChronicle": "const j = pm.response.json(); pm.collectionVariables.set('chronicleId', j.id); pm.collectionVariables.set('chronicleDisplayId', j.displayId);",
  "createChronicleLayer": "const j = pm.response.json(); pm.collectionVariables.set('chronicleLayerId', j.id);",
  # 126: the queued index fit (a 103 job: read it with getBacktest).
  "fitRegionalIndex": "const j = pm.response.json(); pm.collectionVariables.set('jobId', j.id);",
  # 117: the created attention topic.
  "createAttentionTopic": "const j = pm.response.json(); pm.collectionVariables.set('attentionTopicId', j.id);",
  "rotatePeerIngestKey": "const j = pm.response.json(); pm.collectionVariables.set('peerIngestKey', j.key); console.log('New peer ingest key (shown once):', j.key);",
  "createSnapshot": "const j = pm.response.json(); pm.collectionVariables.set('exportId', j.id);",
  "createPack": "const j = pm.response.json(); pm.collectionVariables.set('exportId', j.id);",
  "createHold": "const j = pm.response.json(); pm.collectionVariables.set('holdId', j.id);",
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
    # 086: NOAA Weather Radio SAME headers from multimon-ng, for a custom source with the same-alert kind.
    ("SAME header (NOAA Weather Radio)", None, {"format": "same", "content_type": "text/plain", "language": "text",
                                                "raw": "EAS: ZCZC-WXR-TOR-048453-048201+0030-2861504-KEWX/NWS-\nEAS: NNNN\n"}),
  ],
  "pushExternalItems": [
    ("NDJSON", None, {"content_type": "application/x-ndjson", "language": "text",
                      "raw": "\n".join(json.dumps(x) for x in [{"protocol": "foresignal-edp/1.0", "sourceId": "hormuz-transits"}] + EDP_ITEMS[:2]) + "\n"}),
  ],
  # 059: a source created from a device preset; the response's setup holds the device instructions and URL.
  # 122: one question per resolver type, and forecasts of every shape.
  "createForecastQuestion": [
    ("count-threshold", {"title": "Will 5 or more M5+ earthquakes be recorded in Japan in December 2026?", "type": "binary",
                         "resolutionCriteria": "Yes if at least 5 seismic:m5 events tagged JP occur in December 2026 (UTC).",
                         "resolver": {"type": "count-threshold", "selector": {"referenceFamily": "seismic:m5"},
                                      "area": {"regionCodes": ["JP"]}, "window": {"start": "2026-12-01T00:00:00Z",
                                                                                 "end": "2027-01-01T00:00:00Z"},
                                      "op": "gte", "threshold": 5},
                         "closesAt": "2026-12-01T00:00:00Z", "resolveBy": "2027-01-01T00:00:00Z", "category": "seismic"}),
    ("series-value, numeric", {"title": "What will the weekly managed-money net position in crude be in week 2026-W48?",
                               "type": "numeric", "outcomes": {"unit": "contracts"},
                               "resolutionCriteria": "The last value of the series for ISO week 2026-W48 as published by the CFTC.",
                               "resolver": {"type": "series-value", "ref": "series:cftc-cot/067651.disagg.mm_net",
                                            "period": "2026-W48", "aggregate": "last"},
                               "closesAt": "2026-11-23T00:00:00Z", "resolveBy": "2026-11-30T00:00:00Z",
                               "category": "markets"}),
    ("alert-fired", {"title": "Will the Hormuz loitering rule fire before 2026-12-01?", "type": "binary",
                     "resolutionCriteria": "Yes if rule 3f2b7c1e-8a4d-4f6b-9c2e-1d5a7b9e0f13 fires at least once before 2026-12-01T00:00Z.",
                     "resolver": {"type": "alert-fired", "ruleId": "3f2b7c1e-8a4d-4f6b-9c2e-1d5a7b9e0f13",
                                  "window": {"start": "2026-11-01T00:00:00Z", "end": "2026-12-01T00:00:00Z"}},
                     "closesAt": "2026-11-15T00:00:00Z", "resolveBy": "2026-12-01T00:00:00Z", "category": "maritime"}),
    ("situation-status", {"title": "Will the Red Sea situation be critical on 2026-12-01?", "type": "binary",
                          "resolutionCriteria": "Yes if the situation's severity is critical at 2026-12-01T00:00Z.",
                          "resolver": {"type": "situation-status", "situationId": "01JA8Q3V7S2K9W4T6Y1B5N0M3C",
                                       "minSeverity": "critical"},
                          "closesAt": "2026-11-24T00:00:00Z", "resolveBy": "2026-12-01T00:00:00Z", "category": "maritime"}),
    ("hypothesis-outcome", {"title": "Which hypothesis explains the Hormuz transit drop?", "type": "multiple",
                            "outcomes": {"labels": ["weather", "security incident", "market rerouting", "other"]},
                            "resolutionCriteria": "Resolved by the operator's confirmation of the hypothesis set.",
                            "resolver": {"type": "hypothesis-outcome", "setId": "01JA8Q3V7S2K9W4T6Y1B5N0M3D"},
                            "closesAt": "2026-12-01T00:00:00Z", "resolveBy": "2026-12-15T00:00:00Z", "category": "maritime"}),
    ("manual, ordered", {"title": "How severe will the 2026-27 winter grid outages in Ukraine be?", "type": "ordered",
                         "outcomes": {"labels": ["minor", "moderate", "severe"]},
                         "resolutionCriteria": "Resolved by the operator on 2027-03-31 from the regulator's outage reports.",
                         "resolver": {"type": "manual"}, "closesAt": "2026-12-01T00:00:00Z",
                         "resolveBy": "2027-03-31T00:00:00Z", "category": "infrastructure"}),
  ],
  "submitForecast": [
    ("multiple", {"distribution": {"weather": 0.2, "security incident": 0.5, "market rerouting": 0.25, "other": 0.05},
                  "rationale": "Security incidents explain most past drops of this size."}),
    ("numeric", {"quantiles": {"p05": 120000, "p10": 135000, "p25": 160000, "p50": 185000, "p75": 205000,
                               "p90": 225000, "p95": 240000},
                 "rationale": "Seasonal range of the last five years, centered on the latest value."}),
  ],
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
                  "config": {"chatId": "-1001234567890", "interactive": True,
                             "allowedChats": [{"chatId": "123456789", "name": "My phone", "scope": "agent"},
                                              {"chatId": "-1001234567891", "name": "Family group", "scope": "read",
                                               "userIds": ["123456789"]}],
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
    ("Meshtastic, private channel via the node's JSON downlink (084)", {"name": "Mesh alerts", "type": "meshtastic",
                  "config": {"brokerUrl": "mqtt://192.168.1.20:1883", "root": "msh/local", "gatewayNodeNum": "2864434397",
                             "channelIndex": 1, "channelName": "Family", "privateChannelConfirmed": True,
                             "region": "EU_868", "airtimePreset": "meshtastic-longfast", "dutyCyclePercent": 1.0,
                             "minSeverity": "warning", "reportsEnabled": True,
                             "allowedSenders": {"!a1b2c3d4": "ack", "!0badcafe": "report"}},
                  "secrets": {"username": "foresignal", "password": "REPLACE"}, "events": ["alert"]}),
    ("MeshCore, private slot over the companion protocol (084)", {"name": "MeshCore alerts", "type": "meshcore",
                  "config": {"companionUrl": "tcp://192.168.1.30:5000", "channelIndex": 1, "region": "EU_868",
                             "airtimePreset": "custom",
                             "airtimeCustom": {"spreadingFactor": 10, "bandwidthKhz": 250, "codingRate": 5, "preambleSymbols": 8},
                             "profile": "text", "minSeverity": "warning", "reportsEnabled": True,
                             "allowedSenders": {"name:Alice": "ack"}},
                  "events": ["alert"]}),
    ("Webhook (HMAC signed)", {"name": "Automation webhook", "type": "webhook", "config": {"url": "https://REPLACE/hooks/foresignal", "method": "POST"},
                               "secrets": {"hmacSecret": "REPLACE"}, "events": ["alert", "briefing", "system"]}),
  ],
}

QUERY_DEFAULTS = {"after": "{{lastSeq}}", "cursor": "{{cursor}}", "source": "{{sourceId}}", "ruleId": "{{ruleId}}",
                  # 119: every query parameter named ref is a series reference.
                  "ref": "{{seriesRef}}",
                  # 131: a chronicle is deleted with its display id as confirmation.
                  "confirm": "{{chronicleDisplayId}}"}

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
    elif scope == "peer":
        # 113: instance-to-instance pushes authenticate with the peer ingest key (fsp_), never a token.
        request["auth"] = {"type": "bearer", "bearer": [{"key": "token", "value": "{{peerIngestKey}}", "type": "string"}]}
    elif path.startswith("/v1/federation/") and scope == "read" and op.get("security") == [{"bearerToken": []}]:
        # 113: the federation feed needs a token with restrictions.federation=true.
        request["auth"] = {"type": "bearer", "bearer": [{"key": "token", "value": "{{federationToken}}", "type": "string"}]}
    elif opid == "selfRegisterExternalSource":
        request["auth"] = {"type": "noauth"}
        request["header"].append({"key": "Authorization", "value": "Bearer {{registrationToken}}"})
    elif admin:
        var = "{{agentToken}}" if scope == "agent" else "{{adminToken}}"
        request["auth"] = {"type": "bearer", "bearer": [{"key": "token", "value": var, "type": "string"}]}
    elif scope == "read" and (op.get("x-foresignal-always-authenticated")
                              or not any(alt == {} for alt in op.get("security", [{}]))):
        # Always authenticated (023), or a security list without the anonymous {} alternative (the per-caller reads
        # of 129 onward): a token is needed even while openReads is on.
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
           "description": f"{spec['info']['description']}\n\nGenerated from docs/OpenAPI.json (v{spec['info']['version']}). Every operation is included, grouped by tag. Admin operations use Bearer `{{{{adminToken}}}}`, agent operations `{{{{agentToken}}}}` (defaults to the admin token; set it to an agent-scope token to test scopes), always-authenticated read operations `{{{{readToken}}}}` (defaults to the admin token; claiming a pairing code stores a read token there), ingest uses `X-Foresignal-Ingest-Key: {{{{ingestKey}}}}` (EDP pushes `{{{{edpIngestKey}}}}`, Gotify `X-Gotify-Key`), federation pushes Bearer `{{{{peerIngestKey}}}}` and the federation feed Bearer `{{{{federationToken}}}}` (a read token with restrictions.federation=true), and other reads use no auth (open while openReads is on). Test scripts capture ids into collection variables (ruleId, channelId, alertId, briefingId, threadId, ingestKey, externalId, lastSeq, cursor…).",
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
      ("placeId", "home"), ("viewId", ""), ("watchlistId", ""), ("exportId", ""), ("holdId", ""),
      ("peerId", ""), ("peerIngestKey", ""), ("federationToken", ""),
      ("polSubjectId", ""), ("polPlaceId", ""), ("tripId", ""),
      ("exerciseId", ""), ("suggestionId", ""), ("tuningLogId", ""),
      ("canonicalEventId", ""), ("manifestId", ""), ("templateId", "ask-system"), ("jobId", ""),
      ("attentionTopicId", "red-sea-shipping"),
      ("situationId", ""), ("situationMemberId", ""), ("otherSituationId", ""),
      ("findingId", ""), ("seriesRef", "rollup:day/*/earthquake/US"),
      ("precursorId", ""), ("precursorBatchId", ""), ("precursorCandidateId", ""),
      ("referenceImportId", ""), ("baseRateId", ""),
      ("questionId", ""), ("forecasterId", "ensemble"), ("evaluationId", ""),
      ("cycleId", ""), ("cycleRunId", ""), ("assessmentId", ""),
      ("objectRef", "situation:{{situationId}}"), ("workspaceId", ""), ("contextVersion", "1"), ("annotationId", ""),
      ("relationId", ""),
      ("principalId", ""), ("groupId", ""), ("tagId", ""), ("attachmentId", ""), ("evidenceItemId", ""),
      ("savedSearchId", ""),
      ("reportId", ""), ("blockId", ""), ("packageId", ""), ("receivedId", ""),
      ("chronicleLinkId", ""), ("chronicleClusterId", ""), ("accessRequestId", ""), ("networkSalt", ""),
      ("benchmarkRunId", ""), ("benchmarkCaseId", ""),
      ("hypothesisSetId", ""), ("hypothesisId", ""), ("indicatorId", ""), ("achEvidenceId", ""), ("assumptionId", ""),
      ("indexId", "instability"), ("outlookRegion", "YE"), ("outlookFamily", "conflict:battles"),
      ("tileLayer", "earthquake"), ("tileZ", "0"), ("tileX", "0"), ("tileY", "0"),
      ("regionCode", "US-TX")]],
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
