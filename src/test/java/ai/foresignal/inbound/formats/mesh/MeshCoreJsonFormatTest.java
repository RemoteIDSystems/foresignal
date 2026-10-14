package ai.foresignal.inbound.formats.mesh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.ItemRejection;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.adapters.mesh.MeshCoreBridgeAdapter;
import ai.foresignal.source.adapters.mesh.MeshMessageBus;
import ai.foresignal.source.adapters.mesh.MeshRecords;
import ai.foresignal.source.adapters.mesh.MeshTextMessage;
import ai.foresignal.source.adapters.mesh.RepeaterHealth;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.http.MediaType;

/**
 * The {@code meshcore-json} format (prompt 083) over the bridge fixture: the same records as the native link (contacts,
 * the companion, live adverts with repeater health), messages on the bus, two items rejected on their own, and a body
 * that is not JSON refused as a whole.
 */
class MeshCoreJsonFormatTest {

    private static final Instant RECEIVED = Instant.ofEpochSecond(1_791_900_100L);

    private final MeshMessageBus bus = new MeshMessageBus();
    private final List<MeshTextMessage> heard = new ArrayList<>();
    private final MeshCoreJsonFormat format;

    MeshCoreJsonFormatTest() {
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        beans.addBean("bus", bus);
        format = new MeshCoreJsonFormat(beans.getBeanProvider(SourceService.class), beans.getBeanProvider(MeshMessageBus.class));
        bus.subscribe(heard::add);
    }

    private ParsedIngest parse(byte[] body) {
        return format.parse(new IngestRequest(body, MediaType.APPLICATION_JSON, null, Map.of(), RECEIVED, null),
                new MeshCoreBridgeAdapter().descriptor(), FormatOptions.empty());
    }

    @Test
    @DisplayName("the bridge fixture: 6 node records, 2 bus messages, the key-only advert and the unknown type rejected")
    void fixture() {
        ParsedIngest parsed = parse(AdapterFixtures.bytes("meshcore-bridge", "events.json"));
        assertThat(parsed.items()).isEqualTo(10);
        assertThat(parsed.records()).hasSize(6).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo(MeshRecords.NODE_KIND);
            assertThat(r.observedAt()).isNull();
        });
        assertThat(parsed.rejections()).extracting(ItemRejection::index, ItemRejection::reason).containsExactly(
                org.assertj.core.groups.Tuple.tuple(8, MeshCoreJsonFormat.INVALID),
                org.assertj.core.groups.Tuple.tuple(9, MeshCoreJsonFormat.UNKNOWN));
        assertThat(heard).extracting(MeshTextMessage::text).containsExactly("Synthetic Hiker: #E synthetic report",
                "synthetic direct");
        assertThat(heard.get(0).channelName()).isEqualTo("Ops");
        assertThat(heard.get(1).fromId()).isEqualTo("mc:7b065359ae9c");
        assertThat(heard.get(1).toId()).isEqualTo("mc:f7ff142f1f28");

        NormalizedRecord companion = parsed.records().get(0);
        assertThat(companion.externalId()).isEqualTo("mc:f7ff142f1f28");
        assertThat(companion.payload()).containsEntry("longName", "Synthetic Companion").containsEntry("nodeType", "chat");
        NormalizedRecord listed = parsed.records().get(2);
        assertThat(listed.payload()).containsEntry("advertStatus", RepeaterHealth.LATE).doesNotContainKey("lastAdvertAt");
        NormalizedRecord advert = parsed.records().get(4);
        assertThat(advert.externalId()).isEqualTo("mc:5f6a9ca3f1cc");
        assertThat(advert.payload()).containsEntry("advertStatus", RepeaterHealth.OK)
                .containsEntry("lastAdvertAt", RECEIVED.toString());
        NormalizedRecord sensor = parsed.records().get(5);
        assertThat(sensor.payload()).containsEntry("nodeType", "sensor");
        assertThat(sensor.geometry().getCoordinate().y).isEqualTo(37.79);
        assertThat(parsed.records().get(2).geometry()).as("0,0 is no position").isNull();
    }

    @Test
    @DisplayName("a single event body is accepted; a body that is not JSON or not an event is a 400")
    void shapes() {
        ParsedIngest one = parse(("{\"type\":\"contact_msg\",\"data\":{\"pubkey_prefix\":\"a1b2c3d4e5f6\",\"text\":\"x\","
                + "\"sender_timestamp\":1791900000}}").getBytes(StandardCharsets.UTF_8));
        assertThat(one.items()).isEqualTo(1);
        assertThat(one.records()).isEmpty();
        assertThat(heard).hasSize(1);
        assertThatThrownBy(() -> parse("not json".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> parse("[1]".getBytes(StandardCharsets.UTF_8))).isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("events missing what their type needs are rejected one by one")
    void invalidEvents() {
        String body = "{\"events\":[{\"data\":{}},{\"type\":\"contact\",\"data\":{\"public_key\":\"zz\",\"type\":1}},"
                + "{\"type\":\"self_info\",\"data\":{}},{\"type\":\"channel_msg\",\"data\":{\"text\":\"x\"}},"
                + "{\"type\":\"contact_msg\",\"data\":{\"pubkey_prefix\":\"xyz\",\"text\":\"x\",\"sender_timestamp\":1}}]}";
        ParsedIngest parsed = parse(body.getBytes(StandardCharsets.UTF_8));
        assertThat(parsed.records()).isEmpty();
        assertThat(parsed.rejections()).hasSize(5).allSatisfy(r -> assertThat(r.reason()).isEqualTo(MeshCoreJsonFormat.INVALID));
        assertThat(heard).isEmpty();
    }

    @Test
    @DisplayName("the format's id, media type and description")
    void metadata() {
        assertThat(format.id()).isEqualTo("meshcore-json");
        assertThat(format.mediaTypes()).containsExactly(MediaType.APPLICATION_JSON);
        assertThat(format.description()).contains("MeshCore");
    }
}
