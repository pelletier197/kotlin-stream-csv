rootProject.name = "kotlin-stream-csv"
include("core")
include("core-java-tests")
include("examples")

pluginManagement {
    val kotlinVersion: String by settings
    val ktlintVersion: String by settings
    val coverallsVersion: String by settings
    val nexusPublishPluginVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion

        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
        id("org.jlleitschuh.gradle.ktlint-idea") version ktlintVersion

        id("com.github.kt3k.coveralls") version coverallsVersion

        id("io.github.gradle-nexus.publish-plugin") version nexusPublishPluginVersion
    }
}