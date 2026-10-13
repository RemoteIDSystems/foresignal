package ai.foresignal.source.adapters.ct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link CertSpotterParser} on a real Cert Spotter page (captured 2026-10-13 for {@code example.com}, 13 issuances)
 * and {@link DomainNames}: {@code unexpectedIssuer} by friendly name or CAA domain, the upstream-asserted expiry and
 * the domain rules.
 */
class CertSpotterParserTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static SourceDescriptor descriptor() {
        return new CtDomainWatchAdapter("", Duration.ZERO, d -> { }).descriptor();
    }

    static byte[] page() {
        return AdapterFixtures.bytes("ct-domain-watch", "issuances.json");
    }

    @Test
    @DisplayName("the descriptor: RECEIVED time base, 1 h (min 15 min), 2/min and 48/day, domains required, no crt.sh")
    void descriptorShape() {
        SourceDescriptor d = descriptor();
        assertThat(DescriptorValidator.problems(d, new CtDomainWatchAdapter("", Duration.ZERO, x -> { }))).isEmpty();
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofHours(1));
        assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(15));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(2);
        assertThat(d.defaultBudget().perDay()).isEqualTo(48);
        assertThat(d.param("domains").required()).isTrue();
        assertThat(d.termsUrl()).isNotBlank();
        assertThat(d.requiresCredentials()).isFalse();
        assertThat(CtDomainWatchAdapter.API_URL).startsWith("https://api.certspotter.com/").doesNotContain("crt.sh");
    }

    @Test
    @DisplayName("the fixture: 13 issuances, the last id as cursor, validUntil = not_after, eventTime = not_before")
    @SuppressWarnings("unchecked")
    void fixture() {
        CertSpotterParser.Page parsed = CertSpotterParser.parse(page(), "example.com", List.of());
        assertThat(parsed.count()).isEqualTo(13);
        assertThat(parsed.records()).hasSize(13);
        assertThat(parsed.lastId()).isEqualTo("17433446883");
        assertThat(parsed.skipped()).isEmpty();
        NormalizedRecord first = parsed.records().getFirst();
        assertThat(first.externalId()).isEqualTo("12809115180");
        assertThat(first.kind()).isEqualTo("certificate-issuance");
        assertThat(first.observedAt()).as("RECEIVED: stamped at receipt").isNull();
        assertThat(first.eventTime()).isEqualTo(Instant.parse("2025-11-20T00:00:00Z"));
        assertThat(first.validUntil()).isEqualTo(Instant.parse("2026-11-20T23:59:59Z"));
        assertThat(first.payload()).containsEntry("watchedDomain", "example.com")
                .containsEntry("dnsNames", List.of("example.com", "example.edu", "example.net", "example.org"))
                .containsEntry("issuerName", "Sectigo").containsEntry("issuerOperator", "Sectigo")
                .containsEntry("revoked", true).containsEntry("unexpectedIssuer", false)
                .containsEntry("certSha256", "4973b48cf244e4981d43ff0485c347d50a22f0b827f9d0c96f9233a9e7646bf5")
                .containsEntry("pubkeySha256", "54d18757bf715a84566fb9722785e9f78109c2a1e66a2c63ab2c57d94f41b627")
                .containsEntry("notBefore", "2025-11-20T00:00:00Z").containsEntry("notAfter", "2026-11-20T23:59:59Z");
        assertThat((List<Object>) first.payload().get("issuerCaaDomains")).contains("sectigo.com", "comodoca.com");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), parsed.records());
    }

    @Test
    @DisplayName("unexpectedIssuer: by friendly name, by CAA domain, case-insensitive; false without expectations")
    void unexpectedIssuer() {
        List<NormalizedRecord> byName = CertSpotterParser.parse(page(), "example.com", List.of("sectigo")).records();
        assertThat(byName).filteredOn(r -> Boolean.TRUE.equals(r.payload().get("unexpectedIssuer")))
                .hasSize(8).allSatisfy(r -> assertThat(r.payload()).containsEntry("issuerName", "SSL.com"));
        List<NormalizedRecord> byCaa = CertSpotterParser.parse(page(), "example.com", List.of("SSL.COM",
                "usertrust.com")).records();
        assertThat(byCaa).noneMatch(r -> Boolean.TRUE.equals(r.payload().get("unexpectedIssuer")));
        List<NormalizedRecord> other = CertSpotterParser.parse(page(), "example.com", List.of("letsencrypt.org"))
                .records();
        assertThat(other).allMatch(r -> Boolean.TRUE.equals(r.payload().get("unexpectedIssuer")));
        assertThat(CertSpotterParser.unexpected(null, null, Set.of("x"))).as("no issuer data").isTrue();
        assertThat(CertSpotterParser.unexpected(null, null, Set.of())).isFalse();
    }

    @Test
    @DisplayName("an empty page has no cursor; an error object (rate_limited) is a format error")
    void emptyAndErrors() {
        CertSpotterParser.Page empty = CertSpotterParser.parse(AdapterFixtures.bytes("ct-domain-watch",
                "issuances-empty.json"), "foresignal.ai", List.of());
        assertThat(empty.count()).isZero();
        assertThat(empty.lastId()).isNull();
        assertThatThrownBy(() -> CertSpotterParser.parse(("{\"code\": \"rate_limited\", \"message\": \"slow down\"}")
                .getBytes(StandardCharsets.UTF_8), "example.com", List.of()))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("rate_limited");
    }

    @Test
    @DisplayName("domains: IDNA-normalized, lower case, no wildcards, at least two labels, at most 253 characters")
    void domainNames() {
        assertThat(DomainNames.normalize(" Example.COM. ")).contains("example.com");
        assertThat(DomainNames.normalize("bücher.example")).contains("xn--bcher-kva.example");
        assertThat(DomainNames.normalize("*.example.com")).isEmpty();
        assertThat(DomainNames.normalize("com")).isEmpty();
        assertThat(DomainNames.normalize("192.0.2.1")).isEmpty();
        assertThat(DomainNames.normalize("-bad.example.com")).isEmpty();
        assertThat(DomainNames.normalize("a..example.com")).isEmpty();
        assertThat(DomainNames.normalize("https://example.com/")).isEmpty();
        assertThat(DomainNames.normalize(("a".repeat(60) + ".").repeat(5) + "com")).isEmpty();
        assertThat(DomainNames.normalize(null)).isEqualTo(Optional.empty());

        CtDomainWatchAdapter adapter = new CtDomainWatchAdapter("", Duration.ZERO, d -> { });
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("domains", List.of("example.com"))))).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("domains", List.of("*.example.com")))))
                .containsKey("domains");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of()))).containsKey("domains");
        List<String> many = java.util.stream.IntStream.range(0, 26).mapToObj(i -> "d" + i + ".example").toList();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("domains", many)))).containsKey("domains");
    }

    @Test
    @DisplayName("the same issuance seen again keeps its static hash")
    void snapshot() {
        JsonNode issuance = JSON.readTree(page()).get(1);
        SnapshotContract.assertStableStaticHash(descriptor(), List.of(
                CertSpotterParser.issuance(issuance, "example.com", Set.of()),
                CertSpotterParser.issuance(issuance.deepCopy(), "example.com", Set.of())));
    }
}
