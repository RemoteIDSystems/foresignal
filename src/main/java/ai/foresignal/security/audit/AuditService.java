package ai.foresignal.security.audit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * The audit log writer (prompt 023).
 *
 * <p>{@link #record} never blocks a request: entries go into a bounded queue
 * ({@value #QUEUE_CAPACITY}), and a background virtual thread inserts them in JDBC batches. When the
 * queue is full the entry is dropped and counted ({@code foresignal.audit.dropped}); auditing must
 * not take the API down with it.
 *
 * <p>Recorded by the token filter: every authenticated write (non-GET/HEAD), every 401/403 on a
 * write path, pairing claims and token revocations. Prompt 025 adds MCP tool calls through
 * {@link #record}.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    /** Entries held before new ones are dropped. */
    public static final int QUEUE_CAPACITY = 10_000;

    /** Entries per insert batch. */
    static final int BATCH = 500;

    static final String INSERT = """
            INSERT INTO audit_event (at, actor_kind, token_id, token_name, principal_id, scope, method, path,
                                     operation_id, status, target_type, target_id, remote_ip, detail)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb))
            """;

    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Counter dropped;
    private final BlockingQueue<AuditRecord> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final ReentrantLock writing = new ReentrantLock();
    private volatile boolean running;
    private Thread worker;

    /** Creates the service. */
    public AuditService(JdbcTemplate jdbc, JsonMapper json, MeterRegistry meters) {
        this.jdbc = jdbc;
        this.json = json;
        this.dropped = Counter.builder("foresignal.audit.dropped")
                .description("Audit entries dropped because the audit queue was full")
                .register(meters);
    }

    /** Starts the background writer. */
    @PostConstruct
    public void start() {
        running = true;
        worker = Thread.ofVirtual().name("audit-writer").start(this::drainLoop);
    }

    /** Stops the writer after writing what is queued. */
    @PreDestroy
    public void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();
            try {
                worker.join(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        flush();
    }

    /**
     * Queues {@code entry}; drops and counts it when the queue is full.
     *
     * @return true when queued
     */
    public boolean record(AuditRecord entry) {
        if (entry == null) {
            return false;
        }
        boolean queued = queue.offer(entry);
        if (!queued) {
            dropped.increment();
        }
        return queued;
    }

    /** Entries dropped since start. */
    public long droppedCount() {
        return (long) dropped.count();
    }

    /** Writes every queued entry now, on the calling thread (tests, shutdown). */
    public void flush() {
        writing.lock();
        try {
            List<AuditRecord> batch = new ArrayList<>();
            while (queue.drainTo(batch, BATCH) > 0) {
                insert(batch);
                batch.clear();
            }
        } finally {
            writing.unlock();
        }
    }

    private void drainLoop() {
        List<AuditRecord> batch = new ArrayList<>();
        while (running) {
            try {
                AuditRecord first = queue.poll(200, TimeUnit.MILLISECONDS);
                if (first == null) {
                    continue;
                }
                writing.lock();
                try {
                    batch.add(first);
                    queue.drainTo(batch, BATCH - 1);
                    insert(batch);
                } finally {
                    batch.clear();
                    writing.unlock();
                }
            } catch (InterruptedException e) {
                if (!running) {
                    return;
                }
            } catch (RuntimeException e) {
                log.warn("Could not write audit entries: {}", e.toString());
            }
        }
    }

    private void insert(List<AuditRecord> batch) {
        if (batch.isEmpty()) {
            return;
        }
        try {
            jdbc.batchUpdate(INSERT, batch, batch.size(), (ps, r) -> {
                ps.setTimestamp(1, Timestamp.from(r.at()));
                ps.setString(2, r.actorKind());
                ps.setObject(3, r.tokenId(), Types.OTHER);
                ps.setString(4, truncate(r.tokenName(), 200));
                ps.setString(5, r.principalId());
                ps.setString(6, r.scope());
                ps.setString(7, r.method());
                ps.setString(8, truncate(r.path(), 2048));
                ps.setString(9, truncate(r.operationId(), 128));
                if (r.status() == null) {
                    ps.setNull(10, Types.INTEGER);
                } else {
                    ps.setInt(10, r.status());
                }
                ps.setString(11, truncate(r.targetType(), 64));
                ps.setString(12, truncate(r.targetId(), 256));
                ps.setString(13, truncate(r.remoteIp(), 64));
                ps.setString(14, json.writeValueAsString(r.detail()));
            });
        } catch (RuntimeException e) {
            log.warn("Dropped {} audit entries that could not be written: {}", batch.size(), e.toString());
            dropped.increment(batch.size());
        }
    }

    private static String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
