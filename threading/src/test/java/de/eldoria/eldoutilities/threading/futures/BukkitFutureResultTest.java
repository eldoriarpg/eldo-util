/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.threading.futures;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

class BukkitFutureResultTest {
    String result = "";
    Plugin plugin = null;
    Player player = null;

    @Test
    void of() {
        // direct use
        CompletableBukkitFuture.supplyAsync(() -> {
            //Some heavy async calling to database or other stuff
            return result;
        }).whenComplete(plugin, result -> {
            player.sendMessage(result);
        });

        // Wrap a completeable future
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            //Some heavy async calling to database or other stuff
            return result;
        });

        BukkitFutureResult.of(future)
                .whenComplete(plugin, result -> {
                    player.sendMessage(result);
                });
    }
}
