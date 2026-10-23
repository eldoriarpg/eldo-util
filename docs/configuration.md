---
title: Configuration
sidebar_position: 7
---

# Configuration

The `jackson-configuration` module stores your configuration as YAML files in the plugin folder and maps them to
plain Java classes. It is built on [ocular](https://docs.chojo.dev/ocular) and Jackson 3, and it registers the
Paper module of [jackson-bukkit](/jackson-bukkit), so fields like `Location`, `ItemStack` or `Color` just work.

## A config class

A config file is a class with fields and default values. Jackson reads and writes the fields directly, private ones
included. Getters are not used, so you can name them as you like.

```java
package com.example.config;

public class ConfigFile {
    private String language = "en_US";
    private int maxHomes = 3;
    private Database database = new Database();

    public String language() {
        return language;
    }

    public int maxHomes() {
        return maxHomes;
    }

    public Database database() {
        return database;
    }
}
```

```java
package com.example.config;

public class Database {
    private String host = "localhost";
    private int port = 3306;

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }
}
```

Jackson needs a constructor without arguments to create the class when it reads the file.

## The configuration

Extend `JacksonConfig` with your main file type and give it the key of the main file:

```java
package com.example.config;

import de.eldoria.eldoutilities.config.JacksonConfig;
import dev.chojo.ocular.key.Key;
import java.nio.file.Path;
import org.bukkit.plugin.Plugin;

public class MyConfig extends JacksonConfig<ConfigFile> {
    public MyConfig(Plugin plugin) {
        super(plugin, Key.builder(Path.of("config.yml"), ConfigFile::new).build());
    }

    public Database database() {
        return main().database();
    }
}
```

Create it early, for example in `onPluginLoad`:

```java
configuration = new MyConfig(this);
configuration.main();
configuration.save();
```

`main()` returns the main file. A file that does not exist yet is created from the defaults the first time it is
asked for. Calling `save()` right after loading writes keys you added in a new version into existing files, so
server owners see them.

A `Key` names one file. The path is relative to the plugin folder and must end in `.yml` or `.yaml`.

| `Key.builder(Path, Supplier)` method | What it does |
|---|---|
| `name(String)` | A readable name, used in logs and debug reports. Default: the file name. |
| `strict()` | Fails when the file cannot be read, and leaves it untouched. |
| `rootPackage(String)` | The package of your config classes, for [ocular overrides](https://docs.chojo.dev/ocular/overrides). Default: the package of the class. |
| `build()` | Creates the key. |

Two keys with the same path are the same file.

## More files

Other files use the same instance. Keep the key in a constant and ask for the file with `secondary`:

```java
public static final Key<Messages> MESSAGES =
        Key.builder(Path.of("messages.yml"), Messages::new).name("Messages").build();

Messages messages = configuration.secondary(MESSAGES);
```

| Method | What it does |
|---|---|
| `main()` | The main file. |
| `secondary(Key)` | Another file. Created from its defaults if missing. |
| `save()` | Writes every loaded file. |
| `save(Key)` | Writes one file. |
| `reload()` | Reads every loaded file again. |
| `reload(Key)` | Reads one file again. |
| `replace(Key, value)` | Replaces the object of a file. Call `save(key)` to write it. |
| `exists(Key)` | Whether the file exists on disk. |
| `secondaryWrapped(Key)` | A `Wrapper`. Its `config()` returns the file, and closing it saves the file, so it works with try-with-resources. |

`reload()` creates new objects. Do not keep the objects in fields. Ask `main()` or `secondary(key)` each time, as
`MyConfig.database()` above does.

## Broken files

When a file cannot be read, for example after a typo, the error is logged and the file is moved to
`backup_<date>_<name>` next to it. A new file with the defaults takes its place. With `strict()` on the key, loading
throws a `ConfigurationException` instead, and the file stays as it is.

## How the mapper is set up

`JacksonConfig` configures Jackson like this:

- fields of any visibility are read and written, getters are ignored,
- unknown keys in a file are ignored, so removed options do not break old files,
- classes without fields can be written,
- enum values are read without regard to case,
- classes are loaded with your plugin's class loader, which the library loader and polymorphic types need,
- the jackson-bukkit Paper module is registered, with colors written as hex.

Jackson annotations still come from `com.fasterxml.jackson.annotation`, for example `@JsonProperty`. Everything else
of Jackson 3 lives under `tools.jackson`.

To change the mapper, override `configure` and call `super` first:

```java
@Override
public void configure(MapperBuilder<ObjectMapper, ?> builder) {
    super.configure(builder);
    builder.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
}
```

To add Jackson modules, override `additionalModules` and keep the modules of `super`:

```java
@Override
public List<JacksonModule> additionalModules() {
    var modules = new ArrayList<>(super.additionalModules());
    modules.add(new MyModule());
    return modules;
}
```

To set up jackson-bukkit differently, override `getPaperModule()` and return your own `JacksonPaper`.

## Base configuration and log level

`PluginBaseConfiguration` is a ready made file for things every plugin needs. Its key is
`PluginBaseConfiguration.KEY`, the file `base_configuration.yml`:

```yaml
version: 1
lastInstalledVersion: "1.0.0"
logLevel: "INFO"
```

| Field | Use |
|---|---|
| `logLevel` | The log level of the plugin. Read it in `getLogLevel()` of your [plugin class](./plugin.md). |
| `version` | A number for your own config migrations. eldo-util does not read it. |
| `lastInstalledVersion` | The plugin version that last ran. `lastInstalledVersion(plugin)` sets it to the current version. eldo-util does not read it. |

```java
@Override
public Level getLogLevel() {
    if (configuration == null) return Level.INFO;
    return configuration.secondary(PluginBaseConfiguration.KEY).logLevel();
}
```

`EldoPlugin` asks for the log level when it creates its logger, which can happen before your configuration exists.
That is why the method has to handle a missing configuration.

`logLevel` accepts `OFF`, `SEVERE`, `WARNING`, `INFO`, `CONFIG`, `FINE`, `FINER`, `FINEST` and `ALL`, in any case.
`DEBUG` and `TRUE` also mean `CONFIG`. Anything else means `INFO`. It is written back as the level name.
`LevelSerializer` and `LevelDeserializer` do this, and you can use them on `Level` fields of your own classes with
`@JsonSerialize` and `@JsonDeserialize`.

## Debug reports

`JacksonConfig` is a `DebugDataProvider`. Return it from `getDebugProviders()` of your plugin, and
[debug reports](./debugging.md) contain every file it has loaded.

```java
@Override
public @NotNull List<DebugDataProvider> getDebugProviders() {
    return List.of(configuration);
}
```

A file whose content the report filters can not hide, like a file that holds nothing but credentials, can be left
out. `excludeFromDebug` takes the keys of such files. They are then neither in the debug data of the configuration
nor among the [configuration files](./debugging.md#configuration-files) of the report:

```java
configuration.excludeFromDebug(DATABASE_KEY);
```

Every file is included until you exclude it.

## Dependencies

`jackson-configuration` brings ocular 2.3.1, jackson-bukkit 2.0.0 and jackson-dataformat-yaml 3.2.4. When your
plugin loads eldo-util through Paper's library loader, also list the Jackson artifacts with fixed versions, as
shown in [Setup](./setup.md).
