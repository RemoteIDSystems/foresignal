package ai.foresignal.source.adapters.celestrak;

import ai.foresignal.source.formats.csv.CsvReader;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Parses CelesTrak SOCRATES {@code sort-minRange.csv} (prompt 064;
 * {@code https://celestrak.org/SOCRATES/socrates-format.php}) row by row, so the full file (about 23 MB on
 * 2026-10-13) is never held as rows: only the kept records are.
 *
 * <p>Header-driven columns: {@code NORAD_CAT_ID_1, OBJECT_NAME_1, DSE_1, NORAD_CAT_ID_2, OBJECT_NAME_2, DSE_2,
 * TCA} (UTC, {@code yyyy-MM-dd HH:mm:ss.SSS}), {@code TCA_RANGE} (km), {@code TCA_RELATIVE_SPEED} (km/s),
 * {@code MAX_PROB, DILUTION} (km). A row is kept when any one filter matches: {@code MAX_PROB ≥ minProb},
 * {@code TCA_RANGE ≤ maxRangeKm}, or either object in {@code watch}; at most {@link #MAX_RECORDS} are kept.
 */
public final class SocratesParser {

    /** The record kind. */
    public static final String KIND = "conjunction";

    /** The most records kept per run. */
    public static final int MAX_RECORDS = 2_000;

    /** The columns a row needs. */
    static final List<String> REQUIRED = List.of("NORAD_CAT_ID_1", "NORAD_CAT_ID_2", "TCA", "TCA_RANGE");

    private static final DateTimeFormatter TCA_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS][.SS][.S]", Locale.ROOT);

    /**
     * The row filters.
     *
     * @param minProb keep rows whose {@code MAX_PROB} is at least this
     * @param maxRangeKm keep rows whose {@code TCA_RANGE} is at most this
     * @param watch keep rows involving these catalog numbers
     */
    public record Filter(double minProb, double maxRangeKm, Set<Long> watch) {

        /** Copies the watch list. */
        public Filter {
            watch = watch == null ? Set.of() : Set.copyOf(watch);
        }

        boolean keeps(double prob, double rangeKm, long a, long b) {
            return prob >= minProb || rangeKm <= maxRangeKm || watch.contains(a) || watch.contains(b);
        }
    }

    /**
     * What a parse produced.
     *
     * @param records the kept records, at most {@link #MAX_RECORDS}
     * @param rows data rows read
     * @param skipped rows that could not be read
     * @param capped true when more rows matched than were kept
     */
    public record Parsed(List<NormalizedRecord> records, long rows, int skipped, boolean capped) {

        /** Copies the list. */
        public Parsed {
            records = List.copyOf(records);
        }
    }

    private SocratesParser() {}

    /** True when {@code head} starts like the SOCRATES CSV header. */
    public static boolean looksLike(byte[] head) {
        String text = new String(head, 0, Math.min(head.length, 32), StandardCharsets.UTF_8).stripLeading();
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        return text.startsWith("NORAD_CAT_ID_1");
    }

    /**
     * Parses the CSV stream.
     *
     * @throws UpstreamFormatException when the header lacks a required column
     */
    public static Parsed parse(InputStream in, Filter filter) {
        List<NormalizedRecord> out = new ArrayList<>();
        long rows = 0;
        int skipped = 0;
        boolean capped = false;
        try (CsvReader csv = new CsvReader(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8),
                64 * 1024))) {
            List<String> header = csv.next();
            if (header == null) {
                throw new UpstreamFormatException("SOCRATES CSV is empty", "");
            }
            Map<String, Integer> index = CsvReader.index(header);
            for (String column : REQUIRED) {
                if (!index.containsKey(column)) {
                    throw new UpstreamFormatException("SOCRATES CSV has no " + column + " column",
                            String.join(",", header));
                }
            }
            for (List<String> row = csv.next(); row != null; row = csv.next()) {
                if (row.size() == 1 && row.get(0).isBlank()) {
                    continue;
                }
                rows++;
                NormalizedRecord record;
                try {
                    record = map(row, index, filter);
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    skipped++;
                    continue;
                }
                if (record == null) {
                    continue;
                }
                if (out.size() >= MAX_RECORDS) {
                    capped = true;
                    break;
                }
                out.add(record);
            }
        } catch (java.io.IOException | java.io.UncheckedIOException e) {
            throw new UpstreamFormatException("SOCRATES CSV could not be read", null, e);
        } catch (IllegalArgumentException e) {
            throw new UpstreamFormatException("SOCRATES CSV could not be read: " + e.getMessage(), null, e);
        }
        return new Parsed(out, rows, skipped, capped);
    }

    /** One row as a record, or null when no filter keeps it. */
    static NormalizedRecord map(List<String> row, Map<String, Integer> index, Filter filter) {
        long a = Long.parseLong(require(row, index, "NORAD_CAT_ID_1"));
        long b = Long.parseLong(require(row, index, "NORAD_CAT_ID_2"));
        double range = Double.parseDouble(require(row, index, "TCA_RANGE"));
        Double prob = number(row, index, "MAX_PROB");
        if (!filter.keeps(prob == null ? 0 : prob, range, a, b)) {
            return null;
        }
        Instant tca = parseTca(require(row, index, "TCA"));
        long low = Math.min(a, b);
        long high = Math.max(a, b);
        String id = low + "-" + high + "-" + Math.floorDiv(tca.getEpochSecond(), 60);
        return NormalizedRecord.builder(id, KIND)
                .eventTime(tca)
                .put("norad1", a)
                .put("name1", CsvReader.get(row, index, "OBJECT_NAME_1"))
                .put("daysSinceEpoch1", number(row, index, "DSE_1"))
                .put("norad2", b)
                .put("name2", CsvReader.get(row, index, "OBJECT_NAME_2"))
                .put("daysSinceEpoch2", number(row, index, "DSE_2"))
                .put("tca", tca.toString())
                .put("tcaRangeKm", range)
                .put("relSpeedKms", number(row, index, "TCA_RELATIVE_SPEED"))
                .put("maxProb", prob)
                .put("dilutionKm", number(row, index, "DILUTION"))
                .build();
    }

    /** {@code 2026-10-16 10:59:33.430} (UTC) → an instant; an ISO-8601 form is accepted too. */
    static Instant parseTca(String text) {
        String t = text.strip();
        if (t.contains("T")) {
            return t.endsWith("Z") ? Instant.parse(t) : LocalDateTime.parse(t).toInstant(ZoneOffset.UTC);
        }
        return LocalDateTime.parse(t, TCA_FORMAT).toInstant(ZoneOffset.UTC);
    }

    private static String require(List<String> row, Map<String, Integer> index, String column) {
        String v = CsvReader.get(row, index, column);
        if (v == null) {
            throw new IllegalArgumentException(column + " is missing");
        }
        return v;
    }

    private static Double number(List<String> row, Map<String, Integer> index, String column) {
        String v = CsvReader.get(row, index, column);
        if (v == null) {
            return null;
        }
        double d = Double.parseDouble(v);
        return Double.isFinite(d) ? d : null;
    }
}
