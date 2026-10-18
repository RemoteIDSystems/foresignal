package ai.foresignal.source.formats.news;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Country and language names as news upstreams write them, mapped to ISO codes (prompt 133). GDELT's ArtList gives
 * {@code sourcecountry} and {@code language} as English names ("United States", "English"); feeds give BCP 47 tags
 * ({@code en-us}). Names come from the JDK's ISO tables in English, plus aliases for the spellings GDELT uses that the
 * JDK spells differently. Pure.
 */
public final class NewsNames {

    private static final Pattern ISO2 = Pattern.compile("^[A-Za-z]{2}$");
    private static final Pattern ISO3 = Pattern.compile("^[A-Za-z]{3}$");
    private static final Pattern TAG = Pattern.compile("^([A-Za-z]{2,3})([-_][A-Za-z0-9]+)*$");

    private static final Map<String, String> COUNTRIES = new HashMap<>();
    private static final Map<String, String> ISO3_TO_ISO2 = new HashMap<>();
    private static final Map<String, String> LANGUAGES = new HashMap<>();

    static {
        for (String code : Locale.getISOCountries()) {
            Locale locale = Locale.of("", code);
            COUNTRIES.put(key(locale.getDisplayCountry(Locale.ENGLISH)), code);
            try {
                ISO3_TO_ISO2.put(locale.getISO3Country(), code);
            } catch (RuntimeException e) {
                // no ISO 3166-1 alpha-3 code for this entry
            }
        }
        Map<String, String> aliases = Map.ofEntries(
                Map.entry("united states", "US"), Map.entry("usa", "US"), Map.entry("united kingdom", "GB"),
                Map.entry("uk", "GB"), Map.entry("russia", "RU"), Map.entry("south korea", "KR"),
                Map.entry("north korea", "KP"), Map.entry("iran", "IR"), Map.entry("syria", "SY"),
                Map.entry("vietnam", "VN"), Map.entry("taiwan", "TW"), Map.entry("czech republic", "CZ"),
                Map.entry("ivory coast", "CI"), Map.entry("cote d'ivoire", "CI"), Map.entry("burma", "MM"),
                Map.entry("myanmar", "MM"), Map.entry("macedonia", "MK"), Map.entry("north macedonia", "MK"),
                Map.entry("congo", "CG"), Map.entry("republic of the congo", "CG"),
                Map.entry("democratic republic of the congo", "CD"), Map.entry("bosnia-herzegovina", "BA"),
                Map.entry("bosnia and herzegovina", "BA"), Map.entry("laos", "LA"), Map.entry("moldova", "MD"),
                Map.entry("palestine", "PS"), Map.entry("west bank", "PS"), Map.entry("gaza strip", "PS"),
                Map.entry("hong kong", "HK"), Map.entry("macau", "MO"), Map.entry("macao", "MO"),
                Map.entry("brunei", "BN"), Map.entry("cape verde", "CV"), Map.entry("swaziland", "SZ"),
                Map.entry("eswatini", "SZ"), Map.entry("east timor", "TL"), Map.entry("timor-leste", "TL"),
                Map.entry("the bahamas", "BS"), Map.entry("bahamas", "BS"), Map.entry("the gambia", "GM"),
                Map.entry("gambia", "GM"), Map.entry("vatican city", "VA"), Map.entry("holy see", "VA"),
                Map.entry("turkey", "TR"), Map.entry("turkiye", "TR"), Map.entry("saint kitts and nevis", "KN"),
                Map.entry("saint lucia", "LC"), Map.entry("saint vincent and the grenadines", "VC"),
                Map.entry("antigua and barbuda", "AG"), Map.entry("trinidad and tobago", "TT"),
                Map.entry("sao tome and principe", "ST"), Map.entry("micronesia", "FM"),
                Map.entry("kosovo", "XK"));
        COUNTRIES.putAll(aliases);
        for (String code : Locale.getISOLanguages()) {
            String name = Locale.of(code).getDisplayLanguage(Locale.ENGLISH);
            if (name != null && !name.isBlank() && !LANGUAGES.containsKey(key(name))) {
                LANGUAGES.put(key(name), code);
            }
        }
        LANGUAGES.put("chinese", "zh");
        LANGUAGES.put("persian", "fa");
        LANGUAGES.put("farsi", "fa");
        LANGUAGES.put("hebrew", "he");
        LANGUAGES.put("indonesian", "id");
        LANGUAGES.put("norwegian", "no");
    }

    private NewsNames() {}

    /**
     * The ISO 3166-1 alpha-2 code of a country name, alpha-2 or alpha-3 code, or null when unknown.
     *
     * @param name "United States", {@code US} or {@code USA}
     */
    public static String countryIso2(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String text = name.strip();
        if (ISO2.matcher(text).matches()) {
            String upper = text.toUpperCase(Locale.ROOT);
            return COUNTRIES.containsValue(upper) ? upper : null;
        }
        if (ISO3.matcher(text).matches()) {
            String iso2 = ISO3_TO_ISO2.get(text.toUpperCase(Locale.ROOT));
            if (iso2 != null) {
                return iso2;
            }
        }
        return COUNTRIES.get(key(text));
    }

    /**
     * The ISO 639-1 (or 639-2 where no 639-1 exists) code of a language name or BCP 47 tag, or null.
     *
     * @param name "English" or {@code en-us}
     */
    public static String languageCode(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String text = name.strip();
        String byName = LANGUAGES.get(key(text));
        if (byName != null) {
            return byName;
        }
        var m = TAG.matcher(text);
        if (m.matches()) {
            String primary = m.group(1).toLowerCase(Locale.ROOT);
            if (primary.length() == 2 || LANGUAGES.containsValue(primary)) {
                return primary;
            }
        }
        return null;
    }

    private static String key(String name) {
        return name.strip().toLowerCase(Locale.ROOT).replace('’', '\'').replace('&', ' ').replaceAll("\\s+", " ");
    }
}
