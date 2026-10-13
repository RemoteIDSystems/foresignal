package ai.foresignal.source.adapters.gbfs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.IngestPolicyValidator;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The reviewed GBFS system list and the {@code gbfs-*} descriptors (prompt 071): every URL host and path is
 * allowlisted, ids are unique, the list validates, and every registered source passes registry validation with
 * the LATEST keyframe constraint {@code 1200 ≤ 1800 − 60 − 30}.
 */
class GbfsSystemsTest {

    @Test
    @DisplayName("every URL host is allowlisted and every path ends in station_information.json or station_status.json")
    void allowlisted() {
        assertThat(GbfsSystems.REVIEWED).hasSize(30);
        for (GbfsSystem s : GbfsSystems.REVIEWED) {
            assertThat(GbfsSystems.isAllowed(s.stationInformationUrl())).as(s.id()).isTrue();
            assertThat(GbfsSystems.isAllowed(s.stationStatusUrl())).as(s.id()).isTrue();
            assertThat(GbfsSystems.isAllowedHost(s.stationInformationUrl().getHost())).isTrue();
            assertThat(s.stationInformationUrl().getPath()).endsWith("/station_information.json");
            assertThat(s.stationStatusUrl().getPath()).endsWith("/station_status.json");
            assertThat(GbfsSystems.isAllowedPath(s.stationStatusUrl().getPath())).isTrue();
        }
        assertThat(GbfsSystems.ALLOWED_HOSTS).contains("gbfs.lyft.com", "gbfs.bcycle.com", "gbfs.bluebikes.com",
                "gbfs.biketownpdx.com", "austin.publicbikesystem.net", "hon.publicbikesystem.net",
                "chat.publicbikesystem.net", "tucson.publicbikesystem.net", "fortworth.publicbikesystem.net",
                "reddybikeshare.socialbicycles.com").hasSize(10);
    }

    @Test
    @DisplayName("the allowlist refuses other hosts, paths, schemes, queries and GEV's publicbikesystem.net wildcard")
    void refusals() {
        assertThat(GbfsSystems.isAllowedHost("evil.example")).isFalse();
        assertThat(GbfsSystems.isAllowedHost("other.publicbikesystem.net")).as("no wildcard").isFalse();
        assertThat(GbfsSystems.isAllowedHost(" GBFS.LYFT.COM ")).isTrue();
        assertThat(GbfsSystems.isAllowedHost(null)).isFalse();
        assertThat(GbfsSystems.isAllowedPath("/gbfs/2.3/bkn/gbfs.json")).isFalse();
        assertThat(GbfsSystems.isAllowedPath("/gbfs/2.3/bkn/en/free_bike_status.json")).isFalse();
        assertThat(GbfsSystems.isAllowedPath("/x/STATION_STATUS.JSON")).isTrue();
        assertThat(GbfsSystems.isAllowedPath(null)).isFalse();
        assertThat(GbfsSystems.isAllowed(URI.create("http://gbfs.lyft.com/gbfs/2.3/bkn/en/station_status.json")))
                .isFalse();
        assertThat(GbfsSystems.isAllowed(URI.create("https://gbfs.lyft.com/gbfs/2.3/bkn/en/station_status.json?x=1")))
                .isFalse();
        assertThat(GbfsSystems.isAllowed(URI.create("https://gbfs.lyft.com:8443/gbfs/station_status.json"))).isFalse();
        assertThat(GbfsSystems.isAllowed(null)).isFalse();
    }

