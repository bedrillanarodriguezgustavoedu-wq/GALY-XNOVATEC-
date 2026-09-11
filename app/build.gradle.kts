plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // alias(libs.plugins.googleServices)
}

android {
    namespace = "com.galyxnovatec.tienda"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.galyxnovatec.tienda"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    androidResources {
        // Permitimos la compresión para que el sistema las extraiga al instalar
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
            // ELIMINACIÓN RADICAL DE LIBRERÍAS QUE NO SOPORTAN 16KB
            excludes += "**/libimagepipeline.so"
            excludes += "**/libnative-filters.so"
            excludes += "**/libnative-imagetranscoder.so"
            excludes += "**/libstatic-webp.so"
            excludes += "**/libandroidx.graphics.path.so"
            excludes += "**/libimagepipeline-base.so"
            excludes += "**/libnative-imagetranscoder-base.so"
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Solo excluimos las que sabemos que fallan en 16KB
            excludes += "**/libimagepipeline.so"
            excludes += "**/libnative-filters.so"
            excludes += "**/libnative-imagetranscoder.so"
            excludes += "**/libstatic-webp.so"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.recyclerview)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.coil.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.lottie)
    
    // Pasarela de Pagos (Mercado Pago)
    implementation(libs.mercadopago)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}