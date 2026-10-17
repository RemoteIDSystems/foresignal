package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The deterministic packet (prompt 128 §2): every section from seeded inputs, scope filtering, the first-run
 * lookback, ranking, the 30-per-section LLM cap with the complete packet stored, private items kept out of the LLM
 * view, the review-due cadence rule and the collection gaps.
 */
class CyclePacketBuilderTest {

    static final Instant NOW = Instant.parse("2026-10-17T06:00:00Z");
    static final Instant SINCE = NOW.minus(Duration.ofDays(1));

    static CyclePacketItem item(String ref, String change, double score, List<String> regions, String situationId,
            String setId, String setCadence, String category, boolean priv) {
        return new CyclePacketItem(ref, "Title of " + ref, change, "warning", NOW.minusSeconds((long) (score * 60)),
                score, regions, null, situationId, setId, setCadence, List.of(), category, priv, Map.of("k", "v"));
    }

    static CyclePacketBuilder.Inputs inputs(Map<String, List<CyclePacketItem>> sections, boolean firstRun) {
        Map<String, Object> gap = new LinkedHashMap<>();
        gap.put("id", "01JA8Q3V7S2K9W4T6Y1B5N0M3G");
        gap.put("setId", "SET-YE");
        gap.put("indicatorId", "01JA8Q3V7S2K9W4T6Y1B5N0M3H");
        gap.put("potentialEig", 0.4);
        gap.put("actualEig", 0.1);
        gap.put("recommendations", List.of());
        gap.put("regionCodes", List.of("YE"));
        gap.put("setCadence", "daily");
        gap.put("private", false);
        return new CyclePacketBuilder.Inputs(SINCE, NOW, firstRun, sections, List.of("acled"), List.of("gdelt"),
                List.of("ER"), List.of(gap), List.of(Map.of("text", "Earlier judgment", "citations", List.of(),
                        "textOrigin", "llm")));
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> section(Map<String, Object> packet, String key) {
        int dot = key.indexOf('.');
        if (dot < 0) {
            return (List<Map<String, Object>>) packet.get(key);
        }
        return (List<Map<String, Object>>) ((Map<String, Object>) packet.get(key.substring(0, dot)))
                .get(key.substring(dot + 1));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("every section is filled from the seeded inputs, with the coverage, gaps and previous judgments")
    void everySection() {
        Map<String, List<CyclePacketItem>> sections = new LinkedHashMap<>();
        for (String s : CyclePacketBuilder.SECTIONS) {
            sections.put(s, List.of(item("finding:" + s.replace('.', '-'), "changed", 1, List.of("YE"), null, null,
                    null, null, false)));
        }
        CyclePacketBuilder.Packet p = CyclePacketBuilder.build(inputs(sections, false), CycleScope.ALL, "daily", false);
        for (String s : CyclePacketBuilder.SECTIONS) {
            assertThat(section(p.stored(), s)).as(s).hasSize(1);
            assertThat(section(p.llm(), s)).as(s).hasSize(1);
            assertThat(section(p.stored(), s).get(0)).containsKeys("ref", "title", "change", "severity", "at",
                    "detail", "rank");
        }
        assertThat(p.stored()).containsEntry("version", "cycle-packet 1.0.0").containsEntry("firstRun", false)
                .containsEntry("since", SINCE.toString()).containsEntry("until", NOW.toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> coverage = (Map<String, Object>) p.stored().get("coverage");
        assertThat(coverage).containsEntry("failingSources", List.of("acled"))
                .containsEntry("degradedSources", List.of("gdelt"))
                .containsEntry("lowReportingDensityRegions", List.of("ER"));
        assertThat((List<?>) p.stored().get("collectionGaps")).singleElement().satisfies(g ->
                assertThat((Map<String, Object>) g).containsKeys("id", "setId", "potentialEig").doesNotContainKey("setCadence"));
        assertThat((List<?>) p.stored().get("previousJudgments")).hasSize(1);
        assertThat(p.refs()).hasSize(CyclePacketBuilder.SECTIONS.size());
        assertThat(p.size()).isEqualTo(CyclePacketBuilder.SECTIONS.size());
    }

    @Test
    @DisplayName("the first run says so")
    void firstRun() {
        CyclePacketBuilder.Packet p = CyclePacketBuilder.build(inputs(Map.of(), true), CycleScope.ALL, "weekly", false);
        assertThat(p.stored()).containsEntry("firstRun", true).containsEntry("cadence", "weekly");
        assertThat(section(p.stored(), "findings")).isEmpty();
    }

    @Test
    @DisplayName("the scope filters by region, situation, set, cadence-match and category")
    void scope() {
        List<CyclePacketItem> findings = List.of(
                item("finding:A", "x", 1, List.of("YE"), null, null, null, null, false),
                item("finding:B", "x", 1, List.of("US-TX"), null, null, null, null, false),
                item("finding:C", "x", 1, List.of(), null, null, null, null, false));
        List<CyclePacketItem> revisions = List.of(
                item("hypothesis-set:D", "revised", 1, List.of(), null, "SET-D", "daily", null, false),
                item("hypothesis-set:W", "revised", 1, List.of(), null, "SET-W", "weekly", null, false));
        List<CyclePacketItem> due = List.of(
                item("question:Q1", "due", 1, List.of(), null, null, null, "conflict", false),
                item("question:Q2", "due", 1, List.of(), null, null, null, "markets", false));
        CycleScope scope = new CycleScope(List.of("YE"), List.of(), List.of(), List.of(), true, List.of(),
                List.of("conflict"), false);
        CyclePacketBuilder.Packet p = CyclePacketBuilder.build(inputs(Map.of("findings", findings,
                "hypotheses.revisions", revisions, "forecasts.due", due), false), scope, "daily", false);
        assertThat(section(p.stored(), "findings")).extracting(m -> m.get("ref"))
                .containsExactlyInAnyOrder("finding:A", "finding:C");
        assertThat(section(p.stored(), "hypotheses.revisions")).extracting(m -> m.get("ref"))
                .containsExactly("hypothesis-set:D");
        assertThat(section(p.stored(), "forecasts.due")).extracting(m -> m.get("ref")).containsExactly("question:Q1");

        CycleScope focused = CycleScope.focusedOn("SIT-1", List.of("SET-W"));
        CyclePacketBuilder.Packet f = CyclePacketBuilder.build(inputs(Map.of("hypotheses.revisions", revisions,
                "findings", List.of(item("finding:S", "x", 1, List.of(), "SIT-1", null, null, null, false),
                        item("finding:T", "x", 1, List.of(), "SIT-2", null, null, null, false))), false), focused,
                "on-demand", false);
        assertThat(section(f.stored(), "hypotheses.revisions")).extracting(m -> m.get("ref"))
                .containsExactly("hypothesis-set:W");
        assertThat(section(f.stored(), "findings")).extracting(m -> m.get("ref")).containsExactly("finding:S");
    }

    @Test
    @DisplayName("sections are ranked by score, then recency; the LLM sees 30 per section and the stored packet all")
    void capsAndRanking() {
        List<CyclePacketItem> many = new ArrayList<>();
        for (int i = 0; i < 35; i++) {
            many.add(item("finding:F" + i, "x", i, List.of(), null, null, null, null, false));
        }
        CyclePacketBuilder.Packet p = CyclePacketBuilder.build(inputs(Map.of("findings", many), false),
                CycleScope.ALL, "daily", false);
        assertThat(section(p.stored(), "findings")).hasSize(35);
        assertThat(section(p.llm(), "findings")).hasSize(CycleVocabulary.LLM_SECTION_CAP);
        assertThat(section(p.stored(), "findings").get(0)).containsEntry("ref", "finding:F34").containsEntry("rank", 1);
        assertThat(section(p.llm(), "findings").get(29)).containsEntry("ref", "finding:F5");
        assertThat(p.stored().get("truncatedForLlm")).isEqualTo(Map.of("findings", 5));
        assertThat(p.llm().get("truncatedForLlm")).isEqualTo(Map.of("findings", 5));
    }

    @Test
    @DisplayName("private items are stored and mark the packet private, but reach the LLM only when allowed")
    void privateItems() {
        List<CyclePacketItem> findings = List.of(item("finding:P", "x", 2, List.of(), null, null, null, null, true),
                item("finding:Q", "x", 1, List.of(), null, null, null, null, false));
        CyclePacketBuilder.Packet closed = CyclePacketBuilder.build(inputs(Map.of("findings", findings), false),
                CycleScope.ALL, "daily", false);
        assertThat(closed.privateInput()).isTrue();
        assertThat(section(closed.stored(), "findings")).hasSize(2);
        assertThat(section(closed.llm(), "findings")).extracting(m -> m.get("ref")).containsExactly("finding:Q");
        CyclePacketBuilder.Packet open = CyclePacketBuilder.build(inputs(Map.of("findings", findings), false),
                CycleScope.ALL, "daily", true);
        assertThat(section(open.llm(), "findings")).hasSize(2);
    }

    @Test
    @DisplayName("a daily cycle reviews daily sets, a weekly one daily and weekly sets, a monthly one all")
    void reviewDue() {
        List<CyclePacketItem> due = List.of(
                item("hypothesis-set:D", "due-for-review", 1, List.of(), null, "D", "daily", null, false),
                item("hypothesis-set:W", "due-for-review", 1, List.of(), null, "W", "weekly", null, false),
                item("hypothesis-set:M", "due-for-review", 1, List.of(), null, "M", "monthly", null, false));
        assertThat(section(CyclePacketBuilder.build(inputs(Map.of("hypotheses.dueForReview", due), false),
                CycleScope.ALL, "daily", false).stored(), "hypotheses.dueForReview")).hasSize(1);
        assertThat(section(CyclePacketBuilder.build(inputs(Map.of("hypotheses.dueForReview", due), false),
                CycleScope.ALL, "weekly", false).stored(), "hypotheses.dueForReview")).hasSize(2);
        assertThat(section(CyclePacketBuilder.build(inputs(Map.of("hypotheses.dueForReview", due), false),
                CycleScope.ALL, "monthly", false).stored(), "hypotheses.dueForReview")).hasSize(3);
        assertThat(CyclePacketBuilder.reviewDue("weekly", "on-demand")).isTrue();
    }

    @Test
    @DisplayName("collection gaps follow the scope; duplicates of one change are dropped")
    void gapsAndDuplicates() {
        CycleScope us = new CycleScope(List.of("US"), List.of(), List.of(), List.of(), false, List.of(), List.of(),
                false);
        CyclePacketItem a = item("finding:A", "x", 1, List.of(), null, null, null, null, false);
        CyclePacketBuilder.Packet p = CyclePacketBuilder.build(inputs(Map.of("findings", List.of(a, a)), false), us,
                "daily", false);
        assertThat((List<?>) p.stored().get("collectionGaps")).isEmpty();
        assertThat(section(p.stored(), "findings")).hasSize(1);
    }
}
