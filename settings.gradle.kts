import org.gradle.api.initialization.resolve.RepositoriesMode


pluginManagement {
    repositories {
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
        maven("https://mirrors.cloud.tencent.com/nexus/repository/gradle-plugins")
        maven("https://jitpack.io")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.chaquo.python") {
                useModule("com.chaquo.python:gradle:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_SETTINGS
    repositories {
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
        maven("https://mirrors.cloud.tencent.com/nexus/repository/gradle-plugins")
        // Cling (DLNA) 不在 Maven Central 镜像上，仅发布于 4thline 私有仓库
        maven("https://4thline.org/m2")
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "AVBox"
include(":app")
include(":quickjs")
include(":pyramid")
include(":libs:backdrop")
