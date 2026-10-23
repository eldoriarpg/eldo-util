plugins {
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Utilities for localisation"

dependencies {
    api(project(":core"))
    api(project(":messaging"))
}

