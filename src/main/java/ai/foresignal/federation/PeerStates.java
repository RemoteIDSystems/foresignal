package ai.foresignal.federation;

import java.time.Clock;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads a peer and records its runtime state (prompt 113): the PULL session and the RECEIVE endpoint report
 * {@code state}, the health reason, the last applied seq, the remote watermark and the last error here, and pin
 * the remote key on the first verified contact (TOFU).
 */
@Component
public class PeerStates {

    private final PeerRepository peers;
    private final Clock clock;

    /** Creates the store. */
    public PeerStates(PeerRepository peers, Clock clock) {
        this.peers = peers;
        this.clock = clock;
    }

    /**
     * The record sources of every enabled peer ({@code peer-{id}} and {@code peer-{id}-trk}): an origin owned by a
     * source outside this set changes owner at its next arrival.
     */
    @Transactional(readOnly = true)
    public java.util.Set<String> aliveSources() {
        java.util.Set<String> out = new java.util.HashSet<>();
        for (PeerEntity peer : peers.findAll()) {
            if (peer.isEnabled()) {
                out.add(PeerDescriptors.standardId(peer.getId()));
                out.add(PeerDescriptors.trackId(peer.getId()));
            }
        }
        return out;
    }

    /** The peer, read fresh. */
    @Transactional(readOnly = true)
    public Optional<PeerEntity> find(String peerId) {
        return peers.findById(peerId);
    }

    /**
     * Records the runtime state.
     *
     * @param state {@code connecting}, {@code streaming}, {@code resyncing}, {@code failing} or {@code stopped}
     * @param healthReason {@code PEER_KEY_MISMATCH}, {@code PEER_UNREACHABLE}, {@code PEER_RESYNC} or null
     * @param lastSeq the last applied remote seq, or null to keep the stored one
     * @param watermark the remote watermark, or null to keep the stored one
     * @param error the last error, or null to clear it
     */
    @Transactional
    public void update(String peerId, String state, String healthReason, Long lastSeq, Long watermark, String error) {
        peers.findById(peerId).ifPresent(peer -> {
            peer.setState(state);
            peer.setHealthReason(healthReason);
            if (lastSeq != null) {
                peer.setLastSeq(lastSeq);
            }
            if (watermark != null) {
                peer.setRemoteWatermark(watermark);
            }
            peer.setLastError(error);
            peer.setUpdatedAt(clock.instant());
            peers.save(peer);
        });
    }

    /** Records the last applied remote seq, keeping the state and health reason. */
    @Transactional
    public void recordSeq(String peerId, long lastSeq) {
        peers.findById(peerId).ifPresent(peer -> {
            peer.setLastSeq(lastSeq);
            peer.setUpdatedAt(clock.instant());
            peers.save(peer);
        });
    }

    /**
     * Pins {@code kid} and {@code key} when nothing is pinned yet (TOFU), and records the remote instance id.
     *
     * @return the pinned kid after the call (the stored one when one was pinned before)
     */
    @Transactional
    public String pinIfUnset(String peerId, String kid, byte[] key, String remoteInstanceId) {
        PeerEntity peer = peers.findById(peerId).orElse(null);
        if (peer == null) {
            return null;
        }
        if (peer.getPinnedKid() == null) {
            peer.setPinned(kid, key);
            if (remoteInstanceId != null) {
                peer.setRemoteInstanceId(remoteInstanceId);
            }
            peer.setUpdatedAt(clock.instant());
            peers.save(peer);
        } else if (peer.getPinnedKey() == null && kid.equals(peer.getPinnedKid())) {
            // An admin pinned the kid only: keep the matching key with it.
            peer.setPinned(kid, key);
            peers.save(peer);
        }
        if (peer.getRemoteInstanceId() == null && remoteInstanceId != null) {
            peer.setRemoteInstanceId(remoteInstanceId);
            peers.save(peer);
        }
        return peer.getPinnedKid();
    }
}
