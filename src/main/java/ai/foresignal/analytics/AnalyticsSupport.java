package ai.foresignal.analytics;

import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.observability.ObservabilityDeclarations;
import ai.foresignal.observability.ObservabilityService;
import ai.foresignal.observability.Place;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Shared checks of the discovery detectors (prompt 119): whether a series rests on a private source (its findings are
 * private, EGR-1), whether a downward conclusion is supported by 116 detectability ({@code canSeeAbsence}, OBS-1), and
 * plain-language labels and numbers for titles and explanations.
 */
@Component
public class AnalyticsSupport {

    /** The 117 families of market series ({@code DAILY_RESOLUTION_ONLY}). */
    public static final Set<String> MARKET_FAMILIES = Set.of("price", "volume", "open-interest", "position", "rate");

    private final ObjectProvider<IntelVisibility> visibility;
    private final ObjectProvider<ObservabilityService> observability;
    private final ObjectProvider<ObservabilityDeclarations> declarations;

    /** Creates the component. */
    public AnalyticsSupport(ObjectProvider<IntelVisibility> visibility,
            ObjectProvider<ObservabilityService> observability, ObjectProvider<ObservabilityDeclarations> declarations) {
        this.visibility = visibility;
        this.observability = observability;
        this.declarations = declarations;
    }

    /** True when any source the series rests on is private. */
    public boolean privateSeries(SeriesData data) {
        IntelVisibility v = visibility.getIfAvailable();
        if (v == null) {
            return false;
        }
        for (String source : data.sources()) {
            if (v.isPrivateSource(source)) {
                return true;
            }
        }
        return false;
    }

    /** The {@code LOCAL_ONLY} sources of a series (its findings withhold raw values from callers without local egress). */
    public List<String> localOnlySources(SeriesData data) {
        return data.localOnly() ? new ArrayList<>(data.sources()) : List.of();
    }

    /** True for a market series ({@code DAILY_RESOLUTION_ONLY}). */
    public static boolean market(SeriesData data) {
        return MARKET_FAMILIES.contains(data.seriesFamily()) || "markets".equals(data.signalFamily());
    }

    /**
     * Whether a downward conclusion about a count series over {@code [from, to)} is supported: 116
     * {@code canSeeAbsence} for the observability family of the series' kind in its region. Series without a kind
     * (rule groups, series points) are judged by their coverage flags alone, which the caller has already checked.
     */
    public AbsenceCheck canSeeDecline(SeriesData data, Instant from, Instant to) {
        ObservabilityService service = observability.getIfAvailable();
        ObservabilityDeclarations decl = declarations.getIfAvailable();
        SeriesRef ref = data.ref();
        if (service == null || decl == null || ref.type() != SeriesRef.Type.ROLLUP) {
            return new AbsenceCheck(true, null, null);
        }
        String family = decl.familyOfKind(ref.kind());
        Place place = ref.region() != null && !"ZZ".equals(ref.region()) ? Place.region(ref.region()) : Place.GLOBAL;
        try {
            if (data.sources().isEmpty()) {
                return service.canSeeAbsence(family, place, from, to, null);
            }
            return service.canSeeAbsence(data.sources(), family, place, from, to, null);
        } catch (RuntimeException e) {
            return new AbsenceCheck(false, null, "detectability could not be judged: " + e.getMessage());
        }
    }

    /** A short human label of a series reference. */
    public static String label(SeriesRef ref) {
        return switch (ref.type()) {
            case SERIES -> ref.seriesId();
            case ROLLUP -> ref.kind() + " count in " + (ref.region() != null ? ref.region() : "cell " + ref.cell())
                    + (ref.allSources() ? "" : " (" + ref.sourceId() + ")");
            case RULE -> "anomaly rule " + ref.ruleId() + " group " + ref.groupKey();
        };
    }

    /** A number with up to three significant digits for prose. */
    public static String num(double value) {
        if (!Double.isFinite(value)) {
            return "n/a";
        }
        double abs = Math.abs(value);
        if (abs >= 100) {
            return String.format(Locale.ROOT, "%.0f", value);
        }
        if (abs >= 10) {
            return String.format(Locale.ROOT, "%.1f", value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    /** A rounded double for stored statistics (six significant digits), or null when not finite. */
    public static Double round(double value) {
        if (!Double.isFinite(value)) {
            return null;
        }
        if (value == 0) {
            return 0.0;
        }
        java.math.BigDecimal d = new java.math.BigDecimal(value).round(new java.math.MathContext(6));
        return d.doubleValue();
    }
}
