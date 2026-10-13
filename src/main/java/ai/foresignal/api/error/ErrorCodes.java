package ai.foresignal.api.error;

import java.net.URI;
import java.util.Locale;

/**
 * The stable machine-readable codes carried in a problem's {@code code} property (Architecture §10).
 *
 * <p>Clients branch on these rather than on HTTP status or prose, so they must not change once
 * published.
 */
public final class ErrorCodes {

    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String BUDGET_EXCEEDED = "BUDGET_EXCEEDED";
    public static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
    public static final String UNSUPPORTED_MEDIA_TYPE = "UNSUPPORTED_MEDIA_TYPE";
    public static final String NOT_ACCEPTABLE = "NOT_ACCEPTABLE";
    public static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
    public static final String PAYLOAD_TOO_LARGE = "PAYLOAD_TOO_LARGE";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    /** 400: a source's ingest-policy override breaks a Core-Data-Model §5.1 rule. */
    public static final String INGEST_POLICY_INVALID = "INGEST_POLICY_INVALID";

    /** 400: retention was set on a TRACK source, whose window is {@code retention.trackWindow}. */
    public static final String RETENTION_TIER_FIXED = "RETENTION_TIER_FIXED";

    /** 400: a pagination cursor is malformed, tampered with, or belongs to a different query. */
    public static final String INVALID_CURSOR = "INVALID_CURSOR";

    /** 400: {@code asOf} without a {@code source} or {@code kind} filter (Core-Data-Model §12). */
    public static final String ASOF_FILTER_REQUIRED = "ASOF_FILTER_REQUIRED";

    /** 400: {@code asOf} before the retention horizon of an involved source; carries {@code horizon}. */
    public static final String ASOF_BEYOND_RETENTION = "ASOF_BEYOND_RETENTION";

    /** 400: {@code asOf} in the future, or combined with a parameter it cannot be combined with. */
    public static final String ASOF_INVALID = "ASOF_INVALID";

    /**
     * 410: {@code after} is below the purge horizon; the client must re-snapshot. Carries
     * {@code purgeHorizon} and {@code watermarkSeq}.
     */
    public static final String RESYNC_REQUIRED = "RESYNC_REQUIRED";

    /** 400: a streaming response would exceed {@code foresignal.api.stream.maxRows}. */
    public static final String RESULT_TOO_LARGE = "RESULT_TOO_LARGE";

    /** 503: a read query ran past {@code foresignal.api.query.statementTimeout}. */
    public static final String QUERY_TIMEOUT = "QUERY_TIMEOUT";

    /** 400: a track window is larger than the source's retention window (prompt 037). */
    public static final String TRACK_WINDOW_TOO_LARGE = "TRACK_WINDOW_TOO_LARGE";

    /** 400: history frames would cover more than 20,000 entities or scan 2,000,000 rows (prompt 037). */
    public static final String FRAMES_TOO_LARGE = "FRAMES_TOO_LARGE";

    /** 400: a timeline would have more than 1,000 buckets (prompt 037). */
    public static final String TIMELINE_TOO_LARGE = "TIMELINE_TOO_LARGE";

    /**
     * 400: a stats request would return more than 2,000 buckets or 10,000 cells, or a raw grid would
     * generate too many hexagons (prompt 038).
     */
    public static final String STATS_TOO_LARGE = "STATS_TOO_LARGE";

    /**
     * 503 (server-wide {@code maxClients}) or 429 (per-principal stream count, with
     * {@code Retry-After}): no further SSE stream may be opened now (prompt 015).
     */
    public static final String STREAM_LIMIT = "STREAM_LIMIT";

    /** 400: an event-stream topic that is unknown or not registered yet (prompt 015). */
    public static final String UNKNOWN_TOPIC = "UNKNOWN_TOPIC";

    /** 404: a tile layer that is neither a registered source id nor a kind of a visible source (016). */
    public static final String UNKNOWN_LAYER = "UNKNOWN_LAYER";

    /** 404: a SatDump station has no active pass, so there is no polar plot (prompt 049). */
    public static final String NO_ACTIVE_PASS = "NO_ACTIVE_PASS";

    /** 404: no place has this id (prompt 036). */
    public static final String UNKNOWN_PLACE = "UNKNOWN_PLACE";

    /** 409: a place is referenced by rules, views or briefings and {@code force} was not given (036). */
    public static final String PLACE_IN_USE = "PLACE_IN_USE";

    /** 409: a place with this id already exists (prompt 036). */
    public static final String PLACE_EXISTS = "PLACE_EXISTS";

    /** 409: a saved view with this slug already exists (prompt 036). */
    public static final String VIEW_EXISTS = "VIEW_EXISTS";

