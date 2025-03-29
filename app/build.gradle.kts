plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

android {
    namespace = "com.my.bookshelf"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.my.bookshelf"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 1) Signing-Konfiguration hinzufügen
    signingConfigs {
        create("release") {
            // Windows-Pfade funktionieren in Kotlin DSL i. d. R. auch mit Forward-Slashes
            storeFile = file("C:/Users/jonas/Nextcloud/Programier_Projekte/MyAndroidKeyStore.jks")
            // Wichtig: Dollarzeichen müssen mit '\' escaped werden
            storePassword = "n\$EzGtyMx5w4qqHKm*%@S7J%C"
            keyAlias = "mybookshelf"
            keyPassword = "n\$EzGtyMx5w4qqHKm*%@S7J%C"
        }
    }

    buildTypes {
        // 2) release-Build mit obiger Signing-Konfiguration verknüpfen
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
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
    }
}

// --------------------------------------------------
// Nur EINE Compose-BOM-Version festlegen, z.B. 2023.05.01
// --------------------------------------------------
//def composeBomVersion = "2023.05.01"

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2023.05.01"))

    // Jeweils Compose-Module, jetzt in passender Version
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    // Falls du Material 2 brauchst (SwipeToDismiss etc.):
    implementation("androidx.compose.material:material")
    // Falls du Material 3 willst (Achtung: Inkompat. APIs):
    implementation("androidx.compose.material3:material3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10")

    // Coil
    implementation("io.coil-kt:coil-compose:2.4.0")

    // AndroidX-Kram:
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // BOM auch für AndroidTests
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.05.01"))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}