    @Test
    @DisplayName("ids are unique and kebab-case; the list validates; licenses and attributions are set")
    void listValid() {
        Set<String> ids = new HashSet<>();
        for (GbfsSystem s : GbfsSystems.REVIEWED) {
            assertThat(ids.add(s.id())).as(s.id()).isTrue();
            assertThat(s.sourceId()).isEqualTo("gbfs-" + s.id());
            assertThat(s.license()).isEqualTo(GbfsSystems.OPERATOR_TERMS);
            assertThat(s.licenseUrl().getScheme()).isEqualTo("https");
            assertThat(s.attribution()).contains(s.provider());
            assertThat(s.gbfsVersion()).isEqualTo(GbfsSystem.V2);
        }
        assertThat(GbfsSystems.problems(GbfsSystems.REVIEWED)).isEmpty();
        assertThat(GbfsSystems.reviewed("nyc-citibike").stationStatusUrl())
                .isEqualTo(URI.create("https://gbfs.lyft.com/gbfs/2.3/bkn/en/station_status.json"));
        assertThatThrownBy(() -> GbfsSystems.reviewed("nowhere")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("problems: duplicates, bad ids, wrong file names, http, queries, unknown versions")
    void problems() {
        GbfsSystem good = GbfsSystems.reviewed("boulder-bcycle");
        List<GbfsSystem> list = new ArrayList<>(List.of(good, good));
        list.add(new GbfsSystem("Bad_Id", "X", "Y", URI.create("https://gbfs.bcycle.com/x/station_status.json"),
                URI.create("https://gbfs.bcycle.com/x/station_information.json"), "4.0", "t",
                URI.create("http://example.org"), "a"));
        list.add(new GbfsSystem("q", "X", "Y", URI.create("http://gbfs.bcycle.com/x/station_information.json"),
                URI.create("https://gbfs.bcycle.com/x/station_status.json?k=1"), GbfsSystem.V3, " ",
                URI.create("https://example.org"), "a"));
        List<String> problems = GbfsSystems.problems(list);
        assertThat(problems).anyMatch(p -> p.contains("duplicate id"));
        assertThat(problems).anyMatch(p -> p.startsWith("Bad_Id: the id is not kebab-case"));
        assertThat(problems).anyMatch(p -> p.startsWith("Bad_Id:") && p.contains("station_information.json URL"));
        assertThat(problems).anyMatch(p -> p.startsWith("Bad_Id:") && p.contains("station_status.json URL"));
        assertThat(problems).anyMatch(p -> p.contains("unknown GBFS version 4.0"));
        assertThat(problems).anyMatch(p -> p.contains("license URL must be https"));
        assertThat(problems).anyMatch(p -> p.startsWith("q:") && p.contains("http://gbfs.bcycle.com"));
        assertThat(problems).anyMatch(p -> p.startsWith("q:") && p.contains("?k=1"));
        assertThat(problems).anyMatch(p -> p.startsWith("q:") && p.contains("are required"));
    }

    @Test
    @DisplayName("the BCycle URL builder")
    void bcycle() {
        assertThat(GbfsSystems.bcycleUrls("bcycle_boulder")).containsExactly(
                URI.create("https://gbfs.bcycle.com/bcycle_boulder/station_information.json"),
                URI.create("https://gbfs.bcycle.com/bcycle_boulder/station_status.json"));
        assertThat(GbfsSystems.reviewed("boulder-bcycle").stationInformationUrl())
                .isEqualTo(GbfsSystems.bcycleUrls("bcycle_boulder").get(0));
    }

    @Test
    @DisplayName("the family registers one source per system; every descriptor passes registry validation")
    void registryValidation() {
        GbfsFamily family = new GbfsFamily("", () -> null);
        assertThat(family.adapters()).hasSize(GbfsSystems.REVIEWED.size());
        for (SourceAdapter adapter : family.adapters()) {
            assertThat(adapter.id()).startsWith("gbfs-");
            assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).as(adapter.id()).isEmpty();
            SourceDescriptor d = adapter.descriptor();
            assertThat(IngestPolicyValidator.problems(d.defaultIngestPolicy(), d, "policy")).as(adapter.id()).isEmpty();
            GbfsSystem system = ((GbfsAdapter) adapter).system();
            assertThat(GbfsSystems.isAllowed(system.stationInformationUrl())).isTrue();
        }
    }

    @Test
    @DisplayName("the descriptor: 60 s (min 30 s), 3/min and 2000/day, 5 MB, 15 s, no redirects, STANDARD + LATEST")
    void descriptor() {
        SourceDescriptor d = GbfsDescriptors.descriptor(GbfsSystems.reviewed("nyc-citibike"));
        assertThat(d.id()).isEqualTo("gbfs-nyc-citibike");
        assertThat(d.kinds()).containsExactly("bikeshare-station");
        assertThat(d.mode()).isEqualTo(Mode.POLLING);
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofSeconds(60));
        assertThat(d.minInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(3);
        assertThat(d.defaultBudget().perDay()).isEqualTo(2000);
        assertThat(d.followRedirects()).isFalse();
        assertThat(d.maxResponseBytes()).isEqualTo(5 * 1024 * 1024);
        assertThat(d.fetchTimeout()).isEqualTo(Duration.ofSeconds(15));
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.RECEIVED);
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(30));
        assertThat(d.defaultIngestPolicy()).isEqualTo(new IngestPolicy.Latest(Duration.ofMinutes(20),
                Duration.ofSeconds(30)));
        assertThat(d.significantFields()).containsExactly("name", "capacity", "isInstalled", "isRenting",
                "isReturning", "availability");
        assertThat(d.titleField()).isEqualTo("name");
        assertThat(d.searchFields()).containsExactly("name", "address");
        assertThat(d.tileProperties()).containsExactly("bikesAvailable", "docksAvailable", "availability",
                "ebikesAvailable");
        assertThat(d.zeroResultsNormal()).isFalse();
        assertThat(d.licence()).isEqualTo("operator terms");
        assertThat(d.termsUrl()).isEqualTo("https://citibikenyc.com/data-sharing-policy");
        assertThat(d.defaultRetention()).isEqualTo(Duration.ofDays(365));
        assertThat(d.fieldSpecs()).containsKeys("name", "address", "capacity", "regionId", "bikesAvailable",
                "ebikesAvailable", "docksAvailable", "disabledBikes", "disabledDocks", "isInstalled", "isRenting",
                "isReturning", "availability", "system");
        // The keyframe constraint: keyframe <= ttl - interval - 30 s.
        assertThat(GbfsDescriptors.KEYFRAME.getSeconds())
                .isLessThanOrEqualTo(GbfsDescriptors.TTL.minus(GbfsDescriptors.INTERVAL).minusSeconds(30).getSeconds());
    }

    @Test
    @DisplayName("the language parameter must be a BCP 47 tag")
    void languageParam() {
        GbfsAdapter adapter = new GbfsAdapter(GbfsSystems.reviewed("austin-capmetro"), null);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("language", "fr-CA")))).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of()))).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("language", "../etc"))))
                .containsKey(GbfsDescriptors.LANGUAGE_PARAM);
    }

    @Test
    @DisplayName("test rebasing keeps systems that share a path on different hosts distinct")
    void rebasing() {
        GbfsFamily family = new GbfsFamily("http://localhost:9999/", () -> null);
        Set<URI> urls = new HashSet<>();
        for (SourceAdapter adapter : family.adapters()) {
            GbfsSystem s = ((GbfsAdapter) adapter).system();
            assertThat(s.stationStatusUrl().toString()).startsWith("http://localhost:9999/");
            assertThat(urls.add(s.stationStatusUrl())).as(s.id()).isTrue();
        }
        assertThat(((GbfsAdapter) family.adapters().get(8)).system().stationStatusUrl()).isEqualTo(URI.create(
                "http://localhost:9999/austin.publicbikesystem.net/customer/gbfs/v2/en/station_status.json"));
        assertThat(GbfsSystem.rebase(URI.create("https://a.example/x.json"), " ")).isEqualTo(
                URI.create("https://a.example/x.json"));
    }
}
