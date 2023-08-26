// TODO: Remove once KTIJ-19369 is fixed
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlinBuild.android.application) apply false
    alias(libs.plugins.kotlinBuild.android.library) apply false
    alias(libs.plugins.kotlinBuild.dependencyUpdates)
}

tasks.register<Copy>("installPreCommitHook") {
    from(fileTree("./scripts").include("pre-commit"))
    into(".git/hooks")
}