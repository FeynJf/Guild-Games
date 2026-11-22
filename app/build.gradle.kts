plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

val ksPath: String? = System.getenv("KEYSTORE_PATH")

/** Baca nilai rahasia dari env (GitHub Secrets) atau gradle.properties lokal, aman untuk string BuildConfig. */
fun secret(name: String): String =
    (System.getenv(name) ?: providers.gradleProperty(name).orNull ?: "")
        .replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "com.guild.games"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.guild.games"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"

        buildConfigField("String", "EMAILJS_SERVICE_ID", "\"${secret("EMAILJS_SERVICE_ID")}\"")
        buildConfigField("String", "EMAILJS_TEMPLATE_ID", "\"${secret("EMAILJS_TEMPLATE_ID")}\"")
        buildConfigField("String", "EMAILJS_PUBLIC_KEY", "\"${secret("EMAILJS_PUBLIC_KEY")}\"")
        buildConfigField("String", "APPROVER_EMAIL", "\"${secret("APPROVER_EMAIL")}\"")
    }

    signingConfigs {
        if (ksPath != null) {
            create("ci") {
                storeFile = file(ksPath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            if (ksPath != null) signingConfig = signingConfigs.getByName("ci")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (ksPath != null) signingConfig = signingConfigs.getByName("ci")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
