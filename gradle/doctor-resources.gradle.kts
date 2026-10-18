// packageDoctorResources (prompt 140): the runtime inputs of the adapter doctor, packaged into the jar.
//
// - Fixtures: src/test/resources/fixtures/** is copied to META-INF/foresignal/fixtures/ with a per-file cap of
//   256 KB. A larger JSON file is cut to a structurally valid prefix (its largest arrays halved until it fits), a
//   CSV/TSV file to its header plus the first 500 rows (fewer if still too large), a JSON Lines file to whole lines;
//   any other larger file is skipped with a log line. fixtures-index.json lists, per source id, the packaged files
//   with their sizes and SHA-256, and the capture date from the fixture README (Adapter Guide §8) when it has one.
// - Adapter source: src/main/java/ai/foresignal/source/adapters/**/*.java is copied to
//   META-INF/foresignal/adapter-src/ with adapter-src-index.json (adapter package -> files). The project is MIT, so
//   shipping its own sources is fine; files ported from third-party code (an origin header naming the project they
//   were ported from) and anything that looks like key material are left out and listed as excluded.
//
// Applied by build.gradle.kts and wired into processResources; nothing here touches the network.

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.security.MessageDigest

val doctorFixtureCap = 256 * 1024
val doctorCsvRows = 500
val doctorFixturesDir = layout.projectDirectory.dir("src/test/resources/fixtures")
val doctorAdaptersDir = layout.projectDirectory.dir("src/main/java/ai/foresignal/source/adapters")
val doctorOutputDir = layout.buildDirectory.dir("generated/doctor-resources")

