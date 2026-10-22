package ai.foresignal.social.narrative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.media.outlet.OutletRegistry;
import ai.foresignal.news.NewsReliability;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Outlet reputation lowering a social claim's reliability (prompt 153): factuality caps the track-record grade and a
 * strongly partisan lean or state-affiliated outlet drops it one further step, and the source-key domain resolver.
 */
class SocialReputationTest {

    @Test
    @DisplayName("worse factuality caps the track-record grade")
    void factualityCaps() {
        assertThat(SocialEvidenceGrades.withReputation("D", "F", null, false)).isEqualTo("F");
        assertThat(SocialEvidenceGrades.withReputation("F", "B", null, false)).isEqualTo("F");
    }

    @Test
    @DisplayName("a strongly partisan lean drops the grade one step toward F")
    void partisanDowngrades() {
        assertThat(SocialEvidenceGrades.withReputation("E", null, "right", false)).isEqualTo("F");
        assertThat(SocialEvidenceGrades.withReputation("B", "B", "left", false)).isEqualTo("C");
    }

    @Test
    @DisplayName("a state-affiliated outlet drops the grade one step toward F")
    void stateAffiliationDowngrades() {
        assertThat(SocialEvidenceGrades.withReputation("B", "C", "center", true)).isEqualTo("D");
    }

    @Test
    @DisplayName("a high-factuality, centrist, non-state outlet leaves the grade unchanged")
    void trustedOutletUnchanged() {
        assertThat(SocialEvidenceGrades.withReputation("A", "A", "center", false)).isEqualTo("A");
    }

    @Test
    @DisplayName("only a domain-like source key resolves to an outlet")
    void domainResolution() {
        assertThat(ClaimEnrichment.domainOf("channel:telegram:example.com")).isEqualTo("example.com");
        assertThat(ClaimEnrichment.domainOf("account:x:somehandle")).isNull();
        assertThat(ClaimEnrichment.domainOf(null)).isNull();
    }

    @Test
    @DisplayName("a source with no resolvable outlet leaves reliability and reputation untouched")
    void noOutletNoChange() {
        ObjectProvider<OutletRegistry> outlets = provider(null);
        ObjectProvider<NewsReliability> news = provider(null);
        ClaimEnrichment enrichment = new ClaimEnrichment(outlets, news);
        assertThat(enrichment.reputation("account:x:somehandle")).isNull();
        assertThat(enrichment.adjustedReliability("E", null)).isEqualTo("E");
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> p = mock(ObjectProvider.class);
        when(p.getIfAvailable()).thenReturn(value);
        return p;
    }
}
