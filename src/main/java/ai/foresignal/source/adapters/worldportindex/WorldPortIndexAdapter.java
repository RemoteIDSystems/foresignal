package ai.foresignal.source.adapters.worldportindex;

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
 * {@code world-port-index}: the NGA MSI World Port Index (prompt 094), one {@code port} point and one
 * buffered {@code port-area} polygon per port. Public domain (US Government).
 *
 * <p>Prompt 099 uses the {@code port-area} buffers. One 6.3 MB JSON response (2,951 ports) fetched weekly
 * with a conditional GET.
 */
@Component
public class WorldPortIndexAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "world-port-index";

    /** The production JSON. */
    public static final String WPI_URL = "https://msi.nga.mil/api/publications/world-port-index?output=json";

    /** The test-only base-URL property. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.world-port-index.base-url";

    /** The {@code getConditional} state key. */
    public static final String STATE_KEY = "wpi";

    private final URI wpiUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /** Creates the adapter. */
    @Autowired
    public WorldPortIndexAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.wpiUri = URI.create(KitUrls.rebased(WPI_URL, baseUrlOverride));
    }

    /** The WPI URL this adapter fetches. */
    public URI wpiUri() {
        return wpiUri;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(wpiUri, STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200 || response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new UpstreamFormatException(ID + (response.status() != 200
                    ? ": the WPI answered HTTP " + response.status() : ": the WPI response exceeds the cap"), "");
        }
        List<ai.foresignal.source.spi.NormalizedRecord> records;
        try {
            records = WpiParser.parse(response.body());
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        ReferenceSupport.emit(ctx, records);
        return FetchResult.complete(List.of(), release(response));
    }

    private static String release(UpstreamResponse response) {
        String modified = response.header("Last-Modified");
        if (modified != null && !modified.isBlank()) {
            return modified;
        }
        return response.header("ETag");
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = ReferenceSupport.defaults(SourceDescriptor.builder(ID)
                .name("NGA World Port Index")
                .description("Ports from the NGA MSI World Port Index, with a decimal point geometry and a"
                        + " buffered port-area polygon (a 64-vertex geodesic circle by harbor size) for prompt"
                        + " 099. Public domain (US Government). An absent port from a complete run is"
                        + " tombstoned.")
                .kind(WpiParser.PORT_KIND)
                .kind(WpiParser.AREA_KIND)
                .kind("dataset-release")
                .homepage("https://msi.nga.mil/Publications/WPI")
                .attribution("National Geospatial-Intelligence Agency (NGA), World Port Index")
                .licence("Public domain (US Government)")
                .termsUrl("https://msi.nga.mil/Publications/WPI")
                .titleField("portName")
                .searchFields("portName", "countryName", "unloCode")
                .tileProperties("portName", "harborSize", "countryCode")
                .kindDisplay(WpiParser.PORT_KIND, new KindDisplay("{portName}", "harbor", "#1971C2", null))
                .kindDisplay(WpiParser.AREA_KIND, new KindDisplay("Area of {portNumber}", "harbor", "#1C7ED6",
                        null)));
        text(b, "portName", "Port", "The port name");
        text(b, "countryCode", "Country code", "The country code");
        text(b, "countryName", "Country", "The country name");
        text(b, "unloCode", "UN/LOCODE", "The UN/LOCODE");
        text(b, "regionName", "Region", "The region name");
        text(b, "harborSize", "Harbor size", "V, S, M or L");
        text(b, "harborType", "Harbor type", "The harbor type");
        text(b, "harborUse", "Harbor use", "The harbor use");
        text(b, "shelter", "Shelter", "The shelter rating");
        number(b, "channelDepthM", "Channel depth", "Channel depth, metres", "m");
        number(b, "anchorageDepthM", "Anchorage depth", "Anchorage depth, metres", "m");
        number(b, "maxVesselLengthM", "Max vessel length", "Maximum vessel length, metres", "m");
        number(b, "maxVesselDraftM", "Max vessel draft", "Maximum vessel draft, metres", "m");
        text(b, "alternateName", "Alternate name", "An alternate port name");
        number(b, "bufferKm", "Buffer radius", "The port-area buffer radius, kilometres", "km");
        text(b, "portNumber", "Port number", "The WPI port number");
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    private static void number(SourceDescriptor.Builder b, String key, String label, String description,
            String unit) {
        b.fieldSpec(key, new FieldSpec(FieldType.NUMBER, unit, label, description, null, Set.of()));
    }
}
