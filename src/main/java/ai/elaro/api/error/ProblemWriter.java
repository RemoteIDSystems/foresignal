package ai.elaro.api.error;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes an RFC 9457 problem straight to the response.
 *
 * <p>Servlet filters run before Spring MVC's exception handling, so a filter that rejects a request
 * cannot rely on {@link GlobalExceptionHandler}. This keeps filter responses identical in shape to
 * controller ones.
 */
@Component
public class ProblemWriter {

    private final JsonMapper jsonMapper;

    public ProblemWriter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /** Writes {@code problem} as {@code application/problem+json} with its own status. */
    public void write(HttpServletResponse response, ProblemDetail problem) throws IOException {
        response.setStatus(problem.getStatus());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonMapper.writeValueAsString(problem));
        response.flushBuffer();
    }

    /** Builds a problem for {@code status} and {@code code}, with the conventional type URI. */
    public ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(status.getReasonPhrase());
        problem.setDetail(detail);
        problem.setType(ErrorCodes.typeUri(code));
        problem.setProperty("code", code);
        return problem;
    }
}
