# Cookbook: event-bus bridges

Fan Foresignal's change feed out to Kafka or Redpanda, NATS JetStream, Redis or Valkey Streams,
ClickHouse and S3, with [Redpanda Connect](https://docs.redpanda.com/redpanda-connect/) or
[Vector](https://vector.dev). Artifacts:

- [`integrations/redpanda-connect/`](../../integrations/redpanda-connect/): one pipeline per target
  (`changes-to-kafka.yaml`, `changes-to-nats.yaml`, `changes-to-redis.yaml`,
  `changes-to-clickhouse.yaml`, `changes-to-s3.yaml`).
- [`integrations/vector/`](../../integrations/vector/): `vector.yaml` and `foresignal_sse.py`.

**Token scope:** a **read** token. It sees every source that is not private; give it a source or
kind restriction when only some data may leave (prompt 035 enforces restrictions).

**URLs used:** `/v1/changes` (polling with a cursor) and `/v1/changes/stream` (Server-Sent Events
with `Last-Event-ID`); `/v1/records` for a fresh snapshot after a resync.

## Two ways to read the feed

| | `/v1/changes?after=<seq>` (Redpanda Connect) | `/v1/changes/stream` (Vector) |
|---|---|---|
| Transport | One page per request (`limit` ≤ 10,000, default 1,000) | One long-lived SSE connection |
| Cursor | `lastSeq` of the page; store it, pass it as `after` | `id:` of each `change` event; resend as `Last-Event-ID` |
| Latency | The poll interval (5 s in the pipelines) | About a second |
| Durable cursor | A Connect cache (`file` by default; `redis` for several instances) | A file written by the script |

Both deliver every persisted version exactly once **per read**; a crash between delivering and
storing the cursor repeats some changes. Every pipeline is therefore at least once: **consumers
dedupe on `seq`**, which is unique and strictly increasing in commit order (with gaps).

## Shape of a change, and payloads

A change is the `Change` object of `GET /v1/changes`: `seq`, `source`, `externalId`, `kind`,
`version`, `observedAt`, `tombstone`, `geometry`, `persistReason`, `validUntil`, `bbox`, … and
`payload` **only with `payload=true`** (the default is false). Payloads can be large; fetch them
only for the kinds you need, for example a second pipeline with
`/v1/changes?after=…&kind=earthquake&payload=true`, or `GET /v1/records/{sourceId}/{externalId}`
for the few changes that matter. `FORESIGNAL_QUERY="kind=earthquake&payload=true"` does the same for
the SSE script.

## Keys, topics and deduplication

- **Key messages by `source:externalId`** wherever the target partitions or compacts by key: all
  versions of one record then stay in order on one partition.
- **Keep a non-compacted change-log topic keyed by `seq`** (`foresignal.changes` in the Kafka
  pipeline, `cleanup.policy=delete`). Compaction keeps only the newest message per key, which turns
  a log into current state. If you also want current state, write a second, compacted topic keyed by
  `source:externalId` (`foresignal.current`), and treat `tombstone: true` as a delete.
- **NATS JetStream** drops a repeated `Nats-Msg-Id` only inside the stream's duplicate window, two
  minutes by default. The pipeline sets `Nats-Msg-Id` to the `seq`, but a replay after a longer
  outage is not caught: consumers must still dedupe on `seq`. The subject must belong to a stream
  (for example one bound to `foresignal.changes.>`).
- **Redis or Valkey Streams:** `XADD foresignal:changes` with the change JSON in the field `change`
  and `MAXLEN ~ 1000000`. Vector's `redis` sink cannot write Streams (only lists, sorted sets and
  pub/sub channels); use the Redpanda Connect pipeline for Streams.
- **ClickHouse:** a `ReplacingMergeTree` ordered by `seq` collapses repeats on merge:

  ```sql
  CREATE TABLE foresignal_changes (
    seq UInt64, source LowCardinality(String), external_id String, kind LowCardinality(String),
    version UInt32, observed_at DateTime64(3, 'UTC'), persist_reason LowCardinality(String),
    tombstone Bool, doc String
  ) ENGINE = ReplacingMergeTree ORDER BY seq;
  ```

  The Vector example writes JSON rows into `foresignal_changes_raw` with `skip_unknown_fields`;
  create that table with the columns you keep, named as in the change (`seq`, `source`,
  `externalId`, …).
- **S3:** newline-delimited JSON objects partitioned by day (`date=YYYY-MM-DD/`).

## Redpanda Connect

```sh
FORESIGNAL_URL=http://foresignal.local:8080 FORESIGNAL_TOKEN=fsr_your_read_token \
  rpk connect run integrations/redpanda-connect/changes-to-kafka.yaml
```

- `generate` ticks every 5 s; the `cache` processor reads the cursor (or `FORESIGNAL_START_SEQ`,
  default 0, on the first run); the `http` processor fetches one page; `unarchive` splits `items`
  into one message per change, with metadata `seq` and `key`.
- The output is a `broker` with `fan_out_sequential`: the target first, then a `cache` output that
  stores the message's `seq` as the new cursor, so the cursor only moves after the target accepted
  the change.
- **Licenses.** Redpanda Connect is dual-licensed: Apache-2.0 for most connectors, and the Redpanda
  Community License for enterprise components. None of the components used here is an enterprise
  one. The connectors (`kafka_franz`, `nats_jetstream`, `redis_streams`, `aws_s3`, `sql_insert`) are
  Apache-2.0; the core pieces (`generate`, `http`, `cache`, `mapping`, `unarchive`, `catch`, `log`,
  `archive`, `broker`, the `file` cache) come from the Benthos core, which is MIT. `kafka_franz` is
  marked deprecated in favor of the `redpanda` output with the same fields; it still works.

## Vector

Vector's `http_client` source repeats one request; VRL can compute query parameters (for example
`now()`), but nothing carries a cursor from one response into the next request. The change feed is
therefore read by `foresignal_sse.py` (Python standard library only) through an `exec` source in
`streaming` mode:

