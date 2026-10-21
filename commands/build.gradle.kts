plugins {
    id("eldo-util.java-conventions")
}

description = "Command framework for Paper plugins"

dependencies {
    api(project(":core"))
    api(project(":messaging"))
    api(project(":debugging"))
    api(project(":localization"))
}
