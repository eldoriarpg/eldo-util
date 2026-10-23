plugins {
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Deprecated: Bukkit conversations. Removed in 4.0."

dependencies{
    api(project(":core"))
    api(project(":messaging"))
}
