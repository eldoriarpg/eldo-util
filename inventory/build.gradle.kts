plugins {
    id("eldo-util.java-conventions")
}

description = "Utilities for inventory GUIs"

dependencies{
    api(project(":core"))
    api(project(":items"))
}
