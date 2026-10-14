package ai.foresignal.codec.ais;

import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The hand-made golden sentences of {@code fixtures/ais/golden-sentences.tsv} (prompt 085), by name: one line per
 * message, its sentences tab-separated. The expected values in the tests were read independently with pyais 3.3.1 in
 * a scratch environment (see the fixture README); no code was taken from it.
 */
final class AisGoldens {

    private static final Map<String, List<String>> GOLDENS = load();

    private AisGoldens() {}

    /** The sentences of {@code name}. */
    static List<String> sentences(String name) {
        List<String> s = GOLDENS.get(name);
        if (s == null) {
            throw new IllegalArgumentException("no golden named " + name);
        }
        return s;
    }

    /** Every golden name. */
    static List<String> names() {
        return List.copyOf(GOLDENS.keySet());
    }

    /** The golden's message, reassembled and decoded. */
    static AisMessage decode(String name) {
        FragmentAssembler assembler = new FragmentAssembler();
        Optional<FragmentAssembler.Assembled> done = Optional.empty();
        for (String line : sentences(name)) {
            NmeaSentence.Result result = NmeaSentence.parse(line);
            if (!(result instanceof NmeaSentence.Parsed parsed)) {
                throw new IllegalStateException(name + " has an unusable sentence: " + result);
            }
            done = assembler.add(parsed.sentence(), null, Instant.EPOCH);
        }
        return AisDecoder.decode(done.orElseThrow(() -> new IllegalStateException(name + " is incomplete")).armored());
    }

    /** The golden's first sentence parsed. */
    static NmeaSentence sentence(String name, int index) {
        return ((NmeaSentence.Parsed) NmeaSentence.parse(sentences(name).get(index))).sentence();
    }

    private static Map<String, List<String>> load() {
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (String line : AdapterFixtures.text("ais", "golden-sentences.tsv").split("\n")) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.strip().split("\t");
            List<String> sentences = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) {
                sentences.add(parts[i]);
            }
            out.put(parts[0], List.copyOf(sentences));
        }
        return out;
    }
}
