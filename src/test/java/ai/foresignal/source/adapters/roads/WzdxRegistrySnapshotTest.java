package ai.foresignal.source.adapters.roads;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.SourceAdapter;
import java.net.URI;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The vendored WZDx Feed Registry snapshot (prompt 074): no URL carries a token or credential-looking value, ids are
 * unique, every key-required feed declares its credential with a documented placement, and every feed is a 4.x
 * GeoJSON feed that registers disabled with a valid descriptor.
 */
class WzdxRegistrySnapshotTest {

    static final Pattern LONG_TOKEN = Pattern.compile("[A-Za-z0-9_\\-]{24,}");

    @Test
    @DisplayName("31 feeds from the 2026-10-13 snapshot of 43 registry rows")
    void snapshot() {
        assertThat(WzdxRegistry.snapshotDate()).isEqualTo("2026-10-13");
        List<WzdxFeed> feeds = WzdxRegistry.load();
        assertThat(feeds).hasSize(31);
        assertThat(WzdxRegistry.problems(feeds)).isEmpty();
        assertThat(feeds).extracting(WzdxFeed::feedname).contains("wsdot", "mndot", "udot", "txdot_v4_2")
                .doesNotContain("odot", "fldot", "quebec", "cdot_cwz", "modot", "wisdot", "michigandot", "ordot",
                        "mdot_4");
    }

    @Test
    @DisplayName("no snapshot URL contains a token, a credential parameter or user info")
    void noTokens() {
        for (WzdxFeed f : WzdxRegistry.load()) {
            URI url = f.url();
            assertThat(url.getScheme()).as(f.id()).isEqualTo("https");
            assertThat(url.getRawUserInfo()).as(f.id()).isNull();
            assertThat(KitUrls.problems(f.id(), url.toString(), true)).as(f.id()).isEmpty();
            String query = url.getRawQuery() == null ? "" : url.getRawQuery();
            for (String pair : query.split("&")) {
                if (pair.isEmpty()) {
                    continue;
                }
                String name = pair.contains("=") ? pair.substring(0, pair.indexOf('=')) : pair;
                String value = pair.contains("=") ? pair.substring(pair.indexOf('=') + 1) : "";
                assertThat(KitUrls.CREDENTIAL_PARAMS).as(f.id() + " query " + name)
                        .doesNotContain(name.toLowerCase(Locale.ROOT));
                assertThat(LONG_TOKEN.matcher(value).matches()).as(f.id() + " value of " + name).isFalse();
            }
        }
    }

    @Test
    @DisplayName("ids are unique wzdx-<feedname> in lower-kebab case")
    void uniqueIds() {
        Set<String> ids = new HashSet<>();
        for (WzdxFeed f : WzdxRegistry.load()) {
            assertThat(ids.add(f.id())).as("duplicate %s", f.id()).isTrue();
            assertThat(f.id()).matches("^wzdx-[a-z0-9]+(-[a-z0-9]+)*$");
        }
        assertThat(ids).contains("wzdx-mndot", "wzdx-wsdot", "wzdx-txdot-v4-2", "wzdx-civiclink-crewcast");
    }

    @Test
    @DisplayName("key-required feeds declare a required apiKey credential with a documented placement")
    void keyedFeeds() {
        List<String> keyed = WzdxRegistry.load().stream().filter(f -> f.apiKey() != null).map(WzdxFeed::feedname)
                .sorted().toList();
        assertThat(keyed).containsExactly("cdot", "iltollway", "mtc", "nps", "ohio", "ptc", "txdot_v4_2", "vdot");
        WzdxFamily family = new WzdxFamily("", () -> null);
        for (SourceAdapter a : family.adapters()) {
            WzdxFeed feed = ((WzdxAdapter) a).feed();
            if (feed.apiKey() == null) {
                assertThat(a.descriptor().requiresCredentials()).as(a.id()).isFalse();
                continue;
            }
            assertThat(feed.apiKey().placement()).as(a.id()).isIn("query", "header");
            assertThat(feed.apiKey().name()).as(a.id()).isNotBlank();
            assertThat(a.descriptor().requiredCredentialFields()).extracting(CredentialField::name).as(a.id())
                    .containsExactly(WzdxFeed.CREDENTIAL);
        }
    }

    @Test
    @DisplayName("every feed: version 4.x, interval clamped to [5, 30] min, valid descriptor, attribution")
    void descriptors() {
        WzdxFamily family = new WzdxFamily("", () -> null);
        assertThat(family.adapters()).hasSize(31);
        for (SourceAdapter a : family.adapters()) {
            WzdxFeed feed = ((WzdxAdapter) a).feed();
            assertThat(feed.version()).as(a.id()).startsWith("4");
            Duration interval = a.descriptor().defaultInterval();
            assertThat(interval).as(a.id()).isBetween(Duration.ofMinutes(5), Duration.ofMinutes(30));
            assertThat(a.descriptor().attribution()).as(a.id()).isEqualTo(feed.issuingOrganization());
            assertThat(a.descriptor().kinds()).containsExactly("road-event");
            assertThat(DescriptorValidator.problems(a.descriptor(), a)).as(a.id()).isEmpty();
        }
    }

    @Test
    @DisplayName("update frequencies: seconds, minutes and hours are read and clamped")
    void frequencies() {
        assertThat(feed("60s").defaultInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(feed("1m").defaultInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(feed("10m").defaultInterval()).isEqualTo(Duration.ofMinutes(10));
        assertThat(feed("15m").defaultInterval()).isEqualTo(Duration.ofMinutes(15));
        assertThat(feed("72h").defaultInterval()).isEqualTo(Duration.ofMinutes(30));
        assertThat(feed("weekly").frequency()).isNull();
        assertThat(feed("weekly").defaultInterval()).isEqualTo(Duration.ofMinutes(30));
        assertThat(WzdxRegistry.problems(List.of(feed("weekly")))).anyMatch(p -> p.contains("unreadable"));
    }

    static WzdxFeed feed(String frequency) {
        return new WzdxFeed("test_feed", "Test DOT", "test", "4.2", frequency, URI.create("https://example.org/wzdx"),
                null, null);
    }
}
