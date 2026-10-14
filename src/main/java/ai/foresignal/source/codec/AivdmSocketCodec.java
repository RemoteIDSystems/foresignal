package ai.foresignal.source.codec;

import ai.foresignal.codec.ais.AisDecoder;
import ai.foresignal.codec.ais.AisMessage;
import ai.foresignal.codec.ais.FragmentAssembler;
import ai.foresignal.codec.ais.NmeaSentence;
import ai.foresignal.codec.ais.TagBlock;
import ai.foresignal.codec.ais.VesselStateMerger;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Codec {@value #ID} (prompt 085): AIS as NMEA 0183 lines ({@code !AIVDM}, {@code !AIVDO}, {@code !BSVDM}, optionally
 * behind an IEC 61162-450 TAG block), the socket face of the shared codec in {@code ai.foresignal.codec.ais}.
 *
 * <ul>
 *   <li><strong>Framing:</strong> {@link Framing#LINES}.
 *   <li><strong>Session:</strong> each connection's session owns a {@link FragmentAssembler} and a
 *       {@link VesselStateMerger}; every decoded position or static message yields the vessel's complete snapshot
 *       (prompt 063's payload keys).
 *   <li><strong>Time:</strong> {@link TimeBase#TAG} takes {@code observedAt} from the TAG block's {@code c} and drops
 *       (counts) a line without one, never falling back per record ({@code ais-kystverket}, UPSTREAM);
 *       {@link TimeBase#RECEIVED} leaves {@code observedAt} to the framework ({@code local-ais-{n}}).
 *   <li><strong>Observer:</strong> the TAG block's {@code s} (the base station), or a fixed station name.
 *   <li><strong>Drops</strong> (each counted, {@code foresignal.ais.dropped{source,reason}}): a bad TAG or sentence
 *       checksum ({@code checksum}), a malformed line ({@code malformed}), an untimed line under {@link TimeBase#TAG}
 *       ({@code untimed}), an undecodable payload ({@code undecodable}), an invalid MMSI ({@code mmsi}). Decoded
 *       messages are counted by type ({@code foresignal.ais.messages{source,type}}); types 4, 21 and the unsupported
 *       ones are counted and dropped. Fragments: {@code foresignal.ais.fragments.completed} and
 *       {@code foresignal.ais.fragments.expired}.
 *   <li><strong>Writes nothing:</strong> no {@link #onConnect()} bytes; the sources open their sockets receive-only.
 * </ul>
 */
@Component
public class AivdmSocketCodec implements SocketCodec {

    /** The codec id. */
    public static final String ID = "aivdm";

    /** The optional parameter naming the receiving station (the observer of every record). */
    public static final String STATION_PARAM = "stationName";

    /** The metric of completed multi-sentence messages. */
    public static final String FRAGMENTS_COMPLETED = "foresignal.ais.fragments.completed";

    /** The metric of incomplete groups dropped. */
    public static final String FRAGMENTS_EXPIRED = "foresignal.ais.fragments.expired";

    /** The metric of decoded messages by type. */
    public static final String MESSAGES = "foresignal.ais.messages";

    /** The metric of dropped lines and messages by reason. */
    public static final String DROPPED = "foresignal.ais.dropped";

    /** Where {@code observedAt} comes from. */
    public enum TimeBase {
        /** The TAG block's {@code c}; a line without one is dropped. */
        TAG,
        /** None: the framework stamps the receive time. */
        RECEIVED
    }

    /**
     * How one session decodes.
     *
     * @param timeBase where {@code observedAt} comes from
     * @param receiverFromTag true to take the observer from the TAG block's {@code s}
     * @param station the fixed observer when the TAG block names none (or {@code receiverFromTag} is false), or null
     */
    public record Options(TimeBase timeBase, boolean receiverFromTag, String station) {

        /** Defaults the time base to RECEIVED. */
        public Options {
            timeBase = timeBase == null ? TimeBase.RECEIVED : timeBase;
        }
    }

    /** Hears every vessel message a session decodes, for the source's health statistics. */
    @FunctionalInterface
    public interface Listener {

        /** No listener. */
        Listener NONE = (mmsi, receivedAt) -> {};

        /** A message was decoded and carried a valid vessel MMSI. */
        void vessel(String mmsi, Instant receivedAt);
    }

    private final Supplier<MeterRegistry> meters;

    /**
     * Creates the codec for Spring.
     *
     * @param meters the registry of the codec metrics, resolved lazily
     */
    @Autowired
    public AivdmSocketCodec(ObjectProvider<MeterRegistry> meters) {
        this(meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the codec with a plain registry supplier (tests).
     *
     * @param meters supplies the registry, or null
     */
    public AivdmSocketCodec(Supplier<MeterRegistry> meters) {
        this.meters = meters == null ? () -> null : meters;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Framing framing() {
        return Framing.LINES;
    }

    /** A RECEIVED session whose observer is {@value #STATION_PARAM}, else the source id. */
    @Override
    public CodecSession newSession(CodecContext context) {
        String station = context.params().optionalString(STATION_PARAM).filter(s -> !s.isBlank()).map(String::strip)
                .orElse(context.sourceId());
        return newSession(context, new Options(TimeBase.RECEIVED, false, station), Listener.NONE);
    }

    /**
     * A session with explicit options.
     *
     * @param context the source
     * @param options the time base and observer
     * @param listener hears every vessel message, or null
     */
    public Session newSession(CodecContext context, Options options, Listener listener) {
        return new Session(context.sourceId(), options, listener == null ? Listener.NONE : listener,
                new Metrics(context.sourceId(), meters));
    }

    /** The counts of one session. */
    public record Counts(long lines, long checksum, long malformed, long untimed, long undecodable, long mmsi,
            Map<Integer, Long> byType) {}

    /** One connection's decoding state. Called from one thread at a time. */
    public static final class Session implements CodecSession {

        private final Options options;
        private final Listener listener;
        private final Metrics metrics;
        private final FragmentAssembler assembler;
        private final VesselStateMerger merger;
        private final Observer fixedObserver;
        private final Map<Integer, Long> byType = new TreeMap<>();
        private long lines;
        private long checksum;
        private long malformed;
        private long untimed;
        private long undecodable;
        private long mmsi;

        Session(String sourceId, Options options, Listener listener, Metrics metrics) {
            this.options = options;
            this.listener = listener;
            this.metrics = metrics;
            this.assembler = new FragmentAssembler(metrics);
            this.merger = new VesselStateMerger(options.timeBase() == TimeBase.TAG);
            this.fixedObserver = options.station() == null || options.station().isBlank() ? null
                    : Observer.of(options.station().strip());
        }

        @Override
        public List<NormalizedRecord> onFrame(byte[] frame, Instant receivedAt) {
            String line = new String(frame, StandardCharsets.US_ASCII).strip();
            if (line.isEmpty()) {
                return List.of();
            }
            lines++;
            TagBlock.Split split;
            try {
                split = TagBlock.strip(line);
            } catch (TagBlock.TagBlockException e) {
                if (e.badChecksum()) {
                    drop("checksum");
                    checksum++;
                } else {
                    drop("malformed");
                    malformed++;
                }
                return List.of();
            }
            NmeaSentence.Result result = NmeaSentence.parse(split.sentence());
            NmeaSentence sentence;
            switch (result) {
                case NmeaSentence.Parsed p -> sentence = p.sentence();
                case NmeaSentence.BadChecksum b -> {
                    drop("checksum");
                    checksum++;
                    return List.of();
                }
                case NmeaSentence.Malformed m -> {
                    drop("malformed");
                    malformed++;
                    return List.of();
                }
            }
            Optional<FragmentAssembler.Assembled> assembled = assembler.add(sentence, split.tag(), receivedAt);
            if (assembled.isEmpty()) {
                return List.of();
            }
            FragmentAssembler.Assembled message = assembled.get();
            Instant time = receivedAt;
            if (options.timeBase() == TimeBase.TAG) {
                time = message.tag() == null ? null : message.tag().time();
                if (time == null) {
                    drop("untimed");
                    untimed++;
                    return List.of();
                }
            }
            AisMessage decoded;
            try {
                decoded = AisDecoder.decode(message.armored());
            } catch (IllegalArgumentException e) {
                drop("undecodable");
                undecodable++;
                return List.of();
            }
            byType.merge(decoded.type(), 1L, Long::sum);
            metrics.message(decoded.type());
            Observer observer = fixedObserver;
            if (options.receiverFromTag() && message.tag() != null && message.tag().source() != null) {
                observer = Observer.of(message.tag().source());
            }
            long rejectedBefore = merger.rejectedMmsi();
            Optional<NormalizedRecord> snapshot = merger.merge(decoded, time, observer);
            if (merger.rejectedMmsi() > rejectedBefore) {
                drop("mmsi");
                mmsi++;
                return List.of();
            }
            String vessel = VesselStateMerger.mmsiText(decoded.mmsi());
            if (vessel != null && !(decoded instanceof AisMessage.BaseStationReport)
                    && !(decoded instanceof AisMessage.AidToNavigation)
                    && !(decoded instanceof AisMessage.Unsupported)) {
                listener.vessel(vessel, receivedAt);
            }
            return snapshot.map(List::of).orElse(List.of());
        }

        @Override
        public long skipped() {
            return checksum + malformed + untimed + undecodable;
        }

        /** The counts so far. */
        public Counts counts() {
            return new Counts(lines, checksum, malformed, untimed, undecodable, mmsi, Map.copyOf(byType));
        }

        /** The session's merger (tests). */
        public VesselStateMerger merger() {
            return merger;
        }

        /** The session's fragment assembler (tests). */
        public FragmentAssembler assembler() {
            return assembler;
        }

        private void drop(String reason) {
            metrics.dropped(reason);
        }
    }

    /** The Micrometer counters of one source, registered on first use; nothing when no registry is available. */
    static final class Metrics implements FragmentAssembler.Listener {

        private final String sourceId;
        private final Supplier<MeterRegistry> meters;
        private final Map<String, Counter> counters = new ConcurrentHashMap<>();

        Metrics(String sourceId, Supplier<MeterRegistry> meters) {
            this.sourceId = sourceId;
            this.meters = meters;
        }

        @Override
        public void completed() {
            increment(FRAGMENTS_COMPLETED, null, null, 1, "Multi-sentence AIS messages completed");
        }

        @Override
        public void expired(int count) {
            increment(FRAGMENTS_EXPIRED, null, null, count, "Incomplete multi-sentence AIS messages dropped");
        }

        void message(int type) {
            increment(MESSAGES, "type", Integer.toString(type), 1, "AIS messages decoded, by type");
        }

        void dropped(String reason) {
            increment(DROPPED, "reason", reason, 1, "AIS lines and messages dropped, by reason");
        }

        private void increment(String name, String tag, String value, int amount, String description) {
            MeterRegistry registry = meters.get();
            if (registry == null) {
                return;
            }
            String key = name + "|" + tag + "|" + value;
            Counter counter = counters.computeIfAbsent(key, k -> {
                Counter.Builder b = Counter.builder(name).description(description).tag("source", sourceId);
                if (tag != null) {
                    b = b.tag(tag, value);
                }
                return b.register(registry);
            });
            counter.increment(amount);
        }
    }
}
