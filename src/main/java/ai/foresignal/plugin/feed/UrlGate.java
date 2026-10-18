package ai.foresignal.plugin.feed;

import ai.foresignal.net.OutboundUrlPolicy;
import java.net.URI;
import java.util.function.Supplier;

/**
 * Decides, before every fetch, whether a {@code wasm-feed} URL may be requested (prompt 141). Production uses the
 * outbound URL policy (prompt 035, {@code PUBLIC_ONLY}, https); tests that serve the upstream from loopback supply
 * {@link #ALLOW_ALL}.
 */
@FunctionalInterface
public interface UrlGate {

    /** A gate that refuses nothing (tests). */
    UrlGate ALLOW_ALL = uri -> null;

    /**
     * Checks one URL.
     *
     * @param uri the URL
     * @return null when it may be requested, else why not
     */
    String refusal(URI uri);

    /**
     * The production gate: the outbound URL policy's {@code PUBLIC_ONLY} profile, re-resolving the host on every
     * call so a DNS change after startup is caught; refused when the policy is unavailable.
     *
     * @param policy supplies the policy, or returns null when it is unavailable
     */
    static UrlGate outbound(Supplier<OutboundUrlPolicy> policy) {
        return uri -> {
            OutboundUrlPolicy p = policy == null ? null : policy.get();
            if (p == null) {
                return "the outbound URL policy is unavailable";
            }
            OutboundUrlPolicy.Result result = p.check(uri, OutboundUrlPolicy.Profile.PUBLIC_ONLY);
            return result instanceof OutboundUrlPolicy.Denied denied ? denied.reason() : null;
        };
    }
}
