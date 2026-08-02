plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.rlvault"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rlvault"
        minSdk = 26          // SAF folder picker (ACTION_OPEN_DOCUMENT_TREE) needs 21+,
                              // but we target 26+ for reliable scoped storage / notification APIs later.
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-milestone1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true   // no Compose yet — plain Views for milestone 1, matches "no
                              // placeholder UI code" instruction; kept simple/portable.
    }
}

dependencies {
    // Core — no Firebase, no networking libs, per spec (fully offline).
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0") // Material 3 components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Raw SQLite (androidx.sqlite is a thin wrapper over SQLiteOpenHelper, not Room —
    // deliberately avoiding Room's annotation processor per the "editor-agnostic,
    // CLI-buildable" requirement).
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")

    // Repository implementations hop off the main thread via Dispatchers.IO for all DB calls.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
