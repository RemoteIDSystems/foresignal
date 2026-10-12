package ai.foresignal.inbound.formats.location;

import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

/** Shared fixtures for the personal-location format tests (synthetic data only). */
final class LocationTestSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant NOW = Instant.parse("2026-10-12T08:00:00Z");

    private LocationTestSupport() {}

    /** A personal-location push source declaring {@code tracker} and {@code tracker-transition}. */
    static SourceDescriptor source() {
        return SourceDescriptor.builder("custom-family").kinds("tracker", "tracker-transition").mode(Mode.PUSH)
                .defaultBudget(60, 86_400).build();
    }

    /** A source declaring only {@code tracker}. */
    static SourceDescriptor trackerOnly() {
        return SourceDescriptor.builder("custom-family").kinds("tracker").mode(Mode.PUSH)
                .defaultBudget(60, 86_400).build();
    }

    /** A JSON POST. */
    static IngestRequest json(String body) {
        return new IngestRequest(bytes(body), MediaType.APPLICATION_JSON, new HttpHeaders(), Map.of(), NOW);
    }

    /** A JSON POST with headers. */
    static IngestRequest json(String body, HttpHeaders headers) {
        return new IngestRequest(bytes(body), MediaType.APPLICATION_JSON, headers, Map.of(), NOW);
    }

    /** A form POST. */
    static IngestRequest form(String body) {
        return new IngestRequest(bytes(body), MediaType.APPLICATION_FORM_URLENCODED, new HttpHeaders(), Map.of(), NOW);
    }

    /** A GET with {@code pairs} as query parameters ({@code name, value, name, value, …}). */
    static IngestRequest query(String... pairs) {
        Map<String, List<String>> q = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            q.put(pairs[i], List.of(pairs[i + 1]));
        }
        return new IngestRequest(new byte[0], null, new HttpHeaders(), q, NOW);
    }

    /** A fixture under {@code fixtures/location/}. */
    static String fixture(String name) {
        try (InputStream in = LocationTestSupport.class.getResourceAsStream("/fixtures/location/" + name)) {
            if (in == null) {
                throw new IllegalStateException("missing fixture " + name);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Options from a JSON object. */
    static FormatOptions options(String json) {
        return new FormatOptions(JSON.readTree(json));
    }

    private static byte[] bytes(String body) {
        return body.getBytes(StandardCharsets.UTF_8);
    }
}
