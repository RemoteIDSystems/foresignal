package ai.foresignal.ask;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Everything the tools returned that an answer may cite (prompt 028; shared with briefings 029 and
 * enrichment 030): record ids with every persisted seq returned for them, alert ids and briefing run
 * ids, plus the returned text, which the uncited-claim check searches for names.
 *
 * <p>Filled while tools run, possibly in parallel, so every method is synchronized.
 */
public final class CitationSet {

    /** The most tool-output characters kept for the name check. */
    public static final int MAX_CORPUS_CHARS = 2_000_000;

    private final Map<String, TreeMap<Long, String>> records = new LinkedHashMap<>();
    private final Map<String, String> alerts = new HashMap<>();
    private final Map<String, String> briefings = new HashMap<>();
    private final StringBuilder corpus = new StringBuilder();

    /**
     * Adds a record version.
     *
     * @param id {@code source:externalId}
     * @param seq its persisted version seq
     * @param title its title, or null
     */
    public synchronized void addRecord(String id, long seq, String title) {
        TreeMap<Long, String> seqs = records.computeIfAbsent(id, k -> new TreeMap<>());
        String known = seqs.get(seq);
        seqs.put(seq, known != null ? known : title);
    }

    /** Adds an alert ({@code id} without the {@code alert:} prefix). */
    public synchronized void addAlert(String id, String title) {
        alerts.merge(id, title == null ? "" : title, (a, b) -> a.isEmpty() ? b : a);
    }

    /** Adds a briefing run ({@code id} without the {@code briefing:} prefix). */
    public synchronized void addBriefing(String id, String title) {
        briefings.merge(id, title == null ? "" : title, (a, b) -> a.isEmpty() ? b : a);
    }

    /** Adds returned text for the name check (kept up to {@value #MAX_CORPUS_CHARS} characters). */
    public synchronized void addText(String text) {
        if (text == null || corpus.length() >= MAX_CORPUS_CHARS) {
            return;
        }
        corpus.append(text, 0, Math.min(text.length(), MAX_CORPUS_CHARS - corpus.length())).append('\n');
    }

    /** True when nothing citable was returned. */
    public synchronized boolean isEmpty() {
        return records.isEmpty() && alerts.isEmpty() && briefings.isEmpty();
    }

    /** The highest seq returned for record {@code id}. */
    public synchronized Optional<Long> latestSeq(String id) {
        TreeMap<Long, String> seqs = records.get(id);
        return seqs == null || seqs.isEmpty() ? Optional.empty() : Optional.of(seqs.lastKey());
    }

    /** True when a tool returned record {@code id} at {@code seq}. */
    public synchronized boolean hasRecord(String id, long seq) {
        TreeMap<Long, String> seqs = records.get(id);
        return seqs != null && seqs.containsKey(seq);
    }

    /** The title returned with record {@code id} at {@code seq}, or null. */
    public synchronized String recordTitle(String id, long seq) {
        TreeMap<Long, String> seqs = records.get(id);
        return seqs == null ? null : seqs.get(seq);
    }

    /** True when a tool returned alert {@code id}. */
    public synchronized boolean hasAlert(String id) {
        return alerts.containsKey(id);
    }

    /** The title returned with alert {@code id}, or null. */
    public synchronized String alertTitle(String id) {
        String title = alerts.get(id);
        return title == null || title.isEmpty() ? null : title;
    }

    /** True when a tool returned briefing run {@code id}. */
    public synchronized boolean hasBriefing(String id) {
        return briefings.containsKey(id);
    }

    /** The title returned with briefing run {@code id}, or null. */
    public synchronized String briefingTitle(String id) {
        String title = briefings.get(id);
        return title == null || title.isEmpty() ? null : title;
    }

    /** The returned text. */
    public synchronized String corpus() {
        return corpus.toString();
    }
}
