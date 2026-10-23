# Eldo Util

The base library of every Eldoria plugin: a plugin base class, commands, messages, localization, Jackson
configuration files, Lyna update checks and debug reports for Paper plugins.

[![Release](https://img.shields.io/nexus/maven-releases/de.eldoria.util/plugin?label=Release&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)][release]
[![Snapshot](https://img.shields.io/nexus/s/de.eldoria.util/plugin?color=orange&label=Snapshot&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)][snapshot]

## Requirements

- Java 21 or newer.
- Paper 1.21.11 or newer.

## Dependency

eldo-util is published to EldoNexus only.

```kotlin
repositories {
    maven("https://eldonexus.de/repository/maven-public/")
}

dependencies {
    implementation("de.eldoria.util:plugin:3.6.0")
    implementation("de.eldoria.util:jackson-configuration:3.6.0")
    implementation("de.eldoria.util:updater:3.6.0")
}
```

```xml
<repository>
    <id>eldonexus</id>
    <url>https://eldonexus.de/repository/maven-public/</url>
</repository>

<dependency>
    <groupId>de.eldoria.util</groupId>
    <artifactId>plugin</artifactId>
    <version>3.6.0</version>
</dependency>
```

Snapshots are on `https://eldonexus.de/repository/maven-snapshots/`.

## Documentation

https://docs.eldoria.de/eldo-util

[release]: https://eldonexus.de/#browse/browse:maven-releases:de%2Feldoria%2Futil
[snapshot]: https://eldonexus.de/#browse/browse:maven-snapshots:de%2Feldoria%2Futil
