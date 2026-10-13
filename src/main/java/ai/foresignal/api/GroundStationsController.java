package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.media.LiveImageStore;
import ai.foresignal.source.SourceRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * The {@code Ground Stations} operations (prompt 049, Weather-Imagery-Design §6.6):
 * {@code getGroundStationPolarplot}, {@code read}.
 *
 * <p>Serves the latest {@code /polarplot.jpeg} the station's adapter fetched during an active autotrack
 * pass, held in memory only ({@link LiveImageStore}). A request never causes a fetch (invariant SRC-6).
 * An unknown or hidden source is {@code 404 NOT_FOUND}; a station with no active pass is
 * {@code 404 NO_ACTIVE_PASS}. The image changes every 15 s, so it is sent with
 * {@code Cache-Control: private, max-age=5}.
 */
@RestController
@Tag(name = "Ground Stations")
public class GroundStationsController {

    /** The id prefix of a SatDump station source. */
    static final String STATION_PREFIX = "satdump-";

    /** The cache header of a polar plot. */
    static final String CACHE_CONTROL = "private, max-age=5";

    private final LiveImageStore images;
    private final SourceRegistry registry;
    private final ReadCallerResolver callers;

    /** Creates the controller. */
    public GroundStationsController(LiveImageStore images, SourceRegistry registry, ReadCallerResolver callers) {
        this.images = images;
        this.registry = registry;
        this.callers = callers;
    }

    /** {@code getGroundStationPolarplot}: the latest polar plot of an active pass. */
    @GetMapping("/v1/ground-stations/{sourceId}/polarplot.jpeg")
    @Operation(operationId = "getGroundStationPolarplot", tags = "Ground Stations")
    public ResponseEntity<byte[]> getGroundStationPolarplot(@PathVariable String sourceId, HttpServletRequest request) {
        if (!sourceId.startsWith(STATION_PREFIX) || !registry.contains(sourceId)
                || callers.excludedSources(request).contains(sourceId)) {
            throw new NotFoundException("No SatDump ground station source '" + sourceId + "'.");
        }
        LiveImageStore.LiveImage image = images.latest(sourceId).orElseThrow(() -> new ForesignalException(
                HttpStatus.NOT_FOUND, ErrorCodes.NO_ACTIVE_PASS,
                "Ground station " + sourceId + " has no active pass, so there is no polar plot."));
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL);
        headers.setLastModified(image.fetchedAt());
        return ResponseEntity.ok().headers(headers).contentType(MediaType.IMAGE_JPEG).contentLength(image.bytes().length)
                .body(image.bytes());
    }
}
