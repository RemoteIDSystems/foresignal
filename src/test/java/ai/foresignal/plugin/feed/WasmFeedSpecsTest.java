package ai.foresignal.plugin.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.plugin.PluginTestFiles;
import ai.foresignal.settings.DurationShorthandModule;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.IngestPolicyCodec;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code wasm-feed} specs (prompt 141): loading from {@code ${FORESIGNAL_DATA_DIR}/wasm-feeds/}, the checks that
 * stop startup, the outbound URL policy at load, the descriptor built from the spec, and registration through the
 * source registry's own validation.
 */
class WasmFeedSpecsTest {

    static final IngestPolicyCodec CODEC =
            new IngestPolicyCodec(JsonMapper.builder().addModule(new DurationShorthandModule()).build());

    /** Public for every name except {@code private.example} (10.0.0.5); {@code nowhere.example} does not resolve. */
    static final OutboundUrlPolicy POLICY = OutboundUrlPolicy.withoutOverrides(host -> {
        if (host.equals("nowhere.example")) {
            throw new UnknownHostException(host);
        }
        if (host.equals("private.example")) {
            return new InetAddress[] {InetAddress.getByName("10.0.0.5")};
        }
        return new InetAddress[] {InetAddress.getByName("93.184.216.34")};
    });

    @TempDir
    Path dir;

    static final String VALID = """
            [{
              "id": "wasm-stations",
              "name": "Synthetic stations",
              "plugin": "geojson-passthrough",
              "url": "https://data.example.org/stations.geojson",
              "kinds": ["station"],
              "homepage": "https://data.example.org/",
              "attribution": "Example Data",
              "license": "CC-BY-4.0",
              "termsUrl": "https://data.example.org/terms",
              "licenseTerms": {"id": "CC-BY-4.0", "name": "Creative Commons Attribution 4.0", "commercialUse": "ALLOWED",
                "shareAlike": false, "attributionRequired": true, "redistribution": "ALLOWED",
                "attributionText": "Example Data", "termsUrl": "https://creativecommons.org/licenses/by/4.0/"},
              "historyTier": "STANDARD",
              "observedAtMode": "RECEIVED",
              "recordTtl": "2h",
              "ttlAnchor": "RECEIVED",
              "ingestPolicy": {"mode": "LATEST", "keyframe": "1h"},
              "interval": "10m",
              "minInterval": "5m",
              "budget": {"perMinute": 2, "perDay": 300, "mbPerDay": 50},
              "retention": "365d",
              "titleField": "name",
              "searchFields": ["name"],
              "embedFields": ["name"],
              "tileProperties": ["name", "level"],
              "volatileFields": ["observedAt"],
              "significantFields": ["level"],
              "fieldSpecs": {"level": {"type": "NUMBER", "unit": "m", "label": "Level"}},
              "egress": "LOCAL_ONLY",
              "zeroResultsNormal": true,
              "conditional": false,
              "maxResponseBytes": 1048576
            }]
            """;

    void write(String file, String json) throws Exception {
        Files.writeString(dir.resolve(file), json, StandardCharsets.UTF_8);
    }

    WasmFeedSpecs load() {
        return new WasmFeedSpecs(dir, () -> POLICY, CODEC);
    }

    static String minimal(String id, String url) {
        return "{\"id\":\"" + id + "\",\"name\":\"N\",\"plugin\":\"geojson-passthrough\",\"url\":\"" + url
                + "\",\"kinds\":[\"station\"]}";
    }

    @Test
    @DisplayName("a missing directory holds no specs: the broker runs unchanged")
    void missingDirectory() {
        WasmFeedSpecs specs = new WasmFeedSpecs(dir.resolve("absent"), () -> POLICY, CODEC);
        assertThat(specs.sources()).isEmpty();
        assertThat(specs.sourceIds("geojson-passthrough")).isEmpty();
    }

