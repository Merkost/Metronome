import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.components.resources)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.kotlinx.coroutines.swing)
}

val macVersion = "1.5.0"
val macBuild = "1"

fun macSetting(property: String, environment: String) =
    providers.gradleProperty(property).orElse(providers.environmentVariable(environment))

val macAppStore = macSetting("macos.appStore", "METRONOME_MAC_APP_STORE")
    .map { value ->
        value.toBooleanStrictOrNull()
            ?: error("macos.appStore must be true or false")
    }
    .getOrElse(false)
val macTestFlight = macSetting("macos.testFlight", "METRONOME_MAC_TESTFLIGHT")
    .map { value ->
        value.toBooleanStrictOrNull()
            ?: error("macos.testFlight must be true or false")
    }
    .getOrElse(false)
require(!macTestFlight || macAppStore) { "macos.testFlight requires macos.appStore=true" }
val macJdkHome = macSetting("macos.jdkHome", "METRONOME_MACOS_JDK_HOME")
    .orElse(providers.environmentVariable("JAVA_HOME"))
    .getOrElse(System.getProperty("java.home"))
val macIdentity = macSetting("macos.signingIdentity", "METRONOME_MAC_SIGNING_IDENTITY")
    .getOrElse("Konstantin Merenkov (P47X2292CM)")
val macAppProfile = macSetting("macos.appProvisioningProfile", "METRONOME_MAC_APP_PROFILE")
val macRuntimeProfile = macSetting("macos.runtimeProvisioningProfile", "METRONOME_MAC_RUNTIME_PROFILE")
val macKeychain = macSetting("macos.signingKeychain", "METRONOME_MAC_SIGNING_KEYCHAIN")
val signingDirectory = layout.buildDirectory.dir("macos/signing")
val packagingValidator = rootProject.file("tools/macos/validate-packaging-inputs.py")
val desktopRuntimeClasspath = configurations.named("runtimeClasspath")
val skikoRuntime = desktopRuntimeClasspath.map { runtime ->
    runtime.files.single { it.name.startsWith("skiko-awt-runtime-macos-arm64-") && it.extension == "jar" }
}
val preparedSkikoDirectory = layout.buildDirectory.dir("macos/skiko-runtime")
val preparedSkikoRuntime = preparedSkikoDirectory.zip(skikoRuntime) { directory, source ->
    directory.file(source.name)
}
val skikoPreparer = rootProject.file("tools/macos/prepare-skiko-runtime.py")

val prepareMacSkikoRuntime by tasks.registering(Exec::class) {
    group = "distribution"
    description = "Prepares the arm64 Skiko dependency before Compose extraction and signing"
    inputs.file(skikoRuntime)
    inputs.file(skikoPreparer)
    inputs.file(rootProject.file("tools/macos/release_support.py"))
    outputs.dir(preparedSkikoDirectory)
    doFirst {
        commandLine(
            "/usr/bin/python3", skikoPreparer.absolutePath,
            "--source", skikoRuntime.get().absolutePath,
            "--destination", preparedSkikoRuntime.get().asFile.absolutePath,
            "--receipt", preparedSkikoDirectory.get().file("provenance.json").asFile.absolutePath,
        )
    }
}

val validateMacPackaging by tasks.registering(Exec::class) {
    group = "distribution"
    description = "Validates the arm64 JetBrains runtime and optional Mac App Store signing assets"
    val arguments = mutableListOf(
        "/usr/bin/python3",
        packagingValidator.absolutePath,
        "--root", rootProject.projectDir.absolutePath,
        "--jdk-home", macJdkHome,
    )
    if (macAppStore) {
        arguments += listOf(
            "--app-store",
            "--identity", macIdentity,
        )
    }
    if (macTestFlight) {
        arguments += listOf(
            "--testflight",
            "--app-profile", macAppProfile.getOrElse(""),
            "--runtime-profile", macRuntimeProfile.getOrElse(""),
            "--stage-directory", signingDirectory.get().asFile.absolutePath,
        )
    }
    commandLine(arguments)
}

compose.desktop {
    application {
        disableDefaultConfiguration()
        val desktopJar = tasks.named<Jar>("jar")
        mainJar.set(desktopJar.flatMap { it.archiveFile })
        fromFiles(
            desktopJar,
            desktopRuntimeClasspath.map { runtime ->
                runtime.filter { !it.name.startsWith("skiko-awt-runtime-macos-arm64-") }
            },
            files(preparedSkikoRuntime).builtBy(prepareMacSkikoRuntime),
        )
        mainClass = "com.merkost.metronome.desktop.MainKt"
        javaHome = macJdkHome
        jvmArgs += listOf(
            "-Xms32m",
            "-Xmx512m",
            "-Dapple.awt.application.appearance=system",
            "-Dapple.laf.useScreenMenuBar=true",
            "-Dmetronome.version=$macVersion",
            "-Dmetronome.build=$macBuild",
        )
        nativeDistributions {
            targetFormats(TargetFormat.Pkg)
            packageName = "Metronome"
            packageVersion = macVersion
            description = "A focused metronome and practice toolkit"
            vendor = "Merkost"
            copyright = "© 2026 Merkost"
            modules("java.desktop", "java.logging", "java.prefs", "jdk.unsupported", "jdk.charsets")
            macOS {
                bundleID = "com.merkost.metronome"
                dockName = "Metronome"
                packageVersion = macVersion
                packageBuildVersion = macBuild
                pkgPackageVersion = macVersion
                pkgPackageBuildVersion = macBuild
                minimumSystemVersion = "13.0"
                appCategory = "public.app-category.music"
                appStore = macAppStore
                iconFile.set(project.file("packaging/Metronome.icns"))
                signing {
                    sign.set(macAppStore)
                    if (macAppStore) {
                        identity.set(macIdentity)
                        if (macKeychain.isPresent) {
                            keychain.set(macKeychain)
                        }
                    }
                }
                if (macAppStore) {
                    val suffix = if (macTestFlight) "-testflight" else ""
                    entitlementsFile.set(project.file("packaging/app$suffix.entitlements.plist"))
                    runtimeEntitlementsFile.set(project.file("packaging/runtime$suffix.entitlements.plist"))
                }
                if (macTestFlight) {
                    provisioningProfile.set(signingDirectory.map { it.file("embedded.provisionprofile") })
                    runtimeProvisioningProfile.set(signingDirectory.map { it.file("runtime.provisionprofile") })
                }
                infoPlist {
                    extraKeysRawXml = """
                        <key>CFBundleDisplayName</key>
                        <string>Metronome</string>
                        <key>ITSAppUsesNonExemptEncryption</key>
                        <false/>
                        <key>LSArchitecturePriority</key>
                        <array><string>arm64</string></array>
                    """.trimIndent()
                }
            }
        }
    }
}

tasks.matching {
    it.name in setOf(
        "createDistributable",
        "createReleaseDistributable",
        "packagePkg",
        "packageReleasePkg",
    )
}.configureEach {
    dependsOn(validateMacPackaging)
}
