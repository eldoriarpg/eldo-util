plugins {
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Utilitites for plugin auto updating and update checks"

dependencies {
    api(project(":core"))
    api(project(":messaging"))
}
