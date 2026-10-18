package ai.foresignal.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A plugin's {@code plugin.json} (prompt 141, {@code docs/Plugin-Design.md} §2), bound with unknown properties refused.
 *
 * @param name the plugin name; must equal its directory name
 * @param version the plugin's own version, free text up to 64 characters
 * @param abi the ABI version; this host runs {@value #ABI}
 * @param kind the plugin kind; ABI v1 knows only {@value #KIND_PARSER}
 * @param sha256 the lower-case hex SHA-256 of {@code plugin.wasm}
 */
public record PluginManifest(String name, String version, Integer abi, String kind, String sha256) {

    /** The ABI this host runs. */
    public static final int ABI = 1;

    /** The only kind of ABI v1. */
    public static final String KIND_PARSER = "parser";

    /** A plugin name: lower case, digits and hyphens, 1 to 64 characters, starting with a letter or digit. */
    public static final Pattern NAME = Pattern.compile("^[a-z0-9][a-z0-9-]{0,63}$");

    /** A lower-case hex SHA-256. */
    public static final Pattern SHA256 = Pattern.compile("^[0-9a-f]{64}$");

    /**
     * Every problem with this manifest for the plugin in directory {@code directoryName}, in field order.
     *
     * @param directoryName the name of the plugin's directory
     * @return the problems; empty when the manifest is valid
     */
    public List<String> problems(String directoryName) {
        List<String> problems = new ArrayList<>();
        if (name == null || !NAME.matcher(name).matches()) {
            problems.add("name must match " + NAME.pattern());
        } else if (!name.equals(directoryName)) {
            problems.add("name '" + name + "' must equal the directory name '" + directoryName + "'");
        }
        if (version == null || version.isBlank() || version.length() > 64) {
            problems.add("version is required (at most 64 characters)");
        }
        if (abi == null || abi != ABI) {
            problems.add("abi " + abi + " is not supported; this host runs abi " + ABI);
        }
        if (!KIND_PARSER.equals(kind)) {
            problems.add("kind '" + kind + "' is not supported; ABI v1 knows only \"" + KIND_PARSER + "\"");
        }
        if (sha256 == null || !SHA256.matcher(sha256).matches()) {
            problems.add("sha256 must be the lower-case hex SHA-256 of plugin.wasm");
        }
        return problems;
    }
}
