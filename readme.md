![GitHub Workflow Status](https://img.shields.io/github/workflow/status/eldoriarpg/eldo-util/Publish%20to%20Nexus?style=for-the-badge&label=Publishing)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/eldoriarpg/eldo-util/Verify%20state?style=for-the-badge&label=Building)
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/maven-releases/de.eldoria/eldo-util?label=Release&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)
![Sonatype Nexus (Development)](https://img.shields.io/nexus/maven-dev/de.eldoria/eldo-util?label=DEV&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/de.eldoria/eldo-util?color=orange&label=Snapshot&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)
# Dependency
Gradle
``` kotlin
repositories {
    maven("https://eldonexus.de/repository/maven-public")
}

dependencies {
    implementation("de.eldoria", "eldo-util", "version")
}
```

Maven
``` xml
<repository>
    <id>EldoNexus</id>
    <url>https://eldonexus.de/repository/maven-public/</url>
</repository>

<dependency>
    <groupId>de.eldoria</groupId>
    <artifactId>eldo-util</artifactId>
    <version>version</version>
</dependency>
```
