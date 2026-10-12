package ai.foresignal.ask;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Verifies the citations of a model's answer against what the tools returned (prompt 028; shared
 * with briefings 029 and enrichment 030).
 *
 * <p><strong>Formats.</strong> {@code [source:externalId@seq]} cites a record version;
 * {@code [source:externalId]} is accepted and resolved to the highest seq the tools returned for that
 * record; {@code [alert:id]} and {@code [briefing:runId]} cite an alert and a briefing run. Markdown
 * links ({@code [text](url)}) are not citations.
 *
 * <p><strong>Verification.</strong> A citation whose id no tool returned, or whose explicit
 * {@code @seq} no tool returned for that id, is dropped with the warning
 * {@code "dropped citation [x]: not returned by any tool"}. Accepted citations are de-duplicated in
 * order of first appearance and titled from the tool output, else from the stored version
 * ({@link RecordTitleLookup}).
 *
 * <p><strong>Uncited claims.</strong> The answer is split into lines (list items, headings and table
 * rows each count as one) and sentences. A segment without an accepted citation that contains a
 * number, a date or time, a coordinate, or a capitalized name (other than its first word) that also
 * appears in the tool output, adds {@code "uncited claim: <first 80 chars>"}; after
 * {@value #MAX_UNCITED} such warnings the rest are counted in one {@code "…and N more"}. When the tools
 * returned anything citable and the answer cites nothing, {@value #NO_CITATIONS} is added. Warnings
 * are never stripped by later stages.
 */
@Component
public class CitationVerifier {

    /** The most uncited-claim warnings listed one by one. */
    public static final int MAX_UNCITED = 10;

    /** The warning when an answer with citable material cites nothing. */
    public static final String NO_CITATIONS = "answer contains no citations";

    /** A citation-shaped token: {@code [prefix:rest]} not followed by {@code (}. */
    static final Pattern CITATION = Pattern.compile("\\[([A-Za-z0-9][A-Za-z0-9._-]*):([^\\[\\]\\s]+)](?!\\()");

    private static final Pattern SEQ_SUFFIX = Pattern.compile("^(.+)@(\\d{1,18})$");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern MONTH = Pattern.compile(
            "\\b(January|February|March|April|May|June|July|August|September|October|November|December"
                    + "|Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec"
                    + "|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|today|yesterday|tomorrow)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{M}][\\p{L}\\p{M}'’-]*");
    private static final Pattern SENTENCE_END = Pattern.compile("(?<=[.!?])\\s+(?=[\\p{Lu}\\d\\[(\"“])");
    private static final Pattern LIST_MARKER = Pattern.compile("^(?:[-*+•]|\\d{1,3}[.)])\\s+");
    private static final Pattern HEADING = Pattern.compile("^#{1,6}\\s+");
    private static final Pattern TABLE_RULE = Pattern.compile("^\\|?\\s*:?-{3,}.*$");

    /** Capitalized words that are never names on their own. */
    static final Set<String> NOT_NAMES = Set.of("The", "A", "An", "This", "That", "These", "Those", "There", "It",
            "Its", "In", "On", "At", "Of", "For", "From", "To", "By", "With", "And", "Or", "But", "No", "Yes", "Not",
            "I", "We", "You", "They", "He", "She", "If", "As", "All", "Some", "Most", "Many", "One", "Two", "Three",
            "Foresignal", "Note", "Summary", "Answer", "Record", "Records", "Alert", "Alerts", "Source", "Sources",
            "Live", "None", "Other", "Also", "However", "Currently", "Here", "Based");

    private final RecordTitleLookup titles;

    /**
     * Creates the verifier.
     *
     * @param titles titles of record versions the tool output left untitled
     */
    @Autowired
    public CitationVerifier(RecordTitleLookup titles) {
        this.titles = Objects.requireNonNull(titles, "titles");
    }

    /**
     * Verifies {@code answer}.
     *
     * @param answer the model's markdown answer
     * @param citable what the tools returned
     * @return the accepted citations and the warnings
     */
    public Verification verify(String answer, CitationSet citable) {
        String text = answer == null ? "" : answer;
        List<AskCitation> accepted = new ArrayList<>();
        Set<String> acceptedKeys = new LinkedHashSet<>();
        Set<String> acceptedTokens = new LinkedHashSet<>();
        Set<String> warnings = new LinkedHashSet<>();

        Matcher m = CITATION.matcher(text);
        while (m.find()) {
            String token = m.group();
            String inner = m.group(1) + ":" + m.group(2);
            AskCitation citation = resolve(m.group(1), m.group(2), citable);
            if (citation == null) {
                warnings.add("dropped citation [" + inner + "]: not returned by any tool");
                continue;
            }
            acceptedTokens.add(token);
            if (acceptedKeys.add(citation.id() + "@" + citation.seq())) {
                accepted.add(citation);
            }
        }

        List<String> uncited = uncitedClaims(text, acceptedTokens, citable.corpus());
        for (int i = 0; i < Math.min(uncited.size(), MAX_UNCITED); i++) {
            warnings.add(uncited.get(i));
        }
        if (uncited.size() > MAX_UNCITED) {
            warnings.add("…and " + (uncited.size() - MAX_UNCITED) + " more");
        }
        if (!citable.isEmpty() && accepted.isEmpty()) {
            warnings.add(NO_CITATIONS);
        }
        return new Verification(List.copyOf(accepted), List.copyOf(warnings));
    }

    /** The citation a token stands for, or null when no tool returned it. */
    AskCitation resolve(String prefix, String rest, CitationSet citable) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        if (lower.equals(AskCitation.ALERT)) {
            String id = stripSeq(rest);
            return citable.hasAlert(id)
                    ? new AskCitation("alert:" + id, null, orElse(citable.alertTitle(id), "Alert " + id), AskCitation.ALERT)
                    : null;
        }
        if (lower.equals(AskCitation.BRIEFING)) {
            String id = stripSeq(rest);
            return citable.hasBriefing(id)
                    ? new AskCitation("briefing:" + id, null, orElse(citable.briefingTitle(id), "Briefing " + id),
                            AskCitation.BRIEFING)
                    : null;
        }
        Matcher seqMatch = SEQ_SUFFIX.matcher(rest);
        String id;
        long seq;
        if (seqMatch.matches()) {
            id = prefix + ":" + seqMatch.group(1);
            seq = Long.parseLong(seqMatch.group(2));
            if (!citable.hasRecord(id, seq)) {
                return null;
            }
        } else {
            id = prefix + ":" + rest;
            var latest = citable.latestSeq(id);
            if (latest.isEmpty()) {
                return null;
            }
            seq = latest.get();
        }
        String title = citable.recordTitle(id, seq);
        if (title == null) {
            title = titles.title(id, seq).orElse(id);
        }
        return new AskCitation(id, seq, title, AskCitation.RECORD);
    }

    /** The uncited-claim warnings of {@code text}, in order, uncapped. */
    List<String> uncitedClaims(String text, Set<String> acceptedTokens, String corpus) {
        List<String> out = new ArrayList<>();
        boolean inFence = false;
        for (String rawLine : text.split("\\R")) {
            String line = rawLine.strip();
            if (line.startsWith("```")) {
                inFence = !inFence;
                continue;
            }
            if (inFence || line.isEmpty() || TABLE_RULE.matcher(line).matches()) {
                continue;
            }
            line = HEADING.matcher(LIST_MARKER.matcher(line).replaceFirst("")).replaceFirst("");
            for (String segment : SENTENCE_END.split(line)) {
                String s = segment.strip();
                if (s.isEmpty() || containsAny(s, acceptedTokens)) {
                    continue;
                }
                String bare = CITATION.matcher(s).replaceAll(" ");
                if (isClaim(bare, corpus)) {
                    out.add("uncited claim: " + (s.length() <= 80 ? s : s.substring(0, 80)));
                }
            }
        }
        return out;
    }

    /** True when {@code segment} states a checkable fact: a number, a date or time, or a returned name. */
    static boolean isClaim(String segment, String corpus) {
        if (DIGIT.matcher(segment).find() || MONTH.matcher(segment).find()) {
            return true;
        }
        Matcher words = WORD.matcher(segment);
        boolean first = true;
        while (words.find()) {
            String word = words.group();
            if (first) {
                first = false;
                continue;
            }
            if (word.length() >= 2 && Character.isUpperCase(word.codePointAt(0)) && !NOT_NAMES.contains(word)
                    && corpus != null && containsWord(corpus, word)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsWord(String corpus, String word) {
        int from = 0;
        while (true) {
            int at = corpus.indexOf(word, from);
            if (at < 0) {
                return false;
            }
            int end = at + word.length();
            boolean startOk = at == 0 || !Character.isLetterOrDigit(corpus.charAt(at - 1));
            boolean endOk = end >= corpus.length() || !Character.isLetterOrDigit(corpus.charAt(end));
            if (startOk && endOk) {
                return true;
            }
            from = at + 1;
        }
    }

    private static boolean containsAny(String segment, Set<String> tokens) {
        for (String token : tokens) {
            if (segment.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static String stripSeq(String rest) {
        Matcher seq = SEQ_SUFFIX.matcher(rest);
        return seq.matches() ? seq.group(1) : rest;
    }

    private static String orElse(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * The outcome.
     *
     * @param citations the accepted citations, de-duplicated, in order of first appearance
     * @param warnings dropped citations, uncited claims and the no-citations warning, in that order
     */
    public record Verification(List<AskCitation> citations, List<String> warnings) {}
}
