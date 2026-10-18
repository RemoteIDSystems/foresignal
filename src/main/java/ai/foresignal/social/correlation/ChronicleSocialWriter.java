package ai.foresignal.social.correlation;

import ai.foresignal.chronicle.ChronicleCaptureService;
import ai.foresignal.chronicle.ChronicleEventEntity;
import ai.foresignal.chronicle.ChronicleIndex;
import ai.foresignal.chronicle.ChronicleSelector;
import ai.foresignal.chronicle.ChronicleThreadService;
import ai.foresignal.chronicle.CompiledLayer;
import ai.foresignal.social.narrative.NarrativeStore;
import ai.foresignal.social.narrative.NarrativeViews;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Writes the thread items of {@code social-narratives} chronicle layers (prompt 137): {@code narrative-change} when a
 * narrative's status changes and {@code comparison-change} when a comparison is created or revised. Each item keeps a
 * JSON snapshot (the narrative summary, its claim summaries, the comparison), so the chronicle keeps narrative, claim
 * and comparison history after the posts are deleted or expire. The central post's text is kept only when the layer's
 * {@code keepPostText} says so.
 */
@Component
public class ChronicleSocialWriter {

    private static final Logger log = LoggerFactory.getLogger(ChronicleSocialWriter.class);

    /** Narrative change item type (spec {@code ChronicleThreadItem.type}). */
    public static final String NARRATIVE_CHANGE = "narrative-change";

    /** Comparison change item type. */
    public static final String COMPARISON_CHANGE = "comparison-change";

    private final ObjectProvider<ChronicleCaptureService> capture;
    private final ObjectProvider<ChronicleThreadService> thread;
    private final NarrativeStore narratives;
    private final JdbcTemplate jdbc;

    /** Creates the writer. */
    public ChronicleSocialWriter(ObjectProvider<ChronicleCaptureService> capture,
            ObjectProvider<ChronicleThreadService> thread, NarrativeStore narratives, JdbcTemplate jdbc) {
        this.capture = capture;
        this.thread = thread;
        this.narratives = narratives;
        this.jdbc = jdbc;
    }

    /** The layer of an active chronicle that takes a narrative, or null. */
    static CompiledLayer layerFor(ChronicleIndex.Entry e, NarrativeStore.Narrative n) {
        for (CompiledLayer layer : e.layers()) {
            if (layer.selector() instanceof ChronicleSelector.SocialNarratives sel && sel.takes(n.topicIds(),
                    n.placeRefs(), n.regionCodes(), n.situationIds(), n.status())) {
                return layer;
            }
        }
        return null;
    }

    /** The snapshot of a narrative: summary, claim summaries and, when kept, the central post's text. */
    Map<String, Object> snapshot(NarrativeStore.Narrative n, boolean keepPostText) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("narrative", NarrativeViews.summary(n));
        out.put("claims", narratives.claims(n.id()).stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.id());
            m.put("type", c.type());
            m.put("signalFamily", c.signalFamily());
            m.put("polarity", c.polarity());
            m.put("place", c.place());
            m.put("supportCount", c.supportCount());
            m.put("evidenceGroupCount", c.evidenceGroupCount());
            m.put("firstObservedAt", c.firstObservedAt().toString());
            m.put("status", c.status());
            m.put("comparisonId", c.comparisonId());
            return m;
        }).toList());
        if (keepPostText && n.centralPost() != null) {
            int slash = n.centralPost().indexOf('/');
            if (slash > 0) {
                List<String> text = jdbc.queryForList("SELECT payload ->> 'text' FROM record_head WHERE source_id = ?"
                        + " AND external_id = ?", String.class, n.centralPost().substring(0, slash),
                        n.centralPost().substring(slash + 1));
                out.put("centralPostText", text.isEmpty() ? null : text.get(0));
            }
        }
        return out;
    }

    /**
     * Writes {@code narrative-change} to every active chronicle whose social layer takes the narrative.
     *
     * @return the items written
     */
    public int narrativeChanged(NarrativeStore.Narrative n, String previousStatus, Instant at) {
        return write(n, NARRATIVE_CHANGE, at, "Narrative " + previousStatus + " → " + n.status() + ": " + n.title(),
                "narrative:" + n.id(), null);
    }

    /**
     * Writes {@code comparison-change} to every active chronicle whose social layer takes the comparison's narrative.
     *
     * @return the items written
     */
    public int comparisonChanged(NarrativeStore.Narrative n, ComparisonStore.Comparison c, String previousStatus,
            Instant at) {
        Map<String, Object> comparison = new LinkedHashMap<>();
        comparison.put("id", c.id());
        comparison.put("claimId", c.claimId());
        comparison.put("status", c.status());
        comparison.put("previousStatus", previousStatus);
        comparison.put("revision", c.revision());
        comparison.put("signalFamily", c.signalFamily());
        comparison.put("place", c.place());
        comparison.put("window", Map.of("start", c.windowStart().toString(), "end", c.windowEnd().toString()));
        comparison.put("physicalEvidence", c.physicalEvidence());
        comparison.put("detectability", c.detectability());
        comparison.put("leadTimes", c.leadTimes());
        return write(n, COMPARISON_CHANGE, at, "Claim " + c.claimType() + " " + c.status() + " (" + n.title() + ")",
                "social-comparison:" + c.id(), comparison);
    }

    private int write(NarrativeStore.Narrative n, String type, Instant at, String title, String ref,
            Map<String, Object> extra) {
        ChronicleCaptureService service = capture.getIfAvailable();
        ChronicleThreadService threads = thread.getIfAvailable();
        if (service == null || threads == null) {
            return 0;
        }
        int written = 0;
        for (ChronicleIndex.Entry e : service.index().entries()) {
            CompiledLayer layer = layerFor(e, n);
            if (layer == null) {
                continue;
            }
            try {
                ChronicleSelector.SocialNarratives sel = (ChronicleSelector.SocialNarratives) layer.selector();
                ChronicleEventEntity item = threads.item(e.id(), type, at, title.length() > 500 ? title.substring(0,
                        497) + "..." : title);
                item.setLayerId(layer.layerId());
                item.setRef(ref);
                item.setSummary(n.status() + ", " + n.postCount() + " posts, " + n.evidenceGroupCount()
                        + " independent origins");
                Map<String, Object> snapshot = snapshot(n, sel.keepPostText());
                if (extra != null) {
                    snapshot.put("comparison", extra);
                }
                item.setSnapshot(snapshot);
                item.setPrivateItem(false);
                threads.append(item);
                written++;
            } catch (RuntimeException ex) {
                log.warn("Chronicle {} social item failed: {}", e.id(), ex.toString());
            }
        }
        return written;
    }
}
