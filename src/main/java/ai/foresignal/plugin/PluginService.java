package ai.foresignal.plugin;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.plugin.feed.WasmFeedSpecs;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The plugin operations (prompt 141, OpenAPI tag {@code Plugins}): list, inspect, switch the subsystem on or off, and
 * enable or disable one plugin after reviewing its SHA-256.
 *
 * <p><strong>State</strong> ({@link PluginState}), in order: {@code error} when the manifest or module is invalid;
 * {@code hash-mismatch} when the file does not hash to its declared SHA-256, or to the approved one of an enabled
 * plugin; {@code enabled} when the admin enabled it and plugins are on; otherwise {@code disabled}.
 *
 * <p><strong>Enabling</strong> needs plugins on ({@code 409 PLUGINS_DISABLED}), a {@code sha256} in the body
 * ({@code 400}), a file that hashes to it ({@code 409 PLUGIN_HASH_MISMATCH}) and to the declared hash
 * ({@code 409 PLUGIN_HASH_MISMATCH}), and a module that loads ({@code 400}). Disabling always works.
 */
@Service
public class PluginService {

    private static final Logger log = LoggerFactory.getLogger(PluginService.class);

    private final PluginRuntime runtime;
    private final AppSettingService settings;
    private final WasmFeedSpecs feeds;
    private final Clock clock;

    /**
     * Creates the service.
     *
     * @param runtime the plugin gate and catalog
     * @param settings the settings store
     * @param feeds the {@code wasm-feed} specs, for each plugin's source ids
     * @param clock the clock
     */
    public PluginService(PluginRuntime runtime, AppSettingService settings, WasmFeedSpecs feeds, Clock clock) {
        this.runtime = runtime;
        this.settings = settings;
        this.feeds = feeds;
        this.clock = clock;
    }

    /** {@code listPlugins}: the setting and every plugin found on disk. */
    public PluginListView list() {
        boolean on = runtime.enabled();
        PluginApprovals approvals = runtime.approvals();
        List<PluginView> views = runtime.catalog().discover().stream().map(p -> view(p, on, approvals)).toList();
        return new PluginListView(on, views);
    }

    /**
     * {@code getPlugin}: one plugin.
     *
     * @throws NotFoundException ({@code UNKNOWN_PLUGIN}) when no such directory exists
     */
    public PluginView get(String name) {
        DiscoveredPlugin plugin = require(name);
        return view(plugin, runtime.enabled(), runtime.approvals());
    }

    /**
     * {@code updatePluginSettings}: switches the subsystem on or off.
     *
     * @throws ValidationException when {@code enabled} is missing
     */
    public PluginListView updateSettings(PluginSettings body) {
        if (body == null || body.enabled() == null) {
            throw ValidationException.forField("enabled", "is required");
        }
        settings.put(SettingKeys.PLUGINS, new PluginSettings(body.enabled()));
        log.info("Plugins {}", body.on() ? "enabled" : "disabled");
        return list();
    }

