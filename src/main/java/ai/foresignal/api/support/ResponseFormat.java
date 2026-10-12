package ai.foresignal.api.support;

import ai.foresignal.api.error.ValidationException;
import java.util.List;
import java.util.Locale;
import org.springframework.http.MediaType;

/**
 * Response formats of the read endpoints, chosen by {@code ?format=} (which wins) or the
 * {@code Accept} header.
 */
public enum ResponseFormat {
    JSON("json", MediaType.APPLICATION_JSON_VALUE, false),
    GEOJSON("geojson", "application/geo+json", false),
    NDJSON("ndjson", "application/x-ndjson", true),
    GEOJSONSEQ("geojsonseq", "application/geo+json-seq", true),
    CSV("csv", "text/csv", true);

    private final String wireValue;
    private final String mediaType;
    private final boolean streaming;

    ResponseFormat(String wireValue, String mediaType, boolean streaming) {
        this.wireValue = wireValue;
        this.mediaType = mediaType;
        this.streaming = streaming;
    }

    /** The {@code format=} value. */
    public String wireValue() {
        return wireValue;
    }

    /** The response content type (without parameters). */
    public String mediaType() {
        return mediaType;
    }

    /** True for the unpaged streaming formats ({@code ndjson}, {@code geojsonseq}, {@code csv}). */
    public boolean streaming() {
        return streaming;
    }

    /**
     * Chooses the format.
     *
     * @param format the {@code format} parameter, or null
     * @param accept the {@code Accept} header, or null
     * @param allowed the formats the endpoint offers, in preference order (the first is the default)
     * @return the chosen format
     * @throws ValidationException when {@code format} names a format the endpoint does not offer
     */
    public static ResponseFormat negotiate(String format, String accept, List<ResponseFormat> allowed) {
        if (format != null && !format.isBlank()) {
            String value = format.strip().toLowerCase(Locale.ROOT);
            for (ResponseFormat candidate : allowed) {
                if (candidate.wireValue.equals(value)) {
                    return candidate;
                }
            }
            throw ValidationException.forField(
                    "format",
                    "must be one of " + String.join(", ", allowed.stream().map(ResponseFormat::wireValue).toList()));
        }
        if (accept != null && !accept.isBlank()) {
            List<MediaType> types;
            try {
                types = MediaType.parseMediaTypes(accept);
            } catch (RuntimeException e) {
                return allowed.getFirst();
            }
            types = new java.util.ArrayList<>(types);
            types.sort(java.util.Comparator.comparingDouble(MediaType::getQualityValue).reversed());
            for (MediaType type : types) {
                if (type.isWildcardType() || type.isWildcardSubtype()) {
                    continue;
                }
                for (ResponseFormat candidate : allowed) {
                    if (MediaType.parseMediaType(candidate.mediaType).equalsTypeAndSubtype(type)) {
                        return candidate;
                    }
                }
            }
        }
        return allowed.getFirst();
    }
}
