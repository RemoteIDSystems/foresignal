package ai.foresignal.mcp.tools;

import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import java.time.ZoneOffset;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * Resolves a record's current live position for MCP sampling (prompt 106): the head ⋈ live geometry,
 * under the caller's source exclusions and read restriction (private records are hidden without scope).
 */
public final class RecordPositions {

    private RecordPositions() {}

    /** The live position of {@code sourceId/externalId}. */
    public static SampleService.Point point(RecordQueryService records, String record, Set<String> excluded,
            ReadRestriction restriction) {
        int slash = record == null ? -1 : record.indexOf('/');
        if (slash <= 0 || slash == record.length() - 1) {
            throw new McpToolException("record must be {sourceId}/{externalId}");
        }
        String sourceId = record.substring(0, slash);
        String externalId = record.substring(slash + 1);
        RecordRow row = records.get(sourceId, externalId, false, FieldSelection.ALL, GeometryOptions.DEFAULT, excluded,
                restriction).orElseThrow(() -> new McpToolException("no live record " + record));
        if (row.geometry() == null || row.geometry().isBlank()) {
            throw new McpToolException("record " + record + " has no position");
        }
        Geometry geometry;
        try {
            geometry = Geo.fromGeoJson(row.geometry());
        } catch (RuntimeException e) {
            throw new McpToolException("record " + record + " has no readable position");
        }
        Point p = geometry instanceof Point point ? point : geometry.getCentroid();
        return new SampleService.Point(p.getX(), p.getY(), null, ZoneOffset.UTC);
    }

    /** The same point, with the observed time, for a {@code position} block. */
    public static RecordRow row(RecordQueryService records, String record, Set<String> excluded,
            ReadRestriction restriction) {
        int slash = record == null ? -1 : record.indexOf('/');
        if (slash <= 0 || slash == record.length() - 1) {
            throw new McpToolException("record must be {sourceId}/{externalId}");
        }
        return records.get(record.substring(0, slash), record.substring(slash + 1), false, FieldSelection.ALL,
                GeometryOptions.DEFAULT, excluded, restriction)
                .orElseThrow(() -> new McpToolException("no live record " + record));
    }
}
