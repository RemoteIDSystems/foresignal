package ai.foresignal.doctor;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.doctor.DiagnosisInput.BudgetFacts;
import ai.foresignal.doctor.DiagnosisInput.FetchFacts;
import ai.foresignal.doctor.DiagnosisInput.HealthFacts;
import ai.foresignal.doctor.DiagnosisInput.ProbeFacts;
import ai.foresignal.doctor.DiagnosisInput.Redirect;
import ai.foresignal.store.CoverageSample;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link DiagnosisRules} (prompt 140, task 3): one fixture per finding code, the ordering and verdict, and the
 * {@code UA_BLOCKED} advice never suggesting spoofing.
 */
class DiagnosisRulesTest {

    static final Instant NOW = Instant.parse("2026-10-18T12:00:00Z");
    static final StructureExtractor X = new StructureExtractor();
    static final String FIXTURE = "{\"features\":[{\"properties\":{\"mag\":2.5,\"place\":\"A\"}},"
            + "{\"properties\":{\"mag\":4.1,\"place\":\"B\"}}]}";
    static final ParserLiterals PARSER = ParserLiterals.scan(List.of(
            "class Parser { void p(JsonNode f) { f.path(\"properties\").path(\"mag\"); f.path(\"place\"); } }"));

    /** A builder of inputs: by default a healthy probe whose body equals the fixture. */
    static final class In {
        boolean zeroResultsNormal;
        HealthFacts health = new HealthFacts("healthy", null, 0);
        List<FetchFacts> fetches = new ArrayList<>();
        List<CoverageSample> coverage = new ArrayList<>();
        BudgetFacts budget = new BudgetFacts(4, 1440, 10, 4, false);
        Instant backoffUntil;
        ProbeFacts probe = probe(200, "application/json", FIXTURE, null, 2);
        String body = FIXTURE;
        String contentType = "application/json";
        String fixture = FIXTURE;
        ParserLiterals literals = PARSER;

        In body(int status, String contentType, String body, String parseError, int records) {
            this.body = body;
            this.contentType = contentType;
            this.probe = probe(status, contentType, body, parseError, records);
            return this;
        }

        DiagnosisInput build() {
            Structure probeStructure = probe.skipped() || body == null ? null : X.extract(body, contentType);
            Structure fixtureStructure = fixture == null ? null : X.extract(fixture, null);
            StructureDiff diff = probeStructure == null ? null
                    : StructureDiff.compare(fixtureStructure, probeStructure, "all_day.geojson", literals);
            return new DiagnosisInput("src", zeroResultsNormal, health, fetches, coverage, budget, backoffUntil, NOW,
                    probe, probeStructure, fixtureStructure, diff, literals);
        }

        List<DoctorFinding> diagnose() {
            return DiagnosisRules.diagnose(build());
        }
    }

    static ProbeFacts probe(int status, String contentType, String body, String parseError, int records) {
        return new ProbeFacts(false, null, status, contentType, body == null ? null
                : body.substring(0, Math.min(4096, body.length())), parseError, records, List.of());
    }

    static List<FindingCode> codes(List<DoctorFinding> findings) {
        return findings.stream().map(DoctorFinding::code).toList();
    }

