package ai.foresignal.system;

/**
 * Contributes one top-level block to the full {@code /v1/system} document (prompt 025 adds
 * {@code mcp}). Contributors are looked up lazily, when the document is built, so a contributor may
 * depend on beans that themselves use {@link SystemInfoService}.
 */
public interface SystemBlockContributor {

    /** The member name, for example {@code mcp}. */
    String key();

    /** The block's current value: a map or DTO serialized as JSON. */
    Object block();
}
