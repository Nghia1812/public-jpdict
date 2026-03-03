import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.prj.data"
    compileSdk = 35
    android.buildFeatures.buildConfig = true

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "TRANSLATE_API_KEY", "\"${localProperties.getProperty("TRANSLATE_API_KEY") ?: ""}\"")
        buildConfigField("String", "BASE_URL_TRANSLATE", "\"${localProperties.getProperty("BASE_URL_TRANSLATE") ?: ""}\"")
        buildConfigField("String", "BASE_URL_JLPT_EXAM", "\"${localProperties.getProperty("BASE_URL_JLPT_EXAM") ?: ""}\"")
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
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE.txt"
            pickFirsts += "META-INF/NOTICE.md"
            pickFirsts += "META-INF/LICENSE.md"
            pickFirsts += "META-INF/CONTRIBUTORS.md"
        }
    }
}

dependencies {
    // RoomDB
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(project(":domain"))
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.datastore.core)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    ksp(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Retrofit related packages
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.moshi.kotlin)
    implementation(libs.logging.interceptor)

    // Image processing
    implementation("com.google.mlkit:text-recognition-japanese:16.0.1")

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore")

    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    //Timber
    implementation ("com.jakewharton.timber:timber:4.7.1")

    // Tensor flow
    implementation(libs.tensorflow.tensorflow.lite)

    implementation(files("libs/kuromoji-core-1.0-SNAPSHOT.jar"))
    implementation(files("libs/kuromoji-ipadic-1.0-SNAPSHOT.jar"))

    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}