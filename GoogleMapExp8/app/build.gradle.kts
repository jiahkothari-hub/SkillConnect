plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.googlemapexp8"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.googlemapexp8"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
}
