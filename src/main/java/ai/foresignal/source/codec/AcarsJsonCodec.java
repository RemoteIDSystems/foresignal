package ai.foresignal.source.codec;

import ai.foresignal.source.formats.acars.AcarsMetadataMapper;
import ai.foresignal.source.formats.acars.JsonObjectStream;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Codec {@value #ID} (prompt 086): acars_router's TCP server output (acarsdec, dumpvdl2 and dumphfdl JSON) as
 * {@code acars-message} metadata records, through {@link AcarsMetadataMapper}.
 *
 * <p>Framed {@link Framing#RAW}: each session runs a {@link JsonObjectStream}, which accepts one object per line (the
 * expected framing) as well as objects written back to back, so the codec does not depend on a framing that is
 * confirmed only at fixture capture. An object that does not parse or is not one of the three decoder shapes is skipped
 * and counted; <strong>nothing of a skipped or mapped message is ever logged</strong>, because the message text is
 * content Foresignal must not redistribute.
 *
 * <p>Session option {@value #STATION_PARAM}: the operator's station id, the observer of every record (the decoder's
 * own {@code station_id} when unset).
 */
@Component
public class AcarsJsonCodec implements SocketCodec {

    /** The codec id. */
    public static final String ID = "acars-json";

    /** The station parameter read from the codec context. */
    public static final String STATION_PARAM = "stationId";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Override
    public String id() {
        return ID;
    }

    /** Raw chunks; the session finds the objects. */
    @Override
    public Framing framing() {
        return Framing.RAW;
    }

    @Override
    public CodecSession newSession(CodecContext context) {
        String station = context == null ? null : context.params().string(STATION_PARAM);
        return new Session(station);
    }

    /** One connection: the object splitter and the counters. */
    public static final class Session implements CodecSession {

        private final String station;
        private final JsonObjectStream objects = new JsonObjectStream();
        private long skipped;
        private long mapped;

        Session(String station) {
            this.station = station;
        }

        @Override
        public List<NormalizedRecord> onFrame(byte[] frame, Instant receivedAt) {
            List<NormalizedRecord> out = new ArrayList<>();
            for (byte[] object : objects.feed(frame)) {
                JsonNode node;
                try {
                    node = JSON.readTree(object);
                } catch (JacksonException e) {
                    skipped++;
                    continue;
                }
                NormalizedRecord record = AcarsMetadataMapper.map(node, station);
                if (record == null) {
                    skipped++;
                } else {
                    mapped++;
                    out.add(record);
                }
            }
            return out;
        }

        @Override
        public long skipped() {
            return skipped + objects.discarded();
        }

        /** Messages mapped since the session opened. */
        public long mapped() {
            return mapped;
        }
    }
}
