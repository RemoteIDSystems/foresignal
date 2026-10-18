// Client SDK generation (prompt 141). Applied by build.gradle.kts; nothing here is wired to build, check or test.
//
// - generateSdks regenerates the three API clients from docs/OpenAPI.json:
//     Dart        sdk/dart/foresignal_api        openapi-generator dart-dio (json_serializable) from the
//                                                OAS 3.0.3 copy written by scripts/sdk/preprocess-dart.py,
//                                                then `dart run build_runner build` for the *.g.dart parts
//     TypeScript  sdk/typescript/src/generated   openapi-typescript (types for openapi-fetch)
//     Python      sdk/python/foresignal_client   openapi-python-client (--meta none)
//   The hand-written layers (sdk/dart/foresignal_sdk, sdk/typescript/src/{realtime,paging,contract},
//   sdk/python/foresignal_sdk) live outside the generated directories and are never touched.
// - checkSdkGeneration runs the preprocess step plus an openapi-generator dart-dio dry run (no files written
//   outside build/), replacing the prompt 020 smoke test.
//
// Tools are resolved at execution time: openapi-generator from a Java runtime (the pinned jar is downloaded
// from Maven Central into build/sdk/tools and checked against its SHA-256) or Docker; openapi-typescript via
// npx; openapi-python-client via pipx or uvx; build_runner via dart. A missing tool fails with install
// instructions. Every version comes from sdk/versions.properties.

import groovy.json.JsonSlurper
import java.security.MessageDigest
import java.util.Properties

val sdkVersionsFile = layout.projectDirectory.file("sdk/versions.properties").asFile
val sdkVersions = Properties().apply { sdkVersionsFile.inputStream().use { load(it) } }

fun sdkVersion(key: String): String =
    sdkVersions.getProperty(key) ?: throw GradleException("sdk/versions.properties has no '$key'")

/** True when `command` resolves on the PATH (named apart from build.gradle.kts's onPath). */
fun sdkOnPath(command: String): Boolean =
    System.getenv("PATH").orEmpty().split(File.pathSeparator).any { dir ->
        File(dir, command).canExecute() || File(dir, "$command.cmd").canExecute() || File(dir, "$command.exe").canExecute()
    }

val sdkRoot = layout.projectDirectory.asFile
val sdkSpec = layout.projectDirectory.file("docs/OpenAPI.json").asFile
val sdkBuildDir = layout.buildDirectory.dir("sdk")
val sdkDartSpec = layout.buildDirectory.file("sdk/openapi-dart.json")
val sdkGeneratorJar = layout.buildDirectory.file("sdk/tools/openapi-generator-cli-${sdkVersion("openapiGenerator.version")}.jar")
val sdkDartPackage = layout.projectDirectory.dir("sdk/dart/foresignal_api").asFile

/** The API version (info.version) that versions every SDK. */
fun sdkApiVersion(): String {
    val spec = JsonSlurper().parse(sdkSpec) as Map<*, *>
    val info = spec["info"] as? Map<*, *>
    return info?.get("version") as? String ?: throw GradleException("docs/OpenAPI.json has no info.version")
}

/** The dart-dio generator arguments shared by generation and the dry run. */
fun sdkDartGeneratorArgs(input: String, output: String): List<String> = listOf(
    "generate",
    "-i", input,
    "-g", "dart-dio",
    "-o", output,
    "--global-property", "apiTests=false,modelTests=false,apiDocs=false,modelDocs=false",
    // `override` and `_type` collide with Dart's @override and the `type` property; `index` with Enum.index.
    "--name-mappings", "override=overrideValue,_type=typeUnderscore",
    "--enum-name-mappings", "index=indexValue",
    "--additional-properties",
    "pubName=foresignal_api,serializationLibrary=json_serializable,pubVersion=${sdkApiVersion()}",
)

/**
 * The command prefix that runs openapi-generator: `java -jar <pinned jar>` when a Java runtime is on the PATH,
 * else the pinned Docker image with the repository mounted at /local (paths must then be under the repository).
 */
