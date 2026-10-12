package ai.foresignal.system;

import java.util.List;

/**
 * Contributes warnings to {@code /v1/system} (prompt 042 adds the raster disk quota). A warning of
 * severity {@code warning} or {@code critical} makes the system status {@code degraded}.
 * Contributors are looked up lazily, when the warnings are built.
 */
public interface SystemWarningContributor {

    /** The warnings that apply now; empty when none. */
    List<SystemWarnings.Warning> warnings();
}
