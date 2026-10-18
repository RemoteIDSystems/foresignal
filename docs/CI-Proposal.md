# Foresignal — CI Proposal

Status: **proposal, not active.** This is the GitLab pipeline prompt 144 specified. Under the standing owner rule,
no `.gitlab-ci.yml` is added to the repository by an automated run; the owner decides whether to adopt it. To adopt
it, copy the YAML below to `.gitlab-ci.yml` at the repository root.

Nothing in this document has been run. The job commands are the Gradle tasks and scripts in this repository; the
runner set-up (a Docker executor with privileged mode for Docker-in-Docker) is the owner's to provide.

## 1. Stages

| Stage | Job | What it runs | Blocks the pipeline |
|---|---|---|---|
| `build` | `compile` | `./gradlew compileJava compileTestJava` | yes |
| `test` | `test` | `./gradlew test jacocoTestCoverageVerification` (≥ 90 % line coverage, `build.gradle.kts`), with a Docker-in-Docker service for Testcontainers | yes |
| `api` | `api-compat` | `oasdiff breaking docs/api/v1.0.0/OpenAPI.json docs/OpenAPI.json --fail-on ERR`, the same check as `./gradlew checkApiCompat` | yes |
| `api` | `metric-drift` | `scripts/verify/metric-drift.sh` against a running instance, or a saved scrape in `SCRAPE_FILE` | no (`allow_failure: true`) |
| `lint` | `lint-openapi` | the Redocly lint that `./gradlew lintOpenApi` runs | yes |

### Why each job looks the way it does

- **Image.** The Java jobs use `eclipse-temurin:21-jdk`, the build-stage image of the `Dockerfile`, so CI compiles
  with the same JDK as the container build.
