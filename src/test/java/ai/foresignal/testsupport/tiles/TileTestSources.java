package ai.foresignal.testsupport.tiles;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import ai.foresignal.testsupport.records.RecordsTestSources;
import java.time.Duration;
import java.util.Set;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The vector tile test context (prompt 016): the read-API sources of {@link RecordsTestSources},
 * plus
 *
 * <ul>
 *   <li>{@value #QUAKE}: STANDARD ALL, kind {@value #QUAKE_KIND}, title from {@code place}, tile
 *       properties {@code mag}, {@code place} and the invalid {@code bad-name}; {@code mag} is a
 *       labeled NUMBER; rank field {@code mag}; never aggregates;
 *   <li>{@value #PLANE}: LATEST (significant {@code callsign}), kind {@value #PLANE_KIND}, tile
 *       properties {@code track} and {@code gs}, {@code aggBelowZoom} 6, so a move or a new
 *       {@code track} is a LIVE update rather than a version;
 *   <li>{@value #FIRE_A}, {@value #FIRE_B} and the private {@value #FIRE_PRIVATE}: all kind
 *       {@value #FIRE_KIND}; {@code aggBelowZoom} 8, 5 and 3; rank field {@code frp} on A and the
 *       private one only;
 *   <li>{@value #EXPIRING}: STANDARD with a 10 min TTL, kind {@value #EXPIRING_KIND}.
 * </ul>
 *
 * <p>Every tile integration test imports this one configuration, so they share one context.
 */
@TestConfiguration
@Import(RecordsTestSources.class)
public class TileTestSources {

    public static final String QUAKE = "tile-quake";
    public static final String QUAKE_KIND = "tile-earthquake";
    public static final String PLANE = "tile-plane";
    public static final String PLANE_KIND = "tile-aircraft";
    public static final String FIRE_A = "tile-fire-a";
    public static final String FIRE_B = "tile-fire-b";
    public static final String FIRE_PRIVATE = "tile-fire-private";
    public static final String FIRE_KIND = "tile-fire";
    public static final String EXPIRING = "tile-expiring";
    public static final String EXPIRING_KIND = "tile-event";

    /** {@value #QUAKE}. */
    @Bean
    public TestPushAdapter tileQuakeAdapter() {
        return new TestPushAdapter(base(QUAKE, QUAKE_KIND)
                .description("Test earthquakes for tiles")
                .titleField("place")
                .tileProperties("mag", "place", "bad-name")
                .rankField("mag")
                .fieldSpec("mag", new FieldSpec(FieldType.NUMBER, "1", "Magnitude", null, null, Set.of()))
                .fieldSpec("place", new FieldSpec(FieldType.STRING, null, null, null, null, Set.of()))
                .build());
    }

    /** {@value #PLANE}. */
    @Bean
    public TestPushAdapter tilePlaneAdapter() {
        return new TestPushAdapter(base(PLANE, PLANE_KIND)
                .significantFields("callsign")
                .defaultIngestPolicy(IngestPolicy.latest(null))
                .tileProperties("track", "gs")
                .aggBelowZoom(6)
                .build());
    }

    /** {@value #FIRE_A}. */
    @Bean
    public TestPushAdapter tileFireAAdapter() {
        return new TestPushAdapter(base(FIRE_A, FIRE_KIND)
                .tileProperties("frp")
                .rankField("frp")
                .aggBelowZoom(8)
                .fieldSpec("frp", new FieldSpec(FieldType.NUMBER, "MW", "Fire radiative power", null, null, Set.of()))
                .build());
    }

    /** {@value #FIRE_B}. */
    @Bean
    public TestPushAdapter tileFireBAdapter() {
        return new TestPushAdapter(base(FIRE_B, FIRE_KIND).tileProperties("frp", "confidence").aggBelowZoom(5).build());
    }

    /** {@value #FIRE_PRIVATE}. */
    @Bean
    public TestPushAdapter tileFirePrivateAdapter() {
        return new TestPushAdapter(base(FIRE_PRIVATE, FIRE_KIND)
                .privateSource(true)
                .tileProperties("frp", "owner")
                .rankField("frp")
                .aggBelowZoom(3)
                .build());
    }

    /** {@value #EXPIRING}. */
    @Bean
    public TestPushAdapter tileExpiringAdapter() {
        return new TestPushAdapter(base(EXPIRING, EXPIRING_KIND).recordTtl(Duration.ofMinutes(10)).build());
    }

    private static SourceDescriptor.Builder base(String id, String kind) {
        return SourceDescriptor.builder(id)
                .name("Tile test source " + id)
                .attribution("Attribution of " + id)
                .kind(kind)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }
}
