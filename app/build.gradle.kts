plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("com.google.dagger.hilt.android")
    id ("kotlin-kapt")

}


android {
    namespace = "com.example.a0utperform"
    compileSdk = 35
    lint {
        baseline = file("lint-baseline.xml")
    }
    defaultConfig {
        applicationId = "com.example.a0utperform"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    kapt{
        correctErrorTypes = true
    }
   buildFeatures{
       viewBinding = true
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
}

dependencies {

    implementation (libs.firebase.firestore.ktx)
    implementation(libs.play.services.auth)

    implementation (libs.material)

    implementation(libs.hilt.android)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.datastore.preferences)
    kapt (libs.hilt.android.compiler)

    implementation (libs.facebook.login)

    implementation(platform(libs.firebase.bom))
    implementation (libs.google.material.v1110)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    //credential manager
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
