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
        maven("https://jitpack.io")
        maven("https://chaquo.com/maven")
        maven("https://repo1.maven.org/maven2")
        google()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/google")
        // Cling(UPnP/DLNA) 只发布在作者自建仓库,不在 Maven Central / Google / 阿里云
        maven {
            url = uri("https://4thline.org/m2")
        }
        maven {
            url = uri("http://4thline.org/m2")
            isAllowInsecureProtocol = true
        }
    }
}

rootProject.name = "AVBox"
include(":app")
include(":quickjs")
include(":pyramid")
include(":libs:backdrop")
