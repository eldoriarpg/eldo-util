plugins {
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Deprecated: functions that depend on the server version. Removed in 4.0."

dependencies {
    api(project(":core"))
}
