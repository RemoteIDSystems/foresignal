package ai.foresignal.stix;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.HistoryQuery;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Reads cyber records for the STIX export and the TAXII collections (prompt 141).
 *
 * <p>Both read through {@link RecordQueryService}, so private sources the caller may not see and its restriction are
 * left out by the query, and then ask {@link EgressPolicy#decide(RecordRow, EgressPath, ai.foresignal.provenance.LicenceTerms,
 * EgressPolicy.EgressContext)} on the {@link EgressPath#EXPORT} path for every record: a STIX bundle or a TAXII feed
 * is data leaving Foresignal for another system, so a private source, a {@code LOCAL_ONLY} source or license, or an
 * MLAT position never goes out this way. Refused records are counted, never named.
 *
 * <ul>
 *   <li><b>Export:</b> the current version of every record of the requested kinds observed at or after
 *       {@code since}; more than {@value #MAX_EXPORT} records is {@code 400 RESULT_TOO_LARGE} (narrow {@code since},
 *       or page through TAXII).
 *   <li><b>TAXII objects:</b> stored versions in ingest order ({@code timeField=ingested}, keyset on seq), so
 *       {@code added_after} and {@code next} page through every object version exactly once; the date added of an
 *       object is its version's ingest time.
 * </ul>
 */
@Component
public class StixService {

    /** The most records one export holds. */
    public static final int MAX_EXPORT = 50_000;

    /** The default TAXII page size. */
    public static final int DEFAULT_PAGE = 100;

    /** The largest TAXII page. */
    public static final int MAX_PAGE = 1000;

    static final int EXPORT_PAGE = 5000;

    private final RecordQueryService queries;
    private final EgressPolicy egress;

    /** Creates the service. */
    public StixService(RecordQueryService queries, EgressPolicy egress) {
        this.queries = queries;
        this.egress = egress;
    }

    /**
     * A STIX bundle.
     *
     * @param bundle the bundle object ({@code type}, {@code id}, {@code objects})
     * @param refused records left out by the egress policy
     */
    public record Bundle(Map<String, Object> bundle, int refused) {}

    /**
     * One TAXII page.
     *
     * @param objects the STIX objects
     * @param more true when another page follows
     * @param next the token of the next page, or null
     * @param firstAdded the date added of the first version read, or null when none
     * @param lastAdded the date added of the last version read, or null when none
     * @param refused records left out by the egress policy
     */
    public record Page(List<Map<String, Object>> objects, boolean more, String next, Instant firstAdded,
            Instant lastAdded, int refused) {}

    /**
     * The STIX bundle of the current records of {@code kinds} observed at or after {@code since}.
     *
     * @param kinds the kinds, each one of {@link StixMapper#KINDS}
     * @param since the lower bound on the observed time, or null for everything
     * @param excludedSources sources the caller may not read
     * @param restriction the caller's restriction
     * @param viewer the caller as the egress policy sees it
     * @throws ForesignalException {@code 400 RESULT_TOO_LARGE} above {@value #MAX_EXPORT} records
     */
    public Bundle export(List<String> kinds, Instant since, Set<String> excludedSources, ReadRestriction restriction,
            Viewer viewer) {
        List<Map<String, Object>> objects = new ArrayList<>();
        objects.add(StixMapper.identity());
        int refused = 0;
        int read = 0;
        String cursor = null;
        do {
            CurrentQuery page = new CurrentQuery(List.of(), kinds, List.of(), null, null, since, null,
                    TimeField.OBSERVED, null, true, ListOrder.ID, null, cursor, EXPORT_PAGE, FieldSelection.ALL,
                    GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false, excludedSources, restriction);
            Slices.RecordSlice slice = queries.list(page);
            read += slice.rows().size();
            if (read > MAX_EXPORT) {
                throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.RESULT_TOO_LARGE,
                        "More than " + MAX_EXPORT + " records match; narrow 'since' or page through TAXII"
                                + " (/taxii2/foresignal/collections/).");
            }
            for (RecordRow row : slice.rows()) {
                if (!allowed(row, viewer)) {
                    refused++;
                    continue;
                }
                Map<String, Object> object = StixMapper.toStix(row);
                if (object != null) {
                    objects.add(object);
                }
            }
            cursor = slice.nextCursor();
        } while (cursor != null);
        return new Bundle(bundle(objects), refused);
    }

    /**
     * One page of a TAXII collection.
     *
     * @param collection the collection
     * @param addedAfter only versions ingested strictly after this instant, or null
     * @param limit the page size (1 to {@value #MAX_PAGE})
     * @param next the token of the previous page, or null for the first
     * @param excludedSources sources the caller may not read
     * @param restriction the caller's restriction
     * @param viewer the caller as the egress policy sees it
     */
    public Page objects(TaxiiCollection collection, Instant addedAfter, int limit, String next,
            Set<String> excludedSources, ReadRestriction restriction, Viewer viewer) {
        HistoryQuery query = new HistoryQuery(List.of(), collection.kinds(), List.of(), null, null, addedAfter, null,
                TimeField.INGESTED, next, limit, FieldSelection.ALL, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON,
                false, excludedSources, restriction);
        Slices.VersionSlice slice = queries.history(query);
        List<Map<String, Object>> objects = new ArrayList<>();
        Instant first = null;
        Instant last = null;
        int refused = 0;
        for (RecordRow row : slice.rows()) {
            Instant added = row.ingestedAt() != null ? row.ingestedAt() : row.observedAt();
            if (addedAfter != null && added != null && !added.isAfter(addedAfter)) {
                continue;
            }
            if (added != null) {
                first = first == null ? added : first;
                last = added;
            }
            if (!allowed(row, viewer)) {
                refused++;
                continue;
            }
            Map<String, Object> object = StixMapper.toStix(row);
            if (object != null) {
                objects.add(object);
            }
        }
        return new Page(objects, slice.nextCursor() != null, slice.nextCursor(), first, last, refused);
    }

    /** The STIX bundle of {@code objects}; its id is a UUIDv5 of the object ids and modified times, so it is stable. */
    static Map<String, Object> bundle(List<Map<String, Object>> objects) {
        StringBuilder name = new StringBuilder("bundle");
        for (Map<String, Object> object : objects) {
            name.append('|').append(object.get("id")).append('@').append(object.get("modified"));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "bundle");
        out.put("id", StixIds.id("bundle", name.toString()));
        out.put("objects", objects);
        return out;
    }

    private boolean allowed(RecordRow row, Viewer viewer) {
        return egress.decide(row, EgressPath.EXPORT, null, EgressPolicy.EgressContext.export(viewer, false, false))
                .allowed();
    }
}
