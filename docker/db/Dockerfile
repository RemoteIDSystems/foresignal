# Foresignal database image (Architecture §14, prompt 027): PostGIS plus pgvector.
#
# The base is the PostGIS image Foresignal has always pinned. It already carries the PostgreSQL
# Global Development Group (PGDG) apt repository for PostgreSQL 17, which publishes pgvector as
# postgresql-17-pgvector, so nothing else is added to the trust chain.
#
# The base is Debian 11 (bullseye), whose PGDG suite has moved from apt.postgresql.org to the
# archive host apt-archive.postgresql.org (same signing key; the old host answers 404). The
# image's own source list is pointed there before installing.
#
# Built by docker-compose.yml (image foresignal-db:17-3.5-pgvector) and, once per test JVM, by
# Testcontainers (PostgisContainers). pgvector is optional at run time: without it Foresignal
# starts, text search works and GET /v1/search/status explains why semantic search is off.
FROM postgis/postgis:17-3.5

RUN sed -i 's#http://apt.postgresql.org/#http://apt-archive.postgresql.org/#' /etc/apt/sources.list.d/pgdg.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends postgresql-17-pgvector \
    && rm -rf /var/lib/apt/lists/*
