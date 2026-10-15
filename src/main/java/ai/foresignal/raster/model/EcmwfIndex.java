package ai.foresignal.raster.model;

import ai.foresignal.raster.RasterFormatException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The ECMWF open-data {@code .index} inventory (prompt 105; Weather-Imagery-Design §5.3): one JSON
 * object per line, each naming one GRIB message of a step file by {@code param}, {@code levtype},
 * optional {@code levelist}, {@code _offset} and {@code _length}. It is <strong>not</strong> the NCEP
 * {@code .idx} format, so it gets its own parser and its own range planner
 * ({@link EcmwfRunPlanner}), alongside the God's Eye View {@code .idx} path.
 *
 * <p>A message's byte range is exactly {@code [_offset, _offset + _length − 1]}, so unlike an
 * {@code .idx} the object size is never needed to find the last message's end. Fields are matched by
 * {@code param} + {@code levtype} (+ {@code levelist}), never by message order.
 *
 * <p>ECMWF writes the 10 m gust as {@code 10fg} at short steps and {@code 10fg3}/{@code 10fg6} once
 * the accumulation reaches three or six hours (verified 2026-10-15: {@code 10fg} at f012,
 * {@code 10fg3} at f144). {@link #normalizeParam} folds those onto {@code 10fg}, so a gust component
 * matches whichever the step carries.
 *
 * @param entries the valid lines, in file order
 */
public record EcmwfIndex(List<Entry> entries) {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** Copies the entries. */
    public EcmwfIndex {
        entries = List.copyOf(entries);
    }

    /**
     * One inventory line.
     *
     * @param position the line's index in {@link #entries()}
     * @param param the ECMWF short parameter name ({@code 10u}, {@code 2t}, {@code tp}, …)
     * @param levtype the level type ({@code sfc}, {@code pl}, {@code sol}, …)
     * @param levelist the level, or null when the entry has none
     * @param offset the message's first byte in the object
     * @param length the message's length, bytes
     */
    public record Entry(int position, String param, String levtype, String levelist, long offset, long length) {

        /** The message's last byte, inclusive. */
        public long end() {
            return offset + length - 1;
        }

        /** {@code PARAM:LEVTYPE[:LEVELIST]}, as health reasons name a field. */
        public String label() {
            return param + ":" + levtype + (levelist == null || levelist.isBlank() ? "" : ":" + levelist);
        }
    }

    /**
     * Parses an {@code .index} body.
     *
     * @param text the body
     * @return the inventory
     * @throws RasterFormatException when no line is a usable entry
     */
    public static EcmwfIndex parse(String text) {
        List<Entry> out = new ArrayList<>();
        if (text != null) {
            for (String line : text.split("\\r?\\n")) {
                String trimmed = line.strip();
                if (trimmed.isEmpty()) {
                    continue;
                }
                Entry entry = parseLine(trimmed, out.size());
                if (entry != null) {
                    out.add(entry);
                }
            }
        }
        if (out.isEmpty()) {
            throw new RasterFormatException("malformed ECMWF index: no valid JSON entry");
        }
        return new EcmwfIndex(out);
    }

    private static Entry parseLine(String line, int position) {
        try {
            JsonNode node = JSON.readTree(line);
            String param = text(node, "param");
            String levtype = text(node, "levtype");
            if (param == null || levtype == null || !node.has("_offset") || !node.has("_length")) {
                return null;
            }
            long offset = node.get("_offset").asLong(-1);
            long length = node.get("_length").asLong(-1);
            if (offset < 0 || length <= 0) {
                return null;
            }
            String levelist = text(node, "levelist");
            return new Entry(position, normalizeParam(param), levtype, levelist, offset, length);
        } catch (JacksonException e) {
            return null;
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String s = value.asString("");
        return s.isBlank() ? null : s;
    }

    /**
     * Folds ECMWF's step-qualified gust names onto the plain one: {@code 10fg3} and {@code 10fg6}
     * become {@code 10fg}. Other parameters are returned unchanged.
     */
    public static String normalizeParam(String param) {
        if (param != null && param.toLowerCase(Locale.ROOT).matches("10fg\\d+")) {
            return "10fg";
        }
        return param;
    }

    /**
     * The first entry matching {@code param} and {@code levtype}, and {@code levelist} when it is not
     * null; the first match wins.
     *
     * @return the entry, or empty when the inventory lacks the field
     */
    public Optional<Entry> find(String param, String levtype, String levelist) {
        String normalized = normalizeParam(param);
        for (Entry e : entries) {
            if (e.param().equals(normalized) && e.levtype().equals(levtype)
                    && (levelist == null || levelist.equals(e.levelist()))) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    /** True when {@code entry} is the last line. */
    public boolean isLast(Entry entry) {
        return entry.position() == entries.size() - 1;
    }

    /** The entries, unmodifiable. */
    public List<Entry> all() {
        return Collections.unmodifiableList(entries);
    }
}
