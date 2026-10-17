package ai.foresignal.baserate;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.baserate.reference.ReferenceFamilies;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * The reference class of a base rate (prompt 121, {@code BaseRateDefinition}): a family, the regions (040 codes, or a
 * region class, or neither for worldwide), a window ({@code P7D}, {@code P28D} or {@code P90D}) and an optional
 * condition (an antecedent family within so many days before the window).
 *
 * @param family the family (a 119 signal family plus a subtype, e.g. {@code conflict:battles})
 * @param regionCodes the 040 region codes, sorted, or empty
 * @param regionClass the region class, or null
 * @param window the window
 * @param condition the condition, or null
 */
public record BaseRateDefinition(String family, List<String> regionCodes, String regionClass, String window,
        Condition condition) {

    /** The windows. */
    public static final List<String> WINDOWS = List.of("P7D", "P28D", "P90D");

    /** The most region codes one definition names. */
    public static final int MAX_REGIONS = 300;

    private static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");
    private static final Set<String> FIELDS = Set.of("family", "regionCodes", "regionClass", "window", "condition");

    /**
     * A condition.
     *
     * @param family the antecedent family
     * @param withinDays how many days before the window the antecedent must have occurred (1–365)
     */
    public record Condition(String family, int withinDays) {}

    /** Copies the regions. */
    public BaseRateDefinition {
        regionCodes = regionCodes == null ? List.of() : List.copyOf(new TreeSet<>(regionCodes));
    }

    /** The window in weeks: 1, 4 or 13 (a 90-day window is 13 whole weeks, 91 days). */
    public int windowWeeks() {
        return switch (window) {
            case "P7D" -> 1;
            case "P28D" -> 4;
            default -> 13;
        };
    }

    /** The {@code BaseRateDefinition} object (canonical: nulls left out). */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("family", family);
        if (!regionCodes.isEmpty()) {
            out.put("regionCodes", regionCodes);
        }
        if (regionClass != null) {
            out.put("regionClass", regionClass);
        }
        out.put("window", window);
        if (condition != null) {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("family", condition.family());
            c.put("withinDays", condition.withinDays());
            out.put("condition", c);
        }
        return out;
    }

    /** The plain-language reference class. */
    public String referenceClass() {
        StringBuilder text = new StringBuilder(family).append(" events");
        if (!regionCodes.isEmpty()) {
            text.append(" in ").append(String.join(", ", regionCodes));
        } else if (regionClass != null) {
            text.append(" in the region class ").append(regionClass);
        } else {
            text.append(" worldwide");
        }
        text.append(", per ").append(windowWeeks() * 7).append("-day window");
        if (condition != null) {
            text.append(", in windows that followed ").append(condition.family()).append(" within ")
                    .append(condition.withinDays()).append(condition.withinDays() == 1 ? " day" : " days");
        }
        return text.toString();
    }

    /**
     * Parses and validates a definition.
     *
     * @param map the {@code BaseRateDefinition} object
     * @return the definition
     * @throws ValidationException 400 {@code BASE_RATE_INVALID}
     */
    public static BaseRateDefinition parse(Map<String, Object> map) {
        List<FieldError> errors = new ArrayList<>();
        if (map == null) {
            throw invalid(List.of(new FieldError("definition", "is required")));
        }
        for (String key : map.keySet()) {
            if (!FIELDS.contains(key)) {
                errors.add(new FieldError(key, "is not a BaseRateDefinition member"));
            }
        }
        String family = map.get("family") == null ? null : map.get("family").toString().strip();
        if (family == null || !ReferenceFamilies.FAMILY.matcher(family).matches()) {
            errors.add(new FieldError("family", "must be a family such as conflict:battles or seismic:m5"));
        }
        List<String> regions = new ArrayList<>();
        Object regionValue = map.get("regionCodes");
        if (regionValue != null) {
            if (!(regionValue instanceof List<?> list)) {
                errors.add(new FieldError("regionCodes", "must be an array of 040 region codes"));
            } else {
                for (Object o : list) {
                    String code = o == null ? "" : o.toString().strip();
                    if (!REGION.matcher(code).matches()) {
                        errors.add(new FieldError("regionCodes", "'" + code + "' is not a 040 region code"));
                    } else {
                        regions.add(code);
                    }
                }
                if (regions.size() > MAX_REGIONS) {
                    errors.add(new FieldError("regionCodes", "may name at most " + MAX_REGIONS + " regions"));
                }
            }
        }
        String regionClass = map.get("regionClass") == null ? null : map.get("regionClass").toString().strip();
        if (regionClass != null && !RegionClasses.valid(regionClass)) {
            errors.add(new FieldError("regionClass", "must be continent:{AF|AN|AS|EU|NA|OC|SA} or"
                    + " income:{low|lower-middle|upper-middle|high}"));
        }
        if (regionClass != null && !regions.isEmpty()) {
            errors.add(new FieldError("regionClass", "is exclusive with regionCodes"));
        }
        String window = map.get("window") == null ? null : map.get("window").toString();
        if (!WINDOWS.contains(window)) {
            errors.add(new FieldError("window", "must be one of " + WINDOWS));
        }
        Condition condition = null;
        Object c = map.get("condition");
        if (c != null) {
            if (!(c instanceof Map<?, ?> cm)) {
                errors.add(new FieldError("condition", "must be an object"));
            } else {
                Object cf = cm.get("family");
                Object within = cm.get("withinDays");
                String conditionFamily = cf == null ? null : cf.toString().strip();
                if (conditionFamily == null || !ReferenceFamilies.FAMILY.matcher(conditionFamily).matches()) {
                    errors.add(new FieldError("condition.family", "must be a family"));
                }
                int days = within instanceof Number n && n.doubleValue() == Math.rint(n.doubleValue()) ? n.intValue()
                        : -1;
                if (days < 1 || days > 365) {
                    errors.add(new FieldError("condition.withinDays", "must be a whole number from 1 to 365"));
                }
                for (Object key : cm.keySet()) {
                    if (!"family".equals(key) && !"withinDays".equals(key)) {
                        errors.add(new FieldError("condition." + key, "is not a condition member"));
                    }
                }
                condition = new Condition(conditionFamily, days);
            }
        }
        if (!errors.isEmpty()) {
            throw invalid(errors);
        }
        return new BaseRateDefinition(family, regions, regionClass, window, condition);
    }

    private static ValidationException invalid(List<FieldError> errors) {
        return new ValidationException(ErrorCodes.BASE_RATE_INVALID, "The base-rate definition is not valid.",
                errors);
    }
}
