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
        maven("https://4thline.org/m2")
        maven("http://4thline.org/m2").apply { isAllowInsecureProtocol = true }
        maven("https://chaquo.com/maven")
        maven("https://repo1.maven.org/maven2")
        google()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/jcenter")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://repo.huaweicloud.com/repository/maven/")
        maven("https://mirrors.huaweicloud.com/repository/maven/")
    }
}

rootProject.name = "AVBox"
include(":app")
include(":quickjs")
include(":pyramid")
include(":libs:backdrop")
