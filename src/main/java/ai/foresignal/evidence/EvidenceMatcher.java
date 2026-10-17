package ai.foresignal.evidence;

import ai.foresignal.source.spi.Geo;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Decides whether two observations of one family describe the same real-world event (prompt 116), pure and
 * deterministic.
 *
 * <ul>
 *   <li><strong>Upstream id</strong> (every family): the same upstream event id in the same namespace means the
 *       same event, confidence 1.0, ahead of any proximity rule.
 *   <li><strong>{@code proximity}</strong> (earthquakes, fires): within the time, distance and attribute
 *       tolerances; confidence {@code 1 − max(dt/maxDt, km/maxKm, Δ/maxΔ)}, floored at {@value #FLOOR}.
 *   <li><strong>{@code cell-day}</strong> (conflict events): the same {@code cellDegrees} cell and UTC day;
 *       confidence {@code 1 − 0.5 · dt/1 day}.
 *   <li><strong>{@code outage}</strong>: the same ASN (0.9) or country (0.8) with overlapping intervals (an
 *       observation without an interval spans {@code maxDt} either side of its event time).
 *   <li><strong>{@code hazard}</strong> (disasters): the same normalized hazard type within {@code maxKm} and
 *       {@code maxDt}, proximity confidence.
 *   <li><strong>{@code identifier}</strong> (weather alerts): only the upstream id (the CAP identifier).
 * </ul>
 */
public final class EvidenceMatcher {

    /** The resolver version recorded on canonical events. */
    public static final String RESOLVER_VERSION = "1.0.0";

    /** The lowest confidence a proximity match reports. */
    public static final double FLOOR = 0.3;

    /** The rule name of an upstream-id match. */
    public static final String UPSTREAM_RULE = "upstream-id";

    private EvidenceMatcher() {}

    /**
     * A match.
     *
     * @param confidence the link confidence, 0–1
     * @param reason {@code {rule, dtSeconds, distanceKm, attributeDeltas, upstreamIdMatch}}
     */
    public record Match(double confidence, Map<String, Object> reason) {}

    /** Matches {@code a} against {@code b} under {@code rule}; empty when they are not the same event. */
    public static Optional<Match> match(CanonicalRules.FamilyRule rule, ObservationFacts a, ObservationFacts b) {
        Double dt = dtSeconds(a.eventTime(), b.eventTime());
        Double km = a.located() && b.located() ? Geo.haversineKm(a.lat(), a.lon(), b.lat(), b.lon()) : null;
        for (String id : a.upstreamIds()) {
            if (b.upstreamIds().contains(id)) {
                return Optional.of(new Match(1.0, reason(UPSTREAM_RULE, dt, km, Map.of(), true)));
            }
        }
        return switch (rule.match()) {
            case "proximity" -> proximity(rule, a, b, dt, km);
            case "cell-day" -> cellDay(rule, a, b, dt, km);
            case "outage" -> outage(rule, a, b, dt, km);
            case "hazard" -> hazard(rule, a, b, dt, km);
            default -> Optional.empty();
        };
    }

    private static Optional<Match> proximity(CanonicalRules.FamilyRule rule, ObservationFacts a, ObservationFacts b,
            Double dt, Double km) {
        if (dt == null || km == null || rule.maxDtSeconds() <= 0 || rule.maxKm() <= 0) {
            return Optional.empty();
        }
        if (dt > rule.maxDtSeconds() || km > rule.maxKm()) {
            return Optional.empty();
        }
        double normalized = Math.max(dt / rule.maxDtSeconds(), km / rule.maxKm());
        Map<String, Object> deltas = new LinkedHashMap<>();
        if (rule.attribute() != null && a.attribute() != null && b.attribute() != null) {
            double delta = Math.abs(a.attribute() - b.attribute());
            if (delta > rule.attribute().maxDelta()) {
                return Optional.empty();
            }
            deltas.put(rule.attribute().name(), round(delta));
            if (rule.attribute().maxDelta() > 0) {
                normalized = Math.max(normalized, delta / rule.attribute().maxDelta());
            }
        }
        return Optional.of(new Match(confidence(normalized), reason(rule.match(), dt, km, deltas, false)));
    }

    private static Optional<Match> cellDay(CanonicalRules.FamilyRule rule, ObservationFacts a, ObservationFacts b,
            Double dt, Double km) {
        if (!a.located() || !b.located() || a.eventTime() == null || b.eventTime() == null) {
            return Optional.empty();
        }
        double cell = rule.cellDegrees() > 0 ? rule.cellDegrees() : 0.1;
        boolean sameCell = Math.floor(a.lon() / cell) == Math.floor(b.lon() / cell)
                && Math.floor(a.lat() / cell) == Math.floor(b.lat() / cell);
        boolean sameDay = a.eventTime().atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS)
                .equals(b.eventTime().atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS));
        if (!sameCell || !sameDay) {
            return Optional.empty();
        }
        double confidence = 1 - 0.5 * Math.min(1, dt / 86_400.0);
        return Optional.of(new Match(round(confidence), reason(rule.match(), dt, km, Map.of(), false)));
    }

    private static Optional<Match> outage(CanonicalRules.FamilyRule rule, ObservationFacts a, ObservationFacts b,
            Double dt, Double km) {
        boolean asn = a.asn() != null && a.asn().equals(b.asn());
        boolean country = a.country() != null && a.country().equalsIgnoreCase(b.country());
        if (!asn && !country) {
            return Optional.empty();
        }
        Duration slack = Duration.ofSeconds((long) (rule.maxDtSeconds() > 0 ? rule.maxDtSeconds() : 21_600));
        Instant[] ia = interval(a, slack);
        Instant[] ib = interval(b, slack);
        if (ia == null || ib == null || ia[0].isAfter(ib[1]) || ib[0].isAfter(ia[1])) {
            return Optional.empty();
        }
        Map<String, Object> deltas = new LinkedHashMap<>();
        deltas.put(asn ? "asn" : "country", 0);
        return Optional.of(new Match(asn ? 0.9 : 0.8, reason(rule.match(), dt, km, deltas, false)));
    }

    private static Instant[] interval(ObservationFacts facts, Duration slack) {
        Instant start = facts.start();
        Instant end = facts.end();
        if (start == null) {
            if (facts.eventTime() == null) {
                return null;
            }
            return new Instant[] {facts.eventTime().minus(slack), facts.eventTime().plus(slack)};
        }
        return new Instant[] {start, end == null || end.isBefore(start) ? start.plus(slack) : end};
    }

    private static Optional<Match> hazard(CanonicalRules.FamilyRule rule, ObservationFacts a, ObservationFacts b,
            Double dt, Double km) {
        if (a.hazard() == null || !a.hazard().equals(b.hazard())) {
            return Optional.empty();
        }
        if (dt == null || km == null || dt > rule.maxDtSeconds() || km > rule.maxKm()) {
            return Optional.empty();
        }
        double normalized = Math.max(dt / rule.maxDtSeconds(), km / rule.maxKm());
        return Optional.of(new Match(confidence(normalized),
                reason(rule.match(), dt, km, Map.of("hazard", a.hazard()), false)));
    }

    /** {@code 1 − normalized}, floored at {@value #FLOOR}, rounded to 4 decimals. */
    static double confidence(double normalized) {
        return round(Math.max(FLOOR, 1 - Math.max(0, normalized)));
    }

    private static Map<String, Object> reason(String rule, Double dt, Double km, Map<String, Object> deltas,
            boolean upstream) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("rule", rule);
        out.put("dtSeconds", dt == null ? null : round(dt));
        out.put("distanceKm", km == null ? null : round(km));
        out.put("attributeDeltas", deltas);
        out.put("upstreamIdMatch", upstream);
        return out;
    }

    private static Double dtSeconds(Instant a, Instant b) {
        if (a == null || b == null) {
            return null;
        }
        return Math.abs(Duration.between(a, b).toMillis()) / 1000.0;
    }

    private static double round(double value) {
        return Math.round(value * 10_000) / 10_000.0;
    }
}
