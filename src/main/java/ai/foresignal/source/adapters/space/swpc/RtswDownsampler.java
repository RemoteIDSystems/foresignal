package ai.foresignal.source.adapters.space.swpc;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.time.Duration;
import java.time.Instant;
import tools.jackson.databind.JsonNode;

/**
 * The downsampled real-time solar wind of {@code swpc-rtsw} (prompt 077). Pure.
 *
 * <ul>
 *   <li><strong>Summaries</strong> (every poll, about 60 bytes each): {@code products/summary/solar-wind-speed.json}
 *       {@code [{proton_speed, time_tag}]} and {@code products/summary/solar-wind-mag-field.json}
 *       {@code [{bt, bz_gsm, time_tag}]}.
 *   <li><strong>Density</strong> (once per {@code densityInterval}): {@code json/rtsw/rtsw_wind_1m.json}, 1.5–2.7 MB
 *       of one-minute rows from several spacecraft ({@code source} {@code SOLAR1}, {@code IMAP}, {@code ACE}),
 *       one flagged {@code active}; the newest active row with a density gives {@code density} and
 *       {@code spacecraft}.
 *   <li><strong>Rounding</strong>, so noise does not create versions: {@code speedKms} to the nearest 10,
 *       {@code btNt} and {@code bzNt} to the nearest 0.5, {@code densityCc} to 0.1.
 * </ul>
 */
public final class RtswDownsampler {

    /** The record kind. */
    public static final String KIND = "solar-wind";

    /** The single entity: the L1 point. */
    public static final String ENTITY = "l1";

    /** The default {@code densityInterval}. */
    public static final Duration DEFAULT_DENSITY_INTERVAL = Duration.ofMinutes(60);

    /** The shortest {@code densityInterval}. */
    public static final Duration MIN_DENSITY_INTERVAL = Duration.ofMinutes(30);

    /**
     * The speed summary.
     *
     * @param speedKms the proton speed in km/s, unrounded
     * @param at its {@code time_tag}
     */
    public record Speed(double speedKms, Instant at) {}

    /**
     * The magnetic field summary.
     *
     * @param btNt the total field in nT, unrounded
     * @param bzNt the GSM z component in nT, unrounded
     * @param at its {@code time_tag}
     */
    public record Field(Double btNt, Double bzNt, Instant at) {}

    /**
     * The density of the newest active row.
     *
     * @param densityCc protons per cm³, unrounded
     * @param spacecraft the row's {@code source}
     * @param at its {@code time_tag}
     */
    public record Density(double densityCc, String spacecraft, Instant at) {}

    private RtswDownsampler() {}

    /**
     * Reads the speed summary.
     *
     * @return the newest row, or null when there is none with a speed and time
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static Speed speed(byte[] body) {
        JsonNode row = SwpcProductsParser.newest(
                SwpcProductsParser.array(SwpcProductsParser.read(body, "solar-wind-speed"), "solar-wind-speed"),
                "proton_speed");
        return row == null ? null
                : new Speed(JsonProps.number(row, "proton_speed"), SpaceSources.utc(JsonProps.text(row, "time_tag")));
    }

    /**
     * Reads the magnetic field summary.
     *
     * @return the newest row, or null when there is none with {@code bt} and a time
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static Field field(byte[] body) {
        JsonNode row = SwpcProductsParser.newest(
                SwpcProductsParser.array(SwpcProductsParser.read(body, "solar-wind-mag-field"), "solar-wind-mag-field"),
                "bt");
        return row == null ? null : new Field(JsonProps.number(row, "bt"), JsonProps.number(row, "bz_gsm"),
                SpaceSources.utc(JsonProps.text(row, "time_tag")));
    }

    /**
     * The newest row of {@code rtsw_wind_1m.json} with {@code active=true} and a {@code proton_density}.
     *
     * @return the density, or null when no row qualifies
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static Density activeDensity(byte[] body) {
        JsonNode rows = SwpcProductsParser.array(SwpcProductsParser.read(body, "rtsw_wind_1m"), "rtsw_wind_1m");
        JsonNode best = null;
        Instant bestAt = null;
        for (JsonNode row : rows) {
            JsonNode active = row.get("active");
            if (active == null || !active.isBoolean() || !active.booleanValue()) {
                continue;
            }
            Instant at = SpaceSources.utc(JsonProps.text(row, "time_tag"));
            if (at != null && JsonProps.number(row, "proton_density") != null && (bestAt == null || at.isAfter(bestAt))) {
                best = row;
                bestAt = at;
            }
        }
        return best == null ? null
                : new Density(JsonProps.number(best, "proton_density"), JsonProps.text(best, "source"), bestAt);
    }

    /**
     * True when the large file is due: never read, or read at least {@code interval} before {@code now}.
     *
     * @param lastRead when it was last read, or null
     */
    public static boolean densityDue(Instant lastRead, Instant now, Duration interval) {
        return lastRead == null || !now.isBefore(lastRead.plus(interval));
    }

    /** {@code speedKms} rounded to the nearest 10 km/s. */
    public static long roundSpeed(double speedKms) {
        return Math.round(SpaceSources.roundTo(speedKms, 10));
    }

    /** A field component rounded to the nearest 0.5 nT, or null. */
    public static Double roundField(Double nt) {
        return nt == null || !Double.isFinite(nt) ? null : SpaceSources.roundTo(nt, 0.5);
    }

    /** A density rounded to 0.1 per cm³. */
    public static Double roundDensity(Double cc) {
        return SpaceSources.round(cc, 1);
    }

    /**
     * The {@value #ENTITY} record: {@code observedAt} is the newer of the two summary times, the TTL counts from it.
     *
     * @param speed the speed summary; required
     * @param field the field summary, or null
     * @param density the last density, or null
     * @return the record, or null without a speed
     */
    public static NormalizedRecord record(Speed speed, Field field, Density density) {
        if (speed == null || speed.at() == null) {
            return null;
        }
        Instant observed = speed.at();
        if (field != null && field.at() != null && field.at().isAfter(observed)) {
            observed = field.at();
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(ENTITY, KIND).observedAt(observed)
                .put("speedKms", roundSpeed(speed.speedKms()));
        if (field != null) {
            b.put("btNt", roundField(field.btNt())).put("bzNt", roundField(field.bzNt()));
        }
        if (density != null) {
            b.put("densityCc", roundDensity(density.densityCc())).put("spacecraft", density.spacecraft());
        }
        return b.build();
    }
}
