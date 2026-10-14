package ai.foresignal.source.adapters.radio;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * What the prompt 082 amateur-radio sources share: the test base URL (the 075 {@code radio} property), the call-sign
 * rule, the response-body check and the field-spec helpers. Not an adapter.
 */
final class RadioSources {

    /**
     * The test-only property moving every radio polling URL under one base as {@code {base}/{host}{path}} (shared with
     * {@code radio-browser}); blank in production.
     */
    static final String BASE_URL_PROPERTY = RadioBrowserAdapter.BASE_URL_PROPERTY;

    /**
     * An amateur call sign as PSK Reporter shows it: 3 to 15 letters, digits and {@code /} (portable prefixes and
     * suffixes such as {@code F5OYA/P}), with at least one digit.
     */
    static final Pattern CALLSIGN = Pattern.compile("^(?=.*[0-9])[A-Za-z0-9/]{3,15}$");

    private RadioSources() {}

    /** {@code url} under {@code baseOverride} as {@code {base}/{host}{path}?{query}}, or unchanged when blank. */
    static URI uri(String url, String baseOverride) {
        return URI.create(KitUrls.rebased(url, baseOverride));
    }

    /**
     * The body of a 200 response that was not cut at the response cap.
     *
     * @param stateKey the validator state key to forget on failure, or null
     * @throws UpstreamFormatException otherwise
     */
    static byte[] body(FetchContext ctx, UpstreamResponse response, String stateKey, URI uri) {
        if (response.status() != 200 || response.truncated()) {
            if (stateKey != null) {
                SourceHttp.forgetValidators(ctx.state(), stateKey);
            }
            throw new UpstreamFormatException(ctx.sourceId() + ": " + uri.getHost() + uri.getPath()
                    + (response.status() != 200 ? " answered HTTP " + response.status() : " exceeds the response cap"),
                    "");
        }
        return response.body();
    }

    /** Declares a field of {@code type}. */
    static void field(SourceDescriptor.Builder b, String key, FieldType type, String unit, String label,
            String description, String... kinds) {
        b.fieldSpec(key, new FieldSpec(type, unit, label, description, null, Set.of(kinds)));
    }

    /** Declares a vertical field with a datum. */
    static void vertical(SourceDescriptor.Builder b, String key, String unit, String label, String description,
            String datum, String... kinds) {
        b.fieldSpec(key, new FieldSpec(FieldType.NUMBER, unit, label, description, datum, Set.of(kinds)));
    }
}
