package ai.foresignal.system;

import ai.foresignal.security.SecureRequestMonitor;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * The {@code security} block of the full {@code /v1/system} document ({@code SystemInfo.security},
 * prompts 023 and 035): {@code openReads}, {@code anonymousRestricted} and the security warnings.
 *
 * <p>{@value #ANONYMOUS_UNRESTRICTED} (warning) applies when {@code openReads} is on,
 * {@code publicBaseUrl} names a host that is not loopback or private, and the anonymous profile
 * limits no source, kind or area: anyone who can reach that address reads everything public. It is
 * logged at startup and whenever the security or server setting changes.
 */
@Component
public class SecuritySystemBlock implements SystemBlockContributor {

    /** The warning code of an exposed, unrestricted anonymous profile. */
    public static final String ANONYMOUS_UNRESTRICTED = "ANONYMOUS_UNRESTRICTED";

    private static final Logger log = LoggerFactory.getLogger(SecuritySystemBlock.class);

    private final AppSettingService settings;
    private final SecureRequestMonitor secureRequests;

    /** Creates the contributor. */
    public SecuritySystemBlock(AppSettingService settings, SecureRequestMonitor secureRequests) {
        this.settings = settings;
        this.secureRequests = secureRequests;
    }

    @Override
    public String key() {
        return "security";
    }

    @Override
    public Map<String, Object> block() {
        SecuritySettings security = security();
        ServerSettings server = server();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("openReads", security.openReads());
        out.put("anonymousRestricted", security.anonymous() != null && !security.anonymous().isEmpty());
        List<Map<String, Object>> warnings = new ArrayList<>();
        List<SystemWarnings.Warning> all = new ArrayList<>(PublicBaseUrlWarnings.of(server, secureRequests.sawHttps()));
        anonymousWarning(security, server).ifPresent(all::add);
        for (SystemWarnings.Warning w : all) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", w.code());
            m.put("severity", w.severity());
            m.put("message", w.message());
            warnings.add(m);
        }
        out.put("warnings", warnings);
        return out;
    }

    /** The {@value #ANONYMOUS_UNRESTRICTED} warning, when it applies. */
    public static java.util.Optional<SystemWarnings.Warning> anonymousWarning(SecuritySettings security,
            ServerSettings server) {
        if (!security.openReads() || !security.anonymousUnrestricted()) {
            return java.util.Optional.empty();
        }
        String host = SystemWarnings.host(server.normalizedPublicBaseUrl());
        if (host == null || SystemWarnings.isLocal(host)) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(new SystemWarnings.Warning(ANONYMOUS_UNRESTRICTED, "warning",
                "Data reads need no token (security.openReads), publicBaseUrl names a public host (" + host
                        + ") and the anonymous profile limits no source, kind or area. Restrict"
                        + " security.anonymous, use feed keys or restricted tokens, or turn openReads off."));
    }

    /** Logs the warning at startup. */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        logWarning();
    }

    /** Logs the warning again when the security or server setting changes. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.SECURITY.equals(event.key()) || SettingKeys.SERVER.equals(event.key())) {
            logWarning();
        }
    }

    private void logWarning() {
        try {
            anonymousWarning(security(), server()).ifPresent(w -> log.warn("{}: {}", w.code(), w.message()));
        } catch (RuntimeException e) {
            log.debug("Could not evaluate the anonymous-profile warning: {}", e.toString());
        }
    }

    private SecuritySettings security() {
        return settings.getOrDefault(SettingKeys.SECURITY, SecuritySettings.class, SecuritySettings::defaults);
    }

    private ServerSettings server() {
        return settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
    }
}
