package ai.foresignal.api;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 * {@code searchReferenceAreas} and {@code getReferenceArea} (prompt 161) over the fixture reference data: countries,
 * admin-1 regions and places by name, the comma qualifier, the place's admin-1 region found spatially, outlines with
 * and without loaded boundaries, and validation.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RegionTestSources.class)
class ReferenceAreasControllerIT {

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired ApplicationContext context;

    String reader;

    @BeforeEach
    void setUp() {
        reader = TestTokens.bearer(TestTokens.create(tokens, "areas-reader", Scope.READ));
    }

    @Test
    @DisplayName("a typed name finds the admin-1 region, the place in it and the country, with the codes a rule uses")
    void search() throws Exception {
        ReferenceFixtures.loadAll(context);
        mvc.perform(get("/v1/reference/areas").param("q", "texas").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].kind").value("admin1"))
                .andExpect(jsonPath("$[0].code").value(ReferenceFixtures.TX))
                .andExpect(jsonPath("$[0].name").value("Texas"))
                .andExpect(jsonPath("$[0].label").value("Texas, United States of America"))
                .andExpect(jsonPath("$[0].countryIso2").value("US"));
        mvc.perform(get("/v1/reference/areas").param("q", "Austin").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].kind").value("place"))
                .andExpect(jsonPath("$[0].code").value(nullValue()))
                .andExpect(jsonPath("$[0].name").value("Austin"))
                .andExpect(jsonPath("$[0].admin1").value(ReferenceFixtures.TX))
                .andExpect(jsonPath("$[0].label").value("Austin, Texas, United States of America"))
                .andExpect(jsonPath("$[0].lon").value(closeTo(-97.74, 0.05)))
                .andExpect(jsonPath("$[0].lat").value(closeTo(30.27, 0.05)))
                .andExpect(jsonPath("$[0].population").value(931830));
        // An exact country name ranks first; a prefix of a country's name ranks before a place it only prefixes.
        mvc.perform(get("/v1/reference/areas").param("q", "Mexico").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$[0].kind").value("country"))
                .andExpect(jsonPath("$[0].code").value(ReferenceFixtures.MX));
        mvc.perform(get("/v1/reference/areas").param("q", "Aus").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$[0].kind").value("country"))
                .andExpect(jsonPath("$[?(@.name == 'Austin')].kind").value("place"));
        mvc.perform(get("/v1/reference/areas").param("q", "o").param("limit", "1")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/v1/reference/areas").param("q", "on").param("limit", "1")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("a qualifier after a comma keeps the places in that admin-1 region or country")
    void qualifier() throws Exception {
        ReferenceFixtures.loadAll(context);
        mvc.perform(get("/v1/reference/areas").param("q", "Houston, TX").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Houston"));
        mvc.perform(get("/v1/reference/areas").param("q", "Houston, Texas").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$[0].name").value("Houston"));
        mvc.perform(get("/v1/reference/areas").param("q", "Houston, Mexico").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$", hasSize(0)));
        // The admin-1 region is the one the point lies in, not GeoNames' admin-1 code: the fixture's Nuevo León box
        // ends at 24.9° N, south of the fixture's Monterrey (25.68° N), so the place has none.
        mvc.perform(get("/v1/reference/areas").param("q", "Monterrey, MX").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$[0].name").value("Monterrey"))
                .andExpect(jsonPath("$[0].admin1").value(nullValue()))
                .andExpect(jsonPath("$[0].label").value("Monterrey, Mexico"));
    }

    @Test
    @DisplayName("without reference data only the bundled countries are found, and an admin-1 outline is 409")
    void degraded() throws Exception {
        mvc.perform(get("/v1/reference/areas").param("q", "Austin").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/v1/reference/areas").param("q", "France").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$[0].kind").value("country"))
                .andExpect(jsonPath("$[0].code").value("FR"));
        mvc.perform(get("/v1/reference/areas/FR").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("country"))
                .andExpect(jsonPath("$.name").value("France"))
                .andExpect(jsonPath("$.geometry.type").exists());
        mvc.perform(get("/v1/reference/areas/US-TX").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REFERENCE_DATA_MISSING"));
    }

    @Test
    @DisplayName("an outline has names, a bounding box and a geometry; unknown codes are 404, malformed 400")
    void outline() throws Exception {
        ReferenceFixtures.loadAll(context);
        mvc.perform(get("/v1/reference/areas/us-tx").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("admin1"))
                .andExpect(jsonPath("$.code").value(ReferenceFixtures.TX))
                .andExpect(jsonPath("$.name").value("Texas"))
                .andExpect(jsonPath("$.countryName").value("United States of America"))
                .andExpect(jsonPath("$.bbox", hasSize(4)))
                .andExpect(jsonPath("$.geometry.coordinates").isArray());
        mvc.perform(get("/v1/reference/areas/US").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("country"))
                .andExpect(jsonPath("$.countryIso2").value("US"));
        mvc.perform(get("/v1/reference/areas/US-ZZ").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isNotFound());
        mvc.perform(get("/v1/reference/areas/QQ").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isNotFound());
        mvc.perform(get("/v1/reference/areas/" + "X".repeat(33)).header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isBadRequest());
    }
}
