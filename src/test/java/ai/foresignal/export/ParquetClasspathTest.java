package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Keeps the Hadoop server stack off the runtime classpath (prompt 111): GeoParquet is written with
 * parquet-java's Hadoop-free path, so no {@code org.apache.hadoop.hdfs}, {@code yarn} or
 * {@code mapreduce} class (and no Hadoop jar at all) ships, while the codec the writer uses
 * (zstd-jni) does.
 *
 * <p>The runtime classpath comes from the system property the Gradle test task sets for
 * {@code LicenseAllowlistTest}; test-only jars are not part of it.
 */
class ParquetClasspathTest {

    private static List<File> runtimeJars() {
        String classpath = System.getProperty("foresignal.test.runtime-classpath");
        assertThat(classpath).as("the Gradle test task passes the runtime classpath").isNotBlank();
        List<File> jars = new ArrayList<>();
        for (String entry : classpath.split(File.pathSeparator)) {
            if (entry.endsWith(".jar")) {
                jars.add(new File(entry));
            }
        }
        return jars;
    }

    @Test
    @DisplayName("no HDFS, YARN or MapReduce class is on the runtime classpath")
    void noHadoopServerClasses() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (File jar : runtimeJars()) {
            if (!jar.isFile()) {
                continue;
            }
            try (JarFile file = new JarFile(jar)) {
                Enumeration<JarEntry> entries = file.entries();
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (name.startsWith("org/apache/hadoop/hdfs/") || name.startsWith("org/apache/hadoop/yarn/")
                            || name.startsWith("org/apache/hadoop/mapreduce/")
                            || name.startsWith("org/apache/hadoop/mapred/")) {
                        offenders.add(jar.getName() + "!" + name);
                        break;
                    }
                }
            }
        }
        assertThat(offenders).isEmpty();
    }

    @Test
    @DisplayName("no Hadoop, Jetty or Jersey jar ships; parquet and the zstd-jni codec do")
    void jarNames() {
        List<String> names = runtimeJars().stream().map(File::getName).toList();
        assertThat(names).noneMatch(n -> n.startsWith("hadoop-"));
        assertThat(names).noneMatch(n -> n.startsWith("jetty-") && n.contains("hadoop"));
        assertThat(names).noneMatch(n -> n.startsWith("jersey-"));
        assertThat(names).anyMatch(n -> n.startsWith("parquet-hadoop-"));
        assertThat(names).anyMatch(n -> n.startsWith("parquet-column-"));
        assertThat(names).anyMatch(n -> n.startsWith("zstd-jni-"));
    }
}
