package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.kml.KmlService;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.tiles.TileLayer;
import ai.foresignal.store.tiles.TileLayerResolver;
import ai.foresignal.store.tiles.TileLayerType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * KML and KMZ for Google Earth and Cesium (prompt 058): {@code getKmlNetworkLink}, {@code getKmlRecords},
 * {@code getKmlTracks} and {@code getTriggerKml}.
 *
 * <p>All need {@code read} (open while {@code security.openReads} is on). Header-less clients use a 035
 * feed key ({@code /v1/k/{key}/kml/networklink.kml}); the NetworkLink and icon URLs then carry the same
 * key, and the access log shows only its first eight characters ({@code PathKeyFilter.redact}). Responses
 * are revalidated on every use ({@code no-cache}); Google Earth refreshes on its own interval.
 */
@RestController
@Tag(name = "KML")
public class KmlController {

    /** The default NetworkLink refresh interval in seconds. */
    static final int DEFAULT_REFRESH = 60;

    /** The shortest NetworkLink refresh interval in seconds. */
    static final int MIN_REFRESH = 30;

    /** The longest NetworkLink refresh interval in seconds (one day). */
    static final int MAX_REFRESH = 86_400;

    /** A source filter that matches nothing (source ids are kebab-case and never start with a hyphen). */
    static final String NO_SOURCE = "-none-";

    private final KmlService kml;
    private final TileLayerResolver layers;
    private final ReadCallerResolver callers;
    private final AppSettingService settings;
    private final Clock clock;

    /** Creates the controller. */
    public KmlController(KmlService kml, TileLayerResolver layers, ReadCallerResolver callers,
            AppSettingService settings, Clock clock) {
        this.kml = kml;
        this.layers = layers;
        this.callers = callers;
        this.settings = settings;
        this.clock = clock;
    }

    /** {@code getKmlNetworkLink}: one self-refreshing NetworkLink per layer (default: every readable source layer). */
    @GetMapping("/v1/kml/networklink.kml")
    @Operation(operationId = "getKmlNetworkLink", tags = "KML")
    public ResponseEntity<byte[]> getKmlNetworkLink(
            @RequestParam(required = false) String layers,
            @RequestParam(required = false) String refresh,
            HttpServletRequest request) {
        int interval = QueryParams.intInRange("refresh", refresh, MIN_REFRESH, MAX_REFRESH, DEFAULT_REFRESH);
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        List<String> ids = new ArrayList<>();
        for (TileLayer layer : MapController.select(this.layers, layers, caller, excluded)) {
            ids.add(layer.id());
        }
        PublicUrls urls = PublicUrls.of(settings, request);
        return respond(kml.networkLinks(ids, interval, urls), caller, urls);
    }

    /**
     * {@code getKmlRecords}: the records of a layer, sources or kinds as Placemarks, newest first, capped
     * at 5,000; {@code 400} without any of {@code layer}, {@code source} or {@code kind}.
     */
    @GetMapping("/v1/kml/records.kml")
    @Operation(operationId = "getKmlRecords", tags = "KML")
    @RestrictedRead("the record query carries the caller's restriction; private sources need the private gate")
    public ResponseEntity<byte[]> getKmlRecords(
            @RequestParam(required = false) String layer,
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String asOf,
            @RequestParam(required = false) String format,
            HttpServletRequest request) {
        boolean kmz = kmz(format);
        BboxFilter box = BboxParser.parse(bbox);
        Instant at = QueryParams.instant("asOf", asOf);
        if (at != null && at.isAfter(clock.instant())) {
            throw new ValidationException(ErrorCodes.ASOF_INVALID, "asOf must not be in the future.", List.of());
        }
        List<String> sources = QueryParams.multi("source", source);
        List<String> kinds = QueryParams.multi("kind", kind);
        if ((layer == null || layer.isBlank()) && sources.isEmpty() && kinds.isEmpty()) {
            throw ValidationException.forField("layer", "one of layer, source or kind is required");
        }
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        String name = "Foresignal";
        if (layer != null && !layer.isBlank()) {
            TileLayer resolved = TilesController.allowed(layers.resolve(layer.strip(), excluded), caller.restriction());
            name = "Foresignal: " + resolved.name();
            if (resolved.type() == TileLayerType.SOURCE) {
                sources = narrow(sources, List.of(resolved.id()));
            } else {
                kinds = narrow(kinds, List.of(resolved.id()));
                sources = narrow(sources, resolved.sourceIds());
            }
            if (sources == null || kinds == null) {
                // The filters exclude the layer entirely: an empty, well-formed document (no source id can
                // start with a hyphen, so this matches nothing).
                sources = List.of(NO_SOURCE);
                kinds = List.of();
            }
        } else {
            name = "Foresignal: " + String.join(", ", sources.isEmpty() ? kinds : sources);
        }
        PublicUrls urls = PublicUrls.of(settings, request);
        KmlService.Selection selection = new KmlService.Selection(name, sources, kinds, box, excluded,
                caller.restriction(), FeedsController.viewer(caller));
        return respond(kml.records(selection, at, urls, kmz), caller, urls);
    }

