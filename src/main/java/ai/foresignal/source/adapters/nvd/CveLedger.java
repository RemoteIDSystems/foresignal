package ai.foresignal.source.adapters.nvd;

import ai.foresignal.source.spi.AdapterStateStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The CVE ids {@code nvd-cves} ingested recently, with the day each was last ingested (prompt 076): the list the
 * daily EPSS refresh walks, kept in the adapter state under {@value #STATE_KEY}.
 *
 * <p>Entries older than {@code epssRefreshDays} are pruned, and at most {@value #MAX_IDS} are kept (the oldest go
 * first). The value is {@code CVE-…|yyyy-MM-dd} lines, sorted; above {@value #COMPRESS_ABOVE_BYTES} bytes it is
 * gzipped and base64-encoded behind {@value #GZIP_PREFIX}.
 */
public final class CveLedger {

    /** The adapter-state key. */
    public static final String STATE_KEY = "epss:ids";

    /** The most ids kept. */
    public static final int MAX_IDS = 20_000;

    /** A value longer than this many bytes is compressed. */
    public static final int COMPRESS_ABOVE_BYTES = 32 * 1024;

    /** The prefix of a compressed value. */
    public static final String GZIP_PREFIX = "gz:";

    private final Map<String, LocalDate> ids = new TreeMap<>();

    private CveLedger() {}

    /** An empty ledger. */
    public static CveLedger empty() {
        return new CveLedger();
    }

    /**
     * The ledger stored in {@code state}; empty when there is none or it cannot be read.
     *
     * @param state the adapter state
     */
    public static CveLedger load(AdapterStateStore state) {
        CveLedger ledger = new CveLedger();
        String value = state.get(STATE_KEY).orElse(null);
        if (value == null || value.isBlank()) {
            return ledger;
        }
        String text = value.startsWith(GZIP_PREFIX) ? gunzip(value.substring(GZIP_PREFIX.length())) : value;
        for (String line : text.split("\n")) {
            int bar = line.indexOf('|');
            if (bar <= 0) {
                continue;
            }
            try {
                ledger.ids.put(line.substring(0, bar), LocalDate.parse(line.substring(bar + 1)));
            } catch (DateTimeParseException e) {
                // A damaged line is dropped; the CVE returns when it is next modified.
            }
        }
        return ledger;
    }

    /**
     * Writes the ledger to {@code state} (removes the key when empty).
     *
     * @param state the adapter state
     */
    public void save(AdapterStateStore state) {
        if (ids.isEmpty()) {
            state.remove(STATE_KEY);
            return;
        }
        StringBuilder sb = new StringBuilder();
        ids.forEach((id, day) -> sb.append(id).append('|').append(day).append('\n'));
        String text = sb.toString();
        state.put(STATE_KEY, text.getBytes(StandardCharsets.UTF_8).length > COMPRESS_ABOVE_BYTES
                ? GZIP_PREFIX + gzip(text) : text);
    }

    /**
     * Records that {@code id} was ingested on {@code day}.
     *
     * @param id the CVE id
     * @param day the ingest day (UTC)
     */
    public void ingested(String id, LocalDate day) {
        ids.merge(id, day, (a, b) -> a.isAfter(b) ? a : b);
    }

    /**
     * Forgets {@code id} (a rejected CVE).
     *
     * @param id the CVE id
     */
    public void remove(String id) {
        ids.remove(id);
    }

    /**
     * Drops entries last ingested before {@code oldest}, then the oldest entries beyond {@value #MAX_IDS}.
     *
     * @param oldest the first day kept
     */
    public void prune(LocalDate oldest) {
        ids.values().removeIf(day -> day.isBefore(oldest));
        if (ids.size() > MAX_IDS) {
            List<Map.Entry<String, LocalDate>> entries = new ArrayList<>(ids.entrySet());
            entries.sort(Map.Entry.<String, LocalDate>comparingByValue().thenComparing(Map.Entry.comparingByKey(
                    Comparator.reverseOrder())));
            for (int i = 0; i < entries.size() - MAX_IDS; i++) {
                ids.remove(entries.get(i).getKey());
            }
        }
    }

    /** True when {@code id} is in the ledger. */
    public boolean contains(String id) {
        return ids.containsKey(id);
    }

    /** The ids, sorted. */
    public List<String> ids() {
        return List.copyOf(ids.keySet());
    }

    /** How many ids the ledger holds. */
    public int size() {
        return ids.size();
    }

    private static String gzip(String text) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
            gz.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    private static String gunzip(String base64) {
        try (GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(base64)))) {
            return new String(gz.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException | IllegalArgumentException e) {
            return "";
        }
    }
}
