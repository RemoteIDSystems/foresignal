package ai.foresignal.security;

/** What kind of actor a {@link PrincipalEntity} is (OpenAPI {@code Principal.kind}). */
public enum PrincipalKind {
    PERSON,
    AGENT,
    SERVICE;

    /** The wire value. */
    public String wireValue() {
        return name().toLowerCase();
    }
}
