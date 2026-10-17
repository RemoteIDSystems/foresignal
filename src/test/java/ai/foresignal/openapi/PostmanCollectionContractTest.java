package ai.foresignal.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.dto.CredentialsUpdate;
import ai.foresignal.api.dto.SourceSettingsUpdate;
import ai.foresignal.notify.ChannelInputParser;
import ai.foresignal.notify.ChannelType;
import ai.foresignal.notify.ChannelTypeRegistry;
import ai.foresignal.notify.NotificationSettingsService;
import ai.foresignal.notify.types.AppriseChannel;
import ai.foresignal.notify.types.DiscordChannel;
import ai.foresignal.notify.types.EmailChannel;
import ai.foresignal.notify.types.GoogleChatChannel;
import ai.foresignal.notify.types.GotifyChannel;
import ai.foresignal.notify.types.HomeAssistantChannel;
import ai.foresignal.notify.types.MatrixChannel;
import ai.foresignal.notify.types.MattermostChannel;
import ai.foresignal.notify.types.NtfyChannel;
import ai.foresignal.notify.push.device.PushDeviceService;
import ai.foresignal.notify.types.PushoverChannel;
import ai.foresignal.notify.types.SlackChannel;
import ai.foresignal.notify.types.TeamsChannel;
import ai.foresignal.notify.types.TelegramChannel;
import ai.foresignal.notify.types.WebhookChannel;
import ai.foresignal.notify.types.ZulipChannel;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenHasher;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.pairing.PairingCodes;
import ai.foresignal.security.ticket.TicketStore;
import ai.foresignal.settings.CorsSettings;
import ai.foresignal.settings.SecuritySettingsService;
import ai.foresignal.settings.ServerSettingsService;
import ai.foresignal.settings.DurationShorthandModule;
import ai.foresignal.settings.WatchdogSettingsService;
import ai.foresignal.testsupport.openapi.OpenApiSpec;
import ai.foresignal.testsupport.openapi.OpenApiSpec.SpecOperation;
import ai.foresignal.testsupport.inbound.InboundBodies;
import ai.foresignal.testsupport.openapi.SpecSchemas;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.Snoozes;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The generated Postman collection ({@code postman/Foresignal.postman_collection.json}) against the
 * authored spec (prompt 020, Roadmap §6). Unit test, no Spring.
 *
 * <ul>
 *   <li>Every route operation appears in some request description as {@code operationId: `<id>`}.
 *       Webhooks are requests Foresignal sends, not ones it serves, so they are not in the
 *       collection.
 *   <li>Each request's auth follows the operation's {@code x-foresignal-scope}; always-authenticated
 *       {@code read} operations use {@code {{readToken}}} (prompt 023).
 *   <li>Every example body of an implemented operation conforms to its spec request schema and
 *       passes the implemented validation: Jackson plus Bean Validation for typed DTOs, or the
 *       operation's own body parser. Operations whose validation needs the database (trigger rules,
 *       retention, settings import, event-stream filters) are checked against the schema only; their
 *       controller ITs exercise the rest. Pending operations are skipped until their DTO exists.
 * </ul>
 */
class PostmanCollectionContractTest {

    private static final Pattern OPERATION_ID = Pattern.compile("operationId: `([^`]+)`");

    /** The moment the snooze examples are judged at; relative examples ({@code for}) never expire. */
    private static final Instant NOW = Instant.parse("2026-10-11T00:00:00Z");

