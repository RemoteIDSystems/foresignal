package ai.foresignal.source.adapters.wikistream;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A pure, in-memory edit-surge detector for Wikipedia (prompt 095).
 *
 * <p>Per {@code (wiki, title)} it keeps a sliding window of edit timestamps and an <strong>exact set of hashed
 * editor names</strong> (SHA-256 truncated, held in memory only and never stored). A page is in surge when the
 * window holds at least {@code minEdits} edits from at least {@code minEditors} distinct editors, or, for a
 * newly created page, at least {@code newPageMinEdits} edits from {@code newPageMinEditors} editors within the
 * longer new-page window. A surge that falls below threshold is kept for {@code linger} (60 min) as
 * {@code fading}, then tombstoned.
 *
 * <p><strong>Banding.</strong> So that a steady surge becomes touches rather than a version per edit, an
 * emission happens only when the banded state changes: edits in bands of 5, distinct editors in bands of 2, and
 * the phase ({@code rising}, {@code peak}, {@code fading}).
 *
 * <p><strong>Bounded.</strong> At most {@code maxPages} pages are tracked; the least recently updated is
 * evicted and the eviction counted.
 *
 * <p>Deterministic for identical inputs and a fixed clock (INT-1); no external state.
 */
public final class SurgeDetector {

    /** The phase of a surge. */
    public static final String RISING = "rising";
    /** The phase of a surge. */
    public static final String PEAK = "peak";
    /** The phase of a surge. */
    public static final String FADING = "fading";

    /** The edits-band width. */
    public static final int EDIT_BAND = 5;

    /** The editors-band width. */
    public static final int EDITOR_BAND = 2;

    /**
     * The detector's thresholds.
     *
     * @param minEdits edits within {@code window}
     * @param minEditors distinct editors within {@code window}
     * @param newPageMinEdits edits within {@code newPageWindow} for a new page
     * @param newPageMinEditors distinct editors within {@code newPageWindow} for a new page
     * @param window the sliding window
     * @param newPageWindow the longer new-page window
     * @param linger how long a sub-threshold surge is kept before it is tombstoned
     * @param maxPages the most pages tracked before the oldest is evicted
     */
    public record Config(int minEdits, int minEditors, int newPageMinEdits, int newPageMinEditors, Duration window,
            Duration newPageWindow, Duration linger, int maxPages) {

        /** The prompt's defaults. */
        public static Config defaults() {
            return new Config(15, 5, 10, 5, Duration.ofMinutes(15), Duration.ofMinutes(30), Duration.ofMinutes(60),
                    200_000);
        }
    }

    /**
     * One emitted surge state.
     *
     * @param wiki the wiki
     * @param title the page title
     * @param url the canonical page URL
     * @param edits edits within the window
     * @param editors distinct editors within the window
     * @param firstEditAt the earliest retained edit time
     * @param isNewPage true when the page was created
     * @param phase rising, peak or fading
     * @param observedAt the newest event time
     * @param tombstone true when the surge has ended (the record must be tombstoned)
     */
    public record Surge(String wiki, String title, String url, int edits, int editors, Instant firstEditAt,
            boolean isNewPage, String phase, Instant observedAt, boolean tombstone) {}

    private record Edit(Instant at, String editorHash) {}

    private final Config config;
    private final LinkedHashMap<String, PageState> pages = new LinkedHashMap<>(16, 0.75f, true);
    private long evictions;

    /** Creates the detector. */
    public SurgeDetector(Config config) {
        this.config = config;
    }

    /** Creates the detector with the prompt's defaults. */
    public SurgeDetector() {
        this(Config.defaults());
    }

    /** The number of evictions (least-recently-updated pages dropped at the cap). */
    public long evictions() {
        return evictions;
    }

    /** The number of tracked pages. */
    public int size() {
        return pages.size();
    }

    /**
     * Records one edit and returns the surge state to emit, if the banded state changed.
     *
     * @param wiki the wiki
     * @param title the page title
     * @param url the canonical page URL
     * @param newPage true when the edit created a page
     * @param editor the editor name (hashed here, never stored)
     * @param at the event time
     * @return the surge to emit, or empty
     */
    public Optional<Surge> onEdit(String wiki, String title, String url, boolean newPage, String editor, Instant at) {
        if (wiki == null || title == null || at == null) {
            return Optional.empty();
        }
        String key = wiki + "\u0000" + title;
        PageState state = pages.get(key);
        if (state == null) {
            state = new PageState(wiki, title);
            pages.put(key, state);
            evictIfNeeded();
        }
        state.url = url == null ? state.url : url;
        state.isNewPage |= newPage;
        state.lastEditAt = at;
        if (state.firstEditAt == null) {
            state.firstEditAt = at;
        }
        state.edits.addLast(new Edit(at, hashEditor(editor)));
        prune(state, at);
        return evaluate(state, at);
    }

