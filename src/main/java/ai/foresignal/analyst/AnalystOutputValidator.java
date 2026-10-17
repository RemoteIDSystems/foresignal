package ai.foresignal.analyst;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The pure output validator (prompt 102). It enforces the binding analytic principles on what the
 * model returned: every claim is typed; an {@code evidence} claim without a valid citation becomes
 * {@code speculation}; unknown citations are dropped; a proposed event needs at least two verified
 * members and a known type; a proposal type must be allowed; and a note over 2,000 words is truncated. Since prompt
 * 122 the note and every claim are also linted with {@code EstimativeLanguage} (a likelihood term that disagrees with
 * its number, mixed ICD 203 rows, or a likelihood term beside a confidence level), each violation becoming a warning.
 */
public final class AnalystOutputValidator {

    /** The maximum note body length, words; longer is truncated. */
    public static final int MAX_WORDS = 2_000;

    /** The absence keywords that require a coverage-ok citation. */
    static final List<String> ABSENCE_KEYWORDS = List.of("stopped", "ceased", "no longer", "absent", "none",
            "cleared", "quiet");

    /** True when a citation id is citable (appeared in this run's tool output). */
    @FunctionalInterface
    public interface CitationCheck {

        /** Whether the id is citable. */
        boolean valid(String id);
    }

    /** True when a proposed event's members are all verified and number at least two. */
    @FunctionalInterface
    public interface MemberCheck {

        /** Whether the members exist (by seq) and there are at least two. */
        boolean atLeastTwo(String type, List<Map<String, Object>> members);
    }

    private AnalystOutputValidator() {}

    /** A raw claim from the model. */
    public record RawClaim(String text, String basis, List<String> citations) {}

    /** A raw proposal from the model. */
    public record RawProposal(String type, Map<String, Object> payload, String rationale, List<String> citations) {}

    /** The validated output. */
    public record Validated(String title, String body, String priority, List<AnalystClaim> claims,
            List<AnalystProposalSpec> proposals, List<String> warnings, int citationsDropped, int claimsDowngraded) {}

    /** A validated proposal ready to persist. */
    public record AnalystProposalSpec(String type, Map<String, Object> payload, String rationale, List<String> citations) {}

