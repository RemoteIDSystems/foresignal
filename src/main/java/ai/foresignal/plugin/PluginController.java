package ai.foresignal.plugin;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The plugin operations over REST (prompt 141, OpenAPI tag {@code Plugins}), all {@code admin} through
 * {@code AccessPolicy}:
 *
 * <ul>
 *   <li>{@code GET /v1/plugins} ({@code listPlugins}) and {@code PUT /v1/plugins} ({@code updatePluginSettings});
 *   <li>{@code GET /v1/plugins/{name}} ({@code getPlugin}) and {@code PUT /v1/plugins/{name}} ({@code updatePlugin}).
 * </ul>
 * A name that is not a valid plugin name is reported as unknown ({@code 404 UNKNOWN_PLUGIN}), so no path outside the
 * plugin root is ever looked up.
 */
@RestController
@Tag(name = "Plugins")
public class PluginController {

    private final PluginService plugins;

    /** Creates the controller. */
    public PluginController(PluginService plugins) {
        this.plugins = plugins;
    }

    /** {@code listPlugins}: the {@code plugins} setting and every plugin found on disk. */
    @GetMapping("/v1/plugins")
    @Operation(operationId = "listPlugins", tags = "Plugins")
    public PluginListView listPlugins() {
        return plugins.list();
    }

    /** {@code updatePluginSettings}: switches plugins on or off. */
    @PutMapping("/v1/plugins")
    @Operation(operationId = "updatePluginSettings", tags = "Plugins")
    public PluginListView updatePluginSettings(@RequestBody PluginSettings body) {
        return plugins.updateSettings(body);
    }

    /** {@code getPlugin}: one plugin; 404 {@code UNKNOWN_PLUGIN}. */
    @GetMapping("/v1/plugins/{name}")
    @Operation(operationId = "getPlugin", tags = "Plugins")
    public PluginView getPlugin(@PathVariable String name) {
        return plugins.get(checkedName(name));
    }

    /** {@code updatePlugin}: enables a plugin after review of its SHA-256, or disables it. */
    @PutMapping("/v1/plugins/{name}")
    @Operation(operationId = "updatePlugin", tags = "Plugins")
    public PluginView updatePlugin(@PathVariable String name, @RequestBody PluginUpdate body) {
        return plugins.update(checkedName(name), body);
    }

    private static String checkedName(String name) {
        if (name == null || !PluginManifest.NAME.matcher(name).matches()) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_PLUGIN, "There is no plugin '" + name + "'.");
        }
        return name;
    }
}