    /** 404: no saved view has this id or slug (prompt 036). */
    public static final String UNKNOWN_VIEW = "UNKNOWN_VIEW";

    /** 404: no watchlist has this id (prompt 036). */
    public static final String UNKNOWN_WATCHLIST = "UNKNOWN_WATCHLIST";

    /** 400: a watchlist would hold more than 100,000 items (prompt 036). */
    public static final String WATCHLIST_TOO_LARGE = "WATCHLIST_TOO_LARGE";

    /** A moving anchor is stale and its rule is suspended (prompt 036; reported in {@code lastError}). */
    public static final String ANCHOR_STALE = "ANCHOR_STALE";

    /** 400: a trigger condition reads a field the source declares volatile (prompt 017). */
    public static final String TRIGGER_PATH_VOLATILE = "TRIGGER_PATH_VOLATILE";

    /** 400: a rule names a place, but no Places component is installed (prompt 017; 036 adds it). */
    public static final String PLACES_UNAVAILABLE = "PLACES_UNAVAILABLE";

    /** 400: a rule asks for a feature this installation does not have (prompt 017). */
    public static final String FEATURE_UNAVAILABLE = "FEATURE_UNAVAILABLE";

    /** 400: shrinking a geofence by {@code hysteresisMeters} leaves nothing (prompt 017). */
    public static final String GEOFENCE_TOO_SMALL_FOR_HYSTERESIS = "GEOFENCE_TOO_SMALL_FOR_HYSTERESIS";

    /** 403: the token's scope is below the operation's; carries {@code requiredScope} (prompt 023). */
    public static final String INSUFFICIENT_SCOPE = "INSUFFICIENT_SCOPE";

    /** 403: the token has an endpoint restriction and no pattern matches the request (prompt 023). */
    public static final String ENDPOINT_NOT_ALLOWED = "ENDPOINT_NOT_ALLOWED";

    /**
     * 403: the token has source, kind or bbox restrictions, which are enforced from prompt 035; until
     * then such a token is refused on data reads rather than served unrestricted (prompt 023).
     */
    public static final String RESTRICTION_NOT_ENFORCED = "RESTRICTION_NOT_ENFORCED";

    /**
     * 403: the request is outside the principal's restriction: an endpoint a restricted token, feed
     * key or the anonymous profile may not call (prompt 035).
     */
    public static final String RESTRICTED = "RESTRICTED";

    /** 400: restrictions were set on an admin token, which can mint tokens anyway (prompt 035). */
    public static final String RESTRICTION_NOT_ALLOWED = "RESTRICTION_NOT_ALLOWED";

    /** 401: a {@code /v1/k/{key}/…} path carries an unknown or malformed feed key (prompt 035). */
    public static final String INVALID_FEED_KEY = "INVALID_FEED_KEY";

    /** 410: a feed key in a {@code /v1/k/{key}/…} path has expired or was revoked (prompt 035). */
    public static final String FEED_KEY_GONE = "FEED_KEY_GONE";

    /**
     * 401: a {@code /v1/k/{key}/…} path carries an unknown, expired or revoked tile key ({@code fsk_},
     * prompt 050).
     */
    public static final String INVALID_TILE_KEY = "INVALID_TILE_KEY";

    /** 409: the bootstrap admin token cannot be revoked; rotate it instead (prompt 023). */
    public static final String BOOTSTRAP_TOKEN = "BOOTSTRAP_TOKEN";

    /** 400: an unknown, expired or already claimed pairing code (prompt 023). */
    public static final String PAIRING_CODE_INVALID = "PAIRING_CODE_INVALID";

    /**
     * 429: too many attempts; honour {@code Retry-After} (pairing claims, prompt 023; per-client rate
     * limits and auth-failure backoff, prompt 035).
     */
    public static final String RATE_LIMITED = "RATE_LIMITED";

    // ---- Inbound webhooks, custom sources and EDP (prompt 024) ----

    /** 409: a source id is already registered (custom sources, EDP registration). */
    public static final String SOURCE_EXISTS = "SOURCE_EXISTS";

    /** 409: a custom-source update removes a kind that still has current heads. */
    public static final String KIND_IN_USE = "KIND_IN_USE";

    /** 409: {@code observedAtMode} cannot change while the source has heads. */
    public static final String OBSERVED_AT_MODE_LOCKED = "OBSERVED_AT_MODE_LOCKED";

    /** 429: the source's records-per-minute cap is spent; honour {@code Retry-After}. */
    public static final String INGEST_RECORD_RATE = "INGEST_RECORD_RATE";

    /** 404: no ingest format of that name. */
    public static final String FORMAT_UNKNOWN = "FORMAT_UNKNOWN";

    /** 401: a configured webhook signature verifier did not verify. */
    public static final String SIGNATURE_INVALID = "SIGNATURE_INVALID";

