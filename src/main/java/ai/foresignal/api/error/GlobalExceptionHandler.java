package ai.foresignal.api.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns every exception into an RFC 9457 problem document (Architecture §10).
 *
 * <p>Extending {@link ResponseEntityExceptionHandler} covers Spring MVC's own exceptions — wrong
 * method, unreadable body, type mismatch — and lets each one be given Foresignal's {@code code} and
 * {@code type}. Declaring this bean also makes Boot's own problem-details configuration back off
 * (it is {@code @ConditionalOnMissingBean(ResponseEntityExceptionHandler.class)}), so there is
 * exactly one handler deciding error shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Ties a 500 body to the stack trace in the log without exposing the trace to the caller. */
    static final String CORRELATION_ID_PROPERTY = "correlationId";

    // ---------- Foresignal's own exceptions ----------

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidation(ValidationException e, WebRequest request) {
        ProblemDetail problem = problem(e.getStatus(), e.getCode(), e.getMessage());
        e.getProperties().forEach(problem::setProperty);
        if (!e.getErrors().isEmpty()) {
            problem.setProperty("errors", e.getErrors());
        }
        return handleExceptionInternal(e, problem, new HttpHeaders(), e.getStatus(), request);
    }

    @ExceptionHandler(ForesignalException.class)
    public ResponseEntity<Object> handleForesignal(ForesignalException e, WebRequest request) {
        ProblemDetail problem = problem(e.getStatus(), e.getCode(), e.getMessage());
        e.getProperties().forEach(problem::setProperty);
        HttpHeaders headers = new HttpHeaders();
        e.getHeaders().forEach(headers::set);
        return handleExceptionInternal(e, problem, headers, e.getStatus(), request);
    }

    /**
     * An image refused by {@code ImageGuard} (prompt 043): {@code 400 VALIDATION_FAILED} with the
     * refusal in a {@code reason} property.
     */
    @ExceptionHandler(ai.foresignal.source.spi.ImageRejectedException.class)
    public ResponseEntity<Object> handleImageRejected(ai.foresignal.source.spi.ImageRejectedException e,
            WebRequest request) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_FAILED,
                "The image was rejected: " + e.reason());
        problem.setProperty("reason", e.tag());
        return handleExceptionInternal(e, problem, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * A multipart request over the container's limits (prompt 054, remote SatDump pushes):
     * {@code 413 MEDIA_TOO_LARGE}.
     */
    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            org.springframework.web.multipart.MaxUploadSizeExceededException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problem = problem(HttpStatus.CONTENT_TOO_LARGE, ErrorCodes.MEDIA_TOO_LARGE,
                "The upload is larger than the server accepts.");
        return handleExceptionInternal(e, problem, headers, HttpStatus.CONTENT_TOO_LARGE, request);
    }

    // ---------- Validation from the framework ----------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<FieldError> errors =
                e.getBindingResult().getFieldErrors().stream()
                        .map(
                                field ->
                                        new FieldError(
                                                field.getField(),
                                                field.getDefaultMessage() == null
                                                        ? "is not valid"
                                                        : field.getDefaultMessage()))
                        .toList();

        ProblemDetail problem =
                problem(
                        HttpStatus.BAD_REQUEST,
                        ErrorCodes.VALIDATION_FAILED,
                        "The request body is not valid.");
        problem.setProperty("errors", errors);
        return handleExceptionInternal(e, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    /** Validation on path variables and query parameters, which arrives as a different exception. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException e, WebRequest request) {

        List<FieldError> errors =
                e.getConstraintViolations().stream()
                        .map(v -> new FieldError(lastPathNode(v), v.getMessage()))
                        .toList();

        ProblemDetail problem =
                problem(
                        HttpStatus.BAD_REQUEST,
                        ErrorCodes.VALIDATION_FAILED,
                        "One or more parameters are not valid.");
        problem.setProperty("errors", errors);
        return handleExceptionInternal(e, problem, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    // ---------- Malformed input ----------

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        // Jackson's message can quote the offending payload, which may hold a credential, so it is
        // deliberately not passed through.
        ProblemDetail problem =
                problem(
                        HttpStatus.BAD_REQUEST,
                        ErrorCodes.MALFORMED_REQUEST,
                        "The request body could not be parsed as JSON.");
        return handleExceptionInternal(e, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * A bare {@code IllegalArgumentException} is treated as an internal error.
     *
     * <p>This used to be a 400, on the reasoning that parsers report bad input this way. The
     * problem is that the handler cannot tell a parser complaint from an
     * {@code IllegalArgumentException} thrown by a library deep inside a service: the second is a
     * bug, and reporting it as a validation failure hid it from the logs and misled the caller.
     * Input parsers now throw {@link InvalidInputException} instead, so anything still arriving
     * here is unexpected and is handled like any other unexpected exception.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(
            IllegalArgumentException e, WebRequest request) {
        return handleUnexpected(e, request);
    }

    // ---------- Anything unexpected ----------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception e, WebRequest request) {
        String correlationId = UUID.randomUUID().toString();

        // The caller gets the id; the log gets the id and the trace. That pairing is what makes a
        // bug report actionable without leaking internals in the response.
        log.error("Unhandled exception [correlationId={}]", correlationId, e);

        ProblemDetail problem =
                problem(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorCodes.INTERNAL_ERROR,
                        "Something went wrong. Quote correlation id " + correlationId + " when reporting it.");
        problem.setProperty(CORRELATION_ID_PROPERTY, correlationId);
        return handleExceptionInternal(
                e, problem, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // ---------- Shared shaping ----------

    /**
     * Gives Spring MVC's built-in problems Foresignal's {@code code} and {@code type}, so a client can
     * branch on {@code code} for every error rather than only Foresignal-raised ones.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception e,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        // Spring's own handlers (wrong method, unsupported media type, ...) pass a null body and
        // leave the superclass to fill it from the exception. Filling it here instead is what lets
        // those problems carry Foresignal's code and type too.
        if (body == null && e instanceof ErrorResponse errorResponse) {
            body = errorResponse.updateAndGetBody(getMessageSource(), LocaleContextHolder.getLocale());
        }
        if (body instanceof ProblemDetail problem) {
            Map<String, Object> properties = problem.getProperties();
            boolean alreadyShaped = properties != null && properties.containsKey("code");
            if (!alreadyShaped) {
                String code = codeForStatus(status);
                problem.setType(ErrorCodes.typeUri(code));
                problem.setProperty("code", code);
            }
        }
        return super.handleExceptionInternal(e, body, headers, status, request);
    }

    static String codeForStatus(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> ErrorCodes.MALFORMED_REQUEST;
            case 401 -> ErrorCodes.UNAUTHORIZED;
            case 403 -> ErrorCodes.FORBIDDEN;
            case 404 -> ErrorCodes.NOT_FOUND;
            case 405 -> ErrorCodes.METHOD_NOT_ALLOWED;
            case 406 -> ErrorCodes.NOT_ACCEPTABLE;
            case 413 -> ErrorCodes.PAYLOAD_TOO_LARGE;
            case 415 -> ErrorCodes.UNSUPPORTED_MEDIA_TYPE;
            case 429 -> ErrorCodes.BUDGET_EXCEEDED;
            default -> ErrorCodes.INTERNAL_ERROR;
        };
    }

    private static ProblemDetail problem(HttpStatusCode status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(
                status instanceof HttpStatus known ? known.getReasonPhrase() : "Error");
        problem.setDetail(detail);
        problem.setType(ErrorCodes.typeUri(code));
        problem.setProperty("code", code);
        return problem;
    }

    private static String lastPathNode(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int dot = path.lastIndexOf('.');
        return dot >= 0 && dot + 1 < path.length() ? path.substring(dot + 1) : path;
    }
}
