plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// À MODIFIER : adresse du site, ex. "https://monsite.com" (ou variable GitHub DSI_SITE_URL)
val dsiSiteUrlRaw: String = System.getenv("DSI_SITE_URL") ?: ""
val dsiSiteUrl: String = dsiSiteUrlRaw.trim().trimEnd('/')
if (dsiSiteUrl.isNotEmpty() && !dsiSiteUrl.startsWith("https://")) {
    throw GradleException(
        "SITE_URL invalide : doit commencer par « https:// » (valeur reçue : « $dsiSiteUrl »)."
    )
}

android {
    namespace = "app.dsiacademy.academy"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.dsiacademy.academy"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Worker URLs (mêmes valeurs que le site — src/app/config/env.js)
        buildConfigField("String", "PAYMENT_WORKER_URL", "\"https://dsi-worker-paiement.azo9-com234-az09.workers.dev\"")
        buildConfigField("String", "CONTENT_WORKER_URL", "\"https://dsi-worker.legiss2517.workers.dev\"")
        buildConfigField("String", "SHOP_WORKER_URL", "\"https://dsi-worker-boutique.azo9-digitalservice.workers.dev\"")
        buildConfigField("String", "MARKETPLACE_WORKER_URL", "\"https://dsi-worker-marketplace.raza-9-ck2261.workers.dev\"")
        buildConfigField("String", "COMMUNITY_WORKER_URL", "\"https://dsi-worker-community.dave2-kiemde2517.workers.dev\"")
        buildConfigField("String", "ADMIN_WORKER_URL", "\"https://dsi-worker-admin.kiemdedavid23.workers.dev\"")

        // Adresse du site (WebView inscription, etc.)
        buildConfigField("String", "SITE_URL", "\"$dsiSiteUrl\"")
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
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Network
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)

    // Local
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto)

    // Media & images
    implementation(libs.coil.compose)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.session)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
