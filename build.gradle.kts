plugins {
    id("eldo-util.java-conventions")
}

description = "Utility library for Paper plugins."

dependencies {
    api(project(":commands"))
    api(project(":configuration"))
    api(project(":core"))
    api(project(":crossversion"))
    api(project(":debugging"))
    api(project(":entities"))
    api(project(":inventory"))
    api(project(":items"))
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
        "https://javadoc.io/doc/tools.jackson.core/jackson-core/latest/",
        "https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-annotations/latest",
        "https://javadoc.io/doc/tools.jackson.core/jackson-databind/latest",
        "https://jd.papermc.io/paper/1.21.11/"
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
