package ai.foresignal.share;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.security.TokenHasher;
import ai.foresignal.security.restrict.PathKeyResolver;
import java.time.Clock;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * The {@code fss_} share-link keys of {@code PathKeyFilter} (prompt 112; Conventions "Key-in-path URLs"): the 035
 * matcher picks this resolver by prefix, refuses unknown keys ({@code 401 INVALID_SHARE_KEY}) and revoked, expired
 * or exhausted ones ({@code 410 SHARE_EXPIRED}), counting both against the client's failed-credential backoff,
 * and allows only the share-link sub-paths: the key itself, {@code /geojson}, {@code /tiles/…}, {@code /download}
 * and (prompt 132) {@code /report} (the controller answers {@code 404} for a sub-path the target type does not have, as the
 * spec says). The request is not forwarded
 * ({@link #forwards()}); the share-link controller serves {@code /v1/k/{key}/…} and counts the use. The key is
 * never a bearer token ({@code TokenService} accepts only {@code fsr_}).
 */
@Component
public class ShareKeyResolver implements PathKeyResolver {

    /** The sub-paths after the key ({@code ""} is the key itself). */
    static final List<String> SUB_PATHS = List.of("", "/geojson", "/tiles/", "/download", "/report");

    private final ShareLinkRepository links;
    private final TokenHasher hasher;
    private final Clock clock;

    /** Creates the resolver (only the repository and the hasher, so the filter chain pulls in nothing heavier). */
    public ShareKeyResolver(ShareLinkRepository links, TokenHasher hasher, Clock clock) {
        this.links = links;
        this.hasher = hasher;
        this.clock = clock;
    }

    @Override
    public String prefix() {
        return ShareLinkService.KEY_PREFIX;
    }

    @Override
    public Resolution resolve(String key) {
        if (key == null || !key.startsWith(ShareLinkService.KEY_PREFIX)) {
            return new Invalid();
        }
        return links.findByKeyHash(hasher.hash(key)).filter(l -> hasher.matches(key, l.getKeyHash()))
                .<Resolution>map(link -> "active".equals(link.status(clock.instant()))
                ? new Valid(null, SUB_PATHS) : new Gone()).orElseGet(Invalid::new);
    }

    @Override
    public boolean forwards() {
        return false;
    }

    @Override
    public String invalidCode() {
        return ErrorCodes.INVALID_SHARE_KEY;
    }

    @Override
    public HttpStatus goneStatus() {
        return HttpStatus.GONE;
    }

    @Override
    public String goneCode() {
        return ErrorCodes.SHARE_EXPIRED;
    }

    @Override
    public String outsidePrefixCode() {
        return ErrorCodes.RESTRICTED;
    }
}
