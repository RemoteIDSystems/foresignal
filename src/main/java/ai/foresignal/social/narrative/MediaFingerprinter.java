package ai.foresignal.social.narrative;

import ai.foresignal.evidence.RecordRef;
import ai.foresignal.media.GuardedImages;
import ai.foresignal.media.ImageInfo;
import ai.foresignal.media.MediaStore;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.net.PinnedHttp;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.ImageRejectedException;
import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRole;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Media fingerprints of post images (prompt 136, {@code MediaFingerprinter} 1.0.0). For each queued image or video
 * thumbnail URL: fetch it over HTTPS through the outbound URL policy ({@code PUBLIC_ONLY}, pinned addresses, no
 * redirects, Foresignal's own User-Agent, at most the 043 guard's size), inspect and decode it through 043's
 * {@link GuardedImages} (size, type and pixel limits), compute PDQ, and discard the image unless
 * {@code social.keepMedia} is on (then it is stored content-addressed in the 043 media store under the post's
 * retention).
 *
 * <p>A hash identical to a stored fingerprint, or within Hamming distance 31 of a matchable one (quality ≥ 50), is
 * another use of it; otherwise it is a new fingerprint first seen with this post. Hashes of quality below 50 are kept
 * but not matched. Each use updates the narrative's fingerprints, runs {@link MediaReuseDetector}, and merges a
 * one-post narrative into an active narrative that already holds the image ({@code media} join). Matching uses an
 * in-memory {@link PdqIndex} loaded from the store.
 */
@Component
public class MediaFingerprinter {

    private static final Logger log = LoggerFactory.getLogger(MediaFingerprinter.class);

    /** Fetch attempts before an image is given up. */
    static final int MAX_ATTEMPTS = 3;

    /** The fetch timeout. */
    static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final NarrativeStore store;
    private final NarrativeService service;
    private final MediaReuseDetector reuse;
    private final GuardedImages images;
    private final ObjectProvider<OutboundUrlPolicy> policy;
    private final ObjectProvider<PinnedHttp> pinned;
    private final ObjectProvider<MediaStore> mediaStore;
    private final ObjectProvider<InstallIdService> install;
    private final AppSettingService settings;
    private final TransactionTemplate transactions;
    private final Clock clock;
    private final PdqIndex index = new PdqIndex();
    private volatile boolean loaded;

    /** Creates the fingerprinter. */
    public MediaFingerprinter(NarrativeStore store, NarrativeService service, MediaReuseDetector reuse,
            GuardedImages images, ObjectProvider<OutboundUrlPolicy> policy, ObjectProvider<PinnedHttp> pinned,
            ObjectProvider<MediaStore> mediaStore, ObjectProvider<InstallIdService> install, AppSettingService settings,
            TransactionTemplate transactions, Clock clock) {
        this.store = store;
        this.service = service;
        this.reuse = reuse;
        this.images = images;
        this.policy = policy;
        this.pinned = pinned;
        this.mediaStore = mediaStore;
        this.install = install;
        this.settings = settings;
        this.transactions = transactions;
        this.clock = clock;
    }

