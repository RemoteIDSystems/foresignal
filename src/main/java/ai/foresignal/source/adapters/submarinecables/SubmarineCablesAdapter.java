package ai.foresignal.source.adapters.submarinecables;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.NormalizedRecord;
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
 * {@code submarine-cables}: TeleGeography's public Submarine Cable Map data (prompt 094). Disabled by default.
 *
 * <p>The JSON is the public map's undocumented data and the geometry is stylised; the citation page states
 * CC BY-SA 4.0 for the public maps and the underlying database is subscriber-only, so the source is
 * non-commercial and its egress is local-only for prompts 109 and 110.
 */
@Component
public class SubmarineCablesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "submarine-cables";

    /** The cable GeoJSON. */
    public static final String CABLE_URL = "https://www.submarinecablemap.com/api/v3/cable/cable-geo.json";

    /** The landing-point GeoJSON. */
    public static final String LANDING_URL =
            "https://www.submarinecablemap.com/api/v3/landing-point/landing-point-geo.json";

    /** The test-only base-URL property. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.submarine-cables.base-url";

    /** The cable conditional-GET state key. */
    public static final String CABLE_STATE = "cable";

    /** The landing-point conditional-GET state key. */
    public static final String LANDING_STATE = "landing";

    private final URI cableUri;
    private final URI landingUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /** Creates the adapter. */
    @Autowired
    public SubmarineCablesAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.cableUri = URI.create(KitUrls.rebased(CABLE_URL, baseUrlOverride));
        this.landingUri = URI.create(KitUrls.rebased(LANDING_URL, baseUrlOverride));
    }

    /** The cable URL this adapter fetches. */
    public URI cableUri() {
        return cableUri;
    }

    /** The landing-point URL this adapter fetches. */
    public URI landingUri() {
        return landingUri;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        // Each file is its own kind, so a 304 on one leaves its records alone and the other is still reconciled
        // (FetchResult.completeFor; 2026-10 review).
        java.util.Set<String> fresh = new java.util.LinkedHashSet<>();
        List<String> releases = new java.util.ArrayList<>(2);

        UpstreamResponse cables = ctx.http().getConditional(cableUri, CABLE_STATE);
        if (!cables.notModified()) {
            requireComplete(cables, CABLE_STATE, ctx, "cable-geo.json");
            ReferenceSupport.ChunkEmitter chunks = ReferenceSupport.chunks(ctx);
            try {
                logCounts(ctx, "cable", SubmarineCableParser.parseCables(cables.body(), chunks));
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), CABLE_STATE);
                throw e;
            }
            chunks.flush();
            fresh.add(SubmarineCableParser.CABLE_KIND);
            releases.add(release(cables));
        }

        UpstreamResponse landings = ctx.http().getConditional(landingUri, LANDING_STATE);
        if (!landings.notModified()) {
            requireComplete(landings, LANDING_STATE, ctx, "landing-point-geo.json");
            ReferenceSupport.ChunkEmitter chunks = ReferenceSupport.chunks(ctx);
            try {
                logCounts(ctx, "landing point", SubmarineCableParser.parseLandings(landings.body(), chunks));
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), LANDING_STATE);
                throw e;
            }
            chunks.flush();
            fresh.add(SubmarineCableParser.LANDING_KIND);
            releases.add(release(landings));
        }

        if (fresh.isEmpty()) {
            return FetchResult.unchanged();
        }
        return FetchResult.completeFor(fresh, List.of(), String.join(";", releases));
    }

    private static void logCounts(FetchContext ctx, String what, ReferenceSupport.ParseCounts counts) {
        if (counts.skipped() > 0 || counts.badGeometry() > 0) {
            ctx.log().info("{}: {} {}(s), {} without an id skipped, {} malformed geometry(ies) kept without geometry",
                    ID, counts.records(), what, counts.skipped(), counts.badGeometry());
        }
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
                .name("Submarine cables")
                .description("Submarine cables and their landing points from TeleGeography's public Submarine"
                        + " Cable Map. The geometry is stylised and the underlying data is subscriber-only:"
                        + " display only, attribute TeleGeography, non-commercial.")
                .kind(SubmarineCableParser.CABLE_KIND)
                .kind(SubmarineCableParser.LANDING_KIND)
                .kind("dataset-release")
                .homepage("https://www.submarinecablemap.com/")
                .attribution("TeleGeography submarine cable map (display only, attribute TeleGeography)")
                .licence("CC BY-SA 4.0 (TeleGeography public map; underlying data restricted) — treat as"
                        + " non-commercial")
                .termsUrl("https://www2.telegeography.com/cite-telegeography-map")
                .egress(Egress.LOCAL_ONLY)
                .titleField("name")
                .searchFields("name")
                .tileProperties("name", "color")
                .kindDisplay(SubmarineCableParser.CABLE_KIND, new KindDisplay("{name}", "water", "#0CA678", null))
                .kindDisplay(SubmarineCableParser.LANDING_KIND,
                        new KindDisplay("{name}", "water", "#099268", null))
                .kindPresentation(SubmarineCableParser.CABLE_KIND, java.util.Map.of("nonCommercial", true))
                .kindPresentation(SubmarineCableParser.LANDING_KIND, java.util.Map.of("nonCommercial", true)));
        b.fieldSpec("name", new FieldSpec(FieldType.STRING, null, "Name", "The cable or landing name", null,
                Set.of()));
        b.fieldSpec("color", new FieldSpec(FieldType.STRING, null, "Color", "The map colour", null, Set.of()));
        b.fieldSpec("isTbd", new FieldSpec(FieldType.BOOLEAN, null, "To be determined", "The cable's route is not"
                + " final", null, Set.of()));
        return b.build();
    }
}
