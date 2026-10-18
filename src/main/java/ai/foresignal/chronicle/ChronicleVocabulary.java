package ai.foresignal.chronicle;

import ai.foresignal.ingest.PersistReason;
import java.util.List;
import java.util.Set;

/**
 * The words of chronicles (prompt 131): statuses, origins, cap states, thread item types and the persist reasons
 * {@code events-only} keeps.
 */
public final class ChronicleVocabulary {

    /** Created, not capturing; the analysis cycles' proposals start here. */
    public static final String PROPOSED = "proposed";

    /** Capturing. */
    public static final String ACTIVE = "active";

    /** Capture paused; history kept. */
    public static final String PAUSED = "paused";

    /** Capture ended; history kept and replayable. */
    public static final String STOPPED = "stopped";

    /** Storage moved to a cold-archive package; restorable. */
    public static final String ARCHIVED = "archived";

    /** Every status, in lifecycle order. */
    public static final List<String> STATUSES = List.of(PROPOSED, ACTIVE, PAUSED, STOPPED, ARCHIVED);

    /** Created by a person through the API. */
    public static final String ORIGIN_OPERATOR = "operator";

    /** Created by an agent (MCP or an agent token). */
    public static final String ORIGIN_AGENT = "agent";

    /** Proposed by the analysis cycles (128) or the 102 pipeline. */
    public static final String ORIGIN_ANALYST = "analyst";

    /** Every origin. */
    public static final List<String> ORIGINS = List.of(ORIGIN_OPERATOR, ORIGIN_AGENT, ORIGIN_ANALYST);

    /** Below 90% of the cap. */
    public static final String CAP_OK = "ok";

    /** At 90% of the cap or more: the owner was notified. */
    public static final String CAP_WARNING = "warning";

    /** At the cap: full and downsampled layers capture events only. */
    public static final String CAP_EXCEEDED = "exceeded";

    /** The share of the cap at which the owner is warned. */
    public static final double CAP_WARNING_SHARE = 0.9;

    /** Thread item: an alert of an alerts layer fired. */
    public static final String ITEM_ALERT = "alert";

    /** Thread item: a finding of a findings layer appeared or changed significantly. */
    public static final String ITEM_FINDING = "finding";

    /** Thread item: a linked situation changed (activated, escalated, dormant, closed, merged). */
    public static final String ITEM_SITUATION_CHANGE = "situation-change";

    /** Thread item: a linked hypothesis set changed band. */
    public static final String ITEM_HYPOTHESIS_BAND_CHANGE = "hypothesis-band-change";

    /** Thread item: a ledger question about a linked situation or set resolved. */
    public static final String ITEM_FORECAST_RESOLUTION = "forecast-resolution";

    /** Thread item: an entity appeared for the first time in a layer. */
    public static final String ITEM_FIRST_APPEARANCE = "first-appearance";

    /** Thread item: a layer was added, changed or removed. */
    public static final String ITEM_LAYER_EDIT = "layer-edit";

    /** Thread item: capture fell behind global retention for an interval. */
    public static final String ITEM_CAPTURE_GAP = "capture-gap";

    /** Thread item: storage reached 90% of the cap. */
    public static final String ITEM_CAP_WARNING = "cap-warning";

    /** Thread item: storage reached the cap; full and downsampled layers capture events only. */
    public static final String ITEM_CAP_FALLBACK = "cap-fallback";

    /** Thread item: the lifecycle status changed (also SSE {@code chronicle.status}). */
    public static final String ITEM_STATUS_CHANGE = "status-change";

    /** Thread item: a backfill finished. */
    public static final String ITEM_BACKFILL = "backfill";

    /** Every thread item type of the spec ({@code narrative-change} and {@code comparison-change} arrive with 137). */
    public static final List<String> ITEM_TYPES = List.of(ITEM_ALERT, ITEM_FINDING, ITEM_SITUATION_CHANGE,
            ITEM_HYPOTHESIS_BAND_CHANGE, ITEM_FORECAST_RESOLUTION, ITEM_FIRST_APPEARANCE, ITEM_LAYER_EDIT,
            ITEM_CAPTURE_GAP, ITEM_CAP_WARNING, ITEM_CAP_FALLBACK, ITEM_STATUS_CHANGE, ITEM_BACKFILL,
            "narrative-change", "comparison-change");

    /** The persist reasons {@code events-only} keeps. */
    public static final Set<PersistReason> EVENT_REASONS = Set.of(PersistReason.FIRST, PersistReason.SIGNIFICANT,
            PersistReason.REAPPEAR, PersistReason.TOMBSTONE, PersistReason.TRIGGER_EDGE);

    /** The {@code foresignal-intel} source (situations, intelligence events, hypothesis sets). */
    public static final String INTEL_SOURCE = "foresignal-intel";

    /** The {@code foresignal-analytics} source (findings). */
    public static final String ANALYTICS_SOURCE = "foresignal-analytics";

    /** The finding record kind (119). */
    public static final String FINDING_KIND = "analytic-finding";

    /** The situation record kind (118). */
    public static final String SITUATION_KIND = "situation";

    /** The intelligence event record kind (101). */
    public static final String INTEL_EVENT_KIND = "intelligence-event";

    /** The series point record kind (093). */
    public static final String SERIES_POINT_KIND = "series-point";

    /** The raster frame record kind (042). */
    public static final String RASTER_FRAME_KIND = "raster-frame";

    private ChronicleVocabulary() {}

    /** True for a known status. */
    public static boolean isStatus(String status) {
        return status != null && STATUSES.contains(status);
    }

    /** True for a status in which the chronicle holds storage that can be replayed. */
    public static boolean hasStorage(String status) {
        return ACTIVE.equals(status) || PAUSED.equals(status) || STOPPED.equals(status);
    }

    /** The {@code chronicle.cursor.{id}} consumer name of a chronicle. */
    public static String cursorName(String chronicleId) {
        return "chronicle.cursor." + chronicleId;
    }
}
