package ai.foresignal.source.kit.feature;

import ai.foresignal.settings.Durations;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One feature-kit source, as reviewed in {@code src/main/resources/kits/features/*.json} (prompt 073).
 *
 * <p>Bound from JSON with unknown properties refused and checked by {@link FeatureSpecValidator} at startup. Field
 * references ({@code idField}, {@code titleField}, the time fields, {@code significantFields} and the other lists)
 * name <em>target</em> payload keys, never upstream attribute names.
 *
 * @param id the source id ({@code feature-…}; overlay specs {@code op-…})
 * @param name the human-readable name
 * @param description what the source holds
 * @param kind the record kind
 * @param protocol {@code arcgis}, {@code ogc} or {@code wfs}
 * @param url the layer URL (ArcGIS), the API landing URL (OGC) or the WFS endpoint
 * @param layer the ArcGIS layer id, appended to {@code url} when set
 * @param collection the OGC API collection id
 * @param typeName the WFS feature type name
 * @param format {@code geojson} (default) or {@code esrijson} (ArcGIS only: {@code f=json} converted)
 * @param query the query: ArcGIS {@code where} and {@code outFields}, OGC and WFS property {@code filters}, a
 *     {@code bbox}, an OGC {@code datetime}
 * @param idField the target key holding the external id; required
 * @param titleField the target key holding the title
 * @param fields upstream attribute name to its mapping; only these attributes reach the payload
 * @param constants target key to a fixed value added to every record
 * @param time where the record times come from
 * @param recordTtl how long a record stays current ({@code 6h}, {@code 30d}); null means it never expires
 * @param ttlAnchor {@code RECEIVED} (default) or {@code OBSERVED}
 * @param interval the default poll interval (default 15 min)
 * @param minInterval the floor on the poll interval (default 5 min)
 * @param budget the default request budget
 * @param paging page size and caps
 * @param geometry generalization and axis order
 * @param absenceTombstones true to tombstone a feature missing from two consecutive complete fetches
 * @param zeroResultsNormal true when an empty layer is unremarkable
 * @param significantFields target keys whose change is always a version
 * @param volatileFields target keys left out of the hash
 * @param searchFields target keys of the search text
 * @param tileProperties target keys copied into vector tiles
 * @param license the data license (the descriptor's {@code licence})
 * @param termsUrl the upstream's terms
 * @param attribution the credit line
 * @param homepage where a human reads about the upstream
 * @param headers extra request headers; only {@code Accept} and {@code Digitraffic-User}
 * @param credential the optional runtime credential and where it goes
 * @param retention the default raw retention (default 1825 d, the {@code event} class)
 * @param maxResponseBytes the cap on one response body (default 16 MB)
 * @param latestPerKey true when the layer holds several rows per entity (a time series per station, prompt 079):
 *     a repeated {@code idField} value then keeps the row with the newest {@code observedAt} instead of the first;
 *     needs {@code time.observedAt} to name a field
 */
public record FeatureSourceSpec(
        String id,
        String name,
        String description,
        String kind,
        String protocol,
        String url,
        String layer,
        String collection,
        String typeName,
        String format,
        Query query,
        String idField,
        String titleField,
        Map<String, FieldMapping> fields,
        Map<String, Object> constants,
        TimeSpec time,
        String recordTtl,
        String ttlAnchor,
        String interval,
        String minInterval,
        BudgetSpec budget,
        PagingSpec paging,
        GeometrySpec geometry,
        Boolean absenceTombstones,
        Boolean zeroResultsNormal,
        List<String> significantFields,
        List<String> volatileFields,
        List<String> searchFields,
        List<String> tileProperties,
        String license,
        String termsUrl,
        String attribution,
        String homepage,
        Map<String, String> headers,
        CredentialSpec credential,
        String retention,
        Integer maxResponseBytes,
        Boolean latestPerKey) {

    /** ArcGIS FeatureServer or MapServer query. */
    public static final String ARCGIS = "arcgis";

    /** OGC API – Features. */
    public static final String OGC = "ogc";

    /** WFS 2.0 GetFeature with GeoJSON output. */
    public static final String WFS = "wfs";

    /** GeoJSON output ({@code f=geojson}). */
    public static final String GEOJSON = "geojson";

    /** Esri JSON output ({@code f=json}), converted. */
    public static final String ESRIJSON = "esrijson";

    /** The default poll interval. */
    public static final Duration DEFAULT_INTERVAL = Duration.ofMinutes(15);

    /** The default floor on the poll interval. */
    public static final Duration DEFAULT_MIN_INTERVAL = Duration.ofMinutes(5);

    /** The default raw retention: the {@code event} class (Conventions). */
    public static final Duration DEFAULT_RETENTION = Duration.ofDays(1825);

    /** The default response cap. */
    public static final int DEFAULT_MAX_RESPONSE_BYTES = 16 * 1024 * 1024;

    /** Null collections become empty ones. */
    public FeatureSourceSpec {
        fields = fields == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(fields));
        constants = constants == null ? Map.of()
                : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(constants));
        significantFields = significantFields == null ? List.of() : List.copyOf(significantFields);
        volatileFields = volatileFields == null ? List.of() : List.copyOf(volatileFields);
        searchFields = searchFields == null ? List.of() : List.copyOf(searchFields);
        tileProperties = tileProperties == null ? List.of() : List.copyOf(tileProperties);
        headers = headers == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        query = query == null ? Query.EMPTY : query;
        time = time == null ? TimeSpec.RECEIVED_ONLY : time;
        paging = paging == null ? PagingSpec.DEFAULT : paging;
        geometry = geometry == null ? GeometrySpec.DEFAULT : geometry;
    }

    /** The output format, {@value #GEOJSON} when unset. */
    public String effectiveFormat() {
        return format == null || format.isBlank() ? GEOJSON : format;
    }

    /** True when a repeated id keeps its newest row ({@code latestPerKey}). */
    public boolean keepsLatestPerKey() {
        return Boolean.TRUE.equals(latestPerKey);
    }

    /** True when absence tombstones are on. */
    public boolean tombstonesAbsent() {
        return Boolean.TRUE.equals(absenceTombstones);
    }

    /** The parsed default interval. */
    public Duration intervalDuration() {
        return parse(interval, DEFAULT_INTERVAL);
    }

    /** The parsed minimum interval. */
    public Duration minIntervalDuration() {
        return parse(minInterval, DEFAULT_MIN_INTERVAL);
    }

    /** The parsed record TTL, or null. */
    public Duration recordTtlDuration() {
        return parse(recordTtl, null);
    }

    /** The parsed retention. */
    public Duration retentionDuration() {
        return parse(retention, DEFAULT_RETENTION);
    }

    /** The response cap. */
    public int effectiveMaxResponseBytes() {
        return maxResponseBytes == null ? DEFAULT_MAX_RESPONSE_BYTES : maxResponseBytes;
    }

    /** The target key of each field mapping, in declaration order. */
    public List<String> targets() {
        return fields.entrySet().stream().map(e -> e.getValue() == null || e.getValue().target() == null
                ? e.getKey() : e.getValue().target()).toList();
    }

    /** The mapping whose target is {@code target}, or null. */
    public FieldMapping mappingOf(String target) {
        for (Map.Entry<String, FieldMapping> e : fields.entrySet()) {
            FieldMapping m = e.getValue();
            String t = m == null || m.target() == null ? e.getKey() : m.target();
            if (t.equals(target)) {
                return m;
            }
        }
        return null;
    }

    /** {@code text} parsed as a duration, or {@code fallback} when blank. */
    static Duration parse(String text, Duration fallback) {
        return text == null || text.isBlank() ? fallback : Durations.parse(text);
    }

    /**
     * The query.
     *
     * @param where the ArcGIS {@code where} clause (default {@code 1=1})
     * @param filters property filters added as query parameters (OGC and WFS)
     * @param bbox {@code [minLon, minLat, maxLon, maxLat]}: sent to OGC, used by WFS for the axis-order check
     * @param outFields the ArcGIS attributes requested (default: the keys of {@code fields})
     * @param datetime the OGC {@code datetime} parameter
     */
    public record Query(String where, Map<String, String> filters, List<Double> bbox, List<String> outFields,
            String datetime) {

        /** No query. */
        public static final Query EMPTY = new Query(null, null, null, null, null);

        /** Null collections become empty ones. */
        public Query {
            filters = filters == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(filters));
            bbox = bbox == null ? List.of() : List.copyOf(bbox);
            outFields = outFields == null ? List.of() : List.copyOf(outFields);
        }
    }

    /**
     * One upstream attribute's mapping.
     *
     * @param target the payload key (default: the attribute name)
     * @param type {@code string}, {@code number}, {@code integer}, {@code boolean} or {@code timestamp}
     * @param unit a UCUM unit, or null
     * @param valueMap upstream value (as text) to the value stored instead, for coded values
     */
    public record FieldMapping(String target, String type, String unit, Map<String, Object> valueMap) {

        /** Null collections become empty ones. */
        public FieldMapping {
            valueMap = valueMap == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(valueMap));
        }
    }

    /**
     * Where the record times come from. Each names one target key; the time base is fixed per source
     * (Core-Data-Model §2), so there is never a per-record fallback between fields.
     *
     * @param observedAt a timestamp target key, or {@code RECEIVED}
     * @param eventTime a timestamp target key, or null
     * @param validFrom a timestamp target key, or null
     * @param validUntil a timestamp target key, or null
     * @param format {@code auto} (default: numbers are epoch milliseconds, text ISO-8601 or
     *     {@code yyyy-MM-dd HH:mm:ss}), {@code epoch-ms}, {@code epoch-s}, {@code iso}, or a
     *     {@code DateTimeFormatter} pattern
     * @param zone the zone of local date-times (default {@code UTC})
     */
    public record TimeSpec(String observedAt, String eventTime, String validFrom, String validUntil, String format,
            String zone) {

        /** The {@code observedAt} value meaning "when Foresignal received it". */
        public static final String RECEIVED = "RECEIVED";

        /** Times from receipt only. */
        public static final TimeSpec RECEIVED_ONLY = new TimeSpec(RECEIVED, null, null, null, null, null);

        /** True when {@code observedAt} is receipt time. */
        public boolean observedAtReceived() {
            return observedAt == null || observedAt.isBlank() || RECEIVED.equals(observedAt);
        }
    }

    /**
     * The default request budget.
     *
     * @param perMinute requests per minute
     * @param perDay requests per day
     * @param mbPerDay megabytes per day, or null
     */
    public record BudgetSpec(Integer perMinute, Integer perDay, Integer mbPerDay) {}

    /**
     * Paging.
     *
     * @param pageSize features per page (default 1000; ArcGIS caps it at the layer's {@code maxRecordCount})
     * @param maxPages pages per fetch (default 10)
     * @param maxFeatures features per fetch (default and ceiling 50,000)
     */
    public record PagingSpec(Integer pageSize, Integer maxPages, Integer maxFeatures) {

        /** The defaults. */
        public static final PagingSpec DEFAULT = new PagingSpec(null, null, null);

        /** The default page size. */
        public static final int DEFAULT_PAGE_SIZE = 1000;

        /** The default page cap. */
        public static final int DEFAULT_MAX_PAGES = 10;

        /** The default and largest feature cap. */
        public static final int DEFAULT_MAX_FEATURES = 50_000;

        /** The page size. */
        public int effectivePageSize() {
            return pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        }

        /** The page cap. */
        public int effectiveMaxPages() {
            return maxPages == null ? DEFAULT_MAX_PAGES : maxPages;
        }

        /** The feature cap. */
        public int effectiveMaxFeatures() {
            return maxFeatures == null ? DEFAULT_MAX_FEATURES : maxFeatures;
        }
    }

    /**
     * Geometry options.
     *
     * @param maxAllowableOffset ArcGIS generalization, in degrees (sent as {@code maxAllowableOffset})
     * @param geometryPrecision ArcGIS decimal places (sent as {@code geometryPrecision})
     * @param axisOrder WFS coordinate order, {@code lonlat} (default) or {@code latlon}
     */
    public record GeometrySpec(Double maxAllowableOffset, Integer geometryPrecision, String axisOrder) {

        /** The defaults. */
        public static final GeometrySpec DEFAULT = new GeometrySpec(null, null, null);

        /** True when coordinates arrive latitude first. */
        public boolean latLon() {
            return "latlon".equalsIgnoreCase(axisOrder);
        }
    }

    /**
     * A runtime credential.
     *
     * @param name the credential field name (entered with {@code PUT /v1/sources/{id}/credentials})
     * @param placement {@code query} or {@code header}
     * @param param the query parameter or header name
     * @param required true when the source cannot be enabled without it
     * @param description what to paste and where to get it
     */
    public record CredentialSpec(String name, String placement, String param, Boolean required, String description) {

        /** True for a query-parameter credential. */
        public boolean inQuery() {
            return "query".equalsIgnoreCase(placement);
        }
    }
}
