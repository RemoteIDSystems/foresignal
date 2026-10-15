package ai.foresignal.entity;

/** Whether a key identifies an entity on its own (ANCHOR) or may be re-assigned (WEAK, prompt 098). */
public enum EntityKeyRole {
    ANCHOR,
    WEAK
}
