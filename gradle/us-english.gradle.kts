// checkUsEnglish (prompt 149): US English is the only spelling in this project. This task scans the project-authored
// text (src/main/**/*.java, src/test/**/*.java and docs/*.md) for British spellings and fails the build with each
// finding's file:line. Wired into `check` by build.gradle.kts.
//
// What it flags: the British forms in gradle/us-english/british-words.txt, a family list whose inflections are
// generated here. Each identifier is split at camelCase, underscore and digit boundaries, so `licenceUrl`,
// `LICENCE_TERMS` and "licence" in prose all match; a whole-word match never fires inside an unrelated word.
//
// What it skips: resources, docs/OpenAPI.json, docs/api/** and build/ are outside the scanned trees. Text that must
// keep its British spelling (the five never-correct cases: license files and copyright holders, vendored code and a
// dependency's own API, direct quotations, translations, and anything that crosses a wire or persists) is
// suppressed one of two ways, each with a reason:
//   - a line marker, `// us-english: <reason>` (Java) or `<!-- us-english: <reason> -->` (Markdown);
//   - an entry in gradle/us-english/allowlist.txt, `<path glob> | <match> | [<category>] <reason>`, where <match> is
//     `code:<british>` (the word as or inside an identifier: Java code outside comments and string literals, a
//     javadoc {@code}/{@link} reference, or a Markdown `code span` or fence), `string:<british>` (the word inside a
//     Java string literal: SQL, JSON keys and values in the matching files), `ident:<Identifier>` (exactly that
//     identifier, anywhere), `text:<substring>` (a finding on a line containing that text) or `word:<british>` (any
//     use of the word in matching files: only for a file that is wholly data, quotation or foreign text).
//
// A compound token (camelCase, snake_case or with digits) is an identifier wherever it appears, in a comment, in
// prose or inside a string literal (a JSON key, a JSONPath), so it is judged as code.
//
// Each finding is printed with its context (code, string, comment or prose), so the reviewer of a new suppression
// can see whether it names an identifier or corrects nothing but a sentence.
// An allowlist entry that no longer matches anything fails the task too, so the list cannot rot.

val usEnglishRoot: File = layout.projectDirectory.asFile
val usEnglishWords: File = file("gradle/us-english/british-words.txt")
val usEnglishAllowlist: File = file("gradle/us-english/allowlist.txt")
val usEnglishSources = files(
    fileTree("src/main") { include("**/*.java"); exclude("**/resources/**") },
    fileTree("src/test") { include("**/*.java"); exclude("**/resources/**") },
    fileTree("docs") { include("*.md") },
)
val usEnglishReport = layout.buildDirectory.file("reports/us-english/findings.txt")

