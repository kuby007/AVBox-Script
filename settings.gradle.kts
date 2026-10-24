import org.gradle.api.initialization.resolve.RepositoriesMode


pluginManagement {
    repositories {
        // AGP 的插件标记只在 google() 上，gradlePluginPortal / mavenCentral 都没有
        google()
        gradlePluginPortal()
        maven("https://mirrors.cloud.tencent.com/nexus/repository/gradle-plugins")
        mavenCentral()
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

// 仓库顺序（踩坑记录，别再改回去）：
//  - 腾讯云镜像虽然快，但有些库【只有 pom 没有 aar/jar】（XXPermissions、AndroidAutoSize 的
//    重定向目标），一旦 Gradle 在某个仓库找到了 pom，就只从该仓库取产物，取不到就直接失败。
//    所以这里不再用腾讯云镜像做依赖仓库。
//  - aliyun public 是唯一还能拿到 com.github.ctiao:DanmakuFlameMaster:0.9.25 的地方（jcenter 残留）。
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_SETTINGS
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.aliyun.com/repository/public")
    }
}

rootProject.name = "AVBox"
include(":app")
include(":quickjs")
include(":pyramid")
include(":libs:backdrop")
