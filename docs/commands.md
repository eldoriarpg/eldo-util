---
title: Commands
sidebar_position: 4
---

# Commands

The `commands` artifact is a small command framework. A command is a class. It has a meta with its name, aliases,
permissions and arguments. A command either runs something itself or routes to its subcommands. Errors are thrown as
exceptions and sent to the user as localized messages.

The `plugin` artifact already brings `commands` with it.

## A first command

```java
package com.example.commands;

import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.command.util.Arguments;
import de.eldoria.eldoutilities.commands.executor.IPlayerTabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Toggle extends AdvancedCommand implements IPlayerTabExecutor {
    public Toggle(Plugin plugin) {
        super(plugin, CommandMeta.builder("toggle").withPermission("myplugin.toggle").build());
    }

    @Override
    public void onCommand(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) {
        messageSender().sendMessage(player, "toggle.enabled");
    }
}
```

A top level command with subcommands:

```java
package com.example.commands;

import com.example.MyPlugin;
import de.eldoria.eldoutilities.commands.command.AdvancedCommand;
import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.defaultcommands.DefaultAbout;
import de.eldoria.eldoutilities.commands.defaultcommands.DefaultDebug;

public class MyCommand extends AdvancedCommand {
    public MyCommand(MyPlugin plugin) {
        super(plugin, CommandMeta.builder("myplugin")
                .addAlias("mp")
                .withSubCommand(new Toggle(plugin))
                .withSubCommand(new DefaultAbout(plugin))
                .withSubCommand(new DefaultDebug(plugin, "myplugin.debug"))
                .build());
    }
}
```

