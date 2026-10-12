package ai.foresignal.reference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * The bundled country table ({@code reference/countries.csv}, prompt 040): ISO and FIPS codes, names,
 * aliases and a label point inside every country.
 *
 * <p>Generated once from Natural Earth admin-0 and the GeoNames {@code countryInfo} names by
 * {@code scripts/build-countries.py} and committed, so it is available at startup with no database and
 * no network. Adapters that publish country-level records (IODA, abuse.ch, UNHCR, travel advisories)
 * use it instead of shipping their own centroid tables, and set {@code geometryApproximate: true} in
 * the payload (Adapter Guide).
 *
 * <p>{@link #byName} is case-, accent- and punctuation-insensitive and includes the aliases
 * ("Türkiye", "Turkey", "Czech Republic", "Ivory Coast").
 */
@Component
public class CountryCentroids {

    /** The bundled table. */
    public static final String RESOURCE = "/reference/countries.csv";

    private static final Pattern MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    private final List<CountryRef> all;
    private final Map<String, CountryRef> byIso2;
    private final Map<String, CountryRef> byIso3;
    private final Map<String, CountryRef> byFips;
    private final Map<String, CountryRef> byName;

    /** Loads the bundled table. */
    public CountryCentroids() {
        this(load());
    }

    /** A table over {@code countries} (tests). */
    public CountryCentroids(List<CountryRef> countries) {
        this.all = List.copyOf(countries);
        Map<String, CountryRef> iso2 = new LinkedHashMap<>();
        Map<String, CountryRef> iso3 = new HashMap<>();
        Map<String, CountryRef> fips = new HashMap<>();
        Map<String, CountryRef> names = new HashMap<>();
        for (CountryRef c : all) {
            iso2.put(c.iso2(), c);
            iso3.put(c.iso3(), c);
            if (c.fips() != null) {
                fips.putIfAbsent(c.fips(), c);
            }
        }
        // Primary names win over aliases, so an alias can never take another country's own name.
        for (CountryRef c : all) {
            names.putIfAbsent(normalize(c.name()), c);
            if (c.nameLong() != null) {
                names.putIfAbsent(normalize(c.nameLong()), c);
            }
        }
        for (CountryRef c : all) {
            for (String alias : c.aliases()) {
                names.putIfAbsent(normalize(alias), c);
            }
        }
        this.byIso2 = Collections.unmodifiableMap(iso2);
        this.byIso3 = Map.copyOf(iso3);
        this.byFips = Map.copyOf(fips);
        this.byName = Map.copyOf(names);
    }

    /** Every country, ordered by ISO2. */
    public List<CountryRef> all() {
        return all;
    }

    /** The country with ISO 3166-1 alpha-2 {@code code} (case-insensitive). */
    public Optional<CountryRef> byIso2(String code) {
        return code == null ? Optional.empty() : Optional.ofNullable(byIso2.get(code.strip().toUpperCase(Locale.ROOT)));
    }

    /** The country with ISO 3166-1 alpha-3 {@code code} (case-insensitive). */
    public Optional<CountryRef> byIso3(String code) {
        return code == null ? Optional.empty() : Optional.ofNullable(byIso3.get(code.strip().toUpperCase(Locale.ROOT)));
    }

    /** The country with FIPS 10-4 {@code code} (case-insensitive), as travel advisories use. */
    public Optional<CountryRef> byFips(String code) {
        return code == null ? Optional.empty() : Optional.ofNullable(byFips.get(code.strip().toUpperCase(Locale.ROOT)));
    }

    /** The country named {@code name}: case-, accent- and punctuation-insensitive, aliases included. */
    public Optional<CountryRef> byName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byName.get(normalize(name)));
    }

    /** True when {@code code} is a known ISO2 code. */
    public boolean isKnown(String code) {
        return byIso2(code).isPresent();
    }

    /** The name of {@code iso2}, or null when unknown. */
    public String name(String iso2) {
        return byIso2(iso2).map(CountryRef::name).orElse(null);
    }

    /** {@code value} folded for name lookups: accents removed, lower case, punctuation and spaces dropped. */
    public static String normalize(String value) {
        String decomposed = Normalizer.normalize(value.strip(), Normalizer.Form.NFD);
        String folded = MARKS.matcher(decomposed).replaceAll("").toLowerCase(Locale.ROOT);
        return NON_ALNUM.matcher(folded).replaceAll("");
    }

    // ------------------------------------------------------------------------- loading

    static List<CountryRef> load() {
        try (InputStream in = CountryCentroids.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("The bundled country table " + RESOURCE + " is missing");
            }
            return parse(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + RESOURCE, e);
        }
    }

    /** Parses the CSV: '#' comment lines, a header row, then one country per row. */
    static List<CountryRef> parse(BufferedReader reader) throws IOException {
        List<CountryRef> out = new ArrayList<>();
        Map<String, Integer> columns = null;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            List<String> cells = cells(line);
            if (columns == null) {
                columns = new HashMap<>();
                for (int i = 0; i < cells.size(); i++) {
                    columns.put(cells.get(i), i);
                }
                continue;
            }
            String aliases = cell(cells, columns, "aliases");
            out.add(new CountryRef(
                    cell(cells, columns, "iso2"),
                    cell(cells, columns, "iso3"),
                    cell(cells, columns, "isoNumeric"),
                    cell(cells, columns, "fips"),
                    cell(cells, columns, "name"),
                    cell(cells, columns, "nameLong"),
                    cell(cells, columns, "continent"),
                    Double.parseDouble(cell(cells, columns, "labelLon")),
                    Double.parseDouble(cell(cells, columns, "labelLat")),
                    aliases == null ? List.of() : Arrays.asList(aliases.split("\\|"))));
        }
        return out;
    }

    private static String cell(List<String> cells, Map<String, Integer> columns, String name) {
        Integer index = columns.get(name);
        if (index == null || index >= cells.size()) {
            return null;
        }
        String value = cells.get(index);
        return value == null || value.isEmpty() ? null : value;
    }

    /** RFC 4180 cells of one line: quoted cells may hold commas and doubled quotes. */
    static List<String> cells(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cell.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                out.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(c);
            }
        }
        out.add(cell.toString());
        return out;
    }
}
