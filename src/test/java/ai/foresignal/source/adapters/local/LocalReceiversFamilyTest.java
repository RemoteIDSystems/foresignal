package ai.foresignal.source.adapters.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.codec.CodecRegistry;
import ai.foresignal.source.codec.GpsdCodec;
import ai.foresignal.source.codec.Sbs1Codec;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The local receiver families (prompt 069): the slot count, every descriptor valid, every one LOCAL_ONLY and
 * overridable with only LAN parameters, and each family's time base, tier, policy and privacy.
 */
class LocalReceiversFamilyTest {

    private static Map<String, SourceDescriptor> descriptors(int slots) {
        LocalReceiversFamily family = new LocalReceiversFamily(slots,
                new CodecRegistry(List.of(new Sbs1Codec(), new GpsdCodec())), null);
        return family.adapters().stream().map(SourceAdapter::descriptor)
                .collect(Collectors.toMap(SourceDescriptor::id, Function.identity()));
    }

    @Test
    @DisplayName("two slots by default: six families, twelve sources, each valid")
    void slots() {
        LocalReceiversFamily family = new LocalReceiversFamily(LocalReceivers.DEFAULT_SLOTS,
                new CodecRegistry(List.of(new Sbs1Codec(), new GpsdCodec())), null);
        assertThat(family.adapters()).hasSize(12);
        for (SourceAdapter adapter : family.adapters()) {
            assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).as(adapter.id()).isEmpty();
        }
        assertThat(descriptors(2)).containsKeys("local-adsb-1", "local-adsb-2", "local-sbs-2", "local-gpsd-1",
                "station-readsb-1", "wxstation-davis-2", "wxstation-ecowitt-1");
    }

    @Test
    @DisplayName("the slot count must be 1 to 16")
    void slotRange() {
        CodecRegistry codecs = new CodecRegistry(List.of(new Sbs1Codec(), new GpsdCodec()));
        assertThatThrownBy(() -> new LocalReceiversFamily(0, codecs, null)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new LocalReceiversFamily(17, codecs, null)).isInstanceOf(IllegalStateException.class);
        assertThat(new LocalReceiversFamily(16, codecs, null).adapters()).hasSize(96);
    }

    @Test
    @DisplayName("every source is LOCAL_ONLY, overridable, and takes its address only from LAN_URL parameters")
    void localOnly() {
        for (SourceDescriptor d : descriptors(1).values()) {
            assertThat(d.egress()).as(d.id()).isEqualTo(Egress.LOCAL_ONLY);
            assertThat(d.egressOverridable()).as(d.id()).isTrue();
            assertThat(d.params()).as(d.id()).filteredOn(p -> p.type() == ParamType.LAN_URL).hasSize(1);
            assertThat(d.params()).as(d.id()).noneMatch(p -> p.type() == ParamType.STRING
                    && p.name().toLowerCase().contains("host"));
        }
    }

    @Test
    @DisplayName("time bases, tiers and policies per family")
    void families() {
        Map<String, SourceDescriptor> d = descriptors(1);
        SourceDescriptor sbs = d.get("local-sbs-1");
        assertThat(sbs.mode()).isEqualTo(Mode.STREAMING);
        assertThat(sbs.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(sbs.historyTier()).isEqualTo(HistoryTier.TRACK);
        assertThat(sbs.param("endpoint").allowed()).containsExactly("tcp");

        SourceDescriptor gpsd = d.get("local-gpsd-1");
        assertThat(gpsd.privateSource()).isTrue();
        assertThat(gpsd.defaultIngestPolicy()).isEqualTo(new IngestPolicy.Latest(Duration.ofMinutes(20),
                Duration.ofSeconds(15)));
        assertThat(gpsd.recordTtl()).isEqualTo(Duration.ofMinutes(30));
        assertThat(gpsd.significantFields()).containsExactly("mode", "fixQuality", "navDegraded");

        SourceDescriptor station = d.get("station-readsb-1");
        assertThat(station.kinds()).containsExactly("receiver-status");
        assertThat(station.defaultInterval()).isEqualTo(Duration.ofSeconds(60));
        assertThat(station.significantFields()).containsExactly("state");
        assertThat(station.recordTtl()).isEqualTo(Duration.ofMinutes(30));

        SourceDescriptor davis = d.get("wxstation-davis-1");
        assertThat(davis.minInterval()).as("Davis: never more often than every 10 s").isEqualTo(Duration.ofSeconds(10));
        assertThat(davis.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(davis.recordTtl()).isEqualTo(Duration.ofMinutes(10));
        assertThat(davis.fieldSpecs().get("tempC").unit()).isEqualTo("Cel");
        assertThat(davis.fieldSpecs().get("pressureHpa").unit()).isEqualTo("hPa");

        SourceDescriptor ecowitt = d.get("wxstation-ecowitt-1");
        assertThat(ecowitt.description()).startsWith("Experimental");
        assertThat(ecowitt.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
    }
}
