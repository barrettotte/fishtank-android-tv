plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.barrettotte.fishtank"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.barrettotte.fishtank"
        minSdk = 22
        targetSdk = 34
        versionCode = property("app.versionCode").toString().toInt()
        versionName = property("app.versionName").toString()
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    lint {
        // FlowOperatorInvokedInComposition crashes due to Kotlin metadata version mismatch between AGP 8.7 lint and newer dependencies
        disable += "FlowOperatorInvokedInComposition"
        abortOnError = true
        warningsAsErrors = false
    }
}

dependencies {
    // Compose for TV (pinned - newer versions require Kotlin 2.x)
    implementation("androidx.tv:tv-foundation:1.0.0-alpha10")
    implementation("androidx.tv:tv-material:1.0.0-alpha10")

    // Compose core (pinned to versions compatible with Kotlin 1.9.22 / Compose compiler 1.5.8)
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.6.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.1")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.runtime:runtime:1.6.1")

    // Navigation (pinned - 2.8+ requires Kotlin 2.0)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // ExoPlayer (Media3) - pinned to 1.2.1 for API 22 (Fire TV Stick 2nd Gen) support
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Leanback (for Android TV core support)
    implementation("androidx.leanback:leanback:1.2.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
}
