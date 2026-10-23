import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    id("com.vanniktech.maven.publish")
    id("de.chojo.publishdata")
}

publishData {
    eldoNexus(dev = false)
}

mavenPublishing {
    configure(JavaLibrary(javadocJar = JavadocJar.Javadoc(), sourcesJar = SourcesJar.Sources()))
    signAllPublications()
    coordinates(rootProject.group.toString(), project.name, project.version.toString())
    pom {
        name = if (project == rootProject) "eldo-util" else "eldo-util ${project.name}"
        description = provider { project.description }
        url = "https://github.com/eldoriarpg/eldo-util"
        inceptionYear = "2021"
        licenses {
            license {
                name = "LGPL-3.0-or-later"
                url = "https://www.gnu.org/licenses/lgpl-3.0.html"
            }
        }
        developers {
            developer {
                id = "rainbowdashlabs"
                name = "Nora Fülling"
                email = "mail@chojo.dev"
                url = "https://github.com/rainbowdashlabs"
                organization = "Eldoria"
                organizationUrl = "https://github.com/eldoriarpg"
            }
        }
        scm {
            url = "https://github.com/eldoriarpg/eldo-util"
            connection = "scm:git:git://github.com/eldoriarpg/eldo-util.git"
            developerConnection = "scm:git:ssh://github.com/eldoriarpg/eldo-util.git"
        }
    }
}
