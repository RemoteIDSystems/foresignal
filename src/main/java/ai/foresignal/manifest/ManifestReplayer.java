package ai.foresignal.manifest;

/**
 * Re-runs one kind of deterministic component from its manifest (prompt 116). Implementations are Spring beans;
 * {@link ManifestReplayService} picks the first that {@link #supports} a manifest's component.
 */
public interface ManifestReplayer {

    /** True when this replayer re-runs {@code component}. */
    boolean supports(String component);

    /**
     * Re-runs the component at the manifest's {@code asOfSeq} with its recorded configuration and inputs and
     * compares the result with the stored output.
     */
    ReplayOutcome replay(ManifestService.Manifest manifest);
}
