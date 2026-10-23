---
title: Eldo Util
sidebar_position: 1
---

# Eldo Util

eldo-util is the base library of every Eldoria plugin. It is a library for plugin developers, not a plugin you put on
a server. It gives a Paper plugin:

- a base class with a safe start, a failsafe mode and a debug logger,
- a command framework with sub commands, argument parsing and tab completion,
- MiniMessage messages with a prefix, replacements and translations,
- locale files with a language per player,
- configuration files with Jackson 3, built on ocular and Jackson Bukkit,
- update checks and auto updates through Lyna, Eldoria's release service,
- debug reports that server owners can upload for support.

## A first look

```java
package com.example.myplugin;

import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.plugin.EldoPlugin;
import java.util.logging.Level;

public class MyPlugin extends EldoPlugin {
    @Override
    public void onPluginEnable() {
        var localizer = Localizer.builder(this, "en_US").build();
        MessageSender.builder(this).prefix("<gold>[MyPlugin]").localizer(localizer).register();
        registerCommand(new MyCommand(this));
    }

    @Override
    public Level getLogLevel() {
        return Level.INFO;
    }
}
```

## Where to start

1. [Setup](./setup.md): requirements, the modules you need and how Paper loads them.
2. [Plugin base class](./plugin.md): the lifecycle of an `EldoPlugin`.
3. [Commands](./commands.md), [Messages](./messages.md) and [Localization](./localization.md).
4. [Configuration](./configuration.md) for your config files.
5. [Update checks](./updates.md) and [Debug reports](./debugging.md) for Lyna.

Moving a plugin from eldo-util 2? Read [Migrating from 2.x](./migrating.md).

## Versions

This documentation describes eldo-util 3.6. Version 3 runs on Paper only and uses Jackson 3. It is published to
EldoNexus, not to Maven Central.
