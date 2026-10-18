package ai.foresignal.media.outlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.evidence.Originator;
import ai.foresignal.news.NewsStoryResolver;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.news.NewsTestSupport;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The outlet registry (prompt 133): domain resolution (parent domains, merges), unknown domains added as
 * {@code unrated}, the seeded wires mapped to their own originators, syndication through {@code aggregates}, and the
 * registry edit that never touches a rating.
 */
@IntegrationTest
class OutletRegistryTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired OutletRegistry registry;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        NewsTestSupport.clean(jdbc);
        registry.seedWires();
    }

    @Test
    @DisplayName("the seeded wires are type wire and map to their own originators")
    void wires() {
        OutletRegistry.Outlet ap = registry.byDomain("apnews.com").orElseThrow();
        assertThat(ap.type()).isEqualTo("wire");
        assertThat(ap.originatorId()).isEqualTo("ap");
        assertThat(ap.rated()).isFalse();
        assertThat(registry.byDomain("ap.org").orElseThrow().id()).isEqualTo(ap.id());
        assertThat(registry.wires()).extracting(OutletRegistry.Outlet::originatorId).contains("ap", "reuters", "afp");
        Originator o = NewsStoryResolver.originatorOf(ap);
        assertThat(o.id()).isEqualTo("ap");
        assertThat(o.originatorClass()).isEqualTo("media");
        registry.seedWires();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM media_outlet WHERE originator_id = 'ap'", Integer.class))
                .as("seeding is idempotent").isEqualTo(1);
    }

    @Test
    @DisplayName("an unknown domain is registered once as unrated, with its country and language")
    void unknownDomain() {
        String id = registry.resolve("www.Example-News.com", "United States", "English");
        OutletRegistry.Outlet o = registry.get(id);
        assertThat(o.name()).isEqualTo("example-news.com");
        assertThat(o.domains()).containsExactly("example-news.com");
        assertThat(o.type()).isEqualTo("other");
        assertThat(o.rated()).isFalse();
        assertThat(o.country()).isEqualTo("US");
        assertThat(o.languages()).containsExactly("en");
        assertThat(registry.resolve("example-news.com", null, null)).isEqualTo(id);
        assertThat(NewsStoryResolver.originatorOf(o).id()).isEqualTo("outlet:" + id);
    }

    @Test
    @DisplayName("a subdomain resolves to its parent domain's outlet")
    void parentDomain() {
        String id = registry.resolve("example-news.com", null, null);
        assertThat(registry.byDomain("edition.example-news.com").orElseThrow().id()).isEqualTo(id);
        assertThat(registry.byDomain("com")).isEmpty();
    }

    @Test
    @DisplayName("syndication: no aggregates means any wire; listed aggregates restrict; a wire never syndicates")
    void syndication() {
        OutletRegistry.Outlet any = registry.get(registry.resolve("daily-a.example", null, null));
        assertThat(any.syndicates("ap")).isTrue();
        OutletRegistry.Outlet restricted = registry.patch(registry.resolve("daily-b.example", null, null),
                JSON.readTree("{\"aggregates\":[\"reuters\"]}"));
        assertThat(restricted.syndicates("reuters")).isTrue();
        assertThat(restricted.syndicates("ap")).isFalse();
        assertThat(registry.byDomain("apnews.com").orElseThrow().syndicates("reuters")).isFalse();
    }

    @Test
    @DisplayName("PATCH edits registry facts and merges outlets: domains and ratings move, the loser resolves to the survivor")
    void patchAndMerge() {
        String a = registry.resolve("alpha.example", "US", "en");
        String b = registry.resolve("alpha-news.example", "US", "en");
        Instant now = Instant.parse("2026-10-17T00:00:00Z");
        registry.upsertRating(new OutletRegistry.Rating(b, "operator", "lean", -1.0, "-1", "lean-left", null,
                "CC BY 4.0", "Example", null, "OPEN", now));
        OutletRegistry.Outlet merged = registry.patch(a, JSON.readTree("{\"name\":\"Alpha News\",\"type\":\"national\","
                + "\"ownerOrg\":\"Alpha Media Group\",\"wikidataQid\":\"Q42\",\"mergeOutletIds\":[\"" + b + "\"]}"));
        assertThat(merged.name()).isEqualTo("Alpha News");
        assertThat(merged.type()).isEqualTo("national");
        assertThat(merged.ownerOrg()).isEqualTo("Alpha Media Group");
        assertThat(merged.wikidataQid()).isEqualTo("Q42");
        assertThat(merged.domains()).contains("alpha.example", "alpha-news.example");
        assertThat(registry.ratings(a)).extracting(OutletRegistry.Rating::provider).containsExactly("operator");
        assertThat(registry.get(b).id()).isEqualTo(a);
        assertThat(registry.byDomain("alpha-news.example").orElseThrow().id()).isEqualTo(a);
        assertThat(merged.rated()).isTrue();
    }

    @Test
    @DisplayName("PATCH refuses rating members, bad types and QIDs; an unknown outlet is 404")
    void patchValidation() {
        String a = registry.resolve("beta.example", null, null);
        assertThatThrownBy(() -> registry.patch(a, JSON.readTree("{\"ratings\":[]}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> registry.patch(a, JSON.readTree("{\"type\":\"tabloid\"}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> registry.patch(a, JSON.readTree("{\"wikidataQid\":\"42\"}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> registry.patch(a, JSON.readTree("{\"mergeOutletIds\":[\"" + a + "\"]}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> registry.get("01NOPE0000000000000000000"))
                .isInstanceOfSatisfying(NotFoundException.class, e -> assertThat(e.getMessage()).contains("outlet"));
    }

    @Test
    @DisplayName("consensus is stored per dimension over the enabled providers and listed by bucket")
    void consensusAndBuckets() {
        String a = registry.resolve("gamma.example", null, null);
        Instant now = Instant.parse("2026-10-17T00:00:00Z");
        registry.upsertRating(new OutletRegistry.Rating(a, "operator", "lean", 1.5, "1.5", null, null, "CC BY 4.0",
                "Example", null, "OPEN", now));
        registry.upsertRating(new OutletRegistry.Rating(a, "allsides", "lean", 2.0, "Right", "Right", null,
                "CC BY-NC 4.0", "AllSides", null, "LOCAL_ONLY", now));
        registry.recomputeConsensus(a, Set.of("operator"));
        assertThat(registry.consensus(a).get("lean").median()).as("allsides disabled").isEqualTo(1.5);
        registry.recomputeConsensus(a, Set.of("operator", "allsides"));
        Consensus.Result lean = registry.consensus(a).get("lean");
        assertThat(lean.median()).isEqualTo(1.75);
        assertThat(lean.bucket()).isEqualTo("right");
        assertThat(lean.providers()).extracting(Consensus.ProviderValue::egress).contains("LOCAL_ONLY");
        assertThat(registry.list(null, null, null, "right", null, 10).items()).extracting(OutletRegistry.Outlet::id)
                .containsExactly(a);
        assertThat(registry.list(null, null, null, "unrated", null, 100).items()).extracting(OutletRegistry.Outlet::id)
                .doesNotContain(a);
        assertThat(registry.list("gamma", null, null, null, null, 10).items()).hasSize(1);
        assertThat(List.of(registry.list(null, null, "wire", null, null, 500).items().size())).first()
                .isNotEqualTo(0);
    }
}
