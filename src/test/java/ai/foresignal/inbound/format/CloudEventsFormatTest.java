package ai.foresignal.inbound.format;

import static ai.foresignal.inbound.format.FormatTestSupport.options;
import static ai.foresignal.inbound.format.FormatTestSupport.request;
import static ai.foresignal.inbound.format.FormatTestSupport.source;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/** {@link CloudEventsFormat}: structured, batch and binary mode, and the handshake helper (prompt 024). */
class CloudEventsFormatTest {

    private final CloudEventsFormat format = new CloudEventsFormat(FormatTestSupport.JSON, FormatTestSupport.mapper());

    @Test
    @DisplayName("structured mode: subject is the externalId, time is observedAt, data object is the payload with geometry")
    void structured() {
        ParsedIngest parsed = format.parse(request("""
                {"specversion":"1.0","id":"e-1","source":"/sensors/door","type":"door.opened","subject":"door-7",
                 "time":"2026-10-12T07:55:00Z","data":{"state":"open","geometry":{"type":"Point","coordinates":[5,51]}}}""",
                CloudEventsFormat.STRUCTURED, new HttpHeaders()), source("door"), FormatOptions.empty());

        NormalizedRecord record = parsed.records().get(0);
        assertThat(record.externalId()).isEqualTo("door-7");
        assertThat(record.observedAt()).isEqualTo(Instant.parse("2026-10-12T07:55:00Z"));
        assertThat(record.payload()).containsEntry("state", "open").containsEntry("ceType", "door.opened")
                .containsEntry("ceSource", "/sensors/door").doesNotContainKey("geometry");
        assertThat(record.geometry().getCoordinate().y).isEqualTo(51.0);
    }

    @Test
    @DisplayName("batch mode: typeKinds maps types; without subject the id is source/id; invalid events are rejected")
    void batch() {
        ParsedIngest parsed = format.parse(request("""
                [{"specversion":"1.0","id":"1","source":"ha","type":"temp","data":{"lat":52.0,"lon":4.0,"c":20}},
                 {"specversion":"1.0","id":"2","source":"ha","type":"unmapped","data":7},
                 {"specversion":"0.3","id":"3","source":"ha","type":"temp"}]""",
                CloudEventsFormat.BATCH, new HttpHeaders()), source("temperature", "other"),
                options("{\"typeKinds\":{\"temp\":\"temperature\"}}"));

        assertThat(parsed.records()).singleElement().satisfies(r -> {
            assertThat(r.externalId()).isEqualTo("ha/1");
            assertThat(r.kind()).isEqualTo("temperature");
            assertThat(r.geometry().getCoordinate().x).isEqualTo(4.0);
        });
        assertThat(parsed.rejections()).extracting(ItemRejection::reason).containsExactly("UNKNOWN_KIND", "INVALID_ITEM");
        assertThat(parsed.items()).isEqualTo(3);
    }

    @Test
    @DisplayName("binary mode: attributes from ce-* headers, a non-object body is wrapped as {data}")
    void binary() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ce-specversion", "1.0");
        headers.add("ce-id", "77");
        headers.add("ce-source", "urn:sdr");
        headers.add("ce-type", "frame");
        headers.add("ce-time", "2026-10-12T07:00:00Z");
        ParsedIngest parsed = format.parse(request("hello", MediaType.TEXT_PLAIN, headers), source("frame"),
                FormatOptions.empty());

        NormalizedRecord record = parsed.records().get(0);
        assertThat(record.externalId()).isEqualTo("urn:sdr/77");
        assertThat(record.payload()).containsEntry("data", "hello").containsEntry("ceType", "frame");
    }

    @Test
    @DisplayName("the handshake echoes the trimmed origin, or null when there is none")
    void handshake() {
        assertThat(CloudEventsFormat.handshakeAllowedOrigin(" eventgrid.azure.net ")).isEqualTo("eventgrid.azure.net");
        assertThat(CloudEventsFormat.handshakeAllowedOrigin("  ")).isNull();
        assertThat(CloudEventsFormat.handshakeAllowedOrigin(null)).isNull();
    }
}
