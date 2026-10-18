package ai.foresignal.chronicle;

import ai.foresignal.collab.ShareableSource;
import ai.foresignal.collab.ShareableTypes;
import ai.foresignal.collab.Visibility;
import ai.foresignal.security.PrincipalService;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Chronicles as a 130 shareable type ({@code chronicle}): {@link ChronicleService} registers each chronicle's owner,
 * visibility and TLP on creation; these defaults (the instance {@code owner} principal, {@code private}) only repair
 * a missing registration. Display ids ({@code CHR-yyyy-nnnn}) resolve to the canonical id.
 */
@Component
public class ChronicleShareableSource implements ShareableSource {

    private final ChronicleRepository chronicles;
    private final PrincipalService principals;

    /** Creates the source. */
    public ChronicleShareableSource(ChronicleRepository chronicles, PrincipalService principals) {
        this.chronicles = chronicles;
        this.principals = principals;
    }

    @Override
    public String type() {
        return ShareableTypes.CHRONICLE;
    }

    @Override
    public Optional<Defaults> defaults(String id) {
        Optional<ChronicleEntity> found = ai.foresignal.security.Ulids.isUlid(id) ? chronicles.findById(id)
                : Optional.empty();
        if (found.isEmpty() && id.startsWith("CHR-")) {
            found = chronicles.findByDisplayId(id);
        }
        return found.map(c -> new Defaults(c.getId(), principals.owner().getId(), Visibility.PRIVATE, null));
    }
}
