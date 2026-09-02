// 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
// 构建环境适配（buildToolsVersion 36.1.0）。
// 原作者版权与许可见 LICENSE。
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.JavaVersion

plugins {
    id("com.android.library")
    id("kotlin-android")
}

group = "com.github.mikaelzero"
android {
    namespace = "net.mikaelzero.mojito.view.sketch"
    compileSdk = 34
    buildToolsVersion = "36.1.0"

    defaultConfig {
        minSdk = 16
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "VERSION_NAME", "\"1.0.0\"")
        buildConfigField("int", "VERSION_CODE", "1")
    }
    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    lint {
        abortOnError = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.squareup.okhttp)
    implementation(libs.sketch.gif)
    implementation(libs.androidx.exifinterface)
    implementation(project(":mojito"))
}

