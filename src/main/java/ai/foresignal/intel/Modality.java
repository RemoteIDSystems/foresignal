package ai.foresignal.intel;

import java.util.Set;

/**
 * The sensing modality of a member (prompt 154): how the world was observed, a dimension distinct from the
 * signal family (what was observed) and the originator root (who observed it). Two reports from different
 * modalities — a satellite image, a seismometer, an official bulletin, a social post — corroborate more
 * strongly than two of the same modality, so an activation rule may require several distinct modalities.
 *
 * <p>The modality is derived deterministically from the originator class ({@link ai.foresignal.evidence.Originator}),
 * never taken from a client or model: {@code sensor-network} is {@link #SENSOR}, {@code imagery} is
 * {@link #IMAGERY}, {@code agency} is {@link #OFFICIAL}, {@code social} is {@link #SOCIAL}, {@code media} is
 * {@link #MEDIA}, {@code crowd} is {@link #CROWD}, {@code operator} is {@link #OPERATOR}, {@code derived} is
 * {@link #DERIVED}; anything else (an unmapped aggregator) is {@link #OTHER}.
 */
public final class Modality {

    /** A physical sensor network: AIS, ADS-B, seismometers, GNSS monitors, grid telemetry. */
    public static final String SENSOR = "sensor";

    /** Overhead or remote imagery: satellite fire/thermal detections, SAR, optical. */
    public static final String IMAGERY = "imagery";

    /** An official or agency source: a government bulletin, a notice to mariners, a met-office warning. */
    public static final String OFFICIAL = "official";

    /** A social-media account or post. */
    public static final String SOCIAL = "social";

    /** A news outlet or broadcaster. */
    public static final String MEDIA = "media";

    /** A crowd or volunteer report. */
    public static final String CROWD = "crowd";

    /** The operator of this instance. */
    public static final String OPERATOR = "operator";

    /** A derived product this instance computed from other observations. */
    public static final String DERIVED = "derived";

    /** An unclassified aggregator or anything with no mapped modality. */
    public static final String OTHER = "other";

    /** Every modality. */
    public static final Set<String> ALL = Set.of(SENSOR, IMAGERY, OFFICIAL, SOCIAL, MEDIA, CROWD, OPERATOR, DERIVED,
            OTHER);

    private Modality() {}

    /** The modality of an originator class; {@link #OTHER} for null or an unmapped class. */
    public static String of(String originatorClass) {
        if (originatorClass == null) {
            return OTHER;
        }
        return switch (originatorClass) {
            case "sensor-network" -> SENSOR;
            case "imagery" -> IMAGERY;
            case "agency" -> OFFICIAL;
            case "social" -> SOCIAL;
            case "media" -> MEDIA;
            case "crowd" -> CROWD;
            case "operator" -> OPERATOR;
            case "derived" -> DERIVED;
            default -> OTHER;
        };
    }
}
