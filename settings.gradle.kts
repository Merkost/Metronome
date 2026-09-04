pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
dependencyResolutionManagement {
    // PREFER_PROJECT rather than FAIL_ON_PROJECT_REPOS: the Kotlin plugin declares
    // its own ivy repositories for the wasmJs toolchain (Node, Yarn, Binaryen),
    // each with its own layout. FAIL_ON_PROJECT_REPOS rejects them and there is
    // no supported way to pre-declare all three here.
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "Metronome"
include(":shared", ":androidApp")
