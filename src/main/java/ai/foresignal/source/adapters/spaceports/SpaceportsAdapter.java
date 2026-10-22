package ai.foresignal.source.adapters.spaceports;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.ReferenceSupport;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code spaceports}: launch sites and pads from Jonathan McDowell's GCAT (prompt 094), using the
 * {@code sites.tsv} and {@code lp.tsv} tables.
 *
 * <p>Launch Library 2 is not used (064 already covers launches). Coordinates may be missing; a record is
 * still emitted without geometry. Each table is its own kind and is reconciled when it arrived fresh; a table
 * that answered 304 keeps its records (2026-10 review).
 */
@Component
public class SpaceportsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "spaceports";

    /** The production sites table. */
    public static final String SITES_URL = "https://planet4589.org/space/gcat/tsv/tables/sites.tsv";

    /** The production launch-pad table. */
    public static final String PADS_URL = "https://planet4589.org/space/gcat/tsv/tables/lp.tsv";

    /** The test-only base-URL property. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.spaceports.base-url";

    /** The sites conditional-GET state key. */
    public static final String SITES_STATE = "sites";

    /** The pads conditional-GET state key. */
    public static final String PADS_STATE = "pads";

    private final URI sitesUri;
    private final URI padsUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /** Creates the adapter. */
    @Autowired
    public SpaceportsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.sitesUri = URI.create(KitUrls.rebased(SITES_URL, baseUrlOverride));
        this.padsUri = URI.create(KitUrls.rebased(PADS_URL, baseUrlOverride));
    }

    /** The sites URL this adapter fetches. */
    public URI sitesUri() {
        return sitesUri;
    }

    /** The pads URL this adapter fetches. */
    public URI padsUri() {
        return padsUri;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        // Each table is its own kind, so a 304 on one leaves its records alone and the other is still reconciled
        // (FetchResult.completeFor; 2026-10 review).
        java.util.Set<String> fresh = new java.util.LinkedHashSet<>();
        List<String> releases = new java.util.ArrayList<>(2);

        UpstreamResponse sites = ctx.http().getConditional(sitesUri, SITES_STATE);
        if (!sites.notModified()) {
            requireComplete(sites, SITES_STATE, ctx, "sites.tsv");
            ReferenceSupport.ChunkEmitter chunks = ReferenceSupport.chunks(ctx);
            try {
                GcatParser.parseSites(sites.body(), chunks);
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), SITES_STATE);
                throw e;
            }
            chunks.flush();
            fresh.add(GcatParser.SITE_KIND);
            releases.add(release(sites));
        }

        UpstreamResponse pads = ctx.http().getConditional(padsUri, PADS_STATE);
        if (!pads.notModified()) {
            requireComplete(pads, PADS_STATE, ctx, "lp.tsv");
            ReferenceSupport.ChunkEmitter chunks = ReferenceSupport.chunks(ctx);
            try {
                GcatParser.parsePads(pads.body(), chunks);
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), PADS_STATE);
                throw e;
            }
            chunks.flush();
            fresh.add(GcatParser.PAD_KIND);
            releases.add(release(pads));
        }

        if (fresh.isEmpty()) {
            return FetchResult.unchanged();
        }
        return FetchResult.completeFor(fresh, List.of(), String.join(";", releases));
    }

    private static void requireComplete(UpstreamResponse response, String stateKey, FetchContext ctx, String what) {
        if (response.status() != 200 || response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), stateKey);
            throw new UpstreamFormatException(ID + (response.status() != 200
                    ? ": " + what + " answered HTTP " + response.status()
                    : ": " + what + " exceeds the response cap"), "");
        }
    }

    private static String release(UpstreamResponse response) {
        String etag = response.header("ETag");
        if (etag != null && !etag.isBlank()) {
            return etag;
        }
        return response.header("Last-Modified");
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = ReferenceSupport.defaults(SourceDescriptor.builder(ID)
                .name("GCAT launch sites and pads")
                .description("Launch sites and pads from Jonathan McDowell's GCAT tables (sites.tsv and lp.tsv):"
                        + " code, type, state, name, active period, parent and error radius, with a point"
                        + " geometry where GCAT has one. CC BY 4.0. A site or pad absent from a complete run is"
                        + " tombstoned.")
                .kind(GcatParser.SITE_KIND)
                .kind(GcatParser.PAD_KIND)
                .kind("dataset-release")
                .homepage("https://planet4589.org/space/gcat/")
                .attribution("Data from J. McDowell, planet4589.org")
                .licence("CC BY 4.0")
                .termsUrl("https://planet4589.org/space/gcat/web/cat/index.html")
                .titleField("name")
                .searchFields("name", "code", "stateCode")
                .tileProperties("name", "type", "stateCode")
                .kindDisplay(GcatParser.SITE_KIND, new KindDisplay("{name}", "rocket", "#F76707", null))
                .kindDisplay(GcatParser.PAD_KIND, new KindDisplay("{name}", "rocket", "#F59F00", null)));
        b.fieldSpec("code", new FieldSpec(FieldType.STRING, null, "Code", "The site or pad code", null, Set.of()));
        b.fieldSpec("type", new FieldSpec(FieldType.STRING, null, "Type", "The site or pad type", null, Set.of()));
        b.fieldSpec("stateCode", new FieldSpec(FieldType.STRING, null, "State code", "The state code", null,
                Set.of()));
        b.fieldSpec("name", new FieldSpec(FieldType.STRING, null, "Name", "The site or pad name", null, Set.of()));
        b.fieldSpec("tStart", new FieldSpec(FieldType.STRING, null, "Active from", "The start of operation", null,
                Set.of()));
        b.fieldSpec("tStop", new FieldSpec(FieldType.STRING, null, "Active to", "The end of operation", null,
                Set.of()));
        b.fieldSpec("parent", new FieldSpec(FieldType.STRING, null, "Parent", "The parent site", null, Set.of()));
        b.fieldSpec("errorKm", new FieldSpec(FieldType.NUMBER, "km", "Error", "The coordinate error, kilometers",
                null, Set.of()));
        return b.build();
    }
}
