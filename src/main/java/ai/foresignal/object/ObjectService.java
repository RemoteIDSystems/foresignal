package ai.foresignal.object;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Resolves object references to envelopes through the per-type {@link ObjectResolver}s and adds the caller's
 * {@code actions} ({@code getObject}, {@code resolveObjects}; docs/Client-Contract.md §3). Prompt 130's
 * {@link EnvelopeDecorator}s then add tags and TLP and drop work products the caller may not see.
 */
@Service
public class ObjectService {

    /** Most references in one batch resolve. */
    public static final int MAX_BATCH = 500;

    private static final Logger log = LoggerFactory.getLogger(ObjectService.class);

    private final Map<String, ObjectResolver> resolvers;
    private final Clock clock;
    private org.springframework.beans.factory.ObjectProvider<EnvelopeDecorator> decorators;

    /** Creates the service over every resolver bean. */
    public ObjectService(List<ObjectResolver> resolvers, Clock clock) {
        Map<String, ObjectResolver> byType = new HashMap<>();
        for (ObjectResolver r : resolvers) {
            if (byType.put(r.type(), r) != null) {
                throw new IllegalStateException("Two object resolvers for type " + r.type());
            }
        }
        this.resolvers = Map.copyOf(byType);
        this.clock = clock;
    }

    /**
     * Injects the envelope decorators of prompt 130 (tags, TLP, access-list visibility). Lazy, because the
     * decorators themselves resolve objects.
     */
    @org.springframework.beans.factory.annotation.Autowired
    public void setDecorators(org.springframework.beans.factory.ObjectProvider<EnvelopeDecorator> decorators) {
        this.decorators = decorators;
    }

    /** The types with a resolver. */
    public java.util.Set<String> resolvableTypes() {
        return resolvers.keySet();
    }

    /**
     * The envelope of {@code ref} with the caller's actions.
     *
     * @throws NotFoundException {@code UNKNOWN_OBJECT} when it is missing or invisible
     */
    public ObjectEnvelope get(ObjectRef ref, ResolveContext context) {
        return find(ref, context).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_OBJECT,
                "No object '" + ref + "' exists, or you may not see it."));
    }

    /** The envelope of {@code ref} with the caller's actions, or empty when missing or invisible. */
    public Optional<ObjectEnvelope> find(ObjectRef ref, ResolveContext context) {
        checkAsOf(context.asOf());
        ObjectResolver resolver = resolvers.get(ref.type());
        if (resolver == null) {
            return Optional.empty();
        }
        Optional<ObjectEnvelope> found = safe(() -> resolver.resolve(ref, context)).map(e -> e.withActions(
                ObjectActions.of(e, context.caller())));
        if (found.isEmpty()) {
            return found;
        }
        Map<ObjectRef, ObjectEnvelope> one = new LinkedHashMap<>();
        one.put(ref, found.get());
        return Optional.ofNullable(decorate(one, context).get(ref));
    }

    /** Applies every {@link EnvelopeDecorator}: tags, TLP and access-list visibility (prompt 130). */
    private Map<ObjectRef, ObjectEnvelope> decorate(Map<ObjectRef, ObjectEnvelope> envelopes, ResolveContext context) {
        if (decorators == null || envelopes.isEmpty()) {
            return envelopes;
        }
        Map<ObjectRef, ObjectEnvelope> out = envelopes;
        for (EnvelopeDecorator decorator : decorators.orderedStream().toList()) {
            out = decorator.decorate(out, context);
        }
        return out;
    }

    /**
     * A batch resolve result.
     *
     * @param items envelopes in request order
     * @param missing references that are missing or invisible
     * @param asOf the knowledge cutoff used, or null
     */
    public record Resolved(List<ObjectEnvelope> items, List<String> missing, Instant asOf) {}

    /**
     * Resolves up to {@value #MAX_BATCH} references; missing or invisible ones are listed, not errors.
     *
     * @throws ValidationException {@code OBJECT_REF_INVALID} for a malformed reference or too many
     */
    public Resolved resolve(List<String> refs, ResolveContext context) {
        if (refs == null || refs.isEmpty()) {
            throw ObjectRef.invalid("refs", "must hold at least one reference");
        }
        if (refs.size() > MAX_BATCH) {
            throw ObjectRef.invalid("refs", "holds more than " + MAX_BATCH + " references");
        }
        List<ObjectRef> parsed = new ArrayList<>();
        for (int i = 0; i < refs.size(); i++) {
            parsed.add(ObjectRef.parse(refs.get(i), "refs[" + i + "]"));
        }
        Map<ObjectRef, ObjectEnvelope> found = resolveRefs(new LinkedHashSet<>(parsed), context);
        List<ObjectEnvelope> items = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (ObjectRef ref : new LinkedHashSet<>(parsed)) {
            ObjectEnvelope e = found.get(ref);
            if (e == null) {
                missing.add(ref.format());
            } else {
                items.add(e);
            }
        }
        return new Resolved(items, missing, context.asOf());
    }

    /** Envelopes (with actions) for already-parsed references, grouped by type for batch reads. */
    public Map<ObjectRef, ObjectEnvelope> resolveRefs(java.util.Collection<ObjectRef> refs, ResolveContext context) {
        checkAsOf(context.asOf());
        Map<String, List<ObjectRef>> byType = new LinkedHashMap<>();
        for (ObjectRef ref : refs) {
            byType.computeIfAbsent(ref.type(), t -> new ArrayList<>()).add(ref);
        }
        Map<ObjectRef, ObjectEnvelope> out = new HashMap<>();
        for (Map.Entry<String, List<ObjectRef>> group : byType.entrySet()) {
            ObjectResolver resolver = resolvers.get(group.getKey());
            if (resolver == null) {
                continue;
            }
            Map<ObjectRef, ObjectEnvelope> part = safe(() -> Optional.of(resolver.resolveAll(group.getValue(),
                    context))).orElse(Map.of());
            part.forEach((ref, e) -> out.put(ref, e.withActions(ObjectActions.of(e, context.caller()))));
        }
        return decorate(out, context);
    }

    /** Rejects an {@code asOf} in the future (400 {@code ASOF_INVALID}). */
    public void checkAsOf(Instant asOf) {
        if (asOf != null && asOf.isAfter(clock.instant().plusSeconds(1))) {
            throw new ValidationException(ErrorCodes.ASOF_INVALID, "asOf is in the future.",
                    List.of(new FieldError("asOf", "must not be in the future")));
        }
    }

    private <T> Optional<T> safe(java.util.function.Supplier<Optional<T>> call) {
        try {
            return call.get();
        } catch (NotFoundException e) {
            return Optional.empty();
        } catch (ValidationException e) {
            throw e;
        } catch (RuntimeException e) {
            log.warn("Object resolver failed: {}", e.toString());
            return Optional.empty();
        }
    }
}
