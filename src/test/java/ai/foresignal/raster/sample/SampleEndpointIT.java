package ai.foresignal.raster.sample;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.place.PlaceService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.openapi.SpecSchemas;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/**
 * {@code getSample}, {@code postSample} and {@code getPlaceWeather} over HTTP (prompt 052): values with status,
 * valid time, local time, age and run; the no-coverage text; relative and forecast times; places need a token;
 * 100 points at most; 404 for unknown products and places. Responses are validated against the spec.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class SampleEndpointIT extends RasterApiSupport {

    static final SpecSchemas SCHEMAS = new SpecSchemas();

    @Autowired PlaceService places;
    @Autowired TokenService tokens;
    @Autowired SampleService samples;

    String reader;

    @BeforeEach
    void setUp() {
        reader = TestTokens.bearer(TestTokens.create(tokens, "sample-reader", Scope.READ));
        samples.invalidateHealth();
        if (places.find("sample-home").isEmpty()) {
            places.create(json.readTree("{\"id\":\"sample-home\",\"name\":\"Home\",\"shape\":\"point\",\"geometry\":"
                    + "{\"type\":\"Point\",\"coordinates\":[-97.75,31.0]},\"radiusKm\":20,\"timezone\":\"America/Chicago\"}"));
        }
    }

    @AfterEach
    void tearDown() {
        places.delete("sample-home", true);
    }

    private JsonNode value(JsonNode doc, int i) {
        return doc.get("values").get(i);
    }

    @Test
    @DisplayName("a point sample: value, units, label, frame key, valid time, local time, age, method, status, attribution")
    void pointSample() throws Exception {
        commitObservation(T, 0);
        HttpResponse<String> r = get("/v1/sample?lon=-97.75&lat=31.0&products=" + OBS);
        assertThat(r.statusCode()).isEqualTo(200);
        JsonNode doc = body(r);
        assertThat(SCHEMAS.validateComponent("SampleResponse", doc)).isEmpty();
        JsonNode v = value(doc, 0);
        assertThat(v.get("status").asString()).isEqualTo("ok");
        assertThat(v.get("value").asDouble()).isEqualTo(0.0);
        assertThat(v.get("units").asString()).isEqualTo("dBZ");
        assertThat(v.get("validTime").asString()).isEqualTo(T.toString());
        assertThat(v.get("localTime").asString()).isEqualTo("2026-10-10T14:00Z");
        assertThat(v.get("ageMinutes").asDouble()).isEqualTo(1.0);
        assertThat(v.get("frameKey").asString()).startsWith("20261010T140000Z~");
        assertThat(v.get("method").asString()).isEqualTo("nearest");
        assertThat(v.get("statusText").isNull()).isTrue();
        assertThat(v.get("attribution").get("licence").asString()).isEqualTo("US-PD");
        assertThat(doc.get("serverTime").asString()).isNotBlank();
    }

    @Test
    @DisplayName("no-coverage (MRMS code 0) carries the not-clear text and no value; stale and unavailable are explained")
    void statuses() throws Exception {
        commitObservation(T, 0);
        JsonNode noCov = value(body(get("/v1/sample?lon=-99.25&lat=33.0&products=" + OBS)), 0);
        assertThat(noCov.get("status").asString()).isEqualTo("no-coverage");
        assertThat(noCov.get("value").isNull()).isTrue();
        assertThat(noCov.get("statusText").asString())
                .isEqualTo("outside product coverage; this is not evidence of clear weather");
        JsonNode past = value(body(get("/v1/sample?lon=-97.75&lat=31.0&products=" + OBS + "&time=-PT2H")), 0);
        assertThat(past.get("status").asString()).isEqualTo("unavailable");
        clock.setInstant(T.plus(Duration.ofMinutes(10)));
        JsonNode stale = value(body(get("/v1/sample?lon=-97.75&lat=31.0&products=" + OBS)), 0);
        assertThat(stale.get("status").asString()).isEqualTo("stale");
        assertThat(stale.get("statusText").asString()).contains("maximum gap");
        assertThat(stale.get("frameKey").isNull()).isFalse();
    }

    @Test
    @DisplayName("a future offset samples the newest complete forecast run")
    void forecast() throws Exception {
        commitRun(T, 3);
        clock.setInstant(T.plus(Duration.ofMinutes(5)));
        HttpResponse<String> r = get("/v1/sample?lon=-97.75&lat=31.0&products=" + FCST + "&time=%2BPT1H");
        JsonNode v = value(body(r), 0);
        assertThat(v.get("status").asString()).isEqualTo("ok");
        assertThat(v.get("run").asString()).isNotBlank();
        assertThat(v.get("step").asInt()).isEqualTo(1);
        assertThat(v.get("method").asString()).isEqualTo("nearest");
        assertThat(SCHEMAS.validateComponent("SampleResponse", body(r))).isEmpty();
    }

    @Test
    @DisplayName("a place needs a token; with one, localTime is in the place's zone")
    void place() throws Exception {
        commitObservation(T, 0);
        HttpResponse<String> anonymous = get("/v1/sample?place=sample-home&products=" + OBS);
        assertThat(anonymous.statusCode()).isEqualTo(401);
        HttpResponse<String> r = get("/v1/sample?place=sample-home&products=" + OBS, "Authorization", reader);
        assertThat(r.statusCode()).isEqualTo(200);
        JsonNode doc = body(r);
        assertThat(doc.get("placeId").asString()).isEqualTo("sample-home");
        assertThat(doc.get("timezone").asString()).isEqualTo("America/Chicago");
        assertThat(value(doc, 0).get("localTime").asString()).isEqualTo("2026-10-10T09:00-05:00");
        assertThat(get("/v1/sample?place=nowhere&products=" + OBS, "Authorization", reader).statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("POST /v1/sample answers in request order; more than 100 points is 400")
    void batch() throws Exception {
        commitObservation(T, 0);
        HttpResponse<String> r = post("/v1/sample", "{\"points\":[{\"lon\":-97.75,\"lat\":31.0},{\"lon\":-99.25,\"lat\":33.0}],"
                + "\"products\":[\"" + OBS + "\"]}");
        assertThat(r.statusCode()).isEqualTo(200);
        JsonNode doc = body(r);
        assertThat(SCHEMAS.validateComponent("SampleBatchResponse", doc)).isEmpty();
        assertThat(doc.get("items")).hasSize(2);
        assertThat(doc.get("items").get(0).get("values").get(0).get("status").asString()).isEqualTo("ok");
        assertThat(doc.get("items").get(1).get("values").get(0).get("status").asString()).isEqualTo("no-coverage");
        StringBuilder many = new StringBuilder("{\"points\":[");
        for (int i = 0; i < 101; i++) {
            many.append(i == 0 ? "" : ",").append("{\"lon\":-97.75,\"lat\":31.0}");
        }
        many.append("],\"products\":[\"").append(OBS).append("\"]}");
        HttpResponse<String> tooMany = post("/v1/sample", many.toString());
        assertThat(tooMany.statusCode()).isEqualTo(400);
        assertThat(code(tooMany)).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    @DisplayName("unknown product 404, bad coordinates 400, a malformed record= 400 and an unknown one 404")
    void errors() throws Exception {
        HttpResponse<String> unknown = get("/v1/sample?lon=-97.75&lat=31.0&products=nope");
        assertThat(unknown.statusCode()).isEqualTo(404);
        assertThat(code(unknown)).isEqualTo("UNKNOWN_PRODUCT");
        assertThat(get("/v1/sample?lon=-197&lat=31.0&products=" + OBS).statusCode()).isEqualTo(400);
        assertThat(get("/v1/sample?products=" + OBS).statusCode()).isEqualTo(400);
        // prompt 106 made record= a supported parameter: a malformed ref is 400, an unknown record 404
        assertThat(get("/v1/sample?lon=-97.75&lat=31.0&products=" + OBS + "&record=ab").statusCode()).isEqualTo(400);
        HttpResponse<String> noRecord = get("/v1/sample?lon=-97.75&lat=31.0&products=" + OBS + "&record=a/b");
        assertThat(noRecord.statusCode()).isEqualTo(404);
        assertThat(code(noRecord)).isEqualTo("UNKNOWN_RECORD");
        assertThat(get("/v1/sample?lon=-97.75&lat=31.0&products=" + OBS + "&time=yesterday").statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("GET /v1/places/{id}/weather needs a token and returns the record (none yet) and forecast series")
    void placeWeather() throws Exception {
        assertThat(get("/v1/places/sample-home/weather").statusCode()).isEqualTo(401);
        HttpResponse<String> r = get("/v1/places/sample-home/weather", "Authorization", reader);
        assertThat(r.statusCode()).isEqualTo(200);
        JsonNode doc = body(r);
        assertThat(SCHEMAS.validateComponent("PlaceWeatherResponse", doc)).isEmpty();
        assertThat(doc.get("placeId").asString()).isEqualTo("sample-home");
        assertThat(doc.get("record").isNull()).isTrue();
        assertThat(doc.get("forecastSeries").isArray()).isTrue();
        assertThat(get("/v1/places/nowhere/weather", "Authorization", reader).statusCode()).isEqualTo(404);
    }
}
