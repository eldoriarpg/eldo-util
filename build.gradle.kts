import com.diffplug.gradle.spotless.SpotlessPlugin
import de.chojo.PublishData

plugins {
    java
    `maven-publish`
    `java-library`
    id("de.chojo.publishdata") version "1.2.4"
    id("com.diffplug.spotless") version "6.18.0"
}
group = "de.eldoria.util"
var mainPackage = "eldoutilities"
val shadebase = group as String? + "." + mainPackage + "."
version = "2.0.0"
description = "Utility Library for spigot plugins used by the eldoria team."

allprojects {
    apply {
        plugin<JavaLibraryPlugin>()
        plugin<SpotlessPlugin>()
        plugin<JavaPlugin>()
        plugin<MavenPublishPlugin>()
        plugin<PublishData>()
    }

    repositories {
        mavenCentral()
        maven("https://eldonexus.de/repository/maven-proxies")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        compileOnly("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
        compileOnly("org.jetbrains", "annotations", "24.0.1")

        testImplementation("org.jetbrains", "annotations", "24.0.1")
        testImplementation("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
        testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.9.2")
        testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.9.2")
        testImplementation("org.mockito", "mockito-core", "5.2.0")
        testImplementation("com.github.seeseemelk", "MockBukkit-v1.19", "2.145.0")
    }

    java {
        withSourcesJar()
        withJavadocJar()
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    publishData{
        useEldoNexusRepos()
        publishComponent("java")
    }

    publishing {
        publications.create<MavenPublication>("maven") {
            publishData.configurePublication(this);
        }

        repositories {
            maven {
                authentication {
                    credentials(PasswordCredentials::class) {
                        username = System.getenv("NEXUS_USERNAME")
                        password = System.getenv("NEXUS_PASSWORD")
                    }
                }

                name = "EldoNexus"
                setUrl(publishData.getRepository())
            }
        }
    }

    spotless {
        java {
            licenseHeaderFile(rootProject.file("HEADER.txt"))
            target("**/*.java")
        }
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }

        javadoc {
            options.encoding = "UTF-8"
        }

        compileTestJava {
            options.encoding = "UTF-8"
        }

        test {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }
}

dependencies{
    api(project(":commands"))
    api(project(":configuration"))
    api(project(":core"))
    api(project(":crossversion"))
    api(project(":debugging"))
    api(project(":entities"))
    api(project(":inventory"))
    api(project(":items"))
    api(project(":legacy-serialization"))
    api(project(":localization"))
    api(project(":messaging"))
    api(project(":metrics"))
    api(project(":plugin"))
    api(project(":threading"))
    api(project(":updater"))
}