    /** The production mapper's relevant configuration, strict about unknown members. */
    private static final JsonMapper DTO_JSON = JsonMapper.builder()
            .addModule(new DurationShorthandModule())
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private static final List<ChannelType> CHANNEL_TYPES = List.of(new AppriseChannel(), new DiscordChannel(),
            new EmailChannel(), new GoogleChatChannel(), new GotifyChannel(), new HomeAssistantChannel(),
            new MatrixChannel(), new MattermostChannel(), new NtfyChannel(), new PushoverChannel(), new SlackChannel(),
            new TeamsChannel(), new TelegramChannel(), new WebhookChannel(), new ZulipChannel(),
            // 060 and 083/084 added these types to the registry; the collection has examples for them.
            new ai.foresignal.notify.types.MqttChannel(),
            new ai.foresignal.notify.types.MeshtasticChannel(ai.foresignal.testsupport.net.TestOutbound.offlinePolicy()),
            new ai.foresignal.notify.types.MeshCoreChannel(
                    ai.foresignal.testsupport.net.TestOutbound.offlinePolicy(), null));

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    private final JsonNode collection = OpenApiSpec.readJson(OpenApiSpec.POSTMAN);
    private final List<Request> requests = requests(collection);

    /**
     * Implemented validation per operation, beyond the spec schema. Each entry throws (or fails an
     * assertion) when the body would be rejected.
     */
    private final Map<String, Consumer<JsonNode>> validators = Map.ofEntries(
            Map.entry("updateSource", body -> bean(body, SourceSettingsUpdate.class)),
            Map.entry("setSourceCredentials", body -> bean(body, CredentialsUpdate.class)),
            Map.entry("updateCors", body -> bean(body, CorsSettings.class)),
            Map.entry("updateNotificationSettings", NotificationSettingsService::parse),
            Map.entry("updateWatchdogSettings", WatchdogSettingsService::parse),
            Map.entry("acknowledgeAlert", body -> AlertService.transition(body, false)),
            Map.entry("resolveAlert", body -> AlertService.transition(body, false)),
            Map.entry("setAlertFeedback", body -> AlertService.transition(body, true)),
            Map.entry("snoozeAlert", body -> Snoozes.expiry(body, NOW, null)),
            Map.entry("snoozeTrigger", body -> Snoozes.expiry(body, NOW, Snoozes.MAX_RULE_SNOOZE)),
            Map.entry("createChannel", body -> channelParser().parse(body, null)),
            Map.entry("updateChannel", body -> channelParser().parse(body, Map.of())),
            Map.entry("updateSecuritySettings", SecuritySettingsService::parse),
            Map.entry("updateServerSettings", ServerSettingsService::parse),
            // 035: the outbound URL policy overrides.
            Map.entry("updateOutboundSettings", ai.foresignal.settings.OutboundSettingsService::parse),
            // 035: the per-class rate limits.
            Map.entry("updateRateLimits", ai.foresignal.security.ratelimit.RateLimitSettingsService::parse),
            // 036: watchlists and their items (validated per identity: icao24 here).
            Map.entry("createWatchlist", body -> ai.foresignal.watch.WatchlistService.parse(body, null)),
            Map.entry("updateWatchlist", body -> ai.foresignal.watch.WatchlistService.parse(body, null)),
            Map.entry("addWatchlistItems", body -> assertThat(ai.foresignal.watch.WatchlistService
                    .parseJsonItems(icao24List(), body.get("items")).invalid()).as("invalid items").isEmpty()),
            Map.entry("updateCurrentTokenDevice", body -> device(body, "")),
            Map.entry("claimPairingCode", body -> {
                device(body.get("device"), "device.");
                assertThat(PairingCodes.normalize(body.get("code").asString())).as("pairing code").isNotNull();
            }),
            // Prompt 024: the production parsers of custom sources, formats, compat and EDP bodies.
            Map.entry("createCustomSource", InboundBodies::customSource),
            Map.entry("updateCustomSource", InboundBodies::customSource),
            Map.entry("ingestRecords", InboundBodies::nativeBody),
            Map.entry("ingestRecordsFormat", InboundBodies::formatBody),
            Map.entry("ntfyPublishJson", InboundBodies::ntfyJson),
            Map.entry("gotifyPublish", InboundBodies::gotify),
            Map.entry("registerExternalSource", InboundBodies::registration),
            Map.entry("selfRegisterExternalSource", InboundBodies::manifest),
            Map.entry("updateExternalSourceManifest", InboundBodies::manifest),
            Map.entry("pushExternalItems", InboundBodies::batch),
            Map.entry("validateEdp", InboundBodies::validation),
            // Prompt 032: the production parsers of device registration and update.
            Map.entry("registerPushDevice", PushDeviceService::parse),
            Map.entry("updatePushDevice", PushDeviceService::parseUpdate),
            // 033: the inbox, presence, escalation policies and rule mutes.
            Map.entry("markNotificationsRead", ai.foresignal.notify.inbox.InboxService::parseRead),
            Map.entry("updatePresence", ai.foresignal.notify.inbox.PresenceService::parse),
            Map.entry("createEscalationPolicy", ai.foresignal.notify.escalation.EscalationPolicyInput::parse),
            Map.entry("updateEscalationPolicy", ai.foresignal.notify.escalation.EscalationPolicyInput::parse),
            Map.entry("muteTrigger", body -> ai.foresignal.trigger.Mutes.expiry(body, NOW)),
            // 038: metric projections (the production parser over a registry holding the USGS source) and the
            // rollup rebuild range.
            Map.entry("updateMetricSettings", body -> new ai.foresignal.stats.metrics.MetricSettingsService(null,
                    new ai.foresignal.source.SourceRegistry(
                            List.of(new ai.foresignal.source.adapters.usgs.UsgsEarthquakesAdapter("")), List.of(),
                            event -> { })).parse(body)),
            // 039: the device input and the optional purge options (production parsers).
            Map.entry("createIngestDevice", body -> ai.foresignal.inbound.device.IngestDeviceService.readInput(body)),
            Map.entry("purgeDevice", body -> ai.foresignal.inbound.device.IngestDevicesController.purgeAlerts(body)),
            // 056: the weather settings (production parser).
            Map.entry("updateWeatherSettings", ai.foresignal.weather.WeatherSettingsService::parse),
            Map.entry("purgeRecord", body -> ai.foresignal.inbound.device.IngestDevicesController.purgeAlerts(body)),
            // 040: the production body reader, and every named dataset is in the bundled manifest.
            Map.entry("loadReference", body -> assertThat(ai.foresignal.api.ReferenceController.datasets(body))
                    .allSatisfy(name -> assertThat(new ai.foresignal.reference.ReferenceManifest().find(name))
                            .as("dataset %s", name).isPresent())),
            Map.entry("rebuildStatsRollup", body -> assertThat(Instant.parse(body.get("from").asString()))
                    .isBefore(Instant.parse(body.get("to").asString()))),
            // 026: the production LLM provider parser (the path's {{providerId}} is the collection's ollama
            // provider), with the offline outbound policy.
            Map.entry("createLlmProvider", body -> new ai.foresignal.llm.LlmProviderValidator(
                    ai.foresignal.testsupport.net.TestOutbound.offlinePolicy()).parse(body, null)),
            Map.entry("updateLlmProvider", body -> new ai.foresignal.llm.LlmProviderValidator(
                    ai.foresignal.testsupport.net.TestOutbound.offlinePolicy()).parse(body, "ollama")),
            // 028: the production Ask request parser over a registry holding the USGS source.
            Map.entry("ask", body -> ai.foresignal.ask.AskRequestParser.parse(body, usgsRegistry(), Set.of())),
            Map.entry("askStream", body -> ai.foresignal.ask.AskRequestParser.parse(body, usgsRegistry(), Set.of())),
            // 029: the production briefing validator; no trigger rule or channel exists in this unit test.
            Map.entry("createBriefing", PostmanCollectionContractTest::briefing),
            Map.entry("updateBriefing", PostmanCollectionContractTest::briefing),
            Map.entry("previewBriefing", PostmanCollectionContractTest::briefing),
            // 059: the production mapping compiler validates a previewed mapping (no source context).
            Map.entry("previewMapping", body -> new ai.foresignal.mapping.MappingCompiler()
                    .compile(body.get("mapping"), ai.foresignal.mapping.MappingContext.none())),
            Map.entry("suggestMapping", body -> ai.foresignal.mapping.MappingInputs.fromSample(
                    body.get("sample"), OpenApiSpec.JSON)),
            // 114: the production parsers of pattern-of-life subjects and labels, trips, trip checks and CPA batches
            // (the database checks of references, places, channels and anchors are covered by PolServiceIT and
            // TripCorridorIT).
            Map.entry("createPolSubject", body -> ai.foresignal.pol.PolService.parse(body, OpenApiSpec.JSON)),
            Map.entry("updatePolPlace", ai.foresignal.pol.PolService::parseLabel),
            Map.entry("createTrip", ai.foresignal.trip.TripService::validateInput),
            Map.entry("updateTrip", ai.foresignal.trip.TripService::validateInput),
            Map.entry("checkTrip", ai.foresignal.api.TripsController::parseCheck),
            Map.entry("cpaBatch", ai.foresignal.api.KinematicsController::parseBatch),
            Map.entry("createStreamTicket", body -> new TicketStore(new TokenHasher(), null, Clock.fixed(NOW, ZoneOffset.UTC))
                    .mint(new AuthenticatedToken(UUID.randomUUID(), "t", Scope.READ, TokenRestrictions.NONE, null,
                            "01JA8Q3V7S2K9W4T6Y1B5N0M3C", false), body)));

