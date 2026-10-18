package ai.foresignal.chronicle;

import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.place.PlaceService;
import ai.foresignal.store.TriggerCondition;
import ai.foresignal.trigger.IdentityOperator;
import ai.foresignal.trigger.Operator;
import ai.foresignal.trigger.OperatorRegistry;
import ai.foresignal.trigger.RecordPredicate;
import ai.foresignal.trigger.RuleCondition;
import ai.foresignal.view.SavedViewEntity;
import ai.foresignal.view.SavedViewService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Compiles chronicle layers into {@link CompiledLayer}s (prompt 131). Per selector:
 *
 * <ul>
 *   <li>{@code records}: sources and kinds, and the 017 conditions in their Java meaning
 *       ({@link RecordPredicate#testConditions}, the evaluator saved views and the persist gate share);</li>
 *   <li>{@code saved-view}: the 036 view compiled by {@link SavedViewService} (its sources, kinds, conditions and
 *       area);</li>
 *   <li>{@code watchlist}: the {@code inWatchlist} identity condition of the 036 watchlist;</li>
 *   <li>{@code entities}: the records the 098 entities link to now (open entity links);</li>
 *   <li>{@code situations}: each situation's own {@code foresignal-intel} record and its members' records (record and
 *       anomaly members, intelligence events, findings), membership-driven: the index refreshes the members;</li>
 *   <li>{@code findings}: {@code foresignal-analytics} findings by {@code findingType} and {@code signalFamily};</li>
 *   <li>{@code series}: the {@code series-point} records of {@code series:} refs ({@code rollup:} and {@code rule:}
 *       refs are sampled instead, see {@link ChronicleSeriesSampler});</li>
 *   <li>{@code raster-products}: the {@code raster-frame} records of the products;</li>
 *   <li>{@code alerts}: not record-backed; the alert listener of {@link ChronicleCaptureService} feeds them.</li>
 * </ul>
 *
 * <p>The area (the layer's override, else the chronicle's) applies to records, saved views, watchlists, findings and
 * raster products; entities, situations and series follow their membership and ignore the chronicle area unless the
 * layer overrides it.
 */
@Component
public class ChronicleLayerCompiler {

    private static final Logger log = LoggerFactory.getLogger(ChronicleLayerCompiler.class);

    private final OperatorRegistry operators;
    private final SavedViewService views;
    private final PlaceService places;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the compiler. */
    public ChronicleLayerCompiler(OperatorRegistry operators, SavedViewService views, PlaceService places,
            JdbcTemplate jdbc, JsonMapper json) {
        this.operators = operators;
        this.views = views;
        this.places = places;
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * Compiles one layer.
     *
     * @param chronicle the chronicle (its area applies unless the layer overrides it)
     * @param layer the layer
     * @param backfill true to ignore the layer's {@code added_at_seq} (a backfill copies history before activation)
     * @return the compiled layer, or empty when the layer cannot be compiled (logged; it then captures nothing)
     */
    public Optional<CompiledLayer> compile(ChronicleEntity chronicle, ChronicleLayerEntity layer, boolean backfill) {
        try {
            ChronicleSelector selector = ChronicleSelector.fromMap(layer.getSelector(), json);
            CaptureMode capture = CaptureMode.fromMap(layer.getCapture());
            ChronicleArea override = ChronicleArea.fromMap(layer.getAreaOverride(), json);
            ChronicleArea chronicleArea = ChronicleArea.fromMap(chronicle.getArea(), json);
            Long fromSeq = backfill ? null : layer.getAddedAtSeq();
            return Optional.of(compile(chronicle.getId(), layer.getId(), layer.getName(), selector, capture, fromSeq,
                    override, chronicleArea));
        } catch (RuntimeException e) {
            log.warn("Chronicle {} layer {} cannot be compiled; it captures nothing: {}", chronicle.getId(),
                    layer.getId(), e.toString());
            return Optional.empty();
        }
    }

    /**
     * Compiles a selector with its areas.
     *
     * @param chronicleId the chronicle
     * @param layerId the layer
     * @param name the layer name
     * @param selector the selector
     * @param capture the capture mode
     * @param fromSeq the seq from which the definition applies, or null
     * @param override the layer's area override, or null
     * @param chronicleArea the chronicle's area, or null
     * @return the compiled layer
     */
    public CompiledLayer compile(String chronicleId, String layerId, String name, ChronicleSelector selector,
            CaptureMode capture, Long fromSeq, ChronicleArea override, ChronicleArea chronicleArea) {
        boolean membership = selector instanceof ChronicleSelector.Entities
                || selector instanceof ChronicleSelector.Situations || selector instanceof ChronicleSelector.Series;
        ChronicleArea effective = override != null ? override : membership ? null : chronicleArea;
        ChronicleArea.Resolved area = effective == null || effective.isEmpty() ? ChronicleArea.Resolved.ANYWHERE
                : effective.resolve(id -> places.area(id).orElse(null));
        Predicate<CapturedRow> inArea = row -> area.contains(row.geometry(), row.region());
        return switch (selector) {
            case ChronicleSelector.Records r -> {
                RecordPredicate predicate = new RecordPredicate(new LinkedHashSet<>(r.sources()),
                        new LinkedHashSet<>(r.kinds()), conditions(r.conditions(), r.sources()), null);
                yield new CompiledLayer(chronicleId, layerId, name, selector, capture, fromSeq, Set.copyOf(r.sources()),
                        Set.copyOf(r.kinds()), inArea.and(row -> predicate.testConditions(row.sourceId(),
                                row.payloadMap(json))), true);
            }
            case ChronicleSelector.SavedView v -> {
                SavedViewEntity view = views.find(v.viewId()).orElseThrow(() -> new IllegalStateException(
                        "saved view " + v.viewId() + " no longer exists"));
                RecordPredicate predicate = views.compile(view).predicate();
                yield new CompiledLayer(chronicleId, layerId, name, selector, capture, fromSeq, predicate.sources(),
                        predicate.kinds(), inArea.and(row -> predicate.test(row.sourceId(), row.kind(), row.geometry(),
                                row.payloadMap(json))), true);
            }
            case ChronicleSelector.Watchlist w -> {
                Operator op = operators.find("inWatchlist").orElseThrow(() -> new IllegalStateException(
                        "the inWatchlist operator is not registered"));
                Object value = op.normalize(w.watchlistId());
                RuleCondition condition = op instanceof IdentityOperator io
                        ? new RuleCondition(null, op, value, io.identityPaths(value, List.of()))
                        : new RuleCondition(null, op, value);
                RecordPredicate predicate = RecordPredicate.of(List.of(condition));
                yield new CompiledLayer(chronicleId, layerId, name, selector, capture, fromSeq, Set.of(), Set.of(),
                        inArea.and(row -> predicate.testConditions(row.sourceId(), row.payloadMap(json))), true);
            }
            case ChronicleSelector.Entities e -> {
                Set<String> keys = entityKeys(e.entityIds());
                Set<String> sources = sourcesOf(keys);
                yield new CompiledLayer(chronicleId, layerId, name, selector, capture, fromSeq, sources, Set.of(),
                        inArea.and(row -> keys.contains(row.entityKey())), true);
            }
            case ChronicleSelector.Situations s -> {
                Set<String> keys = situationKeys(s.situationIds());
                Set<String> sources = sourcesOf(keys);
                yield new CompiledLayer(chronicleId, layerId, name, selector, capture, fromSeq, sources, Set.of(),
                        inArea.and(row -> keys.contains(row.entityKey())), true);
            }
            case ChronicleSelector.Findings f -> {
                Set<String> types = Set.copyOf(f.findingTypes());
                Set<String> families = Set.copyOf(f.families());
                yield new CompiledLayer(chronicleId, layerId, name, selector, capture, fromSeq,
                        Set.of(ChronicleVocabulary.ANALYTICS_SOURCE), Set.of(ChronicleVocabulary.FINDING_KIND),
                        inArea.and(row -> {
                            Map<String, Object> p = row.payloadMap(json);
                            return (types.isEmpty() || types.contains(String.valueOf(p.get("findingType"))))
                                    && (families.isEmpty() || families.contains(String.valueOf(p.get("signalFamily"))));
                        }), true);
            }
            case ChronicleSelector.Series s -> {
                List<SeriesRef> records = new ArrayList<>();
                for (String text : s.refs()) {
                    SeriesRef ref = SeriesRef.parse(text);
                    if (ref.type() == SeriesRef.Type.SERIES) {
                        records.add(ref);
                    }
                }
                Set<String> sources = new HashSet<>();
                records.forEach(r -> sources.add(r.sourceId()));
                yield new CompiledLayer(chronicleId, layerId, name, selector, capture, fromSeq, sources,
                        Set.of(ChronicleVocabulary.SERIES_POINT_KIND), inArea.and(row -> seriesOf(row, records) != null),
                        !records.isEmpty());
            }
            case ChronicleSelector.RasterProducts r -> {
                Set<String> products = Set.copyOf(r.productIds());
                yield new CompiledLayer(chronicleId, layerId, name, selector, capture, fromSeq, Set.of(),
                        Set.of(ChronicleVocabulary.RASTER_FRAME_KIND),
                        inArea.and(row -> products.contains(row.externalId())), true);
            }
            case ChronicleSelector.Alerts a -> new CompiledLayer(chronicleId, layerId, name, selector, capture, fromSeq,
                    Set.of(), Set.of(), row -> false, false);
        };
    }

    /**
     * The {@code series:} ref a {@code series-point} row belongs to, or null: the row's source is the ref's source,
     * its external id starts with the series id and a colon, and its payload names the series.
     */
    SeriesRef seriesOf(CapturedRow row, List<SeriesRef> refs) {
        for (SeriesRef ref : refs) {
            if (ref.sourceId().equals(row.sourceId()) && row.externalId().startsWith(ref.seriesId() + ":")
                    && ref.seriesId().equals(String.valueOf(row.payloadMap(json).get("series")))) {
                return ref;
            }
        }
        return null;
    }

    /** The 017 conditions as rule conditions, normalized by their operators. */
    List<RuleCondition> conditions(List<TriggerCondition> raw, List<String> sources) {
        List<RuleCondition> out = new ArrayList<>();
        for (TriggerCondition c : raw) {
            Operator op = operators.find(c.op()).orElseThrow(() -> new IllegalStateException(
                    "unknown operator '" + c.op() + "'"));
            Object value = op.normalize(c.value());
            if (c.path() == null && op instanceof IdentityOperator io) {
                out.add(new RuleCondition(null, op, value, io.identityPaths(value, sources)));
            } else {
                out.add(new RuleCondition(c.path(), op, value));
            }
        }
        return out;
    }

    /** The {@code source\u0000external} keys the entities link to now. */
    Set<String> entityKeys(List<String> entityIds) {
        Set<String> out = new HashSet<>();
        if (entityIds.isEmpty()) {
            return out;
        }
        jdbc.query("SELECT source_id, external_id FROM entity_link WHERE entity_id = ANY(?) AND valid_to IS NULL",
                rs -> {
                    out.add(rs.getString(1) + '\u0000' + rs.getString(2));
                }, (Object) entityIds.toArray(String[]::new));
        return out;
    }

    /** The record keys of the situations and their members. */
    Set<String> situationKeys(List<String> situationIds) {
        Set<String> out = new HashSet<>();
        if (situationIds.isEmpty()) {
            return out;
        }
        for (String id : situationIds) {
            out.add(ChronicleVocabulary.INTEL_SOURCE + '\u0000' + id);
        }
        jdbc.query("SELECT member_type, ref, source_id FROM situation_member WHERE situation_id = ANY(?)", rs -> {
            String type = rs.getString(1);
            String ref = rs.getString(2);
            if (ref == null) {
                return;
            }
            switch (type) {
                case "record", "anomaly" -> {
                    int slash = ref.indexOf('/');
                    if (slash > 0) {
                        out.add(ref.substring(0, slash) + '\u0000' + ref.substring(slash + 1));
                    }
                }
                case "intel_event" -> out.add(ChronicleVocabulary.INTEL_SOURCE + '\u0000' + ref);
                case "finding" -> out.add(ChronicleVocabulary.ANALYTICS_SOURCE + '\u0000' + ref);
                default -> {
                    // Alerts and investigations are not records.
                }
            }
        }, (Object) situationIds.toArray(String[]::new));
        return out;
    }

    private static Set<String> sourcesOf(Set<String> keys) {
        Set<String> out = new HashSet<>();
        for (String key : keys) {
            int nul = key.indexOf('\u0000');
            if (nul > 0) {
                out.add(key.substring(0, nul));
            }
        }
        if (out.isEmpty()) {
            // Nothing linked yet: a source no record has, so the layer matches nothing until the index refreshes.
            out.add("\u0000none");
        }
        return out;
    }
}
