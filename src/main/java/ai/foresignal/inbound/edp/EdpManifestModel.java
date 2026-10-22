package ai.foresignal.inbound.edp;

import ai.foresignal.settings.Durations;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * A validated EDP manifest, read into typed fields (docs/External-Data-Protocol.md §2).
 *
 * <p>Built only from a manifest that passed {@code EdpManifestValidator}; unknown members are
 * ignored, as the protocol requires for forward compatibility.
 *
 * @param raw the manifest as submitted
 * @param protocol the {@code protocol} string
 * @param minor its minor version
 * @param providerId {@code source.id}
 * @param name {@code source.name}
 * @param description {@code source.description}, or null
 * @param publisherName {@code source.publisher.name}
 * @param homepage {@code source.homepage}, or null
 * @param licenceId {@code source.licence.id}, or null
 * @param licenceUrl {@code source.licence.url}, or null
 * @param attribution {@code source.licence.attribution}, or null
 * @param redistribution {@code allowed}, {@code local-only} or {@code unknown} (the default)
 * @param termsSummary {@code source.terms.summary}, or null
 * @param termsUrl {@code source.terms.url}, or null
 * @param accessRoute {@code source.access.route}, or null when absent
 * @param accessFragility {@code source.access.fragility}, or null
 * @param originatorId {@code source.originator.id}
 * @param originatorClass {@code source.originator.class}
 * @param aggregates {@code source.originator.aggregates}
 * @param privateSource {@code source.private}
 * @param push {@code transport.push}
 * @param pull {@code transport.pull}, or null
 * @param search {@code transport.search}, or null
 * @param analysisRequest {@code transport.analysisRequest} (prompt 155), or null
 * @param streams the streams, in manifest order
 */
