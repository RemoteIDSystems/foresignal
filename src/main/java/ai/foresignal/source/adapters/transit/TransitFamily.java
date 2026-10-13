package ai.foresignal.source.adapters.transit;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers the {@code transit-*} sources (prompt 070).
 *
 * <ul>
 *   <li><strong>Reviewed feeds</strong> ({@link TransitFeeds#REVIEWED}): {@code transit-{id}} for every feed with a
 *       VehiclePositions URL and {@code transit-alerts-{id}} for every feed with an alerts URL.
 *   <li><strong>Catalog feeds:</strong> the bootstrap property {@value #CATALOG_FEEDS_PROPERTY} (comma-separated
 *       Mobility Database ids, default empty) enables rows of the vendored snapshot ({@link MobilityCatalog}):
 *       {@code transit-mdb-{id}} for a row serving vehicle positions ({@code vp}) and {@code transit-alerts-mdb-{id}}
 *       for one serving service alerts ({@code sa}), with only the vendored URL, {@code timeBase} HEADER (the feed is
 *       unknown), and the license from {@code urls.license}, else "see provider terms". Trip-update-only rows, ids
 *       missing from the snapshot and URLs that fail the outbound URL policy ({@link OutboundUrlPolicy.Profile#PUBLIC_ONLY},
 *       checked here at startup; a host that does not resolve at that moment is accepted, as a DNS outage must not
 *       disable a feed) are skipped and logged.
 * </ul>
 *
 * <p><strong>A restart is required to enable a catalog feed</strong>, by design: URLs are never accepted at runtime,
 * so the set of transit URLs is fixed when Foresignal starts and every one of them came from code or the reviewed
 * snapshot, never from an API caller.
 *
 * <p>A catalog id becomes a source-id part by lower-casing it and replacing every other character with a hyphen; one
 * that would make a source id longer than 64 characters is shortened and suffixed with 8 hex digits of its SHA-256.
 */
@Component
public class TransitFamily implements SourceAdapterFamily {

    private static final Logger log = LoggerFactory.getLogger(TransitFamily.class);

    /** The bootstrap property listing the enabled Mobility Database ids. */
    public static final String CATALOG_FEEDS_PROPERTY = "foresignal.transit.catalog-feeds";

    /** Test-only override moving every transit URL to one base (scheme and authority), keeping the path. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.transit.base-url";

    /** The license line of a catalog feed whose license the catalog does not know. */
    public static final String UNKNOWN_LICENSE = "see provider terms";

    /** The longest source id (DescriptorValidator). */
    static final int MAX_SOURCE_ID = 64;

    private final List<SourceAdapter> adapters;
    private final Map<String, List<String>> enabledCatalog;

    /**
     * Creates the family.
     *
     * @param catalogFeeds the value of {@value #CATALOG_FEEDS_PROPERTY}
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank in production
     * @param catalog the vendored snapshot
     * @param policy the outbound URL policy every catalog URL must pass
     * @param install supplies the install id for Entur's {@code ET-Client-Name}, resolved at fetch time
     * @param meters the registry of the untimed counter, resolved lazily
     * @throws IllegalStateException when the reviewed list is invalid
     */
    public TransitFamily(@Value("${" + CATALOG_FEEDS_PROPERTY + ":}") String catalogFeeds,
            @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride, MobilityCatalog catalog,
            OutboundUrlPolicy policy, ObjectProvider<InstallIdService> install, ObjectProvider<MeterRegistry> meters) {
        this(catalogFeeds, baseUrlOverride, catalog, policy,
                install == null ? () -> null : () -> {
                    InstallIdService service = install.getIfAvailable();
                    return service == null ? null : service.installId();
                },
                meters == null ? () -> null : meters::getIfAvailable);
    }

    /**
     * Creates the family with plain suppliers (tests).
     *
     * @param installId supplies the install id
     * @param meters supplies the meter registry, or null
     */
    public TransitFamily(String catalogFeeds, String baseUrlOverride, MobilityCatalog catalog, OutboundUrlPolicy policy,
            Supplier<String> installId, Supplier<MeterRegistry> meters) {
        List<String> problems = TransitFeeds.problems(TransitFeeds.REVIEWED);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Foresignal cannot start: the reviewed transit feed list is invalid: "
                    + problems);
        }
        String base = baseUrlOverride == null || baseUrlOverride.isBlank() ? null : baseUrlOverride.strip();
        List<SourceAdapter> built = new ArrayList<>();
        Set<String> ids = new LinkedHashSet<>();
        for (TransitFeed reviewed : TransitFeeds.REVIEWED) {
            TransitFeed feed = base == null ? reviewed : reviewed.rebased(base);
            add(built, ids, feed, installId, meters);
        }
        Map<String, List<String>> enabled = new LinkedHashMap<>();
        for (String id : parseIds(catalogFeeds)) {
            TransitFeed feed = catalogFeed(id, catalog, policy);
            if (feed == null) {
                continue;
            }
            List<String> sourceIds = add(built, ids, base == null ? feed : feed.rebased(base), installId, meters);
            if (!sourceIds.isEmpty()) {
                enabled.put(id, sourceIds);
            }
        }
        this.adapters = List.copyOf(built);
        this.enabledCatalog = Collections.unmodifiableMap(enabled);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }

    /** Catalog id → the source ids registered for it, for every catalog feed that was enabled. */
    public Map<String, List<String>> enabledCatalogFeeds() {
        return enabledCatalog;
    }

    /** The source-id part of a catalog id (see the class note). */
    public static String slug(String catalogId) {
        String lower = catalogId.strip().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        int room = MAX_SOURCE_ID - (TransitFeed.ALERTS_PREFIX + TransitFeed.CATALOG_INFIX).length();
        if (lower.length() <= room) {
            return lower;
        }
        String hash = sha256Hex(catalogId).substring(0, 8);
        String head = lower.substring(0, room - hash.length() - 1).replaceAll("-+$", "");
        return head + "-" + hash;
    }

    /** The distinct non-blank ids of a comma-separated list. */
    static List<String> parseIds(String value) {
        Set<String> out = new LinkedHashSet<>();
        if (value != null) {
            for (String part : value.split(",")) {
                if (!part.isBlank()) {
                    out.add(part.strip());
                }
            }
        }
        return List.copyOf(out);
    }

    /** The catalog row {@code id} as a feed, or null (logged) when it cannot be enabled. */
    static TransitFeed catalogFeed(String id, MobilityCatalog catalog, OutboundUrlPolicy policy) {
        MobilityCatalog.Entry entry = catalog.get(id).orElse(null);
        if (entry == null) {
            log.warn("{}: {} is not in the vendored Mobility Database snapshot; skipped", CATALOG_FEEDS_PROPERTY, id);
            return null;
        }
        boolean vp = entry.entityTypes().contains("vp");
        boolean sa = entry.entityTypes().contains("sa");
        if (!vp && !sa) {
            log.warn("{}: {} serves {}; only vehicle positions (vp) and service alerts (sa) are ingested; skipped",
                    CATALOG_FEEDS_PROPERTY, id, entry.entityType().isEmpty() ? "no declared entity type"
                            : entry.entityType());
            return null;
        }
        String slug = slug(id);
        if (!TransitFeeds.ID_PATTERN.matcher(slug).matches()) {
            log.warn("{}: {} cannot be turned into a source id; skipped", CATALOG_FEEDS_PROPERTY, id);
            return null;
        }
        URI url = entry.directDownloadUrl();
        if (policy != null) {
            OutboundUrlPolicy.Result result = policy.check(url, OutboundUrlPolicy.Profile.PUBLIC_ONLY);
            if (result instanceof OutboundUrlPolicy.Denied denied && !denied.unresolved()) {
                log.warn("{}: {} is skipped: its URL fails the outbound URL policy ({})", CATALOG_FEEDS_PROPERTY, id,
                        denied.reason());
                return null;
            }
        }
        String region = entry.countryCode() == null ? "country unknown" : entry.countryCode();
        return new TransitFeed(slug, entry.provider(), entry.provider(), region, vp ? url : null, sa ? url : null,
                Map.of(), entry.licenseUrl() == null ? UNKNOWN_LICENSE : entry.licenseUrl(), entry.licenseUrl(),
                entry.provider() + " (feed listed in the Mobility Database, " + id + ")", TimeBase.HEADER,
                TransitModes.UNKNOWN, null, Egress.OPEN, true);
    }

    private static List<String> add(List<SourceAdapter> built, Set<String> ids, TransitFeed feed,
            Supplier<String> installId, Supplier<MeterRegistry> meters) {
        List<String> added = new ArrayList<>();
        if (feed.vehiclePositionsUrl() != null) {
            if (ids.add(feed.vehicleSourceId())) {
                built.add(new TransitVehicleAdapter(feed, installId, meters));
                added.add(feed.vehicleSourceId());
            } else {
                log.warn("Transit source {} is already registered; the duplicate is skipped", feed.vehicleSourceId());
            }
        }
        if (feed.alertsUrl() != null) {
            if (ids.add(feed.alertsSourceId())) {
                built.add(new TransitAlertsAdapter(feed, installId));
                added.add(feed.alertsSourceId());
            } else {
                log.warn("Transit source {} is already registered; the duplicate is skipped", feed.alertsSourceId());
            }
        }
        return added;
    }

    private static String sha256Hex(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
