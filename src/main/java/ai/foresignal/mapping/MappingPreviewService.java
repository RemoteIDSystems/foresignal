package ai.foresignal.mapping;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.inbound.CustomSourceEntity;
import ai.foresignal.inbound.CustomSourceService;
import ai.foresignal.inbound.format.ItemRejection;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.ingest.NormalizedBatch;
import ai.foresignal.ingest.Normalizer;
import ai.foresignal.ingest.Observation;
import ai.foresignal.ingest.Rejection;
import ai.foresignal.ingest.SourceIngestProfile;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code previewMapping} and {@code testCustomSourceMapping} (prompt 059): what ingest would write for a sample,
 * computed exactly as ingest computes it and never written.
 *
 * <p>The mapping is compiled (time-base mismatches become warnings instead of errors), applied by
 * {@link MappingEngine} and then normalized by the standard {@link Normalizer} with the source's ingest profile,
 * so limits, null island, the clamp, the private precision cap and the time base show up exactly as they would
 * on ingest. Without a {@code sourceId} the records are normalized against a stand-in push source with the kinds
 * the mapping produced and the time base its {@code observedAt} implies.
 *
 * <p>Warnings: unmapped top-level input keys (not read by any path and not dropped), produced fields the
 * source's {@code fieldSpecs} does not declare, significant fields a SAMPLED or LATEST policy needs that the
 * mapping cannot produce, and a time base that disagrees with the source's {@code observedAtMode}.
 */
@Service
public class MappingPreviewService {

    /** The most unmapped-key warnings one preview reports. */
    static final int MAX_UNMAPPED = 50;

    private final MappingCompiler compiler;
    private final MappingEngine engine;
    private final SourceRegistry registry;
    private final SourceService sources;
    private final CustomSourceService customSources;
    private final SampleCapture samples;
    private final JsonMapper json;
    private final Clock clock;
    private final Normalizer normalizer = new Normalizer();
    private final GeoJsonWriter geoJson = new GeoJsonWriter();

    /** Creates the service. */
    public MappingPreviewService(MappingCompiler compiler, MappingEngine engine, SourceRegistry registry,
            SourceService sources, CustomSourceService customSources, SampleCapture samples, JsonMapper json,
            Clock clock) {
        this.compiler = compiler;
        this.engine = engine;
        this.registry = registry;
        this.sources = sources;
        this.customSources = customSources;
        this.samples = samples;
        this.json = json;
        this.clock = clock;
    }

