/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.inventory;

import de.eldoria.eldoutilities.plugin.EldoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A InventoryActionHandler allows to handle basic click uis easily.
 *
 * @since 1.1.1
 */
public class InventoryActionHandler implements Listener {
    private static final Map<Class<? extends Plugin>, InventoryActionHandler> PLUGIN_HANDLER = new HashMap<>();

    private final Map<UUID, InventoryActions> inventories = new HashMap<>();

    private Runnable onClose = () -> {
    };

    public InventoryActionHandler() {
    }

    public InventoryActionHandler(Runnable onClose) {
        this.onClose = onClose;
    }

    public static InventoryActionHandler create(Plugin plugin) {
        return PLUGIN_HANDLER.computeIfAbsent(plugin.getClass(), k -> {
            var handler = new InventoryActionHandler();
            Bukkit.getPluginManager().registerEvents(handler, plugin);
            return handler;
        });

    }

    public static InventoryActionHandler create(EldoPlugin plugin, Runnable onClose) {
        return PLUGIN_HANDLER.computeIfAbsent(plugin.getClass(), k -> {
            var handler = new InventoryActionHandler(onClose);
            plugin.registerListener(handler);
            return handler;
        });

    }

    public static InventoryActionHandler getPluginInventoryHandler(Class<? extends Plugin> plugin) {
        return PLUGIN_HANDLER.computeIfAbsent(plugin, k -> new InventoryActionHandler());
    }

    /**
     * Wraps an inventory in inventory actions and registers it.
     *
     * @param player    player of intenvory actions
     * @param inventory inventory with actions
     * @return inventory wrapped into inventory actions.
     */
    public InventoryActions wrap(Player player, Inventory inventory) {
        var inventoryActions = InventoryActions.of(inventory);
        inventories.put(player.getUniqueId(), inventoryActions);
        return inventoryActions;
    }

    public InventoryActions wrap(Player player, Inventory inventory, Consumer<InventoryCloseEvent> onClose) {
        var inventoryActions = InventoryActions.of(inventory, onClose);
        inventories.put(player.getUniqueId(), inventoryActions);
        return inventoryActions;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (inventories.containsKey(event.getPlayer().getUniqueId())) {
            inventories.remove(event.getPlayer().getUniqueId()).onInventoryClose(event);
        }
        onClose.run();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (inventories.containsKey(event.getWhoClicked().getUniqueId())) {
            inventories.get(event.getWhoClicked().getUniqueId()).onInventoryClick(event);
            event.setCancelled(true);
        }
    }

}
