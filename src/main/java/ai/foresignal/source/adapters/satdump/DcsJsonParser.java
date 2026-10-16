package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.adapters.satdump.SatDumpFiles.FormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * A lenient reader of SatDump's {@code parse_dcs} JSON files (prompt 107). Unknown keys are ignored and
 * common spellings are accepted, because the exact layout depends on the pinned SatDump version (the
 * fixture per version records it). One file may hold one object or an array of messages.
 *
 * <p>Recognised keys (first present wins): {@code address}/{@code address_hex}/{@code dcp_address};
 * {@code time}/{@code timestamp}/{@code message_time} (ISO-8601 or seconds/ms since the epoch);
 * {@code channel}; {@code signal}/{@code signal_strength}/{@code signal_dbm}; {@code freq_offset}/
 * {@code frequency_offset}; {@code data}/{@code data_hex}/{@code bytes} (byte array or hex string);
 * {@code ascii}/{@code text}; {@code failure}/{@code failure_code}.
 */
public final class DcsJsonParser {

    /** The largest DCS data kept, bytes. */
    public static final int MAX_DATA_BYTES = 2048;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private DcsJsonParser() {}

    /**
     * One DCS message.
     *
     * @param address the DCP address, upper-cased hexadecimal (or decimal digits)
     * @param time the message time, or null
     * @param channel the channel, or null
     * @param signalDbm the signal strength, or null
     * @param freqOffsetHz the frequency offset, or null
     * @param data the message data, at most {@value #MAX_DATA_BYTES} bytes
     * @param ascii the data as printable ASCII, or null
     * @param failureCode the failure code, or null
     */
    public record Message(String address, Instant time, Integer channel, Double signalDbm, Double freqOffsetHz,
            byte[] data, String ascii, Integer failureCode) {}

    /** Parses a file, returning every message it holds. */
    public static List<Message> parse(byte[] json) throws FormatException {
        if (json == null || json.length == 0) {
            return List.of();
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (RuntimeException e) {
            throw new FormatException("DCS JSON is not valid: " + e.getMessage());
        }
        List<Message> out = new ArrayList<>();
        if (root.isArray()) {
            for (JsonNode node : root) {
                Message m = message(node);
                if (m != null) {
                    out.add(m);
                }
            }
        } else if (root.isObject()) {
            JsonNode messages = first(root, "messages", "dcs", "data");
            if (messages != null && messages.isArray() && !root.has("address") && !root.has("address_hex")) {
                for (JsonNode node : messages) {
                    Message m = message(node);
                    if (m != null) {
                        out.add(m);
                    }
                }
            } else {
                Message m = message(root);
                if (m != null) {
                    out.add(m);
                }
            }
        }
        return out;
    }

    private static Message message(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        String address = text(node, "address", "address_hex", "dcp_address", "dcpAddress");
        if (address == null) {
            return null;
        }
        address = address.strip().toUpperCase(Locale.ROOT);
        Instant time = instant(node, "time", "timestamp", "message_time", "messageTime");
        Integer channel = integer(node, "channel", "chan");
        Double signal = number(node, "signal_dbm", "signal", "signal_strength", "signalStrength", "snr");
        Double freq = number(node, "freq_offset", "frequency_offset", "frequencyOffset", "offset");
        byte[] data = data(node);
        String ascii = text(node, "ascii", "text", "data_ascii");
        if (ascii == null && data != null) {
            ascii = printable(data);
        }
        Integer failure = integer(node, "failure_code", "failure", "failureCode");
        return new Message(address, time, channel, signal, freq, data == null ? new byte[0] : data, ascii, failure);
    }

    private static byte[] data(JsonNode node) {
        JsonNode value = first(node, "bytes", "data", "data_hex", "dataHex", "hex");
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isArray()) {
            byte[] out = new byte[Math.min(value.size(), MAX_DATA_BYTES)];
            for (int i = 0; i < out.length; i++) {
                out[i] = (byte) (value.get(i).asInt(0) & 0xFF);
            }
            return out;
        }
        if (value.isString()) {
            String s = value.asString().replaceAll("[^0-9A-Fa-f]", "");
            if (s.length() % 2 == 1) {
                s = "0" + s;
            }
            byte[] out = new byte[Math.min(s.length() / 2, MAX_DATA_BYTES)];
            for (int i = 0; i < out.length; i++) {
                try {
                    out[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return out;
        }
        return null;
    }

    /** Printable ASCII only, capped. */
    private static String printable(byte[] data) {
        StringBuilder s = new StringBuilder(data.length);
        for (byte b : data) {
            int c = b & 0xFF;
            if (c == 9 || c == 10 || c == 13 || (c >= 32 && c < 127)) {
                s.append((char) c);
            } else {
                s.append('.');
            }
        }
        return s.toString().strip();
    }

    private static JsonNode first(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode v = node.get(name);
            if (v != null && !v.isNull()) {
                return v;
            }
        }
        return null;
    }

    private static String text(JsonNode node, String... names) {
        JsonNode v = first(node, names);
        if (v == null) {
            return null;
        }
        if (v.isNumber()) {
            return v.asString();
        }
        String s = v.asString("");
        return s.isBlank() ? null : s;
    }

    private static Integer integer(JsonNode node, String... names) {
        JsonNode v = first(node, names);
        if (v == null || !v.isNumber()) {
            return null;
        }
        return v.asInt();
    }

    private static Double number(JsonNode node, String... names) {
        JsonNode v = first(node, names);
        if (v == null || !v.isNumber()) {
            return null;
        }
        return v.asDouble();
    }

    private static Instant instant(JsonNode node, String... names) {
        JsonNode v = first(node, names);
        if (v == null) {
            return null;
        }
        if (v.isNumber()) {
            long n = v.asLong();
            return n > 1_000_000_000_000L ? Instant.ofEpochMilli(n) : Instant.ofEpochSecond(n);
        }
        String s = v.asString("");
        if (s.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(s.strip());
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** The data bytes as their ASCII form, for tests. */
    static String ascii(byte[] data) {
        return new String(data, StandardCharsets.US_ASCII);
    }
}
