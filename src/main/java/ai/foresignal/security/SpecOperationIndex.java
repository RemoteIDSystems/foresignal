package ai.foresignal.security;

import ai.foresignal.system.ApiVersion;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The operations of the authored contract ({@code docs/OpenAPI.json}, on the classpath as
 * {@link ApiVersion#RESOURCE}), indexed for two questions prompt 023 asks at run time:
 *
 * <ul>
 *   <li>which {@code operationId} a request is (the audit log's {@code operation_id});
 *   <li>whether an {@code allowed_endpoints} pattern matches at least one operation a scope can
 *       reach (token restriction validation).
 * </ul>
 *
 * <p>The top-level {@code webhooks} are requests Foresignal sends, so they are not indexed.
 */
@Component
public class SpecOperationIndex {

    private static final Logger log = LoggerFactory.getLogger(SpecOperationIndex.class);

    private static final Set<String> METHODS = Set.of("get", "head", "post", "put", "patch", "delete", "options");

    /**
     * One operation.
     *
     * @param method the HTTP method
     * @param path the path template, for example {@code /v1/tokens/{tokenId}}
     * @param pattern the template as an Ant pattern ({@code /v1/tokens/*})
     * @param operationId the operation id
     * @param scope {@code x-foresignal-scope}
     */
    public record SpecOperation(HttpMethod method, String path, String pattern, String operationId, String scope) {

        /** The template with every variable replaced by a sample segment. */
        public String samplePath() {
            return path.replaceAll("\\{[^/}]+}", "sample");
        }

        /** True when a token of {@code tokenScope} may call this operation. */
        public boolean reachableBy(Scope tokenScope) {
            return switch (scope == null ? "" : scope) {
                case "open" -> true;
                case "read", "agent", "admin" -> tokenScope.includes(Scope.fromWireValue(scope));
                default -> false;
            };
        }
    }

    private final AntPathMatcher matcher = new AntPathMatcher();
    private final List<SpecOperation> operations;

    /** Loads the authored document from the classpath. */
    public SpecOperationIndex(JsonMapper json) {
        this.operations = load(json);
    }

    /** Every indexed operation. */
    public List<SpecOperation> operations() {
        return operations;
    }

    /**
     * The operation {@code method} on {@code path} is, preferring the most specific template
     * ({@code /v1/sources/health} over {@code /v1/sources/{sourceId}}).
     *
     * @return the operation id, or null when no operation matches
     */
    public String operationId(HttpMethod method, String path) {
        String normalized = AccessPolicy.normalize(path);
        Comparator<String> specificity = matcher.getPatternComparator(normalized);
        SpecOperation best = null;
        for (SpecOperation op : operations) {
            if (!op.method().equals(method) || !matcher.match(op.pattern(), normalized)) {
                continue;
            }
            if (best == null || specificity.compare(op.pattern(), best.pattern()) < 0) {
                best = op;
            }
        }
        return best == null ? null : best.operationId();
    }

    /**
     * True when the restriction pattern {@code methodPattern} ({@code "*"} or a method) and
     * {@code pathPattern} (Ant style) match at least one operation a {@code scope} token can reach.
     */
    public boolean matchesReachable(String methodPattern, String pathPattern, Scope scope) {
        for (SpecOperation op : operations) {
            if (!"*".equals(methodPattern) && !op.method().name().equals(methodPattern)) {
                continue;
            }
            if (op.reachableBy(scope)
                    && (matcher.match(pathPattern, op.samplePath()) || matcher.match(pathPattern, op.path()))) {
                return true;
            }
        }
        return false;
    }

    private static List<SpecOperation> load(JsonMapper json) {
        ClassPathResource resource = new ClassPathResource(ApiVersion.RESOURCE);
        if (!resource.exists()) {
            log.warn("{} is not on the classpath; operation ids and endpoint restrictions cannot be resolved",
                    ApiVersion.RESOURCE);
            return List.of();
        }
        try (InputStream in = resource.getInputStream()) {
            JsonNode paths = json.readTree(in).path("paths");
            List<SpecOperation> out = new ArrayList<>();
            for (String path : paths.propertyNames()) {
                JsonNode item = paths.get(path);
                for (String method : item.propertyNames()) {
                    if (!METHODS.contains(method)) {
                        continue;
                    }
                    JsonNode op = item.get(method);
                    out.add(new SpecOperation(
                            HttpMethod.valueOf(method.toUpperCase(Locale.ROOT)),
                            path,
                            path.replaceAll("\\{[^/}]+}", "*"),
                            op.path("operationId").asString(""),
                            op.path("x-foresignal-scope").asString(null)));
                }
            }
            return List.copyOf(out);
        } catch (IOException | RuntimeException e) {
            log.warn("Could not index {}: {}", ApiVersion.RESOURCE, e.toString());
            return List.of();
        }
    }
}
