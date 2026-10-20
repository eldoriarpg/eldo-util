plugins {
    id("eldo-util.java-conventions")
}

description = "Utilitites for debugging plugins"

configurations.testImplementation {
    exclude(group = "org.spigotmc")
}

dependencies {
    api(project(":core"))
    compileOnly(libs.paper)
    testImplementation(libs.paper)
}
