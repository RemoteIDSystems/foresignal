package ai.foresignal.config;

import ai.foresignal.security.restrict.PathKeyFilter;
import java.util.Map;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

/**
 * Spring Boot's error attributes with any key-in-path segment redacted (prompt 050): an error that
 * reaches the container's {@code /error} page reports the original request URI as {@code path}, and
 * for a {@code /v1/k/{key}/…} request that URI carries the key. {@link PathKeyFilter#redact} keeps
 * only the key's first eight characters ({@code /v1/k/fsk_AbCd…/…}).
 *
 * <p>Foresignal's own problem responses never carry the key: {@code PathKeyFilter}'s refusals have no
 * {@code instance}, and everything after the forward sees the canonical path.
 */
@Component
public class RedactingErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> attributes = super.getErrorAttributes(webRequest, options);
        Object path = attributes.get("path");
        if (path instanceof String text) {
            attributes.put("path", PathKeyFilter.redact(text));
        }
        return attributes;
    }
}