    @BeforeAll
    static void startValidation() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void stopValidation() {
        validatorFactory.close();
    }

    /**
     * One request of the collection.
     *
     * @param name the item name
     * @param operationId the operation it calls
     * @param request the Postman request object
     */
    record Request(String name, String operationId, JsonNode request) {

        /** The auth type: {@code bearer} or {@code noauth}. */
        String authType() {
            JsonNode auth = request.get("auth");
            return auth == null ? null : auth.get("type").asString();
        }

        /** The bearer token value, or null. */
        String bearerToken() {
            JsonNode auth = request.get("auth");
            if (auth == null || auth.get("bearer") == null) {
                return null;
            }
            for (JsonNode entry : auth.get("bearer")) {
                if ("token".equals(entry.get("key").asString())) {
                    return entry.get("value").asString();
                }
            }
            return null;
        }

        /** The value of header {@code name}, or null. */
        String header(String name) {
            for (JsonNode header : request.path("header")) {
                // A disabled header is documentation of an optional header, not one that is sent (041).
                if (header.path("disabled").asBoolean(false)) {
                    continue;
                }
                if (name.equalsIgnoreCase(header.get("key").asString())) {
                    return header.get("value").asString();
                }
            }
            return null;
        }

        /**
         * The raw JSON body, or null (also for a text, XML or NDJSON body, prompt 024, and for a
         * multipart {@code formdata} body, prompt 054).
         */
        JsonNode body() {
            JsonNode body = request.get("body");
            if (body == null || !"raw".equals(body.path("mode").asString("raw"))) {
                return null;
            }
            String language = body.path("options").path("raw").path("language").asString("json");
            return "json".equals(language) ? OpenApiSpec.JSON.readTree(body.get("raw").asString()) : null;
        }
    }

