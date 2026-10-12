package ai.foresignal.inbound.edp;

import static ai.foresignal.inbound.edp.EdpFixtures.RECEIVED;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Time rules of EDP items (docs/External-Data-Protocol.md §3.5, invariant EXT-4). */
class EdpTimeTest {

    private final EdpItemMapper mapper = EdpFixtures.mapper();

    private static EdpValidItem valid(String manifest, String item) {
        EdpManifestValidator.ManifestCheck m = EdpFixtures.manifest(manifest);
        EdpItemValidator.ItemCheck check = EdpFixtures.itemValidator()
                .validate(List.of(EdpFixtures.parse(item)), m.model(), m.payloadSchemas(), RECEIVED);
        assertThat(check.rejected()).isEmpty();
        return check.valid().get(0);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> times(NormalizedRecord record) {
        return (Map<String, Object>) record.payload().get(EdpItemMapper.PAYLOAD_TIMES);
    }

    @Test
    @DisplayName("effective availability is never earlier than receipt, and a later provider time is kept")
    void effective() {
        Instant early = RECEIVED.minusSeconds(3600);
        Instant late = RECEIVED.plusSeconds(60);

        assertThat(EdpTimes.effective(early, RECEIVED)).isEqualTo(RECEIVED);
        assertThat(EdpTimes.effective(null, RECEIVED)).isEqualTo(RECEIVED);
        assertThat(EdpTimes.effective(late, RECEIVED)).isEqualTo(late);
    }

    @Test
    @DisplayName("an observation whose knownAt is a day before receipt becomes available at receipt")
    void knownAtBeforeReceipt() {
        EdpManifestModel model = EdpFixtures.manifest("hormuz-example.json").model();
        EdpValidItem item = valid("hormuz-example.json", """
                {"stream":"transits","type":"observation","id":"tanker:2026-10-09","observedAt":"2026-10-10T06:00:00Z",
                 "seriesKey":"tanker","t":"2026-10-09","value":28,"knownAt":"2026-10-10T06:00:00Z"}""");

        NormalizedRecord record = mapper.map(item, model, RECEIVED);

        assertThat(record.payload().get("knownAt")).isEqualTo("2026-10-10T06:00:00Z");
        assertThat(Instant.parse((String) times(record).get("effectiveKnownAt"))).isEqualTo(RECEIVED);
        assertThat(times(record).get("receivedAt")).isEqualTo(RECEIVED.toString());
    }

    @Test
    @DisplayName("publishedAt and madeAt are effective no earlier than receipt")
    void documentsAndForecasts() {
        EdpManifestModel model = EdpFixtures.manifest("push-layer.json").model();
        NormalizedRecord document = mapper.map(valid("push-layer.json", """
                {"stream":"notes","type":"document","id":"n","observedAt":"2026-10-11T17:00:00Z","documentType":"note",
                 "text":"x","publishedAt":"2026-10-01T00:00:00Z"}"""), model, RECEIVED);
        NormalizedRecord forecast = mapper.map(valid("push-layer.json", """
                {"stream":"odds","type":"forecast","id":"f","madeAt":"2026-09-01T00:00:00Z","questionRef":"q","probability":0.3}"""),
                model, RECEIVED);

        assertThat(Instant.parse((String) times(document).get("effectivePublishedAt"))).isEqualTo(RECEIVED);
        assertThat(Instant.parse((String) times(forecast).get("effectiveMadeAt"))).isEqualTo(RECEIVED);
        assertThat(forecast.observedAt()).isEqualTo(Instant.parse("2026-09-01T00:00:00Z"));
    }

    @Test
    @DisplayName("observedAt more than 5 minutes after receipt is OBSERVED_AT_IN_FUTURE; 4 minutes is accepted")
    void futureObservedAt() {
        EdpManifestValidator.ManifestCheck m = EdpFixtures.manifest("hormuz-example.json");
        String template = """
                {"stream":"incidents","type":"event","id":"%s","observedAt":"%s","eventTime":"2026-10-11T17:00:00Z",
                 "properties":{"category":"c"}}""";

        EdpItemValidator.ItemCheck check = EdpFixtures.itemValidator().validate(List.of(
                EdpFixtures.parse(template.formatted("far", RECEIVED.plusSeconds(301))),
                EdpFixtures.parse(template.formatted("near", RECEIVED.plusSeconds(240)))),
                m.model(), m.payloadSchemas(), RECEIVED);

        assertThat(check.rejected()).singleElement().satisfies(r -> {
            assertThat(r.reason()).isEqualTo("OBSERVED_AT_IN_FUTURE");
            assertThat(r.id()).isEqualTo("far");
        });
        assertThat(check.valid()).extracting(EdpValidItem::id).containsExactly("near");
    }

    @Test
    @DisplayName("a received-mode stream stamps observedAt with the receipt time; repeats map to identical stable keys")
    void receivedModeAndStableProvenance() {
        EdpManifestModel model = EdpFixtures.manifest("hormuz-example.json").model();
        EdpValidItem item = valid("hormuz-example.json", """
                {"stream":"incidents","type":"event","id":"inc-1","observedAt":"2026-10-11T17:41:00Z",
                 "eventTime":"2026-10-11T16:55:00Z","upstreamEventId":"up-9",
                 "geometry":{"type":"Point","coordinates":[56.4,26.5]},"properties":{"category":"c"}}""");

        NormalizedRecord first = mapper.map(item, model, RECEIVED);
        NormalizedRecord repeat = mapper.map(item, model, RECEIVED.plusSeconds(600));

        assertThat(first.externalId()).isEqualTo("incidents:inc-1");
        assertThat(first.kind()).isEqualTo("maritime-incident");
        assertThat(first.eventTime()).isEqualTo(Instant.parse("2026-10-11T16:55:00Z"));
        assertThat(first.geometry()).isNotNull();
        assertThat(first.payload().get(EdpItemMapper.PAYLOAD_EDP)).isEqualTo(repeat.payload().get(EdpItemMapper.PAYLOAD_EDP));
        @SuppressWarnings("unchecked")
        Map<String, Object> edp = (Map<String, Object>) first.payload().get(EdpItemMapper.PAYLOAD_EDP);
        assertThat(edp).containsEntry("originator", "example-osint-lab").containsEntry("upstreamId", "up-9")
                .containsEntry("mode", "layer");

        EdpManifestModel.Stream stream = model.stream("incidents");
        EdpManifestModel.Stream received = new EdpManifestModel.Stream(stream.raw(), stream.id(), stream.mode(),
                stream.itemType(), stream.kind(), stream.title(), stream.geometry(), stream.update(),
                stream.snapshotScope(), stream.payloadSchema(), stream.titleField(), stream.significant(),
                stream.volatileFields(), true, stream.ttl(), stream.retentionClass(), stream.unit(),
                stream.documentType(), stream.presentation());
        NormalizedRecord stamped = mapper.map(new EdpValidItem(0, item.item(), "event", received), model, RECEIVED);
        assertThat(stamped.observedAt()).isEqualTo(RECEIVED);
    }

    @Test
    @DisplayName("a delete becomes a tombstone and a status item no record")
    void deleteAndStatus() {
        EdpManifestModel model = EdpFixtures.manifest("hormuz-example.json").model();

        NormalizedRecord tombstone = mapper.map(valid("hormuz-example.json",
                "{\"stream\":\"incidents\",\"type\":\"delete\",\"id\":\"inc-2\"}"), model, RECEIVED);
        NormalizedRecord status = mapper.map(valid("hormuz-example.json",
                "{\"type\":\"status\",\"health\":\"blocked\"}"), model, RECEIVED);

        assertThat(tombstone.tombstone()).isTrue();
        assertThat(tombstone.observedAt()).isEqualTo(RECEIVED);
        assertThat(status).isNull();
    }
}
