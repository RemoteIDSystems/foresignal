package ai.foresignal.object;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.benchmark.BenchmarkCaseEntity;
import ai.foresignal.benchmark.BenchmarkInputs;
import ai.foresignal.benchmark.BenchmarkService;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Benchmark-case proposals from verdicts (prompt 129): a verdict on an object with a time window proposes a
 * historical 123 benchmark case (source {@code operator}); an operator accepts or rejects it. Nothing is added to the
 * benchmark corpus automatically.
 */
@Service
public class BenchmarkCaseProposals {

    /** Proposal status: waiting for an operator. */
    public static final String PROPOSED = "proposed";

    /** The shortest window a proposed case gets around an instantaneous object. */
    static final Duration MIN_WINDOW = Duration.ofDays(1);

    private final BenchmarkCaseProposalRepository proposals;
    private final BenchmarkService benchmarks;
    private final Clock clock;

    /** Creates the service. */
    public BenchmarkCaseProposals(BenchmarkCaseProposalRepository proposals, BenchmarkService benchmarks,
            Clock clock) {
        this.proposals = proposals;
        this.benchmarks = benchmarks;
        this.clock = clock;
    }

    /**
     * Proposes a case for a verdict, when the judged object has a time window and the verdict is a label
     * (confirmed, rejected or duplicate-of; {@code needs-review} proposes nothing).
     *
     * @param verdict the verdict annotation
     * @param target the judged object's envelope
     * @param findingType the finding type when the target is a finding, or null
     * @param regionCodes the target's 040 region codes
     * @return the proposal, or empty when none applies
     */
    @Transactional
    public Optional<BenchmarkCaseProposalEntity> propose(AnnotationEntity verdict, ObjectEnvelope target,
            String findingType, List<String> regionCodes) {
        if (verdict.getVerdict() == null || "needs-review".equals(verdict.getVerdict())) {
            return Optional.empty();
        }
        Instant start = target.time() == null ? null : target.time().start();
        if (start == null) {
            return Optional.empty();
        }
        Instant end = target.time().end();
        if (end == null || end.isBefore(start.plus(MIN_WINDOW))) {
            end = start.plus(MIN_WINDOW);
        }
        Map<String, Object> expectations = new LinkedHashMap<>();
        if (findingType != null && !"duplicate-of".equals(verdict.getVerdict())) {
            Map<String, Object> finding = new LinkedHashMap<>();
            finding.put("findingType", findingType);
            finding.put("present", "confirmed".equals(verdict.getVerdict()));
            if (regionCodes != null && !regionCodes.isEmpty()) {
                finding.put("regionCodes", regionCodes);
            }
            expectations.put("findings", List.of(finding));
        }
        expectations.put("notes", "Proposed by a " + verdict.getVerdict() + " verdict on " + target.ref()
                + " (annotation:" + verdict.getId() + ").");
        String name = clip("Verdict " + verdict.getVerdict() + ": " + target.title(), 200);
        Instant now = clock.instant();
        BenchmarkCaseProposalEntity p = new BenchmarkCaseProposalEntity(Ulids.next(now), verdict.getId(),
                target.ref(), name, start, end, verdict.getAuthor(), now);
        p.setRegionCodes(regionCodes == null ? List.of() : regionCodes.stream()
                .filter(r -> r != null && BenchmarkInputs.REGION.matcher(r).matches()).toList());
        p.setExpectations(expectations);
        p.setPrivateProposal(target.privateObject());
        return Optional.of(proposals.save(p));
    }

    /** Proposals waiting for an operator, oldest first. */
    public List<BenchmarkCaseProposalEntity> pending() {
        return proposals.findByStatusOrderByIdAsc(PROPOSED);
    }

    /**
     * Accepts a proposal: creates the 123 benchmark case with source {@code operator}.
     *
     * @param id the proposal id
     * @param actor who accepts it
     * @return the created case
     */
    @Transactional
    public BenchmarkCaseEntity accept(String id, String actor) {
        BenchmarkCaseProposalEntity p = waiting(id);
        BenchmarkCaseEntity c = benchmarks.createCase(new BenchmarkInputs.CaseInput(p.getName(),
                "From a verdict on " + p.getTargetRef(), p.getWindowFrom(), p.getWindowTo(), p.getRegionCodes(),
                p.getExpectations()), "operator", actor);
        p.decide("accepted", actor, clock.instant(), c.getId());
        proposals.save(p);
        return c;
    }

    /** Rejects a proposal. */
    @Transactional
    public void reject(String id, String actor) {
        BenchmarkCaseProposalEntity p = waiting(id);
        p.decide("rejected", actor, clock.instant(), null);
        proposals.save(p);
    }

    private BenchmarkCaseProposalEntity waiting(String id) {
        BenchmarkCaseProposalEntity p = proposals.findById(id).orElseThrow(() -> new NotFoundException(
                "No benchmark-case proposal '" + id + "' exists."));
        if (!PROPOSED.equals(p.getStatus())) {
            throw new ConflictException(ErrorCodes.VALIDATION_FAILED, "The proposal was already " + p.getStatus()
                    + ".");
        }
        return p;
    }

    private static String clip(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
