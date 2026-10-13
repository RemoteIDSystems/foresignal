/*
 * The fetch rules (redirects refused, the Digitraffic-User header, TfL's optional app_key, OGC rel=next paging,
 * DATEX locations joined with images) are ported from God's Eye View, server/providers/cctv/sources.js and
 * server/providers/cctv/catalog.js, https://github.com/bilawalsidhu/gods-eye-view,
 * commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java on Foresignal's budgeted upstream client (conditional GETs, the descriptor's cap and timeout, the
 * redacted credential); no frame proxy, stream relay, ground heights, source cap or distance prioritization;
 * absent cameras are tombstoned after two complete fetches.
 */
package ai.foresignal.source.adapters.cctv;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.AbsenceTombstoner;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.locationtech.jts.geom.Envelope;

/**
 * One {@code cctv-*} source (prompt 072): a reviewed camera-catalog pack ({@link CctvPack}) as {@code camera}
 * records ({@link CctvDescriptors#descriptor}, {@link CameraRecordMapper}).
 *
 * <p><strong>Each run</strong> reads the whole catalog:
 * <ul>
 *   <li>a <strong>bundled</strong> pack reads its classpath resource and makes no request;
 *   <li>a pack with <strong>one catalog URL</strong> makes one conditional GET (a 304 is
 *       {@link FetchResult#unchanged()});
 *   <li>a <strong>paginated</strong> pack (Vegvesen) follows OGC {@code rel=next} links on the same host and path,
 *       at most {@link CctvPack#maxPages()} pages; a catalog still unfinished after the last page is returned as a
 *       partial result and tombstones nothing;
 *   <li>a pack with <strong>several catalog URLs</strong> (Estonia: locations and images) requests each and hands
 *       the later bodies to the parser.
 * </ul>
 * A credential, when the pack has one, is added as its query parameter ({@code app_key} for TfL, {@code key} for
 * Ontario 511); the client redacts its value from every trace. Extra headers come from the pack
 * ({@code Digitraffic-User: Foresignal/<version>}); the User-Agent is always Foresignal's own.
 *
 * <p><strong>Failures:</strong> a 3xx ({@code redirect refused}), another non-200 status, a truncated body or a
 * document that does not parse is an {@link UpstreamFormatException}, and the stored validators are forgotten.
 * Error statuses (for example Ontario 511's {@code 400 Invalid Key}) surface from the client as
 * {@code UpstreamHttpException}.
 *
 * <p><strong>Absence:</strong> only after a complete, non-empty fetch, a camera missing from two consecutive complete
 * fetches is tombstoned ({@link AbsenceTombstoner}). A 304, a truncated or unparseable body, an empty catalog or an
 * unfinished paginated catalog tombstones nothing.
 *
 * <p>Skipped cameras are counted in {@value #SKIPPED_METRIC}, tagged {@code source} and {@code reason}.
 */
public class CctvPackAdapter implements PollingAdapter {

    /** The counter of cameras skipped by the parser or the mapper. */
    public static final String SKIPPED_METRIC = "foresignal.cctv.skipped";

    /** The {@code getConditional} state key of the catalog. */
    public static final String STATE_KEY = "catalog";

    /** The placeholder of Foresignal's version in a pack header value. */
    public static final String VERSION_PLACEHOLDER = "{version}";

    private final CctvPack pack;
    private final SourceDescriptor descriptor;
    private final Supplier<String> version;
    private final Supplier<MeterRegistry> meters;

