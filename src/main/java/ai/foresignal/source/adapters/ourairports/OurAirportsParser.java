package ai.foresignal.source.adapters.ourairports;

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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * OurAirports airports and navaids (prompt 094). Streaming CSV; {@code externalId} = {@code ident} for an
 * airport and {@code navaid:id} for a navaid (kinds share one head namespace, so the prefix keeps them apart).
 */
public final class OurAirportsParser {

    /** The airport kind. */
    public static final String AIRPORT_KIND = "airport";

    /** The navaid kind. */
    public static final String NAVAID_KIND = "navaid";

    /** Records per emitted chunk. */
    public static final int CHUNK = 5_000;

    /** The default airport types. */
    public static final String DEFAULT_TYPES = "large_airport,medium_airport,small_airport";

    private OurAirportsParser() {}

    /** One parse. */
    public record Result(int records, int skipped) {}

    /** Parses an airports CSV body into records, without chart details (fixtures and tests). */
    public static List<NormalizedRecord> parseAirportsAll(byte[] body, Set<String> types) {
        return parseAirportsAll(body, types, Map.of());
    }

    /** Parses an airports CSV body into records with their chart details (fixtures and tests). */
    public static List<NormalizedRecord> parseAirportsAll(byte[] body, Set<String> types,
            Map<String, AirportChart.Info> chart) {
        List<NormalizedRecord> records = new ArrayList<>();
        parseAirports(body, types, chart, records::addAll);
        return records;
    }

    /** Parses a navaids CSV body into records (fixtures and tests). */
    public static List<NormalizedRecord> parseNavaidsAll(byte[] body) {
        List<NormalizedRecord> records = new ArrayList<>();
        parseNavaids(body, records::addAll);
        return records;
    }

    /**
     * Streams an airports CSV.
     *
     * @param types the accepted {@code type} values; empty accepts all
     * @param chart the chart details by airport ident ({@link AirportChart#index}); an airport it lacks gets
     *     {@link AirportChart#NONE}
     */
    public static Result parseAirports(byte[] body, Set<String> types, Map<String, AirportChart.Info> chart,
            Consumer<List<NormalizedRecord>> sink) {
        return parse(body, sink, (row, index) -> airport(row, index, types, chart));
    }

    /** Streams a navaids CSV. */
    public static Result parseNavaids(byte[] body, Consumer<List<NormalizedRecord>> sink) {
        return parse(body, sink, (row, index) -> {
            String id = CsvReader.get(row, index, "ID");
            return id == null ? null : navaid(id, row, index);
        });
    }

    /** The accepted type set of a {@code types} parameter value, or empty for all. */
    public static Set<String> types(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        java.util.LinkedHashSet<String> out = new java.util.LinkedHashSet<>();
        for (String part : value.split(",")) {
            String trimmed = part.strip().toLowerCase(Locale.ROOT);
            if (!trimmed.isEmpty()) {
                out.add(trimmed);
            }
        }
        return out;
    }

    private interface RowMapper {
        NormalizedRecord map(List<String> row, Map<String, Integer> index);
    }

