import com.diffplug.gradle.spotless.SpotlessPlugin
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost
import de.chojo.PublishData
import de.chojo.Repo

plugins {
    java
    `java-library`
    alias(libs.plugins.spotless)
    alias(libs.plugins.publishdata)
    id("com.vanniktech.maven.publish") version "0.30.0"
}

publishData {
    addRepo(Repo.main("", "", false))
    addRepo(Repo.snapshot("SNAPSHOT", "", false))
    publishingVersion = "2.1.10"
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
        plugin<SigningPlugin>()
        plugin(com.vanniktech.maven.publish.MavenPublishPlugin::class)
    }

    repositories {
        mavenCentral()
        maven("https://eldonexus.de/repository/maven-public")
        maven("https://eldonexus.de/repository/maven-proxies")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        compileOnly("org.jetbrains", "annotations", "24.1.0")

        testImplementation("org.jetbrains", "annotations", "24.1.0")
        testImplementation("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
        testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.10.1")
        testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.10.1")
        testImplementation("org.mockito", "mockito-core", "5.7.0")
        testImplementation("com.github.seeseemelk", "MockBukkit-v1.19", "2.145.0")
    }

    mavenPublishing {
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
        signAllPublications()


        coordinates(groupId = "de.chojo.sadu", artifactId = project.name, version = publishData.getVersion())

        pom {
            name.set("eldo-util")
            description.set(project.description)
            inceptionYear.set("2025")
            url.set("https://github.com/eldoriarpg/eldo-util")
            licenses {
                license {
                    name.set("LGPL-3.0")
                    url.set("https://opensource.org/license/lgpl-3-0")
                }
            }

            developers {
                developer {
                    id.set("rainbowdashlabs")
                    name.set("Lilly Fülling")
                    email.set("mail@chojo.dev")
                    url.set("https://github.com/rainbowdashlabs")
                }
            }

            scm {
                url.set("https://github.com/eldoriarpg/eldo-util")
                connection.set("scm:git:git://github.com/eldoriarpg/eldo-util.git")
                developerConnection.set("scm:git:ssh://github.com/eldoriarpg/eldo-util.git")
            }
        }

        configure(
            JavaLibrary(
                javadocJar = JavadocJar.Javadoc(),
                sourcesJar = true
            )
        )
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
    register<Javadoc>("alljavadoc") {
        applyJavaDocOptions(options)

        setDestinationDir(file("${layout.buildDirectory.get()}/docs/javadoc"))
        val projects = project.rootProject.allprojects.filter { p -> !p.name.contains("example") }
        setSource(projects.map { p -> p.sourceSets.main.get().allJava.filter { p -> p.name != "module-info.java" } })
        classpath = files(projects.map { p -> p.sourceSets.main.get().compileClasspath })
    }
}
