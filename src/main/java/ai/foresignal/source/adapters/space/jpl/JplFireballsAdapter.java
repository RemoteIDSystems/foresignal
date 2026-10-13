package ai.foresignal.source.adapters.space.jpl;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code jpl-fireballs} (prompt 077): bright fireballs reported by US Government sensors, from the JPL Fireball Data
 * API, as {@code fireball} records with a point ({@link JplParsers#fireballs}).
 *
 * <p><strong>Each run:</strong> one request for {@value #API}{@code ?date-min={today-30d}&req-loc=true} (located
 * events only). Rows are revised without a revision stamp, so {@code observedAtMode} is RECEIVED and a revised row
 * is a new version. The API sends no validators.
 */
@Component
public class JplFireballsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "jpl-fireballs";

    /** The API. */
    public static final String API = "https://ssd-api.jpl.nasa.gov/fireball.api";

    /** How far back each run reads. */
    public static final Duration WINDOW = Duration.ofDays(30);

    /** Raw retention: fireballs are the {@code event} class (1825 d). */
    static final Duration RETENTION = Duration.ofDays(1825);

    private final String api;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value SpaceSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public JplFireballsAdapter(@Value("${" + SpaceSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.api = SpaceSources.uri(API, baseUrlOverride).toString();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The request URL at {@code now}: the last 30 UTC days, located events only. */
    public URI uri(Instant now) {
        return URI.create(api + "?date-min=" + LocalDate.ofInstant(now.minus(WINDOW), ZoneOffset.UTC) + "&req-loc=true");
    }

    /**
     * Fetches the window (see the class note).
     *
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException when the answer is not the API's table
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        URI uri = uri(ctx.now());
        JplParsers.Parsed parsed = JplParsers.fireballs(SpaceSources.body(ctx, ctx.http().get(uri), null, uri));
        ctx.log().debug("{}: API {} with {} fireball(s), skipped {}", ID, parsed.version(), parsed.records().size(),
                parsed.skipped());
        return FetchResult.of(parsed.records());
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = JplParsers.FIREBALL_KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("JPL fireballs")
                .description("Bright fireballs (bolides) detected by US Government sensors, from the NASA/JPL"
                        + " Fireball Data API: time, location, altitude, velocity, radiated energy and estimated"
                        + " impact energy. Located events of the last 30 days; rows are revised in place.")
                .kind(kind)
                .homepage("https://cneos.jpl.nasa.gov/fireballs/")
                .attribution("NASA/JPL Center for Near Earth Object Studies (CNEOS), Fireball Data API")
                .licence("US-PD")
                .termsUrl("https://ssd-api.jpl.nasa.gov/doc/fireball.html")
                .mode(Mode.POLLING)
                .interval(Duration.ofHours(1), Duration.ofHours(6))
                .defaultBudget(Budget.of(1, 10))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.RECEIVED)
                .zeroResultsNormal(true)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .searchFields()
                .rankField("impactEnergyKt");
        b.fieldSpec("radiatedEnergyJ", new FieldSpec(FieldType.NUMBER, "J", "Radiated energy",
                "Total radiated optical energy in joules (energy x 1e10)", null, Set.of()));
        b.fieldSpec("impactEnergyKt", new FieldSpec(FieldType.NUMBER, null, "Impact energy (kt)",
                "Estimated total impact energy in kilotons of TNT", null, Set.of()));
        b.fieldSpec("altitudeKm", new FieldSpec(FieldType.NUMBER, "km", "Altitude",
                "Altitude above the geoid at peak brightness", "MSL", Set.of()));
        b.fieldSpec("velocityKms", new FieldSpec(FieldType.NUMBER, "km/s", "Velocity", "Pre-impact velocity", null,
                Set.of()));
        return b.build();
    }
}
