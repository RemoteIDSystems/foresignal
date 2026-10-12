package ai.foresignal.testsupport.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.inbound.CompatPublishService;
import ai.foresignal.inbound.CustomSourceInputReader;
import ai.foresignal.inbound.edp.EdpItemValidator;
import ai.foresignal.inbound.edp.EdpManifestValidator;
import ai.foresignal.inbound.edp.EdpProperties;
import ai.foresignal.inbound.edp.EdpSchemaCompiler;
import ai.foresignal.inbound.format.AlertmanagerFormat;
import ai.foresignal.inbound.format.CapFormat;
import ai.foresignal.inbound.format.CloudEventsFormat;
import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.GeoJsonIngestMapper;
import ai.foresignal.inbound.format.IngestFormat;
import ai.foresignal.inbound.format.IngestFormatRegistry;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.MappingFormat;
import ai.foresignal.inbound.format.NativeFormat;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.inbound.format.RecordMappingEngine;
import ai.foresignal.inbound.signature.ForesignalSignatureVerifier;
import ai.foresignal.inbound.signature.HmacSha256Verifier;
import ai.foresignal.inbound.signature.NoneVerifier;
import ai.foresignal.inbound.signature.SignatureVerifiers;
import ai.foresignal.inbound.signature.StandardWebhooksVerifier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The production parsers of prompt 024's request bodies, built without a Spring context, for the
 * Postman contract test: each method throws or fails an assertion when the implementation would
 * reject the body.
 */
public final class InboundBodies {

    /** The receipt time the examples are judged at. */
    public static final Instant RECEIVED = Instant.parse("2026-10-11T18:00:00Z");

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Clock CLOCK = Clock.fixed(RECEIVED, ZoneOffset.UTC);
    private static final GeoJsonIngestMapper GEOJSON = new GeoJsonIngestMapper(JSON);
    private static final NativeFormat NATIVE = new NativeFormat(JSON, GEOJSON);
    private static final AlertmanagerFormat ALERTMANAGER = new AlertmanagerFormat(JSON, GEOJSON);
    private static final CloudEventsFormat CLOUDEVENTS = new CloudEventsFormat(JSON, GEOJSON);
    private static final MappingFormat MAPPING = new MappingFormat(JSON, new RecordMappingEngine(GEOJSON));
    private static final List<IngestFormat> FORMATS = List.of(NATIVE, ALERTMANAGER, CLOUDEVENTS, new CapFormat(), MAPPING);
    private static final EdpSchemaCompiler COMPILER = new EdpSchemaCompiler();

    /** The mapping the Postman {@code mapping} example is written for. */
    private static final String EXAMPLE_MAPPING = "{\"recordsPath\":\"/items\",\"externalId\":\"/device\","
            + "\"observedAt\":\"/ts\",\"observedAtFormat\":\"epoch-s\",\"lat\":\"/pos/lat\",\"lon\":\"/pos/lon\","
            + "\"payload\":[{\"from\":\"/pm25\",\"to\":\"pm25\"}]}";

    private InboundBodies() {}

    /** {@code createCustomSource} / {@code updateCustomSource}: the production input reader. */
    public static void customSource(JsonNode body) {
        SignatureVerifiers verifiers = new SignatureVerifiers(List.of(new NoneVerifier(), new HmacSha256Verifier(),
                new StandardWebhooksVerifier(CLOCK), new ForesignalSignatureVerifier(CLOCK)));
        new CustomSourceInputReader(new IngestFormatRegistry(FORMATS), verifiers, JSON).read(body, null, Set.of());
    }

    /** {@code ingestRecords}: the native format, with no rejection. */
    public static void nativeBody(JsonNode body) {
        accepted(NATIVE.parse(request(body, MediaType.APPLICATION_JSON), source(), FormatOptions.empty()));
    }

    /** {@code ingestRecordsFormat}: the format the body's shape belongs to, with no rejection. */
    public static void formatBody(JsonNode body) {
        if (body.isArray()) {
            accepted(CLOUDEVENTS.parse(request(body, CloudEventsFormat.BATCH), source(), FormatOptions.empty()));
        } else if (body.has("alerts")) {
            accepted(ALERTMANAGER.parse(request(body, MediaType.APPLICATION_JSON), source(), FormatOptions.empty()));
        } else if (body.has("specversion")) {
            accepted(CLOUDEVENTS.parse(request(body, CloudEventsFormat.STRUCTURED), source(), FormatOptions.empty()));
        } else {
            accepted(MAPPING.parse(request(body, MediaType.APPLICATION_JSON), source(),
                    new FormatOptions(JSON.readTree(EXAMPLE_MAPPING))));
        }
    }

    /** {@code ntfyPublishJson}: a topic and a valid priority. */
    public static void ntfyJson(JsonNode body) {
        assertThat(body.path("topic").asString("")).as("topic").isNotBlank();
        if (body.has("priority")) {
            CompatPublishService.ntfyPriority(body.get("priority").asString());
        }
    }

    /** {@code gotifyPublish}: a message and a priority from 0 to 10. */
    public static void gotify(JsonNode body) {
        assertThat(body.path("message").isString()).as("message").isTrue();
        if (body.has("priority")) {
            assertThat(body.get("priority").asInt()).isBetween(0, 10);
        }
    }

    /** {@code selfRegisterExternalSource} / {@code updateExternalSourceManifest}: a valid manifest. */
    public static void manifest(JsonNode manifest) {
        EdpManifestValidator.ManifestCheck check = manifestValidator().validate(manifest);
        assertThat(check.errors()).as("manifest errors").isEmpty();
        assertThat(check.valid()).isTrue();
    }

    /** {@code registerExternalSource}: a valid manifest under {@code manifest}. */
    public static void registration(JsonNode body) {
        manifest(body.get("manifest"));
    }

    /** {@code pushExternalItems}: every item valid against the §2 example manifest. */
    public static void batch(JsonNode body) {
        EdpManifestValidator.ManifestCheck check = manifestValidator().validate(exampleManifest());
        List<JsonNode> items = new ArrayList<>();
        body.get("items").forEach(items::add);
        EdpItemValidator.ItemCheck result = new EdpItemValidator(COMPILER, EdpProperties.defaults(), CLOCK)
                .validate(items, check.model(), check.payloadSchemas(), RECEIVED);
        assertThat(result.rejected()).as("rejected items").isEmpty();
    }

    /** {@code validateEdp}: a valid manifest and valid items. */
    public static void validation(JsonNode body) {
        manifest(body.get("manifest"));
        batch(body);
    }

    private static JsonNode exampleManifest() {
        try (var in = InboundBodies.class.getClassLoader()
                .getResourceAsStream("edp/conformance/manifests/valid/hormuz-example.json")) {
            return JSON.readTree(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static EdpManifestValidator manifestValidator() {
        return new EdpManifestValidator(COMPILER, EdpProperties.defaults());
    }

    private static void accepted(ParsedIngest parsed) {
        assertThat(parsed.rejections()).as("rejections").isEmpty();
        assertThat(parsed.records()).as("records").isNotEmpty();
    }

    private static IngestRequest request(JsonNode body, MediaType type) {
        return IngestRequest.of(JSON.writeValueAsBytes(body), type, RECEIVED);
    }

    private static SourceDescriptor source() {
        return SourceDescriptor.builder("custom-home-sensors").kind("sensor-reading").mode(Mode.PUSH)
                .defaultBudget(60, 86_400).custom(true).build();
    }
}
