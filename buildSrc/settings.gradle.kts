dependencyResolutionManagement {
    repositories {
        maven("https://eldonexus.de/repository/maven-proxies/")
        maven("https://eldonexus.de/repository/maven-public/")
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
