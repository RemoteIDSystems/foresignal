package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.intel.ExposureService;
import ai.foresignal.intel.IntelligenceEventEntity;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.intel.IntelVisibility;
import jakarta.servlet.http.HttpServletRequest;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.source.spi.Geo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/** {@code POST /v1/exposure} (prompt 101): what is inside an area, from a geometry, place or event. */
@RestController
@Tag(name = "Intelligence")
@RequestMapping("/v1/exposure")
public class ExposureController {

    private final ExposureService exposure;
    private final PlaceService places;
    private final IntelEventService events;
    private final IntelVisibility visibility;

    /** Creates the controller. */
    public ExposureController(ExposureService exposure, PlaceService places, IntelEventService events,
            IntelVisibility visibility) {
        this.exposure = exposure;
        this.places = places;
        this.events = events;
        this.visibility = visibility;
    }

    /** {@code computeExposure}; an {@code eventId} of a private event is {@code 404} without private access. */
    @PostMapping
    @RestrictedRead
    @Operation(operationId = "computeExposure", tags = "Intelligence")
    public Map<String, Object> computeExposure(@RequestBody JsonNode body, HttpServletRequest request) {
        Geometry area = area(body, request);
        if (area == null) {
            throw ValidationException.forField("geometry", "provide geometry, placeId or eventId");
        }
        boolean population = !body.hasNonNull("population") || body.get("population").asBoolean(true);
        return exposure.exposure(area, population);
    }

    private Geometry area(JsonNode body, HttpServletRequest request) {
        if (body.hasNonNull("eventId")) {
            IntelligenceEventEntity event = events.visible(body.get("eventId").asString(),
                    visibility.seesPrivate(request));
            return event.getGeom();
        }
        if (body.hasNonNull("placeId")) {
            return places.geometry(body.get("placeId").asString()).orElse(null);
        }
        if (body.hasNonNull("geometry")) {
            return Geo.fromGeoJson(body.get("geometry").toString());
        }
        return null;
    }
}
