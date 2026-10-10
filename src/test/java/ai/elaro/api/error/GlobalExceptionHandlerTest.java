package ai.elaro.api.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Drives every branch of the handler through real MVC dispatch, so each assertion covers the
 * status, headers and serialized body rather than a hand-built {@code ProblemDetail}.
 *
 * <p>Uses a standalone MockMvc rather than {@code @WebMvcTest}: the slice would also load Elaro's
 * servlet filters and everything they depend on, none of which this test is about.
 */
class GlobalExceptionHandlerTest {

    private final MockMvc mvc =
            MockMvcBuilders.standaloneSetup(new ThrowingController())
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .build();

    @Test
    @DisplayName("404 carries NOT_FOUND and its type URI")
    void notFound() throws Exception {
        mvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.type").value("https://elaro.ai/problems/not-found"))
                .andExpect(jsonPath("$.detail").value("Source 'usgs-earthquakes' does not exist."));
    }

    @Test
    @DisplayName("409 carries the supplied code, not a generic conflict code")
    void conflict() throws Exception {
        mvc.perform(post("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CREDENTIALS_REQUIRED"))
                .andExpect(jsonPath("$.type").value("https://elaro.ai/problems/credentials-required"));
    }

    @Test
    @DisplayName("429 carries BUDGET_EXCEEDED")
    void budgetExceeded() throws Exception {
        mvc.perform(post("/test/budget"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("BUDGET_EXCEEDED"))
                .andExpect(jsonPath("$.type").value("https://elaro.ai/problems/budget-exceeded"));
    }

    @Test
    @DisplayName("an explicit ValidationException lists its field errors")
    void validationWithFieldErrors() throws Exception {
        mvc.perform(post("/test/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("intervalSeconds"))
                .andExpect(jsonPath("$.errors[0].message").value("must be at least 10"));
    }

    @Test
    @DisplayName("a ValidationException with no field errors omits the errors property")
    void validationWithoutFieldErrors() throws Exception {
        mvc.perform(post("/test/validation-bare"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @DisplayName("Bean Validation on the body becomes VALIDATION_FAILED with field errors")
    void beanValidationOnBody() throws Exception {
        mvc.perform(
                        post("/test/validated")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"\",\"count\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.type").value("https://elaro.ai/problems/validation-failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    @DisplayName("malformed JSON is MALFORMED_REQUEST and does not echo the body")
    void malformedJson() throws Exception {
        mvc.perform(
                        post("/test/validated")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": \"leaky-secret\", "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.detail").value("The request body could not be parsed as JSON."))
                // A parser message can quote the payload, which may hold a credential.
                .andExpect(content().string(not(containsString("leaky-secret"))));
    }

    @Test
    @DisplayName("a query parameter of the wrong type is a 400 with a code")
    void typeMismatch() throws Exception {
        mvc.perform(get("/test/typed").param("limit", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("InvalidInputException from a parser is a 400 that passes its message through")
    void invalidInputFromParser() throws Exception {
        mvc.perform(get("/test/bad-duration"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.type").value("https://elaro.ai/problems/validation-failed"))
                .andExpect(jsonPath("$.detail").value(containsString("shorthand")));
    }

    @Test
    @DisplayName("a bare IllegalArgumentException is a 500, not a validation failure")
    void bareIllegalArgumentIsInternal() throws Exception {
        // It used to be a 400, which reported internal bugs to callers as validation errors and
        // kept them out of the logs. Parsers now throw InvalidInputException instead.
        mvc.perform(get("/test/bare-illegal-argument"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$." + GlobalExceptionHandler.CORRELATION_ID_PROPERTY).exists());
    }

    @Test
    @DisplayName("an IllegalArgumentException from library code does not leak its message")
    void illegalArgumentMessageIsNotLeaked() throws Exception {
        mvc.perform(get("/test/library-illegal-argument"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(not(containsString("internal invariant"))));
    }

    @Test
    @DisplayName("an unexpected exception is a 500 with a correlation id and no internals")
    void unexpectedException() throws Exception {
        mvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$." + GlobalExceptionHandler.CORRELATION_ID_PROPERTY).exists())
                .andExpect(jsonPath("$.detail").value(containsString("correlation id")))
                .andExpect(content().string(not(containsString("internal detail"))));
    }

    @Test
    @DisplayName("each 500 gets its own correlation id")
    void correlationIdIsPerRequest() throws Exception {
        String first = mvc.perform(get("/test/boom")).andReturn().getResponse().getContentAsString();
        String second = mvc.perform(get("/test/boom")).andReturn().getResponse().getContentAsString();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("the wrong method is 405 with METHOD_NOT_ALLOWED and an Allow header")
    void methodNotAllowed() throws Exception {
        mvc.perform(post("/test/not-found"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(header().exists("Allow"));
    }

    @Test
    @DisplayName("an unsupported content type is 415")
    void unsupportedMediaType() throws Exception {
        mvc.perform(post("/test/validated").contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @ParameterizedTest(name = "status {0} maps to {1}")
    @CsvSource({
        "400, MALFORMED_REQUEST",
        "401, UNAUTHORIZED",
        "403, FORBIDDEN",
        "404, NOT_FOUND",
        "405, METHOD_NOT_ALLOWED",
        "406, NOT_ACCEPTABLE",
        "413, PAYLOAD_TOO_LARGE",
        "415, UNSUPPORTED_MEDIA_TYPE",
        "429, BUDGET_EXCEEDED",
        "500, INTERNAL_ERROR",
        "502, INTERNAL_ERROR",
    })
    void mapsStatusesToCodes(int status, String expectedCode) {
        assertThat(GlobalExceptionHandler.codeForStatus(HttpStatus.valueOf(status)))
                .isEqualTo(expectedCode);
    }

    /** Endpoints that raise each kind of failure. */
    @RestController
    @RequestMapping("/test")
    static class ThrowingController {

        record Payload(@NotBlank String name, @Min(0) int count) {}

        @GetMapping("/not-found")
        String notFound() {
            throw NotFoundException.of("Source", "usgs-earthquakes");
        }

        @PostMapping("/conflict")
        String conflict() {
            throw new ConflictException(
                    ConflictException.CREDENTIALS_REQUIRED, "This source needs credentials first.");
        }

        @PostMapping("/budget")
        String budget() {
            throw new BudgetExceededProblem("The per-minute budget for this source is spent.");
        }

        @PostMapping("/validation")
        String validation() {
            throw new ValidationException(
                    "The request is not valid.",
                    List.of(new FieldError("intervalSeconds", "must be at least 10")));
        }

        @PostMapping("/validation-bare")
        String validationBare() {
            throw new ValidationException("The request is not valid.");
        }

        @PostMapping("/validated")
        String validated(@Valid @RequestBody Payload payload) {
            return payload.name();
        }

        @GetMapping("/typed")
        String typed(@RequestParam int limit) {
            return String.valueOf(limit);
        }

        @GetMapping("/bad-duration")
        String badDuration() {
            throw new InvalidInputException(
                    "Cannot parse duration 'nonsense'. Expected ISO-8601 (PT15M, P7D) or shorthand"
                            + " (30s, 15m, 12h, 7d, 1w).");
        }

        @GetMapping("/bare-illegal-argument")
        String bareIllegalArgument() {
            throw new IllegalArgumentException();
        }

        @GetMapping("/library-illegal-argument")
        String libraryIllegalArgument() {
            throw new IllegalArgumentException("internal invariant violated");
        }

        @GetMapping("/boom")
        String boom() {
            throw new IllegalStateException("internal detail that must not reach the client");
        }
    }
}
