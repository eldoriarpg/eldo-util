---
title: Update checks
sidebar_position: 8
---

# Update checks

The `updater` module looks for new versions of your plugin on [Lyna](https://lyna.eldoria.de), Eldoria's release
service. It tells admins about a new version, can download it for them, and can install stable updates on its own.

Every plugin that uses it needs a product on Lyna. The product id is part of your build.

## Name the Lyna product in the build

The updater reads the product id from the `build.data` file in your plugin jar. The
[publishdata](https://github.com/rainbowdashlabs/publishdata) Gradle plugin writes that file. Add the id to it in
the module that builds the plugin jar:

```kotlin
publishData {
    buildData { put("lynaProduct", "42") }
}
```

`build.data` is a plain `key=value` file at the root of the jar. Besides `lynaProduct` the updater reads
`artifactVersion` (the version it compares, falling back to the version of your plugin descriptor), `artifact` and
`unix` (the build time). publishdata writes all of them.

## Start the updater

Build the update data with `LynaUpdateData.builder(plugin)` and create the updater with `Updater.lyna(data)`:

```java
LynaUpdateData data = LynaUpdateData.builder(this)
        .notifyPermission("myplugin.updates")
        .build();

Updater<LynaUpdateResponse, LynaUpdateData> updater = Updater.lyna(data);
updater.start();
```

`builder(plugin)` throws an `IllegalStateException` when `build.data` has no `lynaProduct` or the value is not a
number. `builder(plugin, productId)` takes the id directly instead.

`start()` checks right away and then every 6 hours. Checks and downloads run on a thread of the updater, not on the
server thread.

Create the updater after you registered your [message sender](./messages.md) and your
[localizer](./localization.md). The updater sends its messages through them, and it adds its texts to your locale
files when it is created.

### Change the settings or stop

- `reconfigure(data)` hands a running updater new settings, for example after a config reload. It removes the
  current notice and checks again with the new settings.
- `stop()` ends the updater for good. No further checks run. Call it in `onPluginDisable`, and when the config
  switches update checks off. Create a new updater to start again.

## Builder options

| Method | Default | What it does |
|---|---|---|
| `notifyPermission(String)` | `eldoutilities.admin` | Players with this permission (and operators) get the notice on join. It also allows the update command. |
| `notifyUpdate(boolean)` | `true` | Send the notice to players on join. The console always gets it. |
| `autoUpdate(boolean)` | `false` | Download new stable versions on their own. See [automatic updates](#automatic-updates). |
| `askForAutoUpdate(boolean)` | `true` | Offer admins to switch automatic updates on from chat. |
| `autoUpdateHook(Consumer<AutoUpdateDecision>)` | none | Receives what an admin chose in chat. Without a hook, nothing is offered in chat. |
| `messageSender(MessageSender)` | the sender registered for the plugin | The sender the updater uses for its messages. |
| `updateMessage(String)` | `eldoutilities.updater.lyna-notice` | The notice text, a MiniMessage text or a locale code. |
| `updateUrl(String)` | the product page, `https://lyna.eldoria.de/products/<id>` | The address the notice links to. |
| `host(String)` | `https://lyna.eldoria.de` | Another Lyna instance. |
| `tokenStore(UpgradeTokenStore)` | `upgrade-token.txt` in the plugin folder | Where the upgrade token of a premium product is kept. |
| `downloadType(int)` | none | The download type a premium product requests, when it has more than one stable download. |

The notice can use these placeholders as tags: `<plugin_name>`, `<new_version>`, `<current_version>`, `<website>`,
`<new_time>` and `<current_time>` (time since the version was published), and `<new_date_time>` and
`<current_date_time>`.

## What admins see

When a check finds a new version:

- The console logs the notice. A new major version also gets a warning that it may not be compatible.
- Players with the notify permission and operators get the notice when they join, unless `notifyUpdate(false)` was
  set.
- The notice ends with buttons when they apply: **Download** and **Enable auto updates**.

All texts of the updater are locale codes that start with `eldoutilities.updater.`. With a localizer they are added
to your locale files, so server owners can translate them. Without a localizer the English defaults are sent.

## The update command

The buttons run a command that the updater registers on its own. You do not declare it anywhere:

```text
/<plugin>:eldo-update <download|enable-auto|later|never>
```

`<plugin>` is your plugin name in lower case. The console, operators and players with the notify permission may use
it.

| Action | What it does |
|---|---|
| `download` | Downloads the new version into the server's update folder. Without an action, the command downloads. |
| `download confirm` | Downloads a new major version. A plain `download` asks for this confirmation first. |
| `enable-auto` | Switches automatic updates on. |
| `later` | Asks again after the next manual update. |
| `never` | Does not ask again. |

After a manual download the admin is asked whether future updates should be installed automatically, if
automatic updates are offered.

## Automatic updates

With automatic updates on, the updater downloads a new version on its own when:

- the new version is a stable release (its version contains neither `snapshot` nor `dev`), and
- it has the same major version as the running one (the number before the first dot).

A new major version is never installed on its own. It is marked in the notice, and its download has to be
confirmed. The download button offers stable releases, and dev or snapshot builds only to a server that already
runs a dev or snapshot build.

Downloads go into the server's update folder under the name of the running plugin file. The server applies them on
the next start.

### Let admins decide in chat

With an `autoUpdateHook`, the notice offers to switch automatic updates on. The hook gets the decision as an
`AutoUpdateDecision`, on the server's global thread, so you can store it in your config:

| Decision | Store it as |
|---|---|
| `ENABLED` | auto update on |
| `NEVER_ASK` | pass `askForAutoUpdate(false)` from now on |

## Premium products

Lyna hands out a public download only for free products. A premium product trades an upgrade token for each
download. The server owner creates the token in their account on Lyna (`/account/updaters`) and puts it into
`upgrade-token.txt` in your plugin folder. Every download uses the token up, and the updater stores the next one it
gets back in the same file.

Without a token, an automatic update stops with a warning in the console that says where to put one.

## A wrapper class

Keep the updater behind a small class of your plugin. It reads the settings from your config, starts or
reconfigures the updater, and stores the decisions from chat. This is the shape Eldoria's plugins use:

```java
package com.example.myplugin.updates;

import de.eldoria.eldoutilities.updater.AutoUpdateDecision;
import de.eldoria.eldoutilities.updater.Updater;
import de.eldoria.eldoutilities.updater.lynaupdater.LynaUpdateData;
import de.eldoria.eldoutilities.updater.lynaupdater.LynaUpdateResponse;
import org.bukkit.plugin.Plugin;

public final class Updates {
    private final Plugin plugin;
    private final MyConfig configuration;
    private Updater<LynaUpdateResponse, LynaUpdateData> updater;

    public Updates(Plugin plugin, MyConfig configuration) {
        this.plugin = plugin;
        this.configuration = configuration;
    }

    public void apply() {
        UpdateSettings settings = configuration.updates();
        if (!settings.notice() && !settings.autoUpdate()) {
            stop();
            return;
        }
        LynaUpdateData data = LynaUpdateData.builder(plugin)
                .notifyPermission("myplugin.updates")
                .notifyUpdate(settings.notice())
                .autoUpdate(settings.autoUpdate())
                .askForAutoUpdate(settings.askForAutoUpdate())
                .autoUpdateHook(this::decided)
                .build();
        if (updater == null) {
            updater = Updater.lyna(data);
            updater.start();
        } else {
            updater.reconfigure(data);
        }
    }

    public void stop() {
        if (updater != null) updater.stop();
        updater = null;
    }

    private void decided(AutoUpdateDecision decision) {
        UpdateSettings settings = configuration.updates();
        switch (decision) {
            case ENABLED -> settings.autoUpdate(true);
            case NEVER_ASK -> settings.askForAutoUpdate(false);
        }
        configuration.save();
    }
}
```

`MyConfig` and `UpdateSettings` are your own config classes, for example an `updates` section with `notice`,
`autoUpdate` and `askForAutoUpdate`. Call `apply()` in `onPluginEnable` and after a reload, and `stop()` in
`onPluginDisable`. Declare the notify permission in your plugin descriptor with `default: op`.

## Spigot update checks

`Updater.spigot(...)` and the classes in `updater.spigotupdater` are deprecated and will be removed in 4.0. Use the
Lyna updater.
