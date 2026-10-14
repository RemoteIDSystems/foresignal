package ai.foresignal.source.adapters.mesh;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PushAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import org.springframework.stereotype.Component;

/**
 * {@code meshcore-bridge} (prompt 083): the operator's MeshCore companion over USB, BLE or TCP through the bridge script
 * {@code contrib/meshcore-bridge/bridge.py}, which uses the MIT {@code meshcore} package (meshcore_py) and POSTs JSON
 * events to {@code /v1/ingest/meshcore-bridge/meshcore-json}.
 *
 * <p>A PUSH source: Foresignal calls nothing. The bridge authenticates with the key the operator stores as this
 * source's {@value SourceDescriptor#REMOTE_MEDIA_KEY} credential (an {@code eik_…} value of their choosing; an admin
 * token also works). The {@code meshcore-json} format maps the events with the same {@link MeshCoreEventMapper} as
 * {@code meshcore-local}, so records, repeater health and the {@link MeshMessageBus} behave alike. RECEIVED times;
 * private.
 */
@Component
public class MeshCoreBridgeAdapter implements PushAdapter {

    /** The source id. */
    public static final String ID = "meshcore-bridge";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    /** Creates the adapter. */
    public MeshCoreBridgeAdapter() {}

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Own MeshCore companion (bridge)")
                .description("The operator's own MeshCore companion over USB, BLE or TCP through the bridge script"
                        + " (contrib/meshcore-bridge, MIT, uses the meshcore Python package), which pushes contact,"
                        + " advert, self_info, channel_msg and contact_msg events to the meshcore-json ingest format."
                        + " The same records as meshcore-local: contacts and the companion as mesh-node, repeater health"
                        + " from adverts, messages on the in-process mesh message bus (records only with includeText)."
                        + " Authenticate with the ingestKey credential. Private.")
                .kinds(MeshRecords.NODE_KIND, MeshRecords.MESSAGE_KIND)
                .homepage("https://github.com/meshcore-dev/meshcore_py")
                .attribution("The operator's own MeshCore companion")
                .licence("Operator's own data (no upstream license)")
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(120, 50_000))
                .observedAtMode(ObservedAtMode.RECEIVED)
                .zeroResultsNormal(true)
                .privateSource(true)
                .credential(CredentialField.optional(SourceDescriptor.REMOTE_MEDIA_KEY, "The key the bridge presents"
                        + " (X-Foresignal-Ingest-Key or Bearer); choose a random value starting with eik_"))
                .param(ParamSpec.optional(MeshRecords.INCLUDE_TEXT, ParamType.BOOLEAN, "Keep channel and direct"
                        + " messages as mesh-message records (never embedded, never sent to an LLM)", false))
                .param(ParamSpec.optional(MeshCoreLocalAdapter.REPEATER_LATE_AFTER, ParamType.DURATION, "A repeater"
                        + " whose last advert is older is late", "13h"))
                .param(ParamSpec.optional(MeshCoreLocalAdapter.REPEATER_SILENT_AFTER, ParamType.DURATION, "A repeater"
                        + " whose last advert is older is silent", "26h"));
        MeshRecords.defaults(b, MeshRecords.TTL, MeshRecords.STREAM_KEYFRAME);
        return b.build();
    }
}
