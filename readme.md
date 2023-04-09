![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/eldoriarpg/eldo-util/publish_to_nexus.yml?style=for-the-badge&label=Publishing&branch=master)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/eldoriarpg/eldo-util/verify.yml?style=for-the-badge&label=Building&branch=master)
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/maven-releases/de.eldoria.util/eldo-util?label=Release&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)
![Sonatype Nexus (Development)](https://img.shields.io/nexus/maven-dev/de.eldoria.util/eldo-util?label=DEV&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/de.eldoria.util/eldo-util?color=orange&label=Snapshot&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)

# Dependency

Gradle

``` kotlin
repositories {
    maven("https://eldonexus.de/repository/maven-public")
}

dependencies {
    implementation("de.eldoria.util", "eldo-util", "version")
}
```

Maven

``` xml
<repository>
    <id>EldoNexus</id>
    <url>https://eldonexus.de/repository/maven-public/</url>
</repository>

<dependency>
    <groupId>de.eldoria.util</groupId>
    <artifactId>eldo-util</artifactId>
    <version>version</version>
</dependency>
```
