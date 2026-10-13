package ai.foresignal.source.adapters.celestrak;

// Origin: an empty or error body is a failure, never data, after God's Eye View (MIT, Copyright (c) 2026
// Bilawal Sidhu): server/providers/space/celestrak.js (fetchUpstream). See THIRD_PARTY_NOTICES.md.

import ai.foresignal.source.formats.csv.CsvReader;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.space.OmmElements;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses CelesTrak GP data (prompt 064; {@code https://celestrak.org/NORAD/documentation/gp-data-formats.php}):
 * {@link #parseCsv} (RFC 4180, header-driven) and {@link #parseJson} produce the same element sets, and
 * {@link #record} turns one into a {@code satellite} record.
 *
 * <p>The 17 OMM columns: {@code OBJECT_NAME, OBJECT_ID, EPOCH} (no zone, so UTC), {@code MEAN_MOTION,
 * ECCENTRICITY, INCLINATION, RA_OF_ASC_NODE, ARG_OF_PERICENTER, MEAN_ANOMALY, EPHEMERIS_TYPE,
 * CLASSIFICATION_TYPE, NORAD_CAT_ID, ELEMENT_SET_NO, REV_AT_EPOCH, BSTAR, MEAN_MOTION_DOT, MEAN_MOTION_DDOT}.
 * Catalog numbers may have up to nine digits. A row that cannot be read is skipped and counted; a 200 with
 * zero objects for {@code active} is an {@link UpstreamFormatException} (a {@code FORMAT=json} response
 * once came back empty, which is why CSV is the default).
 */
public final class CelestrakGpParser {

    /** The record kind. */
    public static final String KIND = "satellite";

    /** The columns a row needs. */
    static final List<String> REQUIRED = List.of("OBJECT_NAME", "EPOCH", "MEAN_MOTION", "ECCENTRICITY",
            "INCLINATION", "RA_OF_ASC_NODE", "ARG_OF_PERICENTER", "MEAN_ANOMALY", "NORAD_CAT_ID");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * What a parse produced.
     *
     * @param elements the element sets, in upstream order
     * @param skipped rows that could not be read
     */
    public record Parsed(List<OmmElements> elements, int skipped) {

        /** Copies the list. */
        public Parsed {
            elements = List.copyOf(elements);
        }
    }

    private CelestrakGpParser() {}

    /**
     * Parses {@code FORMAT=CSV}.
     *
     * @param group the group requested; zero objects for {@code active} is a format error
     * @throws UpstreamFormatException when the header lacks a required column, or {@code active} is empty
     */
    public static Parsed parseCsv(byte[] body, String group) {
        List<OmmElements> out = new ArrayList<>();
        int skipped = 0;
        try (CsvReader csv = new CsvReader(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(body),
                StandardCharsets.UTF_8)))) {
            List<String> header = csv.next();
            if (header == null) {
                return empty(group, "");
            }
            Map<String, Integer> index = CsvReader.index(header);
            for (String column : REQUIRED) {
                if (!index.containsKey(column)) {
                    throw new UpstreamFormatException("CelesTrak CSV has no " + column + " column", String.join(",", header));
                }
            }
            for (List<String> row = csv.next(); row != null; row = csv.next()) {
                if (row.size() == 1 && row.get(0).isBlank()) {
                    continue;
                }
                Map<String, Object> values = new HashMap<>();
                for (String key : OmmElements.KEYS) {
                    String v = CsvReader.get(row, index, key);
                    if (v != null) {
                        values.put(key, v);
                    }
                }
                try {
                    out.add(OmmElements.fromPayload(values));
                } catch (IllegalArgumentException e) {
                    skipped++;
                }
            }
        } catch (IllegalArgumentException e) {
            throw new UpstreamFormatException("CelesTrak CSV could not be read: " + e.getMessage(),
                    snippet(body), e);
        } catch (java.io.IOException | java.io.UncheckedIOException e) {
            throw new UpstreamFormatException("CelesTrak CSV could not be read", snippet(body), e);
        }
        return result(out, skipped, group, body);
    }

    /**
     * Parses {@code FORMAT=JSON}: an array of OMM objects.
     *
     * @param group the group requested; zero objects for {@code active} is a format error
     * @throws UpstreamFormatException when the body is not a JSON array, or {@code active} is empty
     */
    public static Parsed parseJson(byte[] body, String group) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (RuntimeException e) {
            throw new UpstreamFormatException("CelesTrak JSON could not be parsed", snippet(body), e);
        }
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("CelesTrak JSON is not an array", snippet(body));
        }
        List<OmmElements> out = new ArrayList<>();
        int skipped = 0;
        for (JsonNode item : root) {
            if (!item.isObject()) {
                skipped++;
                continue;
            }
            Map<String, Object> values = new HashMap<>();
            for (String key : OmmElements.KEYS) {
                JsonNode v = item.get(key);
                if (v != null && !v.isNull()) {
                    values.put(key, v.isNumber() ? v.numberValue() : v.asString());
                }
            }
            try {
                out.add(OmmElements.fromPayload(values));
            } catch (IllegalArgumentException e) {
                skipped++;
            }
        }
        return result(out, skipped, group, body);
    }

    /** True when {@code body} looks like a CelesTrak CSV ({@code OBJECT_NAME} header) or JSON array. */
    public static boolean looksLike(byte[] body, String format) {
        String head = new String(body, 0, Math.min(body.length, 64), StandardCharsets.UTF_8).stripLeading();
        if (head.startsWith("﻿")) {
            head = head.substring(1);
        }
        return "json".equals(format) ? head.startsWith("[") : head.startsWith("OBJECT_NAME");
    }

    /**
     * The {@code satellite} record of one element set: the OMM keys verbatim, numbers as JSON numbers, plus
     * {@code groups}; {@code externalId} = {@code NORAD_CAT_ID}, {@code observedAt} = {@code EPOCH}, no event
     * time and no geometry.
     *
     * @param groups the sorted member groups the object belongs to
     */
    public static NormalizedRecord record(OmmElements e, List<String> groups) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(Long.toString(e.norad()), KIND)
                .observedAt(e.epoch())
                .putAll(e.toPayload());
        b.put("groups", groups == null ? List.of() : List.copyOf(groups));
        return b.build();
    }

    private static Parsed result(List<OmmElements> out, int skipped, String group, byte[] body) {
        if (out.isEmpty()) {
            return empty(group, snippet(body));
        }
        return new Parsed(out, skipped);
    }

    private static Parsed empty(String group, String snippet) {
        if (CelestrakGroups.ACTIVE.equals(group)) {
            throw new UpstreamFormatException("CelesTrak returned zero objects for GROUP=active", snippet);
        }
        return new Parsed(List.of(), 0);
    }

    private static String snippet(byte[] body) {
        return new String(body, 0, Math.min(body.length, 500), StandardCharsets.UTF_8);
    }
}
