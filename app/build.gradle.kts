plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}
android {
    namespace = "com.mintlifescience.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mintlifescience.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.3"
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
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }

    kotlinOptions {
        jvmTarget = "19"
    }


    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}
kotlin {
    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation ("com.google.firebase:firebase-storage:20.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")


    // Other dependencies
    implementation("com.codesgood:justifiedtextview:1.1.0")
    implementation(libs.glide)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Media3 (replaces deprecated ExoPlayer 2.x)
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")

    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")


    //Room
    implementation ("androidx.room:room-runtime:2.6.1")
    kapt ("androidx.room:room-compiler:2.6.1")
    implementation ("com.google.code.gson:gson:2.9.0")
    implementation ("androidx.room:room-ktx:2.6.1")

    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Encrypted SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
