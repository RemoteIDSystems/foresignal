package ai.foresignal.mapping.preset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.derive.DerivedRecords;
import ai.foresignal.inbound.CustomSourceEntity;
import ai.foresignal.inbound.CustomSourceRepository;
import ai.foresignal.inbound.format.AlertmanagerFormat;
import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.GeoJsonIngestMapper;
import ai.foresignal.inbound.format.IngestFormat;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.mapping.MappingCache;
import ai.foresignal.mapping.MappingCompiler;
import ai.foresignal.mapping.MappingEngine;
import ai.foresignal.mapping.MappingInputs;
import ai.foresignal.mapping.format.DeviceMappingFormat;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The shared steps of the preset contract tests (prompt 059): a preset applied exactly as {@code POST
 * /v1/custom-sources {preset, presetParams}} applies it, its format fed the captured fixture under
 * {@code fixtures/mapping/{id}/}, and the records compared with the golden {@code expected.json}.
 *
 * <p>No Spring context: the stored mapping comes from a stubbed {@code custom_source} row and the current state
 * from a stub that knows no entity.
 */
public final class PresetContract {

    /** Shared mapper. */
    public static final JsonMapper JSON = JsonMapper.builder().build();

    /** Relative tolerance of numeric comparisons. */
    static final double TOLERANCE = 1e-9;

    private PresetContract() {}

    /**
     * The outcome of one fixture.
     *
     * @param parsed what the format produced
     * @param expected the {@code expected.json} document
     * @param descriptor the source the preset defines
     * @param merged the preset-filled {@code CustomSourceInput}
     */
    public record Run(ParsedIngest parsed, JsonNode expected, SourceDescriptor descriptor, ObjectNode merged) {}

    /** Runs the fixture of {@code presetId}. */
    public static Run run(String presetId) {
        JsonNode expected = resource("fixtures/mapping/" + presetId + "/expected.json");
        JsonNode sample = resource("fixtures/mapping/" + presetId + "/sample.json");
        return run(presetId, expected.get("presetParams"), sample, expected);
    }

    /** Runs {@code sample} through the preset {@code presetId} with {@code params}. */
    public static Run run(String presetId, JsonNode params, JsonNode sample, JsonNode expected) {
        MappingPreset preset = PresetCatalog.find(presetId).orElseThrow();
        ObjectNode body = JSON.createObjectNode();
        body.put("id", "custom-contract-" + presetId);
        body.put("preset", presetId);
        if (params != null && !params.isNull()) {
            body.set("presetParams", params);
        }
        ObjectNode merged = PresetCatalog.apply(body, preset);
        SourceDescriptor descriptor = descriptor(merged);
        CustomSourceEntity row = new CustomSourceEntity(descriptor.id());
        row.setMapping(merged.hasNonNull("mapping") ? JSON.writeValueAsString(merged.get("mapping")) : null);
        CustomSourceRepository repository = mock(CustomSourceRepository.class);
        when(repository.findById(anyString())).thenReturn(Optional.of(row));
        DerivedRecords current = mock(DerivedRecords.class);
        when(current.get(anyString(), anyString())).thenReturn(Optional.empty());
        IngestFormat format = format(preset, repository, current);
        FormatOptions options = new FormatOptions(merged.path("formats").path(preset.format()));
        ParsedIngest parsed = format.parse(request(sample), descriptor, options);
        return new Run(parsed, expected, descriptor, merged);
    }

    /** The format a preset uses: its device format, or the built-in {@code alertmanager}. */
    static IngestFormat format(MappingPreset preset, CustomSourceRepository repository, DerivedRecords current) {
        if (AlertmanagerFormat.ID.equals(preset.format())) {
            return new AlertmanagerFormat(JSON, new GeoJsonIngestMapper(JSON));
        }
        return new DeviceMappingFormat(preset, new MappingCache(repository, new MappingCompiler(), JSON),
                new MappingEngine(JSON), JSON, current);
    }

    /** The descriptor of a preset-filled input: its kinds, time base and privacy. */
    static SourceDescriptor descriptor(ObjectNode merged) {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(merged.get("id").asString())
                .name(merged.path("name").asString("contract"))
                .mode(Mode.PUSH)
                .custom(true)
                .observedAtMode(ObservedAtMode.valueOf(merged.path("observedAtMode").asString("UPSTREAM")))
                .privateSource(merged.path("private").asBoolean(false))
                .defaultBudget(60, 86_400);
        merged.path("kinds").forEach(k -> builder.kind(k.asString()));
        return builder.build();
    }

