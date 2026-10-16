package ai.foresignal.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * A scoped API token ({@code api_token}, Architecture §7.1 and §15, prompt 023).
 *
 * <p>Only the SHA-256 of the token is stored ({@link TokenHasher}); {@code token_prefix} keeps its
 * first eight characters for display. Prompt 023 adds the restriction columns, the device columns,
 * {@code created_via} and the owning principal to the Architecture columns. The bootstrap admin
 * token is the row named {@value #BOOTSTRAP_NAME} with {@code created_via = bootstrap}.
 *
 * <p>Prompt 035 adds the {@code ReadRestriction} columns ({@code allowed_area},
 * {@code exclude_private}, {@code max_requests_per_minute}) and the row {@code type}: {@code token}
 * (null on rows written before 035), or {@code feed_key} for a path-bound feed key ({@code fsf_},
 * scope {@code read}, with its allowed path {@code prefixes}); prompt 050 adds {@code tile_key}.
 * Prompt 036 adds {@code allowed_place_ids}, the {@code placeIds} restriction member.
 */
@Entity
@Table(name = "api_token", indexes = @Index(name = "api_token_hash_idx", columnList = "token_hash", unique = true))
public class ApiTokenEntity {

    /** The name of the bootstrap admin token's row. */
    public static final String BOOTSTRAP_NAME = "bootstrap";

    /** {@code type} of an API token (also what a null type means). */
    public static final String TYPE_TOKEN = "token";

    /** {@code type} of a feed key (prompt 035). */
    public static final String TYPE_FEED_KEY = "feed_key";

    /**
     * {@code type} of a tile key (prompt 050): the path-carrier feed-key variant for header-less map
     * clients ({@code fsk_}, scope {@code read}, prefixes from {@code TileKeyPolicy.ALLOWED_PREFIXES},
     * at most 30 days).
     */
    public static final String TYPE_TILE_KEY = "tile_key";

    /** Characters of the token kept in {@code token_prefix}. */
    public static final int PREFIX_LENGTH = 8;

    private static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "scope", length = 16, nullable = false)
    private String scope;

    @Column(name = "token_hash", length = 64, nullable = false)
    private String tokenHash;

    @Column(name = "token_prefix", length = PREFIX_LENGTH, nullable = false)
    private String tokenPrefix;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "expires_at")
    private Instant expiresAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "revoked_at")
    private Instant revokedAt;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_sources", columnDefinition = "text[]")
    private String[] allowedSources;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_kinds", columnDefinition = "text[]")
    private String[] allowedKinds;

    @Column(name = "allowed_bbox", columnDefinition = "geometry(Polygon,4326)")
    private Polygon allowedBbox;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_endpoints", columnDefinition = "text[]")
    private String[] allowedEndpoints;

    @Column(name = "allowed_area", columnDefinition = "geometry(Geometry,4326)")
    private org.locationtech.jts.geom.Geometry allowedArea;

    @Column(name = "exclude_private")
    private Boolean excludePrivate;

    @Column(name = "max_requests_per_minute")
    private Integer maxRequestsPerMinute;

    /** Places whose areas bound what the token reads (prompt 036). */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_place_ids", columnDefinition = "text[]")
    private String[] allowedPlaceIds;

    /** The 039 private grant: the token or feed key may see private sources; null reads as false. */
    @Column(name = "private_access")
    private Boolean privateAccess;

    /** The token's default licence policy flags (prompt 109): the floor a request cannot loosen. */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "licence_policy", columnDefinition = "text[]")
    private String[] licencePolicy;

    /** The token's licence policy flags (never null). */
    public java.util.List<String> getLicencePolicy() {
        return list(licencePolicy);
    }

    /** Stores the token's licence policy flags. */
    public void setLicencePolicy(java.util.List<String> value) {
        this.licencePolicy = array(value);
    }

    @Column(name = "type", length = 16)
    private String type;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "prefixes", columnDefinition = "text[]")
    private String[] prefixes;

    @Column(name = "device_name", length = DeviceInfo.MAX_NAME)
    private String deviceName;

    @Column(name = "device_platform", length = 16)
    private String devicePlatform;

    @Column(name = "app_version", length = DeviceInfo.MAX_APP_VERSION)
    private String appVersion;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "device_reported_at")
    private Instant deviceReportedAt;

    @Column(name = "created_via", length = 16, nullable = false)
    private String createdVia;

    @Column(name = "principal_id", length = 26, nullable = false)
    private String principalId;

    protected ApiTokenEntity() {
        // for JPA
    }

    /**
     * A new token row.
     *
     * @param id the token id
     * @param name the token's name
     * @param scope its scope
     * @param tokenHash the SHA-256 of the plaintext
     * @param tokenPrefix the plaintext's first eight characters
     * @param createdVia how it was created
     * @param principalId the owning principal
     * @param now the creation time
     */
    public ApiTokenEntity(UUID id, String name, Scope scope, String tokenHash, String tokenPrefix,
            TokenCreatedVia createdVia, String principalId, Instant now) {
        this.id = id;
        this.name = name;
        this.scope = scope.wireValue();
        this.tokenHash = tokenHash;
        this.tokenPrefix = tokenPrefix;
        this.createdVia = createdVia.wireValue();
        this.principalId = principalId;
        this.createdAt = now;
    }

    /** The token id. */
    public UUID getId() {
        return id;
    }

    /** The token's name. */
    public String getName() {
        return name;
    }

    /** The scope. */
    public Scope getScope() {
        return Scope.fromWireValue(scope);
    }

    /** The stored SHA-256 (lowercase hex). */
    public String getTokenHash() {
        return tokenHash;
    }

    /** Replaces the hash and prefix (bootstrap rotation and reset). */
    public void replaceSecret(String hash, String prefix) {
        this.tokenHash = hash;
        this.tokenPrefix = prefix;
    }

    /** The first eight characters of the plaintext. */
    public String getTokenPrefix() {
        return tokenPrefix;
    }

    /** When the token was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When the token was last used (updated at most once a minute), or null. */
    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    /** When the token expires, or null. */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** Sets the expiry. */
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    /** When the token was revoked, or null. */
    public Instant getRevokedAt() {
        return revokedAt;
    }

    /** Marks the token revoked. */
    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    /** {@code api}, {@code pairing} or {@code bootstrap}. */
    public String getCreatedVia() {
        return createdVia;
    }

    /** True for the bootstrap admin token. */
    public boolean isBootstrap() {
        return TokenCreatedVia.BOOTSTRAP.wireValue().equals(createdVia);
    }

    /** The owning principal's id. */
    public String getPrincipalId() {
        return principalId;
    }

    /** True when the token is neither revoked nor expired at {@code now}. */
    public boolean isActive(Instant now) {
        return revokedAt == null && (expiresAt == null || now.isBefore(expiresAt));
    }

    /** The restrictions (never null; {@link TokenRestrictions#NONE} when none). */
    public TokenRestrictions getRestrictions() {
        List<Double> bbox = null;
        if (allowedBbox != null) {
            Envelope e = allowedBbox.getEnvelopeInternal();
            bbox = List.of(e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY());
        }
        java.util.Map<String, Object> area = allowedArea == null ? null
                : new ai.foresignal.source.formats.geojson.GeoJsonWriter().write(allowedArea);
        return new TokenRestrictions(list(allowedSources), list(allowedKinds), bbox, list(allowedEndpoints), area,
                excludePrivate, maxRequestsPerMinute, list(allowedPlaceIds));
    }

    /** Stores {@code restrictions}; a validated bbox becomes a WGS84 polygon. */
    public void setRestrictions(TokenRestrictions restrictions) {
        TokenRestrictions r = TokenRestrictions.orNone(restrictions);
        this.allowedSources = array(r.sources());
        this.allowedKinds = array(r.kinds());
        this.allowedEndpoints = array(r.endpoints());
        this.allowedBbox = r.bbox() == null ? null : polygon(r.bbox());
        this.allowedArea = ai.foresignal.security.restrict.ReadRestriction.area(r.area());
        this.excludePrivate = r.excludePrivate();
        this.maxRequestsPerMinute = r.maxRequestsPerMinute();
        this.allowedPlaceIds = array(r.placeIds());
    }

    /** True when the token or feed key may see private sources (prompt 039 {@code privateAccess}). */
    public boolean isPrivateAccess() {
        return Boolean.TRUE.equals(privateAccess);
    }

    /** Sets {@link #isPrivateAccess()}. */
    public void setPrivateAccess(boolean privateAccess) {
        this.privateAccess = privateAccess;
    }

    /**
     * {@value #TYPE_TOKEN}, {@value #TYPE_FEED_KEY} (prompt 035) or {@value #TYPE_TILE_KEY} (prompt 050); a
     * null stored type reads as a token.
     */
    public String getType() {
        return type == null ? TYPE_TOKEN : type;
    }

    /** Sets the row type. */
    public void setType(String type) {
        this.type = type;
    }

    /** True for a feed key (prompt 035). */
    public boolean isFeedKey() {
        return TYPE_FEED_KEY.equals(type);
    }

    /** True for a tile key (prompt 050). */
    public boolean isTileKey() {
        return TYPE_TILE_KEY.equals(type);
    }

    /**
     * True for any key carried in a {@code /v1/k/{key}/…} path (a feed key or a tile key): such rows are
     * never bearer tokens and never appear in the token APIs.
     */
    public boolean isPathKey() {
        return isFeedKey() || isTileKey();
    }

    /** A feed key's allowed canonical path prefixes, or null for a token. */
    public List<String> getPrefixes() {
        return list(prefixes);
    }

    /** Sets a feed key's allowed canonical path prefixes. */
    public void setPrefixes(List<String> prefixes) {
        this.prefixes = array(prefixes);
    }

    /** The reported device, or null when none was reported. */
    public DeviceInfo getDevice() {
        if (deviceName == null && devicePlatform == null) {
            return null;
        }
        return new DeviceInfo(deviceName, devicePlatform, appVersion, deviceReportedAt);
    }

    /** Stores the device metadata and stamps when it was reported. */
    public void setDevice(DeviceInfo device, Instant reportedAt) {
        this.deviceName = device == null ? null : device.deviceName();
        this.devicePlatform = device == null ? null : device.platform();
        this.appVersion = device == null ? null : device.appVersion();
        this.deviceReportedAt = device == null ? null : reportedAt;
    }

    /** A rectangle polygon for {@code [minLon, minLat, maxLon, maxLat]}. */
    static Polygon polygon(List<Double> bbox) {
        double minLon = bbox.get(0);
        double minLat = bbox.get(1);
        double maxLon = bbox.get(2);
        double maxLat = bbox.get(3);
        return WGS84.createPolygon(new Coordinate[] {
            new Coordinate(minLon, minLat), new Coordinate(maxLon, minLat), new Coordinate(maxLon, maxLat),
            new Coordinate(minLon, maxLat), new Coordinate(minLon, minLat)
        });
    }

    private static List<String> list(String[] values) {
        return values == null ? null : Arrays.asList(values);
    }

    private static String[] array(List<String> values) {
        return values == null ? null : values.toArray(String[]::new);
    }
}
