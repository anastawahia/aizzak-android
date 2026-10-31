import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
}

val signingValues = listOf("ANDROID_KEYSTORE_PATH", "ANDROID_KEYSTORE_PASSWORD", "ANDROID_KEY_ALIAS", "ANDROID_KEY_PASSWORD")
    .associateWith { providers.environmentVariable(it).orNull }

// Environment values are public origins, never credentials. A missing release origin
// fails release preparation, while debug and manifest inspection remain available.
val configuredApiBaseUrl = providers.gradleProperty("AIZZAK_API_BASE_URL")
    .orElse(providers.environmentVariable("AIZZAK_API_BASE_URL"))
val debugApiBaseUrl = configuredApiBaseUrl.orElse("http://127.0.0.1:8080/api/v1/")
fun quotedBuildConfig(value: String): String = "\"" + value
    .replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\""

android {
    namespace = "android.aizzak.com"
    compileSdk = 36
    defaultConfig {
        applicationId = "android.aizzak.com"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        if (signingValues.values.all { !it.isNullOrBlank() }) {
            create("upload") {
                storeFile = file(requireNotNull(signingValues["ANDROID_KEYSTORE_PATH"]))
                storePassword = signingValues["ANDROID_KEYSTORE_PASSWORD"]
                keyAlias = signingValues["ANDROID_KEY_ALIAS"]
                keyPassword = signingValues["ANDROID_KEY_PASSWORD"]
            }
        }
    }
    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", quotedBuildConfig(debugApiBaseUrl.get()))
        }
        release {
            buildConfigField("String", "API_BASE_URL", quotedBuildConfig(configuredApiBaseUrl.orNull.orEmpty()))
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("upload")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true; buildConfig = true }
    testOptions { unitTests.isReturnDefaultValues = false }
}
kotlin { jvmToolchain(17) }

val validateReleaseSigning by tasks.registering {
    doLast {
        check(signingValues.values.all { !it.isNullOrBlank() }) {
            "Release signing requires ANDROID_KEYSTORE_PATH, ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS, ANDROID_KEY_PASSWORD from an approved secret provider."
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn(validateReleaseSigning) }

val validateReleaseApiEnvironment by tasks.registering {
    doLast {
        val value = configuredApiBaseUrl.orNull
        val uri = value?.let { runCatching { URI(it) }.getOrNull() }
        check(uri != null && uri.scheme == "https" && !uri.host.isNullOrBlank() &&
            uri.rawUserInfo == null && uri.rawQuery == null && uri.rawFragment == null &&
            uri.rawPath == "/api/v1/") {
            "Release requires AIZZAK_API_BASE_URL as an absolute HTTPS origin with /api/v1/ and no credentials, query or fragment."
        }
    }
}
tasks.matching { it.name in setOf("assembleRelease", "bundleRelease", "packageRelease", "packageReleaseBundle") }
    .configureEach { dependsOn(validateReleaseApiEnvironment) }

// Isolate Detekt's compiler from Kotlin 2.3. Opt-in task; no baseline suppressions.
val detektCli by configurations.creating
dependencies {
    implementation(libs.appcompat)
    implementation(libs.datastore.preferences)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    detektCli(libs.detekt.cli) { artifact { classifier = "all" } }
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
}
tasks.register<JavaExec>("detekt") {
    group = "verification"
    description = "Run isolated Detekt source analysis. Review parser support when adding new Kotlin syntax."
    mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")
    classpath = detektCli
    args("--input", "src", "--build-upon-default-config", "--config", "../config/detekt.yml")
}
