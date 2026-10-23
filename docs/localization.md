---
title: Localization
sidebar_position: 6
---

# Localization

The `localization` module translates your messages. Texts live in `.properties` files, one per language. The
`Localizer` copies them into the plugin folder, keeps them up to date when you add keys, and picks the right
language for each player. The [message sender](./messages.md) uses it on its own.

## Locale files in your jar

Put the files at the root of your resources:

```text
src/main/resources/
├── messages.properties
├── messages_en_US.properties
└── messages_de_DE.properties
```

- `messages.properties` is the reference. Its keys are the keys every language file must have, and its values are
  the texts used when a language has no bundled file of its own.
- `messages_<locale>.properties` holds the texts of one language. The locale is a language code with an optional
  country code: `en_US`, `de_DE`, `fr`.

A key needs at least one dot, like `toggle.enabled` or `error.maxStack`. Only text in this shape is treated as a
locale key. Values are MiniMessage text and may contain placeholders:

```properties
error.maxStack=You can not carry a stack higher than <max>.
toggle.enabled=Other players can pick you up now.
```

## Create the localizer

```java
import de.eldoria.eldoutilities.localization.Localizer;

@Override
public void onPluginEnable() {
    Localizer localizer = Localizer.builder(this, "en_US")
            .setIncludedLocales("de_DE")
            .setUserLocale(player -> player.locale().toString())
            .build();
    localizer.setDefaultLocale("de_DE");
}
```

The second argument of `builder` is the fallback locale. It is always included, so it needs a bundled file.

| Builder method | What it does | Default |
|---|---|---|
| `setIncludedLocales(String...)` | The locales you ship in the jar. They are copied into the plugin folder. | only the fallback locale |
| `setUserLocale(Function<Player, String>)` | Which language a player gets. | the fallback locale |
| `setLocalesPath(String)` | The folder inside the plugin folder that holds the files. | `messages` |
| `setLocalesPrefix(String)` | The file name prefix, both in the jar and on disk. | `messages` |
| `addLocaleCodes(String key, String value)` | A key with its default text, added to every file. | none |
| `addLocaleCodes(Map<String, String>)` | Several of those at once. | none |

`build()` creates the localizer and registers it for your plugin class. From then on the message sender and the
[commands](./commands.md) find it on their own, and `ILocalizer.getPluginLocalizer(plugin)` returns it.

## Files on disk

When the localizer is built, it does this:

1. It creates `plugins/MyPlugin/messages/` and copies each included locale file there, unless the file already
   exists.
2. It checks every locale file in that folder. Keys from `messages.properties` and from `addLocaleCodes` that a
   file lacks are appended at its end, under a comment with the date. The text comes from the bundled file of the
   same language, or from `messages.properties` when that language has no bundled file. Keys from `addLocaleCodes`
   that the bundled file lacks get their default text. Existing lines,
   comments and their order stay as they are.
3. It loads the fallback locale, the included locales and every other locale file in the folder.

So server owners can edit the texts, and they can add a language you do not ship: they drop
`messages_fr_FR.properties` into the folder, and it is loaded and completed on the next start. Files that do not
match `<prefix>_<locale>.properties` are skipped with a note in the log.

The files are read when the localizer is built. To read edited files, build a new localizer.

### Built-in keys

The localizer also adds the texts eldo-util itself sends, so they can be translated too. They are appended to your
files like your own keys:

- `error.*`: errors of the command framework, like `error.permission`, `error.invalidNumber` or `error.onlyPlayer`,
- `commands.about`: the text of the about command,
- `dialog.*`: button texts like `dialog.accept`.

To change one of them, put it into your bundled locale files or pass it to `addLocaleCodes` on the builder. Server
owners can edit it in the files on disk.

## Which language is used

A message sent to a player is looked up in the language that `setUserLocale` returns for that player. Every other
receiver, like the console, gets the default language. The default language starts as the fallback locale.
`setDefaultLocale(String)` changes it to another loaded locale. An unknown locale is ignored with a warning.

The requested language does not have to match a file exactly. The localizer takes the closest loaded one:

1. the same locale, ignoring case and `-` or `_`: `en-us` finds `en_US`,
2. else a locale of the same language: `de_AT` finds `de_DE`,
3. else the default language.

A key missing in that language is taken from the default language, then from child localizers, then from the
built-in keys. If no text exists at all, the key itself is shown and a warning is logged once per key.

:::warning[Players and the default language]
Without `setUserLocale`, every player gets the fallback locale given to `builder`, even after `setDefaultLocale`.
To use one language from your config for everyone, return it for players as well:
`.setUserLocale(player -> config.main().language())`.
:::

## Localize text yourself

The localizer is an `ILocalizer`. Besides sending messages through the [message sender](./messages.md), you can
read texts directly:

| Method | Returns |
|---|---|
| `localize(CommandSender, String)` | The text of a locale key for the sender, or the input unchanged if it is not a key. |
| `getMessage(String key, CommandSender)` | The text for the sender, or the key if it is missing. |
| `getMessage(String key, String language)` | The text in a given language. |
| `getValue(String key, CommandSender)` | The text, or `null` if it is missing. |
| `includedLocales()` | The locales given to the builder. |
| `addLocaleCodes(Map<String, String>)` | Adds keys with default texts after the localizer was built. They are written to the files and available right away. |

`registerChild(ILocalizer)` adds another localizer as a child. Keys this localizer cannot find are then looked up in
the child. An add-on registers its own localizer as a child of its main plugin's localizer, so the main plugin can
show the add-on's texts.
