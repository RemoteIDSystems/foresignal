package ai.foresignal.report;

import ai.foresignal.collab.ShareableSource;
import ai.foresignal.collab.ShareableTypes;
import ai.foresignal.collab.Visibility;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Ulids;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Reports as a 130 shareable type ({@code report}): {@link ReportService} registers each report's owner, visibility
 * and TLP on creation; these defaults (the first author, else the instance {@code owner} principal, {@code private})
 * only repair a missing registration. Display ids ({@code RPT-yyyy-nnnn}) resolve to the canonical id, a block
 * reference ({@code report:{id}#{blockId}}, the target of a comment on one block) resolves to its report, and every
 * author other than the owner holds the {@code editor} role intrinsically.
 */
@Component
public class ReportShareableSource implements ShareableSource {

    private final ReportRepository reports;
    private final PrincipalService principals;

    /** Creates the source. */
    public ReportShareableSource(ReportRepository reports, PrincipalService principals) {
        this.reports = reports;
        this.principals = principals;
    }

    @Override
    public String type() {
        return ShareableTypes.REPORT;
    }

    @Override
    public Optional<Defaults> defaults(String id) {
        return find(id).map(r -> new Defaults(r.getId(), r.getAuthors().isEmpty() ? principals.owner().getId()
                : r.getAuthors().getFirst(), Visibility.PRIVATE, null));
    }

    @Override
    public ai.foresignal.collab.AclRole intrinsicRole(String id, String principalId) {
        if (principalId == null) {
            return null;
        }
        return find(id).filter(r -> r.getAuthors().contains(principalId))
                .map(r -> ai.foresignal.collab.AclRole.EDITOR).orElse(null);
    }

    private Optional<ReportEntity> find(String raw) {
        String id = raw.indexOf('#') > 0 ? raw.substring(0, raw.indexOf('#')) : raw;
        Optional<ReportEntity> found = Ulids.isUlid(id) ? reports.findById(id) : Optional.empty();
        if (found.isEmpty() && id.startsWith(ReportVocabulary.DISPLAY_PREFIX)) {
            found = reports.findByDisplayId(id);
        }
        return found;
    }
}
