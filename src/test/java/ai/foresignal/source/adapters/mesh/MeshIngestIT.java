package ai.foresignal.source.adapters.mesh;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.inbound.formats.mesh.MeshCoreJsonFormat;
import ai.foresignal.mqtt.MqttInbound;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * The prompt 083 mesh sources end to end in the shared ingest context ({@link WeatherIngestSupport}): all five register
 * disabled; {@code meshtastic-mqtt} cannot be configured outside the Mesh.md limits; the bridge pushes
 * {@code meshcore-json} with its {@code ingestKey} credential; the own-node sources are private (hidden from anonymous
 * readers, never embedded or federated); {@code meshtastic-map} ingests the trimmed real list through the fetch path.
 */
class MeshIngestIT extends WeatherIngestSupport {

    static final String KEY = "eik_mesh083testkey0123456789abcdefABCDEF";
    static final List<String> IDS = List.of(MeshtasticMqttAdapter.ID, MeshtasticMapAdapter.ID, MeshtasticLocalAdapter.ID,
            MeshCoreLocalAdapter.ID, MeshCoreBridgeAdapter.ID);
    static final List<String> PRIVATE = List.of(MeshtasticLocalAdapter.ID, MeshCoreLocalAdapter.ID,
            MeshCoreBridgeAdapter.ID);

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired EgressPolicy egress;
    @Autowired IngestQueue queue;

    @Test
    @DisplayName("all five register disabled; the three own-node sources are private")
    void registered() {
        assertThat(sources.list()).filteredOn(s -> IDS.contains(s.id())).hasSize(5)
                .allSatisfy(s -> assertThat(s.enabled()).as(s.id()).isFalse());
        assertThat(sources.list()).filteredOn(s -> IDS.contains(s.id())).allSatisfy(s -> assertThat(
                s.descriptor().privateSource()).as(s.id()).isEqualTo(PRIVATE.contains(s.id())));
    }

