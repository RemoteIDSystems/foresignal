package ai.foresignal.media.outlet;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.ProbeService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.news.NewsTestSupport;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The declared rating providers (prompt 133, NEWS-1): the Wikidata SPARQL mapping (a live capture), The Partisan Web
 * axis mapping (a live file and a synthetic one), the operator CSV and JSON rows, the license acknowledgement gate,
 * and {@code LOCAL_ONLY} egress for AllSides and MBFC. Every stored rating names its provider, license and date; none
 * is generated.
 */
@IntegrationTest
class RatingProvidersTest extends WireMockSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired OutletRegistry registry;
    @Autowired MediaProviders providers;
    @Autowired RatingImportService imports;
    @Autowired JdbcTemplate jdbc;
    @Autowired ApplicationContext context;
    @Autowired Clock clock;

    @BeforeEach
    void setUp() {
        NewsTestSupport.clean(jdbc);
        registry.seedWires();
    }

    // ------------------------------------------------------------------------- pure mappings

    @Test
    @DisplayName("Wikidata: P856 lookup, item choice, alignment to lean, state media, public broadcaster and agency")
    void wikidataMapping() {
        JsonNode results = JSON.readTree(AdapterFixtures.text("media-ratings", "wikidata-sparql.json"));
        List<String> domains = List.of("foxnews.com", "theguardian.com", "rt.com", "reuters.com", "bbc.co.uk");
        Map<String, WikidataParser.DomainFacts> facts = WikidataParser.parse(results, domains,
                RatingImportService.WIKIDATA_ITEM);
        WikidataParser.DomainFacts fox = facts.get("foxnews.com");
        assertThat(fox.qid()).isEqualTo("Q186068");
        assertThat(fox.country()).isEqualTo("US");
        assertThat(fox.ratings()).filteredOn(r -> r.dimension().equals("lean")).singleElement()
                .satisfies(r -> {
                    assertThat(r.valueNorm()).isEqualTo(2.0);
                    assertThat(r.valueRaw()).isEqualTo("P1387=Q76074");
                    assertThat(r.url()).isEqualTo("https://www.wikidata.org/wiki/Q186068");
                });
        WikidataParser.DomainFacts guardian = facts.get("theguardian.com");
        assertThat(guardian.ratings()).filteredOn(r -> r.dimension().equals("lean")).singleElement()
                .extracting(RatingRow::valueNorm).isEqualTo(-1.0);
        assertThat(guardian.ownerOrg()).isEqualTo("Guardian Media Group");
        WikidataParser.DomainFacts reuters = facts.get("reuters.com");
        assertThat(reuters.qid()).as("the news agency item wins over the online newspaper item").isEqualTo("Q130879");
        assertThat(reuters.type()).isEqualTo("wire");
        WikidataParser.DomainFacts bbc = facts.get("bbc.co.uk");
        assertThat(bbc.qid()).isEqualTo("Q9531");
        assertThat(bbc.type()).isEqualTo("public-broadcaster");
        assertThat(bbc.ratings()).extracting(RatingRow::label).contains("public-broadcaster");
        assertThat(WikidataParser.query(List.of("example.com"))).contains("<https://www.example.com/>")
                .contains("wdt:P856").contains("wdt:P1387");
    }

    @Test
    @DisplayName("Partisan Web: bias_social to lean, fact_sourcing to factuality, other axes kept separate, template skipped")
    void partisanWebMapping() {
        List<RatingRow> afp = PartisanWebParser.parse(AdapterFixtures.text("media-ratings", "partisan-web-afp.md"),
                "https://github.com/stockhuman/tpw-data/blob/master/organisations/afp.md");
        assertThat(afp).extracting(RatingRow::domain).containsOnly("afp.com");
        assertThat(afp).filteredOn(r -> r.dimension().equals("lean")).singleElement()
                .extracting(RatingRow::valueNorm).isEqualTo(0.0);
        assertThat(afp).filteredOn(r -> r.dimension().equals("factuality")).as("fact_sourcing -1: never assessed")
                .isEmpty();
        assertThat(afp).filteredOn(r -> r.dimension().equals("partisan-web:editorial")).singleElement()
                .extracting(RatingRow::valueNorm).isEqualTo(9.0);
        assertThat(afp).filteredOn(r -> r.dimension().equals("state-affiliation")).singleElement()
                .extracting(RatingRow::label).isEqualTo("state-sponsored");
        assertThat(afp.get(0).outlet().country()).isEqualTo("FR");
        assertThat(afp.get(0).outlet().ownerOrg()).as("ownership may name people: never imported").isNull();

        List<RatingRow> left = PartisanWebParser.parse(AdapterFixtures.text("media-ratings",
                "partisan-web-example-left.md"), null);
        assertThat(left).filteredOn(r -> r.dimension().equals("lean")).singleElement()
                .satisfies(r -> assertThat(r.valueNorm()).isEqualTo(-1.4));
        assertThat(left).filteredOn(r -> r.dimension().equals("factuality")).singleElement()
                .extracting(RatingRow::valueNorm).isEqualTo(3.4);
        assertThat(left).filteredOn(r -> r.dimension().equals("partisan-web:governmental")).singleElement()
                .extracting(RatingRow::valueNorm).as("never forced into lean").isEqualTo(3.0);
        assertThat(left).extracting(RatingRow::dimension).contains("partisan-web:ideological",
                "partisan-web:pseudoscience", "partisan-web:conspiracy", "partisan-web:message");
        assertThat(PartisanWebParser.parse(AdapterFixtures.text("media-ratings", "partisan-web-template.md"), null))
                .as("the template has no url").isEmpty();
    }

    @Test
    @DisplayName("operator rows: CSV and JSON, labels mapped, ranges checked, license and attribution required")
    void operatorRows() {
        OperatorRatingsParser.Parsed csv = OperatorRatingsParser.csv("domain,dimension,value,label,licence,attribution\n"
                + "example-news.com,lean,-1,,CC BY 4.0,Example project\n"
                + "https://www.example-news.com/x,factuality,High,,CC BY 4.0,Example project\n"
                + "example-news.com,lean,3,,CC BY 4.0,Example project\n"
                + "example-news.com,tone,1,,CC BY 4.0,Example project\n"
                + "example-news.com,lean,Right,,,\n", "operator");
        assertThat(csv.rows()).hasSize(2);
        assertThat(csv.rows().get(0).valueNorm()).isEqualTo(-1.0);
        assertThat(csv.rows().get(0).label()).isEqualTo("lean-left");
        assertThat(csv.rows().get(1).domain()).isEqualTo("example-news.com");
        assertThat(csv.rows().get(1).valueNorm()).isEqualTo(4.0);
        assertThat(csv.rejected()).isEqualTo(3);
        assertThat(csv.errors()).extracting(e -> e.get("row")).containsExactly(4, 5, 6);

        OperatorRatingsParser.Parsed json = OperatorRatingsParser.json(JSON.readTree("{\"rows\":[{\"domain\":"
                + "\"example.org\",\"dimension\":\"acme:axis\",\"value\":\"7\",\"licence\":\"CC0\",\"attribution\":"
                + "\"Acme\"}]}"), "operator");
        assertThat(json.rows()).singleElement().extracting(RatingRow::dimension).isEqualTo("acme:axis");

        OperatorRatingsParser.Parsed mbfc = OperatorRatingsParser.json(JSON.readTree("{\"rows\":[{\"domain\":"
                + "\"example.org\",\"dimension\":\"lean\",\"value\":\"Left-Center\"},{\"domain\":\"example.org\","
                + "\"dimension\":\"lean\",\"value\":\"Satire\"},{\"domain\":\"example.org\",\"dimension\":"
                + "\"factuality\",\"value\":\"Mostly Factual\"}]}"), "mbfc");
        assertThat(mbfc.rows()).extracting(RatingRow::dimension).containsExactly("lean", "mbfc:category",
                "factuality");
        assertThat(mbfc.rows()).extracting(RatingRow::valueNorm).containsExactly(-1.0, null, 3.5);

        assertThat(RatingImportService.operatorEgress("CC BY 4.0")).isEqualTo("OPEN");
        assertThat(RatingImportService.operatorEgress("CC BY-NC 4.0")).isEqualTo("LOCAL_ONLY");
        assertThat(RatingImportService.operatorEgress("proprietary, licensed to us")).isEqualTo("LOCAL_ONLY");
    }

    // ------------------------------------------------------------------------- gate and egress

    @Test
    @DisplayName("the defaults: wikidata, partisan-web and operator on; allsides and mbfc off and LOCAL_ONLY")
    void defaults() {
        assertThat(providers.enabled()).containsExactlyInAnyOrder("wikidata", "partisan-web", "operator");
        assertThat(providers.get("allsides").licenseAcknowledged()).isFalse();
        assertThat(providers.get("allsides").egress()).isEqualTo("LOCAL_ONLY");
        assertThat(providers.get("mbfc").egress()).isEqualTo("LOCAL_ONLY");
        assertThat(providers.get("wikidata").egress()).isEqualTo("OPEN");
        assertThat(providers.get("partisan-web").view()).containsEntry("licence", "CC BY 4.0");
    }

    @Test
    @DisplayName("enabling or importing without an acknowledged license is 409; acknowledging in the same call works")
    void licenseGate() {
        assertThatThrownBy(() -> providers.update("allsides", new MediaProviders.Input(true, null, null, null),
                "admin")).isInstanceOfSatisfying(ConflictException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.PROVIDER_LICENCE_NOT_ACKNOWLEDGED));
        assertThatThrownBy(() -> imports.importRatings("allsides", new RatingImportService.Body(null,
                "domain,dimension,value\nexample.org,lean,Left\n"), "admin"))
                .isInstanceOf(ConflictException.class);
        MediaProviders.State state = providers.update("allsides", new MediaProviders.Input(true, true, null, null),
                "token:admin");
        assertThat(state.enabled()).isTrue();
        assertThat(state.acknowledgedBy()).isEqualTo("token:admin");
        assertThat(providers.update("allsides", new MediaProviders.Input(null, false, null, null), "admin").enabled())
                .as("withdrawing the acknowledgement disables").isFalse();
        assertThatThrownBy(() -> providers.update("wikidata", new MediaProviders.Input(null, null, null, "key"),
                "admin")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> providers.update("mbfc", new MediaProviders.Input(null, null, true, null), "admin"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("AllSides ratings are LOCAL_ONLY until permission is recorded; MBFC stays LOCAL_ONLY; the key is stored")
    void localOnlyEgress() {
        providers.update("allsides", new MediaProviders.Input(true, true, null, null), "admin");
        Map<String, Object> result = imports.importRatings("allsides", new RatingImportService.Body(null,
                "domain,dimension,value\nexample-left.org,lean,Lean Left\n"), "admin");
        assertThat(result).containsEntry("imported", 1).containsEntry("outletsCreated", 1);
        String outlet = registry.byDomain("example-left.org").orElseThrow().id();
        OutletRegistry.Rating rating = registry.ratings(outlet).get(0);
        assertThat(rating.egress()).isEqualTo("LOCAL_ONLY");
        assertThat(rating.license()).contains("CC BY-NC 4.0");
        assertThat(rating.attribution()).contains("AllSides");
        assertThat(rating.valueNorm()).isEqualTo(-1.0);
        assertThat(OutletViews.rating(rating, false)).containsEntry("valueNorm", null).containsEntry("label", null);
        assertThat(OutletViews.rating(rating, true)).containsEntry("valueNorm", -1.0);

        providers.update("allsides", new MediaProviders.Input(null, null, true, null), "admin");
        imports.importRatings("allsides", new RatingImportService.Body(null,
                "domain,dimension,value\nexample-left.org,lean,Lean Left\n"), "admin");
        assertThat(registry.ratings(outlet).get(0).egress()).isEqualTo("OPEN");

        providers.update("mbfc", new MediaProviders.Input(true, true, null, "rapidapi-key"), "admin");
        assertThat(providers.get("mbfc").credentialSet()).isTrue();
        assertThat(providers.apiKey("mbfc")).isEqualTo("rapidapi-key");
        assertThat(providers.get("mbfc").view()).doesNotContainKey("apiKey");
        imports.importRatings("mbfc", new RatingImportService.Body(JSON.readTree("{\"rows\":[{\"domain\":"
                + "\"example-left.org\",\"dimension\":\"factuality\",\"value\":\"High\"}]}"), null), "admin");
        assertThat(registry.ratings(outlet)).filteredOn(r -> r.provider().equals("mbfc")).singleElement()
                .extracting(OutletRegistry.Rating::egress).isEqualTo("LOCAL_ONLY");
        assertThatThrownBy(() -> imports.importRatings("mbfc", RatingImportService.Body.NONE, "admin"))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.RATINGS_IMPORT_INVALID));
    }

    @Test
    @DisplayName("an operator import stores each row's license, recomputes the consensus and reports rejects")
    void operatorImport() {
        Map<String, Object> result = imports.importRatings("operator", new RatingImportService.Body(null,
                "domain,dimension,value,label,licence,attribution\n"
                        + "apnews.com,lean,0,,CC BY 4.0,Example project\n"
                        + "apnews.com,factuality,5,,CC BY 4.0,Example project\n"
                        + "bad,lean,0,,CC BY 4.0,Example project\n"), "admin");
        assertThat(result).containsEntry("imported", 2).containsEntry("rejected", 1).containsEntry("outletsCreated", 0);
        String ap = registry.byDomain("apnews.com").orElseThrow().id();
        assertThat(registry.consensus(ap).get("lean").bucket()).isEqualTo("center");
        assertThat(registry.consensus(ap).get("factuality").median()).isEqualTo(5.0);
        assertThat(registry.get(ap).rated()).isTrue();
        assertThat(imports.importRatings("operator", new RatingImportService.Body(null,
                "domain,dimension,value,label,licence,attribution\napnews.com,lean,0.5,,CC BY 4.0,Example project\n"),
                "admin")).containsEntry("updated", 1);
        assertThatThrownBy(() -> imports.importRatings("operator", new RatingImportService.Body(null,
                "domain,dimension\n"), "admin")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> imports.importRatings("nope", RatingImportService.Body.NONE, "admin"))
                .isInstanceOf(ValidationException.class);
        assertThat(providers.get("operator").lastImportStatus()).isNotNull();
    }

    @Test
    @DisplayName("Partisan Web through the budgeted upstream path: listing, raw files, hosts outside the allowlist skipped")
    void partisanWebImport() {
        String listing = AdapterFixtures.text("media-ratings", "partisan-web-listing.json").replace("{{BASE}}",
                baseUrl());
        WIREMOCK.stubFor(get(urlPathEqualTo("/repos/stockHuman/tpw-data/contents/organisations")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(listing)));
        WIREMOCK.stubFor(get(urlPathEqualTo("/raw/organisations/afp.md")).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.text("media-ratings", "partisan-web-afp.md"))));
        WIREMOCK.stubFor(get(urlPathEqualTo("/raw/organisations/example-left.md")).willReturn(aResponse()
                .withStatus(200).withBody(AdapterFixtures.text("media-ratings", "partisan-web-example-left.md"))));
        RatingImportService local = new RatingImportService(registry, providers,
                context.getBeanProvider(ProbeService.class), JSON, clock, baseUrl() + "/sparql",
                baseUrl() + "/repos/stockHuman/tpw-data/contents/organisations", false);
        Map<String, Object> result = local.importRatings("partisan-web", RatingImportService.Body.NONE, "admin");
        assertThat(result.get("errors").toString()).doesNotContain("HTTP");
        String left = registry.byDomain("example-left.com").orElseThrow().id();
        assertThat(registry.ratings(left)).extracting(OutletRegistry.Rating::provider).containsOnly("partisan-web");
        assertThat(registry.ratings(left)).extracting(OutletRegistry.Rating::license).containsOnly("CC BY 4.0");
        assertThat(registry.consensus(left).get("lean").bucket()).isEqualTo("left");
        assertThat(local.allowedDownload("https://example.org/elsewhere.md")).isFalse();
        assertThat(local.allowedDownload("https://raw.githubusercontent.com/stockhuman/tpw-data/master/x.md"))
                .isTrue();
    }
}
