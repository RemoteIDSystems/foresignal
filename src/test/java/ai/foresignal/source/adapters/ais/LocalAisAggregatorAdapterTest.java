package ai.foresignal.source.adapters.ais;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.codec.AivdmSocketCodec;
import ai.foresignal.source.formats.ais.VesselDescriptors;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code local-ais-aggregator} (prompt R2-020): a {@code local-ais-} source with the slots' decoding, whose endpoint
 * may also be a UDP listener; a datagram carrying several NMEA lines decodes to the same records as the same lines
 * over TCP.
 */
class LocalAisAggregatorAdapterTest {

    private static final Instant NOW = Instant.parse("2026-10-26T12:00:00Z");

    private final LocalAisAggregatorAdapter adapter = new LocalAisAggregatorAdapter(new AivdmSocketCodec(() -> null));

    @Test
    @DisplayName("a datagram of many lines decodes line by line, like the TCP stream")
    void datagramOfManyLines() {
        LocalAisAdapter.Connection connection = adapter.newConnection(LocalAisAggregatorAdapter.ID,
                new ValidatedParams(Map.of(), Map.of()));

        List<NormalizedRecord> records = LocalAisAggregatorAdapter.decode(connection,
                AdapterFixtures.bytes("ais", "aiscatcher-stream.txt"), NOW);

        assertThat(records).hasSize(KystverketAdapterTest.FIXTURE_RECORDS);
        assertThat(records).allSatisfy(r -> assertThat(r.kind()).isEqualTo(VesselDescriptors.KIND));
    }

    @Test
    @DisplayName("blank lines and CRLF endings in a datagram are ignored")
    void blankLinesAndCrlf() {
        LocalAisAdapter.Connection connection = adapter.newConnection(LocalAisAggregatorAdapter.ID,
                new ValidatedParams(Map.of(), Map.of()));
        byte[] datagram = "\r\n!AIVDM,1,1,,A,13m=TNPtAsPh4;0RAV`8h6g22000,0*6B\r\n\r\n"
                .getBytes(StandardCharsets.US_ASCII);

        assertThat(LocalAisAggregatorAdapter.decode(connection, datagram, NOW)).hasSize(1);
    }

    @Test
    @DisplayName("allowSenders keeps the entries that parse")
    void senders() {
        assertThat(LocalAisAggregatorAdapter.senders(List.of("192.168.1.30", "10.0.0.0/8", "nonsense"))).hasSize(2);
        assertThat(LocalAisAggregatorAdapter.senders(null)).isEmpty();
    }

    @Test
    @DisplayName("the descriptor: tcp or udp LAN endpoint, allowSenders, LOCAL_ONLY and overridable, local-ais- prefix")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.id()).isEqualTo("local-ais-aggregator").startsWith(LocalAisAdapter.ID_PREFIX);
        assertThat(d.mode()).isEqualTo(Mode.STREAMING);
        assertThat(d.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(d.egressOverridable()).isTrue();
        assertThat(d.allowedHosts()).isEmpty();
        assertThat(d.param(LocalAisAdapter.ENDPOINT).type()).isEqualTo(ParamType.LAN_URL);
        assertThat(d.param(LocalAisAggregatorAdapter.ALLOW_SENDERS).type()).isEqualTo(ParamType.STRING_LIST);
        assertThat(d.kinds()).containsExactlyInAnyOrder("vessel", "ais-receiver");
    }
}
