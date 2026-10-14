package ai.foresignal.source.adapters.mesh;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PushAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * {@code mesh-reports} (prompt 084): field reports sent over the operator's own private mesh channel
 * ({@code #E R <TYPE> [lat,lon | @place] <text>}) by allowlisted senders, recorded by the mesh channels' report
 * service. A built-in PUSH source: Foresignal itself writes it; kind {@value #KIND}, private, received times.
 *
 * <p>The payload is {@code type}, {@code text} (at most 200 characters), {@code reporter} (the sender id),
 * {@code network} and {@code channel}; the observer is the reporting node with its RSSI and SNR. Report text is
 * untrusted: it is never embedded (no {@code embedFields}, no search fields), and Ask and briefings quote it only as
 * data. Retention is the {@code event} class (1825 d).
 */
@Component
public class MeshReportsAdapter implements PushAdapter {

    /** The source id. */
    public static final String ID = "mesh-reports";

    /** The one kind. */
    public static final String KIND = "field-report";

    private static final Set<String> KINDS = Set.of(KIND);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    /** Creates the adapter. */
    public MeshReportsAdapter() {}

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Mesh field reports")
                .description("Field reports (#E R <TYPE> ...) sent over the operator's own private Meshtastic or"
                        + " MeshCore channel by allowlisted senders and recorded by the mesh channels (prompt 084)."
                        + " Types FIRE, SMOKE, FLOOD, POWER, ROAD, MED, WX, SEC and OTHER. The text is untrusted:"
                        + " never embedded, quoted only as data. Private.")
                .kind(KIND)
                .attribution("The operator's own mesh")
                .licence("Operator's own data (no upstream license)")
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 5_000))
                .observedAtMode(ObservedAtMode.RECEIVED)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(Duration.ofDays(1825))
                .zeroResultsNormal(true)
                .privateSource(true)
                .titleField("type")
                .tileProperties("type", "reporter");
        b.fieldSpec("type", new FieldSpec(FieldType.STRING, null, "Type", "FIRE, SMOKE, FLOOD, POWER, ROAD, MED, WX, SEC"
                + " or OTHER", null, KINDS));
        b.fieldSpec("text", new FieldSpec(FieldType.STRING, null, "Report", "The reporter's text (untrusted, at most"
                + " 200 characters)", null, KINDS));
        b.fieldSpec("reporter", new FieldSpec(FieldType.STRING, null, "Reporter", "The sender: a Meshtastic node id, a"
                + " MeshCore key prefix or name:<advertised name>", null, KINDS));
        b.fieldSpec("network", new FieldSpec(FieldType.STRING, null, "Network", "meshtastic or meshcore", null, KINDS));
        b.fieldSpec("channel", new FieldSpec(FieldType.INTEGER, null, "Channel", "The private channel index it arrived"
                + " on", null, KINDS));
        return b.build();
    }
}
