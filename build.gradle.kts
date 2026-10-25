import java.net.HttpURLConnection
import java.net.URI
import java.security.MessageDigest

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    jacoco
}

group = "ai.foresignal"
version = "1.5.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    // The one explicit exception to the Maven-Central rule (Architecture §2 and §26, prompt 046):
    // NetCDF-Java is published only to Unidata's own repository. exclusiveContent makes the edu.ucar
    // groups resolve from there and nowhere else, and nothing else resolve from there;
    // checkUnidataScope below fails the build if that ever stops being true. The old
    // /repository/unidata-all/ path 301-redirects; do not use it. If the Unidata host is down, the
    // Operator Guide describes a vendored file repository under gradle/vendor-repo/.
    exclusiveContent {
        forRepository {
            maven("https://artifacts.unidata.ucar.edu/all/") { name = "unidata" }
        }
        filter {
            includeGroup("edu.ucar")
            includeGroup("edu.ucar.unidata")
        }
    }
}

// RecordMapping (059) needs Jayway JsonPath 3.0.0 (Jackson 3 providers); the Spring Boot BOM pins 2.10.0
// (its json-path.version property), which would otherwise win for the test classpath's copy.
extra["json-path.version"] = libs.versions.jsonPath.get()
// protobuf-java (070): the Spring Boot BOM manages 4.35.1, below NetCDF-Java cdm-core's 4.36.1; the catalog pins
// 4.36.1, which satisfies both cdm-core and gtfs-realtime-bindings (generated with 4.34.0; protobuf allows a newer
// runtime of the same major).
extra["protobuf-java.version"] = libs.versions.protobufJava.get()

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    // Schema ownership (144): Flyway applies src/main/resources/db/migration before Hibernate validates.
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.hibernate.spatial)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.jackson.dataformat.csv)
    implementation(libs.jackson.dataformat.cbor)
    implementation(libs.bucket4j.core)
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.micrometer.tracing.opentelemetry)
    implementation(libs.micrometer.tracing.bridge.otel)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.zxing.core)
    implementation(libs.json.schema.validator)
    implementation(libs.re2j)
    implementation(libs.mcp.core)
    implementation(libs.mcp.json.jackson3)
    // MCP remote authorization (141): IdP access tokens on /mcp; Tink for Nimbus's Ed25519Verifier.
    implementation(libs.nimbus.jose.jwt)
    implementation(libs.tink) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    implementation(libs.anthropic.java)
    implementation(libs.commonmark)
    implementation(libs.commonmark.ext.gfm.tables)
    implementation(libs.owasp.java.html.sanitizer)
    implementation(libs.h3)
    implementation(libs.commons.compress)
    // Discovery statistics (119): Apache Commons Statistics, Numbers and RNG (Apache-2.0); no GPL statistics library.
    implementation(libs.commons.statistics.distribution)
    implementation(libs.commons.statistics.inference)
    implementation(libs.commons.numbers.gamma)
    implementation(libs.commons.rng.simple)
    implementation(libs.commons.rng.sampling)
    // Series forecasts (126): Hipparchus optimizers (Apache-2.0) for the ETS maximum likelihood.
    implementation(libs.hipparchus.optim)
    implementation(libs.caffeine)
    // GRIB2 and NetCDF4 decoding (046): cdm-core and grib, versions from the NetCDF-Java BOM; cdm-radial for
    // the NIDS (NEXRAD Level III) reader of nexrad-l3 (106).
    implementation(platform(libs.netcdf.java.bom))
    implementation(libs.netcdf.cdm.core)
    implementation(libs.netcdf.grib)
    implementation(libs.netcdf.cdm.radial)
    runtimeOnly(libs.libaec.native)
    // SGP4 approximate swaths of SatDump passes (054), loaded only on that code path.
    implementation(libs.orekit)
    // The mapping code always passes its own Jackson 3 configuration; the json-smart default is never used.
    implementation(libs.json.path) {
        exclude(group = "net.minidev", module = "json-smart")
    }
    // MQTT channel and HA discovery (060): the shaded HiveMQ client carries its own Netty 4.1, because the
    // Spring Boot BOM would move the plain artifact's Netty to 4.2 (see the version catalog).
    implementation(libs.hivemq.mqtt.client.shaded)
    // permessage-deflate WebSockets for streaming sources (063): used only inside ai.foresignal.collect.
    implementation(libs.java.websocket)
    // GTFS-Realtime VehiclePositions and ServiceAlerts of the transit-* sources (070), with protobuf-java pinned.
    implementation(libs.gtfs.realtime.bindings)
    implementation(libs.protobuf.java)
    // Feed-kit item text (073): HTML summaries become plain text with Safelist.none().
    implementation(libs.jsoup)
    // Attachment text extraction (130): PDF text through Apache PDFBox; files are never rendered or executed.
    implementation(libs.pdfbox)
    implementation(libs.webp.imageio)
    implementation(libs.sqlite.jdbc)
    // GeoParquet exports (111): parquet-java, minus the Hadoop server stack (HDFS/YARN/MapReduce/Jetty/Jersey).
    implementation(libs.parquet.column)
    implementation(libs.parquet.hadoop) {
        exclude(group = "org.apache.hadoop", module = "hadoop-hdfs")
        exclude(group = "org.apache.hadoop", module = "hadoop-hdfs-client")
        exclude(group = "org.apache.hadoop", module = "hadoop-yarn-api")
        exclude(group = "org.apache.hadoop", module = "hadoop-yarn-client")
        exclude(group = "org.apache.hadoop", module = "hadoop-yarn-common")
        exclude(group = "org.apache.hadoop", module = "hadoop-mapreduce-client-core")
        exclude(group = "org.apache.hadoop", module = "hadoop-mapreduce-client-common")
        exclude(group = "org.eclipse.jetty")
        exclude(group = "org.eclipse.jetty.websocket")
        exclude(group = "com.sun.jersey")
        exclude(group = "org.glassfish.jersey.core")
    }
    // ZSTD pages for GeoParquet (111) through zstd-jni directly (ParquetCodecs), so no Hadoop codec is needed.
    implementation(libs.zstd.jni)
    // parquet-hadoop's ParquetFileWriter has constructor overloads naming org.apache.hadoop.conf.Configuration, so
    // javac needs the class to resolve the Hadoop-free overload. Compile-only and non-transitive: nothing of Hadoop
    // is on the runtime classpath (ParquetClasspathTest).
    compileOnly(libs.hadoop.common) { isTransitive = false }
    implementation(libs.flatgeobuf)
    // Social post language detection (135): Lingua, restricted to social.languages.
    implementation(libs.lingua)
    // Out-of-tree WASM parser plugins (141): the Chicory interpreter, no WASI.
    implementation(libs.chicory.runtime)
    runtimeOnly(libs.postgresql)

    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.spring.boot.webmvc.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.toxiproxy)
    testImplementation(libs.wiremock.standalone)
    testImplementation(libs.awaitility)
    // Test-only (111): parquet-hadoop's ParquetFileReader.open overloads name org.apache.hadoop.conf.Configuration,
    // so the round-trip test reader needs the class at compile time. Non-transitive; never on the runtime classpath.
    testImplementation(libs.hadoop.common) { isTransitive = false }
    testImplementation(libs.greenmail)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.encoding = "UTF-8"
}

