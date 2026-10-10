package ai.elaro.source;

import ai.elaro.api.error.InvalidInputException;
import ai.elaro.source.spi.IngestPolicy;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Converts an {@link IngestPolicy} to and from the JSON stored in
 * {@code source_settings.ingest_policy}.
 *
 * <p>A thin wrapper, but it keeps the stored form in one place and turns a malformed stored policy
 * into a clear message rather than a Jackson stack trace.
 */
@Component
public class IngestPolicyCodec {

    private final JsonMapper jsonMapper;

    public IngestPolicyCodec(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /** The policy as JSON, or null for a null policy. */
    public String toJson(IngestPolicy policy) {
        if (policy == null) {
            return null;
        }
        try {
            return jsonMapper.writeValueAsString(policy);
        } catch (JacksonException e) {
            throw new InvalidInputException(
                    "Could not serialize the ingest policy: " + e.getOriginalMessage(), e);
        }
    }

    /** The policy parsed from {@code json}, or null when there is none. */
    public IngestPolicy fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return jsonMapper.readValue(json, IngestPolicy.class);
        } catch (JacksonException e) {
            throw new InvalidInputException(
                    "Could not read the ingest policy. Expected"
                            + " {\"mode\":\"ALL|SAMPLED|LATEST\", ...}, but got: "
                            + e.getOriginalMessage(),
                    e);
        }
    }
}
