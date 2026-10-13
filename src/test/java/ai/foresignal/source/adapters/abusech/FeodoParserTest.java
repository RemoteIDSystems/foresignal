package ai.foresignal.source.adapters.abusech;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
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
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link FeodoParser} on the real Feodo Tracker lists (captured 2026-10-13) and a hand-made sample: ids are
 * {@code ip:port}, the point is the country's label point with {@code geometryApproximate}, and the list may be empty.
 */
class FeodoParserTest {

    static final CountryCentroids COUNTRIES = new CountryCentroids();
    static final JsonMapper JSON = JsonMapper.builder().build();

    static SourceDescriptor descriptor() {
        return new FeodoAdapter("", COUNTRIES).descriptor();
    }

    @Test
    @DisplayName("the descriptor validates: 15 min (min 5), 2/min and 200/day, zero results normal, CC0")
    void descriptorShape() {
        SourceDescriptor d = descriptor();
        assertThat(DescriptorValidator.problems(d, new FeodoAdapter("", COUNTRIES))).isEmpty();
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(15));
        assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(2);
        assertThat(d.defaultBudget().perDay()).isEqualTo(200);
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(d.licence()).startsWith("CC0");
        assertThat(d.param(FeodoAdapter.LIST_PARAM).allowed()).containsExactly("standard", "recommended");
    }

    @Test
    @DisplayName("the standard list: 5 C2s at their country label points, ip:port ids, last_online as observedAt")
    void standardList() {
        FeodoParser.Blocklist list = FeodoParser.parse(AdapterFixtures.bytes("abusech-feodo", "ipblocklist.json"),
                COUNTRIES::byIso2);
        assertThat(list.skipped()).isEmpty();
        assertThat(list.records()).extracting(NormalizedRecord::externalId).containsExactly("162.243.103.246:8080",
                "50.16.16.211:443", "34.204.119.63:443", "178.62.3.223:443", "27.133.154.218:443");
        assertThat(list.records()).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("malware-c2");
            assertThat(r.geometry()).isInstanceOf(Point.class);
            assertThat(r.payload()).containsEntry("geometryApproximate", true);
        });
        NormalizedRecord qakbot = list.records().get(1);
        CountryRef us = COUNTRIES.byIso2("US").orElseThrow();
        assertThat(qakbot.geometry().getCoordinate().x).isEqualTo(us.labelLon());
        assertThat(qakbot.geometry().getCoordinate().y).isEqualTo(us.labelLat());
        assertThat(qakbot.observedAt()).isEqualTo(Instant.parse("2026-03-12T00:00:00Z"));
        assertThat(qakbot.eventTime()).isEqualTo(Instant.parse("2025-12-30T13:56:31Z"));
        assertThat(qakbot.payload()).containsEntry("ip", "50.16.16.211").containsEntry("port", 443L)
                .containsEntry("status", "online").containsEntry("hostname", "ec2-50-16-16-211.compute-1.amazonaws.com")
                .containsEntry("asn", 14618L).containsEntry("asName", "AMAZON-AES").containsEntry("country", "US")
                .containsEntry("malware", "QakBot").containsEntry("firstSeen", "2025-12-30T13:56:31Z")
                .containsEntry("lastOnline", "2026-03-12");
        assertThat(list.records().getFirst().payload()).doesNotContainKey("hostname");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), list.records());

        FeodoParser.Blocklist recommended = FeodoParser.parse(AdapterFixtures.bytes("abusech-feodo",
                "ipblocklist_recommended.json"), COUNTRIES::byIso2);
        assertThat(recommended.records()).extracting(NormalizedRecord::externalId).containsExactly("50.16.16.211:443");
    }

    @Test
    @DisplayName("the hand-made sample: IPv6 in brackets, unknown country without geometry, bad port, host names,"
            + " no time and a repeat skipped")
    void sample() {
        FeodoParser.Blocklist list = FeodoParser.parse(AdapterFixtures.bytes("abusech-feodo", "sample.json"),
                COUNTRIES::byIso2);
        assertThat(list.records()).extracting(NormalizedRecord::externalId).containsExactly("198.51.100.23:443",
                "[2001:db8::10]:8443");
        assertThat(list.skipped()).containsOnly(Map.entry(MappedRecord.MISSING_ID, 2),
                Map.entry(MappedRecord.MISSING_OBSERVED_AT, 1), Map.entry(MappedRecord.DUPLICATE_ID, 1));
        NormalizedRecord v6 = list.records().get(1);
        assertThat(v6.geometry()).as("XX is no country").isNull();
        assertThat(v6.payload()).doesNotContainKeys("geometryApproximate", "lastOnline", "hostname");
        assertThat(v6.observedAt()).as("first_seen when never online").isEqualTo(Instant.parse("2026-10-02T09:00:00Z"));
        assertThat(list.records().getFirst().geometry()).isNotNull();
    }

    @Test
    @DisplayName("an empty list is normal; a non-array is a format error")
    void emptyAndBroken() {
        assertThat(FeodoParser.parse(AdapterFixtures.bytes("abusech-feodo", "empty.json"), COUNTRIES::byIso2)
                .records()).isEmpty();
        assertThatThrownBy(() -> FeodoParser.parse("{\"error\": 1}".getBytes(StandardCharsets.UTF_8),
                COUNTRIES::byIso2)).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> FeodoParser.parse("<html>".getBytes(StandardCharsets.UTF_8), COUNTRIES::byIso2))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("the same entry seen again on a later regeneration keeps its static hash")
    void snapshot() {
        JsonNode row = JSON.readTree(AdapterFixtures.bytes("abusech-feodo", "ipblocklist.json")).get(1);
        ObjectNode again = ((ObjectNode) row).deepCopy();
        SnapshotContract.assertStableStaticHash(descriptor(), List.of(FeodoParser.entry(row, COUNTRIES::byIso2)
                .record(), FeodoParser.entry(again, COUNTRIES::byIso2).record()));
    }
}
