#!/usr/bin/env bash
# Runs prompts/000-Run-Sequence.md in fresh headless Claude Code sessions until
# the roadmap is finished or something needs the owner.
#
# Stops when:
#   - completion-reports/RUN-STATUS.md exists (a stop condition, or Finished)
#   - every prompts/NNN-*.md has a completion report
#   - two sessions in a row make no new commit (stuck, or the API is unreachable)
#   - MAX_SESSIONS sessions have run
#
# Sessions run in auto permission mode: actions are checked by Claude Code's
# safety classifier instead of prompting, so nothing waits for a human.
#
# Usage:  scripts/run-sequence.sh
# Env:    MAX_SESSIONS  (default 40)
#         RETRY_WAIT    seconds to wait after a session without progress (default 120)
# Logs:   run-logs/<timestamp>-<prompt>.log  (ignored by git through *.log)

set -euo pipefail

cd "$(dirname "$0")/.."

MAX_SESSIONS="${MAX_SESSIONS:-40}"
RETRY_WAIT="${RETRY_WAIT:-120}"
STATUS_FILE="completion-reports/RUN-STATUS.md"
LOG_DIR="run-logs"

mkdir -p "$LOG_DIR"

# Keep the Mac awake (no idle, display or system sleep) while this script runs.
if command -v caffeinate >/dev/null 2>&1; then
  caffeinate -dims -w $$ &
fi

next_prompt() {
  local f num
  for f in prompts/[0-9][0-9][0-9]-*.md; do
    num="$(basename "$f" | cut -c1-3)"
    [ "$num" = "000" ] && continue
    if ! ls completion-reports/"$num"-CR-*.md >/dev/null 2>&1; then
      basename "$f" .md
      return
    fi
  done
}

log() { printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"; }

if ! docker info >/dev/null 2>&1; then
  log "Warning: Docker is not responding. Prompts 007-009 and the checkpoints (012, 022, ...) need it for Testcontainers."
fi

sessions=0
stalls=0

while :; do
  git pull --ff-only --quiet origin main || log "git pull failed; the session will retry it."

  if [ -f "$STATUS_FILE" ]; then
    log "Stopped: $STATUS_FILE exists."
    cat "$STATUS_FILE"
    exit 0
  fi

  next="$(next_prompt)"
  if [ -z "$next" ]; then
    log "Finished: every prompt has a completion report."
    exit 0
  fi

  if [ "$sessions" -ge "$MAX_SESSIONS" ]; then
    log "Stopped: reached MAX_SESSIONS=$MAX_SESSIONS. Next prompt is $next."
    exit 1
  fi

  sessions=$((sessions + 1))
  before="$(git rev-parse HEAD)"
  logfile="$LOG_DIR/$(date '+%Y%m%d-%H%M%S')-$next.log"
  log "Session $sessions starting at $next (log: $logfile)"

  set +e
  claude -p "Follow prompts/000-Run-Sequence.md in this repository exactly, starting at its step 1. The file's contents follow.

$(cat prompts/000-Run-Sequence.md)" \
    --permission-mode auto >"$logfile" 2>&1
  rc=$?
  set -e

  after="$(git rev-parse HEAD)"
  log "Session $sessions exited with code $rc. Last lines:"
  tail -n 15 "$logfile" | sed 's/^/    /'

  if [ "$before" = "$after" ]; then
    stalls=$((stalls + 1))
    if [ "$stalls" -ge 2 ]; then
      log "Stopped: two sessions in a row made no commit. Check $LOG_DIR/."
      exit 1
    fi
    log "No new commit; waiting ${RETRY_WAIT}s before retrying."
    sleep "$RETRY_WAIT"
  else
    stalls=0
  fi
done
