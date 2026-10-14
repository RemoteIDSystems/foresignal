package ai.foresignal.source.adapters.cap;

import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers one {@link CapFeedAdapter} per reviewed CAP feed (prompt 089): {@value CapFeeds#RESOURCE},
 * validated at startup. Every source ships disabled.
 *
 * <p>The list is code-reviewed and static; the WMO Register is never read at runtime and no caller can add a
 * feed. An invalid list stops Foresignal rather than registering a URL nobody reviewed.
 */
@Component
public class CapFeedAdapterFamily implements SourceAdapterFamily {

    /** Test-only override moving every feed URL under one base (scheme and authority), keeping the path. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.cap.base-url";

    private final List<SourceAdapter> adapters;

    /** Creates the family. */
    @Autowired
    public CapFeedAdapterFamily(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this(baseUrlOverride, CapFeeds.load());
    }

    /** Creates the family from an explicit list (tests). */
    public CapFeedAdapterFamily(String baseUrlOverride, List<CapFeed> feeds) {
        List<String> problems = CapFeeds.problems(feeds);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Foresignal cannot start: the reviewed CAP feed list is invalid: "
                    + problems);
        }
        String base = baseUrlOverride == null || baseUrlOverride.isBlank() ? null : baseUrlOverride.strip();
        List<SourceAdapter> built = new ArrayList<>();
        for (CapFeed feed : feeds) {
            built.add(new CapFeedAdapter(base == null ? feed : feed.rebased(base)));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
