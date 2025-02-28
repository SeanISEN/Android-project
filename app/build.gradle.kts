plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
}

android {
    buildFeatures {
        buildConfig = true
    }

    namespace = "fr.isen.Bouhaben.isensmartcompanion"
    compileSdk = 35

    android {
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
    }


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
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "GOOGLE_AI_API_KEY", project.findProperty("GOOGLE_AI_API_KEY")?.let { "\"$it\"" } ?: "\"\"")
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    // Animation
    implementation(libs.androidx.animation)
    // Retrofit & Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.play.services.tasks)

    // ✅ Google AI Client SDK
    // Ensure this version exists
    implementation(libs.generativeai)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.material3)
    implementation(libs.gson)
}


