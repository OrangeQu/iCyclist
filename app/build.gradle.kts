import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
}

// 🔐 从本地配置文件读取 API 密钥 (不会提交到 Git)
val apiKeysPropertiesFile = rootProject.file("apikeys.properties")
val apiKeysProperties = Properties()
if (apiKeysPropertiesFile.exists()) {
    apiKeysProperties.load(FileInputStream(apiKeysPropertiesFile))
}

android {
    namespace = "com.example.icyclist"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.icyclist"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // ✅ 从配置文件读取 API 密钥 (如果文件不存在则使用空字符串)
        manifestPlaceholders["AMAP_API_KEY"] = apiKeysProperties.getProperty("AMAP_API_KEY", "")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xstring-concat=inline")
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    // 内存和包大小优化配置
    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/*.kotlin_module"
            )
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
}

task("printSHA1") {
    doLast {
        val signingConfig = android.signingConfigs.getByName("debug")
        val storeFile = signingConfig.storeFile
        val storePassword = signingConfig.storePassword
        val keyAlias = signingConfig.keyAlias
        val keyPassword = signingConfig.keyPassword

        exec {
            if (storeFile != null) {
                commandLine(
                    "keytool",
                    "-list",
                    "-v",
                    "-keystore", storeFile.absolutePath,
                    "-storepass", storePassword,
                    "-alias", keyAlias,
                    "-keypass", keyPassword
                )
            }
        }
    }
}

// ✅ dependencies 代码块必须独立放置，与 android { } 块同级
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ✅ 高德地图 SDK 线上依赖（推荐方式）
//    implementation("com.amap.api:services-core:3.1.0")
    implementation("com.amap.api:3dmap:latest.integration")
//    implementation("com.amap.api:location:latest.integration")

    // Room 数据库依赖
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Retrofit 网络请求库
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // androidx.security for EncryptedSharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // iCyclist-Android2 (社区功能) 所需的依赖
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

