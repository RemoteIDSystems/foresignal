package ai.foresignal.cycle;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.finding.FindingViews;
import ai.foresignal.baserate.BaseRateEntity;
import ai.foresignal.baserate.BaseRateService;
import ai.foresignal.baserate.BaseRateViews;
import ai.foresignal.export.ExportSchema;
import ai.foresignal.export.ExportWriter;
import ai.foresignal.export.GeoParquetWriter;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastRepository;
import ai.foresignal.forecast.ForecastScoreEntity;
import ai.foresignal.forecast.ForecastScoreRepository;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecastViews;
import ai.foresignal.hypothesis.HypothesisRevisionEntity;
import ai.foresignal.hypothesis.HypothesisRevisionRepository;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.hypothesis.HypothesisViews;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.security.EgressPath;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * The export of an assessment with its cited objects (prompt 128 §4, {@code getAssessmentBundle}): the findings, base
 * rates, ledger questions with their forecasts and scores, forecasts, and hypothesis sets with their recent revisions
 * that the assessment cites (its citations, and the questions and sets its key judgments name), each with an
 * {@code ObjectEnvelope}, its provenance and the 116 manifests behind them. {@code json} returns the
 * {@code AssessmentBundle}; {@code geoparquet} returns a ZIP of one GeoParquet file per object type written with the
 * 111 {@link GeoParquetWriter} (findings carry their geometry), plus {@code assessment.json}. Private objects are
 * left out for callers without private access; the GeoParquet export withholds raw values of {@code LOCAL_ONLY}
 * series.
 */
@Service
public class AssessmentBundleService {

    /** Revisions included per hypothesis set. */
    static final int REVISIONS_PER_SET = 5;

    private static final Pattern TYPED = Pattern.compile("^([a-z][a-z-]*):([0-9A-Z]{26})$");

    private final ObjectProvider<FindingService> findings;
    private final ObjectProvider<BaseRateService> baseRates;
    private final ForecastService ledger;
    private final ForecastRepository forecasts;
    private final ForecastScoreRepository scores;
    private final HypothesisService hypotheses;
    private final HypothesisRevisionRepository revisions;
    private final ObjectProvider<ManifestService> manifests;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public AssessmentBundleService(ObjectProvider<FindingService> findings, ObjectProvider<BaseRateService> baseRates,
            ForecastService ledger, ForecastRepository forecasts, ForecastScoreRepository scores,
            HypothesisService hypotheses, HypothesisRevisionRepository revisions,
            ObjectProvider<ManifestService> manifests, JsonMapper json, Clock clock) {
        this.findings = findings;
        this.baseRates = baseRates;
        this.ledger = ledger;
        this.forecasts = forecasts;
        this.scores = scores;
        this.hypotheses = hypotheses;
        this.revisions = revisions;
        this.manifests = manifests;
        this.json = json;
        this.clock = clock;
    }

    /** One resolved object: its type, envelope, data, provenance, manifest and geometry. */
    record Resolved(String ref, String type, Map<String, Object> envelope, Map<String, Object> data,
            Map<String, Object> provenance, String manifestId, Geometry geometry) {}