    /**
     * Advances the clock: tombstones surges that ended and drops pages that never surged and went quiet.
     *
     * @param now the current time
     * @return the tombstones to emit, in a stable order
     */
    public List<Surge> sweep(Instant now) {
        List<Surge> out = new ArrayList<>();
        Iterator<Map.Entry<String, PageState>> iterator = pages.entrySet().iterator();
        while (iterator.hasNext()) {
            PageState state = iterator.next().getValue();
            prune(state, now);
            if (state.active) {
                continue;
            }
            if (state.everActive) {
                if (Duration.between(state.lastActiveAt, now).compareTo(config.linger()) > 0) {
                    out.add(tombstone(state, now));
                    iterator.remove();
                }
            } else if (state.lastEditAt != null
                    && Duration.between(state.lastEditAt, now).compareTo(config.window()) > 0) {
                iterator.remove();
            }
        }
        return out;
    }

    /** Drops edits older than the longest window. */
    private void prune(PageState state, Instant now) {
        Duration longest = config.newPageWindow().compareTo(config.window()) >= 0
                ? config.newPageWindow() : config.window();
        while (!state.edits.isEmpty()
                && Duration.between(state.edits.peekFirst().at(), now).compareTo(longest) > 0) {
            state.edits.pollFirst();
        }
    }

    /** Computes the counts, phase and emission decision for one page. */
    private Optional<Surge> evaluate(PageState state, Instant at) {
        int edits15 = 0;
        int edits30 = 0;
        Set<String> editors15 = new HashSet<>();
        Set<String> editors30 = new HashSet<>();
        for (Edit edit : state.edits) {
            Duration age = Duration.between(edit.at(), at);
            if (age.compareTo(config.newPageWindow()) <= 0) {
                edits30++;
                editors30.add(edit.editorHash());
            }
            if (age.compareTo(config.window()) <= 0) {
                edits15++;
                editors15.add(edit.editorHash());
            }
        }
        int editors = editors15.size();
        boolean regular = edits15 >= config.minEdits() && editors >= config.minEditors();
        boolean newPageMet = state.isNewPage && edits30 >= config.newPageMinEdits() && editors30.size() >= config.newPageMinEditors();
        boolean met = regular || newPageMet;

        String phase;
        if (met) {
            state.active = true;
            boolean wasActive = state.everActive;
            state.everActive = true;
            state.lastActiveAt = at;
            phase = wasActive ? PEAK : RISING;
        } else if (state.everActive) {
            state.active = false;
            phase = FADING;
        } else {
            return Optional.empty();
        }
        state.firstEditAt = state.edits.isEmpty() ? state.firstEditAt : state.edits.peekFirst().at();

        int editsBand = edits15 / EDIT_BAND;
        int editorsBand = editors / EDITOR_BAND;
        String bandKey = editsBand + ":" + editorsBand + ":" + phase + ":" + state.isNewPage;
        if (bandKey.equals(state.lastBandKey)) {
            return Optional.empty();
        }
        state.lastBandKey = bandKey;
        return Optional.of(new Surge(state.wiki, state.title, state.url, edits15, editors, state.firstEditAt,
                state.isNewPage, phase, at, false));
    }

    private Surge tombstone(PageState state, Instant now) {
        return new Surge(state.wiki, state.title, state.url, 0, 0, state.firstEditAt, state.isNewPage, FADING,
                now, true);
    }

    private void evictIfNeeded() {
        while (pages.size() > config.maxPages()) {
            Iterator<Map.Entry<String, PageState>> iterator = pages.entrySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }
            iterator.next();
            iterator.remove();
            evictions++;
        }
    }

    /** SHA-256 of {@code editor}, truncated to 8 bytes of lowercase hex; deterministic and one-way. */
    public static String hashEditor(String editor) {
        String value = editor == null ? "" : editor;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] full = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            byte[] truncated = new byte[8];
            System.arraycopy(full, 0, truncated, 0, 8);
            return HexFormat.of().formatHex(truncated);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required of every JVM", e);
        }
    }

    /** Per-page mutable state. */
    private static final class PageState {

        private final String wiki;
        private final String title;
        private final Deque<Edit> edits = new ArrayDeque<>();
        private String url;
        private Instant firstEditAt;
        private Instant lastEditAt;
        private Instant lastActiveAt;
        private boolean isNewPage;
        private boolean active;
        private boolean everActive;
        private String lastBandKey;

        PageState(String wiki, String title) {
            this.wiki = wiki;
            this.title = title;
        }
    }
}
