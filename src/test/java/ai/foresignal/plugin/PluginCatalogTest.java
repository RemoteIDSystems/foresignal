package ai.foresignal.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Plugin discovery (prompt 141): directories by name, manifests bound strictly and checked, modules hashed, links
 * refused, and nothing ever run.
 */
class PluginCatalogTest {

    @TempDir
    Path root;

    @Test
    @DisplayName("a missing root holds no plugins; non-plugin entries are ignored; results are ordered by name")
    void discovery() throws Exception {
        assertThat(new PluginCatalog(root.resolve("absent")).discover()).isEmpty();
        assertThat(new PluginCatalog(null).discover()).isEmpty();
        PluginTestFiles.install(root, PluginTestFiles.PASSTHROUGH);
        PluginTestFiles.install(root, PluginTestFiles.LOG_SPAMMER);
        Files.createDirectories(root.resolve("Not_A_Plugin"));
        Files.writeString(root.resolve("readme.txt"), "x");

        List<DiscoveredPlugin> found = new PluginCatalog(root).discover();

        assertThat(found).extracting(DiscoveredPlugin::name)
                .containsExactly(PluginTestFiles.PASSTHROUGH, PluginTestFiles.LOG_SPAMMER);
        DiscoveredPlugin example = found.get(0);
        assertThat(example.error()).isNull();
        assertThat(example.manifest().version()).isEqualTo("1.0.0");
        assertThat(example.actualSha256()).isEqualTo(PluginTestFiles.PASSTHROUGH_SHA256);
        assertThat(example.hashMatches()).isTrue();
    }

    @Test
    @DisplayName("the committed example manifest declares the committed module's real SHA-256")
    void committedManifestMatches() {
        String manifest = new String(PluginTestFiles.bytes(PluginTestFiles.PASSTHROUGH, PluginCatalog.MANIFEST),
                java.nio.charset.StandardCharsets.UTF_8);
        assertThat(manifest).contains(PluginTestFiles.PASSTHROUGH_SHA256).contains("\"abi\": 1")
                .contains("\"kind\": \"parser\"");
        assertThat(PluginCatalog.sha256(PluginTestFiles.wasm(PluginTestFiles.PASSTHROUGH)))
                .isEqualTo(PluginTestFiles.PASSTHROUGH_SHA256);
        for (String plugin : List.of(PluginTestFiles.MEMORY_HOG, PluginTestFiles.INFINITE_LOOP,
                PluginTestFiles.OVERSIZED_OUTPUT, PluginTestFiles.WASI_IMPORT, PluginTestFiles.LOG_SPAMMER)) {
            String json = new String(PluginTestFiles.bytes(plugin, PluginCatalog.MANIFEST),
                    java.nio.charset.StandardCharsets.UTF_8);
            assertThat(json).as(plugin).contains(PluginCatalog.sha256(PluginTestFiles.wasm(plugin)));
        }
    }

    @Test
    @DisplayName("a declared hash that differs from the file is reported, not hidden")
    void hashMismatch() {
        PluginTestFiles.install(root, PluginTestFiles.PASSTHROUGH, PluginTestFiles.PASSTHROUGH, "a".repeat(64));

        DiscoveredPlugin plugin = new PluginCatalog(root).find(PluginTestFiles.PASSTHROUGH).orElseThrow();

        assertThat(plugin.error()).isNull();
        assertThat(plugin.hashMatches()).isFalse();
        assertThat(plugin.actualSha256()).isEqualTo(PluginTestFiles.PASSTHROUGH_SHA256);
    }

    @Test
    @DisplayName("manifest problems: abi, kind, name, version, sha256 format, unknown property, missing file")
    void manifestProblems() throws Exception {
        Path dir = PluginTestFiles.install(root, PluginTestFiles.PASSTHROUGH);
        PluginCatalog catalog = new PluginCatalog(root);

        Files.writeString(dir.resolve(PluginCatalog.MANIFEST),
                "{\"name\":\"other\",\"version\":\"\",\"abi\":2,\"kind\":\"sink\",\"sha256\":\"ABC\"}");
        DiscoveredPlugin bad = catalog.find(PluginTestFiles.PASSTHROUGH).orElseThrow();
        assertThat(bad.manifest()).isNull();
        assertThat(bad.error()).contains("must equal the directory name").contains("version is required")
                .contains("abi 2").contains("kind 'sink'").contains("sha256 must be");

        Files.writeString(dir.resolve(PluginCatalog.MANIFEST), "{\"name\":\"geojson-passthrough\",\"extra\":1}");
        assertThat(catalog.find(PluginTestFiles.PASSTHROUGH).orElseThrow().error()).contains("does not bind");

        Files.delete(dir.resolve(PluginCatalog.MANIFEST));
        assertThat(catalog.find(PluginTestFiles.PASSTHROUGH).orElseThrow().error()).contains("plugin.json is missing");

        Files.delete(dir.resolve(PluginCatalog.MODULE));
        DiscoveredPlugin noModule = catalog.find(PluginTestFiles.PASSTHROUGH).orElseThrow();
        assertThat(noModule.actualSha256()).isNull();
        assertThat(noModule.error()).contains("plugin.wasm is missing");
        assertThatThrownBy(() -> catalog.readModule(noModule))
                .isInstanceOfSatisfying(PluginException.class,
                        e -> assertThat(e.reason()).isEqualTo(PluginException.Reason.INVALID_MODULE));
    }

    @Test
    @DisplayName("links are refused: a linked plugin directory and a linked module")
    void linksRefused() throws Exception {
        Path real = PluginTestFiles.install(root.resolve("elsewhere"), PluginTestFiles.PASSTHROUGH);
        Path plugins = Files.createDirectories(root.resolve("plugins"));
        Files.createSymbolicLink(plugins.resolve("linked"), real);
        Path dir = Files.createDirectories(plugins.resolve("linked-module"));
        Files.createSymbolicLink(dir.resolve(PluginCatalog.MODULE), real.resolve(PluginCatalog.MODULE));
        Files.writeString(dir.resolve(PluginCatalog.MANIFEST),
                PluginTestFiles.manifest("linked-module", PluginTestFiles.PASSTHROUGH_SHA256));

        PluginCatalog catalog = new PluginCatalog(plugins);

        assertThat(catalog.find("linked").orElseThrow().error()).contains("links are refused");
        DiscoveredPlugin linkedModule = catalog.find("linked-module").orElseThrow();
        assertThat(linkedModule.actualSha256()).isNull();
        assertThat(linkedModule.error()).contains("plugin.wasm is a link");
    }

    @Test
    @DisplayName("find refuses names outside the pattern, so no path outside the root is looked up")
    void findRefusesBadNames() {
        PluginCatalog catalog = new PluginCatalog(root);
        assertThat(catalog.find("../etc")).isEmpty();
        assertThat(catalog.find("Upper")).isEmpty();
        assertThat(catalog.find(null)).isEmpty();
        assertThat(catalog.find("absent")).isEmpty();
        assertThat(catalog.root()).isEqualTo(root);
    }
}
