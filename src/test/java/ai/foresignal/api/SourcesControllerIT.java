package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.IngestCounts;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.adapters.usgs.UsgsEarthquakesAdapter;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.collect.ControllablePollingAdapter;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Every {@code Sources} operation through the real filter chain, controller, services and database,
 * with fake adapters ({@link CollectorTestSources}, {@link TestIngestSources}).
 *
 * <p>Response shapes are checked against the field names of {@code docs/OpenAPI.json}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class SourcesControllerIT {

    private static final String POLL = CollectorTestSources.POLL;
    private static final String KEYED = CollectorTestSources.KEYED;
    private static final String TRACK = TestIngestSources.TRACK;
    private static final String LATEST = TestIngestSources.LATEST;
    private static final String USGS = UsgsEarthquakesAdapter.ID;
    private static final Instant T0 = Instant.parse("2026-04-01T12:00:00Z");
    private static final String SECRET = "s3cr3t-VALUE-never-returned-7f3a";

    @Autowired private MockMvc mvc;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private AppSettingService settings;
    @Autowired private ControllablePollingAdapter collectPollAdapter;
    @Autowired private ControllablePollingAdapter collectKeyedAdapter;
    @Autowired private BackoffRegistry backoff;
    @Autowired private UsageMeter usage;
    @Autowired private MutableClock clock;

    private String token;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        token = adminTokens.rotate();
        clock.setInstant(T0);
        collectPollAdapter.reset();
        collectKeyedAdapter.reset();
        backoff.onSuccess(POLL);
    }

    private MockHttpServletRequestBuilder admin(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private ResultActions putJson(String path, String json) throws Exception {
        return mvc.perform(admin(put(path)).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    // ---------- listSources ----------

    @Nested
    @DisplayName("listSources")
    class ListSources {

        @Test
        @DisplayName("lists every source sorted by id with the summary fields")
        void lists() throws Exception {
            String body =
                    mvc.perform(get("/v1/sources"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$[?(@.id == '" + POLL + "')].name").value(hasItem("Fake " + POLL)))
                            .andExpect(jsonPath("$[?(@.id == '" + POLL + "')].mode").value(hasItem("polling")))
                            .andExpect(jsonPath("$[?(@.id == '" + POLL + "')].enabled").value(hasItem(false)))
                            .andExpect(jsonPath("$[?(@.id == '" + POLL + "')].status").value(hasItem("disabled")))
                            .andExpect(jsonPath("$[?(@.id == '" + POLL + "')].credentialsRequired").value(hasItem(false)))
                            .andExpect(jsonPath("$[?(@.id == '" + POLL + "')].credentialsSet").value(hasItem(false)))
                            .andExpect(jsonPath("$[?(@.id == '" + POLL + "')].historyTier").value(hasItem("STANDARD")))
                            .andExpect(jsonPath("$[?(@.id == '" + POLL + "')].licence").value(hasItem(CollectorTestSources.LICENCE)))
                            .andExpect(jsonPath("$[?(@.id == '" + POLL + "')].interval").value(hasItem("1s")))
                            .andExpect(jsonPath("$[?(@.id == '" + POLL + "')].budget.perMinute").value(hasItem(600)))
                            .andExpect(jsonPath("$[?(@.id == '" + POLL + "')].kinds[0]").value(hasItem(CollectorTestSources.KIND)))
                            .andExpect(jsonPath("$[?(@.id == '" + KEYED + "')].credentialsRequired").value(hasItem(true)))
                            .andExpect(jsonPath("$[?(@.id == '" + TRACK + "')].historyTier").value(hasItem("TRACK")))
                            .andExpect(jsonPath("$[?(@.id == '" + USGS + "')].licence").value(hasItem("US-PD")))
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            assertThat(body).contains("\"lastDataAt\"", "\"termsUrl\"", "\"budgetUsedToday\"", "\"lastSuccessAt\"");
            List<String> ids = com.jayway.jsonpath.JsonPath.read(body, "$[*].id");
            List<String> sorted = new ArrayList<>(ids);
            sorted.sort(String::compareTo);
            assertThat(ids).isEqualTo(sorted).contains(POLL, KEYED, TRACK, USGS);
        }
    }

    // ---------- getSource ----------

    @Nested
    @DisplayName("getSource")
    class GetSource {

        @Test
        @DisplayName("returns the v1.2 descriptor, settings and health fields")
        void detail() throws Exception {
            mvc.perform(get("/v1/sources/" + TRACK))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.descriptor.id").value(TRACK))
                    .andExpect(jsonPath("$.descriptor.mode").value("push"))
                    .andExpect(jsonPath("$.descriptor.historyTier").value("TRACK"))
                    .andExpect(jsonPath("$.descriptor.observedAtMode").value("UPSTREAM"))
                    .andExpect(jsonPath("$.descriptor.ttlAnchor").value("OBSERVED"))
                    .andExpect(jsonPath("$.descriptor.recordTtl").value("5m"))
                    .andExpect(jsonPath("$.descriptor.defaultIngestPolicy.mode").value("SAMPLED"))
                    .andExpect(jsonPath("$.descriptor.defaultIngestPolicy.minInterval").value("1m"))
                    .andExpect(jsonPath("$.descriptor.defaultIngestPolicy.keyframe").value("4m"))
                    .andExpect(jsonPath("$.descriptor.defaultIngestPolicy.edgeMinInterval").value("30s"))
                    .andExpect(jsonPath("$.descriptor.defaultIngestPolicy.minDeviationMeters").value(1000.0))
                    .andExpect(jsonPath("$.descriptor.defaultIngestPolicy.thresholds").value(true))
                    .andExpect(jsonPath("$.descriptor.significantFields[0]").value("callsign"))
                    .andExpect(jsonPath("$.descriptor.motion.speedField").value("gs"))
                    .andExpect(jsonPath("$.descriptor.motion.speedUnit").value("knots"))
                    .andExpect(jsonPath("$.descriptor.motion.courseField").value("track"))
                    .andExpect(jsonPath("$.descriptor.thresholds[0].field").value("alt"))
                    .andExpect(jsonPath("$.descriptor.thresholds[1].angular").value(true))
                    .andExpect(jsonPath("$.descriptor.skewTolerance").value("2m"))
                    .andExpect(jsonPath("$.descriptor.allowNullIsland").value(false))
                    .andExpect(jsonPath("$.descriptor.private").value(false))
                    .andExpect(jsonPath("$.descriptor.products").isArray())
                    .andExpect(jsonPath("$.descriptor.credentialFields").isArray())
                    .andExpect(jsonPath("$.descriptor.params").isArray())
                    .andExpect(jsonPath("$.descriptor.defaultBudget.perDay").value(10000))
                    .andExpect(jsonPath("$.settings.enabled").value(false))
                    .andExpect(jsonPath("$.settings.ingestPolicy").value(nullValue()))
                    .andExpect(jsonPath("$.settings.effectiveIngestPolicy.mode").value("SAMPLED"))
                    .andExpect(jsonPath("$.settings.retention").value(nullValue()))
                    .andExpect(jsonPath("$.settings.effectiveRetention").value("3d"))
                    .andExpect(jsonPath("$.settings.credentialsSet").value(false))
                    .andExpect(jsonPath("$.health.sourceId").value(TRACK))
                    .andExpect(jsonPath("$.health.status").value("disabled"))
                    .andExpect(jsonPath("$.health.consecutiveFailures").value(0))
                    .andExpect(jsonPath("$.health.storage.persistRatio").exists())
                    .andExpect(jsonPath("$.health.storage.projectedBytes").exists())
                    .andExpect(jsonPath("$.health", hasKey("coverage")))
                    .andExpect(jsonPath("$.health", hasKey("lastDataAt")))
                    .andExpect(jsonPath("$.health", hasKey("budgetHoursRemaining")))
                    .andExpect(jsonPath("$.health", hasKey("newestFrameAt")))
                    .andExpect(jsonPath("$.health", hasKey("frameAgeSeconds")));
        }

        @Test
        @DisplayName("maps the 008 extras: fieldSpecs, titleField, licence and termsUrl")
        void extras() throws Exception {
            mvc.perform(get("/v1/sources/" + USGS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.descriptor.fieldSpecs.mag.type").value("NUMBER"))
                    .andExpect(jsonPath("$.descriptor.licence").value("US-PD"))
                    .andExpect(jsonPath("$.descriptor", hasKey("termsUrl")))
                    .andExpect(jsonPath("$.descriptor", hasKey("titleField")))
                    .andExpect(jsonPath("$.descriptor", hasKey("entityKeys")))
                    .andExpect(jsonPath("$.descriptor", hasKey("kindDisplay")))
                    .andExpect(jsonPath("$.descriptor", hasKey("aggBelowZoom")))
                    .andExpect(jsonPath("$.descriptor.defaultRetention").value("1825d"))
                    .andExpect(jsonPath("$.settings.effectiveRetention").value("1825d"))
                    .andExpect(jsonPath("$.descriptor.params[?(@.name == 'feed')].default").value(hasItem("all_day")));
        }

        @Test
        @DisplayName("effectiveIngestPolicy falls back to the descriptor default, keyframe none without a TTL")
        void effectiveFallsBack() throws Exception {
            mvc.perform(get("/v1/sources/" + LATEST))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.settings.ingestPolicy").value(nullValue()))
                    .andExpect(jsonPath("$.settings.effectiveIngestPolicy.mode").value("LATEST"))
                    .andExpect(jsonPath("$.settings.effectiveIngestPolicy.keyframe").value("none"))
                    .andExpect(jsonPath("$.descriptor.defaultIngestPolicy.keyframe").value("none"));
        }

        @Test
        @DisplayName("an unknown source is 404 NOT_FOUND")
        void unknown() throws Exception {
            for (String path :
                    List.of(
                            "/v1/sources/nope",
                            "/v1/sources/nope/health",
                            "/v1/sources/nope/usage")) {
                mvc.perform(get(path))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("NOT_FOUND"));
            }
            mvc.perform(admin(get("/v1/sources/nope/fetches")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("NOT_FOUND"));
            putJson("/v1/sources/nope", "{}")
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("NOT_FOUND"));
            mvc.perform(admin(post("/v1/sources/nope/fetch")))
                    .andExpect(status().isNotFound());
            mvc.perform(admin(post("/v1/sources/nope/probe")))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- health ----------

    @Nested
    @DisplayName("health")
    class Health {

        @Test
        @DisplayName("/v1/sources/health lists health and is not captured by /{sourceId}")
        void notShadowed() throws Exception {
            mvc.perform(get("/v1/sources/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[?(@.sourceId == '" + POLL + "')].status").value(hasItem("disabled")))
                    .andExpect(jsonPath("$[0].storage").exists());
        }

        @Test
        @DisplayName("the status filter keeps only matching sources and rejects unknown values")
        void filter() throws Exception {
            mvc.perform(get("/v1/sources/health").param("status", "disabled"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].status").value(everyItem(is("disabled"))));
            mvc.perform(get("/v1/sources/health").param("status", "healthy,failing"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.sourceId == '" + POLL + "')]").isEmpty());
            mvc.perform(get("/v1/sources/health").param("status", "bogus"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.errors[0].field").value("status"));
        }

        @Test
        @DisplayName("getSourceHealth returns one source's health")
        void one() throws Exception {
            mvc.perform(get("/v1/sources/" + POLL + "/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sourceId").value(POLL))
                    .andExpect(jsonPath("$.budget.perMinute").value(600))
                    .andExpect(jsonPath("$.storage.bytesPerDay").isNumber())
                    .andExpect(jsonPath("$.storage.triggerEdgeShare").isNumber())
                    .andExpect(jsonPath("$", hasKey("budgetHoursRemaining")))
                    .andExpect(jsonPath("$.budgetRemainingToday").isNumber());
        }
    }

    // ---------- updateSource ----------

    @Nested
    @DisplayName("updateSource")
    class UpdateSource {

        @Test
        @DisplayName("applies a partial change and returns the detail")
        void partial() throws Exception {
            putJson("/v1/sources/" + POLL, "{\"interval\":\"30s\",\"budget\":{\"perMinute\":5,\"perDay\":500}}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.descriptor.id").value(POLL))
                    .andExpect(jsonPath("$.settings.interval").value("30s"))
                    .andExpect(jsonPath("$.settings.budget.perMinute").value(5))
                    .andExpect(jsonPath("$.settings.enabled").value(false))
                    .andExpect(jsonPath("$.settings.updatedAt").exists());
            putJson("/v1/sources/" + POLL, "{\"retention\":\"90d\"}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.settings.interval").value("30s"))
                    .andExpect(jsonPath("$.settings.retention").value("90d"))
                    .andExpect(jsonPath("$.settings.effectiveRetention").value("90d"));
            putJson("/v1/sources/" + POLL, "{\"retention\":null}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.settings.retention").value(nullValue()))
                    .andExpect(jsonPath("$.settings.effectiveRetention").value("7d"));
        }

        @Test
        @DisplayName("an interval below the minimum is 400 with errors[]")
        void intervalBelowMinimum() throws Exception {
            putJson("/v1/sources/" + USGS, "{\"interval\":\"10s\"}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.errors[0].field").value("interval"))
                    .andExpect(jsonPath("$.errors[0].message").value(containsString("at least")));
        }

        @Test
        @DisplayName("a malformed duration is 400 with errors[] naming the field")
        void badDuration() throws Exception {
            putJson("/v1/sources/" + POLL, "{\"interval\":\"soon\",\"retention\":\"forever\"}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.errors[*].field").value(hasItem("interval")))
                    .andExpect(jsonPath("$.errors[*].field").value(hasItem("retention")));
        }

        @Test
        @DisplayName("an unknown parameter is 400 with errors[] naming params.<key>")
        void unknownParam() throws Exception {
            putJson("/v1/sources/" + USGS, "{\"params\":{\"nope\":1}}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.errors[0].field").value("params.nope"));
        }

        @Test
        @DisplayName("a negative budget fails Bean Validation with errors[]")
        void negativeBudget() throws Exception {
            putJson("/v1/sources/" + POLL, "{\"budget\":{\"perMinute\":-1,\"perDay\":10}}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.errors[0].field").value("budget.perMinute"));
        }

        @Test
        @DisplayName("enabling without required credentials is 409 CREDENTIALS_REQUIRED")
        void credentialsRequired() throws Exception {
            putJson("/v1/sources/" + KEYED, "{\"enabled\":true}")
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("CREDENTIALS_REQUIRED"));
        }

        @Test
        @DisplayName("ingestPolicy round trip: set SAMPLED, read back, clear with explicit null")
        void policyRoundTrip() throws Exception {
            putJson(
                            "/v1/sources/" + TRACK,
                            "{\"ingestPolicy\":{\"mode\":\"SAMPLED\",\"minInterval\":\"30s\",\"keyframe\":\"4m\","
                                    + "\"minDeviationMeters\":500,\"thresholds\":false}}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.settings.ingestPolicy.mode").value("SAMPLED"))
                    .andExpect(jsonPath("$.settings.ingestPolicy.minInterval").value("30s"))
                    .andExpect(jsonPath("$.settings.ingestPolicy.keyframe").value("4m"))
                    .andExpect(jsonPath("$.settings.ingestPolicy.edgeMinInterval").value("15s"))
                    .andExpect(jsonPath("$.settings.ingestPolicy.minDeviationMeters").value(500.0))
                    .andExpect(jsonPath("$.settings.ingestPolicy.thresholds").value(false))
                    .andExpect(jsonPath("$.settings.effectiveIngestPolicy.minInterval").value("30s"));

            mvc.perform(get("/v1/sources/" + TRACK))
                    .andExpect(jsonPath("$.settings.ingestPolicy.minInterval").value("30s"));

            // Omitting the key leaves the override alone.
            putJson("/v1/sources/" + TRACK, "{\"interval\":\"10s\"}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.settings.ingestPolicy.minInterval").value("30s"));

            putJson("/v1/sources/" + TRACK, "{\"ingestPolicy\":null}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.settings.ingestPolicy").value(nullValue()))
                    .andExpect(jsonPath("$.settings.effectiveIngestPolicy.minInterval").value("1m"));
        }

        @Test
        @DisplayName("an invalid policy is 400 INGEST_POLICY_INVALID with every violation's field path")
        void invalidPolicy() throws Exception {
            putJson(
                            "/v1/sources/" + TRACK,
                            "{\"ingestPolicy\":{\"mode\":\"SAMPLED\",\"minInterval\":\"1s\",\"keyframe\":\"1h\"}}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INGEST_POLICY_INVALID"))
                    .andExpect(jsonPath("$.type").value("https://foresignal.ai/problems/ingest-policy-invalid"))
                    .andExpect(jsonPath("$.errors[*].field").value(hasItem("ingestPolicy.minInterval")))
                    .andExpect(jsonPath("$.errors[*].field").value(hasItem("ingestPolicy.keyframe")));

            putJson("/v1/sources/" + POLL, "{\"ingestPolicy\":{\"mode\":\"SAMPLED\",\"minInterval\":\"10s\"}}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INGEST_POLICY_INVALID"))
                    .andExpect(jsonPath("$.errors[*].field").value(hasItem("ingestPolicy.mode")));

            putJson("/v1/sources/" + POLL, "{\"ingestPolicy\":{\"mode\":\"LATEST\",\"keyframe\":\"soon\"}}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INGEST_POLICY_INVALID"))
                    .andExpect(jsonPath("$.errors[0].field").value("ingestPolicy.keyframe"));

            putJson("/v1/sources/" + POLL, "{\"ingestPolicy\":{\"mode\":\"EVERYTHING\"}}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INGEST_POLICY_INVALID"))
                    .andExpect(jsonPath("$.errors[0].field").value("ingestPolicy.mode"));

            mvc.perform(get("/v1/sources/" + TRACK)).andExpect(jsonPath("$.settings.ingestPolicy").value(nullValue()));
        }

        @Test
        @DisplayName("retention on a TRACK source is 400 RETENTION_TIER_FIXED naming retention.trackWindow")
        void trackRetention() throws Exception {
            putJson("/v1/sources/" + TRACK, "{\"retention\":\"7d\"}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("RETENTION_TIER_FIXED"))
                    .andExpect(jsonPath("$.detail").value(containsString("retention.trackWindow")));
        }

        @Test
        @DisplayName("a body that is not JSON is 400 MALFORMED_REQUEST")
        void malformed() throws Exception {
            putJson("/v1/sources/" + POLL, "{not json")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
        }
    }

    // ---------- credentials ----------

    @Nested
    @DisplayName("credentials")
    class Credentials {

        @Test
        @DisplayName("set, enable, read and delete; the secret never appears in a response")
        void lifecycle() throws Exception {
            List<String> bodies = new ArrayList<>();

            mvc.perform(admin(put("/v1/sources/" + KEYED + "/credentials"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"values\":{\"apiKey\":\"" + SECRET + "\"}}"))
                    .andExpect(status().isNoContent())
                    .andDo(r -> bodies.add(r.getResponse().getContentAsString()));

            putJson("/v1/sources/" + KEYED, "{\"enabled\":true}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.settings.enabled").value(true))
                    .andExpect(jsonPath("$.settings.credentialsSet").value(true))
                    .andDo(r -> bodies.add(r.getResponse().getContentAsString()));

            for (String path :
                    List.of(
                            "/v1/sources",
                            "/v1/sources/" + KEYED,
                            "/v1/sources/" + KEYED + "/health",
                            "/v1/sources/health",
                            "/v1/sources/" + KEYED + "/usage",
                            "/v1/sources/" + KEYED + "/fetches")) {
                mvc.perform(admin(get(path)))
                        .andExpect(status().isOk())
                        .andDo(r -> bodies.add(r.getResponse().getContentAsString()));
            }

            mvc.perform(admin(delete("/v1/sources/" + KEYED + "/credentials")))
                    .andExpect(status().isNoContent());
            mvc.perform(get("/v1/sources/" + KEYED))
                    .andExpect(jsonPath("$.settings.credentialsSet").value(false))
                    .andExpect(jsonPath("$.settings.enabled").value(false))
                    .andDo(r -> bodies.add(r.getResponse().getContentAsString()));

            assertThat(bodies).isNotEmpty().allSatisfy(body -> assertThat(body).doesNotContain(SECRET));
        }

        @Test
        @DisplayName("an undeclared credential key is 400 with errors[]")
        void undeclared() throws Exception {
            mvc.perform(admin(put("/v1/sources/" + KEYED + "/credentials"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"values\":{\"apiKey\":\"x\",\"other\":\"" + SECRET + "\"}}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("values.other"))
                    .andExpect(content().string(not(containsString(SECRET))));
        }

        @Test
        @DisplayName("a body without values fails Bean Validation")
        void missingValues() throws Exception {
            mvc.perform(admin(put("/v1/sources/" + KEYED + "/credentials"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("values"));
        }
    }

    // ---------- fetch and probe ----------

    @Nested
    @DisplayName("fetchSourceNow and probeSource")
    class FetchAndProbe {

        @Test
        @DisplayName("a manual fetch returns the outcome with counts, all nine reasons and the fetch id")
        void fetchNow() throws Exception {
            collectPollAdapter.then(ctx -> FetchResult.complete(List.of(
                    CollectorTestSources.record("a", T0.minusSeconds(30)),
                    CollectorTestSources.record("b", T0.minusSeconds(20)))));

            mvc.perform(admin(post("/v1/sources/" + POLL + "/fetch")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sourceId").value(POLL))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.recordsSeen").value(2))
                    .andExpect(jsonPath("$.persisted").value(2))
                    .andExpect(jsonPath("$.recordsChanged").value(2))
                    .andExpect(jsonPath("$.byReason.FIRST").value(2))
                    .andExpect(jsonPath("$.byReason.TRIGGER_EDGE").value(0))
                    .andExpect(jsonPath("$.byReason", aMapWithSize(9)))
                    .andExpect(jsonPath("$.fetchId").isNumber())
                    .andExpect(jsonPath("$.fromSeq").isNumber())
                    .andExpect(jsonPath("$", not(hasKey("status"))))
                    .andExpect(jsonPath("$", not(hasKey("skipReason"))))
                    .andExpect(jsonPath("$", hasKey("touchesThrottled")))
                    .andExpect(jsonPath("$", hasKey("live")));
        }

        @Test
        @DisplayName("a manual fetch of a push source is 409 NOT_SUPPORTED_FOR_MODE")
        void fetchNotSupported() throws Exception {
            mvc.perform(admin(post("/v1/sources/" + TRACK + "/fetch")))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("NOT_SUPPORTED_FOR_MODE"));
        }

        @Test
        @DisplayName("a probe returns previews and never ingests")
        void probe() throws Exception {
            collectPollAdapter.then(ctx -> FetchResult.complete(List.of(
                    CollectorTestSources.record("p1", T0.minusSeconds(5)))));

            mvc.perform(admin(post("/v1/sources/" + POLL + "/probe")).param("maxBytes", "2048").param("seconds", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sourceId").value(POLL))
                    .andExpect(jsonPath("$.requests").isArray())
                    .andExpect(jsonPath("$.records[0].externalId").value("p1"))
                    .andExpect(jsonPath("$.records[0].kind").value(CollectorTestSources.KIND))
                    .andExpect(jsonPath("$.records[0].geometry.type").value("Point"))
                    .andExpect(jsonPath("$.records[0]", hasKey("eventTime")))
                    .andExpect(jsonPath("$.records[0]", hasKey("quality")))
                    .andExpect(jsonPath("$.records[0].observers").isArray())
                    .andExpect(jsonPath("$.parseError").value(nullValue()));

            mvc.perform(admin(get("/v1/sources/" + POLL + "/fetches")))
                    .andExpect(jsonPath("$.items").isEmpty());
        }

        @Test
        @DisplayName("probe arguments out of range are 400 naming the parameter")
        void probeRange() throws Exception {
            mvc.perform(admin(post("/v1/sources/" + POLL + "/probe")).param("maxBytes", "10"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("maxBytes"));
            mvc.perform(admin(post("/v1/sources/" + POLL + "/probe")).param("seconds", "61"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("seconds"));
        }
    }

    // ---------- usage ----------

    @Nested
    @DisplayName("getSourceUsage")
    class Usage {

        private static final Instant U0 = Instant.parse("2026-03-01T10:15:00Z");

        @Test
        @DisplayName("minute, hour and day series carry the ingest counters and totals")
        void series() throws Exception {
            clock.setInstant(U0);
            usage.record(POLL, 100, true);
            usage.record(POLL, 50, false);
            usage.recordIngest(POLL, new IngestCounts(5, 2, 1, 1, 0, 0, 1, 1234));
            clock.setInstant(U0.plusSeconds(120));
            usage.record(POLL, 10, true);

            mvc.perform(get("/v1/sources/" + POLL + "/usage")
                            .param("from", "2026-03-01T10:00:00Z")
                            .param("to", "2026-03-01T11:00:00Z")
                            .param("resolution", "minute"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sourceId").value(POLL))
                    .andExpect(jsonPath("$.resolution").value("minute"))
                    .andExpect(jsonPath("$.points.length()").value(2))
                    .andExpect(jsonPath("$.points[0].t").value("2026-03-01T10:15:00Z"))
                    .andExpect(jsonPath("$.points[0].requests").value(2))
                    .andExpect(jsonPath("$.points[0].errors").value(1))
                    .andExpect(jsonPath("$.points[0].bytes").value(150))
                    .andExpect(jsonPath("$.points[0].recordsSeen").value(5))
                    .andExpect(jsonPath("$.points[0].versions").value(2))
                    .andExpect(jsonPath("$.points[0].liveUpdates").value(1))
                    .andExpect(jsonPath("$.points[0].touches").value(1))
                    .andExpect(jsonPath("$.points[0].rejected").value(1))
                    .andExpect(jsonPath("$.points[0].versionBytes").value(1234))
                    .andExpect(jsonPath("$.totals.requests").value(3))
                    .andExpect(jsonPath("$.totals.bytes").value(160))
                    .andExpect(jsonPath("$.totals.versions").value(2))
                    .andExpect(jsonPath("$.totals.t").value("2026-03-01T10:00:00Z"));

            mvc.perform(get("/v1/sources/" + POLL + "/usage")
                            .param("from", "2026-03-01T00:00:00Z")
                            .param("to", "2026-03-02T00:00:00Z")
                            .param("resolution", "day"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.points.length()").value(1))
                    .andExpect(jsonPath("$.points[0].t").value("2026-03-01T00:00:00Z"))
                    .andExpect(jsonPath("$.points[0].requests").value(3));
        }

        @Test
        @DisplayName("defaults to the last 24 hours at hour resolution")
        void defaults() throws Exception {
            // A minute of its own: the meter's unflushed counters outlive a test's table cleanup.
            clock.setInstant(Instant.parse("2026-03-05T08:15:30Z"));
            usage.record(POLL, 1, true);

            mvc.perform(get("/v1/sources/" + POLL + "/usage"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resolution").value("hour"))
                    .andExpect(jsonPath("$.points.length()").value(1))
                    .andExpect(jsonPath("$.points[0].t").value("2026-03-05T08:00:00Z"))
                    .andExpect(jsonPath("$.totals.requests").value(1))
                    .andExpect(jsonPath("$.totals.t").value("2026-03-04T08:15:30Z"));
        }

        @Test
        @DisplayName("over 31 days at minute resolution, a reversed range or a bad resolution is 400")
        void rejected() throws Exception {
            mvc.perform(get("/v1/sources/" + POLL + "/usage")
                            .param("from", "2026-01-01T00:00:00Z")
                            .param("to", "2026-02-05T00:00:00Z")
                            .param("resolution", "minute"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.errors[0].field").value("resolution"));
            mvc.perform(get("/v1/sources/" + POLL + "/usage")
                            .param("from", "2026-01-01T00:00:00Z")
                            .param("to", "2026-02-05T00:00:00Z")
                            .param("resolution", "hour"))
                    .andExpect(status().isOk());
            mvc.perform(get("/v1/sources/" + POLL + "/usage")
                            .param("from", "2026-02-01T00:00:00Z")
                            .param("to", "2026-01-01T00:00:00Z"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("from"));
            mvc.perform(get("/v1/sources/" + POLL + "/usage").param("resolution", "week"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("resolution"));
            mvc.perform(get("/v1/sources/" + POLL + "/usage").param("from", "yesterday"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("from"));
        }
    }

    // ---------- authorization ----------

    @Nested
    @DisplayName("authorization")
    class Authorization {

        @Test
        @DisplayName("every write and listSourceFetches are 401 without the token")
        void writesNeedTheToken() throws Exception {
            mvc.perform(put("/v1/sources/" + POLL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
            mvc.perform(put("/v1/sources/" + KEYED + "/credentials")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"values\":{\"apiKey\":\"x\"}}"))
                    .andExpect(status().isUnauthorized());
            mvc.perform(delete("/v1/sources/" + KEYED + "/credentials")).andExpect(status().isUnauthorized());
            mvc.perform(post("/v1/sources/" + POLL + "/fetch")).andExpect(status().isUnauthorized());
            mvc.perform(post("/v1/sources/" + POLL + "/probe")).andExpect(status().isUnauthorized());
            mvc.perform(get("/v1/sources/" + POLL + "/fetches")).andExpect(status().isUnauthorized());

            assertThat(collectPollAdapter.fetchCount()).isZero();
        }

        @Test
        @DisplayName("the other GETs stay open while openReads is on")
        void readsAreOpen() throws Exception {
            mvc.perform(get("/v1/sources")).andExpect(status().isOk());
            mvc.perform(get("/v1/sources/health")).andExpect(status().isOk());
            mvc.perform(get("/v1/sources/" + POLL)).andExpect(status().isOk());
            mvc.perform(get("/v1/sources/" + POLL + "/health")).andExpect(status().isOk());
            mvc.perform(get("/v1/sources/" + POLL + "/usage")).andExpect(status().isOk());
        }
    }
}
