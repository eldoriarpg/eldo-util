plugins {
    alias(libs.plugins.aggregate.javadoc)
    id("eldo-util.java-conventions")
    id("eldo-util.publishing-conventions")
}

description = "Utility library for Eldoria's Paper plugins."

val published = listOf(
    "commands",
    "configuration",
    "conversation",
    "core",
    "crossversion",
    "debugging",
    "entities",
    "inventory",
    "items",
    "jackson-configuration",
    "localization",
    "messaging",
    "metrics",
    "plugin",
    "threading",
    "updater",
)

dependencies {
    published.forEach {
        api(project(":$it"))
        javadoc(project(":$it"))
    }
    javadocClasspath(libs.paper)
}