    @Test
    @DisplayName("every route operation of the spec appears in the collection as operationId: `<id>`")
    void everyOperationIsInTheCollection() {
        Set<String> inCollection = new LinkedHashSet<>();
        requests.forEach(r -> inCollection.add(r.operationId()));
        List<String> missing = OpenApiSpec.routes().stream()
                .map(SpecOperation::operationId)
                .filter(id -> !inCollection.contains(id))
                .toList();

        assertThat(missing).as("operations missing from the Postman collection (run ./gradlew generatePostman)").isEmpty();
    }

    @Test
    @DisplayName("every request names an operation that exists in the spec")
    void noRequestForAnUnknownOperation() {
        Set<String> ids = new LinkedHashSet<>();
        OpenApiSpec.routes().forEach(op -> ids.add(op.operationId()));
        List<String> unknown = requests.stream().map(Request::operationId).filter(id -> !ids.contains(id)).toList();

        assertThat(unknown).isEmpty();
    }

    @Test
    @DisplayName("auth follows x-foresignal-scope: admin and agent bearer tokens, the ingest key header, no auth otherwise")
    void authFollowsScope() {
        Map<String, SpecOperation> byId = operationsById();
        List<String> problems = new ArrayList<>();
        for (Request request : requests) {
            SpecOperation op = byId.get(request.operationId());
            if (op == null) {
                continue;
            }
            boolean always = op.node().path("x-foresignal-always-authenticated").asBoolean(false);
            boolean federationFeed = op.path().startsWith("/v1/federation/") && "read".equals(op.scope())
                    && op.node().path("security").size() == 1 && op.node().path("security").get(0).has("bearerToken");
            String expected = switch (op.scope()) {
                case "admin" -> "bearer {{adminToken}}";
                case "agent" -> "bearer {{agentToken}}";
                // 113: the federation feed needs a token with restrictions.federation=true.
                case "read" -> federationFeed ? "bearer {{federationToken}}" : always ? "bearer {{readToken}}" : "noauth";
                // 113: instance-to-instance pushes use the peer ingest key (fsp_) as a bearer token.
                case "peer" -> "bearer {{peerIngestKey}}";
                default -> "noauth";
            };
            if (acceptsIngestKey(op)) {
                // Ingest-key operations (024): the source's key in a header, no bearer.
                expected = "noauth";
                String key = request.header("X-Foresignal-Ingest-Key") != null
                        ? request.header("X-Foresignal-Ingest-Key")
                        : request.header("X-Gotify-Key");
                if (!"{{ingestKey}}".equals(key) && !"{{edpIngestKey}}".equals(key)) {
                    problems.add(request.name() + ": " + op.operationId()
                            + " needs the ingest key header ({{ingestKey}} or {{edpIngestKey}})");
                }
            }
            String actual = "bearer".equals(request.authType())
                    ? "bearer " + request.bearerToken()
                    : String.valueOf(request.authType());
            if (!expected.equals(actual)) {
                problems.add(request.name() + " (" + op.operationId() + ", scope " + op.scope() + "): expected "
                        + expected + ", got " + actual);
            }
        }
        assertThat(problems).isEmpty();
    }