    /**
     * {@code updatePlugin}: enables a plugin after review, or disables it (see the class note).
     *
     * @throws NotFoundException ({@code UNKNOWN_PLUGIN}) when no such directory exists
     * @throws ValidationException when {@code enabled} or {@code sha256} is missing or malformed, or the plugin cannot
     *     load
     * @throws ConflictException ({@code PLUGINS_DISABLED}, {@code PLUGIN_HASH_MISMATCH})
     */
    public PluginView update(String name, PluginUpdate body) {
        if (body == null || body.enabled() == null) {
            throw ValidationException.forField("enabled", "is required");
        }
        DiscoveredPlugin plugin = require(name);
        PluginApprovals approvals = runtime.approvals();
        if (!body.enabled()) {
            if (approvals.find(name).isPresent()) {
                settings.put(SettingKeys.PLUGINS_APPROVALS, approvals.without(name));
                log.info("Plugin {} disabled", name);
            }
            runtime.invalidate(name);
            return view(plugin, runtime.enabled(), approvals.without(name));
        }
        if (!runtime.enabled()) {
            throw new ConflictException(ErrorCodes.PLUGINS_DISABLED,
                    "Plugins are disabled; enable them with PUT /v1/plugins before enabling '" + name + "'.");
        }
        String reviewed = body.sha256() == null ? null : body.sha256().strip();
        if (reviewed == null || reviewed.isEmpty()) {
            throw ValidationException.forField("sha256",
                    "is required to enable a plugin: review the SHA-256 of plugin.wasm and send it");
        }
        if (!PluginManifest.SHA256.matcher(reviewed).matches()) {
            throw ValidationException.forField("sha256", "must be a lower-case hex SHA-256");
        }
        if (plugin.manifest() == null || plugin.actualSha256() == null) {
            throw new ValidationException("The plugin '" + name + "' cannot be enabled: "
                    + (plugin.error() == null ? "its files are invalid" : plugin.error()));
        }
        if (!reviewed.equals(plugin.actualSha256())) {
            throw new ConflictException(ErrorCodes.PLUGIN_HASH_MISMATCH, "The reviewed SHA-256 " + reviewed
                    + " does not match plugin.wasm, which hashes to " + plugin.actualSha256() + ".");
        }
        if (!plugin.hashMatches()) {
            throw new ConflictException(ErrorCodes.PLUGIN_HASH_MISMATCH, "plugin.json declares "
                    + plugin.manifest().sha256() + " but plugin.wasm hashes to " + plugin.actualSha256() + ".");
        }
        String loadProblem = runtime.inspect(plugin);
        if (loadProblem != null) {
            throw new ValidationException("The plugin '" + name + "' cannot be enabled: " + loadProblem);
        }
        PluginApprovals updated = approvals.with(name, reviewed, clock.instant());
        settings.put(SettingKeys.PLUGINS_APPROVALS, updated);
        runtime.invalidate(name);
        log.info("Plugin {} {} enabled at sha256 {}", name, plugin.manifest().version(), reviewed);
        return view(plugin, true, updated);
    }

    private DiscoveredPlugin require(String name) {
        return runtime.catalog().find(name).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_PLUGIN,
                "There is no plugin '" + name + "' under " + PluginCatalog.FOLDER + "/."));
    }

    private PluginView view(DiscoveredPlugin plugin, boolean on, PluginApprovals approvals) {
        PluginManifest manifest = plugin.manifest();
        PluginApprovals.Approval approval = approvals.find(plugin.name()).orElse(null);
        String error = plugin.error();
        if (error == null) {
            error = runtime.inspect(plugin);
        }
        PluginState state;
        if (error != null) {
            state = PluginState.ERROR;
        } else if (!plugin.hashMatches()) {
            state = PluginState.HASH_MISMATCH;
            error = "plugin.wasm hashes to " + plugin.actualSha256() + " but plugin.json declares "
                    + manifest.sha256();
        } else if (approval != null && !approval.sha256().equals(plugin.actualSha256())) {
            state = PluginState.HASH_MISMATCH;
            error = "plugin.wasm hashes to " + plugin.actualSha256() + " but the admin approved " + approval.sha256()
                    + "; review the new file and enable it again";
        } else if (approval != null && on) {
            state = PluginState.ENABLED;
        } else {
            state = PluginState.DISABLED;
        }
        String declared = manifest != null ? manifest.sha256()
                : plugin.actualSha256() != null ? plugin.actualSha256() : "";
        return new PluginView(plugin.name(), manifest == null ? "" : manifest.version(), PluginManifest.ABI,
                PluginManifest.KIND_PARSER, declared, plugin.actualSha256(), plugin.hashMatches(), approval != null,
                state, error, feeds.sourceIds(plugin.name()), runtime.loadedAt(plugin.name()).orElse(null));
    }
}