fun sdkOpenApiGenerator(): Pair<List<String>, (File) -> String> {
    if (sdkOnPath("java")) {
        return listOf("java", "-jar", sdkGeneratorJar.get().asFile.absolutePath) to { f: File -> f.absolutePath }
    }
    if (sdkOnPath("docker")) {
        val prefix = listOf("docker", "run", "--rm", "-v", "${sdkRoot.absolutePath}:/local",
            sdkVersion("openapiGenerator.dockerImage"))
        return prefix to { f: File -> "/local/" + f.absoluteFile.relativeTo(sdkRoot).invariantSeparatorsPath }
    }
    throw GradleException(
        "SDK generation needs openapi-generator ${sdkVersion("openapiGenerator.version")}: install a Java runtime " +
            "(Java 11+, e.g. https://adoptium.net/) so the pinned jar can be downloaded from Maven Central, " +
            "or Docker (https://docs.docker.com/get-docker/) to run ${sdkVersion("openapiGenerator.dockerImage")}."
    )
}

val sdkPreprocessDartSpec = tasks.register<Exec>("sdkPreprocessDartSpec") {
    group = "api"
    description = "Writes the OpenAPI 3.0.3 copy of docs/OpenAPI.json for dart-dio (scripts/sdk/preprocess-dart.py)."
    workingDir = sdkRoot
    inputs.files(sdkSpec, "scripts/sdk/preprocess-dart.py")
    outputs.file(sdkDartSpec)
    commandLine("python3", "scripts/sdk/preprocess-dart.py", "--input", sdkSpec.absolutePath,
        "--output", sdkDartSpec.get().asFile.absolutePath)
    doFirst {
        if (!sdkOnPath("python3")) {
            throw GradleException("sdkPreprocessDartSpec needs python3 (3.10+, standard library only) on the PATH.")
        }
    }
}

val sdkDownloadOpenApiGenerator = tasks.register("sdkDownloadOpenApiGenerator") {
    group = "api"
    description = "Downloads the pinned openapi-generator-cli jar from Maven Central and checks its SHA-256."
    val jar = sdkGeneratorJar
    val url = sdkVersion("openapiGenerator.jarUrl")
    val sha256 = sdkVersion("openapiGenerator.sha256")
    outputs.file(jar)
    onlyIf("a Java runtime is on the PATH (Docker is used otherwise)") { sdkOnPath("java") }
    doLast {
        val target = jar.get().asFile
        fun digest(f: File) = MessageDigest.getInstance("SHA-256").digest(f.readBytes())
            .joinToString("") { "%02x".format(it) }
        if (target.isFile && digest(target) == sha256) {
            return@doLast
        }
        target.parentFile.mkdirs()
        val partial = File(target.parentFile, target.name + ".part")
        java.net.URI(url).toURL().openStream().use { input -> partial.outputStream().use { input.copyTo(it) } }
        val actual = digest(partial)
        if (actual != sha256) {
            partial.delete()
            throw GradleException("openapi-generator jar SHA-256 mismatch: expected $sha256, got $actual ($url)")
        }
        partial.renameTo(target)
    }
}

val sdkCleanDart = tasks.register<Delete>("sdkCleanDart") {
    group = "api"
    description = "Deletes the generated Dart sources (lib/, doc/) so removed schemas leave no stale files."
    delete(File(sdkDartPackage, "lib"), File(sdkDartPackage, "doc"), File(sdkDartPackage, "test"))
}

val sdkGenerateDart = tasks.register<Exec>("sdkGenerateDart") {
    group = "api"
    description = "Generates sdk/dart/foresignal_api with openapi-generator dart-dio from the preprocessed spec."
    dependsOn(sdkPreprocessDartSpec, sdkDownloadOpenApiGenerator, sdkCleanDart)
    workingDir = sdkRoot
    doFirst {
        val (prefix, path) = sdkOpenApiGenerator()
        commandLine(prefix + sdkDartGeneratorArgs(path(sdkDartSpec.get().asFile), path(sdkDartPackage)))
    }
}

