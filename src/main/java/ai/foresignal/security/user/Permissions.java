package ai.foresignal.security.user;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * The fixed permission catalog (OpenAPI {@code Permission}, prompt R2-001).
 *
 * <p>This enum is the single Java definition of the catalog. {@code V14__identity_model.sql} seeds the same keys,
 * groups and descriptions into {@code permission}, and {@code PermissionCatalogMigrationIT} fails when the two
 * disagree. Nothing creates a permission at runtime; a custom role is a subset of this catalog.
 */
public enum Permissions {
    USERS_MANAGE("users.manage", PermissionGroup.IDENTITY, "Create, edit, suspend and delete user accounts"),
    ROLES_MANAGE("roles.manage", PermissionGroup.IDENTITY, "Create, edit and delete custom roles"),

    SYSTEM_SETTINGS("system.settings", PermissionGroup.PLATFORM, "Change instance-wide server settings"),
    COLLECTION_MANAGE("collection.manage", PermissionGroup.PLATFORM, "Choose the sources the instance collects"),
    COLLECTOR_SETTINGS("collector.settings", PermissionGroup.PLATFORM, "Tune the collector's pacing and budgets"),
    LLM_SHARED_MANAGE("llm.shared.manage", PermissionGroup.PLATFORM, "Manage the shared LLM providers and budgets"),
    CHANNELS_GLOBAL("channels.global", PermissionGroup.PLATFORM, "Manage the instance-wide notification channels"),
    STORAGE_MANAGE("storage.manage", PermissionGroup.PLATFORM, "Manage disk, retention, archival and backup"),
    NETWORK_MANAGE("network.manage", PermissionGroup.PLATFORM, "Manage egress routing and first-party aggregators"),
    AUDIT_READ("audit.read", PermissionGroup.PLATFORM, "Read the audit log"),

    SELF_SOURCES("self.sources", PermissionGroup.SELF, "Choose one's own enabled sources over the shared collection"),
    SELF_ALERTS("self.alerts", PermissionGroup.SELF, "Own alert rules"),
    SELF_CHANNELS("self.channels", PermissionGroup.SELF, "Own notification channels"),
    SELF_LLM("self.llm", PermissionGroup.SELF, "Own personal LLM providers"),
    SELF_VIEWS("self.views", PermissionGroup.SELF, "Own saved views"),
    SELF_PREFERENCES("self.preferences", PermissionGroup.SELF, "Change one's own preferences"),
    SELF_WORKSPACES("self.workspaces", PermissionGroup.SELF, "Own workspaces"),
    SELF_EDITIONS("self.editions", PermissionGroup.SELF, "Own newspaper editions"),

    DATA_READ("data.read", PermissionGroup.DATA, "Read collected records"),
    DATA_EXPORT("data.export", PermissionGroup.DATA, "Export collected records"),
    INTEL_READ("intel.read", PermissionGroup.DATA, "Read intelligence events and analyses"),
    FORECAST_READ("forecast.read", PermissionGroup.DATA, "Read forecasts and questions");

    private final String key;
    private final PermissionGroup group;
    private final String description;

    Permissions(String key, PermissionGroup group, String description) {
        this.key = key;
        this.group = group;
        this.description = description;
    }

    /** The permission key, for example {@code users.manage}; the primary key of {@code permission}. */
    public String key() {
        return key;
    }

    /** The catalog group. */
    public PermissionGroup group() {
        return group;
    }

    /** The US English description seeded with the key. */
    public String description() {
        return description;
    }

    /**
     * Every permission in one group.
     *
     * @param group the group
     * @return the group's permissions, in catalog order
     */
    public static Set<Permissions> inGroup(PermissionGroup group) {
        EnumSet<Permissions> out = EnumSet.noneOf(Permissions.class);
        for (Permissions permission : values()) {
            if (permission.group == group) {
                out.add(permission);
            }
        }
        return out;
    }

    /**
     * The permission with a key.
     *
     * @param key a permission key
     * @return the permission, or empty when the catalog has no such key
     */
    public static Optional<Permissions> byKey(String key) {
        for (Permissions permission : values()) {
            if (permission.key.equals(key)) {
                return Optional.of(permission);
            }
        }
        return Optional.empty();
    }
}
