package ai.foresignal.inbound.signature;

/** The result of checking one request's signature. */
public enum Verification {
    /** The signature is present and correct. */
    VALID,
    /** A signature is present but wrong, expired, replayed or malformed. */
    INVALID,
    /** The request carries no signature for this scheme. */
    MISSING
}
