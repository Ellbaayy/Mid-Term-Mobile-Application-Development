plugins {
    alias(libs.plugins.android.application)
    // The Google Services plugin stays disabled until google-services.json is placed
    // in the app/ directory. Uncomment the line below after adding that file.
    // alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.midtermapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.midtermapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)

    // Cloud Firestore: the student records live in the "Students" collection.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
