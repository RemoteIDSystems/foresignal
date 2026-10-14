package ai.foresignal.source.adapters.internet;

import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers one {@link StatusPageAdapter} per reviewed Statuspage v2 page (prompt 092). The list is static and
 * code-reviewed; no caller can add a host. Fastly is not on Statuspage and is left out.
 */
@Component
public class StatusPageFamily implements SourceAdapterFamily {

    /** Test-only override moving every page under one base. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.status.base-url";

    /** One reviewed Statuspage page. */
    public record Page(String id, String host, String name) {}

    /** The reviewed pages (hosts verified 2026-10). */
    public static final List<Page> REVIEWED = List.of(
            new Page("status-github", "www.githubstatus.com", "GitHub"),
            new Page("status-atlassian", "status.atlassian.com", "Atlassian"),
            new Page("status-discord", "discordstatus.com", "Discord"),
            new Page("status-reddit", "www.redditstatus.com", "Reddit"),
            new Page("status-dropbox", "status.dropbox.com", "Dropbox"),
            new Page("status-twilio", "status.twilio.com", "Twilio"),
            new Page("status-zoom", "www.zoomstatus.com", "Zoom"),
            new Page("status-anthropic", "status.claude.com", "Anthropic"),
            new Page("status-cloudflare", "www.cloudflarestatus.com", "Cloudflare"),
            new Page("status-digitalocean", "status.digitalocean.com", "DigitalOcean"),
            new Page("status-openai", "status.openai.com", "OpenAI"));

    private final List<SourceAdapter> adapters;

    /** Creates the family. */
    @Autowired
    public StatusPageFamily(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        List<SourceAdapter> built = new ArrayList<>();
        for (Page page : REVIEWED) {
            built.add(new StatusPageAdapter(page, baseUrlOverride));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
