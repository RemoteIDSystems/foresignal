package ai.foresignal.security.quota;

/**
 * A quota checked when a user creates something (prompt R2-026). The {@link #field()} is the {@code Quota} member that
 * names it, carried as {@code limit} in a {@code 409 QUOTA_EXCEEDED} problem.
 */
public enum QuotaLimit {

    /** Trigger rules the user owns ({@code maxAlerts}). */
    ALERTS("maxAlerts"),

    /** Notification channels the user owns ({@code maxChannels}). */
    CHANNELS("maxChannels"),

    /** Saved views the user owns ({@code maxSavedViews}). */
    SAVED_VIEWS("maxSavedViews"),

    /** Bytes of attachments and workspaces the user owns ({@code maxStorageBytes}). */
    STORAGE("maxStorageBytes");

    private final String field;

    QuotaLimit(String field) {
        this.field = field;
    }

    /**
     * The {@code Quota} member.
     *
     * @return the member name
     */
    public String field() {
        return field;
    }
}
