@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.bcv)
    id("module.publication")
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    js {
        browser {
            testTask {
                enabled = false
            }
        }
    }

    wasmJs {
        browser {
            testTask {
                enabled = true
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets.commonMain.dependencies {
        implementation(libs.compose.runtime)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material)
        implementation(libs.compose.material3)

        // HTML parsing library
        implementation(libs.ksoup.html)
        implementation(libs.ksoup.entities)

        // Markdown parsing library
        implementation(libs.jetbrains.markdown)
    }

    sourceSets.androidMain.dependencies {
        // AndroidClipboard (the interface foundation's paste path type-checks at
        // runtime) only exists in compose-ui 1.12+, which would force compileSdk 37
        // + AGP 9.1 here. Instead we compile against the REAL AndroidClipboard(_androidKt)
        // classes extracted verbatim from ui-android:1.12.0-beta01 into a local stub
        // jar. compileOnly => never packaged; at runtime the app's compose-ui provides
        // them. Android consumers must be on compose-ui 1.12+ to use the rich editor.
        compileOnly(files("libs/compose-ui-1.12-clipboard-stub.jar"))
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(libs.compose.ui.test)
    }

    sourceSets.named("desktopTest").dependencies {
        implementation(libs.compose.ui.test.junit4)
        implementation(compose.desktop.currentOs)
    }
}

android {
    namespace = "com.mohamedrejeb.richeditor.compose"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        consumerProguardFile("proguard-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

apiValidation {
    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib {
        enabled = true
    }
}