    /**
     * {@code getKmlTracks}: {@code gx:Track} history of one entity ({@code sourceId} and
     * {@code externalId}) or of up to 100 entities of {@code kind} inside {@code bbox}.
     */
    @GetMapping("/v1/kml/tracks.kml")
    @Operation(operationId = "getKmlTracks", tags = "KML")
    @RestrictedRead("track versions outside the restriction are left out; private sources need the private gate")
    public ResponseEntity<byte[]> getKmlTracks(
            @RequestParam(required = false) String sourceId,
            @RequestParam(required = false) String externalId,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String format,
            HttpServletRequest request) {
        boolean kmz = kmz(format);
        Instant fromAt = QueryParams.instant("from", from);
        Instant toAt = QueryParams.instant("to", to);
        boolean single = sourceId != null && !sourceId.isBlank();
        if (single && (externalId == null || externalId.isBlank())) {
            throw ValidationException.forField("externalId", "is required with sourceId");
        }
        if (!single && (kind == null || kind.isBlank())) {
            throw ValidationException.forField("kind", "sourceId and externalId, or kind (with an optional bbox), are required");
        }
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        PublicUrls urls = PublicUrls.of(settings, request);
        KmlService.Selection selection = new KmlService.Selection("Foresignal tracks: " + (single ? sourceId : kind),
                List.of(), single ? List.of() : List.of(kind.strip()), BboxParser.parse(bbox), excluded,
                caller.restriction(), FeedsController.viewer(caller));
        return respond(kml.tracks(single ? sourceId.strip() : null, single ? externalId : null, selection, fromAt, toAt,
                urls, kmz), caller, urls);
    }

    /** {@code getTriggerKml}: the rule's geofence (polygon, route line plus corridor, or region union). */
    @GetMapping("/v1/triggers/{ruleId}.kml")
    @Operation(operationId = "getTriggerKml", tags = "KML")
    public ResponseEntity<byte[]> getTriggerKml(@PathVariable String ruleId, HttpServletRequest request) {
        UUID id;
        try {
            id = UUID.fromString(ruleId);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Trigger rule", ruleId);
        }
        ReadCaller caller = callers.resolve(request);
        return respond(kml.geofence(id), caller, PublicUrls.of(settings, request));
    }

    /**
     * {@code requested} narrowed to {@code allowed}: {@code allowed} when nothing was requested, the
     * intersection otherwise, or null when the intersection is empty.
     */
    static List<String> narrow(List<String> requested, List<String> allowed) {
        if (requested.isEmpty()) {
            return allowed;
        }
        List<String> out = new ArrayList<>(requested);
        out.retainAll(allowed);
        return out.isEmpty() ? null : out;
    }

    private static boolean kmz(String format) {
        return "kmz".equals(QueryParams.oneOf("format", format, List.of("kml", "kmz"), "kml"));
    }

    private static ResponseEntity<byte[]> respond(KmlService.Output output, ReadCaller caller, PublicUrls urls) {
        HttpHeaders headers = ETags.cacheHeaders(new HttpHeaders(), null, caller.publicCache() && !urls.keyed());
        if (output.mediaType().equals(ai.foresignal.kml.KmlWriter.KMZ_MEDIA_TYPE)) {
            headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment()
                    .filename("foresignal.kmz").build());
        }
        return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType(output.mediaType()))
                .body(output.body());
    }
}
