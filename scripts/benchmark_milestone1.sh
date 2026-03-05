#!/usr/bin/env bash
set -euo pipefail

WORKDIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$WORKDIR"

URL="http://127.0.0.1:8081/api/users/load-test-user?delayMs=40"
REQUESTS="${REQUESTS:-3000}"
CONCURRENCY="${CONCURRENCY:-200}"
WARMUP_REQUESTS="${WARMUP_REQUESTS:-200}"
WARMUP_CONCURRENCY="${WARMUP_CONCURRENCY:-50}"

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

wait_for_service() {
  local retries=120
  for ((i=1; i<=retries; i++)); do
    if curl -fsS "http://127.0.0.1:8081/actuator/health" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done
  return 1
}

run_mode() {
  local mode="$1"
  local virtual_enabled="$2"
  local app_log="$TMP_DIR/${mode}.app.log"
  local ab_log="$TMP_DIR/${mode}.ab.log"

  APP_VIRTUAL_THREADS_ENABLED="$virtual_enabled" \
    mvn -q -pl services/user-service spring-boot:run \
    -Dmaven.repo.local=.m2/repository >"$app_log" 2>&1 &
  local app_pid=$!

  if ! wait_for_service; then
    echo "Failed to start user-service for mode=$mode"
    cat "$app_log"
    kill "$app_pid" >/dev/null 2>&1 || true
    wait "$app_pid" >/dev/null 2>&1 || true
    exit 1
  fi

  ab -q -n "$WARMUP_REQUESTS" -c "$WARMUP_CONCURRENCY" "$URL" >/dev/null
  ab -n "$REQUESTS" -c "$CONCURRENCY" "$URL" >"$ab_log"

  local rps
  local p95_ms
  local failures

  rps="$(awk '/Requests per second/ {print $4}' "$ab_log")"
  failures="$(awk '/Failed requests/ {print $3}' "$ab_log")"
  p95_ms="$(awk '$1=="95%" {print $2}' "$ab_log")"

  kill "$app_pid" >/dev/null 2>&1 || true
  wait "$app_pid" >/dev/null 2>&1 || true

  echo "$mode|$virtual_enabled|$rps|$p95_ms|$failures"
}

RESULT_FILE="docs/load-test-report.md"

PLATFORM_RESULT="$(run_mode platform-threads false)"
VIRTUAL_RESULT="$(run_mode virtual-threads true)"

IFS='|' read -r p_mode p_enabled p_rps p_p95 p_fail <<<"$PLATFORM_RESULT"
IFS='|' read -r v_mode v_enabled v_rps v_p95 v_fail <<<"$VIRTUAL_RESULT"

python3 - <<PY > "$TMP_DIR/summary.txt"
p_rps=float("$p_rps")
v_rps=float("$v_rps")
p_p95=float("$p_p95")
v_p95=float("$v_p95")
throughput=((v_rps-p_rps)/p_rps)*100 if p_rps else 0
p95=((p_p95-v_p95)/p_p95)*100 if p_p95 else 0
print(f"{throughput:.1f}")
print(f"{p95:.1f}")
PY

THROUGHPUT_DELTA="$(sed -n '1p' "$TMP_DIR/summary.txt")"
P95_DELTA="$(sed -n '2p' "$TMP_DIR/summary.txt")"

cat > "$RESULT_FILE" <<MARKDOWN
# Milestone 1 Load Test Report

Date: $(date '+%Y-%m-%d %H:%M:%S %Z')

## Goal
Compare Java 21 platform threads vs virtual threads under blocking I/O simulation in user-service.

## Test Setup
- Endpoint: $URL
- Load tool: ab
- Total requests per run: $REQUESTS
- Concurrency: $CONCURRENCY
- Warmup: $WARMUP_REQUESTS requests @ $WARMUP_CONCURRENCY concurrency
- Service startup command:
  - Platform threads: APP_VIRTUAL_THREADS_ENABLED=false mvn -q -pl services/user-service spring-boot:run
  - Virtual threads: APP_VIRTUAL_THREADS_ENABLED=true mvn -q -pl services/user-service spring-boot:run

## Results
| Mode | Virtual Threads Enabled | Requests/sec | p95 latency (ms) | Failed requests |
|---|---:|---:|---:|---:|
| platform-threads | $p_enabled | $p_rps | $p_p95 | $p_fail |
| virtual-threads | $v_enabled | $v_rps | $v_p95 | $v_fail |

## Delta
- Throughput improvement (virtual vs platform): $THROUGHPUT_DELTA%
- p95 latency improvement (virtual vs platform): $P95_DELTA%

## Notes
- This test intentionally uses a blocking delay (delayMs=40) to emulate real-world blocking operations.
- The comparison uses the same code path, only toggling APP_VIRTUAL_THREADS_ENABLED.
- For portfolio use, rerun with larger request volume and include CPU/memory snapshots from Grafana.
MARKDOWN

echo "Wrote $RESULT_FILE"
cat "$RESULT_FILE"
