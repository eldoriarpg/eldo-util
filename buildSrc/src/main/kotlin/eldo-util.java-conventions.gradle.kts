plugins {
    `java-library`
    id("com.diffplug.spotless")
}

val libs = versionCatalogs.named("libs")

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-public/")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}

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

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    withType<Javadoc>().configureEach {
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

spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        target("**/*.java")
    }
}
