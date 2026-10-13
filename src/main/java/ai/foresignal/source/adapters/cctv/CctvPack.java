package ai.foresignal.source.adapters.cctv;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One reviewed camera-catalog pack, registered as the source {@link #id()} (prompt 072).
 *
 * <p>Packs are code-reviewed entries of {@link CctvPacks#REVIEWED}, never caller-supplied. A pack either requests
 * its {@link #catalogUrls()} through the budgeted client, or, when {@link #resource()} is set, reads a bundled
 * classpath catalog and makes no network request.
 *
 * @param id the source id, {@code cctv-…}
 * @param name the source name
 * @param provider the operator shown in each record's {@code provider}
 * @param homepage the upstream's home or data page
 * @param attribution the attribution text clients must show
 * @param license the license (descriptor {@code licence})
 * @param termsUrl the upstream's terms page
 * @param catalogUrls the catalog URLs, requested in order on each run; empty for a bundled pack
 * @param resource the classpath of a bundled catalog, or null
 * @param parser reads the response
 * @param allowedFrameOrigins URL prefixes ({@code scheme://host[:port]/path-prefix}) a frame URL must start with;
 *     anything else is dropped from the record
 * @param bounds the pack's plausible area; cameras outside are skipped and counted
 * @param minInterval the shortest operator interval
 * @param defaultInterval the default interval
 * @param budget the default request budget
 * @param credential an optional or required credential, or null
 * @param credentialQueryParam the query parameter the credential is sent in, or null
 * @param headers extra request headers; {@code {version}} in a value becomes Foresignal's version. Never a
 *     {@code User-Agent}
 * @param maxResponseBytes the response cap
 * @param maxPages the most pages followed through {@code rel=next} (1 for an unpaginated catalog)
 */
public record CctvPack(
        String id,
        String name,
        String provider,
        String homepage,
        String attribution,
        String license,
        String termsUrl,
        List<URI> catalogUrls,
        String resource,
        CctvPackParser parser,
        List<String> allowedFrameOrigins,
        Bounds bounds,
        Duration minInterval,
        Duration defaultInterval,
        Budget budget,
        CredentialField credential,
        String credentialQueryParam,
        Map<String, String> headers,
        int maxResponseBytes,
        int maxPages) {

    /** The default interval of a network pack. */
    public static final Duration DEFAULT_INTERVAL = Duration.ofMinutes(15);

    /** The minimum interval of a network pack. */
    public static final Duration MIN_INTERVAL = Duration.ofMinutes(10);

    /** The default response cap. */
    public static final int DEFAULT_MAX_RESPONSE_BYTES = 4 * 1024 * 1024;

    /** Normalizes the collections. */
    public CctvPack {
        catalogUrls = catalogUrls == null ? List.of() : List.copyOf(catalogUrls);
        allowedFrameOrigins = allowedFrameOrigins == null ? List.of() : List.copyOf(allowedFrameOrigins);
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    /** True when the catalog is a bundled resource and no request is made. */
    public boolean bundled() {
        return resource != null;
    }

    /**
     * This pack with every catalog URL moved under {@code base} as {@code {base}/{host}{path}?{query}} (tests only;
     * {@link CctvCatalogFamily#BASE_URL_PROPERTY}). Frame origins are unchanged: frames are never requested.
     */
    public CctvPack rebased(String base) {
        String root = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        List<URI> moved = new ArrayList<>();
        for (URI url : catalogUrls) {
            moved.add(URI.create(root + "/" + url.getHost() + url.getRawPath()
                    + (url.getRawQuery() == null ? "" : "?" + url.getRawQuery())));
        }
        return new CctvPack(id, name, provider, homepage, attribution, license, termsUrl, moved, resource, parser,
                allowedFrameOrigins, bounds, minInterval, defaultInterval, budget, credential, credentialQueryParam,
                headers, maxResponseBytes, maxPages);
    }

    /**
     * A pack's plausible area, ported from God's Eye View's {@code isLikely*Coordinate} checks where one exists.
     *
     * @param south the southern latitude
     * @param west the western longitude
     * @param north the northern latitude
     * @param east the eastern longitude
     */
    public record Bounds(double south, double west, double north, double east) {

        /**
         * True when the point is finite, in range, not the null island and inside the box (GEV
         * {@code isPlausibleLatLon} plus the pack's box).
         */
        public boolean contains(double lat, double lon) {
            if (!Double.isFinite(lat) || !Double.isFinite(lon) || Math.abs(lat) > 90 || Math.abs(lon) > 180) {
                return false;
            }
            if (lat == 0 && lon == 0) {
                return false;
            }
            return lat >= south && lat <= north && lon >= west && lon <= east;
        }
    }

    /**
     * Starts a pack.
     *
     * @param id the source id
     * @param parser the parser
     */
    public static Builder builder(String id, CctvPackParser parser) {
        return new Builder(id, parser);
    }

    /** Fluent construction of the reviewed list. */
    public static final class Builder {

        private final String id;
        private final CctvPackParser parser;
        private String name;
        private String provider;
        private String homepage;
        private String attribution;
        private String license;
        private String termsUrl;
        private final List<URI> catalogUrls = new ArrayList<>();
        private String resource;
        private final List<String> origins = new ArrayList<>();
        private Bounds bounds;
        private Duration minInterval = MIN_INTERVAL;
        private Duration defaultInterval = DEFAULT_INTERVAL;
        private Budget budget = Budget.of(2, 200);
        private CredentialField credential;
        private String credentialQueryParam;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private int maxResponseBytes = DEFAULT_MAX_RESPONSE_BYTES;
        private int maxPages = 1;

        private Builder(String id, CctvPackParser parser) {
            this.id = id;
            this.parser = parser;
        }

        /** Sets the source name. */
        public Builder name(String value) {
            this.name = value;
            return this;
        }

        /** Sets the operator. */
        public Builder provider(String value) {
            this.provider = value;
            return this;
        }

        /** Sets the home page. */
        public Builder homepage(String value) {
            this.homepage = value;
            return this;
        }

        /** Sets the attribution. */
        public Builder attribution(String value) {
            this.attribution = value;
            return this;
        }

        /** Sets the license. */
        public Builder license(String value) {
            this.license = value;
            return this;
        }

        /** Sets the terms page. */
        public Builder terms(String value) {
            this.termsUrl = value;
            return this;
        }

        /** Adds a catalog URL. */
        public Builder catalog(String url) {
            this.catalogUrls.add(URI.create(url));
            return this;
        }

        /** Makes this a bundled pack read from {@code classpath}. */
        public Builder resource(String classpath) {
            this.resource = classpath;
            return this;
        }

        /** Adds allowed frame-URL prefixes. */
        public Builder origins(String... prefixes) {
            this.origins.addAll(List.of(prefixes));
            return this;
        }

        /** Sets the plausible area. */
        public Builder bounds(double south, double west, double north, double east) {
            this.bounds = new Bounds(south, west, north, east);
            return this;
        }

        /** Sets the minimum and default interval. */
        public Builder interval(Duration min, Duration def) {
            this.minInterval = min;
            this.defaultInterval = def;
            return this;
        }

        /** Sets the default budget. */
        public Builder budget(int perMinute, int perDay) {
            this.budget = Budget.of(perMinute, perDay);
            return this;
        }

        /** Sets the credential and the query parameter that carries it. */
        public Builder credential(CredentialField field, String queryParam) {
            this.credential = field;
            this.credentialQueryParam = queryParam;
            return this;
        }

        /** Adds an extra request header. */
        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        /** Sets the response cap. */
        public Builder maxResponseBytes(int value) {
            this.maxResponseBytes = value;
            return this;
        }

        /** Sets the most pages followed. */
        public Builder maxPages(int value) {
            this.maxPages = value;
            return this;
        }

        /** The pack. */
        public CctvPack build() {
            return new CctvPack(Objects.requireNonNull(id), name, provider, homepage, attribution, license, termsUrl,
                    catalogUrls, resource, Objects.requireNonNull(parser), origins, bounds, minInterval,
                    defaultInterval, budget, credential, credentialQueryParam, headers, maxResponseBytes, maxPages);
        }
    }
}
