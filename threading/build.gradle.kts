plugins {
    id("eldo-util.java-conventions")
}

description = "Utilitites for threading and scheduling"

dependencies{
    api(project(":core"))
    compileOnly(libs.spigot)
}
