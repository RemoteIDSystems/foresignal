package ai.foresignal.source.aggregator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.test.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * The aggregator surface (prompt R2-019) through the API: list, get, update and test dispatch by {@code {domain}}; an
 * unknown domain is 404 UNKNOWN_AGGREGATOR; a configuration round-trips; a domain without a backend can be stored
 * disabled but not enabled; validation reports every problem.
 */
@IntegrationTest
@AutoConfigureMockMvc
class AggregatorSurfaceIT extends AggregatorItSupport {

    @Autowired MockMvc mvc;

    private ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
        request.header(HttpHeaders.AUTHORIZATION, admin);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"'));
        }
        return mvc.perform(request);
    }

    @Test
    @DisplayName("the list has every domain, off and unconfigured, with adsb implemented")
    void listsEveryDomain() throws Exception {
        send(get("/v1/aggregators"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(5))
                .andExpect(jsonPath("$.items[0].domain").value("adsb"))
                .andExpect(jsonPath("$.items[0].implemented").value(true))
                .andExpect(jsonPath("$.items[0].enabled").value(false))
                .andExpect(jsonPath("$.items[0].status").value("unconfigured"))
                .andExpect(jsonPath("$.items[0].sourceIds[0]").value("local-adsb-aggregator"))
                .andExpect(jsonPath("$.items[0].supersededSources[?(@ == 'adsblol-world')]").exists())
                .andExpect(jsonPath("$.items[0].supersededSources[?(@ == 'gpsjam-daily')]").exists())
                .andExpect(jsonPath("$.items[1].domain").value("ais"))
                .andExpect(jsonPath("$.items[4].domain").value("ground-sensors"));
    }

    @Test
    @DisplayName("an unknown domain is 404 UNKNOWN_AGGREGATOR on every operation")
    void unknownDomain() throws Exception {
        send(get("/v1/aggregators/radar"), null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNKNOWN_AGGREGATOR"));
        send(put("/v1/aggregators/radar"), "{'enabled':false}").andExpect(status().isNotFound());
        send(post("/v1/aggregators/radar/test"), null).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("an adsb configuration round-trips and its test reads the endpoint")
    void adsbRoundTripsAndTests() throws Exception {
        stubReadsb();
        turnedOn.add("local-adsb-aggregator");
        String body = "{'enabled':true,'mode':'hybrid','endpoint':'" + endpoint() + "','config':{'band':'1090'},"
                + "'disableSupersededSources':false}";

        send(put("/v1/aggregators/adsb"), body)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.mode").value("hybrid"))
                .andExpect(jsonPath("$.endpoint").value(endpoint()))
                .andExpect(jsonPath("$.config.band").value("1090"))
                .andExpect(jsonPath("$.disableSupersededSources").value(false));

        send(get("/v1/aggregators/adsb"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("hybrid"))
                .andExpect(jsonPath("$.endpoint").value(endpoint()))
                .andExpect(jsonPath("$.status").isNotEmpty());

        send(post("/v1/aggregators/adsb/test"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.reachable").value(true))
                .andExpect(jsonPath("$.sampleCount").value(2))
                .andExpect(jsonPath("$.checkedAt").isNotEmpty());
    }

    @Test
    @DisplayName("a test with no endpoint, or an unreachable one, fails without a 5xx")
    void testFailures() throws Exception {
        send(post("/v1/aggregators/adsb/test"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.reachable").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
        send(post("/v1/aggregators/ais/test"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    @DisplayName("a domain without a backend stores a disabled configuration but cannot be enabled")
    void pendingDomain() throws Exception {
        // ais has a backend since R2-020; ground-sensors has none until R2-023.
        send(put("/v1/aggregators/ground-sensors"), "{'enabled':false,'mode':'receiver',"
                + "'endpoint':'tcp://sensors:10110','config':{'note':'later'}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.implemented").value(false))
                .andExpect(jsonPath("$.mode").value("receiver"))
                .andExpect(jsonPath("$.config.note").value("later"))
                .andExpect(jsonPath("$.status").value("unconfigured"));
        send(put("/v1/aggregators/ground-sensors"), "{'enabled':true}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("enabled"));
    }

    @Test
    @DisplayName("validation reports every problem in one 400 and changes nothing")
    void validation() throws Exception {
        send(put("/v1/aggregators/adsb"), "{'mode':'satellite','endpoint':5,'config':[],'extra':1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'enabled')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'mode')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'endpoint')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'config')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'extra')]").exists());
        send(put("/v1/aggregators/adsb"), "{'enabled':true}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("endpoint"));
        send(put("/v1/aggregators/adsb"), "{'enabled':true,'endpoint':'http://127.0.0.1/tar1090/'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("endpoint"));
        send(put("/v1/aggregators/adsb"), "{'enabled':true,'endpoint':'http://203.0.113.10/data/aircraft.json'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("endpoint"));
        send(put("/v1/aggregators/adsb"), "{'enabled':false,'config':{'band':'433'}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("config.band"));
        send(get("/v1/aggregators/adsb"), null).andExpect(jsonPath("$.enabled").value(false));
    }
}
