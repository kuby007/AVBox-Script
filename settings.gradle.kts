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
        google()
        mavenCentral()
        maven("https://jitpack.io")
        // 弹幕库 DanmakuFlameMaster 只在 jcenter 系仓库里，腾讯云/中央仓库没有
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/jcenter")
        maven("https://maven.aliyun.com/repository/releases")
    }
}

rootProject.name = "AVBox"
include(":app")
include(":quickjs")
include(":pyramid")
include(":libs:backdrop")
