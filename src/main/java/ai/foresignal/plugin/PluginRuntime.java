package ai.foresignal.plugin;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.inbound.format.GeoJsonIngestMapper;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The gate and cache in front of every plugin call (prompt 141).
 *
 * <p>A call to {@link #parse} runs only when, in this order: the {@code plugins} setting is on
 * ({@link PluginException.Reason#DISABLED}); a plugin directory of that name exists
 * ({@link PluginException.Reason#UNKNOWN}); the admin enabled it ({@link PluginException.Reason#NOT_ENABLED}); its
 * manifest is valid ({@link PluginException.Reason#INVALID_MANIFEST}); and the module bytes read <em>for this call</em>
 * hash to both the declared and the approved SHA-256 ({@link PluginException.Reason#HASH_MISMATCH}). The bytes that
 * were hashed are the bytes that run, so a file swapped after review never runs.
 *
 * <p>Loaded modules are cached by name and SHA-256. At most {@value #MAX_CONCURRENT_CALLS} calls run at once (each may hold up to
 * 64 MB of plugin memory); a caller waits at most {@value #SLOT_WAIT_SECONDS} s for a slot.
 */
@Component
public class PluginRuntime implements PluginParsers {

    /** The most plugin calls running at once. */
    public static final int MAX_CONCURRENT_CALLS = 2;

    /** How long a call waits for a free slot. */
    public static final int SLOT_WAIT_SECONDS = 30;

    private final PluginCatalog catalog;
    private final Supplier<PluginSettings> settings;
    private final Supplier<PluginApprovals> approvals;
    private final PluginLimits limits;
    private final Clock clock;
    private final PluginOutputReader reader;
    private final PluginLog.Sink sink;
    private final Semaphore slots = new Semaphore(MAX_CONCURRENT_CALLS, true);

    /** {@code name:sha256} to the loaded host. */
    private final Map<String, WasmParserHost> hosts = new ConcurrentHashMap<>();
    /** Plugin name to when its current module was loaded to run. */
    private final Map<String, Loaded> loaded = new ConcurrentHashMap<>();

    /**
     * Creates the runtime over {@code ${FORESIGNAL_DATA_DIR}/plugins/} and the stored settings.
     *
     * @param properties supplies the data directory
     * @param appSettings the settings store
     * @param mapper the native ingest format's record mapper
     * @param json the JSON mapper
     * @param clock the clock
     */
    @Autowired
    public PluginRuntime(ForesignalProperties properties, AppSettingService appSettings, GeoJsonIngestMapper mapper,
            JsonMapper json, Clock clock) {
        this(new PluginCatalog(properties.dataDir().resolve(PluginCatalog.FOLDER)),
                () -> appSettings.getOrDefault(SettingKeys.PLUGINS, PluginSettings.class, () -> PluginSettings.DEFAULTS),
                () -> appSettings.getOrDefault(SettingKeys.PLUGINS_APPROVALS, PluginApprovals.class,
                        () -> PluginApprovals.NONE),
                PluginLimits.DEFAULTS, clock, new PluginOutputReader(mapper, json), null);
    }

    /**
     * Creates the runtime from parts (tests).
     *
     * @param catalog the plugin directories
     * @param settings supplies the {@code plugins} setting
     * @param approvals supplies the approvals
     * @param limits the sandbox limits
     * @param clock the clock
     * @param reader reads plugin output
     * @param sink where plugin log messages go, or null for the per-plugin logger
     */
    public PluginRuntime(PluginCatalog catalog, Supplier<PluginSettings> settings, Supplier<PluginApprovals> approvals,
            PluginLimits limits, Clock clock, PluginOutputReader reader, PluginLog.Sink sink) {
        this.catalog = Objects.requireNonNull(catalog);
        this.settings = Objects.requireNonNull(settings);
        this.approvals = Objects.requireNonNull(approvals);
        this.limits = Objects.requireNonNull(limits);
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.reader = Objects.requireNonNull(reader);
        this.sink = sink;
    }

    /** The plugin directories. */
    public PluginCatalog catalog() {
        return catalog;
    }

    /** The sandbox limits. */
    public PluginLimits limits() {
        return limits;
    }

    /** True when the {@code plugins} setting is on. */
    public boolean enabled() {
        PluginSettings s = settings.get();
        return s != null && s.on();
    }

    /** The stored approvals. */
    public PluginApprovals approvals() {
        PluginApprovals a = approvals.get();
        return a == null ? PluginApprovals.NONE : a;
    }

    /** When {@code name}'s current module was last loaded to run, or empty. */
    public Optional<Instant> loadedAt(String name) {
        return Optional.ofNullable(loaded.get(name)).map(Loaded::at);
    }

    @Override
    public ParserOutput parse(String plugin, byte[] input, SourceDescriptor descriptor) {
        WasmParserHost host = host(plugin);
        boolean acquired;
        try {
            acquired = slots.tryAcquire(SLOT_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PluginException(plugin, PluginException.Reason.BUSY, "interrupted while waiting for a slot", e);
        }
        if (!acquired) {
            throw new PluginException(plugin, PluginException.Reason.BUSY,
                    "every plugin slot stayed busy for " + SLOT_WAIT_SECONDS + " s");
        }
        try {
            return reader.read(plugin, host.parse(input), descriptor, limits);
        } finally {
            slots.release();
        }
    }

    @Override
    public void requireAvailable(String plugin) {
        host(plugin);
    }

    /**
     * The host that runs {@code name} now, after every gate in the class note.
     *
     * @param name the plugin name
     * @throws PluginException when a gate refuses
     */
    public WasmParserHost host(String name) {
        if (!enabled()) {
            throw new PluginException(name, PluginException.Reason.DISABLED,
                    "plugins are disabled (setting plugins.enabled is false)");
        }
        DiscoveredPlugin plugin = catalog.find(name).orElseThrow(() -> new PluginException(name,
                PluginException.Reason.UNKNOWN, "no such plugin under " + catalog.root()));
        PluginApprovals.Approval approval = approvals().find(name).orElseThrow(() -> new PluginException(name,
                PluginException.Reason.NOT_ENABLED, "the plugin is not enabled; review its SHA-256 and enable it with"
                        + " PUT /v1/plugins/" + name));
        if (plugin.manifest() == null) {
            throw new PluginException(name, PluginException.Reason.INVALID_MANIFEST,
                    plugin.error() == null ? "plugin.json is invalid" : plugin.error());
        }
        byte[] bytes = catalog.readModule(plugin);
        String sha = PluginCatalog.sha256(bytes);
        if (!sha.equals(plugin.manifest().sha256())) {
            throw new PluginException(name, PluginException.Reason.HASH_MISMATCH, "plugin.wasm hashes to " + sha
                    + " but plugin.json declares " + plugin.manifest().sha256() + "; refusing to load");
        }
        if (!sha.equals(approval.sha256())) {
            throw new PluginException(name, PluginException.Reason.HASH_MISMATCH, "plugin.wasm hashes to " + sha
                    + " but the admin approved " + approval.sha256() + "; review the new file and enable it again");
        }
        WasmParserHost host = hosts.computeIfAbsent(name + ":" + sha, k -> WasmParserHost.load(name, bytes, limits, clock, sink));
        Loaded previous = loaded.get(name);
        if (previous == null || !previous.sha256().equals(sha)) {
            loaded.put(name, new Loaded(sha, clock.instant()));
        }
        return host;
    }

    /**
     * Checks that a module loads (parses, imports only what is granted, exports the ABI), without running it and
     * without the setting and approval gates. Used when the admin enables a plugin and to report its state.
     *
     * @param plugin the plugin
     * @return null when the module loads, else why not
     */
    public String inspect(DiscoveredPlugin plugin) {
        if (plugin.actualSha256() == null) {
            return plugin.error() == null ? "plugin.wasm is missing" : plugin.error();
        }
        if (hosts.containsKey(plugin.name() + ":" + plugin.actualSha256())) {
            return null;
        }
        try {
            byte[] bytes = catalog.readModule(plugin);
            WasmParserHost host = WasmParserHost.load(plugin.name(), bytes, limits, clock, sink);
            hosts.putIfAbsent(plugin.name() + ":" + PluginCatalog.sha256(bytes), host);
            return null;
        } catch (PluginException e) {
            return e.getMessage();
        }
    }

    /** Forgets {@code name}'s load time (after its approval changed). */
    public void invalidate(String name) {
        loaded.remove(name);
    }

    /** Forgets every load time when the plugin settings change. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.PLUGINS.equals(event.key()) || SettingKeys.PLUGINS_APPROVALS.equals(event.key())) {
            loaded.clear();
        }
    }

    private record Loaded(String sha256, Instant at) {}
}
