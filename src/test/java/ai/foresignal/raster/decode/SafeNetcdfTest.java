package ai.foresignal.raster.decode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.raster.RasterFormatException;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * {@link SafeNetcdf} is the only way into NetCDF-Java, and it opens only regular files under its roots
 * (prompt 046; NetCDF-Java accepts URLs, so anything else would be server-side request forgery).
 */
class SafeNetcdfTest {

    private static final Path MAIN = Path.of("src", "main", "java");
    private static final Path ENTRY = MAIN.resolve(Path.of("ai", "foresignal", "raster", "decode", "SafeNetcdf.java"));

    /** The NetCDF-Java entry points that take a location string (and so a URL). */
    private static final Pattern OPENERS = Pattern.compile(
            "NetcdfFiles\\s*\\.\\s*(open|openInMemory|acquireFile)|NetcdfDatasets\\s*\\.\\s*(openDataset|openFile|acquireDataset|acquireFile)"
                    + "|NetcdfFile\\s*\\.\\s*open|NetcdfDataset\\s*\\.\\s*openDataset|new\\s+(ucar\\.unidata\\.io\\.)?RandomAccessFile\\s*\\(");

    @TempDir Path root;

    @TempDir Path outside;

    @Test
    @DisplayName("opens a regular file under a root, by real path")
    void opensUnderRoot() throws IOException {
        SafeNetcdf safe = DecoderFixtures.safe(root);
        Path file = DecoderFixtures.selfTest("goes19-abi-dmwvc-c08-20261012-0001.nc", root);
        assertThat(safe.requireAllowed(file)).isEqualTo(file.toRealPath());
        try (NetcdfFile nc = safe.open(file)) {
            assertThat((Object) nc.findVariable("wind_speed")).isNotNull();
        }
        try (NetcdfDataset ds = safe.openDataset(file)) {
            assertThat((Object) ds.findVariable("wind_speed")).isNotNull();
        }
        Path grib = DecoderFixtures.selfTest("gfs-1p00-tmp2m-f000.grib2", root);
        try (var raf = safe.openRandomAccess(grib)) {
            assertThat(raf.length()).isEqualTo(Files.size(grib));
        }
        assertThat(safe.roots()).containsExactly(root.toRealPath());
    }

    @Test
    @DisplayName("refuses files outside the roots, symbolic links, directories, missing files and escapes")
    void refuses() throws IOException {
        SafeNetcdf safe = DecoderFixtures.safe(root);
        Path elsewhere = DecoderFixtures.selfTest("goes19-abi-dmwvc-c08-20261012-0001.nc", outside);
        assertThatThrownBy(() -> safe.open(elsewhere)).isInstanceOf(RasterFormatException.class)
                .hasMessageContaining("outside");
        assertThatThrownBy(() -> safe.openDataset(elsewhere)).isInstanceOf(RasterFormatException.class);
        assertThatThrownBy(() -> safe.openRandomAccess(elsewhere)).isInstanceOf(RasterFormatException.class);

        Path inside = DecoderFixtures.selfTest("goes19-abi-dmwvc-c08-20261012-0001.nc", root);
        Path linkToInside = Files.createSymbolicLink(root.resolve("link.nc"), inside);
        assertThatThrownBy(() -> safe.open(linkToInside)).isInstanceOf(RasterFormatException.class)
                .hasMessageContaining("symbolic link");
        Path linkToOutside = Files.createSymbolicLink(root.resolve("escape.nc"), elsewhere);
        assertThatThrownBy(() -> safe.open(linkToOutside)).isInstanceOf(RasterFormatException.class);

        Path subdir = Files.createDirectories(root.resolve("sub"));
        assertThatThrownBy(() -> safe.requireAllowed(subdir)).hasMessageContaining("Not a regular file");
        assertThatThrownBy(() -> safe.requireAllowed(root.resolve("missing.nc"))).isInstanceOf(RasterFormatException.class);
        Path dotDot = root.resolve("..").resolve(outside.getFileName()).resolve(elsewhere.getFileName());
        assertThatThrownBy(() -> safe.requireAllowed(dotDot)).isInstanceOf(RasterFormatException.class);
        assertThatThrownBy(() -> safe.requireAllowed(null)).isInstanceOf(RasterFormatException.class);
    }

    @Test
    @DisplayName("no public method or constructor takes a String (NetCDF-Java would read it as a URL)")
    void noStringOverload() {
        List<String> offenders = new ArrayList<>();
        for (Method m : SafeNetcdf.class.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && Arrays.asList(m.getParameterTypes()).contains(String.class)) {
                offenders.add(m.toString());
            }
        }
        for (Constructor<?> c : SafeNetcdf.class.getConstructors()) {
            if (Arrays.asList(c.getParameterTypes()).contains(String.class)) {
                offenders.add(c.toString());
            }
        }
        assertThat(offenders).isEmpty();
    }

    @Test
    @DisplayName("no other file in src/main/java opens anything with NetCDF-Java")
    void onlyCallSite() throws IOException {
        assertThat(Files.readString(ENTRY)).as("the scan must find the sanctioned call sites").containsPattern(OPENERS);
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(MAIN)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).sorted().toList()) {
                if (file.equals(ENTRY)) {
                    continue;
                }
                if (OPENERS.matcher(Files.readString(file)).find()) {
                    offenders.add(file.toString());
                }
            }
        }
        assertThat(offenders).as("only SafeNetcdf may call NetcdfFiles.open / NetcdfDatasets.openDataset").isEmpty();
    }

    @Test
    @DisplayName("constructing the guard pins NetCDF-Java's disk caches under the given root")
    void pinsCaches() {
        Path cache = root.resolve("cache-netcdf");
        new SafeNetcdf(List.of(root), cache);
        assertThat(NetcdfRuntime.configuredRoot()).isEqualTo(cache.toAbsolutePath().normalize());
        assertThat(System.getProperty(NetcdfRuntime.DEFAULT_CACHE_PROPERTY)).startsWith(cache.toAbsolutePath().toString());
        assertThat(ucar.nc2.util.DiskCache.getRootDirectory()).startsWith(cache.toAbsolutePath().toString());
        assertThat(ucar.nc2.grib.GribIndexCache.getDiskCache2().getRootDirectory())
                .startsWith(cache.resolve("grib").toAbsolutePath().toString());
        assertThat(Files.isDirectory(cache.resolve("grib"))).isTrue();
    }
}
