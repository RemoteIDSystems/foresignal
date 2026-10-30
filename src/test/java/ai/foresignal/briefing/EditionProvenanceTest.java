package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.intel.EventScorer;
import ai.foresignal.intel.IntelCorroborationService;
import ai.foresignal.provenance.CredibilityService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Edition provenance and corroboration (prompt NP-007): with the flags on, each record and alert citation carries the
 * evidence refs that put it in the edition and a corroboration badge; with them off, neither member appears; the HTML
 * page shows the badges and the "why included" line, and the audio script mentions single-source items.
 */
class EditionProvenanceTest {

    static final Instant T = Instant.parse("2026-10-30T07:00:00Z");

    static final EvidenceStore.StoredEvent EVENT = new EvidenceStore.StoredEvent("01JEVENT", "earthquake", T, 139.8,
            35.6, List.of("JP"), null, T, "record:usgs/q1@7", 3, 2, List.of(), List.of("usgs", "jma"), "open", null,
            "1", T, T);

    @SuppressWarnings("unchecked")
    static <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    /** The annotator with the record and event lookups replaced by fixtures. */
    static EditionProvenance annotator(CredibilityService grades) {
        IntelCorroborationService policy = mock(IntelCorroborationService.class);
        when(policy.quality()).thenReturn(EventScorer.Quality.DEFAULT);
        return new EditionProvenance(mock(JdbcTemplate.class), provider(null), provider(grades), provider(policy)) {
            @Override
            Item item(Map<String, Object> citation) {
                return switch (String.valueOf(citation.get("id"))) {
                    case "usgs:q1" -> new Item("usgs", "q1", "earthquake", 7L, 0, null, null);
                    case "alert:42" -> new Item("usgs", "q2", "earthquake", 8L, 0, 42L, "Big quakes");
                    case "usgs:gone" -> new Item("usgs", "gone", null, null, 0, null, null);
                    default -> null;
                };
            }

            @Override
            List<EvidenceStore.StoredEvent> events(Item item) {
                return "q1".equals(item.externalId()) ? List.of(EVENT) : List.of();
            }
        };
    }

    static CredibilityService grades() {
        CredibilityService grades = mock(CredibilityService.class);
        when(grades.load()).thenReturn(new CredibilityService.Lookup(Map.of(), java.util.Set.of()));
        when(grades.reliability("usgs")).thenReturn("A");
        when(grades.assess(eq("usgs"), eq("q1"), anyString(), anyInt(), any())).thenReturn(2);
        when(grades.assess(eq("usgs"), eq("q2"), anyString(), anyInt(), any())).thenReturn(6);
        return grades;
    }

    static List<Map<String, Object>> citations() {
        return List.of(
                Map.of("id", "usgs:q1", "seq", 7L, "title", "M 6.1 Tokyo Bay", "type", "record"),
                Map.of("id", "alert:42", "title", "Big quakes: M 5.0", "type", "alert"),
                Map.of("id", "usgs:gone", "title", "A removed record", "type", "record"),
                Map.of("id", "briefing:b1", "title", "Yesterday", "type", "briefing"));
    }

    @Test
    @DisplayName("flags on: record and alert items carry evidence refs, a reason and a corroboration badge")
    void flagsOn() {
        List<Map<String, Object>> out = annotator(grades()).annotate(citations(), true, true);

        assertThat(out.get(0)).containsKey("provenance").containsKey("corroboration");
        @SuppressWarnings("unchecked")
        Map<String, Object> provenance = (Map<String, Object>) out.get(0).get("provenance");
        assertThat(provenance.get("refs")).isEqualTo(List.of("record:usgs/q1@7", "canonical-event:01JEVENT"));
        assertThat(provenance.get("reason")).isEqualTo("In scope: earthquake from usgs, linked to 1 event");
        assertThat(out.get(0).get("corroboration")).isEqualTo(Map.of("evidenceQuality", "strong",
                "independentSources", 2, "credibility", 2, "reliability", "A"));

        @SuppressWarnings("unchecked")
        Map<String, Object> alert = (Map<String, Object>) out.get(1).get("provenance");
        assertThat(alert.get("refs")).isEqualTo(List.of("alert:42", "record:usgs/q2@8"));
        assertThat(alert.get("reason")).isEqualTo("Alert of rule 'Big quakes' on earthquake from usgs");
        assertThat(out.get(1).get("corroboration")).isEqualTo(Map.of("evidenceQuality", "weak",
                "independentSources", 1, "credibility", 6, "reliability", "A"));

        @SuppressWarnings("unchecked")
        Map<String, Object> gone = (Map<String, Object>) out.get(2).get("corroboration");
        assertThat(gone).containsEntry("evidenceQuality", "insufficient").containsEntry("independentSources", 0);

        assertThat(out.get(3)).doesNotContainKeys("provenance", "corroboration");
    }

