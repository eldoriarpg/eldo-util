---
title: Messages
sidebar_position: 5
---

# Messages

The `messaging` module sends text to players and the console. Messages are written in
[MiniMessage](https://docs.advntr.dev/minimessage/format.html), can be locale keys that the
[localizer](./localization.md) translates, and get a prefix and a color for normal messages and for errors.

## Create the message sender

Every plugin registers one `MessageSender`. Do it once while your plugin enables:

```java
import de.eldoria.eldoutilities.messages.MessageSender;
import net.kyori.adventure.text.format.NamedTextColor;

@Override
public void onPluginEnable() {
    MessageSender.builder(this)
            .prefix("<gold>[MyPlugin]")
            .messageColor(NamedTextColor.AQUA)
            .errorColor(NamedTextColor.RED)
            .register();
}
```

`register()` builds the sender and stores it for your plugin class. Anywhere else, fetch it with
`MessageSender.getPluginMessageSender(plugin)`. That call throws an `IllegalStateException` when no sender was
registered for the plugin. The [command framework](./commands.md) fetches the sender the same way, so register it
before the first command runs.

`EldoPlugin` removes the sender again when the plugin disables. Without `EldoPlugin`, call
`MessageSender.unregister(plugin)` in `onDisable`.

### Builder options

| Method | What it does |
|---|---|
| `prefix(String)` | The prefix as MiniMessage text. |
| `prefix(Component)` | The prefix as a component. |
| `messageColor(TextColor)` | Color of normal messages. Default: green. |
| `errorColor(TextColor)` | Color of error messages. Default: red. |
| `addTag(Consumer<TagResolver.Builder>)` | Your own tags, usable in every message. |
| `addMessageTag(Consumer<TagResolver.Builder>)` | Tags only for normal messages. |
| `addErrorTag(Consumer<TagResolver.Builder>)` | Tags only for error messages. |
| `strict(boolean)` | MiniMessage strict mode: unclosed tags are an error. |
| `debug(Consumer<String>)` | Receives MiniMessage's parser debug output. |
| `preProcessor(UnaryOperator<String>)` | Changes the raw text before it is parsed. |
| `postProcessor(UnaryOperator<Component>)` | Changes the component after it is parsed. |
| `localizer(ILocalizer)` | The localizer for locale codes and `<i18n>` tags. Without it, the localizer registered for your plugin is used. |

The color of a message is applied through the `<default>` tag. Every message starts with it, and you can use it to
switch back to the message color after another color: `<gold><amount><default> items`. The prefix is also
available as the `<prefix>` tag.

## Send messages

```java
MessageSender sender = MessageSender.getPluginMessageSender(plugin);

sender.sendMessage(player, "Your home was saved.");
sender.sendError(player, "You have no home yet.");
sender.sendMessage(player, "home.saved");
```

The last line sends a locale key. When your plugin has a [localizer](./localization.md) and the whole message is a
key, the text of that key is sent, in the language of the receiver.

What the sender offers:

| Method | Sends |
|---|---|
| `sendMessage(CommandSender, String, TagResolver...)` | A chat message with prefix in the message color. |
| `sendError(CommandSender, String, TagResolver...)` | A chat message with prefix in the error color. |
| `sendMessage(CommandSender, Component)` | A finished component with prefix. |
| `sendTitle(Player, String title, String subtitle, Title.Times, TagResolver...)` | A title and subtitle. |
| `sendActionBar(Player, String, TagResolver...)` | An action bar text in the message color. |
| `sendErrorActionBar(Player, String, TagResolver...)` | An action bar text in the error color. |
| `sendBossBar(Player, String, float, BossBar.Color, BossBar.Overlay, Set<BossBar.Flag>)` | A boss bar. It returns the `BossBar`, so you can hide it with `hideBossBar`. |
| `broadcast(String)` | A message to everyone on the server. |

Only chat messages sent with `sendMessage` and `sendError` get the prefix. Titles, action bars, boss bars and
broadcasts do not.

To get text without sending it:

| Method | Returns |
|---|---|
| `serializeMessage(CommandSender, String, TagResolver...)` | A `Component` in the message color, without prefix. |
| `serializeError(CommandSender, String, TagResolver...)` | A `Component` in the error color, without prefix. |
| `translatePlain(CommandSender, String, TagResolver...)` | Plain text without any formatting, for example for tab completion. |

Pass the receiver as `CommandSender` where you have one, so the text is in their language. Each method also has a
variant without it, which uses the default language.

## Replacements

Placeholders in a message are MiniMessage tags. Fill them with `Replacement`:

```java
import de.eldoria.eldoutilities.messages.Replacement;

sender.sendMessage(player, "You picked up <amount> <item>.",
        Replacement.create("amount", 5),
        Replacement.create("item", "<gold>diamonds"));
```

Keys are made lower case and spaces become `_`, so write the tag in lower case: `Replacement.create("Item Name", …)`
fills `<item_name>`.

| Method | Value |
|---|---|
| `create(String, String)`, `create(String, Object)` | Parsed as MiniMessage. Alias: `replacement`. |
| `unparsed(String, Object)` | Shown as plain text. Tags in the value stay visible as text. |
| `create(String, Double)`, `create(String, Float)` | Formatted with two decimals in the server's locale: `1.50`, or `1,50` on a German server. Alias: `number`. |
| `create(String, Enum)` | The constant's `name()`. Alias: `name`. |
| `create(String, Player)` | The player's name as plain text. Alias: `player`. |
| `create(String, World)` | The world's name as plain text. Alias: `world`. |

:::warning[Player input]
`Replacement.create` parses the value as MiniMessage. A value a player typed, like a home name or a sign text, can
then add colors, click actions and hover texts to your message. Use `Replacement.unparsed` for every value that
comes from players or other untrusted places.
:::

```java
sender.sendMessage(player, "home.saved", Replacement.unparsed("name", homeName));
```

## Locale keys inside messages

The `<i18n:key>` tag inserts the text of a locale key, in the receiver's language. It works in every message the
sender handles, also in plain text and inside localized texts. Replacements of the message apply inside the
inserted text too.

```java
sender.sendMessage(player, "<i18n:shop.header> <gold><price>",
        Replacement.create("price", 20));
```

Without a localizer the tag inserts the key itself.

## Longer messages

`MessageComposer` (from the `localization` module) builds a message from several parts:

```java
import de.eldoria.eldoutilities.localization.MessageComposer;

var message = MessageComposer.create()
        .localeCode("shop.header")
        .newLine()
        .localeCode("shop.price", Replacement.create("price", 20))
        .newLine()
        .text("<gray>%s items left", stock)
        .prependLines();

sender.sendMessage(player, message);
```

`localeCode` adds an `<i18n:…>` tag and collects its replacements. `text` accepts a format string like
`String.format`. `prependLines()` puts 25 empty lines in front of the message, which pushes older chat out of
view. `fillLines()` only adds as many as the message needs to fill 25 lines.

## Legacy color codes

Old `&` and `§` color codes in a message are converted to MiniMessage tags, and the plugin logs a warning with the
message. A `§` code is always converted. A `&` code is only converted at the start of a message, after a character
that is neither a letter nor a digit, or right after another code. So `&aReady` and `&l&aReady` become colored, but
`rock&roll` and `?a=1&b=2` stay as they are. Write MiniMessage instead of color codes.
