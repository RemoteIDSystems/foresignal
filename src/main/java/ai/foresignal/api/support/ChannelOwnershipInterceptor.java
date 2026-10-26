package ai.foresignal.api.support;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.notify.ChannelScope;
import ai.foresignal.store.NotificationChannelRepository;
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
 * Keeps every operation on one channel ({@code /v1/channels/{channelId}} and everything below it) to the callers the
 * channel belongs to (prompt R2-009, {@link ChannelScope}).
 *
 * <p>Another user's channel is {@code 404}, exactly as an unknown one, so a channel id never reveals that another
 * user's channel exists. A global channel is visible to every caller of the channel operations; changing, testing or
 * republishing one without {@code channels.global} is {@code 403 PERMISSION_DENIED}. API tokens and sessions holding
 * {@code users.manage} pass. Sitting in front of every controller that takes a {@code channelId}, it covers an
 * operation added later without a change here.
 */
@Configuration
public class ChannelOwnershipInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    /** {@code /v1/channels/{uuid}} and {@code /v1/channels/{uuid}/…}. */
    static final Pattern CHANNEL_PATH = Pattern.compile(
            "^/v1/channels/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})(?:/.*)?$");

    private final ObjectProvider<NotificationChannelRepository> channels;

    /**
     * Creates the interceptor.
     *
     * @param channels the channel store (absent in web slices without persistence, where nothing is checked)
     */
    public ChannelOwnershipInterceptor(ObjectProvider<NotificationChannelRepository> channels) {
        this.channels = channels;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/v1/channels/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Optional<UUID> channelId = channelId(TriggerOwnershipInterceptor.path(request));
        if (channelId.isEmpty()) {
            return true;
        }
        ChannelScope scope = ChannelScope.of(request);
        if (scope.all()) {
            return true;
        }
        NotificationChannelRepository repository = channels.getIfAvailable();
        if (repository == null) {
            return true;
        }
        boolean read = HttpMethod.GET.matches(request.getMethod()) || HttpMethod.HEAD.matches(request.getMethod());
        repository.findById(channelId.get()).ifPresent(channel -> {
            String owner = channel.getOwnerPrincipalId();
            if (!scope.sees(owner)) {
                throw NotFoundException.of("Channel", channelId.get());
            }
            if (!read && !scope.manages(owner)) {
                throw ChannelScope.globalDenied();
            }
        });
        return true;
    }

    /**
     * The channel id a path names, if it is a single-channel path.
     *
     * @param path the path without the context path
     * @return the channel id, or empty
     */
    static Optional<UUID> channelId(String path) {
        Matcher m = CHANNEL_PATH.matcher(path);
        if (!m.matches()) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(m.group(1)));
    }
}
