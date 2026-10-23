---
title: Migrating from 2.x
sidebar_position: 12
---

# Migrating from 2.x

eldo-util 3 is a Paper only library on Jackson 3. Moving a plugin from 2.x means moving it to Paper, to Jackson 3
and to the new configuration base at the same time. This page lists what changes in your code.

## Coordinates and loading

| 2.x | 3.x |
|---|---|
| Maven Central | EldoNexus only, see [Setup](./setup.md#repository) |
| Shaded and relocated into the plugin jar | Loaded by Paper's library loader through a `PluginLoader`, see [Setup](./setup.md#load-it-through-paper) |
| Spigot and Paper | Paper 1.21.11 or newer only |

Drop the relocation of `de.eldoria.eldoutilities`, Jackson and Adventure from your Shadow configuration. Only
bStats still has to be shaded and relocated.

## Jackson 3

Jackson 3 moved to new packages. The annotations stay where they were.

| 2.x | 3.x |
|---|---|
| `com.fasterxml.jackson.core` and `com.fasterxml.jackson.databind` | `tools.jackson.core` and `tools.jackson.databind` |
| `com.fasterxml.jackson.annotation` | unchanged |
| `JsonSerializer` and `JsonDeserializer` | `ValueSerializer` and `ValueDeserializer` |
| `SerializerProvider` | `SerializationContext` |
| `JsonProcessingException` | `tools.jackson.core.JacksonException` |
| `YAMLGenerator.Feature` | `YAMLWriteFeature` |
| `TypeFactory.defaultInstance()` | `TypeFactory.createDefaultInstance()` |
| `setVisibility(...)` | `changeDefaultVisibility(...)` |

Jackson Bukkit moves to 2.0.0 (`de.eldoria.jacksonbukkit:paper`), which `jackson-configuration` brings for you.

## Configuration

The Jackson configuration is now built on [ocular](https://docs.chojo.dev/ocular). See
[Configuration](./configuration.md) for the full picture.

| 2.x | 3.x |
|---|---|
| `ConfigKey.of(name, path, Class, Supplier)` | `Key.builder(path, Supplier).name(name).build()` from `dev.chojo.ocular.key` |
| `Wrapper`, `ConfigSubscriber`, `ConfigurationException` | the types in `dev.chojo.ocular.components`, `dev.chojo.ocular.hooks` and `dev.chojo.ocular.exceptions` |
| overriding `createMapper()`, `createReadMapper()` or `createWriteMapper()` | override `configure(MapperBuilder<ObjectMapper, ?>)` and call `super.configure(builder)` |
| `additionalModules()` | public, returns `List<JacksonModule>`; keep the modules of `super.additionalModules()` |
| `getBukkitModule()` and `getPlatformModule()` | `getPaperModule()` |

The old migration from Bukkit serialized files (`==:` type markers) is gone. Tell your users to update through the
last 2.x release of your plugin first. It is a good idea to add a test that loads a config file written by your
previous major version through the new mapper.

## Messages

| 2.x | 3.x |
|---|---|
| `SpigotMessageSender` and adventure-platform-bukkit | the Paper message sender, see [Messages](./messages.md) |
| plain text in `Replacement.create` | `Replacement.create` parses MiniMessage. Use `Replacement.unparsed` for text a player typed. |
| `&` colour codes anywhere | `&` codes are only converted when no letter or digit comes right before them. `§` codes are always converted. |

`<i18n:key>` works inside any message sent through `MessageSender`, and replacements apply inside the localized text.
For a language per player, see [Localization](./localization.md).

## Updates and debug reports

| 2.x | 3.x |
|---|---|
| `Updater.butler(...)` | removed. Use `Updater.lyna(...)`, see [Update checks](./updates.md). |
| `Updater.spigot(...)` | deprecated, removed in 4.0 |
| `DebugPayload` and `ConfigDump` | Lyna debug reports, see [Debug reports](./debugging.md) |
| | `DebugDataProvider.getHealthChecks()` and `getCharts()` are new |

The update command is now `/<plugin>:eldo-update`. Your plugin does not declare it.

## Commands on a Paper plugin

A `paper-plugin.yml` has no `commands` section. `EldoPlugin.registerCommand` registers such commands through Paper.
Call it from `onPluginEnable`, because Paper only accepts commands while the plugin enables. Aliases come from
`CommandMetaBuilder.addAlias`. See [Plugin base class](./plugin.md).

## Removed modules

`legacy-serialization` is gone. `crossversion`, `configuration` and `conversation` still exist in 3.x but are
deprecated for removal, see [Modules](./modules.md#deprecated-modules).
