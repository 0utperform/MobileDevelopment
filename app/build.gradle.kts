import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("com.google.dagger.hilt.android")
    id ("kotlin-kapt")
    id ("org.jetbrains.kotlin.plugin.serialization") version ("2.0.0")
    id ("kotlin-parcelize")
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

        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${properties.getProperty("SUPABASE_ANON_KEY")}\"")
    }
    kapt{
        correctErrorTypes = true
    }
   buildFeatures{
       viewBinding = true
       buildConfig = true
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
    implementation (libs.kotlinx.metadata.jvm)
    implementation (libs.androidx.appcompat.v161)
    implementation (libs.glide)
    implementation (libs.material)

    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.okhttp)

    implementation(libs.hilt.android)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.preference)
    kapt (libs.hilt.android.compiler)

    implementation (libs.facebook.login)

    implementation(platform(libs.firebase.bom))
    implementation (libs.google.material.v1110)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    //credential manager
    implementation(libs.supabase.kt)
    implementation (libs.postgrest.kt)
    implementation (libs.storage.kt)
    implementation (libs.auth.kt)
    implementation(libs.ktor.android.kt)
    implementation(libs.ktor.core.kt)
    implementation(libs.ktor.utils.kt)

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
