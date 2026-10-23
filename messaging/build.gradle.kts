plugins {
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Utilities for message dispatching"

dependencies {
    api(project(":core"))
}
