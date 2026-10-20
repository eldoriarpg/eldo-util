import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    `java-library`
    id("com.diffplug.spotless")
    id("de.chojo.publishdata")
    id("com.vanniktech.maven.publish")
}

group = "de.eldoria.util"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

publishData {
    eldoNexus()
    channels {
        clear()
        release()
        dev("https://eldonexus.de/repository/maven-dev/")
        snapshot("https://eldonexus.de/repository/maven-snapshots/")
    }
}

repositories {
    maven("https://eldonexus.de/repository/maven-proxies")
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.jetbrains", "annotations", "26.1.0")

    testImplementation("org.jetbrains", "annotations", "26.1.0")
    testImplementation("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "6.1.3")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "6.1.3")
    testRuntimeOnly("org.junit.platform", "junit-platform-launcher", "6.1.3")
    testImplementation("org.mockito", "mockito-core", "5.24.0")
    testImplementation("com.github.seeseemelk", "MockBukkit-v1.19", "2.145.0")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(groupId = "de.eldoria.util", artifactId = project.name, version = project.version.toString())

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
            sourcesJar = SourcesJar.Sources()
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
        failOnNoDiscoveredTests = false
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
