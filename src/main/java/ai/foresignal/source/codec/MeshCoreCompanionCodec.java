package ai.foresignal.source.codec;

import ai.foresignal.source.adapters.mesh.MeshCoreEventMapper;
import ai.foresignal.source.adapters.mesh.MeshRecords;
import ai.foresignal.source.adapters.mesh.RepeaterHealth;
import ai.foresignal.source.formats.meshcore.MeshCoreFrameDecoder;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Codec {@value #ID} (prompt 083): the MeshCore WiFi companion firmware's TCP stream.
 *
 * <ul>
 *   <li><strong>Framing:</strong> {@link Framing#RAW}; {@link MeshCoreFrameDecoder} reassembles {@code '>'} + uint16
 *       little-endian length frames across reads and skips frames over 176 bytes.
 *   <li><strong>Records:</strong> {@link MeshCoreEventMapper}: contacts, adverts and the companion's own frames as
 *       {@code mesh-node} records ({@code includeText} turns on {@code mesh-message} records).
 *   <li><strong>Writes nothing itself:</strong> the companion only answers commands, which only the shared
 *       {@code MeshCoreCompanionLink} sends, through its command guard. A session of this codec alone therefore decodes
 *       whatever the companion pushes unasked (new adverts) and serves tests and captures; {@code meshcore-local} uses
 *       the link, which uses the same decoder and mapper.
 * </ul>
 */
@Component
public class MeshCoreCompanionCodec implements SocketCodec {

    /** The codec id. */
    public static final String ID = "meshcore-companion";

    /** Creates the codec. */
    public MeshCoreCompanionCodec() {}

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
        return new Session(new MeshCoreEventMapper(context.sourceId(),
                context.params().bool(MeshRecords.INCLUDE_TEXT, false), RepeaterHealth.defaults()));
    }

    /** One connection's reassembly and mapping. */
    public static final class Session implements CodecSession {

        private final MeshCoreFrameDecoder decoder = new MeshCoreFrameDecoder();
        private final MeshCoreEventMapper mapper;

        Session(MeshCoreEventMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public List<NormalizedRecord> onFrame(byte[] frame, Instant receivedAt) {
            List<NormalizedRecord> out = new ArrayList<>();
            for (byte[] companionFrame : decoder.feed(frame)) {
                out.addAll(mapper.onFrame(companionFrame, receivedAt).records());
            }
            return out;
        }

        /** Frames skipped as oversize, plus bytes skipped while resynchronizing. */
        @Override
        public long skipped() {
            return decoder.oversize() + decoder.junk();
        }

        /** The decoder (tests). */
        public MeshCoreFrameDecoder decoder() {
            return decoder;
        }
    }
}
