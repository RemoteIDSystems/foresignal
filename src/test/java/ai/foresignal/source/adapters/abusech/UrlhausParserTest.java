package ai.foresignal.source.adapters.abusech;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link UrlhausParser} on a synthetic response built from the URLhaus API documentation (no key during
 * construction): defanging, the {@code noLink} hint on the URL fields, and the documented query statuses.
 */
class UrlhausParserTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static SourceDescriptor descriptor() {
        return new UrlhausAdapter("").descriptor();
    }

    @Test
    @DisplayName("the descriptor: the authKey credential is required, noLink on url and urlDefanged, 30 d TTL")
    void descriptorShape() {
        SourceDescriptor d = descriptor();
        assertThat(DescriptorValidator.problems(d, new UrlhausAdapter(""))).isEmpty();
        assertThat(d.requiresCredentials()).isTrue();
        assertThat(d.requiredCredentialFields()).extracting(f -> f.name()).containsExactly("authKey");
        assertThat(d.fieldSpecs().get("url").noLink()).isTrue();
        assertThat(d.fieldSpecs().get("urlDefanged").noLink()).isTrue();
        assertThat(d.fieldSpecs().get("reference").noLink()).isFalse();
        assertThat(d.recordTtl()).isEqualTo(Duration.ofDays(30));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(15));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(2);
        assertThat(d.defaultBudget().perDay()).isEqualTo(200);
        assertThat(d.licence()).contains("Spamhaus").contains("commercial");
        assertThat(d.searchFields()).doesNotContain("url");
        assertThat(new FieldSpec(null, null, null, null, null, null).noLink()).as("the six-argument form").isFalse();
    }

    @Test
    @DisplayName("the fixture: 3 records, 2 skipped; the URL kept as text and defanged")
    void fixture() {
        UrlhausParser.Recent recent = UrlhausParser.parse(AdapterFixtures.bytes("abusech-urlhaus", "recent.json"));
        assertThat(recent.records()).extracting(NormalizedRecord::externalId).containsExactly("223622", "223621",
                "3610001");
        assertThat(recent.skipped()).containsOnly(Map.entry(MappedRecord.MISSING_ID, 1),
                Map.entry(MappedRecord.MISSING_OBSERVED_AT, 1));
        NormalizedRecord first = recent.records().getFirst();
        assertThat(first.kind()).isEqualTo("malware-url");
        assertThat(first.geometry()).isNull();
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2019-08-10T09:02:05Z"));
        assertThat(first.eventTime()).isEqualTo(first.observedAt());
        assertThat(first.payload()).containsEntry("url", "http://45.61.49.78/razor/r4z0r.mips")
                .containsEntry("urlDefanged", "hxxp://45[.]61[.]49[.]78/razor/r4z0r.mips")
                .containsEntry("host", "45.61.49.78").containsEntry("urlStatus", "offline")
                .containsEntry("threat", "malware_download").containsEntry("tags", List.of("elf"))
                .containsEntry("blacklists", Map.of("spamhaus_dbl", "not listed", "surbl", "not listed"))
                .containsEntry("reporter", "zbetcheckin")
                .containsEntry("reference", "https://urlhaus.abuse.ch/url/223622/")
                .doesNotContainKey("larted");
        NormalizedRecord https = recent.records().get(2);
        assertThat(https.payload()).containsEntry("urlDefanged",
                "hxxps://files[.]malware[.]example[.]com:8443/dl/invoice.zip?id=1.2").doesNotContainKey("tags");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), recent.records());
    }

    @Test
    @DisplayName("defang: scheme, host dots only, no scheme, null")
    void defang() {
        assertThat(UrlhausParser.defang("HTTP://a.b/c.d")).isEqualTo("hxxp://a[.]b/c.d");
        assertThat(UrlhausParser.defang("https://a.b.c")).isEqualTo("hxxps://a[.]b[.]c");
        assertThat(UrlhausParser.defang("ftp://x.y/z")).isEqualTo("ftp://x[.]y/z");
        assertThat(UrlhausParser.defang("a.b?q=1.2#f.g")).isEqualTo("a[.]b?q=1.2#f.g");
        assertThat(UrlhausParser.defang(null)).isNull();
    }

    @Test
    @DisplayName("no_results is an empty window; another status or a non-object is a format error")
    void statuses() {
        assertThat(UrlhausParser.parse("{\"query_status\": \"no_results\"}".getBytes(StandardCharsets.UTF_8))
                .records()).isEmpty();
        assertThatThrownBy(() -> UrlhausParser.parse("{\"query_status\": \"http_get_expected\"}"
                .getBytes(StandardCharsets.UTF_8))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("http_get_expected");
        assertThatThrownBy(() -> UrlhausParser.parse("[]".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> UrlhausParser.parse("{\"error\": \"Unauthorized\"}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("the same entry seen in a later window keeps its static hash; a status change does not")
    void snapshot() {
        JsonNode row = JSON.readTree(AdapterFixtures.bytes("abusech-urlhaus", "recent.json")).get("urls").get(0);
        SnapshotContract.assertStableStaticHash(descriptor(), List.of(UrlhausParser.entry(row).record(),
                UrlhausParser.entry(row.deepCopy()).record()));
        ObjectNode online = ((ObjectNode) row).deepCopy();
        online.put("url_status", "online");
        assertThat(UrlhausParser.entry(online).record().payload()).containsEntry("urlStatus", "online");
    }
}
