package ai.foresignal.inbound.edp.binding;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What a binding does (or would do) with one item (prompt 138, the {@code ExternalBindingPreview.effects} entries).
 *
 * @param itemRef the item's record ref
 * @param action what is done, e.g. {@code series-point}, {@code canonical-link}, {@code forecast-recorded}, or a
 *     {@code skip:…} reason
 * @param targetRef what it creates or changes, or null
 * @param error why it cannot be applied, or null
 */
public record BindingEffect(String itemRef, String action, String targetRef, String error) {

    /** An applied (or applicable) effect. */
    public static BindingEffect of(BoundItem item, String action, String targetRef) {
        return new BindingEffect(item.ref(), action, targetRef, null);
    }

    /** An item the binding leaves alone, with the reason (not an error). */
    public static BindingEffect skip(BoundItem item, String reason) {
        return new BindingEffect(item.ref(), "skip:" + reason, null, null);
    }

    /** An item the binding cannot apply. */
    public static BindingEffect error(BoundItem item, String message) {
        return new BindingEffect(item.ref(), "error", null, message);
    }

    /** True when nothing was (or would be) done because of a skip reason. */
    public boolean skipped() {
        return action != null && action.startsWith("skip:");
    }

    /** True when the item cannot be applied. */
    public boolean failed() {
        return error != null;
    }

    /** The preview shape ({@code itemRef}, {@code action}, {@code targetRef}). */
    public Map<String, Object> view() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("itemRef", itemRef);
        out.put("action", action);
        out.put("targetRef", targetRef);
        return out;
    }
}
