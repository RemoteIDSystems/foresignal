// The Foresignal CLI (prompt 141): picocli, java.net.http and Jackson 3, with an optional GraalVM native image.
// Every version below was checked on Maven Central or the Gradle plugin portal on 2026-10-18 and is at least two
// weeks old: picocli and picocli-codegen 4.7.7, Jackson 3.2.3, json-schema-validator 3.0.8 (the broker's version),
// slf4j-nop 2.0.20, native-build-tools 1.1.14, JUnit 5.14.4, WireMock 3.13.2 (the broker's version).

plugins {
    java
    application
    id("org.graalvm.buildtools.native") version "1.1.14"
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
    implementation("info.picocli:picocli:4.7.7")
    // Generates META-INF/native-image/picocli-generated/{project}/reflect-config.json (and resource and proxy
    // configuration) for the native image from the command annotations.
    annotationProcessor("info.picocli:picocli-codegen:4.7.7")

    implementation(platform("tools.jackson:jackson-bom:3.2.3"))
    implementation("tools.jackson.core:jackson-databind")
    // Local `edp validate` against the bundled EDP 1.0 JSON Schemas (draft 2020-12).
    implementation("com.networknt:json-schema-validator:3.0.8")
    // json-schema-validator logs through SLF4J; a CLI stays quiet.
    runtimeOnly("org.slf4j:slf4j-nop:2.0.20")

    testImplementation(platform("org.junit:junit-bom:5.14.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.wiremock:wiremock-standalone:3.13.2")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters", "-Aproject=${project.group}/${project.name}"))
}

application {
    mainClass = "ai.foresignal.cli.ForesignalCli"
    applicationName = "foresignal"
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "ai.foresignal.cli.ForesignalCli",
            "Implementation-Title" to "foresignal-cli",
            "Implementation-Version" to project.version,
        )
    }
}

// A single runnable jar: `java -jar cli/build/libs/foresignal-cli-<version>-all.jar`.
tasks.register<Jar>("uberJar") {
    group = "build"
    description = "Assembles a runnable jar with every runtime dependency."
    archiveClassifier = "all"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Main-Class" to "ai.foresignal.cli.ForesignalCli",
            "Implementation-Title" to "foresignal-cli",
            "Implementation-Version" to project.version,
        )
    }
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({ configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) } })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.EC", "module-info.class",
        "META-INF/versions/*/module-info.class")
}

tasks.test {
    useJUnitPlatform()
}

// Optional: `./gradlew -p cli nativeCompile` with a GraalVM JDK 21 builds cli/build/native/nativeCompile/foresignal.
graalvmNative {
    binaries {
        named("main") {
            imageName = "foresignal"
            mainClass = "ai.foresignal.cli.ForesignalCli"
            buildArgs.addAll("--no-fallback", "--enable-url-protocols=http,https")
        }
    }
}
