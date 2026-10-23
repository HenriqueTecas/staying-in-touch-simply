import groovy.json.JsonSlurper

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// The printable keyguard is the source of truth for the three dock controls.
val keyguardSource = layout.projectDirectory.file("../../design/pixel-9-pro-xl/Rev-B-PETG/parameters.json")
val keyguardGenerated = layout.buildDirectory.dir("generated/source/keyguard")
val generateKeyguardSpec by tasks.registering {
    inputs.file(keyguardSource)
    outputs.dir(keyguardGenerated)
    doLast {
        val spec = JsonSlurper().parse(keyguardSource.asFile) as Map<*, *>
        val phone = spec["phone"] as Map<*, *>
        val display = spec["display_reference"] as Map<*, *>
        val guard = spec["keyguard"] as Map<*, *>
        val output = keyguardGenerated.get().file("com/dumpphone/launcher/KeyguardSpec.kt").asFile
        output.parentFile.mkdirs()
        fun floats(key: String) = (guard[key] as List<*>).joinToString { "${it}f" }
        output.writeText("""
            // Generated from the Revision B CAD parameters; do not edit.
            package com.dumpphone.launcher
            internal object KeyguardSpec {
                const val phoneHeight = ${phone["height"]}f
                const val displayWidth = ${display["active_width_assumed"]}f
                const val displayHeight = ${display["active_height_assumed"]}f
                const val guardHeight = ${guard["height_from_phone_bottom"]}f
                const val rowFromBottom = ${guard["hole_row_from_phone_bottom"]}f
                val centersX = listOf(${floats("hole_x")})
                val diameters = listOf(${floats("hole_diameters")})
            }
        """.trimIndent())
    }
}

android {
    namespace = "com.dumpphone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dumpphone"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.1-keyguard"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    sourceSets.getByName("main").java.srcDir(keyguardGenerated)
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

tasks.named("preBuild") { dependsOn(generateKeyguardSpec) }

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
