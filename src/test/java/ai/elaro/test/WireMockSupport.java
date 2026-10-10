package ai.elaro.test;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Base class for adapter tests that need a stubbed upstream.
 *
 * <p>WireMock listens on a dynamic port so test classes never collide, and stubs are reset between
 * tests. Unmatched requests fail the test, which keeps an adapter from silently calling an endpoint
 * the test did not expect.
 */
public abstract class WireMockSupport {

    @RegisterExtension
    protected static final WireMockExtension WIREMOCK = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .configureStaticDsl(true)
            .failOnUnmatchedRequests(true)
            .resetOnEachTest(true)
            .build();

    /** The root URL of the stub server, for example {@code http://localhost:54321}. */
    protected static String baseUrl() {
        return WIREMOCK.baseUrl();
    }

    /** Absolute URL for {@code path} on the stub server. */
    protected static String url(String path) {
        return WIREMOCK.url(path);
    }

    /** The port the stub server is listening on. */
    protected static int port() {
        return WIREMOCK.getPort();
    }
}
