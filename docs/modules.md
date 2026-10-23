---
title: Modules
sidebar_position: 11
---

# Modules

Every module is published as `de.eldoria.util:<module>`. A module brings the modules it builds on, so you only
declare the ones you use directly.

| Module | What it does | Brings |
|---|---|---|
| `core` | Shared helpers: argument parsing, text and math utilities, containers like `Pair`, persistent data helpers, and the `EldoUtilities` setup class. | |
| `messaging` | `MessageSender` and `Replacement` for MiniMessage text. See [Messages](./messages.md). | `core` |
| `localization` | `Localizer` with locale files and per player language. See [Localization](./localization.md). | `core`, `messaging` |
| `debugging` | Debug reports for Lyna. See [Debug reports](./debugging.md). | `core` |
| `commands` | The command framework. See [Commands](./commands.md). | `core`, `messaging`, `debugging`, `localization` |
| `plugin` | `EldoPlugin`, the plugin base class. See [Plugin base class](./plugin.md). | `core`, `commands`, `debugging` |
| `jackson-configuration` | Configuration files with Jackson 3, ocular and Jackson Bukkit. See [Configuration](./configuration.md). | `debugging`, ocular, Jackson Bukkit (`paper`), `jackson-dataformat-yaml` |
| `updater` | Update checks and auto updates through Lyna. See [Update checks](./updates.md). | `core`, `messaging` |
| `threading` | Scheduling helpers and futures. See [Threading and metrics](./threading.md). | `core` |
| `metrics` | A small helper around bStats. See [Threading and metrics](./threading.md#metrics). | |
| `inventory` | Inventory menus whose items run actions on click and on close. | `core`, `items` |
| `items` | `ItemStackBuilder` for item stacks. | |
| `entities` | An entity builder and a helper to find who shot a projectile. | `core` |
| `eldo-util` | Pulls in every module. Not needed by plugins. | everything |

## Deprecated modules

These modules are still published in 3.x, but every type in them is deprecated for removal. They are removed in
eldo-util 4.0.

| Module | Use instead |
|---|---|
| `crossversion` | Nothing. eldo-util targets a single Paper version. |
| `configuration` | `jackson-configuration`. This is the old configuration on top of the Bukkit configuration system. |
| `conversation` | Nothing. |

The Spigot update checker in the `updater` module (`Updater.spigot` and the `spigotupdater` package) is deprecated
as well. Use the [Lyna updater](./updates.md).

Since 3.6.0 the `plugin` module no longer brings `configuration`. If you still use `EldoConfig`, declare
`de.eldoria.util:configuration` yourself until you move to `jackson-configuration`.
