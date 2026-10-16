package ai.foresignal.source.adapters.goes;

import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.PollingAdapter;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * GOES-West (GOES-18) ABI band 13 (prompt 106; Weather-Imagery-Design §5.3): the same as
 * {@code goes-east-abi}'s clean IR, on {@code noaa-goes18}, with its own {@code geos} grid read from
 * the file (GOES-West's longitude). Off by default on the small profile and on by default on the
 * large profile (prompt 106 task 1); the {@code products} param overrides either.
 */
@Component
public class GoesWestAbiAdapter extends GoesAbiAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "goes-west-abi";

    /** The GOES-18 bucket. */
    public static final String BUCKET = "noaa-goes18";

    /** The band 13 product. */
    public static final String IR = "goes-west-ir";

    /** Test-only: a path-style base URL for object downloads. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.goes-west.base-url";

    /** The channel. */
    static final List<Channel> CHANNELS = List.of(
            channel(IR, "CMIPC", "13", "", "ir", "CONUS", 7, 6, "ir-gray@1", "ir-enhanced@1",
                    "GOES-West clean infrared (ABI band 13)", "Brightness temperature at 10.3 µm, K; cold (bright) is"
                            + " high cloud", Duration.ofHours(24), false, false, 1));

    /** The products on by default for the small profile: none. */
    public static final List<String> LIGHT_DEFAULT = List.of();

    /** The products on by default for the large profile: GOES-18 C13. */
    public static final List<String> LARGE_DEFAULT = List.of(IR);

    /** Creates the GOES-West adapter. */
    @Autowired
    public GoesWestAbiAdapter(NetcdfDecoder decoder, @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            @Value("${foresignal.profile:small}") String profile, ObjectProvider<AreaOfInterest> aoi) {
        super(decoder, baseUrlOverride, profile, aoi, ID, BUCKET, "G18", "GOES-West (GOES-18)", CHANNELS,
                LIGHT_DEFAULT, LARGE_DEFAULT);
    }
}
