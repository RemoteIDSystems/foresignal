package ai.foresignal.source.adapters.maritime;

import ai.foresignal.source.kit.feature.ArcGisQueryClient;
import ai.foresignal.source.kit.feature.FeatureFetch;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.ScopedFetchContext;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * What the two hand-written IMF PortWatch adapters share (prompt 088): the layer URLs, the scoped
 * {@link ArcGisQueryClient} calls, the incremental {@code date > DATE '…'} window over a daily table, and the cached
 * point table the daily rows are joined to. Not an adapter.
 *
 * <p><strong>Layers</strong> (verified 2026-10-14 under {@value #SERVICES}): {@code PortWatch_chokepoints_database}
 * and {@code PortWatch_ports_database} (points, {@code maxRecordCount} 1000), and the non-spatial tables
 * {@code Daily_Chokepoints_Data} and {@code Daily_Ports_Data} (field {@code date} is {@code esriFieldTypeDateOnly},
 * returned as {@code "2026-10-04"} in GeoJSON). Every layer publishes {@code editingInfo}, so the client's
 * "layer unchanged" short-circuit applies: when a daily table's edit date equals that of the last complete read, no
 * table query is sent. The daily tables were last edited on a Tuesday and lag about 10 days: the data updates weekly.
 *
 * <p><strong>Scopes:</strong> each layer is read through a {@link ScopedFetchContext} (scope {@code points},
 * {@code ports} or {@code daily}) so the layers' cached metadata and edit dates never overwrite each other.
 *
 * <p><strong>Window:</strong> the daily table is read with {@code date > DATE '{lastDate}'} (first run:
 * {@code backfillDays} before today), ordered by {@code date} then {@code ObjectId}, in pages of 1,000. After a
 * complete read {@code lastDate} becomes the newest date seen. A read cut by the page cap keeps the day before the
 * newest date it reached, so the next run resumes there (the repeated day is idempotent).
 */
final class PortWatchSupport {

    /** The ArcGIS services of the IMF PortWatch organization. */
    static final String SERVICES = "https://services9.arcgis.com/weJ1QsnbMYJlCHdG/ArcGIS/rest/services/";

    /** The chokepoint points. */
    static final String CHOKEPOINTS_URL = SERVICES + "PortWatch_chokepoints_database/FeatureServer/0";

    /** The daily chokepoint transits. */
    static final String DAILY_CHOKEPOINTS_URL = SERVICES + "Daily_Chokepoints_Data/FeatureServer/0";

    /** The port points. */
    static final String PORTS_URL = SERVICES + "PortWatch_ports_database/FeatureServer/0";

    /** The daily port calls and trade. */
    static final String DAILY_PORTS_URL = SERVICES + "Daily_Ports_Data/FeatureServer/0";

    /** The license of every PortWatch source. */
    static final String LICENSE = "IMF PortWatch terms (attribution: IMF PortWatch)";

    /**
     * The terms: IMF Copyright and Usage, where the PortWatch footer's "Copyright &amp; Usage" link
     * ({@code https://www.imf.org/external/terms.htm}) now lands (verified 2026-10-14). Its "Special Terms for
     * Statistical Data" allow reuse with attribution.
     */
    static final String TERMS_URL = "https://www.imf.org/en/About/copyright-and-terms";

    /** The attribution. */
    static final String ATTRIBUTION = "IMF PortWatch";

    /** The homepage. */
    static final String HOMEPAGE = "https://portwatch.imf.org";

    /** The page size of every query (the layers' {@code maxRecordCount}). */
    static final int PAGE_SIZE = 1000;

    /** The scope of the point layer. */
    static final String POINTS_SCOPE = "points";

    /** The scope of the daily table. */
    static final String DAILY_SCOPE = "daily";

    /** The daily tables' order: by date, then a stable tiebreak. */
    static final String DAILY_ORDER = "date,ObjectId";

    private PortWatchSupport() {}

    /**
     * A query of one layer.
     *
     * @param layer the layer URL
     * @param where the {@code where} clause
     * @param outFields the attributes wanted
     * @param maxPages the page cap
     * @param orderBy the {@code orderByFields} value, or null for the object id
     * @return the request
     */
    static ArcGisQueryClient.Request request(URI layer, String where, List<String> outFields, int maxPages,
            String orderBy) {
        Map<String, String> extra = new LinkedHashMap<>();
        if (orderBy != null) {
            extra.put("orderByFields", orderBy);
        }
        return new ArcGisQueryClient.Request(layer, where, outFields, PAGE_SIZE, maxPages, maxPages * PAGE_SIZE,
                null, null, false, Map.of(), extra);
    }

    /**
     * Reads a layer through {@code client} under a state scope.
     *
     * @param client the ArcGIS client
     * @param ctx the source's context
     * @param scope the state scope
     * @param request the query
     * @return the fetch (possibly {@linkplain FeatureFetch#unchanged() unchanged})
     * @throws AdapterException when a request fails
     */
    static FeatureFetch read(ArcGisQueryClient client, FetchContext ctx, String scope,
            ArcGisQueryClient.Request request) throws AdapterException {
        return client.fetch(new ScopedFetchContext(ctx, scope), request);
    }

    /**
     * Forgets the edit date of the last complete read under {@code scope}, so the next read queries the layer.
     *
     * @param state the source's state
     * @param scope the state scope
     */
    static void forceRead(AdapterStateStore state, String scope) {
        ScopedFetchContext.scoped(state, scope).remove(ArcGisQueryClient.LAST_EDIT_KEY);
    }

    /**
     * The layer's edit date as cached by the last read under {@code scope}.
     *
     * @param state the source's state
     * @param scope the state scope
     * @return the instant, or null when unknown
     */
    static Instant layerEditDate(AdapterStateStore state, String scope) {
        ArcGisQueryClient.LayerMeta meta = ArcGisQueryClient.cachedMeta(ScopedFetchContext.scoped(state, scope));
        return meta == null || meta.lastEditDate() == null ? null : Instant.ofEpochMilli(meta.lastEditDate());
    }

    /**
     * The first day after which rows are read: {@code lastDate}, or {@code backfillDays} before today.
     *
     * @param lastDate the stored last date, or null
     * @param today the UTC date of the run
     * @param backfillDays the first-run window
     * @return the exclusive lower bound
     */
    static LocalDate from(LocalDate lastDate, LocalDate today, int backfillDays) {
        return lastDate != null ? lastDate : today.minusDays(backfillDays);
    }

    /**
     * The incremental {@code where}.
     *
     * @param from the exclusive lower bound
     * @return {@code date > DATE 'yyyy-MM-dd'}
     */
    static String dateWhere(LocalDate from) {
        return "date > DATE '" + from + "'";
    }

    /**
     * The next {@code lastDate} after a read.
     *
     * @param from the bound the read used
     * @param newest the newest date it returned, or null
     * @param complete true when the read was complete
     * @return the new bound (never before {@code from})
     */
    static LocalDate nextLastDate(LocalDate from, LocalDate newest, boolean complete) {
        if (newest == null) {
            return from;
        }
        if (complete) {
            return newest.isAfter(from) ? newest : from;
        }
        LocalDate resume = newest.minusDays(1);
        return resume.isAfter(from) ? resume : from;
    }

    /**
     * A {@code date} value ({@code "2026-10-04"}, or epoch milliseconds from an older layer version).
     *
     * @param node the attribute
     * @return the date, or null
     */
    static LocalDate date(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return Instant.ofEpochMilli(node.longValue()).atZone(ZoneOffset.UTC).toLocalDate();
        }
        String text = MaritimeSources.text(node);
        if (text == null) {
            return null;
        }
        try {
            return LocalDate.parse(text.length() > 10 ? text.substring(0, 10) : text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * A stored ISO date.
     *
     * @param text the value, possibly null
     * @return the date, or null
     */
    static LocalDate storedDate(String text) {
        try {
            return text == null ? null : LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * A point of the point table.
     *
     * @param lon the longitude
     * @param lat the latitude
     * @param name the display name, or null
     */
    record Place(double lon, double lat, String name) {}

    /**
     * The point of a feature: its {@code lat}/{@code lon} attributes, else its GeoJSON point.
     *
     * @param feature the GeoJSON feature
     * @return the point, or null when it has none in range
     */
    static Place place(JsonNode feature) {
        JsonNode p = feature.get("properties");
        Double lat = p == null ? null : MaritimeSources.number(p.get("lat"));
        Double lon = p == null ? null : MaritimeSources.number(p.get("lon"));
        if (lat == null || lon == null) {
            JsonNode g = feature.get("geometry");
            JsonNode c = g == null ? null : g.get("coordinates");
            if (g != null && "Point".equals(MaritimeSources.text(g.get("type"))) && c != null && c.isArray()
                    && c.size() >= 2) {
                lon = MaritimeSources.number(c.get(0));
                lat = MaritimeSources.number(c.get(1));
            }
        }
        if (lat == null || lon == null || Math.abs(lat) > 90 || Math.abs(lon) > 180) {
            return null;
        }
        String name = p == null ? null : MaritimeSources.text(p.get("portname"));
        if (name == null && p != null) {
            name = MaritimeSources.text(p.get("fullname"));
        }
        return new Place(lon, lat, name);
    }

    /**
     * The point table as stored in state: {@code {portid: [lon, lat, name]}}, packed.
     *
     * @param places the table
     * @return the state value
     */
    static String encode(Map<String, Place> places) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        places.forEach((id, place) -> {
            ArrayNode a = root.putArray(id);
            a.add(place.lon());
            a.add(place.lat());
            if (place.name() == null) {
                a.addNull();
            } else {
                a.add(place.name());
            }
        });
        return MaritimeSources.pack(root.toString());
    }

    /**
     * The point table stored by {@link #encode}.
     *
     * @param value the state value, possibly null
     * @return the table; empty when missing or damaged
     */
    static Map<String, Place> decode(String value) {
        Map<String, Place> out = new LinkedHashMap<>();
        String text = MaritimeSources.unpack(value);
        if (text == null || text.isBlank()) {
            return out;
        }
        try {
            JsonNode root = MaritimeSources.JSON.readTree(text);
            root.properties().forEach(e -> {
                JsonNode a = e.getValue();
                if (a.isArray() && a.size() >= 2 && a.get(0).isNumber() && a.get(1).isNumber()) {
                    out.put(e.getKey(), new Place(a.get(0).doubleValue(), a.get(1).doubleValue(),
                            a.size() > 2 ? MaritimeSources.text(a.get(2)) : null));
                }
            });
        } catch (RuntimeException e) {
            out.clear();
        }
        return out;
    }

    /**
     * The attributes of a feature.
     *
     * @param feature the GeoJSON feature
     * @return its {@code properties}
     * @throws UpstreamFormatException when it has none
     */
    static JsonNode properties(JsonNode feature) {
        JsonNode p = feature.get("properties");
        if (p == null || !p.isObject()) {
            throw new UpstreamFormatException("PortWatch feature has no properties", "");
        }
        return p;
    }
}
