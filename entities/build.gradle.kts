plugins {
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Utilities for entity handling"

dependencies{
    api(project(":core"))
}
