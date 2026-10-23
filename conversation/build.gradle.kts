plugins {
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Legacy ported module for conversations. Deprecated."

dependencies{
    api(project(":core"))
    api(project(":messaging"))
}
