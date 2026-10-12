package ai.foresignal.system;

import ai.foresignal.retention.RetentionJob;
import ai.foresignal.retention.RetentionRun;
import ai.foresignal.security.SecureRequestMonitor;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.LocalAddresses;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.PartitionManager;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration and operating warnings for {@code /v1/system.warnings} (prompt 019).
 *
 * <ul>
 *   <li>{@code OPEN_READS_EXPOSED} (warning): {@code openReads} is on and the public base URL's host
 *       or the advertised bind is not local: loopback, RFC 1918, CGNAT 100.64/10 (tailnets),
 *       link-local, IPv6 unique-local, {@code localhost} or a {@code .local} name. Host names are not
 *       resolved, so any other name counts as exposed.
 *   <li>{@code OPEN_READS_ALL_INTERFACES} (info): {@code openReads} is on and the bind is all
 *       interfaces ({@code 0.0.0.0} or {@code ::}).
 *   <li>{@code HISTORY_RATE_HIGH} (warning): more than 20,000 versions per minute.
 *   <li>{@code PARTITION_HEADROOM_LOW} (critical): a tier has less than one block attached ahead.
 *   <li>{@code RETENTION_CONSUMER_LAG} (warning): the last retention run released a consumer clamp.
 *   <li>{@code PUBLIC_BASE_URL_INSECURE} and {@code PUBLIC_BASE_URL_MISMATCH} (warning, prompt 023):
 *       see {@link PublicBaseUrlWarnings}.
 * </ul>
 */
@Component
public class SystemWarnings {

    private static final Pattern IPV4 = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");

    /**
     * One warning.
     *
     * @param code the stable code
     * @param severity {@code info}, {@code warning} or {@code critical}
     * @param message what to do about it
     */
    public record Warning(String code, String severity, String message) {}

    private final AppSettingService settings;
    private final PartitionManager partitions;
    private final RetentionJob retention;
    private final String advertisedBind;
    private final SecureRequestMonitor secureRequests;

    /** Creates the component without a request monitor (no {@code PUBLIC_BASE_URL_MISMATCH}). */
    public SystemWarnings(AppSettingService settings, PartitionManager partitions, RetentionJob retention,
            String advertisedBind) {
        this(settings, partitions, retention, advertisedBind, new SecureRequestMonitor());
    }

    /** Creates the component. */
    @Autowired
    public SystemWarnings(AppSettingService settings, PartitionManager partitions, RetentionJob retention,
            @Value("${foresignal.advertised-bind:127.0.0.1}") String advertisedBind,
            SecureRequestMonitor secureRequests) {
        this.settings = settings;
        this.partitions = partitions;
        this.retention = retention;
        this.advertisedBind = advertisedBind == null ? "" : advertisedBind.strip();
        this.secureRequests = secureRequests;
    }

    /** The bind the container publishes the port on ({@code FORESIGNAL_BIND}). */
    public String advertisedBind() {
        return advertisedBind;
    }

    /** Every warning that applies now, given the current history snapshot. */
    public List<Warning> warnings(HistoryStats.Snapshot history) {
        List<Warning> out = new ArrayList<>();
        SecuritySettings security =
                settings.getOrDefault(SettingKeys.SECURITY, SecuritySettings.class, SecuritySettings::defaults);
        ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
        out.addAll(PublicBaseUrlWarnings.of(server, secureRequests.sawHttps()));
        if (security.openReads()) {
            String baseHost = host(server.normalizedPublicBaseUrl());
            boolean bindAll = isAllInterfaces(advertisedBind);
            boolean bindExposed = !bindAll && !isLocal(advertisedBind);
            boolean baseExposed = baseHost != null && !isLocal(baseHost);
            if (bindExposed || baseExposed) {
                out.add(new Warning("OPEN_READS_EXPOSED", "warning",
                        "Data reads need no token (security.openReads) and Foresignal is reachable beyond the local"
                                + " network (" + (baseExposed ? "publicBaseUrl host " + baseHost : "bind " + advertisedBind)
                                + "). Turn openReads off or keep Foresignal on a private network."));
            }
            if (bindAll) {
                out.add(new Warning("OPEN_READS_ALL_INTERFACES", "info",
                        "The port is published on all interfaces (FORESIGNAL_BIND=" + advertisedBind
                                + ") and data reads need no token; anyone on those networks can read."));
            }
        }
        if (history != null && history.versionsPerMinute() > HistoryStats.HIGH_RATE) {
            out.add(new Warning("HISTORY_RATE_HIGH", "warning",
                    "History grows by " + Math.round(history.versionsPerMinute())
                            + " versions per minute (above 20,000). Review ingest policies or windows."));
        }
        for (HistoryTier tier : HistoryTier.values()) {
            double headroom = partitions.headroom(tier);
            if (!Double.isNaN(headroom) && headroom < partitions.block(tier)) {
                out.add(new Warning("PARTITION_HEADROOM_LOW", "critical",
                        "The " + tier.name() + " tier has less than one partition block attached ahead of the"
                                + " allocator; check partition maintenance."));
            }
        }
        List<RetentionRun.Lag> lags = retention.lastLags();
        for (RetentionRun.Lag lag : lags) {
            out.add(new Warning("RETENTION_CONSUMER_LAG", "warning",
                    "Retention consumer " + lag.consumer() + " (cursor " + lag.cursorSeq()
                            + ") is older than twice the retention window; its clamp was released."));
        }
        return out;
    }

    /** The host of {@code url}, without IPv6 brackets, or null. */
    public static String host(String url) {
        if (url == null) {
            return null;
        }
        try {
            String host = URI.create(url).getHost();
            if (host != null && host.startsWith("[") && host.endsWith("]")) {
                host = host.substring(1, host.length() - 1);
            }
            return host;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static boolean isAllInterfaces(String bind) {
        return "0.0.0.0".equals(bind) || "::".equals(bind) || "[::]".equals(bind);
    }

    /** True for a local literal address or name; host names are never resolved. */
    public static boolean isLocal(String host) {
        if (host == null || host.isBlank()) {
            return true;
        }
        String h = host.strip().toLowerCase(Locale.ROOT);
        if (h.startsWith("[") && h.endsWith("]")) {
            h = h.substring(1, h.length() - 1);
        }
        if (h.equals("localhost") || h.endsWith(".localhost") || h.endsWith(".local")) {
            return true;
        }
        if (!IPV4.matcher(h).matches() && !h.contains(":")) {
            return false;
        }
        try {
            // A literal never triggers a DNS lookup.
            InetAddress address = InetAddress.getByName(h);
            if (address.isAnyLocalAddress()) {
                return true;
            }
            return LocalAddresses.isPrivate(address) || LocalAddresses.isCarrierGradeNat(address);
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