tasks.register("checkUsEnglish") {
    group = "verification"
    description = "Fails on a British spelling in src/main, src/test (Java) or docs/*.md that is not allowlisted with a reason."
    inputs.files(usEnglishSources, usEnglishWords, usEnglishAllowlist)
    outputs.file(usEnglishReport)
    val root = usEnglishRoot
    val wordsFile = usEnglishWords
    val allowFile = usEnglishAllowlist
    val sources = usEnglishSources
    val report = usEnglishReport
    doLast {
        val dictionary = UsEnglish.loadDictionary(wordsFile)
        val allowlist = UsEnglish.loadAllowlist(allowFile)
        val findings = mutableListOf<String>()
        var scanned = 0
        var suppressedByMarker = 0
        var suppressedByAllowlist = 0
        for (source in sources.files.sortedBy { it.path }) {
            scanned++
            val path = source.relativeTo(root).invariantSeparatorsPath
            val entries = allowlist.filter { it.matcher.matches(java.nio.file.Paths.get(path)) }
            val text = source.readText()
            val contexts = if (path.endsWith(".md")) UsEnglish.markdownContexts(text) else UsEnglish.javaContexts(text)
            var offset = 0
            text.split("\n").forEachIndexed { index, line ->
                val lineStart = offset
                offset += line.length + 1
                val hits = UsEnglish.scanLine(line) { dictionary[it] }.map { it.at(UsEnglish.contextOf(it, contexts[lineStart + it.column])) }
                if (hits.isEmpty()) {
                    return@forEachIndexed
                }
                val marker = UsEnglish.MARKER.find(line)
                if (marker != null) {
                    if (marker.groupValues[1].isBlank()) {
                        findings.add("$path:${index + 1}: a us-english marker needs a reason")
                    } else {
                        suppressedByMarker += hits.size
                    }
                    return@forEachIndexed
                }
                for (hit in hits) {
                    val entry = entries.firstOrNull { it.allows(hit, line) }
                    if (entry != null) {
                        entry.used++
                        suppressedByAllowlist++
                    } else {
                        val token = if (hit.token.equals(hit.word, ignoreCase = false)) "" else "  (in ${hit.token})"
                        findings.add("$path:${index + 1}: ${hit.word} -> ${hit.american} [${hit.context}]$token")
                    }
                }
            }
        }
        allowlist.filter { it.used == 0 }.forEach {
            findings.add("${allowFile.relativeTo(root).invariantSeparatorsPath}:${it.lineNumber}: unused allowlist entry '${it.glob} | ${it.kind}:${it.value}'")
        }
        val out = report.get().asFile
        out.parentFile.mkdirs()
        out.writeText(findings.joinToString("\n", postfix = if (findings.isEmpty()) "" else "\n"))
        logger.lifecycle(
            "checkUsEnglish: $scanned files, ${dictionary.size} British forms; suppressed $suppressedByMarker by marker, " +
                "$suppressedByAllowlist by ${allowlist.size} allowlist entries; ${findings.size} finding(s)"
        )
        allowlist.groupBy { it.category }.toSortedMap().forEach { (category, entries) ->
            logger.info("  [$category] ${entries.size} entries, ${entries.sumOf { it.used }} suppressed")
            entries.forEach { logger.info("    ${it.used}\t${it.glob} | ${it.kind}:${it.value}") }
        }
        if (findings.isNotEmpty()) {
            findings.forEach { logger.error(it) }
            throw GradleException(
                "checkUsEnglish: ${findings.size} finding(s), listed above and in $out. Correct the spelling, or, for " +
                    "text that must keep it (a wire or persisted name, a quotation, a proper noun, foreign or vendored " +
                    "text), add a reasoned `us-english:` marker or a gradle/us-english/allowlist.txt entry."
            )
        }
    }
}

/** The dictionary, the allowlist and the per-line scan behind checkUsEnglish. */
object UsEnglish {
    /** A line marker: `us-english:` followed by its reason. */
    val MARKER = Regex("""us-english:\s*([^>]*?)\s*(?:-->|$)""")

    private val TOKEN = Regex("""[A-Za-z][A-Za-z0-9_]*""")
    private val WORD = Regex("""[A-Z]+(?![a-z])|[A-Z]?[a-z]+""")
    private val CATEGORIES = setOf("wire", "data", "quote", "license", "vendored", "foreign", "proper-noun")

    /** A British form: its US spelling, and whether a standalone capitalized use is a proper noun to skip. */
    class Entry(val american: String, val lowercaseOnly: Boolean)

    /** Where a character sits: an identifier or code reference, a Java string literal, or prose (comment or text). */
    enum class Context { CODE, STRING, COMMENT, PROSE;
        override fun toString() = name.lowercase()
    }

    /** One British word found on a line: the word as written, the identifier it sits in, its US spelling, its column. */
    class Hit(val word: String, val token: String, val american: String, val column: Int,
              val context: Context = Context.PROSE) {
        /** This hit placed in [context]. */
        fun at(context: Context) = Hit(word, token, american, column, context)
    }

    /** One reasoned allowlist entry; `used` counts the findings it suppressed in this run. */
    class Allowed(val lineNumber: Int, val glob: String, val kind: String, val value: String, val category: String) {
        val matcher: java.nio.file.PathMatcher = java.nio.file.FileSystems.getDefault().getPathMatcher("glob:$glob")
        var used = 0

