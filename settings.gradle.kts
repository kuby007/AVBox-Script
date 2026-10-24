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

// 顺序说明：
// 1) google()/mavenCentral() 放最前 —— 腾讯镜像有些库只有 pom 没有 aar（如 XXPermissions），会取不到产物
// 2) 腾讯镜像兜底常用库（含 me.jessyan:autosize 这类 jcenter 老库）
// 3) aliyun public 放最后 —— 只有它还有 DanmakuFlameMaster（ctiao 0.9.25）
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_SETTINGS
    repositories {
        google()
        mavenCentral()
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
        maven("https://jitpack.io")
        maven("https://maven.aliyun.com/repository/public")
    }
}

rootProject.name = "AVBox"
include(":app")
include(":quickjs")
include(":pyramid")
include(":libs:backdrop")
