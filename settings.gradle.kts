rootProject.name = "eldo-util"

pluginManagement{
    repositories{
        mavenLocal()
        gradlePluginPortal()
        maven{
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
include("serialization")
include("commands")
include("localization")
