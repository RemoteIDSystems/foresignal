package ai.foresignal.source.kit.feature;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.AbsenceTombstoner;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import tools.jackson.databind.JsonNode;

/**
 * One feature-kit source (prompt 073): a reviewed {@link FeatureSourceSpec} read through the protocol's client and
 * mapped by {@link FeatureMapper}.
 *
 * <p><strong>Each run:</strong>
 * <ol>
 *   <li>the client for the spec's protocol ({@link ArcGisQueryClient}, {@link OgcFeaturesClient},
 *       {@link WfsJsonClient}) fetches the layer, page by page, within {@code maxPages} and {@code maxFeatures};
 *   <li>an unchanged ArcGIS layer is {@link FetchResult#unchanged()} with no query sent;
 *   <li>each feature becomes a record, or is rejected and counted in {@value #REJECTED_METRIC}
 *       ({@code source}, {@code reason}); a repeated id keeps its first feature;
 *   <li>with {@code absenceTombstones}, a feature missing from two consecutive <em>complete</em> fetches is tombstoned
 *       ({@link AbsenceTombstoner}); a truncated fetch (a cap hit) or an empty layer tombstones nothing.
 * </ol>
 *
 * <p>A credential, when the spec declares one, goes into the query string or a header; the upstream client redacts
 * it from every trace. A missing required credential fails before any request.
 */
public class KitFeatureAdapter implements PollingAdapter {

    /** The counter of features the mapper rejected. */
    public static final String REJECTED_METRIC = "foresignal.kit.features.rejected";

    private final FeatureSourceSpec spec;
    private final SourceDescriptor descriptor;
    private final Supplier<MeterRegistry> meters;
    private final ArcGisQueryClient arcgis = new ArcGisQueryClient();
    private final OgcFeaturesClient ogc = new OgcFeaturesClient();
    private final WfsJsonClient wfs = new WfsJsonClient();

