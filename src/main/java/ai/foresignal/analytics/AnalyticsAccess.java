package ai.foresignal.analytics;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.SeriesEgress;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.stats.RegionStatsService;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * What a caller may see of the discovery layer (prompt 119), shared by the REST controller and the MCP tools.
 *
 * <ul>
 *   <li>A series resting on a source the caller may not see (a private source without the grant, a source outside
 *       its restriction) is {@code 404 SERIES_NOT_FOUND}, as is any series for a spatially restricted caller (series
 *       carry no geometry, so they cannot be clipped: fail closed, as 093's series reads).
 *   <li>Raw values are withheld when {@code EgressPolicy.decide} refuses a source of the series on the read's path
 *       (a {@code LOCAL_ONLY} source for a caller not allowed local egress); standardized statistics remain (EGR-2).
 * </ul>
 */
@Component
public class AnalyticsAccess {

    private final SeriesEgress egress;

    /** Creates the component. */
    public AnalyticsAccess(SeriesEgress egress) {
        this.egress = egress;
    }

    /**
     * Checks that the caller may read the series and says whether its raw values are withheld.
     *
     * @param hidden the sources the caller may not see at all
     * @return true when raw values must be withheld
     * @throws NotFoundException 404 {@code SERIES_NOT_FOUND} when the caller may not read it
     */
    public boolean checkSeries(SeriesRef ref, SeriesReader.Meta meta, ReadCaller caller, Set<String> hidden,
            ReadRestriction restriction, EgressPath path) {
        if (RegionStatsService.spatiallyRestricted(restriction)) {
            throw notFound(ref);
        }
        for (String source : meta.sources()) {
            if (hidden.contains(source) || (restriction != null && !restriction.allowsSource(source))) {
                throw notFound(ref);
            }
        }
        Set<String> refused = egress.excluded(caller, Set.of(), path);
        for (String source : meta.sources()) {
            if (refused.contains(source)) {
                return true;
            }
        }
        return false;
    }

    /** True when the raw values in a finding's statistics must be withheld from the caller on {@code path}. */
    public boolean withheld(AnalyticFindingEntity finding, ReadCaller caller, EgressPath path) {
        if (finding.getLocalOnlySources().isEmpty() || finding.getRawStatKeys().isEmpty()) {
            return false;
        }
        Set<String> refused = egress.excluded(caller, Set.of(), path);
        for (String source : finding.getLocalOnlySources()) {
            if (refused.contains(source)) {
                return true;
            }
        }
        return false;
    }

    private static NotFoundException notFound(SeriesRef ref) {
        return new NotFoundException(ErrorCodes.SERIES_NOT_FOUND, "No series '" + ref.format() + "'.");
    }
}
