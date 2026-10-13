package ai.foresignal.source.adapters.transit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.testsupport.net.TestOutbound;
import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link MobilityCatalog} and the catalog part of {@link TransitFamily} (prompt 070): the vendored snapshot loads, the
 * filters, {@code foresignal.transit.catalog-feeds} registers only vendored URLs, and a URL failing the outbound URL
 * policy is skipped.
 */
class MobilityCatalogTest {

    static final String TEST_CATALOG = "transit/test-catalog.csv";

    private static MobilityCatalog testCatalog() {
        return new MobilityCatalog(TEST_CATALOG);
    }

    @Test
    @DisplayName("the vendored snapshot loads: active keyless https GTFS-RT rows with known entity types")
    void vendoredSnapshot() {
        MobilityCatalog catalog = new MobilityCatalog("");

        assertThat(catalog.all()).hasSizeGreaterThan(1000);
        assertThat(catalog.all()).allSatisfy(e -> {
            assertThat(e.directDownloadUrl().getScheme()).isEqualTo("https");
            assertThat(e.provider()).isNotBlank();
            assertThat(MobilityCatalog.ENTITY_TYPES).containsAll(e.entityTypes());
        });
        MobilityCatalog.Entry mbta = catalog.get("mdb-1602").orElseThrow();
        assertThat(mbta.directDownloadUrl()).isEqualTo(URI.create("https://cdn.mbta.com/realtime/VehiclePositions.pb"));
        assertThat(mbta.countryCode()).isEqualTo("US");
        assertThat(mbta.entityTypes()).containsExactly("vp");
        assertThat(catalog.search("us", null, "vp")).isNotEmpty()
                .allSatisfy(e -> assertThat(e.countryCode()).isEqualTo("US"));
    }

    @Test
    @DisplayName("rows without a provider or an https URL are skipped; entity types are split on |")
    void load() {
        MobilityCatalog catalog = testCatalog();

        assertThat(catalog.all()).extracting(MobilityCatalog.Entry::id).containsExactly("mdb-9001", "mdb-9002",
                "mdb-9003", "mdb-9004", "mdb-9005", "mdb-9006", "odpt-Some_City-AllLines-vp");
        MobilityCatalog.Entry combined = catalog.get("mdb-9003").orElseThrow();
        assertThat(combined.entityTypes()).containsExactly("sa", "tu", "vp");
        assertThat(combined.entityType()).isEqualTo("sa|tu|vp");
        assertThat(combined.staticReference()).isNull();
        assertThat(catalog.get("mdb-9002").orElseThrow().licenseUrl()).isNull();
        assertThat(catalog.get("mdb-9006").orElseThrow().entityTypes()).isEmpty();
        assertThat(catalog.get("nope")).isEmpty();
        assertThat(catalog.get(null)).isEmpty();
    }

    @Test
    @DisplayName("search filters by country, provider or id substring and entity type, together")
    void filters() {
        MobilityCatalog catalog = testCatalog();

        assertThat(catalog.search("fr", null, null)).extracting(MobilityCatalog.Entry::id).containsExactly("mdb-9003");
        assertThat(catalog.search(null, "EXAMPLE transit", null)).extracting(MobilityCatalog.Entry::id)
                .containsExactly("mdb-9001", "mdb-9002");
        assertThat(catalog.search(null, "9004", null)).extracting(MobilityCatalog.Entry::id).containsExactly("mdb-9004");
        assertThat(catalog.search(null, null, "vp")).extracting(MobilityCatalog.Entry::id)
                .containsExactly("mdb-9001", "mdb-9003", "mdb-9005", "odpt-Some_City-AllLines-vp");
        assertThat(catalog.search("US", "example", "sa")).extracting(MobilityCatalog.Entry::id).containsExactly("mdb-9002");
        assertThat(catalog.search(" ", "", null)).hasSize(7);
    }

