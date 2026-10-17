package ai.foresignal.federation;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ReconnectPolicy;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The descriptors of a peer's dynamic sources (prompt 113, the {@code foresignal-peer} family).
 *
 * <ul>
 *   <li>{@code peer-{id}}: STANDARD, ingest policy {@code ALL}.
 *   <li>{@code peer-{id}-trk}: TRACK, {@code ttlAnchor=OBSERVED}, TTL {@code selection.trackTtl} (default 10 min),
 *       ingest policy {@code SAMPLED} with the Core-Data-Model §5.3 track defaults (aircraft and vessel significant
 *       fields, 30 s minimum interval, 250 m dead-reckoning deviation, a 240 s keyframe that fits below the TTL).
 *       Registered when the selection names no kinds or names a kind some local TRACK source declares.
 *   <li>{@code peer-{id}-events}: STANDARD PUSH, kind {@value #REMOTE_EVENT_KIND}, for compact messages received in
 *       a bundle; expiry comes from each message (key 12).
 * </ul>
 *
 * <p>A {@code PULL} peer's record sources are {@code STREAMING} (the supervisor runs them); a {@code RECEIVE}
 * peer's are {@code PUSH}. All carry {@code observedAtMode=UPSTREAM} (the remote {@code observedAt}), license
 * {@value #LICENSE} (109 resolves each record's license from {@code _foresignal.originLicence}), reliability
 * {@code F} unless configured, and {@code volatileFields: ["_foresignal"]} so provenance never enters the hash: an
 * identical re-send makes no new version.
 */
public final class PeerDescriptors {

    /** The source id prefix. */
    public static final String PREFIX = "peer-";

    /** The suffix of the TRACK source. */
    public static final String TRACK_SUFFIX = "-trk";

    /** The suffix of the compact-message source. */
    public static final String EVENTS_SUFFIX = "-events";

    /** The kind of a received compact message. */
    public static final String REMOTE_EVENT_KIND = "remote-event";

    /** The descriptor license of a peer source: the real license is per record. */
    public static final String LICENSE = "mixed (per record)";

    /** The default reliability of peer data: cannot be judged. */
    public static final String DEFAULT_RELIABILITY = "F";

    /** Significant fields of the TRACK source: the §5.3 aircraft and vessel lists. */
    static final List<String> TRACK_SIGNIFICANT = List.of("flight", "r", "t", "squawk", "category", "emergency",
            "onGround", "navDegraded", "name", "callsign", "imo", "shipType", "destination", "navStatus", "draught");

    private PeerDescriptors() {}

    /** {@code peer-{id}}. */
    public static String standardId(String peerId) {
        return PREFIX + peerId;
    }

    /** {@code peer-{id}-trk}. */
    public static String trackId(String peerId) {
        return PREFIX + peerId + TRACK_SUFFIX;
    }

    /** {@code peer-{id}-events}. */
    public static String eventsId(String peerId) {
        return PREFIX + peerId + EVENTS_SUFFIX;
    }

    /** True when {@code sourceId} belongs to the {@code foresignal-peer} family. */
    public static boolean isPeerSource(String sourceId) {
        return sourceId != null && sourceId.startsWith(PREFIX);
    }

    /**
     * The kinds a peer's record sources declare: the selection's kinds, or every kind a local source declares
     * (the local vocabulary) when the selection names none. Items of other kinds are dropped
     * ({@value FederationRules#DROP_SELECTION}).
     */
    public static List<String> recordKinds(PeerSelection selection, Collection<String> localKinds) {
        Set<String> kinds = new TreeSet<>(selection.kinds().isEmpty() ? localKinds : selection.kinds());
        kinds.remove(REMOTE_EVENT_KIND);
        if (kinds.isEmpty()) {
            kinds.add("record");
        }
        return new ArrayList<>(kinds);
    }

    /** True when the peer gets a TRACK source: no kinds selected, or a selected kind is TRACK locally. */
    public static boolean wantsTrack(PeerSelection selection, Collection<String> localTrackKinds) {
        return selection.kinds().isEmpty() || selection.kinds().stream().anyMatch(localTrackKinds::contains);
    }

    /** The STANDARD record source. */
    public static SourceDescriptor standard(String peerId, String name, boolean pull, String reliability,
            List<String> kinds) {
        return base(standardId(peerId), "Peer " + name, pull, reliability, kinds)
                .description("Records replicated from the Foresignal instance '" + name + "' (federation peer "
                        + peerId + "), with their provenance chain in _foresignal.")
                .historyTier(HistoryTier.STANDARD)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .build();
    }

    /** The TRACK record source with TTL {@code trackTtl}. */
    public static SourceDescriptor track(String peerId, String name, boolean pull, String reliability,
            List<String> kinds, Duration trackTtl) {
        Duration ttl = trackTtl == null || trackTtl.compareTo(PeerSelection.MIN_TRACK_TTL) < 0
                ? PeerSelection.DEFAULT_TRACK_TTL : trackTtl;
        Duration keyframe = ttl.minusSeconds(60).compareTo(Duration.ofSeconds(240)) < 0
                ? ttl.minusSeconds(60) : Duration.ofSeconds(240);
        return base(trackId(peerId), "Peer " + name + " (tracks)", pull, reliability, kinds)
                .description("Track positions replicated from the Foresignal instance '" + name + "' (federation peer "
                        + peerId + ").")
                .historyTier(HistoryTier.TRACK)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(ttl)
                .significantFields(TRACK_SIGNIFICANT.toArray(String[]::new))
                .defaultIngestPolicy(IngestPolicy.sampled(Duration.ofSeconds(30), keyframe, 250.0, false))
                .build();
    }

    /** The compact-message source. */
    public static SourceDescriptor events(String peerId, String name, String reliability) {
        return SourceDescriptor.builder(eventsId(peerId))
                .name("Peer " + name + " (compact events)")
                .description("Alerts and intelligence events received from '" + name + "' as compact messages"
                        + " (federation bundles); never re-federated.")
                .kind(REMOTE_EVENT_KIND)
                .attribution("Federated from " + name)
                .licence(LICENSE)
                .reliability(reliability == null ? DEFAULT_RELIABILITY : reliability)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 50_000))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .titleField("title")
                .volatileFields(FederationRules.MEMBER)
                .build();
    }

    private static SourceDescriptor.Builder base(String id, String name, boolean pull, String reliability,
            List<String> kinds) {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(id);
        if (pull) {
            builder.reconnect(ReconnectPolicy.exponential(Duration.ofSeconds(5), Duration.ofMinutes(5)));
        }
        return builder
                .name(name)
                .kinds(kinds.toArray(String[]::new))
                .attribution("Federated from " + name.substring("Peer ".length()))
                .licence(LICENSE)
                .reliability(reliability == null ? DEFAULT_RELIABILITY : reliability)
                .mode(pull ? Mode.STREAMING : Mode.PUSH)
                .defaultBudget(Budget.of(60, 50_000))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .maxResponseBytes(64 * 1024 * 1024)
                .volatileFields(FederationRules.MEMBER);
    }
}
