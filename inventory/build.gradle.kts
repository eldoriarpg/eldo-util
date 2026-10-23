plugins {
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Utilities for inventory GUIs"

dependencies{
    api(project(":core"))
    api(project(":items"))
}
