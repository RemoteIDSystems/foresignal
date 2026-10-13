package ai.foresignal.api;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.sample.ForecastSeriesService;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.source.adapters.wx.PlaceWeatherState;
import ai.foresignal.source.adapters.wx.WxPlacesGenerator;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code getPlaceWeather} (OpenAPI tag {@code Places}, prompt 052; Weather-Imagery-Design §6.5): the place's
 * {@code place-weather} record (the {@code wx-places} head {@code place:{id}}) plus the cached GFS and HRRR
 * forecast series at the place (NWS joins in prompt 053). {@code read}, always authenticated
 * ({@code /v1/places/**}); the record is read under the caller's restrictions, so a token that may not see
 * {@code wx-places} gets {@code record: null}. Sections that are stale, failing or without coverage keep their
 * previous values with a non-ok status: unknown is never clear.
 */
@RestController
@Tag(name = "Places")
public class PlaceWeatherController {

    private final PlaceService places;
    private final RecordQueryService records;
    private final RecordMapper mapper;
    private final ForecastSeriesService forecasts;
    private final ReadCallerResolver callers;
    private final SampleService samples;
    private final JsonMapper json;

    /** Creates the controller. */
    public PlaceWeatherController(PlaceService places, RecordQueryService records, RecordMapper mapper,
            ForecastSeriesService forecasts, ReadCallerResolver callers, SampleService samples, JsonMapper json) {
        this.places = places;
        this.records = records;
        this.mapper = mapper;
        this.forecasts = forecasts;
        this.callers = callers;
        this.samples = samples;
        this.json = json;
    }

    /** {@code getPlaceWeather}. */
    @GetMapping("/v1/places/{placeId}/weather")
    @Operation(operationId = "getPlaceWeather", tags = "Places")
    @RestrictedRead
    public Map<String, Object> getPlaceWeather(@PathVariable String placeId, HttpServletRequest request) {
        PlaceView place = places.find(placeId).orElseThrow(() -> SampleService.unknownPlace(placeId));
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        Optional<RecordRow> row = records.get(WxPlacesGenerator.ID, PlaceWeatherState.externalId(place.id()), false,
                FieldSelection.ALL, GeometryOptions.DEFAULT, excluded, caller.restriction());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("placeId", place.id());
        out.put("place", place.name());
        out.put("timezone", place.timezone() == null ? "UTC" : place.timezone());
        if (row.isPresent() && !row.get().tombstone()) {
            out.put("record", mapper.record(row.get(), FieldSelection.ALL, RecordMapper.Shape.CURRENT));
            out.put("weather", row.get().payload() == null ? null : json.readValue(row.get().payload(), Map.class));
        } else {
            out.put("record", null);
            out.put("weather", null);
        }
        Geometry g = place.geometry() == null || place.geometry().isEmpty() ? place.area() : place.geometry();
        Point p = g instanceof Point point ? point : g.getInteriorPoint();
        out.put("forecastSeries", forecasts.series(p.getX(), p.getY(), excluded));
        out.put("serverTime", samples.now().toString());
        return out;
    }
}
