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

RUN groupadd --system --gid 1001 elaro \
 && useradd --system --uid 1001 --gid elaro --create-home --home-dir /home/elaro elaro \
 && mkdir -p /app /data \
 && chown -R elaro:elaro /app /data

COPY --from=build --chown=elaro:elaro /src/build/libs/elaro.jar /app/elaro.jar

USER elaro
WORKDIR /app
VOLUME ["/data"]
EXPOSE 8080

ENV ELARO_DATA_DIR=/data

ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/elaro.jar"]