    /** 409: the push source is disabled. */
    public static final String SOURCE_DISABLED = "SOURCE_DISABLED";

    /** 503: the ingest queue stayed full for 30 s; honour {@code Retry-After}. */
    public static final String INGEST_BUSY = "INGEST_BUSY";

    /** 400: an EDP manifest failed validation; {@code errors} lists each problem. */
    public static final String MANIFEST_INVALID = "MANIFEST_INVALID";

    /** 400: an EDP protocol minor version above what this instance supports. */
    public static final String PROTOCOL_UNSUPPORTED = "PROTOCOL_UNSUPPORTED";

    /** 400: an EDP batch names a different source than the one it was sent to. */
    public static final String SOURCE_MISMATCH = "SOURCE_MISMATCH";

    /** 409: the external source awaits the operator's approval. */
    public static final String SOURCE_PENDING_APPROVAL = "SOURCE_PENDING_APPROVAL";

    /** 409: the external source is suspended. */
    public static final String SOURCE_SUSPENDED = "SOURCE_SUSPENDED";

    /** 401: the EDP registration token is unknown, expired or used up. */
    public static final String REGISTRATION_TOKEN_INVALID = "REGISTRATION_TOKEN_INVALID";

    /** 409: a self-registering provider's id is outside its registration token's prefix. */
    public static final String SOURCE_ID_NOT_ALLOWED = "SOURCE_ID_NOT_ALLOWED";

    /** 404: no external (EDP) source of that id. */
    public static final String UNKNOWN_EXTERNAL_SOURCE = "UNKNOWN_EXTERNAL_SOURCE";

    /** 429: an LLM call would exceed the provider's per-minute or daily token budget (prompt 026). */
    public static final String LLM_BUDGET_EXCEEDED = "LLM_BUDGET_EXCEEDED";

    /** 409: an LLM provider with that id already exists (prompt 026). */
    public static final String LLM_PROVIDER_EXISTS = "LLM_PROVIDER_EXISTS";

    /** 409: the LLM provider is assigned to a role and cannot be deleted (prompt 026). */
    public static final String LLM_PROVIDER_IN_USE = "LLM_PROVIDER_IN_USE";

    /** 502: the LLM provider failed or could not be reached (prompt 026). */
    public static final String LLM_PROVIDER_ERROR = "LLM_PROVIDER_ERROR";

    /** 429: the LLM provider itself rate-limited the call; {@code Retry-After} when it said (prompt 026). */
    public static final String LLM_PROVIDER_RATE_LIMITED = "LLM_PROVIDER_RATE_LIMITED";

    /** 409: an embedding reindex is already running; cancel it first (prompt 027). */
    public static final String REINDEX_RUNNING = "REINDEX_RUNNING";

    /** 409: semantic search is unavailable (no pgvector, no embedding provider, or no index yet) (prompt 027). */
    public static final String SEMANTIC_UNAVAILABLE = "SEMANTIC_UNAVAILABLE";

    /** 409: Ask is disabled or no chat provider is assigned (prompts 026, 028). */
    public static final String LLM_NOT_CONFIGURED = "LLM_NOT_CONFIGURED";

    /** 409: the proposed Ask action was already executed, rejected or failed (prompt 028). */
    public static final String ACTION_NOT_PENDING = "ACTION_NOT_PENDING";

    /** 409: a run of this briefing is already in progress (prompt 029). */
    public static final String BRIEFING_RUNNING = "BRIEFING_RUNNING";

    /** 409: too many push subscriptions (5 pending per token, 200 in total; 031) or devices (20 per token; 032). */
    public static final String PUSH_LIMIT_REACHED = "PUSH_LIMIT_REACHED";

    /** 400: the echoed push verification code does not match (prompt 031). */
    public static final String VERIFY_FAILED = "VERIFY_FAILED";

    /**
     * 400: an outbound URL is not allowed by {@code OutboundUrlPolicy}: wrong scheme, user info, or
     * an address the field's profile refuses (push endpoints 031; every operator-supplied URL 035).
     */
    public static final String URL_NOT_ALLOWED = "URL_NOT_ALLOWED";

    /**
     * 409: no enabled native push provider serves the app (or, for APNs, the device's environment)
     * (prompt 032).
     */
    public static final String PUSH_PROVIDER_NOT_CONFIGURED = "PUSH_PROVIDER_NOT_CONFIGURED";

    /** 410: a notification action capability is unknown, expired or already used (prompt 033). */
    public static final String ACTION_EXPIRED = "ACTION_EXPIRED";

    /**
     * 400: an inbox operation needs a device: a token with device metadata, or a
     * {@code Foresignal-Device-Id} header (prompt 033).
     */
    public static final String DEVICE_ID_REQUIRED = "DEVICE_ID_REQUIRED";