    private static Result parse(byte[] body, Consumer<List<NormalizedRecord>> sink, RowMapper mapper) {
        int records = 0;
        int skipped = 0;
        try (CsvReader reader = new CsvReader(new InputStreamReader(new ByteArrayInputStream(body),
                StandardCharsets.UTF_8))) {
            List<String> header = reader.next();
            if (header == null) {
                throw new UpstreamFormatException("OurAirports CSV is empty", "");
            }
            Map<String, Integer> index = CsvReader.index(header);
            List<NormalizedRecord> chunk = new ArrayList<>(CHUNK);
            List<String> row;
            while ((row = reader.next()) != null) {
                NormalizedRecord record;
                try {
                    record = mapper.map(row, index);
                } catch (RuntimeException e) {
                    record = null;
                }
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
        } catch (IOException | UncheckedIOException e) {
            throw new UpstreamFormatException("OurAirports CSV could not be read: " + e.getMessage(), "", e);
        }
        return new Result(records, skipped);
    }

    /**
     * One airport, or null when its type is filtered out or it has no ident. Its chart details give it
     * {@code towered}, {@code runwaySurface}, {@code longestRunwayFt}, {@code runwayHeading} and the chart symbol
     * ({@code chartSymbol}, {@link AirportChart#symbol}).
     */
    static NormalizedRecord airport(List<String> row, Map<String, Integer> index, Set<String> types,
            Map<String, AirportChart.Info> chart) {
        String ident = CsvReader.get(row, index, "IDENT");
        if (ident == null) {
            return null;
        }
        String type = CsvReader.get(row, index, "TYPE");
        if (!types.isEmpty() && (type == null || !types.contains(type.toLowerCase(Locale.ROOT)))) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(ident, AIRPORT_KIND)
                .put("name", CsvReader.get(row, index, "NAME"))
                .put("type", type)
                .put("elevationFt", integer(CsvReader.get(row, index, "ELEVATION_FT")))
                .put("isoCountry", CsvReader.get(row, index, "ISO_COUNTRY"))
                .put("isoRegion", CsvReader.get(row, index, "ISO_REGION"))
                .put("municipality", CsvReader.get(row, index, "MUNICIPALITY"))
                .put("scheduledService", CsvReader.get(row, index, "SCHEDULED_SERVICE"))
                .put("icaoCode", CsvReader.get(row, index, "ICAO_CODE"))
                .put("iataCode", CsvReader.get(row, index, "IATA_CODE"))
                .put("gpsCode", CsvReader.get(row, index, "GPS_CODE"))
                .put("localCode", CsvReader.get(row, index, "LOCAL_CODE"));
        AirportChart.Info info = chart.getOrDefault(ident, AirportChart.NONE);
        String symbol = AirportChart.symbol(type, info);
        b.put("towered", info.towered())
                .put("runwaySurface", info.surface())
                .put("longestRunwayFt", info.longestFt())
                // The map turns the symbol by this; a heliport's H, an anchor or a closed X stays upright.
                .put("runwayHeading", AirportChart.turnsWithRunway(symbol) ? info.heading() : null)
                .put("chartSymbol", symbol);
        point(b, CsvReader.get(row, index, "LATITUDE_DEG"), CsvReader.get(row, index, "LONGITUDE_DEG"));
        return b.build();
    }

    /** One navaid, given its id. */
    static NormalizedRecord navaid(String id, List<String> row, Map<String, Integer> index) {
        NormalizedRecord.Builder b = NormalizedRecord.builder("navaid:" + id, NAVAID_KIND)
                .put("ident", CsvReader.get(row, index, "IDENT"))
                .put("name", CsvReader.get(row, index, "NAME"))
                .put("type", CsvReader.get(row, index, "TYPE"))
                .put("frequencyKhz", integer(CsvReader.get(row, index, "FREQUENCY_KHZ")))
                .put("elevationFt", integer(CsvReader.get(row, index, "ELEVATION_FT")))
                .put("isoCountry", CsvReader.get(row, index, "ISO_COUNTRY"))
                .put("usageType", CsvReader.get(row, index, "USAGETYPE"))
                .put("power", CsvReader.get(row, index, "POWER"))
                .put("associatedAirport", CsvReader.get(row, index, "ASSOCIATED_AIRPORT"));
        point(b, CsvReader.get(row, index, "LATITUDE_DEG"), CsvReader.get(row, index, "LONGITUDE_DEG"));
        return b.build();
    }

    private static void point(NormalizedRecord.Builder b, String latitude, String longitude) {
        Double lat = number(latitude);
        Double lon = number(longitude);
        if (lat != null && lon != null && Math.abs(lat) <= 90 && Math.abs(lon) <= 180) {
            b.point(lon, lat);
        }
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
