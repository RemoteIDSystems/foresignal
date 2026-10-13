package ai.foresignal.source.adapters.nvd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link NvdParser} on a page of 16 real NVD CVEs (captured 2026-10-13): metric selection (v4.0 before v3.1, the
 * Primary entry before others), CPE vendor and product extraction (vulnerable criteria only), the score and vendor
 * filters, KEV CVEs always kept, and rejected CVEs reported instead of mapped.
 */
class NvdParserTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final NvdParser.Filter DEFAULT = new NvdParser.Filter(7.0, Set.of());

    static SourceDescriptor descriptor() {
        return new NvdCvesAdapter("", Duration.ZERO, d -> { }).descriptor();
    }

    static byte[] page() {
        return AdapterFixtures.bytes("nvd-cves", "cves-page.json");
    }

    @Test
    @DisplayName("the descriptor: two kinds, 2 h (min 2 h), 10/min and 600/day, rank cvssScore, modelDate volatile,"
            + " both attribution notices")
    void descriptorShape() {
        SourceDescriptor d = descriptor();
        assertThat(DescriptorValidator.problems(d, new NvdCvesAdapter("", Duration.ZERO, x -> { }))).isEmpty();
        assertThat(d.kinds()).containsExactlyInAnyOrder("vulnerability", "cve-epss");
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofHours(2));
        assertThat(d.minInterval()).isEqualTo(Duration.ofHours(2));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(10);
        assertThat(d.defaultBudget().perDay()).isEqualTo(600);
        assertThat(d.rankField()).isEqualTo("cvssScore");
        assertThat(d.titleField()).isEqualTo("cveId");
        assertThat(d.embedFields()).containsExactly("description");
        assertThat(d.volatileFields()).containsExactly("modelDate");
        assertThat(d.requiresCredentials()).as("the apiKey is optional").isFalse();
        assertThat(d.attribution()).contains("This product uses the NVD API but is not endorsed or certified by the"
                + " NVD.").contains("EPSS scores from FIRST.org (first.org/epss)");
    }

    @Test
    @DisplayName("the default filter keeps 9 of 16: 7 at 7.0 or above and 2 KEV CVEs below; one rejected; 6 skipped")
    void defaultFilter() {
        NvdParser.Page page = NvdParser.parse(page(), DEFAULT);
        assertThat(page.totalResults()).isEqualTo(16);
        assertThat(page.count()).isEqualTo(16);
        assertThat(page.records()).extracting(NormalizedRecord::externalId).containsExactly("CVE-2025-8679",
                "CVE-2025-59951", "CVE-2025-5914", "CVE-2024-33396", "CVE-2025-58777", "CVE-2025-59147",
                "CVE-2025-23355", "CVE-2010-0738", "CVE-2015-5477");
        assertThat(page.rejected()).containsExactly("CVE-2026-97058");
        assertThat(page.skipped()).containsOnly(Map.entry("below-min-cvss", 6));
        assertThat(page.records()).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("vulnerability");
            assertThat(r.geometry()).isNull();
            assertThat(r.payload()).containsKey("cveId").containsKey("kev").doesNotContainKey("lastModified");
        });
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), page.records());
    }

    @Test
    @DisplayName("metric selection: CVSS 4.0 (a Secondary entry) wins over a Primary 3.1 for cvssScore; both kept")
    void metricSelection() {
        NormalizedRecord extreme = byId(NvdParser.parse(page(), DEFAULT).records(), "CVE-2025-8679");
        assertThat(extreme.observedAt()).isEqualTo(Instant.parse("2026-10-12T15:10:00.207Z"));
        assertThat(extreme.eventTime()).isEqualTo(Instant.parse("2025-10-01T18:15:46.880Z"));
        assertThat(extreme.payload()).containsEntry("cvssScore", 7.6).containsEntry("vulnStatus", "Analyzed")
                .containsEntry("cwes", List.of("CWE-307")).containsEntry("vendors", List.of("extremenetworks"))
                .containsEntry("products", List.of("extremeguest_essentials")).containsEntry("kev", false)
                .containsEntry("references", List.of("https://extreme-networks.my.site.com/ExtrArticleDetail?an=000130289"))
                .doesNotContainKeys("cisaActionDue", "cisaRequiredAction");
        @SuppressWarnings("unchecked")
        Map<String, Object> v40 = (Map<String, Object>) extreme.payload().get("cvssV40");
        @SuppressWarnings("unchecked")
        Map<String, Object> v31 = (Map<String, Object>) extreme.payload().get("cvssV31");
        assertThat(v40).containsEntry("baseScore", 7.6).containsEntry("baseSeverity", "HIGH");
        assertThat((String) v40.get("vectorString")).startsWith("CVSS:4.0/AV:A/AC:H");
        assertThat(v31).containsOnly(Map.entry("baseScore", 9.8), Map.entry("baseSeverity", "CRITICAL"),
                Map.entry("vectorString", "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H"));
        assertThat((String) extreme.payload().get("description")).startsWith("In ExtremeGuest Essentials before 25.5.0");

        assertThat(NvdParser.parse(page(), new NvdParser.Filter(8.0, Set.of())).records())
                .extracting(NormalizedRecord::externalId).as("7.6 is below 8 although v3.1 says 9.8")
                .doesNotContain("CVE-2025-8679").contains("CVE-2025-59951", "CVE-2024-33396");
    }

    @Test
    @DisplayName("CPE extraction: only vulnerable criteria, at most 20 products; KEV fields; 10 references")
    @SuppressWarnings("unchecked")
    void cpeAndKev() {
        List<NormalizedRecord> records = NvdParser.parse(page(), DEFAULT).records();
        assertThat(byId(records, "CVE-2025-23355").payload()).as("microsoft is a non-vulnerable platform")
                .containsEntry("vendors", List.of("nvidia")).containsEntry("products", List.of("nsight_graphics"));
        NormalizedRecord bind = byId(records, "CVE-2015-5477");
        assertThat((List<?>) bind.payload().get("products")).hasSize(NvdParser.MAX_CPE);
        assertThat((List<Object>) bind.payload().get("vendors")).hasSize(13).contains("isc", "redhat");
        assertThat((List<?>) bind.payload().get("references")).hasSize(NvdParser.MAX_LIST);
        assertThat(bind.payload()).containsEntry("cwes", List.of("CWE-19", "CWE-617"));
        NormalizedRecord jboss = byId(records, "CVE-2010-0738");
        assertThat(jboss.payload()).containsEntry("kev", true).containsEntry("cvssScore", 5.3)
                .containsEntry("cisaActionDue", "2022-06-15")
                .containsEntry("cisaRequiredAction", "Apply updates per vendor instructions.");
    }

    @Test
    @DisplayName("vendor filter: only matching vendors pass, KEV CVEs always; minCvss 0 keeps the unscored CVE")
    void vendorAndZeroFilters() {
        assertThat(NvdParser.parse(page(), new NvdParser.Filter(7.0, Set.of("NVIDIA"))).records())
                .extracting(NormalizedRecord::externalId)
                .containsExactly("CVE-2025-23355", "CVE-2010-0738", "CVE-2015-5477");
        NvdParser.Page cisco = NvdParser.parse(page(), new NvdParser.Filter(7.0, Set.of("cisco")));
        assertThat(cisco.records()).extracting(NormalizedRecord::externalId).containsExactly("CVE-2010-0738",
                "CVE-2015-5477");
        assertThat(cisco.skipped()).containsEntry("below-min-cvss", 6).containsEntry("vendor", 7);
        NvdParser.Page all = NvdParser.parse(page(), new NvdParser.Filter(0, Set.of()));
        assertThat(all.records()).hasSize(15).extracting(NormalizedRecord::externalId).contains("CVE-2026-98385");
        assertThat(byId(all.records(), "CVE-2026-98385").payload()).doesNotContainKeys("cvssScore", "cvssV40",
                "cvssV31");
    }

    @Test
    @DisplayName("a rejected CVE is never a record, whatever the filter; a broken page is a format error")
    void rejectedAndBroken() {
        NvdParser.Page all = NvdParser.parse(page(), new NvdParser.Filter(0, Set.of()));
        assertThat(all.records()).extracting(NormalizedRecord::externalId).doesNotContain("CVE-2026-97058");
        assertThat(all.rejected()).containsExactly("CVE-2026-97058");
        assertThatThrownBy(() -> NvdParser.parse("{\"vulnerabilities\": []}".getBytes(StandardCharsets.UTF_8),
                DEFAULT)).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> NvdParser.parse("<html>".getBytes(StandardCharsets.UTF_8), DEFAULT))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("a record whose only change is lastModified keeps its static hash (a touch, not a version)")
    void snapshot() {
        JsonNode cve = JSON.readTree(page()).get("vulnerabilities").get(0).get("cve");
        ObjectNode later = ((ObjectNode) cve).deepCopy();
        later.put("lastModified", "2026-10-13T08:00:00.000");
        SnapshotContract.assertStableStaticHash(descriptor(), List.of(NvdParser.record(cve), NvdParser.record(later)));
    }

    static NormalizedRecord byId(List<NormalizedRecord> records, String id) {
        return records.stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }
}
