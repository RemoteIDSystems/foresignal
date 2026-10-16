package ai.foresignal.trigger.window;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * Emitters for the prompt 100 window tests: {@link Capture} keeps what {@link WindowEngine} emits, and
 * {@link Ingesting} also writes it through the real ingest writer into {@code rule-windows}, so the
 * engine's owed alerts find the persisted {@code window-state} version they must reference.
 */
final class WindowEmitters {

    private WindowEmitters() {}

    /** Keeps every emitted record and tombstone. */
    static class Capture implements WindowEngine.Emitter {

        final List<NormalizedRecord> emitted = new ArrayList<>();

        @Override
        public void emit(NormalizedRecord record) {
            emitted.add(record);
        }

        @Override
        public void tombstone(String kind, String externalId) {
            emitted.add(NormalizedRecord.tombstone(externalId, kind, java.time.Instant.parse("2026-01-02T00:00:00Z")));
        }

        /** True when a {@code window-state} record of {@code externalId} with {@code state} was emitted. */
        boolean hasState(String externalId, String state) {
            return emitted.stream().anyMatch(r -> WindowEngine.STATE_KIND.equals(r.kind())
                    && r.externalId().equals(externalId) && state.equals(r.payload().get("state")));
        }

        /** The last emitted {@code window-state} payload value of {@code key} for {@code externalId}, or null. */
        Object lastState(String externalId, String key) {
            Object out = null;
            for (NormalizedRecord r : emitted) {
                if (WindowEngine.STATE_KIND.equals(r.kind()) && r.externalId().equals(externalId)) {
                    out = r.payload().get(key);
                }
            }
            return out;
        }

        /** True when an {@code anomaly} record classified {@code classification} was emitted. */
        boolean emittedAnomaly(String classification) {
            return emitted.stream().anyMatch(r -> WindowEngine.ANOMALY_KIND.equals(r.kind())
                    && classification.equals(r.payload().get("classification")));
        }
    }

    /** Captures and ingests into {@code rule-windows}, received at the application clock. */
    static final class Ingesting extends Capture {

        private final TestRecords records;
        private final Clock clock;

        Ingesting(TestRecords records, Clock clock) {
            this.records = records;
            this.clock = clock;
        }

        @Override
        public void emit(NormalizedRecord record) {
            super.emit(record);
            records.ingestAt(WindowEngine.SOURCE_ID, clock.instant(), record);
        }

        @Override
        public void tombstone(String kind, String externalId) {
            super.tombstone(kind, externalId);
            records.ingestAt(WindowEngine.SOURCE_ID, clock.instant(),
                    NormalizedRecord.tombstone(externalId, kind, clock.instant()));
        }
    }
}
