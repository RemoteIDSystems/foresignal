package ai.foresignal.api.support;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.security.user.OwnerScope;
import ai.foresignal.store.TriggerRuleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Keeps every operation on one trigger rule ({@code /v1/triggers/{ruleId}} and everything below it, the KML export
 * {@code /v1/triggers/{ruleId}.kml} included) to the callers allowed to see the rule (prompt R2-008).
 *
 * <p>A signed-in user without {@code users.manage} reaches only their own rules; anyone else who is not an API token
 * (anonymous under {@code security.openReads}, a feed or tile key) only the shared ones. Any other rule is
 * {@code 404}, exactly as an unknown one, so a rule id never reveals that another user's rule exists. API tokens and
 * sessions holding {@code users.manage} pass ({@link OwnerScope#all()}). The check sits in front of every controller
 * that takes a {@code ruleId}, so an operation added later is covered without a change here.
 */
@Configuration
public class TriggerOwnershipInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    /** {@code /v1/triggers/{uuid}}, {@code /v1/triggers/{uuid}/…} and {@code /v1/triggers/{uuid}.kml}. */
    static final Pattern RULE_PATH = Pattern.compile(
            "^/v1/triggers/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})(?:\\.kml|/.*)?$");

    private final ObjectProvider<TriggerRuleRepository> rules;

    /**
     * Creates the interceptor.
     *
     * @param rules the rule store (absent in web slices without persistence, where nothing is checked)
     */
    public TriggerOwnershipInterceptor(ObjectProvider<TriggerRuleRepository> rules) {
        this.rules = rules;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/v1/triggers/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Optional<UUID> ruleId = ruleId(path(request));
        if (ruleId.isEmpty()) {
            return true;
        }
        OwnerScope scope = OwnerScope.of(request);
        if (scope.all()) {
            return true;
        }
        TriggerRuleRepository repository = rules.getIfAvailable();
        if (repository == null) {
            return true;
        }
        boolean read = HttpMethod.GET.matches(request.getMethod()) || HttpMethod.HEAD.matches(request.getMethod());
        repository.findById(ruleId.get()).ifPresent(rule -> {
            String owner = rule.getOwnerPrincipalId();
            if (read ? !scope.sees(owner) : !scope.manages(owner)) {
                throw NotFoundException.of("Trigger rule", ruleId.get());
            }
        });
        return true;
    }

    /**
     * The rule id a path names, if it is a single-rule path.
     *
     * @param path the path without the context path
     * @return the rule id, or empty
     */
    static Optional<UUID> ruleId(String path) {
        Matcher m = RULE_PATH.matcher(path);
        if (!m.matches()) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(m.group(1)));
    }

    /** The request path without the context path. */
    static String path(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        return context != null && !context.isEmpty() && uri.startsWith(context) ? uri.substring(context.length()) : uri;
    }
}
