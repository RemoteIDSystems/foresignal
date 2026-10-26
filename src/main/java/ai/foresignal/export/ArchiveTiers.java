package ai.foresignal.export;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** The {@link ArchiveTier} beans by target (prompt R2-016). */
@Component
public class ArchiveTiers {

    private final Map<String, ArchiveTier> byTarget = new LinkedHashMap<>();

    /** Creates the registry. */
    public ArchiveTiers(List<ArchiveTier> tiers) {
        tiers.forEach(tier -> byTarget.put(tier.target(), tier));
    }

    /**
     * The tier of {@code target}.
     *
     * @throws IllegalStateException when no tier serves it
     */
    public ArchiveTier forTarget(String target) {
        ArchiveTier tier = byTarget.get(target);
        if (tier == null) {
            throw new IllegalStateException("No archive tier serves the target " + target);
        }
        return tier;
    }
}
