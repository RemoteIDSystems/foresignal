package ai.foresignal.collab;

import ai.foresignal.security.Tlp;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Collaboration settings ({@code foresignal.collab.*}, prompt 130).
 *
 * @param defaultTlp the TLP label of a work product created without one (default {@code AMBER})
 * @param keepExif true to keep EXIF metadata in uploaded images (default false: stripped)
 * @param maxUploadMb the largest attachment upload, in MB (default 50)
 * @param savedSearchScheduled whether saved-search match notifications run on their own (off in tests, which call
 *     {@code SavedSearchService.notifyDue()})
 */
@ConfigurationProperties("foresignal.collab")
public record CollabProperties(
        @DefaultValue("AMBER") String defaultTlp,
        @DefaultValue("false") boolean keepExif,
        @DefaultValue("50") int maxUploadMb,
        @DefaultValue("true") boolean savedSearchScheduled) {

    /** Fills missing values with the defaults and validates the label. */
    public CollabProperties {
        defaultTlp = defaultTlp == null || defaultTlp.isBlank() ? "AMBER" : Tlp.fromWire(defaultTlp).wireValue();
        maxUploadMb = maxUploadMb <= 0 ? 50 : maxUploadMb;
    }

    /** The default label. */
    public Tlp tlp() {
        return Tlp.fromWire(defaultTlp);
    }

    /** The upload limit in bytes. */
    public long maxUploadBytes() {
        return maxUploadMb * 1024L * 1024L;
    }
}
