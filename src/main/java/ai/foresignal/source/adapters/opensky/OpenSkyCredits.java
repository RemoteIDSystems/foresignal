/*
 * The credit governor follows God's Eye View, server/providers/aircraft/opensky.js (the credit governor
 * reading x-rate-limit-remaining, openskyAdaptiveTtlMs), https://github.com/bilawalsidhu/gods-eye-view,
 * commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; instead of stretching a cache TTL by remaining-credit tiers, the remaining count is kept in
 * adapter state and a fetch is deferred when it would not leave the reserve; the cost follows OpenSky's
 * published bbox-area table.
 */
package ai.foresignal.source.adapters.opensky;

import ai.foresignal.source.spi.AdapterStateStore;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.locationtech.jts.geom.Envelope;

/**
 * OpenSky's API credits (prompt 062; verified 2026-10 against the REST documentation).
 *
 * <p>Daily credits per endpoint family: 400 anonymous, 4,000 standard, 8,000 for an active feeder; a
 * licensed account gets 14,400 (refilled hourly, per the same page). The cost of one {@code /states/all}
 * depends on the bounding box area (latitude range × longitude range, in square degrees): ≤ 25 → 1,
 * ≤ 100 → 2, ≤ 400 → 3, larger or the whole world → 4.
 *
 * <p>After every response the adapter stores {@code X-Rate-Limit-Remaining} and its time
 * ({@link #record}); before a fetch, {@link #deferral} says whether today's stored balance (UTC) would fall
 * below the call's cost plus the reserve.
 */
final class OpenSkyCredits {

    /** Adapter-state key of the last {@code X-Rate-Limit-Remaining}. */
    static final String REMAINING_KEY = "credits.remaining";

    /** Adapter-state key of when it was read. */
    static final String AT_KEY = "credits.at";

    /** The response header. */
    static final String REMAINING_HEADER = "x-rate-limit-remaining";

    /** The cost of a query of the whole world. */
    static final int WORLD_COST = 4;

    private OpenSkyCredits() {}

    /** The credits one {@code /states/all} costs for {@code bbox}; null means the world. */
    static int cost(Envelope bbox) {
        if (bbox == null || bbox.isNull()) {
            return WORLD_COST;
        }
        double area = bbox.getWidth() * bbox.getHeight();
        if (area <= 25.0) {
            return 1;
        }
        if (area <= 100.0) {
            return 2;
        }
        if (area <= 400.0) {
            return 3;
        }
        return WORLD_COST;
    }

    /** Stores the header's value and {@code now}; a missing or unreadable header changes nothing. */
    static void record(AdapterStateStore state, String headerValue, Instant now) {
        if (headerValue == null || headerValue.isBlank()) {
            return;
        }
        try {
            long remaining = Long.parseLong(headerValue.trim());
            state.putDurably(REMAINING_KEY, Long.toString(remaining));
            state.putDurably(AT_KEY, now.toString());
        } catch (NumberFormatException e) {
            // An unreadable header leaves the last good reading in place.
        }
    }

    /** The stored balance when it was read today (UTC), else empty. */
    static Optional<Long> remainingToday(AdapterStateStore state, Instant now) {
        Optional<String> at = state.get(AT_KEY);
        Optional<String> remaining = state.get(REMAINING_KEY);
        if (at.isEmpty() || remaining.isEmpty()) {
            return Optional.empty();
        }
        try {
            LocalDate readOn = Instant.parse(at.get()).atZone(ZoneOffset.UTC).toLocalDate();
            if (!readOn.equals(now.atZone(ZoneOffset.UTC).toLocalDate())) {
                return Optional.empty();
            }
            return Optional.of(Long.parseLong(remaining.get()));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * When a fetch costing {@code cost} must wait: today's stored balance is below {@code cost + reserve}.
     *
     * @return the next UTC midnight, when the daily credits refill; empty when the fetch may go ahead
     */
    static Optional<Instant> deferral(AdapterStateStore state, Instant now, int cost, int reserve) {
        Optional<Long> remaining = remainingToday(state, now);
        if (remaining.isEmpty() || remaining.get() >= (long) cost + reserve) {
            return Optional.empty();
        }
        return Optional.of(now.atZone(ZoneOffset.UTC).toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
    }
}
