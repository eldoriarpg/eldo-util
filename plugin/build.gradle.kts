plugins {
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Utilities for plugin creation and base class"

dependencies {
    api(project(":commands"))
    api(project(":core"))
    api(project(":debugging"))
}
