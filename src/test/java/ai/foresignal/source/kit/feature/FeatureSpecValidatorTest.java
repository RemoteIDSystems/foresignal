package ai.foresignal.source.kit.feature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.kit.KitOverlay;
import ai.foresignal.source.kit.KitSpecLoader;
import ai.foresignal.source.kit.KitSpecs;
import ai.foresignal.source.kit.KitTestSpecs;
import ai.foresignal.source.kit.feed.FeedKitFamily;
import ai.foresignal.source.kit.feed.FeedSpecValidator;
import ai.foresignal.source.spi.SourceAdapter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link FeatureSpecValidator} (prompt 073): each rule fails with its field path, every problem of every spec is
 * reported together, the shipped specs are valid, and the operator overlay is off by default and checked when on.
 */
class FeatureSpecValidatorTest {

    static final String URL = "https://services.example.org/arcgis/rest/services/X/FeatureServer/0";

    static List<String> problems(Map<String, Object> overrides) {
        return FeatureSpecValidator.problems(KitTestSpecs.arcgis(URL, overrides), false);
    }

    static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    @Test
    @DisplayName("the test spec is valid, and so is its descriptor")
    void valid() {
        assertThat(problems(Map.of())).isEmpty();
        KitFeatureAdapter adapter = new KitFeatureAdapter(KitTestSpecs.arcgis(URL), null);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
    }

    @Test
    @DisplayName("identity: id pattern and length, name, kind")
    void identity() {
        assertThat(problems(map("id", "wfigs"))).anyMatch(p -> p.startsWith("id: 'wfigs' must match"));
        assertThat(problems(map("id", "feature-" + "x".repeat(64)))).anyMatch(p -> p.startsWith("id: is longer"));
        assertThat(problems(map("id", null))).contains("id: is required");
        assertThat(problems(map("name", " "))).contains("name: is required");
        assertThat(problems(map("kind", "Fire Incident"))).anyMatch(p -> p.startsWith("kind:"));
        assertThat(FeatureSpecValidator.problems(KitTestSpecs.arcgis(URL, map("id", "op-mine")), true)).isEmpty();
        assertThat(FeatureSpecValidator.problems(KitTestSpecs.arcgis(URL), true))
                .anyMatch(p -> p.startsWith("id: 'feature-test-arcgis' must match ^op-"));
    }

    @Test
    @DisplayName("URL: https only, no user info, no credential-looking query parameter")
    void url() {
        assertThat(FeatureSpecValidator.problems(KitTestSpecs.arcgis("http://a.example/x"), false))
                .contains("url: must be an https URL");
        assertThat(FeatureSpecValidator.problems(KitTestSpecs.arcgis("https://u:p@a.example/x"), false))
                .contains("url: must not carry user info");
        assertThat(FeatureSpecValidator.problems(KitTestSpecs.arcgis("https://a.example/x?token=abc"), false))
                .anyMatch(p -> p.startsWith("url: the query parameter 'token' looks like a credential"));
        assertThat(FeatureSpecValidator.problems(
                KitTestSpecs.arcgis("https://a.example/x?q=a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8"), false))
                .anyMatch(p -> p.startsWith("url: the value of query parameter 'q' looks like a credential"));
    }

    @Test
    @DisplayName("protocol: known protocols, collection and typeName, esrijson only for ArcGIS")
    void protocol() {
        assertThat(problems(map("protocol", "wms"))).anyMatch(p -> p.startsWith("protocol:"));
        assertThat(problems(map("protocol", "ogc"))).contains("collection: is required for protocol ogc");
        assertThat(problems(map("protocol", "wfs"))).contains("typeName: is required for protocol wfs");
        assertThat(problems(map("protocol", "ogc", "collection", "c", "format", "esrijson")))
                .contains("format: esrijson is only for protocol arcgis");
        assertThat(problems(map("query", Map.of("bbox", List.of(10, 0, 5, 1)))))
                .anyMatch(p -> p.startsWith("query.bbox:"));
        assertThat(problems(map("query", Map.of("filters", Map.of("apikey", "x")))))
                .anyMatch(p -> p.startsWith("query.filters.apikey: looks like a credential"));
        assertThat(problems(map("layer", "zero"))).anyMatch(p -> p.startsWith("layer:"));
    }

