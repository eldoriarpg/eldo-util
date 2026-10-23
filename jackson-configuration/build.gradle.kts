plugins {
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Configuration file wrapper for the jackson-bukkit framework, based on ocular"

dependencies {
    api(project(":debugging"))
    api(libs.ocular)
    api(libs.jacksonbukkit.paper)
    api(libs.jackson.yaml)
}
