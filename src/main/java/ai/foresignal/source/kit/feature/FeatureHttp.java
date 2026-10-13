package ai.foresignal.source.kit.feature;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The request and response rules every feature-kit client shares (prompt 073): requests go through
 * {@code ctx.http()} only; a 3xx is refused, another non-200 status, a truncated body or a document that is not JSON
 * is an {@link UpstreamFormatException}, and an ArcGIS-style {@code {"error": …}} body is one too.
 */
final class FeatureHttp {

    /** The JSON mapper of upstream documents. */
    static final JsonMapper JSON = JsonMapper.builder().build();

    private FeatureHttp() {}

    /**
     * A GET returning the parsed JSON body.
     *
     * @param ctx the fetch context
     * @param uri the URL
     * @param headers extra request headers
     * @param what what is being read, for messages
     * @return the parsed body and its response
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException on a redirect, another status, truncation or a body that is not JSON
     */
    static Page get(FetchContext ctx, URI uri, Map<String, String> headers, String what) throws AdapterException {
        UpstreamResponse response = ctx.http().get(uri, headers == null ? Map.of() : headers);
        return new Page(parse(response, what), response);
    }

    /**
     * Parses a response.
     *
     * @throws UpstreamFormatException on a redirect, another status, truncation or a body that is not JSON
     */
    static JsonNode parse(UpstreamResponse response, String what) {
        int status = response.status();
        if (status >= 300 && status < 400) {
            throw new UpstreamFormatException(what + ": redirect refused", "");
        }
        if (status != 200) {
            throw new UpstreamFormatException(what + " answered HTTP " + status, "");
        }
        if (response.truncated()) {
            throw new UpstreamFormatException(what + " exceeds the response cap", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(response.body());
        } catch (JacksonException e) {
            throw new UpstreamFormatException(what + " is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException(what + " is not a JSON object", "");
        }
        JsonNode error = root.get("error");
        if (error != null && error.isObject()) {
            JsonNode message = error.get("message");
            throw new UpstreamFormatException(what + " returned an error: "
                    + (message == null ? error.toString() : message.asString()), "");
        }
        return root;
    }

    /**
     * {@code base} plus every entry of {@code params} (nulls skipped), URL-encoded.
     *
     * @param base the URL so far
     * @param params parameter name to value, in order
     */
    static URI uri(String base, Map<String, ?> params) {
        String url = base;
        for (Map.Entry<String, ?> e : params.entrySet()) {
            url = KitUrls.withParam(url, e.getKey(), e.getValue());
        }
        return URI.create(url);
    }

    /** An ordered parameter map. */
    static Map<String, Object> params() {
        return new LinkedHashMap<>();
    }

    /**
     * A parsed page.
     *
     * @param root the JSON document
     * @param response the response it came from
     */
    record Page(JsonNode root, UpstreamResponse response) {}
}
