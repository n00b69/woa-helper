plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.woa.helper"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.woa.helper"
        minSdk = 25
        targetSdk = 35
        versionCode = 4
        versionName = "1.8.4_BETA42"

        val locales = listOf(
            "ar", "az", "be", "cs", "de", "en", "es", "fa", "fr", "in", "ja", "ka", "ko", "ms", "nl", "pl", "pt", "ru", "ro", "ro-rMD", "th", "tr", "uk", "vi", "zh", "zh-rCN", "zh-rHK", "zh-rMO", "zh-rSG", "zh-rTW"
        )
        buildConfigField("String[]", "LOCALES", "{\"${locales.toString().trim('[').trim(']').replace(", ", "\",\"").replace("zh-", "zh-Hans-").replace("-r", "-")}\"}")
        resourceConfigurations += locales
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            isDebuggable = false
            isJniDebuggable = false
        }
        debug {
            isDebuggable = true
        }
    }

    buildFeatures {
        aidl = true
        renderScript = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.preference.preference)
    implementation(libs.com.intuit.sdp.sdp.android)
    implementation(libs.com.github.topjohnwu.libsu.core)
    implementation(libs.com.github.topjohnwu.libsu.service)
    implementation(libs.com.github.topjohnwu.libsu.nio)
    implementation(libs.realtimeblurview)
    implementation(libs.androidx.core.ktx)
}
