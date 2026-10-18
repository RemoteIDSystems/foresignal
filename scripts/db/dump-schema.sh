#!/bin/sh
# Foresignal schema dumps and parity checks (prompt 144, Architecture §25).
#
# The Flyway baseline (src/main/resources/db/migration/V1__baseline.sql) was hand-derived from the JPA entities and
# the old bootstrap SQL, because nothing could be run when it was written. This script is the procedure that checks
# it, and that checks any database adopted with spring.flyway.baseline-on-migrate.
#
#   scripts/db/dump-schema.sh dump [FILE]       pg_dump --schema-only of the compose database to FILE
#                                               (default ./schema-out/schema-<UTC stamp>.sql), normalized
#   scripts/db/dump-schema.sh compare A B       diff two normalized dumps; exits 1 when they differ
#   scripts/db/dump-schema.sh baseline [FILE]   creates a scratch database next to foresignal, applies V1__baseline.sql
#                                               to it with psql, dumps it to FILE (default
#                                               ./schema-out/baseline-<stamp>.sql), then drops the scratch database
#
# A full parity check of the baseline against the pre-1.0 schema:
#   1. git checkout the last commit before 144 (bc4a2105), docker compose down -v, start it with every feature
#      enabled until it is ready, then: scripts/db/dump-schema.sh dump pre-cutover.sql
#   2. On 1.0.0: scripts/db/dump-schema.sh baseline flyway.sql
#   3. scripts/db/dump-schema.sh compare flyway.sql pre-cutover.sql
#   Expected differences: the CHECK constraints Hibernate adds for enum columns (absent from the baseline by design),
#   constraint and index names (Hibernate's are hashed), column order, and the record_version leaves the running
#   application had attached (rv_s_*, rv_t_*). FlywaySchemaParityIT automates the same comparison in the test suite.
#
#   COMPOSE_PROJECT   compose project name (-p); default: compose's own default
#   COMPOSE_FILE      honored by docker compose as usual
#   DB_SERVICE        the compose service (default db); DB_NAME (default foresignal); DB_USER (default foresignal)
#   SCHEMA_OUT        output directory for default file names (default ./schema-out)
#
# POSIX sh; needs only Docker Compose, date, grep, sed, sort and diff on the host (pg_dump and psql run inside the
# database container).

set -eu

HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$(cd "$HERE/../.." && pwd)
OUT=${SCHEMA_OUT:-./schema-out}
SERVICE=${DB_SERVICE:-db}
DB=${DB_NAME:-foresignal}
USER_NAME=${DB_USER:-foresignal}
MODE=${1:-}

compose() {
    if [ -n "${COMPOSE_PROJECT:-}" ]; then
        docker compose -p "$COMPOSE_PROJECT" "$@"
    else
        docker compose "$@"
    fi
}

# Drops what never matters to a comparison: comments, SET and SELECT pg_catalog lines, blank lines, and the
# \restrict / \unrestrict tokens newer pg_dump versions emit.
normalize() {
    grep -v -e '^--' -e '^SET ' -e '^SELECT pg_catalog' -e '^\\restrict' -e '^\\unrestrict' -e '^$' || true
}

dump_db() {
    compose exec -T "$SERVICE" pg_dump -U "$USER_NAME" -d "$1" --schema-only --no-owner --no-privileges \
        --no-comments | normalize
}

stamp=$(date -u +%Y%m%dT%H%M%SZ)

case "$MODE" in
    dump)
        file=${2:-$OUT/schema-$stamp.sql}
        mkdir -p "$(dirname "$file")"
        dump_db "$DB" > "$file"
        echo "Saved: $file"
        ;;
    baseline)
        file=${2:-$OUT/baseline-$stamp.sql}
        mkdir -p "$(dirname "$file")"
        scratch="foresignal_baseline_check"
        compose exec -T "$SERVICE" psql -U "$USER_NAME" -d postgres -X -q -v ON_ERROR_STOP=1 \
            -c "DROP DATABASE IF EXISTS $scratch" -c "CREATE DATABASE $scratch TEMPLATE template1"
        compose exec -T "$SERVICE" psql -U "$USER_NAME" -d "$scratch" -X -q -v ON_ERROR_STOP=1 -f - \
            < "$ROOT/src/main/resources/db/migration/V1__baseline.sql"
        dump_db "$scratch" > "$file"
        compose exec -T "$SERVICE" psql -U "$USER_NAME" -d postgres -X -q -c "DROP DATABASE IF EXISTS $scratch"
        echo "Saved: $file"
        ;;
    compare)
        if [ $# -ne 3 ]; then
            echo "usage: $0 compare A B" >&2
            exit 2
        fi
        a=$(mktemp)
        b=$(mktemp)
        trap 'rm -f "$a" "$b"' EXIT
        normalize < "$2" | sort > "$a"
        normalize < "$3" | sort > "$b"
        if diff -u "$a" "$b"; then
            echo "No difference."
        else
            echo "The schemas differ (lines sorted; - only in $2, + only in $3)." >&2
            exit 1
        fi
        ;;
    *)
        sed -n '2,30p' "$0"
        exit 2
        ;;
esac