    @Test
    @DisplayName("every example body of an implemented operation conforms to its request schema and passes validation")
    void bodiesAreValid() {
        Map<String, SpecOperation> byId = operationsById();
        Set<String> pending = OpenApiSpec.pending().keySet();
        SpecSchemas schemas = new SpecSchemas();
        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (Request request : requests) {
            SpecOperation op = byId.get(request.operationId());
            JsonNode body = request.body();
            if (op == null || body == null || pending.contains(op.operationId()) || !op.implemented()) {
                continue;
            }
            // The schema of the request's own content type when the spec has one (CloudEvents batch, 024).
            JsonNode content = op.node().path("requestBody").path("content");
            String contentType = request.header("Content-Type");
            JsonNode schema = contentType != null && content.has(contentType)
                    ? content.get(contentType).get("schema")
                    : content.path("application/json").get("schema");
            if (schema == null) {
                continue;
            }
            checked++;
            for (String error : schemas.validateInline(schema, body)) {
                problems.add(request.name() + " (" + op.operationId() + ") schema: " + error);
            }
            Consumer<JsonNode> check = validators.get(op.operationId());
            if (check != null) {
                try {
                    check.accept(body);
                } catch (RuntimeException | AssertionError e) {
                    problems.add(request.name() + " (" + op.operationId() + ") validation: " + e);
                }
            }
        }
        assertThat(checked).as("implemented bodies checked").isPositive();
        assertThat(problems).isEmpty();
    }