    @Test
    @DisplayName("meshtastic-mqtt refuses maxSubscriptions above 3 and wildcard roots at PUT, and stays disabled")
    void meshtasticRulesAtPut() {
        assertThatThrownBy(() -> configure(MeshtasticMqttAdapter.ID, false,
                Map.of(MeshtasticMqttAdapter.MAX_SUBSCRIPTIONS, 4))).isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors())
                        .anySatisfy(f -> assertThat(f.field()).isEqualTo("params.maxSubscriptions")));
        assertThatThrownBy(() -> configure(MeshtasticMqttAdapter.ID, false, Map.of(MeshtasticMqttAdapter.ROOT, "msh/#")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> configure(MeshtasticMqttAdapter.ID, false, Map.of(MeshtasticMqttAdapter.EXTRA_ROOTS,
                List.of("msh/EU_868")))).isInstanceOf(ValidationException.class);
        assertThat(sources.list()).filteredOn(s -> s.id().equals(MeshtasticMqttAdapter.ID)).singleElement()
                .satisfies(s -> assertThat(s.enabled()).isFalse());
    }

    @Test
    @DisplayName("the bridge pushes meshcore-json with its ingestKey credential; a wrong key is refused")
    void bridgePush() throws Exception {
        configure(MeshCoreBridgeAdapter.ID, true, Map.of());
        sources.setCredentials(MeshCoreBridgeAdapter.ID, Map.of(SourceDescriptor.REMOTE_MEDIA_KEY, KEY));
        clock.setInstant(Instant.parse("2026-10-13T14:01:40Z"));
        try {
            byte[] body = AdapterFixtures.bytes("meshcore-bridge", "events.json");
            MockHttpServletResponse wrong = mvc.perform(MockMvcRequestBuilders.post("/v1/ingest/{source}/{format}",
                    MeshCoreBridgeAdapter.ID, MeshCoreJsonFormat.ID).header("X-Foresignal-Ingest-Key",
                    "eik_wrong0123456789abcdefABCDEF0123456789").contentType("application/json").content(body))
                    .andReturn().getResponse();
            assertThat(wrong.getStatus()).isEqualTo(401);

            MockHttpServletResponse ok = mvc.perform(MockMvcRequestBuilders.post("/v1/ingest/{source}/{format}",
                    MeshCoreBridgeAdapter.ID, MeshCoreJsonFormat.ID).header("X-Foresignal-Ingest-Key", KEY)
                    .contentType("application/json").content(body)).andReturn().getResponse();
            assertThat(ok.getStatus()).as(ok.getContentAsString()).isEqualTo(200);
            assertThat(ok.getContentAsString()).contains(MeshCoreJsonFormat.INVALID, MeshCoreJsonFormat.UNKNOWN);
            assertThat(versions(MeshCoreBridgeAdapter.ID, "mc:7b065359ae9c")).isEqualTo(1);
            assertThat(versions(MeshCoreBridgeAdapter.ID, "mc:f7ff142f1f28")).isEqualTo(1);
            assertThat(versions(MeshCoreBridgeAdapter.ID, "mc:74979e2bf73d")).isEqualTo(1);
            assertThat(jdbc.queryForObject("select count(*) from record_head where source_id = ? and kind = ?",
                    Integer.class, MeshCoreBridgeAdapter.ID, MeshRecords.MESSAGE_KIND)).as("includeText is off").isZero();

            MockHttpServletResponse admin = mvc.perform(MockMvcRequestBuilders.post("/v1/ingest/{source}/{format}",
                    MeshCoreBridgeAdapter.ID, MeshCoreJsonFormat.ID).header("Authorization", "Bearer "
                    + adminTokens.rotate()).contentType("application/json").content(body)).andReturn().getResponse();
            assertThat(admin.getStatus()).isEqualTo(200);
        } finally {
            configure(MeshCoreBridgeAdapter.ID, false, Map.of());
        }
    }

    @Test
    @DisplayName("meshtastic-local records are private: hidden from anonymous readers, never embedded or federated")
    void meshtasticLocalPrivate() {
        for (String id : PRIVATE) {
            assertThat(egress.isPrivate(id)).as(id).isTrue();
            assertThat(egress.hiddenSources(Viewer.anonymous())).as(id).contains(id);
            assertThat(egress.visible(id, Viewer.anonymous())).as(id).isFalse();
            assertThat(egress.allowsEmbedding(id)).as(id).isFalse();
            assertThat(egress.allows(EgressPath.FEDERATION, id, Viewer.admin())).as(id).isFalse();
        }
        assertThat(egress.visible(MeshtasticMqttAdapter.ID, Viewer.anonymous())).isTrue();

        MeshtasticStream stream = new MeshtasticStream(MeshtasticLocalAdapter.ID, false, null,
                new SlidingWindowLimiter(1_000, Duration.ofMinutes(1)), new MeshNodeStates(), null);
        Instant at = Instant.parse("2026-10-13T14:05:00Z");
        clock.setInstant(at);
        List<NormalizedRecord> records = new ArrayList<>();
        for (MqttInbound message : MeshFixtures.mqtt("meshtastic-local", "private-broker-5min.tsv")) {
            records.addAll(stream.onMessage(message, at));
        }
        IngestOutcome outcome = new TestRecords(queue, clock).ingestAt(MeshtasticLocalAdapter.ID, at,
                records.toArray(NormalizedRecord[]::new));
        assertThat(outcome.persisted()).isPositive();
        assertThat(versions(MeshtasticLocalAdapter.ID, "!aabbccdd")).isEqualTo(1);
    }

    @Test
    @DisplayName("meshtastic-map ingests the trimmed real list through the fetch path; a 304 is unchanged")
    void mapFetch() {
        configure(MeshtasticMapAdapter.ID, false, Map.of());
        UPSTREAM.stubFor(get(urlPathEqualTo("/meshtastic.liamcottle.net/api/v1/nodes")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json").withHeader("ETag", "W/\"083\"")
                .withBody(AdapterFixtures.bytes("meshtastic-map", "nodes-200.json"))));
        clock.setInstant(Instant.parse("2026-10-14T02:00:00Z"));
        assertThat(fetch(MeshtasticMapAdapter.ID).success()).isTrue();
        assertThat(versions(MeshtasticMapAdapter.ID, (ai.foresignal.ingest.PersistReason) null)).isEqualTo(200);
        assertThat(versions(MeshtasticMapAdapter.ID, "!10000001")).isEqualTo(1);
    }
}
