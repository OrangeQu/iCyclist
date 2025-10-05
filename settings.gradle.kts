pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()

        // ✅ 主要使用阿里云镜像
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        maven { url = uri("https://maven.aliyun.com/repository/google/") }

        // ✅ 备用镜像
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
    }
}

rootProject.name = "iCyclist"
include(":app")