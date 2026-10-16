package ai.foresignal.source.spi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SourceDescriptorBuilderTest {

    private static SourceDescriptor minimal() {
        return SourceDescriptor.builder("usgs-earthquakes")
                .kind("earthquake")
                .interval(Duration.ofSeconds(30), Duration.ofMinutes(1))
                .defaultBudget(60, 10_000)
                .build();
    }

    @Test
    @DisplayName("the documented defaults are applied")
    void appliesDocumentedDefaults() {
        SourceDescriptor descriptor = minimal();

        assertThat(descriptor.searchFields())
                .isEqualTo(SourceDescriptor.DEFAULT_SEARCH_FIELDS)
                .containsExactly("name", "title", "callsign", "place");
        assertThat(descriptor.zeroResultsNormal()).isFalse();
        assertThat(descriptor.fetchTimeout()).isEqualTo(Duration.ofSeconds(60));
        assertThat(descriptor.maxResponseBytes()).isEqualTo(32 * 1024 * 1024);
        assertThat(descriptor.mode()).isEqualTo(Mode.POLLING);
        assertThat(descriptor.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(descriptor.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(descriptor.skewTolerance()).isEqualTo(Duration.ofMinutes(2));
        assertThat(descriptor.limits()).isEqualTo(RecordLimits.defaults());
        assertThat(descriptor.allowNullIsland()).isFalse();
    }

    @Test
    @DisplayName("the name defaults to the id, so a descriptor is never nameless")
    void nameDefaultsToId() {
        assertThat(minimal().name()).isEqualTo("usgs-earthquakes");
    }

    @Test
    @DisplayName("collections default to empty rather than null")
    void collectionsDefaultEmpty() {
        SourceDescriptor descriptor = minimal();

        assertThat(descriptor.credentialFields()).isEmpty();
        assertThat(descriptor.params()).isEmpty();
        assertThat(descriptor.tileProperties()).isEmpty();
        assertThat(descriptor.embedFields()).isEmpty();
        assertThat(descriptor.volatileFields()).isEmpty();
        assertThat(descriptor.significantFields()).isEmpty();
        assertThat(descriptor.thresholds()).isEmpty();
        assertThat(descriptor.kindRetention()).isEmpty();
        assertThat(descriptor.products()).isEmpty();
    }

    @Test
    @DisplayName("the default ingest policy is ALL")
    void defaultPolicyIsAll() {
        assertThat(minimal().defaultIngestPolicy()).isInstanceOf(IngestPolicy.All.class);
        assertThat(minimal().defaultIngestPolicy().mode()).isEqualTo(IngestPolicy.PolicyMode.ALL);
    }

    @Test
    @DisplayName("STANDARD anchors its TTL on receivedAt, TRACK on observedAt")
    void ttlAnchorDependsOnTier() {
        assertThat(minimal().ttlAnchor()).isEqualTo(TtlAnchor.RECEIVED);

        SourceDescriptor track =
                SourceDescriptor.builder("adsblol-mil")
                        .kind("aircraft")
                        .historyTier(HistoryTier.TRACK)
                        .interval(Duration.ofSeconds(10), Duration.ofSeconds(60))
                        .defaultBudget(60, 10_000)
                        .recordTtl(Duration.ofMinutes(5))
                        .build();

        assertThat(track.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
        assertThat(track.isTrack()).isTrue();
    }

    @Test
    @DisplayName("an explicit anchor overrides the tier default")
    void explicitAnchorWins() {
        SourceDescriptor descriptor =
                SourceDescriptor.builder("openaq")
                        .kind("air-quality")
                        .ttlAnchor(TtlAnchor.OBSERVED)
                        .interval(Duration.ofMinutes(10), Duration.ofHours(1))
                        .defaultBudget(10, 100)
                        .build();

        assertThat(descriptor.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
    }

    @Test
    @DisplayName("searchFields can be replaced wholesale")
    void searchFieldsAreReplaceable() {
        SourceDescriptor descriptor =
                SourceDescriptor.builder("x").kind("k").defaultBudget(1, 1).searchFields("flight", "r").build();

        assertThat(descriptor.searchFields()).containsExactly("flight", "r");
    }

    @Test
    @DisplayName("a stream's effective poll interval is 2s, since it has none of its own")
    void streamPollIntervalIsTwoSeconds() {
        SourceDescriptor stream =
                SourceDescriptor.builder("aisstream")
                        .kind("vessel")
                        .mode(Mode.STREAMING)
                        .defaultBudget(10, 100)
                        .build();

        assertThat(stream.effectivePollInterval()).isEqualTo(Duration.ofSeconds(2));
        assertThat(minimal().effectivePollInterval()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    @DisplayName("required credential fields are reported separately from optional ones")
    void reportsRequiredCredentials() {
        SourceDescriptor descriptor =
                SourceDescriptor.builder("nasa-firms")
                        .kind("fire")
                        .defaultBudget(10, 100)
                        .credential(CredentialField.required("apiKey", "MAP_KEY from FIRMS"))
                        .credential(CredentialField.optional("referer", "optional referer"))
                        .build();

        assertThat(descriptor.requiresCredentials()).isTrue();
        assertThat(descriptor.requiredCredentialFields())
                .extracting(CredentialField::name)
                .containsExactly("apiKey");
    }

    @Test
    void reportsNoCredentialsWhenNoneRequired() {
        assertThat(minimal().requiresCredentials()).isFalse();
        assertThat(minimal().requiredCredentialFields()).isEmpty();
    }

    @Test
    void findsParamsByName() {
        SourceDescriptor descriptor =
                SourceDescriptor.builder("x")
                        .kind("k")
                        .defaultBudget(1, 1)
                        .param(ParamSpec.optional("feed", ParamType.STRING, "which feed", "all"))
                        .build();

        assertThat(descriptor.param("feed")).isNotNull();
        assertThat(descriptor.param("missing")).isNull();
    }

    @Test
    @DisplayName("descriptor collections are immutable once built")
    void collectionsAreImmutable() {
        SourceDescriptor descriptor = minimal();

        assertThatThrownBy(() -> descriptor.kinds().add("other"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> descriptor.searchFields().add("other"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("the builder keeps every field it was given")
    void keepsEverySuppliedField() {
        SourceDescriptor descriptor =
                SourceDescriptor.builder("aisstream")
                        .name("AISStream")
                        .description("Global AIS")
                        .kinds("vessel", "vessel-static")
                        .homepage("https://aisstream.io")
                        .attribution("AISStream")
                        .licence("CC-BY-4.0")
                        .termsUrl("https://aisstream.io/terms")
                        .mode(Mode.STREAMING)
                        .historyTier(HistoryTier.TRACK)
                        .recordTtl(Duration.ofMinutes(30))
                        .defaultBudget(10, 1_000)
                        .significantFields("name", "callsign")
                        .volatileFields("rawCounter")
                        .motion(new MotionSpec("sog", MotionSpec.SpeedUnit.KNOTS, "cog"))
                        .threshold(FieldThreshold.angular("cog", 15))
                        .tileProperties("name")
                        .embedFields("name")
                        .rankField("draught")
                        .skewTolerance(Duration.ofMinutes(5))
                        .allowNullIsland(true)
                        .fetchTimeout(Duration.ofSeconds(30))
                        .maxResponseBytes(1024)
                        .product(new ProductSpec("ais-density", "AIS density", ProductSpec.KIND_RASTER))
                        .build();

        assertThat(descriptor.name()).isEqualTo("AISStream");
        assertThat(descriptor.kinds()).containsExactlyInAnyOrder("vessel", "vessel-static");
        assertThat(descriptor.licence()).isEqualTo("CC-BY-4.0");
        assertThat(descriptor.significantFields()).containsExactly("name", "callsign");
        assertThat(descriptor.volatileFields()).containsExactly("rawCounter");
        assertThat(descriptor.motion().speedField()).isEqualTo("sog");
        assertThat(descriptor.thresholds()).hasSize(1);
        assertThat(descriptor.rankField()).isEqualTo("draught");
        assertThat(descriptor.allowNullIsland()).isTrue();
        assertThat(descriptor.maxResponseBytes()).isEqualTo(1024);
        assertThat(descriptor.products()).hasSize(1);
    }
    @Test
    @DisplayName("entity keys, prefixes and attributes keep their declaration order (prompt 098)")
    void entityMapsKeepDeclarationOrder() {
        SourceDescriptor descriptor = SourceDescriptor.builder("entity-order")
                .kind("aircraft")
                .interval(Duration.ofSeconds(30), Duration.ofMinutes(1))
                .defaultBudget(60, 10_000)
                .entityKey("registration", "r")
                .entityKey("icao24", "hex")
                .entityKey("callsign", "flight")
                .entityKeyPrefix("callsign", "air")
                .entityAttribute("t", "typeCode")
                .entityAttribute("desc", "model")
                .build();

        assertThat(descriptor.entityKeys().keySet()).containsExactly("registration", "icao24", "callsign");
        assertThat(descriptor.entityAttributes().keySet()).containsExactly("t", "desc");
        assertThat(descriptor.entityKeyPrefixes()).containsEntry("callsign", "air");
        assertThatThrownBy(() -> descriptor.entityKeys().put("mmsi", "mmsi"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
