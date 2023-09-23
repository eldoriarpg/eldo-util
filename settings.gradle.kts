rootProject.name = "eldo-util"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven {
            name = "EldoNexus"
            url = uri("https://eldonexus.de/repository/maven-public/")

        }
    }
}
include("core")
include("messaging")
include("plugin")
include("debugging")
include("updater")
include("legacy-serialization")
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

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // plugins
            plugin("spotless", "com.diffplug.spotless").version("6.21.0")
            plugin("publishdata", "de.chojo.publishdata").version("1.2.5")

            library("paper", "io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
            library("spigot", "org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")

            version("indra", "3.1.3")
            plugin("indra-core", "net.kyori.indra").versionRef("indra")
            plugin("indra-publishing", "net.kyori.indra.publishing").versionRef("indra")
            plugin("indra-sonatype", "net.kyori.indra.publishing.sonatype").versionRef("indra")

            version("jackson", "2.15.2")
            library("jackson-core", "com.fasterxml.jackson.core", "jackson-core").versionRef("jackson")
            library("jackson-databind", "com.fasterxml.jackson.core", "jackson-databind").versionRef("jackson")
            bundle("jackson", listOf("jackson-core", "jackson-databind"))

        }
    }
}
