# Resilent Mesh: Chaos-Resilient Banking Core

Resilent Mesh is a Java 21 distributed banking simulation focused on three engineering themes:

- Reliability under partial failures
- Observability for fast diagnosis
- Concurrency at scale with virtual threads

## System Scope

The platform is split into three services:

- `user-service`: user profile and tier source of truth
- `wallet-service`: wallet balance API, depends on `user-service`
- `transaction-service`: payment endpoint, depends on `wallet-service`

Each service uses Spring Boot 3.x, Actuator, Micrometer Prometheus metrics, and Java 21 virtual threads.

## Key Features

- Uses Project Loom (`Executors.newVirtualThreadPerTaskExecutor`) to model high concurrency.
- Uses Resilience4j retries + circuit breakers to stop cascading failures.
- Exposes operational metrics for Prometheus/Grafana and enables Kubernetes-native autoscaling.
- Includes EKS-oriented manifests: requests/limits, probes, IRSA-ready service account, and HPA.

## Repository Layout

- `services/user-service`
- `services/wallet-service`
- `services/transaction-service`
- `infra/docker` local stack via Docker Compose
- `infra/prometheus` scrape config
- `infra/grafana` datasource provisioning
- `infra/k8s` deployment and autoscaling manifests

## Run locally (without Docker)

From repository root:

```bash
mvn -pl services/user-service spring-boot:run
mvn -pl services/wallet-service spring-boot:run
mvn -pl services/transaction-service spring-boot:run
```

Quick API checks:

```bash
curl http://localhost:8081/api/users/u-123
curl http://localhost:8082/api/wallets/u-123
curl -X POST http://localhost:8083/api/transactions \
  -H 'Content-Type: application/json' \
  -d '{"userId":"u-123","amount":12.50}'
```

Metrics endpoints:

- `http://localhost:8081/actuator/prometheus`
- `http://localhost:8082/actuator/prometheus`
- `http://localhost:8083/actuator/prometheus`

## Run local observability stack

```bash
docker compose -f infra/docker/docker-compose.yml up --build
```

Then open:

- Grafana: `http://localhost:3000` (`admin`/`admin`)
- Prometheus: `http://localhost:9090`

## EKS strategy (production baseline)

- Managed node groups across 3 AZs
- IRSA for pod-level AWS access control
- VPC CNI for pod-native VPC networking
- CPU-based HPA with room for custom metrics later
- JVM memory tuned via container limits and `-XX:MaxRAMPercentage`

## Project Milestones

- [x] Milestone 0: Core platform scaffold (3 services, Java 21, virtual threads, resilience, observability, Kubernetes baseline)
- [ ] Milestone 1: Load testing + benchmark report (virtual threads vs platform threads)
- [ ] Milestone 2: Data consistency hardening (PostgreSQL, idempotency keys, optimistic locking)
- [ ] Milestone 3: Distributed tracing + incident dashboard (OpenTelemetry + Grafana)
- [ ] Milestone 4: Chaos engineering + custom-metric autoscaling (Prometheus Adapter + fault injection)