    @Test
    @DisplayName("every implemented operation with a typed or parsed body has an implemented-validation entry or is schema-only by design")
    void validatorTableCoversTheParsedBodies() {
        // createToken and createPairingCode validate restrictions against the registered sources and
        // the spec operations, which need the application context; TokensControllerIT and PairingIT
        // exercise them.
        // 024: ntfyPublish and ntfyPublishPut carry a text body (the message); createRegistrationToken's
        // parser lives in a service that needs the database (EdpIngestIT covers it).
        // 032: putPushProvider's example carries a placeholder key, which the key check rightly refuses
        // (no real key is ever committed); PushProvidersIT covers the parser with keys generated at test
        // time. testPushProvider's body is an optional device id.
        // 033: executeAction's body is empty (a form post or {}); nothing in it is read.
        // 035: createFeedKey validates its restriction and prefixes against the registered sources and
        // the spec operations, which need the application context; PathKeyFilterIT covers it.
        // 036: places derive their area with PostGIS and views check sources and places, so both need the
        // database (PlacesIT, SavedViewsIT); importWatchlist's example is a CSV text body (WatchlistsIT).
        // 041: updateLlmSettings names stored providers for its roles (LlmControllersIT); createPushSubscription's
        // parser is an instance method of PushSubscriptionService that checks the endpoint against the outbound
        // policy (PushSubscriptionsIT); verifyPushSubscription's body is the code, compared with the stored row;
        // ingestRecordsPathKey's body is read by the format named in the path (PersonalLocationIngestIT).
        // 050: createTileKey validates its restriction against the registered sources, which needs the
        // application context; TileKeyServiceIT covers it.
        // 057: postSample and postProductStats parse inline in SampleController and resolve products and places
        // against the registry and the database (SampleEndpointIT, StatsIT); ingestSourceMedia and
        // ingestSourceRasters take multipart bodies with an ingest key (RemoteIngestIT).
        Set<String> schemaOnly = Set.of("createTrigger", "updateTrigger", "updateRetention", "importSettings",
                "updateEventStreamFilter", "createToken", "createPairingCode", "ntfyPublish", "ntfyPublishPut",
                "createRegistrationToken", "putPushProvider", "testPushProvider", "executeAction", "createFeedKey",
                "createPlace", "updatePlace", "createView", "updateView", "importWatchlist",
                "updateLlmSettings", "createPushSubscription", "verifyPushSubscription", "ingestRecordsPathKey",
                "createTileKey", "postSample", "postProductStats", "ingestSourceMedia", "ingestSourceRasters",
                // 059: testCustomSourceMapping tests the proposed (or stored) mapping over a custom source's
                // captured samples, which needs the database (MappingPresetIT covers it).
                "testCustomSourceMapping",
                // 073: a WebSub notification body is the raw feed (Atom, RSS, XML or JSON), parsed by the
                // subscription's source, not a typed DTO (WebSubControllerIT covers it).
                "receiveWebSubNotification",
                // 113: peers check the base URL against the outbound policy and the id against stored peers and
                // sources (PeerSourceIT, FederationIngestIT); an edge batch must carry a valid signature over its
                // exact body by a pinned key (FederationIngestIT); a bundle is CBOR (CompactMessageCodecTest).
                "createPeer", "updatePeer", "federationIngest", "receiveFederationBundle");
        List<String> uncovered = OpenApiSpec.routes().stream()
                .filter(SpecOperation::implemented)
                .filter(op -> op.node().has("requestBody"))
                .map(SpecOperation::operationId)
                .filter(id -> !validators.containsKey(id) && !schemaOnly.contains(id))
                .toList();

        assertThat(uncovered).as("add a validator entry or justify schema-only").isEmpty();
    }

