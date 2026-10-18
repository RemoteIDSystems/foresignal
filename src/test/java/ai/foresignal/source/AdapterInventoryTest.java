package ai.foresignal.source;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.IntegrationTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Focus area 1 of checkpoint 097: the adapter inventory.
 *
 * <p>Walks {@link SourceRegistry#all()} (every built-in and family member) and asserts, for every
 * non-DERIVED and non-PUSH source:
 *
 * <ul>
 *   <li>its descriptor is well formed (an id, at least one kind, a licence);
 *   <li>a capture fixture directory exists under {@code src/test/resources/fixtures/<source-id or
 *       family-id>/} with a {@code README.md} — or the source is a family member whose bundle of packs
 *       shares one captured representative (or is captured inline in its WireMock test), listed in
 *       {@link #SHARED_CAPTURE};
 *   <li>the adapter package has a parser/contract test by the naming convention
 *       ({@code *ParserTest}, {@code *CodecTest}, {@code *MapperTest}, {@code *Adapter(s)Test},
 *       {@code *FamilyTest}), or is listed in {@link #NO_NAMED_CONTRACT}.
 * </ul>
 *
 * <p>The 008 {@code fieldSpecs} contract and the {@code SnapshotContract} static-hash check are per
 * adapter and live in each package's {@code *ParserTest} (they call {@code FieldSpecContract} and
 * {@code SnapshotContract}); the last two assertions confirm those helpers are exercised widely rather
 * than duplicated here, where no per-adapter parser entry point is known.
 */
@IntegrationTest
class AdapterInventoryTest {

    private static final Path FIXTURES = Path.of("src", "test", "resources", "fixtures");
    private static final Path TESTS = Path.of("src", "test", "java");
    private static final String CONTRACT = ".*(Parser|Parsers|Codec|Mapper|Adapter|Adapters|Family|Event|Detector"
            + "|Generator|WireMock|Item|Json|Spec).*Test\\.java";

    /**
     * Family members whose capture is the family's representative fixture ({@code meteoalarm-greece},
     * {@code status-github}, {@code cctv-caltrans-d4}, …) or an inline WireMock body (GBFS, transit,
     * WZDx, the local receivers, the reviewed CAP/RSS feeds), so they have no fixture directory of
     * their own. The corresponding test exercises the same parser.
     */
    static final Set<String> SHARED_CAPTURE = Set.of(
            "cctv-caltrans-d1", "cctv-caltrans-d10", "cctv-caltrans-d11", "cctv-caltrans-d12",
            "cctv-caltrans-d2", "cctv-caltrans-d3", "cctv-caltrans-d5", "cctv-caltrans-d6",
            "cctv-caltrans-d7", "cctv-caltrans-d8", "cctv-caltrans-d9", "cctv-tallinn",
            "cctv-warendorf", "feed-cisa-advisories", "feed-ecdc-threats", "feed-ncsc-uk",
            "feed-nhc-atlantic", "feed-nhc-eastpacific", "feed-who-news", "local-acars-1",
            "local-acars-2", "local-adsb-1", "local-adsb-2", "local-ais-1",
            "local-ais-2", "local-gpsd-1", "local-gpsd-2", "local-remoteid",
            "local-sbs-1", "local-sbs-2", "meteoalarm-andorra", "meteoalarm-austria",
            "meteoalarm-belgium", "meteoalarm-bosnia-herzegovina", "meteoalarm-bulgaria", "meteoalarm-croatia",
            "meteoalarm-cyprus", "meteoalarm-czechia", "meteoalarm-denmark", "meteoalarm-estonia",
            "meteoalarm-finland", "meteoalarm-france", "meteoalarm-germany", "meteoalarm-hungary",
            "meteoalarm-iceland", "meteoalarm-ireland", "meteoalarm-italy", "meteoalarm-latvia",
            "meteoalarm-lithuania", "meteoalarm-luxembourg", "meteoalarm-malta", "meteoalarm-moldova",
            "meteoalarm-montenegro", "meteoalarm-netherlands", "meteoalarm-norway", "meteoalarm-poland",
            "meteoalarm-portugal", "meteoalarm-republic-of-north-macedonia", "meteoalarm-romania", "meteoalarm-serbia",
            "meteoalarm-slovakia", "meteoalarm-slovenia", "meteoalarm-spain", "meteoalarm-sweden",
            "meteoalarm-switzerland", "meteoalarm-ukraine", "meteoalarm-united-kingdom", "ris-live",
            "station-readsb-1", "station-readsb-2", "status-anthropic", "status-atlassian",
            "status-cloudflare", "status-digitalocean", "status-discord", "status-dropbox",
            "status-openai", "status-reddit", "status-twilio", "status-zoom",
            "wxstation-davis-1", "wxstation-davis-2", "wxstation-ecowitt-1", "wxstation-ecowitt-2");

    /**
     * Adapter packages whose contract test lives elsewhere: the local receivers' codecs/mappers are
     * tested in {@code ai.foresignal.source.codec} and {@code ai.foresignal.source.formats} (the shared
     * parser contract test), and the system canary is Foresignal's own health source.
     */
    static final Set<String> NO_NAMED_CONTRACT = Set.of(
            "ai.foresignal.source.adapters.system",
            "ai.foresignal.source.adapters.acars",
            "ai.foresignal.source.adapters.space.localsonde",
            // Social platforms are exercised by social.PlatformAdaptersTest; media-ratings by RatingProvidersTest.
            "ai.foresignal.source.adapters.instagram",
            "ai.foresignal.source.adapters.mastodon",
            "ai.foresignal.source.adapters.reddit",
            "ai.foresignal.source.adapters.threads",
            "ai.foresignal.source.adapters.x",
            "ai.foresignal.source.adapters.youtube",
            "ai.foresignal.source.adapters.media");

    @Autowired private SourceRegistry registry;

    @Test
    @DisplayName("every source has a fixture directory (or a shared capture) and a parser/contract test")
    void inventory() throws IOException {
        List<String> fixtures = new ArrayList<>();
        List<String> contracts = new ArrayList<>();
        List<String> descriptors = new ArrayList<>();
        Set<String> packages = new TreeSet<>();

        for (SourceRegistry.Registration registration : registry.all()) {
            SourceDescriptor descriptor = registration.descriptor();
            // Foresignal's own health sources (system-canary) carry no third-party licence.
            boolean system = descriptor.id() != null && descriptor.id().startsWith("system-");
            if (descriptor.id() == null || descriptor.id().isBlank() || descriptor.kinds().isEmpty()
                    || (!system && descriptor.licence() == null)) {
                descriptors.add(String.valueOf(descriptor.id()));
            }
            // Checkpoint 124: DERIVED generators (099 onward) read Foresignal's own records, not an upstream format,
            // so like the fixture check below the parser/contract check does not apply to them; their behavior is
            // covered by their own tests (for example AisGapCoverageTest, FindingServiceIT, PolServiceIT).
            if (descriptor.mode() != Mode.DERIVED) {
                registration.adapterIfPresent().map(SourceAdapter::getClass).map(Class::getPackageName)
                        .ifPresent(packages::add);
            }
            if (descriptor.mode() == Mode.DERIVED || descriptor.mode() == Mode.PUSH) {
                continue;
            }
            if (!SHARED_CAPTURE.contains(descriptor.id())) {
                Path directory = fixtureDirectory(descriptor.id());
                if (directory == null) {
                    fixtures.add(descriptor.id() + " (no fixture directory)");
                } else if (!Files.isRegularFile(directory.resolve("README.md"))) {
                    fixtures.add(descriptor.id() + " (" + directory.getFileName() + " has no README.md)");
                }
            }
        }

        for (String pkg : packages) {
            if (NO_NAMED_CONTRACT.contains(pkg)) {
                continue;
            }
            Path tests = TESTS.resolve(pkg.replace('.', '/'));
            if (!Files.isDirectory(tests) || !hasContractTest(tests)) {
                contracts.add(pkg);
            }
        }

        assertThat(descriptors).as("well-formed descriptors").isEmpty();
        assertThat(fixtures).as("fixture directories and READMEs").isEmpty();
        assertThat(contracts).as("parser/contract tests per adapter package").isEmpty();
    }

    @Test
    @DisplayName("the fieldSpecs and SnapshotContract checks are exercised across the adapters")
    void contractHelpersAreUsed() throws IOException {
        try (Stream<Path> files = Files.walk(TESTS)) {
            List<Path> java = files.filter(p -> p.toString().endsWith(".java")).toList();
            assertThat(java.stream().filter(AdapterInventoryTest::containsFieldSpecContract).count())
                    .as("parser tests using FieldSpecContract").isGreaterThanOrEqualTo(20);
            assertThat(java.stream().filter(AdapterInventoryTest::containsSnapshotContract).count())
                    .as("parser tests using SnapshotContract").isGreaterThanOrEqualTo(10);
        }
    }

    /** The fixture directory for {@code id}: the id itself, or the longest id prefix that is a directory. */
    private static Path fixtureDirectory(String id) {
        Path exact = FIXTURES.resolve(id);
        if (Files.isDirectory(exact)) {
            return exact;
        }
        String[] parts = id.split("-");
        for (int i = parts.length - 1; i > 0; i--) {
            String prefix = String.join("-", java.util.Arrays.copyOfRange(parts, 0, i));
            Path candidate = FIXTURES.resolve(prefix);
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean hasContractTest(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.map(p -> p.getFileName().toString()).anyMatch(name -> name.matches(CONTRACT));
        }
    }

    private static boolean containsFieldSpecContract(Path file) {
        return contains(file, "FieldSpecContract.");
    }

    private static boolean containsSnapshotContract(Path file) {
        return contains(file, "SnapshotContract.");
    }

    private static boolean contains(Path file, String needle) {
        try {
            return Files.readString(file).contains(needle);
        } catch (IOException e) {
            return false;
        }
    }
}