    /**
     * Creates the source.
     *
     * @param spec the validated spec, with any test rebasing already applied
     * @param meters supplies the meter registry, or null
     */
    public KitFeatureAdapter(FeatureSourceSpec spec, Supplier<MeterRegistry> meters) {
        this.spec = Objects.requireNonNull(spec);
        this.descriptor = FeatureDescriptors.descriptor(spec);
        this.meters = meters == null ? () -> null : meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The spec. */
    public FeatureSourceSpec spec() {
        return spec;
    }

    /**
     * Fetches the layer once (see the class note).
     *
     * @throws AdapterException when a request fails or a required credential is missing
     * @throws UpstreamFormatException on a redirect, a bad status or body, or a page that is not a feature collection
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        Map<String, String> headers = new LinkedHashMap<>(spec.headers());
        Map<String, String> extraQuery = new LinkedHashMap<>();
        credential(ctx, headers, extraQuery);
        FeatureFetch fetch;
        try {
            fetch = switch (spec.protocol()) {
                case FeatureSourceSpec.ARCGIS -> arcgis.fetch(ctx, arcgisRequest(headers, extraQuery));
                case FeatureSourceSpec.OGC -> ogc.fetch(ctx, ogcRequest(headers, extraQuery));
                case FeatureSourceSpec.WFS -> wfs.fetch(ctx, wfsRequest(headers, extraQuery));
                default -> throw new AdapterException(spec.id() + ": unknown protocol " + spec.protocol());
            };
        } catch (UpstreamFormatException e) {
            AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.PARSE_ERROR, List.of());
            throw e;
        }
        if (fetch.unchanged()) {
            AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.NOT_MODIFIED, List.of());
            return FetchResult.unchanged();
        }
        List<NormalizedRecord> records = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Map<String, Integer> rejected = new TreeMap<>();
        for (JsonNode feature : fetch) {
            FeatureMapper.Mapped mapped = FeatureMapper.map(spec, feature);
            if (!mapped.accepted()) {
                rejected.merge(mapped.reason(), 1, Integer::sum);
            } else if (ids.add(mapped.record().externalId())) {
                records.add(mapped.record());
            } else {
                rejected.merge("duplicate-id", 1, Integer::sum);
            }
        }
        count(ctx, rejected);
        ctx.log().debug("{}: {} feature(s) in {} page(s) ({}), {} record(s), rejected {}", ctx.sourceId(),
                fetch.size(), fetch.pages(), fetch.stopReason(), records.size(), rejected);
        if (!fetch.complete()) {
            AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.TRUNCATED, List.of());
            ctx.log().warn("{}: the fetch stopped early ({}); returning a partial set and tombstoning nothing",
                    ctx.sourceId(), fetch.stopReason());
            return FetchResult.of(records);
        }
        if (!spec.tombstonesAbsent() || records.isEmpty()) {
            // An empty layer never retires features: far likelier an upstream fault than everything gone.
            return FetchResult.complete(records);
        }
        List<String> gone = AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.COMPLETE,
                records.stream().map(NormalizedRecord::externalId).toList());
        List<NormalizedRecord> out = new ArrayList<>(records);
        out.addAll(AbsenceTombstoner.tombstones(gone, spec.kind(), ctx.now()));
        return FetchResult.complete(out);
    }

    private ArcGisQueryClient.Request arcgisRequest(Map<String, String> headers, Map<String, String> extraQuery) {
        String url = spec.url();
        if (spec.layer() != null) {
            url = url.replaceAll("/+$", "") + "/" + spec.layer();
        }
        List<String> outFields = spec.query().outFields().isEmpty() ? List.copyOf(spec.fields().keySet())
                : spec.query().outFields();
        return new ArcGisQueryClient.Request(URI.create(url), spec.query().where(), outFields,
                spec.paging().effectivePageSize(), spec.paging().effectiveMaxPages(),
                spec.paging().effectiveMaxFeatures(), spec.geometry().maxAllowableOffset(),
                spec.geometry().geometryPrecision(), FeatureSourceSpec.ESRIJSON.equals(spec.effectiveFormat()),
                headers, extraQuery);
    }

    private OgcFeaturesClient.Request ogcRequest(Map<String, String> headers, Map<String, String> extraQuery) {
        return new OgcFeaturesClient.Request(URI.create(spec.url()), spec.collection(),
                spec.paging().effectivePageSize(), spec.paging().effectiveMaxPages(),
                spec.paging().effectiveMaxFeatures(), spec.query().bbox(), spec.query().datetime(),
                spec.query().filters(), headers, extraQuery);
    }

    private WfsJsonClient.Request wfsRequest(Map<String, String> headers, Map<String, String> extraQuery) {
        return new WfsJsonClient.Request(URI.create(spec.url()), spec.typeName(), spec.paging().effectivePageSize(),
                spec.paging().effectiveMaxPages(), spec.paging().effectiveMaxFeatures(), spec.geometry().latLon(),
                spec.query().bbox(), spec.query().filters(), headers, extraQuery, spec.id());
    }

    /**
     * Adds the spec's credential to the headers or the query.
     *
     * @throws AdapterException when it is required and missing
     */
    private void credential(FetchContext ctx, Map<String, String> headers, Map<String, String> extraQuery)
            throws AdapterException {
        FeatureSourceSpec.CredentialSpec c = spec.credential();
        if (c == null) {
            return;
        }
        String value = ctx.credentials() == null ? null : ctx.credentials().get(c.name());
        if (value == null || value.isBlank()) {
            if (Boolean.TRUE.equals(c.required())) {
                throw new AdapterException(spec.id() + " needs its " + c.name() + " credential");
            }
            return;
        }
        if (c.inQuery()) {
            extraQuery.put(c.param(), value.strip());
        } else {
            headers.put(c.param(), value.strip());
        }
    }

    private void count(FetchContext ctx, Map<String, Integer> rejected) {
        MeterRegistry registry = meters.get();
        if (registry == null || rejected.isEmpty()) {
            return;
        }
        rejected.forEach((reason, n) -> Counter.builder(REJECTED_METRIC).tag("source", ctx.sourceId())
                .tag("reason", reason).description("Feature-kit features the mapper rejected").register(registry)
                .increment(n));
    }

    /**
     * {@code spec} with its URL moved under a test base URL ({@link KitUrls#rebased}).
     *
     * @param spec the spec
     * @param base the test base URL, or blank
     */
    public static FeatureSourceSpec rebased(FeatureSourceSpec spec, String base) {
        if (base == null || base.isBlank()) {
            return spec;
        }
        return new FeatureSourceSpec(spec.id(), spec.name(), spec.description(), spec.kind(), spec.protocol(),
                KitUrls.rebased(spec.url(), base), spec.layer(), spec.collection(), spec.typeName(), spec.format(),
                spec.query(), spec.idField(), spec.titleField(), spec.fields(), spec.constants(), spec.time(),
                spec.recordTtl(), spec.ttlAnchor(), spec.interval(), spec.minInterval(), spec.budget(), spec.paging(),
                spec.geometry(), spec.absenceTombstones(), spec.zeroResultsNormal(), spec.significantFields(),
                spec.volatileFields(), spec.searchFields(), spec.tileProperties(), spec.license(), spec.termsUrl(),
                spec.attribution(), spec.homepage(), spec.headers(), spec.credential(), spec.retention(),
                spec.maxResponseBytes());
    }
}
