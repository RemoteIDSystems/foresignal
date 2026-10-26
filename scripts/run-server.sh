#!/bin/sh
# Runs the Foresignal broker from build/libs/foresignal.jar under a hard memory cap.
#
#   scripts/run-server.sh start    start it detached, logging to build/foresignal-server.log
#   scripts/run-server.sh stop     stop it (SIGTERM, a graceful Spring shutdown)
#   scripts/run-server.sh status   its state, PID, memory, peak, swap and OOM kills
#
# The JVM runs in a systemd user scope, foresignal-server.scope, with MemoryMax and
# MemorySwapMax=0. MemoryMax alone still lets the scope swap; with swap at 0 the kernel kills the
# JVM at the cap instead of thrashing the whole host. On 2026-10-25 an uncapped `java -jar`, next
# to two Gradle daemons, filled 24 GB of swap and the host had to be power-cycled.
#
# There is no uncapped mode. Without a working `systemd-run --user` the script refuses to start,
# and after starting it reads the scope's cgroup and stops the server unless memory.max and
# memory.swap.max are what was asked for.
#
# The JVM flags are fixed: -Xmx4g (the compose small profile, so heap and native growth can be
# told apart), -XX:MaxDirectMemorySize=2g, -XX:+ExitOnOutOfMemoryError,
# -XX:NativeMemoryTracking=summary (read with `jcmd <pid> VM.native_memory summary`) and
# -XX:GCLockerRetryAllocationCount=100. The last is for Java 21's G1: while the JDK Inflater holds
# the GCLocker, a large allocation (the raster decode's ~98 MB array) is retried only twice before
# G1 declares a heap OOM with the heap a third full, and on 2026-10-25 that ended the server after
# 2.5 minutes. JDK 22+ removed the GCLocker from G1 and rejects the flag, so the compose image
# (JDK 25) does not carry it. See docs/Host-Memory-Safety.md.
#
#   FORESIGNAL_MEM_MAX   the scope's MemoryMax (default 10G; a whole number of K, M or G)
#   FORESIGNAL_JAVA      the java binary (default $JAVA_HOME/bin/java, then a Gradle-provisioned
#                        JDK 21 under ~/.gradle/jdks, then java on PATH); must be Java 21 or later
#   FORESIGNAL_PORT      the HTTP port checked for a second server (default 8080)
#
# The script never builds or starts anything on its own: build the JAR with ./gradlew bootJar.
# The server runs only through `docker compose up -d` or this script, never a bare `java -jar`
# or `./gradlew bootRun`, neither of which is capped.

set -eu

UNIT=foresignal-server.scope
ROOT=$(cd "$(dirname "$0")/.." && pwd)
JAR=$ROOT/build/libs/foresignal.jar
LOG=$ROOT/build/foresignal-server.log
MEM_MAX=${FORESIGNAL_MEM_MAX:-10G}
PORT=${FORESIGNAL_PORT:-8080}

fail() {
    printf 'run-server: %s\n' "$1" >&2
    exit 1
}

# Converts a K, M or G size to bytes, as systemd writes it to memory.max.
to_bytes() {
    n=${1%?}
    case $1 in
        *K) echo $((n * 1024)) ;;
        *M) echo $((n * 1024 * 1024)) ;;
        *G) echo $((n * 1024 * 1024 * 1024)) ;;
    esac
}

# Prints the scope's cgroup directory, or nothing when the scope is not loaded.
cgroup_dir() {
    cg=$(systemctl --user show -p ControlGroup --value "$UNIT" 2>/dev/null || true)
    [ -n "$cg" ] && [ -d "/sys/fs/cgroup$cg" ] && echo "/sys/fs/cgroup$cg"
    return 0
}

is_active() {
    [ "$(systemctl --user show -p ActiveState --value "$UNIT" 2>/dev/null)" = active ]
}

