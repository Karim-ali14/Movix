import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")

    if (file.exists()) {
        file.inputStream().use(::load)
    }
}
val apiBaseUrl = localProperties.getProperty(
    "api.base.url",
    "https://api.themoviedb.org/3/"
)

require(apiBaseUrl.endsWith("/")) {
    "api.base.url must end with '/' (got: $apiBaseUrl)"
}
val accessToken = localProperties.getProperty(
    "tmdb.access.token",
    ""
)

require(accessToken.isNotBlank()) {
    "TMDB Access Token is missing. Add tmdb.access.token to local.properties"
}

fun String.toBuildConfigString(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""


android {
    namespace = "com.karimali.movix"
    compileSdk = 36


    defaultConfig {
        applicationId = "com.karimali.movix"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        buildConfigField(
            "String",
            "BASE_URL",
            apiBaseUrl.toBuildConfigString()
        )

        buildConfigField(
            "String",
            "TMDB_ACCESS_TOKEN",
            accessToken.toBuildConfigString()
        )
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
// AndroidX & Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.kotlinx.serialization.json)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.androidx.junit.ktx)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Retrofit & OkHttp (Network)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Room Database (Caching)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    // Paging 3
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    // Navigation Compose
    implementation(libs.navigation.compose)

    // --- Unit Testing ---
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.robolectric)

    // --- Instrumented UI Testing ---
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.turbine)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Firebase Cloud Messaging
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
}