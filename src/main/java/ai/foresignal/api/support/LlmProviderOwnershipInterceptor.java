package ai.foresignal.api.support;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.llm.LlmProviderRepository;
import ai.foresignal.llm.LlmProviderScope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Keeps every operation on one LLM provider ({@code /v1/llm/providers/{providerId}} and everything below it) to the
 * callers it belongs to (prompt R2-011, {@link LlmProviderScope}).
 *
 * <p>Another user's personal provider is {@code 404}, exactly as an unknown one. A shared provider can be read
 * ({@code getLlmProvider}) by every caller of the provider operations; changing, deleting or testing it, listing its
 * models and reading its budget need {@code llm.shared.manage} ({@code 403 PERMISSION_DENIED} otherwise), because
 * those spend the shared budget or show the instance's spend. API tokens and sessions holding {@code users.manage}
 * pass.
 */
@Configuration
public class LlmProviderOwnershipInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    /** {@code /v1/llm/providers/{id}} and {@code /v1/llm/providers/{id}/…}. */
    static final Pattern PROVIDER_PATH = Pattern.compile("^/v1/llm/providers/([^/]+)(/.*)?$");

    private final ObjectProvider<LlmProviderRepository> providers;

    /**
     * Creates the interceptor.
     *
     * @param providers the provider store (absent in web slices without persistence, where nothing is checked)
     */
    public LlmProviderOwnershipInterceptor(ObjectProvider<LlmProviderRepository> providers) {
        this.providers = providers;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/v1/llm/providers/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Matcher m = PROVIDER_PATH.matcher(TriggerOwnershipInterceptor.path(request));
        if (!m.matches()) {
            return true;
        }
        LlmProviderScope scope = LlmProviderScope.of(request);
        if (scope.all()) {
            return true;
        }
        LlmProviderRepository repository = providers.getIfAvailable();
        if (repository == null) {
            return true;
        }
        String id = m.group(1);
        boolean read = m.group(2) == null
                && (HttpMethod.GET.matches(request.getMethod()) || HttpMethod.HEAD.matches(request.getMethod()));
        repository.findById(id).ifPresent(provider -> {
            String owner = provider.getOwnerPrincipalId();
            if (!scope.sees(owner)) {
                throw NotFoundException.of("LLM provider", id);
            }
            if (!read && !scope.manages(owner)) {
                throw LlmProviderScope.sharedDenied();
            }
        });
        return true;
    }
}
