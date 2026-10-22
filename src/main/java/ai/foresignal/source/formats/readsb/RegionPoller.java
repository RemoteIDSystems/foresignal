package ai.foresignal.source.formats.readsb;

import ai.foresignal.collect.UpstreamAccessDeniedException;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Polls a fixed set of {@link AircraftRegion circles} in one fetch and merges the answers by ICAO address, the
 * way Shadowbroker's aircraft feeds do: the first circle (in list order) that reports an aircraft keeps it, a
 * circle that fails contributes nothing, and the fetch fails only when every circle failed.
 *
 * <ul>
 *   <li>{@link #parallel}: every circle at once (the main adsb.lol feed);
 *   <li>{@link #serial}: one circle after another, with a pause between them (the blind-spot gap-fill, which
 *       bursts of parallel requests get throttled for). A refusal of this client stops it at once: the backoff
 *       it starts would hold back every later circle anyway.
 * </ul>
 *
 * <p>A result is complete when every circle answered and partial otherwise, so the aircraft of a circle that
 * failed keep their records until the TTL retires them. It carries {@code Coverage(actual = aircraft kept,
 * region = the envelope of every circle)}.
 */
public final class RegionPoller {

    /** Fetches and maps one circle. */
    @FunctionalInterface
    public interface RegionFetch {

        /**
         * Fetches {@code region} and maps the answer.
         *
         * @throws AdapterException when the upstream fails or refuses this client
         */
        ReadsbAircraftMapper.MapResult fetch(AircraftRegion region) throws AdapterException;
    }

    /** Waits between serial requests; replaceable in tests. */
    @FunctionalInterface
    public interface Pauser {

        /**
         * Waits {@code duration}.
         *
         * @throws InterruptedException when interrupted
         */
        void pause(Duration duration) throws InterruptedException;
    }

    /** The production {@link Pauser}. */
    public static final Pauser SLEEP = duration -> Thread.sleep(duration.toMillis());

    private RegionPoller() {}

    /**
     * Fetches every circle at once, one virtual thread each.
     *
     * @throws AdapterException the first circle's failure, in list order, when every circle failed
     */
    public static FetchResult parallel(FetchContext ctx, List<AircraftRegion> regions, RegionFetch fetch)
            throws AdapterException {
        List<Outcome> outcomes = new ArrayList<>(regions.size());
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ReadsbAircraftMapper.MapResult>> futures = new ArrayList<>(regions.size());
            for (AircraftRegion region : regions) {
                futures.add(pool.submit(() -> fetch.fetch(region)));
            }
            for (int i = 0; i < regions.size(); i++) {
                try {
                    outcomes.add(Outcome.of(regions.get(i), futures.get(i).get()));
                } catch (ExecutionException e) {
                    outcomes.add(Outcome.failed(regions.get(i), e.getCause()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    futures.forEach(future -> future.cancel(true));
                    throw new AdapterException(ctx.sourceId() + ": interrupted while fetching regions", e);
                }
            }
        }
        return merge(ctx, regions, outcomes);
    }

    /**
     * Fetches the circles one after another, pausing {@code pause} plus a uniform random {@code jitter} between
     * two requests (not after the last).
     *
     * @throws UpstreamAccessDeniedException as soon as the upstream refuses this client
     * @throws AdapterException the first circle's failure when every circle failed
     */
    public static FetchResult serial(FetchContext ctx, List<AircraftRegion> regions, Duration pause,
            Duration jitter, Pauser pauser, RegionFetch fetch) throws AdapterException {
        List<Outcome> outcomes = new ArrayList<>(regions.size());
        for (int i = 0; i < regions.size(); i++) {
            AircraftRegion region = regions.get(i);
            try {
                outcomes.add(Outcome.of(region, fetch.fetch(region)));
            } catch (UpstreamAccessDeniedException e) {
                throw e;
            } catch (AdapterException | RuntimeException e) {
                outcomes.add(Outcome.failed(region, e));
            }
            if (i < regions.size() - 1) {
                try {
                    pauser.pause(pause.plus(randomUpTo(jitter)));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AdapterException(ctx.sourceId() + ": interrupted between regions", e);
                }
            }
        }
        return merge(ctx, regions, outcomes);
    }

    /** A uniformly random duration from zero to {@code bound}, or zero when {@code bound} is zero. */
    static Duration randomUpTo(Duration bound) {
        long millis = bound == null ? 0 : bound.toMillis();
        return millis <= 0 ? Duration.ZERO : Duration.ofMillis(ThreadLocalRandom.current().nextLong(millis + 1));
    }

    private static FetchResult merge(FetchContext ctx, List<AircraftRegion> regions, List<Outcome> outcomes)
            throws AdapterException {
        Map<String, NormalizedRecord> byHex = new LinkedHashMap<>();
        List<Outcome> failed = new ArrayList<>();
        for (Outcome outcome : outcomes) {
            if (outcome.error != null) {
                failed.add(outcome);
                continue;
            }
            outcome.mapped.records().forEach(record -> byHex.putIfAbsent(record.externalId(), record));
            if (outcome.mapped.skipped() > 0) {
                ctx.log().debug("{} ({}): skipped {} aircraft without hex", ctx.sourceId(), outcome.region.name(),
                        outcome.mapped.skipped());
            }
        }
        if (failed.size() == outcomes.size() && !outcomes.isEmpty()) {
            Throwable first = failed.getFirst().error;
            if (first instanceof AdapterException adapter) {
                throw adapter;
            }
            if (first instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new AdapterException(ctx.sourceId() + ": every region failed", first);
        }
        for (Outcome outcome : failed) {
            ctx.log().warn("{} ({}): region failed, its aircraft keep their records: {}", ctx.sourceId(),
                    outcome.region.name(), outcome.error.toString());
        }
        List<NormalizedRecord> records = List.copyOf(byHex.values());
        FetchResult result = failed.isEmpty() ? FetchResult.complete(records) : FetchResult.of(records);
        return result.withCoverage(new Coverage(null, null, records.size(), AircraftRegion.envelopeOf(regions)));
    }

    /** One circle's answer or failure. */
    private record Outcome(AircraftRegion region, ReadsbAircraftMapper.MapResult mapped, Throwable error) {

        static Outcome of(AircraftRegion region, ReadsbAircraftMapper.MapResult mapped) {
            return new Outcome(region, mapped, null);
        }

        static Outcome failed(AircraftRegion region, Throwable error) {
            return new Outcome(region, null, error);
        }
    }
}
