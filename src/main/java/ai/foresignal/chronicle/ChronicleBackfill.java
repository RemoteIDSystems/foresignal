package ai.foresignal.chronicle;

import ai.foresignal.retention.DownsampleRanges;
import ai.foresignal.retention.RetentionStateStore;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.query.SeqClockQuery;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Copies matching history still retained globally into a newly activated chronicle (prompt 131, {@code backfill}):
 * every version ingested from {@code from} up to the chronicle's {@code capture_from_seq}, read from
 * {@code record_version} (TRACK raw and downsampled leaves, STANDARD history) and written with each layer's capture
 * mode, marked {@code backfilled}. Rollup-only layers count the same rows. Caveats name what was already gone (the
 * retention floor of a layer source passed {@code from}) or thinned (the range overlaps a downsampled track leaf). A
 * {@code backfill} thread item reports the counts and caveats; backfills write no other thread items.
 */
@Component
public class ChronicleBackfill {

    private static final Logger log = LoggerFactory.getLogger(ChronicleBackfill.class);

    private final ChronicleRepository chronicles;
    private final ChronicleLayerRepository layers;
    private final ChronicleLayerCompiler compiler;
    private final ChronicleStorage storage;
    private final ChronicleCaptureService capture;
    private final ChronicleThreadService thread;
    private final RetentionStateStore retention;
    private final DownsampleRanges downsampled;
    private final SeqClockQuery seqClock;
    private final NamedParameterJdbcTemplate named;
    private final ChronicleProperties properties;
    private final Clock clock;
    private final TransactionTemplate tx;

