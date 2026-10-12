package ai.foresignal.api;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.reference.RegionAttribution;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.reference.ReferenceFixtures;
import ai.foresignal.testsupport.reference.RegionTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code reverseGeocode} and {@code listReferenceCountries} (prompt 040): tags of a point over the
 * fixture reference data, null fields (never an error) without it, 400 for bad coordinates, and the
 * bundled country table with no database rows.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RegionTestSources.class)
class ReverseGeocodeControllerIT {

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired ApplicationContext context;

    String reader;

    @BeforeEach
    void setUp() {
        reader = TestTokens.bearer(TestTokens.create(tokens, "reverse-reader", Scope.READ));
    }

    @Test
    @DisplayName("a point gets country, admin1, county, names and near, with the GeoNames credit")
    void tags() throws Exception {
        ReferenceFixtures.loadAll(context);
        mvc.perform(get("/v1/reference/reverse").param("lon", "-97.70").param("lat", "30.37")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iso2").value("US"))
                .andExpect(jsonPath("$.admin1").value("US-TX"))
                .andExpect(jsonPath("$.admin2").value("48453"))
                .andExpect(jsonPath("$.sea").value(nullValue()))
                .andExpect(jsonPath("$.countryName").value("United States of America"))
                .andExpect(jsonPath("$.admin1Name").value("Texas"))
                .andExpect(jsonPath("$.countyName").value("Travis"))
                .andExpect(jsonPath("$.near.name").value("Austin"))
                .andExpect(jsonPath("$.near.compass16").value("NNE"))
                .andExpect(jsonPath("$.near.text").value("12 km NNE of Austin, TX"))
                .andExpect(jsonPath("$.attribution").value(RegionAttribution.GEONAMES));
        mvc.perform(get("/v1/reference/reverse").param("lon", "-85").param("lat", "25")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$.iso2").value(nullValue()))
                .andExpect(jsonPath("$.sea").value("Gulf of Mexico"));
    }

    @Test
    @DisplayName("without reference data the fields are null and only the bundled country is known; never an error")
    void degraded() throws Exception {
        mvc.perform(get("/v1/reference/reverse").param("lon", "2.35").param("lat", "48.86")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iso2").value("FR"))
                .andExpect(jsonPath("$.countryName").value("France"))
                .andExpect(jsonPath("$.admin1").value(nullValue()))
                .andExpect(jsonPath("$.near").value(nullValue()))
                .andExpect(jsonPath("$.attribution").value(nullValue()));
    }

    @Test
    @DisplayName("missing or out-of-range coordinates are 400")
    void validation() throws Exception {
        mvc.perform(get("/v1/reference/reverse").param("lon", "200").param("lat", "0")
                .header(HttpHeaders.AUTHORIZATION, reader)).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/reference/reverse").param("lat", "0")
                .header(HttpHeaders.AUTHORIZATION, reader)).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/reference/reverse").param("lon", "x").param("lat", "0")
                .header(HttpHeaders.AUTHORIZATION, reader)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("the bundled country table is served with no database rows")
    void countries() throws Exception {
        mvc.perform(get("/v1/reference/countries").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(230)))
                .andExpect(jsonPath("$[?(@.iso2 == 'TR')].aliases[*]").value(org.hamcrest.Matchers.hasItem("Türkiye")))
                .andExpect(jsonPath("$[?(@.iso2 == 'XK')].name").value("Kosovo"));
    }
}
