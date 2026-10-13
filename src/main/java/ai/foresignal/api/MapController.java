package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.map.MapStyleService;
import ai.foresignal.map.SpriteCatalog;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.tiles.TileLayer;
import ai.foresignal.store.tiles.TileLayerResolver;
import ai.foresignal.store.tiles.TileLayerType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code Map Styles} operations (prompt 058): {@code getMapStyle}, {@code getMapSprite} and
 * {@code getMapIcon}.
 *
 * <p>All three need {@code read} (open while {@code security.openReads} is on). The style document is
 * strongly tagged and cached for {@value #STYLE_MAX_AGE_SECONDS} s; it is {@code private} when the caller
 * sees private sources, is restricted, or came through a key-in-path URL (its follow-up URLs then carry
 * that key). Sprite files are content-addressed, so they are {@code immutable} for a year; an unknown
 * hash is {@code 404} with a {@code Link} to the current sprite.
 */
@RestController
@Tag(name = "Map Styles")
public class MapController {

    /** {@code max-age} of the style document. */
    static final long STYLE_MAX_AGE_SECONDS = 300;

    /** {@code max-age} of a content-addressed sprite file. */
    static final long SPRITE_MAX_AGE_SECONDS = 31_536_000;

    /** {@code max-age} of a KML icon (named, not content-addressed). */
    static final Duration ICON_MAX_AGE = Duration.ofDays(7);

    /** {@code Vary} of the style: it depends on the caller. */
    static final String VARY = "Accept-Encoding, Authorization";

    private final MapStyleService styles;
    private final SpriteCatalog sprite;
    private final TileLayerResolver layers;
    private final ReadCallerResolver callers;
    private final AppSettingService settings;
    private final JsonMapper json;

    /** The shared private-source visibility (prompt 039); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private SourceVisibility visibility;

    /** Creates the controller. */
    public MapController(MapStyleService styles, SpriteCatalog sprite, TileLayerResolver layers,
            ReadCallerResolver callers, AppSettingService settings, JsonMapper json) {
        this.styles = styles;
        this.sprite = sprite;
        this.layers = layers;
        this.callers = callers;
        this.settings = settings;
        this.json = json;
    }

    /**
     * {@code getMapStyle}: the version-8 overlay style for {@code layers} (default: every source layer
     * the caller may read). A requested layer the caller cannot see, or that does not exist, is left out,
     * exactly like a private source.
     */
    @GetMapping("/v1/map/style.json")
    @Operation(operationId = "getMapStyle", tags = "Map Styles")
    public ResponseEntity<String> getMapStyle(
            @RequestParam(required = false) String layers,
            @RequestParam(required = false) String theme,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        MapStyleService.Theme parsedTheme = MapStyleService.Theme.parse(
                QueryParams.oneOf("theme", theme, List.of("light", "dark"), "light"));
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        List<TileLayer> selected = select(this.layers, layers, caller, excluded);
        PublicUrls urls = PublicUrls.of(settings, request);
        String text = json.writeValueAsString(styles.style(selected, parsedTheme, urls));
        String etag = ETags.forDocument(text);
        boolean publicCache = caller.publicCache() && !seesPrivate(excluded) && !urls.keyed();
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(etag);
        headers.setCacheControl((publicCache ? "public" : "private") + ", max-age=" + STYLE_MAX_AGE_SECONDS);
        headers.set(HttpHeaders.VARY, VARY);
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers)
                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)).body(text);
    }

    /**
     * {@code getMapSprite}: {@code foresignal-{sha8}[@2x].{json|png}}. The current hash answers with
     * {@code max-age=31536000, immutable}; another hash is {@code 404} with
     * {@code Link: <…/foresignal-{current}.json>; rel="latest-version"}.
     */
    @GetMapping("/v1/map/sprites/{spriteFile}")
    @Operation(operationId = "getMapSprite", tags = "Map Styles")
    public ResponseEntity<byte[]> getMapSprite(@PathVariable String spriteFile, HttpServletRequest request) {
        Matcher m = SpriteCatalog.SPRITE_FILE.matcher(spriteFile);
        PublicUrls urls = PublicUrls.of(settings, request);
        String current = urls.api("/v1/map/sprites/" + sprite.spriteName() + ".json");
        if (!m.matches() || !m.group(1).equals(sprite.sha8())) {
            throw new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND,
                    "No sprite '" + spriteFile + "'; the current sprite is " + sprite.spriteName() + ".")
                    .withHeader(HttpHeaders.LINK, "<" + current + ">; rel=\"latest-version\"");
        }
        boolean retina = m.group(2) != null;
        boolean png = "png".equals(m.group(3));
        SpriteCatalog.Sheet sheet = sprite.sheet(retina);
        ReadCaller caller = callers.resolve(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl((caller.publicCache() && !urls.keyed() ? "public" : "private")
                + ", max-age=" + SPRITE_MAX_AGE_SECONDS + ", immutable");
        headers.setETag("\"" + sprite.sha8() + (retina ? "@2x" : "") + "." + m.group(3) + "\"");
        return ResponseEntity.ok().headers(headers)
                .contentType(png ? MediaType.IMAGE_PNG : MediaType.APPLICATION_JSON)
                .body(png ? sheet.png() : sheet.index());
    }

    /** {@code getMapIcon}: the 32 px KML icon of a sprite icon; {@code 404} for another name. */
    @GetMapping("/v1/map/icons/{name}.png")
    @Operation(operationId = "getMapIcon", tags = "Map Styles")
    public ResponseEntity<byte[]> getMapIcon(@PathVariable String name, HttpServletRequest request) {
        Optional<byte[]> icon = sprite.icon(name);
        if (icon.isEmpty()) {
            throw new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND,
                    "No map icon '" + name + "'.");
        }
        ReadCaller caller = callers.resolve(request);
        boolean keyed = PublicUrls.of(settings, request).keyed();
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl((caller.publicCache() && !keyed ? "public" : "private")
                + ", max-age=" + ICON_MAX_AGE.toSeconds());
        headers.setETag(ETags.forBytes(icon.get()));
        return ResponseEntity.ok().headers(headers).contentType(MediaType.IMAGE_PNG).body(icon.get());
    }

    /**
     * The layers of a style request: the comma-separated {@code layers} that resolve for the caller, in
     * request order without duplicates, or every source layer the caller may read when absent.
     */
    static List<TileLayer> select(TileLayerResolver layers, String requested, ReadCaller caller,
            Set<String> excluded) {
        List<TileLayer> out = new ArrayList<>();
        if (requested == null || requested.isBlank()) {
            for (TileLayer layer : layers.layers(excluded)) {
                if (layer.type() == TileLayerType.SOURCE && TilesController.layerAllowed(layer, caller.restriction())) {
                    out.add(layer);
                }
            }
            return out;
        }
        Set<String> ids = new LinkedHashSet<>();
        for (String part : requested.split(",")) {
            if (!part.isBlank()) {
                ids.add(part.strip());
            }
        }
        if (ids.size() > QueryParams.MAX_VALUES) {
            throw ai.foresignal.api.error.ValidationException.forField("layers",
                    "at most " + QueryParams.MAX_VALUES + " layers");
        }
        for (String id : ids) {
            try {
                TileLayer layer = layers.resolve(id, excluded);
                if (TilesController.layerAllowed(layer, caller.restriction())) {
                    out.add(layer);
                }
            } catch (ForesignalException unknownOrHidden) {
                // Left out, like a private source: the style never tells which layers exist.
            }
        }
        return out;
    }

    /** True when the caller can see at least one private source (the style is then never public). */
    private boolean seesPrivate(Set<String> excluded) {
        if (visibility == null) {
            return false;
        }
        Set<String> all = visibility.privateSourceIds();
        return !all.isEmpty() && !excluded.containsAll(all);
    }
}
