---
title: Threading and metrics
sidebar_position: 10
---

# Threading and metrics

## Threading

The `threading` module has helpers that spread work over server ticks, and futures that hand their result back to
the server thread.

:::warning
Everything in this module runs on the Bukkit scheduler. Folia has no Bukkit scheduler, so a plugin that should run on
Folia cannot use this module. Use the region, entity, global region and async schedulers of the server instead. They
exist on Paper as well.
:::

Add it with `de.eldoria.util:threading`. See [Setup](./setup.md).

### ReschedulingTask

`ReschedulingTask` is the base of the workers below. It runs your `run()` method once per tick while it is scheduled.

| Method | What it does |
|---|---|
| `schedule()` | Starts the task, if it is not running yet. |
| `cancel()` | Stops the task. It can be scheduled again. |
| `shutdown()` | Stops scheduling for good. `schedule()` does nothing after this. |
| `isRunning()`, `isActive()` | Whether it runs, and whether it may still be scheduled. |

### QueuingSelfSchedulingTask

A queue that works through its objects on the server thread, a few per tick. You add objects with `schedule(object)`
and handle each one in `execute(object)`. The task schedules itself when it gets work.

- Each tick it handles objects until the queue is empty or 50 ms have passed. A smaller limit can be passed to the
  constructor; a larger one is cut to 50 ms.
- Override `proceed(object)` to leave the next object in the queue until a later tick.
- `tick()` runs once per tick before the queue is worked on.
- After 200 idle ticks (or the number you pass) the task stops until new work arrives.
- `shutdown()` handles everything left in the queue right away. `clear()` drops the queue and stops the task.

```java
public class SaveQueue extends QueuingSelfSchedulingTask<PlayerData> {
    public SaveQueue(Plugin plugin) {
        super(plugin);
    }

    public void save(PlayerData data) {
        schedule(data);
    }

    @Override
    public void execute(PlayerData data) {
        data.write();
    }
}
```

### SelfSchedulingWorker

Like the queue, but it keeps its objects. Every tick it calls `execute(object)` for each registered object, until it
is removed with `unregister(object)`. You choose the collection in `getQueueImplementation()` and add objects with
`register(object)`. After 200 ticks without objects (or the number you pass) it stops until a new one is registered.

### DelayedActions

`DelayedActions.start(plugin)` gives you one scheduler for many delayed actions. `schedule(runnable, delay)` runs the
action after `delay` ticks; a delay of 0 runs it right away. It uses the same 50 ms limit per tick as the queue, so
many actions that come due at once are spread over several ticks.

### IteratingTask

An `IteratingTask` is a `BukkitRunnable` that works through a collection, 50 ms per run. The processor returns `true`
for each element it handled. When the collection is done, the task cancels itself and passes a `TaskStatistics`
with the count of handled elements and the time spent to your consumer.

```java
IteratingTask<Block> task = new IteratingTask<>(blocks, block -> {
    if (block.getType() != Material.STONE) return false;
    block.setType(Material.AIR);
    return true;
}, stats -> getLogger().info("Cleared %d blocks in %d ms".formatted(stats.getProcessedElements(), stats.getTime())));

task.runTaskTimer(plugin, 0, 1);
```

### Futures

`CompletableBukkitFuture.supplyAsync(...)` and `runAsync(...)` run work off the server thread, like the matching
`CompletableFuture` methods, and return a `BukkitFutureResult`. Its `whenComplete` callbacks run on the server thread
through the Bukkit scheduler, so they may use the Bukkit API.

```java
CompletableBukkitFuture.supplyAsync(() -> database.loadHome(player.getUniqueId()))
        .whenComplete(plugin, home -> player.teleport(home));
```

| Method of `FutureResult` | What it does |
|---|---|
| `whenComplete(plugin, callback)` | Runs the callback on the server thread. Errors are logged. |
| `whenComplete(plugin, callback, errorHandler)` | Same, with your own error handler. |
| `whenComplete(callback)` | Uses the first `EldoPlugin` that started as the owner of the task. Prefer passing your plugin. |
| `join()` | Waits for the result. Only call it off the server thread. |
| `asFuture()` | The result as a plain `CompletableFuture`. |

`BukkitFutureResult.of(future)` wraps a `CompletableFuture` you already have.

## Metrics

The `metrics` module has one helper for [bStats](https://bstats.org): `EldoMetrics.isEnabled(plugin)` reads
`plugins/bStats/config.yml` and tells you whether the server owner left metrics on.

```java
new Metrics(this, 12345);
if (EldoMetrics.isEnabled(this)) {
    getLogger().info("Metrics enabled. Thank you!");
}
```

The module does not bring bStats itself. Add `org.bstats:bstats-bukkit` to your plugin, shade it into the jar and
relocate it. bStats refuses to run when it is not relocated, so it is the one library that stays in the jar instead
of going through the Paper library loader:

```kotlin
tasks.shadowJar {
    relocate("org.bstats", "com.example.myplugin.libs.bstats")
}
```
