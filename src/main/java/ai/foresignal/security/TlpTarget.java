package ai.foresignal.security;

/**
 * Where a labeled work product would go (prompt 130), as {@link EgressPolicy#tlp(Tlp, TlpTarget)} judges it.
 *
 * @param kind the kind of target
 * @param sameOrganization true when every principal the target reaches belongs to the owner's organization (two
 *     principals without an organization count as the same organization, so a single-organization instance is one
 *     organization); for {@link Kind#REMOTE_RECIPIENT} the recipient's organization matches the owner's
 * @param named true when the target names its recipients one by one (a principal, or a named recipient on another
 *     instance) rather than reaching a set (a group, the instance, a peer, a link)
 */
public record TlpTarget(Kind kind, boolean sameOrganization, boolean named) {

    /** The kinds of target. */
    public enum Kind {
        /** A principal of this instance, named in the access list. */
        PRINCIPAL,
        /** A group of this instance. */
        GROUP,
        /** Every principal of this instance ({@code instance} visibility or grantee). */
        INSTANCE,
        /** A public share link (112). */
        PUBLIC_LINK,
        /** A federated peer (113), as a whole. */
        FEDERATED_PEER,
        /** A named recipient on another instance (132 exchange). */
        REMOTE_RECIPIENT
    }

    /** A principal of this instance (always named). */
    public static TlpTarget principal(boolean sameOrganization) {
        return new TlpTarget(Kind.PRINCIPAL, sameOrganization, true);
    }

    /** A group whose members all share the owner's organization, or not. */
    public static TlpTarget group(boolean allSameOrganization) {
        return new TlpTarget(Kind.GROUP, allSameOrganization, false);
    }

    /** The instance, whose principals all share the owner's organization, or not. */
    public static TlpTarget instance(boolean allSameOrganization) {
        return new TlpTarget(Kind.INSTANCE, allSameOrganization, false);
    }

    /** A public share link. */
    public static TlpTarget publicLink() {
        return new TlpTarget(Kind.PUBLIC_LINK, false, false);
    }

    /** A federated peer. */
    public static TlpTarget federatedPeer() {
        return new TlpTarget(Kind.FEDERATED_PEER, false, false);
    }

    /** A named recipient on another instance, of the owner's organization or not. */
    public static TlpTarget remoteRecipient(boolean sameOrganization) {
        return new TlpTarget(Kind.REMOTE_RECIPIENT, sameOrganization, true);
    }

    /**
     * What {@link EgressPolicy#tlp} decided.
     *
     * @param allowed true when the label allows the target
     * @param rule the rule that decided, quoted in a {@code TLP_VIOLATION} problem
     */
    public record Decision(boolean allowed, String rule) {}
}
