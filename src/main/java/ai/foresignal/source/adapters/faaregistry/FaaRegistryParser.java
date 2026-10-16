package ai.foresignal.source.adapters.faaregistry;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Parses the FAA Releasable Aircraft Database archive (prompt 098): {@code MASTER.txt} rows joined to
 * the {@code ACFTREF.txt} aircraft reference codes, with <strong>no</strong> owner name or address
 * fields ever copied into a record.
 *
 * <p>The archive is comma-delimited with a header row and trailing spaces; both files end with an
 * empty column. Both files are stream-parsed line by line, never held in memory whole. Entries
 * whose name is absolute or contains {@code ..} are rejected (zip-slip), and the
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
        return parse(() -> new ByteArrayInputStream(archive), nonIndividualOwnerNames, sink);
    }

    /**
     * Parses the archive by streaming it: neither {@code MASTER.txt} nor {@code ACFTREF.txt} is ever
     * held in memory whole. The archive is read twice, because a zip stream is sequential and the
     * {@code ACFTREF.txt} codes must be known before {@code MASTER.txt} rows can be joined to them:
     * the first pass checks every entry name (zip-slip) and reads the small reference table line by
     * line; the second streams {@code MASTER.txt} line by line into chunks of 5,000 records.
     *
     * @param archive opens a fresh stream over the zip bytes; called twice
     * @param nonIndividualOwnerNames when true, {@code NAME} is kept for corporate, government, LLC or
     *     non-citizen-corporation registrants
     * @param sink receives records in chunks
     * @return the counts
     * @throws UpstreamFormatException when the archive has no {@code MASTER.txt}, an entry escapes the
     *     archive, or the uncompressed total exceeds {@value #MAX_UNCOMPRESSED_BYTES} bytes
     */
    public static Result parse(Supplier<InputStream> archive, boolean nonIndividualOwnerNames,
            Consumer<List<NormalizedRecord>> sink) {
        Map<String, Reference> references = new HashMap<>();
        boolean hasMaster = false;
        long referenceBytes = 0;
        try (ZipInputStream zip = new ZipInputStream(archive.get())) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String base = checkedBaseName(entry);
                if (base.equals("master.txt")) {
                    hasMaster = true;
                } else if (base.equals("acftref.txt")) {
                    CappedInputStream capped = new CappedInputStream(zip, MAX_UNCOMPRESSED_BYTES);
                    references = parseReferences(reader(capped));
                    referenceBytes = capped.count();
                }
            }
        } catch (IOException e) {
            throw unreadable(e);
        }
        if (!hasMaster) {
            throw new UpstreamFormatException("Archive has no MASTER.txt", "");
        }
        try (ZipInputStream zip = new ZipInputStream(archive.get())) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (checkedBaseName(entry).equals("master.txt")) {
                    CappedInputStream capped = new CappedInputStream(zip, MAX_UNCOMPRESSED_BYTES - referenceBytes);
                    return parseMaster(reader(capped), references, nonIndividualOwnerNames, sink);
                }
            }
        } catch (IOException e) {
            throw unreadable(e);
        }
        throw new UpstreamFormatException("Archive has no MASTER.txt", "");
    }

    /** The lower-cased base name of a file entry, or "" for a directory; rejects zip-slip names. */
    private static String checkedBaseName(ZipEntry entry) {
        String name = entry.getName();
        if (name.contains("..") || name.startsWith("/") || name.startsWith("\\")
                || (name.length() > 1 && name.charAt(1) == ':')) {
            throw new UpstreamFormatException("Zip entry '" + name + "' escapes the archive", "");
        }
        if (entry.isDirectory()) {
            return "";
        }
        return name.substring(name.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
    }

    private static UpstreamFormatException unreadable(IOException e) {
        if (e instanceof CapExceededException) {
            return new UpstreamFormatException("Archive exceeds the uncompressed cap", "", e);
        }
        return new UpstreamFormatException("Unreadable zip: " + e.getMessage(), "", e);
    }

    /** A line reader over an entry; never closed, so the zip stream stays open. */
    private static BufferedReader reader(InputStream in) {
        return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8), 64 * 1024);
    }

    /** The aircraft reference codes: {@code CODE → {mfr, model, typeAcft, typeEng, noEng, noSeats, weight}}. */
    static Map<String, Reference> parseReferences(BufferedReader lines) throws IOException {
        Map<String, Reference> out = new HashMap<>();
        String first = lines.readLine();
        if (first == null) {
            return out;
        }
        Map<String, Integer> header = header(first);
        String line;
        while ((line = lines.readLine()) != null) {
            List<String> cells = cells(line);
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

    /** Streams the {@code MASTER.txt} rows, joined to their reference codes, into chunks. */
    static Result parseMaster(BufferedReader lines, Map<String, Reference> references, boolean nonIndividualOwnerNames,
            Consumer<List<NormalizedRecord>> sink) throws IOException {
        List<NormalizedRecord> chunk = new ArrayList<>();
        int records = 0;
        int skipped = 0;
        String first = lines.readLine();
        if (first == null) {
            return new Result(0, 0);
        }
        Map<String, Integer> header = header(first);
        String line;
        while ((line = lines.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            List<String> cells = cells(line);
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

    /** Thrown when an entry exceeds the uncompressed cap. */
    private static final class CapExceededException extends IOException {
        CapExceededException() {
            super("entry exceeds the uncompressed cap");
        }
    }

    /** Counts the bytes read through it and fails once they pass {@code max}; never closes the source. */
    private static final class CappedInputStream extends FilterInputStream {
        private final long max;
        private long count;

        CappedInputStream(InputStream in, long max) {
            super(in);
            this.max = max;
        }

        long count() {
            return count;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                add(1);
            }
            return b;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int n = super.read(buffer, offset, length);
            if (n > 0) {
                add(n);
            }
            return n;
        }

        private void add(long n) throws IOException {
            count += n;
            if (count > max) {
                throw new CapExceededException();
            }
        }

        @Override
        public void close() {
            // The zip stream is owned by the caller.
        }
    }

    /** One {@code ACFTREF.txt} row. */
    public record Reference(String mfr, String model, String typeAircraft, String typeEngine,
            Long engines, Long seats, String weightClass) {}

    /** The parse counts. */
    public record Result(int records, int skipped) {}
}
