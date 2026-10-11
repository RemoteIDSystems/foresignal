package ai.foresignal.testsupport.adapters;

import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import java.util.List;

/**
 * A family of three feeds, standing in for the real multi-feed parsers (GTFS-RT agencies, GBFS
 * systems, adsb.lol regions).
 *
 * <p>Its feed list is static, as every real family's must be: a family that took its feeds from a
 * request would make Foresignal an open HTTP proxy.
 */
public class FakeFamily implements SourceAdapterFamily {

    /** The feed ids this family registers. */
    public static final List<String> FEED_IDS =
            List.of("fake-family-alpha", "fake-family-bravo", "fake-family-charlie");

    private final List<SourceAdapter> adapters;

    public FakeFamily() {
        this.adapters = FEED_IDS.stream().map(id -> (SourceAdapter) FakePollingAdapter.withId(id)).toList();
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }

    /** The adapter for one feed, so a test can script it. */
    public FakePollingAdapter feed(String id) {
        return adapters.stream()
                .filter(adapter -> adapter.id().equals(id))
                .map(FakePollingAdapter.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such feed: " + id));
    }
}
