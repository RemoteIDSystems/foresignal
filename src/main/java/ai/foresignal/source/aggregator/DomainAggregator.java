package ai.foresignal.source.aggregator;

import ai.foresignal.api.error.FieldError;
import java.util.List;
import java.util.Map;

/**
 * One domain's first-party aggregator (prompt R2-019): the backend {@link AggregatorService} dispatches
 * {@code /v1/aggregators/{domain}} to. Each domain (adsb, ais, datalink, satellite, ground-sensors) has at most one
 * bean; a domain without one is served by {@link PendingDomainAggregator} until its prompt adds it.
 */
public interface DomainAggregator {

    /** The domain, as in the path. */
    String domain();

    /** The display name. */
    String name();

    /** Whether this server has the domain's backend (false only for {@link PendingDomainAggregator}). */
    default boolean implemented() {
        return true;
    }

    /** The first-party sources the aggregator drives, whose health is its status; the first is the main one. */
    List<String> sourceIds();

    /** The registered public/mirror sources it supersedes, sorted; they stay in the catalog. */
    List<String> supersededSourceIds();

    /**
     * Adds every problem of {@code proposed} to {@code errors}, under the {@code AggregatorConfig} field names
     * ({@code endpoint}, {@code config.<key>}, …).
     */
    void validate(AggregatorSettings proposed, List<FieldError> errors);

    /**
     * Configures the domain's sources for {@code settings}: on with its endpoint when live, off otherwise.
     *
     * @throws ai.foresignal.api.error.ValidationException when a source refuses the configuration
     */
    void apply(AggregatorSettings settings);

    /**
     * Probes the endpoint of {@code settings} once and returns the {@code AggregatorTestResult} members {@code ok},
     * {@code reachable}, {@code sampleCount} and {@code message}; nothing is persisted.
     */
    Map<String, Object> test(AggregatorSettings settings);
}
