// TODO: Remove once KTIJ-19369 is fixed
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.convention.application) apply false
    alias(libs.plugins.convention.library) apply false
    alias(libs.plugins.convention.dependencyUpdates)
}

tasks.register<Copy>("installPreCommitHook") {
    from(fileTree("./scripts").include("pre-commit"))
    into(".git/hooks")
}