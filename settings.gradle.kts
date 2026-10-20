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
            plugin("spotless", "com.diffplug.spotless").version("6.23.3")
            plugin("publishdata", "de.chojo.publishdata").version("1.2.5")

            library("paper", "io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
            library("spigot", "org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")

            version("indra", "3.1.3")
            plugin("indra-core", "net.kyori.indra").versionRef("indra")
            plugin("indra-publishing", "net.kyori.indra.publishing").versionRef("indra")
            plugin("indra-sonatype", "net.kyori.indra.publishing.sonatype").versionRef("indra")

            library("jackson-dataformat-yaml", "tools.jackson.dataformat", "jackson-dataformat-yaml").version("3.2.4")
            library("jackson-bukkit", "de.eldoria.jacksonbukkit", "jackson-bukkit").version("2.0.0")
            library("ocular", "dev.chojo", "ocular").version("2.3.0")
        }
    }
}
include("conversation")
