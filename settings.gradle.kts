pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://eldonexus.de/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "eldo-util"

include("core")
include("messaging")
include("plugin")
include("debugging")
include("updater")
include("commands")
include("localization")
include("threading")
include("entities")
include("inventory")
include("crossversion")
include("configuration")
include("metrics")
include("items")
include("jackson-configuration")
include("conversation")
