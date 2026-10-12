package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ask.AskCitation;
import ai.foresignal.trigger.AlertView;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link DigestRenderer} against a golden markdown fixture ({@code briefing/digest-golden.md}, prompt
 * 029): the period line in the briefing's timezone, alerts critical first, the activity table with
 * deltas, routine versions apart, notable records with escaped titles and safe links, health,
 * contributor sections, the retention note, and the citations of everything listed.
 */
class DigestRendererIT {

    static final Instant FROM = Instant.parse("2026-10-11T12:00:00Z");
    static final Instant TO = Instant.parse("2026-10-12T12:00:00Z");
    static final ZoneId CHICAGO = ZoneId.of("America/Chicago");

    final DigestRenderer renderer = new DigestRenderer();

    static AlertView alert(long id, String rule, String message, String severity) {
        return new AlertView(id, UUID.fromString("00000000-0000-0000-0000-0000000000a1"), rule, 70 + id, "quakes",
                "q" + id, "earthquake", TO, TO, message, severity, "firing", 1, null, null, null, null, null, null, null,
                null, null, false, null, List.of(), null);
    }

    static DigestStats fixture() {
        DigestStats.Count quakes = new DigestStats.Count("quakes", "earthquake",
                new LinkedHashMap<>(Map.of("FIRST", 3L, "SIGNIFICANT", 1L, "KEYFRAME", 4L)), 3, 1, 0, 0, 4,
                new DigestStats.Entities(3, 1, 0, 0), new DigestStats.Previous(1, 1, 0, null));
        DigestStats.Count vessels = new DigestStats.Count("vessels", "vessel",
                new LinkedHashMap<>(Map.of("FIRST", 1L, "TOMBSTONE", 2L)), 1, 0, 2, 0, 0,
                new DigestStats.Entities(1, 0, 2, 0), null);
        AlertView critical = alert(41, "Big quakes", "M6.2 near *Tokyo*", "critical");
        DigestStats.Alerts alerts = new DigestStats.Alerts(40L, 42L, Map.of("critical", 1L, "warning", 1L),
                Map.of("firing", 2L), List.of(), List.of(new DigestStats.AlertItem(critical, Map.of("id", 41L),
                        "https://fs.example/v1/alerts/41")), 2);
        DigestStats.NotableGroup group = new DigestStats.NotableGroup("earthquake", List.of(
                new DigestStats.NotableItem("quakes", "q1", "earthquake", 77, "M 6.2 - Tokyo [bay]", false,
                        Instant.parse("2026-10-12T03:30:00Z"), Instant.parse("2026-10-12T03:30:00Z"), 6.2, "mag",
                        "https://fs.example/v1/records/quakes/q1"),
                new DigestStats.NotableItem("quakes", "q(2)", "earthquake", 78, null, true,
                        Instant.parse("2026-10-12T04:00:00Z"), Instant.parse("2026-10-12T04:00:00Z"), null, null,
                        "https://fs.example/v1/records/quakes/q(2)")));
        return new DigestStats(5, 99, FROM, TO, true, "fromSeq 5 is below the retention floor 9",
                List.of(quakes, vessels),
                List.of(new DigestStats.Active("quakes", "earthquake", 12, null),
                        new DigestStats.Active("vessels", "vessel", 5, null)),
                alerts, List.of(group), List.of(new DigestStats.HealthItem("vessels", "failing", null, "HTTP 503")), true,
                List.of(new BriefingSection("extra", Map.of("n", 1), "## Extra\n\nContributor text.", null)));
    }

    static String golden() throws IOException {
        try (InputStream in = DigestRendererIT.class.getResourceAsStream("/briefing/digest-golden.md")) {
            assertThat(in).as("golden fixture").isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("the markdown equals the golden fixture byte for byte")
    void goldenMarkdown() throws IOException {
        DigestRenderer.Rendered rendered = renderer.render("Morning brief", fixture(), CHICAGO, FROM, TO);
        assertThat(rendered.markdown()).isEqualTo(golden());
    }

    @Test
    @DisplayName("every listed alert and record is cited with its id, persisted seq and title")
    void citations() {
        List<AskCitation> citations = renderer.render("Morning brief", fixture(), CHICAGO, FROM, TO).citations();
        assertThat(citations).containsExactly(
                new AskCitation("alert:41", null, "Big quakes: M6.2 near *Tokyo*", AskCitation.ALERT),
                new AskCitation("quakes:q1", 77L, "M 6.2 - Tokyo [bay]", AskCitation.RECORD),
                new AskCitation("quakes:q(2)", 78L, "q(2)", AskCitation.RECORD));
    }

    @Test
    @DisplayName("the gap note appears only with gap; an empty digest says so")
    void gapAndEmpty() {
        DigestStats empty = new DigestStats(0, 0, FROM, TO, false, null, List.of(), List.of(), null, List.of(), null,
                true, List.of());
        String md = renderer.render("Empty", empty, ZoneOffset.UTC, FROM, TO).markdown();
        assertThat(md).contains("No alerts in the period.").contains("No activity in the period.")
                .doesNotContain(DigestRenderer.GAP_HEADING).doesNotContain("## Notable").doesNotContain("## Source health");

        DigestStats gap = new DigestStats(0, 0, FROM, TO, true, null, List.of(), List.of(), null, List.of(), List.of(),
                true, List.of());
        String gapMd = renderer.render("Gap", gap, ZoneOffset.UTC, FROM, TO).markdown();
        assertThat(gapMd).contains("## " + DigestRenderer.GAP_HEADING).contains("All sources in scope are healthy.");
    }

    @Test
    @DisplayName("record text cannot add structure: newlines are folded and markdown is escaped")
    void untrustedText() {
        DigestStats.NotableGroup group = new DigestStats.NotableGroup("k", List.of(new DigestStats.NotableItem("s", "e",
                "k", 1, "line\n# heading [x](javascript:alert(1)) <script>", false, TO, TO, null, null,
                "https://fs.example/v1/records/s/e")));
        DigestStats stats = new DigestStats(0, 1, FROM, TO, false, null, List.of(), List.of(), null, List.of(group), null,
                true, List.of());
        String md = renderer.render("T", stats, ZoneOffset.UTC, FROM, TO).markdown();
        assertThat(md).contains("- [line # heading \\[x\\](javascript:alert(1)) <script>](https://fs.example/v1/records/s/e)")
                .doesNotContain("\n# heading");
    }

    @Test
    @DisplayName("numbers and deltas are formatted compactly")
    void formatting() {
        assertThat(DigestRenderer.number(5.0)).isEqualTo("5");
        assertThat(DigestRenderer.number(6.25)).isEqualTo("6.25");
        assertThat(DigestRenderer.number(6.2)).isEqualTo("6.2");
        assertThat(DigestRenderer.signed(3)).isEqualTo("+3");
        assertThat(DigestRenderer.signed(-2)).isEqualTo("-2");
        assertThat(DigestRenderer.signed(0)).isEqualTo("0");
        assertThat(DigestRenderer.url("http://x/a b(c)")).isEqualTo("http://x/a%20b%28c%29");
    }
}
