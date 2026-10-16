package ai.foresignal.api;

import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.ical.CalendarService;
import ai.foresignal.ical.ICalWriter;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.security.restrict.ReadScope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * iCalendar feeds (prompt 112, {@code getCalendar}): {@code GET /v1/calendars/{feed}.ics} for calendar apps,
 * which cannot send headers and subscribe with a feed key ({@code /v1/k/{key}/calendars/launches.ics}).
 * {@code read} scope (open while {@code security.openReads} is on); filters {@code bbox}, {@code placeId},
 * {@code source} and {@code kind}; a strong ETag over the document answers {@code If-None-Match} with
 * {@code 304}. The events are built by {@link CalendarService}.
 */
@RestController
@Tag(name = "Calendars")
public class CalendarsController {

    /** The iCalendar media type with its charset. */
    static final MediaType CALENDAR = new MediaType("text", "calendar", StandardCharsets.UTF_8);

    private final CalendarService calendars;
    private final ReadCallerResolver callers;
    private final InstallIdService installId;
    private final ForesignalVersion version;
    private final AppSettingService settings;

    /** Creates the controller. */
    public CalendarsController(CalendarService calendars, ReadCallerResolver callers, InstallIdService installId,
            ForesignalVersion version, AppSettingService settings) {
        this.calendars = calendars;
        this.callers = callers;
        this.installId = installId;
        this.version = version;
        this.settings = settings;
    }

    /** {@code getCalendar}: one calendar feed as RFC 5545. */
    @GetMapping("/v1/calendars/{feed}.ics")
    @Operation(operationId = "getCalendar", tags = "Calendars")
    @RestrictedRead("the record query carries the caller's restriction (narrowed by placeId); FEED egress applies")
    public ResponseEntity<String> getCalendar(@PathVariable String feed, @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String placeId, @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        ReadCaller caller = placeId == null || placeId.isBlank() ? callers.resolve(request)
                : callers.resolveScoped(request, new ReadScope(List.of(placeId.strip()), null, null, null));
        ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
        CalendarService.Result result = calendars.build(new CalendarService.Request(feed,
                QueryParams.multi("source", source), QueryParams.multi("kind", kind), BboxParser.parse(bbox),
                callers.excludedSources(caller), caller.restriction(), FeedsController.viewer(caller),
                installId.installId(), server.effectiveClientLinks().record()));
        String body = ICalWriter.write(version.version(), result.name(), result.events(), result.truncated());
        String etag = ETags.forDocument(body);
        boolean publicCache = caller.publicCache() && !FeedsController.seesPrivate(caller);
        HttpHeaders headers = ETags.cacheHeaders(new HttpHeaders(), etag, publicCache);
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(CALENDAR).body(body);
    }
}
