/*
 * The "now in seconds or milliseconds" rule is ported in part from God's Eye View,
 * src/data/adsbLolFallback.js (normalizeAdsbLolPointResponse), https://github.com/bilawalsidhu/gods-eye-view,
 * commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; the threshold is 1e11 (prompt 062); fractional seconds are kept; a missing or
 * non-numeric now is a format error instead of falling back to the local clock.
 */
package ai.foresignal.source.formats.readsb;

import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The top level of a readsb-format aircraft response (prompt 062): adsb.lol's v2 API ({@code ac}) or a
 * local readsb/dump1090 {@code aircraft.json} ({@code aircraft}).
 *
 * <p>{@code now} is the time the upstream assembled the snapshot: seconds since the epoch (readsb, possibly
 * fractional) or milliseconds (adsb.lol sends milliseconds; verified 2026-10-13, {@code now: 1791894193500}).
 * A value above {@value #MILLIS_THRESHOLD} is read as milliseconds. It is the base of every record's
 * {@code observedAt} ({@code now − seen}, Core-Data-Model §2), so a response without it is a format error.
 *
 * @param aircraft the entries of {@code ac} or {@code aircraft}, in response order
 * @param now the snapshot time
 * @param total adsb.lol's {@code total}, or null when absent
 * @param ctime adsb.lol's {@code ctime} (cache time), or null when absent
 */
public record ReadsbDocument(List<JsonNode> aircraft, Instant now, Integer total, Instant ctime) {

    /** Epoch values above this are milliseconds (about the year 5138 in seconds, 1973 in milliseconds). */
    public static final double MILLIS_THRESHOLD = 1e11;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** Copies the list. */
    public ReadsbDocument {
        aircraft = aircraft == null ? List.of() : List.copyOf(aircraft);
    }

    /**
     * Parses a response body.
     *
     * @throws UpstreamFormatException when the body is not a JSON object with an {@code ac} or
     *     {@code aircraft} array and a numeric {@code now}
     */
    public static ReadsbDocument parse(byte[] body) {
        if (body == null || body.length == 0) {
            throw new UpstreamFormatException("readsb response is empty", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("readsb response is not JSON: " + e.getOriginalMessage(),
                    new String(body, StandardCharsets.UTF_8), e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("readsb response is not a JSON object", snippet(body));
        }
        JsonNode list = root.has("ac") ? root.get("ac") : root.get("aircraft");
        if (list == null || !list.isArray()) {
            throw new UpstreamFormatException("readsb response has no ac or aircraft array", snippet(body));
        }
        JsonNode now = root.get("now");
        if (now == null || !now.isNumber()) {
            throw new UpstreamFormatException("readsb response has no numeric now", snippet(body));
        }
        List<JsonNode> entries = new ArrayList<>(list.size());
        for (JsonNode entry : list) {
            entries.add(entry);
        }
        JsonNode total = root.get("total");
        JsonNode ctime = root.get("ctime");
        return new ReadsbDocument(
                entries,
                epoch(now.doubleValue()),
                total != null && total.isIntegralNumber() ? total.intValue() : null,
                ctime != null && ctime.isNumber() ? epoch(ctime.doubleValue()) : null);
    }

    /** An epoch value in seconds, or in milliseconds when above {@link #MILLIS_THRESHOLD}. */
    public static Instant epoch(double value) {
        double millis = value > MILLIS_THRESHOLD ? value : value * 1000.0;
        return Instant.ofEpochMilli(Math.round(millis));
    }

    private static String snippet(byte[] body) {
        return new String(body, 0, Math.min(body.length, 1000), StandardCharsets.UTF_8);
    }
}