    @Test
    @DisplayName("flags off: neither member appears; each flag adds only its own member")
    void flagsOff() {
        EditionProvenance annotator = annotator(grades());
        assertThat(annotator.annotate(citations(), false, false)).isEqualTo(citations());
        assertThat(annotator.annotate(citations(), true, false))
                .allSatisfy(c -> assertThat(c).doesNotContainKey("corroboration"));
        assertThat(annotator.annotate(citations(), false, true))
                .allSatisfy(c -> assertThat(c).doesNotContainKey("provenance"));
    }

    @Test
    @DisplayName("evidence quality follows the 154 policy's strongGroups")
    void quality() {
        assertThat(EditionProvenance.quality(2, 2, EventScorer.Quality.DEFAULT)).isEqualTo("strong");
        assertThat(EditionProvenance.quality(2, 2, new EventScorer.Quality(3, 3, 2, 0))).isEqualTo("moderate");
        assertThat(EditionProvenance.quality(1, 3, EventScorer.Quality.DEFAULT)).isEqualTo("moderate");
        assertThat(EditionProvenance.quality(1, 6, EventScorer.Quality.DEFAULT)).isEqualTo("weak");
        assertThat(EditionProvenance.quality(1, null, EventScorer.Quality.DEFAULT)).isEqualTo("weak");
    }

    @Test
    @DisplayName("the HTML page shows the badges and the why-included line; without annotations there is no aside")
    void html() {
        BriefingHtmlRenderer renderer = new BriefingHtmlRenderer();
        List<Map<String, Object>> annotated = annotator(grades()).annotate(citations(), true, true);

        String page = renderer.page("Edition", "# Edition\n\nText.", null, "en", annotated);
        assertThat(page).contains("<aside class=\"sources\"><h2>Sources</h2>")
                .contains("M 6.1 Tokyo Bay<span class=\"badge strong\">strong</span>")
                .contains("<span class=\"badge\">2 sources</span>").contains("<span class=\"badge\">A2</span>")
                .contains("Why included: In scope: earthquake from usgs, linked to 1 event")
                .contains("<span class=\"badge weak\">weak</span>").doesNotContain("Yesterday");

        assertThat(renderer.page("Edition", "# Edition", null, "en", citations())).doesNotContain("<aside");
        assertThat(renderer.page("Edition", "# Edition", null, "en", citations()))
                .isEqualTo(renderer.page("Edition", "# Edition", null, "en"));
    }

    @Test
    @DisplayName("the audio script mentions single-source items only when there are some")
    void spoken() {
        List<Map<String, Object>> annotated = annotator(grades()).annotate(citations(), true, true);
        assertThat(EditionProvenance.spokenNote(annotated))
                .isEqualTo("A note on sourcing: 2 of the 3 items in this edition rest on a single, uncorroborated source.");
        assertThat(EditionProvenance.spokenNote(citations())).isNull();
        assertThat(EditionProvenance.spokenNote(List.of(annotated.get(0)))).isNull();
    }

    @Test
    @DisplayName("validation: the flags are booleans, default false, and are stored and read back")
    void validation() {
        BriefingInput.Edition edition = new BriefingInput.Edition(null, List.of(), BriefingInput.Site.OFF, null, true,
                false);
        BriefingEntity entity = new BriefingEntity();
        new BriefingInput("E", true, "0 7 * * *", "UTC", BriefingScope.fromMap(Map.of()), null, null, null, null,
                null, 10, null, edition).applyTo(entity);
        assertThat(entity.isIncludeProvenance()).isTrue();
        assertThat(entity.isIncludeCorroboration()).isFalse();
        assertThat(new BriefingInput.Edition(null).includeProvenance()).isFalse();
    }
}
