// checkUnidataScope (prompt 046): the build-time half of the one exception to the Maven-Central rule
// (Architecture §2 and §26). The Unidata repository is declared under exclusiveContent for the
// edu.ucar and edu.ucar.unidata groups; this task walks the resolved runtimeClasspath graph and fails
// when any component resolved from the repository named "unidata" has another group, so a later
// edit that widens the filter (or drops exclusiveContent) cannot quietly pull ordinary dependencies
// from a second host. Wired into `check` by build.gradle.kts; exercised by UnidataScopeBuildTest.
//
// The repository a component came from is not on Gradle's public result API, so the task reads
// ResolvedComponentResultInternal.getRepositoryName (Gradle 9.x), and fails loudly rather than
// passing if that internal type ever disappears.

import org.gradle.api.internal.artifacts.result.ResolvedComponentResultInternal

val unidataRepositoryName = "unidata"
val unidataGroups = setOf("edu.ucar", "edu.ucar.unidata")

tasks.register("checkUnidataScope") {
    group = "verification"
    description = "Fails if anything other than edu.ucar / edu.ucar.unidata resolves from the unidata repository."
    val graph = configurations.getByName("runtimeClasspath").incoming.resolutionResult.rootComponent
    doLast {
        val root = graph.get()
        val seen = LinkedHashSet<org.gradle.api.artifacts.result.ResolvedComponentResult>()
        val queue = ArrayDeque(listOf(root))
        while (queue.isNotEmpty()) {
            val component = queue.removeFirst()
            if (!seen.add(component)) {
                continue
            }
            component.dependencies
                .filterIsInstance<org.gradle.api.artifacts.result.ResolvedDependencyResult>()
                .forEach { queue.addLast(it.selected) }
        }
        val fromUnidata = mutableListOf<String>()
        val offenders = mutableListOf<String>()
        for (component in seen) {
            if (component == root) {
                continue
            }
            val internal = component as? ResolvedComponentResultInternal
                ?: throw GradleException(
                    "checkUnidataScope cannot read the repository of ${component.id}; " +
                        "Gradle's ResolvedComponentResultInternal changed, so update gradle/unidata-scope.gradle.kts."
                )
            if (internal.repositoryName != unidataRepositoryName) {
                continue
            }
            val id = component.moduleVersion
            val coordinates = "${id?.group}:${id?.name}:${id?.version}"
            fromUnidata.add(coordinates)
            if (id == null || id.group !in unidataGroups) {
                offenders.add(coordinates)
            }
        }
        if (offenders.isNotEmpty()) {
            throw GradleException(
                "Only ${unidataGroups.joinToString(" and ")} may resolve from the '$unidataRepositoryName' " +
                    "repository, but these did: ${offenders.joinToString(", ")}"
            )
        }
        logger.lifecycle("checkUnidataScope: ${fromUnidata.size} component(s) from '$unidataRepositoryName', all in ${unidataGroups.sorted()}")
        fromUnidata.sorted().forEach { logger.info("  $it") }
    }
}
