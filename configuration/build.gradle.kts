plugins {
    id("eldo-util.java-conventions")
}

description = "Legacy configuration files using spigots configuration system"

dependencies{
    compileOnly(libs.spigot)
}
