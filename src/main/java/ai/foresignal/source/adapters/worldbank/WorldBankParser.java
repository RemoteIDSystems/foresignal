package ai.foresignal.source.adapters.worldbank;

import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.source.series.SeriesPeriods;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the World Bank Indicators API v2 (prompt 117): the country list (to tell countries from aggregates and map
 * ISO3 to ISO2), an indicator's metadata and its data pages. Pure: no network, no state.
 *
 * <p>Every response is a two-element array {@code [header, rows]}; an error is a one-element array
 * {@code [{"message": [...]}]}. Aggregates (regions, income groups, "World") have {@code region.id = "NA"} in the
 * country list and are skipped; the ISO2 code of a country is the 040 region code.
 */
public final class WorldBankParser {

    /** The {@code region.id} of an aggregate. */
    static final String AGGREGATE_REGION = "NA";

    /** The release lag of a structural series. */
    public static final double RELEASE_LAG_DAYS = 180;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private WorldBankParser() {}

    /**
     * One page of a World Bank response.
     *
     * @param page this page's number
     * @param pages the page count
     * @param rows the rows
     */
    public record Page(int page, int pages, JsonNode rows) {}

    /**
     * An indicator's metadata.
     *
     * @param id the indicator id
     * @param name its name
     * @param unit its unit, possibly empty
     * @param sourceOrganization the organization the data comes from
     */
    public record Indicator(String id, String name, String unit, String sourceOrganization) {}

    /**
     * One page.
     *
     * @throws UpstreamFormatException when the body is not a World Bank page, or is an error message
     */
    public static Page page(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body == null ? new byte[0] : body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("worldbank-indicators: the response is not JSON: "
                    + e.getOriginalMessage(), null, e);
        }
        if (root == null || !root.isArray() || root.isEmpty()) {
            throw new UpstreamFormatException("worldbank-indicators: the response is not a World Bank page", null);
        }
        JsonNode header = root.get(0);
        if (header.has("message")) {
            throw new UpstreamFormatException("worldbank-indicators: the World Bank answered with an error: "
                    + header.get("message").toString(), null);
        }
        Long page = JsonProps.integer(header, "page");
        Long pages = JsonProps.integer(header, "pages");
        JsonNode rows = root.size() > 1 && root.get(1).isArray() ? root.get(1) : JSON.createArrayNode();
        return new Page(page == null ? 1 : page.intValue(), pages == null ? 1 : pages.intValue(), rows);
    }

    /**
     * The ISO3 → ISO2 map of the countries in a country-list page; aggregates are left out.
     *
     * @param rows the country rows
     */
    public static Map<String, String> countries(JsonNode rows) {
        Map<String, String> out = new LinkedHashMap<>();
        for (JsonNode row : rows) {
            String iso3 = JsonProps.text(row, "id");
            String iso2 = JsonProps.text(row, "iso2Code");
            JsonNode region = row.get("region");
            String regionId = region == null ? null : JsonProps.text(region, "id");
            if (iso3 == null || iso2 == null || regionId == null || AGGREGATE_REGION.equals(regionId)
                    || !iso2.matches("[A-Z]{2}")) {
                continue;
            }
            out.put(iso3, iso2);
        }
        return out;
    }

    /** The indicator of a metadata page, or a bare one named after {@code id} when the page holds none. */
    public static Indicator indicator(String id, Page page) {
        if (page.rows().isEmpty()) {
            return new Indicator(id, id, "", null);
        }
        JsonNode row = page.rows().get(0);
        String org = JsonProps.text(row, "sourceOrganization");
        if (org != null && org.length() > 300) {
            org = org.substring(0, 300);
        }
        String name = JsonProps.text(row, "name");
        String unit = JsonProps.text(row, "unit");
        return new Indicator(id, name == null ? id : name, unit == null ? "" : unit, org);
    }

    /**
     * The points of a data page: one per country and year, aggregates and null values skipped.
     *
     * @param indicator the indicator
     * @param rows the data rows
     * @param countries ISO3 → ISO2 of the countries
     * @param observedAt the fetch time
     */
    public static List<NormalizedRecord> points(Indicator indicator, JsonNode rows, Map<String, String> countries,
            Instant observedAt) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (JsonNode row : rows) {
            String iso3 = JsonProps.text(row, "countryiso3code");
            String iso2 = iso3 == null ? null : countries.get(iso3);
            String year = JsonProps.text(row, "date");
            Double value = JsonProps.number(row, "value");
            if (iso2 == null || year == null || value == null || !year.matches("\\d{4}")) {
                continue;
            }
            SeriesPeriods.Period period = SeriesPeriods.parse("P1Y", year);
            if (period == null) {
                continue;
            }
            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("indicator", indicator.id());
            extra.put("iso3", iso3);
            extra.put("sourceOrganization", indicator.sourceOrganization());
            JsonNode country = row.get("country");
            String countryName = country == null ? iso2 : JsonProps.text(country, "value");
            out.add(SeriesPoint.point(seriesId(indicator.id(), iso2), indicator.name() + ": "
                    + (countryName == null ? iso2 : countryName), "wb/" + indicator.id(), "P1Y", year,
                    period.start(), period.end(), value, indicator.unit().isBlank() ? "1"
                            : indicator.unit().strip().replaceAll("\\s+", "_"), iso2, extra, "final", observedAt));
        }
        return out;
    }

    /** {@code wb.{indicator}.{iso2}}. */
    public static String seriesId(String indicator, String iso2) {
        return "wb." + indicator + "." + iso2;
    }

    /** The metadata of a World Bank series: structural, level, a 180-day release lag, its region. */
    public static SeriesMeta meta(NormalizedRecord point) {
        Object area = point.payload().get("area");
        return new SeriesMeta(SeriesMeta.STRUCTURAL, SeriesMeta.LEVEL, RELEASE_LAG_DAYS, null,
                area == null ? null : String.valueOf(area), null);
    }

    /** The attribution of a World Bank series: the indicator source and its organization. */
    public static String attribution(Indicator indicator) {
        return "World Bank, World Development Indicators (" + indicator.name() + ")"
                + (indicator.sourceOrganization() == null ? "" : "; source: " + indicator.sourceOrganization());
    }
}
