package ai.foresignal.source.adapters.gvp;

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
 * {@code gvp-volcanoes}: the Smithsonian Global Volcanism Program's Holocene volcanoes (prompt 094), one
 * {@code volcano} record per feature with a point geometry.
 *
 * <p>One WFS GeoJSON response (about 2.4 MB, 1,214 features) fetched weekly with a conditional GET. Only the
 * {@code webservices.volcano.si.edu} host is used ({@code volcano.si.edu} itself challenges scripted clients).
 */
@Component
public class GvpVolcanoesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "gvp-volcanoes";

    /** The production WFS GetFeature URL. */
    public static final String WFS_URL =
            "https://webservices.volcano.si.edu/geoserver/GVP-VOTW/ows?service=WFS&version=2.0.0&request=GetFeature"
                    + "&typeName=GVP-VOTW:Smithsonian_VOTW_Holocene_Volcanoes&outputFormat=application/json";

    /** The test-only base-URL property. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.gvp-volcanoes.base-url";

    /** The {@code getConditional} state key. */
    public static final String STATE_KEY = "volcanoes";

    /** The attribution, including the VOTW version and DOI (read from the citation page 2026-10). */
    public static final String ATTRIBUTION = "Global Volcanism Program, Volcanoes of the World v5.4.0 (2026),"
            + " doi:10.5479/si.GVP.VOTW5-2026.5.4";

    private final URI wfsUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /** Creates the adapter. */
    @Autowired
    public GvpVolcanoesAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.wfsUri = URI.create(KitUrls.rebased(WFS_URL, baseUrlOverride));
    }

    /** The WFS URL this adapter fetches. */
    public URI wfsUri() {
        return wfsUri;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(wfsUri, STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200 || response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new UpstreamFormatException(ID + (response.status() != 200
                    ? ": the WFS answered HTTP " + response.status() : ": the WFS response exceeds the cap"), "");
        }
        List<ai.foresignal.source.spi.NormalizedRecord> records;
        try {
            records = GvpWfsParser.parse(response.body());
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        ReferenceSupport.emit(ctx, records);
        return FetchResult.complete(List.of(), release(response));
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
                .name("GVP Holocene volcanoes")
                .description("Holocene volcanoes from the Smithsonian Global Volcanism Program (VOTW v5.4.0),"
                        + " with a point geometry, type, last eruption year (negative for BCE), tectonic"
                        + " setting and a short geologic summary. An absent volcano from a complete run is"
                        + " tombstoned. Non-commercial; cite the VOTW DOI.")
                .kind(GvpWfsParser.KIND)
                .kind("dataset-release")
                .homepage("https://volcano.si.edu/database/webservices.cfm")
                .attribution(ATTRIBUTION)
                .licence("Smithsonian Terms of Use (non-commercial; cite VOTW DOI)")
                .termsUrl("https://volcano.si.edu/gvp_about.cfm")
                .titleField("name")
                .searchFields("name", "country", "region")
                .embedFields("summary")
                .tileProperties("name", "primaryType", "country")
                .kindDisplay(GvpWfsParser.KIND, new KindDisplay("{name}", "volcano", "#E8590C", null)));
        text(b, "name", "Name", "The volcano name");
        text(b, "landform", "Landform", "The volcano landform");
        text(b, "primaryType", "Type", "The primary volcano type");
        b.fieldSpec("lastEruptionYear", new FieldSpec(FieldType.INTEGER, null, "Last eruption", "The last eruption"
                + " year; negative for BCE", null, Set.of()));
        text(b, "country", "Country", "The country");
        text(b, "region", "Region", "The region");
        text(b, "subregion", "Subregion", "The subregion");
        b.fieldSpec("elevationM", new FieldSpec(FieldType.NUMBER, "m", "Elevation", "Elevation in metres (MSL)",
                "MSL", Set.of()));
        text(b, "tectonicSetting", "Tectonic setting", "The tectonic setting");
        text(b, "evidenceCategory", "Evidence", "The evidence category");
        text(b, "rockType", "Rock type", "The major rock type");
        text(b, "summary", "Summary", "The geologic summary (≤ 2 KB); upstream text, untrusted");
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
