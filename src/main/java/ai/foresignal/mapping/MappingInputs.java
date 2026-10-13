package ai.foresignal.mapping;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.inbound.format.IngestRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Turns an inbound request, or a preview sample, into the JSON document a mapping reads (prompt 059).
 *
 * <p>JSON stays JSON. A form body ({@code application/x-www-form-urlencoded}) and a query string become a flat
 * object of strings, so paths read {@code $.tempf}; a repeated name keeps its first value. A request with no body
 * (a GET, such as the WU protocol or a GMC counter) is its query; a form body also takes the query parameters it
 * does not itself carry.
 */
public final class MappingInputs {

    /** The form media type. */
    public static final MediaType FORM = MediaType.APPLICATION_FORM_URLENCODED;

    private MappingInputs() {}

    /**
     * The document of {@code request}.
     *
     * @throws InvalidInputException when a JSON body cannot be parsed
     * @throws ForesignalException {@code 415} for a body that is neither JSON nor a form
     */
    public static JsonNode fromRequest(IngestRequest request, JsonMapper json) {
        if (!request.hasBody()) {
            return flat(request.query());
        }
        MediaType type = request.contentType();
        if (type != null && FORM.isCompatibleWith(type)) {
            ObjectNode form = parseForm(request.bodyText());
            request.query().forEach((name, values) -> {
                if (!form.has(name) && !values.isEmpty()) {
                    form.put(name, values.get(0));
                }
            });
            return form;
        }
        if (type != null && !isJson(type) && !MediaType.TEXT_PLAIN.isCompatibleWith(type)) {
            throw new ForesignalException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCodes.UNSUPPORTED_MEDIA_TYPE,
                    "This format reads JSON, a form body or a query string, not " + type + ".");
        }
        return readJson(request.bodyText(), json);
    }

    /**
     * The document of an OpenAPI {@code MappingSample}: {@code {contentType, body | query}}.
     *
     * @throws InvalidInputException when the sample is malformed
     */
    public static JsonNode fromSample(JsonNode sample, JsonMapper json) {
        if (sample == null || !sample.isObject()) {
            throw new InvalidInputException("sample must be an object with contentType and body or query.");
        }
        JsonNode contentTypeNode = sample.get("contentType");
        if (contentTypeNode == null || !contentTypeNode.isString()) {
            throw new InvalidInputException("sample.contentType is required.");
        }
        MediaType type;
        try {
            type = MediaType.parseMediaType(contentTypeNode.asString());
        } catch (org.springframework.util.InvalidMimeTypeException e) {
            throw new InvalidInputException("sample.contentType is not a media type.");
        }
        Map<String, List<String>> query = new LinkedHashMap<>();
        JsonNode queryNode = sample.get("query");
        if (queryNode != null && queryNode.isString()) {
            parseForm(queryNode.asString()).properties().forEach(e -> query.put(e.getKey(),
                    List.of(e.getValue().asString())));
        } else if (queryNode != null && queryNode.isObject()) {
            queryNode.properties().forEach(e -> query.put(e.getKey(), List.of(e.getValue().asString())));
        } else if (queryNode != null && !queryNode.isNull()) {
            throw new InvalidInputException("sample.query must be a string, an object or null.");
        }
        JsonNode body = sample.get("body");
        if (body == null || body.isNull() || (body.isString() && body.asString().isEmpty())) {
            if (query.isEmpty()) {
                throw new InvalidInputException("sample needs a body or a query.");
            }
            return flat(query);
        }
        if (FORM.isCompatibleWith(type)) {
            if (!body.isString()) {
                throw new InvalidInputException("A form sample's body must be the raw form string.");
            }
            ObjectNode form = parseForm(body.asString());
            query.forEach((name, values) -> {
                if (!form.has(name)) {
                    form.put(name, values.get(0));
                }
            });
            return form;
        }
        if (body.isString()) {
            return readJson(body.asString(), json);
        }
        return body;
    }

    /** {@code application/x-www-form-urlencoded} text as a flat object of strings (first value wins). */
    public static ObjectNode parseForm(String text) {
        ObjectNode out = JsonNodeFactory.instance.objectNode();
        if (text == null || text.isEmpty()) {
            return out;
        }
        String raw = text.startsWith("?") ? text.substring(1) : text;
        for (String pair : raw.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String name = decode(eq < 0 ? pair : pair.substring(0, eq));
            String value = eq < 0 ? "" : decode(pair.substring(eq + 1));
            if (!name.isEmpty() && !out.has(name)) {
                out.put(name, value);
            }
        }
        return out;
    }

    /** A query map as a flat object of strings (first value of each name). */
    public static ObjectNode flat(Map<String, List<String>> query) {
        ObjectNode out = JsonNodeFactory.instance.objectNode();
        if (query != null) {
            query.forEach((name, values) -> {
                if (values != null && !values.isEmpty() && !out.has(name)) {
                    out.put(name, values.get(0));
                }
            });
        }
        return out;
    }

    private static JsonNode readJson(String text, JsonMapper json) {
        try {
            JsonNode node = json.readTree(text);
            if (node == null || node.isMissingNode()) {
                throw new InvalidInputException("The body is empty.");
            }
            return node;
        } catch (JacksonException e) {
            throw new InvalidInputException("The body is not valid JSON.", e);
        }
    }

    private static boolean isJson(MediaType type) {
        return type.isCompatibleWith(MediaType.APPLICATION_JSON) || type.getSubtype().endsWith("+json");
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
    }
}
