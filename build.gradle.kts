plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    java
    `maven-publish`
    `java-library`
    id("de.chojo.publishdata") version "1.0.9"
    id("org.cadixdev.licenser") version "0.6.1"
}
group = "de.eldoria"
var mainPackage = "eldoutilities"
val shadebase = group as String? + "." + mainPackage + "."
version = "1.14.0"
description = "Utility Library for spigot plugins used by the eldoria team."

javaToolchains {
    java {
        sourceCompatibility = JavaVersion.VERSION_17
    }
    testing{

    }
}

repositories {
    mavenCentral()
    maven("https://eldonexus.de/repository/maven-proxies")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")

}

dependencies {
    implementation("org.bstats", "bstats-bukkit", "3.0.0")
    compileOnly("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains", "annotations", "23.0.0")

    testImplementation("org.jetbrains", "annotations", "23.0.0")
    testImplementation("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.9.1")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.9.1")
    testImplementation("org.mockito", "mockito-core", "4.8.0")
    testImplementation("com.github.seeseemelk", "MockBukkit-v1.19", "2.126.1")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishData {
    useEldoNexusRepos()
    publishTask("shadowJar")
    publishTask("sourcesJar")
    publishTask("javadocJar")
}

license {
    header(rootProject.file("HEADER.txt"))
    include("**/*.java")
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
    shadowJar {
        relocate("org.bstats", shadebase + "bstats")
        mergeServiceFiles()
        archiveClassifier.set("")
    }
}
