---
title: Plugin base class
sidebar_position: 3
---

# Plugin base class

`EldoPlugin` is the base class for your main plugin class. It comes from the `plugin` artifact. It extends
`JavaPlugin` and adds a few things on top:

- lifecycle methods that catch every error,
- a failsave mode with a debug command when your plugin fails to start,
- a logger that can show debug messages,
- short ways to register commands and listeners.

```java
package com.example;

import de.eldoria.eldoutilities.plugin.EldoPlugin;

import java.util.logging.Level;

public class MyPlugin extends EldoPlugin {
    @Override
    public void onPluginEnable() throws Throwable {
        registerListener(new MyListener());
        registerCommand(new MyCommand(this));
    }

    @Override
    public Level getLogLevel() {
        return Level.INFO;
    }
}
```

`getLogLevel()` is the only method you have to implement.

## Lifecycle

`EldoPlugin` implements `onLoad`, `onEnable`, `onDisable` and `onCommand` itself. They are `final`, so you can not
override them. Use these methods instead:

| Method | Called |
|---|---|
| `onPluginLoad()` | from `onLoad`. |
| `onPluginReload()` | from `onEnable`, before the enable methods, but only after a server reload. |
| `onPluginEnable(boolean reload)` | from `onEnable`. `reload` is `true` after a server reload. |
| `onPluginEnable()` | from `onEnable`, right after `onPluginEnable(boolean)`. |
| `onPostStart(boolean reload)` | one tick after the plugin enabled. |
| `onPostStart()` | right after `onPostStart(boolean)`. |
| `onPluginDisable()` | from `onDisable`. |

All of them are empty by default, and all of them may throw any `Throwable`. You only override the ones you need.

The post start methods run on the global region scheduler. That scheduler exists on Paper and on Folia, so the post
start also works on Folia.

On disable, `EldoPlugin` also unregisters the [message sender](./messages.md) of your plugin.

:::tip
Register commands in `onPluginEnable`. Paper plugins can only register commands while they enable, so
`onPostStart` is too late. See [Registering commands](#registering-commands).
:::

### Server reloads

`EldoPlugin` notices when someone runs `/reload`. It then logs a warning on disable and creates a file called `lock`
in your plugin's data folder. When the plugin enables again and finds that file, it logs that reloading is
discouraged, calls `onPluginReload()` and passes `reload = true` to the enable and post start methods. The file is
removed once the plugin enabled.

## Failsave mode

When `onPluginLoad`, `onPluginReload`, one of the `onPluginEnable` methods or one of the `onPostStart` methods throws,
`EldoPlugin` catches the error. It logs the error with level `SEVERE` and starts the failsave mode.

In failsave mode, eldo-util registers a fallback command:

- for every command in your `plugin.yml`,
- or, for a Paper plugin that declares no commands, for your plugin name in lower case.

The fallback command tells the user that the plugin failed to load. `/<command> debug` creates a
[debug report](./debugging.md) that the user can send to you. The fallback command needs the permission
`<plugin>.debug` (your plugin name in lower case, like `myplugin.debug`) or `eldoutilities.debug`.

When `onPluginLoad` fails, `onPluginEnable` is not called. The failsave mode starts when the server enables the plugin,
because a Paper plugin can only register commands then. When `onPluginEnable` fails, the rest of the enable is
skipped. The post start is not scheduled.

## Logging

`getLogger()` returns a `DebugLogger`. It wraps the normal plugin logger.

The server only prints messages with level `INFO` or higher. The debug logger works around that. A message with a
lower level is printed when your log level allows it. It is printed as `INFO` with a prefix that shows the real
level:

| Level | Prefix |
|---|---|
| `CONFIG` | `[DEBUG]` |
| `FINE` | `[FINE]` |
| `FINER` | `[FINER]` |
| `FINEST` | `[FINEST]` |

The log level comes from your `getLogLevel()`. Most Eldoria plugins read it from their config file:

```java
@Override
public Level getLogLevel() {
    if (configuration == null) return Level.INFO;
    return configuration.secondary(PluginBaseConfiguration.KEY).logLevel();
}
```

`getLogLevel()` can be called before your config is loaded, so return a default when it is not there yet.
[Configuration](./configuration.md) explains `PluginBaseConfiguration`. After you changed the level, call
`setLoggerLevel()` to apply it.

`EldoPlugin.logger()` is a static shortcut to the logger, and `EldoPlugin.getInstance()` returns the plugin
instance. Both use the first `EldoPlugin` that was created with this copy of eldo-util.

## Registering commands

There are three methods:

| Method | What it registers |
|---|---|
| `registerCommand(AdvancedCommand command)` | an [eldo-util command](./commands.md) under the name of its meta. |
| `registerCommand(String name, AdvancedCommand command)` | an eldo-util command under another name. |
| `registerCommand(String name, TabExecutor executor)` | a plain Bukkit `TabExecutor`. |

What happens depends on your plugin descriptor:

- When the command is declared in your `plugin.yml`, it gets your executor. If the command is missing there, a
  warning is logged.
- When it is not declared, the command is registered through Paper. This is the normal case for Paper plugins,
  because a `paper-plugin.yml` declares no commands. The aliases come from the command meta
  (`CommandMeta.builder("name").addAlias(...)`). A plain `TabExecutor` gets no aliases.

Paper only accepts new commands while the plugin enables. Call `registerCommand` from `onPluginEnable`.

:::warning
Do not call `getCommand(...)` in a Paper plugin while it starts. Paper throws an `UnsupportedOperationException`.
Use `registerCommand` instead.
:::

## Listeners and scheduling

`registerListener(Listener... listeners)` registers one or more listeners for your plugin.

`getPluginManager()` and `getScheduler()` are shortcuts to the server's plugin manager and scheduler.

`scheduleRepeatingTask(Runnable task, int period)` runs a task on the main thread every `period` ticks. The first
run is after 100 ticks. `scheduleRepeatingTask(task, delay, period)` lets you choose the first delay. Both return
the task id, or `-1` when scheduling failed.

:::warning
`getScheduler()` and `scheduleRepeatingTask` use the Bukkit scheduler. Folia has no main thread and does not support
it. If your plugin should run on Folia, use the region, entity or async schedulers of the server. They exist on
Paper as well. See [Threading](./threading.md).
:::

## Bukkit serialization

`getConfigSerialization()` returns classes that are registered with Bukkit's `ConfigurationSerialization` when the
plugin is created. A class with `@PluginSerializationName` is registered under that name as well. `{plugin}` in the
name is replaced with your plugin name in lower case.

This only matters for old Bukkit configuration files. New plugins use [Jackson configuration](./configuration.md)
and can ignore it.

## Debug information

`EldoPlugin` implements `DebugDataProvider`. Override `getDebugInformations()` to add your own entries to debug
reports. [Debug reports](./debugging.md) shows what you can add.
