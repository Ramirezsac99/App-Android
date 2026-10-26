plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "edu.umg.controlgastos"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.umg.controlgastos"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }

    buildFeatures {
        viewBinding = true   // conecta el XML con Kotlin sin findViewById
    }
}

dependencies {
    implementation("umg.edu.gt.progra2:gastos-core:1.1.0")           // NUESTRA libreria (mvn install)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7") // para lifecycleScope
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
