import org.gradle.api.initialization.resolve.RepositoriesMode


pluginManagement {
    repositories {
        maven("https://chaquo.com/maven")
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
        // 顺序很关键：镜像站常提供 pom 却缺构件（.imy/.aar 等），
        // Gradle 一旦从某仓库拿到元数据就只去该仓库取文件，
        // 因此官方源必须排在镜像之前。
        google()
        maven("https://chaquo.com/maven")
        maven("https://repo1.maven.org/maven2")
        maven("https://jitpack.io")
        // 以下仅为加速镜像，兜底用
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://repo.huaweicloud.com/repository/maven/")
    }
}

rootProject.name = "AVBox"
include(":app")
include(":quickjs")
include(":pyramid")
include(":libs:backdrop")