# Picks the java binary and checks it is Java 21 or later.
find_java() {
    if [ -n "${FORESIGNAL_JAVA:-}" ]; then
        JAVA=$FORESIGNAL_JAVA
    elif [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA=$JAVA_HOME/bin/java
    else
        JAVA=
        for j in "$HOME"/.gradle/jdks/jdk-21*/bin/java; do
            [ -x "$j" ] && JAVA=$j
        done
        [ -n "$JAVA" ] || JAVA=java
    fi
    spec=$("$JAVA" -XshowSettings:properties -version 2>&1 \
        | sed -n 's/^ *java\.specification\.version = //p')
    [ -n "$spec" ] || fail "cannot run $JAVA"
    [ "${spec%%.*}" -ge 21 ] || fail "$JAVA is Java $spec; Foresignal needs 21 (set FORESIGNAL_JAVA)"
}

start() {
    expr "$MEM_MAX" : '[1-9][0-9]*[KMG]$' >/dev/null \
        || fail "FORESIGNAL_MEM_MAX must be a whole number of K, M or G, not '$MEM_MAX'"
    [ -f "$JAR" ] || fail "$JAR is missing; build it with ./gradlew bootJar"
    command -v systemd-run >/dev/null || fail "systemd-run is missing; refusing to start uncapped"
    systemctl --user show-environment >/dev/null 2>&1 \
        || fail "no systemd user manager; refusing to start uncapped"
    is_active && fail "$UNIT is already running (scripts/run-server.sh status)"
    if ss -ltnH "sport = :$PORT" 2>/dev/null | grep -q .; then
        fail "port $PORT is already in use; there is one Foresignal server and it is running"
    fi
    find_java

    # A scope that was OOM-killed stays loaded as failed and would block the unit name.
    systemctl --user reset-failed "$UNIT" >/dev/null 2>&1 || true
    cd "$ROOT"
    setsid nohup systemd-run --user --scope --unit="$UNIT" --quiet \
        -p MemoryMax="$MEM_MAX" -p MemorySwapMax=0 \
        "$JAVA" -Xmx4g -XX:MaxDirectMemorySize=2g -XX:+ExitOnOutOfMemoryError \
        -XX:NativeMemoryTracking=summary \
        -XX:+UnlockDiagnosticVMOptions -XX:GCLockerRetryAllocationCount=100 \
        -jar "$JAR" >"$LOG" 2>&1 </dev/null &

    i=0
    while ! is_active; do
        i=$((i + 1))
        [ $i -le 50 ] || fail "$UNIT did not start; see $LOG"
        sleep 0.1
    done
    dir=$(cgroup_dir)
    max=$(cat "$dir/memory.max")
    swap=$(cat "$dir/memory.swap.max")
    if [ "$max" != "$(to_bytes "$MEM_MAX")" ] || [ "$swap" != 0 ]; then
        systemctl --user stop "$UNIT" || true
        fail "the cap did not apply (memory.max=$max, memory.swap.max=$swap); stopped"
    fi
    printf 'started %s, PID %s, memory.max=%s memory.swap.max=0, log %s\n' \
        "$UNIT" "$(head -1 "$dir/cgroup.procs")" "$MEM_MAX" "$LOG"
}

stop() {
    if ! is_active; then
        systemctl --user reset-failed "$UNIT" >/dev/null 2>&1 || true
        echo "$UNIT is not running"
        return 0
    fi
    systemctl --user stop "$UNIT"
    systemctl --user reset-failed "$UNIT" >/dev/null 2>&1 || true
    echo "stopped $UNIT"
}

status() {
    state=$(systemctl --user show -p ActiveState --value "$UNIT" 2>/dev/null || true)
    result=$(systemctl --user show -p Result --value "$UNIT" 2>/dev/null || true)
    printf 'unit     %s %s (result %s)\n' "$UNIT" "${state:-inactive}" "${result:-none}"
    dir=$(cgroup_dir)
    [ -n "$dir" ] || return 0
    mib() { echo "$(($1 / 1048576)) MiB"; }
    printf 'pid      %s\n' "$(head -1 "$dir/cgroup.procs" 2>/dev/null)"
    printf 'memory   %s of max %s\n' "$(mib "$(cat "$dir/memory.current")")" "$(cat "$dir/memory.max")"
    [ -f "$dir/memory.peak" ] && printf 'peak     %s\n' "$(mib "$(cat "$dir/memory.peak")")"
    printf 'swap     %s (max %s)\n' "$(mib "$(cat "$dir/memory.swap.current")")" "$(cat "$dir/memory.swap.max")"
    printf 'oomkill  %s\n' "$(sed -n 's/^oom_kill //p' "$dir/memory.events")"
}

case ${1:-} in
    start) start ;;
    stop) stop ;;
    status) status ;;
    *) fail "usage: scripts/run-server.sh start|stop|status" ;;
esac
