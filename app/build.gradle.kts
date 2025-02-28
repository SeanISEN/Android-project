// ------------------------------------ Plugins ------------------------------------
// Define plugins required for the project
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize") // ✅ Required for Parcelable support
    id("kotlin-kapt")      // ✅ Required for annotation processing (Room, etc.)
}

// ------------------------------------ Annotation Processing (KAPT) ------------------------------------
kapt {
    correctErrorTypes = true
}

android {
    // ------------------------------------ Build Features ------------------------------------
    buildFeatures {
        buildConfig = true
        compose = true
    }

    namespace = "fr.isen.Bouhaben.isensmartcompanion"
    compileSdk = 35

    // ------------------------------------ Default Configurations ------------------------------------
    defaultConfig {
        applicationId = "fr.isen.Bouhaben.isensmartcompanion"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject API Key into BuildConfig
        buildConfigField("String", "GOOGLE_AI_API_KEY", "\"${project.findProperty("GOOGLE_AI_API_KEY")}\"")
    }

    // ------------------------------------ Build Types ------------------------------------
    buildTypes {
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "GOOGLE_AI_API_KEY", project.findProperty("GOOGLE_AI_API_KEY")?.let { "\"$it\"" } ?: "\"\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "GOOGLE_AI_API_KEY", project.findProperty("GOOGLE_AI_API_KEY")?.let { "\"$it\"" } ?: "\"\"")
        }
    }

    // ------------------------------------ Compile Options ------------------------------------
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

// ------------------------------------ Dependencies ------------------------------------
dependencies {
    // ✅ Core Android Dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))

    // ✅ Jetpack Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    // ✅ Animations
    implementation(libs.androidx.animation)

    // ✅ Networking (Retrofit & Gson)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.play.services.tasks)

    // ✅ Google AI Client SDK
    implementation(libs.generativeai)

    // ✅ Room Database Dependencies (Local Storage)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler) // Room annotation processor

    // ✅ Testing Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // ✅ Debugging & UI Tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ✅ Additional Libraries
    implementation(libs.material3)
    implementation(libs.gson)
}
