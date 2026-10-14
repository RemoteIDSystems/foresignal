package ai.foresignal.source.adapters.wri;

import ai.foresignal.source.formats.csv.CsvReader;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The World Resources Institute Global Power Plant Database (prompt 094).
 *
 * <p>Streaming RFC 4180 CSV: one {@code power-plant} record per row, emitted in chunks of
 * {@value #CHUNK} so the whole 12 MB file is never held as records. {@code externalId} = {@code gppd_idnr};
 * geometry is the {@code latitude}/{@code longitude} point. A row without {@code gppd_idnr} is skipped.
 * Frozen at v1.3.0 (2021).
 */
public final class GppdParser {

    /** The record kind. */
    public static final String KIND = "power-plant";

    /** The dataset version the upstream file has been frozen at. */
    public static final String VERSION = "v1.3.0";

    /** Records per emitted chunk. */
    public static final int CHUNK = 5_000;

    /** One parse. */
    public record Result(int records, int skipped) {}

    private GppdParser() {}

    /** Parses a whole body and returns the records (for fixtures and tests). */
    public static Parsed parseAll(byte[] body) {
        List<NormalizedRecord> records = new ArrayList<>();
        Result result = parse(body, records::addAll);
        return new Parsed(records, result.skipped());
    }

    /** The records of {@link #parseAll} and the skipped count. */
    public record Parsed(List<NormalizedRecord> records, int skipped) {}

    /**
     * Streams the CSV, handing each chunk of at most {@value #CHUNK} records to {@code sink}.
     *
     * @throws UpstreamFormatException when the header is missing the id column
     */
    public static Result parse(byte[] body, Consumer<List<NormalizedRecord>> sink) {
        return parse(new ByteArrayInputStream(body), sink);
    }

    /** {@link #parse(byte[], Consumer)} from a stream. */
    public static Result parse(InputStream in, Consumer<List<NormalizedRecord>> sink) {
        int records = 0;
        int skipped = 0;
        try (CsvReader reader = new CsvReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            List<String> header = reader.next();
            if (header == null) {
                throw new UpstreamFormatException("WRI GPPD CSV is empty", "");
            }
            Map<String, Integer> index = CsvReader.index(header);
            if (!index.containsKey("GPPD_IDNR")) {
                throw new UpstreamFormatException("WRI GPPD CSV has no gppd_idnr column", String.join(",", header));
            }
            List<NormalizedRecord> chunk = new ArrayList<>(CHUNK);
            List<String> row;
            while ((row = reader.next()) != null) {
                NormalizedRecord record = map(row, index);
                if (record == null) {
                    skipped++;
                    continue;
                }
                chunk.add(record);
                records++;
                if (chunk.size() == CHUNK) {
                    sink.accept(new ArrayList<>(chunk));
                    chunk.clear();
                }
            }
            if (!chunk.isEmpty()) {
                sink.accept(chunk);
            }
        } catch (IOException e) {
            throw new UpstreamFormatException("WRI GPPD CSV could not be read: " + e.getMessage(), "", e);
        } catch (UncheckedIOException e) {
            throw new UpstreamFormatException("WRI GPPD CSV could not be read: " + e.getMessage(), "", e);
        }
        return new Result(records, skipped);
    }

    /** One row, or null without a {@code gppd_idnr}. */
    static NormalizedRecord map(List<String> row, Map<String, Integer> index) {
        String id = CsvReader.get(row, index, "GPPD_IDNR");
        if (id == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .put("name", CsvReader.get(row, index, "NAME"))
                .put("country", CsvReader.get(row, index, "COUNTRY_LONG"))
                .put("capacityMw", number(CsvReader.get(row, index, "CAPACITY_MW")))
                .put("primaryFuel", CsvReader.get(row, index, "PRIMARY_FUEL"))
                .put("otherFuels", otherFuels(row, index))
                .put("commissioningYear", integer(CsvReader.get(row, index, "COMMISSIONING_YEAR")))
                .put("owner", CsvReader.get(row, index, "OWNER"))
                .put("source", CsvReader.get(row, index, "SOURCE"))
                .put("url", CsvReader.get(row, index, "URL"))
                .put("geolocationSource", CsvReader.get(row, index, "GEOLOCATION_SOURCE"))
                .put("yearOfCapacityData", integer(CsvReader.get(row, index, "YEAR_OF_CAPACITY_DATA")));
        Double latitude = number(CsvReader.get(row, index, "LATITUDE"));
        Double longitude = number(CsvReader.get(row, index, "LONGITUDE"));
        if (latitude != null && longitude != null && Math.abs(latitude) <= 90 && Math.abs(longitude) <= 180) {
            b.point(longitude, latitude);
        }
        return b.build();
    }

    private static List<String> otherFuels(List<String> row, Map<String, Integer> index) {
        List<String> fuels = new ArrayList<>(3);
        for (String column : List.of("OTHER_FUEL1", "OTHER_FUEL2", "OTHER_FUEL3")) {
            String value = CsvReader.get(row, index, column);
            if (value != null) {
                fuels.add(value);
            }
        }
        return fuels;
    }

    private static Double number(String value) {
        if (value == null) {
            return null;
        }
        try {
            double parsed = Double.parseDouble(value.replace(",", ""));
            return Double.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long integer(String value) {
        Double parsed = number(value);
        return parsed == null || parsed != Math.rint(parsed) ? null : parsed.longValue();
    }
}
