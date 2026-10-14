package ai.foresignal.source.adapters.faa;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the FAA NAS status document ({@code nasstatus.faa.gov/api/airport-status-information}) into
 * {@code airport-delay} records (prompt 087). Pure apart from the bundled airport table.
 *
 * <p><strong>The document is XML</strong>, not JSON (verified 2026-10-14): {@code AIRPORT_STATUS_INFORMATION} with
 * {@code Update_Time} ({@code Wed Oct 14 04:32:43 2026 GMT}) and {@code Delay_type} blocks, each with a {@code Name}
 * and one list. A name can repeat (two "Airport Closures" blocks in the capture). The entries, by element:
 * <ul>
 *   <li>{@code Ground_Delay {ARPT, Reason, Avg, Max}} → {@code ground-delay} ("Ground Delay Programs");
 *   <li>{@code Program {ARPT, Reason, End_Time}} → {@code ground-stop} ("Ground Stop Programs");
 *   <li>{@code Delay {ARPT, Reason, Arrival_Departure Type="Arrival|Departure" {Min, Max, Trend}}} →
 *       {@code arrival-delay} or {@code departure-delay} ("General Arrival/Departure Delay Info");
 *   <li>{@code Airport {ARPT, Reason, Start, Reopen}} → {@code closure} ("Airport Closures");
 *   <li>any other entry with an {@code ARPT} child → a kind from the block name ({@code deicing} for "Deicing",
 *       otherwise the name in lower kebab case).
 * </ul>
 * Durations ("2 hours and 48 minutes", "45 minutes") become minutes. {@code Reopen} ("Oct 17 at 13:15 UTC.") becomes
 * an ISO-8601 instant: the end of the closure NOTAM's validity at the end of {@code Reason} when present, else the
 * {@code Reopen} text in the year of {@code Update_Time}, moved to the next year when it would already be past; text
 * that does not read is kept as given. {@code End_Time} is kept as given ("7:45 pm EDT").
 *
 * <p>externalId {@code ARPT|delayKind}, the first entry of an id kept; {@code observedAt} = {@code Update_Time}
 * (else the fetch time); geometry = the airport's point from {@link UsAirports}, or none for an unknown identifier
 * (counted). Payload {@code airport, delayKind, reason, avgMin, maxMin, minMin, trend, endTime, reopen}.
 */
public final class NasStatusParser {

    /** The record kind. */
    public static final String KIND = "airport-delay";