    @Test
    @DisplayName("a body the implementation rejects is reported, so a pass means something")
    void rejectedBodyIsDetected() {
        assertThatThrownBy(() -> validators.get("setAlertFeedback").accept(OpenApiSpec.JSON.readTree("{}")))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> bean(OpenApiSpec.JSON.readTree("{\"budget\":{\"perMinute\":-1,\"perDay\":5}}"),
                SourceSettingsUpdate.class)).isInstanceOf(AssertionError.class);
    }

    // ------------------------------------------------------------------------- helpers

    /** True when the operation's security alternatives include an ingest key scheme. */
    private static boolean acceptsIngestKey(SpecOperation op) {
        for (JsonNode alternative : op.node().path("security")) {
            for (String scheme : alternative.propertyNames()) {
                // ingestKeyPath (039) carries the device key in the URL path ({{deviceKey}}), not a header.
                if (scheme.startsWith("ingestKey") && !scheme.equals("ingestKeyPath")) {
                    return true;
                }
            }
        }
        return false;
    }

    /** A registry holding the USGS source, as the metric-settings entry builds it. */
    private static ai.foresignal.source.SourceRegistry usgsRegistry() {
        return new ai.foresignal.source.SourceRegistry(
                List.of(new ai.foresignal.source.adapters.usgs.UsgsEarthquakesAdapter("")), List.of(), event -> { });
    }

    /** The production briefing validator (029) over the USGS registry, with no rules or channels stored. */
    private static void briefing(JsonNode body) {
        // The examples name the collection variable {{channelId}}; Postman substitutes the created channel's id.
        body = OpenApiSpec.JSON.readTree(body.toString().replace("{{channelId}}", "6f1c2a7e-9b1d-4c3e-8a2f-0d4b5e6f7a81"));
        // 056's scope.places and scope.weatherProducts are checked against the database and the product registry,
        // which this hand-built validator does not have (BriefingWeatherSectionIT covers unknown ids), so they
        // are left out here; the rest of the body still meets the production parser.
        if (body.get("scope") instanceof tools.jackson.databind.node.ObjectNode scope) {
            scope.remove("places");
            scope.remove("weatherProducts");
        }
        try {
            validateBriefing(body);
        } catch (ai.foresignal.api.error.ValidationException e) {
            throw new AssertionError(e.getMessage() + " " + e.getErrors(), e);
        }
    }

    private static void validateBriefing(JsonNode body) {
        new ai.foresignal.briefing.BriefingValidator(usgsRegistry(),
                org.mockito.Mockito.mock(ai.foresignal.store.TriggerRuleRepository.class),
                org.mockito.Mockito.mock(ai.foresignal.store.NotificationChannelRepository.class))
                .validate(body, Set.of());
    }

    private static Map<String, SpecOperation> operationsById() {
        Map<String, SpecOperation> out = new HashMap<>();
        OpenApiSpec.routes().forEach(op -> out.put(op.operationId(), op));
        return out;
    }

    private static ChannelInputParser channelParser() {
        return new ChannelInputParser(new ChannelTypeRegistry(CHANNEL_TYPES), DTO_JSON,
                ai.foresignal.testsupport.net.TestOutbound.offlinePolicy());
    }

    /** An icao24 watchlist with the default normalization, for item validation (036). */
    private static ai.foresignal.watch.WatchListEntity icao24List() {
        ai.foresignal.watch.WatchListEntity list = new ai.foresignal.watch.WatchListEntity(UUID.randomUUID());
        list.setIdentity("icao24");
        list.setNormalize("upper");
        return list;
    }

    /** Parses a {@code DeviceInfo} with the production parser and fails on any error. */
    private static void device(JsonNode node, String prefix) {
        List<FieldError> errors = new ArrayList<>();
        TokenService.parseDevice(node, prefix, errors);
        assertThat(errors).as("DeviceInfo").isEmpty();
    }

    /** Deserializes {@code body} into {@code type} and runs Bean Validation on it. */
    private static <T> void bean(JsonNode body, Class<T> type) {
        T value = DTO_JSON.treeToValue(body, type);
        Set<ConstraintViolation<T>> violations = validator.validate(value);
        assertThat(violations).as(type.getSimpleName()).isEmpty();
    }

    /** Every request item, recursively through folders. */
    private static List<Request> requests(JsonNode collection) {
        List<Request> out = new ArrayList<>();
        collect(collection.get("item"), out);
        return out;
    }

    private static void collect(JsonNode items, List<Request> out) {
        for (JsonNode item : items) {
            if (item.has("item")) {
                collect(item.get("item"), out);
                continue;
            }
            JsonNode request = item.get("request");
            Matcher m = OPERATION_ID.matcher(request.path("description").asString());
            assertThat(m.find()).as("request " + item.get("name") + " names its operationId").isTrue();
            out.add(new Request(item.get("name").asString(), m.group(1), request));
        }
    }
}
