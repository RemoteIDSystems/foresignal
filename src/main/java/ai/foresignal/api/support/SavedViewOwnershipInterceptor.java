package ai.foresignal.api.support;

import ai.foresignal.view.SavedViewScope;
import ai.foresignal.view.SavedViewService;
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
 * Keeps every operation on one saved view ({@code /v1/views/{viewId}} and everything below it: records, tiles,
 * TileJSON, the stream) to the callers it belongs to (prompt R2-012, {@link SavedViewScope}). A view is named by id or
 * slug.
 *
 * <p>Another user's view is {@code 404 UNKNOWN_VIEW}, exactly as an unknown one, so an id or slug never reveals that
 * another user's view exists. A shared view can be read and rendered by everyone who could before; replacing or
 * deleting it needs {@code users.manage} ({@code 403 PERMISSION_DENIED} otherwise). API tokens and sessions holding
 * {@code users.manage} pass. The check sits in front of every controller under {@code /v1/views}, so an operation
 * added later is covered without a change here.
 */
@Configuration
public class SavedViewOwnershipInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    /** {@code /v1/views/{viewId}} and {@code /v1/views/{viewId}/…}. */
    static final Pattern VIEW_PATH = Pattern.compile("^/v1/views/([^/]+)(/.*)?$");

    private final ObjectProvider<SavedViewService> views;

    /**
     * Creates the interceptor.
     *
     * @param views the saved view service (absent in web slices without persistence, where nothing is checked)
     */
    public SavedViewOwnershipInterceptor(ObjectProvider<SavedViewService> views) {
        this.views = views;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/v1/views/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Matcher m = VIEW_PATH.matcher(TriggerOwnershipInterceptor.path(request));
        if (!m.matches()) {
            return true;
        }
        SavedViewScope scope = SavedViewScope.of(request);
        if (scope.all()) {
            return true;
        }
        SavedViewService service = views.getIfAvailable();
        if (service == null) {
            return true;
        }
        String id = m.group(1);
        boolean read = HttpMethod.GET.matches(request.getMethod()) || HttpMethod.HEAD.matches(request.getMethod());
        service.find(id).ifPresent(view -> {
            String owner = view.getOwnerPrincipalId();
            if (!scope.sees(owner)) {
                throw SavedViewService.unknown(id);
            }
            if (!read && !scope.manages(owner)) {
                throw SavedViewScope.sharedDenied();
            }
        });
        return true;
    }
}