val sdkBuildRunnerDart = tasks.register<Exec>("sdkBuildRunnerDart") {
    group = "api"
    description = "Runs dart pub get and build_runner (json_serializable, copy_with) in sdk/dart/foresignal_api."
    dependsOn(sdkGenerateDart)
    workingDir = sdkDartPackage
    commandLine("sh", "-c", "dart pub get && dart run build_runner build --delete-conflicting-outputs")
    doFirst {
        if (!sdkOnPath("dart")) {
            throw GradleException(
                "sdkBuildRunnerDart needs the Dart SDK ${sdkVersion("dart.sdk")} (https://dart.dev/get-dart, or Flutter) " +
                    "on the PATH to write the json_serializable *.g.dart parts."
            )
        }
    }
}

val sdkGenerateTypescript = tasks.register<Exec>("sdkGenerateTypescript") {
    group = "api"
    description = "Generates sdk/typescript/src/generated/schema.ts with openapi-typescript."
    workingDir = sdkRoot
    inputs.files(sdkSpec, "sdk/typescript/redocly.yaml")
    commandLine("npx", "--yes", "openapi-typescript@${sdkVersion("openapiTypescript.version")}",
        sdkSpec.absolutePath, "-c", "sdk/typescript/redocly.yaml", "-o", "sdk/typescript/src/generated/schema.ts")
    doFirst {
        if (!sdkOnPath("npx")) {
            throw GradleException("sdkGenerateTypescript needs npx (Node.js >= 20.19, https://nodejs.org/) on the PATH.")
        }
    }
}

val sdkGeneratePython = tasks.register<Exec>("sdkGeneratePython") {
    group = "api"
    description = "Generates sdk/python/foresignal_client with openapi-python-client (--meta none)."
    workingDir = sdkRoot
    inputs.file(sdkSpec)
    doFirst {
        val tool = "openapi-python-client==${sdkVersion("openapiPythonClient.version")}"
        val args = listOf("generate", "--path", sdkSpec.absolutePath, "--meta", "none",
            "--config", "sdk/python/generator-config.yaml", "--output-path", "sdk/python/foresignal_client",
            "--overwrite")
        val prefix = when {
            sdkOnPath("pipx") -> listOf("pipx", "run", "--spec", tool, "openapi-python-client")
            sdkOnPath("uvx") -> listOf("uvx", "--from", tool, "openapi-python-client")
            else -> throw GradleException(
                "sdkGeneratePython needs pipx (https://pipx.pypa.io/) or uv (https://docs.astral.sh/uv/) with " +
                    "Python >= 3.11 to run $tool."
            )
        }
        commandLine(prefix + args)
    }
}

tasks.register("generateSdks") {
    group = "api"
    description = "Regenerates the Dart, TypeScript and Python API clients under sdk/ (prompt 141; not part of build)."
    dependsOn(sdkBuildRunnerDart, sdkGenerateTypescript, sdkGeneratePython)
}

val sdkCheckDartGeneration = tasks.register<Exec>("sdkCheckDartGeneration") {
    group = "api"
    description = "Dry-runs openapi-generator dart-dio on the preprocessed spec (writes nothing outside build/)."
    dependsOn(sdkPreprocessDartSpec, sdkDownloadOpenApiGenerator)
    workingDir = sdkRoot
    doFirst {
        val (prefix, path) = sdkOpenApiGenerator()
        val out = sdkBuildDir.get().dir("check/dart-dio").asFile
        out.mkdirs()
        commandLine(prefix + sdkDartGeneratorArgs(path(sdkDartSpec.get().asFile), path(out)) + "--dry-run")
    }
}

tasks.register("checkSdkGeneration") {
    group = "api"
    description = "Optional: preprocesses docs/OpenAPI.json for Dart and dry-runs the dart-dio generator (prompt 141)."
    dependsOn(sdkCheckDartGeneration)
}
