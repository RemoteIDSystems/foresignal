package ai.foresignal.source.adapters.internet;

import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers {@code status-gcp} and {@code status-google-workspace} (prompt 092).
 */
@Component
public class GoogleStatusFamily implements SourceAdapterFamily {

    /** One Google status page. */
    public record Page(String id, String name, String url, String termsHost) {}

    /** The two reviewed pages. */
    public static final List<Page> REVIEWED = List.of(
            new Page("status-gcp", "Google Cloud", "https://status.cloud.google.com/incidents.json",
                    "cloud.google.com"),
            new Page("status-google-workspace", "Google Workspace",
                    "https://www.google.com/appsstatus/dashboard/incidents.json", "workspace.google.com"));

    private final List<SourceAdapter> adapters;

    /** Creates the family. */
    @Autowired
    public GoogleStatusFamily(@Value("${" + GoogleStatusAdapter.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        List<SourceAdapter> built = new ArrayList<>();
        for (Page page : REVIEWED) {
            built.add(new GoogleStatusAdapter(rebased(page, baseUrlOverride), baseUrlOverride));
        }
        this.adapters = List.copyOf(built);
    }

    private static Page rebased(Page page, String baseUrlOverride) {
        if (baseUrlOverride == null || baseUrlOverride.isBlank()) {
            return page;
        }
        return new Page(page.id(), page.name(), baseUrlOverride + "/incidents.json", page.termsHost());
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
