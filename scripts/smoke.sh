#!/bin/sh
# Foresignal smoke test (prompt 021) against a running instance.
#
#   FORESIGNAL_URL    base URL (default http://localhost:8080)
#   FORESIGNAL_TOKEN  admin token (required: the script enables a source and
#                     creates and deletes a trigger)
#   SMOKE_WAIT        seconds to wait for the first USGS records (default 180)
#
# Checks /v1/system and readiness, lists sources, enables usgs-earthquakes,
# waits for records and asserts the page carries a numeric watermarkSeq, then
# creates and deletes a test trigger. usgs-earthquakes is left enabled.
# Needs curl and jq. Exits non-zero on the first failure.

set -eu

BASE=${FORESIGNAL_URL:-http://localhost:8080}
BASE=${BASE%/}
TOKEN=${FORESIGNAL_TOKEN:-}
WAIT=${SMOKE_WAIT:-180}
SOURCE=usgs-earthquakes

PASSED=0
TMP=$(mktemp)
trap 'rm -f "$TMP"' EXIT INT TERM

fail() {
    printf 'FAIL: %s\n' "$1" >&2
    if [ -s "$TMP" ]; then
        printf 'Last response body:\n' >&2
        head -c 2000 "$TMP" >&2
        printf '\n' >&2
    fi
    exit 1
}

ok() {
    PASSED=$((PASSED + 1))
    printf 'ok   %s\n' "$1"
}

for tool in curl jq; do
    command -v "$tool" >/dev/null 2>&1 || fail "$tool is required"
done
[ -n "$TOKEN" ] || fail "set FORESIGNAL_TOKEN to the admin token"

# request METHOD PATH [BODY] - writes the body to $TMP and prints the status.
request() {
    if [ $# -ge 3 ]; then
        curl -sS -o "$TMP" -w '%{http_code}' -X "$1" \
            -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
            --data "$3" "$BASE$2" || true
    else
        curl -sS -o "$TMP" -w '%{http_code}' -X "$1" \
            -H "Authorization: Bearer $TOKEN" "$BASE$2" || true
    fi
}

expect() {
    status=$1
    wanted=$2
    what=$3
    [ "$status" = "$wanted" ] || fail "$what: HTTP $status, expected $wanted"
}

# 1. System document and readiness.
status=$(request GET /v1/system)
expect "$status" 200 "GET /v1/system"
version=$(jq -r '.version // "unknown"' "$TMP")
jq -e '.history.tiers | type == "array"' "$TMP" >/dev/null || fail "/v1/system has no history (token not accepted?)"
ok "/v1/system (version $version)"

status=$(request GET /actuator/health/readiness)
expect "$status" 200 "GET /actuator/health/readiness"
jq -e '.status == "UP"' "$TMP" >/dev/null || fail "readiness is not UP"
ok "readiness UP"

# 2. Sources.
status=$(request GET /v1/sources)
expect "$status" 200 "GET /v1/sources"
count=$(jq 'length' "$TMP")
jq -e --arg id "$SOURCE" 'any(.[]; .id == $id)' "$TMP" >/dev/null || fail "$SOURCE is not listed"
ok "$count sources listed, $SOURCE present"

# 3. Enable USGS.
status=$(request PUT "/v1/sources/$SOURCE" '{"enabled":true}')
expect "$status" 200 "PUT /v1/sources/$SOURCE"
jq -e '.enabled == true' "$TMP" >/dev/null || fail "$SOURCE did not report enabled"
ok "$SOURCE enabled"

# 4. Wait for records; the page must carry a numeric watermarkSeq.
elapsed=0
records=0
while [ "$elapsed" -lt "$WAIT" ]; do
    status=$(request GET "/v1/records?source=$SOURCE&limit=10")
    if [ "$status" = 200 ]; then
        records=$(jq '.items | length' "$TMP")
        if [ "$records" -gt 0 ]; then
            break
        fi
    fi
    sleep 5
    elapsed=$((elapsed + 5))
done
[ "$records" -gt 0 ] || fail "no $SOURCE records after ${WAIT}s (check GET /v1/sources/$SOURCE/fetches)"
jq -e '.watermarkSeq | type == "number"' "$TMP" >/dev/null || fail "the records page has no numeric watermarkSeq"
watermark=$(jq '.watermarkSeq' "$TMP")
ok "records arrived after ${elapsed}s (watermarkSeq $watermark)"

# 5. Create and delete a test trigger.
status=$(request POST /v1/triggers \
    '{"name":"Smoke test trigger","sources":["usgs-earthquakes"],"enabled":false,"conditions":[{"path":"mag","op":">=","value":9.9}]}')
expect "$status" 201 "POST /v1/triggers"
trigger=$(jq -r '.id' "$TMP")
if [ -z "$trigger" ] || [ "$trigger" = null ]; then
    fail "the created trigger has no id"
fi
status=$(request DELETE "/v1/triggers/$trigger")
expect "$status" 204 "DELETE /v1/triggers/$trigger"
status=$(request GET "/v1/triggers/$trigger")
expect "$status" 404 "GET /v1/triggers/$trigger after delete"
ok "trigger created and deleted"

printf '\nSmoke test passed: %s checks against %s (Foresignal %s).\n' "$PASSED" "$BASE" "$version"
printf '%s stays enabled; disable it with PUT /v1/sources/%s {"enabled":false} if you do not want it.\n' "$SOURCE" "$SOURCE"
