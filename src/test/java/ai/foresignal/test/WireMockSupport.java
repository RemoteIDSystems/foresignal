package ai.foresignal.test;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Base class for adapter tests that need a stubbed upstream.
 *
 * <p>WireMock listens on a dynamic port so test classes never collide, and stubs are reset between
 * tests. Unmatched requests fail the test, which keeps an adapter from silently calling an endpoint
 * the test did not expect.
 *
 * <p>WireMock's own response compression is off. With it on, Jetty gzips any response to a request
 * that accepts gzip (which {@code UpstreamClient} always does) and rewrites the stub's {@code ETag}
 * from {@code "v1"} to {@code "v1--gzip"}, so a test could not assert that a validator is stored
 * exactly as the upstream sent it. A test that needs a compressed body stubs one explicitly, with
 * {@code Content-Encoding: gzip}.
 */
public abstract class WireMockSupport {

    @RegisterExtension
    protected static final WireMockExtension WIREMOCK = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().gzipDisabled(true))
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
