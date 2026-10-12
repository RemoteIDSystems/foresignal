package ai.foresignal.api.support;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Read API settings, bound from {@code foresignal.api.*} (prompt 014).
 *
 * @param query settings applied to every read query
 * @param changes settings of {@code GET /v1/changes}
 * @param stream settings of the streaming formats ({@code ndjson}, {@code geojsonseq}, {@code csv})
 */
@ConfigurationProperties("foresignal.api")
public record ApiProperties(@DefaultValue Query query, @DefaultValue Changes changes, @DefaultValue Stream stream) {

    /** Fills missing sections with their defaults. */
    public ApiProperties {
        if (query == null) {
            query = Query.defaults();
        }
        if (changes == null) {
            changes = Changes.defaults();
        }
        if (stream == null) {
            stream = Stream.defaults();
        }
    }

    /** The shipped defaults. */
    public static ApiProperties defaults() {
        return new ApiProperties(Query.defaults(), Changes.defaults(), Stream.defaults());
    }

    /**
     * Query settings.
     *
     * @param statementTimeout {@code SET LOCAL statement_timeout} of every read query; a query that
     *     runs longer fails with {@code 503 QUERY_TIMEOUT}
     */
    public record Query(@DefaultValue("30s") Duration statementTimeout) {

        /** Validates the timeout. */
        public Query {
            if (statementTimeout == null) {
                statementTimeout = Duration.ofSeconds(30);
            }
            if (statementTimeout.isNegative() || statementTimeout.isZero()) {
                throw new IllegalArgumentException(
                        "foresignal.api.query.statement-timeout must be positive, got " + statementTimeout);
            }
        }

        /** 30 s. */
        public static Query defaults() {
            return new Query(Duration.ofSeconds(30));
        }
    }

    /**
     * Change feed settings.
     *
     * @param inlinePayloadKinds kinds whose changes always carry their payload, even with
     *     {@code payload=false} (Weather-Imagery-Design §6.8)
     */
    public record Changes(@DefaultValue("raster-frame") List<String> inlinePayloadKinds) {

        /** Copies the list. */
        public Changes {
            inlinePayloadKinds = inlinePayloadKinds == null ? List.of() : List.copyOf(inlinePayloadKinds);
        }

        /** {@code [raster-frame]}. */
        public static Changes defaults() {
            return new Changes(List.of("raster-frame"));
        }
    }

    /**
     * Streaming format settings.
     *
     * @param maxRows the most rows one streaming response may hold; a larger result is refused with
     *     {@code 400 RESULT_TOO_LARGE} rather than truncated
     */
    public record Stream(@DefaultValue("500000") int maxRows) {

        /** Validates the cap. */
        public Stream {
            if (maxRows < 1) {
                throw new IllegalArgumentException("foresignal.api.stream.max-rows must be positive, got " + maxRows);
            }
        }

        /** 500,000 rows. */
        public static Stream defaults() {
            return new Stream(500_000);
        }
    }
}
