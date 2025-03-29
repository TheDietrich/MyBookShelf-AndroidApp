import org.jetbrains.kotlin.gradle.dsl.JvmTarget


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
        versionCode = 3
        versionName = "1.0.3"
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

    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
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
    implementation(platform(libs.androidx.compose.bom.v20230501))

    // Jeweils Compose-Module, jetzt in passender Version
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    // Falls du Material 2 brauchst (SwipeToDismiss etc.):
    implementation(libs.androidx.material)
    // Falls du Material 3 willst (Achtung: Inkompat. APIs):
    implementation(libs.material3)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Gson
    implementation(libs.gson)

    // Coil
    implementation(libs.coil.compose)

    // AndroidX-Kram:
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // BOM auch für AndroidTests
    androidTestImplementation(platform(libs.androidx.compose.bom.v20230501))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}


