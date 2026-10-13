package ai.foresignal.source.adapters.cisa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
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
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link KevParser} on a real catalog (captured 2026-10-13, trimmed to 25 entries, {@code count} set to 25) and
 * hand-made edge cases: names are trimmed, {@code observedAt} is the catalog's release time, notes are cut at 2 KB.
 */
class KevParserTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant RELEASED = Instant.parse("2026-10-13T19:10:43.6568Z");

    static SourceDescriptor descriptor() {
        return new CisaKevAdapter("").descriptor();
    }

    @Test
    @DisplayName("the descriptor validates: disabled-by-default source data, license, 6 h, 1/min and 20/day")
    void descriptorShape() {
        SourceDescriptor d = descriptor();
        assertThat(DescriptorValidator.problems(d, new CisaKevAdapter(""))).isEmpty();
        assertThat(d.licence()).contains("CC0 1.0");
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofHours(6));
        assertThat(d.minInterval()).isEqualTo(Duration.ofHours(1));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(1);
        assertThat(d.defaultBudget().perDay()).isEqualTo(20);
        assertThat(d.titleField()).isEqualTo("vulnerabilityName");
        assertThat(d.searchFields()).containsExactly("cveId", "vendorProject", "product", "vulnerabilityName");
        assertThat(d.embedFields()).containsExactly("vulnerabilityName", "shortDescription");
        assertThat(d.rankField()).isNull();
        assertThat(d.kindRetention()).containsEntry("vulnerability", Duration.ofDays(1825));
    }

    @Test
    @DisplayName("the fixture: 25 records, trimmed names, release time as observedAt, dateAdded as eventTime")
    void fixture() {
        KevParser.Catalog catalog = KevParser.parse(AdapterFixtures.bytes("cisa-kev", "catalog.json"));
        assertThat(catalog.catalogVersion()).isEqualTo("2026.10.13");
        assertThat(catalog.released()).isEqualTo(RELEASED);
        assertThat(catalog.skipped()).isEmpty();
        assertThat(catalog.records()).hasSize(25).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("vulnerability");
            assertThat(r.geometry()).isNull();
            assertThat(r.observedAt()).isEqualTo(RELEASED);
            assertThat(r.payload()).containsEntry("kev", true).containsEntry("cveId", r.externalId());
            assertThat(r.payload().keySet()).isSubsetOf("cveId", "vendorProject", "product", "vulnerabilityName",
                    "shortDescription", "requiredAction", "dueDate", "knownRansomwareCampaignUse", "cwes", "notes",
                    "kev");
            String name = (String) r.payload().get("vulnerabilityName");
            assertThat(name).isEqualTo(name.strip());
        });
        NormalizedRecord bind = byId(catalog.records(), "CVE-2015-5477");
        assertThat(bind.eventTime()).isEqualTo(Instant.parse("2026-10-08T00:00:00Z"));
        assertThat(bind.payload()).containsEntry("vulnerabilityName", "ISC BIND Data Processing Errors Vulnerability")
                .containsEntry("vendorProject", "ISC").containsEntry("product", "BIND")
                .containsEntry("dueDate", "2026-10-11").containsEntry("knownRansomwareCampaignUse", "Unknown")
                .containsEntry("cwes", List.of("CWE-19"));
        assertThat(byId(catalog.records(), "CVE-2026-5430").payload()).containsEntry("vulnerabilityName",
                "WSO2 Multiple Products Path Traversal Vulnerability");
        assertThat(byId(catalog.records(), "CVE-2026-59310").payload())
                .containsEntry("knownRansomwareCampaignUse", "Known");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), catalog.records());
    }

    @Test
    @DisplayName("edge cases: notes cut at 2 KB, a bad id and a repeat are skipped, no dateReleased is a format error")
    void edgeCases() {
        String notes = "x".repeat(3000);
        String body = """
                {"catalogVersion": "t", "dateReleased": "2026-10-13T00:00:00Z", "vulnerabilities": [
                 {"cveID": "CVE-2026-0001", "vendorProject": " V ", "product": "P", "vulnerabilityName": "N",
                  "dateAdded": "2026-10-01", "dueDate": "not a date", "knownRansomwareCampaignUse": "known",
                  "notes": "%s", "cwes": []},
                 {"cveID": "not-a-cve"},
                 {"cveID": "cve-2026-0001"}]}""".formatted(notes);
        KevParser.Catalog catalog = KevParser.parse(body.getBytes(StandardCharsets.UTF_8));
        assertThat(catalog.records()).hasSize(1);
        assertThat(catalog.skipped()).containsOnly(Map.entry(MappedRecord.MISSING_ID, 1),
                Map.entry(MappedRecord.DUPLICATE_ID, 1));
        NormalizedRecord r = catalog.records().getFirst();
        assertThat((String) r.payload().get("notes")).hasSize(KevParser.MAX_NOTES);
        assertThat(r.payload()).containsEntry("vendorProject", "V").containsEntry("knownRansomwareCampaignUse",
                "Known").doesNotContainKeys("dueDate", "cwes");

        assertThatThrownBy(() -> KevParser.parse("{\"vulnerabilities\": []}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("dateReleased");
        assertThatThrownBy(() -> KevParser.parse("[]".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> KevParser.parse("<html>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("a new catalog release with the entry unchanged keeps the static hash (observedAt is not hashed)")
    void snapshot() {
        ObjectNode entry = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("cisa-kev", "catalog.json"))
                .get("vulnerabilities").get(0);
        SnapshotContract.assertStableStaticHash(descriptor(), List.of(
                KevParser.entry(entry, RELEASED).record(),
                KevParser.entry(entry, RELEASED.plus(Duration.ofHours(6))).record()));
    }

    static NormalizedRecord byId(List<NormalizedRecord> records, String id) {
        return records.stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }
}
