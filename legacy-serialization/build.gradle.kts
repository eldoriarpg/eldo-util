description = "Legacy serialization for type resolving in spigot configuration files"

dependencies {
    api(project(":core"))
    api(project(":entities"))
    compileOnly(libs.spigot)
}
