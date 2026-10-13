package ai.foresignal.ask;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Everything the tools returned that an answer may cite (prompt 028; shared with briefings 029 and
 * enrichment 030): record ids with every persisted seq returned for them, alert ids and briefing run
 * ids, plus the returned text, which the uncited-claim check searches for names. Prompt 056 adds weather
 * values ({@code wx:{productId}@{frameKey}}, forecasts {@code wx:{productId}@{run}/{step}}) and the non-ok
 * weather statuses the answer guard checks.
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
    private final Map<String, Weather> weather = new LinkedHashMap<>();
    private final Map<String, WeatherIssue> weatherIssues = new LinkedHashMap<>();

    /**
     * A weather value a tool returned (prompt 056).
     *
     * @param citationId {@code wx:{productId}@{frameKey}} or {@code wx:{productId}@{run}/{step}}
     * @param title a short title, or null
     * @param productId the product (or forecast provider)
     * @param frameKey the frame key, or null
     * @param run the forecast run, or null
     * @param step the forecast step in hours, or null
     * @param validTime the valid time, or null
     * @param ageMinutes the age at answer time, or null
     */
    public record Weather(String citationId, String title, String productId, String frameKey, String run, Integer step,
            String validTime, Double ageMinutes) {}

    /**
     * A weather result whose status was not {@code ok} (prompt 056).
     *
     * @param productId the product (or provider, or section)
     * @param status the status ({@code stale}, {@code no-coverage}, {@code no-data}, {@code unavailable})
     * @param statusText its explanation, or null
     */
    public record WeatherIssue(String productId, String status, String statusText) {}

    /** Adds a weather value (the first one returned for an id wins). */
    public synchronized void addWeather(Weather value) {
        weather.putIfAbsent(value.citationId(), value);
    }

    /** True when a tool returned the weather value {@code citationId}. */
    public synchronized boolean hasWeather(String citationId) {
        return weather.containsKey(citationId);
    }

    /** The weather value {@code citationId}, or null. */
    public synchronized Weather weather(String citationId) {
        return weather.get(citationId);
    }

    /** Adds a non-ok weather status (one per product and status). */
    public synchronized void addWeatherIssue(WeatherIssue issue) {
        weatherIssues.putIfAbsent(issue.productId() + "\u0000" + issue.status(), issue);
    }

    /** The non-ok weather statuses returned, in order. */
    public synchronized java.util.List<WeatherIssue> weatherIssues() {
        return java.util.List.copyOf(weatherIssues.values());
    }

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
        return records.isEmpty() && alerts.isEmpty() && briefings.isEmpty() && weather.isEmpty();
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
