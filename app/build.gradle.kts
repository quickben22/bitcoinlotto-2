import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val debugAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
val debugBannerUnitId = "ca-app-pub-3940256099942544/9214589741"
val debugRewardedUnitId = "ca-app-pub-3940256099942544/5224354917"
val releaseAdmobAppId = providers.gradleProperty("admobAppId")
    .getOrElse("ca-app-pub-3535032153893847~2306137168")
val releaseBannerUnitId = providers.gradleProperty("admobBannerUnitId")
    .getOrElse("ca-app-pub-3535032153893847/3541547263")
val releaseRewardedUnitId = providers.gradleProperty("admobRewardedUnitId")
    .getOrElse("ca-app-pub-3535032153893847/3485706949")

android {
    namespace = "com.quickben22.bitcoinlotto"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.quickben22.bitcoinlotto"
        minSdk = 24
        targetSdk = 36
        versionCode = 11
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            manifestPlaceholders["admobAppId"] = debugAdmobAppId
            buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"$debugBannerUnitId\"")
            buildConfigField("String", "ADMOB_REWARDED_UNIT_ID", "\"$debugRewardedUnitId\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["admobAppId"] = releaseAdmobAppId
            buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"$releaseBannerUnitId\"")
            buildConfigField("String", "ADMOB_REWARDED_UNIT_ID", "\"$releaseRewardedUnitId\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    implementation(project(":core:crypto"))
    implementation(project(":core:data"))

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(testFixtures(project(":core:data")))

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

val validateReleaseAdIds = tasks.register("validateReleaseAdIds") {
    inputs.property("admobAppId", releaseAdmobAppId)
    inputs.property("admobBannerUnitId", releaseBannerUnitId)
    inputs.property("admobRewardedUnitId", releaseRewardedUnitId)
    doLast {
        val appId = inputs.properties["admobAppId"] as String
        val bannerId = inputs.properties["admobBannerUnitId"] as String
        val rewardedId = inputs.properties["admobRewardedUnitId"] as String
        val validAppId = Regex("""ca-app-pub-\d+~\d+""").matches(appId)
        val validBannerId = Regex("""ca-app-pub-\d+/\d+""").matches(bannerId)
        val validRewardedId = Regex("""ca-app-pub-\d+/\d+""").matches(rewardedId)
        check(validAppId && validBannerId && validRewardedId) {
            "Release builds require an AdMob app ID (ca-app-pub-...~...) and banner/rewarded unit IDs (ca-app-pub-.../...); supply -PadmobBannerUnitId and -PadmobRewardedUnitId."
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    dependsOn(validateReleaseAdIds)
}
