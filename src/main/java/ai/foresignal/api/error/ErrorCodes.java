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

    /**
     * 400: an {@code egress} override on a source whose descriptor is not {@code egressOverridable}
     * (prompt 069).
     */
    public static final String EGRESS_FIXED = "EGRESS_FIXED";

    /**
     * A parameter value that is allowed only on a private source was set on a source that is not effectively private
     * (prompt 086: {@code local-remoteid}'s {@code keepOperatorPosition}), or such a source was being made public.
     */
    public static final String PRIVATE_REQUIRED = "PRIVATE_REQUIRED";

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
     * 422: a timeline whose raw read would scan more record versions than one request can count within its statement
     * timeout (estimated up front from the hourly activity rollup).
     */
    public static final String TIMELINE_RANGE_UNSUPPORTED = "TIMELINE_RANGE_UNSUPPORTED";

    /**
     * 400: a stats request would return more than 2,000 buckets or 10,000 cells, or a raw grid would
     * generate too many hexagons (prompt 038).
     */
    public static final String STATS_TOO_LARGE = "STATS_TOO_LARGE";

    /**
     * 422: a stats time series of {@code measure=entities} with {@code bucket=1d} over a day the daily rollup does
     * not hold, whose distinct entities would be counted from raw history.
     */
    public static final String STATS_MEASURE_UNSUPPORTED = "STATS_MEASURE_UNSUPPORTED";

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

    /** 400: a windowed rule's window is missing or invalid, or it sets a single-version option (prompt 100). */
    public static final String WINDOW_RULE_INVALID = "WINDOW_RULE_INVALID";

    /** 409: an anomaly baseline is requested before any coverage-ok sample exists (prompt 100). */
    public static final String BASELINE_NOT_READY = "BASELINE_NOT_READY";

    /** 404: no intelligence event has that id (prompt 101). */
    public static final String UNKNOWN_EVENT = "UNKNOWN_EVENT";

    /** 404: no investigation has that id (prompt 101). */
    public static final String UNKNOWN_INVESTIGATION = "UNKNOWN_INVESTIGATION";

    /** 400: an intelligence event member does not exist at that seq (prompt 101). */
    public static final String EVENT_MEMBER_NOT_FOUND = "EVENT_MEMBER_NOT_FOUND";

    /** 400: a correlation recipe definition is invalid (prompt 101). */
    public static final String RECIPE_INVALID = "RECIPE_INVALID";

    /** 409: an optimistic-lock conflict on an intelligence event (prompt 101). */
    public static final String EVENT_VERSION_CONFLICT = "EVENT_VERSION_CONFLICT";

    /** 409: the population pack has not been imported (prompt 101). */
    public static final String PACK_NOT_INSTALLED = "PACK_NOT_INSTALLED";

    /** 404: no analyst profile has that id (prompt 102). */
    public static final String UNKNOWN_ANALYST_PROFILE = "UNKNOWN_ANALYST_PROFILE";

    /** 404: no analyst proposal has that id (prompt 102). */
    public static final String UNKNOWN_PROPOSAL = "UNKNOWN_PROPOSAL";

    /** 409: an analyst proposal is not pending (prompt 102). */
    public static final String PROPOSAL_NOT_PENDING = "PROPOSAL_NOT_PENDING";

    /** 409: an analyst run is already in progress (prompt 102). */
    public static final String ANALYST_RUN_IN_PROGRESS = "ANALYST_RUN_IN_PROGRESS";

    /** 400: an analyst profile is not valid (prompt 102). */
    public static final String ANALYST_PROFILE_INVALID = "ANALYST_PROFILE_INVALID";

    /** 404: no backtest job has that id (prompt 103). */
    public static final String UNKNOWN_BACKTEST = "UNKNOWN_BACKTEST";

    /** 404: no precursor test has that id (prompt 103). */
    public static final String UNKNOWN_PRECURSOR_TEST = "UNKNOWN_PRECURSOR_TEST";

    /** 409: a backtest job is already queued or running for this token (prompt 103). */
    public static final String BACKTEST_IN_PROGRESS = "BACKTEST_IN_PROGRESS";

    /** 400: a backtest range is empty, inverted or wholly below retention (prompt 103). */
    public static final String BACKTEST_RANGE_INVALID = "BACKTEST_RANGE_INVALID";

    /** 400: a precursor test definition is not valid (prompt 103). */
    public static final String PRECURSOR_TEST_INVALID = "PRECURSOR_TEST_INVALID";

    /** 404: no such infrastructure node (prompt 104). */
    public static final String UNKNOWN_INFRA_NODE = "UNKNOWN_INFRA_NODE";

    /** 409: the infrastructure graph has not been built yet (prompt 104). */
    public static final String INFRA_GRAPH_NOT_BUILT = "INFRA_GRAPH_NOT_BUILT";

    /** 400: an infrastructure query area is too large without a types filter (prompt 104). */
    public static final String INFRA_QUERY_TOO_LARGE = "INFRA_QUERY_TOO_LARGE";

    /** 403: the token's scope is below the operation's; carries {@code requiredScope} (prompt 023). */
    public static final String INSUFFICIENT_SCOPE = "INSUFFICIENT_SCOPE";

    /**
     * 404: the RFC 9728 protected resource metadata is asked for while {@code mcp.auth.mode} is {@code token}, so
     * {@code /mcp} accepts no identity-provider tokens (prompt 141).
     */
    public static final String MCP_OAUTH_DISABLED = "MCP_OAUTH_DISABLED";

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
     * 429: too many attempts; honor {@code Retry-After} (pairing claims, prompt 023; per-client rate
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

    /** 429: the source's records-per-minute cap is spent; honor {@code Retry-After}. */
    public static final String INGEST_RECORD_RATE = "INGEST_RECORD_RATE";

    /** 404: no ingest format of that name. */
    public static final String FORMAT_UNKNOWN = "FORMAT_UNKNOWN";

    /** 401: a configured webhook signature verifier did not verify. */
    public static final String SIGNATURE_INVALID = "SIGNATURE_INVALID";

    /** 409: the channel is disabled (prompt 060: {@code republishMqttChannel}). */
    public static final String CHANNEL_DISABLED = "CHANNEL_DISABLED";

    /** 409: the push source is disabled. */
    public static final String SOURCE_DISABLED = "SOURCE_DISABLED";

    /** 503: the ingest queue stayed full for 30 s; honor {@code Retry-After}. */
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
     * 400: a {@code meshtastic} or {@code meshcore} channel would transmit on a public or default mesh channel
     * (prompt 084): slot 0, a modem-preset or {@code Public} name, a {@code #hashtag} channel, an all-zero secret,
     * licensed (ham) mode, a broker that is not on the LAN, or no {@code privateChannelConfirmed}.
     */
    public static final String MESH_PUBLIC_CHANNEL = "MESH_PUBLIC_CHANNEL";

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

    /** 404: no entity holds the requested id or key (prompt 098). */
    public static final String ENTITY_NOT_FOUND = "ENTITY_NOT_FOUND";

    /** 400: an entity key is malformed or not a known key type (prompt 098). */
    public static final String ENTITY_KEY_INVALID = "ENTITY_KEY_INVALID";

    /** 409: an entity key is already open on another entity (prompt 098). */
    public static final String ENTITY_KEY_CONFLICT = "ENTITY_KEY_CONFLICT";

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

    /** 400: a field encoding request covers more points than the encoding allows (prompt 106). */
    public static final String FIELD_TOO_LARGE = "FIELD_TOO_LARGE";

    /** 404: the record of a position sample is unknown (prompt 106). */
    public static final String UNKNOWN_RECORD = "UNKNOWN_RECORD";

    /** 406: the requested raster format is not available (WebP unhealthy; prompt 108). */
    public static final String FORMAT_UNAVAILABLE = "FORMAT_UNAVAILABLE";

    /** 413: an EDR area request covers more cells than allowed (prompt 108). */
    public static final String EDR_TOO_LARGE = "EDR_TOO_LARGE";

    /** 413: a place offline pack exceeds its size cap (prompt 108), or a PMTiles pack its tile cap (111). */
    public static final String PACK_TOO_LARGE = "PACK_TOO_LARGE";

    /** 404: the export job is unknown (prompt 108). */
    public static final String UNKNOWN_EXPORT = "UNKNOWN_EXPORT";

    /** 400: notification image links need a public https base URL (prompt 108). */
    public static final String IMAGE_LINKS_REQUIRE_HTTPS = "IMAGE_LINKS_REQUIRE_HTTPS";

    /** 409: a sink with this id exists (prompt 110). */
    public static final String SINK_EXISTS = "SINK_EXISTS";

    /** 400: a bulk export, snapshot or pack spec is not valid (prompt 111). */
    public static final String EXPORT_SPEC_INVALID = "EXPORT_SPEC_INVALID";

    /** 409: the export directory is over {@code exports.diskQuotaGb} (prompt 111). */
    public static final String EXPORT_QUOTA = "EXPORT_QUOTA";

    /** 410: an export or pack expired, failed or was swept (prompt 111). */
    public static final String EXPORT_EXPIRED = "EXPORT_EXPIRED";

    /** 400: a snapshot's {@code atSeq} is below a source's retention floor and no hold covers it (111). */
    public static final String SNAPSHOT_BEYOND_RETENTION = "SNAPSHOT_BEYOND_RETENTION";

    /** 409: the number of holds would exceed {@code holds.max} (prompt 111). */
    public static final String HOLD_LIMIT = "HOLD_LIMIT";

    /** 404: no hold has that id (prompt 111). */
    public static final String UNKNOWN_HOLD = "UNKNOWN_HOLD";

    /** 409: an infrastructure graph build is already running (prompt 104). */
    public static final String INFRA_REBUILD_RUNNING = "INFRA_REBUILD_RUNNING";

    /** 409: an export, snapshot or pack is not finished, so it has nothing to download yet (prompt 111). */
    public static final String EXPORT_NOT_READY = "EXPORT_NOT_READY";

    /** 400: an OGC API Features {@code filter} (CQL2) cannot be parsed or names an unknown property (112). */
    public static final String FILTER_INVALID = "FILTER_INVALID";

    /** 400: a custom calendar's kind declares no {@code startField}/{@code endField} (112). */
    public static final String ICAL_FIELDS_MISSING = "ICAL_FIELDS_MISSING";

    /** 409: a share link's target is a record whose redistribution is not allowed (112). */
    public static final String SHARE_LICENCE_FORBIDDEN = "SHARE_LICENCE_FORBIDDEN";

    /** 401: the {@code fss_} key in a share-link URL is unknown (112). */
    public static final String INVALID_SHARE_KEY = "INVALID_SHARE_KEY";

    /** 410: a share link is revoked, expired or has used up its uses (112). */
    public static final String SHARE_EXPIRED = "SHARE_EXPIRED";

    /**
     * 403: a federation batch's detached JWS does not verify against the peer's pinned key, or a peer's
     * {@code /info} is signed by another key than the pinned one (113).
     */
    public static final String PEER_KEY_MISMATCH = "PEER_KEY_MISMATCH";

    /** 409: a peer with this id exists (113). */
    public static final String PEER_EXISTS = "PEER_EXISTS";

    /** 400: a CPA question about two records more than 500 km apart (114). */
    public static final String CPA_RANGE = "CPA_RANGE";

    /** 400: a trip route that is not a valid LineString of at most 5,000 vertices (114). */
    public static final String TRIP_ROUTE_INVALID = "TRIP_ROUTE_INVALID";

    /** 422: a kinematic state older than 1 h (114). */
    public static final String STATE_TOO_OLD = "STATE_TOO_OLD";

    /**
     * 400: an exercise replay window starts below a source's retained history (115); the detail names the
     * source and its earliest replayable time.
     */
    public static final String REPLAY_BEYOND_RETENTION = "REPLAY_BEYOND_RETENTION";

    /** 409: another exercise replay is running (115); one runs at a time. */
    public static final String EXERCISE_RUNNING = "EXERCISE_RUNNING";

    /** 409: canceling an exercise replay that is no longer planned or running (115). */
    public static final String EXERCISE_NOT_RUNNING = "EXERCISE_NOT_RUNNING";

    /** 409: a tuning suggestion's rule changed since it was generated, or the suggestion is not open (115). */
    public static final String SUGGESTION_STALE = "SUGGESTION_STALE";

    /** 404: no canonical event has that id (116). */
    public static final String UNKNOWN_CANONICAL_EVENT = "UNKNOWN_CANONICAL_EVENT";

    /** 404: no analysis manifest has that id (116). */
    public static final String UNKNOWN_MANIFEST = "UNKNOWN_MANIFEST";

    /** 409: a manifest whose component cannot be replayed, known before a replay job is queued (116). */
    public static final String MANIFEST_NOT_REPLAYABLE = "MANIFEST_NOT_REPLAYABLE";

    /** 404: no attention topic has that id (117). */
    public static final String UNKNOWN_ATTENTION_TOPIC = "UNKNOWN_ATTENTION_TOPIC";

    /** 400: an attention topic failed validation, including its GDELT query test call (117). */
    public static final String TOPIC_INVALID = "TOPIC_INVALID";

    /** 409: an attention topic with that id already exists (117). */
    public static final String ATTENTION_TOPIC_EXISTS = "ATTENTION_TOPIC_EXISTS";

    /** 409: a history backfill of that source is already running (117). */
    public static final String BACKFILL_RUNNING = "BACKFILL_RUNNING";

    /** 404: no situation has that id, or the caller may not see it (118). */
    public static final String UNKNOWN_SITUATION = "UNKNOWN_SITUATION";

    /** 400: a situation member does not exist, is not pinned by a valid seq, or is not in the situation (118). */
    public static final String SITUATION_MEMBER_NOT_FOUND = "SITUATION_MEMBER_NOT_FOUND";

    /** 409: a situation change sent a stale {@code version} (118). */
    public static final String SITUATION_VERSION_CONFLICT = "SITUATION_VERSION_CONFLICT";

    /** 409: a change to a closed situation, which is final (118). */
    public static final String SITUATION_CLOSED = "SITUATION_CLOSED";

    /** 404: no analytic finding has that id, or the caller may not see it (119). */
    public static final String UNKNOWN_FINDING = "UNKNOWN_FINDING";

    /** 400: a series reference does not parse ({@code series:}, {@code rollup:} or {@code rule:}) (119). */
    public static final String SERIES_REF_INVALID = "SERIES_REF_INVALID";

    /** 404: the series a reference names does not exist, the caller may not see it, or it is not registered (093, 119). */
    public static final String SERIES_NOT_FOUND = "SERIES_NOT_FOUND";

    /** 400: an event study's selector, target or range failed validation (119). */
    public static final String EVENT_STUDY_INVALID = "EVENT_STUDY_INVALID";

    /** 404: no precursor has that id (120). */
    public static final String UNKNOWN_PRECURSOR = "UNKNOWN_PRECURSOR";

    /** 409: a precursor mining or re-test run is already queued or running (120). */
    public static final String PRECURSOR_MINING_IN_PROGRESS = "PRECURSOR_MINING_IN_PROGRESS";

    /** 404: no base rate has that id (121). */
    public static final String UNKNOWN_BASE_RATE = "UNKNOWN_BASE_RATE";

    /** 404: no reference import has that id (121). */
    public static final String UNKNOWN_IMPORT = "UNKNOWN_IMPORT";

    /** 409: the reference dataset's file, API or credentials are not available (121). */
    public static final String IMPORT_DATASET_UNAVAILABLE = "IMPORT_DATASET_UNAVAILABLE";

    /** 400: a base-rate definition is not valid (121). */
    public static final String BASE_RATE_INVALID = "BASE_RATE_INVALID";

    /** 409: an automatic base rate cannot be deleted (121). */
    public static final String BASE_RATE_NOT_CUSTOM = "BASE_RATE_NOT_CUSTOM";

    /** 404: no forecast question has that id, or it is private and the caller has no private access (122). */
    public static final String UNKNOWN_QUESTION = "UNKNOWN_QUESTION";

    /** 404: no forecaster has that id, or the caller may not submit as it (122). */
    public static final String UNKNOWN_FORECASTER = "UNKNOWN_FORECASTER";

    /** 409: the question is not open for this change (closed, resolved or annulled) (122). */
    public static final String QUESTION_CLOSED = "QUESTION_CLOSED";

    /** 409: criteria and resolver cannot change once a forecast exists (122). */
    public static final String QUESTION_IMMUTABLE = "QUESTION_IMMUTABLE";

    /** 409: LLM retrospective evaluation needs the provider's knowledgeCutoff (122). */
    public static final String KNOWLEDGE_CUTOFF_REQUIRED = "KNOWLEDGE_CUTOFF_REQUIRED";

    /** 400: a forecast or resolution does not fit its question (122). */
    public static final String FORECAST_INVALID = "FORECAST_INVALID";

    /** 400: a question's resolver is not valid (122). */
    public static final String RESOLVER_INVALID = "RESOLVER_INVALID";

    /** 404: no benchmark run has that id (123). */
    public static final String UNKNOWN_BENCHMARK_RUN = "UNKNOWN_BENCHMARK_RUN";

    /** 404: no benchmark case has that id (123). */
    public static final String UNKNOWN_BENCHMARK_CASE = "UNKNOWN_BENCHMARK_CASE";

    /** 409: a benchmark run is already queued or running (123). */
    public static final String BENCHMARK_IN_PROGRESS = "BENCHMARK_IN_PROGRESS";

    /** 404: no hypothesis set has that id, or it is private and the caller has no private access (125). */
    public static final String UNKNOWN_HYPOTHESIS_SET = "UNKNOWN_HYPOTHESIS_SET";

    /** 404: no hypothesis has that id (125). */
    public static final String UNKNOWN_HYPOTHESIS = "UNKNOWN_HYPOTHESIS";

    /** 404: no indicator has that id (125). */
    public static final String UNKNOWN_INDICATOR = "UNKNOWN_INDICATOR";

    /** 400: a hypothesis set, hypothesis or review is not valid (125). */
    public static final String HYPOTHESIS_SET_INVALID = "HYPOTHESIS_SET_INVALID";

    /** 400: an indicator or its watch rule is not valid (125). */
    public static final String INDICATOR_INVALID = "INDICATOR_INVALID";

    /** 400: text fails the estimative-language lint (125). */
    public static final String LINT_FAILED = "LINT_FAILED";

    /** 409: the hypothesis set changed since the version the caller read (125). */
    public static final String HYPOTHESIS_SET_VERSION_CONFLICT = "HYPOTHESIS_SET_VERSION_CONFLICT";

    /** 409: the hypothesis set is resolved and accepts no more changes (125). */
    public static final String HYPOTHESIS_SET_RESOLVED = "HYPOTHESIS_SET_RESOLVED";

    /** 404: no regional index has that id (126). */
    public static final String UNKNOWN_INDEX = "UNKNOWN_INDEX";

    /** 404: no outlook exists for that region and family (126). */
    public static final String UNKNOWN_OUTLOOK = "UNKNOWN_OUTLOOK";

    /** 404: no projected-impact finding has that id, or the caller may not see it (127). */
    public static final String UNKNOWN_IMPACT = "UNKNOWN_IMPACT";

    /** 400: a regional index definition or weights change is not valid (126). */
    public static final String INDEX_INVALID = "INDEX_INVALID";

    /** 409: no forecast method has been selected for the series yet, or an index's inputs are not ready (126). */
    public static final String FORECAST_MODEL_NOT_READY = "FORECAST_MODEL_NOT_READY";

    /** 404: no analysis cycle has that id (128). */
    public static final String UNKNOWN_CYCLE = "UNKNOWN_CYCLE";

    /** 404: no cycle run has that id (128). */
    public static final String UNKNOWN_CYCLE_RUN = "UNKNOWN_CYCLE_RUN";

    /** 404: no assessment has that id, none exists yet, or the caller may not see it (128). */
    public static final String UNKNOWN_ASSESSMENT = "UNKNOWN_ASSESSMENT";

    /** 409: the analysis cycle (or the situation's on-demand assessment) already has a run in progress (128). */
    public static final String CYCLE_RUN_IN_PROGRESS = "CYCLE_RUN_IN_PROGRESS";

    /** 409: monthly maintenance is already running (128). */
    public static final String MAINTENANCE_IN_PROGRESS = "MAINTENANCE_IN_PROGRESS";

    /** 400: an object reference is malformed or names an unknown type (129, Client-Contract §2). */
    public static final String OBJECT_REF_INVALID = "OBJECT_REF_INVALID";

    /** 404: no object has that reference, or the caller may not see it (129). */
    public static final String UNKNOWN_OBJECT = "UNKNOWN_OBJECT";

    /** 404: no workspace has that id, or the caller is neither its owner nor a member (129). */
    public static final String UNKNOWN_WORKSPACE = "UNKNOWN_WORKSPACE";

    /** 404: no annotation has that id, or the caller may not see it (129). */
    public static final String UNKNOWN_ANNOTATION = "UNKNOWN_ANNOTATION";

    /** 404: no relation has that id, or the caller may not see it (129). */
    public static final String UNKNOWN_RELATION = "UNKNOWN_RELATION";

    /** 409: a projected relation is never edited by hand (129). */
    public static final String RELATION_NOT_EDITABLE = "RELATION_NOT_EDITABLE";

    /** 412: the workspace context changed since the version in {@code If-Match} (129). */
    public static final String WORKSPACE_CONTEXT_STALE = "WORKSPACE_CONTEXT_STALE";

    /** 409: the workspace context has no viewport or time range to summarize (129). */
    public static final String WORKSPACE_CONTEXT_INCOMPLETE = "WORKSPACE_CONTEXT_INCOMPLETE";

    /** 403: the caller's workspace role does not allow the change (129). */
    public static final String WORKSPACE_FORBIDDEN = "WORKSPACE_FORBIDDEN";

    /** 404: no principal has that id or handle (130). */
    public static final String UNKNOWN_PRINCIPAL = "UNKNOWN_PRINCIPAL";

    /** 404: no such group (130). */
    public static final String UNKNOWN_GROUP = "UNKNOWN_GROUP";

    /** 404: no such tag, or a personal tag of someone else (130). */
    public static final String UNKNOWN_TAG = "UNKNOWN_TAG";

    /** 404: no such attachment, or the caller may not see it (130). */
    public static final String UNKNOWN_ATTACHMENT = "UNKNOWN_ATTACHMENT";

    /** 404: no such evidence item, or the caller may not see it (130). */
    public static final String UNKNOWN_EVIDENCE_ITEM = "UNKNOWN_EVIDENCE_ITEM";

    /** 404: no such saved search, or the caller may not see it (130). */
    public static final String UNKNOWN_SAVED_SEARCH = "UNKNOWN_SAVED_SEARCH";

    /** 403: the object's TLP label forbids the share target; the problem names the rule (130). */
    public static final String TLP_VIOLATION = "TLP_VIOLATION";

    /** 413: the upload exceeds {@code foresignal.collab.max-upload-mb} (130). */
    public static final String UPLOAD_TOO_LARGE = "UPLOAD_TOO_LARGE";

    /** 415: the upload's type is not an allowed attachment type (130). */
    public static final String UPLOAD_TYPE_NOT_ALLOWED = "UPLOAD_TYPE_NOT_ALLOWED";

    /** 409: another principal has that handle (130). */
    public static final String PRINCIPAL_HANDLE_TAKEN = "PRINCIPAL_HANDLE_TAKEN";

    /** 409: the owner already has a tag with that name in the namespace (130). */
    public static final String TAG_NAME_TAKEN = "TAG_NAME_TAKEN";

    /** 409: an evidence item still cites the attachment (130). */
    public static final String ATTACHMENT_IN_USE = "ATTACHMENT_IN_USE";

    /** 404: no chronicle has that id or display id, or the caller may not see it (131). */
    public static final String UNKNOWN_CHRONICLE = "UNKNOWN_CHRONICLE";

    /** 404: the chronicle has no layer with that id (131). */
    public static final String UNKNOWN_CHRONICLE_LAYER = "UNKNOWN_CHRONICLE_LAYER";

    /** 409: the size cap is below the bytes the chronicle already holds (131). */
    public static final String CHRONICLE_CAP_EXCEEDED = "CHRONICLE_CAP_EXCEEDED";

    /** 409: the lifecycle transition is not allowed from the chronicle's status (131). */
    public static final String CHRONICLE_STATE_INVALID = "CHRONICLE_STATE_INVALID";

    /** 400: a chronicle layer's selector, area or capture mode is invalid (131). */
    public static final String CHRONICLE_LAYER_INVALID = "CHRONICLE_LAYER_INVALID";

    /** 400: deleting a chronicle needs {@code confirm} equal to its display id (131). */
    public static final String CHRONICLE_CONFIRMATION_REQUIRED = "CHRONICLE_CONFIRMATION_REQUIRED";

    /** 412: the chronicle changed since the version in {@code If-Match} (131). */
    public static final String CHRONICLE_VERSION_STALE = "CHRONICLE_VERSION_STALE";

    /** 404: no report has that id or display id, or the caller may not see it (132). */
    public static final String UNKNOWN_REPORT = "UNKNOWN_REPORT";

    /** 404: no exchange package (sent or received) has that id, or the caller may not see it (132). */
    public static final String UNKNOWN_PACKAGE = "UNKNOWN_PACKAGE";

    /** 400: a package's detached JWS or a content hash does not verify against the sender's key (132). */
    public static final String PACKAGE_SIGNATURE_INVALID = "PACKAGE_SIGNATURE_INVALID";

    /** 422: a package names a recipient principal that does not exist on this instance (132). */
    public static final String PACKAGE_RECIPIENT_UNKNOWN = "PACKAGE_RECIPIENT_UNKNOWN";

    /** 413: a package exceeds {@code exchange.maxPackageMb} (132). */
    public static final String PACKAGE_TOO_LARGE = "PACKAGE_TOO_LARGE";

    /** 409: the report draft changed since the version given, or the report is not a draft (132). */
    public static final String REPORT_VERSION_CONFLICT = "REPORT_VERSION_CONFLICT";

    /** 409: the operation needs a published report revision, and there is none (or it is retracted) (132). */
    public static final String REPORT_NOT_PUBLISHED = "REPORT_NOT_PUBLISHED";

    /** 409: the package is still building or failed, so it cannot be downloaded (132). */
    public static final String PACKAGE_NOT_READY = "PACKAGE_NOT_READY";

    /** 409: this instance already imported the package (132). */
    public static final String PACKAGE_ALREADY_IMPORTED = "PACKAGE_ALREADY_IMPORTED";

    /**
     * 409: the package would loop: it originated on, or is being sent back unchanged to, an instance already in its
     * provenance chain (132, the 113 loop-prevention rule).
     */
    public static final String PACKAGE_LOOP = "PACKAGE_LOOP";

    /** 404: no news story with this id (133). */
    public static final String UNKNOWN_STORY = "UNKNOWN_STORY";

    /** 404: no media outlet with this id (133). */
    public static final String UNKNOWN_OUTLET = "UNKNOWN_OUTLET";

    /**
     * 409: a rating provider cannot be enabled or imported before an admin acknowledged its license (133); AllSides
     * and MBFC are off until the operator records their own rights.
     */
    public static final String PROVIDER_LICENCE_NOT_ACKNOWLEDGED = "PROVIDER_LICENCE_NOT_ACKNOWLEDGED";

    /** 400: a ratings import is malformed (unknown provider, bad CSV or JSON, no usable row) (133). */
    public static final String RATINGS_IMPORT_INVALID = "RATINGS_IMPORT_INVALID";

    /** 404: no chronicle link with this id, or the caller may not learn of it (134). */
    public static final String UNKNOWN_LINK = "UNKNOWN_LINK";

    /** 404: no chronicle cluster with this id holding a chronicle the caller can see (134). */
    public static final String UNKNOWN_CLUSTER = "UNKNOWN_CLUSTER";

    /** 404: no access request with this id addressed to or sent by the caller (134). */
    public static final String UNKNOWN_ACCESS_REQUEST = "UNKNOWN_ACCESS_REQUEST";

    /** 409: the chronicle's TLP label does not allow the requested discoverability; carries the {@code rule} (134). */
    public static final String DISCOVERABILITY_NOT_ALLOWED = "DISCOVERABILITY_NOT_ALLOWED";

    /** 429: the principal sent its 10 access requests of the day (134). */
    public static final String REQUEST_RATE_LIMITED = "REQUEST_RATE_LIMITED";

    /** 409: the access request was already granted or declined (134). */
    public static final String ACCESS_REQUEST_DECIDED = "ACCESS_REQUEST_DECIDED";

    /** 409: federated chronicle discovery is off ({@code network.federated} is false) (134). */
    public static final String FEDERATED_DISCOVERY_DISABLED = "FEDERATED_DISCOVERY_DISABLED";

    /** 409: a sketch batch names a network this instance holds no current salt for (134). */
    public static final String NETWORK_SALT_UNKNOWN = "NETWORK_SALT_UNKNOWN";

    /** 409: an access request on a link whose other chronicle the caller can already see (134). */
    public static final String CHRONICLE_ALREADY_VISIBLE = "CHRONICLE_ALREADY_VISIBLE";

    /** 400: a sketch batch is malformed or its signature does not verify (134). */
    public static final String SKETCH_BATCH_INVALID = "SKETCH_BATCH_INVALID";

    /** 404: no social platform adapter with this id, or no network of this name (135). */
    public static final String UNKNOWN_PLATFORM = "UNKNOWN_PLATFORM";

    /** 409: the social platform adapter is disabled, so the operation cannot use it (135). */
    public static final String PLATFORM_DISABLED = "PLATFORM_DISABLED";

    /** 404: no narrative with this id (136). */
    public static final String UNKNOWN_NARRATIVE = "UNKNOWN_NARRATIVE";

    /** 404: no claim with this id (136). */
    public static final String UNKNOWN_CLAIM = "UNKNOWN_CLAIM";

    /** 404: no media fingerprint with this id (136). */
    public static final String UNKNOWN_FINGERPRINT = "UNKNOWN_FINGERPRINT";

    /** 415 (413 when too large): the image was refused by the 043 ImageGuard (136, media matching). */
    public static final String IMAGE_REJECTED = "IMAGE_REJECTED";

    /** 404: no social–physical comparison with this id (137). */
    public static final String UNKNOWN_COMPARISON = "UNKNOWN_COMPARISON";

    /** 404: no social cue with this id (137). */
    public static final String UNKNOWN_CUE = "UNKNOWN_CUE";

    /** 429: the daily cue limit {@code social.cues.maxPerDay} is reached (137). */
    public static final String CUE_LIMIT_REACHED = "CUE_LIMIT_REACHED";

    /** 404: no external binding with this id (138). */
    public static final String UNKNOWN_BINDING = "UNKNOWN_BINDING";

    /** 404: no external question link with this id (138). */
    public static final String UNKNOWN_QUESTION_LINK = "UNKNOWN_QUESTION_LINK";

    /**
     * 400: a binding or question link does not fit its stream or target: the stream's item type and the target's
     * requirement are named, with each configuration problem (138).
     */
    public static final String BINDING_TARGET_INVALID = "BINDING_TARGET_INVALID";

    /** 409: the provider question is already linked to a ledger question (138). */
    public static final String QUESTION_ALREADY_LINKED = "QUESTION_ALREADY_LINKED";

    /** 409: a doctor run for this source is in progress or ran within the last 10 minutes (140). */
    public static final String DOCTOR_BUSY = "DOCTOR_BUSY";

    /** 404: no adapter-doctor report with this id (140). */
    public static final String UNKNOWN_DOCTOR_REPORT = "UNKNOWN_DOCTOR_REPORT";

    /** 404: no plugin directory with this name under {@code ${FORESIGNAL_DATA_DIR}/plugins/} (141). */
    public static final String UNKNOWN_PLUGIN = "UNKNOWN_PLUGIN";

    /**
     * 409: the plugin cannot be enabled because its file does not hash to the reviewed SHA-256, or to the one its
     * {@code plugin.json} declares (141).
     */
    public static final String PLUGIN_HASH_MISMATCH = "PLUGIN_HASH_MISMATCH";

    /** 409: a plugin cannot be enabled while the {@code plugins} setting is off (141). */
    public static final String PLUGINS_DISABLED = "PLUGINS_DISABLED";

    private static final String TYPE_PREFIX = "https://foresignal.ai/problems/";

    /** 400: a new password does not meet the password policy (R2-002, {@code PasswordPolicy}). */
    public static final String PASSWORD_POLICY = "PASSWORD_POLICY";

    /**
     * 400: an activation or reset token is unknown, of the other kind, expired or already used (R2-002). One code for
     * every case, so the answer does not tell a caller which.
     */
    public static final String USER_TOKEN_INVALID = "USER_TOKEN_INVALID";

    /** 409: the account's status does not allow the requested lifecycle change (R2-002, {@code UserLifecycleService}). */
    public static final String USER_STATUS_CONFLICT = "USER_STATUS_CONFLICT";

    /** 401: the email or password is wrong, or the account may not sign in; one answer for every case (R2-003). */
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";

    /** 409: MFA is already active, so a new enrollment is refused until it is disabled (R2-024, {@code enrollMfa}). */
    public static final String MFA_ALREADY_ENABLED = "MFA_ALREADY_ENABLED";

    /** 409: MFA is neither active nor pending for the caller (R2-024). */
    public static final String MFA_NOT_ENABLED = "MFA_NOT_ENABLED";

    /** 409: a role the user holds requires MFA, so the user may not disable it (R2-024, {@code disableMfa}). */
    public static final String MFA_REQUIRED = "MFA_REQUIRED";

    /**
     * 403: a role the signed-in user holds requires MFA and it is not enrolled; until it is, only reads, the MFA
     * enrollment operations, preferences, signing out and revoking one's own sessions are allowed (R2-024).
     */
    public static final String MFA_ENROLLMENT_REQUIRED = "MFA_ENROLLMENT_REQUIRED";

    /** 400: the TOTP or recovery code is wrong, expired or already used (R2-024). */
    public static final String MFA_CODE_INVALID = "MFA_CODE_INVALID";

    /**
     * 403: a token a user mints for themselves would reach further than the user: a scope above
     * {@code TokenOwnership.maxScope} of their permissions, or the private grant without an admin-derived session
     * (R2-025, {@code createMyToken}).
     */
    public static final String TOKEN_EXCEEDS_USER = "TOKEN_EXCEEDS_USER";

    /**
     * 409: the create would take the user past a quota (R2-026): {@code maxAlerts}, {@code maxChannels},
     * {@code maxSavedViews} or {@code maxStorageBytes}; the problem names it as {@code limit} with the {@code quota} and
     * current {@code usage}.
     */
    public static final String QUOTA_EXCEEDED = "QUOTA_EXCEEDED";

    /**
     * 409: first-run setup is refused because the instance is already set up: a user exists (R2-027,
     * {@code runSetup}).
     */
    public static final String ALREADY_SETUP = "ALREADY_SETUP";

    /** 404: no session with that id belongs to the caller (R2-003, {@code revokeSession}). */
    public static final String UNKNOWN_SESSION = "UNKNOWN_SESSION";

    /** 410: a password reset token is unknown, expired or already used (R2-003, {@code resetPassword}). */
    public static final String RESET_TOKEN_GONE = "RESET_TOKEN_GONE";

    /** 410: an activation token is unknown, expired or already used (R2-003, {@code activateAccount}). */
    public static final String ACTIVATION_TOKEN_GONE = "ACTIVATION_TOKEN_GONE";

    /**
     * 403: a signed-in user's roles do not grant the operation's {@code x-foresignal-permission} (R2-004); the problem
     * carries the key as {@code requiredPermission}. API tokens never get it: they are gated by scope alone.
     */
    public static final String PERMISSION_DENIED = "PERMISSION_DENIED";

    /** 409: another user already has that email (R2-005, {@code createUser}, {@code updateUser}). */
    public static final String EMAIL_TAKEN = "EMAIL_TAKEN";

    /** 404: no user with that id (R2-005). */
    public static final String UNKNOWN_USER = "UNKNOWN_USER";

    /**
     * 409: the change would leave no active user holding {@code users.manage} (R2-005: deleting, suspending or
     * re-roling the last one).
     */
    public static final String LAST_ADMIN = "LAST_ADMIN";

    /** 409: the user is no longer {@code invited}, so there is no invite to resend (R2-005, {@code resendUserInvite}). */
    public static final String ALREADY_ACTIVE = "ALREADY_ACTIVE";

    /** 404: no role with that id (R2-006). */
    public static final String UNKNOWN_ROLE = "UNKNOWN_ROLE";

    /** 409: another role already has that name, ignoring case, built-ins included (R2-006). */
    public static final String ROLE_NAME_TAKEN = "ROLE_NAME_TAKEN";

    /** 409: a built-in role cannot be updated or deleted (R2-006). */
    public static final String BUILTIN_ROLE_PROTECTED = "BUILTIN_ROLE_PROTECTED";

    /** 409: users still hold the role; the problem carries {@code userCount} (R2-006, {@code deleteRole}). */
    public static final String ROLE_IN_USE = "ROLE_IN_USE";

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