springBoot {
    buildInfo()
}

tasks.test {
    // The performance smoke (prompt 022) runs for about 13 minutes; it has its own task, perfTest.
    useJUnitPlatform {
        excludeTags("perf")
    }
    // LicenseAllowlistTest (invariant PRC-2) inspects what ships: the runtime classpath, not the
    // test classpath, which also carries WireMock and Testcontainers.
    // ForesignalApplication.main sets this before anything can initialize the JDK HTTP client
    // (UpstreamClient.allowHostHeaderForPinnedRequests). In the test JVM a test's own HttpClient can
    // initialize it first, so the property is set at JVM start, as production effectively does.
    systemProperty("jdk.httpclient.allowRestrictedHeaders", "host")
    // Checkpoint 041: the suite caches one Spring context per test configuration (13+ by prompt 040),
    // and since 040 each holds the bundled country polygons (prepared JTS geometries). Gradle's default
    // 512 MB test heap ran out of memory (run 1); the production heap budget (NFR-1) is per process
    // with one context. Checkpoint 097: the adapter block (062-096) grew the cached context count and
    // each context now also holds the full source registry (40+ adapters and their descriptors), so the
    // 2 GB that sufficed at 041 is exceeded; the test JVM gets 4 GB. Production keeps its own budget.
    maxHeapSize = "4g"
    // Checkpoint 124: the intelligence prompts (098-123) brought the suite to Spring's default cache of 32 contexts,
    // and 32 cached contexts exhausted the 4 GB heap (run 2: OutOfMemoryError in a raster test). Capping the cache
    // evicts the least recently used context instead; a context evicted and needed again is rebuilt.
    systemProperty("spring.test.context.cache.maxSize", "12")
    // Run 3 still ran out of memory with the cap: closed contexts stayed reachable through their event-bus delivery
    // threads, fixed in production (@PreDestroy on the buses, CompactOutboxService and EntityLinker).
    val runtimeClasspath = configurations.runtimeClasspath
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf("-Dforesignal.test.runtime-classpath=" + runtimeClasspath.get().asPath)
    })
}

