package ai.foresignal.source.adapters.stac;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses an Earth Search STAC ItemCollection into {@code satellite-scene} records (prompt 096).
 *
 * <p>A pure function of its inputs (adapter rule 3): no network, no clock, no database.
 *
 * <p><strong>Time base</strong> (Core-Data-Model §2): {@code observedAt} is the upstream's
 * {@code properties.updated} (falling back to {@code created}), fixed per collection; {@code eventTime} is
 * the acquisition {@code properties.datetime}. A reprocessing that moves only {@code updated} leaves the
 * payload unchanged, so it hashes equal and becomes a touch.
 *
 * <p><strong>Links only.</strong> The record carries the item's {@code self} link and, only when it is an
 * {@code https://} URL, the {@code assets.thumbnail} href. Landsat and Sentinel-1 thumbnails are
 * {@code s3://} hrefs in requester-pays buckets and are dropped, never rewritten or signed. No asset is
 * fetched.
 *
 * <p><strong>Filter guard.</strong> Earth Search ignores a CQL2 {@code filter} silently (it returns every
 * item), so when the requested cloud bound is violated the parser throws
 * {@link UpstreamFormatException} rather than ingesting. Items older than the requested window end a page
 * (they are the documented boundary), and an item after the window's upper bound is also a violation.
 */
public final class StacItemParser {

    /** The record kind. */
    public static final String KIND = "satellite-scene";

    /** The message of the filter-ignored guard. */
    public static final String FILTER_IGNORED = "search filter ignored";

    /** The sentinel kept out of {@link #assetKeys}. */
    private static final int MAX_ASSET_KEYS = 40;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final Pattern CONTROL = Pattern.compile("\\p{Cntrl}");

    private StacItemParser() {}

    /**
     * What the adapter asks the parser to keep.
     *
     * @param collection the source's STAC collection, used when an item omits it
     * @param cloudFiltered true when the collection carries {@code eo:cloud_cover}
     * @param maxCloudCover the requested maximum cloud cover, or null when not filtered
     * @param from the requested window's start; an item older than this ends the page
     * @param until the requested window's end, or null
     * @param aoi the AOI geometry the coverage percentage is measured against
     */
    public record Options(
            String collection,
            boolean cloudFiltered,
            Double maxCloudCover,
            Instant from,
            Instant until,
            Geometry aoi) {}

    /**
     * One parsed page.
     *
     * @param records the kept scenes, in response order
     * @param rows the length of {@code features}, kept or not
     * @param skipped features dropped for a missing id, time or geometry
     * @param newest the newest acquisition on the page, kept or not
     * @param olderSeen true when an item older than the window ended the page
     */
    public record Result(
            List<NormalizedRecord> records, int rows, int skipped, Instant newest, boolean olderSeen) {

        /** Copies the records. */
        public Result {
            records = List.copyOf(records);
        }
    }

    /**
     * Parses one ItemCollection.
     *
     * @throws UpstreamFormatException when the body is empty, not JSON, not a STAC ItemCollection, or
     *     violates the requested cloud or time bounds
     */
    public static Result parse(byte[] body, Options options) {
        if (body == null || body.length == 0) {
            throw new UpstreamFormatException("Earth Search response is empty", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("Earth Search response is not valid JSON: " + e.getOriginalMessage(),
                    snippet(body), e);
        }
        JsonNode features = root == null ? null : root.get("features");
        if (root == null || !root.isObject() || features == null || !features.isArray()) {
            throw new UpstreamFormatException("Earth Search response is not a STAC ItemCollection",
                    snippet(body));
        }
        List<NormalizedRecord> records = new ArrayList<>();
        int skipped = 0;
        Instant newest = null;
        boolean olderSeen = false;
        for (JsonNode feature : features) {
            JsonNode properties = feature == null ? null : feature.get("properties");
            Instant acquisition = properties == null ? null : instant(properties.get("datetime"));
            if (acquisition == null) {
                skipped++;
                continue;
            }
            guard(properties, acquisition, options);
            if (acquisition.isBefore(options.from())) {
                olderSeen = true;
                break;
            }
            NormalizedRecord record = scene(feature, properties, acquisition, options);
            if (record == null) {
                skipped++;
                continue;
            }
            records.add(record);
            if (newest == null || acquisition.isAfter(newest)) {
                newest = acquisition;
            }
        }
        return new Result(records, features.size(), skipped, newest, olderSeen);
    }

    /** The cloud and (upper) time bounds of the request; a violation means the filter was ignored. */
    private static void guard(JsonNode properties, Instant acquisition, Options options) {
        if (options.until() != null && acquisition.isAfter(options.until())) {
            throw new UpstreamFormatException(FILTER_IGNORED, "");
        }
        if (!options.cloudFiltered() || options.maxCloudCover() == null) {
            return;
        }
        Double cloud = number(properties.get("eo:cloud_cover"));
        if (cloud != null && cloud >= options.maxCloudCover()) {
            throw new UpstreamFormatException(FILTER_IGNORED, "");
        }
    }

    private static NormalizedRecord scene(JsonNode feature, JsonNode properties, Instant acquisition, Options options) {
        String id = text(feature.get("id"));
        if (id == null) {
            return null;
        }
        String collection = text(feature.get("collection"));
        if (collection == null) {
            collection = options.collection();
        }
        Geometry footprint = Footprints.read(feature.get("geometry"));
        if (footprint == null) {
            return null;
        }
        Instant observedAt = instant(properties.get("updated"));
        if (observedAt == null) {
            observedAt = instant(properties.get("created"));
        }
        if (observedAt == null) {
            return null;
        }

        Double cloudCover = number(properties.get("eo:cloud_cover"));
        Double cloud = cloudCover == null ? null : round1(cloudCover);
        String mgrsTile = text(properties.get("s2:mgrs_tile"));
        String gridCode = text(properties.get("grid:code"));
        String wrsPath = text(properties.get("landsat:wrs_path"));
        String wrsRow = text(properties.get("landsat:wrs_row"));
        String platform = text(properties.get("platform"));

        NormalizedRecord.Builder b = NormalizedRecord.builder(collection + ":" + id, KIND)
                .observedAt(observedAt)
                .eventTime(acquisition)
                .geometry(footprint);
        b.put("collection", collection);
        b.put("platform", platform);
        b.put("constellation", text(properties.get("constellation")));
        b.put("instruments", strings(properties.get("instruments")));
        b.put("cloudCover", cloud);
        b.put("mgrsTile", mgrsTile);
        b.put("gridCode", gridCode);
        b.put("wrsPath", wrsPath);
        b.put("wrsRow", wrsRow);
        b.put("sunElevation", number(properties.get("view:sun_elevation")));
        b.put("sarMode", text(properties.get("sar:instrument_mode")));
        b.put("polarizations", strings(properties.get("sar:polarizations")));
        b.put("aoiCoverPct", Footprints.aoiCoverPct(footprint, options.aoi()));
        b.put("stacItemUrl", selfLink(feature.get("links")));
        b.put("thumbnailUrl", thumbnailUrl(feature.get("assets")));
        b.put("assetKeys", assetKeys(feature.get("assets")));
        b.put("eventTime", acquisition.toString());
        b.put("title", title(platform, mgrsTile, wrsPath, wrsRow, gridCode, cloud));
        return b.build();
    }

    /** {@code platform tile cloud%}, omitting the parts the collection does not carry. */
    static String title(String platform, String mgrsTile, String wrsPath, String wrsRow, String gridCode,
            Double cloudCover) {
        StringBuilder title = new StringBuilder(platform == null ? "Scene" : platform);
        String tile = mgrsTile != null ? mgrsTile
                : (wrsPath != null && wrsRow != null ? wrsPath + "/" + wrsRow : gridCode);
        if (tile != null) {
            title.append(' ').append(tile);
        }
        if (cloudCover != null) {
            title.append(' ').append(String.format(Locale.ROOT, "%.1f", cloudCover)).append('%');
        }
        return title.toString();
    }

    /** The item's {@code self} link, or null. */
    static String selfLink(JsonNode links) {
        if (links == null || !links.isArray()) {
            return null;
        }
        for (JsonNode link : links) {
            if (link != null && "self".equals(text(link.get("rel")))) {
                String href = text(link.get("href"));
                if (href != null) {
                    return href;
                }
            }
        }
        return null;
    }

    /** The thumbnail href only when it is an {@code https://} URL; {@code s3://} hrefs are dropped. */
    static String thumbnailUrl(JsonNode assets) {
        if (assets == null || !assets.isObject()) {
            return null;
        }
        JsonNode thumbnail = assets.get("thumbnail");
        if (thumbnail == null || !thumbnail.isObject()) {
            return null;
        }
        String href = text(thumbnail.get("href"));
        return href != null && href.toLowerCase(Locale.ROOT).startsWith("https://") ? href : null;
    }

    /** The sorted asset names, capped at {@value #MAX_ASSET_KEYS}. */
    static List<String> assetKeys(JsonNode assets) {
        if (assets == null || !assets.isObject()) {
            return List.of();
        }
        Set<String> keys = new TreeSet<>();
        assets.propertyNames().forEach(keys::add);
        return keys.stream().limit(MAX_ASSET_KEYS).toList();
    }

    private static List<String> strings(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : node) {
            String value = text(item);
            if (value != null) {
                out.add(value);
            }
        }
        return out;
    }

    private static Instant instant(JsonNode node) {
        String value = text(node);
        if (value == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException e) {
            try {
                return Instant.parse(value);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private static Double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static Double number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double value = node.asDouble();
        return Double.isFinite(value) ? value : null;
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        String clean = CONTROL.matcher(node.asString()).replaceAll("").strip();
        return clean.isEmpty() ? null : clean;
    }

    private static String snippet(byte[] body) {
        int length = Math.min(body.length, UpstreamFormatException.MAX_SNIPPET_LENGTH * 4);
        return new String(body, 0, length, StandardCharsets.UTF_8);
    }
}