    @Test
    @DisplayName("a missing snapshot fails startup")
    void missing() {
        assertThatThrownBy(() -> new MobilityCatalog("transit/does-not-exist.csv"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("missing");
    }

    @Test
    @DisplayName("catalog-feeds registers only vendored URLs; unknown ids, trip updates and policy failures are skipped")
    void catalogFeeds() {
        TransitFamily family = new TransitFamily("mdb-9001, mdb-9002,mdb-9003,mdb-9004,mdb-9005,mdb-9006,nope,mdb-9001,"
                + "odpt-Some_City-AllLines-vp", "", testCatalog(), TestOutbound.offlinePolicy(), () -> "abcd1234", null);

        assertThat(family.enabledCatalogFeeds()).containsOnlyKeys("mdb-9001", "mdb-9002", "mdb-9003",
                "odpt-Some_City-AllLines-vp");
        assertThat(family.enabledCatalogFeeds().get("mdb-9001")).containsExactly("transit-mdb-mdb-9001");
        assertThat(family.enabledCatalogFeeds().get("mdb-9002")).containsExactly("transit-alerts-mdb-mdb-9002");
        assertThat(family.enabledCatalogFeeds().get("mdb-9003"))
                .containsExactly("transit-mdb-mdb-9003", "transit-alerts-mdb-mdb-9003");
        List<SourceAdapter> catalogAdapters = family.adapters().stream()
                .filter(a -> a.id().contains("-mdb-")).toList();
        assertThat(catalogAdapters).hasSize(5);
        for (SourceAdapter adapter : catalogAdapters) {
            TransitFeed feed = adapter instanceof TransitVehicleAdapter v ? v.feed() : ((TransitAlertsAdapter) adapter).feed();
            assertThat(feed.catalog()).isTrue();
            assertThat(feed.timeBase()).isEqualTo(TimeBase.HEADER);
            assertThat(feed.headers()).isEmpty();
            URI url = feed.vehiclePositionsUrl() != null ? feed.vehiclePositionsUrl() : feed.alertsUrl();
            assertThat(testCatalog().all()).anySatisfy(e -> assertThat(e.directDownloadUrl()).isEqualTo(url));
            assertThat(adapter.descriptor().id()).hasSizeLessThanOrEqualTo(64);
            assertThat(ai.foresignal.source.DescriptorValidator.problems(adapter.descriptor(), adapter))
                    .as(adapter.id()).isEmpty();
        }
        TransitVehicleAdapter known = (TransitVehicleAdapter) catalogAdapters.get(0);
        assertThat(known.descriptor().licence()).isEqualTo("https://feeds.example/license");
        TransitAlertsAdapter unknownLicense = (TransitAlertsAdapter) family.adapters().stream()
                .filter(a -> a.id().equals("transit-alerts-mdb-mdb-9002")).findFirst().orElseThrow();
        assertThat(unknownLicense.descriptor().licence()).isEqualTo(TransitFamily.UNKNOWN_LICENSE);
    }

    @Test
    @DisplayName("a vendored URL that fails the outbound URL policy is skipped; an unresolvable host is accepted")
    void policy() {
        OutboundUrlPolicy privateAnswer = OutboundUrlPolicy.withoutOverrides(host -> new InetAddress[] {
            InetAddress.getByAddress(host, new byte[] {10, 0, 0, 7})});
        TransitFamily refused = new TransitFamily("mdb-9001", "", testCatalog(), privateAnswer, () -> "x", null);
        assertThat(refused.enabledCatalogFeeds()).isEmpty();

        TransitFamily literal = new TransitFamily("mdb-9005", "", testCatalog(), TestOutbound.offlinePolicy(), () -> "x",
                null);
        assertThat(literal.enabledCatalogFeeds()).isEmpty();

        TransitFamily offline = new TransitFamily("mdb-9001", "", testCatalog(), TestOutbound.offlinePolicy(), () -> "x",
                null);
        assertThat(offline.enabledCatalogFeeds()).containsOnlyKeys("mdb-9001");
    }

    @Test
    @DisplayName("catalog ids become source-id parts: lower case, hyphens, a hash suffix past 64 characters")
    void slugs() {
        assertThat(TransitFamily.slug("mdb-1602")).isEqualTo("mdb-1602");
        assertThat(TransitFamily.slug("jbda-nagai-unyu-Nagaibus-sa")).isEqualTo("jbda-nagai-unyu-nagaibus-sa");
        assertThat(TransitFamily.slug("jbda-naganocity-shiei_noriaibus-vp")).isEqualTo("jbda-naganocity-shiei-noriaibus-vp");
        String longId = "odpt-NagoyaHousingCityPlanningBureau-NagoyaSRT_AllLines-sa";
        String slug = TransitFamily.slug(longId);
        assertThat(("transit-alerts-mdb-" + slug).length()).isLessThanOrEqualTo(64);
        assertThat(slug).matches("^[a-z0-9]+(-[a-z0-9]+)*-[0-9a-f]{8}$");
        assertThat(TransitFamily.slug(longId)).isEqualTo(slug);
        assertThat(TransitFamily.slug(longId.replace("-sa", "-vp"))).isNotEqualTo(slug);
        assertThat(TransitFamily.parseIds(" a, ,b,a ")).containsExactly("a", "b");
        assertThat(TransitFamily.parseIds(null)).isEmpty();
    }
}
