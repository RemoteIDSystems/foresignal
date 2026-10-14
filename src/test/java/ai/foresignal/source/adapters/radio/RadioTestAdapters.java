package ai.foresignal.source.adapters.radio;

import ai.foresignal.source.codec.KissAx25Codec;
import ai.foresignal.source.formats.aprs.AprsPacketDedup;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.function.Supplier;

/** Prompt 082 adapters built without Spring, for the radio tests. */
final class RadioTestAdapters {

    private static final Supplier<MeterRegistry> NO_METERS = () -> null;

    private RadioTestAdapters() {}

    /** {@code pskreporter-ft8} with production URLs. */
    static PskReporterAdapter psk() {
        return psk("");
    }

    /** {@code pskreporter-ft8} under a test base URL. */
    static PskReporterAdapter psk(String base) {
        return new PskReporterAdapter(base, NO_METERS);
    }

    /** {@code kiwisdr} under a test base URL. */
    static KiwiSdrAdapter kiwi(String base) {
        return new KiwiSdrAdapter(base, NO_METERS);
    }

    /** {@code aprs-is} with a fixed install id and version. */
    static AprsIsAdapter aprsIs(AprsPacketDedup dedup, Supplier<MeterRegistry> meters) {
        return new AprsIsAdapter(() -> "abcd1234", () -> "1.3.0", dedup, meters);
    }

    /** {@code local-aprs-1}. */
    static LocalAprsAdapter localAprs(AprsPacketDedup dedup, Supplier<MeterRegistry> meters) {
        return new LocalAprsAdapter(1, new KissAx25Codec(), dedup, meters);
    }
}
