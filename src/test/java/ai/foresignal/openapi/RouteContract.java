package ai.foresignal.openapi;

import ai.foresignal.testsupport.openapi.OpenApiSpec;
import ai.foresignal.testsupport.openapi.OpenApiSpec.SpecOperation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compares the routes of the authored spec with those springdoc generates from the code (prompt
 * 020, Architecture §12 "OpenAPI contract").
 *
 * <p>Routes are keyed by {@code (METHOD, path template)} with path variable names normalized away.
 * The authored routes minus the pending ones must equal the generated routes, in both directions;
 * operationIds must match per route; and every 2xx status the spec documents must also be
 * generated, except where {@link #STATUS_EXCEPTIONS} says springdoc cannot infer it.
 */
final class RouteContract {

    /**
     * Operations whose authored 2xx codes springdoc cannot infer, with the reason. springdoc reads
     * {@code @ResponseStatus}; a status chosen at run time through {@code ResponseEntity} is
     * invisible to it and is reported as {@code 200}. The ITs of each controller assert the real
     * status codes.
     */
    static final Map<String, String> STATUS_EXCEPTIONS = Map.ofEntries(
            // ResponseEntity.created(...): 201 with a Location header (017).
            Map.entry("createTrigger", "ResponseEntity.created"),
            // ResponseEntity.noContent(): 204 (017).
            Map.entry("deleteTrigger", "ResponseEntity.noContent"),
            // ResponseEntity.created(...): 201 with a Location header (018).
            Map.entry("createChannel", "ResponseEntity.created"),
            // ResponseEntity.noContent(): 204 (018).
            Map.entry("deleteChannel", "ResponseEntity.noContent"),
            // Checkpoint 041: the push controllers (031, 032) choose their statuses the same way;
            // PushSubscriptionsIT, PushProvidersIT and PushDevicesIT assert the real codes.
            Map.entry("createPushSubscription", "ResponseEntity.created"),
            Map.entry("deletePushSubscription", "ResponseEntity.noContent"),
            Map.entry("putPushProvider", "ResponseEntity: 201 when created, 200 when replaced"),
            Map.entry("deletePushProvider", "ResponseEntity.noContent"),
            Map.entry("registerPushDevice", "ResponseEntity: 201 when registered, 200 when re-registered"),
            Map.entry("deletePushDevice", "ResponseEntity.noContent"),
            // Checkpoint 057: a tile is 200, or 204 for an empty tile with ?empty=204 (045, ProductTilesIT); the
            // remote uploads answer 202 Accepted through ResponseEntity.status (054, RemoteIngestIT).
            Map.entry("getProductTile", "ResponseEntity: 204 for an empty tile with ?empty=204"),
            Map.entry("ingestSourceMedia", "ResponseEntity.status(ACCEPTED)"),
            Map.entry("ingestSourceRasters", "ResponseEntity.status(ACCEPTED)"),
            // Prompt 141: an FDSN query with no matching events is 204 (FDSN convention), chosen at run time; FdsnEventIT
            // asserts it.
            Map.entry("queryFdsnEvents", "ResponseEntity.noContent when no event matches"));

    private RouteContract() {}

    /**
     * The differences between the specification and the code; {@link #isEmpty()} when they agree.
     *
     * @param missingInCode authored, not pending, but not generated
     * @param missingInSpec generated but not authored at all
     * @param stalePending pending but generated: the operation is implemented
     * @param operationIdMismatches routes whose operationIds differ
     * @param statusMismatches routes missing an authored 2xx code in the generated spec
     */
    record Report(
            List<String> missingInCode,
            List<String> missingInSpec,
            List<String> stalePending,
            List<String> operationIdMismatches,
            List<String> statusMismatches) {

        /** True when code and spec agree. */
        boolean isEmpty() {
            return missingInCode.isEmpty() && missingInSpec.isEmpty() && stalePending.isEmpty()
                    && operationIdMismatches.isEmpty() && statusMismatches.isEmpty();
        }

        /** A readable diff, one section per kind of difference. */
        String describe() {
            StringBuilder out = new StringBuilder("OpenAPI contract differences:\n");
            section(out, "Missing in code (authored, not pending, not implemented)", missingInCode);
            section(out, "Missing in spec (implemented, not in docs/OpenAPI.json)", missingInSpec);
            section(out, "Stale pending entries (implemented: remove it from pending-operations.txt)", stalePending);
            section(out, "operationId mismatches", operationIdMismatches);
            section(out, "2xx status codes missing from the generated spec", statusMismatches);
            return out.toString();
        }

        private static void section(StringBuilder out, String title, List<String> lines) {
            if (lines.isEmpty()) {
                return;
            }
            out.append("\n").append(title).append(" (").append(lines.size()).append("):\n");
            lines.forEach(line -> out.append("  - ").append(line).append('\n'));
        }
    }

    /**
     * Compares the routes.
     *
     * @param authored the authored routes
     * @param pending operationIds not implemented yet
     * @param generated the routes generated from the code
     */
    static Report compare(List<SpecOperation> authored, Set<String> pending, List<SpecOperation> generated) {
        Map<String, SpecOperation> authoredByKey = byKey(authored);
        Map<String, SpecOperation> generatedByKey = byKey(generated);
        List<String> missingInCode = new ArrayList<>();
        List<String> missingInSpec = new ArrayList<>();
        List<String> stale = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        List<String> statuses = new ArrayList<>();

        for (Map.Entry<String, SpecOperation> entry : authoredByKey.entrySet()) {
            SpecOperation spec = entry.getValue();
            SpecOperation code = generatedByKey.get(entry.getKey());
            boolean isPending = pending.contains(spec.operationId());
            if (code == null) {
                if (!isPending) {
                    missingInCode.add(spec.label());
                }
                continue;
            }
            if (isPending) {
                stale.add(spec.operationId() + " (" + entry.getKey() + ") is implemented: remove it from pending-operations.txt");
            }
            if (!spec.operationId().equals(code.operationId())) {
                ids.add(entry.getKey() + ": spec " + spec.operationId() + ", code " + code.operationId());
            }
            Set<String> wanted = OpenApiSpec.successCodes(spec.node());
            Set<String> have = OpenApiSpec.successCodes(code.node());
            if (!have.containsAll(wanted) && !STATUS_EXCEPTIONS.containsKey(spec.operationId())) {
                statuses.add(spec.label() + ": spec " + wanted + ", code " + have);
            }
        }
        for (Map.Entry<String, SpecOperation> entry : generatedByKey.entrySet()) {
            if (!authoredByKey.containsKey(entry.getKey())) {
                missingInSpec.add(entry.getValue().label());
            }
        }
        return new Report(missingInCode, missingInSpec, stale, ids, statuses);
    }

    private static Map<String, SpecOperation> byKey(List<SpecOperation> operations) {
        Map<String, SpecOperation> out = new LinkedHashMap<>();
        for (SpecOperation op : operations) {
            out.put(OpenApiSpec.routeKey(op.path(), op.method()), op);
        }
        return out;
    }
}
