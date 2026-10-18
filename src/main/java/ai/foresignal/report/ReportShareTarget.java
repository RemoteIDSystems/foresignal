package ai.foresignal.report;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.share.ExtraShareTarget;
import ai.foresignal.share.ShareLinkEntity;
import ai.foresignal.share.ShareLinkService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * The {@code report} share-link target (prompt 132, {@code createReportShareLink}): a read-only 112 link to the latest
 * published HTML rendition, served at {@code /v1/k/{key}/report}. Only reports labeled {@code CLEAR} may have one, or
 * {@code GREEN} when the app setting {@code exchange.allowGreenLinks} is true; the label is checked again on every use,
 * so a report relabeled stricter, retracted or deleted stops being served.
 */
@Component
public class ReportShareTarget implements ExtraShareTarget {

    /** The target type. */
    public static final String TYPE = "report";

    private final ReportRepository reports;
    private final ReportRevisionRepository revisions;
    private final AccessResolver access;
    private final AppSettingService settings;

    /** Creates the target. */
    public ReportShareTarget(ReportRepository reports, ReportRevisionRepository revisions, AccessResolver access,
            AppSettingService settings) {
        this.reports = reports;
        this.revisions = revisions;
        this.access = access;
        this.settings = settings;
    }

    @Override
    public String type() {
        return TYPE;
    }

    /** True when GREEN reports may have links ({@code exchange.allowGreenLinks}). */
    public boolean allowGreen() {
        try {
            return settings.get(SettingKeys.EXCHANGE_ALLOW_GREEN_LINKS, Boolean.class).orElse(false);
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public Set<String> check(String ref, ShareLinkService.Caller caller) {
        ReportEntity r = reports.findById(ref).orElseThrow(() -> ReportService.unknown(ref));
        ReportService.checkPublicLink(access.tlpOf(ReportService.ref(r.getId())), allowGreen());
        published(r).orElseThrow(() -> ReportPublisher.notPublished(r, "is not published yet"));
        return Set.of();
    }

    /** The latest published, not retracted revision of a report. */
    Optional<ReportRevisionEntity> published(ReportEntity r) {
        return revisions.findFirstByReportIdOrderByRevisionDesc(r.getId())
                .filter(rev -> ReportVocabulary.PUBLISHED.equals(rev.getStatus()));
    }

    /**
     * The HTML a report link serves: the latest published rendition, while the label still allows public links.
     *
     * @throws NotFoundException {@code UNKNOWN_REPORT} when the report is gone, retracted or relabeled
     */
    public String html(ShareLinkEntity link) {
        ReportEntity r = reports.findById(link.getTargetRef()).orElseThrow(() -> gone());
        try {
            ReportService.checkPublicLink(access.tlpOf(ReportService.ref(r.getId())), allowGreen());
        } catch (RuntimeException e) {
            throw gone();
        }
        return published(r).map(ReportRevisionEntity::getHtml).orElseThrow(ReportShareTarget::gone);
    }

    @Override
    public ShareLinkService.Rendered render(ShareLinkEntity link) {
        ReportEntity r = reports.findById(link.getTargetRef()).orElseThrow(ReportShareTarget::gone);
        ReportRevisionEntity rev = published(r).orElseThrow(ReportShareTarget::gone);
        try {
            ReportService.checkPublicLink(access.tlpOf(ReportService.ref(r.getId())), allowGreen());
        } catch (RuntimeException e) {
            throw gone();
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reportId", r.getId());
        data.put("displayId", r.getDisplayId());
        data.put("title", rev.getTitle());
        data.put("revision", rev.getRevision());
        data.put("tlp", rev.getTlp());
        data.put("publishedAt", rev.getPublishedAt().toString());
        data.put("html", "report");
        return new ShareLinkService.Rendered(data, List.of(), r.getDisplayId() + " " + rev.getTitle(), List.of());
    }

    private static NotFoundException gone() {
        return new NotFoundException(ErrorCodes.UNKNOWN_REPORT, "The shared report is no longer available.");
    }
}
