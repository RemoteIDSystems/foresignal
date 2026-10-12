package ai.foresignal.testsupport.place;

import ai.foresignal.notify.SystemEvent;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

/**
 * The places, views and watchlists test context (prompt 036): the trigger sources of
 * {@link TriggerTestSources}, plus
 *
 * <ul>
 *   <li>{@value #AIR_A}, {@value #AIR_B}, {@value #AIR_C}: aircraft sources that declare the
 *       {@code icao24} identity in three different payload fields ({@code hex}, {@code icao},
 *       {@code addr}) and {@code callsign} in {@code flight};
 *   <li>{@value #AIR_NONE}: an aircraft source without {@code entityKeys} (never matches a watchlist);
 *   <li>{@value #PHONE}: the moving anchor ("my phone"), kind {@value #PHONE_KIND};
 *   <li>{@value #EVENTS}: point events (quakes) and {@value #AREAS}: warning polygons, the targets of
 *       near rules.
 * </ul>
 *
 * <p>All are PUSH sources with the default ALL/STANDARD policy, so every content change persists a
 * version. Every 036 integration test imports this one configuration, so they share one context.
 */
@TestConfiguration
@Import(TriggerTestSources.class)
public class PlaceTestSources {

    /** Aircraft source A ({@code icao24} in {@code hex}). */
    public static final String AIR_A = "test-air-a";

    /** Aircraft source B ({@code icao24} in {@code icao}). */
    public static final String AIR_B = "test-air-b";

    /** Aircraft source C ({@code icao24} in {@code addr}). */
    public static final String AIR_C = "test-air-c";

    /** An aircraft source without identities. */
    public static final String AIR_NONE = "test-air-none";

    /** The aircraft kind. */
    public static final String AIRCRAFT = "test-aircraft";

    /** The anchor source. */
    public static final String PHONE = "test-phone";

    /** The anchor kind. */
    public static final String PHONE_KIND = "test-position";

    /** Point events (quakes). */
    public static final String EVENTS = "test-place-events";

    /** The event kind. */
    public static final String EVENT_KIND = "test-quake-event";

    /** Warning polygons. */
    public static final String AREAS = "test-place-areas";

    /** The warning kind. */
    public static final String AREA_KIND = "test-warning";

    /** {@value #AIR_A}. */
    @Bean
    public TestPushAdapter testAirAAdapter() {
        return new TestPushAdapter(base(AIR_A, AIRCRAFT).entityKey("icao24", "hex").entityKey("callsign", "flight")
                .build());
    }

    /** {@value #AIR_B}. */
    @Bean
    public TestPushAdapter testAirBAdapter() {
        return new TestPushAdapter(base(AIR_B, AIRCRAFT).entityKey("icao24", "icao").build());
    }

    /** {@value #AIR_C}. */
    @Bean
    public TestPushAdapter testAirCAdapter() {
        return new TestPushAdapter(base(AIR_C, AIRCRAFT).entityKey("icao24", "addr").build());
    }

    /** {@value #AIR_NONE}. */
    @Bean
    public TestPushAdapter testAirNoneAdapter() {
        return new TestPushAdapter(base(AIR_NONE, AIRCRAFT).build());
    }

    /** {@value #PHONE}. */
    @Bean
    public TestPushAdapter testPhoneAdapter() {
        return new TestPushAdapter(base(PHONE, PHONE_KIND).build());
    }

    /** {@value #EVENTS}. */
    @Bean
    public TestPushAdapter testPlaceEventsAdapter() {
        return new TestPushAdapter(base(EVENTS, EVENT_KIND).build());
    }

    /** {@value #AREAS}. */
    @Bean
    public TestPushAdapter testPlaceAreasAdapter() {
        return new TestPushAdapter(base(AREAS, AREA_KIND).build());
    }

    /** Records every {@link SystemEvent}. */
    @Bean
    public SystemEvents systemEvents() {
        return new SystemEvents();
    }

    /** Collects system events for assertions; {@link #clear} between tests. */
    public static final class SystemEvents {

        private final List<SystemEvent> events = new CopyOnWriteArrayList<>();

        /** Records one event. */
        @EventListener
        public void on(SystemEvent event) {
            events.add(event);
        }

        /** The events of {@code subtype} so far. */
        public List<SystemEvent> of(String subtype) {
            return events.stream().filter(e -> e.subtype().equals(subtype)).toList();
        }

        /** Forgets them. */
        public void clear() {
            events.clear();
        }
    }

    private static SourceDescriptor.Builder base(String id, String kind) {
        return SourceDescriptor.builder(id)
                .name("Place test source " + id)
                .attribution("Attribution of " + id)
                .kind(kind)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }
}
