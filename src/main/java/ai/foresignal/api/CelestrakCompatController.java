package ai.foresignal.api;

import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.space.CelestrakElementsService;
import ai.foresignal.space.OmmElements;
import ai.foresignal.space.OmmFormats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * CelesTrak-compatible GP output (prompt 064): {@code getCelestrakGp} at {@code /compat/celestrak/gp.php} and
 * {@code getCelestrakGpAlias} at CelesTrak's historical {@code /compat/celestrak/NORAD/elements/gp.php}, served
 * from the {@code celestrak-gp} heads and never by calling CelesTrak, so Gpredict, Look4Sat and SatDump can read
 * elements from Foresignal.
 *
 * <ul>
 *   <li><b>Selectors</b> (upper-case, as at CelesTrak; exactly one): {@code GROUP} (membership, or {@code active}
 *       for every current element set), {@code CATNR}, {@code INTDES} (prefix match, so {@code 1998-067} selects
 *       every piece), {@code NAME} (case-insensitive substring). None or several is a plain-text 400;
 *       {@code SPECIAL} is a 400 "not supported".
 *   <li><b>{@code FORMAT}</b> (case-insensitive): {@code TLE}/{@code 3LE}, {@code 2LE}, {@code JSON},
 *       {@code JSON-PRETTY}, {@code CSV}, {@code XML} (CCSDS OMM XML) and {@code KVN}; absent or unknown is CSV,
 *       as at CelesTrak since 2026-05-09. TLE output omits catalog numbers ≥ 100000 (no alpha-5).
 *   <li>An empty result is the plain text {@code No GP data found} with status 200.
 *   <li><b>Caching:</b> {@code ETag} = hash of (the source's highest seq, the normalized query),
 *       {@code Cache-Control: max-age=3600}; {@code If-None-Match} answers 304. {@code X-Foresignal-Elements-Age}
 *       gives the newest element set's age in seconds, and a {@code Warning} appears when it exceeds 3 days.
 *   <li><b>Access:</b> {@code read} ({@code AccessPolicy} rule for {@code /compat/celestrak/**}). Clients that cannot
 *       send headers pass a 035 feed key as {@code ?key=} ({@code PathKeyFilter}). A caller whose restriction hides
 *       {@code celestrak-gp} or the {@code satellite} kind, or limits the area (the elements have no geometry), gets
 *       no element sets.
 * </ul>
 */
@RestController
@Tag(name = "Space")
public class CelestrakCompatController {

    /** The canonical path. */
    public static final String PATH = "/compat/celestrak/gp.php";

    /** CelesTrak's historical path. */
    public static final String ALIAS = "/compat/celestrak/NORAD/elements/gp.php";

    /** The body of an empty result, as at CelesTrak. */
    public static final String NO_DATA = "No GP data found";

    /** The cache lifetime. */
    public static final String CACHE_CONTROL = "max-age=3600";

    /** The age above which a {@code Warning} is sent. */
    public static final Duration STALE_AFTER = Duration.ofDays(3);

    private static final MediaType TEXT = new MediaType("text", "plain", StandardCharsets.UTF_8);
    private static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);
    private static final MediaType JSON = new MediaType("application", "json", StandardCharsets.UTF_8);
    private static final MediaType XML = new MediaType("application", "xml", StandardCharsets.UTF_8);

    private final CelestrakElementsService elements;
    private final ReadCallerResolver callers;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the controller. */
    public CelestrakCompatController(CelestrakElementsService elements, ReadCallerResolver callers, JsonMapper json,
            Clock clock) {
        this.elements = elements;
        this.callers = callers;
        this.json = json;
        this.clock = clock;
    }

    /** {@code getCelestrakGp}. */
    @GetMapping(PATH)
    @Operation(operationId = "getCelestrakGp", tags = "Space")
    @RestrictedRead("element sets are celestrak-gp records; a restriction hiding the source, the kind or limiting the area yields none")
    public ResponseEntity<String> getCelestrakGp(
            @RequestParam Map<String, String> query,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        return serve(query, ifNoneMatch, request);
    }

    /** {@code getCelestrakGpAlias}. */
    @GetMapping(ALIAS)
    @Operation(operationId = "getCelestrakGpAlias", tags = "Space")
    @RestrictedRead("element sets are celestrak-gp records; a restriction hiding the source, the kind or limiting the area yields none")
    public ResponseEntity<String> getCelestrakGpAlias(
            @RequestParam Map<String, String> query,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        return serve(query, ifNoneMatch, request);
    }

    /** The output formats. */
    enum Format {
        TLE, TWO_LE, JSON, JSON_PRETTY, CSV, XML, KVN;

        /** {@code value} case-insensitively; absent or unknown is CSV. */
        static Format parse(String value) {
            if (value == null) {
                return CSV;
            }
            return switch (value.strip().toUpperCase(Locale.ROOT)) {
                case "TLE", "3LE" -> TLE;
                case "2LE" -> TWO_LE;
                case "JSON" -> JSON;
                case "JSON-PRETTY" -> JSON_PRETTY;
                case "XML" -> XML;
                case "KVN" -> KVN;
                default -> CSV;
            };
        }
    }

    /**
     * A parsed query.
     *
     * @param selector {@code GROUP}, {@code CATNR}, {@code INTDES} or {@code NAME}
     * @param value its value
     * @param format the output format
     */
    record Query(String selector, String value, Format format) {

        /** The normalized form hashed into the ETag. */
        String canonical() {
            return selector + "=" + value + "&FORMAT=" + format;
        }
    }

    private ResponseEntity<String> serve(Map<String, String> raw, String ifNoneMatch, HttpServletRequest request) {
        Map<String, String> q = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        raw.forEach((k, v) -> {
            if (!"key".equals(k)) {
                q.put(k, v);
            }
        });
        if (q.containsKey("SPECIAL")) {
            return text(HttpStatus.BAD_REQUEST, "SPECIAL is not supported");
        }
        List<String> selectors = new ArrayList<>();
        for (String s : List.of("GROUP", "CATNR", "INTDES", "NAME")) {
            if (q.containsKey(s) && q.get(s) != null && !q.get(s).isBlank()) {
                selectors.add(s);
            }
        }
        if (selectors.size() != 1) {
            return text(HttpStatus.BAD_REQUEST,
                    "Exactly one of GROUP, CATNR, INTDES or NAME is required");
        }
        String selector = selectors.get(0);
        String value = q.get(selector).strip();
        if ("CATNR".equals(selector) && !value.matches("\\d{1,9}")) {
            return text(HttpStatus.BAD_REQUEST, "CATNR must be a catalog number of 1 to 9 digits");
        }
        if ("GROUP".equals(selector) && !value.matches("[A-Za-z0-9-]{1,64}")) {
            return text(HttpStatus.BAD_REQUEST, "GROUP must be a CelesTrak group name");
        }
        Query query = new Query(selector, value, Format.parse(q.get("FORMAT")));

        boolean visible = visible(request);
        CelestrakElementsService.Status status = elements.status();
        String etag = "\"" + etag(visible ? status.maxSeq() : -1, query.canonical()) + "\"";
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(etag);
        // A feed-key response is private to its key holder (PathKeyFilter's hygiene); otherwise cacheable.
        headers.setCacheControl(request.getAttribute(ai.foresignal.security.restrict.PathKeyFilter.QUERY_KEY_ATTRIBUTE) != null
                ? "private, " + CACHE_CONTROL : CACHE_CONTROL);
        if (status.newestReceivedAt() != null && visible) {
            long age = Math.max(0, Duration.between(status.newestReceivedAt(), clock.instant()).toSeconds());
            headers.set("X-Foresignal-Elements-Age", Long.toString(age));
            if (age > STALE_AFTER.toSeconds()) {
                headers.set(HttpHeaders.WARNING, "110 - \"Elements are more than 3 days old\"");
            }
        }
        if (ifNoneMatch != null && matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        List<OmmElements> selected = visible ? select(query) : List.of();
        if (query.format() == Format.TLE || query.format() == Format.TWO_LE) {
            selected = selected.stream().filter(OmmElements::tleCompatible).toList();
        }
        if (selected.isEmpty()) {
            return ResponseEntity.ok().headers(headers).contentType(TEXT).body(NO_DATA);
        }
        Instant now = clock.instant();
        return switch (query.format()) {
            case TLE -> ResponseEntity.ok().headers(headers).contentType(TEXT).body(OmmFormats.tle(selected, true));
            case TWO_LE -> ResponseEntity.ok().headers(headers).contentType(TEXT).body(OmmFormats.tle(selected, false));
            case JSON -> ResponseEntity.ok().headers(headers).contentType(JSON).body(OmmFormats.json(selected, json, false));
            case JSON_PRETTY -> ResponseEntity.ok().headers(headers).contentType(JSON)
                    .body(OmmFormats.json(selected, json, true));
            case XML -> ResponseEntity.ok().headers(headers).contentType(XML).body(OmmFormats.xml(selected, now));
            case KVN -> ResponseEntity.ok().headers(headers).contentType(TEXT).body(OmmFormats.kvn(selected, now));
            case CSV -> ResponseEntity.ok().headers(headers).contentType(CSV).body(OmmFormats.csv(selected));
        };
    }

    /** The element sets matching {@code query}, ordered by catalog number. */
    List<OmmElements> select(Query query) {
        List<OmmElements> out = new ArrayList<>();
        String value = query.value();
        String lower = value.toLowerCase(Locale.ROOT);
        for (CelestrakElementsService.Current c : elements.all()) {
            OmmElements e = c.elements();
            boolean keep = switch (query.selector()) {
                case "GROUP" -> "active".equalsIgnoreCase(value) || c.groups().stream().anyMatch(value::equalsIgnoreCase);
                case "CATNR" -> e.norad() == Long.parseLong(value);
                case "INTDES" -> e.objectId() != null && e.objectId().toUpperCase(Locale.ROOT)
                        .startsWith(value.toUpperCase(Locale.ROOT));
                case "NAME" -> e.objectName().toLowerCase(Locale.ROOT).contains(lower);
                default -> false;
            };
            if (keep) {
                out.add(e);
            }
        }
        return out;
    }

    /**
     * False when the caller may not read {@code celestrak-gp} element sets: the source is excluded (private gate
     * or restriction), the kind is restricted away, or the restriction limits the area (the records have no
     * geometry to test).
     */
    private boolean visible(HttpServletRequest request) {
        if (callers.excludedSources(request).contains(CelestrakElementsService.SOURCE)) {
            return false;
        }
        ReadRestriction r = callers.restriction(request);
        if (r == null) {
            return true;
        }
        if (!r.allowsSource(CelestrakElementsService.SOURCE) || !r.allowsKind(CelestrakElementsService.KIND)) {
            return false;
        }
        return r.bbox() == null && r.area() == null && r.region() == null;
    }

    private static boolean matches(String ifNoneMatch, String etag) {
        for (String candidate : ifNoneMatch.split(",")) {
            String c = candidate.strip();
            if (c.startsWith("W/")) {
                c = c.substring(2);
            }
            if (c.equals("*") || c.equals(etag)) {
                return true;
            }
        }
        return false;
    }

    private static String etag(long maxSeq, String canonical) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha.digest((maxSeq + "|" + canonical).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required of every JVM", e);
        }
    }

    private static ResponseEntity<String> text(HttpStatus status, String body) {
        return ResponseEntity.status(status).contentType(TEXT).body(body);
    }
}