    /**
     * Creates the source.
     *
     * @param pack the reviewed pack
     * @param version supplies Foresignal's version for header values, or null for {@code dev}
     * @param meters supplies the registry of the counters, or null
     */
    public CctvPackAdapter(CctvPack pack, Supplier<String> version, Supplier<MeterRegistry> meters) {
        this.pack = Objects.requireNonNull(pack);
        this.descriptor = CctvDescriptors.descriptor(pack);
        this.version = version == null ? () -> "dev" : version;
        this.meters = meters == null ? () -> null : meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The pack. */
    public CctvPack pack() {
        return pack;
    }

    /**
     * Fetches the whole catalog once (see the class note).
     *
     * @return {@link FetchResult#unchanged()} on a 304; otherwise the cameras plus any tombstones
     * @throws AdapterException when a request fails or a required credential is missing
     * @throws UpstreamFormatException on a redirect, a non-200 or truncated body, or a document that does not parse
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        Envelope bbox = ctx.params().has(CctvDescriptors.BBOX_PARAM)
                ? ctx.params().bbox(CctvDescriptors.BBOX_PARAM) : null;
        PackContext pc;
        List<CameraEntry> entries;
        boolean complete = true;
        if (pack.bundled()) {
            pc = PackContext.of(pack);
            entries = pack.parser().parse(readResource(pack.resource()), pc);
        } else if (pack.maxPages() > 1) {
            pc = PackContext.of(pack);
            entries = new ArrayList<>();
            complete = readPages(ctx, pc, entries);
        } else if (pack.catalogUrls().size() > 1) {
            List<byte[]> bodies = new ArrayList<>();
            for (int i = 0; i < pack.catalogUrls().size(); i++) {
                bodies.add(checked(ctx, ctx.http().get(url(ctx, pack.catalogUrls().get(i)), headers()), null));
            }
            pc = new PackContext(pack, bodies.subList(1, bodies.size()));
            entries = pack.parser().parse(bodies.get(0), pc);
        } else {
            UpstreamResponse response = ctx.http().getConditional(url(ctx, pack.catalogUrls().get(0)), STATE_KEY,
                    headers());
            if (response.notModified()) {
                AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.NOT_MODIFIED, List.of());
                return FetchResult.unchanged();
            }
            byte[] body = checked(ctx, response, STATE_KEY);
            pc = PackContext.of(pack);
            try {
                entries = pack.parser().parse(body, pc);
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
                throw e;
            }
        }
        CameraRecordMapper.Mapped mapped = CameraRecordMapper.mapAll(pack, entries, bbox, pc);
        count(ctx, pc);
        ctx.log().debug("{}: {} camera(s), {} outside bbox, skipped {}", ctx.sourceId(), mapped.records().size(),
                mapped.outsideBbox(), pc.skipped());
        if (!complete) {
            ctx.log().warn("{}: the catalog has more than {} pages; returning a partial set and tombstoning nothing",
                    ctx.sourceId(), pack.maxPages());
            return FetchResult.of(mapped.records());
        }
        if (mapped.records().isEmpty()) {
            // An empty catalog never retires cameras: it is far likelier an upstream fault than every camera gone.
            return FetchResult.complete(List.of());
        }
        List<String> ids = mapped.records().stream().map(NormalizedRecord::externalId).toList();
        List<String> gone = AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.COMPLETE, ids);
        List<NormalizedRecord> out = new ArrayList<>(mapped.records());
        out.addAll(AbsenceTombstoner.tombstones(gone, CameraRecordMapper.KIND, now));
        if (!gone.isEmpty()) {
            ctx.log().debug("{}: tombstoned {} camera(s) missing from two complete fetches", ctx.sourceId(),
                    gone.size());
        }
        return FetchResult.complete(out);
    }

    /**
     * Reads every page of a paginated catalog into {@code entries}.
     *
     * @return true when the last page had no {@code rel=next} link
     */
    private boolean readPages(FetchContext ctx, PackContext pc, List<CameraEntry> entries) throws AdapterException {
        URI first = url(ctx, pack.catalogUrls().get(0));
        URI page = first;
        for (int n = 1; n <= pack.maxPages(); n++) {
            byte[] body = checked(ctx, ctx.http().get(page, headers()), null);
            entries.addAll(pack.parser().parse(body, pc));
            URI next = pc.nextPage();
            if (next == null) {
                return true;
            }
            if (!sameEndpoint(first, next)) {
                throw new UpstreamFormatException(pack.id() + ": next link leaves the catalog endpoint ("
                        + next.getHost() + next.getPath() + ")", "");
            }
            page = next;
        }
        return false;
    }

    /** True when {@code next} has the scheme, host, port and path of {@code first}. */
    static boolean sameEndpoint(URI first, URI next) {
        return next.isAbsolute() && Objects.equals(first.getScheme(), next.getScheme())
                && first.getHost() != null && first.getHost().equalsIgnoreCase(next.getHost())
                && first.getPort() == next.getPort() && Objects.equals(first.getPath(), next.getPath())
                && next.getRawUserInfo() == null;
    }

    /**
     * The body of a 200 response.
     *
     * @param stateKey the validators to forget on failure, or null
     * @throws UpstreamFormatException on a 3xx, another status or a truncated body
     */
    private byte[] checked(FetchContext ctx, UpstreamResponse response, String stateKey) {
        int status = response.status();
        if (status >= 300 && status < 400) {
            throw new UpstreamFormatException(pack.id() + ": redirect refused", "");
        }
        if (status != 200) {
            forget(ctx, stateKey);
            throw new UpstreamFormatException(pack.id() + ": the catalog answered HTTP " + status, "");
        }
        if (response.truncated()) {
            forget(ctx, stateKey);
            AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.TRUNCATED, List.of());
            throw new UpstreamFormatException(pack.id() + ": the catalog exceeds " + pack.maxResponseBytes()
                    + " bytes", "");
        }
        return response.body();
    }

    private static void forget(FetchContext ctx, String stateKey) {
        if (stateKey != null) {
            SourceHttp.forgetValidators(ctx.state(), stateKey);
        }
    }

    /**
     * {@code base} with the pack's credential added as its query parameter.
     *
     * @throws AdapterException when the credential is required and missing
     */
    URI url(FetchContext ctx, URI base) throws AdapterException {
        if (pack.credential() == null) {
            return base;
        }
        String value = ctx.credentials() == null ? null : ctx.credentials().get(pack.credential().name());
        if (value == null || value.isBlank()) {
            if (pack.credential().required()) {
                throw new AdapterException(pack.id() + " needs its " + pack.credential().name() + " credential");
            }
            return base;
        }
        String separator = base.getRawQuery() == null ? "?" : "&";
        return URI.create(base + separator + pack.credentialQueryParam() + "="
                + URLEncoder.encode(value.strip(), StandardCharsets.UTF_8));
    }

    /** The pack's extra headers with {@value #VERSION_PLACEHOLDER} filled in. */
    Map<String, String> headers() {
        Map<String, String> out = new LinkedHashMap<>();
        pack.headers().forEach((k, v) -> out.put(k, v.replace(VERSION_PLACEHOLDER, version.get())));
        return out;
    }

    private static byte[] readResource(String resource) {
        try (InputStream in = CctvPackAdapter.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new UpstreamFormatException("missing bundled catalog " + resource, "");
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + resource, e);
        }
    }

    private void count(FetchContext ctx, PackContext pc) {
        MeterRegistry registry = meters.get();
        if (registry == null) {
            return;
        }
        pc.skipped().forEach((reason, amount) -> Counter.builder(SKIPPED_METRIC).tag("source", ctx.sourceId())
                .tag("reason", reason).description("CCTV catalog cameras skipped by the parser or the mapper")
                .register(registry).increment(amount));
    }
}