public record EdpManifestModel(
        JsonNode raw,
        String protocol,
        int minor,
        String providerId,
        String name,
        String description,
        String publisherName,
        String homepage,
        String licenceId,
        String licenceUrl,
        String attribution,
        String redistribution,
        String termsSummary,
        String termsUrl,
        String accessRoute,
        String accessFragility,
        String originatorId,
        String originatorClass,
        List<String> aggregates,
        boolean privateSource,
        boolean push,
        Pull pull,
        Search search,
        AnalysisRequest analysisRequest,
        List<Stream> streams) {

    /** Copies the lists. */
    public EdpManifestModel {
        aggregates = aggregates == null ? List.of() : List.copyOf(aggregates);
        streams = streams == null ? List.of() : List.copyOf(streams);
    }

    /** {@code ext-{providerId}}. */
    public String sourceId() {
        return EdpProtocol.sourceId(providerId);
    }

    /** The stream {@code id}, or null. */
    public Stream stream(String id) {
        if (id == null) {
            return null;
        }
        for (Stream stream : streams) {
            if (stream.id().equals(id)) {
                return stream;
            }
        }
        return null;
    }

    /**
     * Reads a manifest that already passed validation.
     *
     * @throws IllegalArgumentException when a member the validator guarantees is missing
     */
    public static EdpManifestModel parse(JsonNode manifest) {
        if (manifest == null || !manifest.isObject()) {
            throw new IllegalArgumentException("A manifest must be a JSON object");
        }
        JsonNode source = manifest.path("source");
        JsonNode licence = source.path("licence");
        JsonNode terms = source.path("terms");
        JsonNode access = source.path("access");
        JsonNode originator = source.path("originator");
        JsonNode transport = manifest.path("transport");
        String protocol = text(manifest, "protocol");
        List<Stream> streams = new ArrayList<>();
        for (JsonNode stream : manifest.path("streams")) {
            streams.add(Stream.parse(stream));
        }
        String redistribution = text(licence, "redistribution");
        return new EdpManifestModel(
                manifest,
                protocol,
                EdpProtocol.minor(protocol),
                required(source, "id"),
                required(source, "name"),
                text(source, "description"),
                text(source.path("publisher"), "name"),
                text(source, "homepage"),
                text(licence, "id"),
                text(licence, "url"),
                text(licence, "attribution"),
                redistribution == null ? "unknown" : redistribution,
                text(terms, "summary"),
                text(terms, "url"),
                text(access, "route"),
                text(access, "fragility"),
                text(originator, "id"),
                text(originator, "class"),
                strings(originator.path("aggregates")),
                source.path("private").asBoolean(false),
                transport.path("push").asBoolean(false),
                Pull.parse(transport.path("pull")),
                Search.parse(transport.path("search")),
                AnalysisRequest.parse(transport.path("analysisRequest")),
                streams);
    }

    /**
     * One stream (docs/External-Data-Protocol.md §2, {@code streams[]}).
     *
     * @param raw the stream object
     * @param id the stream id
     * @param mode {@code layer} or {@code atomic}
     * @param itemType the item type it carries
     * @param kind the record kind
     * @param title the stream title, or null
     * @param geometry {@code Point}, {@code LineString}, {@code Polygon}, {@code MultiPolygon},
     *     {@code any} (the default) or {@code none}
     * @param update {@code incremental} (the default) or {@code snapshot}
     * @param snapshotScope the item property naming a snapshot scope, or null for the whole stream
     * @param payloadSchema the JSON Schema of item properties, or null
     * @param titleField {@code fields.title}, or null
     * @param significant {@code fields.significant}
     * @param volatileFields {@code fields.volatile}
     * @param observedAtReceived true when {@code fields.observedAtMode} is {@code received}
     * @param ttl the record TTL, or null
     * @param retentionClass the analytic retention class ({@code event} by default)
     * @param unit the observation unit, or null
     * @param documentType the document type, or null
     * @param presentation the layer presentation hints, or null
     */
    public record Stream(
            JsonNode raw,
            String id,
            String mode,
            String itemType,
            String kind,
            String title,
            String geometry,
            String update,
            String snapshotScope,
            JsonNode payloadSchema,
            String titleField,
            List<String> significant,
            List<String> volatileFields,
            boolean observedAtReceived,
            Duration ttl,
            String retentionClass,
            String unit,
            String documentType,
            JsonNode presentation) {

        /** Copies the lists. */
        public Stream {
            significant = significant == null ? List.of() : List.copyOf(significant);
            volatileFields = volatileFields == null ? List.of() : List.copyOf(volatileFields);
        }

        /** True for an atomic stream. */
        public boolean atomic() {
            return "atomic".equals(mode);
        }

        /** True for a snapshot stream. */
        public boolean snapshot() {
            return "snapshot".equals(update);
        }

        static Stream parse(JsonNode stream) {
            JsonNode fields = stream.path("fields");
            String ttl = text(stream, "ttl");
            String geometry = text(stream, "geometry");
            String update = text(stream, "update");
            String retention = text(stream, "retentionClass");
            JsonNode schema = stream.get("payloadSchema");
            JsonNode presentation = stream.get("presentation");
            return new Stream(
                    stream,
                    required(stream, "id"),
                    required(stream, "mode"),
                    required(stream, "itemType"),
                    required(stream, "kind"),
                    text(stream, "title"),
                    geometry == null ? "any" : geometry,
                    update == null ? "incremental" : update,
                    text(stream, "snapshotScope"),
                    schema == null || schema.isNull() ? null : schema,
                    text(fields, "title"),
                    strings(fields.path("significant")),
                    strings(fields.path("volatile")),
                    "received".equals(text(fields, "observedAtMode")),
                    ttl == null ? null : Durations.parse(ttl),
                    retention == null ? "event" : retention,
                    text(stream, "unit"),
                    text(stream, "documentType"),
                    presentation == null || presentation.isNull() ? null : presentation);
        }
    }

    /**
     * {@code transport.pull}.
     *
     * @param itemsUrl the provider's items endpoint
     * @param intervalSeconds the poll interval, at least 60 (300 by default)
     * @param auth {@code none} (the default), {@code bearer}, {@code header} or {@code basic}
     * @param header the header name for {@code auth: header}, or null
     */
    public record Pull(String itemsUrl, int intervalSeconds, String auth, String header) {

        static Pull parse(JsonNode pull) {
            if (pull == null || !pull.isObject()) {
                return null;
            }
            String auth = text(pull, "auth");
            return new Pull(required(pull, "itemsUrl"), pull.path("intervalSeconds").asInt(300),
                    auth == null ? "none" : auth, text(pull, "header"));
        }
    }

    /**
     * {@code transport.search} (stored for prompt 137's cue service).
     *
     * @param url the search endpoint
     * @param auth the auth style, as for pull
     * @param header the header name for {@code auth: header}, or null
     */
    public record Search(String url, String auth, String header) {

        static Search parse(JsonNode search) {
            if (search == null || !search.isObject()) {
                return null;
            }
            String auth = text(search, "auth");
            return new Search(required(search, "url"), auth == null ? "none" : auth, text(search, "header"));
        }
    }

    /**
     * {@code transport.analysisRequest} (prompt 155): the endpoint Foresignal POSTs an on-demand forecast
     * request to ({@code {questionId / seriesRef, horizon, correlationId, callbackUrl, asOf}}). A provider that
     * answers synchronously returns the forecast in the HTTP response; one that answers asynchronously posts it
     * to {@code callbackUrl}. This generalizes the formerly inert {@code transport.search}.
     *
     * @param url the analysis-request endpoint
     * @param auth the auth style, as for pull
     * @param header the header name for {@code auth: header}, or null
     */
    public record AnalysisRequest(String url, String auth, String header) {

        static AnalysisRequest parse(JsonNode node) {
            if (node == null || !node.isObject()) {
                return null;
            }
            String auth = text(node, "auth");
            return new AnalysisRequest(required(node, "url"), auth == null ? "none" : auth, text(node, "header"));
        }
    }

    static String text(JsonNode node, String name) {
        JsonNode value = node == null ? null : node.get(name);
        return value == null || value.isNull() || !value.isValueNode() ? null : value.asString();
    }

    static String required(JsonNode node, String name) {
        String value = text(node, name);
        if (value == null) {
            throw new IllegalArgumentException("The manifest has no " + name);
        }
        return value;
    }

    static List<String> strings(JsonNode array) {
        List<String> out = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode value : array) {
                if (value.isString()) {
                    out.add(value.asString());
                }
            }
        }
        return out;
    }
}
