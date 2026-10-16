package ai.foresignal.source.adapters.goes;

import ai.foresignal.raster.RasterPaths;
import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.source.spi.CurrentRecords;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers one {@code glm-local-{station}} source per name in {@code foresignal.glm-local.stations}
 * (environment {@code FORESIGNAL_GLM_LOCAL_STATIONS}, a comma list, empty by default) (prompt 107). The
 * station names should match the {@code satdump-{station}} names so the local station's
 * {@code ground-station} state can gate health.
 */
@Component
public class GlmLocalFamily implements SourceAdapterFamily {

    private static final Logger log = LoggerFactory.getLogger(GlmLocalFamily.class);

    /** The property naming the local GRB stations. */
    public static final String STATIONS_PROPERTY = "foresignal.glm-local.stations";

    /** A station name: kebab-case. */
    static final Pattern STATION = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    private final List<SourceAdapter> adapters;

    /** Creates the family. */
    @org.springframework.beans.factory.annotation.Autowired
    public GlmLocalFamily(@Value("${" + STATIONS_PROPERTY + ":}") String stations, NetcdfDecoder decoder,
            RasterPaths paths, ObjectProvider<CurrentRecords> records) {
        List<SourceAdapter> built = new ArrayList<>();
        CurrentRecords current = records == null ? null : records.getIfAvailable();
        for (String station : names(stations)) {
            built.add(new GlmLocalAdapter(station, decoder, paths, current));
        }
        this.adapters = List.copyOf(built);
        if (!adapters.isEmpty()) {
            log.info("Local GLM stations configured: {}", names(stations));
        }
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }

    /** The station names of a comma list. */
    static Set<String> names(String stations) {
        Set<String> out = new LinkedHashSet<>();
        if (stations == null) {
            return out;
        }
        for (String part : stations.split(",")) {
            String name = part.trim();
            if (name.isEmpty()) {
                continue;
            }
            if (!STATION.matcher(name).matches() || name.length() > 64 - GlmLocalAdapter.PREFIX.length()) {
                throw new IllegalStateException("Foresignal cannot start: local GLM station name '" + name + "' in "
                        + STATIONS_PROPERTY + " must be kebab-case and at most "
                        + (64 - GlmLocalAdapter.PREFIX.length()) + " characters.");
            }
            out.add(name);
        }
        return out;
    }
}
