---
title: Debug reports
sidebar_position: 9
---

# Debug reports

The `debugging` module builds a debug report about your plugin and the server and uploads it to
[Lyna](https://lyna.eldoria.de). The admin gets a link they can hand to whoever helps them. Your plugin decides what
the report holds besides the basics.

## Add the debug command

The easiest way is the `DefaultDebug` sub command from the `commands` module. Add it to your main command:

```java
CommandMeta.builder("myplugin")
        .withSubCommand(new DefaultDebug(plugin, "myplugin.debug"))
        .build();
```

`/myplugin debug` then uploads a report. The command needs the permission you pass, or `eldoutilities.debug`. A
third argument takes your own [settings](#settings).

To start an upload from your own code, call `DebugUtil.dispatchDebug(sender, plugin, settings)`.

### Consent

The first request on a server does not upload anything. It explains what is sent and asks to run the command again.
That consent is kept as `debugConsent: true` in `plugins/EldoUtilities/config.yml`. Setting it to `false` withdraws
it.

## What a report holds

In the order the viewer shows it:

1. Health checks, worst first.
2. `User Data`: the build data of your plugin jar, its hashes and `Premium` (see [below](#premium-means-built-on-ci)).
3. What your plugin adds about itself.
4. Charts.
5. The server's `latest.log`, and the lines your plugin wrote, if any.
6. Errors and warnings, from your plugin and from others, each with a count and when it was first and last seen.
7. Your configuration files.

The report also carries data about the plugin and the server, such as installed plugins and their versions.

The server and your plugin are asked about themselves on the thread the command runs on. Reading the log and the
files, filtering and uploading then run in the background. The sender gets a message with a read link and a delete
link once the upload is done, or a message that says why it failed. Lyna deletes a report after two weeks.

## Add data about your plugin

`EldoPlugin` already implements `DebugDataProvider`, so your plugin class is where a report starts. Override the
methods you need:

```java
public class MyPlugin extends EldoPlugin {
    @Override
    public @NotNull EntryData[] getDebugInformations() {
        return new EntryData[]{
                new EntryData("Storage", Map.of(
                        "Type", storage.type(),
                        "Pool size", String.valueOf(storage.poolSize())))
        };
    }

    @Override
    public @NotNull List<HealthCheck> getHealthChecks() {
        return List.of(storage.isConnected()
                ? HealthCheck.ok("Database", "Connected")
                : HealthCheck.error("Database", "Not connected"));
    }
}
```

| Method | Returns |
|---|---|
| `getDebugInformations()` | `EntryData` entries. `new EntryData(name, text)` is a block of text, `new EntryData(name, map)` a table of keys and values in map order. |
| `getHealthChecks()` | `HealthCheck` outcomes, made with `HealthCheck.ok`, `warn` or `error` from a name and a message. |
| `getCharts()` | Charts, see [Charts](#charts). |
| `getDebugProviders()` | Other `DebugDataProvider`s to ask as well. |

Only the plugin class is asked first. Any other class that implements `DebugDataProvider` is only asked when a
provider returns it from `getDebugProviders()`. Each provider is asked once; a loop is logged as a warning.

Health checks and charts run on the thread of the debug command. Keep them quick and only copy data you already
have.

## Charts

Each chart type has a class with a static factory and fluent setters:

| Class | Factories | Data |
|---|---|---|
| `SeriesChart` | `line`, `area`, `bar`, `stackedBar` | `series(name, values)`, `xLabels(labels)` |
| `ScatterChart` | `scatter` | `point(series, x, y)` |
| `CategoryChart` | `bar`, `pie`, `donut` | `value(category, value)` |
| `HeatmapChart` | `heatmap` | `row(values)` or `cell(x, y, value)`, `xLabels`, `yLabels`, `xLabel`, `yLabel` |
| `GaugeChart` | `gauge(name, value)` | `range(min, max)`, 0 to 100 by default |

Every chart takes `description(text)` and `unit(unit)`. `SeriesChart` and `ScatterChart` also take `xLabel`,
`yLabel`, `yRange(min, max)` and `mark(value, label)` for a horizontal reference line.

```java
@Override
public @NotNull List<ChartData<?>> getCharts() {
    return List.of(SeriesChart.line("Tick time")
            .unit("ms")
            .series("Tick", timings.lastMinute())
            .mark(50, "One tick"));
}
```

For anything else, `ChartData.of(name, type, data)` sends data of any shape for a `ChartType`.

## Settings

`DebugSettings` decides where reports go, what is hidden, and which files are included. `DebugSettings.DEFAULT` sends
to `https://lyna.eldoria.de` with the default filters.

```java
DebugSettings settings = DebugSettings.builder()
        .withDefaultFilter()
        .withFilter(new Filter(Pattern.compile("license: \\S+"), "license: *****"))
        .build();
```

| Builder method | What it does |
|---|---|
| `forHost(String)` | Sends reports to another Lyna instance. |
| `withDefaultFilter()` | Adds the default filters. |
| `withFilter(Filter...)` | Adds a regex `Filter` with its replacement. |
| `withConfigFiles(Function<Plugin, List<Path>>)` | Chooses the configuration files. |

A builder without `withDefaultFilter()` hides nothing. Everything in a report passes the filters before it leaves the
server.

The default filters hide passwords, tokens, secrets and API keys in YAML, properties and JSON form, credentials in
URLs, user names, and IP addresses. An IPv4 address keeps its first two parts, and `127.x.x.x` stays.

### Configuration files

By default a report carries every `.yml`, `.yaml`, `.json`, `.properties` and `.toml` file in your plugin folder
and up to four folders below it. A file larger than 1 MB is left out, and files are taken until 5 MB are reached.
These limits also apply to files you choose with `withConfigFiles`. Call `ConfigFiles.inDataFolder(plugin)` in your
function to extend the default instead of replacing it.

:::warning
The filters catch the usual names, but not every value an admin may consider private. Keep secrets in keys whose
name contains `password`, `token`, `secret` or `apikey`, or add your own filter.
:::

## Premium means built on CI

`UserData.get(plugin).isPremium()` is `true` when the build data says `type=PUBLIC`, which publishdata writes for
builds made on CI. It does not mean the jar was bought. Do not use it as a licence check.
