package ai.foresignal.source.adapters.space.swpc;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code swpc-rtsw} (prompt 077): the real-time solar wind at L1, downsampled to one {@code solar-wind} entity
 * {@code l1} ({@link RtswDownsampler}).
 *
 * <p>Verified 2026-10: the old {@code products/solar-wind/*} five-minute files are gone (404) and the RTSW
 * one-minute files are 1.5–2.7 MB with rows from several spacecraft. So:
 * <ul>
 *   <li>every run reads the two summary files ({@value #SPEED_URL}, {@value #FIELD_URL}, about 60 bytes each)
 *       with conditional GETs;
 *   <li>once per {@code densityInterval} (default 60 min, at least 30 min) it reads {@value #WIND_URL}, also
 *       conditionally, and keeps the newest active row's density and spacecraft;
 *   <li>the last values of each part are kept in adapter state, so a run where only one part changed still emits a
 *       complete snapshot. Values are rounded, so unchanged rounded values are a touch, not a version.
 * </ul>
 * Every file 304 (and the large file not due) is {@link FetchResult#unchanged()}.
 */
@Component
public class SwpcRtswAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "swpc-rtsw";

    /** The speed summary. */
    public static final String SPEED_URL = "https://services.swpc.noaa.gov/products/summary/solar-wind-speed.json";

    /** The magnetic field summary. */
    public static final String FIELD_URL = "https://services.swpc.noaa.gov/products/summary/solar-wind-mag-field.json";

    /** The one-minute RTSW wind file (1.5–2.7 MB), read at most once per {@code densityInterval}. */
    public static final String WIND_URL = "https://services.swpc.noaa.gov/json/rtsw/rtsw_wind_1m.json";

    /** The {@code densityInterval} parameter. */
    public static final String DENSITY_INTERVAL_PARAM = "densityInterval";

    /** The conditional-GET state key of the speed summary. */
    public static final String SPEED_KEY = "speed";

    /** The conditional-GET state key of the field summary. */
    public static final String FIELD_KEY = "mag-field";

    /** The conditional-GET state key of the wind file. */
    public static final String WIND_KEY = "wind-1m";

    /** The state key of the last speed ({@code speed|time}). */
    public static final String LAST_SPEED_KEY = "rtsw:speed";

    /** The state key of the last field ({@code bt|bz|time}). */
    public static final String LAST_FIELD_KEY = "rtsw:field";

    /** The state key of the last density ({@code density|spacecraft|time}). */
    public static final String LAST_DENSITY_KEY = "rtsw:density";

    /** The state key of when the wind file was last requested. */
    public static final String DENSITY_READ_AT_KEY = "rtsw:densityReadAt";

    /** The record TTL, from {@code observedAt}. */
    static final Duration TTL = Duration.ofMinutes(30);

    /** The keyframe: under {@code ttl - poll - 30 s}. */
    static final Duration KEYFRAME = Duration.ofMinutes(20);

    /** Raw retention: SAMPLED solar-wind state, the {@code state} class (365 d). */
    static final Duration RETENTION = Duration.ofDays(365);

    /** The response cap (the wind file was 2.6 MB on 2026-10-13). */
    static final int MAX_RESPONSE_BYTES = 8 * 1024 * 1024;

    private final URI speedUri;
    private final URI fieldUri;
    private final URI windUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value SpaceSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public SwpcRtswAdapter(@Value("${" + SpaceSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.speedUri = SpaceSources.uri(SPEED_URL, baseUrlOverride);
        this.fieldUri = SpaceSources.uri(FIELD_URL, baseUrlOverride);
        this.windUri = SpaceSources.uri(WIND_URL, baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** {@code densityInterval} must be at least 30 minutes. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params != null && params.has(DENSITY_INTERVAL_PARAM)) {
            Duration interval = params.duration(DENSITY_INTERVAL_PARAM);
            if (interval != null && interval.compareTo(RtswDownsampler.MIN_DENSITY_INTERVAL) < 0) {
                problems.put(DENSITY_INTERVAL_PARAM, "must be at least 30 minutes");
            }
        }
        return problems;
    }

    /**
     * Fetches the summaries and, when due, the wind file (see the class note).
     *
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException when a changed file cannot be used
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        AdapterStateStore state = ctx.state();
        Instant now = ctx.now();
        byte[] speedBody = SpaceSources.conditional(ctx, speedUri, SPEED_KEY);
        byte[] fieldBody = SpaceSources.conditional(ctx, fieldUri, FIELD_KEY);
        byte[] windBody = null;
        boolean windRead = false;
        if (RtswDownsampler.densityDue(instant(state.get(DENSITY_READ_AT_KEY).orElse(null)), now,
                densityInterval(ctx.params()))) {
            windRead = true;
            state.put(DENSITY_READ_AT_KEY, now.toString());
            windBody = SpaceSources.conditional(ctx, windUri, WIND_KEY);
        }
        if (speedBody == null && fieldBody == null && windBody == null) {
            return FetchResult.unchanged();
        }
        if (speedBody != null) {
            RtswDownsampler.Speed speed = parse(ctx, SPEED_KEY, () -> RtswDownsampler.speed(speedBody));
            if (speed != null && speed.at() != null) {
                state.put(LAST_SPEED_KEY, speed.speedKms() + "|" + speed.at());
            }
        }
        if (fieldBody != null) {
            RtswDownsampler.Field field = parse(ctx, FIELD_KEY, () -> RtswDownsampler.field(fieldBody));
            if (field != null && field.at() != null) {
                state.put(LAST_FIELD_KEY, field.btNt() + "|" + field.bzNt() + "|" + field.at());
            }
        }
        if (windBody != null) {
            byte[] body = windBody;
            RtswDownsampler.Density density = parse(ctx, WIND_KEY, () -> RtswDownsampler.activeDensity(body));
            if (density != null) {
                state.put(LAST_DENSITY_KEY, density.densityCc() + "|" + density.spacecraft() + "|" + density.at());
            }
        }
        ctx.log().debug("{}: summaries {}/{}, wind file {}", ID, speedBody != null, fieldBody != null,
                windRead ? (windBody != null ? "read" : "not modified") : "not due");
        NormalizedRecord record = RtswDownsampler.record(lastSpeed(state), lastField(state), lastDensity(state));
        return FetchResult.of(record == null ? List.of() : List.of(record));
    }

    /** The configured {@code densityInterval}, at least 30 minutes. */
    static Duration densityInterval(ValidatedParams params) {
        Duration d = params == null || !params.has(DENSITY_INTERVAL_PARAM) ? null : params.duration(DENSITY_INTERVAL_PARAM);
        if (d == null) {
            return RtswDownsampler.DEFAULT_DENSITY_INTERVAL;
        }
        return d.compareTo(RtswDownsampler.MIN_DENSITY_INTERVAL) < 0 ? RtswDownsampler.MIN_DENSITY_INTERVAL : d;
    }

    private static <T> T parse(FetchContext ctx, String key, java.util.function.Supplier<T> parse) {
        try {
            return parse.get();
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), key);
            throw e;
        }
    }

    static RtswDownsampler.Speed lastSpeed(AdapterStateStore state) {
        String[] p = parts(state, LAST_SPEED_KEY, 2);
        Double speed = p == null ? null : number(p[0]);
        return speed == null ? null : new RtswDownsampler.Speed(speed, instant(p[1]));
    }

    static RtswDownsampler.Field lastField(AdapterStateStore state) {
        String[] p = parts(state, LAST_FIELD_KEY, 3);
        return p == null ? null : new RtswDownsampler.Field(number(p[0]), number(p[1]), instant(p[2]));
    }

    static RtswDownsampler.Density lastDensity(AdapterStateStore state) {
        String[] p = parts(state, LAST_DENSITY_KEY, 3);
        Double density = p == null ? null : number(p[0]);
        return density == null ? null
                : new RtswDownsampler.Density(density, "null".equals(p[1]) ? null : p[1], instant(p[2]));
    }

    private static String[] parts(AdapterStateStore state, String key, int n) {
        String[] p = state.get(key).map(v -> v.split("\\|", -1)).orElse(null);
        return p == null || p.length != n ? null : p;
    }

    private static Double number(String s) {
        try {
            return s == null || s.equals("null") ? null : Double.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Instant instant(String s) {
        try {
            return s == null || s.equals("null") ? null : Instant.parse(s);
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = RtswDownsampler.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NOAA SWPC real-time solar wind")
                .description("The solar wind at L1 from NOAA SWPC's real-time solar wind products, downsampled to"
                        + " one entity (l1): proton speed (nearest 10 km/s) and magnetic field Bt and Bz (nearest"
                        + " 0.5 nT) from the summary files every poll, and proton density (0.1 per cm3) with the"
                        + " active spacecraft from the 2.7 MB one-minute file at most once per densityInterval"
                        + " (default 60 min). No geometry.")
                .kind(kind)
                .homepage("https://www.swpc.noaa.gov/products/real-time-solar-wind")
                .attribution("NOAA Space Weather Prediction Center")
                .licence("US-PD")
                .termsUrl("https://www.weather.gov/disclaimer")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(5))
                .defaultBudget(Budget.of(4, 900, 80))
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(KEYFRAME))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(TTL)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .titleField("speedKms")
                .searchFields("spacecraft")
                .rankField("speedKms")
                .param(ParamSpec.optional(DENSITY_INTERVAL_PARAM, ParamType.DURATION,
                        "How often the 2.7 MB one-minute wind file is read for density, at least 30m", "60m"));
        b.fieldSpec("speedKms", new FieldSpec(FieldType.INTEGER, "km/s", "Speed", "Proton speed, nearest 10 km/s",
                null, Set.of()));
        b.fieldSpec("btNt", new FieldSpec(FieldType.NUMBER, "nT", "Bt", "Total interplanetary magnetic field, nearest"
                + " 0.5 nT", null, Set.of()));
        b.fieldSpec("bzNt", new FieldSpec(FieldType.NUMBER, "nT", "Bz", "GSM z component of the field, nearest 0.5 nT;"
                + " southward is negative", null, Set.of()));
        b.fieldSpec("densityCc", new FieldSpec(FieldType.NUMBER, "cm-3", "Density", "Proton density per cm3, 0.1",
                null, Set.of()));
        b.fieldSpec("spacecraft", new FieldSpec(FieldType.STRING, null, "Spacecraft",
                "The active real-time spacecraft the density comes from (SOLAR1, IMAP, ACE)", null, Set.of()));
        return b.build();
    }
}
