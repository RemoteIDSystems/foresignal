package ai.foresignal.briefing;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.share.ExtraShareTarget;
import ai.foresignal.share.ShareLinkEntity;
import ai.foresignal.share.ShareLinkService;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The {@value #TYPE} share-link target (prompt NP-005): a read-only, revocable 112 link to an edition's hosted site,
 * served at {@code /v1/k/{key}/site}. It grants that one page and nothing else: never writes, never another edition,
 * never records.
 *
 * <p>The edition must exist with its site enabled, and the creator must be able to see it (NP-003: its owner or an
 * instance credential, taken from the current request; the system creates the edition's own link while its owner
 * saves it). The site is checked again on every use, so a link stops serving when the edition is deleted or its site
 * turned off. The page changes with every run, so private and non-redistributable sources are checked on each use, not
 * at creation ({@link EditionSiteService}). A link may live up to {@link #MAX_TTL}, so the shared address stays put.
 */
@Component
public class EditionSiteShareTarget implements ExtraShareTarget {

    /** The target type. */
    public static final String TYPE = "edition-site";

    /** The longest life of an edition-site link. */
    public static final Duration MAX_TTL = Duration.ofDays(365);

    private final BriefingRepository briefings;

    /** Creates the target. */
    public EditionSiteShareTarget(BriefingRepository briefings) {
        this.briefings = briefings;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Duration maxTtl() {
        return MAX_TTL;
    }

    @Override
    public Set<String> check(String ref, ShareLinkService.Caller caller) {
        BriefingEntity entity = edition(ref);
        if (!BriefingService.visibleTo(entity, currentViewer())) {
            throw NotFoundException.of("Briefing", ref);
        }
        if (!entity.isSiteEnabled()) {
            throw ValidationException.forField("targetRef", "the edition's site is off (site.enabled is false)");
        }
        return Set.of();
    }

    /**
     * The edition a link serves, while its site is on.
     *
     * @throws NotFoundException when the edition is gone or its site is off
     */
    public BriefingEntity served(ShareLinkEntity link) {
        BriefingEntity entity;
        try {
            entity = edition(link.getTargetRef());
        } catch (NotFoundException e) {
            throw gone();
        }
        if (!entity.isSiteEnabled()) {
            throw gone();
        }
        return entity;
    }

    @Override
    public ShareLinkService.Rendered render(ShareLinkEntity link) {
        BriefingEntity entity = served(link);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("briefingId", entity.getId().toString());
        data.put("name", entity.getName());
        data.put("html", "site");
        return new ShareLinkService.Rendered(data, List.of(), entity.getName(), List.of());
    }

    private BriefingEntity edition(String ref) {
        UUID id;
        try {
            id = UUID.fromString(ref);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw NotFoundException.of("Briefing", ref);
        }
        return briefings.findById(id).orElseThrow(() -> NotFoundException.of("Briefing", ref));
    }

    /** The viewer of the current HTTP request, or an instance credential outside one (the scheduler). */
    static BriefingService.EditionViewer currentViewer() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
            boolean anonymous = ai.foresignal.security.CurrentToken.of(request).isEmpty();
            return BriefingService.EditionViewer.of(request, anonymous);
        }
        return BriefingService.EditionViewer.INSTANCE;
    }

    /** The edition is gone or its site is off. */
    public static NotFoundException gone() {
        return new NotFoundException(ErrorCodes.NOT_FOUND, "The shared edition site is no longer available.");
    }
}
