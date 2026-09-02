// 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
// 构建环境适配（compileSdk/JVM 目标升级）。
// 原作者版权与许可见 LICENSE。
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.JavaVersion

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "com.example.c001apk.media"
    compileSdk = 36
    buildToolsVersion = "36.1.0"

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(project(":mojito"))
    implementation(project(":SketchImageViewLoader"))
    testImplementation(libs.junit)
}
