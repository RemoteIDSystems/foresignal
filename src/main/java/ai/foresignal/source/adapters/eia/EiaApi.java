package ai.foresignal.source.adapters.eia;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * What the EIA v2 sources share (prompt 093): the base URL and its test property, the credential name, the route
 * metadata defaults, the series names and the EIA-units to UCUM mapping.
 *
 * <p>The EIA API requires the key as the {@code api_key} query parameter, so it always appears in the request URL;
 * the 005 redactor masks the credential value in every fetch trace, log line and recorded exchange.
 */
public final class EiaApi {

    /** The source id of the petroleum source. */
    public static final String PETROLEUM_ID = "eia-petroleum";

    /** The source id of the EIA-930 source. */
    public static final String EIA930_ID = "eia-930";

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://api.eia.gov";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.eia.base-url";

    /** The credential. */
    public static final String API_KEY = "apiKey";

    /** The query parameter the key is sent in. */
    public static final String KEY_PARAM = "api_key";

    /** The license. */
    public static final String LICENCE = "US-PD";

    /** The attribution line EIA asks for. */
    public static final String ATTRIBUTION = "Source: U.S. Energy Information Administration";

    /** The terms page. */
    public static final String TERMS_URL = "https://www.eia.gov/opendata/terms-of-service.php";

    /** The petroleum spot route. */
    public static final String PETROLEUM_SPOT_ROUTE = "/v2/petroleum/pri/spt/data/";

    /** The weekly petroleum stocks route. */
    public static final String PETROLEUM_STOCKS_ROUTE = "/v2/petroleum/stoc/wstk/data/";

    /** The EIA-930 region-data route. */
    public static final String RTO_REGION_ROUTE = "/v2/electricity/rto/region-data/data/";

    /** The EIA-930 fuel-type route. */
    public static final String RTO_FUEL_ROUTE = "/v2/electricity/rto/fuel-type-data/data/";

    /** Well-known EIA series names, so the catalog is readable without a per-series metadata call. */
    public static final Map<String, String> SERIES_NAMES = Map.of(
            "RWTC", "Cushing, OK WTI Spot Price FOB",
            "RBRTE", "Europe Brent Spot Price FOB",
            "WCESTUS1", "Weekly U.S. Ending Stocks excluding SPR of Crude Oil");

    /** Region-data types: demand, demand forecast, net generation and total interchange. */
    public static final Set<String> REGION_TYPES = Set.of("D", "DF", "NG", "TI");

    /** The type-name for each region-data type code. */
    public static final Map<String, String> REGION_TYPE_NAMES = Map.of(
            "D", "Demand",
            "DF", "Demand forecast",
            "NG", "Net generation",
            "TI", "Total interchange");

    /** Fuel type codes read from the fuel-type route. */
    public static final Set<String> FUEL_TYPES =
            Set.of("COL", "NG", "NUC", "OIL", "WAT", "SUN", "WND", "BAT", "OTH", "UNK");

    private EiaApi() {}

    /**
     * An EIA v2 URL.
     *
     * @param baseUrl the base, without a trailing slash
     * @param route the route path, starting with {@code /}
     * @param query ordered query parameters; array keys such as {@code data[0]} are percent-encoded
     */
    public static URI uri(String baseUrl, String route, Query query) {
        return query.build(baseUrl, route);
    }

    /** Percent-encodes a query key or value, keeping the EIA array brackets. */
    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** A mutable ordered query string that allows repeated keys (EIA's {@code facets[x][]=A&facets[x][]=B}). */
    public static final class Query {

        private final java.util.List<String[]> parts = new java.util.ArrayList<>();

        /** Adds one key and value pair. */
        public Query add(String key, String value) {
            parts.add(new String[] {key, value});
            return this;
        }

        /** A copy of this query, so a base query can be extended per page. */
        public Query copy() {
            Query out = new Query();
            for (String[] part : parts) {
                out.parts.add(part.clone());
            }
            return out;
        }

        /** Adds one key for each value. */
        public Query addAll(String key, Iterable<String> values) {
            for (String value : values) {
                add(key, value);
            }
            return this;
        }

        URI build(String baseUrl, String route) {
            StringBuilder out = new StringBuilder(baseUrl).append(route).append('?');
            boolean first = true;
            for (String[] part : parts) {
                if (!first) {
                    out.append('&');
                }
                first = false;
                out.append(encode(part[0])).append('=').append(encode(part[1]));
            }
            return URI.create(out.toString());
        }
    }

    /** A fresh query. */
    public static Query query() {
        return new Query();
    }

    /**
     * An EIA unit string as a UCUM code, falling back to the upstream text when it is not one we know (every unit
     * the default EIA-930 and petroleum series return is mapped; the fallback keeps an unexpected unit visible
     * rather than dropping it).
     *
     * @param units the {@code value-units} or {@code units} field
     */
    public static String ucum(String units) {
        if (units == null || units.isBlank()) {
            return null;
        }
        String text = units.strip();
        return switch (text.toLowerCase()) {
            case "$/bbl", "$ per barrel", "dollars per barrel", "$/barrel" -> "[USD]/[bbl_us]";
            case "$/gal", "$ per gallon", "dollars per gallon" -> "[USD]/[gal_us]";
            case "$/mmbtu", "dollars per million btu" -> "[USD]/[MMBtu]";
            case "$/mcf", "dollars per thousand cubic feet" -> "[USD]/k[ft_i]3";
            case "thousand barrels", "mbbl" -> "k[bbl_us]";
            case "thousand barrels per day", "mbbl/d" -> "k[bbl_us]/d";
            case "barrels", "bbl" -> "[bbl_us]";
            case "million barrels", "mmbbl" -> "M[bbl_us]";
            case "million cubic feet", "mmcf" -> "M[ft_i]3";
            case "billion cubic feet", "bcf" -> "G[ft_i]3";
            case "megawatthours", "megawatt hours", "megawatt-hours", "mwh" -> "MW.h";
            case "gigawatthours", "gigawatt hours", "gwh" -> "GW.h";
            case "kilowatthours", "kilowatt hours", "kwh" -> "kW.h";
            case "megawatts", "mw" -> "MW";
            case "percent", "%" -> "%";
            default -> text;
        };
    }
}
