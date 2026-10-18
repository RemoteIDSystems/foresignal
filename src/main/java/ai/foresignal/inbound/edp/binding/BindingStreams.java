package ai.foresignal.inbound.edp.binding;

import ai.foresignal.inbound.edp.EdpManifestModel;
import ai.foresignal.inbound.edp.EdpManifestService;
import ai.foresignal.inbound.edp.ExternalSourceEntity;
import ai.foresignal.inbound.edp.ExternalSourceRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Looks up the registered streams bindings refer to (prompt 138). */
@Component
public class BindingStreams {

    private final ExternalSourceRepository externals;
    private final EdpManifestService manifests;

    /** Creates the lookup. */
    public BindingStreams(ExternalSourceRepository externals, EdpManifestService manifests) {
        this.externals = externals;
        this.manifests = manifests;
    }

    /** The stream {@code streamId} of provider {@code sourceId}, or empty when either is unknown. */
    public Optional<BoundStream> stream(String sourceId, String streamId) {
        if (sourceId == null || streamId == null) {
            return Optional.empty();
        }
        Optional<ExternalSourceEntity> ext = externals.findById(sourceId);
        if (ext.isEmpty()) {
            return Optional.empty();
        }
        EdpManifestModel model;
        try {
            model = manifests.model(ext.get());
        } catch (RuntimeException e) {
            return Optional.empty();
        }
        EdpManifestModel.Stream stream = model.stream(streamId);
        return stream == null ? Optional.empty()
                : Optional.of(new BoundStream(sourceId, ext.get().getManifestSha256(), model, stream));
    }

    /** The stream of {@code binding}, or empty when the provider or stream is gone. */
    public Optional<BoundStream> stream(ExternalBindingEntity binding) {
        return stream(binding.getSourceId(), binding.getStreamId());
    }

    /** The provider's manifest, or empty when it is not registered. */
    public Optional<EdpManifestModel> manifest(String sourceId) {
        return externals.findById(sourceId == null ? "" : sourceId).map(manifests::model);
    }
}
