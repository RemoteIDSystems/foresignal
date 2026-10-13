package ai.foresignal.source.adapters.satdump;

import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The {@code polarGlob} parameter of a {@code satdump-{station}} source (prompt 054): which files of a
 * ready pass folder are read as polar GeoTIFF overlays. Matched against the file's path relative to
 * the pass folder, with {@code /} separators, ignoring case.
 *
 * <ul>
 *   <li>{@code **} matches any characters, including {@code /}; {@code **}{@code /} also matches no
 *       directory at all, so the default {@value SatDumpDescriptor#DEFAULT_POLAR_GLOB} matches a
 *       {@code .tif} at any depth, the pass folder itself included;
 *   <li>{@code *} matches any characters except {@code /}, {@code ?} one character except {@code /};
 *   <li>everything else is literal.
 * </ul>
 */
public final class PolarGlob {

    /** The longest glob accepted. */
    public static final int MAX_LENGTH = 200;

    private final String glob;
    private final Pattern pattern;

    private PolarGlob(String glob, Pattern pattern) {
        this.glob = glob;
        this.pattern = pattern;
    }

    /**
     * Compiles a glob.
     *
     * @throws IllegalArgumentException when it is empty, too long, absolute or leaves the pass folder
     */
    public static PolarGlob compile(String glob) {
        if (glob == null || glob.isBlank()) {
            throw new IllegalArgumentException("must not be empty");
        }
        String g = glob.strip();
        if (g.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("must be at most " + MAX_LENGTH + " characters");
        }
        if (g.startsWith("/") || g.contains("\\") || g.equals("..") || g.startsWith("../") || g.contains("/../")
                || g.endsWith("/..")) {
            throw new IllegalArgumentException("must be relative to the pass folder and must not use .. or \\");
        }
        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < g.length(); i++) {
            char c = g.charAt(i);
            if (c == '*' && i + 1 < g.length() && g.charAt(i + 1) == '*') {
                if (i + 2 < g.length() && g.charAt(i + 2) == '/') {
                    regex.append("(?:.*/)?");
                    i += 2;
                } else {
                    regex.append(".*");
                    i += 1;
                }
            } else if (c == '*') {
                regex.append("[^/]*");
            } else if (c == '?') {
                regex.append("[^/]");
            } else {
                regex.append(Pattern.quote(String.valueOf(c)));
            }
        }
        regex.append('$');
        try {
            return new PolarGlob(g, Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("is not a valid glob: " + e.getDescription());
        }
    }

    /** The glob of {@code text}, or the default when it is blank. */
    public static PolarGlob orDefault(String text) {
        return compile(text == null || text.isBlank() ? SatDumpDescriptor.DEFAULT_POLAR_GLOB : text);
    }

    /** True when {@code relative} (a path below the pass folder, {@code /}-separated) matches. */
    public boolean matches(String relative) {
        return relative != null && pattern.matcher(relative.replace('\\', '/')).matches();
    }

    /** True when the file name ends in {@code .tif} or {@code .tiff}. */
    static boolean tiff(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".tif") || lower.endsWith(".tiff");
    }

    @Override
    public String toString() {
        return glob;
    }
}