    @Test
    @DisplayName("fields: types, units, targets unique and not reserved, constants never shadow a field")
    void fields() {
        assertThat(problems(map("fields", Map.of("A", Map.of("target", "irwinId", "type", "date")))))
                .anyMatch(p -> p.startsWith("fields.A.type: must be one of"));
        assertThat(problems(map("fields", Map.of("IrwinID", Map.of("target", "irwinId", "type", "string"),
                "B", Map.of("target", "irwinId", "type", "string")))))
                .anyMatch(p -> p.endsWith("is used twice"));
        assertThat(problems(map("fields", Map.of("IrwinID", Map.of("target", "irwin.id", "type", "string")))))
                .anyMatch(p -> p.startsWith("fields.IrwinID.target:"));
        assertThat(problems(map("fields", Map.of("IrwinID", Map.of("target", "irwinId", "type", "string"),
                "S", Map.of("target", "geometrySimplified", "type", "boolean")))))
                .anyMatch(p -> p.contains("is reserved for the kit"));
        assertThat(problems(map("fields", Map.of("IrwinID", Map.of("target", "irwinId", "type", "number",
                "unit", "acres per day")))))
                .anyMatch(p -> p.startsWith("fields.IrwinID.unit:"));
        assertThat(problems(map("constants", Map.of("name", "x")))).contains("constants.name: shadows a field target");
        assertThat(problems(map("constants", Map.of("restrictionType", List.of(1)))))
                .contains("constants.restrictionType: must be a string, number or boolean");
    }

    @Test
    @DisplayName("references: idField, titleField and the field lists name declared targets")
    void references() {
        assertThat(problems(map("idField", "IrwinID"))).contains("idField: 'IrwinID' is not a declared field target");
        assertThat(problems(map("idField", null))).contains("idField: is required");
        assertThat(problems(map("titleField", "title"))).contains("titleField: 'title' is not a declared field target");
        assertThat(problems(map("significantFields", List.of("nope"))))
                .contains("significantFields[0]: 'nope' is not a declared field target");
        assertThat(problems(map("searchFields", List.of("name", "x"))))
                .contains("searchFields[1]: 'x' is not a declared field target");
        assertThat(problems(map("tileProperties", List.of("y")))).contains("tileProperties[0]: 'y' is not a declared"
                + " field target");
        assertThat(problems(map("significantFields", List.of("modifiedAt"))))
                .contains("significantFields: 'modifiedAt' is also volatile");
        assertThat(problems(map("constants", Map.of("restrictionType", "closure"),
                "tileProperties", List.of("restrictionType")))).isEmpty();
    }

    @Test
    @DisplayName("time base: one timestamp field per entry, never a fallback; RECEIVED only for observedAt")
    void timeBase() {
        assertThat(problems(map("time", Map.of("observedAt", "modifiedAt,discoveredAt"))))
                .anyMatch(p -> p.startsWith("time.observedAt: names one field only"));
        assertThat(problems(map("time", Map.of("observedAt", "modifiedAt|discoveredAt"))))
                .anyMatch(p -> p.startsWith("time.observedAt: names one field only"));
        assertThat(problems(map("time", Map.of("observedAt", "name"))))
                .contains("time.observedAt: 'name' must have type timestamp");
        assertThat(problems(map("time", Map.of("observedAt", "updated"))))
                .contains("time.observedAt: 'updated' is not a declared field target");
        assertThat(problems(map("time", Map.of("observedAt", "RECEIVED", "eventTime", "RECEIVED"))))
                .contains("time.eventTime: RECEIVED is only for time.observedAt");
        assertThat(problems(map("time", Map.of("observedAt", "RECEIVED")))).isEmpty();
        assertThat(problems(map("time", Map.of("observedAt", "modifiedAt", "format", "yyyy-MM-dd HH:mm:ss",
                "zone", "America/Denver")))).isEmpty();
        assertThat(problems(map("time", Map.of("observedAt", "modifiedAt", "zone", "Mars/Olympus"))))
                .anyMatch(p -> p.startsWith("time.zone:"));
        assertThat(problems(map("time", Map.of("observedAt", "modifiedAt", "format", "yyyy-MM-dd'T"))))
                .anyMatch(p -> p.startsWith("time.format:"));
        assertThat(problems(map("ttlAnchor", "OBSERVED", "time", Map.of("observedAt", "RECEIVED"))))
                .contains("ttlAnchor: OBSERVED needs time.observedAt to name a field");
    }

