package ai.foresignal.exchange;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code remote:{instanceId}/{type}:{id}} (prompt 132, contract 1.3): read-only material received from
 * another instance, from the local snapshot taken at import. The envelope keeps the sender's title, summary, status,
 * time and point, carries the package's TLP label and links to the received package; it resolves only for callers who
 * may see a package that holds it (its named recipients; RED to them alone). It is never a local analytic object:
 * nothing about it enters evidence groups or the ledger, and copying it into an investigation is an explicit act.
 */
@Component
public class RemoteObjectResolver implements ObjectResolver {

    private final ReceivedObjectRepository objects;
    private final ReceivedPackageRepository received;

    /** Creates the resolver. */
    public RemoteObjectResolver(ReceivedObjectRepository objects, ReceivedPackageRepository received) {
        this.objects = objects;
        this.received = received;
    }

    @Override
    public String type() {
        return ObjectTypes.REMOTE;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        for (ReceivedObjectEntity o : objects.findByRemoteRef(ref.format())) {
            Optional<ReceivedPackageEntity> rp = received.findById(o.getReceivedId())
                    .filter(p -> ExchangeInbox.visible(p, context.caller()))
                    .filter(p -> context.existedAt(p.getReceivedAt()));
            if (rp.isPresent()) {
                return Optional.of(envelope(ref, o.getEnvelope(), rp.get()));
            }
        }
        return Optional.empty();
    }

    /** The envelope of a received object. */
    static ObjectEnvelope envelope(ObjectRef ref, Map<String, Object> env, ReceivedPackageEntity rp) {
        String sender = rp.getSender() == null ? ref.remoteInstanceId()
                : String.valueOf(rp.getSender().getOrDefault("instanceName",
                        rp.getSender().get("instanceId")));
        if ("null".equals(sender)) {
            sender = ref.remoteInstanceId();
        }
        Object summary = env.get("summary");
        ObjectEnvelope.Builder b = ObjectEnvelope.builder(ref)
                .title(env.get("title") instanceof String t ? t : ref.remoteRef())
                .summary("Received from " + sender + (summary instanceof String s && !s.isBlank() ? ": " + s : ""))
                .status(env.get("status") instanceof String s ? s : "received")
                .severity(env.get("severity") instanceof String s ? s : null)
                .tlp(rp.getTlp())
                .provenance(List.of(), null, null)
                .self("/v1/exchange/received/" + rp.getId());
        if (env.get("time") instanceof Map<?, ?> time) {
            b.time(instant(time.get("start")), instant(time.get("end")), instant(time.get("observedAt")));
        } else {
            b.time(rp.getReceivedAt(), null, rp.getReceivedAt());
        }
        if (env.get("bbox") instanceof List<?> box && box.size() == 4 && box.stream().allMatch(Number.class::isInstance)) {
            double w = ((Number) box.get(0)).doubleValue();
            double s = ((Number) box.get(1)).doubleValue();
            double e = ((Number) box.get(2)).doubleValue();
            double n = ((Number) box.get(3)).doubleValue();
            b.point((w + e) / 2, (s + n) / 2);
        }
        return b.build();
    }

    private static Instant instant(Object value) {
        if (!(value instanceof String s)) {
            return null;
        }
        try {
            return Instant.parse(s);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
