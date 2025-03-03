plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id ("kotlin-kapt")
    id ("kotlin-android")
}

android {
    namespace = "com.android.aegentcam"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.android.aegentcam"
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

    viewBinding {
        enable = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.dagger)
    annotationProcessor (libs.dagger.compiler)
    kapt (libs.dagger.compiler)
    implementation (libs.androidx.multidex)
    implementation (libs.gson)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.logging.interceptor)
    implementation (libs.okhttp)
    implementation (libs.glide)
    annotationProcessor (libs.glide.compiler)
    implementation (libs.android.gif.drawable)
    implementation (libs.sdp.android)
    implementation (libs.ssp.android)
    implementation (libs.ismaeldivita.chip.navigation.bar)
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)
    implementation (libs.androidx.recyclerview)
    implementation (libs.dotsindicator)
    implementation(files("libs/pb_sdk.aar"))
    //Butterknife
    implementation ("com.jakewharton:butterknife:10.2.3")
    kapt ("com.jakewharton:butterknife-compiler:10.2.3")
}
