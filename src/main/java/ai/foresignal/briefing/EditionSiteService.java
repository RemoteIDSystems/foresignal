package ai.foresignal.briefing;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.ApiTokenRepository;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.feed.FeedKeyService;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.share.ShareLinkService;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * An edition's hosted site (prompt NP-005): the run it shows and the path keys that keep its URLs stable.
 *
 * <ul>
 *   <li><b>The page</b> is the latest {@code succeeded} or {@code fallback} run ({@link #latest}).
 *   <li><b>The feed key</b> ({@value #FEED}): a 035 {@code fsf_} key whose only prefix is
 *       {@code /v1/editions/{id}/site}, read scope, no private grant, living {@link #KEY_LIFETIME}. It reaches
 *       {@code /v1/k/{key}/editions/{id}/site} and nothing else.
 *   <li><b>The share link</b> ({@value #SHARE}): a 112 {@code fss_} link of target type
 *       {@value EditionSiteShareTarget#TYPE}, kept while {@code site.shareable} is on, served at
 *       {@code /v1/k/{key}/site}.
 *   <li>Both are minted by {@link #sync} when the edition is saved with its site on, and renewed there or after a
 *       run when less than {@link #RENEW_BEFORE} is left. The plaintext keys are stored AES-256-GCM encrypted
 *       ({@link CredentialCipher}) in {@code edition_site_key}, because {@code EditionSite} returns the URLs on every
 *       read. Turning the site off revokes both; turning sharing off revokes the link; deleting the edition revokes
 *       both ({@link #revokeAll}).
 *   <li>A key revoked from outside (an operator's {@code revokeShareLink} or {@code revokeFeedKey}) stays revoked:
 *       it is not silently replaced. Its URL reads as null until the owner turns the setting off and on again.
 * </ul>
 */
@Service
public class EditionSiteService {

    private static final Logger log = LoggerFactory.getLogger(EditionSiteService.class);

    /** The feed-key kind. */
    public static final String FEED = "feed";

    /** The share-link kind. */
    public static final String SHARE = "share";

    /** How long a minted key lives. */
    public static final Duration KEY_LIFETIME = Duration.ofDays(365);

    /** A key with less left than this is renewed. */
    public static final Duration RENEW_BEFORE = Duration.ofDays(30);

    private static final ShareLinkService.Caller SYSTEM = new ShareLinkService.Caller(null, "edition-site", false,
            Viewer.anonymous(), null, ReadRestriction.NONE, Set.of());

    private static final ShareLinkService.Caller SYSTEM_ADMIN = new ShareLinkService.Caller(null, "edition-site", true,
            Viewer.admin(), null, ReadRestriction.NONE, Set.of());

    private final BriefingRunRepository runs;
    private final JdbcTemplate jdbc;
    private final CredentialCipher cipher;
    private final ApiTokenRepository tokens;
    private final ObjectProvider<FeedKeyService> feedKeys;
    private final ObjectProvider<ShareLinkService> shares;
    private final Clock clock;

    /** Creates the service. */
    public EditionSiteService(BriefingRunRepository runs, JdbcTemplate jdbc, CredentialCipher cipher,
            ApiTokenRepository tokens, ObjectProvider<FeedKeyService> feedKeys, ObjectProvider<ShareLinkService> shares,
            Clock clock) {
        this.runs = runs;
        this.jdbc = jdbc;
        this.cipher = cipher;
        this.tokens = tokens;
        this.feedKeys = feedKeys;
        this.shares = shares;
        this.clock = clock;
    }

    /** The run an edition's site shows: its latest {@code succeeded} or {@code fallback} run. */
    public Optional<BriefingRunEntity> latest(UUID briefingId) {
        return runs.findFirstByBriefingIdAndStatusInOrderByStartedAtDescIdDesc(briefingId,
                List.of(BriefingRunEntity.SUCCEEDED, BriefingRunEntity.FALLBACK));
    }

    /**
     * A stored key.
     *
     * @param kind {@value #FEED} or {@value #SHARE}
     * @param keyId the {@code api_token} id or {@code share_link} id
     * @param key the plaintext key
     * @param expiresAt when it expires
     */
    public record Key(String kind, String keyId, String key, Instant expiresAt) {}

    /** The edition's working key of {@code kind}: stored, not expired and not revoked; empty otherwise. */
    public Optional<String> key(UUID briefingId, String kind) {
        return stored(briefingId, kind).filter(this::active).map(Key::key);
    }

    /**
     * Brings an edition's keys in line with its {@code site}: mints what is missing or about to expire while the site
     * (and sharing) is on, and revokes what it no longer needs. A failure is logged and never fails the save or run.
     */
    public void sync(BriefingEntity entity) {
        try {
            if (!entity.isSiteEnabled()) {
                revoke(entity.getId(), FEED);
                revoke(entity.getId(), SHARE);
                return;
            }
            ensure(entity.getId(), FEED);
            if (entity.isSiteShareable()) {
                ensure(entity.getId(), SHARE);
            } else {
                revoke(entity.getId(), SHARE);
            }
        } catch (RuntimeException e) {
            log.warn("The site keys of edition {} could not be updated", entity.getId(), e);
        }
    }

    /** Revokes and forgets every key of an edition (it is being deleted). */
    public void revokeAll(UUID briefingId) {
        revoke(briefingId, FEED);
        revoke(briefingId, SHARE);
    }

    private void ensure(UUID briefingId, String kind) {
        Optional<Key> current = stored(briefingId, kind);
        Instant now = clock.instant();
        if (current.isPresent()) {
            if (revoked(current.get())) {
                return;
            }
            if (current.get().expiresAt().isAfter(now.plus(RENEW_BEFORE))) {
                return;
            }
        }
        Key minted = FEED.equals(kind) ? mintFeed(briefingId, now) : mintShare(briefingId, now);
        Encrypted secret = cipher.encrypt(minted.key());
        jdbc.update("INSERT INTO edition_site_key (briefing_id, kind, key_id, key_ciphertext, key_nonce, expires_at,"
                + " created_at) VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (briefing_id, kind) DO UPDATE SET"
                + " key_id = excluded.key_id, key_ciphertext = excluded.key_ciphertext, key_nonce = excluded.key_nonce,"
                + " expires_at = excluded.expires_at, created_at = excluded.created_at", briefingId, kind,
                minted.keyId(), secret.ciphertext(), secret.nonce(), Timestamp.from(minted.expiresAt()),
                Timestamp.from(now));
        current.ifPresent(old -> revokeKey(old));
    }

    private Key mintFeed(UUID briefingId, Instant now) {
        Instant expires = now.plus(KEY_LIFETIME);
        FeedKeyService.CreatedKey created = feedKeys.getObject().mint(ApiTokenEntity.TYPE_FEED_KEY,
                FeedKeyService.KEY_PREFIX, "edition-site " + briefingId, List.of(sitePath(briefingId)),
                TokenRestrictions.NONE, expires, false);
        return new Key(FEED, created.entity().getId().toString(), created.key(), expires);
    }

    private Key mintShare(UUID briefingId, Instant now) {
        Instant expires = now.plus(KEY_LIFETIME);
        ShareLinkService.Created created = shares.getObject().create(new ShareLinkService.Create(
                EditionSiteShareTarget.TYPE, briefingId.toString(), null, null, expires, null), SYSTEM);
        return new Key(SHARE, created.link().getId(), created.key(), expires);
    }

    private void revoke(UUID briefingId, String kind) {
        Optional<Key> current = stored(briefingId, kind);
        if (current.isEmpty()) {
            return;
        }
        revokeKey(current.get());
        jdbc.update("DELETE FROM edition_site_key WHERE briefing_id = ? AND kind = ?", briefingId, kind);
    }

    private void revokeKey(Key key) {
        try {
            if (FEED.equals(key.kind())) {
                feedKeys.getObject().revoke(UUID.fromString(key.keyId()));
            } else {
                shares.getObject().revoke(key.keyId(), SYSTEM_ADMIN);
            }
        } catch (NotFoundException e) {
            // Already gone.
        } catch (RuntimeException e) {
            log.warn("The {} key {} of an edition site could not be revoked", key.kind(), key.keyId(), e);
        }
    }

    private boolean active(Key key) {
        return key.expiresAt().isAfter(clock.instant()) && !revoked(key);
    }

    /** True when the key's row is revoked, expired or gone. */
    private boolean revoked(Key key) {
        Instant now = clock.instant();
        if (FEED.equals(key.kind())) {
            return tokens.findById(UUID.fromString(key.keyId()))
                    .map(t -> t.getRevokedAt() != null || (t.getExpiresAt() != null && !t.getExpiresAt().isAfter(now)))
                    .orElse(true);
        }
        return shares.getObject().find(key.key()).map(l -> !"active".equals(l.status(now))).orElse(true);
    }

    private Optional<Key> stored(UUID briefingId, String kind) {
        List<Key> rows = jdbc.query("SELECT key_id, key_ciphertext, key_nonce, expires_at FROM edition_site_key"
                + " WHERE briefing_id = ? AND kind = ?", (rs, i) -> new Key(kind, rs.getString(1),
                        cipher.decryptToString(new Encrypted(rs.getBytes(2), rs.getBytes(3))),
                        rs.getTimestamp(4).toInstant()), briefingId, kind);
        return rows.stream().findFirst();
    }

    /** The canonical path of an edition's site. */
    public static String sitePath(UUID briefingId) {
        return "/v1/editions/" + briefingId + "/site";
    }
}
