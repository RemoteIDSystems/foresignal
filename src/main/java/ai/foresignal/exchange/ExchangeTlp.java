package ai.foresignal.exchange;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.collab.ShareService;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.TlpTarget;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * The recipients of a package and the TLP rules they must satisfy (prompt 132, COL-2), as pure functions so they can
 * be checked without a database.
 */
public final class ExchangeTlp {

    /** A principal handle on another instance. */
    static final Pattern HANDLE = Pattern.compile("^[a-z0-9][a-z0-9_.-]{1,31}$");

    private ExchangeTlp() {}

    /**
     * Parsed recipients.
     *
     * @param everyone true for {@code *}
     * @param byInstance instance id → principal handles ({@code *} for anyone there)
     */
    public record Recipients(boolean everyone, Map<String, List<String>> byInstance) {

        /** The manifest form: {@code "*"} or a list of {@code PackageRecipient}s. */
        public Object toManifest() {
            if (everyone) {
                return ExchangeVocabulary.EVERYONE;
            }
            List<Map<String, Object>> out = new ArrayList<>();
            for (Map.Entry<String, List<String>> e : byInstance.entrySet()) {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("instanceId", e.getKey());
                r.put("principals", e.getValue());
                out.add(r);
            }
            return out;
        }
    }

    /**
     * Parses {@code recipients}: {@code "*"} or an array of {@code {instanceId, principals}} (handles lower-cased and
     * de-duplicated).
     *
     * @throws ValidationException when malformed
     */
    public static Recipients parse(JsonNode node) {
        if (node != null && node.isString() && ExchangeVocabulary.EVERYONE.equals(node.asString())) {
            return new Recipients(true, Map.of());
        }
        if (node == null || !node.isArray() || node.isEmpty() || node.size() > 100) {
            throw ValidationException.forField("recipients", "must be \"*\" or 1 to 100 PackageRecipient objects");
        }
        Map<String, List<String>> out = new LinkedHashMap<>();
        int i = 0;
        for (JsonNode n : node) {
            String f = "recipients[" + i++ + "]";
            if (n == null || !n.isObject()) {
                throw ValidationException.forField(f, "must be a PackageRecipient object");
            }
            CollabInputs.object(n, "PackageRecipient", Set.of("instanceId", "principals"));
            String instance = CollabInputs.required(n, "instanceId", 1, 128).strip();
            if (instance.contains("/")) {
                throw ValidationException.forField(f + ".instanceId", "must not contain /");
            }
            List<String> handles = CollabInputs.strings(n, "principals", 500);
            if (handles.isEmpty()) {
                throw ValidationException.forField(f + ".principals", "must name at least one handle, or *");
            }
            List<String> list = out.computeIfAbsent(instance, k -> new ArrayList<>());
            for (String h : handles) {
                String handle = h.strip().toLowerCase(Locale.ROOT);
                if (!ExchangeVocabulary.EVERYONE.equals(handle) && !HANDLE.matcher(handle).matches()) {
                    throw ValidationException.forField(f + ".principals", "'" + h + "' is not a principal handle");
                }
                if (!list.contains(handle)) {
                    list.add(handle);
                }
            }
        }
        return new Recipients(false, out);
    }

    /**
     * The TLP rules of a package: {@code *} (or {@code *} among an instance's principals) needs what a federated peer
     * needs (GREEN or CLEAR); each named principal is a remote recipient, of the owner's organization when its
     * instance is ({@code sameOrganization}).
     *
     * @param tlp the package's label
     * @param recipients the recipients
     * @param sameOrganization whether an instance belongs to the owner's organization
     * @throws ForesignalException {@code 403 TLP_VIOLATION} naming the rule
     */
    public static void enforce(Tlp tlp, Recipients recipients, Predicate<String> sameOrganization) {
        if (recipients.everyone()) {
            ShareService.enforce(EgressPolicy.tlp(tlp, TlpTarget.federatedPeer()));
            return;
        }
        for (Map.Entry<String, List<String>> e : recipients.byInstance().entrySet()) {
            if (e.getValue().contains(ExchangeVocabulary.EVERYONE)) {
                ShareService.enforce(EgressPolicy.tlp(tlp, TlpTarget.federatedPeer()));
            }
            boolean same = sameOrganization.test(e.getKey());
            for (String h : e.getValue()) {
                if (!ExchangeVocabulary.EVERYONE.equals(h)) {
                    ShareService.enforce(EgressPolicy.tlp(tlp, TlpTarget.remoteRecipient(same)));
                }
            }
        }
    }
}