    /** The matching index, loaded from the store on first use. */
    PdqIndex index() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    for (Map.Entry<String, String> e : store.matchable().entrySet()) {
                        index.add(e.getKey(), Pdq.parse(e.getValue()));
                    }
                    loaded = true;
                }
            }
        }
        return index;
    }

    /** Forgets the in-memory index (tests that truncate the tables). */
    public void reset() {
        synchronized (this) {
            index.clear();
            loaded = false;
        }
    }

    /**
     * The matchable fingerprints within {@code maxDistance} of a hash.
     *
     * @param query the hash
     * @param maxDistance the largest Hamming distance (0–63)
     * @param limit the most matches
     */
    public List<PdqIndex.Match> search(Pdq.Hash query, int maxDistance, int limit) {
        return index().search(query, maxDistance, limit);
    }

    /**
     * The PDQ hash of an image, decoded through the 043 guard.
     *
     * @param bytes the image
     * @param declaredContentType the declared type, or null
     * @throws ImageRejectedException when the guard refuses it
     */
    public Pdq.Hash hash(byte[] bytes, String declaredContentType) {
        return Pdq.hash(images.decode(bytes, declaredContentType));
    }

    /** Fingerprints up to {@code limit} queued images; the number processed. */
    public int drain(int limit) {
        int n = 0;
        for (NarrativeStore.QueueItem item : store.pending(limit)) {
            try {
                byte[] bytes = fetch(item.url());
                if (bytes == null) {
                    store.finish(item.id(), item.attempts() + 1 >= MAX_ATTEMPTS ? "failed" : "pending");
                    continue;
                }
                transactions.executeWithoutResult(status -> process(item, bytes));
                store.finish(item.id(), "done");
                n++;
            } catch (ImageRejectedException e) {
                log.debug("Post image {} refused: {}", item.url(), e.reason());
                store.finish(item.id(), "failed");
            } catch (RuntimeException e) {
                log.warn("Fingerprinting {} failed: {}", item.url(), e.toString());
                store.finish(item.id(), item.attempts() + 1 >= MAX_ATTEMPTS ? "failed" : "pending");
            }
        }
        return n;
    }

    /**
     * Fingerprints one fetched image of a post.
     *
     * @param item the queue item
     * @param bytes the image
     * @return the fingerprint id
     */
    public String process(NarrativeStore.QueueItem item, byte[] bytes) {
        Instant now = clock.instant();
        ImageInfo info = images.guard().inspect(bytes, null, false);
        Pdq.Hash hash = Pdq.hash(images.decode(bytes, info.format().contentType()));
        String postRef = RecordRef.format(item.sourceId(), item.externalId());
        NarrativeStore.Fingerprint fp = resolve(hash, item, postRef, now);
        if (Boolean.TRUE.equals(settings.get(SettingKeys.SOCIAL_KEEP_MEDIA, Boolean.class).orElse(false))
                && fp.mediaId() == null) {
            keep(fp.id(), bytes, info, item);
        }
        Optional<NarrativeStore.Member> member = store.memberByPost(item.postKey());
        String narrativeId = member.map(NarrativeStore.Member::narrativeId).orElse(item.narrativeId());
        store.recordUse(fp.id(), postRef, item.postKey(), item.url(), item.platform(), narrativeId, item.seenAt(), now);
        store.addFingerprint(item.postKey(), fp.id());
        if (narrativeId == null) {
            return fp.id();
        }
        store.addNarrative(fp.id(), narrativeId, now);
        store.markDirty(narrativeId, now);
        NarrativeStore.Fingerprint current = store.fingerprint(fp.id()).orElse(fp);
        reuse.check(current, narrativeId);
        joinByMedia(current, narrativeId, item, now);
        return fp.id();
    }

    /** The fingerprint a hash belongs to: identical, else the nearest matchable within 31, else a new one. */
    NarrativeStore.Fingerprint resolve(Pdq.Hash hash, NarrativeStore.QueueItem item, String postRef, Instant now) {
        Optional<NarrativeStore.Fingerprint> exact = store.fingerprintByPdq(hash.hex());
        if (exact.isPresent()) {
            return exact.get();
        }
        if (hash.quality() >= NarrativeVocabulary.PDQ_MIN_QUALITY) {
            List<PdqIndex.Match> near = index().search(hash, NarrativeVocabulary.PDQ_MATCH_DISTANCE, 1);
            if (!near.isEmpty()) {
                Optional<NarrativeStore.Fingerprint> f = store.fingerprint(near.get(0).id());
                if (f.isPresent()) {
                    return f.get();
                }
            }
        }
        NarrativeStore.Fingerprint created = new NarrativeStore.Fingerprint(Ulids.next(now), hash.hex(),
                hash.quality(), item.seenAt(), postRef, item.postKey(), 0,
                item.platform() == null ? List.of() : List.of(item.platform()), List.of(), null, List.of(), now, now);
        store.insertFingerprint(created);
        NarrativeStore.Fingerprint stored = store.fingerprintByPdq(hash.hex()).orElse(created);
        if (stored.quality() >= NarrativeVocabulary.PDQ_MIN_QUALITY) {
            index().add(stored.id(), Pdq.parse(stored.pdq()));
        }
        return stored;
    }

    /** Merges a one-post narrative into an active narrative that already holds the fingerprint. */
    void joinByMedia(NarrativeStore.Fingerprint fp, String narrativeId, NarrativeStore.QueueItem item, Instant now) {
        NarrativeStore.Narrative n = store.narrative(narrativeId).orElse(null);
        if (n == null || n.mergedInto() != null || n.postCount() > 1
                || fp.quality() < NarrativeVocabulary.PDQ_MIN_QUALITY) {
            return;
        }
        Instant from = item.seenAt().minus(Duration.ofHours(72));
        for (String other : fp.narrativeIds()) {
            if (other.equals(narrativeId)) {
                continue;
            }
            NarrativeStore.Narrative target = store.narrative(other).orElse(null);
            if (target != null && target.mergedInto() == null && !target.lastActivityAt().isBefore(from)) {
                service.merge(target.id(), List.of(narrativeId), "system", "identical media fingerprint " + fp.id());
                store.jdbc().update("UPDATE social_narrative_member SET link_reason = ?, matched_key = NULL WHERE"
                        + " post_key = ? AND narrative_id = ?", NarrativeVocabulary.MEDIA, item.postKey(), target.id());
                return;
            }
        }
    }

    private void keep(String fingerprintId, byte[] bytes, ImageInfo info, NarrativeStore.QueueItem item) {
        MediaStore media = mediaStore.getIfAvailable();
        if (media == null) {
            return;
        }
        try {
            String id = media.store(MediaInput.of(bytes, info.format().contentType(), MediaRole.IMAGE)
                    .withSourceId(item.sourceId()).withRecord(item.sourceId(), item.externalId())).id();
            store.setMediaId(fingerprintId, id);
        } catch (RuntimeException e) {
            log.debug("Could not keep the image of {}: {}", item.url(), e.toString());
        }
    }

    /**
     * Fetches an image over HTTPS through the outbound URL policy, at most the guard's size.
     *
     * @return the bytes, or null when refused or unavailable
     */
    byte[] fetch(String url) {
        OutboundUrlPolicy p = policy.getIfAvailable();
        PinnedHttp http = pinned.getIfAvailable();
        if (p == null || http == null || url == null) {
            return null;
        }
        try {
            URI uri = URI.create(url);
            OutboundUrlPolicy.Result result = p.check(uri, OutboundUrlPolicy.Profile.PUBLIC_ONLY, false);
            if (!(result instanceof OutboundUrlPolicy.Allowed allowed)) {
                return null;
            }
            HttpRequest.Builder builder = HttpRequest.newBuilder(PinnedHttp.pinnedUri(uri, allowed.pinned()))
                    .timeout(TIMEOUT).GET();
            PinnedHttp.setHostHeader(builder, uri);
            InstallIdService ids = install.getIfAvailable();
            if (ids != null) {
                builder.header("User-Agent", ids.userAgent());
            }
            HttpClient client = http.client(uri, new PinnedHttp.ClientSpec(TIMEOUT, HttpClient.Version.HTTP_1_1));
            HttpResponse<InputStream> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream in = response.body()) {
                if (response.statusCode() != 200) {
                    return null;
                }
                long max = images.guard().maxFileBytes();
                byte[] bytes = in.readNBytes((int) Math.min(Integer.MAX_VALUE - 8, max + 1));
                return bytes.length > max ? null : bytes;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException | RuntimeException e) {
            log.debug("Post image {} not fetched: {}", url, e.getMessage());
            return null;
        }
    }
}