        /** True when this entry covers [hit] on [line]. */
        fun allows(hit: Hit, line: String): Boolean = when (kind) {
            "word" -> hit.word.lowercase() == value
            "code" -> hit.word.lowercase() == value && hit.context == Context.CODE
            "string" -> hit.word.lowercase() == value && hit.context == Context.STRING
            "ident" -> hit.token == value
            else -> line.contains(value)
        }
    }

    /** Expands gradle/us-english/british-words.txt into a map from each lowercase British form to its entry. */
    fun loadDictionary(file: File): Map<String, Entry> {
        val map = LinkedHashMap<String, Entry>()
        fun put(british: String, american: String, lowercaseOnly: Boolean = false) {
            map[british] = Entry(american, lowercaseOnly)
        }
        file.readLines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.forEach { line ->
            val family = line.substringBefore(":").trim()
            val words = line.substringAfter(":").trim().split(Regex("""\s+"""))
            for (w in words) {
                when (family) {
                    "our" -> {
                        val stem = w.removeSuffix("our")
                        listOf("", "s", "ed", "ing", "ful", "fully", "less", "able", "ably", "ite", "ites", "er", "ers",
                            "hood", "hoods", "ly", "al", "ally", "ist", "ists", "ism")
                            .forEach { put("${stem}our$it", "${stem}or$it") }
                    }
                    "ise" -> listOf("e", "ed", "es", "ing", "ation", "ations", "er", "ers", "able")
                        .forEach { put("${w}is$it", "${w}iz$it") }
                    "yse" -> listOf("e", "ed", "er", "ers", "ing").forEach { put("${w}ys$it", "${w}yz$it") }
                    "re" -> {
                        val stem = w.removeSuffix("re")
                        put(w, "${stem}er")
                        put("${w}s", "${stem}ers")
                        put("${w}d", "${stem}ered")
                        put("${stem}ring", "${stem}ering")
                    }
                    "ce" -> {
                        val stem = w.removeSuffix("ce")
                        put(w, "${stem}se")
                        put("${w}s", "${stem}ses")
                    }
                    "ogue" -> {
                        val stem = w.removeSuffix("ue")
                        put(w, stem)
                        put("${w}s", "${stem}s")
                        put("${w}d", "${stem}ed")
                        put("${stem}uing", "${stem}ing")
                    }
                    "ll" -> listOf("ed", "ing", "er", "ers").forEach { put("${w}l$it", "$w$it") }
                    "pair", "lower" -> put(w.substringBefore("="), w.substringAfter("="), family == "lower")
                    else -> throw GradleException("${file.name}: unknown family '$family'")
                }
            }
        }
        return map
    }

    /** Reads gradle/us-english/allowlist.txt; every entry needs a glob, a match, a known [category] and a reason. */
    fun loadAllowlist(file: File): List<Allowed> = file.readLines().mapIndexedNotNull { index, raw ->
        val line = raw.trim()
        if (line.isEmpty() || line.startsWith("#")) {
            return@mapIndexedNotNull null
        }
        val parts = line.split("|", limit = 3).map { it.trim() }
        val reason = parts.getOrNull(2).orEmpty()
        val category = Regex("""^\[([a-z-]+)]\s*(.*)$""").find(reason)
        if (parts.size < 3 || category == null || category.groupValues[1] !in CATEGORIES ||
            category.groupValues[2].isBlank()
        ) {
            throw GradleException(
                "${file.name}:${index + 1}: expected '<glob> | word:|ident:|text:<value> | [category] reason' " +
                    "with a category in $CATEGORIES"
            )
        }
        val kind = parts[1].substringBefore(":")
        if (kind !in setOf("word", "code", "string", "ident", "text")) {
            throw GradleException("${file.name}:${index + 1}: the match must start code:, string:, ident:, text: or word:")
        }
        val value = parts[1].substringAfter(":")
        Allowed(index + 1, parts[0], kind, if (kind in setOf("word", "code", "string")) value.lowercase() else value,
            category.groupValues[1])
    }