```sh
FORESIGNAL_URL=http://foresignal.local:8080 FORESIGNAL_TOKEN=fsr_your_read_token \
  vector --config integrations/vector/vector.yaml
```

- The script opens `/v1/changes/stream?live=false` (persisted changes only), sends `Last-Event-ID`
  from its cursor file, prints each `change` as one JSON line and then writes the cursor.
- On `reset` with `buffer_overflow` or `replay_cap` it resumes from `resumeAfter`. On
  `resync_required` it exits with code 3: the history it would need was purged (see below).
- `respawn_on_exit` restarts it after a crash or a closed connection.
- The `remap` transform adds `key` (`source:externalId`) and `seq_key` for the Kafka sinks.

## Resync

`410 RESYNC_REQUIRED` (polling) or `reset {reason: "resync_required"}` (SSE) means retention purged
whole entities after your cursor. Changes are not lost silently; the feed refuses. Take a new
snapshot (`GET /v1/records?order=id`, paging with `nextCursor`, see the Agent Guide's "Keeping a
mirror"), reset the cursor to its `watermarkSeq`, and start again. Routine retention never causes
this.

## Limits

- `/v1/changes` returns at most 10,000 changes per request; the pipelines ask for 1,000 every 5 s,
  so a large backlog takes a while. Raise `limit` or lower the interval to catch up faster.
- At most 8 open streams per token; a stream replay is capped at 10,000 events before a `reset`.
- Each client may have 1,000 undelivered changes queued; a slow consumer gets `reset` and resumes.

## Privacy

- Private sources never appear in the feed for a token without private access.
- **Re-publishing license-restricted sources is the operator's responsibility.** Some upstream
  terms forbid redistribution (for example ACLED, OpenSky and aisstream). Until prompt 109 adds
  license egress policy to the feed, exclude those sources from a bridge yourself with `source=` or
  `kind=` filters, or a restricted token.
- A bridge copies data out of Foresignal's retention: deleting or purging a record in Foresignal
  does not delete it downstream. Apply tombstones and set retention on the targets too.