fun doctorSha256(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

/** Halves the largest list in a parsed JSON tree; false when no list has more than one element. */
fun doctorHalveLargestList(root: Any?): Boolean {
    var best: MutableList<Any?>? = null
    fun walk(node: Any?) {
        when (node) {
            is MutableList<*> -> {
                @Suppress("UNCHECKED_CAST")
                val list = node as MutableList<Any?>
                if (list.size > 1 && (best == null || list.size > best!!.size)) {
                    best = list
                }
                list.forEach { walk(it) }
            }
            is Map<*, *> -> node.values.forEach { walk(it) }
        }
    }
    walk(root)
    val list = best ?: return false
    val keep = list.size / 2
    while (list.size > keep) {
        list.removeAt(list.size - 1)
    }
    return true
}

/** A JSON prefix under the cap, or null when none could be made. */
fun doctorTruncateJson(bytes: ByteArray, cap: Int): ByteArray? {
    val root = try {
        JsonSlurper().parse(bytes, "UTF-8")
    } catch (e: Exception) {
        return null
    }
    var out = JsonOutput.toJson(root).toByteArray(Charsets.UTF_8)
    while (out.size > cap) {
        if (!doctorHalveLargestList(root)) {
            return null
        }
        out = JsonOutput.toJson(root).toByteArray(Charsets.UTF_8)
    }
    return out
}

/** The first whole lines (a header plus up to maxRows rows when header is true) under the cap, or null. */
fun doctorTruncateLines(bytes: ByteArray, cap: Int, maxRows: Int, header: Boolean): ByteArray? {
    val lines = String(bytes, Charsets.UTF_8).split("\n")
    val fixed = if (header && lines.isNotEmpty()) 1 else 0
    var rows = minOf(maxRows, lines.size - fixed)
    while (rows >= 1) {
        val out = (lines.take(fixed + rows).joinToString("\n") + "\n").toByteArray(Charsets.UTF_8)
        if (out.size <= cap) {
            return out
        }
        rows /= 2
    }
    return null
}

/** The capture date (yyyy-MM-dd) a fixture README states on a line about the date or the capture, or null. */
fun doctorCapturedAt(readme: File): String? {
    if (!readme.isFile) {
        return null
    }
    val date = Regex("""(\d{4}-\d{2}-\d{2})""")
    for (line in readme.readLines()) {
        val lower = line.lowercase()
        if (lower.contains("date") || lower.contains("captur")) {
            date.find(line)?.let { return it.groupValues[1] }
        }
    }
    return null
}

val packageDoctorResources = tasks.register("packageDoctorResources") {
    group = "build"
    description = "Packages capped fixtures and adapter sources for the adapter doctor (prompt 140)."
    val fixturesRoot = doctorFixturesDir.asFile
    val adaptersRoot = doctorAdaptersDir.asFile
    inputs.dir(fixturesRoot)
    inputs.dir(adaptersRoot)
    outputs.dir(doctorOutputDir)
    doLast {
        val out = doctorOutputDir.get().asFile
        out.deleteRecursively()
        val fixturesOut = File(out, "META-INF/foresignal/fixtures")
        val sourcesOut = File(out, "META-INF/foresignal/adapter-src")
        fixturesOut.mkdirs()
        sourcesOut.mkdirs()

        // ---- fixtures ----
        val sources = sortedMapOf<String, Any>()
        val skipped = mutableListOf<Map<String, Any>>()
        val sourceDirs = fixturesRoot.listFiles { f -> f.isDirectory }?.sortedBy { it.name } ?: emptyList()
        for (dir in sourceDirs) {
            val files = mutableListOf<Map<String, Any?>>()
            dir.walkTopDown().filter { it.isFile }.sortedBy { it.relativeTo(dir).invariantSeparatorsPath }.forEach { file ->
                val rel = file.relativeTo(dir).invariantSeparatorsPath
                val original = file.readBytes()
                val name = file.name.lowercase()
                var packaged: ByteArray? = original
                var truncation: String? = null
                if (original.size > doctorFixtureCap) {
                    packaged = when {
                        name.endsWith(".json") || name.endsWith(".geojson") ->
                            doctorTruncateJson(original, doctorFixtureCap)?.also { truncation = "json-arrays" }
                        name.endsWith(".csv") || name.endsWith(".tsv") ->
                            doctorTruncateLines(original, doctorFixtureCap, doctorCsvRows, true)?.also { truncation = "csv-rows" }
                        name.endsWith(".jsonl") || name.endsWith(".ndjson") ->
                            doctorTruncateLines(original, doctorFixtureCap, doctorCsvRows, false)?.also { truncation = "lines" }
                        else -> null
                    }
                }
                if (packaged == null) {
                    logger.lifecycle("packageDoctorResources: skipped fixture ${dir.name}/$rel (${original.size} bytes, over the " +
                        "$doctorFixtureCap-byte cap and not truncatable)")
                    skipped.add(mapOf("sourceId" to dir.name, "file" to rel, "bytes" to original.size))
                    return@forEach
                }
                val target = File(fixturesOut, dir.name + "/" + rel)
                target.parentFile.mkdirs()
                target.writeBytes(packaged)
                files.add(linkedMapOf(
                    "file" to rel,
                    "bytes" to packaged.size,
                    "originalBytes" to original.size,
                    "sha256" to doctorSha256(packaged),
                    "truncated" to (truncation != null),
                    "truncation" to truncation,
                ))
            }
            if (files.isNotEmpty()) {
                val readme = File(dir, "README.md")
                sources[dir.name] = linkedMapOf(
                    "capturedAt" to doctorCapturedAt(readme),
                    "synthetic" to (readme.isFile && readme.readText().contains("synthetic: true")),
                    "files" to files,
                )
            }
        }
        File(fixturesOut, "fixtures-index.json").writeText(JsonOutput.prettyPrint(JsonOutput.toJson(linkedMapOf(
            "version" to 1,
            "capBytes" to doctorFixtureCap,
            "sources" to sources,
            "skipped" to skipped,
        ))) + "\n")

        // ---- adapter source ----
        val packages = sortedMapOf<String, MutableList<String>>()
        val excluded = mutableListOf<Map<String, String>>()
        val keyMaterial = Regex("-----BEGIN [A-Z ]*PRIVATE KEY-----")
        adaptersRoot.walkTopDown().filter { it.isFile && it.name.endsWith(".java") }
            .sortedBy { it.relativeTo(adaptersRoot).invariantSeparatorsPath }
            .forEach { file ->
                val rel = file.relativeTo(adaptersRoot).invariantSeparatorsPath
                val text = file.readText()
                val head = text.lineSequence().take(40).joinToString("\n").lowercase()
                val reason = when {
                    keyMaterial.containsMatchIn(text) -> "looks like key material"
                    head.contains("god's eye view") || head.contains("ported from") -> "ported from third-party code"
                    else -> null
                }
                if (reason != null) {
                    excluded.add(mapOf("file" to rel, "reason" to reason))
                    return@forEach
                }
                val target = File(sourcesOut, rel)
                target.parentFile.mkdirs()
                target.writeText(text)
                val pkg = rel.substringBeforeLast('/', "")
                packages.getOrPut(pkg) { mutableListOf() }.add(rel)
            }
        File(sourcesOut, "adapter-src-index.json").writeText(JsonOutput.prettyPrint(JsonOutput.toJson(linkedMapOf(
            "version" to 1,
            "root" to "ai/foresignal/source/adapters",
            "packages" to packages,
            "excluded" to excluded,
        ))) + "\n")
    }
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(packageDoctorResources)
    from(doctorOutputDir)
}
