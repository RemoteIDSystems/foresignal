package ai.foresignal.api;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.fdsn.FdsnEvent;
import ai.foresignal.fdsn.FdsnEventQuery;
import ai.foresignal.fdsn.FdsnEventService;
import ai.foresignal.fdsn.FdsnRequestException;
import ai.foresignal.fdsn.FdsnTextWriter;
import ai.foresignal.fdsn.QuakeMlWriter;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The FDSN event web service, fdsnws-event 1.2 (prompt 141; OpenAPI tag {@code Standards}): {@code queryFdsnEvents},
 * {@code getFdsnEventVersion} and {@code getFdsnEventWadl}, so ObsPy, SeisComP and other FDSN clients can read the
 * earthquakes Foresignal collects.
 *
 * <ul>
 *   <li><b>Access:</b> {@code read}, open while {@code security.openReads} is on ({@code AccessPolicy} maps
 *       {@code /fdsnws/**} explicitly). Private sources and the caller's restriction apply exactly as on
 *       {@code /v1/records}: the query goes through the same service.
 *   <li><b>Formats:</b> {@code format=xml} (default) is QuakeML 1.2 BED, {@code format=text} the pipe-separated text.
 *   <li><b>Empty:</b> {@code 204} (FDSN convention), or {@code 404} with {@code nodata=404}.
 *   <li><b>Errors:</b> a bad or unsupported parameter is a {@code 400} with the FDSN plain-text error body (status
 *       line, details, usage URL, request, submission time and service version).
 * </ul>
 */
@RestController
@Tag(name = "Standards")
public class FdsnEventController {

    /** The service root. */
    public static final String ROOT = "/fdsnws/event/1/";

    /** The service version reported by {@code /version} and in error bodies. */
    public static final String VERSION = "1.2.0";

    private static final MediaType TEXT = new MediaType("text", "plain", StandardCharsets.UTF_8);
    private static final MediaType XML = new MediaType("application", "xml", StandardCharsets.UTF_8);

    private final FdsnEventService events;
    private final ReadCallerResolver callers;
    private final ProductUrls urls;
    private final Clock clock;

    /** Creates the controller. */
    public FdsnEventController(FdsnEventService events, ReadCallerResolver callers, ProductUrls urls, Clock clock) {
        this.events = events;
        this.callers = callers;
        this.urls = urls;
        this.clock = clock;
    }

    /** {@code queryFdsnEvents}: earthquakes as QuakeML or text; 204 (or 404) when none match. */
    @GetMapping(ROOT + "query")
    @Operation(operationId = "queryFdsnEvents", tags = "Standards")
    @RestrictedRead("earthquakes are read through RecordQueryService with the caller's restriction and excluded sources")
    public ResponseEntity<String> query(HttpServletRequest request) {
        FdsnEventQuery query;
        try {
            query = FdsnEventQuery.parse(request.getParameterMap());
        } catch (FdsnRequestException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage(), request);
        }
        ReadCaller caller = callers.resolve(request);
        List<FdsnEvent> found = events.query(query, callers.excludedSources(caller), caller.restriction());
        if (found.isEmpty()) {
            if (query.noData() == 404) {
                return error(HttpStatus.NOT_FOUND, "No data matches the selection", request);
            }
            return ResponseEntity.noContent().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(caller.publicCache() ? "public, max-age=60" : "private, max-age=60");
        if ("text".equals(query.format())) {
            return ResponseEntity.ok().headers(headers).contentType(TEXT).body(FdsnTextWriter.write(found));
        }
        return ResponseEntity.ok().headers(headers).contentType(XML)
                .body(QuakeMlWriter.write(found, clock.instant()));
    }

    /** {@code getFdsnEventVersion}: {@value #VERSION}. */
    @GetMapping(ROOT + "version")
    @Operation(operationId = "getFdsnEventVersion", tags = "Standards")
    public ResponseEntity<String> version() {
        return ResponseEntity.ok().contentType(TEXT).body(VERSION);
    }

    /** {@code getFdsnEventWadl}: a minimal WADL of the three resources and the query parameters. */
    @GetMapping(ROOT + "application.wadl")
    @Operation(operationId = "getFdsnEventWadl", tags = "Standards")
    public ResponseEntity<String> wadl(HttpServletRequest request) {
        return ResponseEntity.ok().contentType(XML).body(wadl(urls.base(request) + ROOT));
    }

    /** The WADL document with {@code base} as the resources base. */
    static String wadl(String base) {
        StringBuilder out = new StringBuilder();
        out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<application xmlns=\"http://wadl.dev.java.net/2009/02\"")
                .append(" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n")
                .append("  <doc title=\"Foresignal FDSN event web service ").append(VERSION).append("\"/>\n")
                .append("  <resources base=\"").append(escape(base)).append("\">\n")
                .append("    <resource path=\"query\">\n")
                .append("      <method name=\"GET\" id=\"query\">\n")
                .append("        <request>\n");
        param(out, "starttime", "xsd:dateTime", null);
        param(out, "endtime", "xsd:dateTime", null);
        param(out, "minlatitude", "xsd:double", "-90.0");
        param(out, "maxlatitude", "xsd:double", "90.0");
        param(out, "minlongitude", "xsd:double", "-180.0");
        param(out, "maxlongitude", "xsd:double", "180.0");
        param(out, "latitude", "xsd:double", "0.0");
        param(out, "longitude", "xsd:double", "0.0");
        param(out, "minradius", "xsd:double", "0.0");
        param(out, "maxradius", "xsd:double", "180.0");
        param(out, "minmagnitude", "xsd:double", null);
        param(out, "maxmagnitude", "xsd:double", null);
        param(out, "limit", "xsd:int", null);
        out.append("          <param name=\"orderby\" style=\"query\" type=\"xsd:string\" default=\"time\">\n");
        for (FdsnEventQuery.OrderBy order : FdsnEventQuery.OrderBy.values()) {
            out.append("            <option value=\"").append(order.wire()).append("\"/>\n");
        }
        out.append("          </param>\n")
                .append("          <param name=\"format\" style=\"query\" type=\"xsd:string\" default=\"xml\">\n")
                .append("            <option value=\"xml\" mediaType=\"application/xml\"/>\n")
                .append("            <option value=\"text\" mediaType=\"text/plain\"/>\n")
                .append("          </param>\n")
                .append("          <param name=\"nodata\" style=\"query\" type=\"xsd:int\" default=\"204\">\n")
                .append("            <option value=\"204\"/>\n")
                .append("            <option value=\"404\"/>\n")
                .append("          </param>\n")
                .append("        </request>\n")
                .append("        <response status=\"200\">\n")
                .append("          <representation mediaType=\"application/xml\"/>\n")
                .append("          <representation mediaType=\"text/plain\"/>\n")
                .append("        </response>\n")
                .append("        <response status=\"204 400 401 404 429 503\">\n")
                .append("          <representation mediaType=\"text/plain\"/>\n")
                .append("        </response>\n")
                .append("      </method>\n")
                .append("    </resource>\n")
                .append("    <resource path=\"version\">\n")
                .append("      <method name=\"GET\">\n")
                .append("        <response><representation mediaType=\"text/plain\"/></response>\n")
                .append("      </method>\n")
                .append("    </resource>\n")
                .append("    <resource path=\"application.wadl\">\n")
                .append("      <method name=\"GET\">\n")
                .append("        <response><representation mediaType=\"application/xml\"/></response>\n")
                .append("      </method>\n")
                .append("    </resource>\n")
                .append("  </resources>\n")
                .append("</application>\n");
        return out.toString();
    }

    private static void param(StringBuilder out, String name, String type, String defaultValue) {
        out.append("          <param name=\"").append(name).append("\" style=\"query\" type=\"").append(type).append('"');
        if (defaultValue != null) {
            out.append(" default=\"").append(defaultValue).append('"');
        }
        out.append("/>\n");
    }

    /** The FDSN plain-text error body (fdsnws 1.2, "Error handling"). */
    private ResponseEntity<String> error(HttpStatus status, String details, HttpServletRequest request) {
        String query = request.getQueryString();
        String url = request.getRequestURL() + (query == null ? "" : "?" + query);
        String body = "Error " + status.value() + ": " + status.getReasonPhrase() + "\n\n"
                + details + "\n\n"
                + "Usage details are available from " + urls.base(request) + ROOT + "application.wadl\n\n"
                + "Request:\n" + url + "\n\n"
                + "Request Submitted:\n" + clock.instant() + "\n\n"
                + "Service version:\n" + VERSION + "\n";
        return ResponseEntity.status(status).contentType(TEXT).body(body);
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
