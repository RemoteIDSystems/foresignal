package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.InboxAccess;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What one {@code satdump-{station}} source remembers about its DCS and EMWIN files between inbox scans
 * (prompt 107). Not thread-safe; the station synchronizes.
 *
 * <p><b>Per-file settle.</b> SatDump writes EMWIN and DCS files continuously while GOES HRIT is received,
 * so they are processed as they settle, independent of any pass folder: a file is ready once its size and
 * modification time are unchanged from the previous scan, and is processed once per {@code size|mtime}.
 * Only files modified after a {@linkplain #watermark() watermark} are tracked, so the state stays small:
 * after each scan the watermark advances to {@value #TRACK_MINUTES} minutes before the scan, but never
 * past a file that is still waiting (unsettled, or held back by a per-scan cap).
 *
 * <p><b>Newest wins.</b> For each warning (VTEC {@code office.phen.sig.etn}) the issue time of the newest
 * product applied is kept, and an older product never overrides it, so a late-processed {@code NEW}
 * cannot resurrect a canceled warning. For each DCS address the newest message time is kept, and an
 * older message is not emitted again. Both maps are pruned by age and capped.
 *
 * @see SatDumpExtras
 */
public final class ExtrasTracker {

    /** Minutes of files kept tracked behind the scan time. */
    public static final int TRACK_MINUTES = 10;

    /** How long a warning's newest issue time is remembered. */
    public static final Duration WARNING_MEMORY = Duration.ofDays(7);

    /** How long a DCS address's newest message time is remembered. */
    public static final Duration DCS_MEMORY = Duration.ofDays(3);

    /** The most warnings and the most DCS addresses remembered. */
    public static final int MAX_REMEMBERED = 10_000;

    /**
     * The persisted state.
     *
     * @param watermark files modified at or before this epoch millisecond are no longer tracked; null before
     *     the first scan
     * @param pending each waiting file's {@code size|mtime} at the last scan, by key
     * @param done each processed file's {@code size|mtime}, by key
     * @param warnings each warning's newest applied issue time, epoch milliseconds
     * @param dcs each DCS address's newest emitted message time, epoch milliseconds
     */
    public record State(Long watermark, Map<String, String> pending, Map<String, String> done,
            Map<String, Long> warnings, Map<String, Long> dcs) {}

    private Long watermark;
    private final Map<String, String> pending = new LinkedHashMap<>();
    private final Map<String, String> done = new LinkedHashMap<>();
    private final Map<String, Long> warnings = new LinkedHashMap<>();
    private final Map<String, Long> dcs = new LinkedHashMap<>();

    /** An empty tracker. */
    public ExtrasTracker() {}

    /** Restores a persisted state; null leaves the tracker empty. */
    public void restore(State state) {
        watermark = null;
        pending.clear();
        done.clear();
        warnings.clear();
        dcs.clear();
        if (state == null) {
            return;
        }
        watermark = state.watermark();
        putAll(pending, state.pending());
        putAll(done, state.done());
        putAll(warnings, state.warnings());
        putAll(dcs, state.dcs());
    }

    /** The state to persist. */
    public State state() {
        return new State(watermark, new LinkedHashMap<>(pending), new LinkedHashMap<>(done),
                new LinkedHashMap<>(warnings), new LinkedHashMap<>(dcs));
    }

    /** The watermark, or null before the first scan. */
    public Long watermark() {
        return watermark;
    }

    /**
     * A candidate file.
     *
     * @param key a stable key (the path relative to the inbox path)
     * @param file the file
     */
    public record Candidate(String key, InboxAccess.FileStat file) {

        /** {@code size|mtime}. */
        String signature() {
            return file.size() + "|" + file.modified().toEpochMilli();
        }
    }

    /**
     * The settled, not yet processed files among {@code candidates}: files at or before the watermark and
     * files already processed at their current {@code size|mtime} are skipped; a file is ready when it is
     * unchanged since the previous scan. Every other file is remembered as waiting.
     */
    public List<Candidate> ready(List<Candidate> candidates) {
        Map<String, String> seen = new LinkedHashMap<>();
        List<Candidate> out = new ArrayList<>();
        for (Candidate c : candidates) {
            long mtime = c.file().modified().toEpochMilli();
            if (watermark != null && mtime <= watermark) {
                continue;
            }
            String signature = c.signature();
            if (signature.equals(done.get(c.key()))) {
                continue;
            }
            if (signature.equals(pending.get(c.key()))) {
                out.add(c);
            }
            seen.put(c.key(), signature);
        }
        pending.clear();
        pending.putAll(seen);
        return out;
    }

    /** Records that {@code candidate} was processed at its current {@code size|mtime}. */
    public void markDone(Candidate candidate) {
        pending.remove(candidate.key());
        done.put(candidate.key(), candidate.signature());
    }

    /**
     * Advances the watermark after a scan at {@code now}: to {@value #TRACK_MINUTES} minutes before it,
     * but not past the oldest waiting file, and forgets processed files at or before it.
     */
    public void advance(Instant now) {
        long target = now.minus(Duration.ofMinutes(TRACK_MINUTES)).toEpochMilli();
        for (String signature : pending.values()) {
            long mtime = mtimeOf(signature);
            if (mtime - 1 < target) {
                target = mtime - 1;
            }
        }
        if (watermark == null || target > watermark) {
            watermark = target;
        }
        long w = watermark;
        done.values().removeIf(signature -> mtimeOf(signature) <= w);
        pending.values().removeIf(signature -> mtimeOf(signature) <= w);
    }

    /**
     * True when a product issued at {@code issuedMs} may update warning {@code externalId}: no newer
     * product was applied. When true, the issue time is remembered.
     */
    public boolean acceptWarning(String externalId, long issuedMs) {
        Long newest = warnings.get(externalId);
        if (newest != null && issuedMs < newest) {
            return false;
        }
        warnings.remove(externalId);
        warnings.put(externalId, issuedMs);
        return true;
    }

    /**
     * True when a DCS message at {@code timeMs} is newer than the last one emitted for {@code address};
     * when true, the time is remembered.
     */
    public boolean acceptDcs(String address, long timeMs) {
        Long newest = dcs.get(address);
        if (newest != null && timeMs <= newest) {
            return false;
        }
        dcs.remove(address);
        dcs.put(address, timeMs);
        return true;
    }

    /** The newest message time emitted for {@code address}, or null. */
    Long dcsTime(String address) {
        return dcs.get(address);
    }

    /** Forgets warnings and DCS addresses older than their memory, then caps both maps. */
    public void prune(Instant now) {
        long warningCut = now.minus(WARNING_MEMORY).toEpochMilli();
        long dcsCut = now.minus(DCS_MEMORY).toEpochMilli();
        warnings.values().removeIf(t -> t < warningCut);
        dcs.values().removeIf(t -> t < dcsCut);
        cap(warnings);
        cap(dcs);
    }

    private static void cap(Map<String, Long> map) {
        Iterator<String> it = map.keySet().iterator();
        while (map.size() > MAX_REMEMBERED && it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    private static long mtimeOf(String signature) {
        int bar = signature.indexOf('|');
        try {
            return Long.parseLong(signature.substring(bar + 1));
        } catch (RuntimeException e) {
            return Long.MIN_VALUE;
        }
    }

    private static <V> void putAll(Map<String, V> target, Map<String, V> source) {
        if (source != null) {
            target.putAll(source);
        }
    }
}
