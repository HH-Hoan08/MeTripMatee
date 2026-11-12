plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mytripmatee"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.mytripmatee"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main") {
            res.srcDirs(
                "src/main/res",
                "src/main/res/features/auth",
                "src/main/res/features/main",
                "src/main/res/features/explore",
                "src/main/res/features/trip",
                "src/main/res/features/itinerary",
                "src/main/res/features/expense",
                "src/main/res/features/gallery",
                "src/main/res/features/stats",
                "src/main/res/features/map",
                "src/main/res/features/settings"
            )
        }
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
