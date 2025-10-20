plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

val apiKey: String = project.findProperty("API_KEY") as? String ?: ""

android {
    namespace = "com.example.catbreeds.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 28
        buildConfigField("String", "API_KEY", "\"${apiKey}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))

    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Tests
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.androidx.core)
    testImplementation(kotlin("test"))
    testImplementation(project(":test_core"))

    androidTestImplementation(libs.hilt.android.testing.v2571)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}