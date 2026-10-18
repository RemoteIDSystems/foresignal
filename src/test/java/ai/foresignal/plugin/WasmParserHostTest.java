package ai.foresignal.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.inbound.format.GeoJsonIngestMapper;
import ai.foresignal.inbound.format.ItemRejection;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.MutableClock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.json.JsonMapper;

/**
 * The WASM parser sandbox (prompt 141, {@code docs/Plugin-Design.md}): the example plugin parses a fixture; the
 * memory cap, the wall-clock timeout, the output and input caps are enforced; no WASI import is available; the log
 * import is rate-limited; a SHA-256 mismatch refuses to load; and plugins are disabled by default.
 *
 * <p>The modules are the committed test artifacts under {@code src/test/resources/plugins/} ({@link PluginTestFiles}).
 */
class WasmParserHostTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final PluginOutputReader READER = new PluginOutputReader(new GeoJsonIngestMapper(JSON), JSON);
    static final SourceDescriptor STATIONS = SourceDescriptor.builder("wasm-test-stations")
            .name("Test stations")
            .kind("station")
            .build();

    static WasmParserHost host(String plugin) {
        return WasmParserHost.load(plugin, PluginTestFiles.wasm(plugin), PluginLimits.DEFAULTS);
    }

    static String text(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Nested
    @DisplayName("the example plugin (geojson-passthrough)")
    class Example {

        @Test
        @DisplayName("parses the fixture into the ABI document, byte for byte")
        void parsesFixture() {
            WasmParserHost host = host(PluginTestFiles.PASSTHROUGH);

            byte[] output = host.parse(PluginTestFiles.fixture());

            assertThat(text(output)).isEqualTo(text(PluginTestFiles.bytes(PluginTestFiles.PASSTHROUGH,
                    "expected-output.json")));
            assertThat(host.importsLog()).as("it imports only env.foresignal_log").isTrue();
            assertThat(host.name()).isEqualTo(PluginTestFiles.PASSTHROUGH);
        }

        @Test
        @DisplayName("its output maps to records through the native record mapper: ids, geometry, payload, one rejection")
        void outputMapsToRecords() {
            ParserOutput output = READER.read(PluginTestFiles.PASSTHROUGH,
                    host(PluginTestFiles.PASSTHROUGH).parse(PluginTestFiles.fixture()), STATIONS, PluginLimits.DEFAULTS);

            assertThat(output.complete()).isTrue();
            assertThat(output.records()).extracting(NormalizedRecord::externalId).containsExactly("st-1", "42", "st-4");
            assertThat(output.records()).extracting(NormalizedRecord::kind).containsOnly("station");
            NormalizedRecord first = output.records().get(0);
            assertThat(first.geometry()).isInstanceOf(Point.class);
            assertThat(first.geometry().getCoordinate().x).isEqualTo(-75.6972);
            assertThat(first.geometry().getCoordinate().y).isEqualTo(45.4215);
            assertThat(first.payload()).containsEntry("name", "Ottawa \"Parliament\" {gauge}")
                    .containsEntry("observedAt", "2026-10-18T06:00:00Z");
            assertThat(output.records().get(1).payload()).containsEntry("tags", List.of("coast", "lake"));
            NormalizedRecord last = output.records().get(2);
            assertThat(last.geometry()).isNull();
            assertThat(last.payload()).isEmpty();
            assertThat(output.rejections()).singleElement().satisfies(r -> {
                assertThat(r.index()).isEqualTo(2);
                assertThat(r.reason()).isEqualTo("EXTERNAL_ID_INVALID");
            });
        }

        @Test
        @DisplayName("a collection without features, or an empty one, gives no records and complete=true")
        void emptyCollections() {
            WasmParserHost host = host(PluginTestFiles.PASSTHROUGH);

            assertThat(text(host.parse("{\"type\":\"FeatureCollection\",\"features\":[]}".getBytes())))
                    .isEqualTo("{\"records\":[],\"complete\":true}");
            assertThat(text(host.parse(" { } ".getBytes()))).isEqualTo("{\"records\":[],\"complete\":true}");
        }

        @Test
        @DisplayName("each call starts from a fresh instance: repeated calls give the same output")
        void freshInstancePerCall() {
            WasmParserHost host = host(PluginTestFiles.PASSTHROUGH);
            byte[] first = host.parse(PluginTestFiles.fixture());
            byte[] second = host.parse(PluginTestFiles.fixture());
            assertThat(second).isEqualTo(first);
        }

        @Test
        @DisplayName("malformed input: the plugin logs a warning through the host import and returns no output")
        void malformedInput() {
            List<String> lines = new ArrayList<>();
            WasmParserHost host = WasmParserHost.load(PluginTestFiles.PASSTHROUGH,
                    PluginTestFiles.wasm(PluginTestFiles.PASSTHROUGH), PluginLimits.DEFAULTS, new MutableClock(),
                    (level, message) -> lines.add(level + ":" + message));

            assertThatThrownBy(() -> host.parse("{\"features\":[".getBytes()))
                    .isInstanceOfSatisfying(PluginException.class,
                            e -> assertThat(e.reason()).isEqualTo(PluginException.Reason.NO_OUTPUT));
            assertThat(lines).containsExactly("2:malformed GeoJSON input");
        }
    }

    @Nested
    @DisplayName("limits")
    class Limits {

        @Test
        @DisplayName("the ABI v1 defaults: 1024 pages (64 MB), 5 s, 16 MB in and out")
        void defaults() {
            assertThat(PluginLimits.DEFAULTS.maxMemoryPages()).isEqualTo(1024);
            assertThat(PluginLimits.DEFAULTS.maxMemoryBytes()).isEqualTo(64L * 1024 * 1024);
            assertThat(PluginLimits.DEFAULTS.timeout()).isEqualTo(Duration.ofSeconds(5));
            assertThat(PluginLimits.DEFAULTS.maxOutputBytes()).isEqualTo(16 * 1024 * 1024);
            assertThat(PluginLimits.DEFAULTS.maxInputBytes()).isEqualTo(16 * 1024 * 1024);
            assertThatThrownBy(() -> new PluginLimits(0, Duration.ofSeconds(1), 1, 1, 1, 0, 0, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new PluginLimits(1, Duration.ZERO, 1, 1, 1, 0, 0, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("memory cap: growth past 1024 pages fails, so the hog traps; with a 4096-page cap it would succeed")
        void memoryCap() {
            assertThatThrownBy(() -> host(PluginTestFiles.MEMORY_HOG).parse(new byte[] {1}))
                    .isInstanceOfSatisfying(PluginException.class, e -> {
                        assertThat(e.reason()).isEqualTo(PluginException.Reason.TRAP);
                        assertThat(e.plugin()).isEqualTo(PluginTestFiles.MEMORY_HOG);
                    });

            PluginLimits roomy = new PluginLimits(4096, Duration.ofSeconds(30), 1024, 1024, 10, 0, 0, 16);
            WasmParserHost unconstrained = WasmParserHost.load(PluginTestFiles.MEMORY_HOG,
                    PluginTestFiles.wasm(PluginTestFiles.MEMORY_HOG), roomy);
            assertThat(text(unconstrained.parse(new byte[] {1}))).isEqualTo("{\"records\":[],\"complete\":true}");
        }

        @Test
        @DisplayName("memory cap at load: a module asking for more initial pages than the cap is refused")
        void initialMemoryOverCap() {
            PluginLimits tiny = new PluginLimits(100, Duration.ofSeconds(5), 1024, 1024, 10, 0, 0, 16);

            assertThatThrownBy(() -> WasmParserHost.load(PluginTestFiles.OVERSIZED_OUTPUT,
                    PluginTestFiles.wasm(PluginTestFiles.OVERSIZED_OUTPUT), tiny))
                    .isInstanceOfSatisfying(PluginException.class, e -> {
                        assertThat(e.reason()).isEqualTo(PluginException.Reason.INVALID_MODULE);
                        assertThat(e.getMessage()).contains("258 initial memory pages").contains("100-page");
                    });
        }

        @Test
        @DisplayName("timeout: an infinite loop is interrupted after the wall-clock budget")
        void timeout() {
            PluginLimits quick = PluginLimits.DEFAULTS.withTimeout(Duration.ofMillis(300));
            WasmParserHost host = WasmParserHost.load(PluginTestFiles.INFINITE_LOOP,
                    PluginTestFiles.wasm(PluginTestFiles.INFINITE_LOOP), quick);
            long started = System.nanoTime();

            assertThatThrownBy(() -> host.parse(new byte[] {1}))
                    .isInstanceOfSatisfying(PluginException.class, e -> {
                        assertThat(e.reason()).isEqualTo(PluginException.Reason.TIMEOUT);
                        assertThat(e.getMessage()).contains("300 ms");
                    });
            long elapsedMillis = (System.nanoTime() - started) / 1_000_000;
            assertThat(elapsedMillis).isGreaterThanOrEqualTo(300).isLessThan(300 + WasmParserHost.STOP_GRACE_MILLIS + 1000);
            assertThat(host.importsLog()).isFalse();
        }

        @Test
        @DisplayName("output cap: a length prefix over 16 MB is refused before anything is read")
        void outputCap() {
            assertThatThrownBy(() -> host(PluginTestFiles.OVERSIZED_OUTPUT).parse(new byte[] {1}))
                    .isInstanceOfSatisfying(PluginException.class, e -> {
                        assertThat(e.reason()).isEqualTo(PluginException.Reason.OUTPUT_TOO_LARGE);
                        assertThat(e.getMessage()).contains("16777217").contains("16777216");
                    });
        }

        @Test
        @DisplayName("input cap: input over the limit is refused without running the plugin")
        void inputCap() {
            PluginLimits small = new PluginLimits(1024, Duration.ofSeconds(5), 8, 1024, 10, 0, 0, 16);
            WasmParserHost host = WasmParserHost.load(PluginTestFiles.PASSTHROUGH,
                    PluginTestFiles.wasm(PluginTestFiles.PASSTHROUGH), small);

            assertThatThrownBy(() -> host.parse("{\"features\":[]}".getBytes()))
                    .isInstanceOfSatisfying(PluginException.class,
                            e -> assertThat(e.reason()).isEqualTo(PluginException.Reason.INPUT_TOO_LARGE));
            assertThat(text(host.parse("{}".getBytes()))).isEqualTo("{\"records\":[],\"complete\":true}");
            assertThatThrownBy(() -> host.parse(null)).as("null input is empty input, which is malformed")
                    .isInstanceOfSatisfying(PluginException.class,
                            e -> assertThat(e.reason()).isEqualTo(PluginException.Reason.NO_OUTPUT));
        }

        @Test
        @DisplayName("the record cap applies to the mapped output")
        void recordCap() {
            PluginLimits two = new PluginLimits(1024, Duration.ofSeconds(5), 1 << 20, 1 << 20, 2, 0, 0, 16);
            byte[] output = host(PluginTestFiles.PASSTHROUGH).parse(PluginTestFiles.fixture());

            assertThatThrownBy(() -> READER.read(PluginTestFiles.PASSTHROUGH, output, STATIONS, two))
                    .isInstanceOfSatisfying(PluginException.class, e -> {
                        assertThat(e.reason()).isEqualTo(PluginException.Reason.INVALID_OUTPUT);
                        assertThat(e.getMessage()).contains("4 records").contains("2-record");
                    });
        }
    }

    @Nested
    @DisplayName("imports")
    class Imports {

        @Test
        @DisplayName("no WASI imports are available: a module importing wasi_snapshot_preview1.fd_write is refused at load")
        void noWasi() {
            assertThatThrownBy(() -> host(PluginTestFiles.WASI_IMPORT))
                    .isInstanceOfSatisfying(PluginException.class, e -> {
                        assertThat(e.reason()).isEqualTo(PluginException.Reason.INVALID_MODULE);
                        assertThat(e.getMessage()).contains("wasi_snapshot_preview1.fd_write")
                                .contains("not granted").contains("env.foresignal_log");
                    });
        }

        @Test
        @DisplayName("bytes that are not WebAssembly, and an empty file, are refused at load")
        void notWasm() {
            assertThatThrownBy(() -> WasmParserHost.load("junk", "not wasm".getBytes(), PluginLimits.DEFAULTS))
                    .isInstanceOfSatisfying(PluginException.class,
                            e -> assertThat(e.reason()).isEqualTo(PluginException.Reason.INVALID_MODULE));
            assertThatThrownBy(() -> WasmParserHost.load("empty", new byte[0], PluginLimits.DEFAULTS))
                    .isInstanceOfSatisfying(PluginException.class,
                            e -> assertThat(e.getMessage()).contains("empty"));
        }

        @Test
        @DisplayName("the log import is rate-limited: 20 a call, 60 a minute, and the call still succeeds")
        void logRateLimit() {
            MutableClock clock = new MutableClock(Instant.parse("2026-10-18T06:00:05Z"));
            List<String> lines = new ArrayList<>();
            WasmParserHost host = WasmParserHost.load(PluginTestFiles.LOG_SPAMMER,
                    PluginTestFiles.wasm(PluginTestFiles.LOG_SPAMMER), PluginLimits.DEFAULTS, clock,
                    (level, message) -> lines.add(level + ":" + message));

            assertThat(text(host.parse(new byte[] {1}))).isEqualTo("{\"records\":[],\"complete\":true}");
            assertThat(lines).hasSize(20).containsOnly("1:spam");
            host.parse(new byte[] {1});
            host.parse(new byte[] {1});
            assertThat(lines).hasSize(60);
            host.parse(new byte[] {1});
            assertThat(lines).as("the minute's 60 are spent").hasSize(60);

            clock.advance(Duration.ofMinutes(1));
            host.parse(new byte[] {1});
            assertThat(lines).hasSize(80);
        }
    }

    @Nested
    @DisplayName("loading through the runtime")
    class Runtime {

        @TempDir
        Path root;

        PluginRuntime runtime(boolean enabled, PluginApprovals approvals) {
            AtomicReference<PluginApprovals> stored = new AtomicReference<>(approvals);
            return new PluginRuntime(new PluginCatalog(root), () -> new PluginSettings(enabled), stored::get,
                    PluginLimits.DEFAULTS, new MutableClock(), READER, (level, message) -> { });
        }

        PluginApprovals approved(String name, String sha) {
            return PluginApprovals.NONE.with(name, sha, Instant.parse("2026-10-18T06:00:00Z"));
        }

        @Test
        @DisplayName("disabled by default: the shipped setting is off and nothing runs while it is")
        void disabledByDefault() {
            PluginTestFiles.install(root, PluginTestFiles.PASSTHROUGH);
            assertThat(PluginSettings.DEFAULTS.on()).isFalse();
            assertThat(new PluginSettings(null).on()).isFalse();
            PluginRuntime runtime = new PluginRuntime(new PluginCatalog(root), () -> PluginSettings.DEFAULTS,
                    () -> approved(PluginTestFiles.PASSTHROUGH, PluginTestFiles.PASSTHROUGH_SHA256),
                    PluginLimits.DEFAULTS, new MutableClock(), READER, null);

            assertThat(runtime.enabled()).isFalse();
            assertThatThrownBy(() -> runtime.parse(PluginTestFiles.PASSTHROUGH, PluginTestFiles.fixture(), STATIONS))
                    .isInstanceOfSatisfying(PluginException.class, e -> {
                        assertThat(e.reason()).isEqualTo(PluginException.Reason.DISABLED);
                        assertThat(e.getMessage()).contains("plugins are disabled");
                    });
            assertThat(runtime.loadedAt(PluginTestFiles.PASSTHROUGH)).isEmpty();
        }

        @Test
        @DisplayName("plugins on but this plugin not enabled: refused; unknown plugin: refused")
        void notEnabledOrUnknown() {
            PluginTestFiles.install(root, PluginTestFiles.PASSTHROUGH);
            PluginRuntime runtime = runtime(true, PluginApprovals.NONE);

            assertThatThrownBy(() -> runtime.host(PluginTestFiles.PASSTHROUGH))
                    .isInstanceOfSatisfying(PluginException.class,
                            e -> assertThat(e.reason()).isEqualTo(PluginException.Reason.NOT_ENABLED));
            assertThatThrownBy(() -> runtime.host("no-such-plugin"))
                    .isInstanceOfSatisfying(PluginException.class,
                            e -> assertThat(e.reason()).isEqualTo(PluginException.Reason.UNKNOWN));
        }

        @Test
        @DisplayName("sha256 mismatch refuses to load: a declared hash that is not the file's")
        void declaredHashMismatch() {
            String wrong = "0".repeat(64);
            PluginTestFiles.install(root, PluginTestFiles.PASSTHROUGH, PluginTestFiles.PASSTHROUGH, wrong);
            PluginRuntime runtime = runtime(true, approved(PluginTestFiles.PASSTHROUGH, wrong));

            assertThatThrownBy(() -> runtime.parse(PluginTestFiles.PASSTHROUGH, PluginTestFiles.fixture(), STATIONS))
                    .isInstanceOfSatisfying(PluginException.class, e -> {
                        assertThat(e.reason()).isEqualTo(PluginException.Reason.HASH_MISMATCH);
                        assertThat(e.getMessage()).contains(PluginTestFiles.PASSTHROUGH_SHA256).contains("declares")
                                .contains("refusing to load");
                    });
        }

        @Test
        @DisplayName("sha256 mismatch refuses to load: a file swapped after review no longer matches the approval")
        void approvedHashMismatch() throws Exception {
            Path dir = PluginTestFiles.install(root, PluginTestFiles.PASSTHROUGH);
            PluginRuntime runtime = runtime(true, approved(PluginTestFiles.PASSTHROUGH,
                    PluginTestFiles.PASSTHROUGH_SHA256));
            assertThat(runtime.parse(PluginTestFiles.PASSTHROUGH, PluginTestFiles.fixture(), STATIONS).records())
                    .hasSize(3);
            assertThat(runtime.loadedAt(PluginTestFiles.PASSTHROUGH)).isPresent();

            byte[] other = PluginTestFiles.wasm(PluginTestFiles.LOG_SPAMMER);
            Files.write(dir.resolve(PluginCatalog.MODULE), other);
            Files.writeString(dir.resolve(PluginCatalog.MANIFEST),
                    PluginTestFiles.manifest(PluginTestFiles.PASSTHROUGH, PluginCatalog.sha256(other)));

            assertThatThrownBy(() -> runtime.parse(PluginTestFiles.PASSTHROUGH, PluginTestFiles.fixture(), STATIONS))
                    .isInstanceOfSatisfying(PluginException.class, e -> {
                        assertThat(e.reason()).isEqualTo(PluginException.Reason.HASH_MISMATCH);
                        assertThat(e.getMessage()).contains("approved " + PluginTestFiles.PASSTHROUGH_SHA256);
                    });
        }

        @Test
        @DisplayName("an invalid manifest refuses to load")
        void invalidManifest() throws Exception {
            Path dir = PluginTestFiles.install(root, PluginTestFiles.PASSTHROUGH);
            Files.writeString(dir.resolve(PluginCatalog.MANIFEST), "{\"name\":\"geojson-passthrough\",\"version\":\"1\","
                    + "\"abi\":2,\"kind\":\"parser\",\"sha256\":\"" + PluginTestFiles.PASSTHROUGH_SHA256 + "\"}");
            PluginRuntime runtime = runtime(true, approved(PluginTestFiles.PASSTHROUGH,
                    PluginTestFiles.PASSTHROUGH_SHA256));

            assertThatThrownBy(() -> runtime.host(PluginTestFiles.PASSTHROUGH))
                    .isInstanceOfSatisfying(PluginException.class, e -> {
                        assertThat(e.reason()).isEqualTo(PluginException.Reason.INVALID_MANIFEST);
                        assertThat(e.getMessage()).contains("abi 2");
                    });
        }

        @Test
        @DisplayName("enabled and matching: the runtime parses, caches the module and reports when it loaded it")
        void runs() {
            PluginTestFiles.install(root, PluginTestFiles.PASSTHROUGH);
            PluginRuntime runtime = runtime(true, approved(PluginTestFiles.PASSTHROUGH,
                    PluginTestFiles.PASSTHROUGH_SHA256));

            ParserOutput output = runtime.parse(PluginTestFiles.PASSTHROUGH, PluginTestFiles.fixture(), STATIONS);

            assertThat(output.records()).hasSize(3);
            assertThat(output.rejections()).extracting(ItemRejection::reason).containsExactly("EXTERNAL_ID_INVALID");
            assertThat(runtime.host(PluginTestFiles.PASSTHROUGH)).isSameAs(runtime.host(PluginTestFiles.PASSTHROUGH));
            Instant loaded = runtime.loadedAt(PluginTestFiles.PASSTHROUGH).orElseThrow();
            assertThat(loaded).isEqualTo(MutableClock.DEFAULT_INSTANT);
            runtime.invalidate(PluginTestFiles.PASSTHROUGH);
            assertThat(runtime.loadedAt(PluginTestFiles.PASSTHROUGH)).isEmpty();
            runtime.onSettingChanged(new ai.foresignal.settings.AppSettingChangedEvent("plugins"));
            assertThat(runtime.inspect(runtime.catalog().find(PluginTestFiles.PASSTHROUGH).orElseThrow())).isNull();
        }

        @Test
        @DisplayName("inspect reports a module that will not load without running it")
        void inspect() {
            PluginTestFiles.install(root, PluginTestFiles.WASI_IMPORT);
            PluginRuntime runtime = runtime(false, PluginApprovals.NONE);

            String problem = runtime.inspect(runtime.catalog().find(PluginTestFiles.WASI_IMPORT).orElseThrow());

            assertThat(problem).contains("wasi_snapshot_preview1.fd_write");
            assertThat(runtime.limits()).isEqualTo(PluginLimits.DEFAULTS);
        }
    }
}
