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
        release("https://eldonexus.de/repository/maven-releases/")
        dev("https://eldonexus.de/repository/maven-dev/")
        snapshot("https://eldonexus.de/repository/maven-snapshots/")
    }
}

repositories {
    maven("https://eldonexus.de/repository/maven-proxies")
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://repo.papermc.io/repository/maven-public/")
}

val libs = versionCatalogs.named("libs")

dependencies {
    compileOnly(libs.findLibrary("annotations").get())
    compileOnly(libs.findLibrary("paper").get())

    testImplementation(libs.findLibrary("annotations").get())
    testImplementation(libs.findLibrary("paper").get())
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testImplementation(libs.findLibrary("mockito-core").get())
    testRuntimeOnly(libs.findLibrary("junit-launcher").get())
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
                name.set("LGPL-3.0-or-later")
                url.set("https://www.gnu.org/licenses/lgpl-3.0.html")
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
