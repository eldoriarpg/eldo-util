plugins {
    id("eldo-util.java-conventions")
}

description = "Legacy ported module for conversations. Deprecated."

dependencies{
    api(project(":core"))
    api(project(":messaging"))
}