    /** The {@code AssessmentBundle} of {@code a}. */
    public Map<String, Object> bundle(AssessmentEntity a, boolean seesPrivate) {
        List<Resolved> objects = resolve(a, seesPrivate, false);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("assessment", AssessmentService.view(a, true));
        out.put("generatedAt", clock.instant().toString());
        List<Map<String, Object>> list = new ArrayList<>();
        for (Resolved r : objects) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("ref", r.ref());
            o.put("envelope", r.envelope());
            o.put("data", r.data());
            o.put("provenance", r.provenance());
            list.add(o);
        }
        out.put("objects", list);
        out.put("manifests", manifestViews(objects, a));
        return out;
    }

    /** The GeoParquet ZIP of {@code a}. */
    public byte[] geoparquet(AssessmentEntity a, boolean seesPrivate) {
        List<Resolved> objects = resolve(a, seesPrivate, true);
        Map<String, List<Resolved>> byType = new LinkedHashMap<>();
        for (Resolved r : objects) {
            byType.computeIfAbsent(r.type(), k -> new ArrayList<>()).add(r);
        }
        Path dir = null;
        try {
            dir = Files.createTempDirectory("assessment-bundle-");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
                zip.putNextEntry(new ZipEntry("assessment.json"));
                Map<String, Object> head = new LinkedHashMap<>();
                head.put("assessment", AssessmentService.view(a, true));
                head.put("manifests", manifestViews(objects, a));
                zip.write(json.writeValueAsString(head).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
                for (Map.Entry<String, List<Resolved>> e : byType.entrySet()) {
                    Path file = dir.resolve(e.getKey() + ".parquet");
                    write(file, e.getValue(), a);
                    zip.putNextEntry(new ZipEntry(e.getKey() + ".parquet"));
                    zip.write(Files.readAllBytes(file));
                    zip.closeEntry();
                }
            }
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write the GeoParquet bundle", e);
        } finally {
            deleteQuietly(dir);
        }
    }

    private void write(Path file, List<Resolved> rows, AssessmentEntity a) throws IOException {
        List<ExportSchema.Column> columns = List.of(new ExportSchema.Column("ref", ExportSchema.Type.STRING, false),
                new ExportSchema.Column("object_type", ExportSchema.Type.STRING, false),
                new ExportSchema.Column("title", ExportSchema.Type.STRING, true),
                new ExportSchema.Column("manifest_id", ExportSchema.Type.STRING, true),
                new ExportSchema.Column("provenance", ExportSchema.Type.STRING, true),
                new ExportSchema.Column("data", ExportSchema.Type.STRING, true));
        boolean geometry = rows.stream().anyMatch(r -> r.geometry() != null);
        try (GeoParquetWriter writer = new GeoParquetWriter()) {
            writer.open(file, new ExportSchema(columns, geometry), Map.of("assessmentId", a.getId()));
            WKBWriter wkb = new WKBWriter();
            for (Resolved r : rows) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("ref", r.ref());
                row.put("object_type", r.type());
                row.put("title", r.envelope().get("title"));
                row.put("manifest_id", r.manifestId());
                row.put("provenance", json.writeValueAsString(r.provenance()));
                row.put("data", json.writeValueAsString(r.data()));
                if (geometry && r.geometry() != null && !r.geometry().isEmpty()) {
                    row.put(ExportWriter.GEOMETRY, wkb.write(r.geometry()));
                    Envelope env = r.geometry().getEnvelopeInternal();
                    row.put(ExportWriter.BBOX, new double[] {env.getMinX(), env.getMinY(), env.getMaxX(),
                        env.getMaxY()});
                }
                writer.write(row);
            }
        }
    }

    /** The objects an assessment cites, resolved and visible to the caller, in citation order. */
    List<Resolved> resolve(AssessmentEntity a, boolean seesPrivate, boolean withheld) {
        Set<String> refs = new LinkedHashSet<>(a.getCitations());
        for (Map<String, Object> j : a.getBluf()) {
            if (j.get("questionId") != null) {
                refs.add("question:" + j.get("questionId"));
            }
            if (j.get("hypothesisSetId") != null) {
                refs.add("hypothesis-set:" + j.get("hypothesisSetId"));
            }
            if (j.get("citations") instanceof List<?> l) {
                l.forEach(c -> refs.add(String.valueOf(c)));
            }
        }
        List<Resolved> out = new ArrayList<>();
        for (String ref : refs) {
            Matcher m = TYPED.matcher(ref);
            if (!m.matches()) {
                continue;
            }
            Resolved r = switch (m.group(1)) {
                case "finding" -> finding(ref, m.group(2), seesPrivate, withheld);
                case "question" -> question(ref, m.group(2), seesPrivate);
                case "forecast" -> forecast(ref, m.group(2), seesPrivate);
                case "hypothesis-set" -> set(ref, m.group(2), seesPrivate);
                case "base-rate" -> baseRate(ref, m.group(2));
                default -> null;
            };
            if (r != null) {
                out.add(r);
            }
        }
        return out;
    }

    private Resolved finding(String ref, String id, boolean seesPrivate, boolean withheld) {
        FindingService service = findings.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            AnalyticFindingEntity f = service.visible(id, seesPrivate);
            return new Resolved(ref, "finding", envelope(ref, "finding", f.getTitle(), f.getStatus(), f.getSeverity(),
                    f.isPrivateFinding(), f.getManifestId(), f.getAsOfSeq()), FindingViews.full(f, f.getSituationId(),
                    withheld), provenance(f.getManifestId(), f.getAsOfSeq()), f.getManifestId(), f.getGeom());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Resolved question(String ref, String id, boolean seesPrivate) {
        try {
            ForecastQuestionEntity q = ledger.get(id, seesPrivate);
            Map<String, Object> data = new LinkedHashMap<>(ledger.view(q));
            List<Map<String, Object>> fs = new ArrayList<>();
            for (ForecastEntity f : ledger.latestByForecaster(q.getId()).values()) {
                fs.add(ForecastViews.forecast(f));
            }
            data.put("latestForecasts", fs);
            List<Map<String, Object>> ss = new ArrayList<>();
            for (ForecastScoreEntity s : scores.findByQuestionId(q.getId())) {
                ss.add(ForecastViews.score(s));
            }
            data.put("scores", ss);
            return new Resolved(ref, "question", envelope(ref, "question", q.getTitle(), q.getStatus(), null,
                    q.isPrivateQuestion(), q.getManifestId(), null), data, provenance(q.getManifestId(), null),
                    q.getManifestId(), null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Resolved forecast(String ref, String id, boolean seesPrivate) {
        ForecastEntity f = forecasts.findById(id).orElse(null);
        if (f == null) {
            return null;
        }
        ForecastQuestionEntity q = ledger.find(f.getQuestionId()).orElse(null);
        if (q == null || (q.isPrivateQuestion() && !seesPrivate)) {
            return null;
        }
        Map<String, Object> data = new LinkedHashMap<>(ForecastViews.forecast(f));
        return new Resolved(ref, "forecast", envelope(ref, "forecast", "Forecast by " + f.getForecasterId() + " on "
                + q.getTitle(), null, null, q.isPrivateQuestion(), f.getManifestId(), f.getAsOfSeq()), data,
                provenance(f.getManifestId(), f.getAsOfSeq()), f.getManifestId(), null);
    }

    private Resolved set(String ref, String id, boolean seesPrivate) {
        try {
            HypothesisSetEntity s = hypotheses.get(id, seesPrivate);
            Map<String, Object> data = new LinkedHashMap<>(hypotheses.view(s, true));
            List<Map<String, Object>> revs = new ArrayList<>();
            for (HypothesisRevisionEntity r : revisions.findBySetIdOrderByIdDesc(id, Limit.of(REVISIONS_PER_SET))) {
                revs.add(HypothesisViews.revision(r));
            }
            data.put("recentRevisions", revs);
            return new Resolved(ref, "hypothesis-set", envelope(ref, "hypothesis-set", s.getQuestion(), s.getStatus(),
                    null, s.isPrivateSet(), null, null), data, provenance(null, null), null, null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Resolved baseRate(String ref, String id) {
        BaseRateService service = baseRates.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            BaseRateEntity b = service.get(id);
            return new Resolved(ref, "base-rate", envelope(ref, "base-rate", "Base rate " + id, null, null, false,
                    b.getManifestId(), null), BaseRateViews.view(b, service, EgressPath.API_READ),
                    provenance(b.getManifestId(), null), b.getManifestId(), null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Map<String, Object> envelope(String ref, String type, String title, String status, String severity,
            boolean privateObject, String manifestId, Long asOfSeq) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ref", ref);
        out.put("type", type);
        out.put("title", title == null ? ref : title);
        out.put("textOrigin", "deterministic");
        out.put("status", status);
        out.put("severity", severity);
        out.put("private", privateObject);
        out.put("provenance", provenance(manifestId, asOfSeq));
        return out;
    }

    private static Map<String, Object> provenance(String manifestId, Long asOfSeq) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceIds", List.of());
        out.put("manifestId", manifestId);
        out.put("asOfSeq", asOfSeq);
        return out;
    }

    private List<Map<String, Object>> manifestViews(List<Resolved> objects, AssessmentEntity a) {
        ManifestService service = manifests.getIfAvailable();
        List<Map<String, Object>> out = new ArrayList<>();
        if (service == null) {
            return out;
        }
        Set<String> ids = new LinkedHashSet<>();
        if (a.getManifestId() != null) {
            ids.add(a.getManifestId());
        }
        for (Resolved r : objects) {
            if (r.manifestId() != null) {
                ids.add(r.manifestId());
            }
        }
        for (String id : ids) {
            service.find(id).ifPresent(m -> out.add(m.view()));
        }
        return out;
    }

    private static void deleteQuietly(Path dir) {
        if (dir == null) {
            return;
        }
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    // best effort
                }
            });
        } catch (IOException e) {
            // best effort
        }
    }
}
