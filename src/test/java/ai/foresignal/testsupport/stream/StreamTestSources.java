package ai.foresignal.testsupport.stream;

import ai.foresignal.testsupport.records.RecordsTestSources;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * The SSE test context (prompt 015): the read-API sources and stub retention state of
 * {@link RecordsTestSources}, on a real port ({@code @WebIntegrationTest}). Every SSE integration
 * test imports this one configuration, so they share one context.
 */
@TestConfiguration
@Import(RecordsTestSources.class)
public class StreamTestSources {}
