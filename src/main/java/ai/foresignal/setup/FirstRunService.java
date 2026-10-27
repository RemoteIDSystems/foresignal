package ai.foresignal.setup;

import ai.foresignal.security.user.UserRepository;
import ai.foresignal.settings.SmtpSettings;
import ai.foresignal.settings.SmtpSettingsService;
import ai.foresignal.system.ApiVersion;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Whether the instance still needs first-run setup ({@code getFirstRunStatus}, prompt R2-027): {@code firstRun} while
 * no user exists. The answer is open, so the admin app can offer setup before any account exists; it says nothing a
 * stranger could use beyond whether setup is done, whether account email is configured and how many sources collect.
 */
@Service
public class FirstRunService {

    private final UserRepository users;
    private final SmtpSettingsService smtp;
    private final JdbcTemplate jdbc;
    private final ApiVersion apiVersion;

    /**
     * Creates the service.
     *
     * @param users the users
     * @param smtp the account email settings
     * @param jdbc the template, for the source count
     * @param apiVersion the contract version
     */
    public FirstRunService(UserRepository users, SmtpSettingsService smtp, JdbcTemplate jdbc, ApiVersion apiVersion) {
        this.users = users;
        this.smtp = smtp;
        this.jdbc = jdbc;
        this.apiVersion = apiVersion;
    }

    /**
     * True while no user exists.
     *
     * @return whether setup is still needed
     */
    public boolean firstRun() {
        return users.count() == 0;
    }

    /**
     * The {@code FirstRunStatus}.
     *
     * @return {@code firstRun}, {@code hasUsers}, {@code smtpConfigured}, {@code sourcesEnabled} and
     *     {@code apiVersion}
     */
    public Map<String, Object> status() {
        boolean hasUsers = users.count() > 0;
        SmtpSettings settings = smtp.get();
        Long enabled = jdbc.queryForObject("SELECT count(*) FROM source_settings WHERE enabled", Long.class);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("firstRun", !hasUsers);
        out.put("hasUsers", hasUsers);
        out.put("smtpConfigured", settings.enabled() && settings.host() != null && !settings.host().isBlank());
        out.put("sourcesEnabled", enabled == null ? 0 : enabled.intValue());
        out.put("apiVersion", apiVersion.version());
        return out;
    }
}
