plugins {
    alias(libs.plugins.agp.app)
}

android {
    namespace = "id.kuato.woahelper"
    compileSdk = 34

    defaultConfig {
        applicationId = "id.kuato.woahelper"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.8.4_BETA41"

        val locales =listOf(
            "ar", "az", "be", "cs", "de", "en", "es", "fa", "fr", "in", "ja", "ka", "ko", "ms", "nl", "pl", "pt", "ru", "ro","ro-rMD", "th", "tr", "uk", "vi", "zh", "zh-rCN", "zh-rHK", "zh-rMO", "zh-rSG", "zh-rTW"
        )
        buildConfigField("String[]","LOCALES","{\"${locales.toString().trim('[').trim(']').replace(", ","\",\"").replace("zh-","zh-Hans-").replace("-r","-")}\"}");
        resourceConfigurations +=locales;
    }

    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            isDebuggable = false
            isJniDebuggable = false
        }
        getByName("debug") {
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

    buildFeatures {
        viewBinding = true
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
}
