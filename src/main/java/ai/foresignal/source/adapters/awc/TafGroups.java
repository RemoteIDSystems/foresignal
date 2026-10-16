package ai.foresignal.source.adapters.awc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A cheap TAF body tokenizer (prompt 106): splits a raw TAF into forecast change groups and pulls out
 * the wind, visibility, weather and cloud tokens of each. Best-effort and documented as such; the raw
 * text remains the authority. Pure.
 */
public final class TafGroups {

    private static final Pattern CHANGE = Pattern.compile("^(FM\\d{4}|TEMPO|BECMG|INTER|PROB\\d{2}(?: TEMPO)?)$");
    private static final Pattern WIND = Pattern.compile("^(\\d{3}|VRB)(\\d{2,3})(?:G(\\d{2,3}))?(KT|MPS)$");
    private static final Pattern VISIBILITY = Pattern.compile("^(P)?(\\d{1,2})(SM|)(?:/(\\d{1,2})SM)?$");
    private static final Pattern CLOUDS = Pattern.compile("^(FEW|SCT|BKN|OVC|VV)(\\d{3})(CB|TCU)?$");
    private static final Pattern WX = Pattern.compile(
            "^[+-]?(VC|RE|MI|PR|BC|DR|BL|SH|TS|FZ)?(DZ|RA|SN|SG|IC|PL|GR|GS|UP|BR|FG|FU|VA|DU|SA|HZ|PY|PO|SQ|FC|SS|DS|XX)+$");
    private static final Pattern VALIDITY = Pattern.compile("^(\\d{4})/(\\d{4})$");

    private TafGroups() {}

    /** The forecast groups: {@code [{changeIndicator, wind, visibility, wx[], clouds[], from, to}]}. */
    public static List<Map<String, Object>> parse(String raw) {
        List<Map<String, Object>> groups = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return groups;
        }
        String body = raw.replaceAll("\n", " ").replaceAll("\\s+", " ").strip();
        List<String> tokens = new ArrayList<>(List.of(body.split(" ")));
        Map<String, Object> current = null;
        List<String> wx = new ArrayList<>();
        List<String> clouds = new ArrayList<>();
        String visibility = null;
        Map<String, Object> wind = null;
        String change = "INITIAL";
        String from = null;
        String to = null;
        for (String token : tokens) {
            Matcher changeMatcher = CHANGE.matcher(token);
            if (changeMatcher.matches()) {
                if (current != null) {
                    groups.add(finish(current, change, wind, visibility, wx, clouds, from, to));
                }
                current = new LinkedHashMap<>();
                change = token;
                wx = new ArrayList<>();
                clouds = new ArrayList<>();
                visibility = null;
                wind = null;
                from = null;
                to = null;
                continue;
            }
            Matcher validity = VALIDITY.matcher(token);
            if (validity.matches()) {
                from = validity.group(1);
                to = validity.group(2);
                continue;
            }
            if (current == null) {
                current = new LinkedHashMap<>();
            }
            Matcher windMatcher = WIND.matcher(token);
            if (windMatcher.matches()) {
                Map<String, Object> w = new LinkedHashMap<>();
                w.put("dir", "VRB".equals(windMatcher.group(1)) ? null : Integer.parseInt(windMatcher.group(1)));
                w.put("speed", Integer.parseInt(windMatcher.group(2)));
                if (windMatcher.group(3) != null) {
                    w.put("gust", Integer.parseInt(windMatcher.group(3)));
                }
                w.put("unit", windMatcher.group(4));
                wind = w;
                continue;
            }
            if (visibility == null && VISIBILITY.matcher(token).matches()) {
                visibility = token;
                continue;
            }
            if (CLOUDS.matcher(token).matches()) {
                clouds.add(token);
                continue;
            }
            if (WX.matcher(token).matches()) {
                wx.add(token);
            }
        }
        if (current != null) {
            groups.add(finish(current, change, wind, visibility, wx, clouds, from, to));
        }
        return groups;
    }

    private static Map<String, Object> finish(Map<String, Object> group, String change, Map<String, Object> wind,
            String visibility, List<String> wx, List<String> clouds, String from, String to) {
        group.put("changeIndicator", change);
        if (wind != null) {
            group.put("wind", wind);
        }
        if (visibility != null) {
            group.put("visibility", visibility);
        }
        if (!wx.isEmpty()) {
            group.put("wx", new ArrayList<>(wx));
        }
        if (!clouds.isEmpty()) {
            group.put("clouds", new ArrayList<>(clouds));
        }
        if (from != null) {
            group.put("from", from);
        }
        if (to != null) {
            group.put("to", to);
        }
        return group;
    }
}
