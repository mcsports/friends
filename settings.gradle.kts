enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 * For more detailed information on multi-project builds, please refer to https://docs.gradle.org/8.12.1/userguide/multi_project_builds.html in the Gradle documentation.
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "friends"

include("runtime", "plugin", "api", "shared", "api-lite")
findProject("runtime")?.name = "${rootProject.name}-droplet"
findProject("plugin")?.name = "${rootProject.name}-plugin"
findProject("api")?.name = "${rootProject.name}-api"
findProject("api-lite")?.name = "${rootProject.name}-api-lite"
findProject("shared")?.name = "${rootProject.name}-shared"