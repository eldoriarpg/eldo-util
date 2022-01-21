/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A class which represents an item in a slot which will execute actions on click on the item and on close of the inventory.
 *
 * @since 1.1.1
 */
public class ActionItem {
    private final ItemStack itemStack;
    private final int slot;
    private final Consumer<InventoryClickEvent> onClick;
    private final Consumer<ItemStack> onClose;

    /**
     * Creates a new Action Item. A action item will be passed to the {@link InventoryActions} wrapper.
     * <p>
     * The {@link #itemStack} will be inserted at the given {@link #slot} and will not leave this slot until the inventory is closed.
     *
     * @param itemStack item for the action
     * @param slot      position of the item
     * @param onClick   This consumer will be called for the clicked item stack and only for the clicked item stack.
     *                  <p>
     *                  It will be only called when the item stack is clicked and not when another item stack inside this inventory is clicked.
     *                  <p>
     *                  The click event will be canceled after forwarding it to the action item.
     * @param onClose   This method is called for each registered item stack individually when an inventory is closed.
     *                  <p>
     *                  The event will not be passed.
     *                  <p>
     *                  Use this method to save the settings.
     */
    public ActionItem(ItemStack itemStack, int slot, Consumer<InventoryClickEvent> onClick, Consumer<@Nullable ItemStack> onClose) {
        this.itemStack = itemStack;
        this.slot = slot;
        this.onClick = onClick;
        this.onClose = onClose;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getSlot() {
        return slot;
    }

    /**
     * This method is called for each registered item stack individually when an inventory is closed.
     * <p>
     * The event will not be passed.
     * <p>
     * Use this method to save the settings.
     *
     * @param itemStack item stack in the closed inventory.
     */
    public void onInventoryClose(ItemStack itemStack) {
        onClose.accept(itemStack);
    }

    /**
     * This method will be called for the clicked item stack and only for the clicked item stack.
     * <p>
     * It will be only called when the item stack is clicked and not when another item stack inside this inventory is clicked.
     * <p>
     * The click event will be canceled after forwarding it to the action item.
     *
     * @param event click event of the clicked item stack
     */
    public void onInventoryClick(InventoryClickEvent event) {
        onClick.accept(event);
    }
}
