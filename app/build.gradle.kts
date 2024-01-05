plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.mbytes.mkplayer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mbytes.mkplayer"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("org.apache.tika:tika-core:2.3.0")

    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}