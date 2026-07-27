plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.suibiankan.tv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.suibiankan.tv"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // TV box CPU architectures (armeabi-v7a covers most Dangbei boxes)
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // ── Leanback (TV UI framework) ──
    implementation("androidx.leanback:leanback:1.0.0")

    // ── AndroidX Core ──
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // ── Lifecycle + ViewModel ──
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    // ── Navigation ──
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")

    // ── Room ──
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ── Networking ──
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // ── HTML Parsing ──
    implementation("org.jsoup:jsoup:1.17.2")

    // ── Coroutines ──
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ── Image Loading ──
    implementation("io.coil-kt:coil:2.5.0")

    // ── DI ──
    implementation("io.insert-koin:koin-android:3.5.3")

    // ── Logging ──
    implementation("com.jakewharton.timber:timber:5.0.1")

    // ── JSON ──
    implementation("com.google.code.gson:gson:2.10.1")

    // ── WebView ──
    implementation("androidx.webkit:webkit:1.9.0")
}
