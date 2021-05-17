import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "6.0.0"
    java
    `maven-publish`
    `java-library`
}

group = "de.eldoria"
var mainPackage = "eldoutilities"
val shadebade = group as String? + "." + mainPackage + "."
version = "1.8.5"
description = "Utility Library for spigot plugins used by the eldoria team."
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    }
}

dependencies {
    implementation("org.bstats", "bstats-bukkit", "2.2.1")
    compileOnly("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:19.0.0")

    testImplementation("org.jetbrains:annotations:19.0.0")
    testImplementation("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    testImplementation("org.mockito:mockito-core:3.5.13")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifact(tasks["shadowJar"])
        artifact(tasks["sourcesJar"])
        artifact(tasks["javadocJar"])
        groupId = project.group as String?
        artifactId = project.name
        version = project.version as String?
    }

    repositories {
        maven {
            val isSnapshot = version.toString().endsWith("SNAPSHOT");
            val release = "https://eldonexus.de/repository/maven-releases/";
            val snapshot = "https://eldonexus.de/repository/maven-snapshots/";

            authentication {
                credentials(PasswordCredentials::class) {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }

            name = "EldoNexus"
            url = uri(if (isSnapshot) snapshot else release)
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}



tasks.named<ShadowJar>("shadowJar") {
    relocate("org.bstats", shadebade + "bstats")
    mergeServiceFiles()
    archiveClassifier.set("")
    //archiveFileName.set("${archiveBaseName}-${archiveVersion}.${archiveExtension}")
}