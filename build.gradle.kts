import com.diffplug.gradle.spotless.SpotlessPlugin
import de.chojo.PublishData
import de.chojo.Repo
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
    addRepo(Repo.main("","", false))
    addRepo(Repo.snapshot("SNAPSHOT","", false))
    publishingVersion = "2.0.3"
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
        compileOnly("org.jetbrains", "annotations", "24.0.1")

        testImplementation("org.jetbrains", "annotations", "24.0.1")
        testImplementation("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
        testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.10.1")
        testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.10.1")
        testImplementation("org.mockito", "mockito-core", "5.7.0")
        testImplementation("com.github.seeseemelk", "MockBukkit-v1.19", "2.145.0")
    }

    indra {
        javaVersions {
            target(17)
            testWith(17)
        }

        github("eldoriarpg", "eldo-util") {
            ci(true)
        }

        lgpl3OrLaterLicense()

        signWithKeyFromPrefixedProperties("rainbowdashlabs")

        configurePublications {
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

fun applyJavaDocOptions(options: MinimalJavadocOptions) {
    val javaDocOptions = options as StandardJavadocDocletOptions
    javaDocOptions.links(
            "https://javadoc.io/doc/com.google.code.findbugs/jsr305/latest/",
            "https://javadoc.io/doc/org.jetbrains/annotations/latest/",
            "https://docs.oracle.com/en/java/javase/${java.toolchain.languageVersion.get().asInt()}/docs/api/",
            "https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-core/latest/",
            "https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-annotations/latest",
            "https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-databind/latest",
            "https://jd.papermc.io/paper/1.19/"
    )
}

tasks {
    register<Javadoc>("allJavadocs") {
        dependsOn(javadocJar)
        applyJavaDocOptions(options)

        destinationDir = file("${layout.buildDirectory}/docs/javadoc")
        val projects = project.rootProject.allprojects
        setSource(projects.map { p -> p.sourceSets.main.get().allJava })
        classpath = files(projects.map { p -> p.sourceSets.main.get().compileClasspath })
    }
}