    @Test
    @DisplayName("schedule: durations, minInterval, the keyframe constraint, the budget")
    void schedule() {
        assertThat(problems(map("interval", "often"))).anyMatch(p -> p.startsWith("interval: 'often' is not a"));
        assertThat(problems(map("interval", "5m", "minInterval", "10m")))
                .anyMatch(p -> p.startsWith("minInterval: PT10M exceeds interval"));
        assertThat(problems(map("recordTtl", "10m", "interval", "10m")))
                .anyMatch(p -> p.startsWith("recordTtl: PT10M leaves no keyframe"));
        assertThat(problems(map("recordTtl", "6h", "interval", "10m"))).isEmpty();
        assertThat(problems(map("budget", Map.of("perMinute", 1)))).contains("budget: perMinute and perDay are"
                + " required");
        assertThat(problems(map("budget", Map.of("perMinute", 0, "perDay", 10))))
                .contains("budget: perMinute and perDay must be positive");
        assertThat(problems(map("ttlAnchor", "SOMETIME"))).anyMatch(p -> p.startsWith("ttlAnchor:"));
    }

    @Test
    @DisplayName("limits: paging caps, axis order for WFS only, header allowlist, credential shape")
    void limits() {
        assertThat(problems(map("paging", Map.of("maxFeatures", 60_000)))).anyMatch(p -> p.startsWith("paging"
                + ".maxFeatures"));
        assertThat(problems(map("paging", Map.of("maxPages", 0)))).anyMatch(p -> p.startsWith("paging.maxPages"));
        assertThat(problems(map("paging", Map.of("pageSize", 20_000)))).anyMatch(p -> p.startsWith("paging"
                + ".pageSize"));
        assertThat(problems(map("geometry", Map.of("axisOrder", "latlon"))))
                .contains("geometry.axisOrder: is only for protocol wfs");
        assertThat(problems(map("headers", Map.of("User-Agent", "Mozilla/5.0"))))
                .anyMatch(p -> p.startsWith("headers.User-Agent: only"));
        assertThat(problems(map("headers", Map.of("Accept", "application/geo+json")))).isEmpty();
        assertThat(problems(map("credential", Map.of("name", "apiKey", "placement", "cookie", "param", "k"))))
                .contains("credential.placement: must be query or header");
        assertThat(problems(map("credential", Map.of("name", "apiKey", "placement", "header", "param", "Referer"))))
                .anyMatch(p -> p.startsWith("credential.param: 'Referer'"));
        assertThat(problems(map("credential", Map.of("name", "apiKey", "placement", "query", "param", "key"))))
                .isEmpty();
        assertThat(problems(map("license", null, "attribution", ""))).contains("license: is required",
                "attribution: is required");
        assertThat(problems(map("homepage", "http://example.org/"))).contains("homepage: must be an https URL");
    }

    @Test
    @DisplayName("every problem of every spec is reported together, with the file, index and id")
    void aggregated() {
        List<String> problems = new ArrayList<>();
        List<KitSpecLoader.Loaded<FeatureSourceSpec>> loaded = new ArrayList<>();
        String bad = KitTestSpecs.with(KitTestSpecs.ARCGIS.formatted("http://x.example/"),
                map("id", "bad id", "kind", "", "budget", null));
        KitSpecLoader.read("kits/features/test.json", ("[" + bad + "," + KitTestSpecs.ARCGIS.formatted(URL) + ","
                + KitTestSpecs.ARCGIS.formatted(URL) + ", {\"id\": \"feature-x\", \"surprise\": 1}]")
                .getBytes(StandardCharsets.UTF_8), FeatureSourceSpec.class, false, loaded, problems);
        problems.addAll(FeatureSpecValidator.problems(loaded, Set.of(), KitOverlay.disabled()));
        assertThat(problems).anyMatch(p -> p.startsWith("kits/features/test.json[0] bad id: id:"));
        assertThat(problems).anyMatch(p -> p.startsWith("kits/features/test.json[0] bad id: url: must be an https"));
        assertThat(problems).anyMatch(p -> p.startsWith("kits/features/test.json[0] bad id: kind:"));
        assertThat(problems).anyMatch(p -> p.startsWith("kits/features/test.json[0] bad id: budget:"));
        assertThat(problems).anyMatch(p -> p.startsWith("kits/features/test.json[2] feature-test-arcgis: id:")
                && p.endsWith("is declared twice"));
        assertThat(problems).anyMatch(p -> p.startsWith("kits/features/test.json[3]: does not bind"));
    }

