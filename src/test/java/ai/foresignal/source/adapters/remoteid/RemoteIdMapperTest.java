package ai.foresignal.source.adapters.remoteid;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.mqtt.MqttInbound;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * {@link RemoteIdMapper} and {@code local-remoteid}'s message handling (prompt 086): the captured DragonSync session maps
 * Remote ID drones, drops the DJI DroneID detection and every non-drone topic, never carries pilot or home positions
 * unless {@code keepOperatorPosition}, and refuses that parameter on a source that is not private.
 */
class RemoteIdMapperTest {

    /** The fixture's messages. */
    static List<MqttInbound> session() {
        List<MqttInbound> out = new ArrayList<>();
        for (String line : AdapterFixtures.text("local-rf", "dragonsync-session.tsv").lines().toList()) {
            int tab = line.indexOf('\t');
            out.add(new MqttInbound(line.substring(0, tab), line.substring(tab + 1).getBytes(StandardCharsets.UTF_8),
                    false));
        }
        return out;
    }

    @Test
    @DisplayName("the session: four drone records, one DJI detection dropped and counted, four other topics dropped")
    void sessionMapping() {
        SimpleMeterRegistry meters = new SimpleMeterRegistry();
        LocalRemoteIdAdapter adapter = new LocalRemoteIdAdapter(null, () -> "test", () -> meters);
        List<NormalizedRecord> records = new ArrayList<>();
        for (MqttInbound m : session()) {
            records.addAll(adapter.onMessage(m, false));
        }
        assertThat(records).hasSize(4);
        assertThat(adapter.dropped(RemoteIdMapper.Drop.DJI)).isEqualTo(1);
        assertThat(adapter.dropped(RemoteIdMapper.Drop.TOPIC)).isEqualTo(4);
        assertThat(meters.get(LocalRemoteIdAdapter.DROPPED_METRIC).tag("reason", "dji").counter().count()).isEqualTo(1.0);
        assertThat(records).noneMatch(r -> r.externalId().contains("F6Q8D244C00CL2KF"));

        NormalizedRecord wifi = records.getFirst();
        assertThat(wifi.kind()).isEqualTo(RemoteIdMapper.KIND);
        assertThat(wifi.externalId()).isEqualTo("rid:1581F5FJD239C00FS01");
        assertThat(wifi.observedAt()).isEqualTo(Instant.ofEpochSecond(1791900000L));
        assertThat(wifi.payload()).containsEntry("alt", 1702.5).containsEntry("agl", 48.0).containsEntry("speed", 6.2)
                .containsEntry("vspeed", 0.4).containsEntry("course", 91.0).containsEntry("uaType", 2)
                .containsEntry("idType", "Serial Number (ANSI/CTA-2063-A)").containsEntry("operatorId", "FIN87astrdge12k8");
        assertThat(((Point) wifi.geometry()).getX()).isEqualTo(-105.27);
        assertThat(wifi.observers()).singleElement().satisfies(o -> {
            assertThat(o.receiverId()).isEqualTo("wardragon-FSTEST001");
            assertThat(o.rssi()).isEqualTo(-61.0);
            assertThat(o.frequencyHz()).isEqualTo(2_437_000_000L);
        });

        NormalizedRecord ble = records.getLast();
        assertThat(ble.externalId()).isEqualTo("rid:AABBCCDDEEFF");
        assertThat(ble.geometry()).as("0, 0 is no fix").isNull();
        assertThat(ble.payload()).doesNotContainKeys("alt", "uaType", "operatorId");
        assertThat(ble.observers().getFirst().frequencyHz()).isNull();

        for (NormalizedRecord r : records) {
            assertThat(r.payload()).doesNotContainKeys("operatorPoint", "pilot_lat", "pilot_lon", "home_lat", "home_lon");
            assertThat(String.valueOf(r.payload())).doesNotContain("40.0141").doesNotContain("40.014");
        }
    }

    @Test
    @DisplayName("keepOperatorPosition keeps the pilot position as payload operatorPoint, never geometry; home never")
    void keepOperatorPosition() {
        MqttInbound wifi = session().getFirst();
        NormalizedRecord r = RemoteIdMapper.map(wifi.topic(), wifi.payload(), true).record();
        assertThat(r.payload()).containsEntry("operatorPoint", Map.of("lat", 40.0141, "lon", -105.2712));
        assertThat(((Point) r.geometry()).getY()).isEqualTo(40.015);
        assertThat(String.valueOf(r.payload())).doesNotContain("40.014,").doesNotContain("-105.2711");
    }

