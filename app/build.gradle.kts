@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebasePerf)
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinCompose)
}

android {
    namespace = "com.merkost.metronome"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.merkost.metronome"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                arguments("-DANDROID_STL=c++_shared")
                cppFlags += ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        prefab = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    externalNativeBuild {
//        cmake {
//            path = file("src/main/cpp/CMakeLists.txt")
//            version = "3.22.1"
//        }
    }
}

dependencies {
//    api("com.google.oboe:oboe:1.8.1")

    implementation(libs.timber)

    implementation(libs.androidxCore)
    implementation(libs.lifecycleRuntime)
    implementation(libs.activityCompose)
    implementation(libs.lifecycleViewModelCompose)
    implementation(platform(libs.composeBom))
    implementation(libs.composeUi)
    implementation(libs.composeUiUtil)
    implementation(libs.composeUiGraphics)
    implementation(libs.composeToolingPreview)
    implementation(libs.material3)
    implementation(libs.materialIconsExtended)

    implementation(platform(libs.firebaseBom))
    implementation(libs.firebaseAnalytics)
    implementation(libs.firebaseCrashlytics)
    implementation(libs.firebasePerf)

    // Preferences DataStore
    implementation(libs.datastore)

    // Koin main features for Android
    implementation(libs.koinAndroid)
    implementation(libs.koinAndroidCompose)
    implementation(libs.accompanistPermissions)

    // Optionally, for SharedPreferences support (currently commented out)
    // implementation(libs.preferenceKtx)

    implementation(libs.navigationCompose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidxJunit)
    androidTestImplementation(libs.espresso)
    androidTestImplementation(platform(libs.composeBom))
    androidTestImplementation(libs.composeUiTestJunit4)
    debugImplementation(libs.composeTooling)
    debugImplementation(libs.composeTestManifest)
}