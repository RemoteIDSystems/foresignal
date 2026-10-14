package ai.foresignal.source.adapters.mesh;

import ai.foresignal.mqtt.MqttInbound;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/** Reads the prompt 083 fixtures: MQTT captures as topic and payload, and MeshCore hex streams as byte blocks. */
final class MeshFixtures {

    private MeshFixtures() {}

    /** The messages of a {@code topic<TAB>payload} capture. */
    static List<MqttInbound> mqtt(String sourceId, String name) {
        List<MqttInbound> out = new ArrayList<>();
        for (String line : AdapterFixtures.text(sourceId, name).split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split("\t", 2);
            out.add(new MqttInbound(parts[0], parts[1].getBytes(StandardCharsets.UTF_8), false));
        }
        return out;
    }

    /** The hex blocks of a {@code .hex} fixture, comments skipped, one byte array per block. */
    static List<byte[]> hexBlocks(String sourceId, String name) {
        List<byte[]> out = new ArrayList<>();
        for (String line : AdapterFixtures.text(sourceId, name).split("\n")) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            out.add(HexFormat.of().parseHex(line.strip()));
        }
        return out;
    }

    /** Every block of a {@code .hex} fixture, concatenated. */
    static byte[] hexStream(String sourceId, String name) {
        List<byte[]> blocks = hexBlocks(sourceId, name);
        int size = blocks.stream().mapToInt(b -> b.length).sum();
        byte[] out = new byte[size];
        int at = 0;
        for (byte[] block : blocks) {
            System.arraycopy(block, 0, out, at, block.length);
            at += block.length;
        }
        return out;
    }
}
