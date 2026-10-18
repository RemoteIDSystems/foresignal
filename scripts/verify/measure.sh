#!/bin/sh
# Foresignal runtime measurements (prompt 143).
#
# Runs scripts/verify/measure.sql through `docker compose exec -T db psql`, so it needs nothing on the
# host but Docker Compose. Every query is read-only. Output is printed and also kept under
# $MEASURE_OUT (default ./verify-out) with a UTC timestamp in the file name.
#
#   scripts/verify/measure.sh [report] [HOURS]   the full report over the last HOURS hours (default 1)
#   scripts/verify/measure.sh wal-sample         appends one WAL/checkpoint sample to
#                                                $MEASURE_OUT/wal-samples.csv and prints the delta
#                                                against the previous sample (run it hourly, e.g. from cron)
#
#   COMPOSE_PROJECT   compose project name (-p), e.g. foresignal-restore; default: compose's own default
#   COMPOSE_FILE      honored by docker compose as usual
#   MEASURE_OUT       output directory (default ./verify-out)
#
# POSIX sh; uses only docker, date, tail, awk and tee. Exits non-zero when psql fails.

set -eu

HERE=$(cd "$(dirname "$0")" && pwd)
OUT=${MEASURE_OUT:-./verify-out}
MODE=${1:-report}

mkdir -p "$OUT"

compose() {
    if [ -n "${COMPOSE_PROJECT:-}" ]; then
        docker compose -p "$COMPOSE_PROJECT" "$@"
    else
        docker compose "$@"
    fi
}

psql_db() {
    compose exec -T db psql -U foresignal -d foresignal -X -q "$@"
}

stamp=$(date -u +%Y%m%dT%H%M%SZ)

case "$MODE" in
    report)
        hours=${2:-1}
        case "$hours" in
            ''|*[!0-9]*) echo "HOURS must be a whole number of hours" >&2; exit 2 ;;
        esac
        file="$OUT/measure-$stamp.txt"
        psql_db -v hours="$hours" -f - < "$HERE/measure.sql" | tee "$file"
        echo "Saved: $file"
        ;;
    wal-sample)
        csv="$OUT/wal-samples.csv"
        line=$(psql_db -A -t -F ',' -c \
            "SELECT extract(epoch FROM now())::bigint, w.wal_bytes, c.num_timed, c.num_requested
               FROM pg_stat_wal w, pg_stat_checkpointer c")
        [ -f "$csv" ] || echo "epoch,wal_bytes,checkpoints_timed,checkpoints_requested" > "$csv"
        prev=$(tail -n 1 "$csv")
        echo "$line" >> "$csv"
        case "$prev" in
            epoch,*) echo "First sample recorded in $csv: $line" ;;
            *)
                printf '%s\n%s\n' "$prev" "$line" | awk -F ',' '
                    NR == 1 { t = $1; b = $2; ct = $3; cr = $4; next }
                    {
                        dt = $1 - t; db = $2 - b
                        if (dt <= 0 || db < 0) { print "Counters went backwards (stats reset or restart); delta skipped"; exit }
                        printf "Interval %.2f h: WAL %.2f GB (%.2f GB/day projected), checkpoints timed %d, requested %d\n",
                            dt / 3600, db / 1e9, db / 1e9 * 86400 / dt, $3 - ct, $4 - cr
                    }'
                ;;
        esac
        ;;
    *)
        echo "usage: $0 [report [HOURS] | wal-sample]" >&2
        exit 2
        ;;
esac
