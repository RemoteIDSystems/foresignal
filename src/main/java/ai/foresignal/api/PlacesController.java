package ai.foresignal.api;

import ai.foresignal.api.support.QueryParams;
import ai.foresignal.place.PlaceMapper;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlacesAoiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Named places (OpenAPI tag {@code Places}, prompt 036): {@code listPlaces}, {@code createPlace},
 * {@code getPlace}, {@code updatePlace}, {@code deletePlace} and {@code getPlacesAoi}.
 *
 * <p>Reads need the {@code read} scope and are always authenticated (a home location is sensitive
 * even while {@code openReads} is on); writes need {@code agent}. Unknown ids are
 * {@code 404 UNKNOWN_PLACE}.
 */
@RestController
@Tag(name = "Places")
public class PlacesController {

    /** Largest page. */
    static final int MAX_LIMIT = 500;

    private final PlaceService places;
    private final PlacesAoiService aoi;
    private final PlaceMapper mapper;

    /** Creates the controller. */
    public PlacesController(PlaceService places, PlacesAoiService aoi, PlaceMapper mapper) {
        this.places = places;
        this.aoi = aoi;
        this.mapper = mapper;
    }

    /** {@code listPlaces}: by id, filtered by {@code kind} and {@code tag}. */
    @GetMapping("/v1/places")
    @Operation(operationId = "listPlaces", tags = "Places")
    public Map<String, Object> listPlaces(
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        String k = QueryParams.oneOf("kind", kind, List.of("user", "airport", "asset"), null);
        int size = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, 100);
        PlaceService.Page page = places.list(k, tag == null || tag.isBlank() ? null : tag.strip(), cursor, size);
        return mapper.page(page.items(), page.nextCursor());
    }

    /** {@code createPlace}: 201; 409 {@code PLACE_EXISTS} when the id is taken. */
    @PostMapping("/v1/places")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createPlace", tags = "Places")
    public Map<String, Object> createPlace(@RequestBody JsonNode body) {
        return mapper.place(places.create(body));
    }

    /** {@code getPlacesAoi}: the weather area of interest. */
    @GetMapping("/v1/places/aoi")
    @Operation(operationId = "getPlacesAoi", tags = "Places")
    public Map<String, Object> getPlacesAoi() {
        return mapper.aoi(aoi.current());
    }

    /** {@code getPlace}. */
    @GetMapping("/v1/places/{placeId}")
    @Operation(operationId = "getPlace", tags = "Places")
    public Map<String, Object> getPlace(@PathVariable String placeId) {
        return mapper.placeWithExternal(places.get(placeId));
    }

    /** {@code updatePlace}: re-materializes the rules that use it. */
    @PutMapping("/v1/places/{placeId}")
    @Operation(operationId = "updatePlace", tags = "Places")
    public Map<String, Object> updatePlace(@PathVariable String placeId, @RequestBody JsonNode body) {
        return mapper.place(places.update(placeId, body));
    }

    /** {@code deletePlace}: 409 {@code PLACE_IN_USE} unless {@code force=true}. */
    @DeleteMapping("/v1/places/{placeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deletePlace", tags = "Places")
    public void deletePlace(@PathVariable String placeId, @RequestParam(required = false) String force) {
        places.delete(placeId, QueryParams.bool("force", force, false));
    }
}
