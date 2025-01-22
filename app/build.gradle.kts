plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.android.gms)
}

android {
    namespace = "com.freewillsolutions.scantext"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.freewillsolutions.scantext"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1")
    implementation("com.github.bumptech.glide:glide:4.15.0")
//    implementation("com.google.firebase:firebase-ml-vision:24.0.3")
//    implementation("com.google.firebase:firebase-ml-vision-image-label-model:20.0.1")
    implementation("com.google.mlkit:text-recognition:16.0.1")

    implementation(libs.androidx.camera.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}