#!/bin/sh
# Foresignal metric-name drift check (prompt 143; meant for CI in 144).
#
# Compares three sets of names:
#   documented  the first column of the table in docs/Metrics.md (dotted Micrometer names)
#   scraped     the foresignal_* series on /actuator/prometheus of a running broker
#   dashboards  the foresignal_* names referenced by integrations/grafana/dashboards/*.json
#
# It fails (exit 1) when a scraped series or a dashboard reference matches no documented meter.
# Documented meters missing from the scrape are listed for information only: many meters are
# registered lazily, on their first event, or only while their source is enabled.
# JVM, Tomcat, HikariCP and other library meters are ignored (only foresignal_* is compared), and so
# are the /v1/metrics projections (foresignal_data_*), which live in their own registry.
#
#   FORESIGNAL_URL     base URL (default http://localhost:8080)
#   FORESIGNAL_TOKEN   admin token (/actuator/prometheus needs it)
#   SCRAPE_FILE        read a saved scrape instead of fetching one (no URL or token needed)
#
# Run from the repository root. POSIX sh; needs curl (unless SCRAPE_FILE is set), grep, sed, sort,
# comm and mktemp.

set -eu

BASE=${FORESIGNAL_URL:-http://localhost:8080}
BASE=${BASE%/}
DOC=docs/Metrics.md
DASHBOARDS=integrations/grafana/dashboards

[ -f "$DOC" ] || { echo "run from the repository root ($DOC not found)" >&2; exit 2; }

WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT INT TERM

# Documented names, converted to their exposition stem: dots and hyphens become underscores.
grep -oE '^\| *`foresignal\.[a-z0-9_.-]+`' "$DOC" \
    | sed -e 's/^| *`//' -e 's/`$//' -e 's/[.-]/_/g' | sort -u > "$WORK/documented"

# Every exposition name a documented stem may produce (Micrometer's Prometheus naming).
while read -r stem; do
    for suffix in "" _total _seconds _seconds_count _seconds_sum _seconds_max _seconds_bucket \
                  _count _sum _max _bucket _active_count _duration_seconds; do
        echo "$stem$suffix"
    done
done < "$WORK/documented" | sort -u > "$WORK/expanded"

# The scrape.
if [ -n "${SCRAPE_FILE:-}" ]; then
    cp "$SCRAPE_FILE" "$WORK/scrape"
else
    [ -n "${FORESIGNAL_TOKEN:-}" ] || { echo "set FORESIGNAL_TOKEN (admin) or SCRAPE_FILE" >&2; exit 2; }
    curl -fsS -H "Authorization: Bearer $FORESIGNAL_TOKEN" "$BASE/actuator/prometheus" > "$WORK/scrape"
fi
grep -oE '^foresignal_[a-zA-Z0-9_]+' "$WORK/scrape" | grep -v '^foresignal_data_' | sort -u > "$WORK/scraped" || true

# Dashboard references.
if [ -d "$DASHBOARDS" ]; then
    cat "$DASHBOARDS"/*.json | grep -oE 'foresignal_[a-zA-Z0-9_]+' | grep -v '^foresignal_data_' | sort -u > "$WORK/dashboards" || true
else
    : > "$WORK/dashboards"
fi

status=0

comm -23 "$WORK/scraped" "$WORK/expanded" > "$WORK/undocumented"
if [ -s "$WORK/undocumented" ]; then
    echo "DRIFT: scraped series with no row in $DOC:"
    sed 's/^/  /' "$WORK/undocumented"
    status=1
fi

comm -23 "$WORK/dashboards" "$WORK/expanded" > "$WORK/dashboard-unknown"
if [ -s "$WORK/dashboard-unknown" ]; then
    echo "DRIFT: dashboard references with no row in $DOC:"
    sed 's/^/  /' "$WORK/dashboard-unknown"
    status=1
fi

# Documented stems with no scraped series (informational).
: > "$WORK/unscraped"
while read -r stem; do
    if ! grep -qE "^${stem}(_total|_seconds|_seconds_count|_seconds_sum|_seconds_max|_seconds_bucket|_count|_sum|_max|_bucket|_active_count|_duration_seconds)?\$" "$WORK/scraped"; then
        echo "$stem" >> "$WORK/unscraped"
    fi
done < "$WORK/documented"

printf 'documented %s, scraped %s, dashboard references %s, documented but not yet registered %s\n' \
    "$(wc -l < "$WORK/documented" | tr -d ' ')" "$(wc -l < "$WORK/scraped" | tr -d ' ')" \
    "$(wc -l < "$WORK/dashboards" | tr -d ' ')" "$(wc -l < "$WORK/unscraped" | tr -d ' ')"
if [ -s "$WORK/unscraped" ]; then
    echo "Not registered in this scrape (lazy or feature-gated; not drift):"
    sed 's/^/  /' "$WORK/unscraped"
fi

[ "$status" -eq 0 ] && echo "No drift."
exit "$status"
