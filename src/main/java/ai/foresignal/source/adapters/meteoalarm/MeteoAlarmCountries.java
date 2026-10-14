package ai.foresignal.source.adapters.meteoalarm;

import java.util.List;
import java.util.regex.Pattern;

/**
 * The reviewed MeteoAlarm member countries (prompt 089): the JSON API slug, the ISO 3166-1 alpha-2 code and the
 * display name of each of the 38 members of the MeteoAlarm cooperation. Every slug was verified live on
 * 2026-10-14 against {@code https://feeds.meteoalarm.org/api/v1/warnings/feeds-<slug>}.
 *
 * <p>The list is static and code-reviewed; the API accepts no caller-supplied slug.
 */
public final class MeteoAlarmCountries {

    /** One member. */
    public record Country(String slug, String iso2, String name) {

        /** The source id, {@code meteoalarm-<slug>}. */
        public String id() {
            return "meteoalarm-" + slug;
        }
    }

    /** The members whose JSON feed answered HTTP 200 on 2026-10-14. */
    public static final List<Country> REVIEWED = List.of(
            new Country("andorra", "AD", "Andorra"),
            new Country("austria", "AT", "Austria"),
            new Country("belgium", "BE", "Belgium"),
            new Country("bosnia-herzegovina", "BA", "Bosnia and Herzegovina"),
            new Country("bulgaria", "BG", "Bulgaria"),
            new Country("croatia", "HR", "Croatia"),
            new Country("cyprus", "CY", "Cyprus"),
            new Country("czechia", "CZ", "Czechia"),
            new Country("denmark", "DK", "Denmark"),
            new Country("estonia", "EE", "Estonia"),
            new Country("finland", "FI", "Finland"),
            new Country("france", "FR", "France"),
            new Country("germany", "DE", "Germany"),
            new Country("greece", "GR", "Greece"),
            new Country("hungary", "HU", "Hungary"),
            new Country("iceland", "IS", "Iceland"),
            new Country("ireland", "IE", "Ireland"),
            new Country("italy", "IT", "Italy"),
            new Country("latvia", "LV", "Latvia"),
            new Country("lithuania", "LT", "Lithuania"),
            new Country("luxembourg", "LU", "Luxembourg"),
            new Country("malta", "MT", "Malta"),
            new Country("moldova", "MD", "Moldova"),
            new Country("montenegro", "ME", "Montenegro"),
            new Country("netherlands", "NL", "Netherlands"),
            new Country("norway", "NO", "Norway"),
            new Country("poland", "PL", "Poland"),
            new Country("portugal", "PT", "Portugal"),
            new Country("republic-of-north-macedonia", "MK", "North Macedonia"),
            new Country("romania", "RO", "Romania"),
            new Country("serbia", "RS", "Serbia"),
            new Country("slovakia", "SK", "Slovakia"),
            new Country("slovenia", "SI", "Slovenia"),
            new Country("spain", "ES", "Spain"),
            new Country("sweden", "SE", "Sweden"),
            new Country("switzerland", "CH", "Switzerland"),
            new Country("ukraine", "UA", "Ukraine"),
            new Country("united-kingdom", "GB", "United Kingdom"));

    private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    private MeteoAlarmCountries() {}

    /** The problems of a country list. */
    public static List<String> problems(List<Country> countries) {
        List<String> out = new java.util.ArrayList<>();
        java.util.Set<String> slugs = new java.util.HashSet<>();
        for (Country c : countries) {
            String id = c.id();
            if (c.slug() == null || !SLUG.matcher(c.slug()).matches()) {
                out.add(id + ": the slug must be kebab-case");
            } else if (!slugs.add(c.slug())) {
                out.add(id + ": duplicate slug");
            }
            if (c.iso2() == null || !c.iso2().matches("[A-Z]{2}")) {
                out.add(id + ": iso2 must be an ISO 3166-1 alpha-2 code");
            }
            if (c.name() == null || c.name().isBlank()) {
                out.add(id + ": name is required");
            }
        }
        return out;
    }
}
