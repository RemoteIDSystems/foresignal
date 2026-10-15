/*
 * The station field normalization (station id, coordinates, name, capacity, the boolean coercion of
 * normalizeGbfsBool, the non-negative integer rule of toNonNegativeInteger and the stations-array lookup of
 * extractStationsArray) is ported from God's Eye View, src/sources/gbfsStations.js,
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; GBFS v3.0 documents (localized names, RFC 3339 times, num_vehicles_available); the status
 * booleans come from station_status only; a station without a usable last_reported is dropped rather than kept;
 * the alternate id/latitude/longitude spellings are not read (no reviewed system uses them).
 */
package ai.foresignal.source.adapters.gbfs;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses GBFS {@code station_information} and {@code station_status} documents and merges them into one snapshot
 * per station (prompt 071; Core-Data-Model §3 rule 7). Pure: no I/O, no clock.
 *
 * <p>Both envelopes are read, {@code {last_updated, ttl, version?, data: {stations: [...]}}}:
 * <ul>
 *   <li><strong>GBFS 1.x and 2.x:</strong> {@code name} is a string; {@code num_bikes_available},
 *       {@code num_ebikes_available} (optional; BCycle's {@code num_bikes_available_types.electric} is read when it is
 *       absent), {@code num_bikes_disabled}, {@code num_docks_available}; times are epoch seconds.
 *   <li><strong>GBFS 3.0:</strong> {@code name} is localized ({@code [{text, language}]}): the requested language,
 *       then its primary subtag, then the first entry; {@code num_vehicles_available},
 *       {@code num_vehicles_disabled}; times are RFC 3339. {@code vehicle_types_available} counts per vehicle type,
 *       but which types are electric is published only in {@code vehicle_types.json}, which is not on the reviewed
 *       path allowlist, so {@code ebikesAvailable} is absent for 3.0 documents.
 *   <li>Booleans may be {@code true/false}, {@code 0/1} or the strings {@code "true"/"false"/"yes"/"no"}
 *       ({@link #normalizeBool}).
 * </ul>
 *
 * <p>A time before {@value #EARLIEST_PLAUSIBLE_EPOCH_SECOND} (2000-01-01) is treated as missing: Lyft feeds report
 * {@code last_reported: 86400} for stations that never reported (54 of Citi Bike's 2519 stations on 2026-10-13).
 */
public final class GbfsParser {

    /** The kind of every record. */
    public static final String KIND = "bikeshare-station";

    /** The default {@code language} parameter. */
    public static final String DEFAULT_LANGUAGE = "en";

    /** Times before this epoch second (2000-01-01T00:00:00Z) are placeholders, not observations. */
    public static final long EARLIEST_PLAUSIBLE_EPOCH_SECOND = 946_684_800L;

    /** The bike count at or below which a renting station is {@code low}. */
    public static final int LOW_BIKES = 2;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private GbfsParser() {}

    /**
     * One station of {@code station_information}.
     *
     * @param stationId the GBFS {@code station_id}
     * @param name the name in the chosen language, or null
     * @param address the street address, or null
     * @param lat latitude, WGS84
     * @param lon longitude, WGS84
     * @param capacity the number of docks, or null
     * @param regionId the GBFS {@code region_id}, or null
     */
    public record StationInfo(String stationId, String name, String address, double lat, double lon, Integer capacity,
            String regionId) {}

    /**
     * A parsed {@code station_information} document.
     *
     * @param lastUpdated the document's {@code last_updated}, or null
     * @param ttlSeconds the document's {@code ttl}, 0 when absent
     * @param version the document's {@code version}, or null (GBFS 1.x)
     * @param stations the stations by id
     * @param skipped stations without an id or a usable position
     */
    public record Information(Instant lastUpdated, long ttlSeconds, String version, Map<String, StationInfo> stations,
            int skipped) {}

    /**
     * One station of {@code station_status}.
     *
     * @param stationId the GBFS {@code station_id}
     * @param bikesAvailable bikes (2.x) or vehicles (3.0) available
     * @param ebikesAvailable e-bikes available, or null when the document does not say
     * @param docksAvailable docks available, or null
     * @param disabledBikes disabled bikes or vehicles, or null
     * @param disabledDocks disabled docks, or null
     * @param installed {@code is_installed}
     * @param renting {@code is_renting}
     * @param returning {@code is_returning}
     * @param lastReported {@code last_reported}, or null when absent or implausible
     */
    public record StationStatus(String stationId, int bikesAvailable, Integer ebikesAvailable, Integer docksAvailable,
            Integer disabledBikes, Integer disabledDocks, boolean installed, boolean renting, boolean returning,
            Instant lastReported) {}

    /**
     * A parsed {@code station_status} document.
     *
     * @param lastUpdated the document's {@code last_updated}, or null
     * @param ttlSeconds the document's {@code ttl}, 0 when absent
     * @param version the document's {@code version}, or null (GBFS 1.x)
     * @param stations the stations, one per id (the newest report wins)
     * @param skipped entries without an id or a bike count
     */
    public record Status(Instant lastUpdated, long ttlSeconds, String version, List<StationStatus> stations,
            int skipped) {}

    /**
     * The merged snapshot.
     *
     * @param records one record per station with both information and a usable {@code last_reported}
     * @param untimed status entries dropped for lack of a usable {@code last_reported}
     * @param unmatched status entries skipped because {@code station_information} does not list them (yet)
     */
    public record Snapshot(List<NormalizedRecord> records, int untimed, int unmatched) {}

    /**
     * Parses a {@code station_information} document.
     *
     * @param json the body
     * @param language the preferred language of a 3.0 localized name; null means {@value #DEFAULT_LANGUAGE}
     * @throws UpstreamFormatException when the body is not JSON or not a GBFS stations document
     */
    public static Information parseInformation(byte[] json, String language) {
        JsonNode root = root(json, "station_information");
        String lang = language == null || language.isBlank() ? DEFAULT_LANGUAGE : language.strip();
        Map<String, StationInfo> stations = new LinkedHashMap<>();
        int skipped = 0;
        for (JsonNode raw : stations(root, json, "station_information")) {
            String id = text(raw.get("station_id"));
            Double lat = number(raw.get("lat"));
            Double lon = number(raw.get("lon"));
            if (id == null || lat == null || lon == null || !validPosition(lat, lon)) {
                skipped++;
                continue;
            }
            String name = localized(raw.get("name"), lang);
            if (name == null) {
                name = localized(raw.get("short_name"), lang);
            }
            stations.put(id, new StationInfo(id, name, localized(raw.get("address"), lang), lat, lon,
                    nonNegative(raw.get("capacity")), text(raw.get("region_id"))));
        }
        return new Information(time(root.get("last_updated")), ttl(root), text(root.get("version")),
                Collections.unmodifiableMap(stations), skipped);
    }

    /**
     * Parses a {@code station_status} document.
     *
     * @param json the body
     * @throws UpstreamFormatException when the body is not JSON or not a GBFS stations document
     */
    public static Status parseStatus(byte[] json) {
        JsonNode root = root(json, "station_status");
        Map<String, StationStatus> byId = new LinkedHashMap<>();
        int skipped = 0;
        for (JsonNode raw : stations(root, json, "station_status")) {
            String id = text(raw.get("station_id"));
            Integer bikes = nonNegative(raw.get("num_vehicles_available"));
            if (bikes == null) {
                bikes = nonNegative(raw.get("num_bikes_available"));
            }
            if (id == null || bikes == null) {
                skipped++;
                continue;
            }
            Integer ebikes = nonNegative(raw.get("num_ebikes_available"));
            JsonNode types = raw.get("num_bikes_available_types");
            if (ebikes == null && types != null && types.isObject()) {
                ebikes = nonNegative(types.get("electric"));
            }
            Integer disabledBikes = nonNegative(raw.get("num_vehicles_disabled"));
            if (disabledBikes == null) {
                disabledBikes = nonNegative(raw.get("num_bikes_disabled"));
            }
            StationStatus status = new StationStatus(id, bikes, ebikes, nonNegative(raw.get("num_docks_available")),
                    disabledBikes, nonNegative(raw.get("num_docks_disabled")),
                    normalizeBool(raw.get("is_installed"), true), normalizeBool(raw.get("is_renting"), true),
                    normalizeBool(raw.get("is_returning"), true), time(raw.get("last_reported")));
            StationStatus previous = byId.get(id);
            if (previous == null || newer(status, previous)) {
                byId.put(id, status);
            }
        }
        return new Status(time(root.get("last_updated")), ttl(root), text(root.get("version")),
                List.copyOf(byId.values()), skipped);
    }

    /**
     * Merges status with information: one record per status entry whose station is listed and has a usable
     * {@code last_reported}.
     *
     * @param systemId the reviewed system id, stored as {@code system}
     * @param information the stations of {@code station_information}, by id
     * @param status the parsed {@code station_status}
     */
    public static Snapshot merge(String systemId, Map<String, StationInfo> information, Status status) {
        List<NormalizedRecord> records = new ArrayList<>();
        int untimed = 0;
        int unmatched = 0;
        for (StationStatus s : status.stations()) {
            StationInfo info = information == null ? null : information.get(s.stationId());
            if (info == null) {
                unmatched++;
                continue;
            }
            if (s.lastReported() == null) {
                untimed++;
                continue;
            }
            records.add(NormalizedRecord.builder(s.stationId(), KIND)
                    .observedAt(s.lastReported())
                    .point(info.lon(), info.lat())
                    .put("name", info.name())
                    .put("address", info.address())
                    .put("capacity", info.capacity())
                    .put("regionId", info.regionId())
                    .put("bikesAvailable", s.bikesAvailable())
                    .put("ebikesAvailable", s.ebikesAvailable())
                    .put("docksAvailable", s.docksAvailable())
                    .put("disabledBikes", s.disabledBikes())
                    .put("disabledDocks", s.disabledDocks())
                    .put("isInstalled", s.installed())
                    .put("isRenting", s.renting())
                    .put("isReturning", s.returning())
                    .put("availability", availability(s.installed(), s.renting(), s.bikesAvailable(),
                            s.docksAvailable()))
                    .put("system", systemId)
                    .build());
        }
        return new Snapshot(List.copyOf(records), untimed, unmatched);
    }

    /**
     * The station's service summary, the first that applies: {@code out-of-service} (not installed or not renting),
     * {@code empty} (no bikes), {@code low} (at most {@value #LOW_BIKES} bikes), {@code full} (no docks), {@code ok}.
     *
     * @param docks docks available, or null when unknown (never {@code full} then)
     */
    public static String availability(boolean installed, boolean renting, int bikes, Integer docks) {
        if (!installed || !renting) {
            return "out-of-service";
        }
        if (bikes <= 0) {
            return "empty";
        }
        if (bikes <= LOW_BIKES) {
            return "low";
        }
        if (docks != null && docks <= 0) {
            return "full";
        }
        return "ok";
    }

    /**
     * A GBFS boolean (GEV {@code normalizeGbfsBool}): a JSON boolean, a number (non-zero is true), or the strings
     * {@code true/yes/false/no} and numeric strings; anything else, and absence, is {@code fallback}.
     */
    public static boolean normalizeBool(JsonNode value, boolean fallback) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return fallback;
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        if (value.isNumber()) {
            return value.asDouble() != 0.0;
        }
        if (value.isString()) {
            String text = value.asString().strip().toLowerCase(Locale.ROOT);
            switch (text) {
                case "true", "yes" -> {
                    return true;
                }
                case "false", "no" -> {
                    return false;
                }
                default -> {
                    try {
                        return Double.parseDouble(text) != 0.0;
                    } catch (NumberFormatException e) {
                        return fallback;
                    }
                }
            }
        }
        return fallback;
    }

    /**
     * A GBFS time: epoch seconds (a number or a numeric string, GBFS 1.x and 2.x) or RFC 3339 (GBFS 3.0); null when
     * absent, unparseable, or before 2000-01-01.
     */
    public static Instant time(JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        Instant instant = null;
        if (value.isNumber()) {
            instant = epoch(value.asDouble());
        } else if (value.isString()) {
            String text = value.asString().strip();
            if (text.matches("\\d+(\\.\\d+)?")) {
                instant = epoch(Double.parseDouble(text));
            } else {
                try {
                    instant = OffsetDateTime.parse(text).toInstant();
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        }
        // GBFS times are POSIX seconds; a v3.0 ISO timestamp carries milliseconds, so truncate to
        // seconds so both feed versions produce identical instants.
        return instant == null || instant.getEpochSecond() < EARLIEST_PLAUSIBLE_EPOCH_SECOND
                ? null
                : instant.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    }

    private static Instant epoch(double seconds) {
        if (!Double.isFinite(seconds) || seconds < 0 || seconds > 32_503_680_000.0) {
            return null;
        }
        return Instant.ofEpochSecond((long) Math.floor(seconds));
    }

    private static boolean newer(StationStatus candidate, StationStatus previous) {
        if (candidate.lastReported() == null) {
            return false;
        }
        return previous.lastReported() == null || candidate.lastReported().isAfter(previous.lastReported());
    }

    private static JsonNode root(byte[] json, String what) {
        if (json == null || json.length == 0) {
            throw new UpstreamFormatException("GBFS " + what + " is empty", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("GBFS " + what + " is not valid JSON: " + e.getOriginalMessage(),
                    snippet(json), e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("GBFS " + what + " is not a JSON object", snippet(json));
        }
        return root;
    }

    /** {@code data.stations}, or the {@code stations} of a locale wrapper under {@code data} (GEV). */
    private static JsonNode stations(JsonNode root, byte[] json, String what) {
        JsonNode data = root.get("data");
        if (data != null && data.isObject()) {
            JsonNode stations = data.get("stations");
            if (stations != null && stations.isArray()) {
                return stations;
            }
            for (JsonNode child : data) {
                JsonNode nested = child == null ? null : child.get("stations");
                if (nested != null && nested.isArray()) {
                    return nested;
                }
            }
        }
        throw new UpstreamFormatException("GBFS " + what + " has no data.stations array", snippet(json));
    }

    private static long ttl(JsonNode root) {
        JsonNode ttl = root.get("ttl");
        if (ttl == null || !ttl.isNumber()) {
            return 0;
        }
        long value = ttl.asLong();
        return Math.max(0, value);
    }

    /**
     * A name: a string (GBFS 1.x and 2.x), or a localized array (3.0) read in {@code language}, then its primary
     * subtag, then the first entry.
     */
    static String localized(JsonNode value, String language) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isString()) {
            return blankToNull(value.asString());
        }
        if (!value.isArray() || value.isEmpty()) {
            return null;
        }
        String wanted = language.toLowerCase(Locale.ROOT);
        String primary = wanted.contains("-") ? wanted.substring(0, wanted.indexOf('-')) : wanted;
        String exact = null;
        String sameLanguage = null;
        String first = null;
        for (JsonNode entry : value) {
            String text = entry == null ? null : text(entry.get("text"));
            if (text == null) {
                continue;
            }
            if (first == null) {
                first = text;
            }
            String tag = text(entry.get("language"));
            if (tag == null) {
                continue;
            }
            String lower = tag.toLowerCase(Locale.ROOT);
            if (exact == null && lower.equals(wanted)) {
                exact = text;
            }
            String entryPrimary = lower.contains("-") ? lower.substring(0, lower.indexOf('-')) : lower;
            if (sameLanguage == null && entryPrimary.equals(primary)) {
                sameLanguage = text;
            }
        }
        return exact != null ? exact : sameLanguage != null ? sameLanguage : first;
    }

    private static String text(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isString()) {
            return blankToNull(value.asString());
        }
        if (value.isNumber()) {
            return value.asString();
        }
        return null;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String stripped = value.strip();
        return stripped.isEmpty() ? null : stripped;
    }

    private static Double number(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        double d;
        if (value.isNumber()) {
            d = value.asDouble();
        } else if (value.isString()) {
            try {
                d = Double.parseDouble(value.asString().strip());
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
        return Double.isFinite(d) ? d : null;
    }

    /** GEV {@code toNonNegativeInteger}: a finite, non-negative number rounded, else null. */
    private static Integer nonNegative(JsonNode value) {
        Double d = number(value);
        if (d == null || d < 0 || d > Integer.MAX_VALUE) {
            return null;
        }
        return (int) Math.round(d);
    }

    private static boolean validPosition(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180 && !(lat == 0.0 && lon == 0.0);
    }

    private static String snippet(byte[] json) {
        int length = Math.min(json.length, UpstreamFormatException.MAX_SNIPPET_LENGTH * 4);
        return new String(json, 0, length, StandardCharsets.UTF_8);
    }
}
