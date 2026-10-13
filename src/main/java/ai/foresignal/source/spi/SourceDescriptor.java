package ai.foresignal.source.spi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Everything fixed about a source: what it is, what it needs, and how it behaves.
 *
 * <p>Fixed means not operator-overridable. An operator chooses whether a source runs, how often and
 * at what budget; the descriptor decides what the data <em>is</em> — its tier, its time semantics,
 * which fields are significant. Those are properties of the upstream, and letting them be edited
 * would let an operator make their own history incoherent.
 *
 * <p>Built through {@link #builder(String)}, which supplies the defaults from Core-Data-Model §3
 * so an adapter states only what is unusual about its feed.
 *
 * @param id stable kebab-case identifier, at most 64 characters
 * @param name human-readable name
 * @param description what the feed contains
 * @param kinds the record kinds this source emits; non-empty
 * @param homepage where a human can read about the upstream
 * @param attribution the credit line the upstream asks for
 * @param licence the data licence, where known
 * @param termsUrl the upstream's terms
 * @param mode how data arrives
 * @param credentialFields the credentials the upstream needs
 * @param params adapter parameters
 * @param minInterval floor on the operator-set poll interval
 * @param defaultInterval the poll interval a new installation starts with
 * @param defaultBudget the request budget a new installation starts with
 * @param recordTtl how long a record stays current; null means it never expires
 * @param ttlAnchor which timestamp the TTL counts from
 * @param observedAtMode where {@code observedAt} comes from
 * @param historyTier how much history this source's records justify
 * @param zeroResultsNormal true when an empty response is unremarkable, which suppresses
 *     silent-break detection
 * @param tileProperties payload fields copied into MVT features
 * @param searchFields payload fields concatenated into the head's search text
 * @param embedFields payload fields whose text is embedded for semantic search
 * @param rankField a numeric payload field used to rank notable records in briefings
 * @param defaultIngestPolicy the policy a new installation starts with
 * @param volatileFields top-level keys excluded from hashing
 * @param significantFields top-level keys whose change is always worth a version; empty means every
 *     non-volatile key
 * @param motion where to find speed and course, for dead reckoning
 * @param thresholds numeric change thresholds
 * @param defaultRetention a STANDARD source's default retention window
 * @param kindRetention per-kind retention overrides, STANDARD only
 * @param skewTolerance how far ahead of {@code receivedAt} an {@code observedAt} may be before it is
 *     flagged
 * @param limits per-record caps
 * @param allowNullIsland true when (0,0) is a real location for this feed
 * @param fetchTimeout overall timeout for one fetch
 * @param maxResponseBytes cap on a single response body
 * @param userAgentOverride a fixed User-Agent this upstream demands; null means Foresignal's own.
 *     <strong>Not for browser spoofing.</strong> It exists for the rare upstream that rejects
 *     anything but a specific format, and using it costs the accountability that the standard
 *     User-Agent provides
 * @param products raster, field or external products this source publishes; a source with
 *     products lists {@code raster-frame} in its kinds and, through the builder, defaults to
 *     {@code zeroResultsNormal=true}
 * @param titleField the top-level payload key whose value is the record title; null means the
 *     resolver's fallbacks ({@code title}, {@code name}, {@code callsign}, {@code place})
 * @param entityKeys entity key type (for example {@code icao24}, {@code mmsi}) to the top-level
 *     payload field holding it, for the entity registry (prompt 098)
 * @param fieldSpecs declared payload fields keyed by top-level name
 * @param kindDisplay client display hints keyed by kind
 * @param aggBelowZoom tiles aggregate below this zoom (prompt 016); null means never
 * @param startField the top-level payload key of an interval start; set together with
 *     {@code endField}
 * @param endField the top-level payload key of an interval end; set together with
 *     {@code startField}
 * @param privateSource true for personal data (JSON {@code private}); prompt 039 enforces its
 *     exclusion from egress paths, and the normalizer caps its coordinate precision
 * @param privatePrecisionDecimals coordinate decimals kept for a private source, 2 to 7
 * @param custom true for a runtime-defined custom push source or external (EDP) provider (prompt
 *     024)
 * @param egress where the records may be served; {@link Egress#OPEN} unless a licence says
 *     otherwise
 * @param originator the originator id used for evidence independence (prompt 116 reads it); null
 *     means the default from the originator registry
 * @param access how the source reaches its data, or null when not declared
 * @param hiddenKinds kinds left out of the default tile layer lists (EDP atomic streams, prompt
 *     024); still queryable, searchable and usable in rules, and requestable as a layer by name
 * @param kindPresentation layer presentation hints per kind ({@code color}, {@code icon},
 *     {@code labelField}, {@code minZoom}, {@code maxZoom}, {@code tileProperties}), exposed in the
 *     kind's TileJSON as {@code foresignal:presentation}
 * @param acceptsRemoteMedia true when a remote station may push images and pass bundles to
 *     {@code POST /v1/ingest/{sourceId}/media} and {@code /rasters} (prompt 054: the {@code satdump-*}
 *     family); every other source answers those paths with {@code 404 NOT_FOUND}
 * @param accessDeniedIsTerminal true when an HTTP 403 from this upstream means "this client is refused"
 *     rather than "this request was wrong" (prompt 062: the adsb.lol sources). {@code UpstreamClient} then
 *     treats a 403 like a 451: a one-hour backoff and {@code UpstreamAccessDeniedException}. Internal, not
 *     in the OpenAPI {@code SourceDescriptor}
 * @param defaultRetryAfter the backoff after an HTTP 429 that carries no {@code Retry-After} (prompt 062:
 *     30 s for adsb.lol, after God's Eye View's {@code adsb-lol.js}); null means the exponential ladder of
 *     {@code BackoffRegistry}. Internal, not in the OpenAPI {@code SourceDescriptor}
 * @param followRedirects false when a redirect from this upstream must reach the adapter as a 3xx response
 *     instead of being followed (prompt 064: CelesTrak's usage policy counts a 301 as an error a client must
 *     stop on). True by default. Internal, not in the OpenAPI {@code SourceDescriptor}
 * @param egressOverridable true when the operator may override {@code egress} per source in
 *     {@code source_settings.egress} (prompt 069: the local receiver families, whose data is the operator's
 *     own); otherwise an override is refused with {@code 400 EGRESS_FIXED}. False by default
 * @param hostPacing the minimum spacing between the starts of two requests to one upstream host, with at most
 *     one request to that host in flight (prompt 080: {@code HostPacer}; 1.1 s for the adsb.fi and
 *     airplanes.live sources, which allow one request per second). The gate is shared by every source that
 *     requests the same host. Null (the default) means no pacing. Internal, not in the OpenAPI
 *     {@code SourceDescriptor}
 * @param accessDeniedReasons health reasons for a refusal of this client, keyed by a text the refusal's
 *     response body contains (prompt 080: airplanes.live's 403 asks for an approval email). The first key the
 *     body contains extends {@code UpstreamAccessDeniedException}'s reason with its value. Empty by default.
 *     Internal, not in the OpenAPI {@code SourceDescriptor}
 */
public record SourceDescriptor(
        String id,
        String name,
        String description,
        Set<String> kinds,
        String homepage,
        String attribution,
        String licence,
        String termsUrl,
        Mode mode,
        List<CredentialField> credentialFields,
        List<ParamSpec> params,
        Duration minInterval,
        Duration defaultInterval,
        Budget defaultBudget,
        Duration recordTtl,
        TtlAnchor ttlAnchor,
        ObservedAtMode observedAtMode,
        HistoryTier historyTier,
        boolean zeroResultsNormal,
        List<String> tileProperties,
        List<String> searchFields,
        List<String> embedFields,
        String rankField,
        IngestPolicy defaultIngestPolicy,
        List<String> volatileFields,
        List<String> significantFields,
        MotionSpec motion,
        List<FieldThreshold> thresholds,
        Duration defaultRetention,
        Map<String, Duration> kindRetention,
        Duration skewTolerance,
        RecordLimits limits,
        boolean allowNullIsland,
        Duration fetchTimeout,
        int maxResponseBytes,
        String userAgentOverride,
        List<ProductSpec> products,
        String titleField,
        Map<String, String> entityKeys,
        Map<String, FieldSpec> fieldSpecs,
        Map<String, KindDisplay> kindDisplay,
        Integer aggBelowZoom,
        String startField,
        String endField,
        @JsonProperty("private") boolean privateSource,
        int privatePrecisionDecimals,
        boolean custom,
        Egress egress,
        String originator,
        SourceAccess access,
        Set<String> hiddenKinds,
        Map<String, Map<String, Object>> kindPresentation,
        boolean acceptsRemoteMedia,
        boolean accessDeniedIsTerminal,
        Duration defaultRetryAfter,
        boolean followRedirects,
        boolean egressOverridable,
        Duration hostPacing,
        Map<String, String> accessDeniedReasons) {

    /** Coordinate decimals kept for a private source unless it says otherwise (about 11 m). */
    public static final int DEFAULT_PRIVATE_PRECISION_DECIMALS = 4;

    /** Payload fields searched unless a source says otherwise. */
    public static final List<String> DEFAULT_SEARCH_FIELDS =
            List.of("name", "title", "callsign", "place");

    public static final Duration DEFAULT_FETCH_TIMEOUT = Duration.ofSeconds(60);
    public static final int DEFAULT_MAX_RESPONSE_BYTES = 32 * 1024 * 1024;

    /** The volatile payload field of {@code raster-frame} records (prompt 042). */
    public static final String RASTER_EXPIRES_AT_FIELD = "expiresAt";

    /**
     * The credential a source with {@link #acceptsRemoteMedia} may hold: a key a remote station presents
     * like an ingest key on {@code POST /v1/ingest/{sourceId}/media} and {@code /rasters} (prompt 054).
     */
    public static final String REMOTE_MEDIA_KEY = "ingestKey";

    /** The ingest path segments a source with {@link #acceptsRemoteMedia} serves (prompt 054). */
    public static final java.util.Set<String> REMOTE_MEDIA_SEGMENTS = java.util.Set.of("media", "rasters");

    /** The default retention of {@code raster-frame} versions (Weather-Imagery-Design §2.4). */
    public static final Duration RASTER_FRAME_RETENTION = Duration.ofDays(7);

    /** How far ahead of {@code receivedAt} an {@code observedAt} may drift silently. */
    public static final Duration DEFAULT_SKEW_TOLERANCE = Duration.ofMinutes(2);

    public SourceDescriptor {
        kinds = kinds == null ? Set.of() : Set.copyOf(kinds);
        credentialFields = credentialFields == null ? List.of() : List.copyOf(credentialFields);
        params = params == null ? List.of() : List.copyOf(params);
        tileProperties = tileProperties == null ? List.of() : List.copyOf(tileProperties);
        searchFields = searchFields == null ? List.of() : List.copyOf(searchFields);
        embedFields = embedFields == null ? List.of() : List.copyOf(embedFields);
        volatileFields = volatileFields == null ? List.of() : List.copyOf(volatileFields);
        significantFields = significantFields == null ? List.of() : List.copyOf(significantFields);
        thresholds = thresholds == null ? List.of() : List.copyOf(thresholds);
        kindRetention = kindRetention == null ? Map.of() : Map.copyOf(kindRetention);
        products = products == null ? List.of() : List.copyOf(products);
        historyTier = historyTier == null ? HistoryTier.STANDARD : historyTier;
        observedAtMode = observedAtMode == null ? ObservedAtMode.UPSTREAM : observedAtMode;
        ttlAnchor = ttlAnchor == null ? TtlAnchor.defaultFor(historyTier) : ttlAnchor;
        limits = limits == null ? RecordLimits.defaults() : limits;
        skewTolerance = skewTolerance == null ? DEFAULT_SKEW_TOLERANCE : skewTolerance;
        defaultIngestPolicy = defaultIngestPolicy == null ? IngestPolicy.all() : defaultIngestPolicy;
        entityKeys = entityKeys == null ? Map.of() : Map.copyOf(entityKeys);
        fieldSpecs = fieldSpecs == null ? Map.of() : Map.copyOf(fieldSpecs);
        kindDisplay = kindDisplay == null ? Map.of() : Map.copyOf(kindDisplay);
        egress = egress == null ? Egress.OPEN : egress;
        hiddenKinds = hiddenKinds == null ? Set.of() : Set.copyOf(hiddenKinds);
        kindPresentation = kindPresentation == null ? Map.of() : copyPresentation(kindPresentation);
        hostPacing = hostPacing == null || hostPacing.isZero() || hostPacing.isNegative() ? null : hostPacing;
        accessDeniedReasons = accessDeniedReasons == null
                ? Map.of()
                : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(accessDeniedReasons));
    }

    private static Map<String, Map<String, Object>> copyPresentation(Map<String, Map<String, Object>> given) {
        Map<String, Map<String, Object>> copy = new LinkedHashMap<>();
        given.forEach((kind, hints) -> copy.put(kind, hints == null
                ? Map.of()
                : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(hints))));
        return java.util.Collections.unmodifiableMap(copy);
    }

    /** True when {@code kind} is left out of the default tile layer lists. */
    public boolean hidesKind(String kind) {
        return hiddenKinds.contains(kind);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    /** The credential fields that must be set before the source can be enabled. */
    public List<CredentialField> requiredCredentialFields() {
        return credentialFields.stream().filter(CredentialField::required).toList();
    }

    /** True when any credential is required. */
    public boolean requiresCredentials() {
        return credentialFields.stream().anyMatch(CredentialField::required);
    }

    /** The named parameter's spec, or null. */
    public ParamSpec param(String name) {
        return params.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
    }

    /** True when this source's history is a continuous track. */
    public boolean isTrack() {
        return historyTier == HistoryTier.TRACK;
    }

    /**
     * The poll interval the keyframe constraint is checked against (§5.1).
     *
     * <p>Streams are treated as 2 seconds, because a stream delivers as fast as the upstream sends
     * and has no interval of its own.
     */
    public Duration effectivePollInterval() {
        return mode == Mode.POLLING ? defaultInterval : Duration.ofSeconds(2);
    }

    /**
     * Fluent builder applying the documented defaults.
     *
     * <p>Mutable by design and not thread-safe; a descriptor is built once in an adapter's
     * constructor or a static initialiser.
     */
    public static final class Builder {

        private final String id;
        private final Set<String> kinds = new LinkedHashSet<>();
        private final List<CredentialField> credentialFields = new ArrayList<>();
        private final List<ParamSpec> params = new ArrayList<>();
        private final List<String> tileProperties = new ArrayList<>();
        private final List<String> embedFields = new ArrayList<>();
        private final List<String> volatileFields = new ArrayList<>();
        private final List<String> significantFields = new ArrayList<>();
        private final List<FieldThreshold> thresholds = new ArrayList<>();
        private final Map<String, Duration> kindRetention = new LinkedHashMap<>();
        private final List<ProductSpec> products = new ArrayList<>();
        private final Map<String, String> entityKeys = new LinkedHashMap<>();
        private final Map<String, FieldSpec> fieldSpecs = new LinkedHashMap<>();
        private final Map<String, KindDisplay> kindDisplay = new LinkedHashMap<>();
        private final Set<String> hiddenKinds = new LinkedHashSet<>();
        private final Map<String, Map<String, Object>> kindPresentation = new LinkedHashMap<>();
        private boolean acceptsRemoteMedia;
        private boolean accessDeniedIsTerminal;
        private Duration defaultRetryAfter;
        private boolean followRedirects = true;
        private boolean egressOverridable;
        private Duration hostPacing;
        private final Map<String, String> accessDeniedReasons = new LinkedHashMap<>();

        private String name;
        private String description;
        private String homepage;
        private String attribution;
        private String licence;
        private String termsUrl;
        private Mode mode = Mode.POLLING;
        private Duration minInterval;
        private Duration defaultInterval;
        private Budget defaultBudget;
        private Duration recordTtl;
        private TtlAnchor ttlAnchor;
        private ObservedAtMode observedAtMode = ObservedAtMode.UPSTREAM;
        private HistoryTier historyTier = HistoryTier.STANDARD;
        private boolean zeroResultsNormal;
        private boolean zeroResultsNormalSet;
        private List<String> searchFields = DEFAULT_SEARCH_FIELDS;
        private String rankField;
        private IngestPolicy defaultIngestPolicy;
        private MotionSpec motion;
        private Duration defaultRetention;
        private Duration skewTolerance = DEFAULT_SKEW_TOLERANCE;
        private RecordLimits limits = RecordLimits.defaults();
        private boolean allowNullIsland;
        private Duration fetchTimeout = DEFAULT_FETCH_TIMEOUT;
        private int maxResponseBytes = DEFAULT_MAX_RESPONSE_BYTES;
        private String userAgentOverride;
        private String titleField;
        private Integer aggBelowZoom;
        private String startField;
        private String endField;
        private boolean privateSource;
        private int privatePrecisionDecimals = DEFAULT_PRIVATE_PRECISION_DECIMALS;
        private boolean custom;
        private Egress egress = Egress.OPEN;
        private String originator;
        private SourceAccess access;

        private Builder(String id) {
            this.id = id;
            this.name = id;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder kind(String kind) {
            this.kinds.add(kind);
            return this;
        }

        public Builder kinds(String... kinds) {
            for (String kind : kinds) {
                this.kinds.add(kind);
            }
            return this;
        }

        public Builder homepage(String homepage) {
            this.homepage = homepage;
            return this;
        }

        public Builder attribution(String attribution) {
            this.attribution = attribution;
            return this;
        }

        public Builder licence(String licence) {
            this.licence = licence;
            return this;
        }

        public Builder termsUrl(String termsUrl) {
            this.termsUrl = termsUrl;
            return this;
        }

        public Builder mode(Mode mode) {
            this.mode = mode;
            return this;
        }

        public Builder credential(CredentialField field) {
            this.credentialFields.add(field);
            return this;
        }

        public Builder param(ParamSpec spec) {
            this.params.add(spec);
            return this;
        }

        public Builder minInterval(Duration minInterval) {
            this.minInterval = minInterval;
            return this;
        }

        public Builder defaultInterval(Duration defaultInterval) {
            this.defaultInterval = defaultInterval;
            return this;
        }

        /** Sets both the floor and the starting interval. */
        public Builder interval(Duration minInterval, Duration defaultInterval) {
            this.minInterval = minInterval;
            this.defaultInterval = defaultInterval;
            return this;
        }

        public Builder defaultBudget(Budget budget) {
            this.defaultBudget = budget;
            return this;
        }

        public Builder defaultBudget(int perMinute, int perDay) {
            this.defaultBudget = Budget.of(perMinute, perDay);
            return this;
        }

        public Builder recordTtl(Duration recordTtl) {
            this.recordTtl = recordTtl;
            return this;
        }

        public Builder ttlAnchor(TtlAnchor ttlAnchor) {
            this.ttlAnchor = ttlAnchor;
            return this;
        }

        public Builder observedAtMode(ObservedAtMode observedAtMode) {
            this.observedAtMode = observedAtMode;
            return this;
        }

        public Builder historyTier(HistoryTier historyTier) {
            this.historyTier = historyTier;
            return this;
        }

        public Builder zeroResultsNormal(boolean zeroResultsNormal) {
            this.zeroResultsNormal = zeroResultsNormal;
            this.zeroResultsNormalSet = true;
            return this;
        }

        public Builder tileProperties(String... fields) {
            this.tileProperties.addAll(List.of(fields));
            return this;
        }

        /** Replaces the default search fields. */
        public Builder searchFields(String... fields) {
            this.searchFields = List.of(fields);
            return this;
        }

        public Builder embedFields(String... fields) {
            this.embedFields.addAll(List.of(fields));
            return this;
        }

        public Builder rankField(String rankField) {
            this.rankField = rankField;
            return this;
        }

        public Builder defaultIngestPolicy(IngestPolicy policy) {
            this.defaultIngestPolicy = policy;
            return this;
        }

        public Builder volatileFields(String... fields) {
            this.volatileFields.addAll(List.of(fields));
            return this;
        }

        public Builder significantFields(String... fields) {
            this.significantFields.addAll(List.of(fields));
            return this;
        }

        public Builder motion(MotionSpec motion) {
            this.motion = motion;
            return this;
        }

        public Builder threshold(FieldThreshold threshold) {
            this.thresholds.add(threshold);
            return this;
        }

        public Builder defaultRetention(Duration defaultRetention) {
            this.defaultRetention = defaultRetention;
            return this;
        }

        public Builder kindRetention(String kind, Duration retention) {
            this.kindRetention.put(kind, retention);
            return this;
        }

        public Builder skewTolerance(Duration skewTolerance) {
            this.skewTolerance = skewTolerance;
            return this;
        }

        public Builder limits(RecordLimits limits) {
            this.limits = limits;
            return this;
        }

        public Builder allowNullIsland(boolean allowNullIsland) {
            this.allowNullIsland = allowNullIsland;
            return this;
        }

        public Builder fetchTimeout(Duration fetchTimeout) {
            this.fetchTimeout = fetchTimeout;
            return this;
        }

        public Builder maxResponseBytes(int maxResponseBytes) {
            this.maxResponseBytes = maxResponseBytes;
            return this;
        }

        /**
         * Overrides the User-Agent for an upstream that demands a specific format.
         *
         * <p>Never for browser spoofing: Foresignal's own User-Agent is what lets an upstream rate-limit
         * or contact one misbehaving install instead of blocking every Foresignal user.
         */
        public Builder userAgentOverride(String userAgentOverride) {
            this.userAgentOverride = userAgentOverride;
            return this;
        }

        public Builder product(ProductSpec product) {
            this.products.add(product);
            return this;
        }

        /** The payload key whose value is the record title. */
        public Builder titleField(String titleField) {
            this.titleField = titleField;
            return this;
        }

        /** Maps an entity key type, such as {@code icao24}, to the payload field holding it. */
        public Builder entityKey(String keyType, String field) {
            this.entityKeys.put(keyType, field);
            return this;
        }

        /** Declares one payload field. */
        public Builder fieldSpec(String field, FieldSpec spec) {
            this.fieldSpecs.put(field, spec);
            return this;
        }

        /** Sets the display hints for one kind. */
        public Builder kindDisplay(String kind, KindDisplay display) {
            this.kindDisplay.put(kind, display);
            return this;
        }

        /** Tiles aggregate below this zoom; null means never. */
        public Builder aggBelowZoom(Integer aggBelowZoom) {
            this.aggBelowZoom = aggBelowZoom;
            return this;
        }

        /** The payload keys of an interval's start and end; both or neither. */
        public Builder intervalFields(String startField, String endField) {
            this.startField = startField;
            this.endField = endField;
            return this;
        }

        /** Marks the source as personal data. */
        public Builder privateSource(boolean privateSource) {
            this.privateSource = privateSource;
            return this;
        }

        /** The coordinate decimals kept when the source is private. */
        public Builder privatePrecisionDecimals(int privatePrecisionDecimals) {
            this.privatePrecisionDecimals = privatePrecisionDecimals;
            return this;
        }

        /** Marks the source as a runtime-defined custom or external source (prompt 024). */
        public Builder custom(boolean custom) {
            this.custom = custom;
            return this;
        }

        /** Where the records may be served. */
        public Builder egress(Egress egress) {
            this.egress = egress;
            return this;
        }

        /** The originator id for evidence independence (prompt 116). */
        public Builder originator(String originator) {
            this.originator = originator;
            return this;
        }

        /** How the source reaches its data. */
        public Builder access(SourceAccess access) {
            this.access = access;
            return this;
        }

        /** Leaves {@code kind} out of the default tile layer lists. */
        public Builder hiddenKind(String kind) {
            this.hiddenKinds.add(kind);
            return this;
        }

        /** Lets a remote station push media and pass bundles to this source (prompt 054). */
        public Builder acceptsRemoteMedia(boolean value) {
            this.acceptsRemoteMedia = value;
            return this;
        }

        /**
         * Treats an HTTP 403 from this upstream as a refusal of the client (prompt 062): a one-hour backoff
         * and {@code UpstreamAccessDeniedException}, like a 451.
         */
        public Builder accessDeniedIsTerminal(boolean value) {
            this.accessDeniedIsTerminal = value;
            return this;
        }

        /** The backoff after an HTTP 429 without {@code Retry-After}; null keeps the exponential ladder. */
        public Builder defaultRetryAfter(Duration value) {
            this.defaultRetryAfter = value;
            return this;
        }

        /**
         * Whether {@code SourceHttp.get}, {@code getConditional} and {@code post} follow redirects (prompt
         * 064); with false a 3xx comes back to the adapter as a response. True unless set.
         */
        public Builder followRedirects(boolean value) {
            this.followRedirects = value;
            return this;
        }

        /**
         * Lets the operator override {@code egress} per source (prompt 069); false unless set, so a license
         * restriction cannot be lifted by configuration.
         */
        public Builder egressOverridable(boolean value) {
            this.egressOverridable = value;
            return this;
        }

        /**
         * Paces requests to each upstream host this source calls (prompt 080): one request in flight per host
         * and at least {@code spacing} between request starts, shared with every other source calling that
         * host. Null keeps requests unpaced.
         */
        public Builder hostPacing(Duration spacing) {
            this.hostPacing = spacing;
            return this;
        }

        /**
         * Adds a refusal reason (prompt 080): when a refusal's body contains {@code bodyContains}, the health
         * reason names {@code reason} instead of the generic refusal text. Keys are tried in insertion order.
         */
        public Builder accessDeniedReason(String bodyContains, String reason) {
            this.accessDeniedReasons.put(bodyContains, reason);
            return this;
        }

        /** Sets the layer presentation hints of one kind. */
        public Builder kindPresentation(String kind, Map<String, Object> hints) {
            this.kindPresentation.put(kind, hints);
            return this;
        }

        /**
         * Builds the descriptor.
         *
         * <p>A source that declares {@linkplain #product products} and the {@code raster-frame} kind
         * gets the raster defaults of prompt 042 unless it says otherwise: {@code zeroResultsNormal=true} (a fetch with no new
         * frame is normal), {@code expiresAt} as a volatile field (it changes when retention thins a
         * frame) and, for a STANDARD source, {@code kindRetention[raster-frame] = 7d}.
         */
        public SourceDescriptor build() {
            if (!products.isEmpty() && kinds.contains(ProductSpec.RECORD_KIND)) {
                if (!zeroResultsNormalSet) {
                    zeroResultsNormal = true;
                }
                if (!volatileFields.contains(RASTER_EXPIRES_AT_FIELD)) {
                    volatileFields.add(RASTER_EXPIRES_AT_FIELD);
                }
                if (historyTier != HistoryTier.TRACK) {
                    kindRetention.putIfAbsent(ProductSpec.RECORD_KIND, RASTER_FRAME_RETENTION);
                }
            }
            return new SourceDescriptor(
                    id,
                    name,
                    description,
                    kinds,
                    homepage,
                    attribution,
                    licence,
                    termsUrl,
                    mode,
                    credentialFields,
                    params,
                    minInterval,
                    defaultInterval,
                    defaultBudget,
                    recordTtl,
                    ttlAnchor,
                    observedAtMode,
                    historyTier,
                    zeroResultsNormal,
                    tileProperties,
                    searchFields,
                    embedFields,
                    rankField,
                    defaultIngestPolicy,
                    volatileFields,
                    significantFields,
                    motion,
                    thresholds,
                    defaultRetention,
                    kindRetention,
                    skewTolerance,
                    limits,
                    allowNullIsland,
                    fetchTimeout,
                    maxResponseBytes,
                    userAgentOverride,
                    products,
                    titleField,
                    entityKeys,
                    fieldSpecs,
                    kindDisplay,
                    aggBelowZoom,
                    startField,
                    endField,
                    privateSource,
                    privatePrecisionDecimals,
                    custom,
                    egress,
                    originator,
                    access,
                    hiddenKinds,
                    kindPresentation,
                    acceptsRemoteMedia,
                    accessDeniedIsTerminal,
                    defaultRetryAfter,
                    followRedirects,
                    egressOverridable,
                    hostPacing,
                    accessDeniedReasons);
        }
    }
}
