package ai.foresignal.source.adapters.space.localsonde;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.CollectorProperties;
import ai.foresignal.collect.FetchContextFactory;
import ai.foresignal.collect.FetchLogWriter;
import ai.foresignal.collect.HealthTracker;
import ai.foresignal.collect.StreamingSupervisor;
import ai.foresignal.collect.socket.SocketMetrics;
import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@code local-sonde} end to end (prompt 077): the real adapter from the registry under a real
 * {@link StreamingSupervisor} (200 ms flush, 100 ms ladder, as in {@code AisStreamIngestIT}), prompt 069's UDP
 * transport bound on loopback, the {@code sonde-udp} codec and the real writer. A datagram from an allowed sender
 * becomes a {@code radiosonde} record; one from a sender outside {@code allowSenders} is dropped and counted; a
 * public {@code allowSenders} entry is refused when the settings are saved.
 */
@Timeout(value = 120, unit = TimeUnit.SECONDS)
class LocalSondeIT extends WeatherIngestSupport {

    static final String ID = LocalSondeAdapter.ID;
    static final Duration WAIT = Duration.ofSeconds(20);

    @Autowired SourceRegistry registry;
    @Autowired FetchContextFactory contexts;
    @Autowired HealthTracker health;
    @Autowired IngestQueue queue;
    @Autowired FetchLogWriter fetchLog;
    @Autowired ForesignalVersion version;
    @Autowired MeterRegistry meters;
    @Autowired EgressPolicy egress;

    StreamingSupervisor supervisor;
    int port;

    @BeforeEach
    void setUp() throws Exception {
        clock.setInstant(Instant.parse("2026-10-13T20:41:00Z"));
        backoff.onSuccess(ID);
        try (DatagramSocket probe = new DatagramSocket(0, InetAddress.getLoopbackAddress())) {
            port = probe.getLocalPort();
        }
        CollectorProperties.Stream stream = new CollectorProperties.Stream(
                Duration.ofMillis(200), 2000, 50_000, Duration.ofSeconds(1), Duration.ofSeconds(1),
                List.of(Duration.ofMillis(100)), Duration.ofMinutes(10), Duration.ofHours(1));
        CollectorProperties properties = new CollectorProperties(
                false, Duration.ofSeconds(2), Duration.ofSeconds(10), Duration.ofSeconds(30), stream);
        supervisor = new StreamingSupervisor(
                registry, sources, contexts, health, queue, fetchLog, version, meters, properties, clock);
    }

    @AfterEach
    void tearDown() {
        supervisor.stopAll(Duration.ofSeconds(10));
        backoff.onSuccess(ID);
    }

    private void settings(List<String> allowSenders) {
        sources.update(ID, new SourceSettingsUpdate(false, null, Map.of(LocalSondeAdapter.ENDPOINT_PARAM,
                "udp://127.0.0.1:" + port, LocalSondeAdapter.ALLOW_SENDERS_PARAM, allowSenders),
                Budget.of(600, 100_000), null, false, null, false));
    }

    private void send(String datagram) throws Exception {
        try (DatagramSocket sender = new DatagramSocket(0, InetAddress.getLoopbackAddress())) {
            byte[] bytes = datagram.getBytes(StandardCharsets.UTF_8);
            sender.send(new DatagramPacket(bytes, bytes.length, new InetSocketAddress(InetAddress.getLoopbackAddress(),
                    port)));
        }
    }

    /** A {@code PAYLOAD_SUMMARY} of the fixture's first sonde under another serial. */
    private static String summary(String serial) {
        return AdapterFixtures.text("local-sonde", "datagrams.jsonl").split("\n")[0].replace("S1234567", serial);
    }

    private double rejected() {
        Counter c = meters.find(SocketMetrics.UDP_REJECTED).tag("source", ID).counter();
        return c == null ? 0.0 : c.count();
    }

    @Test
    @DisplayName("a datagram from an allowed sender becomes a radiosonde record with the station as observer")
    void allowedSender() {
        settings(List.of("127.0.0.1"));
        supervisor.start(ID);
        // UDP before the bind is lost: keep sending until the record exists.
        await().atMost(WAIT).pollInterval(Duration.ofMillis(250)).until(() -> {
            send(summary("U0770001"));
            return versions(ID, "U0770001") >= 1;
        });
        assertThat(jdbc.queryForObject("select payload->>'type' from record_head where source_id = ? and"
                + " external_id = ?", String.class, ID, "U0770001")).isEqualTo("RS41-SGP");
        assertThat(live("observed_at", ID, "U0770001")).isEqualTo(Instant.parse("2026-10-13T20:40:00Z"));
        assertThat(egress.isLocalOnly(ID)).isTrue();
    }

    @Test
    @DisplayName("a sender outside allowSenders is dropped and counted; nothing is stored")
    void disallowedSender() throws Exception {
        settings(List.of("10.77.0.0/16"));
        supervisor.start(ID);
        double before = rejected();
        await().atMost(WAIT).pollInterval(Duration.ofMillis(250)).until(() -> {
            send(summary("U0770002"));
            return rejected() > before;
        });
        Thread.sleep(500);
        assertThat(versions(ID, "U0770002")).isZero();
    }

    @Test
    @DisplayName("a public allowSenders entry is refused at PUT, and so is a missing allowSenders")
    void publicSenderRefused() {
        assertThatThrownBy(() -> settings(List.of("8.8.8.8"))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> settings(List.of("192.168.1.0/24", "203.0.113.7"))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> sources.update(ID, new SourceSettingsUpdate(false, null,
                Map.of(LocalSondeAdapter.ENDPOINT_PARAM, "udp://127.0.0.1:" + port), Budget.of(600, 100_000), null,
                false, null, false))).isInstanceOf(ValidationException.class);
    }
}
