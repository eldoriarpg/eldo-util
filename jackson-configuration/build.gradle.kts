plugins {
    id("eldo-util.java-conventions")
}

description = "Configuration file wrapper for the jackson-bukkit framework, based on ocular"

dependencies {
    api(project(":debugging"))
    api(libs.ocular)
    api(libs.jackson.bukkit.paper)
    api(libs.jackson.dataformat.yaml)
}
