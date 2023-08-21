import com.diffplug.gradle.spotless.SpotlessPlugin
import de.chojo.PublishData
import net.kyori.indra.IndraExtension
import net.kyori.indra.IndraPlugin
import net.kyori.indra.IndraPublishingPlugin

plugins {
    java
    `maven-publish`
    `java-library`
    alias(libs.plugins.spotless)
    alias(libs.plugins.publishdata)
    alias(libs.plugins.indra.core)
    alias(libs.plugins.indra.publishing)
    alias(libs.plugins.indra.sonatype)
}
publishData {
    useEldoNexusRepos()
    publishingVersion = "2.0.1"
}
version = publishData.getVersion()

group = "de.eldoria.util"
var mainPackage = "eldoutilities"
val shadebase = group as String? + "." + mainPackage + "."
description = "Utility Library for spigot plugins."

allprojects {
    apply {
        // We want to apply several plugins to subprojects
        plugin<JavaPlugin>()
        plugin<SpotlessPlugin>()
        plugin<PublishData>()
        plugin<JavaLibraryPlugin>()
        plugin<MavenPublishPlugin>()
        plugin<IndraPlugin>()
        plugin<IndraPublishingPlugin>()
        plugin<SigningPlugin>()
    }

    repositories {
        mavenCentral()
        maven("https://eldonexus.de/repository/maven-public")
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

    indra {
        configureIndra(this)
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

fun configureIndra(extension: IndraExtension) {
    extension.javaVersions {
        target(17)
        testWith(17)
    }

    extension.github("eldoriarpg", "jackson-bukkit") {
        ci(true)
    }

    extension.lgpl3OrLaterLicense()

    extension.signWithKeyFromPrefixedProperties("rainbowdashlabs")

    extension.configurePublications {
        pom {
            developers {
                developer {
                    id.set("rainbowdashlabs")
                    name.set("Florian Fülling")
                    email.set("mail@chojo.dev")
                    url.set("https://github.com/rainbowdashlabs")
                }
            }
        }
    }
}

indraSonatype {
    useAlternateSonatypeOSSHost("s01")
}


dependencies {
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