// The performance smoke (prompt 022, a gate): @Tag("perf") tests only, against the Testcontainers
// database. Not wired to check; run it explicitly with ./gradlew perfTest.
tasks.register<Test>("perfTest") {
    group = "verification"
    description = "Runs the @Tag(\"perf\") performance smoke (prompt 022)."
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform {
        includeTags("perf")
    }
    maxHeapSize = "2g"
    testLogging {
        showStandardStreams = true
    }
    outputs.upToDateWhen { false }
}

// The authored OpenAPI document is the contract. It is copied byte for byte onto
// the classpath (outside static/) and served at /v1/openapi.json by
// OpenApiController (getOpenApiDocument, prompt 020); the springdoc-generated
// document at /v1/openapi-generated.json exists only for drift detection.
tasks.processResources {
    from(layout.projectDirectory.file("docs/OpenAPI.json")) {
        into("openapi")
        rename { "openapi.json" }
    }
    // The EDP JSON Schemas exported from the spec (scripts/export-edp-schemas.py), served at
    // /v1/external/protocol/schemas/{name} and used to validate items (prompt 024).
    from(layout.projectDirectory.dir("docs/protocol/edp-1.0")) {
        include("*.schema.json")
        into("edp/schemas")
    }
}

// The adapter doctor's packaged fixtures and adapter sources (prompt 140); see the script.
apply(from = "gradle/doctor-resources.gradle.kts")

