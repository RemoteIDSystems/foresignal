package ai.foresignal.raster.export;

import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.source.spi.ProductSpec;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The attribution and license every raster export and place pack embeds (prompt 108, task 4: "every
 * export embeds attribution and licence (ECMWF 'changes made' where relevant)"). The structured form is
 * the product's existing attribution object ({@link SampleService#attribution}, wire field
 * {@code licence}); the one-line form goes where a format only takes text (MBTiles/PMTiles
 * {@code attribution}, the TIFF {@code Copyright} tag).
 *
 * <p>A "changes made" note is added when the license asks for modifications to be indicated: any
 * Creative Commons BY family license, and every ECMWF product (ECMWF open data is CC BY 4.0 and asks
 * redistributors to state that the data were changed). Foresignal regrids, quantizes and renders every
 * product it stores, so the note always applies to those.
 */
public final class ExportAttribution {

    /** The note appended where the license asks changes to be indicated. */
    public static final String CHANGES_MADE = "Changes made: regridded, quantized and rendered by Foresignal.";

    private ExportAttribution() {}

    /** True when the product's license asks that modifications be indicated (CC BY family or ECMWF). */
    public static boolean changesMade(ProductSpec spec) {
        ProductSpec.Attribution a = spec.attribution();
        if (a == null) {
            return false;
        }
        String licence = a.licence() == null ? "" : a.licence().toUpperCase(Locale.ROOT);
        String text = a.text() == null ? "" : a.text().toUpperCase(Locale.ROOT);
        return licence.startsWith("CC-BY") || licence.startsWith("CC BY") || text.contains("ECMWF")
                || licence.contains("ECMWF");
    }

    /** The structured attribution: the product's attribution object plus {@code changesMade} when it applies. */
    public static Map<String, Object> structured(ProductSpec spec) {
        Map<String, Object> out = new LinkedHashMap<>(SampleService.attribution(spec));
        if (changesMade(spec)) {
            out.put("changesMade", CHANGES_MADE);
        }
        return out;
    }

    /**
     * The one-line form: {@code <text>. License: <id> (<url>). Changes made: ...}. Control characters
     * are replaced with spaces so the line is safe in every text field it goes into.
     */
    public static String line(ProductSpec spec) {
        ProductSpec.Attribution a = spec.attribution();
        StringBuilder out = new StringBuilder();
        out.append(a == null || a.text() == null ? spec.title() : a.text());
        String licence = a == null || a.licence() == null ? "unknown" : a.licence();
        out.append(". License: ").append(licence);
        if (a != null && a.licenceUrl() != null) {
            out.append(" (").append(a.licenceUrl()).append(')');
        }
        out.append('.');
        if (changesMade(spec)) {
            out.append(' ').append(CHANGES_MADE);
        }
        return clean(out.toString());
    }

    /** {@code text} with every control character replaced by a space. */
    static String clean(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (int k = 0; k < text.length(); k++) {
            char c = text.charAt(k);
            out.append(Character.isISOControl(c) ? ' ' : c);
        }
        return out.toString();
    }
}
