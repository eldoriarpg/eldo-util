plugins {
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Functions to execute code based on the server version"

dependencies {
    api(project(":core"))
}