    @Test
    @DisplayName("the shipped specs are valid, every descriptor validates, and every kit source is disabled by default")
    void shipped() {
        KitSpecs specs = new KitSpecs(KitOverlay.disabled(), "");
        assertThat(specs.features()).extracting(FeatureSourceSpec::id).contains("feature-wfigs-incidents",
                "faa-uas-nsufr");
        assertThat(specs.feeds()).extracting(s -> s.id()).containsExactlyInAnyOrder("feed-cisa-advisories",
                "feed-ncsc-uk", "feed-nhc-atlantic", "feed-nhc-eastpacific", "feed-ecdc-threats", "feed-who-news");
        List<SourceAdapter> adapters = new ArrayList<>(new FeatureKitFamily(specs.features(), "", null).adapters());
        adapters.addAll(new FeedKitFamily(specs.feeds(), "", null, null).adapters());
        for (SourceAdapter a : adapters) {
            assertThat(DescriptorValidator.problems(a.descriptor(), a)).as(a.id()).isEmpty();
            assertThat(a.descriptor().licence()).as(a.id()).isNotBlank();
        }
        // Prompt 087: faa-uas.json now sorts before wfigs.json, so the spec is found by id.
        FeatureSourceSpec wfigs = specs.features().stream().filter(f -> f.id().equals("feature-wfigs-incidents"))
                .findFirst().orElseThrow();
        assertThat(wfigs.protocol()).isEqualTo("arcgis");
        assertThat(wfigs.tombstonesAbsent()).isTrue();
        assertThat(wfigs.time().observedAt()).isEqualTo("modifiedAt");
        assertThat(wfigs.time().eventTime()).isEqualTo("discoveredAt");
        assertThat(wfigs.intervalDuration()).hasMinutes(10);
        assertThat(wfigs.fields()).containsKeys("IrwinID", "IncidentName", "IncidentSize", "PercentContained",
                "POOState", "UniqueFireIdentifier", "FireDiscoveryDateTime", "ModifiedOnDateTime_dt");
        assertThat(specs.feeds().stream().filter(f -> f.id().equals("feed-cisa-advisories")).findFirst().orElseThrow()
                .intervalDuration()).hasMinutes(60);
    }

    @Test
    @DisplayName("latestPerKey (prompt 079) needs time.observedAt to name a field")
    void latestPerKey() {
        assertThat(problems(map("latestPerKey", true, "time", Map.of("observedAt", "RECEIVED"))))
                .contains("latestPerKey: needs time.observedAt to name a field");
        assertThat(problems(map("latestPerKey", false, "time", Map.of("observedAt", "RECEIVED"))))
                .noneMatch(p -> p.startsWith("latestPerKey"));
        assertThat(KitTestSpecs.arcgis(URL, map("latestPerKey", true)).keepsLatestPerKey()).isTrue();
        assertThat(KitTestSpecs.arcgis(URL).keepsLatestPerKey()).isFalse();
    }

    @Test
    @DisplayName("the overlay is off by default; when on, ids need op- and URLs pass the outbound URL policy")
    void overlay(@TempDir Path dir) throws Exception {
        Path features = Files.createDirectories(dir.resolve("kits").resolve("features"));
        Files.writeString(features.resolve("mine.json"), "[" + KitTestSpecs.with(KitTestSpecs.ARCGIS.formatted(URL),
                map("id", "feature-not-op")) + "]");
        assertThat(new KitOverlay(false, dir, () -> null).directory("features")).isNull();
        assertThat(new KitOverlay(true, dir, () -> null).directory("features")).as("no policy: refused").isNull();
        KitOverlay on = new KitOverlay(true, dir, () -> ai.foresignal.net.OutboundUrlPolicy.withoutOverrides(
                host -> new java.net.InetAddress[] {java.net.InetAddress.getByName("10.0.0.5")}));
        assertThat(on.directory("features")).isEqualTo(features);
        assertThatThrownBy(() -> new KitSpecs(on, ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mine.json[0] feature-not-op: id: 'feature-not-op' must match ^op-")
                .hasMessageContaining("mine.json[0] feature-not-op: url: refused by the outbound URL policy");
        assertThat(FeedSpecValidator.OVERLAY_ID_PATTERN.matcher("op-feed").matches()).isTrue();
    }
}
