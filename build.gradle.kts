plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    jacoco
}

group = "ai.foresignal"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.hibernate.spatial)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.jackson.dataformat.csv)
    implementation(libs.bucket4j.core)
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.micrometer.tracing.opentelemetry)
    implementation(libs.micrometer.tracing.bridge.otel)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    runtimeOnly(libs.postgresql)

    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.spring.boot.webmvc.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.wiremock.standalone)
    testImplementation(libs.awaitility)
    testImplementation(libs.greenmail)
    testImplementation(libs.json.schema.validator)
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
}

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

// Optional SDK smoke test (not wired to anything): generates typescript-fetch
// and dart-dio clients from the authored spec into build/sdk-check and fails on
// generator errors. The npm wrapper and the generator jar are both pinned; the
// jar version is written to build/sdk-check/openapitools.json, which the wrapper
// reads from its working directory. Needs npx and a Java runtime.
val openApiGeneratorCli = "@openapitools/openapi-generator-cli@2.41.0"
val openApiGeneratorVersion = "7.26.0"
val sdkCheckDir = layout.buildDirectory.dir("sdk-check")
val sdkCheckTasks = listOf("typescript-fetch", "dart-dio").map { generator ->
    tasks.register<Exec>("checkSdkGeneration-$generator") {
        group = "api"
        description = "Generates a $generator client from docs/OpenAPI.json into build/sdk-check/$generator."
        val specFile = layout.projectDirectory.file("docs/OpenAPI.json").asFile
        workingDir = sdkCheckDir.get().asFile
        commandLine(
            "npx", "--yes", openApiGeneratorCli, "generate",
            "-i", specFile.absolutePath,
            "-g", generator,
            "-o", sdkCheckDir.get().dir(generator).asFile.absolutePath,
        )
        doFirst {
            if (!onPath("npx")) {
                throw GradleException("checkSdkGeneration needs npx (Node.js >= 20.19) and a Java runtime.")
            }
            val dir = sdkCheckDir.get().asFile
            dir.mkdirs()
            File(dir, "openapitools.json").writeText(
                """{"spaces": 2, "generator-cli": {"version": "$openApiGeneratorVersion"}}""" + "\n"
            )
        }
    }
}
tasks.register("checkSdkGeneration") {
    group = "api"
    description = "Optional: generates typescript-fetch and dart-dio clients from docs/OpenAPI.json (prompt 141 owns the real SDKs)."
    dependsOn(sdkCheckTasks)
}