    /** 409: an escalation policy is still referenced by trigger rules (prompt 033). */
    public static final String POLICY_IN_USE = "POLICY_IN_USE";

    /**
     * 403: a device-scoped ingest key ({@code eid_}, prompt 039) was used with a payload naming another
     * entity than the one the key is bound to.
     */
    public static final String DEVICE_ENTITY_MISMATCH = "DEVICE_ENTITY_MISMATCH";

    /**
     * 403: the sender address is outside the custom source's {@code ingestAllowCidrs} (or, when unset,
     * not a private address) for a path key, query key or GET push (prompt 039).
     */
    public static final String INGEST_SOURCE_DENIED = "INGEST_SOURCE_DENIED";

    /**
     * 400: a {@code RecordMapping} did not compile; {@code errors[].field} carries paths such as
     * {@code mapping.fields[3].unit.from} (prompt 059). {@code suggestMapping} answers 422 with it when the
     * model's draft still fails after its repair rounds.
     */
    public static final String MAPPING_INVALID = "MAPPING_INVALID";

    /**
     * 409: the request would send data to a remote LLM provider and did not set {@code confirmEgress: true}
     * (prompt 059, {@code suggestMapping}).
     */
    public static final String EGRESS_CONFIRMATION_REQUIRED = "EGRESS_CONFIRMATION_REQUIRED";

    /** 409: a mapping test has no stored mapping and none was proposed (prompt 059). */
    public static final String MAPPING_NOT_SET = "MAPPING_NOT_SET";

    /** 409: a mapping test found no captured samples; turn {@code captureSamples} on first (prompt 059). */
    public static final String NO_SAMPLES = "NO_SAMPLES";

    /** 400: a {@code country}, {@code admin1} or {@code county} code that is not known (prompt 040). */
    public static final String UNKNOWN_REGION = "UNKNOWN_REGION";

    /** 409: a rule's {@code geofenceRef} needs reference data that is not loaded (prompt 040). */
    public static final String REFERENCE_DATA_MISSING = "REFERENCE_DATA_MISSING";

    /** A reference dataset's file does not have its pinned SHA-256; it was not loaded (prompt 040). */
    public static final String REFERENCE_CHECKSUM_MISMATCH = "REFERENCE_CHECKSUM_MISMATCH";

    /** 404: no product has this id (prompt 042). */
    public static final String UNKNOWN_PRODUCT = "UNKNOWN_PRODUCT";

    /**
     * 404: the frame does not exist, or its {@code sha8} is stale (a reprocessed frame; a {@code Link}
     * header points to the current key), or no frame exists yet (prompt 042).
     */
    public static final String UNKNOWN_FRAME = "UNKNOWN_FRAME";

    /** 410: retention removed the frame (prompt 042). */
    public static final String FRAME_EXPIRED = "FRAME_EXPIRED";

    /** 400: a concrete forecast time was asked for without {@code run} (prompt 042). */
    public static final String RUN_REQUIRED = "RUN_REQUIRED";

    /** 503: the product is disabled, for example by the decoder self-test (prompt 042; 046 sets it). */
    public static final String PRODUCT_UNAVAILABLE = "PRODUCT_UNAVAILABLE";

    /** 404: no media has this id, or the caller may not read its source (prompt 043). */
    public static final String UNKNOWN_MEDIA = "UNKNOWN_MEDIA";

    /** 410: the media expired or retention deleted it (prompt 043). */
    public static final String MEDIA_EXPIRED = "MEDIA_EXPIRED";

    /** 413: an image exceeds {@code foresignal.media.max-file-bytes}, default 25 MB (prompt 043; 054 uploads). */
    public static final String MEDIA_TOO_LARGE = "MEDIA_TOO_LARGE";

    /** 400: a tile above the product's native max zoom or below its min zoom; clients overzoom (prompt 045). */
    public static final String TILE_OUT_OF_RANGE = "TILE_OUT_OF_RANGE";

    /** 400: a style name or version that is not registered (prompt 045). */
    public static final String UNKNOWN_STYLE = "UNKNOWN_STYLE";

    /** 503: the render queue could not start the render within 1.5 s; {@code Retry-After: 1} (prompt 045). */
    public static final String RENDER_BUSY = "RENDER_BUSY";

    private static final String TYPE_PREFIX = "https://foresignal.ai/problems/";

    private ErrorCodes() {}

    /**
     * The {@code type} URI for a code: {@code BUDGET_EXCEEDED} becomes
     * {@code https://foresignal.ai/problems/budget-exceeded}.
     */
    public static URI typeUri(String code) {
        return URI.create(TYPE_PREFIX + toKebabCase(code));
    }

    static String toKebabCase(String code) {
        return code == null ? "" : code.toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
