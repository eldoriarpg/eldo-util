description = "Configuration file wrapper for the jackson-bukkit framework"

dependencies {
    api(project(":debugging"))
    api("de.eldoria.jacksonbukkit:jackson-bukkit:2.0.0")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    compileOnly(libs.spigot)
}