    static DoctorFinding find(List<DoctorFinding> findings, FindingCode code) {
        return findings.stream().filter(f -> f.code() == code).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("NO_CHANGE_DETECTED: the probe matches the fixture and parsed records")
    void noChange() {
        List<DoctorFinding> findings = new In().diagnose();

        assertThat(codes(findings)).containsExactly(FindingCode.NO_CHANGE_DETECTED);
        assertThat(DoctorStatus.of(findings)).isEqualTo(DoctorStatus.OK);
    }

    @Test
    @DisplayName("AUTH_FAILED: 401 without a data body")
    void authFailed() {
        List<DoctorFinding> findings = new In().body(401, "application/json", "{\"error\":\"invalid api key\"}", null, 0)
                .diagnose();

        DoctorFinding f = find(findings, FindingCode.AUTH_FAILED);
        assertThat(f.severity()).isEqualTo(DoctorFinding.Severity.CRITICAL);
        assertThat(f.suggestedAction()).contains("credentials");
        assertThat(codes(findings)).doesNotContain(FindingCode.UA_BLOCKED);
        assertThat(DoctorStatus.of(findings)).isEqualTo(DoctorStatus.BROKEN);
    }

    @Test
    @DisplayName("UA_BLOCKED: 451, and 403 with a block page; the advice never suggests spoofing")
    void uaBlocked() {
        List<DoctorFinding> legal = new In().body(451, "text/plain", "Unavailable", null, 0).diagnose();
        List<DoctorFinding> page = new In().body(403, "text/html",
                "<html><head><title>Access denied</title></head><body>Request blocked by the web application firewall"
                        + "</body></html>", null, 0).diagnose();

        for (List<DoctorFinding> findings : List.of(legal, page)) {
            DoctorFinding f = find(findings, FindingCode.UA_BLOCKED);
            String action = f.suggestedAction().toLowerCase(Locale.ROOT);
            assertThat(action).contains("never spoof").contains("contact the upstream");
            assertThat(action.replace("never spoof", "")).doesNotContain("spoof");
            assertThat(action).doesNotContain("change the user-agent").doesNotContain("browser user-agent");
            assertThat(codes(findings)).doesNotContain(FindingCode.AUTH_FAILED, FindingCode.HTML_INSTEAD_OF_DATA);
        }
        assertThat(DiagnosisRules.UA_BLOCKED_ACTION.toLowerCase(Locale.ROOT)).contains("never spoof");
    }

    @Test
    @DisplayName("ENDPOINT_MOVED: 404, 410, or a permanent redirect to another host")
    void endpointMoved() {
        assertThat(codes(new In().body(404, "text/plain", "Not Found", null, 0).diagnose()))
                .contains(FindingCode.ENDPOINT_MOVED);
        assertThat(codes(new In().body(410, "text/plain", "Gone", null, 0).diagnose()))
                .contains(FindingCode.ENDPOINT_MOVED);

        In moved = new In();
        moved.probe = new ProbeFacts(false, null, 200, "application/json", FIXTURE, null, 2,
                List.of(new Redirect(301, "old.example.org", "https://new.example.net/feed", "new.example.net")));
        DoctorFinding f = find(moved.diagnose(), FindingCode.ENDPOINT_MOVED);
        assertThat(f.evidence().get(0)).contains("old.example.org").contains("new.example.net");

        In sameHost = new In();
        sameHost.probe = new ProbeFacts(false, null, 200, "application/json", FIXTURE, null, 2,
                List.of(new Redirect(301, "a.example.org", "https://a.example.org/v2", "a.example.org")));
        assertThat(codes(sameHost.diagnose())).doesNotContain(FindingCode.ENDPOINT_MOVED);
    }

    @Test
    @DisplayName("RATE_LIMITED: 429 or a backoff in force, with budget advice from descriptor versus usage")
    void rateLimited() {
        In in = new In().body(429, "text/plain", "Too Many Requests", null, 0);
        in.budget = new BudgetFacts(4, 100, 90, 20, false);
        DoctorFinding f = find(in.diagnose(), FindingCode.RATE_LIMITED);
        assertThat(f.severity()).isEqualTo(DoctorFinding.Severity.WARNING);
        assertThat(f.evidence()).anyMatch(e -> e.contains("4/min, 100/day"));
        assertThat(f.suggestedAction()).contains("480 requests a day");

        In backoff = new In();
        backoff.backoffUntil = NOW.plusSeconds(600);
        assertThat(codes(backoff.diagnose())).contains(FindingCode.RATE_LIMITED);
    }

    @Test
    @DisplayName("UPSTREAM_ERROR: a 5xx probe or a streak of 5xx fetches")
    void upstreamError() {
        assertThat(codes(new In().body(503, "text/plain", "Service Unavailable", null, 0).diagnose()))
                .contains(FindingCode.UPSTREAM_ERROR);

        In streak = new In();
        for (int i = 0; i < 3; i++) {
            streak.fetches.add(new FetchFacts("FAILED", 502, "UpstreamHttpException: 502", 0, NOW.minusSeconds(60L * i)));
        }
        assertThat(codes(streak.diagnose())).contains(FindingCode.UPSTREAM_ERROR);
    }

    @Test
    @DisplayName("HTML_INSTEAD_OF_DATA: a maintenance page where JSON was expected; never solves captchas")
    void htmlInsteadOfData() {
        List<DoctorFinding> findings = new In().body(200, "text/html", "<!doctype html><html><head><title>Down for"
                + " maintenance</title></head><body>We'll be back</body></html>", null, 0).diagnose();

        DoctorFinding f = find(findings, FindingCode.HTML_INSTEAD_OF_DATA);
        assertThat(f.evidence().get(0)).contains("maintenance page").contains("Down for maintenance");
        assertThat(f.suggestedAction()).contains("never solves captchas");
        assertThat(codes(findings)).doesNotContain(FindingCode.EMPTY_RESULT, FindingCode.FIELD_REMOVED);
    }

    @Test
    @DisplayName("EMPTY_RESULT: intact structure, zero items; info when zero results are normal")
    void emptyResult() {
        In in = new In().body(200, "application/json", "{\"features\":[]}", null, 0);
        DoctorFinding f = find(in.diagnose(), FindingCode.EMPTY_RESULT);
        assertThat(f.severity()).isEqualTo(DoctorFinding.Severity.WARNING);
        assertThat(f.suggestedAction()).contains("zeroResultsNormal");

        In normal = new In().body(200, "application/json", "{\"features\":[]}", null, 0);
        normal.zeroResultsNormal = true;
        assertThat(find(normal.diagnose(), FindingCode.EMPTY_RESULT).severity())
                .isEqualTo(DoctorFinding.Severity.INFO);
    }

    @Test
    @DisplayName("FIELD_RENAMED: mag → magnitude, read by the parser, high score, marked heuristic")
    void fieldRenamed() {
        String renamed = FIXTURE.replace("\"mag\"", "\"magnitude\"");
        List<DoctorFinding> findings = new In().body(200, "application/json", renamed,
                "UpstreamFormatException: feature without mag", 0).diagnose();

        DoctorFinding f = find(findings, FindingCode.FIELD_RENAMED);
        assertThat(f.path()).isEqualTo("features[].properties.mag");
        assertThat(f.heuristic()).isTrue();
        assertThat(f.evidence()).anyMatch(e -> e.startsWith("heuristic"));
        assertThat(f.suggestedAction()).contains("'magnitude'");
        assertThat(codes(findings)).doesNotContain(FindingCode.FIELD_REMOVED);
    }

    @Test
    @DisplayName("FIELD_REMOVED: a field the parser reads is gone with no rename partner")
    void fieldRemoved() {
        String removed = FIXTURE.replace("\"mag\":2.5,", "").replace("\"mag\":4.1,", "");
        DoctorFinding f = find(new In().body(200, "application/json", removed, null, 2).diagnose(),
                FindingCode.FIELD_REMOVED);

        assertThat(f.path()).isEqualTo("features[].properties.mag");
        assertThat(f.heuristic()).isTrue();
    }

    @Test
    @DisplayName("a removed field the parser does not read is not a finding")
    void unreadFieldRemoved() {
        In in = new In().body(200, "application/json", FIXTURE.replace("\"place\":\"A\"", "\"x\":1")
                .replace("\"place\":\"B\"", "\"x\":2"), null, 2);
        in.literals = ParserLiterals.scan(List.of("class P { String k = \"mag\"; }"));
        assertThat(codes(in.diagnose())).doesNotContain(FindingCode.FIELD_REMOVED);
    }

    @Test
    @DisplayName("TYPE_CHANGED: mag became a string; critical because the parser reads it")
    void typeChanged() {
        String retyped = FIXTURE.replace("2.5", "\"2.5\"").replace("4.1", "\"4.1\"");
        DoctorFinding f = find(new In().body(200, "application/json", retyped, null, 2).diagnose(),
                FindingCode.TYPE_CHANGED);

        assertThat(f.path()).isEqualTo("features[].properties.mag");
        assertThat(f.severity()).isEqualTo(DoctorFinding.Severity.CRITICAL);
        assertThat(f.evidence().get(0)).contains("number").contains("string");
    }

    @Test
    @DisplayName("FORMAT_CHANGED: JSON became XML; a CSV delimiter change")
    void formatChanged() {
        DoctorFinding xml = find(new In().body(200, "application/xml", "<features><f mag=\"2.5\"/></features>", null,
                0).diagnose(), FindingCode.FORMAT_CHANGED);
        assertThat(xml.evidence()).anyMatch(e -> e.contains("json → xml"));

        In csv = new In().body(200, "text/csv", "a;b\n1;2\n3;4\n", null, 2);
        csv.fixture = "a,b\n1,2\n";
        assertThat(find(csv.diagnose(), FindingCode.FORMAT_CHANGED).evidence()).anyMatch(e -> e.contains("delimiter"));
    }

    @Test
    @DisplayName("PARSER_REJECTS: the probe's UpstreamFormatException with its snippet")
    void parserRejects() {
        DoctorFinding f = find(new In().body(200, "application/json", FIXTURE,
                "UpstreamFormatException: expected a FeatureCollection | content: {\"error\":\"x\"}", 0).diagnose(),
                FindingCode.PARSER_REJECTS);

        assertThat(f.evidence().get(0)).contains("content: {\"error\":\"x\"}");
    }

    @Test
    @DisplayName("COVERAGE_DROPPED: the newest coverage at half the median or below")
    void coverageDropped() {
        In in = new In();
        for (int i = 0; i < 5; i++) {
            in.coverage.add(new CoverageSample(100, 100, 90, null));
        }
        in.coverage.add(new CoverageSample(30, 100, 20, null));
        DoctorFinding f = find(in.diagnose(), FindingCode.COVERAGE_DROPPED);
        assertThat(f.evidence().get(0)).startsWith("coverage dropped");
    }

    @Test
    @DisplayName("PROBE_SKIPPED_BUDGET: diagnosed from the history only (a 401 in the last failed fetch)")
    void probeSkipped() {
        In in = new In();
        in.probe = ProbeFacts.skipped("budget");
        in.fetches.add(new FetchFacts("FAILED", 401, "UpstreamAccessDeniedException: 401", 0, NOW));

        List<DoctorFinding> findings = in.diagnose();

        assertThat(codes(findings)).contains(FindingCode.PROBE_SKIPPED_BUDGET, FindingCode.AUTH_FAILED);
        assertThat(find(findings, FindingCode.AUTH_FAILED).evidence().get(0)).contains("last failed fetch");

        In quiet = new In();
        quiet.probe = ProbeFacts.skipped("backoff");
        assertThat(codes(quiet.diagnose())).contains(FindingCode.PROBE_SKIPPED_BUDGET)
                .doesNotContain(FindingCode.NO_CHANGE_DETECTED);
    }

    @Test
    @DisplayName("findings come out worst first")
    void ordering() {
        In in = new In().body(200, "application/json", "{\"features\":[]}", null, 0);
        in.coverage.addAll(List.of(new CoverageSample(100, null, null, null), new CoverageSample(100, null, null, null),
                new CoverageSample(100, null, null, null), new CoverageSample(10, null, null, null)));
        in.probe = new ProbeFacts(false, null, 200, "application/json", "{\"features\":[]}",
                "UpstreamFormatException: empty", 0, List.of());

        List<DoctorFinding> findings = in.diagnose();

        assertThat(findings.get(0).severity()).isEqualTo(DoctorFinding.Severity.CRITICAL);
        assertThat(findings.get(findings.size() - 1).severity().ordinal())
                .isLessThanOrEqualTo(findings.get(0).severity().ordinal());
    }
}
