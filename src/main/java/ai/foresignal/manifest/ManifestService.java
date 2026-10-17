package ai.foresignal.manifest;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.security.Ulids;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Records and reads analysis manifests (prompt 116). {@link #record(ManifestRequest)} builds the manifest
 * content (component and version, build, configuration and its hash, registry hashes, {@code asOfSeq}, inputs
 * and model metadata), hashes its canonical JSON and stores it once: an identical manifest returns the id
 * already stored.
 *
 * <p>Inputs are stored as explicit refs when there are at most {@value #MAX_EXPLICIT_REFS}; above that only the
 * query definition, the count and the SHA-256 of the ordered refs are kept.
 */
@Service
public class ManifestService {

    /** The most input refs stored explicitly. */
    public static final int MAX_EXPLICIT_REFS = 10_000;

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final RegistryHashes registries;
    private final BuildInfo build;
    private final Clock clock;

    /** Creates the service. */
    public ManifestService(JdbcTemplate jdbc, JsonMapper json, RegistryHashes registries, BuildInfo build, Clock clock) {
        this.jdbc = jdbc;
        this.json = json;
        this.registries = registries;
        this.build = build;
        this.clock = clock;
    }

    /**
     * Stores a manifest unless an identical one exists.
     *
     * @return the manifest id
     */
    public String record(ManifestRequest request) {
        Map<String, Object> content = content(request);
        String contentHash = CanonicalJson.hash(content, json);
        List<String> existing = jdbc.queryForList("SELECT id FROM analysis_manifest WHERE content_hash = ?",
                String.class, contentHash);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        Instant now = clock.instant();
        String id = Ulids.next(now);
        jdbc.update("INSERT INTO analysis_manifest (id, component, component_version, build, config, config_hash,"
                        + " registries, asof_seq, inputs, model, content_hash, created_at) VALUES (?, ?, ?,"
                        + " CAST(? AS jsonb), CAST(? AS jsonb), ?, CAST(? AS jsonb), ?, CAST(? AS jsonb),"
                        + " CAST(? AS jsonb), ?, ?) ON CONFLICT (content_hash) DO NOTHING",
                id, request.component(), request.componentVersion(), json.writeValueAsString(content.get("build")),
                json.writeValueAsString(content.get("config")), content.get("configHash"),
                json.writeValueAsString(content.get("registries")), request.asOfSeq(),
                json.writeValueAsString(content.get("inputs")),
                request.model() == null ? null : json.writeValueAsString(content.get("model")), contentHash,
                Timestamp.from(now));
        return jdbc.queryForObject("SELECT id FROM analysis_manifest WHERE content_hash = ?", String.class, contentHash);
    }

    /**
     * The manifest content that is hashed: everything except the id and creation time.
     */
    Map<String, Object> content(ManifestRequest request) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("component", request.component());
        content.put("componentVersion", request.componentVersion());
        Map<String, Object> buildMap = new LinkedHashMap<>();
        buildMap.put("version", build.version());
        buildMap.put("gitCommit", build.gitCommit());
        content.put("build", buildMap);
        content.put("config", CanonicalJson.canonical(json.convertValue(request.config(), Object.class)));
        content.put("configHash", CanonicalJson.hash(request.config(), json));
        content.put("registries", registries.hashes(request.registries()));
        content.put("asOfSeq", request.asOfSeq());
        content.put("inputs", inputs(request.inputRefs(), request.inputQuery()));
        content.put("model", request.model() == null ? null
                : CanonicalJson.canonical(json.convertValue(request.model(), Object.class)));
        return content;
    }

    /** The {@code inputs} object: explicit refs (≤ 10,000) or the query plus count and hash. */
    Map<String, Object> inputs(List<String> refs, Map<String, Object> query) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (refs != null) {
            String joined = String.join("\n", refs);
            out.put("refs", refs.size() <= MAX_EXPLICIT_REFS ? refs : null);
            out.put("query", query == null ? null : CanonicalJson.canonical(json.convertValue(query, Object.class)));
            out.put("count", (long) refs.size());
            out.put("hash", CanonicalJson.sha256(joined));
        } else {
            out.put("refs", null);
            out.put("query", query == null ? null : CanonicalJson.canonical(json.convertValue(query, Object.class)));
            out.put("count", 0L);
            out.put("hash", CanonicalJson.hash(query == null ? Map.of() : query, json));
        }
        return out;
    }

    /** A stored manifest. */
    public Optional<Manifest> find(String id) {
        List<Manifest> rows = jdbc.query("SELECT id, component, component_version, build::text, config::text,"
                + " config_hash, registries::text, asof_seq, inputs::text, model::text, content_hash, created_at"
                + " FROM analysis_manifest WHERE id = ?", (rs, i) -> new Manifest(rs.getString(1), rs.getString(2),
                rs.getString(3), map(rs.getString(4)), map(rs.getString(5)), rs.getString(6), map(rs.getString(7)),
                (Long) rs.getObject(8), map(rs.getString(9)), rs.getString(10) == null ? null : map(rs.getString(10)),
                rs.getString(11), rs.getObject(12, java.time.OffsetDateTime.class).toInstant()), id);
        return rows.stream().findFirst();
    }

    /**
     * A stored manifest.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_MANIFEST}
     */
    public Manifest get(String id) {
        return find(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_MANIFEST, "No such manifest: " + id));
    }

    private Map<String, Object> map(String text) {
        return text == null ? new LinkedHashMap<>() : json.readValue(text, MAP);
    }

    /**
     * A stored manifest.
     *
     * @param id the id
     * @param component the component
     * @param componentVersion its version
     * @param build {@code {version, gitCommit}}
     * @param config the configuration
     * @param configHash its hash
     * @param registries registry name → SHA-256
     * @param asOfSeq the {@code asOfSeq}, or null
     * @param inputs {@code {refs, query, count, hash}}
     * @param model LLM metadata, or null
     * @param contentHash the content hash
     * @param createdAt when first stored
     */
    public record Manifest(String id, String component, String componentVersion, Map<String, Object> build,
            Map<String, Object> config, String configHash, Map<String, Object> registries, Long asOfSeq,
            Map<String, Object> inputs, Map<String, Object> model, String contentHash, Instant createdAt) {

        /** The explicit input refs, or empty when only a query was recorded. */
        @SuppressWarnings("unchecked")
        public List<String> refs() {
            Object refs = inputs == null ? null : inputs.get("refs");
            return refs instanceof List<?> list ? (List<String>) list : List.of();
        }

        /** The {@code AnalysisManifest} API shape. */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("id", id);
            out.put("component", component);
            out.put("componentVersion", componentVersion);
            out.put("build", build);
            out.put("config", config);
            out.put("configHash", configHash);
            out.put("registries", registries);
            out.put("asOfSeq", asOfSeq);
            out.put("inputs", inputs);
            out.put("model", model);
            out.put("contentHash", contentHash);
            out.put("createdAt", createdAt);
            return out;
        }
    }
}
