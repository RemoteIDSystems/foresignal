// The Foresignal command-line client (prompt 141). A standalone build, not part of the broker build:
// build it with the repository's wrapper, `./gradlew -p cli build`.
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "foresignal-cli"
