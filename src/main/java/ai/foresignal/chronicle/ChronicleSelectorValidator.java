package ai.foresignal.chronicle;

import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.TriggerCondition;
import ai.foresignal.trigger.Operator;
import ai.foresignal.trigger.OperatorRegistry;
import ai.foresignal.view.SavedViewService;
import ai.foresignal.watch.WatchlistService;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Parses a layer selector and checks that what it names exists (prompt 131): registered sources, known 017 operators
 * with values they accept, a saved view and a watchlist that exist, entities, situations and raster products that
 * exist, and well-formed series refs. Any failure is {@code 400 CHRONICLE_LAYER_INVALID} with the field.
 */
@Component
public class ChronicleSelectorValidator {

    private final SourceRegistry sources;
    private final OperatorRegistry operators;
    private final SavedViewService views;
    private final ObjectProvider<WatchlistService> watchlists;
    private final ObjectProvider<ProductRegistry> products;
    private final JdbcTemplate jdbc;

    /** Creates the validator. */
    public ChronicleSelectorValidator(SourceRegistry sources, OperatorRegistry operators, SavedViewService views,
            ObjectProvider<WatchlistService> watchlists, ObjectProvider<ProductRegistry> products, JdbcTemplate jdbc) {
        this.sources = sources;
        this.operators = operators;
        this.views = views;
        this.watchlists = watchlists;
        this.products = products;
        this.jdbc = jdbc;
    }

    /**
     * Parses and checks a selector.
     *
     * @param node the {@code ChronicleLayerSelector} body
     * @param field the field name for errors
     * @return the selector
     * @throws ai.foresignal.api.error.ValidationException {@code 400 CHRONICLE_LAYER_INVALID}
     */
    public ChronicleSelector parse(JsonNode node, String field) {
        ChronicleSelector selector = ChronicleSelector.parse(node, field);
        check(selector, field);
        return selector;
    }

    /** Checks that what {@code selector} names exists. */
    public void check(ChronicleSelector selector, String field) {
        switch (selector) {
            case ChronicleSelector.Records r -> {
                for (String id : r.sources()) {
                    if (!sources.contains(id)) {
                        throw ChronicleSelector.invalid(field + ".sources", "unknown source '" + id + "'");
                    }
                }
                conditions(r.conditions(), field + ".conditions");
            }
            case ChronicleSelector.SavedView v -> {
                if (views.find(v.viewId()).isEmpty()) {
                    throw ChronicleSelector.invalid(field + ".viewId", "unknown saved view '" + v.viewId() + "'");
                }
            }
            case ChronicleSelector.Watchlist w -> {
                WatchlistService service = watchlists.getIfAvailable();
                if (service != null) {
                    try {
                        service.get(w.watchlistId());
                    } catch (ForesignalException e) {
                        throw ChronicleSelector.invalid(field + ".watchlistId",
                                "unknown watchlist '" + w.watchlistId() + "'");
                    }
                }
            }
            case ChronicleSelector.Entities e -> exist("entity", e.entityIds(), field + ".entityIds");
            case ChronicleSelector.Situations s -> exist("situation", s.situationIds(), field + ".situationIds");
            case ChronicleSelector.Findings f -> {
                // Free vocabularies: any finding type or family may appear later.
            }
            case ChronicleSelector.Series s -> {
                for (String ref : s.refs()) {
                    try {
                        SeriesRef.parse(ref);
                    } catch (ForesignalException e) {
                        throw ChronicleSelector.invalid(field + ".refs", "'" + ref + "' is not a series ref ("
                                + e.getMessage() + ")");
                    }
                }
            }
            case ChronicleSelector.RasterProducts r -> {
                ProductRegistry registry = products.getIfAvailable();
                if (registry != null) {
                    for (String id : r.productIds()) {
                        if (registry.find(id).isEmpty()) {
                            throw ChronicleSelector.invalid(field + ".productIds", "unknown raster product '" + id + "'");
                        }
                    }
                }
            }
            case ChronicleSelector.Alerts a -> {
                // Rule ids are checked for shape; a rule may be created after the layer.
            }
        }
    }

    private void conditions(List<TriggerCondition> conditions, String field) {
        int i = 0;
        for (TriggerCondition c : conditions) {
            String prefix = field + "[" + i++ + "]";
            Operator op = operators.find(c.op()).orElseThrow(() -> ChronicleSelector.invalid(prefix + ".op",
                    "unknown operator '" + c.op() + "'"));
            try {
                op.normalize(c.value());
            } catch (RuntimeException e) {
                throw ChronicleSelector.invalid(prefix + ".value", e.getMessage() == null ? "is not valid"
                        : e.getMessage());
            }
            if (c.path() == null && !(op instanceof ai.foresignal.trigger.IdentityOperator)) {
                throw ChronicleSelector.invalid(prefix + ".path", "is required for operator '" + c.op() + "'");
            }
        }
    }

    private void exist(String table, List<String> ids, String field) {
        if (ids.isEmpty()) {
            return;
        }
        List<String> found = jdbc.queryForList("SELECT id FROM " + table + " WHERE id = ANY(?)", String.class,
                (Object) ids.toArray(String[]::new));
        for (String id : ids) {
            if (!found.contains(id)) {
                throw ChronicleSelector.invalid(field, "unknown " + table + " '" + id + "'");
            }
        }
    }
}