tasks.bootJar {
    archiveFileName = "foresignal.jar"
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    // The Spring Boot entry point is excluded: it is a main() method with no
    // behaviour worth covering.
    classDirectories.setFrom(
        files(sourceSets.main.get().output.classesDirs).asFileTree.matching {
            exclude("ai/foresignal/ForesignalApplication*")
        }
    )
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

// Raster fixtures too large to commit (prompt 046): fetched into build/test-fixtures/ with sha256
// pins. Tests that need one skip with a logged reason when it is absent. Not wired to anything; run
// ./gradlew downloadTestFixtures before a test run that should cover them. Anonymous HTTPS against the
// public NOAA bucket, the same object scripts/capture-raster-fixtures.sh captured.
// A fixture with a `range` is one or more GRIB2 messages read from a larger object with a ranged GET
// (prompt 051: HRRR messages planned from the step's .idx), answered 206.
data class PinnedFixture(val path: String, val url: String, val sha256: String, val bytes: Long, val range: String? = null)
val pinnedFixtures = listOf(
    PinnedFixture(
        "goes/OR_ABI-L2-CMIPC-M6C13_G19_s20262851201170_e20262851203554_c20262851204053.nc",
        "https://noaa-goes19.s3.amazonaws.com/ABI-L2-CMIPC/2026/285/12/" +
            "OR_ABI-L2-CMIPC-M6C13_G19_s20262851201170_e20262851203554_c20262851204053.nc",
        "bace11ee913a15467b047a76beb12d241589349431e74424a6fe6d09833cc556",
        3_807_021L,
    ),
    PinnedFixture(
        "hrrr/hrrr-f01-ugrd-vgrd.grib2",
        "https://noaa-hrrr-bdp-pds.s3.amazonaws.com/hrrr.20261012/conus/hrrr.t12z.wrfsfcf01.grib2",
        "ac2ea3d466bf3032f82c5bf62501473159892e25af628802d65c6785f3f7df04",
        4_763_230L,
        "bytes=52027080-56790309",
    ),
    PinnedFixture(
        "hrrr/hrrr-f01-gust.grib2",
        "https://noaa-hrrr-bdp-pds.s3.amazonaws.com/hrrr.20261012/conus/hrrr.t12z.wrfsfcf01.grib2",
        "8e2a8b1e33027f098e51e1687649258f039a4906a822b56d50a4aef04fc603b4",
        1_252_096L,
        "bytes=3314894-4566989",
    ),
    PinnedFixture(
        "hrrr/hrrr-f01-massden.grib2",
        "https://noaa-hrrr-bdp-pds.s3.amazonaws.com/hrrr.20261012/conus/hrrr.t12z.wrfsfcf01.grib2",
        "31d8b170fa096b3b846ff17b4b7676165822b0dff706ea2005e870a9a9659556",
        984_361L,
        "bytes=51042719-52027079",
    ),
    // Prompt 053: MRMS multi-sensor QPE 24 h is 4.7 MB on a wet day, over the 2 MB the prompt allows to commit.
    PinnedFixture(
        "mrms/MRMS_MultiSensor_QPE_24H_Pass2_00.00_20261013-030000.grib2.gz",
        "https://noaa-mrms-pds.s3.amazonaws.com/CONUS/MultiSensor_QPE_24H_Pass2_00.00/20261013/" +
            "MRMS_MultiSensor_QPE_24H_Pass2_00.00_20261013-030000.grib2.gz",
        "349bb80cd2e485ff9a8771f3462987398c8c05e82a149ad0f898cdbe4c5ccebb",
        4_725_486L,
    ),
    // Checkpoint 124: every RTMA-RU message is 5.6-7.5 MB (simple packing), over the commit limit.
    PinnedFixture(
        "rtma-ru/rtma-ru-t1100z-gust.grib2",
        "https://noaa-rtma-pds.s3.amazonaws.com/rtma2p5_ru.20261017/rtma2p5_ru.t1100z.2dvaranl_ndfd.grb2",
        "1906f27d0c8b8a1fe6c948e6336f3f5e996ccd69bedab8d352f6242b54458b54",
        5_617_636L,
        "bytes=57580533-63198168",
    ),
)
tasks.register("downloadTestFixtures") {
    group = "verification"
    description = "Downloads the sha256-pinned raster fixtures too large to commit into build/test-fixtures/."
    val root = layout.buildDirectory.dir("test-fixtures")
    doLast {
        val digest = { file: File ->
            val md = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    md.update(buffer, 0, read)
                }
            }
            md.digest().joinToString("") { "%02x".format(it) }
        }
        for (fixture in pinnedFixtures) {
            val target = root.get().file(fixture.path).asFile
            if (target.isFile && digest(target) == fixture.sha256) {
                logger.lifecycle("downloadTestFixtures: ${fixture.path} present")
                continue
            }
            target.parentFile.mkdirs()
            val part = File(target.parentFile, target.name + ".part")
            val connection = URI.create(fixture.url).toURL().openConnection() as HttpURLConnection
            connection.connectTimeout = 30_000
            connection.readTimeout = 120_000
            connection.instanceFollowRedirects = false
            fixture.range?.let { connection.setRequestProperty("Range", it) }
            val expectedStatus = if (fixture.range == null) 200 else 206
            if (connection.responseCode != expectedStatus) {
                throw GradleException("downloadTestFixtures: ${fixture.url} answered ${connection.responseCode}")
            }
            connection.inputStream.use { input -> part.outputStream().use { input.copyTo(it) } }
            val actual = digest(part)
            if (part.length() != fixture.bytes || actual != fixture.sha256) {
                part.delete()
                throw GradleException("downloadTestFixtures: ${fixture.path} has sha256 $actual (${part.length()} bytes); " +
                    "expected ${fixture.sha256} (${fixture.bytes} bytes)")
            }
            if (!part.renameTo(target)) {
                throw GradleException("downloadTestFixtures: could not move ${part} to ${target}")
            }
            logger.lifecycle("downloadTestFixtures: ${fixture.path} downloaded and verified")
        }
    }
}

// The Unidata repository may serve only the edu.ucar groups (prompt 046); see the script.
apply(from = "gradle/unidata-scope.gradle.kts")
tasks.check {
    dependsOn("checkUnidataScope")
}

// US English is the only spelling (prompt 149); see the script and gradle/us-english/.
apply(from = "gradle/us-english.gradle.kts")
tasks.check {
    dependsOn("checkUsEnglish")
}

// ---------------------------------------------------------------------------
// API tooling (prompt 020). None of these tasks is wired to compileJava or test.
// ---------------------------------------------------------------------------

/** True when `command` resolves on the PATH (for a helpful failure instead of a raw exec error). */
fun onPath(command: String): Boolean =
    System.getenv("PATH").orEmpty().split(File.pathSeparator).any { dir ->
        File(dir, command).canExecute() || File(dir, "$command.cmd").canExecute()
    }

// Regenerates the Postman collection and environment from the authored spec
// (Roadmap §6). Run it after every docs/OpenAPI.json change; the collection is
// never hand-edited. Python 3 standard library only.
tasks.register<Exec>("generatePostman") {
    group = "api"
    description = "Regenerates postman/ from docs/OpenAPI.json with scripts/generate-postman.py."
    workingDir = layout.projectDirectory.asFile
    commandLine("python3", "scripts/generate-postman.py", "docs/OpenAPI.json", "postman")
}