    @Test
    @DisplayName("a full spec builds the descriptor it states, which passes the descriptor validator")
    void fullSpec() throws Exception {
        write("stations.json", VALID);

        WasmFeedSpecs specs = load();

        assertThat(specs.sources()).hasSize(1);
        assertThat(specs.sourceIds("geojson-passthrough")).containsExactly("wasm-stations");
        SourceDescriptor d = specs.sources().get(0).descriptor();
        assertThat(d.id()).isEqualTo("wasm-stations");
        assertThat(d.mode()).isEqualTo(Mode.POLLING);
        assertThat(d.kinds()).containsExactly("station");
        assertThat(d.followRedirects()).as("a redirect could leave the checked URL").isFalse();
        assertThat(d.fetchTimeout()).isEqualTo(WasmFeedDescriptors.FETCH_TIMEOUT);
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(10));
        assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(2);
        assertThat(d.defaultBudget().perDay()).isEqualTo(300);
        assertThat(d.defaultBudget().mbPerDay()).isEqualTo(50);
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.RECEIVED);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofHours(2));
        assertThat(d.defaultIngestPolicy()).isInstanceOf(IngestPolicy.Latest.class);
        assertThat(d.defaultRetention()).isEqualTo(Duration.ofDays(365));
        assertThat(d.kindRetention()).containsEntry("station", Duration.ofDays(365));
        assertThat(d.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(d.licence()).isEqualTo("CC-BY-4.0");
        assertThat(d.licenceTerms().attributionRequired()).isTrue();
        assertThat(d.titleField()).isEqualTo("name");
        assertThat(d.tileProperties()).containsExactly("name", "level");
        assertThat(d.fieldSpecs()).containsKey("level");
        assertThat(d.maxResponseBytes()).isEqualTo(1048576);
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(specs.sources().get(0).spec().wantsConditional()).isFalse();

        WasmFeedFamily family = new WasmFeedFamily(specs.sources(), (p, b, desc) -> null, UrlGate.ALLOW_ALL, null);
        assertThat(DescriptorValidator.problems(d, family.adapters().get(0))).isEmpty();
    }

    @Test
    @DisplayName("defaults: 15 min, 4/min and 400/day, 8 MB, STANDARD, RECEIVED, ALL, 1825 d, OPEN, conditional")
    void defaults() throws Exception {
        write("a.json", "[" + minimal("wasm-min", "https://data.example.org/x") + "]");

        WasmFeedSpecs.WasmFeedSource source = load().sources().get(0);
        SourceDescriptor d = source.descriptor();

        assertThat(d.defaultInterval()).isEqualTo(WasmFeedDescriptors.DEFAULT_INTERVAL);
        assertThat(d.minInterval()).isEqualTo(WasmFeedDescriptors.DEFAULT_MIN_INTERVAL);
        assertThat(d.defaultBudget().perMinute()).isEqualTo(WasmFeedDescriptors.DEFAULT_PER_MINUTE);
        assertThat(d.defaultBudget().perDay()).isEqualTo(WasmFeedDescriptors.DEFAULT_PER_DAY);
        assertThat(d.maxResponseBytes()).isEqualTo(WasmFeedDescriptors.DEFAULT_MAX_RESPONSE_BYTES);
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(d.defaultIngestPolicy()).isInstanceOf(IngestPolicy.All.class);
        assertThat(d.defaultRetention()).isEqualTo(WasmFeedDescriptors.DEFAULT_RETENTION);
        assertThat(d.egress()).isEqualTo(Egress.OPEN);
        assertThat(d.recordTtl()).isNull();
        assertThat(d.description()).contains("plugin geojson-passthrough").contains("https://data.example.org/x");
        assertThat(source.spec().wantsConditional()).isTrue();
    }

    @Test
    @DisplayName("every problem stops startup and is listed with its file, index and id")
    void problemsStopStartup() throws Exception {
        write("bad.json", """
                [
                  {"id":"stations","name":"","plugin":"Bad Name","url":"http://data.example.org/x","kinds":[]},
                  {"id":"wasm-b","name":"B","plugin":"p","url":"https://u:p@data.example.org/x","kinds":["Bad Kind"],
                   "historyTier":"HOURLY","observedAtMode":"SOMETIMES","ttlAnchor":"NEVER","egress":"ANYWHERE",
                   "recordTtl":"soon","interval":"0s","budget":{"perMinute":0,"perDay":0,"mbPerDay":0},
                   "maxResponseBytes":99999999,"ingestPolicy":{"mode":"SOMETIMES"}},
                  {"id":"wasm-c","name":"C","plugin":"p","url":"https://private.example/x","kinds":["k"]},
                  {"id":"wasm-c","name":"C again","plugin":"p","url":"https://data.example.org/y","kinds":["k"]}
                ]
                """);

        assertThatThrownBy(this::load)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("wasm-feed spec problem(s)")
                .hasMessageContaining("bad.json[0] (stations): id: must match")
                .hasMessageContaining("name: is required")
                .hasMessageContaining("plugin: must name a plugin directory")
                .hasMessageContaining("url: must be an absolute https URL")
                .hasMessageContaining("kinds: at least one kind is required")
                .hasMessageContaining("url: must not carry user info")
                .hasMessageContaining("kinds: 'Bad Kind'")
                .hasMessageContaining("historyTier: 'HOURLY'")
                .hasMessageContaining("observedAtMode: 'SOMETIMES'")
                .hasMessageContaining("ttlAnchor: 'NEVER'")
                .hasMessageContaining("egress: 'ANYWHERE'")
                .hasMessageContaining("recordTtl:")
                .hasMessageContaining("interval:")
                .hasMessageContaining("budget.perMinute: must be at least 1")
                .hasMessageContaining("budget.perDay: must be at least 1")
                .hasMessageContaining("budget.mbPerDay: must be at least 1")
                .hasMessageContaining("maxResponseBytes: must be 1 to 16777216")
                .hasMessageContaining("ingestPolicy:")
                .hasMessageContaining("bad.json[2] (wasm-c): url: refused by the outbound URL policy")
                .hasMessageContaining("bad.json[3] (wasm-c): id: 'wasm-c' is used by more than one spec");
    }

    @Test
    @DisplayName("an unknown property, a non-array file and a linked file stop startup")
    void bindingProblems() throws Exception {
        write("a.json", "[{\"id\":\"wasm-a\",\"surprise\":true}]");
        write("b.json", "{\"id\":\"wasm-b\"}");
        Path target = Files.writeString(dir.resolve("real.txt"), "[]");
        Files.createSymbolicLink(dir.resolve("c.json"), target);

        assertThatThrownBy(this::load)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not bind")
                .hasMessageContaining("must be a JSON array")
                .hasMessageContaining("must be a regular file, not a link");
    }

    @Test
    @DisplayName("a host that does not resolve now is accepted at load (checked again before every fetch)")
    void unresolvedHostAccepted() throws Exception {
        write("a.json", "[" + minimal("wasm-later", "https://nowhere.example/feed") + "]");
        assertThat(load().sources()).extracting(s -> s.spec().id()).containsExactly("wasm-later");
    }

    @Test
    @DisplayName("without the outbound URL policy every spec is refused")
    void policyUnavailable() throws Exception {
        write("a.json", "[" + minimal("wasm-a", "https://data.example.org/x") + "]");
        assertThatThrownBy(() -> new WasmFeedSpecs(dir, () -> null, CODEC))
                .hasMessageContaining("the outbound URL policy is unavailable");
    }

    @Test
    @DisplayName("the source registry registers the family's sources with its own validation")
    void registryAcceptsTheFamily() throws Exception {
        write("stations.json", VALID);
        WasmFeedSpecs specs = load();
        WasmFeedFamily family = new WasmFeedFamily(specs.sources(), (p, b, d) -> null, UrlGate.ALLOW_ALL, null);

        SourceRegistry registry = new SourceRegistry(List.of(), List.of(family), event -> { });

        assertThat(registry.contains("wasm-stations")).isTrue();
        assertThat(registry.get("wasm-stations").adapter()).isInstanceOf(WasmFeedAdapter.class);
        assertThat(((WasmFeedAdapter) registry.get("wasm-stations").adapter()).spec().plugin())
                .isEqualTo(PluginTestFiles.PASSTHROUGH);
    }

    @Test
    @DisplayName("the production URL gate uses PUBLIC_ONLY and refuses when the policy is missing")
    void outboundGate() {
        UrlGate gate = UrlGate.outbound(() -> POLICY);
        assertThat(gate.refusal(java.net.URI.create("https://data.example.org/x"))).isNull();
        assertThat(gate.refusal(java.net.URI.create("https://private.example/x"))).isNotNull();
        assertThat(gate.refusal(java.net.URI.create("http://data.example.org/x"))).isNotNull();
        assertThat(UrlGate.outbound(() -> null).refusal(java.net.URI.create("https://data.example.org/x")))
                .contains("unavailable");
        assertThat(UrlGate.ALLOW_ALL.refusal(java.net.URI.create("http://localhost/x"))).isNull();
    }
}
