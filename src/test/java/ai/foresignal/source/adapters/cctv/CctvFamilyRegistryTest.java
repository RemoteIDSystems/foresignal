package ai.foresignal.source.adapters.cctv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.IngestPolicyValidator;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The {@code cctv-*} family in the registry (prompt 072): every reviewed pack registers once, with license and terms,
 * disabled under the test profile; Ontario 511 cannot be enabled without its key (409
 * {@code CREDENTIALS_REQUIRED}); the reviewed list validates and its problems are reported.
 */
class CctvFamilyRegistryTest extends WeatherIngestSupport {

    @Autowired private SourceRegistry registry;

    @Test
    @DisplayName("every pack registers once with license and terms, STANDARD/ALL/RECEIVED, and is disabled under the"
            + " test profile")
    void registered() {
        assertThat(CctvPacks.REVIEWED).hasSize(24);
        Set<String> ids = new HashSet<>();
        for (CctvPack pack : CctvPacks.REVIEWED) {
            assertThat(ids.add(pack.id())).as(pack.id()).isTrue();
            assertThat(registry.contains(pack.id())).as(pack.id()).isTrue();
            SourceDescriptor d = registry.descriptor(pack.id());
            assertThat(d.licence()).as(pack.id()).isNotBlank();
            assertThat(d.termsUrl()).as(pack.id()).startsWith("https://");
            assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
            assertThat(d.defaultIngestPolicy()).isInstanceOf(IngestPolicy.All.class);
            assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
            assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.RECEIVED);
            assertThat(d.recordTtl()).isNull();
            assertThat(d.zeroResultsNormal()).isFalse();
            assertThat(d.significantFields()).isEmpty();
            assertThat(d.volatileFields()).isEmpty();
            assertThat(d.embedFields()).isEmpty();
            assertThat(d.searchFields()).containsExactly("name", "road", "region");
            assertThat(d.tileProperties()).containsExactly("name", "direction", "status", "frameAccess");
            assertThat(d.titleField()).isEqualTo("name");
            assertThat(d.defaultRetention()).isEqualTo(Duration.ofDays(90));
            assertThat(d.followRedirects()).isFalse();
            assertThat(d.param(CctvDescriptors.BBOX_PARAM)).isNotNull();
            assertThat(IngestPolicyValidator.problems(d.defaultIngestPolicy(), d, "policy")).as(pack.id()).isEmpty();
            SourceView view = sources.view(pack.id());
            // The test profile seeds every source disabled (prompt 146); in production the keyless packs start enabled.
            assertThat(view.enabled()).as(pack.id() + " disabled under the test profile").isFalse();
        }
        assertThat(ids).contains("cctv-tfl", "cctv-caltrans-d1", "cctv-caltrans-d12", "cctv-ontario511",
                "cctv-digitraffic", "cctv-drivebc", "cctv-nsw", "cctv-calgary", "cctv-austin", "cctv-deldot",
                "cctv-vegvesen", "cctv-estonia", "cctv-tallinn", "cctv-warendorf");
        assertThat(ids).noneMatch(id -> id.contains("txdot") || id.contains("shinjuku"));
    }

    @Test
    @DisplayName("defaults: 15 min (min 10 min), 2/min and 200/day; TfL 60 min; bundled 24 h; Caltrans 8 MB")
    void schedules() {
        SourceDescriptor caltrans = registry.descriptor("cctv-caltrans-d4");
        assertThat(caltrans.defaultInterval()).isEqualTo(Duration.ofMinutes(15));
        assertThat(caltrans.minInterval()).isEqualTo(Duration.ofMinutes(10));
        assertThat(caltrans.defaultBudget().perMinute()).isEqualTo(2);
        assertThat(caltrans.defaultBudget().perDay()).isEqualTo(200);
        assertThat(caltrans.maxResponseBytes()).isEqualTo(8 * 1024 * 1024);
        assertThat(registry.descriptor("cctv-tfl").defaultInterval()).isEqualTo(Duration.ofMinutes(60));
        assertThat(registry.descriptor("cctv-tallinn").defaultInterval()).isEqualTo(Duration.ofHours(24));
        assertThat(registry.descriptor("cctv-tfl").requiresCredentials()).isFalse();
        assertThat(registry.descriptor("cctv-ontario511").requiresCredentials()).isTrue();
    }

    @Test
    @DisplayName("Ontario 511 cannot be enabled without its key: 409 CREDENTIALS_REQUIRED")
    void ontarioNeedsKey() {
        sources.deleteCredentials("cctv-ontario511");
        assertThatThrownBy(() -> sources.update("cctv-ontario511", SourceSettingsUpdate.enabled(true)))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((ConflictException) e).getCode())
                        .isEqualTo(ConflictException.CREDENTIALS_REQUIRED))
                .hasMessageContaining("key");
        assertThat(sources.view("cctv-ontario511").enabled()).isFalse();
    }

    @Test
    @DisplayName("the reviewed list validates; invalid packs are reported")
    void problems() {
        assertThat(CctvPacks.problems(CctvPacks.REVIEWED)).isEmpty();
        List<CctvPack> bad = new ArrayList<>(List.of(CctvPacks.reviewed("cctv-tfl"), CctvPacks.reviewed("cctv-tfl")));
        bad.add(CctvPack.builder("cctv-bad", CctvJsonParsers::tfl).name("Bad").provider("x").license("x")
                .attribution("x").homepage("http://insecure.example").terms("https://x.example")
                .catalog("http://insecure.example/list").origins("https://unlisted.example/")
                .header("User-Agent", "Mozilla/5.0").build());
        bad.add(CctvPack.builder("Not-Kebab", CctvJsonParsers::tfl).build());
        List<String> problems = CctvPacks.problems(bad);
        assertThat(problems).anyMatch(p -> p.contains("duplicate id"));
        assertThat(problems).anyMatch(p -> p.contains("homepage and terms must be https"));
        assertThat(problems).anyMatch(p -> p.contains("must be https without user info"));
        assertThat(problems).anyMatch(p -> p.contains("not in cctv/hosts.json"));
        assertThat(problems).anyMatch(p -> p.contains("header User-Agent is not allowed"));
        assertThat(problems).anyMatch(p -> p.contains("needs a bounding box"));
        assertThat(problems).anyMatch(p -> p.contains("invalid id Not-Kebab"));
    }

    @Test
    @DisplayName("every allowed frame host has a frame-access hint")
    void frameHosts() {
        for (CctvPack pack : CctvPacks.REVIEWED) {
            for (String origin : pack.allowedFrameOrigins()) {
                String host = java.net.URI.create(origin).getHost();
                assertThat(CctvFrameHosts.access(host)).as(host).isIn(CctvFrameHosts.ACCESS_VALUES);
            }
        }
        assertThat(CctvFrameHosts.access("webcams.transport.nsw.gov.au")).isEqualTo("browser");
        assertThat(CctvFrameHosts.access("www.drivebc.ca")).isEqualTo("direct");
        assertThat(CctvFrameHosts.access("unknown.example")).isNull();
        assertThat(CctvFrameHosts.access(null)).isNull();
    }
}
