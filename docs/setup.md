---
title: Setup
sidebar_position: 2
---

# Setup

## Requirements

| What | Version |
|---|---|
| Java | 21 or newer. The library is compiled for Java 21. |
| Server | Paper 1.21.11 or newer. eldo-util is built against the Paper API 1.21.11. Spigot is not supported. |
| Jackson | 3 (`tools.jackson`), for the `jackson-configuration` module. eldo-util 3.6.0 is built against Jackson 3.2.4. |

## Pick the modules

eldo-util is split into modules. Most plugins need three of them:

| Artifact | What you get |
|---|---|
| `de.eldoria.util:plugin` | The [plugin base class](./plugin.md) and, through it, `core`, `commands`, `messaging`, `localization` and `debugging`. |
| `de.eldoria.util:jackson-configuration` | [Configuration files](./configuration.md) with Jackson and ocular. |
| `de.eldoria.util:updater` | [Update checks](./updates.md) through Lyna. |

Add `metrics` for bStats and `threading` for the scheduling helpers ([Threading and metrics](./threading.md)).
[Modules](./modules.md) lists every artifact.

There is also an artifact called `eldo-util`. It only pulls in all modules at once. You do not need it.

## Repository

eldo-util is published to EldoNexus only. Releases are on `maven-releases`, snapshots on `maven-snapshots`, and
`maven-public` contains both. Maven Central stops at 2.2.0, which is a different major version.

```kotlin
repositories {
    maven("https://eldonexus.de/repository/maven-public/")
}
```

## Load it through Paper

Do not shade eldo-util into your plugin jar. Let Paper's library loader download it when the server starts. This
needs a Paper plugin with a `PluginLoader`, because only a loader can add EldoNexus as a repository. The
`paperLibrary` configuration of the `de.eldoria.plugin-yml.paper` Gradle plugin writes your libraries into
`paper-libraries.json`, and the loader reads that file.

```kotlin
plugins {
    id("de.eldoria.plugin-yml.paper") version "0.9.0"
}

dependencies {
    paperLibrary("de.eldoria.util:plugin:3.6.0")
    paperLibrary("de.eldoria.util:jackson-configuration:3.6.0")
    paperLibrary("de.eldoria.util:updater:3.6.0")

    paperLibrary("tools.jackson.core:jackson-core:3.2.4")
    paperLibrary("tools.jackson.core:jackson-databind:3.2.4")
    paperLibrary("tools.jackson.dataformat:jackson-dataformat-yaml:3.2.4")
    paperLibrary("com.fasterxml.jackson.core:jackson-annotations:2.22")
}

paper {
    main = "com.example.myplugin.MyPlugin"
    loader = "com.example.myplugin.MyPluginLoader"
    apiVersion = "1.21.11"
    generateLibrariesJson = true
}
```

:::warning[Pin Jackson yourself]
The library loader picks the version of a transitive dependency that is nearest in the tree, not the newest one.
Without the four Jackson lines above, a plugin can end up with `jackson-databind` 3.0.0 next to
`jackson-dataformat-yaml` 3.2.4, which fails at runtime. Declare all four with the same Jackson version.
:::

Never put a BOM (`platform(...)`) into `paperLibrary`. It ends up in `paper-libraries.json`, and the loader then
tries to download it as a jar.

The loader asks Paper's Maven Central mirror first, then EldoNexus. `maven-releases` has eldo-util, and
`maven-proxies` has everything the Central mirror lacks.

```java
package com.example.myplugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class MyPluginLoader implements PluginLoader {
    private static final String LIBRARIES = "paper-libraries.json";

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(repository("central", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR));
        resolver.addRepository(repository("eldonexus-releases", "https://eldonexus.de/repository/maven-releases/"));
        resolver.addRepository(repository("eldonexus-proxies", "https://eldonexus.de/repository/maven-proxies/"));
        for (var library : libraries().getAsJsonArray("dependencies")) {
            resolver.addDependency(new Dependency(new DefaultArtifact(library.getAsString()), null));
        }
        classpathBuilder.addLibrary(resolver);
    }

    private static RemoteRepository repository(String id, String url) {
        return new RemoteRepository.Builder(id, "default", url).build();
    }

    private JsonObject libraries() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(LIBRARIES)) {
            if (in == null) throw new IllegalStateException(LIBRARIES + " is missing from the plugin jar");
            return new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + LIBRARIES, e);
        }
    }
}
```

The loader only looks at `maven-releases`, so a server always runs a released eldo-util. Snapshots are for your
own builds and tests.

## Compile only

An add-on that runs inside another plugin which already loads eldo-util uses `compileOnly` instead of
`paperLibrary`. It then uses the copy of its parent plugin.

```kotlin
dependencies {
    compileOnly("de.eldoria.util:plugin:3.6.0")
}
```

## Next steps

Extend the [plugin base class](./plugin.md), then add [commands](./commands.md) and [messages](./messages.md).
