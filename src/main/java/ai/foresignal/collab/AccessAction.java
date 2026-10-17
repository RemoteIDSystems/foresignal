package ai.foresignal.collab;

/**
 * What a principal wants to do with a work product (prompt 130), and the weakest {@link AclRole} that allows it.
 */
public enum AccessAction {

    /** Read the object. */
    VIEW(AclRole.VIEWER),

    /** Comment on it (annotation threads). */
    COMMENT(AclRole.COMMENTER),

    /** Change it. */
    EDIT(AclRole.EDITOR),

    /** Read or change its access list and share it. */
    SHARE(AclRole.EDITOR),

    /** Delete it. */
    DELETE(AclRole.OWNER),

    /** Transfer its ownership. */
    TRANSFER(AclRole.OWNER);

    private final AclRole required;

    AccessAction(AclRole required) {
        this.required = required;
    }

    /** The weakest role that allows the action. */
    public AclRole required() {
        return required;
    }
}