Register the top level command in `onPluginEnable` of your [plugin class](./plugin.md#registering-commands):

```java
@Override
public void onPluginEnable() throws Throwable {
    registerCommand(new MyCommand(this));
}
```

Players now have `/myplugin toggle`, `/myplugin about` and `/myplugin debug`, and the same under `/mp`.

## Executors

A command that runs something implements one of three interfaces:

| Interface | Who can run it | `onCommand` gets |
|---|---|---|
| `ITabExecutor` | everyone | `CommandSender` |
| `IPlayerTabExecutor` | players only | `Player` |
| `IConsoleTabExecutor` | the console only | `ConsoleCommandSender` |

Each has `onCommand(sender, alias, args)` and a default `onTabComplete(sender, alias, args)` that returns an empty
list. Override `onTabComplete` to suggest values. Both methods may throw a [`CommandException`](#errors).

A player who runs a console command gets `error.onlyConsole`. The console running a player command gets
`error.onlyPlayer`.

A command without an executor interface is a route. It needs subcommands, a default command, or both.

## The command meta

`CommandMeta.builder(name)` creates a `CommandMetaBuilder`. Pass the built meta to the `AdvancedCommand` constructor.

| Method | What it does |
|---|---|
| `addAlias(String...)` | other names for the command. For a top level command of a Paper plugin, the aliases are registered with Paper. |
| `withPermission(String...)` | permissions for the command. The sender needs at least one of them. Without permissions, everyone may use it. |
| `withPermission(Set<String>)` | the same with a set. |
| `addArgument(String name, boolean required)` | adds an argument. `name` is a locale key and is translated when shown. |
| `addUnlocalizedArgument(String name, boolean required)` | adds an argument with a plain text name. |
| `withArguments(Argument...)` | adds prepared `Argument` objects (`Argument.input`, `Argument.unlocalizedInput`, `Argument.subCommand`). |
| `withSubCommand(AdvancedCommand)` | adds a subcommand under its name and all its aliases. |
| `buildSubCommands(BiConsumer<List<AdvancedCommand>, CommandMetaBuilder>)` | adds subcommands from a list you fill in a callback. The callback also gets the builder, for example to set a default command. |
| `withDefaultCommand(AdvancedCommand)` | the command that runs when no subcommand is given. |
| `hidden()` | the command still works, but it is not suggested in tab completion. |
| `ofParent(AdvancedCommand)` | sets the parent command. Subcommands added with `withSubCommand` get their parent set for you. |
| `allowPlayer()`, `allowConsole()`, `allowCommandSender(...)` | limit which sender types may run an `ITabExecutor` command. A sender matches when it is an instance of one of the allowed types, otherwise it gets `error.invalidSender`. |
| `build()` | creates the `CommandMeta`. |

Arguments are shown in the usage text: required ones as `<name>`, optional ones as `[name]`. Before your
`onCommand` runs, eldo-util checks that at least the required number of arguments is there. If not, the user gets
`error.invalidArguments` with the full syntax, like `myplugin give <player> [amount]`.

Inside a command, `meta()` returns the meta. `meta().createCommandCall()` gives the full path, like
`myplugin toggle`.

If you can only build the meta later, use the constructor `AdvancedCommand(Plugin plugin)` and call `meta(meta)` once.

## How a command is routed

When a user runs a command, eldo-util walks through it level by level:

1. It checks the permissions of the current command. Without one of them, the user gets `error.permission`.
2. If the command has an executor, it checks the sender type and the required arguments, then calls `onCommand`.
3. If no arguments are left, the default command runs. Without a default command, the user gets
   `error.endOfRoute` with a list of the subcommands.
4. Otherwise the first argument picks the subcommand by name or alias, ignoring case. An unknown subcommand also
   gives `error.endOfRoute`. The subcommand gets the remaining arguments.

So a subcommand needs its own permissions and the permissions of every command above it.

Tab completion follows the same route. Without permission, the suggestion is the permission error. On a route, the
names and aliases of all subcommands that are not hidden are suggested. An unknown subcommand suggests the text of
`error.invalidCommand`.

## Arguments

`onCommand` gets an `Arguments` object. Indices start at `0` after the command's own name.

| Method | Returns |
|---|---|
| `size()`, `isEmpty()`, `sizeIs(int)`, `hasArg(int)` | how many arguments there are. `hasArg(i)` is true when index `i` exists. |
| `assertLength(int)` | throws `error.missingArgument` when the index does not exist. |
| `get(int)` | the argument as an [`Input`](#input). A negative index counts from the end. A missing index throws `error.missingArgument`. |
| `get(int, Input)` | the argument, or the default. |
| `asString(int)` | the argument as text. |
| `asInt`, `asLong`, `asDouble` | a number, or `error.invalidNumber`. |
| `asBoolean(int)` | `true` or `false`, or `error.invalidBoolean`. |
| `asBoolean(int, String aTrue, String aFalse)` | a boolean with your own words for true and false. |
| `asMaterial(int)` | a `Material`, or `error.invalidMaterial`. |
| `asEnum(int, Class<T>)` | an enum value, or `error.invalidEnumValue` with all valid values. |
| `asPlayer(int)` | an online player, or `error.notOnline`. |
| `asOfflinePlayer(int)` | a player who has played before, found by name. |
| `asWorld(int)` | a world, found by name. |
| `args()`, `args(from)`, `args(from, to)`, `asArray()` | several arguments as `Input`. |
| `join()`, `join(from)`, `join(from, to)`, `join(delimiter, ...)` | several arguments joined into one string. Negative indices count from the end. |
| `last()` | the last argument, or `null` when there is none. |
| `subArguments()`, `subArguments(n)` | new arguments without the first one or the first `n`. |
| `raw()` | the arguments exactly as typed, including flags. |
| `flags()` | the [flags](#flags). |
| `sender()` | the sender. |

Every `asX` method also comes with a default: `asInt(1, 10)` returns `10` when index `1` is missing. A
`Supplier` works as well: `asPlayer(0, () -> player)`. A default is only used when the argument is missing, not when
it is invalid.

`asMaterial` and `asEnum` take an optional `stripStrings` flag. Arguments are iterable and have `stream()`.

`parseQuoted()` groups words in quotes into one argument, so `"hello world"` becomes a single argument.

### Input

`Input` is a single argument. It has the same parsing methods without the index: `asString()`, `asInt()`,
`asLong()`, `asDouble()`, `asBoolean()`, `asBoolean(aTrue, aFalse)`, `asMaterial()`, `asEnum(Class)`,
`asPlayer()`, `asOfflinePlayer()` and `asWorld()`.

### Flags

The arguments end at the first flag. Flags look like this:

| Typed | Flags |
|---|---|
| `-s` | `s` |
| `-abc` | `a`, `b` and `c` |
| `--silent` | `silent` |
| `-r 10` | `r` with the value `10` |
| `--reason too loud` | `reason` with the value `too loud` |

Words after a flag are its value, up to the next flag. With several single letter flags, the last one gets the
value.

```java
var flags = args.flags();
if (flags.has("s")) {
    // silent
}
int radius = flags.hasValue("r") ? flags.get("r").asInt() : 5;
```

| Method | Returns |
|---|---|
| `has(flag)` | whether the flag is there. |
| `hasValue(flag)` | whether the flag has a value. |
| `get(flag)` | the value as `Input`, or `null`. |
| `get(flag, mapper)` | the value mapped by your function. |
| `getIfPresent(flag)`, `getIfPresent(flag, mapper)` | the value as an `Optional`. |
| `lastFlag()`, `lastFlagArgs()`, `hasLastFlagArg()` | the last flag and its value. |
| `isEmpty()` | whether there are no flags. |

Use `raw()` if you need the words that look like flags as plain text.

## Errors

To stop a command with a message, throw a `CommandException`:

```java
throw CommandException.message("error.notOwner", Replacement.create("owner", owner.getName()));
```

- `CommandException.message(String)` and `message(String, TagResolver...)` send the message as an error. The
  message is a locale key or MiniMessage text. A locale key is translated into the language of the sender.
- `CommandException.silent()` stops the command and sends nothing.

The error is sent through the [message sender](./messages.md) of your plugin with `sendError`. In tab completion,
the error text is shown as the only suggestion. eldo-util only catches `CommandException`. Other exceptions reach the
server, which reports them as usual.

For player input in replacements, use `Replacement.unparsed`. See [Messages](./messages.md).

### Assertions

`CommandAssertions` has static checks that throw a `CommandException`:

| Method | Fails with |
|---|---|
| `isTrue(boolean, message, replacements...)` | your message when the value is `false`. |
| `isFalse(boolean, message, replacements...)` | your message when the value is `true`. |
| `evalAssertSilent(boolean)` | a silent exception when the value is `true`. |
| `player(sender)` | `error.onlyPlayer` |
| `console(sender)` | `error.onlyConsole` |
| `permission(sender, silent, permissions...)` | `error.permission`, or a silent exception. Passes with one of the permissions. |
| `permission(sender, meta, silent)` | the same with the permissions of a meta. |
| `invalidArguments(meta, args)` | `error.invalidArguments` when required arguments are missing. |
| `invalidArguments(meta, args, Argument...)`, `invalidArguments(args, Argument...)` | the same with your own argument list. |
| `range(value, min, max)` | `error.invalidRange` |
| `min(value, min)` | `error.tooSmall` |
| `max(value, max)` | `error.tooLarge` |
| `enumValue(value, Class)` | `error.invalidEnumValue` |
| `isInteger`, `isLong`, `isDouble` | `error.invalidNumber` |
| `invalidNumber()` | `error.invalidNumber`, always. |
| `isBoolean(value)`, `isBoolean(value, aTrue, aFalse)` | `error.invalidBoolean` |
| `invalidLength(input, max)` | `error.invalidLength` when the input has `max` characters or more. |
| `missingArgument(args, index)` | `error.missingArgument` |

## Tab completion helpers

`Completion` has static helpers for `onTabComplete`. They filter by what the user typed so far.

| Method | Suggests |
|---|---|
| `complete(value, String...)`, `complete(value, Collection<String>)`, `complete(value, Stream<String>)` | matching strings. |
| `complete(value, Collection<T>, Function<T, String>)`, `complete(value, Stream<T>, Function<T, String>)` | matching objects, mapped to text. |
| `complete(value, Class<T>)` | enum values in lower case. `complete(value, clazz, lowerCase, strip)` lets you choose. |
| `completeMaterial(value, lowerCase)` | materials. Also matches parts of a name, so `oak` finds `OAK_LOG` and `DARK_OAK_LOG`. |
| `completeBoolean(value)` | `true` and `false`. |
| `completeWorlds(value)` | world names. Spaces become `:`. |
| `completePlayers(value)` | online players and players seen in the last 30 days. |
| `completeOnlinePlayers(value)` | online players. |
| `completeInt(value, min, max)`, `completeDouble(value, min, max)` | the range, like `1 » 10`. Throws an error when the input is no number or out of range. |
| `completeMinInt`, `completeMinDouble`, `completeMaxInt`, `completeMaxDouble` | the limit. Throws an error when the input is no number or past the limit. |
| `completeFreeInput(value, maxLength, hint)` | your hint, or an error when the input is too long. |

```java
@Override
public List<String> onTabComplete(@NotNull Player player, @NotNull String alias, @NotNull Arguments args) {
    if (args.size() == 1) return Completion.completePlayers(args.asString(0));
    if (args.size() == 2) return Completion.completeInt(args.asString(1), 1, 64);
    return Collections.emptyList();
}
```

The number helpers throw a `CommandException`. Its text is shown as the suggestion, so the user sees the problem
while typing.

## Default commands

eldo-util ships three ready commands:

- `DefaultAbout(plugin)` is an `about` subcommand. It sends `commands.about` with the plugin name, authors, version,
  website and the Eldoria Discord. `DefaultAbout(plugin, discord, localeKey, replacements...)` lets you change the
  Discord link, the locale key and add your own replacements. The replacements are `plugin_name`, `authors`,
  `version`, `website` and `discord`.
- `DefaultDebug(plugin, permission)` is a `debug` subcommand. It creates a [debug report](./debugging.md). It needs
  your permission or `eldoutilities.debug`. `DefaultDebug(plugin, permission, settings)` takes `DebugSettings`.
- `FailsaveCommand` is the command of the [failsave mode](./plugin.md#failsave-mode). You do not create it yourself.

## Locale keys

The framework uses these keys. When you use a [Localizer](./localization.md), it has an English text for most of
them. Add a key to your own locale files to change the text.

| Key | Replacements |
|---|---|
| `error.permission` | `permission` |
| `error.invalidArguments` | `syntax` |
| `error.endOfRoute` | `commands` |
| `error.invalidCommand` | |
| `error.invalidSender` | |
| `error.onlyPlayer` | |
| `error.onlyConsole` | |
| `error.missingArgument` | `index` |
| `error.invalidNumber` | |
| `error.invalidBoolean` | `true`, `false` |
| `error.invalidMaterial` | |
| `error.invalidEnumValue` | `values` |
| `error.invalidRange` | `min`, `max` |
| `error.tooSmall` | `min` |
| `error.tooLarge` | `max` |
| `error.invalidLength` | `max` |
| `error.notOnline` | |
| `error.unknownPlayer` | |
| `error.unknownWorld` | |
| `commands.about` | `plugin_name`, `authors`, `version`, `website`, `discord` |

Before 3.6.0 four of these keys were misspelled: `error.unkownPlayer`, `error.unkownWorld`, `error.invalidBoolen` and
`error.tooLow`. A locale file that still has an old key keeps working. When the file is updated, the new key gets the
text of the old one.
