package ai.foresignal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Invariant PRC-2 ({@code docs/Invariants.md}): no GPL or AGPL dependency ships in the application.
 *
 * <p>The scan reads the application's <em>runtime</em> classpath, which {@code build.gradle.kts}
 * passes in as the system property {@value #CLASSPATH_PROPERTY}; test-only libraries such as
 * WireMock and Testcontainers are not part of what ships. For each jar it collects the license names
 * declared in the jar's embedded Maven {@code pom.xml} and in its OSGi {@code Bundle-License} header.
 *
 * <p>A jar is rejected when it declares a GPL-family license (GPL or AGPL) and offers no way out of
 * it: no non-copyleft alternative under a dual license (including an SPDX {@code OR} expression) and
 * no Classpath Exception. LGPL is not
 * rejected, because PRC-2 and {@code docs/Conventions.md} name GPL and AGPL. A jar that declares no
 * license at all cannot be judged by this scan; the explicit deny list catches the GPL libraries that
 * {@code docs/Conventions.md} names (Smile, Weka, JSAT, jdistlib) whatever their metadata says.
 *
 * <p>Copied code is outside this test's reach: the reuse policy in {@code THIRD_PARTY_NOTICES.md}
 * governs it.
 */
class LicenseAllowlistTest {

    /** The system property carrying the runtime classpath; set by the Gradle test task. */
    static final String CLASSPATH_PROPERTY = "foresignal.test.runtime-classpath";

    /** Artifact name prefixes of GPL libraries named in {@code docs/Conventions.md}. */
    private static final List<String> DENIED_ARTIFACT_PREFIXES =
            List.of("smile-", "weka", "jsat", "jdistlib");

    private static final Pattern GPL_FAMILY =
            Pattern.compile("\\bA?GPL|GENERAL PUBLIC LICEN[CS]E", Pattern.CASE_INSENSITIVE);
    private static final Pattern WEAK_COPYLEFT =
            Pattern.compile("\\bLGPL|LESSER GENERAL|LIBRARY GENERAL", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLASSPATH_EXCEPTION =
            Pattern.compile("CLASSPATH[ -]?EXCEPTION|\\bCPE\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPDX_OR = Pattern.compile("\\s+OR\\s+");
    private static final Pattern LICENSES_BLOCK =
            Pattern.compile("<licenses>(.*?)</licenses>", Pattern.DOTALL);
    private static final Pattern NAME =
            Pattern.compile("<name>\\s*(.*?)\\s*</name>", Pattern.DOTALL);

    @Test
    @DisplayName("no runtime dependency is licensed only under the GPL or AGPL")
    void noGplDependency() throws IOException {
        Map<String, List<String>> offenders = new LinkedHashMap<>();
        for (File jar : runtimeJars()) {
            List<String> licenses = declaredLicenses(jar);
            if (isDenied(jar.getName()) || isGplOnly(licenses)) {
                offenders.put(jar.getName(), licenses);
            }
        }

        assertThat(offenders)
                .as("These runtime dependencies are GPL or AGPL licensed, which PRC-2 forbids."
                        + " Replace them with an Apache-2.0, MIT, BSD or EPL alternative.")
                .isEmpty();
    }

    @Test
    @DisplayName("the scan sees the real runtime classpath and real license metadata, so it cannot pass vacuously")
    void scanIsNotVacuous() throws IOException {
        List<File> jars = runtimeJars();
        assertThat(jars).as("runtime jars").extracting(File::getName)
                .anySatisfy(name -> assertThat(name).startsWith("spring-core-"))
                .anySatisfy(name -> assertThat(name).startsWith("postgresql-"))
                .noneSatisfy(name -> assertThat(name).startsWith("wiremock"));

        long withMetadata = 0;
        for (File jar : jars) {
            if (!declaredLicenses(jar).isEmpty()) {
                withMetadata++;
            }
        }
        assertThat(withMetadata).as("jars declaring a license").isGreaterThanOrEqualTo(10);
    }

    @Test
    @DisplayName("the classifier rejects GPL and AGPL, and accepts LGPL, Classpath Exception and dual licenses")
    void classifier() {
        assertThat(isGplOnly(List.of("GNU General Public License, version 2"))).isTrue();
        assertThat(isGplOnly(List.of("GNU Affero General Public License v3.0"))).isTrue();
        assertThat(isGplOnly(List.of("AGPL-3.0-only"))).isTrue();
        assertThat(isGplOnly(List.of("GPL-3.0-or-later"))).isTrue();

        assertThat(isGplOnly(List.of("GNU Lesser General Public License"))).isFalse();
        assertThat(isGplOnly(List.of("LGPL-2.1-or-later"))).isFalse();
        assertThat(isGplOnly(List.of("GPL2 w/ CPE"))).isFalse();
        assertThat(isGplOnly(List.of("GNU General Public License, version 2 with the GNU Classpath Exception")))
                .isFalse();
        assertThat(isGplOnly(List.of("EPL 2.0", "GPL2 w/ CPE"))).isFalse();
        assertThat(isGplOnly(List.of("Eclipse Public License 2.0", "GNU General Public License v2"))).isFalse();
        assertThat(isGplOnly(List.of("EPL-2.0 OR GPL-2.0"))).isFalse();
        assertThat(isGplOnly(List.of("EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0"))).isFalse();
        assertThat(isGplOnly(List.of("Apache License, Version 2.0"))).isFalse();
        assertThat(isGplOnly(List.of())).isFalse();

        assertThat(isDenied("smile-core-3.1.1.jar")).isTrue();
        assertThat(isDenied("weka-stable-3.8.6.jar")).isTrue();
        assertThat(isDenied("spring-core-7.0.9.jar")).isFalse();
    }

    // ---------- Classification ----------

    /**
     * True when every declared license is GPL-family without an exception, i.e. there is no
     * non-copyleft license the application could take the jar under.
     */
    static boolean isGplOnly(List<String> licenses) {
        boolean anyGpl = false;
        for (String declared : licenses) {
            // An SPDX expression such as "EPL-2.0 OR GPL-2.0" offers each side as an alternative.
            for (String license : SPDX_OR.split(declared)) {
                if (isStrongCopyleft(license)) {
                    anyGpl = true;
                } else {
                    return false;
                }
            }
        }
        return anyGpl;
    }

    private static boolean isStrongCopyleft(String license) {
        return GPL_FAMILY.matcher(license).find()
                && !WEAK_COPYLEFT.matcher(license).find()
                && !CLASSPATH_EXCEPTION.matcher(license).find();
    }

    private static boolean isDenied(String jarName) {
        String name = jarName.toLowerCase(Locale.ROOT);
        return DENIED_ARTIFACT_PREFIXES.stream().anyMatch(name::startsWith);
    }

    // ---------- Reading the classpath ----------

    private static List<File> runtimeJars() {
        String classpath = System.getProperty(CLASSPATH_PROPERTY);
        assertThat(classpath)
                .as("the Gradle test task must pass the runtime classpath as -D" + CLASSPATH_PROPERTY)
                .isNotBlank();
        return Arrays.stream(classpath.split(Pattern.quote(File.pathSeparator)))
                .map(File::new)
                .filter(file -> file.isFile() && file.getName().endsWith(".jar"))
                .toList();
    }

    private static List<String> declaredLicenses(File jar) throws IOException {
        List<String> licenses = new ArrayList<>();
        try (JarFile file = new JarFile(jar)) {
            Manifest manifest = file.getManifest();
            if (manifest != null) {
                String bundleLicense = manifest.getMainAttributes().getValue("Bundle-License");
                if (bundleLicense != null && !bundleLicense.isBlank()) {
                    licenses.add(bundleLicense.trim());
                }
            }
            for (JarEntry entry : file.stream().toList()) {
                String name = entry.getName();
                if (name.startsWith("META-INF/maven/") && name.endsWith("/pom.xml")) {
                    try (InputStream in = file.getInputStream(entry)) {
                        licenses.addAll(pomLicenses(new String(in.readAllBytes(), StandardCharsets.UTF_8)));
                    }
                }
            }
        }
        return licenses;
    }

    private static List<String> pomLicenses(String pom) {
        List<String> names = new ArrayList<>();
        Matcher block = LICENSES_BLOCK.matcher(pom);
        while (block.find()) {
            Matcher name = NAME.matcher(block.group(1));
            while (name.find()) {
                names.add(name.group(1).replaceAll("\\s+", " "));
            }
        }
        return names;
    }
}
