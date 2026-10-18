package ai.foresignal.stix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.HistoryQuery;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The STIX 2.1 mapping and export (prompt 141): deterministic UUIDv5 ids (checked against independent vectors),
 * vulnerability and indicator objects, pattern construction and escaping, the identity, the bundle shape, the egress
 * filter and the export cap, and TAXII pages.
 */
class StixExportTest {

    static final Instant ADDED = Instant.parse("2026-03-01T00:00:00Z");
    static final Instant RELEASED = Instant.parse("2026-03-05T12:30:00.123456Z");

    /** The STIX 2.1 identifier grammar: {@code type--UUID} with a version 5 UUID here. */
    static final Pattern STIX_ID = Pattern.compile("^[a-z][a-z0-9-]+--[0-9a-f]{8}-[0-9a-f]{4}-5[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

    /** A STIX timestamp with millisecond precision. */
    static final Pattern TIMESTAMP = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$");

    static RecordRow row(String source, String id, String kind, Instant event, Instant observed, String payload) {
        return new RecordRow(source, id, kind, 1, 1L, observed, observed, event, null, null, false, null, null, payload,
                null, null, null, 0, null, null, observed, observed, null, null, null, null, null, null);
    }

    static RecordRow kev() {
        return row("cisa-kev", "CVE-2026-0001", StixMapper.VULNERABILITY_KIND, ADDED, RELEASED,
                "{\"cveId\":\"CVE-2026-0001\",\"shortDescription\":\"Example overflow\",\"kev\":true}");
    }

    static RecordRow url() {
        return row("abusech-urlhaus", "3456789", StixMapper.URL_KIND, ADDED, ADDED,
                "{\"url\":\"http://bad.example/it's\\\\x.exe\",\"urlDefanged\":\"hxxp://bad[.]example/it's\\\\x.exe\","
                        + "\"threat\":\"malware_download\",\"tags\":[\"exe\",\"Emotet\"],"
                        + "\"reference\":\"https://urlhaus.abuse.ch/url/3456789/\"}");
    }

    @Test
    @DisplayName("UUIDv5 matches RFC 9562 vectors; the namespace, identity and record ids are fixed values")
    void deterministicIds() {
        UUID dns = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
        assertThat(StixIds.uuid5(dns, "python.org")).hasToString("886313e1-3b8a-5372-9b90-0c9aee199e5d");
        assertThat(StixIds.NAMESPACE).hasToString("a9a0c9a8-3f27-52ff-af53-1f4bd6681829");
        assertThat(StixMapper.identityId()).isEqualTo("identity--1513831a-f7cd-585c-8833-54b0f4a5adaf");
        assertThat(StixMapper.idOf(kev())).isEqualTo("vulnerability--aeee59b6-79ef-5fa9-9ea1-8d2d28ed7dae");
        // The same record exports under the same id whatever its version or payload.
        RecordRow later = row("cisa-kev", "CVE-2026-0001", StixMapper.VULNERABILITY_KIND, ADDED,
                RELEASED.plusSeconds(86_400), "{\"cveId\":\"CVE-2026-0001\"}");
        assertThat(StixMapper.toStix(later).get("id")).isEqualTo(StixMapper.toStix(kev()).get("id"));
        // A different source of the same CVE is a different record.
        RecordRow nvd = row("nvd-cves", "CVE-2026-0001", StixMapper.VULNERABILITY_KIND, ADDED, RELEASED, "{}");
        assertThat(StixMapper.idOf(nvd)).isNotEqualTo(StixMapper.idOf(kev())).matches(STIX_ID);
        assertThat(TaxiiCollection.VULNERABILITIES.id()).isEqualTo("e634cb86-2386-58f6-a4f2-096f13451ac2");
        assertThat(TaxiiCollection.INDICATORS.id()).isEqualTo("0606fa1e-f27e-5b54-80e9-2cfb5711dd9b");
    }

    @Test
    @DisplayName("a KEV record becomes a vulnerability with the CVE external reference, the kev label and timestamps")
    void vulnerability() {
        Map<String, Object> v = StixMapper.toStix(kev());
        assertThat(v).containsEntry("type", "vulnerability").containsEntry("spec_version", "2.1")
                .containsEntry("name", "CVE-2026-0001").containsEntry("description", "Example overflow")
                .containsEntry("created_by_ref", StixMapper.identityId())
                .containsEntry("created", "2026-03-01T00:00:00.000Z")
                .containsEntry("modified", "2026-03-05T12:30:00.123Z")
                .containsEntry("labels", List.of("kev"))
                .containsEntry("x_foresignal_ref", "record:cisa-kev/CVE-2026-0001");
        assertThat(v.get("external_references"))
                .isEqualTo(List.of(Map.of("source_name", "cve", "external_id", "CVE-2026-0001")));

        RecordRow nvd = row("nvd-cves", "CVE-2026-0002", StixMapper.VULNERABILITY_KIND, ADDED, RELEASED,
                "{\"description\":\"NVD text\",\"cvssScore\":9.8}");
        Map<String, Object> n = StixMapper.toStix(nvd);
        assertThat(n).containsEntry("name", "CVE-2026-0002").containsEntry("description", "NVD text")
                .containsEntry("x_foresignal_cvss_score", 9.8).doesNotContainKey("labels");
    }

    @Test
    @DisplayName("an event time after the observed time is not used as created")
    void createdNeverAfterModified() {
        RecordRow odd = row("nvd-cves", "CVE-1", StixMapper.VULNERABILITY_KIND, RELEASED, ADDED, "{}");
        Map<String, Object> v = StixMapper.toStix(odd);
        assertThat(v.get("created")).isEqualTo(v.get("modified")).isEqualTo("2026-03-01T00:00:00.000Z");
    }

    @Test
    @DisplayName("a URLhaus record becomes a url indicator; quotes and backslashes in the literal are escaped")
    void urlIndicator() {
        Map<String, Object> i = StixMapper.toStix(url());
        assertThat(i).containsEntry("type", "indicator")
                .containsEntry("pattern", "[url:value = 'http://bad.example/it\\'s\\\\x.exe']")
                .containsEntry("pattern_type", "stix").containsEntry("pattern_version", "2.1")
                .containsEntry("indicator_types", List.of("malicious-activity"))
                .containsEntry("valid_from", "2026-03-01T00:00:00.000Z")
                .containsEntry("labels", List.of("exe", "Emotet"))
                .containsEntry("name", "Malware URL hxxp://bad[.]example/it's\\x.exe")
                .containsEntry("description", "URLhaus threat: malware_download");
        assertThat(i.get("external_references")).isEqualTo(List.of(Map.of("source_name", "urlhaus",
                "url", "https://urlhaus.abuse.ch/url/3456789/")));
        assertThat((String) i.get("id")).startsWith("indicator--").matches(STIX_ID);
        assertThat(StixMapper.toStix(row("abusech-urlhaus", "x", StixMapper.URL_KIND, null, ADDED, "{}")))
                .as("no url").isNull();
    }

    @Test
    @DisplayName("Feodo records: ipv4 with port, bracketed ipv6 with port from the id, an address without a port")
    void c2Indicators() {
        Map<String, Object> v4 = StixMapper.toStix(row("abusech-feodo", "198.51.100.7:8443", StixMapper.C2_KIND,
                ADDED, ADDED, "{\"ip\":\"198.51.100.7\",\"port\":8443,\"malware\":\"QakBot\"}"));
        assertThat(v4.get("pattern")).isEqualTo("[network-traffic:dst_ref.type = 'ipv4-addr' AND"
                + " network-traffic:dst_ref.value = '198.51.100.7' AND network-traffic:dst_port = 8443]");
        assertThat(v4).containsEntry("labels", List.of("qakbot"))
                .containsEntry("name", "C2 server 198.51.100.7:8443 (QakBot)");

        Map<String, Object> v6 = StixMapper.toStix(row("abusech-feodo", "[2001:db8::10]:443", StixMapper.C2_KIND,
                ADDED, ADDED, "{}"));
        assertThat(v6.get("pattern")).isEqualTo("[network-traffic:dst_ref.type = 'ipv6-addr' AND"
                + " network-traffic:dst_ref.value = '2001:db8::10' AND network-traffic:dst_port = 443]");

        Map<String, Object> bare = StixMapper.toStix(row("abusech-feodo", "203.0.113.5", StixMapper.C2_KIND,
                ADDED, ADDED, "{}"));
        assertThat(bare.get("pattern")).isEqualTo("[ipv4-addr:value = '203.0.113.5']");

        assertThat(StixMapper.toStix(row("abusech-feodo", "not-an-ip", StixMapper.C2_KIND, ADDED, ADDED, "{}")))
                .isNull();
    }

    @Test
    @DisplayName("tombstones, unknown kinds and null rows map to nothing; host:port splitting")
    void nothingToMap() {
        RecordRow tomb = new RecordRow("cisa-kev", "CVE-1", StixMapper.VULNERABILITY_KIND, 2, 2L, ADDED, ADDED, null,
                null, null, true, null, null, null, null, null, null, 0, null, null, ADDED, ADDED, null, null, null,
                null, null, null);
        assertThat(StixMapper.toStix(tomb)).isNull();
        assertThat(StixMapper.toStix(row("x", "y", "earthquake", ADDED, ADDED, "{}"))).isNull();
        assertThat(StixMapper.toStix(null)).isNull();
        assertThat(StixMapper.splitHostPort("1.2.3.4:80")).containsExactly("1.2.3.4", "80");
        assertThat(StixMapper.splitHostPort("[::1]")).containsExactly("::1", null);
        assertThat(StixMapper.splitHostPort("::1")).containsExactly("::1", null);
        assertThat(StixMapper.splitHostPort(null)).containsExactly(null, null);
        assertThat(StixMapper.literal("a'b\\c")).isEqualTo("a\\'b\\\\c");
    }

    @Test
    @DisplayName("the identity and the bundle: type bundle, a stable bundle-- id, identity first")
    void bundleShape() {
        Map<String, Object> identity = StixMapper.identity();
        assertThat(identity).containsEntry("type", "identity").containsEntry("identity_class", "system")
                .containsEntry("name", "Foresignal").containsEntry("created", "2026-10-11T00:00:00.000Z");
        List<Map<String, Object>> objects = List.of(identity, StixMapper.toStix(kev()), StixMapper.toStix(url()));
        Map<String, Object> bundle = StixService.bundle(objects);
        assertThat(bundle).containsOnlyKeys("type", "id", "objects").containsEntry("type", "bundle");
        assertThat((String) bundle.get("id")).startsWith("bundle--").matches(STIX_ID);
        assertThat(StixService.bundle(objects).get("id")).as("stable").isEqualTo(bundle.get("id"));
        assertThat(StixService.bundle(objects.subList(0, 2)).get("id")).isNotEqualTo(bundle.get("id"));
        for (Map<String, Object> object : objects) {
            assertThat((String) object.get("id")).matches(STIX_ID);
            assertThat((String) object.get("created")).matches(TIMESTAMP);
            assertThat((String) object.get("modified")).matches(TIMESTAMP);
            assertThat(object.get("spec_version")).isEqualTo("2.1");
        }
    }

    @Test
    @DisplayName("export: identity plus mapped records; refused records are counted; above 50,000 records is a 400")
    void export() {
        RecordQueryService queries = mock(RecordQueryService.class);
        EgressPolicy egress = mock(EgressPolicy.class);
        RecordRow refused = row("private-kev", "CVE-9", StixMapper.VULNERABILITY_KIND, ADDED, ADDED, "{}");
        when(queries.list(any(CurrentQuery.class))).thenReturn(
                new Slices.RecordSlice(List.of(kev(), url(), refused), null, 10L, null));
        when(egress.decide(any(RecordRow.class), eq(EgressPath.EXPORT), any(), any())).thenAnswer(inv -> {
            RecordRow r = inv.getArgument(0);
            return "private-kev".equals(r.sourceId())
                    ? new EgressPolicy.Decision(false, EgressPolicy.REASON_PRIVATE, null)
                    : new EgressPolicy.Decision(true, null, null);
        });
        StixService service = new StixService(queries, egress);
        StixService.Bundle bundle = service.export(List.of(StixMapper.VULNERABILITY_KIND, StixMapper.URL_KIND), null,
                Set.of(), ReadRestriction.NONE, Viewer.anonymous());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> objects = (List<Map<String, Object>>) bundle.bundle().get("objects");
        assertThat(objects).extracting(o -> o.get("type")).containsExactly("identity", "vulnerability", "indicator");
        assertThat(bundle.refused()).isEqualTo(1);

        List<RecordRow> many = new ArrayList<>();
        for (int i = 0; i <= StixService.MAX_EXPORT; i++) {
            many.add(kev());
        }
        when(queries.list(any(CurrentQuery.class))).thenReturn(new Slices.RecordSlice(many, null, 10L, null));
        assertThatThrownBy(() -> service.export(List.of(StixMapper.VULNERABILITY_KIND), null, Set.of(),
                ReadRestriction.NONE, Viewer.anonymous()))
                .isInstanceOfSatisfying(ForesignalException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.RESULT_TOO_LARGE));
    }

