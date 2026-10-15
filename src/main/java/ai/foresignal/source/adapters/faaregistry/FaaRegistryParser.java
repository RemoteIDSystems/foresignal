package ai.foresignal.source.adapters.faaregistry;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Parses the FAA Releasable Aircraft Database archive (prompt 098): {@code MASTER.txt} rows joined to
 * the {@code ACFTREF.txt} aircraft reference codes, with <strong>no</strong> owner name or address
 * fields ever copied into a record.
 *
 * <p>The archive is comma-delimited with a header row and trailing spaces; both files end with an
 * empty column. Entries whose name is absolute or contains {@code ..} are rejected (zip-slip), and the
 * uncompressed total is capped at {@value #MAX_UNCOMPRESSED_BYTES} bytes.
 */
public final class FaaRegistryParser {

    /** The record kind. */
    public static final String KIND = "aircraft-registration";

    /** The cap on uncompressed bytes read from the archive. */
    public static final long MAX_UNCOMPRESSED_BYTES = 500L * 1024 * 1024;

    /**
     * Columns that must never reach a payload (owner PII). Kept here as a single list so the guard is
     * obvious and testable.
     */
    public static final List<String> PII_COLUMNS = List.of(
            "NAME", "STREET", "STREET2", "CITY", "STATE", "ZIP CODE", "REGION", "COUNTY", "COUNTRY",
            "OTHER NAMES(1)", "OTHER NAMES(2)", "OTHER NAMES(3)", "OTHER NAMES(4)", "OTHER NAMES(5)", "UNIQUE ID");

    private FaaRegistryParser() {}

    /**
     * Parses the archive.
     *
     * @param archive the zip bytes
     * @param nonIndividualOwnerNames when true, {@code NAME} is kept for corporate, government, LLC or
     *     non-citizen-corporation registrants
     * @param sink receives records in chunks
     * @return the counts
     * @throws UpstreamFormatException when the archive has no {@code MASTER.txt}/{@code ACFTREF.txt} or
     *     an entry escapes the archive
     */
    public static Result parse(byte[] archive, boolean nonIndividualOwnerNames,
            Consumer<List<NormalizedRecord>> sink) {
        Map<String, Reference> references = new HashMap<>();
        byte[] master = null;
        long total = 0;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.contains("..") || name.startsWith("/") || name.startsWith("\\")) {
                    throw new UpstreamFormatException("Zip entry '" + name + "' escapes the archive", "");
                }
                String base = name.substring(name.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
                if (entry.isDirectory() || (!base.equals("master.txt") && !base.equals("acftref.txt"))) {
                    continue;
                }
                byte[] bytes = read(zip, MAX_UNCOMPRESSED_BYTES - total);
                total += bytes.length;
                if (total > MAX_UNCOMPRESSED_BYTES) {
                    throw new UpstreamFormatException("Archive exceeds the uncompressed cap", "");
                }
                if (base.equals("acftref.txt")) {
                    references = parseReferences(bytes);
                } else {
                    master = bytes;
                }
            }
        } catch (IOException e) {
            throw new UpstreamFormatException("Unreadable zip: " + e.getMessage(), "", e);
        }
        if (master == null) {
            throw new UpstreamFormatException("Archive has no MASTER.txt", "");
        }
        return parseMaster(master, references, nonIndividualOwnerNames, sink);
    }

    private static byte[] read(InputStream in, long max) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[64 * 1024];
        int n;
        while ((n = in.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
            if (out.size() > max) {
                throw new IOException("entry exceeds the cap");
            }
        }
        return out.toByteArray();
    }

    /** The aircraft reference codes: {@code CODE → {mfr, model, typeAcft, typeEng, noEng, noSeats, weight}}. */
    static Map<String, Reference> parseReferences(byte[] body) {
        String[] lines = text(body).split("\n", -1);
        Map<String, Reference> out = new HashMap<>();
        if (lines.length == 0) {
            return out;
        }
        Map<String, Integer> header = header(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            List<String> cells = cells(lines[i]);
            String code = get(cells, header, "CODE");
            if (code == null) {
                continue;
            }
            out.put(code, new Reference(
                    get(cells, header, "MFR"),
                    get(cells, header, "MODEL"),
                    get(cells, header, "TYPE-ACFT"),
                    get(cells, header, "TYPE-ENG"),
                    integer(get(cells, header, "NO-ENG")),
                    integer(get(cells, header, "NO-SEATS")),
                    get(cells, header, "AC-WEIGHT")));
        }
        return out;
    }

    /** Parses the {@code MASTER.txt} rows, joined to their reference codes. */
    static Result parseMaster(byte[] body, Map<String, Reference> references, boolean nonIndividualOwnerNames,
            Consumer<List<NormalizedRecord>> sink) {
        String[] lines = text(body).split("\n", -1);
        List<NormalizedRecord> chunk = new ArrayList<>();
        int records = 0;
        int skipped = 0;
        if (lines.length == 0) {
            return new Result(0, 0);
        }
        Map<String, Integer> header = header(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) {
                continue;
            }
            List<String> cells = cells(lines[i]);
            String nNumber = get(cells, header, "N-NUMBER");
            if (nNumber == null) {
                skipped++;
                continue;
            }
            String mfrMdlCode = get(cells, header, "MFR MDL CODE");
            Reference ref = mfrMdlCode == null ? null : references.get(mfrMdlCode);
            NormalizedRecord record = record(nNumber, cells, header, ref, nonIndividualOwnerNames);
            if (record == null) {
                skipped++;
                continue;
            }
            chunk.add(record);
            records++;
            if (chunk.size() >= 5000) {
                sink.accept(List.copyOf(chunk));
                chunk.clear();
            }
        }
        if (!chunk.isEmpty()) {
            sink.accept(List.copyOf(chunk));
        }
        return new Result(records, skipped);
    }

    static NormalizedRecord record(String nNumber, List<String> cells, Map<String, Integer> header,
            Reference ref, boolean nonIndividualOwnerNames) {
        String serial = get(cells, header, "SERIAL NUMBER");
        String modeSHex = lower(get(cells, header, "MODE S CODE HEX"));
        NormalizedRecord.Builder b = NormalizedRecord.builder("N" + nNumber, KIND)
                .observedAt(null)
                .put("nNumber", nNumber)
                .put("serial", serial)
                .put("mfrMdlCode", get(cells, header, "MFR MDL CODE"))
                .put("manufacturer", ref == null ? null : ref.mfr())
                .put("model", ref == null ? null : ref.model())
                .put("typeAircraft", ref == null ? null : ref.typeAircraft())
                .put("typeEngine", first(get(cells, header, "TYPE ENGINE"), ref == null ? null : ref.typeEngine()))
                .put("engines", ref == null ? null : ref.engines())
                .put("seats", ref == null ? null : ref.seats())
                .put("weightClass", ref == null ? null : ref.weightClass())
                .put("yearMfr", integer(get(cells, header, "YEAR MFR")))
                .put("registrantType", registrantType(get(cells, header, "TYPE REGISTRANT")))
                .put("certIssueDate", date(get(cells, header, "CERT ISSUE DATE")))
                .put("airworthinessDate", date(get(cells, header, "AIR WORTH DATE")))
                .put("expirationDate", date(get(cells, header, "EXPIRATION DATE")))
                .put("statusCode", get(cells, header, "STATUS CODE"))
                .put("modeSHex", modeSHex)
                .put("fractionalOwner", "Y".equalsIgnoreCase(get(cells, header, "FRACT OWNER")))
                .put("kitMfr", get(cells, header, "KIT MFR"))
                .put("kitModel", get(cells, header, "KIT MODEL"));
        if (nonIndividualOwnerNames && isNonIndividual(get(cells, header, "TYPE REGISTRANT"))) {
            b.put("ownerName", get(cells, header, "NAME"));
        }
        return b.build();
    }

    /** Decodes the FAA registrant-type code. */
    static String registrantType(String code) {
        if (code == null) {
            return null;
        }
        return switch (code.strip()) {
            case "1" -> "Individual";
            case "2" -> "Partnership";
            case "3" -> "Corporation";
            case "4" -> "Co-Owned";
            case "5" -> "Government";
            case "7" -> "LLC";
            case "8" -> "Non-Citizen Corporation";
            case "9" -> "Non-Citizen Co-Owned";
            default -> null;
        };
    }

    static boolean isNonIndividual(String code) {
        String decoded = registrantType(code);
        return "Corporation".equals(decoded) || "Government".equals(decoded) || "LLC".equals(decoded)
                || "Non-Citizen Corporation".equals(decoded);
    }

    static String date(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String digits = value.strip();
        // FAA dates are YYYYMMDD.
        if (digits.matches("[0-9]{8}")) {
            try {
                return LocalDate.of(
                                Integer.parseInt(digits.substring(0, 4)),
                                Integer.parseInt(digits.substring(4, 6)),
                                Integer.parseInt(digits.substring(6, 8)))
                        .atStartOfDay(ZoneOffset.UTC).toInstant().toString();
            } catch (RuntimeException e) {
                return null;
            }
        }
        return null;
    }

    static Long integer(String value) {
        if (value == null || !value.strip().matches("[0-9]+")) {
            return null;
        }
        try {
            return Long.parseLong(value.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static String lower(String value) {
        return value == null ? null : value.strip().toLowerCase(Locale.ROOT);
    }

    static String first(String a, String b) {
        String value = a != null ? a : b;
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static String text(byte[] body) {
        // The archive uses Windows line endings.
        return new String(body, StandardCharsets.UTF_8).replace("\r\n", "\n").replace('\r', '\n');
    }

    private static Map<String, Integer> header(String line) {
        Map<String, Integer> out = new HashMap<>();
        List<String> cells = cells(line);
        for (int i = 0; i < cells.size(); i++) {
            out.putIfAbsent(cells.get(i).toUpperCase(Locale.ROOT), i);
        }
        return out;
    }

    /** One comma-delimited row: cells trimmed, a trailing empty column dropped. */
    static List<String> cells(String line) {
        List<String> out = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ',') {
                out.add(line.substring(start, i).strip());
                start = i + 1;
            }
        }
        out.add(line.substring(start).strip());
        while (!out.isEmpty() && out.get(out.size() - 1).isEmpty()) {
            out.remove(out.size() - 1);
        }
        return out;
    }

    private static String get(List<String> cells, Map<String, Integer> header, String column) {
        Integer index = header.get(column);
        if (index == null || index >= cells.size()) {
            return null;
        }
        String value = cells.get(index);
        return value == null || value.isBlank() ? null : value;
    }

    /** One {@code ACFTREF.txt} row. */
    public record Reference(String mfr, String model, String typeAircraft, String typeEngine,
            Long engines, Long seats, String weightClass) {}

    /** The parse counts. */
    public record Result(int records, int skipped) {}
}
