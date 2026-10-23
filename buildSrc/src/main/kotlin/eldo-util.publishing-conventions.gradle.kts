import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    id("com.vanniktech.maven.publish")
    id("de.chojo.publishdata")
}

group = "de.eldoria.util"

publishData {
    eldoNexus()
    channels {
        clear()
        release("https://eldonexus.de/repository/maven-releases/")
        dev("https://eldonexus.de/repository/maven-dev/")
        snapshot("https://eldonexus.de/repository/maven-snapshots/")
    }
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
