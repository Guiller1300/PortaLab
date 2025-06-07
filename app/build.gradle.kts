plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.portalab"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.portalab"
        minSdk = 25
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
        compose = true
        viewBinding = true // Habilita ViewBinding si es necesario
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.1" // Versión del compilador de Compose
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))


    // Firebase sin versión explícita (la BOM la gestiona)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging-ktx")





    //Viewmodal
    //implementation('androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1')

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.2.0") // Última versión estable de UI
    implementation("androidx.compose.material3:material3:1.0.0")  // Material3
    implementation("androidx.compose.ui:ui-tooling-preview:1.2.0") // Previews
    implementation("androidx.compose.foundation:foundation:1.2.0") // Foundation Compose
    implementation("androidx.compose.runtime:runtime-livedata:1.2.0") // Livedata para Compose

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.material3:material3:1.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("com.google.firebase:firebase-auth:22.3.0") // o versión reciente


    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt:coil-compose:2.4.0")










}