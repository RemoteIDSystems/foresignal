package ai.foresignal.source.codec;

import ai.foresignal.source.formats.aprs.AprsPacket;
import ai.foresignal.source.formats.aprs.AprsParser;
import ai.foresignal.source.formats.aprs.AprsRecords;
import ai.foresignal.source.formats.kiss.Ax25UiDecoder;
import ai.foresignal.source.formats.kiss.KissFrameDecoder;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Codec {@value #ID} (prompt 082): APRS packets from a KISS TNC (Direwolf's {@code KISSPORT}) over TCP.
 *
 * <ul>
 *   <li><strong>Framing:</strong> {@link Framing#RAW}; {@link KissFrameDecoder} reassembles KISS frames across reads
 *       and keeps only data frames (command 0).
 *   <li><strong>Frames:</strong> {@link Ax25UiDecoder} turns each AX.25 UI frame (control 0x03, PID 0xF0) into TNC2
 *       text for {@link AprsParser}. A frame that is not AX.25 UI (a connected-mode frame, or a MeshCore KISS modem's
 *       raw MeshCore packet) is counted ({@link Session#nonAx25()}) and dropped.
 *   <li><strong>Records:</strong> {@link AprsRecords}, with the operator's station ({@value #STATION_PARAM}, else the
 *       source id) as the observer.
 *   <li><strong>Writes nothing:</strong> no {@link #onConnect()} bytes, no KISS commands; the source opens the socket
 *       receive-only.
 * </ul>
 */
@Component
public class KissAx25Codec implements SocketCodec {

    /** The codec id. */
    public static final String ID = "kiss-ax25";

    /** The optional parameter naming the operator's station, used as the observer's receiver id. */
    public static final String STATION_PARAM = "stationCallsign";

    /** The optional boolean parameter that turns on {@code aprs-message} records. */
    public static final String INCLUDE_MESSAGES_PARAM = "includeMessages";

    /** Creates the codec. */
    public KissAx25Codec() {}

    /** Hears what a session decodes, for the source's counters and the cross-source duplicate filter. */
    public interface PacketListener {

        /** No listener. */
        PacketListener NONE = new PacketListener() {};

        /** A UI frame that parsed into a supported packet. */
        default void packet(AprsPacket packet, Instant receivedAt) {}

        /** A UI frame whose APRS content is unsupported or malformed. */
        default void unsupported(AprsPacket packet, Instant receivedAt) {}

        /** A frame that is not AX.25 UI. */
        default void nonAx25() {}
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Framing framing() {
        return Framing.RAW;
    }

    @Override
    public CodecSession newSession(CodecContext context) {
        return newSession(context, PacketListener.NONE);
    }

    /**
     * A session reporting to {@code listener}.
     *
     * @param context the source and its parameters ({@value #STATION_PARAM}, {@value #INCLUDE_MESSAGES_PARAM})
     * @param listener hears every decoded packet and every dropped frame
     */
    public Session newSession(CodecContext context, PacketListener listener) {
        String station = context.params().optionalString(STATION_PARAM).filter(s -> !s.isBlank())
                .map(String::strip).orElse(context.sourceId());
        boolean messages = context.params().bool(INCLUDE_MESSAGES_PARAM, false);
        return new Session(Observer.of(station), messages, listener == null ? PacketListener.NONE : listener);
    }

    /** One connection's decoding state. */
    public static final class Session implements CodecSession {

        private final KissFrameDecoder kiss = new KissFrameDecoder();
        private final AprsRecords records = new AprsRecords();
        private final Observer observer;
        private final boolean includeMessages;
        private final PacketListener listener;
        private long nonAx25;
        private long unsupported;

        Session(Observer observer, boolean includeMessages, PacketListener listener) {
            this.observer = observer;
            this.includeMessages = includeMessages;
            this.listener = listener;
        }

        @Override
        public List<NormalizedRecord> onFrame(byte[] frame, Instant receivedAt) {
            List<NormalizedRecord> out = new ArrayList<>();
            for (KissFrameDecoder.Frame data : kiss.feed(frame)) {
                Ax25UiDecoder.Result result = Ax25UiDecoder.decode(data.data());
                if (!(result instanceof Ax25UiDecoder.Result.Ui ui)) {
                    nonAx25++;
                    listener.nonAx25();
                    continue;
                }
                AprsPacket packet = AprsParser.parse(ui.tnc2());
                if (packet.type() == AprsPacket.Type.UNSUPPORTED) {
                    unsupported++;
                    listener.unsupported(packet, receivedAt);
                    continue;
                }
                listener.packet(packet, receivedAt);
                out.addAll(records.map(packet, observer, includeMessages, receivedAt));
            }
            return out;
        }

        /** Frames that were not AX.25 UI, dropped so far. */
        public long nonAx25() {
            return nonAx25;
        }

        /** UI frames with unsupported or malformed APRS content, dropped so far. */
        public long unsupported() {
            return unsupported;
        }

        /** KISS frames dropped as malformed or oversize, and AX.25 frames that were not UI or not AX.25. */
        @Override
        public long skipped() {
            return kiss.malformed() + kiss.oversize() + nonAx25 + unsupported;
        }
    }
}
