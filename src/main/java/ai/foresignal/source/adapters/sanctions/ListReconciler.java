package ai.foresignal.source.adapters.sanctions;

import ai.foresignal.notify.SystemEvent;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.NormalizedRecord;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.springframework.context.ApplicationEventPublisher;

/**
 * The delisting step shared by the three sanctions lists (prompt 091, FR-15): compares a run's entry ids with
 * the previous run's, adds a tombstone for each id that disappeared, and guards against an implausible shrink.
 *
 * <p>When the new entry count is below {@value DelistingGuard#MIN_RETAINED_RATIO} of the previous one and the
 * operator did not release the guard ({@link FetchContext#allowShrink()}), no tombstone is written, the
 * <strong>previous id set is kept</strong> (so a later complete list still sees what disappeared), the source
 * is degraded with a {@link DelistingGuard#SHRINK_REASON} reason, and a {@value #SHRINK_EVENT} system
 * notification is published.
 *
 * <p>The id set is stored in adapter state gzip-compressed and Base64-encoded behind the {@value #GZIP_PREFIX}
 * prefix; a value stored by an earlier build as plain newline-separated text is still read.
 */
final class ListReconciler {

    /** The system-notification subtype raised when the guard trips. */
    static final String SHRINK_EVENT = "system.sanctions_list_shrink";

    /** The prefix of a compressed id set in adapter state. */
    static final String GZIP_PREFIX = "gz:";

    private final ApplicationEventPublisher events;

    /**
     * Creates the reconciler.
     *
     * @param events publishes the shrink notification; null publishes nothing (tests)
     */
    ListReconciler(ApplicationEventPublisher events) {
        this.events = events;
    }

    /**
     * One list's reconciliation result.
     *
     * @param records the run's records plus any tombstones
     * @param tripped true when the shrink guard held the tombstones back
     * @param reason the degraded reason when tripped, else null
     */
    record Outcome(List<NormalizedRecord> records, boolean tripped, String reason) {}

    /**
     * Reconciles one list.
     *
     * @param ctx the fetch, for its state, clock, override and logger
     * @param idsKey the adapter-state key of this list's previous id set
     * @param listId the list label for messages ({@code SDN}, {@code UK}, …)
     * @param records the records of the complete list just parsed
     */
    Outcome reconcile(FetchContext ctx, String idsKey, String listId, List<NormalizedRecord> records) {
        Set<String> previous = decode(ctx.state().get(idsKey).orElse(null));
        Set<String> current = new LinkedHashSet<>();
        for (NormalizedRecord record : records) {
            current.add(record.externalId());
        }
        List<NormalizedRecord> out = new ArrayList<>(records);
        if (DelistingGuard.implausible(previous.size(), current.size()) && !ctx.allowShrink()) {
            String reason = DelistingGuard.SHRINK_REASON + ": the " + listId + " list has " + current.size()
                    + " entries, below 90% of the previous " + previous.size()
                    + "; no delistings were written. Verify upstream, then fetch with allowShrink=true.";
            ctx.log().warn("{}: {}", ctx.sourceId(), reason);
            publish(ctx.sourceId(), listId, previous.size(), current.size(), reason);
            return new Outcome(out, true, reason);
        }
        for (String id : DelistingGuard.gone(previous, current)) {
            out.add(NormalizedRecord.tombstone(id, Sanctions.KIND, ctx.now()));
        }
        ctx.state().put(idsKey, encode(current));
        return new Outcome(out, false, null);
    }

    /**
     * Applies a run's outcomes to the source health: degraded with the first tripped reason, else a reason this
     * guard set earlier is cleared (another component's degraded reason is left alone).
     */
    static void applyHealth(AdapterStateStore state, Collection<Outcome> outcomes) {
        for (Outcome outcome : outcomes) {
            if (outcome.tripped()) {
                state.degrade(outcome.reason());
                return;
            }
        }
        String current = state.get(AdapterStateStore.DEGRADED_REASON_KEY).orElse(null);
        if (current != null && current.startsWith(DelistingGuard.SHRINK_REASON)) {
            state.clearDegraded();
        }
    }

    private void publish(String sourceId, String listId, int previous, int current, String reason) {
        if (events == null) {
            return;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sourceId", sourceId);
        data.put("list", listId);
        data.put("previousCount", previous);
        data.put("currentCount", current);
        try {
            events.publishEvent(new SystemEvent(SHRINK_EVENT, "warning",
                    "Sanctions list " + listId + " shrank implausibly",
                    ai.foresignal.notify.MarkdownRenderer.escapeMarkdown(reason),
                    "/v1/sources/" + sourceId + "/health", data));
        } catch (RuntimeException e) {
            // a notification failure must not fail the fetch; the degraded health still shows the guard
        }
    }

    /** The compressed form of {@code ids} (sorted, newline-joined, gzip, Base64, {@value #GZIP_PREFIX}). */
    static String encode(Collection<String> ids) {
        byte[] plain = String.join("\n", DelistingGuard.sorted(ids)).getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
            gzip.write(plain);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return GZIP_PREFIX + Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    /**
     * The id set of a stored value: compressed, or the plain newline text of an earlier build; empty for null or a
     * corrupt value.
     */
    static Set<String> decode(String stored) {
        Set<String> out = new LinkedHashSet<>();
        if (stored == null || stored.isBlank()) {
            return out;
        }
        String text = stored;
        if (stored.startsWith(GZIP_PREFIX)) {
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(
                    Base64.getDecoder().decode(stored.substring(GZIP_PREFIX.length()))))) {
                text = new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException | IllegalArgumentException e) {
                // A corrupt value reads as no previous run: nothing is tombstoned, and the next run stores a
                // fresh set. Failing every fetch on it would be worse.
                return out;
            }
        }
        text.lines().filter(line -> !line.isBlank()).forEach(out::add);
        return out;
    }

    /** Previous-set helper for tests: the ids stored under {@code idsKey}. */
    static Set<String> stored(AdapterStateStore state, String idsKey) {
        return decode(state.get(idsKey).orElse(null));
    }
}
