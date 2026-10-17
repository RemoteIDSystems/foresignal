package ai.foresignal.observability;

import ai.foresignal.api.mapper.SourceMapper;
import ai.foresignal.evidence.OriginatorRegistry;
import org.springframework.stereotype.Component;

/**
 * Hands the evidence registries to {@link SourceMapper} (prompt 116), so {@code GET /v1/sources/{id}} shows the
 * upstream reference fields and declared observability in force (descriptor, else {@code intel/originators.json}
 * and {@code intel/observability.json}) without the mapper depending on the evidence layer.
 */
@Component
public class EvidenceRegistriesWiring {

    /** Wires the mapper. */
    public EvidenceRegistriesWiring(SourceMapper mapper, OriginatorRegistry originators,
            ObservabilityDeclarations declarations) {
        mapper.setEvidenceRegistries(originators::upstreamRefFields, declarations::declared);
    }
}