- **Testcontainers.** The integration tests start PostgreSQL through Testcontainers (`PostgisContainers` builds the
  `docker/db` image, PostGIS plus pgvector, once per test JVM) and Toxiproxy. In GitLab that needs a `docker:dind`
  service and a runner registered with `privileged = true` (GitLab, "Use Docker-in-Docker": Docker-in-Docker always
  needs privileged mode). The variables follow GitLab's TLS-disabled Docker-in-Docker example and the Testcontainers
  GitLab CI page:
  - `DOCKER_HOST: "tcp://docker:2375"`: Testcontainers talks to the daemon of the `docker` service on the non-TLS
    port.
  - `DOCKER_TLS_CERTDIR: ""`: the service starts without TLS.
  - `TESTCONTAINERS_HOST_OVERRIDE: "docker"`: containers' mapped ports are published on the service host, not on
    `localhost`. Testcontainers derives this from `DOCKER_HOST` already; setting it explicitly costs nothing and
    keeps the address correct if the job ever moves to a socket-mount runner (where Testcontainers documents this
    variable as the Docker host's address).
  - The service runs with `command: ["--tls=false"]`, as the Testcontainers example does, so the daemon does not
    pause at startup waiting for certificates.
  The test JVM is given 4 GB (`maxHeapSize = "4g"` in `build.gradle.kts`), so the runner needs at least that plus
  the database container.
- **`checkApiCompat` in CI.** The Gradle task runs a local `oasdiff` binary, else the Docker image
  `tufin/oasdiff:v1.33.0`, else skips with a warning. A temurin image has neither, so the Gradle task would skip,
  and a skipped gate is no gate. The job therefore runs the identical `oasdiff breaking` command inside the pinned
  `tufin/oasdiff:v1.33.0` image (Alpine-based, entrypoint `/usr/bin/oasdiff`, cleared so GitLab can run a shell).
  `--fail-on ERR` fails the job on any breaking change. The frozen spec is `docs/api/v1.0.0/OpenAPI.json`.
- **Metric drift.** `metric-drift.sh` compares `docs/Metrics.md`, the Grafana dashboards and a Prometheus scrape.
  A scrape needs a running broker with data sources enabled, which a CI job does not have; set a CI/CD variable
  `SCRAPE_FILE` (a file-type variable holding a saved `/actuator/prometheus` scrape) or `FORESIGNAL_URL` plus a
  masked `FORESIGNAL_TOKEN` for a reachable instance. Without either, the script exits 2, and the job is
  `allow_failure: true` so it reports without blocking. `MetricNamesDocumentedTest` in the `test` stage already
  covers the startup half of the same check.
- **OpenAPI lint.** `./gradlew lintOpenApi` runs `npx --yes @redocly/cli@2.62.1 lint docs/OpenAPI.json`
  (`build.gradle.kts`, task `lintOpenApi`) and needs Node.js ≥ 20.19 with `npx`; the temurin image has no Node.js.
  The job runs the same command in a Node.js image. Alternatively, install Node.js ≥ 20.19 in a JDK image and call
  `./gradlew lintOpenApi`. `test` and `jacocoTestCoverageVerification` do not depend on the lint (only `check`
  does), so the `test` job needs no Node.js; if `check` is ever added to its command, pass
  `-PskipOpenApiLint=true` there, since the lint has its own job.

## 2. The pipeline

```yaml
# Foresignal CI (proposal, docs/CI-Proposal.md). Not active until the owner copies it to .gitlab-ci.yml.

stages:
  - build
  - test
  - api
  - lint

default:
  image: eclipse-temurin:21-jdk

variables:
  GRADLE_USER_HOME: "$CI_PROJECT_DIR/.gradle-home"
  GRADLE_OPTS: "-Dorg.gradle.daemon=false"

.gradle-cache:
  cache:
    key:
      files:
        - gradle/wrapper/gradle-wrapper.properties
        - gradle/libs.versions.toml
    paths:
      - .gradle-home/caches/
      - .gradle-home/wrapper/

compile:
  stage: build
  extends: .gradle-cache
  script:
    - ./gradlew compileJava compileTestJava

test:
  stage: test
  extends: .gradle-cache
  # Testcontainers needs a Docker daemon: Docker-in-Docker, on a runner with privileged = true.
  services:
    - name: docker:dind
      # Explicitly disable TLS so the daemon does not wait for certificates at startup.
      command: ["--tls=false"]
  variables:
    # Testcontainers uses the daemon of the dind service, on the non-TLS port.
    DOCKER_HOST: "tcp://docker:2375"
    # The dind service starts without TLS.
    DOCKER_TLS_CERTDIR: ""
    # Mapped container ports are published on the service host.
    TESTCONTAINERS_HOST_OVERRIDE: "docker"
  script:
    - ./gradlew test jacocoTestCoverageVerification
  artifacts:
    when: always
    reports:
      junit: build/test-results/test/TEST-*.xml
    paths:
      - build/reports/tests/test/
      - build/reports/jacoco/

api-compat:
  stage: api
  needs: []
  image:
    name: tufin/oasdiff:v1.33.0
    entrypoint: [""]
  script:
    # The same check as ./gradlew checkApiCompat: /v1 is additive-only (NFR-10).
    - oasdiff breaking docs/api/v1.0.0/OpenAPI.json docs/OpenAPI.json --fail-on ERR

metric-drift:
  stage: api
  needs: []
  image: alpine:3
  # A scrape needs a running broker; without SCRAPE_FILE or FORESIGNAL_URL/FORESIGNAL_TOKEN the script exits 2.
  allow_failure: true
  before_script:
    - apk add --no-cache curl
  script:
    - sh scripts/verify/metric-drift.sh

lint-openapi:
  stage: lint
  needs: []
  image: node:22
  script:
    # The command ./gradlew lintOpenApi runs (build.gradle.kts), without needing a JDK in this image.
    - npx --yes @redocly/cli@2.62.1 lint docs/OpenAPI.json
```

Notes on the YAML:

- `compile` must pass before `test` runs (stage order). `api-compat`, `metric-drift` and `lint-openapi` declare
  `needs: []`, so they start at once and do not wait for the test stage; remove `needs: []` to run them strictly in
  stage order.
- Image tags: `eclipse-temurin:21-jdk` is the `Dockerfile` build stage. `tufin/oasdiff:v1.33.0` is the version
  pinned for `checkApiCompat`. `docker:dind`, `alpine:3` and `node:22` are floating tags; pin them to the exact
  versions the owner's runner uses before adopting.
- When `lintOpenApi`'s Redocly version changes in `build.gradle.kts`, change it here too.

## 3. What the API gate enforces

### NFR-10: additive-only `/v1`

The Product Charter's NFR-10: **`/v1` is additive-only once released; breaking changes go to `/v2`.** v1.0.0 is
the release, and `docs/api/v1.0.0/OpenAPI.json` is the frozen copy of the spec as released (`info.version`
`1.0.0`). Every later change to `docs/OpenAPI.json` is compared against it.

`oasdiff breaking … --fail-on ERR` fails on the changes oasdiff classifies at level `ERR` (breaking for existing
clients, such as a removed operation or response property, a changed type, or a newly required request property);
changes it classifies as `WARN` are printed and do not fail the job. oasdiff's own rule list is the authority on
which change gets which level.

### The API-evolution convention (prompt 020)

Architecture, "API evolution", states the rules prompt 020 wrote down; this gate is the machine check of the part
of them oasdiff can see:

- **Allowed in `/v1`:** new operations, new optional request fields, new response fields, new enum values, new
  error codes and new SSE event types.
- **Clients must be tolerant:** they ignore unknown fields, unknown enum values and unknown SSE event names. The
  generated SDKs (prompt 141) must be configured to do the same.
- **Never in `/v1`:** removing or renaming a field, changing a field's type, tightening the validation of an
  existing field, or adding a required request field. Such a change goes to `/v2`.
- **Deprecation:** mark the operation or field `deprecated: true` in the spec and send the `Deprecation` (RFC 9745)
  and `Sunset` (RFC 8594) response headers. It stays for at least one minor release before it is removed in `/v2`.
- **Every spec change** regenerates Postman (`./gradlew generatePostman`), passes `./gradlew lintOpenApi`, is
  recorded in `info.x-foresignal-changelog`, and bumps `info.version` by a minor version when it adds to the API.
- **Every operation** carries `x-foresignal-scope`, `x-foresignal-prompt` and `x-foresignal-status`.
- **Error codes** are stable identifiers in `ErrorCodes`; a code is never reused for a different condition.

`OpenApiContractIT`, `SpecAnnotationsTest` and `PostmanCollectionContractTest` (in the `test` stage) and the Redocly
lint (in the `lint` stage) enforce the other machine-checkable parts.

### When a breaking change is intended

It does not go into `/v1`. Add the new shape under `/v2` (or as a new operation next to the old one, with the old
one deprecated), and leave `docs/api/v1.0.0/OpenAPI.json` untouched: the frozen file changes only when a new major
version is frozen beside it (for example `docs/api/v2.0.0/OpenAPI.json`).