// Lints the authored spec with Redocly CLI (redocly.yaml: recommended plus the
// project rules; .redocly.lint-ignore.yaml lists the deliberate exceptions).
// Required: wired into `check`. Needs Node.js >= 20.19 with npx on the PATH, or
// -PskipOpenApiLint=true to skip it explicitly.
val redoclyCli = "@redocly/cli@2.62.1"
val skipOpenApiLint = providers.gradleProperty("skipOpenApiLint").map { it.toBoolean() }.orElse(false)
val lintOpenApi = tasks.register<Exec>("lintOpenApi") {
    group = "verification"
    description = "Lints docs/OpenAPI.json with $redoclyCli (pass -PskipOpenApiLint=true to skip)."
    workingDir = layout.projectDirectory.asFile
    inputs.files("docs/OpenAPI.json", "redocly.yaml", ".redocly.lint-ignore.yaml")
    onlyIf("-PskipOpenApiLint=true was not passed") { !skipOpenApiLint.get() }
    commandLine("npx", "--yes", redoclyCli, "lint", "docs/OpenAPI.json")
    doFirst {
        if (!onPath("npx")) {
            throw GradleException(
                "lintOpenApi needs npx (Node.js >= 20.19, https://nodejs.org/). " +
                    "Install Node.js, or pass -PskipOpenApiLint=true to skip the OpenAPI lint."
            )
        }
    }
}
tasks.check {
    dependsOn(lintOpenApi)
}

// Client SDKs (prompt 141): generateSdks regenerates sdk/dart/foresignal_api, sdk/typescript/src/generated
// and sdk/python/foresignal_client; checkSdkGeneration preprocesses the spec for Dart and dry-runs dart-dio.
// Neither is wired to build or check. Tool versions: sdk/versions.properties.
apply(from = "gradle/sdk.gradle.kts")

// API compatibility gate (prompt 144, NFR-10): /v1 is additive only, so docs/OpenAPI.json must not break the frozen
// v1.0.0 contract at docs/api/v1.0.0/OpenAPI.json. Runs `oasdiff breaking <frozen> <current> --fail-on ERR --err-ignore
// docs/api/v1.0.0/oasdiff-err-ignore.txt` (the accepted errors, each with its reason) with a
// local `oasdiff` binary, else the pinned Docker image (tufin/oasdiff:v1.33.0, Apache-2.0, pushed 2026-10-01), else
// skips with a warning. Not wired to build or check; the proposed CI api stage (docs/CI-Proposal.md) runs it.
val oasdiffImage = "tufin/oasdiff:v1.33.0"
tasks.register("checkApiCompat") {
    group = "verification"
    description = "Fails on breaking changes between docs/api/v1.0.0/OpenAPI.json and docs/OpenAPI.json (oasdiff)."
    val projectDir = layout.projectDirectory.asFile
    inputs.files("docs/api/v1.0.0/OpenAPI.json", "docs/OpenAPI.json", "docs/api/v1.0.0/oasdiff-err-ignore.txt")
    doLast {
        val frozen = "docs/api/v1.0.0/OpenAPI.json"
        val current = "docs/OpenAPI.json"
        // The accepted errors, each with its reason; growing value lists are x-extensible-enum in the contract.
        val ignore = "docs/api/v1.0.0/oasdiff-err-ignore.txt"
        val command: List<String>? = when {
            onPath("oasdiff") -> listOf("oasdiff", "breaking", frozen, current, "--fail-on", "ERR", "--err-ignore", ignore)
            onPath("docker") -> listOf(
                "docker", "run", "--rm", "-v", "${projectDir.absolutePath}:/specs:ro", "-w", "/specs",
                oasdiffImage, "breaking", frozen, current, "--fail-on", "ERR", "--err-ignore", ignore,
            )
            else -> null
        }
        if (command == null) {
            logger.warn("checkApiCompat skipped: neither oasdiff nor docker is on the PATH (install oasdiff from " +
                "https://github.com/oasdiff/oasdiff or Docker to run the API compatibility gate).")
            return@doLast
        }
        val process = ProcessBuilder(command).directory(projectDir).inheritIO().start()
        val exit = process.waitFor()
        if (exit != 0) {
            throw GradleException("checkApiCompat: oasdiff reported breaking changes against the frozen v1.0.0 " +
                "contract (exit $exit). /v1 is additive only (NFR-10); breaking changes belong in /v2.")
        }
        logger.lifecycle("checkApiCompat: no breaking changes against $frozen")
    }
}
