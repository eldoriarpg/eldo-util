description = "Utilitites for plugin auto updating and update checks"

dependencies {
    compileOnly(libs.paper)
    api(project(":core")) {
        exclude("org.spigotmc")
    }
    api(project(":messaging")) {
        exclude("org.spigotmc")
    }
}
