plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    jacoco
}

group = "ai.elaro"
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
    implementation(libs.jts.io.common)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.bucket4j.core)
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
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.encoding = "UTF-8"
}

springBoot {
    buildInfo()
}

tasks.test {
    useJUnitPlatform()
}

// The authored OpenAPI document is the contract. Serving it from the classpath
// lets /v1/openapi.json return the authored spec (prompt 017) rather than the
// springdoc-generated one, which is used only for drift detection.
tasks.processResources {
    from(layout.projectDirectory.file("docs/OpenAPI.json")) {
        into("static/v1")
        rename { "openapi.json" }
    }
}

tasks.bootJar {
    archiveFileName = "elaro.jar"
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
            exclude("ai/elaro/ElaroApplication*")
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
