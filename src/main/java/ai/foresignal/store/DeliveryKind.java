package ai.foresignal.store;

import java.util.List;

/** The {@code notification_delivery.kind} values (prompt 018). */
public final class DeliveryKind {

    /** A first message. */
    public static final String SEND = "send";

    /** A lifecycle update that edits the original message. */
    public static final String UPDATE = "update";

    /** A lifecycle update sent as a new message. */
    public static final String FOLLOWUP = "followup";

    /** One message standing for several deferred, folded or grouped messages. */
    public static final String DIGEST = "digest";

    /** Every value. */
    public static final List<String> ALL = List.of(SEND, UPDATE, FOLLOWUP, DIGEST);

    private DeliveryKind() {}
}
