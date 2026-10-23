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
        languageVersion = JavaLanguageVersion.of(25)
    }
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
        options.release = 21
        options.encoding = "UTF-8"
    }

    withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).links(
            "https://docs.oracle.com/en/java/javase/21/docs/api/",
            "https://javadoc.io/doc/org.jetbrains/annotations/latest/",
            "https://javadoc.io/doc/tools.jackson.core/jackson-core/latest/",
            "https://javadoc.io/doc/tools.jackson.core/jackson-databind/latest/",
            "https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-annotations/latest/",
            "https://jd.papermc.io/paper/1.21.11/",
        )
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
        target("src/**/*.java")
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        palantirJavaFormat(libs.findVersion("palantir").get().requiredVersion).formatJavadoc(false)
        removeUnusedImports()
        importOrder("", "java", "javax", "\\#")
        trimTrailingWhitespace()
        endWithNewline()
        encoding("UTF-8")
    }
}
