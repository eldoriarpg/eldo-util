description = "Utilities for message dispatching"

dependencies {
    api("net.kyori", "adventure-platform-bukkit", "4.3.0")
    api("net.kyori", "adventure-text-minimessage", "4.13.0")
    api("net.kyori", "adventure-text-serializer-plain", "4.13.0")
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    api(project(":core")){
        exclude("org.spigot")
    }
}
