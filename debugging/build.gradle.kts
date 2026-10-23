plugins {
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Utilitites for debugging plugins"

dependencies {
    api(project(":core"))
}