    /**
     * {@code previewMapping}: {@code {mapping, sample, sourceId?}} → {@code MappingPreviewResult}.
     *
     * @throws MappingInvalidException when the mapping does not compile
     * @throws NotFoundException for an unknown {@code sourceId}
     */
    public Map<String, Object> preview(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new InvalidInputException("The body must be {mapping, sample, sourceId?}.");
        }
        JsonNode mapping = body.get("mapping");
        if (mapping == null || mapping.isNull()) {
            throw new MappingInvalidException(List.of(new ai.foresignal.api.error.FieldError("mapping", "is required")));
        }
        JsonNode sourceNode = body.get("sourceId");
        SourceDescriptor descriptor = sourceNode == null || sourceNode.isNull() ? null : descriptor(sourceNode.asString());
        return preview(mapping, MappingInputs.fromSample(body.get("sample"), json), descriptor);
    }

    /**
     * The preview of {@code mapping} over {@code doc} for {@code descriptor} (null: no source).
     *
     * @throws MappingInvalidException when the mapping does not compile
     */
    public Map<String, Object> preview(JsonNode mapping, JsonNode doc, SourceDescriptor descriptor) {
        CompiledMapping compiled = compiler.compile(mapping,
                descriptor == null ? MappingContext.none() : MappingContext.of(descriptor, false));
        return run(compiled, doc, descriptor, clock.instant());
    }

    /**
     * {@code testCustomSourceMapping}: the proposed mapping (or the stored one) over each captured sample.
     *
     * @throws NotFoundException for an unknown custom source
     * @throws ConflictException {@code MAPPING_NOT_SET} or {@code NO_SAMPLES}
     */
    public Map<String, Object> test(String sourceId, JsonNode body) {
        CustomSourceEntity row = customSources.get(sourceId);
        SourceDescriptor descriptor = descriptor(sourceId);
        JsonNode proposed = body == null || body.isNull() || !body.isObject() ? null : body.get("mapping");
        String mappingSource;
        JsonNode mapping;
        if (proposed != null && !proposed.isNull()) {
            mappingSource = "proposed";
            mapping = proposed;
        } else if (row.getMapping() != null && !row.getMapping().isBlank()) {
            mappingSource = "stored";
            mapping = json.readTree(row.getMapping());
        } else {
            throw new ConflictException(ErrorCodes.MAPPING_NOT_SET,
                    "The source " + sourceId + " has no stored mapping and the request proposed none.");
        }
        CompiledMapping compiled = compiler.compile(mapping, MappingContext.of(descriptor, false));
        List<SampleCapture.Sample> captured = samples.list(sourceId);
        if (captured.isEmpty()) {
            throw new ConflictException(ErrorCodes.NO_SAMPLES,
                    "No samples are captured for " + sourceId + "; set captureSamples=true and send a request.");
        }
        List<Map<String, Object>> results = new ArrayList<>();
        for (SampleCapture.Sample sample : captured) {
            Map<String, Object> one = new LinkedHashMap<>();
            one.put("receivedAt", sample.receivedAt());
            try {
                one.putAll(run(compiled, document(sample), descriptor, sample.receivedAt()));
            } catch (RuntimeException e) {
                one.put("records", List.of());
                one.put("rejections", List.of(Map.of("index", 0, "reason", "UNREADABLE_SAMPLE")));
                one.put("skipped", 0);
                one.put("warnings", List.of(new MappingWarning("UNREADABLE_SAMPLE", e.getMessage(), null)));
            }
            results.add(one);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mappingSource", mappingSource);
        out.put("samples", results);
        return out;
    }

    /** The document of a captured sample, read as ingest would read it. */
    JsonNode document(SampleCapture.Sample sample) {
        MediaType type = null;
        if (sample.contentType() != null) {
            try {
                type = MediaType.parseMediaType(sample.contentType());
            } catch (org.springframework.util.InvalidMimeTypeException e) {
                type = null;
            }
        }
        byte[] body = sample.body() == null ? new byte[0] : sample.body().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Map<String, List<String>> query = new LinkedHashMap<>();
        MappingInputs.parseForm(sample.query()).properties()
                .forEach(e -> query.put(e.getKey(), List.of(e.getValue().asString())));
        return MappingInputs.fromRequest(new ai.foresignal.inbound.format.IngestRequest(body, type, new HttpHeaders(),
                query, sample.receivedAt()), json);
    }

    // ---------- the shared run ----------

    /** Maps, normalizes and assembles the {@code MappingPreviewResult}. */
    Map<String, Object> run(CompiledMapping compiled, JsonNode doc, SourceDescriptor descriptor, Instant receivedAt) {
        MappingResult result = engine.apply(doc, compiled, receivedAt);
        SourceDescriptor target = descriptor != null ? descriptor : standIn(compiled, result);
        List<IngestRecord> input = new ArrayList<>();
        for (int i = 0; i < result.records().size(); i++) {
            input.add(new IngestRecord(result.records().get(i), receivedAt, i));
        }
        List<Map<String, Object>> rejections = new ArrayList<>();
        for (ItemRejection r : result.rejections()) {
            rejections.add(rejection(r.index(), r.reason(), r.externalId()));
        }
        List<Map<String, Object>> records = new ArrayList<>();
        if (!input.isEmpty()) {
            NormalizedBatch batch = normalizer.normalize(SourceIngestProfile.forDescriptor(target), input);
            List<Observation> observations = new ArrayList<>(batch.observations());
            observations.sort(Comparator.comparingLong(Observation::arrival));
            for (Observation o : observations) {
                records.add(view(o));
            }
            for (Rejection r : batch.rejections()) {
                int item = r.index() >= 0 && r.index() < result.indexes().size() ? result.indexes().get(r.index()) : r.index();
                rejections.add(rejection(item, r.reason().name(), r.externalId()));
            }
        }
        rejections.sort(Comparator.comparingInt(r -> (Integer) r.get("index")));
        List<MappingWarning> warnings = new ArrayList<>(result.warnings());
        warnings.addAll(unmapped(compiled, doc));
        if (descriptor != null) {
            warnings.addAll(fieldSpecWarnings(compiled, result, descriptor));
            warnings.addAll(significantWarnings(compiled, descriptor));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("records", records);
        out.put("rejections", rejections);
        out.put("skipped", result.skipped());
        out.put("warnings", warnings);
        return out;
    }

    private static Map<String, Object> rejection(int index, String reason, String externalId) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("index", index);
        out.put("reason", reason);
        out.put("externalId", externalId);
        return out;
    }

    /** A push source standing in for a preview without {@code sourceId}. */
    static SourceDescriptor standIn(CompiledMapping compiled, MappingResult result) {
        Set<String> kinds = new LinkedHashSet<>();
        for (NormalizedRecord record : result.records()) {
            kinds.add(record.kind());
        }
        if (kinds.isEmpty()) {
            kinds.add("preview");
        }
        ObservedAtMode mode = compiled.observedAt() instanceof TimeSpec.Received
                ? ObservedAtMode.RECEIVED : ObservedAtMode.UPSTREAM;
        SourceDescriptor.Builder builder = SourceDescriptor.builder("mapping-preview")
                .name("Mapping preview")
                .mode(Mode.PUSH)
                .custom(true)
                .observedAtMode(mode)
                .zeroResultsNormal(true);
        kinds.forEach(builder::kind);
        return builder.build();
    }

    /** The {@code NormalizedRecordPreview} of an observation. */
    Map<String, Object> view(Observation o) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("externalId", o.externalId());
        out.put("kind", o.kind());
        out.put("observedAt", o.observedAt());
        out.put("geometry", o.geometry() == null ? null : geoJson.write(o.geometry()));
        out.put("validUntil", o.validUntil());
        out.put("tombstone", o.tombstone());
        out.put("payload", o.payload());
        out.put("eventTime", o.eventTime());
        out.put("validFrom", o.validFrom());
        if (o.quality() == null || o.quality().isEmpty()) {
            out.put("quality", null);
        } else {
            Map<String, Object> quality = new LinkedHashMap<>();
            quality.put("accuracyMeters", o.quality().accuracyMeters());
            quality.put("confidence", o.quality().confidence());
            quality.put("positionSource", o.quality().positionSource() == null ? null
                    : o.quality().positionSource().toString());
            quality.put("extra", o.quality().extra());
            out.put("quality", quality);
        }
        out.put("observers", List.of());
        return out;
    }

    // ---------- warnings ----------

    /** Top-level input keys (of the items under {@code select}) that no path reads and {@code drop} omits. */
    List<MappingWarning> unmapped(CompiledMapping compiled, JsonNode doc) {
        Set<String> keys = new LinkedHashSet<>();
        List<JsonNode> items = new ArrayList<>();
        if (compiled.select() == null) {
            items.add(doc);
        } else if (doc != null && doc.isObject()) {
            // Item keys are what the paths read under select.
            JsonNode selected = engine.dialect().read(compiled.select(), doc);
            if (selected.isArray()) {
                for (int i = 0; i < selected.size() && i < 50; i++) {
                    items.add(selected.get(i));
                }
            } else if (selected.isObject()) {
                items.add(selected);
            }
        }
        for (JsonNode item : items) {
            if (item != null && item.isObject()) {
                for (String key : item.propertyNames()) {
                    keys.add(key);
                }
            }
        }
        if (compiled.spread() != null) {
            return List.of();
        }
        List<MappingWarning> out = new ArrayList<>();
        for (String key : keys) {
            if (!compiled.referencedKeys().contains(key) && !compiled.drop().contains(key) && out.size() < MAX_UNMAPPED) {
                out.add(new MappingWarning(MappingWarning.UNMAPPED_INPUT_KEY,
                        "The input key '" + key + "' is not mapped (add a field, or list it in drop)", null));
            }
        }
        return out;
    }

    /** Produced fields the source's {@code fieldSpecs} does not declare (only when it declares some). */
    static List<MappingWarning> fieldSpecWarnings(CompiledMapping compiled, MappingResult result,
            SourceDescriptor descriptor) {
        if (descriptor.fieldSpecs() == null || descriptor.fieldSpecs().isEmpty()) {
            return List.of();
        }
        Set<String> produced = new LinkedHashSet<>(compiled.fieldNames());
        result.records().forEach(r -> produced.addAll(r.payload().keySet()));
        List<MappingWarning> out = new ArrayList<>();
        List<String> names = new ArrayList<>(compiled.fieldNames());
        for (String name : produced) {
            if (!descriptor.fieldSpecs().containsKey(name)) {
                int index = names.indexOf(name);
                out.add(new MappingWarning(MappingWarning.FIELD_NOT_IN_FIELD_SPECS,
                        "The field '" + name + "' is not declared in the source's fieldSpecs",
                        index >= 0 ? "mapping.fields[" + fieldIndex(compiled, name) + "]" : "mapping.spread"));
            }
        }
        return out;
    }

    private static int fieldIndex(CompiledMapping compiled, String name) {
        for (int i = 0; i < compiled.fields().size(); i++) {
            if (compiled.fields().get(i).name().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /** Significant fields a SAMPLED or LATEST policy needs that the mapping cannot produce. */
    List<MappingWarning> significantWarnings(CompiledMapping compiled, SourceDescriptor descriptor) {
        IngestPolicy policy = Optional.ofNullable(sourcesOverride(descriptor.id())).orElse(descriptor.defaultIngestPolicy());
        if (policy == null || !policy.requiresSignificantFields() || compiled.spread() != null) {
            return List.of();
        }
        List<MappingWarning> out = new ArrayList<>();
        Set<String> produced = compiled.fieldNames();
        for (String field : descriptor.significantFields()) {
            if (!produced.contains(field)) {
                out.add(new MappingWarning(MappingWarning.SIGNIFICANT_FIELD_MISSING, "The " + policy.mode().wireValue()
                        + " policy treats '" + field + "' as significant, but the mapping never produces it", null));
            }
        }
        return out;
    }

    private IngestPolicy sourcesOverride(String id) {
        try {
            return sources.ingestPolicyOverride(id).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private SourceDescriptor descriptor(String sourceId) {
        return registry.find(sourceId).map(SourceRegistry.Registration::descriptor)
                .orElseThrow(() -> NotFoundException.of("Source", sourceId));
    }
}
