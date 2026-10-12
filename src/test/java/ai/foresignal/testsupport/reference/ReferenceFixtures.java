package ai.foresignal.testsupport.reference;

import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.reference.ReferenceDataLoader;
import ai.foresignal.reference.ReferenceManifest;
import ai.foresignal.reference.ReferenceManifest.DatasetSpec;
import ai.foresignal.reference.ReferenceProperties;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Loads the small reference fixtures of {@code src/test/resources/reference/fixtures} through the
 * production {@link ReferenceDataLoader} (prompt 040): the files are copied to a temporary offline
 * directory under their manifest names, their SHA-256 becomes the pin, and the loader stages and swaps
 * them exactly as in production. The fixtures are synthetic squares (see the fixture README):
 *
 * <ul>
 *   <li>{@link #US}: {@code [-110,25,-90,40]}, {@link #MX}: {@code [-110,14,-90,24.9]};
 *   <li>{@link #TX} {@code US-TX}: {@code [-106,26,-94,36]}; Travis County {@link #TRAVIS} {@code 48453}:
 *       {@code [-98.2,30.0,-97.4,30.6]};
 *   <li>the Gulf of Mexico: {@code [-90,20,-80,30]};
 *   <li>cities Austin (30.26715, -97.74306), Houston and Monterrey.
 * </ul>
 */
public final class ReferenceFixtures {

    /** The fixture United States. */
    public static final String US = "US";

    /** The fixture Mexico. */
    public static final String MX = "MX";

    /** Texas. */
    public static final String TX = "US-TX";

    /** Travis County. */
    public static final String TRAVIS = "48453";

    /** The fixture sea area. */
    public static final String GULF = "Gulf of Mexico";

    /** A point in Austin, inside Travis County. */
    public static final double[] AUSTIN = {-97.74, 30.27};

    /** A point about 12 km north-north-east of Austin. */
    public static final double[] NNE_OF_AUSTIN = {-97.70, 30.37};

    /** A point in the fixture Mexico. */
    public static final double[] MEXICO = {-100.3, 20.0};

    /** A point in the Gulf, about 3 km east of the fixture United States. */
    public static final double[] COAST = {-89.97, 28.0};

    /** A point in the open Gulf. */
    public static final double[] SEA = {-85.0, 25.0};

    /** Every fixture dataset, in load order. */
    public static final List<String> ALL = List.of("ne-admin0", "ne-admin1", "ne-admin2-us", "ne-marine",
            "geonames-admin1", "geonames-countryinfo", "geonames-cities15000");

    private ReferenceFixtures() {}

    /** Loads every fixture dataset; the state of each afterwards. */
    public static Map<String, String> loadAll(ApplicationContext context) {
        return load(context, ALL);
    }

    /** Loads the named fixture datasets; the state of each afterwards. */
    public static Map<String, String> load(ApplicationContext context, List<String> names) {
        Path dir = offlineDir();
        ReferenceDataLoader loader = loader(context, dir);
        return loader.loadNow(specs(context.getBean(ReferenceManifest.class), dir, names));
    }

    /** A loader over the context's beans that reads files from {@code offlineDir}; the context itself publishes its events (it is not a bean). */
    public static ReferenceDataLoader loader(ApplicationContext context, Path offlineDir) {
        ReferenceProperties d = ReferenceProperties.defaults();
        ReferenceProperties properties = new ReferenceProperties(false, List.of(), offlineDir, null, Map.of(),
                d.nearMinPopulation(), d.coastalKm(), d.retagKm(), d.trackRetagKm(), d.cacheSize(),
                d.requestsPerMinute(), d.requestsPerDay(), d.autoLoadDelay());
        return new ReferenceDataLoader(context.getBean(ReferenceManifest.class), properties,
                context.getBean(ForesignalProperties.class), context.getBean(UpstreamClient.class),
                context.getBean(JdbcTemplate.class), context.getBean(PlatformTransactionManager.class),
                context, context.getBean(Clock.class));
    }

    /** The manifest entries of {@code names} pinned to the fixture files in {@code dir}. */
    public static List<DatasetSpec> specs(ReferenceManifest manifest, Path dir, List<String> names) {
        List<DatasetSpec> out = new ArrayList<>();
        for (String name : names) {
            DatasetSpec spec = manifest.find(name).orElseThrow();
            out.add(spec.with(spec.url(), ReferenceDataLoader.sha256(dir.resolve(spec.fileName()))));
        }
        return out;
    }

    /** A fresh temporary directory holding every fixture under its manifest file name. */
    public static Path offlineDir() {
        try {
            Path dir = Files.createTempDirectory("foresignal-reference-fixtures");
            for (String file : List.of("ne_10m_admin_0_countries.geojson", "ne_10m_admin_1_states_provinces.geojson",
                    "ne_10m_admin_2_counties.geojson", "ne_10m_geography_marine_polys.geojson",
                    "admin1CodesASCII.txt", "countryInfo.txt")) {
                Files.write(dir.resolve(file), bytes(file));
            }
            try (OutputStream out = Files.newOutputStream(dir.resolve("cities15000.zip"));
                    ZipOutputStream zip = new ZipOutputStream(out)) {
                zip.putNextEntry(new ZipEntry("cities15000.txt"));
                zip.write(bytes("cities15000.txt"));
                zip.closeEntry();
            }
            return dir;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The bytes of fixture {@code file}. */
    public static byte[] bytes(String file) {
        try (InputStream in = ReferenceFixtures.class.getResourceAsStream("/reference/fixtures/" + file)) {
            if (in == null) {
                throw new IllegalStateException("missing fixture " + file);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
