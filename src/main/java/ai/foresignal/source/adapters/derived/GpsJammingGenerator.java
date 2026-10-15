package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.derive.Observation;
import ai.foresignal.geo.H3Cells;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-gps-jamming} (prompt 099): GPS jamming cells from the readsb-format aircraft
 * sources (which carry NACp/NIC; OpenSky does not, so it is not an input).
 *
 * <p>A 15-minute rolling window, evaluated every 5 minutes: airborne aircraft with an ADS-B position
 * (MLAT and TIS-B excluded) are bucketed into H3 res-4 cells (or 1° cells), and a cell is
 * {@code medium} when at least 30% and {@code high} when at least 50% of its aircraft report a
 * degraded navigation solution ({@code nac_p < 8} or {@code nic < 7}), with at least 3 aircraft. A
 * cell drops below medium in two consecutive windows before it is tombstoned. Fewer than 3 aircraft
 * keeps the last level with {@code insufficient-data}, tombstoned after two hours.
 */
@Component
public class GpsJammingGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-gps-jamming";

    /** The record kind. */
    public static final String KIND = "jamming-cell";

    /** The rolling window. */
    public static final Duration WINDOW = Duration.ofMinutes(15);

    /** How often the window is evaluated. */
    public static final Duration TICK = Duration.ofMinutes(5);

    /** How long a stalled cell is kept before tombstoning. */
    public static final Duration STALE_AFTER = Duration.ofHours(2);

    /** The NACp below which a solution is degraded. */
    public static final int NAC_P_OK = 8;

    /** The NIC below which a solution is degraded. */
    public static final int NIC_OK = 7;

    /** The fewest aircraft a cell needs before a level is asserted. */
    public static final int MIN_AIRCRAFT = 3;

    /** The bad ratio for a medium cell. */
    public static final double MEDIUM = 0.3;

    /** The bad ratio for a high cell. */
    public static final double HIGH = 0.5;

    /** The grid parameter. */
    public static final String GRID_PARAM = "grid";

    private final SourceDescriptor descriptor = layer(ID, "GPS jamming cells (derived)",
                    "Airborne aircraft with a degraded navigation solution (NACp < 8 or NIC < 7) per cell,"
                            + " from the readsb-format aircraft sources. A detection, not an accusation."
                            + " Automated, not a warning.",
                    "derived (input licences, e.g. ODbL for adsb.lol)", KIND)
            .param(ParamSpec.enumerated(GRID_PARAM, "h3 (res 4 cells) or deg1 (1-degree cells)", "h3",
                    List.of("h3", "deg1")))
            .build();

    private final Map<String, CellState> cells = new LinkedHashMap<>();

    /** Creates the generator. */
    public GpsJammingGenerator(ObjectProvider<SourceRegistry> registry) {
        super(registry);
    }

    /** One cell's state. */
    static final class CellState {

        String level;
        double ratio;
        int total;
        int bad;
        int belowStreak;
        Instant lastSeen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    protected Set<String> inputPrefixes() {
        return DerivedSources.READSB_PREFIXES;
    }

    @Override
    protected Set<String> inputKinds() {
        return Set.of(DerivedSources.AIRCRAFT);
    }

    @Override
    protected Duration tick() {
        return TICK;
    }

    @Override
    protected void clearState() {
        cells.clear();
    }

    @Override
    public int stateSize() {
        return cells.size();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        boolean h3 = !"deg1".equals(ctx.params().optionalString(GRID_PARAM).orElse("h3"));
        Instant windowStart = now.minus(WINDOW);
        LayerStatus status = status(ctx, windowStart);
        int cap = maxEntities(ctx.params());
        List<Observation> observations =
                ctx.observations().recent(inputSources(), Set.of(DerivedSources.AIRCRAFT), windowStart, cap);
        Map<String, Cell> seen = new LinkedHashMap<>();
        for (Observation observation : observations) {
            if (!LayerSupport.airborne(observation) || !LayerSupport.adsbPosition(observation)) {
                continue;
            }
            Integer nacp = LayerSupport.intField(observation, "nac_p");
            Integer nic = LayerSupport.intField(observation, "nic");
            if (nacp == null && nic == null) {
                continue;
            }
            Double lon = observation.longitude();
            Double lat = observation.latitude();
            if (lon == null || lat == null) {
                continue;
            }
            Cell cell = seen.computeIfAbsent(key(lon, lat, h3), k -> cell(lon, lat, h3));
            cell.total++;
            if ((nacp != null && nacp < NAC_P_OK) || (nic != null && nic < NIC_OK)) {
                cell.bad++;
            }
        }
        for (Map.Entry<String, Cell> entry : seen.entrySet()) {
            evaluate(ctx, now, status, entry.getKey(), entry.getValue(), cap);
        }
        for (Map.Entry<String, CellState> entry : new ArrayList<>(cells.entrySet())) {
            if (seen.containsKey(entry.getKey())) {
                continue;
            }
            CellState state = entry.getValue();
            if (state.lastSeen != null && Duration.between(state.lastSeen, now).compareTo(STALE_AFTER) > 0) {
                cells.remove(entry.getKey());
                ctx.tombstone(KIND, entry.getKey());
            } else if (state.level != null) {
                emit(ctx, now, LayerStatus.INSUFFICIENT_DATA, entry.getKey(), null, state.level, state.ratio,
                        state.total, state.bad, h3, status == LayerStatus.OK ? null : status);
            }
        }
    }

    private void evaluate(DerivedContext ctx, Instant now, LayerStatus coverage, String id, Cell cell, int cap) {
        double ratio = cell.total == 0 ? 0 : (double) cell.bad / cell.total;
        String level = level(cell.total, cell.bad);
        CellState state = cells.computeIfAbsent(id, k -> {
            if (cells.size() >= cap) {
                return null;
            }
            return new CellState();
        });
        if (state == null) {
            return;
        }
        state.ratio = ratio;
        state.total = cell.total;
        state.bad = cell.bad;
        state.lastSeen = now;
        if (cell.total < MIN_AIRCRAFT) {
            if (state.level != null) {
                emit(ctx, now, LayerStatus.INSUFFICIENT_DATA, id, cell.geometry, state.level, ratio, cell.total, cell.bad,
                        h3Of(cell), coverage == LayerStatus.OK ? null : coverage);
            }
            return;
        }
        if (!"low".equals(level)) {
            state.level = level;
            state.belowStreak = 0;
            emit(ctx, now, coverage == LayerStatus.OK ? LayerStatus.OK : coverage, id, cell.geometry, level, ratio,
                    cell.total, cell.bad, h3Of(cell), null);
            return;
        }
        state.belowStreak++;
        if (state.belowStreak >= 2) {
            cells.remove(id);
            ctx.tombstone(KIND, id);
        }
    }

    private boolean h3Of(Cell cell) {
        return cell.h3;
    }

    private void emit(DerivedContext ctx, Instant now, LayerStatus status, String id, Geometry geometry, String level,
            double ratio, int total, int bad, boolean h3, LayerStatus override) {
        LayerStatus effective = override != null ? override : status;
        Map<String, Object> payload = LayerPayload.base(ID, effective, LayerPayload.confidence(ratio), List.of());
        payload.put("level", level);
        payload.put("total", total);
        payload.put("bad", bad);
        payload.put("ratio", LayerPayload.ratio1(ratio));
        payload.put("grid", h3 ? "h3" : "deg1");
        payload.put("cell", id.contains(":") ? id.substring(id.indexOf(':') + 1) : id);
        NormalizedRecord.Builder builder = NormalizedRecord.builder(id, KIND).observedAt(now);
        if (geometry != null) {
            builder.geometry(geometry);
        }
        ctx.emit(builder.putAll(payload).build());
    }

    /** One cell under evaluation. */
    static final class Cell {

        final boolean h3;
        final Geometry geometry;
        final double lon;
        final double lat;
        int total;
        int bad;

        Cell(boolean h3, Geometry geometry, double lon, double lat) {
            this.h3 = h3;
            this.geometry = geometry;
            this.lon = lon;
            this.lat = lat;
        }
    }

    /** The cell's level from its aircraft counts: high at 50%, medium at 30%, else low. */
    static String level(int total, int bad) {
        if (total < MIN_AIRCRAFT) {
            return "low";
        }
        double ratio = (double) bad / total;
        return ratio >= HIGH ? "high" : ratio >= MEDIUM ? "medium" : "low";
    }

    /** The external id of the cell containing the point. */
    static String key(double lon, double lat, boolean h3) {
        if (h3) {
            return "h3:" + H3Cells.cellToAddress(H3Cells.latLngToCell(lat, lon, 4));
        }
        return "deg:" + (int) Math.floor(lat) + ":" + (int) Math.floor(lon);
    }

    /** The cell containing the point. */
    static Cell cell(double lon, double lat, boolean h3) {
        if (h3) {
            long cell = H3Cells.latLngToCell(lat, lon, 4);
            return new Cell(true, H3Cells.cellToPolygon(cell), lon, lat);
        }
        double x0 = Math.floor(lon);
        double y0 = Math.floor(lat);
        Polygon square = Geo.factory().createPolygon(new Coordinate[] {
            new Coordinate(x0, y0), new Coordinate(x0 + 1, y0), new Coordinate(x0 + 1, y0 + 1),
            new Coordinate(x0, y0 + 1), new Coordinate(x0, y0)});
        return new Cell(false, square, lon, lat);
    }
}