    @Test
    @DisplayName("TAXII page: added_after is strict on ingest time, more/next follow the cursor, dates added reported")
    void taxiiPage() {
        RecordQueryService queries = mock(RecordQueryService.class);
        EgressPolicy egress = mock(EgressPolicy.class);
        when(egress.decide(any(RecordRow.class), eq(EgressPath.EXPORT), any(), any()))
                .thenReturn(new EgressPolicy.Decision(true, null, null));
        RecordRow atBoundary = row("abusech-urlhaus", "1", StixMapper.URL_KIND, ADDED, ADDED,
                "{\"url\":\"http://a.example/\"}");
        RecordRow after = row("abusech-urlhaus", "2", StixMapper.URL_KIND, ADDED, ADDED.plusSeconds(5),
                "{\"url\":\"http://b.example/\"}");
        when(queries.history(any(HistoryQuery.class)))
                .thenReturn(new Slices.VersionSlice(List.of(atBoundary, after), "cursor-2"));
        StixService.Page page = new StixService(queries, egress).objects(TaxiiCollection.INDICATORS, ADDED, 2, null,
                Set.of(), ReadRestriction.NONE, Viewer.anonymous());
        assertThat(page.objects()).hasSize(1);
        assertThat(page.objects().getFirst().get("pattern")).isEqualTo("[url:value = 'http://b.example/']");
        assertThat(page.more()).isTrue();
        assertThat(page.next()).isEqualTo("cursor-2");
        assertThat(page.firstAdded()).isEqualTo(ADDED.plusSeconds(5));
        assertThat(page.lastAdded()).isEqualTo(ADDED.plusSeconds(5));
    }
}
