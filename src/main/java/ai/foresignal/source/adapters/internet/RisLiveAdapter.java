package ai.foresignal.source.adapters.internet;

import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.ReconnectPolicy;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.spi.WebSocketOptions;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code ris-live} (prompt 092): a narrow RIPE RIS Live subscription. It watches a small set of prefixes (never
 * the full firehose), tracks per-peer visibility, and emits a {@code bgp-prefix-state} record when the banded
 * state changes or every {@value #EMIT_INTERVAL}; identical snapshots become touches.
 *
 * <p>Optional and disabled by default. The reconnect ladder is 5 s → 15 s → 60 s → 300 s with jitter, from the
 * supervisor; a server "too slow" error ends the session so the next rung is used.
 */
@Component
public final class RisLiveAdapter implements StreamingAdapter {

    /** The source id. */
    public static final String ID = "ris-live";

    /** The record kind. */
    public static final String KIND = "bgp-prefix-state";

    /** The default endpoint. */
    public static final String DEFAULT_URL = "wss://ris-live.ripe.net/v1/ws/";

    /** Test-only endpoint override. */
    public static final String URL_PROPERTY = "foresignal.test.adapters.ris-live.url";

    static final Duration EMIT_INTERVAL = Duration.ofMinutes(10);
    static final Duration SILENCE_RECYCLE = Duration.ofMinutes(5);
    static final Duration RETENTION = Duration.ofDays(1825);

    private final URI uri;
    private final SourceDescriptor descriptor;

    /** Creates the adapter (tests). */
    public RisLiveAdapter(String urlOverride) {
        this(urlOverride, null);
    }

    /** Creates the adapter. */
    @Autowired
    public RisLiveAdapter(@Value("${" + URL_PROPERTY + ":}") String urlOverride,
            ObjectProvider<InstallIdService> install) {
        String client = "foresignal";
        if (install != null) {
            InstallIdService service = install.getIfAvailable();
            if (service != null) {
                client = "foresignal-" + service.installId();
            }
        }
        String base = urlOverride == null || urlOverride.isBlank() ? DEFAULT_URL : urlOverride.strip();
        this.uri = URI.create(base + (base.contains("?") ? "&" : "?") + "client=" + client);
        this.descriptor = buildDescriptor();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws ai.foresignal.source.spi.AdapterException {
        ValidatedParams params = ctx.params();
        List<String> prefixes = params.stringList("prefixes");
        if (prefixes.isEmpty()) {
            throw new ai.foresignal.source.spi.AdapterException("ris-live needs at least one watched prefix");
        }
        Map<String, List<Integer>> expected = expectedOrigins(params.string("expectedOrigins"));
        RisLiveSession session = new RisLiveSession(this, ctx, sink, prefixes, expected, params.stringList("hosts"));
        ctx.http().openWebSocket(uri, session, Map.of(), WebSocketOptions.DEFAULT);
        session.startWatchdog();
        return session;
    }

    /** Parses {@code prefix=ASN,ASN;prefix2=ASN}. */
    static Map<String, List<Integer>> expectedOrigins(String value) {
        Map<String, List<Integer>> out = new LinkedHashMap<>();
        if (value == null || value.isBlank()) {
            return out;
        }
        for (String entry : value.split(";")) {
            int eq = entry.indexOf('=');
            if (eq < 0) {
                continue;
            }
            List<Integer> asns = new ArrayList<>();
            for (String part : entry.substring(eq + 1).split(",")) {
                try {
                    asns.add(Integer.parseInt(part.strip()));
                } catch (NumberFormatException ignored) {
                    // skip
                }
            }
            out.put(entry.substring(0, eq).strip(), asns);
        }
        return out;
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("RIPE RIS Live prefix monitor")
                .description("A narrow RIPE RIS Live subscription: per-peer visibility of a small set of watched "
                        + "prefixes. Never subscribes to the full firehose. Emits a bgp-prefix-state record when "
                        + "the banded state changes or every ten minutes.")
                .kind(KIND)
                .mode(Mode.STREAMING)
                .homepage("https://ris-live.ripe.net")
                .attribution("RIPE NCC / RIS")
                .licence("RIPE NCC RIS terms (attribution; commercial use by permission)")
                .termsUrl("https://www.ripe.net/manage-ips-and-asns/db/service-terms/")
                .param(ParamSpec.required("prefixes", ParamType.STRING_LIST, "Watched prefixes (max 50)"))
                .param(ParamSpec.optional("expectedOrigins", ParamType.STRING,
                        "Expected origins as prefix=ASN,ASN;prefix2=ASN", ""))
                .param(ParamSpec.optional("hosts", ParamType.STRING_LIST, "RRC hosts to subscribe to", List.of()))
                .reconnect(new ReconnectPolicy(Duration.ofSeconds(5), Duration.ofMinutes(5), 0.2))
                .defaultBudget(6, 144)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("Prefix {prefix} visibility", "communications-tower", "#1971C2",
                        null))
                .build();
    }
}
