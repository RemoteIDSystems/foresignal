# syntax=docker/dockerfile:1

# ---- Build stage -------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

# Wrapper and build definition first, so dependency resolution caches.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
# gradle/sdk.gradle.kts reads the SDK tool versions while the build is configured; the rest of sdk/ is not needed.
COPY sdk/versions.properties sdk/versions.properties

# docs/OpenAPI.json is copied onto the classpath by processResources.
COPY docs docs
COPY src src

# Gradle's default 512 MiB build heap thrashes compiling the full source tree from scratch.
RUN ./gradlew bootJar -x test --no-daemon "-Dorg.gradle.jvmargs=-Xmx3g -XX:MaxMetaspaceSize=1g"

# ---- Runtime stage -----------------------------------------------------------
# JDK 25, not 21: the collector runs every fetch on a virtual thread, and on JDK 21 a virtual thread that blocks
# inside synchronized (ConcurrentHashMap.computeIfAbsent, Hibernate's query plan, DefaultFetchContext) pins its
# carrier. With the free sources enabled, dozens pinned waiting for a pool connection took every carrier, so the
# threads holding the connections never ran to return them and the server stopped answering. JDK 24+ (JEP 491)
# unmounts a virtual thread blocked in synchronized. The bytecode is still compiled for 21.
FROM eclipse-temurin:25-jre AS runtime

# Release metadata (prompt 144). VERSION matches the Gradle project version and /v1/system.
ARG VERSION=1.0.0
LABEL org.opencontainers.image.title="Foresignal" \
      org.opencontainers.image.description="Foresignal (foresignal.ai): a headless intelligence broker. Unrelated to foresignal.com." \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.url="https://foresignal.ai" \
      org.opencontainers.image.licenses="MIT"

# Prompt 054: optional GDAL for SatDump GeoTIFFs in another CRS or BigTIFF. Off by default; build with
# --build-arg WITH_GDAL=true to install gdal-bin only (the gdalwarp program: no JNI binding, no AGPL
# drivers). Foresignal runs it as a subprocess (foresignal.raster.gdal.enabled, default auto).
ARG WITH_GDAL=false

# curl for the compose healthcheck on /actuator/health/readiness (019).
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && if [ "$WITH_GDAL" = "true" ]; then apt-get install -y --no-install-recommends gdal-bin; fi \
 && rm -rf /var/lib/apt/lists/*

RUN groupadd --system --gid 1001 foresignal \
 && useradd --system --uid 1001 --gid foresignal --create-home --home-dir /home/foresignal foresignal \
 && mkdir -p /app /data /data/tmp \
 && chown -R foresignal:foresignal /app /data

COPY --from=build --chown=foresignal:foresignal /src/build/libs/foresignal.jar /app/foresignal.jar

# Runs as the non-root foresignal user (uid 1001). docker-compose.yml also makes the root filesystem read-only:
# the only writable paths are the /data volume (data, java.io.tmpdir, native-library extraction) and a tmpfs /tmp.
USER foresignal
WORKDIR /app
VOLUME ["/data"]
EXPOSE 8080

ENV FORESIGNAL_DATA_DIR=/data
# Prompt 046: temporary files (and libaec's native library, which JNA extracts and loads) go to the
# data volume, not the container's /tmp; the volume must allow executables (named volumes do).
# Compose appends the same flags to its own JAVA_TOOL_OPTIONS.
ENV JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=/data/tmp -Djna.tmpdir=/data/tmp"

ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/foresignal.jar"]