    private static final DateTimeFormatter UPDATE_TIME = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy",
            Locale.ENGLISH);

    private static final DateTimeFormatter REOPEN = DateTimeFormatter.ofPattern("MMM d yyyy HH:mm", Locale.ENGLISH);

    private static final Pattern HOURS = Pattern.compile("(\\d+)\\s*hours?", Pattern.CASE_INSENSITIVE);

    private static final Pattern MINUTES = Pattern.compile("(\\d+)\\s*min(?:ute)?s?", Pattern.CASE_INSENSITIVE);

    private static final Pattern NOTAM_RANGE = Pattern.compile("(\\d{10})-(\\d{10})(?:EST)?$");

    private static final DateTimeFormatter NOTAM_TIME = DateTimeFormatter.ofPattern("yyMMddHHmm", Locale.ROOT);

    private static final Pattern REOPEN_TEXT = Pattern.compile(
            "^([A-Za-z]{3})\\s+(\\d{1,2})\\s+at\\s+(\\d{1,2}):(\\d{2})\\s*UTC\\.?$");

    private NasStatusParser() {}

    /**
     * What one document gave.
     *
     * @param records the records
     * @param updateTime {@code Update_Time}, or null
     * @param unknownAirports entries whose airport is not in the table (records without geometry)
     * @param skipped entries without an {@code ARPT}, or repeating an id
     */
    public record Parsed(List<NormalizedRecord> records, Instant updateTime, int unknownAirports, int skipped) {

        /** Copies the list. */
        public Parsed {
            records = List.copyOf(records);
        }
    }

    /**
     * Parses a document.
     *
     * @param xml the document
     * @param airports the airport table
     * @param now the fetch time, used when {@code Update_Time} does not read
     * @throws UpstreamFormatException when it is not well-formed, declares a DOCTYPE or is not a NAS status document
     */
    public static Parsed parse(byte[] xml, UsAirports airports, Instant now) {
        FaaXml.Node root = FaaXml.read(xml, "NAS status");
        if (!"AIRPORT_STATUS_INFORMATION".equals(root.name())) {
            throw new UpstreamFormatException("NAS status root is " + root.name()
                    + ", not AIRPORT_STATUS_INFORMATION", "");
        }
        Instant updated = updateTime(root.text("Update_Time"));
        Instant observed = updated == null ? now : updated;
        Map<String, NormalizedRecord> records = new LinkedHashMap<>();
        int unknown = 0;
        int skipped = 0;
        for (FaaXml.Node block : root.children("Delay_type")) {
            String name = block.text("Name");
            for (FaaXml.Node entry : entries(block)) {
                String airport = entry.text("ARPT");
                if (airport == null) {
                    skipped++;
                    continue;
                }
                airport = airport.toUpperCase(Locale.ROOT);
                Map<String, Object> payload = new LinkedHashMap<>();
                String kind = kind(entry, name);
                payload.put("airport", airport);
                payload.put("reason", entry.text("Reason"));
                switch (entry.name()) {
                    case "Ground_Delay" -> {
                        payload.put("avgMin", minutes(entry.text("Avg")));
                        payload.put("maxMin", minutes(entry.text("Max")));
                    }
                    case "Program" -> payload.put("endTime", entry.text("End_Time"));
                    case "Delay" -> {
                        FaaXml.Node ad = entry.child("Arrival_Departure");
                        if (ad != null) {
                            payload.put("minMin", minutes(ad.text("Min")));
                            payload.put("maxMin", minutes(ad.text("Max")));
                            payload.put("trend", ad.text("Trend"));
                        }
                    }
                    case "Airport" -> payload.put("reopen", reopen(entry.text("Reopen"), entry.text("Reason"),
                            observed));
                    default -> {
                        payload.put("avgMin", minutes(entry.text("Avg")));
                        payload.put("maxMin", minutes(entry.text("Max")));
                        payload.put("endTime", entry.text("End_Time"));
                    }
                }
                payload.put("delayKind", kind);
                String id = airport + "|" + kind;
                if (records.containsKey(id)) {
                    skipped++;
                    continue;
                }
                UsAirports.Airport a = airports == null ? null : airports.find(airport);
                if (a == null) {
                    unknown++;
                }
                records.put(id, NormalizedRecord.builder(id, KIND)
                        .observedAt(observed)
                        .geometry(a == null ? null : Geo.point(a.lon(), a.lat()))
                        .putAll(payload)
                        .build());
            }
        }
        return new Parsed(new ArrayList<>(records.values()), updated, unknown, skipped);
    }

    /** The entries of a block: every descendant element with an {@code ARPT} child, in document order. */
    static List<FaaXml.Node> entries(FaaXml.Node block) {
        List<FaaXml.Node> out = new ArrayList<>();
        collect(block, out);
        return out;
    }

    private static void collect(FaaXml.Node node, List<FaaXml.Node> out) {
        for (FaaXml.Node c : node.children()) {
            if (c.child("ARPT") != null) {
                out.add(c);
            } else {
                collect(c, out);
            }
        }
    }

    /** The delay kind of an entry (see the class note). */
    static String kind(FaaXml.Node entry, String blockName) {
        switch (entry.name()) {
            case "Ground_Delay":
                return "ground-delay";
            case "Program":
                return "ground-stop";
            case "Delay": {
                FaaXml.Node ad = entry.child("Arrival_Departure");
                String type = ad == null ? null : ad.attributes().get("Type");
                if (type != null && type.toLowerCase(Locale.ROOT).startsWith("arr")) {
                    return "arrival-delay";
                }
                if (type != null && type.toLowerCase(Locale.ROOT).startsWith("dep")) {
                    return "departure-delay";
                }
                return "general-delay";
            }
            case "Airport":
                return "closure";
            default:
                break;
        }
        String name = blockName == null ? "" : blockName.toLowerCase(Locale.ROOT);
        if (name.contains("deic") || name.contains("de-ic")) {
            return "deicing";
        }
        String slug = name.replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        return slug.isEmpty() ? "other" : slug;
    }

    /** A duration such as "2 hours and 48 minutes" in minutes, or null when it has no number. */
    static Integer minutes(String text) {
        if (text == null) {
            return null;
        }
        Matcher h = HOURS.matcher(text);
        Matcher m = MINUTES.matcher(text);
        boolean hasHours = h.find();
        boolean hasMinutes = m.find();
        if (!hasHours && !hasMinutes) {
            return null;
        }
        return (hasHours ? Integer.parseInt(h.group(1)) * 60 : 0) + (hasMinutes ? Integer.parseInt(m.group(1)) : 0);
    }

    /** {@code Update_Time} ({@code Sat Oct 10 23:14:47 2026 GMT}) as an instant, or null. */
    static Instant updateTime(String text) {
        if (text == null) {
            return null;
        }
        String value = text.strip().replaceAll("\\s+", " ");
        if (value.endsWith(" GMT") || value.endsWith(" UTC")) {
            value = value.substring(0, value.length() - 4);
        }
        try {
            return LocalDateTime.parse(value, UPDATE_TIME).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * A closure's reopening as ISO-8601 text. The closure NOTAM's own validity at the end of {@code Reason}
     * ({@code …2605271826-2705281600}, {@code yyMMddHHmm} UTC) gives the year; otherwise {@code Reopen}
     * ("Oct 17 at 13:15 UTC.") is read in the year of {@code reference} and moved to the next year when that would
     * already be more than a day past (a current closure does not reopen in the past). Text that does not read is kept
     * as given.
     */
    static String reopen(String text, String reason, Instant reference) {
        if (reason != null) {
            Matcher notam = NOTAM_RANGE.matcher(reason.strip());
            if (notam.find()) {
                try {
                    return LocalDateTime.parse(notam.group(2), NOTAM_TIME).toInstant(ZoneOffset.UTC).toString();
                } catch (DateTimeParseException ignored) {
                    // Fall back to the Reopen text.
                }
            }
        }
        if (text == null) {
            return null;
        }
        Matcher m = REOPEN_TEXT.matcher(text.strip());
        if (!m.matches() || reference == null) {
            return text;
        }
        int year = Year.from(reference.atZone(ZoneOffset.UTC)).getValue();
        try {
            Instant at = LocalDateTime.parse(m.group(1) + " " + m.group(2) + " " + year + " "
                    + String.format(Locale.ROOT, "%02d:%s", Integer.parseInt(m.group(3)), m.group(4)), REOPEN)
                    .toInstant(ZoneOffset.UTC);
            if (at.isBefore(reference.minus(java.time.Duration.ofDays(1)))) {
                at = at.atZone(ZoneOffset.UTC).plusYears(1).toInstant();
            }
            return at.toString();
        } catch (DateTimeParseException e) {
            return text;
        }
    }
}
