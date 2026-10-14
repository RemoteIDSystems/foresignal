package ai.foresignal.codec.ais;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Multi-sentence reassembly (prompt 085): two- and three-part type 5, out of order, interleaved channels, TAG groups,
 * the 2 s expiry and the bound on open groups.
 */
class FragmentAssemblerTest {

    private static final Instant T = Instant.parse("2026-10-13T13:20:00Z");

    private static NmeaSentence parse(String line) {
        return ((NmeaSentence.Parsed) NmeaSentence.parse(line)).sentence();
    }

    /** The golden type 5 payload split into {@code parts} sentences with sequential id {@code seq} on {@code channel}. */
    private static List<NmeaSentence> split(int parts, int seq, String channel) {
        AisMessage m = AisGoldens.decode("type5");
        SixBit.Armored armored = AisEncoder.encode(m);
        String payload = armored.payload();
        int size = (payload.length() + parts - 1) / parts;
        NmeaSentence[] out = new NmeaSentence[parts];
        for (int i = 0; i < parts; i++) {
            String part = payload.substring(i * size, Math.min(payload.length(), (i + 1) * size));
            out[i] = new NmeaSentence("AIVDM", parts, i + 1, seq, channel, part, i == parts - 1 ? armored.fillBits() : 0);
        }
        return List.of(out);
    }

    @Test
    @DisplayName("a two-part type 5 completes on its second sentence and decodes to the golden")
    void twoParts() {
        FragmentAssembler assembler = new FragmentAssembler();
        List<String> lines = AisGoldens.sentences("type5");

        assertThat(assembler.add(parse(lines.get(0)), null, T)).isEmpty();
        Optional<FragmentAssembler.Assembled> done = assembler.add(parse(lines.get(1)), null, T.plusMillis(20));

        assertThat(done).isPresent();
        assertThat(done.get().armored().fillBits()).isEqualTo(2);
        assertThat(done.get().firstReceivedAt()).isEqualTo(T);
        assertThat(AisDecoder.decode(done.get().armored())).isEqualTo(AisGoldens.decode("type5"));
        assertThat(assembler.completed()).isEqualTo(1);
        assertThat(assembler.openGroups()).isZero();
    }

    @Test
    @DisplayName("a three-part type 5 completes in any order")
    void threePartsOutOfOrder() {
        FragmentAssembler assembler = new FragmentAssembler();
        List<NmeaSentence> parts = split(3, 4, "B");

        assertThat(assembler.add(parts.get(2), null, T)).isEmpty();
        assertThat(assembler.add(parts.get(0), null, T)).isEmpty();
        Optional<FragmentAssembler.Assembled> done = assembler.add(parts.get(1), null, T);

        assertThat(done).isPresent();
        assertThat(AisDecoder.decode(done.get().armored())).isEqualTo(AisGoldens.decode("type5"));
    }

    @Test
    @DisplayName("the golden out-of-order pair reassembles")
    void goldenOutOfOrder() {
        assertThat(AisGoldens.decode("type5-out-of-order")).isEqualTo(AisGoldens.decode("type5"));
    }

    @Test
    @DisplayName("the same sequential id on channels A and B forms two groups, interleaved")
    void interleavedChannels() {
        FragmentAssembler assembler = new FragmentAssembler();
        List<NmeaSentence> a = split(2, 3, "A");
        List<NmeaSentence> b = split(2, 3, "B");

        assertThat(assembler.add(a.get(0), null, T)).isEmpty();
        assertThat(assembler.add(b.get(0), null, T)).isEmpty();
        assertThat(assembler.openGroups()).isEqualTo(2);
        assertThat(assembler.add(b.get(1), null, T)).hasValueSatisfying(x -> assertThat(x.channel()).isEqualTo("B"));
        assertThat(assembler.add(a.get(1), null, T)).hasValueSatisfying(x -> assertThat(x.channel()).isEqualTo("A"));
        assertThat(assembler.completed()).isEqualTo(2);
    }

    @Test
    @DisplayName("TAG group ids key the group, and the first fragment's TAG block is kept")
    void tagGroups() {
        FragmentAssembler assembler = new FragmentAssembler();
        List<NmeaSentence> parts = split(2, 0, "A");
        TagBlock first = TagBlock.strip(TagBlock.format(Map.of("g", "1-2-77", "c", "1791900000", "s", "st")) + "x").tag();
        TagBlock second = TagBlock.strip(TagBlock.format(Map.of("g", "2-2-77")) + "x").tag();

        assertThat(assembler.add(parts.get(1), second, T)).isEmpty();
        Optional<FragmentAssembler.Assembled> done = assembler.add(parts.get(0), first, T);

        assertThat(done).hasValueSatisfying(x -> {
            assertThat(x.tag().source()).isEqualTo("st");
            assertThat(x.tag().time()).isEqualTo(Instant.ofEpochSecond(1_791_900_000L));
        });
    }

    @Test
    @DisplayName("a group not completed within 2 s is dropped and counted; its late fragment starts a new group")
    void expiry() {
        AtomicInteger expired = new AtomicInteger();
        FragmentAssembler assembler = new FragmentAssembler(new FragmentAssembler.Listener() {
            @Override
            public void expired(int count) {
                expired.addAndGet(count);
            }
        });
        List<NmeaSentence> parts = split(2, 1, "A");

        assembler.add(parts.get(0), null, T);
        assertThat(assembler.add(parts.get(1), null, T.plus(Duration.ofMillis(2001)))).isEmpty();

        assertThat(assembler.expired()).isEqualTo(1);
        assertThat(expired).hasValue(1);
        assertThat(assembler.openGroups()).isEqualTo(1);
        assembler.expire(T.plusSeconds(10));
        assertThat(assembler.openGroups()).isZero();
        assertThat(assembler.expired()).isEqualTo(2);
    }

    @Test
    @DisplayName("opening a group beyond the bound drops the oldest, counted as expired")
    void bound() {
        FragmentAssembler assembler = new FragmentAssembler(null, 3, Duration.ofSeconds(2));
        for (int seq = 0; seq < 5; seq++) {
            assembler.add(split(2, seq, "A").get(0), null, T);
        }
        assertThat(assembler.openGroups()).isEqualTo(3);
        assertThat(assembler.expired()).isEqualTo(2);
        assertThat(assembler.add(split(2, 0, "A").get(1), null, T)).as("seq 0 was dropped").isEmpty();
        assertThat(assembler.add(split(2, 4, "A").get(1), null, T)).isPresent();
        assertThat(FragmentAssembler.MAX_OPEN).isEqualTo(1_000);
    }

    @Test
    @DisplayName("a single sentence passes straight through")
    void single() {
        FragmentAssembler assembler = new FragmentAssembler();
        assertThat(assembler.add(AisGoldens.sentence("type1", 0), null, T)).isPresent();
        assertThat(assembler.completed()).as("only multi-sentence messages are counted").isZero();
    }
}