    /** Creates the backfill. */
    public ChronicleBackfill(ChronicleRepository chronicles, ChronicleLayerRepository layers,
            ChronicleLayerCompiler compiler, ChronicleStorage storage, ChronicleCaptureService capture,
            ChronicleThreadService thread, RetentionStateStore retention, DownsampleRanges downsampled,
            SeqClockQuery seqClock, NamedParameterJdbcTemplate named, ChronicleProperties properties, Clock clock,
            PlatformTransactionManager transactions) {
        this.chronicles = chronicles;
        this.layers = layers;
        this.compiler = compiler;
        this.storage = storage;
        this.capture = capture;
        this.thread = thread;
        this.retention = retention;
        this.downsampled = downsampled;
        this.seqClock = seqClock;
        this.named = named;
        this.properties = properties;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactions);
    }

    /**
     * What a backfill did.
     *
     * @param fromSeq the first seq read
     * @param toSeq the last seq read
     * @param versions versions written
     * @param rollups rollup increments
     * @param caveats the caveats ({@code Caveat} shape)
     */
    public record Result(long fromSeq, long toSeq, long versions, long rollups, List<Map<String, Object>> caveats) {}

    /**
     * Backfills a chronicle from {@code from}.
     *
     * @param chronicleId the chronicle (active, with its leaf)
     * @param from the earliest ingest time to copy
     * @return what was copied, or null when the chronicle is gone or not capturing
     */
    public Result run(String chronicleId, Instant from) {
        ChronicleEntity c = chronicles.findById(chronicleId).orElse(null);
        if (c == null || c.getCaptureFromSeq() == null || !c.isLeafCreated()) {
            return null;
        }
        List<CompiledLayer> compiled = new ArrayList<>();
        for (ChronicleLayerEntity l : layers.findByChronicleIdOrderByAddedAtAscIdAsc(chronicleId)) {
            if (l.live() && l.isEnabled()) {
                compiler.compile(c, l, true).ifPresent(compiled::add);
            }
        }
        ChronicleIndex.Entry entry = new ChronicleIndex.Entry(c, compiled);
        ChronicleIndex index = new ChronicleIndex(List.of(entry), clock.instant());
        long toSeq = c.getCaptureFromSeq();
        long fromSeq = Math.max(0, seqClock.cutoffSeq(named, from) - 1);
        List<Map<String, Object>> caveats = caveats(entry, from, fromSeq, toSeq);
        long versions = 0;
        long rollups = 0;
        long lo = fromSeq;
        Set<String> filter = index.sourceFilter();
        while (lo < toSeq) {
            long hi = Math.min(lo + properties.chunkSeqs(), toSeq);
            List<CapturedRow> rows = filter != null && filter.isEmpty() ? List.of()
                    : storage.readVersions(lo, hi, filter);
            List<ChronicleCaptureService.Match> found = new ArrayList<>();
            for (CapturedRow row : rows) {
                if (row.ingestedAt() != null && row.ingestedAt().isBefore(from)) {
                    continue;
                }
                for (CompiledLayer layer : index.candidates(row.sourceId())) {
                    if (layer.matches(row)) {
                        found.add(new ChronicleCaptureService.Match(layer, row));
                    }
                }
            }
            if (!found.isEmpty()) {
                ChronicleCaptureService.Written w = tx.execute(s -> {
                    long[] usage = storage.usage(chronicleId);
                    boolean exceeded = usage != null && usage[1] > 0 && usage[0] >= usage[1];
                    ChronicleCaptureService.Written written = capture.write(entry, found, exceeded, true,
                            clock.instant());
                    storage.advance(chronicleId, null, clock.instant(), written.rows(), written.bytes());
                    return written;
                });
                if (w != null) {
                    versions += w.versions();
                    rollups += w.rollups();
                }
            }
            lo = hi;
        }
        long[] usage = storage.usage(chronicleId);
        if (usage != null) {
            capture.capTransition(c, usage[0], usage[1]);
        }
        Result result = new Result(fromSeq, toSeq, versions, rollups, caveats);
        report(c, from, result);
        return result;
    }

    /** The caveats of a backfill range: history already deleted, and downsampled track ranges. */
    List<Map<String, Object>> caveats(ChronicleIndex.Entry entry, Instant from, long fromSeq, long toSeq) {
        List<Map<String, Object>> out = new ArrayList<>();
        Set<String> sources = entry.sources();
        long floor = retention.lowestRetainedSeq(sources == null ? Set.of() : sources);
        if (floor > fromSeq + 1) {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("code", "HISTORY_GONE");
            c.put("text", "Global retention had already deleted part of the requested history (versions below seq "
                    + floor + "); the backfill starts where history is still retained.");
            c.put("data", Map.of("from", from.toString(), "floorSeq", floor));
            out.add(c);
        }
        if (downsampled.overlaps(HistoryTier.TRACK, fromSeq, toSeq)) {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("code", "HISTORY_THINNED");
            c.put("text", "Part of the range lies in downsampled track history (044): only event versions, the last"
                    + " version per entity per leaf and one version per downsample interval remain there.");
            c.put("data", Map.of("fromSeq", fromSeq, "toSeq", toSeq));
            out.add(c);
        }
        return out;
    }

    private void report(ChronicleEntity c, Instant from, Result result) {
        try {
            tx.executeWithoutResult(s -> {
                ChronicleEventEntity item = thread.item(c.getId(), ChronicleVocabulary.ITEM_BACKFILL, clock.instant(),
                        "Backfill from " + from + ": " + result.versions() + " versions copied");
                item.setBackfilled(true);
                item.setSummary(result.caveats().isEmpty() ? "Copied from retained global history."
                        : result.caveats().size() + " caveat(s): global history was already thinned or gone.");
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("from", from.toString());
                snapshot.put("fromSeq", result.fromSeq());
                snapshot.put("toSeq", result.toSeq());
                snapshot.put("versions", result.versions());
                snapshot.put("rollups", result.rollups());
                snapshot.put("caveats", result.caveats());
                item.setSnapshot(snapshot);
                thread.append(item);
            });
        } catch (RuntimeException e) {
            log.warn("Could not report the backfill of chronicle {}: {}", c.getId(), e.toString());
        }
    }
}
