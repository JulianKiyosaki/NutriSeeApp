plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "1.9.0-1.0.11" apply false
}

android {
    namespace = "com.capstone.nutrisee"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.capstone.nutrisee"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        mlModelBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.3")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.3")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")
    implementation ("com.airbnb.android:lottie:6.1.0")

    val roomVersion = "2.6.0"
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.work:work-runtime:2.9.1")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation ("org.tensorflow:tensorflow-lite-gpu:2.12.0")

    val camerax_version = "1.3.4"
    implementation ("androidx.camera:camera-core:$camerax_version")
    implementation ("androidx.camera:camera-camera2:$camerax_version")
    implementation ("androidx.camera:camera-lifecycle:$camerax_version")
    implementation ("androidx.camera:camera-view:$camerax_version")

}