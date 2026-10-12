package ai.foresignal.api;

/** Test access to {@link BriefingsController}'s package-private negotiation helper (prompt 029). */
public final class BriefingsControllerAccess {

    private BriefingsControllerAccess() {}

    /** {@link BriefingsController#wantsHtml}. */
    public static boolean wantsHtml(String format, String accept) {
        return BriefingsController.wantsHtml(format, accept);
    }
}
