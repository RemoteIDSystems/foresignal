package ai.foresignal.observability;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The presence proxy for track kinds (prompt 116), pure: per H3 res-4 cell, the share of the source's active
 * hours in which the cell had at least one observation, divided by the 90th-percentile share across cells
 * with traffic, capped at 1. Always low confidence: traffic that is absent cannot be told from traffic that
 * was missed.
 */
public final class PresenceProxy {

    /** The estimator method name. */
    public static final String METHOD = "presence-proxy";

    /** The measured-range method of local receivers. */
    public static final String RECEIVER_RANGE = "receiver-range";

    /** The share of active hours that puts a cell inside a local receiver's measured range. */
    public static final double RANGE_SHARE = 0.1;

    private PresenceProxy() {}

    /**
     * One cell's estimate.
     *
     * @param p the normalized share, 0–1
     * @param hours the hours the cell had observations
     * @param share the raw share of active hours
     */
    public record CellEstimate(double p, long hours, double share) {}

    /**
     * The estimates of every cell with traffic.
     *
     * @param hoursByCell the hours with at least one observation, per cell
     * @param activeHours the hours the source was active at all
     */
    public static Map<String, CellEstimate> estimate(Map<String, Long> hoursByCell, long activeHours) {
        Map<String, CellEstimate> out = new LinkedHashMap<>();
        if (activeHours <= 0 || hoursByCell.isEmpty()) {
            return out;
        }
        List<Double> shares = new ArrayList<>();
        for (long hours : hoursByCell.values()) {
            if (hours > 0) {
                shares.add(Math.min(1.0, (double) hours / activeHours));
            }
        }
        double p90 = percentile(shares, 0.9);
        hoursByCell.forEach((cell, hours) -> {
            if (hours > 0) {
                double share = Math.min(1.0, (double) hours / activeHours);
                double p = p90 <= 0 ? 0 : Math.min(1.0, share / p90);
                out.put(cell, new CellEstimate(p, hours, share));
            }
        });
        return out;
    }

    /**
     * The measured range of a local receiver: the cells observed in at least {@value #RANGE_SHARE} of its active
     * hours, each with {@code p = 1}.
     */
    public static Map<String, CellEstimate> receiverRange(Map<String, Long> hoursByCell, long activeHours) {
        Map<String, CellEstimate> out = new LinkedHashMap<>();
        if (activeHours <= 0) {
            return out;
        }
        hoursByCell.forEach((cell, hours) -> {
            double share = Math.min(1.0, (double) hours / activeHours);
            if (share >= RANGE_SHARE) {
                out.put(cell, new CellEstimate(1.0, hours, share));
            }
        });
        return out;
    }

    /** The {@code q} quantile (nearest rank) of {@code values}; 0 when empty. */
    static double percentile(List<Double> values, double q) {
        if (values.isEmpty()) {
            return 0;
        }
        List<Double> sorted = new ArrayList<>(values);
        sorted.sort(null);
        int rank = (int) Math.ceil(q * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(sorted.size() - 1, rank)));
    }
}
