package ai.foresignal.doctor;

import ai.foresignal.collect.HealthEvaluator;
import ai.foresignal.doctor.DoctorFinding.Severity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The deterministic diagnosis (prompt 140, task 3): rules over health, the fetch history, budget and backoff, the
 * probe and the structure diff, each producing a {@link DoctorFinding} with evidence and a suggested action. Pure and
 * stateless; no LLM is involved.
 *
 * <p>Findings come out worst first (critical, warning, info), in rule order within a severity. When nothing else is
 * found and the probe ran, {@link FindingCode#NO_CHANGE_DETECTED} is the only finding.
 */
public final class DiagnosisRules {

    /** The consecutive 5xx responses in the fetch history that make an {@code UPSTREAM_ERROR} streak. */
    public static final int ERROR_STREAK = 3;

    /** The UA-blocked advice; it never suggests changing or spoofing the User-Agent. */
    public static final String UA_BLOCKED_ACTION = "Contact the upstream or join its key or feeder program; never spoof"
            + " the User-Agent. Foresignal identifies itself honestly, so a block is a policy question for the"
            + " upstream, not something to work around.";

    private static final Pattern BLOCK_PAGE = Pattern.compile(
            "access denied|request blocked|you have been blocked|blocked by|forbidden by|bot (?:protection|detection)"
                    + "|automated (?:requests|access|queries)|unusual traffic|user-agent|ddos protection"
                    + "|attention required|cloudflare|akamai|incapsula|web application firewall|not permitted to access",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern LOGIN = Pattern.compile("log ?in|sign ?in|password|authenticat|session expired",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern MAINTENANCE = Pattern.compile(
            "maintenance|temporarily unavailable|be right back|under construction|service unavailable|down for",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CAPTCHA = Pattern.compile("captcha|are you a robot|verify you are human|"
            + "checking your browser|challenge", Pattern.CASE_INSENSITIVE);

    private DiagnosisRules() {}

    /** Runs every rule over {@code in}. */
    public static List<DoctorFinding> diagnose(DiagnosisInput in) {
        List<DoctorFinding> out = new ArrayList<>();
        DiagnosisInput.ProbeFacts probe = in.probe();
        Integer status = effectiveStatus(in);
        String origin = probe.skipped() ? "the last failed fetch" : "the probe";

        if (probe.skipped() && ("budget".equals(probe.skipReason()) || "backoff".equals(probe.skipReason()))) {
            out.add(DoctorFinding.of(FindingCode.PROBE_SKIPPED_BUDGET, Severity.INFO,
                    List.of("probe skipped: " + ("budget".equals(probe.skipReason())
                            ? "the request budget is spent" : "a backoff is in force"
                                    + (in.backoffUntil() == null ? "" : " until " + in.backoffUntil())),
                            "diagnosed from health and the fetch history only"),
                    "Wait for the budget to refill or the backoff to end, then run the doctor again with a probe."));
        }

        boolean blockPage = probe.bodyHead() != null && BLOCK_PAGE.matcher(probe.bodyHead()).find();
        boolean dataBody = hasDataBody(in);

        // AUTH_FAILED / UA_BLOCKED
        if (status != null && status == 451) {
            out.add(DoctorFinding.of(FindingCode.UA_BLOCKED, Severity.CRITICAL,
                    List.of(origin + " answered HTTP 451 (unavailable for legal reasons or by policy)"),
                    UA_BLOCKED_ACTION));
        } else if (status != null && status == 403 && blockPage && !dataBody) {
            out.add(DoctorFinding.of(FindingCode.UA_BLOCKED, Severity.CRITICAL,
                    List.of(origin + " answered HTTP 403 with a block page"
                            + titleEvidence(in)), UA_BLOCKED_ACTION));
        } else if (status != null && (status == 401 || status == 403) && !dataBody) {
            out.add(DoctorFinding.of(FindingCode.AUTH_FAILED, Severity.CRITICAL,
                    List.of(origin + " answered HTTP " + status + " without a data body"),
                    "Check the source's credentials (PUT /v1/sources/" + in.sourceId() + "/credentials) and whether"
                            + " the key has expired, been revoked or lost a required permission."));
        }

        // ENDPOINT_MOVED
        if (status != null && (status == 404 || status == 410)) {
            out.add(DoctorFinding.of(FindingCode.ENDPOINT_MOVED, Severity.CRITICAL,
                    List.of(origin + " answered HTTP " + status + (status == 410 ? " (gone)" : " (not found)")),
                    "The endpoint moved or was retired: find the current URL in the upstream's documentation or"
                            + " changelog, update the adapter, then refresh the fixture and contract test."));
        } else {
            for (DiagnosisInput.Redirect r : probe.redirects()) {
                if ((r.status() == 301 || r.status() == 308) && r.toHost() != null
                        && !r.toHost().equalsIgnoreCase(r.fromHost())) {
                    out.add(DoctorFinding.of(FindingCode.ENDPOINT_MOVED, Severity.CRITICAL,
                            List.of("permanent redirect " + r.status() + " from " + r.fromHost() + " to " + r.toHost()
                                    + " (" + r.location() + ")"),
                            "The upstream moved to another host: confirm the new host is the official one, then"
                                    + " update the adapter's base URL and refresh the fixture."));
                    break;
                }
            }
        }

        // RATE_LIMITED
        boolean backoff = in.backoffUntil() != null && in.now() != null && in.backoffUntil().isAfter(in.now());
        boolean history429 = in.recentFetches().stream().limit(5)
                .anyMatch(f -> f.httpStatus() != null && f.httpStatus() == 429);
        if ((status != null && status == 429) || backoff || history429) {
            List<String> evidence = new ArrayList<>();
            if (status != null && status == 429) {
                evidence.add(origin + " answered HTTP 429 (too many requests)");
            } else if (history429) {
                evidence.add("a recent fetch answered HTTP 429");
            }
            if (backoff) {
                evidence.add("backoff in force until " + in.backoffUntil());
            }
            String action = "Space requests out: raise the polling interval or lower the budget.";
            if (in.budget() != null) {
                evidence.add("budget " + in.budget().perMinute() + "/min, " + in.budget().perDay() + "/day; observed "
                        + in.budget().usedToday() + " requests today and " + in.budget().requestsLastHour()
                        + " in the last hour");
                long projected = in.budget().requestsLastHour() * 24;
                if (in.budget().perDay() > 0 && projected > in.budget().perDay()) {
                    action += " The last hour's rate projects " + projected + " requests a day, over the "
                            + in.budget().perDay() + "/day budget.";
                } else {
                    action += " Observed usage is within the budget, so the upstream's own limit is lower than the"
                            + " descriptor's: lower perMinute and perDay to match the upstream's documented limit.";
                }
            }
            out.add(DoctorFinding.of(FindingCode.RATE_LIMITED, Severity.WARNING, evidence, action));
        }

        // UPSTREAM_ERROR
        int streak = 0;
        for (DiagnosisInput.FetchFacts f : in.recentFetches()) {
            if (f.httpStatus() != null && f.httpStatus() >= 500) {
                streak++;
            } else {
                break;
            }
        }
        boolean probe5xx = !probe.skipped() && probe.httpStatus() != null && probe.httpStatus() >= 500;
        if (probe5xx || streak >= ERROR_STREAK) {
            List<String> evidence = new ArrayList<>();
            if (probe5xx) {
                evidence.add("the probe answered HTTP " + probe.httpStatus());
            }
            if (streak > 0) {
                evidence.add(streak + " consecutive fetches answered 5xx");
            }
            out.add(DoctorFinding.of(FindingCode.UPSTREAM_ERROR, streak >= 2 * ERROR_STREAK ? Severity.CRITICAL
                    : Severity.WARNING, evidence,
                    "The upstream is failing on its side. Nothing in the adapter needs to change yet: check the"
                            + " upstream's status page and wait; the collector backs off on its own."));
        }

        // HTML_INSTEAD_OF_DATA
        StructureDiff diff = in.diff();
        boolean htmlInstead = in.probeStructure() != null && in.probeStructure().format() == StructureFormat.HTML
                && (in.fixtureStructure() == null || in.fixtureStructure().format() != StructureFormat.HTML);
        if (htmlInstead && !out.stream().anyMatch(f -> f.code() == FindingCode.UA_BLOCKED)) {
            String head = probe.bodyHead() == null ? "" : probe.bodyHead();
            String kind = CAPTCHA.matcher(head).find() ? "a captcha or browser challenge"
                    : LOGIN.matcher(head).find() ? "a login wall"
                    : MAINTENANCE.matcher(head).find() ? "a maintenance page" : "an HTML page";
            List<String> evidence = new ArrayList<>();
            evidence.add("the probe returned " + kind + " where "
                    + (in.fixtureStructure() == null ? "data" : in.fixtureStructure().format().wireValue())
                    + " was expected" + titleEvidence(in));
            out.add(DoctorFinding.of(FindingCode.HTML_INSTEAD_OF_DATA, Severity.CRITICAL, evidence,
                    "Open the endpoint in a browser to see the page. Wait out a maintenance page; for a login wall,"
                            + " check the credentials or move to the upstream's official API. Foresignal never solves"
                            + " captchas or browser challenges."));
        }

        // PARSER_REJECTS
        if (!probe.skipped() && probe.parseError() != null && probe.parseError().contains("UpstreamFormatException")) {
            out.add(DoctorFinding.of(FindingCode.PARSER_REJECTS, Severity.CRITICAL,
                    List.of("the parser threw on the probe body: " + StructureExtractor.cap(probe.parseError(), 700)),
                    "The snippet usually names the change. Compare the probe with the fixture, adjust the parser,"
                            + " then refresh the fixture and its contract test."));
        }

        // FIELD_RENAMED / FIELD_REMOVED / TYPE_CHANGED / FORMAT_CHANGED
        Set<String> explained = new HashSet<>();
        if (diff != null && !htmlInstead) {
            for (StructureDiff.RenameCandidate c : diff.renameCandidates()) {
                if (c.score() >= 0.7 && c.readByParser()) {
                    explained.add(c.from());
                    out.add(new DoctorFinding(FindingCode.FIELD_RENAMED, Severity.CRITICAL,
                            List.of("'" + c.from() + "' is gone and '" + c.to() + "' appeared under the same parent ("
                                    + c.reason() + ", score " + c.score() + ")", ParserLiterals.HEURISTIC_NOTE),
                            c.from(), "Update the parser to read '" + StructureDiff.leafName(c.to()) + "' instead of '"
                                    + StructureDiff.leafName(c.from()) + "', refresh the fixture and re-run the"
                                    + " contract test.", true));
                }
            }
            for (String removed : diff.removedPaths()) {
                if (explained.contains(removed) || !in.literals().test(removed)) {
                    continue;
                }
                out.add(new DoctorFinding(FindingCode.FIELD_REMOVED, Severity.CRITICAL,
                        List.of("'" + removed + "' is in the fixture but not in the probe",
                                ParserLiterals.HEURISTIC_NOTE),
                        removed, "Check the upstream's changelog for the field's replacement; make the parser"
                                + " tolerate its absence or read the replacement, then refresh the fixture.", true));
            }
            for (StructureDiff.TypeChange t : diff.typeChanged()) {
                boolean read = in.literals().test(t.path());
                List<String> evidence = new ArrayList<>();
                evidence.add("'" + t.path() + "' was " + t.from() + " and is now " + t.to());
                if (read) {
                    evidence.add(ParserLiterals.HEURISTIC_NOTE);
                }
                out.add(new DoctorFinding(FindingCode.TYPE_CHANGED, read ? Severity.CRITICAL : Severity.WARNING,
                        evidence, t.path(), "Make the parser accept the new type (for example a number now sent as"
                                + " a string), then refresh the fixture.", read));
            }
            List<String> format = new ArrayList<>();
            if (diff.contentTypeChanged() != null) {
                format.add("content type " + diff.contentTypeChanged().from() + " → " + diff.contentTypeChanged().to());
            }
            if (diff.delimiterChanged() != null) {
                format.add("delimiter '" + printable(diff.delimiterChanged().from()) + "' → '"
                        + printable(diff.delimiterChanged().to()) + "'");
            }
            if (in.fixtureStructure() != null && in.probeStructure() != null
                    && in.fixtureStructure().format() != in.probeStructure().format()
                    && in.probeStructure().format() != StructureFormat.HTML
                    && !(isDelimited(in.fixtureStructure().format()) && isDelimited(in.probeStructure().format()))) {
                format.add("format " + in.fixtureStructure().format().wireValue() + " → "
                        + in.probeStructure().format().wireValue());
            }
            if (diff.columnsReordered()) {
                format.add("columns reordered");
            }
            if (!format.isEmpty()) {
                out.add(DoctorFinding.of(FindingCode.FORMAT_CHANGED, diff.columnsReordered() && format.size() == 1
                        ? Severity.WARNING : Severity.CRITICAL, format,
                        "The upstream changed how it serves the data. Check whether the old format is still offered"
                                + " (an Accept header, a format parameter); otherwise adapt the parser and refresh"
                                + " the fixture."));
            }
        }

        // EMPTY_RESULT
        if (!probe.skipped() && !htmlInstead && probe.parseError() == null && statusIsSuccess(probe.httpStatus())) {
            boolean structureIntact = diff == null || diff.removedPaths().isEmpty();
            boolean zeroItems = probe.records() == 0 || (diff != null && !diff.arrayEmpty().isEmpty());
            if (structureIntact && zeroItems) {
                List<String> evidence = new ArrayList<>();
                evidence.add("the probe parsed " + probe.records() + " records");
                if (diff != null && !diff.arrayEmpty().isEmpty()) {
                    evidence.add("empty where the fixture had items: " + String.join(", ", diff.arrayEmpty()));
                }
                if (in.zeroResultsNormal()) {
                    evidence.add("the descriptor declares zero results normal");
                }
                out.add(DoctorFinding.of(FindingCode.EMPTY_RESULT,
                        in.zeroResultsNormal() ? Severity.INFO : Severity.WARNING, evidence,
                        in.zeroResultsNormal() ? "Zero results are normal for this source; nothing to do unless it"
                                + " stays empty for longer than usual."
                                : "The structure is intact but empty. Check the source's params (area, filters, time"
                                        + " window) and the upstream's own status; if empty is a legitimate state,"
                                        + " declare zeroResultsNormal on the descriptor."));
            }
        }

        // COVERAGE_DROPPED
        Optional<String> coverage = HealthEvaluator.coverageDrop(in.coverage());
        if (coverage.isEmpty() && in.health().reason() != null && in.health().reason().startsWith("coverage dropped")) {
            coverage = Optional.of(in.health().reason());
        }
        coverage.ifPresent(reason -> out.add(DoctorFinding.of(FindingCode.COVERAGE_DROPPED, Severity.WARNING,
                List.of(reason), "Coverage fell by half or more against the median of the recent reports: check the"
                        + " receivers or stations behind the source and its area params before suspecting the"
                        + " parser.")));

        boolean onlySkip = out.stream().allMatch(f -> f.code() == FindingCode.PROBE_SKIPPED_BUDGET);
        if (out.isEmpty() || (onlySkip && !probe.skipped())) {
            List<String> evidence = new ArrayList<>();
            if (probe.skipped()) {
                evidence.add("no probe was sent (" + probe.skipReason() + "); health and the fetch history show no"
                        + " problem the rules recognize");
            } else {
                evidence.add(diff == null || in.fixtureStructure() == null
                        ? "no fixture to compare with; the probe answered "
                                + (probe.httpStatus() == null ? "without a status" : "HTTP " + probe.httpStatus())
                        : "the probe's structure matches the fixture" + (diff.fixtureFile() == null ? ""
                                : " '" + diff.fixtureFile() + "'"));
                evidence.add("the adapter parsed " + probe.records() + " records");
            }
            out.add(DoctorFinding.of(FindingCode.NO_CHANGE_DETECTED, Severity.INFO, evidence,
                    "Nothing changed upstream that the doctor can see. If health still reports a problem, read the"
                            + " fetch history (GET /v1/sources/" + in.sourceId() + "/fetches) and the budget."));
        }
        out.sort(Comparator.comparingInt((DoctorFinding f) -> -f.severity().ordinal()));
        return List.copyOf(out);
    }

    /** The probe's status when it ran, else the HTTP status of the newest failed fetch, else null. */
    static Integer effectiveStatus(DiagnosisInput in) {
        if (!in.probe().skipped()) {
            return in.probe().httpStatus();
        }
        for (DiagnosisInput.FetchFacts f : in.recentFetches()) {
            if ("FAILED".equals(f.status()) && f.httpStatus() != null) {
                return f.httpStatus();
            }
            if ("OK".equals(f.status()) || "NOT_MODIFIED".equals(f.status())) {
                return null;
            }
        }
        return null;
    }

    /** True when the probe body is structured data (JSON with more than a handful of paths, CSV rows, XML …). */
    static boolean hasDataBody(DiagnosisInput in) {
        Structure s = in.probeStructure();
        if (s == null || !s.format().isData() || s.error() != null) {
            return false;
        }
        if (s.format() == StructureFormat.JSON) {
            long errorish = s.paths().keySet().stream().map(p -> p.toLowerCase(Locale.ROOT))
                    .filter(p -> p.contains("error") || p.contains("message") || p.contains("detail")
                            || p.contains("status") || p.contains("code"))
                    .count();
            return s.paths().size() > 5 && errorish < s.paths().size();
        }
        return s.bytes() > 0;
    }

    private static boolean statusIsSuccess(Integer status) {
        return status == null || (status >= 200 && status < 300) || status == 304;
    }

    private static boolean isDelimited(StructureFormat f) {
        return f == StructureFormat.CSV || f == StructureFormat.TSV;
    }

    private static String titleEvidence(DiagnosisInput in) {
        String title = in.probeStructure() == null ? null : in.probeStructure().htmlTitle();
        return title == null ? "" : " (title: \"" + title + "\")";
    }

    private static String printable(String delimiter) {
        return "\t".equals(delimiter) ? "\\t" : delimiter;
    }
}
