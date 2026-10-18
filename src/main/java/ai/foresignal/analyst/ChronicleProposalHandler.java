package ai.foresignal.analyst;

import java.util.Map;

/**
 * The {@code propose_chronicle} proposal type (prompt 131), implemented by the chronicle package so the 102 pipeline
 * and the 128 proposer need not depend on it. A proposed chronicle is created at once with status {@code proposed}
 * (no capture) and reaches its owner's inbox; executing the proposal (the operator confirms it, or the profile allows
 * the action) activates it, within the auto-activation caps when it runs on its own.
 */
public interface ChronicleProposalHandler {

    /**
     * Why a {@code propose_chronicle} payload is invalid, or null: it needs a {@code situationId} of an existing
     * situation, or an {@code area} (with an optional {@code topic}).
     */
    String validate(Map<String, Object> payload);

    /**
     * Creates the proposed chronicle of a just-saved proposal and records its id in the proposal's payload
     * ({@code chronicleId}); the caller saves the proposal afterwards.
     *
     * @param proposal the saved proposal
     */
    void created(AnalystProposalEntity proposal);

    /**
     * Executes a proposal: activates its chronicle.
     *
     * @param proposal the proposal
     * @param auto true when the profile executes it on its own (the per-day and size caps then apply)
     * @param actor who executes it
     * @return the executed reference ({@code chronicle:{id}})
     */
    String execute(AnalystProposalEntity proposal, boolean auto, String actor);
}
