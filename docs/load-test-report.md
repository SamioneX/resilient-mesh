# Milestone 1 Load Test Report

Date: 2026-03-05 02:16:21 PST

## Goal
Compare Java 21 platform threads vs virtual threads under blocking I/O simulation in user-service.

## Test Setup
- Endpoint: http://127.0.0.1:8081/api/users/load-test-user?delayMs=40
- Load tool: ab
- Total requests per run: 3000
- Concurrency: 200
- Warmup: 200 requests @ 50 concurrency
- Service startup command:
  - Platform threads: APP_VIRTUAL_THREADS_ENABLED=false mvn -q -pl services/user-service spring-boot:run
  - Virtual threads: APP_VIRTUAL_THREADS_ENABLED=true mvn -q -pl services/user-service spring-boot:run

## Results
| Mode | Virtual Threads Enabled | Requests/sec | p95 latency (ms) | Failed requests |
|---|---:|---:|---:|---:|
| platform-threads | false | 3373.83 | 219 | 0 |
| virtual-threads | true | 4268.22 | 119 | 0 |

## Delta
- Throughput improvement (virtual vs platform): 26.5%
- p95 latency improvement (virtual vs platform): 45.7%

## Notes
- This test intentionally uses a blocking delay (delayMs=40) to emulate real-world blocking operations.
- The comparison uses the same code path, only toggling APP_VIRTUAL_THREADS_ENABLED.
- For portfolio use, rerun with larger request volume and include CPU/memory snapshots from Grafana.