    /**
     * Validates a model output.
     *
     * @param title the note title
     * @param body the note body
     * @param priority low | normal | high
     * @param rawClaims the model's claims
     * @param rawProposals the model's proposals
     * @param citations the citable ids from this run
     * @param members the member verification
     * @param allowedActions the profile's allowed actions
     * @param knownEventTypes the recipe-registry event types
     */
    public static Validated validate(String title, String body, String priority, List<RawClaim> rawClaims,
            List<RawProposal> rawProposals, CitationCheck citations, MemberCheck members, Set<String> allowedActions,
            Set<String> knownEventTypes) {
        List<String> warnings = new ArrayList<>();
        int dropped = 0;
        int downgraded = 0;
        List<AnalystClaim> claims = new ArrayList<>();
        for (RawClaim raw : rawClaims) {
            List<String> kept = new ArrayList<>();
            for (String id : raw.citations()) {
                if (citations.valid(id)) {
                    if (!kept.contains(id)) {
                        kept.add(id);
                    }
                } else {
                    dropped++;
                    warnings.add("dropped citation [" + id + "]: not returned by any tool");
                }
            }
            String basis = raw.basis() == null ? AnalystClaim.INFERENCE : raw.basis().toLowerCase(Locale.ROOT);
            if (!Set.of(AnalystClaim.EVIDENCE, AnalystClaim.INFERENCE, AnalystClaim.SPECULATION).contains(basis)) {
                basis = AnalystClaim.INFERENCE;
            }
            boolean wasDowngraded = false;
            String warning = null;
            if (AnalystClaim.EVIDENCE.equals(basis) && kept.isEmpty()) {
                basis = AnalystClaim.SPECULATION;
                wasDowngraded = true;
                downgraded++;
                warning = "evidence claim without a valid citation downgraded to speculation";
                warnings.add(warning);
            }
            if (mentionsAbsence(raw.text()) && kept.isEmpty()) {
                warning = warning == null ? "absence claim without a coverage-ok citation" : warning;
                warnings.add("absence claim without a coverage-ok citation: " + firstWords(raw.text()));
            }
            claims.add(new AnalystClaim(raw.text(), basis, kept, wasDowngraded, warning));
        }
        for (ai.foresignal.forecast.language.EstimativeLanguage.Violation v
                : ai.foresignal.forecast.language.EstimativeLanguage.lint(body, List.of())) {
            warnings.add("estimative language in the note: " + v.message());
        }
        for (AnalystClaim claim : claims) {
            for (ai.foresignal.forecast.language.EstimativeLanguage.Violation v
                    : ai.foresignal.forecast.language.EstimativeLanguage.lint(claim.text(), List.of())) {
                warnings.add("estimative language in a claim: " + v.message());
            }
        }
        int[] words = {0};
        String trimmed = truncate(body, words, warnings);
        // List.of(...).contains(null) throws, and a model may omit the priority.
        String normalizedPriority = priority != null && List.of("low", "normal", "high").contains(priority) ? priority
                : "normal";
        List<AnalystProposalSpec> proposals = new ArrayList<>();
        for (RawProposal original : rawProposals) {
            if (original.type() == null || !allowedActions.contains(original.type())) {
                warnings.add("dropped proposal " + original.type() + ": not in allowed_actions");
                continue;
            }
            RawProposal raw = original;
            if (AnalystActions.PROPOSE_INTEL_EVENT.equals(original.type())) {
                Map<String, Object> clean = scoreFree(original.payload());
                if (!clean.equals(original.payload())) {
                    warnings.add("model-supplied confidence, weights or signal families discarded; EventScorer"
                            + " computes the score");
                }
                raw = new RawProposal(original.type(), clean, original.rationale(), original.citations());
                List<Map<String, Object>> memberList = memberList(raw.payload());
                String type = raw.payload().get("type") == null ? null : String.valueOf(raw.payload().get("type"));
                if (type == null || !knownEventTypes.contains(type)) {
                    warnings.add("dropped event proposal: unknown type " + type);
                    continue;
                }
                if (!members.atLeastTwo(type, memberList)) {
                    warnings.add("dropped event proposal: fewer than two verified members");
                    continue;
                }
            }
            List<String> keptCitations = new ArrayList<>(new LinkedHashSet<>(raw.citations()));
            keptCitations.removeIf(id -> !citations.valid(id));
            proposals.add(new AnalystProposalSpec(raw.type(), raw.payload(), raw.rationale(), keptCitations));
        }
        return new Validated(title, trimmed, normalizedPriority, claims, proposals, warnings, dropped, downgraded);
    }

    /**
     * An event proposal without any number the model chose (principle 3, INT-3): {@code type},
     * {@code title} and the members' identities only. Confidence, weights, signal families and roles are
     * discarded; {@code EventScorer} computes the score from the members.
     */
    static Map<String, Object> scoreFree(Map<String, Object> payload) {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        if (payload.containsKey("type")) {
            out.put("type", payload.get("type"));
        }
        if (payload.containsKey("title")) {
            out.put("title", payload.get("title"));
        }
        Object members = payload.get("members");
        out.put("members", members instanceof List<?> list ? AnalystOutputTools.identities(new ArrayList<>(list))
                : List.of());
        return out;
    }

    /** True when the text asserts an absence. */
    static boolean mentionsAbsence(String text) {
        if (text == null) {
            return false;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String keyword : ABSENCE_KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    static String truncate(String body, int[] words, List<String> warnings) {
        if (body == null) {
            return "";
        }
        String[] split = body.trim().split("\\s+");
        words[0] = split.length == 1 && split[0].isEmpty() ? 0 : split.length;
        if (words[0] <= MAX_WORDS) {
            return body;
        }
        warnings.add("note truncated to " + MAX_WORDS + " words");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_WORDS; i++) {
            sb.append(split[i]).append(i == MAX_WORDS - 1 ? "" : " ");
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> memberList(Map<String, Object> payload) {
        Object raw = payload.get("members");
        if (raw instanceof List<?> list) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    out.add((Map<String, Object>) map);
                }
            }
            return out;
        }
        return List.of();
    }

    private static String firstWords(String text) {
        if (text == null) {
            return "";
        }
        String[] split = text.trim().split("\\s+");
        return String.join(" ", java.util.Arrays.copyOfRange(split, 0, Math.min(8, split.length)));
    }
}
