# syntax=docker/dockerfile:1

# ---- Build stage -------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

# Wrapper and build definition first, so dependency resolution caches.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle

# docs/OpenAPI.json is copied onto the classpath by processResources.
COPY docs docs
COPY src src

RUN ./gradlew bootJar -x test --no-daemon

# ---- Runtime stage -----------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime

# curl for the compose healthcheck on /actuator/health/readiness (019).
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

RUN groupadd --system --gid 1001 foresignal \
 && useradd --system --uid 1001 --gid foresignal --create-home --home-dir /home/foresignal foresignal \
 && mkdir -p /app /data \
 && chown -R foresignal:foresignal /app /data

COPY --from=build --chown=foresignal:foresignal /src/build/libs/foresignal.jar /app/foresignal.jar

USER foresignal
WORKDIR /app
VOLUME ["/data"]
EXPOSE 8080

ENV FORESIGNAL_DATA_DIR=/data

ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/foresignal.jar"]
