plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val signingValues = listOf("ANDROID_KEYSTORE_PATH", "ANDROID_KEYSTORE_PASSWORD", "ANDROID_KEY_ALIAS", "ANDROID_KEY_PASSWORD")
    .associateWith { providers.environmentVariable(it).orNull }

android {
    namespace = "com.aizzak.android"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.aizzak.android"
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
        debug { applicationIdSuffix = ".debug" }
        release {
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
    buildFeatures { compose = true }
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

// Isolate Detekt's compiler from Kotlin 2.3. Opt-in task; no baseline suppressions.
val detektCli by configurations.creating
dependencies {
    implementation(libs.appcompat)
    implementation(libs.datastore.preferences)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.kotlinx.serialization.core)
    detektCli(libs.detekt.cli) { artifact { classifier = "all" } }
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
tasks.register<JavaExec>("detekt") {
    group = "verification"
    description = "Run isolated Detekt source analysis. Review parser support when adding new Kotlin syntax."
    mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")
    classpath = detektCli
    args("--input", "src", "--build-upon-default-config", "--config", "../config/detekt.yml")
}