    /** Every British word on [line], found in its identifiers and prose words; [lookup] maps a lowercase word. */
    fun scanLine(line: String, lookup: (String) -> Entry?): List<Hit> {
        val hits = mutableListOf<Hit>()
        for (token in TOKEN.findAll(line)) {
            for (word in WORD.findAll(token.value)) {
                val entry = lookup(word.value.lowercase()) ?: continue
                val standaloneCapitalized = word.value == token.value &&
                    word.value[0].isUpperCase() && word.value.drop(1).all { it.isLowerCase() }
                if (entry.lowercaseOnly && standaloneCapitalized) {
                    continue
                }
                hits.add(Hit(word.value, token.value, entry.american, token.range.first + word.range.first))
            }
        }
        return hits
    }

    private val JAVADOC_REFERENCE = Regex("""\{@(?:code|link|linkplain|value|literal)\s[^}]*}|@param\s+<?\w+>?""")

    /**
     * The context a hit is judged in: where it sits, except that a compound token (camelCase, underscores or digits)
     * is an identifier being named wherever it sits, since no sentence spells `licenceFiltered`.
     */
    fun contextOf(hit: Hit, at: Context): Context = if (hit.token != hit.word) Context.CODE else at

    /**
     * The context of every character of a Java source: code, string or char literal (text blocks included), or
     * comment, with a javadoc {@code}/{@link} reference or {@literal @}param name inside a comment counted as code.
     */
    fun javaContexts(text: String): Array<Context> {
        val out = Array(text.length + 1) { Context.CODE }
        var i = 0
        fun mark(from: Int, to: Int, context: Context) {
            for (k in from until minOf(to, text.length)) out[k] = context
        }
        while (i < text.length) {
            val start = i
            when {
                text.startsWith("//", i) -> {
                    i = text.indexOf('\n', i).let { if (it < 0) text.length else it }
                    mark(start, i, Context.COMMENT)
                }
                text.startsWith("/*", i) -> {
                    i = text.indexOf("*/", i + 2).let { if (it < 0) text.length else it + 2 }
                    mark(start, i, Context.COMMENT)
                }
                text.startsWith("\"\"\"", i) -> {
                    i += 3
                    while (i < text.length && !text.startsWith("\"\"\"", i)) i += if (text[i] == '\\') 2 else 1
                    i = minOf(i + 3, text.length)
                    mark(start, i, Context.STRING)
                }
                text[i] == '"' || text[i] == '\'' -> {
                    val quote = text[i++]
                    while (i < text.length && text[i] != quote && text[i] != '\n') i += if (text[i] == '\\') 2 else 1
                    i = minOf(i + 1, text.length)
                    mark(start, i, Context.STRING)
                }
                else -> i++
            }
            if (out[start] == Context.COMMENT) {
                JAVADOC_REFERENCE.findAll(text.substring(start, i)).forEach { mark(start + it.range.first, start + it.range.last + 1, Context.CODE) }
            }
        }
        return out
    }

    /**
     * The context of every character of a Markdown file: prose, or code (a fenced block, or a `code span`, which may
     * wrap onto the next line but never across a blank one).
     */
    fun markdownContexts(text: String): Array<Context> {
        val out = Array(text.length + 1) { Context.PROSE }
        var offset = 0
        var fenced = false
        var spanStart = -1
        for (line in text.split("\n")) {
            val fence = line.trimStart().startsWith("```")
            if (fenced || fence) {
                for (k in offset until offset + line.length) out[k] = Context.CODE
                spanStart = -1
            } else if (line.isBlank()) {
                spanStart = -1
            } else {
                for (k in line.indices) {
                    if (line[k] != '`') continue
                    if (spanStart < 0) {
                        spanStart = offset + k
                    } else {
                        for (c in spanStart..offset + k) out[c] = Context.CODE
                        spanStart = -1
                    }
                }
            }
            if (fence) fenced = !fenced
            offset += line.length + 1
        }
        return out
    }
}
