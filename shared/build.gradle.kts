import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "com.merkost.metronome")
        }
        if (iosTarget.name == "iosSimulatorArm64") {
            providers.gradleProperty("iosTestSdkDirectory").orNull?.let { directory ->
                val linkerOptions = file(directory).resolve("linker-options.txt")
                require(linkerOptions.isFile) {
                    "Prepare the real Xcode simulator SDK objects with tools/testing/package-ios-test-sdk.py before setting iosTestSdkDirectory"
                }
                iosTarget.binaries.getTest("DEBUG").linkerOpts(linkerOptions.readLines().filter(String::isNotBlank))
            }
        }
    }


    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.icons.lucide)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.lifecycle.viewmodel.navigation3)
            implementation(libs.navigation3.runtime)
            implementation(libs.navigation3.ui)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.datastore.preferences.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            api(libs.cedar.logging)
        }

        androidMain.dependencies {
            implementation(libs.gitlive.crashlytics)
            implementation(libs.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.accompanist.permissions)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.play.review)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        iosMain.dependencies {
            implementation(libs.gitlive.crashlytics)
        }

        iosTest.dependencies {
            implementation("org.jetbrains.compose.ui:ui-test:${libs.versions.composeMultiplatform.get()}")
        }
    }
}

android {
    namespace = "com.merkost.metronome"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
