package ai.foresignal.baserate;

import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.analytics.series.SeriesValue;
import ai.foresignal.source.adapters.worldbank.WorldBankParser;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Region classes and country covariates (prompt 121): the continent of each country (GeoNames {@code continent} in the
 * 040 gazetteer: {@code AF}, {@code AS}, {@code EU}, {@code NA}, {@code OC}, {@code SA}, {@code AN}), its population
 * (World Bank {@code SP.POP.TOTL} from 117 when collected, else the gazetteer's) and an income band derived from the
 * World Bank GDP per capita ({@code NY.GDP.PCAP.KD}, constant 2015 US$) when collected.
 *
 * <p>Region classes are written {@code continent:AF} or {@code income:low}, {@code income:lower-middle},
 * {@code income:upper-middle}, {@code income:high}. The income band is an approximation of the World Bank's GNI-based
 * groups from GDP per capita ({@value #LOW_MAX}, {@value #LOWER_MIDDLE_MAX} and {@value #UPPER_MIDDLE_MAX} US$) and is
 * labeled as such. The table is read once a day.
 */
@Component
public class RegionClasses {

    /** The upper bound of the low band (GDP per capita, constant 2015 US$). */
    public static final double LOW_MAX = 1_150;

    /** The upper bound of the lower-middle band. */
    public static final double LOWER_MIDDLE_MAX = 4_500;

    /** The upper bound of the upper-middle band. */
    public static final double UPPER_MIDDLE_MAX = 14_000;

    /** The continent codes. */
    public static final List<String> CONTINENTS = List.of("AF", "AN", "AS", "EU", "NA", "OC", "SA");

    /** The income bands. */
    public static final List<String> INCOME_BANDS = List.of("low", "lower-middle", "upper-middle", "high");

    /** How long the table is kept. */
    static final Duration TTL = Duration.ofDays(1);

    private static final Logger log = LoggerFactory.getLogger(RegionClasses.class);

    /**
     * One country's covariates.
     *
     * @param iso2 the country
     * @param continent its continent, or null
     * @param population its population, or null
     * @param gdpPerCapita its GDP per capita, or null
     * @param incomeBand its income band, or null
     */
    public record Country(String iso2, String continent, Long population, Double gdpPerCapita, String incomeBand) {}

    private final JdbcTemplate jdbc;
    private final ObjectProvider<SeriesReader> series;
    private final Clock clock;
    private volatile Map<String, Country> table;
    private volatile Instant loadedAt;

    /** Creates the component. */
    public RegionClasses(JdbcTemplate jdbc, ObjectProvider<SeriesReader> series, Clock clock) {
        this.jdbc = jdbc;
        this.series = series;
        this.clock = clock;
    }

    /** True when {@code regionClass} is {@code continent:XX} or {@code income:band}. */
    public static boolean valid(String regionClass) {
        if (regionClass == null) {
            return false;
        }
        if (regionClass.startsWith("continent:")) {
            return CONTINENTS.contains(regionClass.substring("continent:".length()));
        }
        if (regionClass.startsWith("income:")) {
            return INCOME_BANDS.contains(regionClass.substring("income:".length()));
        }
        return false;
    }

    /** The income band of a GDP per capita, or null. */
    public static String incomeBand(Double gdpPerCapita) {
        if (gdpPerCapita == null || !Double.isFinite(gdpPerCapita) || gdpPerCapita <= 0) {
            return null;
        }
        if (gdpPerCapita < LOW_MAX) {
            return "low";
        }
        if (gdpPerCapita < LOWER_MIDDLE_MAX) {
            return "lower-middle";
        }
        return gdpPerCapita < UPPER_MIDDLE_MAX ? "upper-middle" : "high";
    }

    /** Every known country's covariates, by ISO2. */
    public Map<String, Country> countries() {
        Map<String, Country> current = table;
        if (current == null || loadedAt == null || loadedAt.plus(TTL).isBefore(clock.instant())) {
            current = load();
            table = current;
            loadedAt = clock.instant();
        }
        return current;
    }

    /** One country's covariates, or null. */
    public Country country(String iso2) {
        return iso2 == null ? null : countries().get(iso2.length() > 2 ? iso2.substring(0, 2) : iso2);
    }

    /** The countries of a region class, sorted. */
    public List<String> members(String regionClass) {
        List<String> out = new ArrayList<>();
        if (!valid(regionClass)) {
            return out;
        }
        String value = regionClass.substring(regionClass.indexOf(':') + 1);
        boolean continent = regionClass.startsWith("continent:");
        for (Country c : countries().values()) {
            if (continent ? value.equals(c.continent()) : value.equals(c.incomeBand())) {
                out.add(c.iso2());
            }
        }
        out.sort(null);
        return out;
    }

    /** Forgets the table (tests). */
    public void reset() {
        table = null;
        loadedAt = null;
    }

    private Map<String, Country> load() {
        Map<String, Country> out = new TreeMap<>();
        Map<String, String> continents = new LinkedHashMap<>();
        Map<String, Long> populations = new LinkedHashMap<>();
        try {
            jdbc.query("SELECT iso2, continent, population FROM gazetteer_country WHERE iso2 IS NOT NULL", rs -> {
                String iso2 = rs.getString("iso2").toUpperCase(Locale.ROOT);
                continents.put(iso2, rs.getString("continent"));
                long population = rs.getLong("population");
                if (!rs.wasNull() && population > 0) {
                    populations.put(iso2, population);
                }
            });
        } catch (DataAccessException e) {
            log.debug("The gazetteer is not available: {}", e.getMessage());
        }
        for (String iso2 : continents.keySet()) {
            Double pop = latest("SP.POP.TOTL", iso2);
            Long population = pop != null ? Long.valueOf(Math.round(pop)) : populations.get(iso2);
            Double gdp = latest("NY.GDP.PCAP.KD", iso2);
            out.put(iso2, new Country(iso2, continents.get(iso2), population, gdp, incomeBand(gdp)));
        }
        return out;
    }

    /** The latest usable World Bank value of an indicator for a country, or null. */
    private Double latest(String indicator, String iso2) {
        SeriesReader reader = series.getIfAvailable();
        if (reader == null) {
            return null;
        }
        try {
            SeriesRef ref = SeriesRef.series(ai.foresignal.source.adapters.worldbank.WorldBankIndicatorsAdapter.ID,
                    WorldBankParser.seriesId(indicator, iso2));
            if (!reader.exists(ref)) {
                return null;
            }
            Instant now = clock.instant();
            SeriesData data = reader.read(ref, now.minus(Duration.ofDays(366L * 15)), now, null);
            Double value = null;
            for (SeriesValue v : data.points()) {
                if (v.value() != null && Double.isFinite(v.value())) {
                    value = v.value();
                }
            }
            return value;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
