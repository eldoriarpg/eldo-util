![GitHub Workflow Status](https://img.shields.io/github/workflow/status/eldoriarpg/eldo-util/Publish%20to%20Nexus?style=flat-square)
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/de.eldoria/eldo-util?label=EldoNexus&nexusVersion=3&server=https%3A%2F%2Feldonexus.de&style=flat-square)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/eldoriarpg/eldo-util/Verify%20state?style=flat-square)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/de.eldoria/eldo-util?color=orange&label=EldoNexus&server=https%3A%2F%2Feldonexus.de&style=flat-square)
# Dependency
Gradle
``` kotlin
repositories {
    maven { url = uri("https://eldonexus.de/repository/maven-releases") }
}

dependencies {
    implementation("de.eldoria", "eldo-util", "version")
}
```

Maven
``` xml
<repository>
    <id>EldoNexus</id>
    <url>https://eldonexus.de</url>
</repository>

<dependency>
    <groupId>de.eldoria</groupId>
    <artifactId>eldo-util</artifactId>
    <version>version</version>
</dependency>
```

