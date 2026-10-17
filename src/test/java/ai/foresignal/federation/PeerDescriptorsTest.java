package ai.foresignal.federation;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The {@code foresignal-peer} descriptors and selection (prompt 113): every descriptor passes the same validation as
 * a compile-time adapter, a PULL peer's sources register dynamically as STREAMING sources with their adapters, the
 * TRACK source follows the selection, and a selection parses, defaults and reports its problems.
 */
class PeerDescriptorsTest {

    static final List<String> KINDS = List.of("aircraft", "earthquake");

    @Test
    @DisplayName("the STANDARD, TRACK and events descriptors are valid; _foresignal is volatile")
    void descriptorsValidate() {
        SourceDescriptor standard = PeerDescriptors.standard("friend", "Friend", true, null, KINDS);
        SourceDescriptor track = PeerDescriptors.track("friend", "Friend", true, "B", KINDS, Duration.ofMinutes(10));
        SourceDescriptor events = PeerDescriptors.events("friend", "Friend", null);
        assertThat(DescriptorValidator.problems(standard, new PeerAdapter(standard, "friend", null))).isEmpty();
        assertThat(DescriptorValidator.problems(track, new PeerTrackAdapter(track, "friend", null))).isEmpty();
        assertThat(DescriptorValidator.problems(events, null)).isEmpty();
        assertThat(standard.id()).isEqualTo("peer-friend");
        assertThat(standard.mode()).isEqualTo(Mode.STREAMING);
        assertThat(standard.volatileFields()).containsExactly(FederationRules.MEMBER);
        assertThat(standard.reliability()).isEqualTo(PeerDescriptors.DEFAULT_RELIABILITY);
        assertThat(track.id()).isEqualTo("peer-friend-trk");
        assertThat(track.historyTier()).isEqualTo(HistoryTier.TRACK);
        assertThat(track.recordTtl()).isEqualTo(Duration.ofMinutes(10));
        assertThat(track.reliability()).isEqualTo("B");
        assertThat(events.id()).isEqualTo("peer-friend-events");
        assertThat(events.kinds()).containsExactly(PeerDescriptors.REMOTE_EVENT_KIND);
        SourceDescriptor receive = PeerDescriptors.standard("edge", "Edge", false, null, KINDS);
        assertThat(receive.mode()).isEqualTo(Mode.PUSH);
        assertThat(DescriptorValidator.problems(receive, null)).isEmpty();
        SourceDescriptor shortTtl = PeerDescriptors.track("edge", "Edge", false, null, KINDS, Duration.ofMinutes(2));
        assertThat(DescriptorValidator.problems(shortTtl, null)).isEmpty();
    }

    @Test
    @DisplayName("a PULL peer's STREAMING sources register dynamically with their streaming adapters")
    void registersAsStreaming() {
        SourceRegistry registry = new SourceRegistry(List.of(), List.of(), event -> { });
        SourceDescriptor standard = PeerDescriptors.standard("friend", "Friend", true, null, KINDS);
        registry.registerDynamic(standard, new PeerAdapter(standard, "friend", null));
        assertThat(registry.get("peer-friend").adapterIfPresent()).containsInstanceOf(PeerAdapter.class);
        assertThat(PeerDescriptors.isPeerSource("peer-friend")).isTrue();
        assertThat(PeerDescriptors.isPeerSource("usgs-earthquakes")).isFalse();
    }

    @Test
    @DisplayName("kinds and the TRACK source follow the selection")
    void selectionDrivesKinds() {
        PeerSelection all = PeerSelection.all();
        assertThat(PeerDescriptors.recordKinds(all, List.of("earthquake", "aircraft", "remote-event")))
                .containsExactly("aircraft", "earthquake");
        assertThat(PeerDescriptors.wantsTrack(all, List.of("aircraft"))).isTrue();
        PeerSelection quakes = new PeerSelection(List.of(), List.of("earthquake"), null, null, null, null, false, 3,
                null);
        assertThat(PeerDescriptors.recordKinds(quakes, KINDS)).containsExactly("earthquake");
        assertThat(PeerDescriptors.wantsTrack(quakes, List.of("aircraft"))).isFalse();
    }

    @Test
    @DisplayName("a selection parses with defaults, round trips and reports its problems")
    void selection() {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("sources", List.of("usgs-earthquakes"));
        raw.put("kinds", List.of("earthquake"));
        raw.put("bbox", List.of(-10, 40, 10, 60));
        raw.put("maxAge", "7d");
        raw.put("backfill", "2h");
        raw.put("live", true);
        raw.put("maxHops", 4);
        raw.put("trackTtl", "15m");
        List<FieldError> errors = new ArrayList<>();
        PeerSelection parsed = PeerSelection.parse(raw, "selection", errors);
        assertThat(errors).isEmpty();
        assertThat(parsed.maxAge()).isEqualTo(Duration.ofDays(7));
        assertThat(parsed.backfillDuration()).isEqualTo(Duration.ofHours(2));
        assertThat(parsed.maxHops()).isEqualTo(4);
        assertThat(parsed.trackTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(parsed.inBbox(0, 50)).isTrue();
        assertThat(parsed.inBbox(20, 50)).isFalse();
        assertThat(PeerSelection.parse(parsed.toMap(), "selection", errors)).isEqualTo(parsed);
        assertThat(PeerSelection.parse(null, "selection", errors)).isEqualTo(PeerSelection.all());
        assertThat(PeerSelection.all().maxHops()).isEqualTo(FederationRules.DEFAULT_MAX_HOPS);
        assertThat(PeerSelection.all().backfillDuration()).isNull();
        assertThat(PeerRemote.filters(parsed)).isEqualTo("&source=usgs-earthquakes&kind=earthquake&bbox=-10%2C40%2C10%2C60");

        List<FieldError> bad = new ArrayList<>();
        Map<String, Object> wrong = new LinkedHashMap<>();
        wrong.put("bbox", List.of(10, 0, -10, 5));
        wrong.put("maxHops", 0);
        wrong.put("trackTtl", "30s");
        wrong.put("backfill", "soon");
        wrong.put("kinds", "earthquake");
        PeerSelection.parse(wrong, "selection", bad);
        assertThat(bad).extracting(FieldError::field).contains("selection.bbox", "selection.maxHops",
                "selection.trackTtl", "selection.backfill", "selection.kinds");
    }
}
