package ai.foresignal.forecast.baseline;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.forecast.ForecastInputs.QuestionDraft;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.precursor.Candidate;
import ai.foresignal.precursor.PrecursorEntity;
import ai.foresignal.precursor.PrecursorOccurrences;
import ai.foresignal.precursor.PrecursorRepository;
import ai.foresignal.api.error.ForesignalException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Opens forecast questions from {@code precursor-active} findings (prompt 122 §4): "Will {consequent} occur in
 * {region} between {from} and {to}?", an {@code event-exists} question over the precursor's consequent selector, the
 * finding's regions and its expected window, origin {@code system}, deduplicated by precursor and window. The question
 * links the precursor and the finding, and the finding lists the question. Series consequents (no event selector) and
 * windows ending within the hour open nothing.
 */
@Service
public class PrecursorQuestionOpener {

    private static final Logger log = LoggerFactory.getLogger(PrecursorQuestionOpener.class);

    /** How far back findings are scanned. */
    public static final Duration LOOKBACK = Duration.ofDays(14);

    private final FindingService findings;
    private final PrecursorRepository precursors;
    private final ForecastService ledger;
    private final TransactionTemplate transactions;
    private final Clock clock;

    /** Creates the opener. */
    public PrecursorQuestionOpener(FindingService findings, PrecursorRepository precursors, ForecastService ledger,
            TransactionTemplate transactions, Clock clock) {
        this.findings = findings;
        this.precursors = precursors;
        this.ledger = ledger;
        this.transactions = transactions;
        this.clock = clock;
    }

    /** Opens the questions of recent {@code precursor-active} findings that have none; returns how many opened. */
    public int scan() {
        Instant now = clock.instant();
        int opened = 0;
        for (AnalyticFindingEntity f : findings.detectedBetween("precursor-active", now.minus(LOOKBACK), now)) {
            if (!f.getQuestionIds().isEmpty() || !"open".equals(f.getStatus())) {
                continue;
            }
            try {
                Boolean created = transactions.execute(status -> open(f));
                if (Boolean.TRUE.equals(created)) {
                    opened++;
                }
            } catch (RuntimeException e) {
                log.warn("Opening the question of finding {} failed: {}", f.getId(), e.toString());
            }
        }
        return opened;
    }

    /** The draft of a finding's question, or null when it opens none. */
    public static QuestionDraft draft(AnalyticFindingEntity f, PrecursorEntity p, Instant now) {
        Map<String, Object> selector = Candidate.selector(p.getConsequent());
        Object from = f.getStats().get("expectedFrom");
        Object to = f.getStats().get("expectedTo");
        if (selector == null || from == null || to == null) {
            return null;
        }
        Instant start = Instant.parse(from.toString());
        Instant end = Instant.parse(to.toString());
        if (!end.isAfter(now.plus(Duration.ofHours(1)))) {
            return null;
        }
        Instant closes = start.isAfter(now.plus(Duration.ofHours(1))) ? start : now.plus(Duration.ofHours(1));
        List<String> regions = new ArrayList<>(f.getRegionCodes());
        String label = PrecursorOccurrences.label(selector);
        String where = regions.isEmpty() ? "anywhere" : String.join(", ", regions);
        String title = "Will " + label + " occur in " + where + " between " + start.toString().substring(0, 10)
                + " and " + end.toString().substring(0, 10) + "?";
        if (title.length() > 300) {
            title = title.substring(0, 297) + "...";
        }
        Map<String, Object> resolver = new LinkedHashMap<>();
        resolver.put("type", "event-exists");
        Map<String, Object> sel = new LinkedHashMap<>(selector);
        sel.remove("regionCodes");
        resolver.put("selector", sel);
        if (!regions.isEmpty()) {
            resolver.put("area", Map.of("regionCodes", regions));
        }
        resolver.put("window", Map.of("start", start.toString(), "end", end.toString()));
        Map<String, Object> validated = ResolverSpec.validate(resolver, Ledger.BINARY);
        String criteria = "Resolves yes if Foresignal records at least one occurrence of " + label + " in " + where
                + " between " + start + " and " + end + " (UTC), as found by the event selector in the resolver;"
                + " resolves no otherwise, provided the sources covered the window and could have seen such an event"
                + " (else the question is annulled).";
        Map<String, String> links = new LinkedHashMap<>();
        links.put("precursorId", p.getId());
        links.put("findingId", f.getId());
        String category = f.getSignalFamily() == null ? "activity" : f.getSignalFamily();
        return new QuestionDraft(title, Ledger.BINARY, Map.of(), criteria, validated, now, closes, end, category,
                List.of("precursor", "auto"), links, "precursor:" + p.getId() + ":" + start + ":" + end,
                f.isPrivateFinding());
    }

    private boolean open(AnalyticFindingEntity f) {
        Object precursorId = f.getStats().get("precursorId");
        PrecursorEntity p = precursorId == null ? null : precursors.findById(precursorId.toString()).orElse(null);
        if (p == null) {
            return false;
        }
        QuestionDraft draft;
        try {
            draft = draft(f, p, clock.instant());
        } catch (ForesignalException e) {
            log.debug("Finding {} opens no question: {}", f.getId(), e.getMessage());
            return false;
        }
        if (draft == null) {
            return false;
        }
        ForecastService.Opened opened = ledger.openQuestion(draft, "system", "precursor-watch");
        findings.linkQuestion(f.getId(), opened.question().getId());
        return opened.created();
    }
}
