package ai.foresignal.inbound.format;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Every registered {@link IngestFormat}, by id, plus the reserved names.
 *
 * <p>{@code media} and {@code rasters} are the remote SatDump push endpoints of prompt 054
 * ({@code ingestSourceMedia}, {@code ingestSourceRasters}), whose literal path segments win over
 * {@code /v1/ingest/{sourceId}/{format}}; {@code mapping-preview} stays reserved so it can never shadow
 * prompt 059's {@code POST /v1/mappings/preview}. They are listed as reserved and cannot be registered by any
 * format. Prompt 059's device-preset formats are registered by {@code DeviceFormatsConfiguration}.
 */
@Component
public class IngestFormatRegistry {

    /** Format names no format may take: the 054 push endpoints and {@code mapping-preview}. */
    public static final Set<String> RESERVED = Set.of("media", "rasters", "mapping-preview");

    /** The format used by {@code POST /v1/ingest/{sourceId}}. */
    public static final String DEFAULT_FORMAT = "native";

    private final Map<String, IngestFormat> formats = new LinkedHashMap<>();

    /**
     * Collects the format beans.
     *
     * @throws IllegalStateException on a duplicate or reserved id
     */
    public IngestFormatRegistry(List<IngestFormat> beans) {
        List<IngestFormat> sorted = new ArrayList<>(beans);
        sorted.sort(Comparator.comparing(IngestFormat::id));
        for (IngestFormat format : sorted) {
            if (RESERVED.contains(format.id())) {
                throw new IllegalStateException("Ingest format id '" + format.id() + "' is reserved");
            }
            IngestFormat previous = formats.putIfAbsent(format.id(), format);
            if (previous != null) {
                throw new IllegalStateException("Duplicate ingest format id '" + format.id() + "'");
            }
        }
    }

    /** The format {@code id}, or empty. */
    public Optional<IngestFormat> find(String id) {
        return Optional.ofNullable(id == null ? null : formats.get(id));
    }

    /** Every registered format, ordered by id. */
    public List<IngestFormat> all() {
        return List.copyOf(formats.values());
    }

    /** True when {@code id} is reserved for a later prompt. */
    public boolean isReserved(String id) {
        return RESERVED.contains(id);
    }
}
