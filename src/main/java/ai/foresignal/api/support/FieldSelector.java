package ai.foresignal.api.support;

import ai.foresignal.api.error.ValidationException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Parses {@code fields=}: a comma list of top-level properties and {@code payload.<dot.path>}
 * entries, for example {@code externalId,title,geometry,payload.mag}.
 *
 * <p>Unknown top-level names are a {@code 400 VALIDATION_FAILED}. Payload paths are applied in SQL
 * ({@code jsonb_build_object} of the selected {@code #>} paths); top-level fields are applied by the
 * mapper.
 */
public final class FieldSelector {

    /** Every top-level property a projection may name (Record, RecordVersion and Change fields). */
    public static final Set<String> KNOWN = Set.of(
            "id", "source", "externalId", "kind", "title", "version", "seq", "observedAt",
            "persistedObservedAt", "eventTime", "validFrom", "validUntil", "tombstone", "geometry",
            "payload", "quality", "live", "qaFlags", "attribution", "distanceKm", "ingestedAt",
            "observer", "persistReason", "prevSeq", "historyTier", "receivedAt", "batchId",
            "downsampled", "bbox");

    /** The most entries one projection may list. */
    public static final int MAX_ENTRIES = 100;

    private static final Pattern PATH_SEGMENT = Pattern.compile("[A-Za-z0-9_\\-:@$]{1,128}");

    private FieldSelector() {}

    /**
     * Parses {@code raw}.
     *
     * @return the selection; {@link FieldSelection#ALL} when {@code raw} is null or blank
     * @throws ValidationException naming the offending entry
     */
    public static FieldSelection parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return FieldSelection.ALL;
        }
        Set<String> topLevel = new LinkedHashSet<>();
        Set<String> paths = new LinkedHashSet<>();
        List<String> unknown = new ArrayList<>();
        int count = 0;
        for (String part : raw.split(",")) {
            String entry = part.strip();
            if (entry.isEmpty()) {
                continue;
            }
            if (++count > MAX_ENTRIES) {
                throw ValidationException.forField("fields", "at most " + MAX_ENTRIES + " entries are allowed");
            }
            if (entry.startsWith("payload.")) {
                String path = entry.substring("payload.".length());
                for (String segment : path.split("\\.", -1)) {
                    if (!PATH_SEGMENT.matcher(segment).matches()) {
                        throw ValidationException.forField("fields", "'" + entry + "' is not a valid payload path");
                    }
                }
                paths.add(path);
            } else if (KNOWN.contains(entry)) {
                topLevel.add(entry);
            } else {
                unknown.add(entry);
            }
        }
        if (!unknown.isEmpty()) {
            throw ValidationException.forField("fields", "unknown field(s): " + String.join(", ", unknown));
        }
        if (topLevel.isEmpty() && paths.isEmpty()) {
            return FieldSelection.ALL;
        }
        return new FieldSelection(false, topLevel, List.copyOf(paths));
    }
}