    /** The {@link IngestRequest} of a {@code CapturedSample}-shaped fixture. */
    public static IngestRequest request(JsonNode sample) {
        String body = sample.hasNonNull("body") ? sample.get("body").asString() : "";
        MediaType type = sample.hasNonNull("contentType") ? MediaType.parseMediaType(sample.get("contentType").asString())
                : null;
        Map<String, List<String>> query = new LinkedHashMap<>();
        if (sample.hasNonNull("query")) {
            MappingInputs.parseForm(sample.get("query").asString()).properties()
                    .forEach(e -> query.put(e.getKey(), List.of(e.getValue().asString())));
        }
        return new IngestRequest(body.getBytes(StandardCharsets.UTF_8), type, new HttpHeaders(), query,
                Instant.parse(sample.get("receivedAt").asString()));
    }

    /** Asserts the golden records, the secrets never stored and the input keys never in a payload. */
    public static void assertGolden(Run run) {
        List<NormalizedRecord> records = run.parsed().records();
        JsonNode expected = run.expected().get("records");
        assertThat(run.parsed().rejections()).as("rejections").isEmpty();
        assertThat(records).as("records").hasSize(expected.size());
        for (int i = 0; i < records.size(); i++) {
            assertRecord("record " + i, records.get(i), expected.get(i));
        }
        for (NormalizedRecord record : records) {
            for (JsonNode key : run.expected().path("neverStored")) {
                assertThat(record.payload()).as("payload of " + record.externalId()).doesNotContainKey(key.asString());
            }
            String text = JSON.writeValueAsString(record.payload());
            for (JsonNode secret : run.expected().path("secrets")) {
                assertThat(text).as("payload of " + record.externalId()).doesNotContain(secret.asString());
            }
        }
    }

    static void assertRecord(String label, NormalizedRecord actual, JsonNode expected) {
        assertThat(actual.externalId()).as(label + " externalId").isEqualTo(expected.get("externalId").asString());
        assertThat(actual.kind()).as(label + " kind").isEqualTo(expected.get("kind").asString());
        assertThat(actual.observedAt()).as(label + " observedAt")
                .isEqualTo(Instant.parse(expected.get("observedAt").asString()));
        if (expected.hasNonNull("eventTime")) {
            assertThat(actual.eventTime()).as(label + " eventTime")
                    .isEqualTo(Instant.parse(expected.get("eventTime").asString()));
        }
        if (expected.hasNonNull("validUntil")) {
            assertThat(actual.validUntil()).as(label + " validUntil")
                    .isEqualTo(Instant.parse(expected.get("validUntil").asString()));
        }
        JsonNode geometry = expected.get("geometry");
        if (geometry == null || geometry.isNull()) {
            assertThat(actual.geometry()).as(label + " geometry").isNull();
        } else {
            assertThat(actual.geometry()).as(label + " geometry").isInstanceOf(Point.class);
            Point point = (Point) actual.geometry();
            assertNumber(label + " lon", point.getX(), geometry.get(0).asDouble());
            assertNumber(label + " lat", point.getY(), geometry.get(1).asDouble());
        }
        assertValue(label + " payload", JSON.valueToTree(actual.payload()), expected.get("payload"));
        if (expected.has("qualityExtra")) {
            assertValue(label + " quality.extra", JSON.valueToTree(actual.quality().extra()), expected.get("qualityExtra"));
        }
    }

    /** Deep JSON equality with numeric tolerance. */
    static void assertValue(String label, JsonNode actual, JsonNode expected) {
        if (expected.isNumber()) {
            assertThat(actual.isNumber()).as(label + " is a number: " + actual).isTrue();
            assertNumber(label, actual.asDouble(), expected.asDouble());
        } else if (expected.isObject()) {
            assertThat(actual.isObject()).as(label + " is an object").isTrue();
            List<String> actualKeys = new ArrayList<>(actual.propertyNames());
            List<String> expectedKeys = new ArrayList<>(expected.propertyNames());
            assertThat(actualKeys).as(label + " keys").containsExactlyInAnyOrderElementsOf(expectedKeys);
            for (String key : expectedKeys) {
                assertValue(label + "." + key, actual.get(key), expected.get(key));
            }
        } else if (expected.isArray()) {
            assertThat(actual.size()).as(label + " size").isEqualTo(expected.size());
            for (int i = 0; i < expected.size(); i++) {
                assertValue(label + "[" + i + "]", actual.get(i), expected.get(i));
            }
        } else {
            assertThat(actual).as(label).isEqualTo(expected);
        }
    }

    static void assertNumber(String label, double actual, double expected) {
        double scale = Math.max(1.0, Math.abs(expected));
        assertThat(Math.abs(actual - expected)).as(label + ": " + actual + " vs " + expected)
                .isLessThanOrEqualTo(TOLERANCE * scale);
    }

    /** A classpath JSON resource. */
    public static JsonNode resource(String path) {
        try (InputStream in = PresetContract.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing test resource " + path);
            }
            return JSON.readTree(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
