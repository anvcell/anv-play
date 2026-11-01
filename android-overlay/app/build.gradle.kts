plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.anvplay.overlay"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.anvplay.overlay"
        minSdk = 23
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-firestore")
}