    @Test
    @DisplayName("keepOperatorPosition needs privacy: the adapter names it for PRIVATE_REQUIRED only when true")
    void privacyRequired() {
        LocalRemoteIdAdapter adapter = new LocalRemoteIdAdapter(null, () -> "test", null);
        assertThat(adapter.descriptor().privateSource()).as("the descriptor is not private").isFalse();
        assertThat(adapter.paramsRequiringPrivacy(Map.of(LocalRemoteIdAdapter.KEEP_OPERATOR_POSITION, true)))
                .containsExactly(LocalRemoteIdAdapter.KEEP_OPERATOR_POSITION);
        assertThat(adapter.paramsRequiringPrivacy(Map.of(LocalRemoteIdAdapter.KEEP_OPERATOR_POSITION, "true")))
                .containsExactly(LocalRemoteIdAdapter.KEEP_OPERATOR_POSITION);
        assertThat(adapter.paramsRequiringPrivacy(Map.of(LocalRemoteIdAdapter.KEEP_OPERATOR_POSITION, false))).isEmpty();
        assertThat(adapter.paramsRequiringPrivacy(Map.of())).isEmpty();
        assertThat(adapter.paramsRequiringPrivacy(null)).isEmpty();
    }

    @Test
    @DisplayName("drops: DJI by empty transport or DJI description off a RID link, non-RID links, untimed, malformed")
    void drops() {
        assertThat(map("{\"id\":\"drone-X1\",\"transport\":\"\",\"observed_at\":1}").drop())
                .isEqualTo(RemoteIdMapper.Drop.DJI);
        assertThat(map("{\"id\":\"drone-X1\",\"observed_at\":1}").drop()).isEqualTo(RemoteIdMapper.Drop.DJI);
        assertThat(map("{\"id\":\"drone-X1\",\"transport\":\"ISM-FHSS\",\"description\":\"DJI Mini 2 (O2)\","
                + "\"observed_at\":1}").drop()).isEqualTo(RemoteIdMapper.Drop.DJI);
        assertThat(map("{\"id\":\"drone-900FHSS-NETID-25\",\"transport\":\"ISM-FHSS\",\"observed_at\":1}").drop())
                .isEqualTo(RemoteIdMapper.Drop.NOT_REMOTE_ID);
        assertThat(map("{\"id\":\"drone-X1\",\"transport\":\"WiFi-NAN\",\"description\":\"DJI Mavic RID\","
                + "\"observed_at\":1791900000,\"lat\":1.5,\"lon\":2.5}").record())
                .as("a DJI drone broadcasting real Remote ID is kept").isNotNull();
        assertThat(map("{\"id\":\"drone-X1\",\"transport\":\"WiFi-Beacon\"}").drop()).isEqualTo(RemoteIdMapper.Drop.UNTIMED);
        assertThat(map("{\"transport\":\"WiFi-Beacon\",\"observed_at\":1}").drop()).isEqualTo(RemoteIdMapper.Drop.MALFORMED);
        assertThat(RemoteIdMapper.map("wardragon/drones", "not json".getBytes(StandardCharsets.UTF_8), false).drop())
                .isEqualTo(RemoteIdMapper.Drop.MALFORMED);
        assertThat(RemoteIdMapper.map("wardragon/drones", "[1]".getBytes(StandardCharsets.UTF_8), false).drop())
                .isEqualTo(RemoteIdMapper.Drop.MALFORMED);
        assertThat(RemoteIdMapper.isDroneTopic("wardragon/drone/drone-1/pilot_attrs")).isFalse();
        assertThat(RemoteIdMapper.isDroneTopic("wardragon/drone/drone-1")).isTrue();
        assertThat(RemoteIdMapper.isDroneTopic("site/wardragon/drones")).isTrue();
        assertThat(RemoteIdMapper.isDroneTopic(null)).isFalse();
        assertThat(RemoteIdMapper.Drop.NOT_REMOTE_ID.tag()).isEqualTo("not-remote-id");
    }

    private static RemoteIdMapper.Result map(String json) {
        return RemoteIdMapper.map("wardragon/drones", json.getBytes(StandardCharsets.UTF_8), false);
    }
}
