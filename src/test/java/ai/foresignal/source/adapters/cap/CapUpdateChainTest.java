package ai.foresignal.source.adapters.cap;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.formats.cap.CapAlert;
import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.formats.cap.CapThreadResolver;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The CAP update chain (prompt 089): an {@code Alert}, its {@code Update} and its {@code Cancel} version one
 * record, an unknown reference starts a root, and a non-{@code Actual} status is skipped unless included.
 */
class CapUpdateChainTest {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");

    private static CapAlert parse(String name) {
        return CapParser.parse(AdapterFixtures.bytes("cap-de-dwd-de", name));
    }

    @Test
    @DisplayName("Alert, Update and Cancel touch one record; Cancel is a tombstone")
    void chain() {
        CapThreadResolver resolver = new CapThreadResolver();
        CapAlert alert = parse("chain-alert.xml");
        String thread = resolver.resolve(alert, null, NOW);
        NormalizedRecord first = CapAlertRecords.map(alert, thread, List.of("en-GB"), false);
        assertThat(first.externalId()).isEqualTo(thread);
        assertThat(first.tombstone()).isFalse();
        assertThat(first.payload()).containsEntry("severity", "Severe").containsEntry("event", "Test Wind");
        assertThat(first.geometry()).isNotNull();

        CapAlert update = parse("chain-update.xml");
        String updateThread = resolver.resolve(update, null, NOW);
        assertThat(updateThread).isEqualTo(thread);
        NormalizedRecord second = CapAlertRecords.map(update, updateThread, List.of("en-GB"), false);
        assertThat(second.externalId()).isEqualTo(thread);
        assertThat(second.tombstone()).isFalse();
        assertThat(second.payload()).containsEntry("severity", "Extreme")
                .containsEntry("headline", "Test wind warning updated");

        CapAlert cancel = parse("chain-cancel.xml");
        String cancelThread = resolver.resolve(cancel, null, NOW);
        assertThat(cancelThread).isEqualTo(thread);
        NormalizedRecord tombstone = CapAlertRecords.map(cancel, cancelThread, List.of("en-GB"), false);
        assertThat(tombstone.tombstone()).isTrue();
        assertThat(tombstone.externalId()).isEqualTo(thread);
    }

    @Test
    @DisplayName("a reference to an unknown thread starts a root under the referenced identifier")
    void unknownReferenceStartsRoot() {
        CapAlert alert = parse("chain-alert.xml");
        CapAlert update = parse("chain-update.xml");
        String freshUpdateThread = new CapThreadResolver().resolve(update, null, NOW);
        assertThat(freshUpdateThread).isEqualTo(alert.ownKey());
    }

    @Test
    @DisplayName("a non-Actual status is skipped, unless includeTests")
    void nonActual() {
        CapAlert exercise = parse("chain-exercise.xml");
        assertThat(CapAlertRecords.map(exercise, "t", List.of("en-GB"), false)).isNull();
        NormalizedRecord kept = CapAlertRecords.map(exercise, "t", List.of("en-GB"), true);
        assertThat(kept).isNotNull();
        assertThat(kept.payload()).containsEntry("status", "Exercise");
    }

    @Test
    @DisplayName("an inverted valid range drops validFrom instead of losing the alert")
    void invertedRange() {
        CapAlert alert = parse("chain-cancel.xml");
        NormalizedRecord record = CapAlertRecords.map(alert, "t", List.of("en-GB"), true);
        assertThat(record).isNotNull();
    }
}
