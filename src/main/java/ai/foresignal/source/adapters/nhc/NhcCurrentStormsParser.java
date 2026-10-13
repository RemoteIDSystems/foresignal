// Origin: validation rules ported from God's Eye View server/providers/cyclones.js (parseCycloneStatus: the
// activeStorms cap of 32, the (al|ep|cp)NNNNNN id pattern, unique ids, sanitized strings, the advisory-number
// pattern and its normalization, numbers accepted as strings or numbers within a range, nhc.noaa.gov-only links),
// commit 591f299d11f38a612629a274463196d57ae3862e, MIT License, Copyright (c) 2026 Bilawal Sidhu. See
// THIRD_PARTY_NOTICES.md.
package ai.foresignal.source.adapters.nhc;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses NHC's {@code CurrentStorms.json} into {@code cyclone} records (prompt 067).
 *
 * <p>A pure function of the bytes (adapter rule 3). The document is {@code {activeStorms: [...]}}, the
 * complete set of storms NHC is advising on, with at most {@value #MAX_STORMS} entries.
 *
 * <p><strong>All or nothing.</strong> The adapter tombstones storms that leave the set, so a document that
 * cannot be trusted as a whole must not produce a partial set: a missing {@code activeStorms} array, more
 * than {@value #MAX_STORMS} entries, an entry whose {@code id} does not match
 * {@code ^(al|ep|cp)\d{6}$}, a duplicate id, or an unreadable {@code lastUpdate} (the record's
 * {@code observedAt}) rejects the whole document with {@link UpstreamFormatException}. Every other field is
 * read defensively and left out when it is missing or unreadable, and any product object may be null.
 *
 * <p><strong>Upstream quirks (verified 2026-10).</strong> {@code intensity} and {@code pressure} arrive as
 * strings ({@code "80"}, {@code "977"}) although the 2019 reference says numbers, so both forms are read;
 * coordinates are {@code latitudeNumeric}/{@code longitudeNumeric} (camelCase; the reference's snake_case
 * names are read as a fallback); {@code movementSpeed} is in <strong>miles per hour</strong> (the
 * reference: "Present storm movement speed (miles per hour)") and is converted to knots with one decimal.
 * {@code advNum} is a string such as {@code "024"} or {@code "024A"}; it is stored without leading zeros
 * and upper-cased ({@code "24A"}).
 *
 * <p>Links are kept only when they are {@code https://www.nhc.noaa.gov/} URLs without credentials, query or
 * fragment; anything else is dropped. Strings are stripped of control characters and {@code <>}, and capped
 * at {@value #MAX_TEXT} characters.
 */
public final class NhcCurrentStormsParser {

    /** The record kind. */
    public static final String KIND = "cyclone";

    /** The most storms one document may list. */
    public static final int MAX_STORMS = 32;

    /** The longest string kept, in characters. */
    public static final int MAX_TEXT = 80;

    /** Knots per mile per hour. */
    public static final double KT_PER_MPH = 0.868976241900648;

    /** Storm id pattern: basin and eight characters. */
    public static final Pattern ID = Pattern.compile("^(al|ep|cp)\\d{6}$");

    /** Advisory number pattern: up to three digits and an optional letter. */
    static final Pattern ADVISORY = Pattern.compile("^\\d{1,3}[A-Za-z]?$");

    /** The NHC classification abbreviations, from the 2019 JSON reference. */
    public static final Map<String, String> CLASSIFICATIONS = Map.of(
            "TD", "Tropical Depression",
            "STD", "Subtropical Depression",
            "TS", "Tropical Storm",
            "HU", "Hurricane",
            "STS", "Subtropical Storm",
            "PTC", "Post-tropical Cyclone",
            "TY", "Typhoon",
            "PC", "Potential Tropical Cyclone");

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Pattern UNSAFE = Pattern.compile("[\\p{Cntrl}<>]");

    private NhcCurrentStormsParser() {}

    /**
     * Parses one document.
     *
     * @return one record per active storm, in document order; empty out of season
     * @throws UpstreamFormatException when the document cannot be trusted as a whole (see the class comment)
     */
    public static List<NormalizedRecord> parse(byte[] json) {
        if (json == null || json.length == 0) {
            throw new UpstreamFormatException("NHC CurrentStorms.json is empty", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("NHC CurrentStorms.json is not valid JSON: " + e.getOriginalMessage(),
                    snippet(json), e);
        }
        JsonNode storms = root == null ? null : root.get("activeStorms");
        if (storms == null || !storms.isArray()) {
            throw new UpstreamFormatException("NHC CurrentStorms.json has no activeStorms array", snippet(json));
        }
        if (storms.size() > MAX_STORMS) {
            throw new UpstreamFormatException("NHC CurrentStorms.json lists " + storms.size() + " storms, more than "
                    + MAX_STORMS, snippet(json));
        }
        Set<String> seen = new HashSet<>();
        List<NormalizedRecord> out = new ArrayList<>();
        for (JsonNode storm : storms) {
            String id = storm == null ? null : string(storm.get("id"));
            if (id == null || !ID.matcher(id).matches()) {
                throw new UpstreamFormatException("NHC storm id '" + id + "' is not (al|ep|cp) and six digits",
                        snippet(json));
            }
            if (!seen.add(id)) {
                throw new UpstreamFormatException("NHC storm id '" + id + "' appears twice", snippet(json));
            }
            Instant lastUpdate = instant(storm.get("lastUpdate"));
            if (lastUpdate == null) {
                throw new UpstreamFormatException("NHC storm " + id + " has no readable lastUpdate", snippet(json));
            }
            out.add(record(storm, id, lastUpdate));
        }
        return out;
    }

    private static NormalizedRecord record(JsonNode storm, String id, Instant lastUpdate) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND).observedAt(lastUpdate);
        Double lat = number(first(storm, "latitudeNumeric", "latitude_numeric"), -90, 90);
        Double lon = number(first(storm, "longitudeNumeric", "longitude_numeric"), -180, 180);
        if (lat != null && lon != null) {
            b.point(lon, lat);
        }
        String classification = text(storm.get("classification"), 16);
        b.put("name", text(storm.get("name"), MAX_TEXT));
        b.put("classification", classification);
        b.put("classificationLabel", classification == null
                ? null : CLASSIFICATIONS.get(classification.toUpperCase(Locale.ROOT)));
        b.put("basin", id.substring(0, 2).toUpperCase(Locale.ROOT));
        b.put("binNumber", text(storm.get("binNumber"), 8));
        b.put("intensityKt", integer(number(storm.get("intensity"), 0, 300)));
        b.put("pressureMb", integer(number(storm.get("pressure"), 800, 1100)));
        b.put("movementDirDeg", integer(number(storm.get("movementDir"), 0, 360)));
        Double mph = number(storm.get("movementSpeed"), 0, 250);
        b.put("movementSpeedKt", mph == null ? null : Math.round(mph * KT_PER_MPH * 10) / 10.0);
        JsonNode forecast = storm.get("forecastAdvisory");
        if (forecast != null && forecast.isObject()) {
            b.put("advisoryNumber", advisory(string(forecast.get("advNum"))));
            Instant issued = instant(forecast.get("issuance"));
            b.put("advisoryIssued", issued == null ? null : issued.toString());
            b.put("forecastAdvisoryUrl", officialLink(forecast.get("url")));
        }
        JsonNode publicAdvisory = storm.get("publicAdvisory");
        if (publicAdvisory != null && publicAdvisory.isObject()) {
            b.put("publicAdvisoryUrl", officialLink(publicAdvisory.get("url")));
        }
        return b.build();
    }

    /** The advisory number without leading zeros, upper-cased; null when it does not match the pattern. */
    static String advisory(String value) {
        if (value == null || !ADVISORY.matcher(value.strip()).matches()) {
            return null;
        }
        return value.strip().replaceFirst("^0+(?=\\d)", "").toUpperCase(Locale.ROOT);
    }

    /**
     * The link when it is an {@code https://www.nhc.noaa.gov/} URL without credentials, query or fragment and
     * at most 256 characters; otherwise null.
     */
    static String officialLink(JsonNode node) {
        String value = string(node);
        if (value == null || value.length() > 256) {
            return null;
        }
        try {
            URI uri = new URI(value.strip());
            boolean ok = "https".equals(uri.getScheme()) && "www.nhc.noaa.gov".equalsIgnoreCase(uri.getHost())
                    && uri.getPort() == -1 && uri.getUserInfo() == null && uri.getQuery() == null
                    && uri.getFragment() == null && uri.getPath() != null && uri.getPath().startsWith("/");
            return ok ? uri.toString() : null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /** A number given as a JSON number or a numeric string, within {@code [min, max]}; else null. */
    static Double number(JsonNode node, double min, double max) {
        if (node == null || node.isNull()) {
            return null;
        }
        double value;
        if (node.isNumber()) {
            value = node.doubleValue();
        } else if (node.isString()) {
            String text = node.stringValue().strip();
            if (text.isEmpty()) {
                return null;
            }
            try {
                value = Double.parseDouble(text);
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
        return Double.isFinite(value) && value >= min && value <= max ? value : null;
    }

    private static Integer integer(Double value) {
        return value == null ? null : (int) Math.round(value);
    }

    /** A sanitized string: control characters and {@code <>} removed, stripped, capped; null when blank. */
    static String text(JsonNode node, int max) {
        String value = string(node);
        if (value == null) {
            return null;
        }
        String cleaned = UNSAFE.matcher(value).replaceAll("").strip();
        if (cleaned.isEmpty()) {
            return null;
        }
        return cleaned.length() <= max ? cleaned : cleaned.substring(0, max);
    }

    private static JsonNode first(JsonNode node, String name, String fallback) {
        JsonNode value = node.get(name);
        return value == null || value.isNull() ? node.get(fallback) : value;
    }

    private static Instant instant(JsonNode node) {
        String value = string(node);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.strip()).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String string(JsonNode node) {
        return node != null && node.isString() ? node.stringValue() : null;
    }

    private static String snippet(byte[] json) {
        int length = Math.min(json.length, UpstreamFormatException.MAX_SNIPPET_LENGTH * 4);
        return new String(json, 0, length, StandardCharsets.UTF_8);
    }
}
