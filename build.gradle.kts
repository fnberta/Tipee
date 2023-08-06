// TODO: Remove once KTIJ-19369 is fixed
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.updates)
    alias(libs.plugins.catalog.update)
}

versionCatalogUpdate {
    keep {
        keepUnusedLibraries.set(true)
        keepUnusedPlugins.set(true)
    }
}

tasks {
    dependencyUpdates {
        val immaturityLevels = listOf("rc", "cr", "m", "beta", "alpha", "preview")
            .map { ".*[.\\-]$it[.\\-\\d]*".toRegex(RegexOption.IGNORE_CASE) }

        fun getImmaturityLevel(version: String): Int =
            immaturityLevels.indexOfLast { version.matches(it) }

        rejectVersionIf { getImmaturityLevel(candidate.version) > getImmaturityLevel(currentVersion) }
    }

    register<Copy>("installPreCommitHook") {
        from(fileTree("./scripts").include("pre-commit"))
        into(".git/hooks")
    }
}